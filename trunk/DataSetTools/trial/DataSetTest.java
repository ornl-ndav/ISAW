/*
 *  @(#)  DataSetTest.java    1.0  2000/03/02    Dennis Mikkelson
 *  ( Derived from DataSetDemo1.java, tests setting new DataSet into a viewer )
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

import java.awt.*;
import java.awt.event.*;

/**
  *    This class provides a basic demo of reading DataSets from runfiles, 
  *  showing the DataSets as images and scrolled graphs.
  *
  *    IMPORTANT!! Since this demo does NOT provide for any user interaction, 
  *  you will have to edit this source file to specify different runfiles to 
  *  load, or to call different operators, etc. 
  *
  */  

public class DataSetTest 
{
    String           run_A = "HRCS0979.RUN";
    String           run_B = "gppd9902.run"; 

    DataSet          A_histogram_ds,
                     B_histogram_ds,
                     current;

    RunfileRetriever rr;    // The RunfileRetriever object calls John's runfile
                            // package and returns the data as DataSets

    ViewManager      view_manager;  // Variable to hold reference to a 
                                    // ViewManager that will display a DataSet

  public DataSetTest()
  {                                               // load two DataSets but
    rr = new RunfileRetriever( run_A );           // only show one of them
    A_histogram_ds = rr.getDataSet( 1 );

    rr = new RunfileRetriever( run_B ); 
    B_histogram_ds = rr.getDataSet( 1 );

 //   view_manager = new ViewManager( A_histogram_ds,IViewManager.SELECTED_GRAPHS);
    view_manager = new ViewManager( A_histogram_ds,IViewManager.IMAGE);
    current = A_histogram_ds;

    FileMenuHandler file_menu_handler = new FileMenuHandler();
    JMenu file_menu = view_manager.getJMenuBar().getMenu(
                                      DataSetViewer.FILE_MENU_ID );

    JMenuItem button = new JMenuItem( "Switch DataSet" );
    button.addActionListener( file_menu_handler );
    file_menu.add( button );
  }

  /**
    *  The main program method for this object
    */
  public static void main(String args[])
  {
    DataSetTest test = new DataSetTest();   // create an instance of this test
                                            // object
  }

  private class FileMenuHandler implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();
      System.out.println( action );
      if ( action.equals( "Switch DataSet" )) 
      {
        if ( current == A_histogram_ds )
        {
          current = B_histogram_ds;
          view_manager.setDataSet( current );
        }
        else
        {
          current = A_histogram_ds;
          view_manager.setDataSet( current );
        }        
      }
    }
  }
} 
