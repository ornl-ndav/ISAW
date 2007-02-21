
/*
 * File:  DSGrid.java 
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
 * Revision 1.1  2007/02/21 20:35:33  rmikk
 * Abstraction for x,2,and z information one detectorin the form of a DataSet.
 *
 * 
 * */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.Grid_util;
import DataSetTools.dataset.IDataGrid;

/**
 * This class implements the IGrid3D interface
 * using a Data Grid in a DataSet
 * @author Ruth
 *
 */
public class DSGrid implements IGrid3D {

   DataSet DS;
   int DetID;
   public IDataGrid grid;
   public DSGrid( DataSet DS, int DetID){
      this.DS = DS;
      this.DetID = DetID;
      this.grid = Grid_util.getAreaGrid( DS , DetID );
   }
   
   
   public int num_rows(){
      if( grid == null)
         return 0;
      return grid.num_rows();
      
   }
   
   public int num_cols(){
     if( grid == null)
        return 0;
      return grid.num_cols();
   }
   public int num_channels( int row, int col){
      if( grid == null)
         return 0;
      if( row < 1) return -1;
      if( col < 1) return -1;
      if( row > grid.num_rows())return -1;
      if( col > grid.num_cols())return -1;
      Data D = grid.getData_entry( row, col);
      if( D == null)
         return -1;
      return D.getX_scale().getNum_x();
   }
   public float intensity( int row, int col, int timeChan){
      if( grid == null)
         return Float.NaN;
      if( row < 1) return Float.NaN;
      if( col < 1) return Float.NaN;
      if( timeChan < 0) return Float.NaN;
      if( row > grid.num_rows())return Float.NaN;
      if( col > grid.num_cols())return Float.NaN;
      
      Data D = grid.getData_entry( row, col);
      if( D== null)
         return Float.NaN;
      float[] y =D.getY_values();
      if( timeChan < y.length )
         
            return y[timeChan];
      return Float.NaN;
   }


   /**
    * @param args
    */
   public static void main( String[] args ) {

      // TODO Auto-generated method stub

   }

}
