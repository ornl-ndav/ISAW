package DataSetTools.trial;

import gov.anl.ipns.ViewTools.Components.AxisInfo;
import gov.anl.ipns.ViewTools.Components.IVirtualArray2D;
import gov.anl.ipns.ViewTools.Components.VirtualArray2D;
import gov.anl.ipns.ViewTools.Components.Menu.ViewMenuItem;
import gov.anl.ipns.ViewTools.Components.TwoD.ImageViewComponent;
import gov.anl.ipns.ViewTools.Components.ViewControls.ViewControl;
import gov.anl.ipns.ViewTools.UI.SplitPaneWithState;

import javax.swing.*;
import java.awt.Dimension;

import java.util.*;

/**
 * Simple class to display an image, specified by an IVirtualArray2D or a 
 * 2D array of floats, in a frame.
 */
public class ImageFrame3 extends JFrame
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
  public ImageFrame3( IVirtualArray2D iva )
  {
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    data = new VirtualArray2D(1,1);
    menu_bar = new JMenuBar();
    setJMenuBar(menu_bar);   
    menu_bar.add(new JMenu("File")); 
    menu_bar.add(new JMenu("Options"));
    
    //setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    setBounds(0,0,700,800);
    
    setData(iva);
    setVisible(true);
  }
  
  public void dispose()
  {
    super.dispose();
    if( ivc != null)
      ivc.closeWindows();
  }

 /**
  * Construct a frame with the specified image and title
  *  
  *  @param  array
  *  @param  xinfo
  *  @param  yinfo
  *  @param  title
  */  
  public ImageFrame3( float[][] array, 
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
    ivc.preserveAspectRatio(true);
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

 /*
  * Testing purposes only
  */
  public static void main( String args[] )
  {
    final int NUM_ROWS = 300;
    final int NUM_COLS = 200;
    Random generator = new Random();

    float test_array[][] = new float[NUM_ROWS][NUM_COLS];
    for ( int i = 0; i < NUM_ROWS; i++ )
      for ( int j = 0; j < NUM_COLS; j++ )
        test_array[i][j] = i + j + (NUM_ROWS+NUM_COLS)*generator.nextFloat();
    IVirtualArray2D va2D = new VirtualArray2D( test_array );
    va2D.setAxisInfo( AxisInfo.X_AXIS, 0f, 2000f, 
    		        "TestX","TestUnits", AxisInfo.LINEAR );
    va2D.setAxisInfo( AxisInfo.Y_AXIS, 0f, 3000f, 
    			"TestY","TestYUnits", AxisInfo.LINEAR );
    va2D.setTitle("ImageFrame Test");
    new ImageFrame3( va2D );
  
  }

}