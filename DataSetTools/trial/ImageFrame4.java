/*
 * File:  ImageFrame4.java
 *
 * Copyright (C) 2007, Dennis Mikkelson, Mike Miller
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
 * Revision 1.2  2007/08/09 21:37:53  dennis
 * Now prints out number of points selected and first and
 * last point, for debugging purposes.
 *
 * Revision 1.1  2007/08/09 14:45:06  rmikk
 * New form with testing code
 *
 */

package DataSetTools.trial;

import gov.anl.ipns.ViewTools.Components.Menu.ViewMenuItem;
import gov.anl.ipns.Util.Numeric.floatPoint2D;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.Region.*;
import gov.anl.ipns.ViewTools.Components.TwoD.*;
import gov.anl.ipns.ViewTools.Components.Transparency.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.ViewControl;
import gov.anl.ipns.ViewTools.UI.SplitPaneWithState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.*;

/**
 * Simple class to display an image, specified by an IVirtualArray2D or a 
 * 2D array of floats, in a frame.
 */
public class ImageFrame4 extends JFrame
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
  public ImageFrame4( IVirtualArray2D iva )
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

    ivc.addActionListener( new IVC_Listener() );

    floatPoint2D p1 = new floatPoint2D( 300, 300 );
    floatPoint2D p2 = new floatPoint2D( 500, 500 );
    floatPoint2D points[] = { p1, p2 }; 
    Region region = new BoxRegion( points );
    ivc.addSelectedRegion( region );
  }

 /**
  * Construct a frame with the specified image and title
  *  
  *  @param  array
  *  @param  xinfo
  *  @param  yinfo
  *  @param  title
  */  
  public ImageFrame4( float[][] array, 
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
  
  
  public ImageViewComponent getImageViewComponent(){
     return ivc;
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
    ViewControl[] ctrl = ivc.getControls();
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
      ViewMenuItem[] menus = ivc.getMenuItems();
    
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


  public class IVC_Listener implements ActionListener
  {
    public void actionPerformed( ActionEvent ae )
    {
      String command = ae.getActionCommand();

      if ( command.equals( IViewComponent.POINTED_AT_CHANGED ) )
      {
        floatPoint2D pt = ivc.getPointedAt();
        System.out.println( "WC Coords = " + pt );
        CoordTransform world_to_array = ivc.getWorldToArrayTransform();
        pt = world_to_array.MapTo( pt );
        Point point = new Point( (int)pt.x, (int)pt.y );
        System.out.println( "Image coords = " + point ); 
      }
      else if( command.equals( IViewComponent.SELECTED_CHANGED ) )
      {
        String name = SelectionOverlay.DEFAULT_REGION_NAME;
        RegionOpList reg_list = ivc.getSelectedRegions(name);
        System.out.println("Selected Regions = " );
        System.out.println( reg_list );

        Point points[] = ivc.getSelectedPoints(name);
        int last_point = points.length - 1;
        System.out.println( "Number selected " + points.length );
        if ( last_point >= 0 )
        {
          System.out.println( "First Point = " + points[0] );
          System.out.println( "Last Point  = " + points[last_point] );
        }
      }
    }
  }


 /*
  * Testing purposes only
  */
  public static void main( String args[] )
  {
    final int NUM_ROWS = 100;
    final int NUM_COLS = 200;
    Random generator = new Random();

    float test_array[][] = new float[NUM_ROWS][NUM_COLS];
    for ( int i = 0; i < NUM_ROWS; i++ )
      for ( int j = 0; j < NUM_COLS; j++ )
        test_array[i][j] = i + j + (NUM_ROWS+NUM_COLS)*generator.nextFloat();
    IVirtualArray2D va2D = new VirtualArray2D( test_array );
    va2D.setAxisInfo( AxisInfo.X_AXIS, 0f, 10000f, 
    		        "TestX","TestUnits", AxisInfo.LINEAR );
    va2D.setAxisInfo( AxisInfo.Y_AXIS, 0f, 2000f, 
    			"TestY","TestYUnits", AxisInfo.LINEAR );
    va2D.setTitle("ImageFrame Test");
    ImageFrame4 Fr =new ImageFrame4( va2D );
    ImageViewComponent ivc = Fr.getImageViewComponent();

  String S="z";
  int n=0;
  float f=0f;
  String SS ="";
  RegionOpListWithColor Reg=null;
  while( !S.equals( "x")){
     System.out.println("Enter desired selection");
     System.out.println("  s: enter a String argument");
     System.out.println("  n: enter a number argument");
     System.out.println("  f: enter floating point argument");
     System.out.println("  c: create a new name");
     System.out.println("  C: clear a named selection");
     System.out.println("  R: remove a names selection");
     System.out.println("  G: get Selected Named Regions");
     System.out.println("  S: set Selected Named Regions");
     System.out.println("  N: show selected Names");
     System.out.println("  CN: get current name");
     System.out.println("  e: enable named selection, no show");
     System.out.println("  E. enable and show named selection");
     System.out.println("  B: add a box");
     System.out.println("  E1: add an ellipse");
     System.out.println("  O: set opacity");
     System.out.println("  d: disable editor");
     System.out.println("  SC: show color");
     System.out.println("  do: disableOverlay");
     System.out.println("  eo: enableOverlay");
     
     S = Command.Script_Class_List_Handler.getString();

     if(S.equals("s")){
        SS=Command.Script_Class_List_Handler.getString();
     }
     else if(S.equals("n")){
        try{
           n=new Integer(Command.Script_Class_List_Handler.getString()).
                    intValue();
        }catch( Exception ss){
           System.out.println(" n did not change. n="+n);
        }
     }
     else if(S.equals("f")){
        try{
           f=new Float(Command.Script_Class_List_Handler.getString()).
                    floatValue();
        }catch( Exception ss){
           System.out.println(" f did not change. f="+f);
        }
     }
     else if(S.equals("c")){
        if( SS.length()>1)
           ivc.enableSelection( SS ,false );
     }
     else if(S.equals("C")){
        if( SS.length() > 1)
           ivc.clearSelection( SS );
     }
     else if(S.equals("R")){
        if( SS.length() > 1)
           ivc.removeSelection(  SS );
     }
     else if(S.equals("G")){
        Reg = ivc.getSelectedRegions( SS);
     }
     else if(S.equals("S")){
        if( Reg != null)
           ivc.setSelectedRegions( Reg , SS );
     }
     else if(S.equals("N")){
        String[] names = ivc.getSelectionNames();
        if( names != null)
           for( int i=0; i< names.length; i++)
              System.out.println( names[i]);
     }
     else if(S.equals("CN")){
        
        System.out.println( ivc.getCurrentName());
     }
     else if(S.equals("e")){
        ivc.enableSelection(SS, false);
     }
     else if(S.equals("E")){
        ivc.enableSelection(SS, true);
     }
     else if(S.equals("O")){
        ivc.setOpacity( SS, f);
     }
     else if(S.equals("B")){
        
       ivc.addSelection( new RegionOp(
                new BoxRegion(new floatPoint2D[]{
                         new floatPoint2D(5f,5f),
                         new floatPoint2D(1000f,1000f)
                }),
                RegionOp.Operation.UNION) , 
                SS );
     }
     else if(S.equals("E1")){
        
        ivc.addSelection( new RegionOp(
                 new EllipseRegion(new floatPoint2D[]{
                          new floatPoint2D(850f,10f),
                          new floatPoint2D(50f,800f),
                          new floatPoint2D(450f, 405f )
                 }),
                 RegionOp.Operation.UNION) , 
                 SS );
     }else if( S.equals("d"))
        ivc.disableSelectionEditor();
     else if(S.equals( "SC" ))
        System.out.println( ivc.getColor( SS ));
     else if( S.equals( "do" ))
        ivc.disableOverlay( true );
     else if( S.equals( "eo" ))
        ivc.disableOverlay( false );
     
     
  }
}
}
