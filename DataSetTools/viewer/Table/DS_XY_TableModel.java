
/*
 * File:  DS_XY_TableModel.java 
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
 * Revision 1.4  2002/06/10 22:33:42  pfpeterson
 * Now only creates one string buffer when writting.
 *
 * Revision 1.3  2002/06/10 21:46:07  rmikk
 * Optimized the save using StringBuffer's and the new
 *    methods in XScale to getX(i) and getI(x)
 *
 * Revision 1.2  2002/06/07 22:36:49  pfpeterson
 * Added some error checking and an option to specify the dataset
 * number when running the main program.
 *
 * Revision 1.1  2002/02/27 16:49:05  rmikk
 * Initial Checkin
 *
 *
*/
package DataSetTools.viewer.Table;
import java.awt.event.*;
import javax.swing.text.*;


import javax.swing.*;
import javax.swing.table.*;
import DataSetTools.dataset.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/** Produces a TabelModel that can be plugged into a JTable.  It has
* the following properites.<OL type ="a">
*<li> The table gives the x values versus the y values for a set of groups
*      then (optionally) the rrors for a set of groups.
*<li> This model is memory efficient.  The values displayed are retrieved from the
*     data set when the table manager asks for that informtion.
*<li> It is also an ActionListener.  When fired, it will save itself( slowly for large
*     data sets)
*/  

public class DS_XY_TableModel extends AbstractTableModel
                               implements ActionListener 
  {float xvalMin , 
         dx; 
         int[] Groups; 
   float[] xvals = null;
   DataSet DS;
   boolean includeErrors;

   /** Constructor for dS_XY_TableModel
   *@param  DS  the data set to be modeled
   *@param Groups  The list of groups to be displayed
   *@param includeErrors  if true the errors will be displayed too
   */
   public DS_XY_TableModel( DataSet DS , int[] Groups , boolean includeErrors )
     { super();
       this.Groups = Groups;
       this.DS = DS;
       float[] u= null;
       this.includeErrors = includeErrors;
       if( DS != null )
         { xvals = table_view.MergeXvals( 0, DS, u, false, Groups); 
           if( xvals.length>1 ) 
              dx = xvals[ 1 ] -  xvals[ 0 ];
           else 
               dx = 0;
           for( int i = 1; i + 1 < xvals.length; i++ )
             if( ( xvals[ i + 1 ] - xvals[ i ] ) < dx )
               dx = xvals[ i + 1 ] - xvals[ i ];
         }

      }

   /** Returns the number of rows 
   */
   public int getRowCount()
       {if( xvals == null ) 
            return 0;
        else
            return xvals.length;
        }

  /** Returns the number of columns
  */
  public int getColumnCount()
      {if( Groups == null )
             return 0;
       if( includeErrors )
            return 1 + 2 * Groups.length;
       else 
             return 1 + Groups.length;
       }

  /**Returns the value at row , column
  */
  public Object getValueAt( int row , int column )
      {// Bounds Checking
       if( row < 0 ) 
           return "";
       if( column < 0 )
            return "";
       if( row >= getRowCount() )
            return "";
       if( column >= getColumnCount() ) 
            return"";

        if( column == 0 )
              return new Float( xvals[ row ] );

        //Get x value that corresponds to row=row       
        float x = xvals[ row ];

        
        //Find which group corresponds to the given column
        int Group = column - 1;
        if( Group >= Groups.length ) 
             Group = Group - Groups.length;

         
        //Determine WHICH yvalue, its index,  or error is to be returned
        float[] Gxvals = DS.getData_entry( Group ).getX_scale().getXs();
        int p = java.util.Arrays.binarySearch( Gxvals , x );
        int index = -1;
        if( p <= 0 ) 
          if( java.lang.Math.abs( Gxvals[ 0 ]  - x ) < dx / 10.0 ) 
            index = 0;
          else
            {}
        else if( p>= Gxvals.length )
          if( java.lang.Math.abs( x - Gxvals[ Gxvals.length - 1 ] ) < dx / 10.0 ) 
             index = Gxvals.length - 1;
          else 
            {}
        else if( java.lang.Math.abs( Gxvals[ p ] - x ) < dx / 10.0 )
           index = p;
        else if( java.lang.Math.abs( x - Gxvals[ p - 1 ] ) < dx / 10.0 )
            index = p - 1;
        if( index  < 0 ) 
             return "";
       
          
        //Now return the appropriate value
        float vals[];
        if( !includeErrors ||(  column  < 1 + Groups.length ) )
           vals =  DS.getData_entry( Group ).getY_values();
        else
           vals = DS.getData_entry( Group ).getErrors();
        if( vals == null ) 
           return "";
        if( vals.length  <= index )
            return "";
     
        return new Float( vals[ index ] );
       }

  /** Returns the column (Header) name
  */
  public String getColumnName( int column )
    {if( column == 0 )
         return "X";
     if( column - 1 < Groups.length )
         return "Y:Gr" + Groups[ column - 1 ];
     else
         return "Er:Gr" + Groups[ column - 1 - Groups.length ];
   } 


 /** Saves the current table
 */
 String filename = null;
 public void actionPerformed( ActionEvent evt )
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
       
        StringBuffer S =new StringBuffer( 8192); 
        for( int i = 0; i < getRowCount(); i++ )
         {float x = xvals[ i ];
          
          for( int j = 0; j < getColumnCount(); j++ )
            {float v= x;
           
             String V="";
             if( j == 0)
                V +=x;
             else
              {Data DB;
               int jj =j-1;
               if( jj >= Groups.length)
                 jj = jj- Groups.length;
               DB = DS.getData_entry( Groups[jj] );
              
               XScale xs = DB.getX_scale();
              
               int indx = xs.getI(x);
              
               if( indx < 0)
                 indx = 0;
               if( indx > DB.getX_scale().getNum_x())
                 indx = DB.getX_scale().getNum_x();
             
               if( x > xs.getX(indx) +dx/20.0 )
                 indx++;
             
               if( xs.getX(indx) != Float.NaN)
                 if( x < xs.getX(indx)-dx/20.0 )
                    indx--;
             
               if( j == 0)
                 V =""+x;
               else if( xs.getX( indx) == Float.NaN)
                 {V="";}
               else if(x > xs.getX(indx) +dx/20.0 )
                 {
                 
                 V="";}
               else if(x < xs.getX(indx)-dx/20.0 )
                  {
                    V ="";}
             
               else 
                { float[] vals;
                  if( !includeErrors ||(  j  < 1 + Groups.length ) ) 
                    vals =  DB.getY_values();
                  else
                    vals = DB.getErrors();
                  if( vals == null ) 
                    {
                       V="";}
                  else if( vals.length  <= indx )
                    {
                         V="";}
                  else 
                    V+= vals[indx];
                }
              }
           
             S.append(V);//getValueAt( i , j ).toString());
             S.append ( "\t") ;
             
             }
          S .append( "\n");
         if( S.length() >6000)
            {fout.write( (S.substring(0) ).getBytes() );
             
             S.delete(0,S.length());
            }
          }
         fout.write( (S.substring(0)+ "\n" ).getBytes() ); 
        fout.close( ); 
        System.out.println( "Closed" );
 
        }
       catch( Exception ss ){
          DataSetTools.util.SharedData.status_pane.add( "Cannot Save " + 
                ss.getClass()+":"+ss );}
        
    
   }
/** Test program.  Have a run filename as the argument
*@param  the filename to test
*/
public static void main( String args[] ){
    String filename="";
    int k=-1;
    if( args.length==1){
        filename=args[0];
    }else if( args.length==2){
        filename=args[0];
        k=(new Integer(args[1])).intValue();
    }else{
        System.err.println("SYNTAX: DS_XY_TableModel <filename> [DataSetNumber]");
        System.exit(-1);
    }
  
  DataSet[] DSS = ( new IsawGUI.Util( ) ).loadRunfile( filename );
  if( DSS == null)
     {System.out.println("Error No Data Sets");
      System.exit(0);
      }
  if(k==-1) k= DSS.length-1;
  if(k>=0 && k>=DSS.length){
      System.err.println("ERROR: "+k+" must be less than "+DSS.length+" for "+filename);
      System.exit(-1);
  }
  int[] Groups = new int[ DSS[ k ].getNum_entries() ];
  for( int i = 0;i < Groups.length;i++ ) 
       Groups[ i ] = i;
   
  DS_XY_TableModel tbMod = new DS_XY_TableModel( DSS[ k ] , Groups ,true );
  
  JTable jtb = new JTable( tbMod );
  jtb.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

  JFrame jf = new JFrame( "Test" );
  jf.setSize( 400 , 400 );
  jf.getContentPane( ).add( new JScrollPane( jtb  ) );
  jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  JMenuBar Mbar = new JMenuBar( );
  JMenu Optmenu = new JMenu( "options" );
  
  JMenuItem save = new JMenuItem( "Save" );
  save.addActionListener(  tbMod );
   Optmenu.add( save );
   Mbar.add( Optmenu);
  jf.setJMenuBar( Mbar );
  jf.show();
  }
      
 }
