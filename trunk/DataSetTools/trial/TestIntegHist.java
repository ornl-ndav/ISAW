/*
 * TestIntegHist
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;


public class TestIntegHist 
{
  public static void main(String args[])
  {
    DataSet      monitor_ds;

    String       run = "/usr/home/dennis/ARGONNE_DATA/SEPD_DATA/SEPD16444.RUN";

    RunfileRetriever rr;    
    ViewManager view_manager;  
    rr = new RunfileRetriever( run ); 
    monitor_ds = rr.getDataSet( 0 );

    view_manager = new ViewManager( monitor_ds, IViewManager.IMAGE);

    Operator op = new IntegrateGroup( monitor_ds, 4, 3000, 25000 );

    Data monitor = monitor_ds.getData_entry_with_id( 4 );

    monitor.print( 0, 10 );
    monitor.print( 20, 30 );

    monitor.print( 1120, 1130 );

    float y[] = monitor.getY_values();

    double sum = 0;
    for ( int i = 25; i <= 1124; i++ )
      sum += y[i];

    System.out.println("-----------------------------------------------------");
    System.out.println("Sum of group 4, beginning with bin 25, t_left = 3000");
    System.out.println("ending with bin 1124, tleft = 24980 gives:");
    System.out.println("Sum of bins = "+ sum );
    System.out.println("IntegrateGroup 4 on [3000,25000] = " + op.getResult());

    op = new IntegrateGroup( monitor_ds, 4, 3000, 25020 );
    sum = 0;
    for ( int i = 25; i <= 1125; i++ )
      sum += y[i];
    System.out.println("-----------------------------------------------------");
    System.out.println("Sum of group 4, beginning with bin 25, t_left = 3000");
    System.out.println("ending with bin 1125, tleft = 25000 gives:");
    System.out.println("Sum of bins = "+ sum );
    System.out.println("IntegrateGroup 4 on [3000,25020] = " + op.getResult());

    System.out.println("-----------------------------------------------------");
    System.out.println("NOTE: The result 6565210 from the faxed copy could");
    System.out.println("be obtained by adding bin 1124 twice.");
  } 
} 
