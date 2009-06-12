/* 
 * File: SetPeaks.java
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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.*;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;


/**
 * Handler for setting peaks. It currently subclasses JButton
 * @see subs#getJMenuItem(AbstractButton)
 * @author Ruth
 *
 */
public class SetPeaks extends JButton
{



   /**
    * Maximum number of peaks that can be set
    */
   public static int     MAX_SEL_PEAKS   = 12;
   
   /**
    * Action command when notifying action listeners that new peaks have 
    * been set.
    */
   public static String SET_PEAK_INFO_CHANGED ="Set Peaks Changed";

   /**
    * The Text on the JButton. 
    */
   public static String  SELECT_PEAK_BUT = "Select Peaks";

   //Menu 1 strings
   private static String SET_PEAK        = "Set/Alter  Peak";

   private static String CLEAR_SET_PEAKS = "Clear All set Peaks";

   private static String SET_HKL         = "Set/Alter  hkl val";

   //Menu 2 strings
   private static String SET_PEAK1       = "Set Peak 1 info";

   private static String SET_PEAK2       = "Set Peak 2 info";

   private static String SET_PEAK3       = "Set Peak 3 info";

   private static String SET_PEAK4       = "Set Peak 4 info";

   private static String SET_PEAKN       = "Set Peak more Peak info";


   // List of selected Peaks
   float[][]             SelectedPeaks;

   //List of specified hklVals
   float[][]             hklVals;

   //List of sequence number corresponding to SelectedPeaks.
   // Can be -1. Q vals can be entered without a sequence #
   int[]                 seqNums;

   //Max number of set peaks and set hkl's. Only consecutively set peaks
   //  are considered set
   int                   MaxPksSet , MaxhklSet;

   //Save values in case other entities want to use these as inputs
   // See setDisable
   float[][]             SAV_SelectedPeaks;

   float[][]             SAV_hklVals;

   int[]                 SAV_seqNums;

   int                   SAV_MaxPksSet , SAV_MaxhklSet;

   //Listener to all menu actions
   MyActionListener      listener;

   //If new entries are disabled
   boolean               disable;

   // The 3D view of the peaks in reciprocal space( Q values)
   View3D                View;

   //The vector of peaks
   Vector< IPeak >       Peaks;


   /**
    * Constructor
    * @param view     The 3D view of the peaks in reciprocal space( Q values)
    * @param peaks    The vector of peaks
    */
   public SetPeaks( View3D view, Vector< IPeak > peaks )
   {

      super( SELECT_PEAK_BUT );
      View = view;
      Peaks = peaks;

      listener = new MyActionListener();
      addActionListener( listener );

      disable = false;

      setToolTipText( "<html><body><UL>Selects peaks and hkl values for arguments"
               + " to other parts of the system<LI> Some Calculations for "
               + "orientation matrices<LI> The Info view for setting Peak hkl values"
               + "</ul></body></html)" );

      Clear();
   }


   /*
    * @return the Q values associated with PeakNum. 0 is the first set peak
    *          and null is returned if not set.
    */
   public float[] getSetPeakQ( int PeakNum )
   {
      if( PeakNum < 0 || PeakNum >= MAX_SEL_PEAKS )
         return null;

      for( int j = 0 ; j < 3 ; j++ )
         if( Float.isNaN( SelectedPeaks[ PeakNum ][ j ] ) )
            return null;

      return SelectedPeaks[ PeakNum ];
   }


   /*
    * @return the hkl values associated with PeakNum. 0 is the first set peak
    *          and null is returned if not set.
    */
   public float[] getSetPeak_hkl( int PeakNum )
   {

      if( PeakNum < 0 || PeakNum >= 3 )
         return null;

      for( int j = 0 ; j < 3 ; j++ )
         if( Float.isNaN( hklVals[ PeakNum ][ j ] ) )
            return null;

      return hklVals[ PeakNum ];
   }


   /**
    * @return the sequence number associated with PeakNum. 0 
    *                is the first set peak. -1 is returned if not set
    */
   public int getSetPeakSeqNum( int PeakNum )
   {

      if( PeakNum < 0 || PeakNum >= MAX_SEL_PEAKS )
         return - 1;

      return seqNums[ PeakNum ];
   }


   /**
    * Sets the Q values of a set peak.
    * @param PeakNum  The set peak num. It starts at 0 
    * @param qx        qx value
    * @param qy        qy value
    * @param qz        qz value
    */
   public void setPeakQ( int PeakNum , float qx , float qy , float qz )
   {

      if( PeakNum < 0 || PeakNum >= MAX_SEL_PEAKS )
         return;

      SelectedPeaks[ PeakNum ][ 0 ] = qx;
      SelectedPeaks[ PeakNum ][ 1 ] = qy;
      SelectedPeaks[ PeakNum ][ 2 ] = qz;
      seqNums[ PeakNum ] = - 1;
      fireSetPeakListeners( SET_PEAK_INFO_CHANGED);
   }


   /**
    * Sets the hkl and Q values for a set peak
    * @param PeakNum  The set peak num. It starts at 0 
    * @param pk       The selected peak.
    */
   public void setPeakQ( int PeakNum , IPeak pk )
   {

      if( PeakNum < 0 || PeakNum >= MAX_SEL_PEAKS || pk == null )
         return;

      float[] Qs = pk.getUnrotQ();

      setPeakQ( PeakNum , Qs[ 0 ] , Qs[ 1 ] , Qs[ 2 ] );

      seqNums[ PeakNum ] = pk.seqnum();
   }


   /**
    * Sets the hkl values only for a set peak
    * @param PeakNum  The set peak num. It starts at 0 
    * @param h        The h value
    * @param k        The k value
    * @param l        The l value
    */
   public void setPeakHKL( int PeakNum , float h , float k , float l )
   {

      if( PeakNum < 0 || PeakNum >= 3 )
         return;

      hklVals[ PeakNum ][ 0 ] = h;
      hklVals[ PeakNum ][ 1 ] = k;
      hklVals[ PeakNum ][ 2 ] = l;

      fireSetPeakListeners( SET_PEAK_INFO_CHANGED);

   }


   /**
    * Sets the hkl  values only  for a set peak
    * @param PeakNum  The set peak num. It starts at 0 
    * @param pk       The selected peak. 
    */
   public void setPeakHKL( int PeakNum , IPeak pk )
   {

      if( PeakNum < 0 || PeakNum >= MAX_SEL_PEAKS || pk == null )
         return;

      setPeakHKL( PeakNum , pk.h() , pk.k() , pk.l() );
   }


   Vector<ActionListener> SetPeakListeners = new Vector<ActionListener>();
   public void addSetPeakListeners( ActionListener  Listener)
   {
    if( Listener == null)
       return;
    if( SetPeakListeners.contains( Listener))
       return;
    SetPeakListeners.add( Listener);
    
   }
   
   public void removeSetPeakListener( ActionListener Listener)
   {
      if( Listener == null)
         return;
      if( SetPeakListeners.contains(  Listener ))
         SetPeakListeners.remove(  Listener );
   }
   
   public void removeAllSetPeakListeners()
   {
      SetPeakListeners.clear();
   }
   
   private void fireSetPeakListeners( String message)
   {
      for( int i=0; i< SetPeakListeners.size(); i++)
         SetPeakListeners.elementAt( i ).actionPerformed(   
                  new ActionEvent( this, ActionEvent.ACTION_PERFORMED, message));
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
    * Clears out all set peaks and hkl values
    */
   public void Clear()
   {

      SelectedPeaks = new float[ MAX_SEL_PEAKS ][ 3 ];

      hklVals = new float[ 3 ][ 3 ];
      seqNums = new int[ MAX_SEL_PEAKS ];

      SAV_SelectedPeaks = new float[ MAX_SEL_PEAKS ][ 3 ];

      SAV_hklVals = new float[ 3 ][ 3 ];
      SAV_seqNums = new int[ MAX_SEL_PEAKS ];
      for( int i = 0 ; i < MAX_SEL_PEAKS ; i++ )
      {
         Arrays.fill( SelectedPeaks[ i ] , Float.NaN );
         seqNums[ i ] = - 1;
         if( i < 3 )
            Arrays.fill( hklVals[ i ] , Float.NaN );

         Arrays.fill( SAV_SelectedPeaks[ i ] , Float.NaN );
         SAV_seqNums[ i ] = - 1;
         if( i < 3 )
            Arrays.fill( SAV_hklVals[ i ] , Float.NaN );

      }
      MaxPksSet = 0;
      MaxhklSet = 0;

      SAV_MaxPksSet = 0;
      SAV_MaxhklSet = 0;
      fireSetPeakListeners( SET_PEAK_INFO_CHANGED);
   }


   private void copyTo( float[][] from , float[][] to )
   {

      for( int i = 0 ; i < from.length ; i++ )
         System.arraycopy( from[ i ] , 0 , to[ i ] , 0 , to[ i ].length );
   }


   private void copyTo( int[] from , int[] to )
   {

      System.arraycopy( from , 0 , to , 0 , to.length );
   }


   /**
    * Saves the previous settings so these points are usable for other purposes
    * 
    * @param disable Cannot change selected peaks with hkl if disabled.
    */
   public void setDisable( boolean disable )
   {

      if( this.disable != disable )
         if( disable )
         {
            copyTo( SAV_SelectedPeaks , SelectedPeaks );
            copyTo( SAV_hklVals , hklVals );
            copyTo( SAV_seqNums , seqNums );
            MaxPksSet = SAV_MaxPksSet;
            MaxhklSet = SAV_MaxhklSet;

         }
         else
         {

            copyTo( SelectedPeaks , SAV_SelectedPeaks );
            copyTo( hklVals , SAV_hklVals );
            copyTo( seqNums , SAV_seqNums );
            SAV_MaxPksSet = MaxPksSet;
            SAV_MaxhklSet = MaxhklSet;
         }

      this.disable = disable;
   }

   /**
    * Handles all actions initiated by menus
    * @author Ruth
    *
    */
   class MyActionListener implements ActionListener
   {



      JButton but         = null;

      boolean specifyPeak = true;


      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

         if( disable )
            return;

         String evtString = e.getActionCommand();

         if( evtString == SELECT_PEAK_BUT
                  && ( e.getSource() instanceof JButton ) )
         {
            but = (JButton) e.getSource();
            MakePopUpMenu( (JButton) e.getSource() );
            return;

         }
         if( evtString == SET_PEAK )
         {
            specifyPeak = true;
            MakePopUpMenu1( but );
            return;

         }
         if( evtString == SET_HKL )
         {
            specifyPeak = false;
            MakePopUpMenu1( but );
            return;

         }
         if( evtString == CLEAR_SET_PEAKS )
         {
            specifyPeak = true;
            Clear();
            return;
         }


         if( evtString == SET_PEAK1 )
         {
            MakeMenu2( specifyPeak , 0 , but );
            return;
         }
         if( evtString == SET_PEAK2 )
         {
            MakeMenu2( specifyPeak , 1 , but );

            return;
         }
         if( evtString == SET_PEAK3 )
         {
            MakeMenu2( specifyPeak , 2 , but );

            return;
         }
         if( evtString == SET_PEAK4 )
         {
            MakeMenu2( specifyPeak , 3 , but );

            return;
         }
         if( evtString == SET_PEAKN )
         {
            MakeMenu2( specifyPeak , 4 , but );

            return;
         }
         if( evtString.equals( "Help" ) )
         {
            String SS = "Q";

            if( ! specifyPeak )
               SS = "hkl";

            JEditorPane jed = new JEditorPane(
                     "text/html" ,
                     "<html><body> Sets the "
                              + SS
                              + " values for the "
                              + "selected peaks. The seq num can be set or set to -1 if the "
                              + SS
                              + " values do not come from a peak.<P> The next"
                              + ""
                              + " dialog box gives the following options<UL><LI>Use last "
                              + "selected peak from the 3D Q-Viewer to specify the selection"
                              + "<LI> Specify choice using the sequence number <LI> or just"
                              + " enter the desired 3 values<LI> For Selected peaks past the "
                              + "4th a spinner will allow for specifing which peak to set"
                              + "</ul><P> If \"Use QView checkbox\""
                              + " is selected all other info is ignored. If the seq number is"
                              + " positive all data under this information is ignored<P>"
                              + "If the "
                              + SS
                              + " or seq num values for a given selected Peak"
                              + " have already been set, these values will be the default"
                              + " values in the displayed text boxes</body></html>" );

            JOptionPane.showMessageDialog( null , jed );

            return;

         }
      }


      private void update( JPopupMenu pop , String Text , String ToolTip )
      {

         JMenuItem jmen = pop.add( Text );
         jmen.addActionListener( this );
         jmen.setToolTipText( ToolTip );

      }


      public void MakePopUpMenu( JButton BUT )
      {

         if( disable )
            return;

         JPopupMenu pop = new JPopupMenu();

         update( pop , SET_PEAK , "Sets the Q vals and seq num for a peak" );

         update( pop , SET_HKL , "Sets the hkl vals for a peak" );

         update( pop , CLEAR_SET_PEAKS , "Clears all settings" );

         pop.show( BUT , 0 , 0 );

      }


      public void MakePopUpMenu1( JButton item )
      {

         if( disable )
            return;

         JPopupMenu pop = new JPopupMenu();

         pop.add( SET_PEAK1 ).addActionListener( this );

         pop.add( SET_PEAK2 ).addActionListener( this );

         pop.add( SET_PEAK3 ).addActionListener( this );

         pop.add( SET_PEAK4 ).addActionListener( this );

         pop.add( SET_PEAKN ).addActionListener( this );

         pop.add( "Help" ).addActionListener( this );

         pop.show( item , 0 , 0 );

      }


      /**
       * Currently Uses a JOptionPane with an interesting JPanel as message. Events
       * are not needed because it is a dialog box.
       * 
       * @param specifyPeak    Gets Q values if specifyPeak is true otherwise 
       *                       gets hkl values
       * @param PeakNum       The set Peak number. If 5 or more, a spinner will
       *                      allow for changing this value
       * @param men           The JButton on which to hang the OptionPane on.
       */
      public void MakeMenu2( boolean SpecifyPeak , int PeakNum , JComponent men )
      {

         int seqNum = - 1;

         if( View != null )

            seqNum = View.getLastSelectedSeqNum();

         JPanel message = new JPanel();

         int n = 1;

         if( SpecifyPeak )
            n++ ;
         if( PeakNum > 3 )
            n++ ;
         if( seqNum > 0 )
            n++ ;

         message.setLayout( new GridLayout( n , 1 ) );

         JSpinner spin = null;
         JCheckBox inView = null;
         if( seqNum >= 1 )

            inView = new JCheckBox( "Use Q View Selection" , SpecifyPeak );


         //------------------ spinner
         if( PeakNum > 3 )

            spin = new JSpinner( new SpinnerNumberModel( 5 , 5 ,
                     MAX_SEL_PEAKS + 1 , 1 ) );

         if( spin != null )
         {
            JPanel p = new JPanel();
            p.setLayout( new GridLayout( 1 , 2 ) );

            p.add( new JLabel( "Peak to set " , SwingConstants.RIGHT ) );
            p.add( spin );

            message.add( p );

         }

         //------------------------ Use data selected from view
         if( inView != null )
            message.add( inView );

         //----------------------- seq num text
         JTextField text = null;
         if( SpecifyPeak )
         {
            JPanel pp = new JPanel();
            pp.setLayout( new GridLayout( 1 , 2 ) );

            pp.add( new JLabel( "Sequence Number" ) );
            text = new JTextField( "" + seqNums[ PeakNum ] );

            pp.add( text );

            text.setToolTipText( "<html><body>If enabled and 1 or more, the"
                     + " peak with this seq num values <BR> are used NOT the "
                     + "values from bottom text entry area</body></html>" );

            message.add( pp );
         }


         JPanel p = new JPanel();
         String lab = "Enter Q vals";
         String InitVal = "1.1 , 2.3, -4";

         if( ! SpecifyPeak )
         {
            lab = "Enter h,k,l value";

            if( PeakNum < 3 && ! Float.isNaN( hklVals[ PeakNum ][ 0 ] )
                     && ! Float.isNaN( hklVals[ PeakNum ][ 1 ] )
                     && ! Float.isNaN( hklVals[ PeakNum ][ 2 ] ) )
               InitVal = hklVals[ PeakNum ][ 0 ] + "," + hklVals[ PeakNum ][ 1 ]
                        + "," + hklVals[ PeakNum ][ 2 ];

         }
         else if( ! Float.isNaN( SelectedPeaks[ PeakNum ][ 0 ] )
                  && ! Float.isNaN( SelectedPeaks[ PeakNum ][ 1 ] )
                  && ! Float.isNaN( SelectedPeaks[ PeakNum ][ 2 ] ) )

            InitVal = SelectedPeaks[ PeakNum ][ 0 ] + ","
                     + SelectedPeaks[ PeakNum ][ 1 ] + ","
                     + SelectedPeaks[ PeakNum ][ 2 ];

         p.add( new JLabel( lab ) );
         message.add( p );


         //-------------- disable handling --
         if( inView != null )
         {
            inView.addActionListener( new DisableCompListener( true ,
                     new JComponent[]
                     {
                        text
                     } ) );
            if( inView.isSelected() && text != null )
               text.setEnabled( false );
         }

         String S = JOptionPane.showInputDialog( men , message , InitVal );

         if( S == null )
            return;

         if( PeakNum > 3 && spin != null )
            PeakNum = ( (Integer) spin.getValue() ).intValue() - 1;

         float[] q = new float[ 3 ];

         int SNum = - 1;
         try
         {
            if( text != null)
            SNum = Integer.parseInt( text.getText().trim() );

         }
         catch( Exception ss )
         {}

         if( SNum > 0 && SNum < Peaks.size()
                  && ( inView == null || ! inView.isSelected() ) )
         {

            if( SpecifyPeak )
               seqNums[ PeakNum ] = SNum;

            IPeak pk = Peaks.elementAt( SNum - 1 );
            q = pk.getUnrotQ();

         }
         else if( seqNum > 0 && inView != null && inView.isSelected() )
         {
            if( SpecifyPeak )
               seqNums[ PeakNum ] = seqNum;

            IPeak pk = Peaks.elementAt( seqNum - 1 );
            q = pk.getUnrotQ();

         }
         else if( S != null )
         {

            if( SpecifyPeak )
               seqNums[ PeakNum ] = - 1;

            String[] SS = S.split( "," );
            if( SS.length != 3 )

               q[ 0 ] = q[ 1 ] = q[ 2 ] = Float.NaN;

            else

               for( int i = 0 ; i < 3 ; i++ )
                  try
                  {
                     q[ i ] = Float.parseFloat( SS[ i ].trim() );
                  }
                  catch( Exception sss )
                  {
                     q[ 0 ] = q[ 1 ] = q[ 2 ] = Float.NaN;
                  }
         }

         if( SpecifyPeak )

            SelectedPeaks[ PeakNum ] = q;

         else

            hklVals[ PeakNum ] = q;

         if( SpecifyPeak && PeakNum == MaxPksSet )

            MaxPksSet = FindLastSet( PeakNum , MaxPksSet , SelectedPeaks );

         else if( ! SpecifyPeak && PeakNum == MaxhklSet )

            MaxhklSet = FindLastSet( PeakNum , MaxhklSet , hklVals );
         
         fireSetPeakListeners( SET_PEAK_INFO_CHANGED );

      }


      private int FindLastSet( int PeakNum , int maxSetSoFar , float[][] List )
      {

         if( PeakNum < maxSetSoFar )
            return maxSetSoFar;

         if( List == null )
            return 0;

         for( int i = maxSetSoFar ; i < List.length ; i++ )
            for( int j = 0 ; j < 3 ; j++ )
               if( Float.isNaN( List[ i ][ j ] ) )

                  return i;

         return List.length;

      }
   }

}
