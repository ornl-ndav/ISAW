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
 * Revision 1.1  2001/06/21 15:44:42  neffk
 * redesign of OverplotView
 *
 * ----------
 */

import DataSetTools.dataset.DataSet;
import DataSetTools.viewer.DataSetViewer;
import DataSetTools.util.IObserver;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import OverplotView.GraphableData;

public class GraphableDataManager 
  extends DataSetViewer
  implements IObserver
{

  public static final String AUX_EXIT = "Auxiliry Exit";

  public GraphableDataManager( DataSet data_set )
  {
    super(data_set);     // Records the data_set in the parent class and
                         // sets up the menu bar with items handled by the
                         // parent class.

    OptionMenuHandler option_menu_handler = new OptionMenuHandler();
    JMenu option_menu = menu_bar.getMenu( OPTION_MENU_ID );

    JMenuItem exitButton = new JMenuItem( AUX_EXIT );
    exitButton.addActionListener( option_menu_handler );
    option_menu.add( exitButton );

//    addComponentListener(  new ViewComponentAdapter()  );

    data_set.addIObserver( this );  
  }


  public void update( Object obj, Object reason )
  {
    System.out.println( "update(...) called" );
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
    }
    else if( reason == IObserver.DATA_REORDERED)
    {
    }
    else if( reason == IObserver.DATA_DELETED )
    {
    }
    else if( reason == IObserver.SELECTION_CHANGED )
    {
    }
    else if( reason == IObserver.POINTED_AT_CHANGED )
    {
    }
    else if( reason == IObserver.GROUPS_CHANGED )
    {
    }
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



