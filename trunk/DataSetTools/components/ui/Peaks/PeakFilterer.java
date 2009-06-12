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
//import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    * Constructor
    * @param Peaks  The Vector of Peaks
    */
   public PeakFilterer( Vector< Peak_new > Peaks )
   {

      super( "Peak Filterer" );
      peaks = Peaks;

      listener = new MyActionListener();
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
    * @param list       The list of numbers that apply to several fields
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
         
      }
         
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
   class MyActionListener extends MouseAdapter implements ActionListener
   // ,ListSelectionListener

   {

      public String INTERACTIVE = "Interactive";

      public String START_AND   = "New(OR'd)";

      public String AND_PREV    = "Add(And) to Prev";

      public String CALC_FILTER = "Set Filter";

      public String CLEAR_OMITS = "Clear omitted Peaks";

      JButton       but;                                // Needed for an anchor for pop up menus

      Popup         Pop         = null;
      
      boolean     interactive  = false;


      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

         if( e.getSource() instanceof JButton )
         {
            MakeMenu( (Component) e.getSource() );
            but = (JButton) e.getSource();
            return;
         }

         String evtString = e.getActionCommand();
         
         if( evtString == INTERACTIVE)
         {
            interactive = true;
            MakeFieldMenu( "Interactive", but);
            return;
         }
         interactive = false;
         if( evtString == START_AND )
         {
            if( CurrentAndList.size() > 0 )
               OmitRule.addElement( CurrentAndList );

            CurrentAndList = new Vector< OneFilterElement >();
            interactive = false;
            MakeFieldMenu( "START" , but );

            return;
         }


         if( evtString == AND_PREV )
         {
            interactive = false;
            MakeFieldMenu( "AND" , but );
            return;
         }


         if( evtString == CALC_FILTER )
         {
            if( CurrentAndList.size() > 0 )

               OmitRule.addElement( new Vector< OneFilterElement >(
                        CurrentAndList ) );

            CurrentAndList = new Vector< OneFilterElement >();
            omittedSeqNums = CalcOmits( OmitRule );

            fireFilterListeners();
            OmitRule.clear();
            return;
         }


         if( evtString == CLEAR_OMITS )
         {
            CurrentAndList = new Vector< OneFilterElement >();
            OmitRule = new Vector< Vector< OneFilterElement >>();
         }


      }


      public void mouseClicked( MouseEvent e )
      {

         if( e.getSource() != list )
            return;

         int k = list.locationToIndex( e.getPoint() );

         OneFilterElement F;
         Pop.hide();

         if( k <= LastIntervalIndex )
         {

            IntervalDialog filtElt = new IntervalDialog( Fields[ k ] ,
                     Mins[ k ] , Maxs[ k ], !interactive, k );

            if( filtElt.MinVal() == Mins[ k ] && filtElt.MaxVal() == Maxs[ k ] )

               F = null;

            else

               F = new OneFilterElement( k , filtElt.MinVal() , filtElt
                        .MaxVal() , null , filtElt.Inside );

         }
         else
         {
            String res = JOptionPane.showInputDialog( "Enter " + Fields[ k ]
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
               InterActiveDo(k,Float.NaN,Float.NaN, listt,false);
               
         }

         if( F != null )

            CurrentAndList.addElement( F );


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

         int k = e.getLastIndex();
         System.out.println( "k=" + k + "," + e.getLastIndex() );
         OneFilterElement F;
         Pop.hide();
         if( k <= LastIntervalIndex )
         {

            IntervalDialog filtElt = new IntervalDialog( Fields[ k ] ,
                     Mins[ k ] , Maxs[ k ] );
            if( filtElt.MinVal() == Mins[ k ] && filtElt.MaxVal() == Maxs[ k ] )
               F = null;
            else
               F = new OneFilterElement( k , filtElt.MinVal() , filtElt
                        .MaxVal() , null , filtElt.Inside );

         }
         else
         {
            String res = JOptionPane.showInputDialog( "Enter " + Fields[ k ]
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
            CurrentAndList.addElement( F );

         // list.removeListSelectionListener( listener );
         if( Pop != null )
            Pop.hide();
      }


      private void MakeFieldMenu( String message , Object obj )
      {

         if( Pop != null )
            Pop.hide();

         String Title = "AND to Current AND sequence";

         if( message.startsWith( "START" ) )

            Title = "Start AND Seq";

         ( (TitledBorder) ( list.getBorder() ) ).setTitle( Title );

         Component comp = (Component) obj;

         Point P = getScreenLoc( comp );

         list.clearSelection();


         Pop = PopupFactory.getSharedInstance().getPopup( (Component) obj ,
                  list , P.x , P.y );

         Pop.show();


      }


      private Point getScreenLoc( Component comp )
      {

         if( comp == null )
            return null;

         Point P = comp.getLocation();

         if( comp instanceof Window )

            return P;

         Point P1 = getScreenLoc( comp.getParent() );

         return new Point( P.x + P1.x , P.y + P1.y );
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

         popUp.show( comp , 0 , 0 );
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
   private static JFrame getJFrame( String Message )
   {

      JFrame jf = new JFrame( Message );
      jf.setSize( 200 , 300 );
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

         this( message , minVal , maxVal , true ,-1);
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
               boolean isModal, int FieldIndex )
      {

         super( getJFrame( message ) , message , true );
         
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

         inside = new JCheckBox( "Inside interval" , false );
         inside.setToolTipText( "False-outside of interval, otherwise inside" );
         buttonPanel.add( inside );
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
         setSize( 300 , 400 );
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
