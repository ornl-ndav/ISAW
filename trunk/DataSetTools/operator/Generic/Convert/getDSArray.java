/*
 * File:  getDSArray.java 
 *             
 * Copyright (C) 2005, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2005/01/07 20:00:24  rmikk
 * Made loop more efficient
 *
 * Revision 1.1  2005/01/06 15:47:01  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator.Generic.Convert;

import DataSetTools.operator.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.Util.SpecialStrings.*;
/**
 * This wrappable operator creates an array out of the y values from a
 * dataset. It has an option to create this array for FORTRAN jni programs
 *
 */
public class getDSArray implements Wrappable{

   /**
    *  The Data Set with the y values
    */
   public DataSet  DS;
   
   
   /**
    *  The detector ID from which to get the y value. The result will
    *  be a 3 dimensional array with dimensions of row, col, and time 
    */
   public int  DetectorId =-1;
   
   /**
    *   If false, a multidimensional Java float array is returned with dimensions
    *      row, col, time
    *   If true, a one dimension float array will be returned. The fastest
    *     changing dimension is time then col then row
    */
   public boolean FortArray = false;
   
   
   /**
    *  Returns getDSArray, the name used to invoke this operator in scripts
    * @return the name that invokes this operator in scripts
    */
   public String getCommand(){
     return "getDSArray";
   }
   
   
   /**
    *   Extracts the appropriate y values from the data set and places them
    *   in an array.  Either a 3 dimensional FORTRAN array or a 1-D
    *   Fortran array in Fortran order
    */
   public Object calculate(){
     
     IDataGrid grid = null;
     if( DetectorId < 0){
       int[] ids = NexIO.Write.NxWriteData.getAreaGrids( DS );
       if( (ids == null) ||(ids.length < 1))
         return new ErrorString("No Area Grids in DataSet");
      DetectorId = ids[0]; 
     }
     
     grid = NexIO.Write.NxWriteData.getAreaGrid( DS, DetectorId);
     if( grid == null)
       return new ErrorString( "Detector not found");
       
     int nrows = grid.num_rows();
     int ncols =grid.num_cols() ;
     if( !grid.isData_entered())
         grid.setData_entries( DS );
         
     int ntimes = grid.getData_entry(1,1).getY_values().length;
     float[][][] data = null ;
     float[]fdata = null;
     if( FortArray)
        fdata =new float[ncols*nrows*ntimes];
     else
        data = new float[nrows][ncols][ntimes];
        
     int k=0;
     for( int i=0; i< nrows; i++)
        for( int j = 0; j < ncols; j++){

          Data D= grid.getData_entry(i+1,j+1);
          if( D.getY_values().length != ntimes)
            return new ErrorString("Not All Spectra have the same # of times"); 
          float[] V = D.getY_values();
          
          for(int t=0; t < ntimes; t++ ){
          
             if( FortArray )// C array??
                fdata[k++] = V[t];
             else
                data[i][j][t] = V[t];
          
           }
        }   
     if( data != null)
       return data;
     else return fdata;
   }
   
   
   public String getDocumentation(){
     StringBuffer s = new StringBuffer();

            s.append("@overview  This wrappable operator creates an array out");
            s.append(" of the y values from a dataset. It has an option to ");
            s.append("create this array for FORTRAN jni programs");
            s.append("@param DS  The Data Set with the y values that are to ");            
            s.append("be put in the resultant array");
            s.append("@param DetectorId  The detector ID from which to get ");
            s.append("the y value. The result will be a 3 dimensional array with ");
            s.append("dimensions of row, col, and time ");  
 
            s.append("@param FortArray  If false, a multidimensional Java ");
            s.append("float array is returned with dimensions row, col, time");
            s.append("If true, a one dimension float array will be returned. The ");
             s.append("fastest changing dimension is time then col then row");
   
            s.append("@return The array in the proper format for one detector");
       
            return s.toString();

   }

}
