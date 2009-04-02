/* 
 * File: PeakFilterer.java
 *
 * Copyright (C) 2009, Ruth Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Rev$
 */
package DataSetTools.components.ui.Peaks;

import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.ViewTools.Components.ViewControls.ColorScaleControl.StretchTopBottom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;




public class PeakFilterer extends JButton
{
   public static String OMITTED_PEAKS_CHANGED ="Omitted peaks changed";
   MyActionListener listener;
   Vector<ActionListener> FilterListeners;
   Vector<Peak_new> peaks;
   //add reflag
   private static String[] Fields={
        "row","col","channel","intensity","d-spacing","time",
        "qx","qy","qz","h","k","l","h int offset","k int offset",
        "l int offset","seq nums", "run nums", "det nums"};
   private float[] Mins;
   private float[] Maxs;
   JList  list;
   int LastIntervalIndex = 14;
   Vector<OneFilterElement> CurrentAndList;
   Vector<Vector<OneFilterElement>> OmitRule;
   int[] omittedSeqNums;
   String PeakMainFloatRes =";row;col;channel;iintensity;time;h;k;l;";
   String PeakQCalcRes =";d-spacing;qx;qy;qz;";
   String PeakIntArrayRes =";seq nums;run nums;det nums;";
   public PeakFilterer( Vector<Peak_new> Peaks)
   {
      super("Peak Filterer");
      listener = new MyActionListener();
      addActionListener( listener);
      omittedSeqNums = null;
      FilterListeners = new Vector<ActionListener>();
      peaks =Peaks;
      Mins = new float[Fields.length];
      Maxs = new float[ Fields.length];
      java.util.Arrays.fill( Mins , Float.POSITIVE_INFINITY );
      java.util.Arrays.fill(  Maxs ,Float.NEGATIVE_INFINITY );
      for( int i=0; i< peaks.size(); i++)
      { 
         IPeak pk = peaks.elementAt( i );
         Set( Mins,Maxs,0, pk.y());
         Set( Mins,Maxs,1, pk.x());
         Set( Mins,Maxs,2, pk.z());
         Set( Mins,Maxs,3, pk.ipkobs());
         float[] Qs = pk.getUnrotQ();
         float Q = (new Vector3D(Qs[0],Qs[1],Qs[2])).length();
         Set(Mins, Maxs, 4, 1f/Q);
         Set( Mins,Maxs,5, pk.ipkobs());
         Set( Mins,Maxs,6, pk.ipkobs());
         Set( Mins,Maxs,7, pk.ipkobs());
         
         Set( Mins,Maxs,8, pk.time());
         Set( Mins,Maxs,9, Qs[0]);
         Set( Mins,Maxs,10,Qs[1]);
         Set( Mins,Maxs,11, Qs[2]);

         Set( Mins,Maxs,12, pk.h());
         Set( Mins,Maxs,13, pk.k());
         Set( Mins,Maxs,14, pk.l());
         
         
         Set( Mins,Maxs,1, Qs[2]);
         Set( Mins,Maxs,1, Qs[2]);
         
      }
      
      Mins[12] =Mins[13]=Mins[14] =-.5f;
      Maxs[12] =Maxs[13]=Maxs[14] =.5f;
      
      for( int i=18; i <Fields.length ; i++ )
         Maxs[i]=Mins[i] = Float.NaN;
      
      list = new JList( Fields );
      
      list.setBorder( new TitledBorder( new LineBorder( Color.black), "Start AND Seq") );
      list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      list.addMouseListener( listener );
       CurrentAndList = new Vector<OneFilterElement>();
       
      OmitRule = new Vector<Vector<OneFilterElement>> ();
   }
   
   public int[] getOmittedSequnceNumbers()
   {
      return omittedSeqNums;
   }
   /**
    *  Do this when the peaks object is indexed, The hkl are changed
    */
   public void set_hklMinMax()
   {
      for( int i=12; i<=14; i++){
         Mins[i]= Float.POSITIVE_INFINITY;
         Maxs[i]= Float.NEGATIVE_INFINITY;
      }
      for( int i=0; i< peaks.size(); i++)
      {
         IPeak pk = peaks.elementAt( i );
         Set( Mins,Maxs,12, pk.h());
         Set( Mins,Maxs,13, pk.k());
         Set( Mins,Maxs,14, pk.l());
      }
   }
   private void Set( float[] Mins, float[] Maxs, int index, float val)
   {
      if( val < Mins[index])
         Mins[index] =val;

      if( val > Maxs[index])
         Maxs[index] =val; 
   }
      
  private  int[] CalcOmits( Vector<Vector <OneFilterElement> >omitRule)
  {
     Vector<Integer> omit = new Vector<Integer>();
     for( int pk = 0 ; pk < peaks.size() ; pk++ )
      {
        IPeak peak1 = peaks.elementAt( pk );
         for( int i = 0 ; i < omitRule.size() ; i++ )
         {
            Vector< OneFilterElement > AndSequence = omitRule.elementAt( i );
            boolean Res = true;
            for( int k = 0 ; k < AndSequence.size() && Res ; k++ )
            {
               OneFilterElement elt = AndSequence.elementAt( k );
               float n =1;
               if (!elt.inside)
                  n = -1;
               String FieldName = ";"+Fields[ elt.fieldIndex]+";";
               if( elt.fieldIndex <= LastIntervalIndex )
               {
                  float val;
                  if( PeakMainFloatRes.indexOf( FieldName) >=0)
                     val = getFloatValMain( peak1 , elt.fieldIndex);
                  else if( PeakQCalcRes.indexOf( FieldName )>=0)
                     val = getFloatValQCalc( peak1 ,  elt.fieldIndex);
                  else 
                     val= getFloatOtherCalc(  peak1 , FieldName.charAt(1));
                  boolean r1 =true, r2=true;
                  if( (elt.min - val )*n > 0)
                     r1 = false;
                  if( (elt.max -val)*n < 0  )
                     r2 = false;
                 if( n < 0)
                    Res = r1 || r2;
                 else
                    Res = r1 && r2;
                  
               }else if( elt.list != null && elt.list.length >0)
               { int val =-1;
                 if(FieldName.indexOf(1) == 's' )
                    val = peak1.seqnum();
                 else if(FieldName.indexOf(1) == 'r' )
                    val = peak1.nrun();
                 else  if(FieldName.indexOf(1) == 'd' )
                    val = peak1.detnum();
                 if( java.util.Arrays.binarySearch( elt.list , val ) < 0)
                    Res = false;
               }
               

            }
            if( Res ) omit.addElement(  peak1.seqnum() );
         }
      }
     
     int[] res = new int[ omit.size() ];
     for( int i=0; i< res.length ; i++)
        res[i]= omit.elementAt( i ).intValue();
     
     return res;
  }
  
   // For users to get notif ied of changes
   public void addFilterListener( ActionListener ev)
   {
      if( ev != null && !FilterListeners.contains( ev ))
         FilterListeners.add(  ev );
      
   }
   
   public void removeFilterListener( ActionListener ev)
   {
      if( ev != null && FilterListeners.contains( ev ))
         FilterListeners.remove(  ev );
      
   }
   
   private void fireFilterListeners()
   {
      for( int i=0; i< FilterListeners.size(); i++)
      {
         ActionEvent evt = new ActionEvent( this, ActionEvent.ACTION_PERFORMED, 
                  OMITTED_PEAKS_CHANGED);
         
         RunInEventQueue R =(new RunInEventQueue( evt ,
                                                FilterListeners.elementAt(i)));
      }
      
      
        
   }
   
   
   
   class RunInEventQueue extends Thread 
   {
      ActionEvent Evt;
      ActionListener Listener;
      
      public RunInEventQueue( ActionEvent evt, ActionListener listener)
      {
         Evt = evt;
         Listener = listener;
         if( EventQueue.isDispatchThread())
           run();
         else
            SwingUtilities.invokeLater( this );
      }
      public void run()
      {
         Listener.actionPerformed( Evt );
      }
   }
   
   class MyActionListener extends MouseAdapter implements ActionListener  
                                              // ,ListSelectionListener
                                
   {

      public String START_AND ="New(OR'd)";
      public String AND_PREV ="Add(And) to Prev";
      public String CALC_FILTER ="Set Filter";
      public String CLEAR_OMITS = "Clear omitted Peaks";
      JButton but;
      Popup Pop = null;
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

        if( e.getSource()instanceof JButton)
        {
           MakeMenu( (Component)e.getSource());
           but = (JButton) e.getSource();
           System.out.println("LOC+"+but.getLocation());
           return;
        }
        String evtString = e.getActionCommand();
        if( evtString ==START_AND)
        {
           if( CurrentAndList.size() > 0)
              OmitRule.addElement(  CurrentAndList );
           CurrentAndList = new Vector<OneFilterElement>();
           MakeFieldMenu( "START", but);
           return;
        }
        if( evtString ==AND_PREV)
        {
           MakeFieldMenu("AND", but);
           return;
        }
        if( evtString ==CALC_FILTER)
        {
           if( CurrentAndList.size() > 0)
              OmitRule.addElement( new Vector<OneFilterElement>( CurrentAndList) );
           
           CurrentAndList = new Vector<OneFilterElement>();
           omittedSeqNums = CalcOmits( OmitRule );
           fireFilterListeners();
           OmitRule.clear();
           return;
        }
        if( evtString == CLEAR_OMITS)
        {
           CurrentAndList = new Vector<OneFilterElement>();
           OmitRule = new Vector< Vector< OneFilterElement>>();
        }
      }
      
      public void mouseClicked(MouseEvent e)
      {
         if( e.getSource() != list)
            return;
         
         int k = list.locationToIndex(e.getPoint());

         OneFilterElement F;
         Pop.hide();
         if( k <= LastIntervalIndex )
         {
            
           IntervalDialog filtElt = new IntervalDialog( Fields[k], Mins[k],
                    Maxs[k]);
           if( filtElt.MinVal() == Mins[k] && filtElt.MaxVal() == Maxs[k])
              F = null;
           else
             F = new OneFilterElement( k,filtElt.MinVal(),
                    filtElt.MaxVal(), null, filtElt.Inside);
           
         }else
         {
            String res = JOptionPane.showInputDialog("Enter "+Fields[k]+
                                                            " list" );
            if(res == null)
               F = null;
            else
            {
               int[] listt = IntList.ToArray( res );
               F = new OneFilterElement( k , Float.NaN , Float.NaN , listt, false );
            }
            
            
         }
         if( F != null)
            CurrentAndList.addElement( F );

        // list.removeListSelectionListener( listener );
         if( Pop !=null)
            Pop.hide();
      }
      
      /* Did not work well. Too many valueChanged events occurred with littl ways for
       * determinimg what they are.
       * (non-Javadoc)
       * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
       */
    
      public void valueChanged( ListSelectionEvent e )
      {

         int k = e.getLastIndex();
         System.out.println("k="+k+","+e.getLastIndex());
         OneFilterElement F;
         Pop.hide();
         if( k <= LastIntervalIndex )
         {
            System.out.println("yyyyyyyyyyyyyyyyyyyyy");
           // if( e.getValueIsAdjusting())
           //    return;
           IntervalDialog filtElt = new IntervalDialog( Fields[k], Mins[k],
                    Maxs[k]);
           if( filtElt.MinVal() == Mins[k] && filtElt.MaxVal() == Maxs[k])
              F = null;
           else
             F = new OneFilterElement( k,filtElt.MinVal(),
                    filtElt.MaxVal(), null, filtElt.Inside);
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxx");
         }else
         {
            String res = JOptionPane.showInputDialog("Enter "+Fields[k]+
                                                            " list" );
            if(res == null)
               F = null;
            else
            {
               int[] listt = IntList.ToArray( res );
               F = new OneFilterElement( k , Float.NaN , Float.NaN , listt ,false);
            }
            
            
         }
         if( F != null)
            CurrentAndList.addElement( F );

        // list.removeListSelectionListener( listener );
         if( Pop !=null)
            Pop.hide();
      }

      private void MakeFieldMenu(  String message, Object obj)
      {
         if( Pop != null)
            Pop.hide();
         String Title= "AND to Current AND sequence";
         if( message.startsWith( "START" ))
            Title = "Start AND Seq";
         
         ((TitledBorder)(list.getBorder())).setTitle( Title );
         Component comp =(Component)obj;
         Point P = getScreenLoc( comp);
         list.clearSelection();

         //list.addListSelectionListener( listener );;
         Pop = PopupFactory.getSharedInstance().getPopup( (Component)obj ,
                  list, P.x , P.y );
         
         Pop.show();
         
         
        
      }
      private Point getScreenLoc( Component comp)
      {
         if( comp == null)
            return null;
         Point P = comp.getLocation();
         if( comp instanceof Window)
            return P;
         Point P1 =  getScreenLoc( comp.getParent());
         return new Point( P.x+P1.x, P.y+P1.y);
      }
      
      private void MakeMenu( Component comp)
      {
         JPopupMenu popUp = new JPopupMenu("Peak Filter");
         JMenuItem item =new JMenuItem(START_AND);
         item.setToolTipText( "<html><body>Starts an AND sequence of conditions <BR>"+
                  " This sequence will be OR'd with previous and sequences"+
                  "</body></html>");
         popUp.add( item ).addActionListener( this);
         item =new JMenuItem(AND_PREV);
         item.setToolTipText( "ANDs the next condition to the current"+
                  " AND sequence only ");
         popUp.add( item ).addActionListener( this);
         popUp.add( CLEAR_OMITS).addActionListener( this );
         item =new JMenuItem(CALC_FILTER);
         item.setToolTipText( "Calculates the omitted Peaks, Notifies listeners");
         popUp.add( item ).addActionListener( this);
         popUp.show( comp,0,0 );
      }
      
   }
   
   public static void main( String[] args)
   {
     /* PeakFilterer but = new PeakFilterer();
      JFrame jf = new JFrame();
      jf.getContentPane().add(  but );
      jf.setSize( 300,400);
      jf.setVisible( true);
      */
   }

   private static JFrame getJFrame(String Message )
   {
      JFrame jf = new JFrame( Message );
      jf.setSize(  200,300 );
      jf.setVisible( false );
      jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
      return jf;
   }
   class IntervalDialog  extends JDialog implements ActionListener
   {
      float MinVal,
            MaxVal;
      float minimum,
            maximum;
      boolean Inside;
      String Message;
      JCheckBox inside;
      StretchTopBottom sliders;
      public IntervalDialog( String message, float minVal, float maxVal)
      {
         super( getJFrame(message),message, true);
         MinVal = minimum = minVal;
         MaxVal = maximum = maxVal;
         Message = message;
         Inside = true;
         //-----------------------------
         JPanel contentPane= new JPanel();
         contentPane.setLayout(  new BorderLayout() );
         
           JPanel buttonPanel = new JPanel();
           BoxLayout blayout = new BoxLayout( buttonPanel , BoxLayout.X_AXIS);
           buttonPanel.setLayout( blayout );
           inside = new JCheckBox( "Inside interval", true);
           inside.setToolTipText( "False-outside of interval, otherwise inside" );
           buttonPanel.add( inside );
           buttonPanel.add( Box.createHorizontalGlue());
           
           JButton exit = new JButton("Exit/Save");           
           exit.addActionListener( this );
           buttonPanel.add( Box.createHorizontalGlue());
           JButton Cancel = new JButton("Cancel");
           Cancel.addActionListener(  this );
           buttonPanel.add(Cancel);
           
           buttonPanel.add( exit );
           buttonPanel.setBackground( java.awt.Color.GRAY );
           
           contentPane.add( buttonPanel , BorderLayout.NORTH);
           //-----------------
           sliders = new StretchTopBottom( MinVal, MaxVal);
           sliders.addActionListener( this );
           contentPane.add( sliders, BorderLayout.CENTER );
           
          setContentPane( contentPane);
          setSize(300,400);
          setVisible( true);
         
      }
      public void setMaxMinVal( float max, float min)
      {
          minimum = min;
          maximum = min;
          sliders.setMaxMin( maximum , minimum );
      }
      
      public float MinVal()
      {
         return minimum;
      }
      public float MaxVal()
      {
         return maximum;
      }
      public boolean inside()
      {
         return Inside;
      }
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {
         String evtString = e.getActionCommand();
         if( evtString.equals( "Exit/Save" ))
         {
            minimum= sliders.getBottomValue();
            maximum = sliders.getTopValue();
            Inside = inside.isSelected();
           
            dispose();
            return;
         }
         if( evtString.equals( "Cancel" ))
         {
            dispose();
            return;
         }
         if( e.getSource() instanceof StretchTopBottom)
         {
           StretchTopBottom str = (StretchTopBottom)(e.getSource());
           if( str.getTopValue() < str.getBottomValue())
           {
              float val = str.getTopValue();
              str.setControlValue(  val, StretchTopBottom.BOTTOM_VALUE );
              repaint();
           }
         }
         
      }
      
   }
   
   class OneFilterElement
   {
      public int fieldIndex;
      public float min, max;
      public int[] list;
      public boolean inside;
      public OneFilterElement( int FieldIndex, float Min, float Max,
                            int[] List, boolean Inside )
      {
        fieldIndex = FieldIndex;
        min = Min;
        max = Max;
        list = List;
        inside = Inside;
      }
      
   }  
   
  //;row;col;channel;iintensity;time;h;k;l;=0,1,2,3,5
   private float getFloatValMain( IPeak pk, int k)
   {
      if( k < 2)
        if( k==0)
           return pk.y();
        else
           return pk.x();
         
      else if( k > 2)
         if( k==3)
            return pk.ipkobs();
         else
            return pk.time();
         
     else
         return pk.z();
   }
   
   //;d-spacing;qx;qy;qz;=4,6,7,8
   private float getFloatValQCalc( IPeak pk, int k)
   {
      float[] Qs = pk.getUnrotQ();
      if( k > 8)
         if( k <=11)
           return Qs[k-9];
         else
            return Float.NaN;
      float Q = (float)Math.sqrt( Qs[0]*Qs[0]+Qs[1]*Qs[1]+Qs[2]*Qs[3]);
      return 1f/Q;
      
   }
     // ch is h,k,l,x,y,z
      private float  getFloatOtherCalc(IPeak pk, char c )
      {
         if( c=='h')
            return pk.h()-(float)Math.floor( pk.h() );
         if( c=='k')
            return pk.k()-(float)Math.floor( pk.k() );
         if( c=='l')
            return pk.l()-(float)Math.floor( pk.l() );
         return Float.NaN;
      }
  
}
