
/*
 * File:  Print4Col2D1Chan.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/10/22 20:06:00  rmikk
 * Fixed javadoc error
 *
 * Revision 1.1  2003/08/15 19:34:43  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator.Generic.TOF_SAD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import java.io.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import java.util.*;
import DataSetTools.parameter.*;
import Command.*;

/**
*    Prints Elements in an detector array with only one time channel.
*    For each detector, its x position, y position(on array) yvalue and error
*    are printed to the file.
*/ 
public class Print4Col2D1Chan extends GenericTOF_SAD{


   /**
   *   Default Constructor. Sets the Title to "Print2D"
   */
   public Print4Col2D1Chan(){
     super("Print2D");
   }

   /**
   *      Constructor
   *   @param DS  the DataSet to be saved in 4 Col format
   *   @param filename  The name of the file to store this information
   */    
   public Print4Col2D1Chan( DataSet DS, String filename){
     this();
     parameters= new Vector();
     addParameter( new DataSetPG( "DataSet",DS));
     addParameter( new SaveFilePG("Enter file name", filename));
   }

   public void setDefaultParameters(){
      parameters = new Vector();
     addParameter( new DataSetPG( "DataSet",null));
     addParameter( new SaveFilePG("Enter file name", null));
   }

   /**
   *     Saves the information in the DataSet to the file in the
   *     specified 4 Column format. Col 1 is y position(row), Col 2 is
   *     x positon(col), Col 3 is the y value and the last column contains
   *     the error associated with the detector.
   */
   public Object getResult(){
      DataSet DS = (DataSet)(getParameter(0).getValue());
      String filename = getParameter(1).getValue().toString();

      PixelInfoList ipxlist = (PixelInfoList)
                DS.getData_entry(0).getAttributeValue( Attribute.PIXEL_INFO_LIST);
      if( ipxlist  == null)
        return new ErrorString("Data Set has no pixel info list");
      IPixelInfo ipinf = ipxlist.pixel(0);
      if( ipinf == null)
        return new ErrorString("Data Set's pixel list is empty");
      IDataGrid grid = ipinf.DataGrid();
      if( grid == null)
        return new ErrorString("DataSet has no grid");
      ((UniformGrid)grid).setDataEntriesInAllGrids( DS);
      //Grid_util.setEffectivePositions( DS, grid.ID());
      FileOutputStream fout= null;
      try{
          fout = new FileOutputStream( filename);
      }catch( Exception ss){
        return new ErrorString("File open error="+ss.toString());
      }
      StringBuffer buff = new StringBuffer(8000);
      try{
      for( int row = 1; row <= grid.num_rows(); row++)
        for( int col=1; col<= grid.num_cols(); col++){
            Data D = grid.getData_entry( row,col);
            Vector3D pos = grid.position( row,col);
        //    float[] DetPos= ((DetectorPosition)D.getAttributeValue( 
        //          Attribute.DETECTOR_POS)).getCartesianCoords();
           
            buff.append(Format.real((double)pos.dot( new Vector3D(grid.y_vec(row,col))),9,5));

            buff.append( Format.real((double)pos.dot( new Vector3D(grid.x_vec(row,col))),14,5));
            buff.append("       ");
            buff.append( Format.singleExp((double)(D.getY_values()[0]),11));
            buff.append("       ");
            buff.append( Format.singleExp((double)(D.getErrors()[0]),11));
            buff.append("\n");    
            if( buff.length()>7700){
              fout.write( buff.toString().getBytes());
              buff.setLength(0);
           }
        }
      fout.write( buff.toString().getBytes());
      
       fout.close();
      }catch( Exception sss){}
      return null;
    }
   /**
   *    Shows how to invoke this from the command line.  This message is
   *    displayed when an improper number of arguments are given on the
   *    command line.
   */
   public static void showUsage( String prompt){
     System.out.println("");
     System.out.println("This program requires only one argument- the name");
     System.out.println("  of the file storing the Data Set.");
     System.out.println("");
     System.out.println(" The resultant file will be xxx.dat in the directory");
     System.out.println("   where this program was started"); 
      System.exit(0);
   }

   /**
   *       Starts a program that will take the name of a file storing a DataSet.
   *  args[0]  the name of the file storing the DataSet.
   *  @return  a file with the name xxx.dat in the directory where the programe
   *          was started. This file is in the 4 Col format
   */
   public static void main( String args[]){
     if( args == null)
       showUsage("args is null");
     if( args.length < 1)
       showUsage("not enough arguments");
     DataSet[]  DS = null;
     try{
       DS = ScriptUtil.load( args[0]);
     }catch( Exception sss){
       showUsage("Could not load data set");
     }
     if( DS == null)
       showUsage("No data sets loaded");
     System.out.println(" Result ="+
       ( new Print4Col2D1Chan( DS[0],"xxx.dat")).getResult());

   }
   public String getDocumentation(){
    StringBuffer Res = new StringBuffer();
    Res.append("@overview Prints Elements in an detector array with only one ");
    Res.append( "time channel. For each detector, its y(row) position, x position");
    Res.append("(on array), yvalue and error are printed to the file.");

    Res.append("@param DS - the Data Set to be saved");
    Res.append("@param filename- the name of the file to print to");
    Res.append("@return  null or an error message");
    Res.append("@error - DataSet has no Grid information");

    Res.append("@error - File could not be created ");
    Res.append("@error -  Other java.io errors in file writing/closing");
  
    return Res.toString();


  }
}//class Print4Col2D1Chan
