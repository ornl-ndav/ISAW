
package EventTools.EventList;

import java.util.*;
import java.io.*;

import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.TOF_SCD.*;


public class DumpGrids
{
  
  public static void main( String args[] ) throws Exception
  {
    Vector<IDataGrid> grids = new Vector<IDataGrid>();

//  String filename = "/usr2/ARCS_SCD/ARCS_419.nxs";
    String filename = "/usr2/SEQUOIA/SEQ_328.nxs";

    NexusRetriever nr = new NexusRetriever( filename );
    nr.RetrieveSetUpInfo( null );

    int num_ds = nr.numDataSets();
    System.out.println("Number of DataSets = " + num_ds );

    for ( int i = 1; i < num_ds; i++ )
    {
      DataSet ds = nr.getDataSet( i );
      //System.out.println("DataSet " + i + " has title " + ds.getTitle() );
      if ( ds.getNum_entries() <= 0 )
        System.out.println("NO DATA ENTRIES IN " + ds.getTitle() );
      else
      {
        Data data = ds.getData_entry(0);
        PixelInfoList pil = AttrUtil.getPixelInfoList( data );
        if ( pil == null )
          System.out.println("NO PIXEL INFO IN " + ds.getTitle() ); 
        {
          IPixelInfo pi = pil.pixel(0);
          if ( pi == null )
            System.out.println("NULL PIXEL INFO IN " + ds.getTitle() );
          else
          {
            IDataGrid grid = pi.DataGrid();
            System.out.println("For DS : " + ds.getTitle() + 
                               " Got grid " + grid.ID() );
            grids.add(grid);
          }
        }
      }
    }

    nr.close();

    String outfilename = filename + ".grids";
    PrintStream out = new PrintStream( outfilename );
    for ( int i = 0; i < grids.size(); i++ )
      out.println( Peak_new_IO.GridString(grids.elementAt(i)) );
    out.close();

    System.exit(0);
  }

}
