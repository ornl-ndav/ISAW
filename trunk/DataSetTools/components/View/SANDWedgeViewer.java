/*
 * File:  SANDWedgeViewer.java
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/12/20 03:55:43  millermi
 * - Fixed javadocs error.
 *
 * Revision 1.1  2003/12/19 02:20:09  millermi
 * - Initial Version - Adds specialized functionality to the
 *   IVCTester class. This class allows users to specifically
 *   view SAND data files.
 * - Currently, the FunctionViewComponent is unavailable.
 *   Once this is added, calculations on selected regions will
 *   be possible.
 *
 */

package DataSetTools.components.View;

import javax.swing.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.Serializable;
import java.io.IOException;
import java.io.EOFException;

import DataSetTools.components.View.TwoD.ImageViewComponent;
import DataSetTools.components.View.OneD.FunctionViewComponent;
import DataSetTools.components.View.Menu.MenuItemMaker;
import DataSetTools.components.View.Menu.ViewMenuItem;
import DataSetTools.components.image.*;
import DataSetTools.components.containers.SplitPaneWithState;
import DataSetTools.components.View.Transparency.SelectionOverlay;
import DataSetTools.components.View.Region.Region;
import DataSetTools.components.View.ViewControls.PanViewControl;
import DataSetTools.util.TextFileReader;
import DataSetTools.util.RobustFileFilter;

/**
 * Simple class to display an image, specified by an IVirtualArray2D or a 
 * 2D array of floats, in a frame. This class adds further implementation to
 * the ImageFrame2.java class for thorough testing of the ImageViewComponent.
 */
public class SANDWedgeViewer extends JFrame implements IPreserveState,
                                                       Serializable
{
 /**
  * "ImageViewComponent" - This constant String is a key for referencing
  * the state information about the ImageViewComponent. Since the
  * ImageViewComponent has its own state, this value is of type ObjectState,
  * and contains the state of the ImageViewComponent. 
  */
  public static final String IMAGE_VIEW_COMPONENT    = "ImageViewComponent";
  
 /**
  * "FunctionViewComponent" - This constant String is a key for referencing
  * the state information about the FunctionViewComponent. Since the
  * FunctionViewComponent has its own state, this value is of type
  * ObjectState, and contains the state of the FunctionViewComponent. 
  */
  public static final String FUNCTION_VIEW_COMPONENT = "FunctionViewComponent";
  
 /**
  * "ViewerSize" - This constant String is a key for referencing
  * the state information about the size of the viewer at the time
  * the state was saved. The value this key references is of type
  * Dimension.
  */
  public static final String VIEWER_SIZE           = "ViewerSize"; 
  
 /**
  * "DataDirectory" - This constant String is a key for referencing
  * the state information about the location of the data files being
  * loaded by this viewer. The value this key references is of type
  * String.
  */
  public static final String DATA_DIRECTORY        = "DataDirectory";
  
  // complete viewer, includes controls and ijp
  private transient SplitPaneWithState pane;
  private transient ImageViewComponent ivc;
  private transient FunctionViewComponent fvc;
  private transient IVirtualArray2D data;
  private transient JMenuBar menu_bar;
  private String projectsDirectory = System.getProperty("user.home");

 /**
  * Construct a frame with no data to start with. This constructor will be
  * used when data is being loaded in.
  */
  public SANDWedgeViewer()
  {
    init(null);
  }

 /**
  * Construct a frame with the specified image and title
  *  
  *  @param  iva
  */
  public SANDWedgeViewer( IVirtualArray2D iva )
  {
    init(iva);
  }

 /**
  * Construct a frame with the specified image and title
  *  
  *  @param  array
  *  @param  xinfo
  *  @param  yinfo
  *  @param  title
  */  
  public SANDWedgeViewer( float[][] array, 
                          AxisInfo xinfo,
		          AxisInfo yinfo,
		          String title )
  {
    VirtualArray2D temp = new VirtualArray2D( array );
    temp.setAxisInfo( AxisInfo.X_AXIS, xinfo.copy() );
    temp.setAxisInfo( AxisInfo.Y_AXIS, yinfo.copy() );
    temp.setTitle(title);
    
    init(temp);
  }
  
  public void setObjectState( ObjectState new_state )
  {
    boolean redraw = false;  // if any values are changed, repaint overlay.
    Object temp = new_state.get(IMAGE_VIEW_COMPONENT);
    if( temp != null )
    {
      ivc.setObjectState( (ObjectState)temp );
      redraw = true;  
    } 
    
    temp = new_state.get(FUNCTION_VIEW_COMPONENT);
    if( temp != null )
    {
      //fvc.setObjectState( (ObjectState)temp );
      redraw = true;  
    } 
    
    temp = new_state.get(VIEWER_SIZE); 
    if( temp != null )
    {
      setSize( (Dimension)temp );
      redraw = true;  
    } 
    
    temp = new_state.get(DATA_DIRECTORY); 
    if( temp != null )
    {
      projectsDirectory = (String)temp;
    } 
    
    if( redraw )
      repaint();
  }
  
  public ObjectState getObjectState()
  {
    ObjectState state = new ObjectState();
    state.insert( IMAGE_VIEW_COMPONENT, ivc.getObjectState() );
    //state.insert( FUNCTION_VIEW_COMPONENT, fvc.getObjectState() );
    state.insert( VIEWER_SIZE, getSize() );
    state.insert( DATA_DIRECTORY, new String(projectsDirectory) );
    
    return state;
  }
  
  public void loadData( String filename )
  {
    int NUM_ROWS = 200;
    int NUM_COLS = 200;
    float[][] array = new float[NUM_COLS][NUM_ROWS];
    float qxmin = 0;
    float qymin = 0;
    float qxmax = 0;
    float qymax = 0;
    // try to open the file
    try
    {
      int row = 0;
      int col = 0;
      // file arranged in 4 columns: Qx, Qy, Value, Error
      // Since we only care about the min and max of Qx, Qy, only the first
      // and last values in those columns are saved. The Value column is all
      // stored in the array, the Error column is ignored.
      TextFileReader reader = new TextFileReader( filename );
      // read in first line, this will set the Qx/Qy min and read in
      // the first value.
      StringTokenizer datarow = new StringTokenizer(reader.read_line());
      qxmin = (new Float( datarow.nextToken() )).floatValue();
      qymin = (new Float( datarow.nextToken() )).floatValue();
      array[col][row] = (new Float( datarow.nextToken() )).floatValue();
      col++;  // increment row since first element was read in.
      // let the exception EOF end the loop.
      while( true )
      {
	// now read in the data one row at a time, each row contains a
	// Qx, Qy, Value, and Error value. Store the Values in the array
	// by column instead of row.
        datarow = new StringTokenizer(reader.read_line());
        qxmax = (new Float( datarow.nextToken() )).floatValue();
        qymax = (new Float( datarow.nextToken() )).floatValue();
        array[col][row] = (new Float( datarow.nextToken() )).floatValue();
	//System.out.println("Row/Col: (" + row + "," + col + ")" );
	
	// increment column if at last row, reset row to start.
	// this will cause the numbers to be read in by column
	if( col == NUM_COLS - 1 )
        {
          col = 0;
	  row++;
        }
        else
          col++;
      } // end while
    }
    catch( IOException e1 )
    {
      if( e1.getMessage().equals("End of file") )
      {
        // done reading file
        VirtualArray2D va2D = new VirtualArray2D( array );
        va2D.setAxisInfo( AxisInfo.X_AXIS, qxmin, qxmax, 
    		            "Qx","X Units", true );
        va2D.setAxisInfo( AxisInfo.Y_AXIS, qymin, qymax, 
    			    "Qy","Y Units", true );
        va2D.setTitle("SAND Wedge Viewer");
	setData( va2D );
      }
      else
      {
        // no file to be read, display file not found on empty jpanel.
        VirtualArray2D nullarray = null;
        this.setData(nullarray);
        ((JComponent)pane.getLeftComponent()).add( 
	                                       new JLabel("File Not Found") );
        validate();
        repaint();
      }
    }
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
    if( values != null && data != null &&
        ( values.getNumRows() == data.getNumRows() &&
          values.getNumColumns() == data.getNumColumns() ) )
    {  
      data = values;
      ivc.dataChanged(data);
    }  
    // if different sized array, remove everything and build again.
    else
    { 
      data = values;
      getContentPane().removeAll();
      buildMenubar();
      buildPane();
      getContentPane().add(pane);
      validate();
      repaint();
    }
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
  
  public void setDataDirectory( String path )
  {
    projectsDirectory = path;
  }
  
 /*
  * The init function gathers the common functionality between the constructors
  * so that the code does not have to exist in 3 spots. This will build the
  * niceties of the viewer.
  */ 
  private void init( IVirtualArray2D iva )
  {
    setTitle("SAND Wedge Viewer");
    data = new VirtualArray2D(1,1);
    buildMenubar();
    
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(0,0,700,700);
    
    setData(iva);
  }

 /*
  * This method builds the content pane of the frame.
  */
  private void buildPane()
  { 
    if( data != null )
    {
      ivc = new ImageViewComponent( data );
      //fvc = new FunctionViewComponent( new VirtualArray1D( {0} ) );
      ivc.setColorControlEast(true);
      ivc.addActionListener( new WVListener() );
      ivc.addActionListener( new ImageListener() );
      Box controls = new Box(BoxLayout.Y_AXIS);
      JComponent[] ctrl = ivc.getSharedControls();
      Dimension preferred_size;
      for( int i = 0; i < ctrl.length; i++ )
      {
        controls.add(ctrl[i]);
      }
      JPanel spacer = new JPanel();
      spacer.setPreferredSize( new Dimension(0, 10000) );
      controls.add(spacer);
    
      Box componentholder = new Box(BoxLayout.Y_AXIS);
      JPanel fvcspacer = new JPanel();
      fvcspacer.setPreferredSize( new Dimension(0, 270) );
      componentholder.add( ivc.getDisplayPanel() );
      componentholder.add(fvcspacer);
      pane = new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
                                    componentholder,
	  		            controls, .75f );
      // get menu items from view component and place it in a menu
      ViewMenuItem[] menus = ivc.getSharedMenuItems();
      for( int i = 0; i < menus.length; i++ )
      {
        if( ViewMenuItem.PUT_IN_FILE.toLowerCase().equals(
    	    menus[i].getPath().toLowerCase()) )
    	{
	  menu_bar.getMenu(0).add( menus[i].getItem() ); 
        }
	else if( ViewMenuItem.PUT_IN_OPTIONS.toLowerCase().equals(
    	         menus[i].getPath().toLowerCase()) )
    	{
	  menu_bar.getMenu(1).add( menus[i].getItem() );	   
        }
	else if( ViewMenuItem.PUT_IN_HELP.toLowerCase().equals(
    	         menus[i].getPath().toLowerCase()) )
        {
	  menu_bar.getMenu(2).add( menus[i].getItem() );
        }
      }
    }
    else
    {
      pane = new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
                                    new JPanel(), new JPanel(), .75f );
    }	   
  }
 
 /*
  * This private method will (re)build the menubar. This is necessary since
  * the ImageViewComponent or FunctionViewComponent could add menu items to
  * the Menubar. If the file being loaded is not found, those menu items
  * must be removed. To do so, rebuild the Menubar.
  */ 
  private void buildMenubar()
  {    
    setJMenuBar(null);
    menu_bar = new JMenuBar();
    setJMenuBar(menu_bar);
    
    Vector file = new Vector();
    Vector options = new Vector();
    Vector new_menu = new Vector();
    Vector save_menu = new Vector();
    Vector load_menu = new Vector();
    Vector load_data = new Vector();
    Vector file_listeners = new Vector();
    Vector option_listeners = new Vector();
    file_listeners.add( new ImageListener() );
    file_listeners.add( new ImageListener() );
    file.add("File");
    file.add(load_data);
      load_data.add("Load Data");
    
    option_listeners.add( new ImageListener() );
    option_listeners.add( new ImageListener() );
    option_listeners.add( new ImageListener() );
    options.add("Options");
    options.add(save_menu);
      save_menu.add("Save State");
    options.add(load_menu);
      load_menu.add("Load State");
    
    menu_bar.add( MenuItemMaker.makeMenuItem(file,file_listeners) ); 
    menu_bar.add( MenuItemMaker.makeMenuItem(options,option_listeners) );
    // since the IVC and FVC are not created unless data is available,
    // do not load state unless data is available.
    if( data == null )
    {
      JMenu option_menu = menu_bar.getMenu(1);
      option_menu.getItem(0).setEnabled(false);
      option_menu.getItem(1).setEnabled(false);
    }
    menu_bar.add(new JMenu("Help"));
  }
  
 /*
  * This class is required to update the axes when the divider is moved. 
  * Without it, the image is one frame behind.
  */
  private class ImageListener implements ActionListener
  {
    public void actionPerformed( ActionEvent ae )
    {
      if( ae.getActionCommand().equals("Load Data") )
      {
        JFileChooser fc = new JFileChooser(projectsDirectory);
        fc.setFileFilter( new DataFileFilter() );
        int result = fc.showDialog(new JFrame(),"Load Data");
     
        if( result == JFileChooser.APPROVE_OPTION )
        {
          String filename = fc.getSelectedFile().toString();
	  projectsDirectory = fc.getCurrentDirectory().toString();
          loadData(filename);
        }
      } // end else if load data
      else if( ae.getActionCommand().equals("Save State") )
      {
        //state = ivc.getObjectState();
	getObjectState().openFileChooser(true);
      }
      else if( ae.getActionCommand().equals("Load State") )
      {
        ObjectState state = new ObjectState();
        //state = ivc.getObjectState();
	if( state.openFileChooser(false) )
	  setObjectState(state);
      }
    }
  }
  
 /*
  * This class listeners for selections made by the selection overlay
  */ 
  private class WVListener implements ActionListener
  {
    public void actionPerformed( ActionEvent ae )
    {
      String message = ae.getActionCommand();
      if( message.equals(SelectionOverlay.REGION_ADDED) )
      {
  	Region[] selectedregions = ivc.getSelectedRegions();
        Point[] selectedpoints = 
	          selectedregions[selectedregions.length-1].getSelectedPoints();
        //System.out.println("NumSelectedPoints: " + selectedpoints.length);
        for( int j = 0; j < selectedpoints.length; j++ )
        {
	  int row = selectedpoints[j].y;
	  int col = selectedpoints[j].x;
	  
	  //data.setDataValue( row, col, data.getDataValue(row,col) * 2f );
          //System.out.println("(" + selectedpoints[j].x + "," + 
          //      	     selectedpoints[j].y + ")" );
        }
        //ivc.dataChanged(data);
      }
    }
  }
 
 /*
  * File filter for .dat files being loaded for data analysis.
  */ 
  private class DataFileFilter extends RobustFileFilter
  {
   /*
    *  Default constructor.  Calls the super constructor,
    *  sets the description, and sets the file extensions.
    */
    public DataFileFilter()
    {
      super();
      super.setDescription("Data File (*.dat)");
      super.addExtension(".dat");
    } 
  }
  
  
 /*
  * Main program. To set a consistent projectsDirectory
  */
  public static void main( String args[] )
  {
    SANDWedgeViewer load = new SANDWedgeViewer();
    load.setVisible(true);
  }

}
