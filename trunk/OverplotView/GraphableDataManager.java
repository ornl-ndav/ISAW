package OverplotView;

/**
 * $Id$
 * ----------
 *
 * GraphableDataManager is the top level component for the SelectedGraphView.
 * it interacts with the ViewManager, acts as a container for the data to 
 * be graphed, and manages the the graphical representation of its data.
 * ----------
 *
 * $Log$
 * Revision 1.3  2001/06/28 22:05:04  neffk
 * data is converted to GraphableData on redraw() instead of an explicit call.
 * also, the conversion function creates (subclasses of) Attributes to store
 * things like offset and colors.  the constructor now sets units and labels in
 * the graph using Attributes.
 *
 * Revision 1.2  2001/06/27 16:50:10  neffk
 * this class was formerly implementing IObserver and extending DataSetViewer,
 * which forced redraw(...) and update(...).  these two functions are
 * redundant, as well as the IObserver interface.  this class not longer
 * implements IObserver, and update(...) has been removed.
 *
 * ----------
 */

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.ColorAttribute;
import DataSetTools.dataset.FloatAttribute;
import DataSetTools.dataset.StringAttribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.viewer.DataSetViewer;
import DataSetTools.util.IObserver;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import OverplotView.GraphableData;
import OverplotView.IGraphableDataGraph;
import OverplotView.graphics.sgtGraphableDataGraph;

public class GraphableDataManager 
  extends DataSetViewer
{

  public static final String AUX_EXIT = "Auxiliry Exit";

  private Vector                graphable_data;
  private sgtGraphableDataGraph graph;


  /**
   * default constructor
   */
  public GraphableDataManager( DataSet data_set )
  {
    super(data_set);
    graph = new sgtGraphableDataGraph();
    StringAttribute title, subtitle1, subtitle2, 
                    x_units, y_units, x_label, y_label;
    title = new StringAttribute( IGraphableDataGraph.TITLE,
                                 data_set.getTitle()  );
    subtitle1 = new StringAttribute( IGraphableDataGraph.TITLE_SUB1,
                                     "" );
    subtitle2 = new StringAttribute( IGraphableDataGraph.TITLE_SUB2,
                                     "" );
    x_units = new StringAttribute(  IGraphableDataGraph.X_UNITS,
                                    data_set.getX_units()  );
    y_units = new StringAttribute(  IGraphableDataGraph.Y_UNITS,
                                    data_set.getY_units()  );
    x_label = new StringAttribute(  IGraphableDataGraph.X_LABEL,
                                    data_set.getX_label()  );
    y_label = new StringAttribute(  IGraphableDataGraph.Y_LABEL,
                                    data_set.getY_label()  );
    AttributeList attrs = new AttributeList();
    attrs.addAttribute( title );
    attrs.addAttribute( subtitle1 );
    attrs.addAttribute( subtitle2 );
    attrs.addAttribute( x_units );
    attrs.addAttribute( y_units );
    attrs.addAttribute( x_label );
    attrs.addAttribute( y_label );
    graph.setAttributeList( attrs );

    redraw( IObserver.DATA_CHANGED );

    //modify the menu provided by DataSetViewer
    OptionMenuHandler option_menu_handler = new OptionMenuHandler();
    JMenu option_menu = menu_bar.getMenu( OPTION_MENU_ID );
    JMenuItem exitButton = new JMenuItem( AUX_EXIT );
    exitButton.addActionListener( option_menu_handler );
    option_menu.add( exitButton );
  }

  
  /**
   * This will be called by the "outside world" if the contents of the
   * DataSet are changed and it is necesary to redraw the graphs using the
   * current DataSet.  one example of such a situationn is a change in 
   * selection.
   */
  public void redraw( String reason ) 
  {
    //System.out.println( "DataSetViewer> " + reason );

    if ( reason == IObserver.DESTROY )
    {
      graphable_data = null;
      graph = null;  
    }
    else if( reason == IObserver.DATA_REORDERED)
    {
    }
    else if( reason == IObserver.DATA_DELETED )
    {
    }
    else if( reason == IObserver.SELECTION_CHANGED )
    {
      redraw();
    }
    else if( reason == IObserver.POINTED_AT_CHANGED )
    {
    }
    else if( reason == IObserver.GROUPS_CHANGED )
    {
    }
    else if( reason == IObserver.DATA_CHANGED )
    {
      redraw();
    }
    else if( reason == IObserver.ATTRIBUTE_CHANGED )
    {
    }
    else if( reason == IObserver.FIELD_CHANGED )
    {
    }
    else if( reason == IObserver.HIDDEN_CHANGED )
    {
    }
    else
      redraw();                         //default is to redraw the entire
                                        //viewer so that future expansions
                                        //in the variety of messages
                                        //will not break existing code.
                                        //however, if there are
                                        //effecient ways to update the viewer,
                                        //please maintain this code to catch
                                        //to deal with the update appropriately.
  }


  /**
   * updates the graphic visualization object, whatever that might be.
   * first, all currently selected data is added converted from Data 
   * objects to GraphableData objects
   */
  public void redraw()
  {
                                       //convert data from Data objects
                                       //to GraphableData objects
    convert_Data_to_GraphableData();

                                       //pass the data down to the
                                       //graphic component that's handeling
                                       //the actual graphing of the data
    graph.init( graphable_data );

                                       //replace the previous graph with
                                       //the new graph that redraw() 
                                       //generates
    removeAll();
    add(  graph.redraw()  );

                                       //ask swing to redraw our new
                                       //additions to the DataSetViewer
    validate();
    //setVisable( true );
  }


  /**
   * This will be called by the "outside world" if the viewer is to replace 
   * its reference to a DataSet by a reference to a new DataSet, ds, and
   * rebuild the entire display, titles, borders, etc.
   */
  public void setDataSet( DataSet ds )
  {   
    super.setDataSet( ds );
    this.removeAll(); 
    this.redraw( IObserver.DATA_CHANGED );
  }


/*--------------------------------=[ private ]=-------------------------------*/


  /** 
   * constructs a parallel of the selected Data blocks that are held by this
   * DataSetViewer, but stores them as GraphableData objects.
   */
  private void convert_Data_to_GraphableData()
  {
    graphable_data = new Vector();
    for( int i=0;  i<getDataSet().getNum_entries();  i++ )
      if(  getDataSet().getData_entry(i).isSelected()  )
      {
        GraphableData d = new GraphableData( getDataSet().getData_entry(i) );
          new GraphableData(  getDataSet().getData_entry(i)  );


                                          //create all of the attributes
                                          //that we want our data to
                                          //have.  (generally, these are
                                          //graphical details)  then add
                                          //them to GraphableData object

        FloatAttribute offset_attr = new FloatAttribute( GraphableData.OFFSET,
                                                         0.0f );
        String name_str = new String( "Group # " + i );
        StringAttribute name_attr = new StringAttribute( GraphableData.NAME,
                                                         name_str );
        ColorAttribute color_attr = new ColorAttribute( GraphableData.COLOR,
                                                        Color.black );
        d.addAttribute( offset_attr );
        d.addAttribute( name_attr );
        d.addAttribute( color_attr );
 
                                          
                                          //add the new GraphableData object
                                          //to the list of data to be
        graphable_data.add( d );          //visualized
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
      if(  action.equals( AUX_EXIT )  ) 
        System.exit( 1 );
    }
  }
}



