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
 *  $Log$
 *  Revision 1.1  2003/05/22 13:06:55  dennis
 *  Basic test program for ViewComponent.
 *
 */

 package DataSetTools.components.View;
 
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.Container;
 
 import DataSetTools.components.View.ViewControls.*;
 import DataSetTools.components.View.TwoD.*;
 import DataSetTools.components.View.Menu.*;
 import DataSetTools.components.View.ViewControls.*;
 
public class ViewerSim
{ 
   private IViewComponent2D ivc;
   private ViewMenuItem[] menus;
   private JComponent[] controls;

  /**
   * Constructor reads in an IViewComponent2D and gets the controls and menu
   * from that component.
   */
   public ViewerSim( IViewComponent2D comp )
   {
      ivc = comp;
      menus = ivc.getSharedMenuItems();
      controls = ivc.getSharedControls();
   }
  
  /**
   * Displays IViewComponent2D with its menus. The Controls are displayed
   * in a separate window.
   */ 
   public void show() 
   {
      JFrame window = new JFrame("Test Viewer");
      window.setBounds(0,0,500,500);
      window.getContentPane().add( ivc.getDisplayPanel() );
      
      if( menus.length > 0 )
      {
         JMenuBar menu_bar = new JMenuBar();
         window.setJMenuBar(menu_bar);       
 
         JMenu fileMenu    = new JMenu("File");      // Menus for the menu bar 
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
      window.show();
 
      if( controls.length > 0 )
      {
         JFrame f2 = new JFrame("ISAW ImageViewControls");
         Container cpain = f2.getContentPane();
         cpain.setLayout( new BoxLayout(cpain,BoxLayout.Y_AXIS));  

         for( int i = 0; i < controls.length; i++ )
            cpain.add(controls[i]);
         f2.setBounds(0,0,200,(100 * controls.length)); 
         f2.show(); //display the frame      
      }
   }

  /*
   * MAIN - Basic main program to test an ImageViewComponent object
   */
   public static void main( String args[] ) 
   {
      // *** test ImageVieComponent ***
      int col = 250;
      int row = 250;	
      //Make a sample 2D array
      VirtualArray2D va2D = new VirtualArray2D(row, col); 
      va2D.setAxisInfoVA( AxisInfo2D.XAXIS, .001f, .1f, 
                           "TestX","TestUnits", true );
      va2D.setAxisInfoVA( AxisInfo2D.YAXIS, 0f, -1f, 
                            "TestY","TestYUnits", true );
      va2D.setTitle("Main Test");
      //Fill the 2D array with the function x*y
      float ftemp;
      for(int i = 0; i < row; i++)
      {
         for(int j = 0; j < col; j++)
         {
            ftemp = i*j;
            if ( i % 25 == 0 )
	       va2D.setDataValue(i, j, i*col); //put float into va2D
            else if ( j % 25 == 0 )
	       va2D.setDataValue(i, j, j*row); //put float into va2D
            else
	       va2D.setDataValue(i, j, ftemp); //put float into va2D
	 }
      }
      ImageViewComponent livc = new ImageViewComponent(va2D);
      
      ViewerSim viewer = new ViewerSim(livc);
      viewer.show(); 
   }
} 
