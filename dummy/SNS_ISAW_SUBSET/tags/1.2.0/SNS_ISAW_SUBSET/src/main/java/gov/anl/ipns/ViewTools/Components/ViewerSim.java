/* 
 * file: ViewerSim.java
 *
 * Copyright (C) 2003, Mike Miller
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
 * Primary   Mike Miller <millermi@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *           
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log: ViewerSim.java,v $
 *  Revision 1.14  2007/03/16 18:32:37  dennis
 *  Adapted to new getSelectedPoints() method for regions.  Now passes in
 *  the world_to_array transformation, as an argument to getSelectedPoints().
 *
 *  Revision 1.13  2006/03/30 23:57:56  dennis
 *  Modified to not require the use of mutator methods for the
 *  virtual arrays.  These changes were required since the concept
 *  of a "mutable" virtual array was separated from the concept of
 *  a virtual array.
 *
 *  Revision 1.12  2005/06/02 22:31:21  dennis
 *  Modified to only use IVirtualArray2D methods after creating a
 *  VirtualArray2D object.
 *
 *  Revision 1.11  2005/01/18 23:11:00  millermi
 *  - Listeners that previously listened for events from the
 *    SelectionOverlay now listen for the SELECTED_CHANGED event
 *    from the ImageViewComponent.
 *
 *  Revision 1.10  2004/09/15 21:55:44  millermi
 *  - Updated LINEAR, TRU_LOG, and PSEUDO_LOG setting for AxisInfo class.
 *    Adding a second log required the boolean parameter to be changed
 *    to an int. These changes may affect any ObjectState saved configurations
 *    made prior to this version.
 *
 *  Revision 1.9  2004/03/12 03:36:47  millermi
 *  - Changed package, fixed imports.
 *
 *  Revision 1.8  2004/03/10 23:37:28  millermi
 *  - Changed IViewComponent interface, no longer
 *    distinguish between private and shared controls/
 *    menu items.
 *  - Combined private and shared controls/menu items.
 *
 *  Revision 1.7  2004/01/08 23:55:08  rmikk
 *  Added Save and Print image utilities to the File menu item
 *  Eliminate the EXIT_ON_CLOSE settings for the JFrames
 *
 *  Revision 1.6  2003/12/18 22:42:12  millermi
 *  - This file was involved in generalizing AxisInfo2D to
 *    AxisInfo. This change was made so that the AxisInfo
 *    class can be used for more than just 2D axes.
 *
 *  Revision 1.5  2003/10/16 05:00:03  millermi
 *  - Fixed java docs errors.
 *
 *  Revision 1.4  2003/08/11 23:46:13  millermi
 *  - Adding test for selected regions.
 *
 *  Revision 1.3  2003/06/06 18:50:04  dennis
 *  (Mike Miller) Altered space allocated by the control viewer.
 *
 *  Revision 1.2  2003/05/29 14:26:54  dennis
 *  Two changes: (Mike Miller)
 *   -added exit on close feature
 *   -added coordination in displaying window and f2 JFrames, no longer initially
 *    display over each other.
 * 
 *  Revision 1.1  2003/05/22 13:06:55  dennis
 *  Basic test program for ViewComponent.
 *
 */

 package gov.anl.ipns.ViewTools.Components;
 
 import javax.swing.*;
 import java.awt.Point;
 import java.awt.event.*;
 import java.awt.Container;
 import java.awt.Rectangle;
 
 import gov.anl.ipns.ViewTools.Components.ViewControls.*;
 import gov.anl.ipns.ViewTools.Components.TwoD.*;
 import gov.anl.ipns.ViewTools.Components.Menu.*;
 import gov.anl.ipns.ViewTools.Components.Region.Region;
 import gov.anl.ipns.ViewTools.Panels.Transforms.CoordTransform;

/**
 * This class is a mock viewer to test basic functionality of any 
 * IViewComponent2D components. One big difference between this tester
 * and the newer IVCTester is that the controls are in a separate panel.
 */ 
public class ViewerSim
{ 
   private IViewComponent2D ivc;
   private ViewMenuItem[] menus;
   private ViewControl[] controls;

  /**
   * Constructor reads in an IViewComponent2D and gets the controls and menu
   * from that component.
   *
   *  @param  comp - IViewComponent2D component
   */
   public ViewerSim( IViewComponent2D comp )
   {
      ivc = comp;
      menus = ivc.getMenuItems();
      controls = ivc.getControls();
      ivc.addActionListener( new IVCListener() );
   }
  
  /**
   * Displays IViewComponent2D with its menus. The Controls are displayed
   * in a separate window.
   */ 
   public void show() 
   {
      JFrame window = new JFrame("Test Viewer");
      window.setBounds(0,0,500,500);
      JPanel DispPanel = ivc.getDisplayPanel();
      window.getContentPane().add( DispPanel );
      JMenuBar menu_bar = new JMenuBar();
      window.setJMenuBar(menu_bar);       
 
      JMenu fileMenu    = new JMenu("File");
      //DataSetTools.viewer.SaveImageActionListener.setUpMenuItem(fileMenu,DispPanel);
      //DataSetTools.viewer.PrintComponentActionListener.setUpMenuItem(fileMenu,DispPanel);
     
      if( menus.length > 0 )
      {
               // Menus for the menu bar 
         JMenu editMenu    = new JMenu("Edit");
         JMenu viewMenu    = new JMenu("View");
         JMenu optionsMenu = new JMenu("Options");
   
         menu_bar.add(fileMenu); 
         menu_bar.add(editMenu); 
         menu_bar.add(viewMenu); 
         menu_bar.add(optionsMenu);
      
         for( int i = 0; i < menus.length; i++ )
         {
            if( ViewMenuItem.PUT_IN_FILE.toLowerCase().equals(
	             menus[i].getPath().toLowerCase()) )
               fileMenu.add( menus[i].getItem() ); 
            else if( ViewMenuItem.PUT_IN_EDIT.toLowerCase().equals(
	          menus[i].getPath().toLowerCase()) )
               editMenu.add( menus[i].getItem() );
            else if( ViewMenuItem.PUT_IN_VIEW.toLowerCase().equals(
	          menus[i].getPath().toLowerCase()) )
               viewMenu.add( menus[i].getItem() );
            else // put in options menu
               optionsMenu.add( menus[i].getItem() );           
         }      
      }
      window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      window.setVisible(true);
 
      if( controls.length > 0 )
      {
         JFrame f2 = new JFrame("ISAW ImageViewControls");
         Container cpain = f2.getContentPane();
         cpain.setLayout( new BoxLayout(cpain,BoxLayout.Y_AXIS));  

         for( int i = 0; i < controls.length; i++ )
            cpain.add(controls[i]);
	 Rectangle main = window.getBounds();
	 int x = (int)( main.getX() + main.getWidth() );
	 int y = (int)( main.getY() );
         f2.setBounds(x, y, 200, y + (60 * controls.length)); 
	 f2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         f2.setVisible(true); //display the frame      
      }
   }
   
   private class IVCListener implements ActionListener
   {
     public void actionPerformed( ActionEvent ae )
     {
       String message = ae.getActionCommand();
       if( message.equals(ImageViewComponent.SELECTED_CHANGED) )
       {
         Region[] selectedregions = ivc.getSelectedRegions();
         CoordTransform world_to_array = ivc.getWorldToArrayTransform();
	 for( int i = 0; i < selectedregions.length; i++ )
	 {
	   Point[] selectedpoints = 
		           selectedregions[i].getSelectedPoints(world_to_array);
	   System.out.println("NumSelectedPoints: " + selectedpoints.length);
	   for( int j = 0; j < selectedpoints.length; j++ )
	   {
	     System.out.println("(" + selectedpoints[j].x + "," + 
	                        selectedpoints[j].y + ")" );
	   }
	 }
       }
     }
   }

  /*
   * MAIN - Basic main program to test an ImageViewComponent object
   */
   public static void main( String args[] ) 
   {
      // *** test ImageViewComponent ***
      int n_cols = 250;
      int n_rows = 250;	
      //Make a sample 2D array

      float arr[][] = new float[ n_rows ][ n_cols ];
      for(int i = 0; i < n_rows; i++)
      {
         for(int j = 0; j < n_cols; j++)
         {
            if ( i % 25 == 0 )
               arr[i][j] = i*n_cols; 
            else if ( j % 25 == 0 )
               arr[i][j] = j*n_rows; 
            else
               arr[i][j] = i*j;
         }
      }

      IVirtualArray2D va2D = new VirtualArray2D( arr ); 
      va2D.setAxisInfo( AxisInfo.X_AXIS, .001f, .1f, 
                           "TestX","TestUnits", AxisInfo.LINEAR );
      va2D.setAxisInfo( AxisInfo.Y_AXIS, 0f, -1f, 
                            "TestY","TestYUnits", AxisInfo.LINEAR );
      va2D.setTitle("Main Test");
      
      ImageViewComponent livc = new ImageViewComponent(va2D);
      
      ViewerSim viewer = new ViewerSim(livc);
      viewer.show(); 
   }
} 
