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
 * Revision 1.2  2001/06/27 16:50:10  neffk
 * this class was formerly implementing IObserver and extending DataSetViewer,
 * which forced redraw(...) and update(...).  these two functions are
 * redundant, as well as the IObserver interface.  this class not longer
 * implements IObserver, and update(...) has been removed.
 *
 * ----------
 */

import DataSetTools.dataset.DataSet;
import DataSetTools.viewer.DataSetViewer;
import DataSetTools.util.IObserver;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import OverplotView.GraphableData;
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
    }
    else if( reason == IObserver.DATA_REORDERED)
    {
    }
    else if( reason == IObserver.DATA_DELETED )
    {
    }
    else if( reason == IObserver.SELECTION_CHANGED )
    {
      convert_Data_to_GraphableData();
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
      convert_Data_to_GraphableData();
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
  }


  /**
   * brings the graphics objects up to date.  first, all currently selected
   * data is added converted
   */
  public void redraw()
  {
    graph.init( graphable_data );
    add(  graph.redraw()  );
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
        graphable_data.add(  
          new GraphableData( getDataSet().getData_entry(i) )  );
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



