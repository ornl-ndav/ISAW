/*
 * File:  NXdata_Fields.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Revision 1.3  2002/11/20 16:14:39  pfpeterson
 * reformating
 *
 * Revision 1.2  2001/07/24 20:03:54  rmikk
 * Added a field to processDS so it can get a handle on
 * fields linked to and with NxData
 *
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
 *
 */
package NexIO;

import DataSetTools.dataset.*;

/**
 * An Implementation of NxData where the names of the axes and data
 * fields in NxData are known.  (Only for 2 axes so far)
 */
public class NXdata_Fields  extends NXData_util implements NxData{
  String ax1,ax2,dat;
  String errormessage;
  
  /**
   *  Created with the known axes and data field names
   */
  public NXdata_Fields(String axis1name , String axis2name , String dataname ){
    ax1 = axis1name;
    ax2 = axis2name;
    dat = dataname;
    errormessage = "";
  }
  
  /**
   * Fills out an existing DataSet with information from the NXdata
   * section of a Nexus datasource
   *
   * @param node the current node positioned to an NXdata part of a
   * datasource
   * @param DS the existing DataSet that is to be filled out
   *
   * @return error status: true if there is an error otherwise false
   */
  public boolean processDS( NxNode node , NxNode nxInstr,DataSet DS ){
    errormessage = "";
    if(super.processDS(node, nxInstr, ax1, ax2, dat,DS))
      return true;
    errormessage = super.getErrorMessage();
    return false;
  }

  /**
   *Returns the error or warning message or "" if none
   */
  public String getErrorMessage(){
    return errormessage;
  }
}
