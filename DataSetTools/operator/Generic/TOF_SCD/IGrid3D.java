/*
 * File:  IGrid3D.java 
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
 * Revision 1.1  2007/02/21 20:34:27  rmikk
 * Abstraction for x,2,and z information one detector.  The information could
 *    be from a DataSet, 3D C-Array, or 3D Fortran array
 *
 * */
package DataSetTools.operator.Generic.TOF_SCD;

/**
 * This interface is a "shell" to replace DataSetTools.dataset.IDataGrid.
 * Code that replaces the IDataGrid with an IGrid3D and use and Implementer of this interface 
 * @see DataSetTools.dataset.IDataGrid
 * @author Ruth
 *
 */
public interface IGrid3D {
  
      public int num_rows();
      public int num_cols();
      public int num_channels( int row, int col);
      public float intensity( int row, int col, int timeChan);
  
}
