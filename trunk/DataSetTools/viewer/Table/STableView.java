/*
 * File:  STableView.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at yoJTableur option) any later version.
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * $Log$
 * Revision 1.1  2002/07/24 20:05:20  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.viewer.Table;
import DataSetTools.dataset.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.viewer.*;
import IsawGUI.*;
import java.io.*;
import javax.swing.table.*;
import DataSetTools.components.ui.*;
import DataSetTools.components.containers.*;
import DataSetTools.util.*;


/** This class can be used as a Stub for all the special TableViewers in the old
*   format.  It creates a DataSetViewer with one table at the left and one Xconversions
*   table at the right.  The Options Show Errors and Show Indices are implemented along with
*   the connection between the table and the Xconversions operator.  The POINTED_AT message
*   is handled here
*/
public class STableView  extends DataSetViewer
 {
  protected TableViewModel table_model;
  DataSet ds;
  protected ViewerState state;
  protected DataSet data_set;
  //String order;
  protected JTable jtb ;
  JScrollPane JscrlPane;
  protected JPanel TableHolder;
  JCheckBoxMenuItem jmErr=null; 
  JCheckBoxMenuItem jmInd=null;
  JMenuItem  JMi, 
             JCp ;
  protected JPanel jEast;
  DataSetXConversionsTable InfoTable;
  JPanel InfoTableHolder;
  ExcelAdapter EA;

  
  public STableView( DataSet DS, ViewerState state1, TableViewModel tabMod)
    {super( DS, state1);
     state = state1;
     if( state == null)
       state = new ViewerState();
     this.ds = DS;
     table_model = tabMod;
     initState( state );
     initFrMenuItems();
    }
  /** Initializes the whole viewer including the Menu items. The table Model has been
  *   set and the state initialized. The table model can be fixed a bit here.
  */
  public void initFrMenuItems()
    {// Add the Menu Bars items
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
      JMi = new JMenuItem( "Select All" );
      JCp = new JMenuItem( "Copy Sel" );
      jm.add( JMi);
      jm.add( JCp);
      initAftMenuItems();
     }

   /** fixes the table_model,Jtable(with Adapter) Xconversion table, etc.
   *  Subclasses that redefine this method should remove all components from main dataSetViewer 
   *     if they call super.initAftMenuItems or else they will be added twice
   */
   public void initAftMenuItems()
     {
      table_model = fixTableModel( state , table_model, jmErr.getState(), jmInd.getState());
      jtb = new JTable( table_model);
      EA = new ExcelAdapter( jtb);
      jtb.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
      jtb.setColumnSelectionAllowed( true );
      jtb.setSelectionBackground( Color.red);
      jtb.removeEditor();
      TableHolder = new JPanel( new GridLayout( 1,1));
      JscrlPane = new JScrollPane(jtb);
      TableHolder.add( JscrlPane );
       //Remove all listeners;

      JMi.addActionListener( new MyJTableListener( jtb, table_model, EA ) );
      JCp.addActionListener( new MyJTableListener( jtb, table_model, EA ) );

      InfoTable = new DataSetXConversionsTable (ds );
      jEast = new JPanel();

      BoxLayout bl = new BoxLayout(jEast, BoxLayout.Y_AXIS );
      jEast.setLayout( bl );
      AddComponentsAboveInfo( jEast);
      InfoTableHolder = new JPanel( new GridLayout( 1, 1));
      InfoTableHolder.add( InfoTable.getTable());
      jEast.add(InfoTableHolder);
      AddComponentsBelowInfo( jEast);
      jEast.add( Box.createVerticalStrut(50000));
      setLayout( new GridLayout( 1,1));
      SplitPaneWithState main = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                TableHolder, jEast, .7f);
      add( main);
      jtb.addMouseListener( new MyMouseListener());
     }
  /** Sets the data set, table model, Xconversion table,and the JTable,and validates
  */
  public void setDataSet( DataSet ds)
   { this.ds = ds;
    
     super.setDataSet( ds);
     boolean serr,sind;
     serr = jmErr.getState();
     sind = jmInd.getState(); 
     table_model = fixTableModel( state, table_model, serr,sind);
     
     InfoTableHolder.remove( InfoTable.getTable());
     InfoTable = new DataSetXConversionsTable (ds );
     InfoTableHolder.add(InfoTable.getTable());
     jtb.setModel( table_model);
     validate();
   }

 /** Initializes the state variables for the show error/index options<P>
  * Subclasses can add extra  state information and initializations before or after this.<P>
  * NOTE: All variables in a subclass should be initialized here.
  */
  public void initState( ViewerState state)
    {if( state.TABLE_DATA.equals(""))
       {state.set_String(ViewerState.TABLE_DATA, "Y values;");
       }
   
     }

  /** Subclasses can change the table model to reflect various
  *   items of the state. The viewer state is initially updated before this
  *   method is called.
  */ 
  public TableViewModel fixTableModel( ViewerState state , TableViewModel table_model, 
                            boolean showerrors, boolean showIndices)
    { return table_model;
    }

   public void AddComponentsAboveInfo( JPanel EastPanel)
    {
    }
  
   public void AddComponentsBelowInfo(JPanel EastPanel)
    {
     }
  
   /** Subclasses can redefine this for faster saves<P>
   *   NOTE: the header information has already been written
   */
   public void SaveFileInfo(OutputStream fout)
    {StringBuffer S= new StringBuffer();
     try{
     for( int i=0; i< table_model.getRowCount(); i++)
       {if( S.length()>7000)
          {fout.write(S.toString().getBytes());
           S.setLength(0);
          }
        for(int j=0; j< table_model.getColumnCount(); j++)
          {S.append( table_model.getValueAt(i,j));
           if( j+1 < table_model.getColumnCount())
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
     catch( Exception s) 
       {DataSetTools.util.SharedData.addmsg("Cannot close file "+s);
        }
  
    
   
   }

   /** does nothing
   */
   public void redraw(String S)
     {if( S == IObserver.POINTED_AT_CHANGED )
      { 
        float x = getDataSet().getPointedAtX();
        int index =getDataSet().getPointedAtIndex();
        
       
        InfoTable.showConversions( x, index );

        int row = table_model.getRow( index, x);
        int col = table_model.getCol( index, x);

        if( row <  0)
          return;
        if( col < 0)
          return;
        if( row == jtb.getSelectedRow())
          if( col == jtb.getSelectedColumn())
            return;
        Rectangle R = jtb.getCellRect( row-2,col-2,false);
        JscrlPane.getViewport() .setViewPosition( new Point( R.x, R.y));
        jtb.setColumnSelectionInterval( col,col);
        jtb.setRowSelectionInterval(row,row);

      }
     
     }

   public ViewerState getState()
     {return state;
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
               SaveFileInfo( fout);
               
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
       {table_model=fixTableModel( state, table_model, jmErr.getState(),jmInd.getState());
        jtb.setModel(table_model );
        jtb.invalidate();

       }
    }

 /**Action Listener for the MenuItems to Select all and copy select
    * in the JFrame containing the JTable
    */
   private class MyJTableListener implements ActionListener
     {
      JTable JTb;
      ExcelAdapter EA;
      TableViewModel DTM;
      public MyJTableListener( JTable JTb, TableViewModel DTM, ExcelAdapter EA )
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

   class MyMouseListener  extends MouseAdapter
     {
      public void mouseClicked(MouseEvent e)
        { int row = jtb.getSelectedRow();
          int col = jtb.getSelectedColumn();
          int Group = table_model.getGroup( row,col);
          float Time = table_model.getTime(row,col);
          getDataSet().setPointedAtIndex( Group);
          getDataSet().setPointedAtX( Time);
         
          getDataSet().notifyIObservers( IObserver.POINTED_AT_CHANGED );
          //InfoTable.showConversions( Time,Group);
          
         }

      }

   /** Test program for this module. It requires one argument, the filename
   */
   public static void main( String args[])
    { if( args == null)
        System.exit(0);
      if( args.length<1)
        System.exit(0);
      DataSet[] DS = (new IsawGUI.Util()).loadRunfile( args[0]);
      Time_Slice_TableModel tbm = new Time_Slice_TableModel(DS[1],4317.0f,false,false);
      JFrame jf = new JFrame("Test");
      STableView stab = new STableView( DS[1], null,tbm);
      jf.getContentPane().add( stab);
      jf.setSize( 400,400);
      jf.show();

     }
   }

