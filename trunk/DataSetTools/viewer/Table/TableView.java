
package DataSetTools.viewer.Table;

import javax.swing.*;
import DataSetTools.viewer.*;
import DataSetTools.dataset.*;
import java.awt.*;
import javax.swing.table.*;
public class TableView extends DataSetViewer
{   table_view.Gen_TableModel tbm;


   public TableView( DataSet ds, ViewerState state, table_view.Gen_TableModel tbm)
    {super( ds,state);
     
     this.tbm= tbm;
     JTable jtb = new JTable( tbm);
     if( jtb ==null)
       System.out.println("JTable is null");
     jtb.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
     setLayout( new GridLayout( 1,1));
     add( new JScrollPane(jtb));

     
     }
   public void redraw(String S){}

  public static void main( String args[] )
   { JFrame jf = new JFrame( "Test");
     DataSet[] DS = (new IsawGUI.Util()).loadRunfile( args[0]);
     String state="X values;Y values;";
     table_view tv = new table_view(0);
     tv.setDataSets( DS);
     tv.restoreState( state);
     DefaultListModel LM= tv.getListModel();
     int[] sel = new int[4];
     sel[0]=0; sel[1]=2; sel[2]=4; sel[3]=6;
     table_view.Gen_TableModel tm= tv.getGenTableModel( DS[1],LM,"HT,FG",sel);
      /*JTable jtb = new JTable( tm);
     jtb.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
     jf.getContentPane().setLayout( new GridLayout(1,1));
     jf.getContentPane().add( jtb);
      */
     /*TableView TV= new TableView( DS[1],new ViewerState(), tm);
      jf.getContentPane().setLayout( new GridLayout(1,1));
    
     jf.getContentPane().add(TV);
    jf.setSize( 400,400);
     jf.show();
    */
     DS[1].setSelectFlag(0, true);
     DS[1].setSelectFlag(2, true);
     new ViewManager( DS[1],"Time vs Gr,Field");
   }
}
