/*
 * File:  STableView.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 * $Log$
 * Revision 1.10  2003/03/19 16:52:53  rmikk
 * Added the Intensity calculation for a selected region of the
 * current table to the Edit/SpreadSheet menu item of the
 * viewers for the two quick table views.
 *
 * Revision 1.9  2003/03/07 20:56:36  rmikk
 * -Cell editing has been disables. Eliminates a lot of error messages.
 *
 * Revision 1.8  2003/03/03 16:58:52  pfpeterson
 * Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 * Revision 1.7  2002/12/11 19:06:48  rmikk
 * Fixed indentation and added documentation
 *
 * Revision 1.6  2002/11/27 23:25:37  pfpeterson
 * standardized header
 *
 * Revision 1.5  2002/11/18 17:30:14  rmikk
 * Fixed an error that prevented saving the table
 *
 * Revision 1.4  2002/10/07 14:45:27  rmikk
 * Tries to position viewport after an error and/or index
 *   column is added or deleted. Somewhat successful.
 *
 * Revision 1.3  2002/07/29 22:09:16  rmikk
 * Fixed interactive table views so that they scroll
 *   correctly to the pointed at cell.
 *
 * Revision 1.2  2002/07/25 20:58:51  rmikk
 * Changed Background color of the selected cell
 * Fixed viewport to place the PointedAt cell in the center and
 *   to work with specified row and column subranges
 *
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
import DataSetTools.util.*;
import IsawGUI.*;
import java.io.*;
import javax.swing.table.*;
import DataSetTools.components.ui.*;
import DataSetTools.components.containers.*;
import DataSetTools.util.*;
import java.util.*;

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
  //protected DataSet data_set;
  //String order;
   public XJTable jtb;
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
       {state = new ViewerState();
        
        }
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
      
      JMenu Select = new JMenu( "Select");
      menu_bar.add( Select);
      JMenuItem Sel1 = new JMenuItem( "Rectangle");
      Sel1.addActionListener( new SelectActionListener());
      Select.add( Sel1);
      
      if( state.get_String( ViewerState.TABLE_DATA).indexOf("Err")>0)
         jmErr.setState( true);

      if( state.get_String( ViewerState.TABLE_DATA).indexOf("index")>0)
         jmInd.setState( true);

      jm= menu_bar.getMenu( DataSetViewer.EDIT_MENU_ID);
      JMenu jSprSht= new JMenu( "SpreadSheet");
      jm.add( jSprSht);
      JMi = new JMenuItem( "Select All" );
      JCp = new JMenuItem( "Copy Sel" );
      jSprSht.add( JMi);
      jSprSht.add( JCp);
      JMenuItem calc = new JMenuItem( "Integrate");
      jSprSht.add( calc);
      calc.addActionListener( new IntegrateListener() );
        
      initAftMenuItems();
     }
  
   class IntegrateListener implements ActionListener
    {

     private float floatValue( Object O)
      {

       if( O instanceof Number)
         return ((Number)O).floatValue();

       else if( O instanceof String)
         return ( new Float( (String)O)).floatValue();
       return 0.0f;
       }
     /** "Integrates" the selected cells, removing background (level =
     *   average of bordering cells.  Also, error estimates are given
     */
     public void actionPerformed( ActionEvent evt)
      {
        int[] rows = jtb.getSelectedRows();
        int[] cols = jtb.getSelectedColumns();
        Arrays.sort( rows);
        Arrays.sort( cols) ;
        int nrows = getNlist( rows);
        int ncols = getNlist( cols);
        int firstRow = getFirstinList( rows);
        int firstCol = getFirstinList( cols);
        
        rows= cols = null;
        if( nrows <= 0)
          return;
        if( ncols <= 0)
          return;
        float SumSel = 0.0f, SumBorder = 0.0f;
        int nSel =0, nBord = 0;
        //Selected Area
        float value;

        for( int row = firstRow; row < firstRow + nrows; row++)
          for( int col = firstCol; col < firstCol + ncols; col++)
            {
             value= floatValue(table_model.getValueAt( row, col));
             SumSel += value;
             nSel++;
            }
       //Top and Bottom Borders
        for(int col = firstCol-1; col < firstCol + ncols + 1; col++)
           if( (col >=0) && (col < table_model.getColumnCount()))
             { if( firstRow - 1 >=0)
                 {
                  value= floatValue(table_model.getValueAt( firstRow - 1, col));
                  SumBorder +=value;
                  nBord++;
                  }
               if( firstRow + nrows < table_model.getRowCount())
                 {
                  value = floatValue(table_model.getValueAt( firstRow +nrows, col));
                  SumBorder += value;
                  nBord++;
                  }
              }
         //Right and Left Borders
         for(int  row = firstRow; row < firstRow + nrows; row++)
           {
             if( firstCol -1 >= 0)
              {
                value = floatValue(table_model.getValueAt( row, firstCol - 1));
                SumBorder += value;
                nBord++;
              }   

            if( firstCol + ncols < table_model.getColumnCount() )
              {
               value = floatValue(table_model.getValueAt( row, firstCol +ncols));
               SumBorder += value;
               nBord++;
              }

            }
       
        float Intensity =(SumSel - (nSel * SumBorder/nBord));
        String S="Intensity=" + Intensity +"\n";
        double p_over_b = (0.0 + nSel)/nBord;
        double sigI =java.lang.Math.sqrt( SumSel + p_over_b*p_over_b*SumBorder);
        S+= "(Poisson)Error = " + sigI;
        S+="\n Intensity/error="+(Intensity/sigI);

          
       (new JOptionPane()).showMessageDialog( null, S);

       }//actionPerformed

     //Gets number of consecutive #'s in list starting from first in list
    //Does NOT assume list is increasing
     private int getNlist( int[] list)
       {
        if( list == null) 
           return 0;
        if( list.length <1)
           return 0;
        int F = getFirstinList( list);
        if( F < 0) 
           return 0;
        int Res = 1;
        boolean done =  Res >= list.length;
        
        while(!done)
          if( list[ Res ] == F + Res)
             {
              Res++;
              done = Res >= list.length;
              
              }
          else
             done = true;
              
        return Res;
       }

      private int getFirstinList( int[] list)
       {
         if( list== null)
           return -1;
         if( list.length < 1)
           return -1;
         return list[0];
       }


     }//class IntegrateListener

   /** fixes the table_model,Jtable(with Adapter) Xconversion table, etc.
   *  Subclasses that redefine this method should remove all components from main dataSetViewer 
   *     if they call super.initAftMenuItems or else they will be added twice
   */
   public void initAftMenuItems()
     {
      table_model = fixTableModel( state , table_model, jmErr.getState(), jmInd.getState());
      jtb = new XJTable( table_model);
      EA = new ExcelAdapter( jtb);
      jtb.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
      jtb.setColumnSelectionAllowed( true );
      jtb.setSelectionBackground( Color.lightGray);
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


  /** Subclasses can add components above the info panel. This method is empty
  *   in STableView
  *  @param  EastPanel  The EastPanel with BoxLayout( Vertical ). Add components here
  */
   public void AddComponentsAboveInfo( JPanel EastPanel)
    {
    }
  

  /** Subclasses can add components below the info panel. This method is empty
  *   in STableView
  *  @param  EastPanel  The EastPanel with BoxLayout( Vertical ). Add components here
  */
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



   /** STable Takes care of the POINTED_AT_CHANGED reason and locates the
   *   Scroll pane to the correct position.  Subclasses should take care of 
   *   all other reasons and also call this method if the POINTED_AT groups
   *   and channel change
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
        if( row >= jtb.getRowCount())
           return;
        if( col >= jtb.getColumnCount())
           return;

       

        Rectangle R = jtb.getCellRect( row,col,false);
        Rectangle Rscr = JscrlPane.getViewport().getViewRect();//getViewRect(); NG
        int width = Rscr.width;
        int height = Rscr.height;
        int nrows = height/ R.height;
        int ncols = width/R.width;
       /* if( R.x > Rscr.x + Rscr.width/nrows*4)
           if( R.x < Rscr.x -Rscr.width/nrows*4)
             if( R.y > Rscr.y + Rscr.height/ncols*4)
               if( R.y < Rscr.y -Rscr.height/ncols*4) 
                 {jtb.setColumnSelectionInterval( col,col);
                  jtb.setRowSelectionInterval(row,row);
                   
                 }
         */        
        Rectangle RR = R;
        RR.y = R.y - height/2;
        RR.x = R.x -width/2;
        Rectangle RTL = jtb.getCellRect( 0, 0, false);
        Rectangle RBR = jtb.getCellRect( jtb.getRowCount() -1,jtb.getColumnCount()-1, false);
        
        if( RR.x < RTL.x)
           RR.x = RTL.x;
        else if( RR.x > RBR.x)
           RR.x = RBR.x;


        if( RR.y < RTL.y)
           RR.y = RTL.y;
        else if( RR.y > RBR.y)
           RR.y = RBR.y;

/*        if( col> jtb.getColumnCount() -ncols/2)
           RR.x = jtb.getCellRect(row,java.lang.Math.min(0,col-ncols/2) ,false).x;
        else if( col-ncols/2 <=0)
           RR.x = jtb.getCellRect( row, 0,false).x;
        if( row >= jtb.getRowCount() -nrows/2)//means there is a blank below. min # rows
           RR.y = jtb.getCellRect(java.lang.Math.min(0,row-nrows/2) , col,false).y;
       
        else if( row -nrows/2 < 0)
           RR.y =jtb.getCellRect( 0,col,false).y;
*/

        JscrlPane.getViewport().setViewPosition( new Point( RR.x, RR.y));
        
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

      /** 
      *  Displays a JFileChooser box to save the table and then writes header information
      *  and the data to the file
      */  
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
               S .append( ds.toString());
               S.append("\n");
               S.append("#Selected Groups\n");

      
               String SS = "NO SELECTED INDICES";
               int[] SelInd = ds.getSelectedIndices() ;
               if( SelInd != null ) if( SelInd.length > 0 )
                  SS = (new NexIO.NxNodeUtils()).Showw( SelInd );

               S.append( "#     ");
               S.append(ds.toString());
               S.append(":");
               S.append( SS );

               S.append("\n");
               S.append( "#Operations\n" );
               S.append( "#     ");
               S.append(ds.toString() );
               S.append(":");
               OperationLog oplog = ds.getOp_log();

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
               DataSetTools.util.SharedData.addmsg( "Cannot Save " + 
                         ss.getClass()+":"+ss );
              }
        }
     }

 
  /** 
  *  Listener for events on the show error and show indices Option menu item buttons
  */
  private class CheckBoxListener  implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       {table_model=fixTableModel( state, table_model, jmErr.getState(),jmInd.getState());
        jtb.setModel(table_model );
        jtb.invalidate();
        redraw( IObserver.POINTED_AT_CHANGED);
       }
    }


   /**
   * Action Listener for the MenuItems to Select all and copy select
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

   /** 
   *    Listener for mouse events in the table. These events cause POINTED_AT_CHANGED events
   */
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
  /** Selects the groups that were highlighted
  */
  class SelectActionListener implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
      {System.out.println("In select Action performed");
       int[] cols = jtb.getSelectedColumns();
       int[] rows = jtb.getSelectedRows();
       System.out.println( "Rows="+ (new  NexIO.NxNodeUtils()).Showw( rows));
       System.out.println( "cols="+ (new  NexIO.NxNodeUtils()).Showw( cols));

       if( (cols == null) || ( rows == null))
         return;
       if( (cols.length < 1) ||(rows.length < 1))
         return;
       float minTime = -1, maxTime = -1;
       ds.clearSelections();
       System.out.println("Groups=");
       for( int i=0; i< cols.length; i++)
        for( int j=0; j < rows.length; j++)
         { int Group = table_model.getGroup( rows[j], cols[i] ) ;
           if( Group >= 0 )
            if( Group < ds.getNum_entries())
             ds.getData_entry( Group ).setSelected( true);
           System.out.print( Group+"  ");
           float time = table_model.getTime( rows[j], cols[i]);
           if( (i == 0) &&( j== 0))
             minTime = maxTime = time;
           else if( time < minTime)
             minTime = time;
           else if( time > maxTime)
             maxTime = time;
         }
       ds.setSelectedInterval(new ClosedInterval(minTime , maxTime ));
       System.out.println("");

     }
    }//class SelectActionListener

   /** 
   *    Test program for this module. It requires one argument, the filename of a data set
   *    to display as a Time_Slice_TableModel
   */
   public static void main( String args[])
     {if( args == null)
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

