/*
 * File:  TableView.java
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
 *  $Log$
 *  Revision 1.2  2002/07/17 19:11:28  rmikk
 *  Added GPL
 *  Fixed up the table views menu choices
 *
 */
package DataSetTools.viewer.Table;

import javax.swing.*;
import DataSetTools.viewer.*;
import DataSetTools.dataset.*;
import java.awt.*;
import IsawGUI.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.io.*;

/** Produces a Lot of table views using the table_view.Gen_TableModel.  The state determines
*   whether it shows errors or indices.
*/
public class TableView extends DataSetViewer
{  table_view.Gen_TableModel tbm;
   ExcelAdapter EA;
   DataSet ds;
   ViewerState state;
   DataSet data_set;
   String order;
   JTable jtb ;
   JCheckBoxMenuItem jmErr=null; 
   JCheckBoxMenuItem jmInd=null;
   JMenuItem  JMi, 
              JCp ;
  

   public TableView( DataSet ds, ViewerState state1, String order)
     {super( ds,state1);
      state = state1;
      if( state == null)
         state = new ViewerState();
      this.ds = ds;
      this.order= order;
      data_set = ds;
       

      JMenuBar menu_bar = getMenuBar();
      JMenu jm = menu_bar.getMenu( DataSetViewer.FILE_MENU_ID );

      JMenuItem sv= new JMenuItem( "Save to a File");
      sv.addActionListener( new MyActionListener());
      jm.add(sv);

      jm= menu_bar.getMenu( DataSetViewer.OPTION_MENU_ID);
      jmErr = new JCheckBoxMenuItem("Show Errors");
      jmErr.addActionListener( new CheckBoxListener());

      jmInd = new JCheckBoxMenuItem("Show Indicies");
      jmInd.addActionListener( new CheckBoxListener());
      jm.add( jmErr);
      jm.add( jmInd);
       
      if( state.get_String( ViewerState.TABLE_DATA).indexOf("Err")>0)
         jmErr.setState( true);

      if( state.get_String( ViewerState.TABLE_DATA).indexOf("index")>0)
         jmInd.setState( true);

      jm= menu_bar.getMenu( DataSetViewer.EDIT_MENU_ID);
      JMi = new JMenuItem( "All" );
      JCp = new JMenuItem( "Copy Sel" );
      jm.add( JMi);
      jm.add( JCp);
      tbm =getTableModel();


      jtb = new JTable( tbm);
      EA = new ExcelAdapter( jtb);
     
      JMi.addActionListener( new MyJTableListener( jtb, tbm, EA ) );
      JCp.addActionListener( new MyJTableListener( jtb, tbm, EA ) );
      jtb.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
      setLayout( new GridLayout( 1,1));
      add( new JScrollPane(jtb));
     }


   private table_view.Gen_TableModel getTableModel()
     {table_view tv= new table_view(0);
      String state1 = "X values;Y values;";
      if( jmErr.getState())
         state1 += "Error values;";
      if( jmInd.getState())
         state1 += "XY index;";
      state.set_String( ViewerState.TABLE_DATA, state1);
      DataSet[] DSS = new DataSet[1];
      DSS[0] = data_set;
      tv.setDataSets( DSS );
      tv.restoreState( state1);
      DefaultListModel LM = tv.getListModel();
      return tv.getGenTableModel( data_set, LM, order, data_set.getSelectedIndices());

     }


   public void redraw(String S){}

   public ViewerState getState()
     {return state;
     }

   public static void main( String args[] )
     {JFrame jf = new JFrame( "Test");
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


   private class MyActionListener implements ActionListener
     {
      String filename = null;
      public void actionPerformed( ActionEvent evt )
        {JFileChooser jf ;
         if( filename == null )  
            jf = new JFileChooser();
         else 
            jf = new JFileChooser( filename );
         FileOutputStream fout = null;
         if( !( jf.showSaveDialog( null ) == JFileChooser.CANCEL_OPTION ) )
            try
              {filename = jf.getSelectedFile().toString();
               File ff = new File( filename );
               fout = new FileOutputStream( ff );       
      
               StringBuffer S =new StringBuffer( 8192); 
              // Header Stuff
               S.append( "#Data Set");
               S .append( data_set.toString());
               S.append("\n");
               S.append("#Selected Groups\n");

      
               String SS = "NO SELECTED INDICES";
               int[] SelInd = data_set.getSelectedIndices() ;
               if( SelInd != null ) if( SelInd.length > 0 )
                  SS = (new NexIO.NxNodeUtils()).Showw( SelInd );

               S.append( "#     ");
               S.append(data_set.toString());
               S.append(":");
               S.append( SS );

               S.append("\n");
               S.append( "#Operations\n" );
               S.append( "#     ");
               S.append(data_set.toString() );
               S.append(":");
               OperationLog oplog = data_set.getOp_log();

               if( oplog != null )
                  for( int j = 0; j < oplog.numEntries(); j++ )
                    {
                     S.append(oplog.getEntryAt( j ));
                     if( j + 1 < oplog.numEntries() )
                        S.append( "\n");  
                    }
               S.append("\n");
        
               fout.write(S.toString().getBytes());
               S.setLength(0);

               for( int i=0; i< tbm.getRowCount(); i++)
                 {if( S.length()>7000)
                    {fout.write(S.toString().getBytes());
                     S.setLength(0);
                    }
                  for(int j=0; j< tbm.getColumnCount(); j++)
                    {S.append( tbm.getValueAt(i,j));
                     if( j+1 < tbm.getColumnCount())
                        S.append( "\t");
                     else
                        S.append("\n");
                    }
                 }
               if( S.length()>0)
                  fout.write(S.toString().getBytes());
           
               fout.close( ); 
               System.out.println( "Closed" );
   
              }
            catch( Exception ss )
              {
               DataSetTools.util.SharedData.status_pane.add( "Cannot Save " + 
                         ss.getClass()+":"+ss );
              }
        
    
        }


     }
   private class CheckBoxListener  implements ActionListener
     {
      public void actionPerformed( ActionEvent evt)
        {tbm=getTableModel();
         jtb.setModel(tbm );
         jtb.invalidate();

        }
     }
 /**Action Listener for the MenuItems to Select all and copy select
    * in the JFrame containing the JTable
    */
   public class MyJTableListener implements ActionListener
     {
      JTable JTb;
      ExcelAdapter EA;
      TableModel DTM;
      public MyJTableListener( JTable JTb, TableModel DTM, ExcelAdapter EA )
        {
         this.JTb = JTb;
         this.DTM = DTM;
         this.EA = EA;
        }


      public void actionPerformed( ActionEvent e )
        {
         JMenuItem targ = ( JMenuItem )e.getSource();

         if( targ.equals( JMi ) )
           {JTb.selectAll();
           // JTb.setRowSelectionInterval( 0, DTM.getRowCount() - 1 );
            //JTb.setColumnSelectionInterval( 0, DTM.getColumnCount() - 1 );
           }
         else if( targ.equals( JCp ) )
           {
            EA.actionPerformed( new ActionEvent( JTb, 0, "Copy" ) );
           }
        }
     }

}
