/*
 * File:  ArrayGrid.java 
 *             
 * Copyright (C) 2006, Ruth Mikkelson
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
 * * Modified:
 *
 * $Log$
 * Revision 1.1  2007/02/21 20:36:52  rmikk
 * Abstraction for x,2,and z information one detectorin the form of a 3D
 * java array
 *
 * */
package DataSetTools.operator.Generic.TOF_SCD;

/**
 * This class implements the IGrid3D interface using
 * a 3D java array [row][col][time channel]
 *
 */
public class ArrayGrid implements IGrid3D {

  
      
      float[][][] array;
      public ArrayGrid( float [][][] array){
         this.array = array;
      }
      public int num_rows(){
        if(array != null)
           return array.length;
        return 0;
      }
      
      public int num_cols(){
        if( array != null)
           if( num_rows() >0)
              return array[0].length;
        return 0;
      }
      public int num_channels( int row, int col){
         if( array==null)return 0;
         if( row<1 )return 0;
         if( col < 1) return 0;
         if( row> num_rows() )return 0;
         if( col > num_cols())return 0;
         return array[row-1][col-1].length;
      }
      public float intensity( int row, int col, int timeChan){
         
         if( array==null)return Float.NaN;
         if( row<1 )return Float.NaN;
         if( col < 1) return Float.NaN;
         if( timeChan < 0)return Float.NaN;
         if( row> num_rows() )return Float.NaN;
         if( col > num_cols())return Float.NaN;
         float[] chan= array[row-1][col-1];
         if( timeChan >= chan.length)
            return chan[timeChan];
         return Float.NaN;
         
      }
  
   /**
    * @param args
    */
   public static void main( String[] args ) {

      // TODO Auto-generated method stub

   }

}
