/**
 * File:  ViewerTemplate.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2001/04/26 14:31:32  dennis
 * Added copyright and GPL info at the start of the file.
 *
 *
 */

package DataSetTools.viewer.ViewerTemplate;

import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.components.image.*;
import DataSetTools.viewer.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * Provides a mechanism for selecting and viewing portions of a Data Set using 
 * stacked or overplotted graphs.
 *
 * @see DataSetTools.dataset.DataSet
 * @see DataSetTools.viewer.DataSetViewer
 *
 */

public class ViewerTemplate extends DataSetViewer
{                         
  private GraphJPanel graph_panel   = null; 
  private JPanel      control_panel = null; 

/* --------------------------------------------------------------------------
 *
 * CONSTRUCTORS
 */

/* ------------------------------------------------------------------------ */

public ViewerTemplate( DataSet data_set, ViewerState state ) 
{
  super(data_set, state);  // Records the data_set and current ViewerState
                           // object in the parent class and then
                           // sets up the menu bar with items handled by the
                           // parent class.
  init();
                                        // Add an item to the Option menu and
                                        // add a listener for the option menu
                                        // If the menu options are dependent
                                        // on the DataSet, they must be added
                                        // in init.  Unfortunately, in that 
                                        // case, the old versions would have
                                        // to be removed before adding the 
                                        // new ones.
  OptionMenuHandler option_menu_handler = new OptionMenuHandler();
  JMenu option_menu = menu_bar.getMenu( OPTION_MENU_ID );

  JMenuItem button = new JMenuItem( "My new option" );
  button.addActionListener( option_menu_handler );
  option_menu.add( button );
}

/* -----------------------------------------------------------------------
 *
 *  PUBLIC METHODS
 *
 */


public void redraw( String reason )
{
    // This will be called by the "outside world" if the contents of the
    // DataSet are changed and it is necesary to redraw the graphs using the
    // current DataSet.

  int  num_data_blocks = getDataSet().getNum_entries();

                                  // just show up to five DataBlocks for now 
  Color colors[] = new Color[5];
  colors[0] = Color.black;
  colors[1] = Color.red;
  colors[2] = Color.green;
  colors[3] = Color.blue;
  colors[4] = Color.yellow;

  for ( int i = 0; i < 5; i++ )     
  {
    Data data_block = getDataSet().getData_entry( i );
    if ( data_block != null )
    {
      float x[] = data_block.getX_scale().getXs();
      float y[] = data_block.getY_values();

      graph_panel.setData( x, y, i, false );
      graph_panel.setColor( colors[i], i, false );
    }
    graph_panel.repaint();
  }

}


public void setDataSet( DataSet ds )
{   
  // This will be called by the "outside world" if the viewer is to replace 
  // its reference to a DataSet by a reference to a new DataSet, ds, and
  // rebuild the entire display, titles, borders, etc.

  setVisible( false );
  super.setDataSet( ds );
  init();
  redraw( NEW_DATA_SET );
  setVisible( true );
}


/* ----------------------------- main ------------------------------------ */
/*
 *  For testing purposes only
 */
public static void main(String[] args)
{
  DataSet   data_set   = new DataSet("Sample DataSet", "Sample log-info");
  data_set.setX_units( "Test X Units" );
  data_set.setX_label("Text X Label" );
  data_set.setY_units( "Test Y Units" );
  data_set.setY_label("Text Y Label" );

  Data          spectrum;     // data block that will hold a "spectrum"
  float[]       y_values;     // array to hold the "counts" for the spectrum
  UniformXScale x_scale;      // "time channels" for the spectrum

  for ( int id = 1; id < 10; id++ )            // for each id
  {
    x_scale = new UniformXScale( 1, 5, 50 );   // build list of time channels

    y_values = new float[50];                       // build list of counts
    for ( int channel = 0; channel < 50; channel++ )
      y_values[ channel ] = (float)Math.sin( id * channel / 10.0 );

    spectrum = new Data( x_scale, y_values, id );   // put it into a "Data"
                                                    // object and then add
    data_set.addData_entry( spectrum );             // that data object to
                                                    // the data set
  }

  ViewerTemplate view = new ViewerTemplate( data_set, null );
  JFrame f = new JFrame("Test for ViewerTemplate");
  f.setBounds(0,0,600,400);
  f.setJMenuBar( view.getMenuBar() );
  f.getContentPane().add( view );
  f.setVisible( true );
}
   

/* -----------------------------------------------------------------------
 *
 * PRIVATE METHODS
 *
 */ 


private void init()
{
  if ( graph_panel != null )          // get rid of old components first 
  {
    graph_panel.removeAll();
    control_panel.removeAll();
    removeAll();
  }
  graph_panel   = new GraphJPanel();
  control_panel = new JPanel();
                                        // make a titled border around the
                                        // whole viewer, using an appropriate
                                        // title from the DataSet. 
  String title = getDataSet().toString();
  AttributeList attr_list = getDataSet().getAttributeList();
  Attribute     attr      = attr_list.getAttribute(Attribute.RUN_TITLE);
  if ( attr != null )
   title = attr.getStringValue();

  TitledBorder border = new TitledBorder(
                                    LineBorder.createBlackLineBorder(), title);
  border.setTitleFont( FontUtil.BORDER_FONT );
  setBorder( border );
            
                                     // Place the graph area inside of a
                                     // JPanel and make a titled border around
                                     // the JPanel graph area using the last 
                                     // message, if available
  JPanel graph_container = new JPanel();
  OperationLog op_log = getDataSet().getOp_log();
  if ( op_log.numEntries() <= 0 )
    title = "Graph Display Area";
  else
    title = op_log.getEntryAt( op_log.numEntries() - 1 );

  border = new TitledBorder( LineBorder.createBlackLineBorder(), title );
  border.setTitleFont( FontUtil.BORDER_FONT );
  graph_container.setBorder( border );
  graph_container.setLayout( new GridLayout(1,1) );
  graph_container.add( graph_panel );
  


                                        // make a titled border around the
                                        // control area
  border = new TitledBorder( LineBorder.createBlackLineBorder(), "Controls" );
  border.setTitleFont( FontUtil.BORDER_FONT );
  control_panel.setBorder( border );

                                        // Add the control area and graph
                                        // container to the main viewer
                                        // panel and draw.
  setLayout( new GridLayout(2,1) );
  add ( graph_container );
  add ( control_panel ); 

  redraw( NEW_DATA_SET );

  graph_panel.addMouseMotionListener( new ViewMouseMotionAdapter() );
  graph_panel.addComponentListener( new ViewComponentAdapter() );
}


/* -------------------------------------------------------------------------
 *
 *  INTERNAL CLASSES
 *
 */

/**
 *  Listen for mouse motion events and just print out the pixel coordinates
 *  to demonstrate how to handle such events.
 */
class ViewMouseMotionAdapter extends MouseMotionAdapter
{
   public void mouseDragged( MouseEvent e )
   {
     System.out.println("Mouse moved at: " + e.getPoint() );
   }
}

/**
 *  Listen for resize events on the and just print out the new size to
 *  to demonstrate how to handle such events.
 */
class ViewComponentAdapter extends ComponentAdapter
{
  public void componentResized( ComponentEvent c )
  {
    System.out.println("View Area resized: " + c.getComponent().getSize() );
  }
}

/**
 *  Listen for Option menu selections and just print out the selected option.
 *  It may be most convenient to have a separate listener for each menu.
 */
private class OptionMenuHandler implements ActionListener
{
  public void actionPerformed( ActionEvent e )
  {
    String action = e.getActionCommand();
    System.out.println("The user selected : " + action );
  }
}

}
