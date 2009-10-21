
/* 
 * File: RotatePeaksInfoHandler.java
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

import gov.anl.ipns.MathTools.Geometry.Tran3D;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.*;
import DataSetTools.operator.Generic.TOF_SCD.IPeak;


/**
 * The View3D class has the ability to specify choices via mouse clicks on the
 * Graphics area. This is the Controller part of the View3D( which is just the
 * Viewing part)
 * 
 * @author Ruth
 * 
 */
public class View3DControl implements ActionListener
{



   public static String          SELECT_PEAK_MODE  = "Select Peak";

   public static String          ROTATE_PEAKS_MODE = "Rotate Peaks";

   public static String          NO_MOUSE_MODE     = "Disable Mouse";

   View3D                        view;

   Vector< IPeak >               Peaks;

   Vector< ISelectPeakHandler >  SelectPeakHandlers;

   Vector< IRotatePeaksHandler > RotatePeaksHandlers;

   boolean                       disableView3DMouseModeMenu;

   int                           currentView3DMouseMode;


   /**
    * Constructor
    * 
    * @param view
    *           The 3D view of peaks in reciprocal space
    * @param Peaks
    *           A vector of peaks
    */
   public View3DControl( View3D view, Vector< IPeak > Peaks )
   {

      this.view = view;
      this.Peaks = Peaks;
      SelectPeakHandlers = new Vector< ISelectPeakHandler >();
      RotatePeaksHandlers = new Vector< IRotatePeaksHandler >();
      view.addActionListener( this );
      disableView3DMouseModeMenu = false;
      currentView3DMouseMode = View3D.PickMode;
      view.setMouseMode( currentView3DMouseMode );

   }

   public void kill()
   {
      view = null;
      Peaks = null;
      SelectPeakHandlers.clear();
      RotatePeaksHandlers.clear();
      SelectPeakHandlers = null;
      RotatePeaksHandlers = null;
   }
   /**
    * Sets the mouse input mode in the 3D view of peaks in reciprocal space
    * 
    * @param viewMode
    *           The view mode. Must be View3D.RotateMode, View3D.PickMode, or
    *           View3D.NoMouseMode
    * @param disableView3DMouseModeMenu
    *           Disables the Menu from right clicking in the 3D display of Q
    *           values of Peaks
    */
   public void setViewMode( int viewMode , boolean disableView3DMouseModeMenu )
   {

      this.disableView3DMouseModeMenu = disableView3DMouseModeMenu;
      view.setMouseMode( viewMode );
   }


   /**
    * 
    * @return The view mode in the 3D view of peaks in reciprocal space (Q
    *         values)
    */
   public int getViewMode()
   {

      return currentView3DMouseMode;
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
   // ---------------------------------------
   private JMenuItem getItemWithListener( String Name )
   {

      JMenuItem menItem = new JMenuItem( Name );
      menItem.addActionListener( this );
      return menItem;
   }


   private void MakeMenus()
   {

      if( disableView3DMouseModeMenu )
         return;

      JPopupMenu P = new JPopupMenu( "Mouse Actions" );
      
      P.add( getItemWithListener( SELECT_PEAK_MODE ) );
      P.add( getItemWithListener( ROTATE_PEAKS_MODE ) );
      P.add( getItemWithListener( NO_MOUSE_MODE ) );
      P.add( getItemWithListener( "Help" ) );

      Point pt = view.getCurrent_pixel_point();

      P.show( view , pt.x , pt.y );
      
   }


   /**
    * Adds an ISelectPeakHandler to be notified when a new peak is selected
    * 
    * @param handler
    *           The SelectPeakHandler
    */
   public void addSelectPeakHandler( ISelectPeakHandler handler )
   {

      if( handler == null )
         return;

      if( ! SelectPeakHandlers.contains( handler ) )
         SelectPeakHandlers.add( handler );
   }


   /**
    * removes an ISelectPeakHandler
    * 
    * @param handler
    *           The SelectPeakHandler
    */
   public void removeSelectPeakHandler( ISelectPeakHandler handler )
   {

      if( handler == null )
         return;

      if( SelectPeakHandlers.contains( handler ) )
         SelectPeakHandlers.remove( handler );


   }


   /**
    * removes all ISelectPeakHandler
    *
    */
   public void removeAllSelectPeakHandler()
   {

      SelectPeakHandlers.clear();
   }


   /**
    * Adds an IRotatePeaksHandler to be notified when the display of the peaks(
    * Q values) are rotated
    * 
    * @param handler
    *           The IRotatePeaksHandler
    */
   public void addRotatePeaksHandler( IRotatePeaksHandler handler )
   {

      if( handler == null )
         return;

      if( ! RotatePeaksHandlers.contains( handler ) )
         RotatePeaksHandlers.add( handler );
   }


   /**
    * Removes an IRotatePeaksHandler
    * 
    * @param handler
    *           The IRotPeaksHandler
    */
   public void removeRotatePeaksHandler( IRotatePeaksHandler handler )
   {

      if( handler == null )
         return;

      if( RotatePeaksHandlers.contains( handler ) )
         RotatePeaksHandlers.remove( handler );

   }


   /**
    * Removes all IRotatePeaksHandlers
   */
   public void removeAllRotatePeaksHandler()
   {

      RotatePeaksHandlers.clear();
   }


   /*
    * Handles the Menu items to set mouse mode in the 3D vies and reports
    * events( select peak and rotate peaks) to the appropriate handlers
    * 
    * (non-Javadoc)
    * 
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   @Override
   public void actionPerformed( ActionEvent e )
   {

      // Menu Items
      if( e.getActionCommand() == SELECT_PEAK_MODE )
      {
         view.setMouseMode( View3D.PickMode );

         currentView3DMouseMode = View3D.PickMode;
         return;

      }
      if( e.getActionCommand() == ROTATE_PEAKS_MODE )
      {
         view.setMouseMode( View3D.RotateMode );

         currentView3DMouseMode = View3D.RotateMode;
         return;

      }
      if( e.getActionCommand() == NO_MOUSE_MODE )
      {

         view.setMouseMode( View3D.NoMouseMode );

         currentView3DMouseMode = View3D.NoMouseMode;
         return;

      }
      if( e.getActionCommand().equals( "Help" ) )
         
         JOptionPane.showMessageDialog( null , new JEditorPane( "text/html" ,
                  View3D.Help ) );

      // Events from View3D
      if( e.getActionCommand() == View3D.ROTATED_DISPLAY )
      {
         Tran3D T = view.getCurrentTransformation();

         for( int i = 0 ; i < RotatePeaksHandlers.size() ; i++ )

            RotatePeaksHandlers.elementAt( i ).RotatePeaks( T );


         return;
      }

      if( e.getActionCommand() == View3D.SELECTED_PEAK_CHANGED )
      {

         int seqNum = view.getLastSelectedSeqNum();

         if( seqNum > 0 && seqNum <= Peaks.size() )
         {
            IPeak P = Peaks.elementAt( seqNum - 1 );
            for( int i = 0 ; i < SelectPeakHandlers.size() ; i++ )

               SelectPeakHandlers.elementAt( i ).SelectPeak( P );

         }

         return;
      }

      if( e.getActionCommand() == View3D.RIGHT_CLICKED )
      {
         MakeMenus();
         return;
      }

   }

}
