
/*
 * File:  NxDataStateInfo.java
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
 * Revision 1.2  2003/12/08 17:28:23  rmikk
 * Eliminated a debu print
 *
 * Revision 1.1  2003/11/16 21:42:25  rmikk
 * Initial Checkin
 *
 */

package NexIO.State;
import NexIO.*;
import NexIO.Util.*;

/**
 *   This class contains state information needed to process an NXdata entry
 *   of a NeXus file
 */ 
public class NxDataStateInfo extends StateInfo{
  /**
   *   Contains the names of the axes with axis attributes 1 to 4.
   *   This field is initialized to an array of 4 null strings
  */
  public String[] axisName; 

  /**
   *   The dimensions of the data entry as given by the NeXus file.
   *   The first element of the array is the number of values associated 
   *   with the slowest changing attribute.  The last item is the number
   *   of time_of_flight data( +1 if it is a histogram)
   */
  public int[] dimensions;

  /**
   *   The name of the NXdata node
   */
  public String Name;

  /**
   *   The value of the label attribute on the data field of this NXdata. If 
   *   this is not null, this NXdata node will be merged with all other NXdata
   *   nodes in an NXentry with the same label attribute on their data field.
   */
  public String labelName;

  /**
   *   Determines whether the Datablock ID's are determined by the NXdetector.id
   *   if present or by the default GroupID.  Not used yet
   */
  public boolean hasIntIDField;

  /**
   *   The starting Default GroupID for the Data Blocks in this NXdata
   */
  public int startGroupID;

  /**
   *   The name of the corresponding NXdetector for this NXdata node. This
   *   comes from a link attribute on one of the axes.
   */
  public String linkName;

  /** 
   *   Constructor for the NxDataStatInfo.
   *   NOTE: Fields can be added to this structure, Other methods and/or
   *         constructors can be introduced, or subclassing can be used to
   *         account for variabilities
   *   @param  NxDataNode  An NxNode containing information on a NeXus NXdata 
   *                       class
   *   @param  NxInstrumentNode An NxNode containing information on a NeXus  
   *                       NXinstrument class
   *   @param  Params   a linked list of State information
   *   @param startGroupID the default starting ID for the data blocks. This is
   *                       used if there is NO int id field in the corresponding
   *                       NXdetector 
   */
  public NxDataStateInfo( NxNode NxDataNode, NxNode NxInstrumentNode, 
                NxfileStateInfo Params, int startGroupID ){
     Name = NxDataNode.getNodeName();
     axisName = new String[4];
     axisName[0] = axisName[1]= axisName[2] = axisName[3] = null;
     labelName = null;
     dimensions = null;
     hasIntIDField = false;
     this.startGroupID = startGroupID;

     for( int i = 0; i < NxDataNode.getNChildNodes(); i++){
        NxNode child = NxDataNode.getChildNode( i);
        if( child.getNodeClass().equals("SDS")){
           int axNum = ConvertDataTypes.intValue( child.getAttrValue("axis"));
           if( (axNum >=1) &&( axNum < 4))
              axisName[axNum-1] = child.getNodeName();

           if( child.getNodeName().equals("data")){
              dimensions = child.getDimension();
              labelName = ConvertDataTypes.StringValue( child.getAttrValue("label"));
           }
           String L = ConvertDataTypes.StringValue(child.getAttrValue("link")); 
          
           if( L != null){
              linkName = L;
           } 
           if( child.getNodeName().equals("id")){
               Object O =child.getNodeValue();
               if( O != null)
                 if( O instanceof int[])
                    hasIntIDField = true;

           }

        }//Node Class is SDS  

     }
      
  }
  
}


