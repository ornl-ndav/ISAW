
/*
 * File:  INexUtils.java
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.1  2003/11/16 21:47:21  rmikk
 * Initial Checkin
 *
 */

package NexIO.Util;
import NexIO.*;
import NexIO.State.*;
import DataSetTools.dataset.*;


/**
 *   This Interface gives the required methods that implementers who need 
 *   non-standard processing of data in some of the NexIO.Process classes 
 *   must implement. Query methods will(in the future) to get different 
 *   INexUtils to plug into a Processor.  These should eliminate rewriting 
 *   new processors when subprocessing is all that is different.
 */  
public interface INexUtils{
    
 
  /**
    *  Currently invoked by the standard Process1Nxdata. It adds all the 
    *  data from the data field of NXdata to the DataSet DS using default
    *  GroupID's.  Units are changed to ISaw units
    *  @param DS  the DataSet that is having information added to
    *  @param NxDataNode The NxNode containing information about an NeXus
    *                      NXdata class
    *  @param startGroupID  the default GroupID for the first new DataBlock 
    *                       added by this method
    *  @param States   The linked list of state information
    */
  public boolean setUpNxData( DataSet DS, NxNode NxDataNode, 
          int startGroupID,  NxfileStateInfo States);


  /**
    *  Currently invoked by the standard Process1Nxdata. It adds all the 
    *  data from the data field of NXdetector to the DataSet DS 
    *  @param DS  the DataSet that is having information added to
    *  @param NxDataNode The NxNode containing information about an NeXus
    *                      NXdata class
    *  @param NxDetector The NxNode containing information about an NeXus
    *                      NXdetector class
    *  @param startDSindex  the first GroupIndex to process
    *  @param States   The linked list of state information
    */
  public  boolean setUpNXdetectorAttributes( DataSet DS,NxNode NxDataNode, 
       NxNode NxDetector, int startDSindex, NxfileStateInfo States);


      
  /**
    *  Currently invoked by the standard ProcessNXentry. It adds all the 
    *  data the NXmonitor class to the DataSet DS 
    *  @param DS  the DataSet that is having information added to
    *  @param NxMonitorNode The NxNode containing information about an NeXus
    *                      NXmonitor class
    *  @param startDSindex  the first GroupIndex to process
    *  @param States   The linked list of state information
    */
  public  boolean setUpNXmonitorAttributes( DataSet DS, NxNode NxMonitorNode, 
      int startDSindex, NxfileStateInfo States);
  /**
  *   Does set up assuming separate positions.
  *   Subclasses have to redo this for different ways of specifying detector positions
  */
 
  //public void setUpNX...Attributes, where ...=Beam,Sample, etc.

    

    
}
