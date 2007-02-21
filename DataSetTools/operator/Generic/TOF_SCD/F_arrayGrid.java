/*
 * File:  F_arrayGrid.java 
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
 * Revision 1.1  2007/02/21 20:36:34  rmikk
 * Abstraction for x,2,and z information one detectorin the form of a 3D
 * Fortran array
 *
 * 
 * */
package DataSetTools.operator.Generic.TOF_SCD;

/**
 * This class implements the IGrid3D from a 3D
 *   F array [row][col][time chan]. 
 *   Assumes a 1D array where fastest changing is row , 
 *   next is col and last is channel
 * @author Ruth
 *
 */
public class F_arrayGrid implements IGrid3D {

// assumes fastest changing is roq, next is col and last is channel
  
      
      float[] array;
      int nrows, ncols, nChans;
      public F_arrayGrid( float [] array,int nrows,int ncols,int nChans){
         this.array = array;
         this.nrows = nrows;
         this.ncols = ncols;
         this.nChans = nChans;
      }
      public int num_rows(){
        return Math.max( 0, nrows);
      }
      
      public int num_cols(){
        
        return Math.max(0, ncols);
      }
      public int num_channels( int row, int col){
        return Math.max(0, nChans);
      }
      
      /**
       * returns Float.NaN if not legitimate entries
       */
      public float intensity( int row, int col, int timeChan){
         
        if( row < 1 ) return Float.NaN;
        if( col < 1)  return Float.NaN;
        if( timeChan < 0)return Float.NaN;
        if( row > num_rows()) return Float.NaN;
        if( col > num_cols() )return Float.NaN;
        if( timeChan >= num_channels(row , col ))return Float.NaN;
        if( array ==null) return Float.NaN;
        if( array.length >= row-1 +nrows*(col-1)+ nrows*ncols*(timeChan))
           return Float.NaN;
        return array[ row-1 +nrows*(col-1)+ nrows*ncols*(timeChan)];
         
      }
  


   /**
    * @param args
    */
   public static void main( String[] args ) {

      // TODO Auto-generated method stub

   }

}
