/*
 * File:  Nxlog.java 
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
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/11/16 21:35:00  rmikk
 * Initial Checkin
 *
 * Revision 1.4  2002/11/
 */
package NexIO;

import DataSetTools.dataset.*;


public class Nxlog{

  String errormessage;

  public Nxlog(){
    errormessage = "";
  }

  public String getErrorMessage(){
    return errormessage;
  }

  /**
    *  Returns the list of NxLog data sets
    *  @return NxNode[i] is the NxNode corresponding to the ith NXlog NeXus
    *               class which is converted to the ith log DataSet
    */
  public static DataSetInfo getNxLogDataSet( int i ){
     return null;

  }
   
  public static int getNumNxLogDataSets(){

     return 0;
  }

  public boolean processDS(  NxNode NxlogNode,  DataSet DS){
    errormessage = "Improper inputs to Nxlog";
    if( NxlogNode == null)
      return true;
    if( DS == null)
      return true;
    if( !NxlogNode.getNodeClass().equals( "NXlog"))
      return true;
    errormessage = "";
   
      
   
    return false;  
 }//processDS
}
