package OverplotView;

/**
 * $Id$
 * 
 * top level SelectedGraph class.  this class handles layout and events.
 * provides a mechanism for selecting and viewing portions of a DataSet using 
 * stacked or overplotted graphs.
 *
 * @see DataSetTools.dataset.DataSet
 * @see DataSetTools.viewer.DataSetViewer\
 *
 * $Log$
 * Revision 1.3  2000/07/12 14:58:39  neffk
 * added a makefile for generating documentation
 *
 * Revision 1.2  2000/07/07 21:57:13  neffk
 * changed the aspect ratio of the graph to 1:2 (vertical:horizontal)
 *
 * Revision 1.1.1.1  2000/07/06 16:17:44  neffk
 * imported source code
 *
 * Revision 1.13  2000/06/26 15:03:16  neffk
 * doesn't crash when the setDataSet method is called (178-0)
 *
 * Revision 1.12  2000/06/20 20:51:17  neffk
 * 1) SELECTED GRAPH VIEW DISTRO #1
 * 2) updated some import statements
 *
 * Revision 1.11  2000/06/19 14:35:43  neffk
 * 1) moved to a package seperate from DataSetTools & updated imports
 * 2) changed GraphManager container to GraphableDataManager
 * 3) added "Toggle Controls" to menu bar & associated action
 *
 * Revision 1.10  2000/06/01 18:53:20  neffk
 * added a newline to the end of a log message
 *
 * Revision 1.9  2000/04/30 20:21:15  psam
 * added a more appropriate constructor to initialize units and labels
 *
 * Revision 1.8  2000/04/24 18:36:11  psam
 * works 1.0
 *
 *
 */

import OverplotView.components.containers.*;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;


public class SelectedGraphView 
  extends DataSetViewer
  implements IObserver

{                         


  public SelectedGraphView( DataSet data_set ) 
  {
    super(data_set);     // Records the data_set in the parent class and
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

    JMenuItem exitButton = new JMenuItem( "Auxiliary Exit" );
    exitButton.addActionListener( option_menu_handler );
    option_menu.add( exitButton );

    addComponentListener(  new ViewComponentAdapter()  );

    data_set.addIObserver( this );  
  }



  /**
   * This will be called by the "outside world" if the contents of the
   * DataSet are changed and it is necesary to redraw the graphs using the
   * current DataSet.  one example of such a situationn is a change in 
   * selection.
   */
  public void redraw( String reason )
  {
    System.out.println( "DataSetViewer> " + reason );

    if ( reason == IObserver.DESTROY )
    {
       System.out.println("Reason: " + reason );
    }
    else if( reason == IObserver.DATA_REORDERED)
    {
    }
    else if( reason == IObserver.DATA_DELETED )
    {
    }
    else if( reason == IObserver.SELECTION_CHANGED )
    {
//      System.out.println( "SelectedGraphView::redraw(String)" );

      //clear list of spectra to be graphed
      controlsCP.clearGraphList();

      //add all selected spectra to list
      for( int i=0; i<getDataSet().getNum_entries(); i++ )
      {
        Data data = getDataSet().getData_entry(i);
        if(  data.isSelected()  )
        {
          Integer group = new Integer(  data.getGroup_ID()  );
          AttributeList alist = data.getAttributeList();
          String id_str = alist.getAttributeValue(
                            DataSetTools.dataset.Attribute.RUN_NUM ) +
                            "::Group #" + group.toString();
                            
          controlsCP.addToGraphList(  new GraphableData( getDataSet(), 
                                    id_str )  );
          System.out.println( "added from SGV: " + id_str );
        }
      }
    }
    else if( reason == IObserver.POINTED_AT_CHANGED )
    {
//      System.out.println( "POINTED_AT_CHANGED" );
    }
    else if( reason == IObserver.GROUPS_CHANGED )
    {
    }
  }



  /**
   * removes children, adds a layout, adds graph, adds controls, and
   * forces items to be laid out & redrawn.
   */
  public void redraw()
  {
    removeAll();

    setLayout(  new GridLayout( 1, 1 )  );

    add( splitPane = new JSplitPane(  JSplitPane.VERTICAL_SPLIT,
                                      graphPanel, 
                                      controlsCP )  );
    splitPane.setOneTouchExpandable( true );
    splitPane.setDividerLocation( 250 );
//    splitPane.addComponentListener(  new SplitPaneComponentAdapter()  );

    //set minimum allowable size for each side of the split pane
    Dimension minimumSize = new Dimension( 150, 150 );
    graphPanel.setMinimumSize( minimumSize );
    controlsCP.setMinimumSize( minimumSize );

    invalidate();
    revalidate();
    manager.redraw();
  }



  /**
   * This will be called by the "outside world" if the viewer is to replace 
   * its reference to a DataSet by a reference to a new DataSet, ds, and
   * rebuild the entire display, titles, borders, etc.
   */
  public void setDataSet( DataSet ds )
  {   
    this.setVisible( false );
    super.setDataSet( ds );
    this.removeAll(); 
    this.init();
    this.redraw();
    this.setVisible( true );
  }




  private void init()
  {
    graphPanel = new JPanel();
    graphPanel.addComponentListener(  new GraphSizeComponentAdapter()  );

    graph = new sgtSelectedGraph( graphPanel );
//    graph.addComponentListener(  new GraphSizeComponentAdapter()  );

    manager = new GraphableDataManager( graph );

    controlsCP = new ControlPanel( this.getDataSet(), manager );
    controlsCP.addComponentListener(  new ControlSizeComponentAdapter()  );
    controlsCP.init();

    graphPanel = new JPanel();
    graph.setGraphPanel( graphPanel ); 

    redraw();
  }


 

  /**
   * listens for all mouse events and prints out appropriate debugging data to
   * the console.
   */
  class SelectedGraphViewMouseImputAdapter 
    extends MouseInputAdapter
  {
    public void mouseClicked( MouseEvent e )
    {
      System.out.println(  "Mouse Click: " + e.getPoint()  );
    }


    public void mouseDragged( MouseEvent e )
    {
      System.out.println("Mouse Dragged: " + e.getPoint() );
    }

  }



  /**
   * Listen for resize events that originate from this object
   */
  class ViewComponentAdapter 
    extends ComponentAdapter
  {
    public void componentResized( ComponentEvent c )
    {
      //System.out.println("View Area resized: " + c.getComponent().getSize() );
//      c.getLeftComponent().calculateGraphSize();
    }
  }



  /**
   * Listen for resize events that originate from the Split pane in this
   * object.
   */
  class GraphSizeComponentAdapter 
    extends ComponentAdapter
  {
    public void componentResized( ComponentEvent c )
    {
      System.out.println( "graphPanel resized" );
      System.out.println( c.getComponent().getSize() );
    }
  }



  class ControlSizeComponentAdapter 
    extends ComponentAdapter
  {
    public void componentResized( ComponentEvent c )
    {
//      System.out.println( "control size changed" );
//      System.out.println( "graph Panel size: " + graphPanel.getSize() );

      Dimension mainD = getSize();
      Dimension controlD = c.getComponent().getSize();
      int height = mainD.height - controlD.height;
      int width = controlD.width;

      Dimension graphD = new Dimension( width, height );

      //System.out.println( "controls resized: " + c.getComponent().getSize() );
      //System.out.println( "graph size: " + graphD  );
      //graph.calculateGraphSize( graphD );
    }
  }



  /**
   *  Listen for Option menu selections and just print out the selected option.
   *  It may be most convenient to have a separate listener for each menu.
   */
  private class OptionMenuHandler 
    implements ActionListener 
  {
    public void actionPerformed( ActionEvent e ) 
    {
      String action = e.getActionCommand();
      System.out.println("The user selected : " + action );
      if( action == "Auxiliary Exit" ) 
        System.exit( 1 );
    }
  }




  public void update( Object observed, Object reason )
  {
  }



  /**
   * listens to the auxiliary redraw button
   */
  private class redrawListener 
    implements ActionListener 
  {
    public void actionPerformed( ActionEvent e ) 
    {
      //manager.redraw();
    }
  }


  public static void main(String[] args)
  {
    DataSet   data_set   = new DataSet("Sample DataSet\n", "Sample log-info\n");
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

    SelectedGraphView view = new SelectedGraphView( data_set );
    JFrame f = new JFrame("Test for SelectedGraphView");
    f.setBounds(0,0,600,400);
    f.setJMenuBar( view.getMenuBar() );


    f.getContentPane().add( view );
    f.setVisible( true );
  }
  
/*-------------------------------private data---------------------------------*/

  private sgtSelectedGraph graph;
  private GraphableDataManager manager = null; 
  private JPanel graphPanel;
  private JButton redrawJB;
  ControlPanel controlsCP = null;
  String newline = "\n";
//  boolean displayGraphandControls = true;
  private JSplitPane splitPane;
}



