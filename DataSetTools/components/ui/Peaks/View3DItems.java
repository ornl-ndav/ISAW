/* 
 * File: View3DItems.java
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


import gov.anl.ipns.Util.Numeric.IntList;

import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;


/**
 * A JButton that handles viewing certain information in the 3D view of peaks in
 * reciprocal space( Q values)
 * 
 * @author Ruth
 * 
 */
public class View3DItems extends JButton
{

   /**
    * Value = "Runs". Will cause different runs to have different colors. The
    * Source of the event must be a JCheckBoxMenuItem
    */
   public static String     RUN_NUMS       = "Runs";

   /**
    * Value = "Detectors". Will cause different detectors to have different colors. The
    * Source of the event must be a JCheckBoxMenuItem
    */
   public static String     DETECTOR_NUMS  = "Detectors";

   private static String     SEQUENCE_NUMS  = "Sequence Numbers";

   /**
    * Value="Clear View". Clears out the highlighted sequence numbers specified by the GUI
    * or by the showSeqNums method.
    */
   public static String     CLEAR_SEQ_NUMS = "Clear View";

   /**
    * Value ="One Plane on three peaks". This command to the listener's
    *    actionPerformed method will show a plane through the first 3
    *    selected peaks.
    */
   public static String     SHOW1PLANE     = "One Plane on three peaks";

   /**
    * Value =""Family of planes". This command to the listener's
    *    actionPerformed method will show a plane through the first 3
    *    selected peaks and a family of parallel planes where the
    *    fourth selected point is on the first parallel plane( determines
    *    the distance between planes)
    */
   public static String     SHOW_PLANES    = "Family of planes";


   private View3D           view;
   
   private Vector< Integer > SeqNums_shown;
   /**
    * Help String in html
    */
   public static String Help = "<html><body> Displays Information in the 3D Q viewer<BR>"
                                      + "<OL> Information displayed <LI> Runs(Each run is a different color)<LI>Detectors"
                                      + "(each detector is a different color)<LI> specified seq nums are white<LI>"
                                      + "One Plane through 3 Selected Peaks <LI> Family of Planes parallel to first 3"
                                      + " selected planes. <Br>The direction from the 1st to the 4th selected point "
                                      + "gives the direction to the otherplanes<LI> Clears the planes from the "
                                      + "view</ol> NOTE: 3D Q Viewer does not"
                                      + " change when the selected points change. RePress the View button</body></html>";


   MyActionListener     listener;


   /**
    * Constructor
    * 
    * @param view
    *           3D viewer of peaks in Q
    * @param nPeaks
    *           Number of peaks
    * @param peakSetter
    *           The peak setting object
    */
   public View3DItems( View3D view, int nPeaks, SetPeaks peakSetter )
   {

      super( "View 3D Objects" );
      init( view , nPeaks , peakSetter );
   }


   /**
    * Constructor
    * 
    * @param icon
    * @param view
    *           3D viewer of peaks in Q
    * @param nPeaks
    *           Number of peaks
    * @param peakSetter
    *           The peak setting object
    */
   public View3DItems( Icon icon, View3D view, int nPeaks, SetPeaks peakSetter )
   {

      super( icon );
      init( view , nPeaks , peakSetter );
   }


   /**
    * Constructor
    * 
    * @param text
    *           text to appear on the Button
    * @param view
    *           3D viewer of peaks in Q
    * @param nPeaks
    *           Number of peaks
    * @param peakSetter
    *           The peak setting object
    */
   public View3DItems( String text, View3D view, int nPeaks, SetPeaks peakSetter )
   {

      super( text );
      init( view , nPeaks , peakSetter );
   }


   /**
    * Constructor
    * 
    * @param a
    *           Action
    * @param view
    *           3D viewer of peaks in Q
    * @param nPeaks
    *           Number of peaks
    * @param peakSetter
    *           The peak setting object
    */
   public View3DItems( Action a, View3D view, int nPeaks, SetPeaks peakSetter )
   {

      super( a );
      init( view , nPeaks , peakSetter );
   }


   /**
    * Constructor
    * 
    * @param text
    *           Text to appear on the button
    * @param icon
    *           icon
    * @param view
    *           3D viewer of peaks in Q
    * @param nPeaks
    *           Number of peaks
    * @param peakSetter
    *           The peak setting object
    */
   public View3DItems( String text, Icon icon, View3D view, int nPeaks,
            SetPeaks peakSetter )
   {

      super( text , icon );
      init( view , nPeaks , peakSetter );
   }


   private void init( View3D view , int nPeaks , SetPeaks peakSetter )
   {

      listener = new MyActionListener( view , nPeaks , peakSetter );
      addActionListener( listener );
      this.view = view;
   }
 
   /**
    * Method to programmatically show sequence numbeers
    * @param seqNums  The sequence numbers to show in addition to those
    *                 now showing
    */
   public void showSeqNums(int[] seqNums)
   {
      view.HighlightSeqNums( seqNums , true );
      if( seqNums != null)
      for( int i=0; i< seqNums.length; i++)
         if( !SeqNums_shown.contains( seqNums[i]))
            SeqNums_shown.add( seqNums[i]);
      return;
   }

   String               OmittedMenuItems;
   /**
    * Determines which menu items to display(not implemented yet)
    * @param MenuItem   The name on the menu item
    * @param show       if true it will show otherwise it will not
    *                   be displayed.
    */
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
    * Returns a listener that handles the following ActionEvent commands
    * Listed above
    * 
    * @return  The action listener
    */
   public ActionListener getListener()
   {
      return listener;
   }

   /**
    * listener to menu items
    * 
    * @author Ruth
    * 
    */
   class MyActionListener implements ActionListener
   {



      View3D            View;

      int               NPeaks;

      SetPeaks          PeakSetter;

      boolean           runs , dets;


      public MyActionListener( View3D view, int nPeaks, SetPeaks peakSetter )
      {

         View = view;
         NPeaks = nPeaks;
         runs = dets = false;
         SeqNums_shown = new Vector< Integer >();
         PeakSetter = peakSetter;
      }


      /*
       * (non-Javadoc)
       * 
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

         if( e.getSource() instanceof JButton )
         {
            MakeMenu( (JComponent) ( e.getSource() ) );

            return;
         }

         String evtString = e.getActionCommand();
         JMenuItem source = (JMenuItem) e.getSource();

         if( evtString == RUN_NUMS )
         {
            runs = ( (JCheckBoxMenuItem) source ).getState();
            View.ColorRunsDetectors( runs , dets );
            return;

         }
         if( evtString == DETECTOR_NUMS )
         {
            dets = ( (JCheckBoxMenuItem) source ).getState();
            View.ColorRunsDetectors( runs , dets );
            return;

         }
         if( evtString == SEQUENCE_NUMS )
         {

            String res = JOptionPane.showInputDialog( source ,
                     "Enter Sequence numbers" , "1,3,5:9" );
            if( res == null || res.length() < 1 )
               return;
            int[] seqNums ;
            res = res.trim();
            if( res.endsWith( "]" ))
               res = res.substring( 0, res.length()-1 );
            if( res.startsWith( "[" ))
               res = res.substring( 1 );
            
            seqNums = IntList.ToArray(res);
           /* String[] inputs = res.split( "," );
            if( inputs == null || inputs.length < 1 )
               return;

            int[] seqNums = new int[ inputs.length ];

            for( int i = 0 ; i < seqNums.length ; i++ )
               try
               {
                  seqNums[ i ] = Integer.parseInt( inputs[ i ].trim() );
                  if( ! SeqNums_shown.contains( seqNums[ i ] ) )
                     SeqNums_shown.add( seqNums[ i ] );

               }
               catch( Exception ss )
               {
                  JOptionPane.showMessageDialog( null ,
                           "Improper input format " + i + "=" + inputs[ i ] );
                  return;

               }
            */
            View.HighlightSeqNums( seqNums , true );
            for( int i=0; i< seqNums.length; i++)
               if( !SeqNums_shown.contains( seqNums[i]))
                  SeqNums_shown.add( seqNums[i]);
            return;
         }


         if( evtString == CLEAR_SEQ_NUMS )
         {
            int[] seqNums = new int[ SeqNums_shown.size() ];

            for( int i = 0 ; i < seqNums.length ; i++ )
               seqNums[ i ] = SeqNums_shown.elementAt( i ).intValue();

            View.HighlightSeqNums( seqNums , false );

            SeqNums_shown.clear();
            View.showPlane(  null , null , null );
            return;
         }

         if( evtString == SHOW1PLANE )
         {
            View.showPlane( PeakSetter.getSetPeakQ( 0 ) , PeakSetter
                     .getSetPeakQ( 1 ) , PeakSetter.getSetPeakQ( 2 ) );

            return;
         }


         if( evtString == SHOW_PLANES )
         {

            View.showPlanes( PeakSetter.getSetPeakQ( 0 ) , PeakSetter
                     .getSetPeakQ( 1 ) , PeakSetter.getSetPeakQ( 2 ) ,
                     PeakSetter.getSetPeakQ( 3 ) );

            return;
         }

         if( evtString.equals( "Help" ) )
         {
            JEditorPane edPane = new JEditorPane( "text/html" , Help );
            JOptionPane.showMessageDialog( null , edPane );
         }

      }


      public void MakeMenu( JComponent comp )
      {

         JPopupMenu popUpMenu = new JPopupMenu( "3D Items" );

         popUpMenu.add( new JCheckBoxMenuItem( RUN_NUMS , runs ) )
                  .addActionListener( this );
         popUpMenu.add( new JCheckBoxMenuItem( DETECTOR_NUMS , dets ) )
                  .addActionListener( this );
         popUpMenu.add( new JMenuItem( SEQUENCE_NUMS ) ).addActionListener(
                  this );

         if( PeakSetter != null )
         {
            int nset = 0;
            for( int i = 0 ; i < 4 && PeakSetter.getSetPeakQ( i ) != null ; i++ )
               nset++ ;
            if( nset >= 3 )
               popUpMenu.add( new JMenuItem( SHOW1PLANE ) ).addActionListener(
                        this );
            if( nset >= 4 )
               popUpMenu.add( new JMenuItem( SHOW_PLANES ) ).addActionListener(
                        this );
         }

         popUpMenu.add( new JMenuItem( CLEAR_SEQ_NUMS ) ).addActionListener(
                  this );
         popUpMenu.add( new JMenuItem( "Help" ) ).addActionListener( this );

         popUpMenu.show( comp , comp.getWidth()*3/4 , comp.getHeight()/2 );

      }

   }


}
