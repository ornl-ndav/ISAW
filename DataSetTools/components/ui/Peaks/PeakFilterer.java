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
import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Components.ViewControls.ColorScaleControl.StretchTopBottom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
//import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;


/**
 * Class that handles filtering of peaks. It also listens for notification when
 * the Peaks are indexed or h,k, and l values are changed. The event command is 
 * "Peaks Indexed"
 * 
 * @author Ruth
 *
 */
public class PeakFilterer extends JButton implements ActionListener
{



   /**
    *  The Action command that is sent to listeners of this class
    */
   public static String                OMITTED_PEAKS_CHANGED = "Omitted peaks changed";

   /**
    *  The Action command that this class listens to.
    */
   public static String                PEAKS_ARE_INDEXED     = "Peaks Indexed";

   MyActionListener                    listener;

   /**
    * Vector of listeners for OMITTED_PEAKS_CHANGED command
    */
   Vector< ActionListener >            FilterListeners;

   /**
    * The peaks
    */
   Vector< Peak_new >                  peaks;

   //add reflag

   /**
    * The fields that can be used to filter peaks
    */
   private static String[]             Fields                =
                                                             {
            "row" , "col" , "channel" , "intensity" , "d-spacing" , "time" ,
            "qx" , "qy" , "qz" , "h" , "k" , "l" , "h int offset" ,
            "k int offset" , "l int offset" , "reflag", "seq nums" ,
            "run nums" ,"det nums"
                                                             };

   /**
    * The Minimum value for each of the numeric fields
    */
   private float[]                     Mins;


   /**
    * The Maximum value for each of the numeric fields
    */
   private float[]                     Maxs;

   // List box to display the choices
   JList                               list;

   //Index of the last numeric field 
   int                                 LastIntervalIndex     = 15;

   //Current list of conditions that will be anded to get omitted peaks
   Vector< OneFilterElement >          CurrentAndList;

   //list of and lists that are to be or'ed together
   Vector< Vector< OneFilterElement >> OmitRule;

   //Sequence numbers to be omitted. Set after CalcOmits method
   int[]                               omittedSeqNums;

   // List of fields with indicated calculation/input  method
   String                              PeakMainFloatRes      = 
                                ";row;col;channel;intensity;time;h;k;l;reflag;";

   String                              PeakQCalcRes          = 
                                  ";d-spacing;qx;qy;qz;";

   String                              PeakIntArrayRes       = 
                                               ";seq nums;run nums;det nums;";


   /**
    * Constructor:
    * Field Names are row, col, channel, intensity, time, h, k, l, reflag,
    *    h int offset, k int offset, l int offset, 
    *     d-spacing, qx, qy, qz, seq nums(*), run nums(*), det nums(*)
    *  Those field names followed by (*) need a list of integer as
    *  arguments.  All other need a range of values to omit or to not omit
    *     
    * @param Peaks  The Vector of Peaks
    */
   public PeakFilterer( Vector< Peak_new > Peaks )
   {

      super( "Peak Filterer" );
      peaks = Peaks;

      listener = new MyActionListener( this);
      addActionListener( listener );

      omittedSeqNums = null;
      FilterListeners = new Vector< ActionListener >();

      Mins = new float[ Fields.length ];
      Maxs = new float[ Fields.length ];
      java.util.Arrays.fill( Mins , Float.POSITIVE_INFINITY );
      java.util.Arrays.fill( Maxs , Float.NEGATIVE_INFINITY );

      //Caclulate current Mins and Maxs
      for( int i = 0 ; i < peaks.size() ; i++ )
      {
         IPeak pk = peaks.elementAt( i );
         Set( Mins , Maxs , 0 , pk.y() );
         Set( Mins , Maxs , 1 , pk.x() );
         Set( Mins , Maxs , 2 , pk.z() );
         Set( Mins , Maxs , 3 , pk.ipkobs() );
         
         float[] Qs = pk.getUnrotQ();
         float Q = ( new Vector3D( Qs[ 0 ] , Qs[ 1 ] , Qs[ 2 ] ) ).length();
         Set( Mins , Maxs , 4 , 1f / Q );

         Set( Mins , Maxs , 5 , pk.time() );
         
         Set( Mins , Maxs , 6 , Qs[ 0 ] );
         Set( Mins , Maxs , 7 , Qs[ 1 ] );
         Set( Mins , Maxs , 8 , Qs[ 2 ] );

         Set( Mins , Maxs , 9 , pk.h() );
         Set( Mins , Maxs , 10 , pk.k() );
         Set( Mins , Maxs , 11 , pk.l() );
         
         Set(Mins, Maxs,15, pk.reflag());

      }

      Mins[ 12 ] = Mins[ 13 ] = Mins[ 14 ] = - .5f;
      Maxs[ 12 ] = Maxs[ 13 ] = Maxs[ 14 ] = .5f;

      for( int i = LastIntervalIndex+1 ; i < Fields.length ; i++ )
         Maxs[ i ] = Mins[ i ] = Float.NaN;

      list = new JList( Fields );

      list.setBorder( new TitledBorder( new LineBorder( Color.black ) ,
               "Start AND Seq" ) );
      list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      list.addMouseListener( listener );
      CurrentAndList = new Vector< OneFilterElement >();

      OmitRule = new Vector< Vector< OneFilterElement >>();
   }

   public ActionListener getActionListener()
   {
      return listener;
   }
   public void kill()
   {
      FilterListeners.clear();
      FilterListeners = null;
      removeActionListener( listener);
      peaks = null;
      Mins = Maxs = null;
      list.removeAll();
      list = null;
      CurrentAndList.clear();
      OmitRule.clear();
      CurrentAndList= null;
      OmitRule = null;
      listener.kill();
      listener = null;
      
   }

   /**
    *
    * @return the current omitted sequence numbers
    */
   public int[] getOmittedSequenceNumbers()
   {

      return omittedSeqNums;
   }

   /**
    * Sets the omitted sequence numbers for the peaks.
    * There will be no notification. See fireFilterListeners to notify
    * 
    * @param omittedPeaks  the list of omitted sequence numbers
    */
   public void setOmittedSequenceNumbers( int[] omittedPeaks)
   {
      omittedSeqNums = omittedPeaks;
   }

   /**
    *  Allows for finding the range of h,k, and l values when the
    *  peaks are re-indexed
    */
   public void set_hklMinMax()
   {

      for( int i = 9 ; i <= 11 ; i++ )
      {
         Mins[ i ] = Float.POSITIVE_INFINITY;
         Maxs[ i ] = Float.NEGATIVE_INFINITY;
      }
      
      for( int i = 0 ; i < peaks.size() ; i++ )
      {
         IPeak pk = peaks.elementAt( i );
         Set( Mins , Maxs , 9 , pk.h() );
         Set( Mins , Maxs , 10 , pk.k() );
         Set( Mins , Maxs , 11 , pk.l() );
      }
   }


   private void Set( float[] mins , float[] maxs , int index , float val )
   {

      if( val < mins[ index ] )
         mins[ index ] = val;

      if( val > maxs[ index ] )
         maxs[ index ] = val;
      
      //Get max and min a little over/under an actual value
      float F = Math.min( .1f , ( maxs[index]-mins[index])/100 );
      if( F ==0)
         return;
      
      if( mins[index]== val)
         mins[index]= val - F;
      
      if( maxs[index]== val)
         maxs[index]= val + F;
      
      
      
   }


   //Calculate the sequnce numbers that should be omitted
   private int[] CalcOmits( Vector< Vector< OneFilterElement > > omitRule )
   {

      Vector< Integer > omit = new Vector< Integer >();

      for( int pk = 0 ; pk < peaks.size() ; pk++ )
      {
         IPeak peak1 = peaks.elementAt( pk );
         boolean omitted = false;
         for( int i = 0 ; i < omitRule.size() && ! omitted ; i++ )
         {
            Vector< OneFilterElement > AndSequence = omitRule.elementAt( i );
            omitted = true;

            for( int k = 0 ; k < AndSequence.size() && omitted ; k++ )//If anyone cond is false jump out
            {
               OneFilterElement elt = AndSequence.elementAt( k );

               float n = 1;
               if( ! elt.inside )
                  n = - 1;

               String FieldName = ";" + Fields[ elt.fieldIndex ] + ";";
               if( elt.fieldIndex <= LastIntervalIndex )
               {
                  float val;
                  if( PeakMainFloatRes.indexOf( FieldName ) >= 0 )

                     val = getFloatValMain( peak1 , elt.fieldIndex );

                  else if( PeakQCalcRes.indexOf( FieldName ) >= 0 )

                     val = getFloatValQCalc( peak1 , elt.fieldIndex );

                  else

                     val = getFloatOtherCalc( peak1 , FieldName.charAt( 1 ) );

                  boolean r1 = true , r2 = true;

                  if( ( elt.min - val ) * n > 0 )

                     r1 = false;

                  if( ( elt.max - val ) * n < 0 )

                     r2 = false;

                  if( n < 0 )

                     omitted = r1 || r2;

                  else

                     omitted = r1 && r2;

               }
               else if( elt.List != null && elt.List.length > 0 )
               {
                  int val = - 1;

                  if( FieldName.charAt( 1 ) == 's' )

                     val = peak1.seqnum();

                  else if( FieldName.charAt( 1 ) == 'r' )

                     val = peak1.nrun();

                  else if( FieldName.charAt( 1 ) == 'd' )

                     val = peak1.detnum();

                  if( java.util.Arrays.binarySearch( elt.List , val ) < 0 )

                     omitted = false;
               }


            }

            if( omitted )
               if( ! omit.contains( peak1.seqnum() ) )
                  omit.addElement( peak1.seqnum() );
         }
      }

      int[] res = new int[ omit.size() ];

      for( int i = 0 ; i < res.length ; i++ )

         res[ i ] = omit.elementAt( i ).intValue();

      return res;
   }


   /**
    * Adds an action listener to listen for the OMITTED_PEAKS_CHANGED action event
    * 
    * @param ev  The action listener to be notified when the omitted peaks should be changed.
    */
   public void addFilterListener( ActionListener ev )
   {

      if( ev != null && ! FilterListeners.contains( ev ) )

         FilterListeners.add( ev );

   }


   /**
    * Removes an action listener to listen for the OMITTED_PEAKS_CHANGED 
    *          action event
    * 
    * @param ev  The action listener that was to be notified when the omitted 
    *            peaks should be changed.
    */
   public void removeFilterListener( ActionListener ev )
   {

      if( ev != null && FilterListeners.contains( ev ) )
         FilterListeners.remove( ev );

   }


   public void fireFilterListeners()
   {

      for( int i = 0 ; i < FilterListeners.size() ; i++ )
      {
         ActionEvent evt = new ActionEvent( this ,
                  ActionEvent.ACTION_PERFORMED , OMITTED_PEAKS_CHANGED );

         RunInEventQueue R = ( new RunInEventQueue( evt , FilterListeners
                  .elementAt( i ) ) );
      }

   }

   

   /* 
    * Only responds to PEAKS_ARE_INDEXED action events
    * (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   @Override
   public void actionPerformed( ActionEvent e )
   {

      if( ! e.getActionCommand().equals( PEAKS_ARE_INDEXED ) )
         return;

      set_hklMinMax();

   }
   private void InterActiveDo(int fieldIndex, float MinVal, float MaxVal,
            int[]List, boolean inside   )
      {
         OneFilterElement elt = new OneFilterElement( fieldIndex, MinVal, 
                  MaxVal, List, inside);
         Vector<OneFilterElement> V1 = new Vector<OneFilterElement>();
         V1.add(  elt );
         Vector<Vector<OneFilterElement>> V2 = new Vector<Vector<OneFilterElement>>(  ); 
         V2.add(  V1 );
         omittedSeqNums =CalcOmits( V2);
         fireFilterListeners();
      }

   /**
    * Thread that runs an actionPerformed method in the AWT event queue if not 
    *   already in the event queue.
    *   
    * @author Ruth
    *
    */
   class RunInEventQueue extends Thread
   {



      ActionEvent    Evt;

      ActionListener Listener;


      public RunInEventQueue( ActionEvent evt, ActionListener listener )
      {

         Evt = evt;
         Listener = listener;
         if( EventQueue.isDispatchThread() )
            run();
         else
            SwingUtilities.invokeLater( this );
      }


      public void run()
      {

         Listener.actionPerformed( Evt );
      }

   }

   /**
    * Method for external programs to create a complex or simple rule
    * 
    * @param FieldName  The name of the field
    * 
    * @param min        The new minimum value for field
    * 
    * @param max        The new max value for field
    * 
    * @param inside     if true, omit those inside, otherwise omit those outside
    * 
    * @param List       The list of numbers that apply to several fields
    * 
    * @param newAndSeq  if true, start a new and sequence, or the previous
    *                   and sequence to the other and sequences
    */
   public void addRule( String FieldName, 
                        float min, 
                        float max, 
                        boolean inside, 
                        int[] List, 
                        boolean newAndSeq) throws IllegalArgumentException
   {
      int k = java.util.Arrays.binarySearch( Fields ,FieldName );
      if( k < 0 )
         throw new IllegalArgumentException( FieldName +
                                   " is not a field in list");
      OneFilterElement elt = new OneFilterElement( k,min,max, List, inside);
      
      if( newAndSeq )
        if( CurrentAndList != null && CurrentAndList.size() > 0)
        {
           OmitRule.add(  new Vector< OneFilterElement >(CurrentAndList) );
           CurrentAndList = new Vector<OneFilterElement>();
        }
      CurrentAndList.add( elt );
           
   }
   
   



   /**
    * Calculate and/or clear out the rule. calc= true will notify listeners 
    * 
    * @param calc     if true, will calculate new omits
    * @param clear    if true, will clear out rule only.
    *
    */
   public void CalcClear( boolean calc, boolean clear)
   {
      if( calc )
      {
         if( CurrentAndList != null && CurrentAndList.size() > 0 )
           {
              OmitRule.add(  new Vector< OneFilterElement >(CurrentAndList) );
              CurrentAndList = new Vector< OneFilterElement >();
           }

         omittedSeqNums = CalcOmits( OmitRule );
         fireFilterListeners();
         OmitRule.clear();
         CurrentAndList.clear();
      }
      
      if( clear){
         
         OmitRule.clear();
         CurrentAndList.clear();
         omittedSeqNums = new int[0];
         
      }
         
   }

   /**
    * Finds the point on the screen where the given component is
    * @param comp   The component
    * @return  the absolute position on the screen of the top left corner
    *              of this component.
    */
   public static Point getScreenLoc( Component comp )
   {
     
      if( comp == null )
         return null;

      Point P = comp.getLocation();
   
      if( comp instanceof Window )

         return P;

      Point P1 = getScreenLoc( comp.getParent() );
      
      if( P1 == null)
         return new Point( P.x,P.y);
      
      return new Point( P.x + P1.x , P.y + P1.y );
   }

   String               OmittedMenuItems;
   public void ManageShownMenus( String MenuItem, boolean show)
   {
      if( MenuItem == null)
         return;
      
      if( show)
      {
         int i= OmittedMenuItems.indexOf( ";"+MenuItem+";" );
         if( i < 0)
            return;
         OmittedMenuItems = OmittedMenuItems.substring( 0,i+1 )+
                OmittedMenuItems.substring( i+MenuItem.length()+2 );
         return;
      }
      int i= OmittedMenuItems.indexOf( ";"+MenuItem+";" );
      if(i>=0)
         return;
      
      OmittedMenuItems +=MenuItem+";";
      
   }
   /**
    * Implements menus, menu responses, etc. needed to get the user to select
    * the peaks that are to be omitted.
    * 
    * @author Ruth
    *
    */
   class MyActionListener extends MouseAdapter implements ActionListener,
                                                          WindowListener
   // ,ListSelectionListener

   {

      public String INTERACTIVE = "Interactive";

      public String START_AND   = "New(OR'd)";

      public String AND_PREV    = "Add(And) to Prev";

      public String CALC_FILTER = "Set Filter";

      public String CLEAR_OMITS = "Clear";
      public String ADD_OMIT1 = "OK";

      public String ADD_OMIT2 = "And to Prev";
      public String SUBMIT = "Submit";
      public String CLEAR_ALL ="Clear All Omitted Peaks";

      JButton       but;                                // Needed for an anchor for pop up menus

      Popup         Pop         = null;
      
      boolean     interactive  = false;
      
      JPanel      IntervalPanel;
      JButton     AndButton;
      JButton SubmitButton;
      JButton ClearButton;
      JTextArea   Information;
      int LastSelectedFieldIndex ;
      JCheckBox   Inside, Outside, interActive; 
      StretchTopBottom sliders;
      JTextField  ListFieldVals;
      FinishJFrame jf = null;
      
      WeakReference<PeakFilterer> WpFilt;
      String LLL = ADD_OMIT1+";"+ ADD_OMIT2+";"+ SUBMIT+";"+ CLEAR_ALL+";";
      public MyActionListener( PeakFilterer pFilt)
      {
         this.WpFilt = new WeakReference<PeakFilterer>(pFilt);
         LastSelectedFieldIndex = -1;
         jf = null;
      }
       public void kill()
       {
          but = null;
       
          Pop = null;
          WpFilt =null;

          interactive = false;
       }
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {
         PeakFilterer pFilt = WpFilt.get();
         if(pFilt == null)
            return;

         String evtString = e.getActionCommand();
         if( e.getSource() instanceof JButton  && LLL.indexOf( evtString+";" ) < 0 )
         {
            MakeForm((Component)( e.getSource()));
           /* MakeMenu( (Component) e.getSource() );
            but = (JButton) e.getSource();
            return;
            */
         }

        
         
         if( evtString == INTERACTIVE)
         {
            if( interactive != interActive.isSelected() &&
                     WpFilt != null && WpFilt.get() != null)
            {
               
               WpFilt.get().CalcClear( false , true);
               WpFilt.get().fireFilterListeners();
               Information.setText( "" );
            }
            interactive = interActive.isSelected();
            if( interactive)
            {
               AndButton.setEnabled(false);
               SubmitButton.setEnabled( false);
               ClearButton.setEnabled( false);

               if( ListFieldVals != null)
                  ListFieldVals.addActionListener( this );
               
            }else
            {
               AndButton.setEnabled(true);
               SubmitButton.setEnabled( true);
               ClearButton.setEnabled( true);
               if( ListFieldVals != null)
                  ListFieldVals.removeActionListener( this );
               
            }
            //MakeFieldMenu( "Interactive", but);
            return;
         }
         if( evtString == START_AND )
         {
            if( pFilt.CurrentAndList.size() > 0 )
               pFilt.OmitRule.addElement( pFilt.CurrentAndList );

            pFilt.CurrentAndList = new Vector< OneFilterElement >();
            interactive = false;
            MakeFieldMenu( "START" , but );

            return;
         }
         if( evtString .equals( ADD_OMIT1 )|| evtString.equals( ADD_OMIT2 ))
         {
            if( LastSelectedFieldIndex <0 || LastSelectedFieldIndex >=Fields.length)
               return;
            OneFilterElement F = null;
            if( sliders != null)
            {

               F = new OneFilterElement( LastSelectedFieldIndex , sliders.getBottomValue() , sliders
                        .getTopValue() , null , Inside.isSelected());
               
            }else if( ListFieldVals != null)
            {
               String res = ListFieldVals.getText();
               int[] listt = null;
               if( res == null )

                  F = null;

               else
               {
                  listt = IntList.ToArray( res );

                  F = new OneFilterElement( LastSelectedFieldIndex , Float.NaN , Float.NaN , listt ,
                           false );
               }
            }
            if( F == null)
               return;
           

            pFilt.CurrentAndList.addElement( F );
            String S="";
            if( !evtString.equals( "OK" ))
               S =" AND ";
            String field = Fields[LastSelectedFieldIndex];
            if( ListFieldVals != null)
               S+=field +"=["+ListFieldVals.getText()+"]";
            else
            {
               if( F.inside)
                  S +=field +=">="+F.min+" AND "+field +"<="+F.max;
               else
                  S +="("+field +"<"+F.min+" OR "+field +">"+F.max+")";
            }
            
            Information.append( S );
            AndButton.setText( ADD_OMIT2);
            return;
            
         }
         if( evtString.equals(SUBMIT))
         {
            if( pFilt.CurrentAndList.size() > 0 )

               pFilt.OmitRule.addElement( new Vector< OneFilterElement> (
                        pFilt.CurrentAndList ) );

            pFilt.CurrentAndList = new Vector< OneFilterElement >();
            pFilt.omittedSeqNums = pFilt.CalcOmits( pFilt.OmitRule );
            Information.append( "\n" );
            pFilt.fireFilterListeners();
            AndButton.setText( ADD_OMIT1 );
            return;
         }
         if( evtString.equals(CLEAR_ALL))
         {
            pFilt.CurrentAndList = new Vector< OneFilterElement >();
            pFilt.OmitRule = new Vector< Vector< OneFilterElement >>();
            omittedSeqNums = null;
            pFilt.fireFilterListeners();
            Information.setText( "" );
            
         }

         if( evtString == AND_PREV )
         {
            interactive = false;
            MakeFieldMenu( "AND" , but );
            return;
         }


         if( evtString == CALC_FILTER )
         {
            if( pFilt.CurrentAndList.size() > 0 )

               pFilt.OmitRule.addElement( new Vector< OneFilterElement >(
                        pFilt.CurrentAndList ) );

            pFilt.CurrentAndList = new Vector< OneFilterElement >();
            pFilt.omittedSeqNums = pFilt.CalcOmits( pFilt.OmitRule );

            pFilt.fireFilterListeners();
            pFilt.OmitRule.clear();
            return;
         }


         if( evtString == CLEAR_OMITS )
         {
            pFilt.CurrentAndList = new Vector< OneFilterElement >();
            pFilt.OmitRule = new Vector< Vector< OneFilterElement >>();
            return;
         }
         if( e.getSource() instanceof StretchTopBottom )
         {
            StretchTopBottom str = (StretchTopBottom) ( e.getSource() );
            if( str.getTopValue() < str.getBottomValue() )
            {
               float val = str.getTopValue();
               str.setControlValue( val , StretchTopBottom.BOTTOM_VALUE );
               repaint();
            }
            
         }
         if( interActive.isSelected() && (e.getSource()== Inside || e.getSource()== sliders))
         {
            if( sliders != null)
            {  
            sliders.checkValues();
            float MinVal = sliders.getBottomValue();
            float MaxVal = sliders.getTopValue();
            boolean inside = Inside.isSelected();
        
            InterActiveDo( LastSelectedFieldIndex, MinVal, 
                     MaxVal, null, inside);
            }else if( interActive.isSelected() && e.getSource() == ListFieldVals)
            {
               int[] elts = IntList.ToArray( ListFieldVals.getText() );
               
               InterActiveDo(LastSelectedFieldIndex, -1, 
                     -1, elts, false );
            }
         }

      }

      public void mouseClicked( MouseEvent e)
      {
         PeakFilterer pFilt = WpFilt.get();
         if(pFilt == null)
            return;
         if( e.getSource() != pFilt.list )
            return;

         int k = pFilt.list.locationToIndex( e.getPoint() );

         LastSelectedFieldIndex = k;
         if( k < 0 || k >= Fields.length)
            return;
         IntervalPanel.removeAll();
         String field = Fields[k];
        ((TitledBorder)IntervalPanel.getBorder()).setTitle( Fields[k] );

        JPanel panel = new JPanel();
        BoxLayout bl = new BoxLayout( panel, BoxLayout.Y_AXIS);
        panel.setLayout(  bl );
         if( k <= pFilt.LastIntervalIndex )
         {
            ListFieldVals = null;
           Inside = new JCheckBox("Omit Peaks with "+field+" values BETWEEN values");

           Outside = new JCheckBox("Omit Peaks with "+field+" values OUTSIDE values");
           ButtonGroup bg = new ButtonGroup();
           bg.add(Inside);
           bg.add(Outside);
           Outside.setSelected( true );
           panel.add( Inside );
           panel.add(Outside);
           Inside.addActionListener(  this );
           sliders = new StretchTopBottom( 
                    Mins[k] , Maxs[k] );
           sliders.addActionListener( this );
           panel.add( sliders);
           
         }else
         {  Inside = Outside = null;
            sliders = null;
           JPanel pan = new JPanel();
           pan.setLayout( new GridLayout( 1,2));
           pan.add(  new JLabel("Enter list of "+field+ "values to OMIT") );
           ListFieldVals = new JTextField(20);
           pan.add( ListFieldVals);
           panel.add(pan);
           panel.add( Box.createVerticalGlue());
           if( interActive.isSelected())
              ListFieldVals.addActionListener(  this );
            
         }
         IntervalPanel.setLayout( new GridLayout(1,1));
         IntervalPanel.add(panel);
         IntervalPanel.repaint();
         panel.repaint();
         IntervalPanel.invalidate();
         jf.getContentPane().validate();
         if( interactive && WpFilt != null && WpFilt.get() != null)
         {
           
            WpFilt.get().CalcClear( false , true );
            WpFilt.get().fireFilterListeners();
         }
         
      }
      // deprecated
      public void mouseClicked1( MouseEvent e )
      {
         PeakFilterer pFilt = WpFilt.get();
         if(pFilt == null)
            return;
         if( e.getSource() != pFilt.list )
            return;

         int k = pFilt.list.locationToIndex( e.getPoint() );

         OneFilterElement F;
         Pop.hide();

         if( k <= pFilt.LastIntervalIndex )
         {

            IntervalDialog filtElt = new IntervalDialog( pFilt.Fields[ k ] ,
                     pFilt.Mins[ k ] , pFilt.Maxs[ k ], !interactive, k, but );

            if( filtElt.MinVal() == pFilt.Mins[ k ] && filtElt.MaxVal() == pFilt.Maxs[ k ] )

               F = null;

            else

               F = new OneFilterElement( k , filtElt.MinVal() , filtElt
                        .MaxVal() , null , filtElt.Inside );

         }
         else
         {
            String res = JOptionPane.showInputDialog( "Enter " + pFilt.Fields[ k ]
                     + " list" );
            int[] listt = null;
            if( res == null )

               F = null;

            else
            {
               listt = IntList.ToArray( res );

               F = new OneFilterElement( k , Float.NaN , Float.NaN , listt ,
                        false );
            }
            if( interactive)
               pFilt.InterActiveDo(k,Float.NaN,Float.NaN, listt,false);
               
         }

         if( F != null )

            pFilt.CurrentAndList.addElement( F );


         if( Pop != null )

            Pop.hide();
      }


      /* Did not work well. Too many valueChanged events occurred with littl ways for
       * determinimg what they are.
       * (non-Javadoc)
       * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
       */

      public void valueChanged( ListSelectionEvent e )
      {
         PeakFilterer pFilt = WpFilt.get();
         if(pFilt == null)
            return;
         int k = e.getLastIndex();
         System.out.println( "k=" + k + "," + e.getLastIndex() );
         OneFilterElement F;
         Pop.hide();
         if( k <= pFilt.LastIntervalIndex )
         {

            IntervalDialog filtElt = new IntervalDialog( pFilt.Fields[ k ] ,
                     pFilt.Mins[ k ] , pFilt.Maxs[ k ],true,-1, but );
            if( filtElt.MinVal() == pFilt.Mins[ k ] && filtElt.MaxVal() == pFilt.Maxs[ k ] )
               F = null;
            else
               F = new OneFilterElement( k , filtElt.MinVal() , filtElt
                        .MaxVal() , null , filtElt.Inside );

         }
         else
         {
            String res = JOptionPane.showInputDialog( "Enter " + pFilt.Fields[ k ]
                     + " list" );
            if( res == null )
               F = null;
            else
            {
               int[] listt = IntList.ToArray( res );
               F = new OneFilterElement( k , Float.NaN , Float.NaN , listt ,
                        false );
               
            }


         }
         if( F != null )
            pFilt.CurrentAndList.addElement( F );

         // list.removeListSelectionListener( listener );
         if( Pop != null )
            Pop.hide();
      }


      private void MakeForm( Component hanger)
      {
         if( jf != null)
            return;
         JPanel panel = new JPanel();
         BoxLayout bl = new BoxLayout( panel, BoxLayout.X_AXIS);
         panel.setLayout( bl);
         JPanel FieldPanel = new JPanel();
         FieldPanel.setLayout( new GridLayout(1,1));
         FieldPanel.add(new JScrollPane( list));
         panel.add( FieldPanel);
         JPanel panelMid = new JPanel();
         panelMid.setLayout(  new GridLayout(2,1) );
         IntervalPanel = new JPanel();
         IntervalPanel.setLayout(  new GridLayout(1,1) );
         IntervalPanel.setBorder(  new TitledBorder(
                  new LineBorder(Color.black,2,true),"Field Name") );
         panelMid.add(  IntervalPanel );
         JPanel panelMid1 = new JPanel();
         panelMid1.setLayout(  new GridLayout( 3,1) );
         AndButton = new JButton( ADD_OMIT1);
         interActive = new JCheckBox(INTERACTIVE);
         interActive.setSelected( false );
         interActive.addActionListener(  this );
         JPanel FirstPanel = new JPanel();
         FirstPanel.setLayout(  new GridLayout( 1,2) );
         FirstPanel.add( AndButton);
         FirstPanel.add( interActive);
         panelMid1.add( FirstPanel);
         AndButton.addActionListener( this );
         AndButton.setToolTipText( "<html><body> And's this condition to previous <BR>"+ 
                  "conditions, or starts a new AND sequence if there are no <Br>"+
                  "previous conditions</body></html>");
         
         SubmitButton = new JButton(SUBMIT);
         SubmitButton.addActionListener( this );
         SubmitButton.setToolTipText(
                  "<html><body> These conditions will be submitted to<BR>"+
                  "all listeners. The 3D display should change. These <BR>"+
                  "will be added(or'd) to the previous AND sequences</body></html>");
         panelMid1.add(SubmitButton);
;
         
         

         ClearButton = new JButton(CLEAR_ALL);
         ClearButton.addActionListener( this );
         ClearButton.setToolTipText(
                  "<html><body> Clears out all omitted peaks </body></html>");
         panelMid1.add(ClearButton);
         
         panelMid.add( panelMid1 );
         panel.add( panelMid);
         
         
         JPanel panelR = new JPanel();
         bl = new BoxLayout( panelR, BoxLayout.Y_AXIS);
         
         panelR.setLayout(  bl );
         Information = new JTextArea(10,25);
         Information.setEditable( false );
         Information.setBorder(  new TitledBorder( 
                  new LineBorder(Color.black,4),"Information") );
         
         panelR.add( new JScrollPane(Information) );
         panelR.add(  Box.createVerticalGlue() );
         panel.add( panelR);
         
         
        jf = new FinishJFrame( "Filter Out Peaks");
        jf.getContentPane().setLayout( new GridLayout(1,1));
        jf.getContentPane().add( panel );
        java.awt.Dimension D = jf.getToolkit().getScreenSize();
        jf.setSize(D.width/2, D.height/2 );
        Point p = getAbsPosition( hanger);
        
        jf.setLocation( new Point( p.x+hanger.getWidth(),Math.max( 0 , p.y-D.height/4)) );
        jf.addWindowListener(  this  );
        WindowShower.show(jf);
         
         
         
      }
      public Point getAbsPosition( Component hanger)
      {
         if( hanger == null)
            return new Point(0,0);
         java.awt.Rectangle R = hanger.getBounds();
         if( hanger instanceof Window)
         {  
            return new Point( R.x, R.y);
         }
         Point Pp= getAbsPosition( hanger.getParent());
         return new Point( Pp.x+R.x, Pp.y+R.y);
         
      }
      //deprecated
      private void MakeFieldMenu( String message , Object obj )
      {

         PeakFilterer pFilt = WpFilt.get();
         if(pFilt == null)
            return;
         if( Pop != null )
            Pop.hide();

         String Title = "AND to Current AND sequence";

         if( message.startsWith( "START" ) )

            Title = "Start AND Seq";

         ( (TitledBorder) ( pFilt.list.getBorder() ) ).setTitle( Title );

         Component comp = (Component) obj;

         Point P = pFilt.getScreenLoc( comp );

         pFilt.list.clearSelection();


         Pop = PopupFactory.getSharedInstance().getPopup( (Component) obj ,
                  pFilt.list , P.x+comp.getWidth()*3/4 , P.y+comp.getHeight()/2 );

         Pop.show();


      }


     

      
      private void MakeMenu( Component comp )
      {

         JPopupMenu popUp = new JPopupMenu( "Peak Filter" );

         JMenuItem item = new JMenuItem( INTERACTIVE );
         item.setToolTipText( "Sets only one field immediately" );
         
         popUp.add( item).addActionListener( this );
         
         item = new JMenuItem( START_AND );

         item
                  .setToolTipText( "<html><body>Starts an AND sequence of conditions <BR>"
                           + " This sequence will be OR'd with previous and sequences"
                           + "</body></html>" );

         popUp.add( item ).addActionListener( this );

         item = new JMenuItem( AND_PREV );

         item.setToolTipText( "ANDs the next condition to the current"
                  + " AND sequence only " );

         popUp.add( item ).addActionListener( this );

         popUp.add( CLEAR_OMITS ).addActionListener( this );

         item = new JMenuItem( CALC_FILTER );

         item
                  .setToolTipText( "Calculates the omitted Peaks, Notifies listeners" );

         popUp.add( item ).addActionListener( this );

         popUp.show( comp , comp.getWidth()*3/4 , comp.getHeight()/2 );
      }
      /* (non-Javadoc)
       * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
       */
      @Override
      public void windowActivated( WindowEvent arg0 )
      {

         
         
      }
      /* (non-Javadoc)
       * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosed( WindowEvent arg0 )
      {

        jf = null;
      }
      /* (non-Javadoc)
       * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing( WindowEvent arg0 )
      {

         
      }
      /* (non-Javadoc)
       * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
       */
      @Override
      public void windowDeactivated( WindowEvent arg0 )
      {

         
      }
      /* (non-Javadoc)
       * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
       */
      @Override
      public void windowDeiconified( WindowEvent arg0 )
      {
       
      }
      /* (non-Javadoc)
       * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
       */
      @Override
      public void windowIconified( WindowEvent arg0 )
      {

         
      }
      /* (non-Javadoc)
       * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
       */
      @Override
      public void windowOpened( WindowEvent arg0 )
      {

         
      }
      
      

   }


   /* public static void main( String[] args)
   {
     PeakFilterer but = new PeakFilterer();
      JFrame jf = new JFrame();
      jf.getContentPane().add(  but );
      jf.setSize( 300,400);
      jf.setVisible( true);
      
   }
    */
   //Creates a frame with a message in it.
   private static JFrame getJFrame( String Message ,Component comp)
   {
     
      Point P = getScreenLoc( comp);
      if( P == null)
         P = new Point(0,0);

      JFrame jf = new JFrame( Message );
     
      jf.setBounds(new java.awt.Rectangle( P.x/2,P.y/2, 200 , 300 ));
     
      jf.setVisible( false );
      jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
      return jf;
   }

   /**
    * A Dialog for soliciting a user's selection of range of values for
    * a given item associated with Peak objects
    * 
    * @author Ruth
    *
    */
   class IntervalDialog extends JDialog implements ActionListener
   {



      float            MinVal , MaxVal;

      float            minimum , maximum;

      boolean          Inside;

      String           Message;

      JCheckBox        inside;
      
      JCheckBox        outside;

      StretchTopBottom sliders;
      
      boolean          IsModal;
      
      int              fieldIndex;


      /**
       *  Constructor 
       * @param message  Message on the dialog box indicating the field desired
       * @param minVal  Min value for the field
       * @param maxVal  max value for the field
       */
      public IntervalDialog( String message, float minVal, float maxVal )
      {

         this( message , minVal , maxVal , true ,-1, null);
      }


      /**
       Constructor 
       * @param message  Message on the dialog box indicating the field desired
       * @param minVal  Min value for the field
       * @param maxVal  max value for the field
       * @param isModal Indicates whether the dialog box is modal or not.
       * @param FieldIndex  the index of field if not modal.
       */
      public IntervalDialog( String message, float minVal, float maxVal,
               boolean isModal, int FieldIndex, Component comp )
      {

         super( getJFrame( "OMIT "+message +" Values", comp ) , 
                      "OMIT "+message +" Values" , isModal );
         
         IsModal = isModal;
         fieldIndex = FieldIndex;
         MinVal = minimum = minVal;
         MaxVal = maximum = maxVal;
         Message = message;
         Inside = false;

         //-----------------------------
         JPanel contentPane = new JPanel();
         contentPane.setLayout( new BorderLayout() );

         JPanel buttonPanel = new JPanel();
         BoxLayout blayout = new BoxLayout( buttonPanel , BoxLayout.X_AXIS );
         buttonPanel.setLayout( blayout );

         inside = new JCheckBox( "Omit Peaks inside interval" , false );
         outside = new JCheckBox("Omit Peaks outside interval", true);
         inside.setToolTipText( "Omits peaks whose "+message +
                  " values are BETWEEN the 2 values below" );
         outside.setToolTipText( "Omits peaks whose "+message +
                 " values are OUTSIDE the 2 values below" );
         ButtonGroup bg = new ButtonGroup();
         bg.add( inside );
         bg.add( outside);
         JPanel pan = new JPanel( new GridLayout(2,1));
         pan.add( inside);
         pan.add( outside );
         buttonPanel.add( pan );
         if( !IsModal)
            inside.addActionListener(  this  );

         buttonPanel.add( Box.createHorizontalGlue() );

         JButton exit = new JButton( "Exit/Save" );
         exit.addActionListener( this );

         buttonPanel.add( Box.createHorizontalGlue() );

         JButton Cancel = new JButton( "Cancel" );
         Cancel.addActionListener( this );
         buttonPanel.add( Cancel );

         buttonPanel.add( exit );
         buttonPanel.setBackground( java.awt.Color.GRAY );

         contentPane.add( buttonPanel , BorderLayout.NORTH );

         //-------------Sliders ------------------
         sliders = new StretchTopBottom( MinVal , MaxVal );
         sliders.addActionListener( this );
         contentPane.add( sliders , BorderLayout.CENTER );

         setContentPane( contentPane );
         Point P = getScreenLoc( comp);
         if( P == null)
            P = new Point(0,0);
         int w =0,
             h=0;
         if( comp != null)
         {
            w = comp.getWidth()*3/4;
            h= comp.getHeight()/2;
         }
         setBounds( P.x+w, P.y+h,500 , 300 );
         setVisible( true );

      }


      /**
       * Sets the minimum and maximum possible value
       * 
       * @param max  maximum possible value for this field
       * @param min  minimum possible value for this field
       */
      public void setMaxMinVal( float max , float min )
      {

         minimum = min;
         maximum = min;
         sliders.setMaxMin( maximum , minimum );
      }


      /**
       * 
       * @return  the minimum value set by the user for this field
       */
      public float MinVal()
      {

         sliders.checkValues();
         updateVals();
         return minimum;
      }


      /**
       * 
       * @return  The maximum value set by the user for this field
       */
      public float MaxVal()
      {

         sliders.checkValues();
         updateVals();
         return maximum;
      }


      /**
       * 
       * @return  true if omit when the peaks's field value is inside the interval
       *          otherwise omit peak if its value is outside the interval
       */
      public boolean inside()
      {

         sliders.checkValues();
         updateVals();

         return Inside;
      }


      /**
       * Should do a checkValues before this
       */
      private void updateVals()
      {
        
         minimum = sliders.getBottomValue();
         maximum = sliders.getTopValue();
         Inside = inside.isSelected();
      }

      //interactive should be true
      
      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

         String evtString = e.getActionCommand();
         sliders.checkValues();
         if( evtString.equals( "Exit/Save" ) )
         {
            updateVals();

            dispose();
            return;

         }


         if( evtString.equals( "Cancel" ) )
         {
            dispose();
            return;

         }


         if( e.getSource() instanceof StretchTopBottom )
         {
            StretchTopBottom str = (StretchTopBottom) ( e.getSource() );
            if( str.getTopValue() < str.getBottomValue() )
            {
               float val = str.getTopValue();
               str.setControlValue( val , StretchTopBottom.BOTTOM_VALUE );
               repaint();
            }
            
         }
         if( !IsModal && (e.getSource()== inside || e.getSource()== sliders))
         {
            InterActiveDo( fieldIndex, MinVal(), 
                     MaxVal(), null, inside());
            /*OneFilterElement elt = new OneFilterElement( fieldIndex, MinVal(), 
                     MaxVal(), null, inside());
            Vector<OneFilterElement> V1 = new Vector<OneFilterElement>();
            V1.add(  elt );
            Vector<Vector<OneFilterElement>> V2 = new Vector<Vector<OneFilterElement>>(  ); 
            V2.add(  V1 );
            omittedSeqNums =CalcOmits( V2);
            fireFilterListeners();
            */

         }
      }

   }


   /**
    * Stored information on one field about the values of a peak for the peak
    * to be omitted
    *  
    * @author Ruth
    *
    */
   class OneFilterElement
   {



      public int     fieldIndex;

      public float   min , max;

      public int[]   List;

      public boolean inside;


      /**  Constructor
       * @param FieldIndex  The index(internal) for this field
       * 
       * @param Min  The absolute minimum value for this field
       * @param Max  The absolute maximum value for this field
       * 
       * @param List    The list of values for non numeric fields
       * 
       * @param Inside  true if omit if Peak's value is inside interval
       */
      public OneFilterElement( int FieldIndex, float Min, float Max,
               int[] list, boolean Inside )
      {

         fieldIndex = FieldIndex;
         min = Min;
         max = Max;
         List = list;
         inside = Inside;
      }

   }


   /**
    * 
    * @param pk  The peak
    * @param k   The index in the fields list
    * @return the kth field value for the peak
    */
   private float getFloatValMain( IPeak pk , int k )
   {

      if( k < 4 )
         if( k <= 1 )
            if( k == 0 )
               return pk.y();
            else
               return pk.x();
         else //k>=2
         if( k == 2 )
            return pk.z();
         else
            return pk.ipkobs();


      else //k>=4
      if( k <= 9 )
         if( k == 5 )
            return pk.time();
         else
            return pk.h();
         

      else //k>=10
      if( k == 10 )
         return pk.k();
      else if( k < 15)
         return pk.l();
      else
         return pk.reflag();
   }


   /**
    * 
    * @param pk  The peak
    * @param k   The index in the fields list
    * @return the kth field value for the peak
    */
   private float getFloatValQCalc( IPeak pk , int k )
   {

      float[] Qs = pk.getUnrotQ();
      
      if( k > 8 )
         if( k <= 11 )
            return Qs[ k - 9 ];
         else
            return Float.NaN;
      
      float Q = (float) Math.sqrt( Qs[ 0 ] * Qs[ 0 ] + Qs[ 1 ] * Qs[ 1 ]
               + Qs[ 2 ] * Qs[ 2] );
      
      return 1f / Q;

   }


   /**
    * 
    * @param pk  The peak
    * @param c   'h','k',or 'l'
    * 
    * @return the corresponding h,k, or l value depending on c
    */
   private float getFloatOtherCalc( IPeak pk , char c )
   {

      if( c == 'h' )
         return pk.h() - (float) Math.floor( pk.h() + .5 );
      
      if( c == 'k' )
         return pk.k() - (float) Math.floor( pk.k() + .5 );
      
      if( c == 'l' )
         return pk.l() - (float) Math.floor( pk.l() + .5 );
      
      return Float.NaN;
   }

}
