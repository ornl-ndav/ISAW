
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
 * Revision 1.1  2002/02/27 16:48:50  rmikk
 * Initial Checkin
 *
*/
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

/** Creates a table model the displays y values at a fixed time according to row
* and column values of the group
*/
public class Time_Slice_TableModel extends AbstractTableModel 
                                  implements ActionListener
 {int MaxRow , MaxCol;
  float Time;
  int[] RC_to_Group;
  DataSet DS;

   /** Constructor for this table model of the Data Set DS at time time
   *@param  DS  the data set for which the model will present the data
   *@param  time  the time of this time slice
   */
   public Time_Slice_TableModel( DataSet DS , float time )
     {Time = time;
      int[] row = new int[DS.getNum_entries()];
      int[] col = new int[DS.getNum_entries()];
      Time = time;
      this.DS = DS;
      MaxRow = -1;
      MaxCol =  -1;
      for( int i = 0 ; i < DS.getNum_entries() ; i++ )
        { DetInfoListAttribute Ax =( DetInfoListAttribute ) DS.getData_entry( i ).
                                      getAttribute( Attribute.DETECTOR_INFO_LIST );
          
          row[i] = -1;
          col[i] = -1;
          if( Ax == null )
            {}
          else
            { DetectorInfo[] Ab = ( DetectorInfo[] )( Ax.getValue() );
              if( Ab != null )
                if( Ab.length > 0 )
                { DetectorInfo B = Ab[0];
                  row[i] = B.getRow();
                  col[i] = B.getColumn();
                  if(  row[i] > MaxRow ) 
                     MaxRow = row[i];
                  if( col[i] > MaxCol ) 
                     MaxCol = col[i];
                 }
             }
          
            
         }
       if( (MaxCol < 0 ) || ( MaxRow < 0 ) ) 
            {RC_to_Group = null;
             return;
            }

       RC_to_Group = new int[( MaxRow + 1 ) * ( MaxCol + 1 )];
       Arrays.fill( RC_to_Group , -1 );
       for( int i =  0 ; i < row.length ; i++ )
          {int r = row[i];
           int c = col[i];
           if( r >= 0 )
             if( c >= 0 )
               RC_to_Group[( r ) * ( MaxCol + 1 ) + ( c )] = i;

           }
     }
   


 /** Used to set a new time for this time slice.
 */
   public void setTime( float time )
     {Time = time;

      }


  /** Returns the column name for a column,  In this case it is C # where # is the column
  *  number
  */
   public String getColumnName( int column )
     { 
        return "C" + column;
      }

   /** returns the number of rows
   */
   public int getRowCount()
       { 
        if( MaxRow < 0 ) 
           return 0;
         else 
           return MaxRow;
       }

  /** Returns the number of Columns
  */
  public int getColumnCount()
     {
      if( MaxCol < 0 )
         return 0;
      else 
         return MaxCol;
      }

  /** Returns the y value of the data set associated with the row and column at the time
  * that has been set
  */
  public Object getValueAt( int row , int column )
     {  if( Time < 0 )
           return "";
       
        if( row < 0 )
           return "";

       if( row >= MaxRow ) 
          return "";
       if( column < 0 )
            return "";
       if(column >= MaxCol ) 
             return "";
       int Grp = RC_to_Group[row * ( MaxCol + 1 ) + column];
       
       if( Grp < 0 ) 
            return "";
       if( Grp >= DS.getNum_entries() ) 
            return "";

       float[] xvals = DS.getData_entry( Grp ).getX_scale().getXs();
       int index = Arrays.binarySearch( xvals , Time );
      
       float dx = ( xvals[1] - xvals[0] ) / 10.0f;
       if( index >= xvals.length )
         if( ( Time - xvals[xvals.length - 1] ) <
             .1 * ( xvals[xvals.length - 1] - xvals[xvals.length - 2]  ) )
            index = xvals.length - 1;
          else 
             index = -1;
        else if( java.lang.Math.abs( xvals[index] - Time ) < .1 *  dx )
           {}
        else 
           index = -1;
      
       float[] yvals = DS.getData_entry( Grp ).getY_values();
       if( index < 0 ) 
           return "";
       else if( index >= yvals.length ) 
           return "";
       else 
          return ( new Float( yvals[index] ).toString() );

      }
  String filename = null ;
  public void actionPerformed( ActionEvent evt)
    {JFileChooser jf ;
     if( filename == null )  
        jf = new JFileChooser();
     else 
        jf = new JFileChooser( filename );

    if( !( jf.showSaveDialog( null ) == JFileChooser.CANCEL_OPTION ) )
      try
       {filename = jf.getSelectedFile().toString();
        File ff = new File( filename );
        FileOutputStream fout = new FileOutputStream( ff );
        for( int i = 0; i < getRowCount(); i++ )
         {for( int j = 0; j < getColumnCount(); j++ )
            fout.write( ( getValueAt( i , j ).toString() + "\t" ).getBytes() );
          fout.write( ( "\n" ).getBytes() );
          }
    
        System.out.println( "Through" );
        fout.close( ); 
       }
      catch( Exception ss){ DataSetTools.util.SharedData.status_pane.add("Save ERRor="
                    +ss);
                           }
   }
  /** Test program for this module */
  public static void main( String args[] )
    { DataSet[] DSS;
      if( args == null )
        {System.out.println( "Enter Filename and Time" );
         System.exit( 0  );
         }
       if( args.length < 1 )
        {System.out.println( "Enter Filename and Time" );
         System.exit( 0 );
         }
      DSS = ( new IsawGUI.Util()).loadRunfile( args[0] );
      if( DSS == null )
         {System.out.println( "Could not load " + args[0] );
         System.exit( 0 );
         }
       float Time;
       int k = DSS.length-1;
       float[] xvals = DSS[k].getXRange().getXs();
       
       if( args.length >1)
          Time = ( new Float( args[1])).floatValue();
       else
           { 
             int j = xvals.length/2;
             Time = xvals[j];
            } 

       Time_Slice_TableModel tbmod = new Time_Slice_TableModel( DSS[k] , Time );
       //System.out.println( tbmod.getRowCount() + " , " + tbmod.getColumnCount() + "#Row-col" );
       JTable jtab = new JTable( tbmod );
       jtab.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
 

        JFrame jf = new JFrame( "Test" );
        jf.setSize( 400 , 500 );
        jf.getContentPane().add( new JScrollPane( jtab  ) );
        JMenuBar jmbar= new JMenuBar();
        JMenu jmen = new JMenu("Options");
        JMenuItem jmenItem= new JMenuItem("Save");
        jmenItem.addActionListener( tbmod );
        jmen.add( jmenItem );
        jmbar.add( jmen);
         jf.setJMenuBar( jmbar );
         JMenu TimeMenu= new JMenu("Time:"+Time);
         jmbar.add(TimeMenu);
        jf.validate();
        jf.show();
     
     }


 
  }
