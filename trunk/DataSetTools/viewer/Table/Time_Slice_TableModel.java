/*
 * File:  Time_Slice_TableModel.java 
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
 * Revision 1.16  2003/11/06 20:06:43  rmikk
 * The Constructor now Throws an IllegalArgument
 *    exception if the DataSet does not have enough info to
 *    make a table
 *
 * Revision 1.15  2003/10/27 14:57:20  rmikk
 * Eliminated resampling of Data.
 *
 * Revision 1.14  2003/09/24 13:57:40  rmikk
 * Added accessor methods getDetNum, setDetNum, setErrInd
 *
 * Revision 1.13  2003/09/23 15:54:28  rmikk
 * -Eliminated the persistent reference to the DataGrid. This
 *  grid is changed by other operators and other Viewers so
 * does not retain valid values
 *
 * Revision 1.12  2003/09/05 21:45:16  rmikk
 * Implemented support for more than one detector
 *
 * Revision 1.11  2003/03/12 17:10:49  rmikk
 * Eliminated a null pointer error if Data.getErrors() returns null
 * Added several method stubs to get this class more in line
 *    with our new ViewArray producer
 *
 * Revision 1.10  2003/03/03 16:58:52  pfpeterson
 * Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 * Revision 1.9  2003/02/12 20:02:05  dennis
 * Switched to use PixelInfoList instead of SegmentInfoList
 *
 * Revision 1.8  2003/01/15 20:54:29  dennis
 * Changed to use SegmentInfo, SegInfoListAttribute, etc.
 *
 * Revision 1.7  2002/11/27 23:25:37  pfpeterson
 * standardized header
 *
 * Revision 1.6  2002/10/07 14:51:36  rmikk
 * Implemented and tested the cases where there are several
 *    columns per group.
 * Return an empty string in the cases there is no value
 * The index column now reports the GROUP INDEX.  The
 *    time index is always the same in a time slice. It can be
 *    viewed two other places
 *
 * Revision 1.5  2002/07/26 22:05:55  rmikk
 * Incorporated XScales to find y values and error values.
 * The XScales can now be set from outside.
 *
 * Revision 1.4  2002/07/25 21:02:44  rmikk
 * Fixed code to work better with subranges of rows and
 *   or columns.
 *
 * Revision 1.3  2002/07/24 20:03:04  rmikk
 * Added methods to get correspondences between the
 *   JTable row and column and the associated Group Index
 *   and time
 * added and implemented methods to view a subrange of rows
 *   and or a subrange of columns
 * Eliminated extra carriage returns and adjusted the indentations
 *
 * Revision 1.2  2002/07/19 22:20:48  rmikk
 * Changed to start at row=1, col=1 instead of row,col=0,0
 * Added hooks to display only regions of interest.
 *
 * Revision 1.1  2002/02/27 16:48:50  rmikk
 * Initial Checkin
 *
 */
// To do -resample should be done one time
package DataSetTools.viewer.Table;


import java.awt.event.*;
import javax.swing.text.*;

import DataSetTools.dataset.*;
import javax.swing.table.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import DataSetTools.instruments.*;
import java.io.*;
import javax.swing.*;
import DataSetTools.components.View.ViewControls.*;
import DataSetTools.util.*;

/** Creates a table model the displays y values at a fixed time according to row
 * and column values of the group
 */
public class Time_Slice_TableModel extends TableViewModel implements ActionListener
{
   int MaxRow,
       MaxCol;
   float Time;
   float MinTime = Float.NEGATIVE_INFINITY,
         MaxTime = Float.POSITIVE_INFINITY;
   int[] RC_to_Group;
   DataSet DS;
   boolean err,
           ind;
   int tMinrow,
       tMaxrow;
   int tMincol,
       tMaxcol;
   int DetNum = -1;
   int[] DetNums=null;
   UniformGrid grid = null;
   Data[][] Groups = null;
   int num_rows,num_cols;
   XScale x_scale = null;
   int firstGroup =-1;
   boolean bugg=false;
   /** Constructor for this table model of the Data Set DS at time time
    *@param  DS  the data set for which the model will present the data
    *@param  time  the time of this time slice
    */
   public Time_Slice_TableModel( DataSet DS, float time, boolean showErrors, 
           boolean showInd ) throws IllegalArgumentException
   {   
      Time = time;
      int[] row = new int[DS.getNum_entries()];
      int[] col = new int[DS.getNum_entries()];

      Time = time;
      this.DS = DS;

      err = showErrors;
      ind = showInd;
      MaxRow = -1;
      MaxCol = -1;
      SetUpDetNums();
      if( DetNums == null)
          throw new IllegalArgumentException("DataSet does not have any Grids");

      MaxRow = num_rows;
      MaxCol = num_cols;
      tMinrow = 0;
      tMaxrow = MaxRow - 1;
      tMincol = 0;
      tMaxcol = MaxCol - 1;
     /* for( int i = 0; i < DS.getNum_entries(); i++ )
      {
         PixelInfoListAttribute Ax = ( PixelInfoListAttribute )
               DS.getData_entry(i).getAttribute( Attribute.PIXEL_INFO_LIST );

         row[i] = -1;
         col[i] = -1;
         if( Ax != null )
         {
           if( ((PixelInfoList)(Ax.getValue())).num_pixels() > 0 )
           {
             IPixelInfo B = ((PixelInfoList)(Ax.getValue())).pixel(0);

             row[i] = (int)B.row();
             col[i] = (int)B.col();
             if( row[i] > MaxRow )
               MaxRow = row[i];
             if( col[i] > MaxCol )
               MaxCol = col[i];
           }
         }

      }
      if( ( MaxCol < 0 ) || ( MaxRow < 0 ) )
      {
         RC_to_Group = null;
         return;
      }
      tMinrow = 0;
      tMaxrow = MaxRow - 1;
      tMincol = 0;
      tMaxcol = MaxCol - 1;
      RC_to_Group = new int[( MaxRow ) * ( MaxCol )];
      Arrays.fill( RC_to_Group, -1 );
      for( int i = 0; i < row.length; i++ )
      {
         int r = row[i];
         int c = col[i];

         if( r >= 0 )
            if( c >= 0 )
               RC_to_Group[( r - 1 ) * ( MaxCol ) + ( c - 1 )] = i;
      }

    */   
    firstGroup = 0;
    int[] u = DS.getSelectedIndices();
    if( u != null)
      if( u.length > 0)
        firstGroup = u[0];
   
    x_scale = DS.getData_entry( firstGroup).getX_scale();
   }


   public void setTime( float time )
   { 
      Time = time;
   }


   /**
   *   Sets whether the error and/or indecies are to be shown.
   *   The JTable using this table model must be invalidated to
   *   reset everything
   */
   public void setErrInd( boolean error, boolean index){
     err = error;
     ind = index;

   }

   /** Used to set a new time for this time slice.
    */
   public void setTimeRange( float MINtime, float MAXtime )
   {
      MinTime = MINtime;
      MaxTime = MAXtime;
      if( MaxTime < MinTime )
         MaxTime = MinTime;

   }


   public void setRowRange( int Minrow, int Maxrow )
   {
      tMinrow = Minrow - 1;
      tMaxrow = Maxrow - 1;
      if( tMinrow < 0 )
         tMinrow = 0;
      if( tMaxrow >= MaxRow )
         tMaxrow = MaxRow - 1;
      if( tMinrow >= Maxrow )
         tMinrow = Maxrow - 1;
      if( tMinrow > tMaxrow )
         tMinrow = tMaxrow;
   }


   public void setColRange( int Mincol, int Maxcol )
   {
      tMincol = Mincol - 1;
      tMaxcol = Maxcol - 1;
      if( tMincol < 0 )
         tMincol = 0;
      if( tMaxcol > MaxCol )
         tMaxcol = MaxCol - 1;
      if( tMincol > Maxcol )
         tMincol = Maxcol - 1;
      if( tMincol > tMaxcol )
         tMincol = tMaxcol;
   }


   /** Returns the column name for a column,  In this case it is C # where # is the column
    *  number
    */
   public String getColumnName( int column )
   {
      int n = 1;

      if( err ) n++;
      if( ind )  n++;
      int col = column / n;
      String S = "y";
      int col2 = column - n * ( col );

      if( col2 == 1 )
         if( err )
            S = ";err";
         else
            S = ";Index";
      if( col2 == 2 )
         S = ";Index";

      col = col + tMincol + 1;
      return "C" + ( col ) + ":" + S;
   }


   /** returns the number of rows
    */
   public int getRowCount()
   {  if( Groups == null)
        return 0;
      if( num_rows < 0 )
         return 0;
      else
         return tMaxrow - tMinrow + 1;
   }


   /** Returns the number of Columns
    */
   public int getColumnCount()
   {
      int n = 1;
      if( Groups == null)
       return 0;
      if( err ) n++;
      if( ind )  n++;

      if( num_rows < 0 )
         return 0;
      else
         return( tMaxcol - tMincol + 1 ) * n;
   }

   /**
   *    returns the group index of the screen pixel at row and col
   */
   public int getGroup( int row, int column )
   {
      if( Time < 0 )
         return -1;

      if( row < 0 )
         return -1;

      int rc = getRowCount();

      if( row >= rc )
         return -1;
      if( column < 0 )
         return -1;

      rc = getColumnCount();

      if( column >= rc )
         return -1;
      int n = 1;

      if( err ) n++;
      if( ind ) n++;
      boolean doo = ( row == 1 ) && ( column == 2 );

      row = row + tMinrow;// -1;
      column = column + tMincol;// - 1;
      int Grp = DS.getIndex_of_data(Groups[ row+1][ column+1]);
          //RC_to_Group[row * ( MaxCol ) + column / n];

      // if( doo)
      //System.out.println( "rw col="+Grp+","+row+","+column);

      return Grp;
   }


   public float getTime( int row, int column )
   {
      return Time;
   }


   /** Get the table row corresponding to The given GroupIndx.  The time is
    *  ignored because this is a time slice.
    * @param  GroupIndx   The index of the Group
    * @param time      Ignored because this is a time slice
    * @return   The table row #(starting at 0) or -1 if none.
    */
   public int getRow( int GroupIndx, float time )
   {  //System.out.println("in getRow GroupIndx="+GroupIndx+","+tMinrow+","+tMaxrow);
      if( GroupIndx < 0 )
         return -1;
     /* for( int r = 0; r < MaxRow; r++ )
         for( int c = 0; c < MaxCol; c++ )
         {//System.out.print(r*MaxCol+c+" ");
            if( RC_to_Group[ r * MaxCol + c] == GroupIndx )
            {//System.out.println("   Found row ="+r);
               if( r > tMaxrow )
                  return -1;
               else
                  return r - tMinrow;
            }
         }
       */  //System.out.println("");    

      PixelInfoList pilist =((PixelInfoList)(DS.getData_entry(GroupIndx). 
                       getAttributeValue( Attribute.PIXEL_INFO_LIST)));
      for( int i=0; i< pilist.num_pixels(); i++){
           if( pilist.pixel(i).DataGrid().ID() == DetNum){

              int r= (int)pilist.row() -1;
              if( r > tMaxrow)
                 return -1;
              else
                return r -tMinrow;
           }

      }
      return -1;
   }


   /** Get the table column corresponding to The given GroupIndx.  The time is
    *  ignored because this is a time slice.
    * @param  GroupIndx   The index of the Group
    * @param time      Ignored because this is a time slice
    * @return   The table column #(starting at 0) or -1 if none.
    */
   public int getCol( int GroupIndx, float time )
   { 
     if( GroupIndx < 0 )
         return -1;
     int n=1;
     if( err) n++;
     if(ind) n++;
     /*for( int r = 0; r < MaxRow; r++ )
         for( int c = 0; c < MaxCol; c++ )
            if( RC_to_Group[ r * MaxCol + c] == GroupIndx )
            { 
               if( c <= tMaxcol )
                  return n*(c - tMincol);
            }*/

      PixelInfoList pilist =((PixelInfoList)(DS.getData_entry(GroupIndx). 
                       getAttributeValue( Attribute.PIXEL_INFO_LIST)));
      for( int i=0; i< pilist.num_pixels(); i++){
           if( pilist.pixel(i).DataGrid().ID() == DetNum){

              int c =(int)pilist.col() -1;
              if( c <= tMaxcol )
                  return n*(c - tMincol);
              else return -1;

           }

      }
      return -1;
   }


   /** Returns the y value of the data set associated with the row and column at the time
    * that has been set
    */
   public Object getValueAt( int row, int column )
   {

      int Grp = getGroup( row, column );
      if( Grp < 0 )
         return "";
      if( Grp >= DS.getNum_entries() )
         return "";
      //XScale xscl =DS.getData_entry( Grp ).getX_scale();
     // float[] xvals = DS.getData_entry( Grp ).getX_scale().getXs();

      //int index = xscl.getI( Time );

      /*float dx = ( xvals[1] - xvals[0] ) / 10.0f;

      if( index >= xvals.length )
         if( ( Time - xvals[xvals.length - 1] ) <
            .1 * ( xvals[xvals.length - 1] - xvals[xvals.length - 2] ) )
            index = xvals.length - 1;
         else
            index = -1;
      else if( index < 0 )
         index = -1;

      else if( java.lang.Math.abs( xvals[index] - Time ) < .1 * dx )
      {}
      else
         index = -1;
      */
      int n = 1;
      if( err ) n++;
      if( ind ) n++;
      int field = column - n * ( column / n );

      float[] yvals = null;
      Data db = (Data)(DS.getData_entry( Grp).clone());
     // if( x_scale != null)
     //   db.resample( x_scale,0);
     // XScale xscl;
     // xscl = db.getX_scale();
     // int index = xscl.getI( Time );
     /* if( index > 0)
        if( (xscl.getX( index)-Time) > xscl.getX( index-1)-Time )
          index = index -1;
     
      if( java.lang.Math.abs( Time -xscl.getX(index))<= 1E-5*java.lang.Math.abs(Time))
        return "";
     */
     
      if( field == 0 )
         yvals = db.getY_values();
      else if( field == 1 && err )
         yvals = db.getErrors();
      else
         return new Integer(Grp);//returns group index instead of time index

      
      return SumVals(x_scale,db.getX_scale(),Time, yvals);
      /*if( index < 0 )
         return new Integer( 0 );
      else if( yvals == null)
         return new Integer( 0 );
      else if( index >= yvals.length )
         return new Integer( 0 );
      else
         return( new Float( yvals[index] ) );
     */
   }

  /**
  *     Integrates with interpolation all the yvalues in the time slice of
  *     x_scale containing the time Time. dbX_scale give the xvalues corresp
  *     to yvals
  */
  public Object SumVals( XScale x_scale, XScale dbX_scale, float Time, float[] yvals){
      if( yvals == null)
         return "";
      int index = Arrays.binarySearch( x_scale.getXs(), Time);
      if( index < 0) index = -index-1-1;
      if( index < 0) return "";
      if( index +1 >= x_scale.getNum_x()) return "";
      int index_y = Arrays.binarySearch( dbX_scale.getXs(), x_scale.getX(index));
      if( index_y < 0) index_y = -index_y -2 ;
      if( index_y < 0) index_y = 0;
      if( index_y  >= dbX_scale.getNum_x())
          return "";
      float V = 0;
      float a = x_scale.getX(index);
      float b = x_scale.getX(index+1);
      for( int i= index_y ; (i < dbX_scale.getNum_x())&&(dbX_scale.getX(i) <= b); i++){
          float p = 1;
          if( dbX_scale.getX(i) < a)
            p =(dbX_scale.getX(i+1) - a) /(dbX_scale.getX(i+1) - dbX_scale.getX(i));
          if( i+1 >= dbX_scale.getNum_x()){
            p=0;
          }
          else if( dbX_scale.getX(i+1) > b)
             p *=(b - dbX_scale.getX(i))/
                 (dbX_scale.getX(i+1) - dbX_scale.getX(i));
          if( i < yvals.length)
            V += yvals[i]*p;
          if( bugg) System.out.println("99Val ="+V+","+i+","+yvals[i]);
      }
      return new Float(V); 

  }
  public void setXScale( XScale xscale)
    { x_scale = xscale;
     }
   String filename = null;
   public void actionPerformed( ActionEvent evt )
   {
      JFileChooser jf;

      if( filename == null )
         jf = new JFileChooser();
      else
         jf = new JFileChooser( filename );

      if( !( jf.showSaveDialog( null ) == JFileChooser.CANCEL_OPTION ) )
         try
         {
            filename = jf.getSelectedFile().toString();
            File ff = new File( filename );
            FileOutputStream fout = new FileOutputStream( ff );

            for( int i = 0; i < getRowCount(); i++ )
            {
               for( int j = 0; j < getColumnCount(); j++ )
                  fout.write( ( getValueAt( i, j ).toString() + "\t" ).getBytes() );
               fout.write( ( "\n" ).getBytes() );
            }

            System.out.println( "Through" );
            fout.close();
         }
         catch( Exception ss )
         {
            DataSetTools.util.SharedData.addmsg( "Save ERRor=" + ss );
         }
   }

   public JMenuItem[] getJMenuItems( int MenuTitle)
     {return null;}
   public JComponent[] getControlComponents()
      {return null;}

   public void addActionListener( ActionListener list)
     {}
   public void removeActionListener( ActionListener list)
    {}
   public void SetUpDetNums(){
      DetNums = Grid_util.getAreaGridIDs( DS);
      if( DetNums != null)
        if( DetNums.length >0)
          DetNum = DetNums[0];
        else{
          DetNum = -1;
          DetNums = null;
          return;
        }
       else{
         DetNum = -1;
         return;
       }
         

      grid = (UniformGrid)(Grid_util.getAreaGrid( DS, DetNum));
      grid.setDataEntriesInAllGrids(DS);
      SetUpGroups();
   }
   public void SetUpGroups(){
      num_rows = grid.num_rows();
      num_cols = grid.num_cols();
      Groups = new Data[ 1+ num_rows][1+num_cols];
      for( int row = 1; row <= num_rows; row++)
        for( int col = 1; col <= num_cols; col++)
           Groups[row][col] = grid.getData_entry(row,col);

   } 

  LabelCombobox  DetChoices = null;
  public JComponent[] getControls(){
    if( DetNums == null)
      return new JComponent[0];
    if( DetNums.length <2)
      return new JComponent[0];
    if( DetChoices == null){
      String[] choices = new String[ DetNums.length];
      for( int i =0; i< choices.length; i++)
        choices[i] = ""+DetNums[i];
      DetChoices = new LabelCombobox("Detectors", choices);
      DetChoices.cbox.addActionListener( new DetectorActionListener());
    }
    
    JComponent[] Res = new JComponent[1];
    Res[0] = DetChoices;
    return Res;
  }
  ActionListener DataChangeListener = null;
  public void addDataChangeListener( ActionListener listener){
    DataChangeListener = listener;
  }

   
  public int getDetNum(){
    return DetNum;
  } 


   public void setDetNum( int DetNum){
      //Check to see if it is there
      if( DetNum < 0)
        return;
      grid = (UniformGrid)Grid_util.getAreaGrid( DS, DetNum);
      if( grid == null)
         return;
      this.DetNum = DetNum;
      MaxRow = grid.num_rows();
      MaxCol = grid.num_cols();
      SetUpGroups();
      tMinrow = 0;
      tMaxrow = MaxRow - 1;
      tMincol = 0;
      tMaxcol = MaxCol - 1;
      if( DataChangeListener != null)
      DataChangeListener.actionPerformed( new ActionEvent(this,
          ActionEvent.ACTION_PERFORMED,"DataChange"));

  }
  class DetectorActionListener implements ActionListener{
    public void actionPerformed( ActionEvent evt){
    int choice= DetNum;
    try{
       choice = (new Integer( (String)DetChoices.cbox.getSelectedItem())).
                 intValue();
    }catch( Exception ss){};
    if( choice != DetNum){
      DetNum = choice;
      grid = (UniformGrid)Grid_util.getAreaGrid( DS, DetNum);
      grid.setDataEntriesInAllGrids( DS );
      MaxRow = grid.num_rows();
      MaxCol = grid.num_cols();
      SetUpGroups();
      tMinrow = 0;
      tMaxrow = MaxRow - 1;
      tMincol = 0;
      tMaxcol = MaxCol - 1;
      if( DataChangeListener != null)
      DataChangeListener.actionPerformed( new ActionEvent(this,
          ActionEvent.ACTION_PERFORMED,"DataChange"));
    }
   } 

  }//class DetectorActionListener
  

   /** Test program for this module */
   public static void main( String args[] )
   {
      DataSet[] DSS;

      if( args == null )
      {
         System.out.println( "Enter Filename and Time" );
         System.exit( 0 );
      }
      if( args.length < 1 )
      {
         System.out.println( "Enter Filename and Time" );
         System.exit( 0 );
      }
      DSS = ( new IsawGUI.Util() ).loadRunfile( args[0] );
      if( DSS == null )
      {
         System.out.println( "Could not load " + args[0] );
         System.exit( 0 );
      }
      float Time;
      int k = DSS.length - 1;

      if( k < 0 ) k = 0;
      float[] xvals = DSS[k].getXRange().getXs();

      if( args.length > 1 )
         Time = ( new Float( args[1] ) ).floatValue();
      else
      {
         int j = xvals.length / 2;

         // System.out.print("j="+j+":");
         Time = xvals[j];
      }

      Time_Slice_TableModel tbmod = new Time_Slice_TableModel( DSS[k], Time, false, false );

      tbmod.setRowRange( 2, 6 );
      tbmod.setColRange( 2, 10 );

      JTable jtab = new JTable( tbmod );

      jtab.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

      JFrame jf = new JFrame( "Test" );

      jf.setSize( 400, 500 );
      jf.getContentPane().add( new JScrollPane( jtab ) );
      JMenuBar jmbar = new JMenuBar();
      JMenu jmen = new JMenu( "Options" );
      JMenuItem jmenItem = new JMenuItem( "Save" );

      jmenItem.addActionListener( tbmod );
      jmen.add( jmenItem );
      jmbar.add( jmen );
      jf.setJMenuBar( jmbar );
      JMenu TimeMenu = new JMenu( "Time:" + Time );

      jmbar.add( TimeMenu );
      jf.validate();
      jf.show();

   }

}
