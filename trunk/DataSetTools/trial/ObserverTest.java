
package DataSetTools.trial;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;

public class ObserverTest extends    JFrame
                          implements ActionListener
{
  Button  loadButton;
  Button  modifyButton;
  Button  testButton;
  Button  deleteButton;
  ViewManager      view_man;

  public ObserverTest()
  {
    super( "ObserverTest" );
    setSize( 200, 200 );

    JPanel toolbar = new JPanel();
    toolbar.setLayout( new FlowLayout(FlowLayout.LEFT) );

    loadButton = new Button("Load");
    loadButton.addActionListener( this );
    toolbar.add( loadButton );

    modifyButton = new Button("Modify");
    modifyButton.addActionListener( this );
    toolbar.add( modifyButton );

    testButton = new Button("Test");
    testButton.addActionListener( this );
    toolbar.add( testButton );

    deleteButton = new Button("Delete");
    deleteButton.addActionListener( this );
    toolbar.add( deleteButton );

    getContentPane().add( toolbar, BorderLayout.NORTH );
  }

  public void actionPerformed( ActionEvent ae )
  {
    String command = ae.getActionCommand();
    System.out.println( command );
    DataSet ds;

    if ( command == "Load" )
    {
      System.out.println("Should load");
      RunfileRetriever rr = new RunfileRetriever(
                          "/usr/LOCAL/IPNS_Software/SampleRuns/gppd9898.run" );
      ds = rr.getDataSet( 1 );
//      view_man = new ViewManager( ds, IViewManager.SCROLLED_GRAPHS );
      view_man = new ViewManager( ds, IViewManager.IMAGE );
      view_man = new ViewManager( ds, IViewManager.IMAGE );
      rr = null;
      ds = null;
//      view_man = null;
    } 

    if ( command == "Modify" )
    {
      ds = view_man.getDataSet();
      System.out.println("Should modify");
      for ( int i = 0; i < 100; i++ )           // delete 100 spectra
        ds.removeData_entry( 10 );

      ds.notifyIObservers( IObserver.DATA_REORDERED );
   }

    if ( command == "Test" )
    {
      System.out.println("Loop to repeatedly create/destroy DataSet");
      DataSet new_ds;
      for (int i = 0; i < 100; i++)
      {
        System.out.println("1. Now Getting DataSet #" + i );
        RunfileRetriever rr = new RunfileRetriever(
                          "/usr/LOCAL/IPNS_Software/SampleRuns/gppd9898.run" );
        new_ds = rr.getDataSet( 1 );
        new_ds.removeData_entry( 0 );
        ViewManager viewer = new ViewManager( new_ds, "IMAGE" );

        new_ds = null;
        viewer = null;
        System.runFinalization();
        System.gc();
      }
    }

    if ( command == "Delete" )
    {
      System.out.println("Should delete");
//      view_man.destroy(); 
      view_man.getDataSet().notifyIObservers( IObserver.DESTROY ); 
    } 
  }

  public static void main(String args[])
  {
    ObserverTest test = new ObserverTest();
    test.setVisible( true );
  } 

} 
