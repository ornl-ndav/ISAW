/*
 * File:  ImageFrame2.java
 *
 * Copyright (C) 2003, Dennis Mikkelson, Mike Miller
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.7  2003/12/18 22:44:21  millermi
 * - This file was involved in generalizing AxisInfo2D to
 *   AxisInfo. This change was made so that the AxisInfo
 *   class can be used for more than just 2D axes.
 *
 * Revision 1.6  2003/08/29 23:07:31  millermi
 * - setData() now will setVisible(true) if the ImageFrame2
 *   is not visible.
 * - setData() now creates a new pane for the ImageFrame2 if
 *   the data array is of different size.
 * - Labels and titles are now updated when setData() is called.
 *
 * Revision 1.5  2003/08/26 06:00:02  millermi
 * - Replaced EXIT_ON_CLOSE with HIDE_ON_CLOSE so it does not
 *   close other windows.
 * - Removed unnecessary code, including import statements.
 *
 * Revision 1.4  2003/08/25 15:55:35  dennis
 * No longer exits entire application when window is closed.
 * Sets correct title on Frame, when axes change.
 * Places all controls in box, and uses larger spacer panel
 * to keep compomponents small.
 *
 * Revision 1.3  2003/08/14 17:05:37  millermi
 * - Changed how controls are selected for display.
 *
 * Revision 1.2  2003/08/13 02:53:23  millermi
 * - Removed controls for Selection Overlay and Color scale
 * - Controls now contained in a Box instead of a JPanel.
 *
 * Revision 1.1  2003/08/07 15:58:58  dennis
 * - Further implementation of basic structure provided in
 *   ImageFrame.java.
 * - Uses an ImageViewComponent instead of ImageJPanel, which
 *   allows for controls to tweak the image.
 *   (Mike Miller)
 *
 * Revision 1.3  2003/06/05 14:35:09  dennis
 * Added method to set a new image and change the title on the frame.
 *
 * Revision 1.2  2003/01/08 20:11:37  dennis
 * Now shows blank frame and writes error message if a null image is
 * specified.
 *
 * Revision 1.1  2003/01/08 19:23:05  dennis
 * Initial version
 *
 */

package DataSetTools.trial;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import DataSetTools.components.View.TwoD.ImageViewComponent;
import DataSetTools.components.View.AxisInfo;
import DataSetTools.components.View.IVirtualArray2D;
import DataSetTools.components.View.VirtualArray2D;
import DataSetTools.components.View.Menu.ViewMenuItem;
import DataSetTools.components.containers.SplitPaneWithState;

/**
 * Simple class to display an image, specified by an IVirtualArray2D or a 
 * 2D array of floats, in a frame.
 */
public class ImageFrame2 extends JFrame
{
  private SplitPaneWithState pane; // complete viewer, includes controls and ijp
  private ImageViewComponent ivc;
  private IVirtualArray2D data;
  private JMenuBar menu_bar;
  private boolean paneadded = false;

 /**
  * Construct a frame with the specified image and title
  *  
  *  @param  iva
  */
  public ImageFrame2( IVirtualArray2D iva )
  {
    data = new VirtualArray2D(1,1);
    menu_bar = new JMenuBar();
    setJMenuBar(menu_bar);   
    menu_bar.add(new JMenu("File")); 
    menu_bar.add(new JMenu("Options"));
    
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    setBounds(0,0,700,500);
    
    setData(iva);
    setVisible(true);
  }

 /**
  * Construct a frame with the specified image and title
  *  
  *  @param  array
  *  @param  xinfo
  *  @param  yinfo
  *  @param  title
  */  
  public ImageFrame2( float[][] array, 
                      AxisInfo xinfo,
		      AxisInfo yinfo,
		      String title )
  {
    VirtualArray2D temp = new VirtualArray2D( array );
    temp.setAxisInfo( AxisInfo.X_AXIS, xinfo.copy() );
    temp.setAxisInfo( AxisInfo.Y_AXIS, yinfo.copy() );
    temp.setTitle(title);
    
    data = new VirtualArray2D(1,1);
    
    menu_bar = new JMenuBar();
    setJMenuBar(menu_bar);   
    menu_bar.add(new JMenu("File")); 
    menu_bar.add(new JMenu("Options"));
    
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    setBounds(0,0,700,500);
    
    setData(temp);
    setVisible(true);
  }
  
 /**
  * This method takes in a virtual array and updates the image. If the array
  * is the same size as the previous data array, the image is just redrawn.
  * If the size is different, the frame is disposed and a new view component
  * is constructed.
  *
  *  @param  values
  */ 
  public void setData( IVirtualArray2D values )
  {
    // if new array is same size as old array
    if( values.getNumRows() == data.getNumRows() &&
        values.getNumColumns() == data.getNumColumns() )
    {  
      data = values;
      ivc.dataChanged(data);
    }  
    // if different sized array, remove everything and build again.
    else
    {
      dispose();
      // if pane has been added, remove it.
      if( paneadded )
        remove(pane);
      data = values;
      buildPane();
      getContentPane().add(pane);
      paneadded = true;
    }
    setTitle( values.getTitle() );       // set correct title on frame
    if( !isVisible() )
      setVisible(true);
  }
  
 /**
  * This method takes in a 2D array and updates the image. If the array
  * is the same size as the previous data array, the image is just redrawn.
  * If the size is different, the frame is disposed and a new view component
  * is constructed.
  *
  *  @param  array
  */ 
  public void setData( float[][] array )
  {
    setData( new VirtualArray2D(array) );
  }

 /*
  * This method builds the content pane of the frame.
  */
  private void buildPane()
  {  
    setTitle( data.getTitle() );
    ivc = new ImageViewComponent( data );
    ivc.setColorControlSouth(true);
    Box controls = new Box(BoxLayout.Y_AXIS);
    JComponent[] ctrl = ivc.getSharedControls();
    for( int i = 0; i < ctrl.length; i++ )
      controls.add(ctrl[i]);
    
    JPanel spacer = new JPanel();
//    spacer.setPreferredSize(new Dimension(0,((10-ctrlcounter)*40) ) );
    spacer.setPreferredSize( new Dimension(0,10000) );
    controls.add(spacer);
    pane = new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
                                  ivc.getDisplayPanel(),
			          controls, .75f );
    
    if( !paneadded ) // only add the menu items for the ivc once
    {
      // get menu items from view component and place it in a menu
      ViewMenuItem[] menus = ivc.getSharedMenuItems();
    
      for( int i = 0; i < menus.length; i++ )
      {
        if( ViewMenuItem.PUT_IN_FILE.toLowerCase().equals(
                 menus[i].getPath().toLowerCase()) )
          menu_bar.getMenu(0).add( menus[i].getItem() ); 
        else // put in options menu
          menu_bar.getMenu(1).add( menus[i].getItem() ); 	  
      }	
    }   
  }

 /*
  * Testing purposes only
  */
  public static void main( String args[] )
  {
    float test_array[][] = new float[500][500];
    for ( int i = 0; i < 500; i++ )
      for ( int j = 0; j < 500; j++ )
        test_array[i][j] = i + j;
    VirtualArray2D va2D = new VirtualArray2D( test_array );
    va2D.setAxisInfo( AxisInfo.X_AXIS, 0f, 10000f, 
    		        "TestX","TestUnits", true );
    va2D.setAxisInfo( AxisInfo.Y_AXIS, 0f, 1500f, 
    			"TestY","TestYUnits", false );
    va2D.setTitle("ImageFrame Test");
    ImageFrame2 im_frame = new ImageFrame2( va2D );
    // test setData() 10 times
    /*
    for( int x = 0; x < 20; x++ )
    {
      for ( int i = 0; i < 500; i++ )
        for ( int j = 0; j < 500; j++ )
          test_array[i][j] = i*j;
      va2D = new VirtualArray2D( test_array );
      im_frame.setData(va2D);
    }
    
    ImageFrame2 im_frame2 = new ImageFrame2( test_array,
                                             new AxisInfo( 0f, 10000f,"TestX",
			                                    "TestUnits", true ),
                                             new AxisInfo( 0f, 1500f,"TestY",
			                                   "TestYUnits", true ),
					     "ImageFrame Alternate Test" );
    im_frame2.setData( test_array );*/
  }

}
