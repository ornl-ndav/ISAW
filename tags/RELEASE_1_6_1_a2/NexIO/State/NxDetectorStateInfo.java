
/*
 * File:  NxDetectorStateInfo.java
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
 * Revision 1.1  2003/11/16 21:43:03  rmikk
 * Initial Checkin
 *
 */

package NexIO.State;
import NexIO.*;

/**
 *   This class contains state information needed to process an NXdetector 
 *   entry of a NeXus file
 */ 
public class NxDetectorStateInfo extends StateInfo{
  /**
   *   Determines whether the GroupIDs should be changed to values
   *   in the NXdetector.id field
   */
  public boolean hasIntIDs;
  
  /**
   *  The dimensions of the distance, azimuthal_angle, polar_angle and 
   *  solid_angle fields of the NXdetector.  These should all be the same
   */
  public int[] distance_dimension, 
      azimuthal_dimension, 
      polar_dimension,
      solidAngle_dimension;
  /**
   *  The name of this NXdetector
   */
  public String Name;

  /**
   *   Constructor
   *  @param NxDetectorNode  the NxNode containing information on the NeXus 
   *                           NXdetector class
   *  @param  Params  the link list of state information that can be used to
   *                  calculate various quantities
   */
  public NxDetectorStateInfo( NxNode NxDetectorNode, 
                NxfileStateInfo Params ){
     if( NxDetectorNode == null){
        distance_dimension=azimuthal_dimension=polar_dimension=
               solidAngle_dimension=null;
        hasIntIDs= false;
        Name = null;
     }
     Name = NxDetectorNode.getNodeName();
     NxNode node = NxDetectorNode.getChildNode( "id");
     hasIntIDs = false;
     if( node != null){
        Object O = node.getNodeValue();
        if( O != null)
           if( O instanceof int[])
              hasIntIDs = true;
     } 

     distance_dimension = findDataDimension( NxDetectorNode, "distance");
     azimuthal_dimension = findDataDimension( NxDetectorNode, "azimuthal_angle");
     polar_dimension = findDataDimension( NxDetectorNode, "polar_angle");
     solidAngle_dimension = findDataDimension( NxDetectorNode, "solid_angle");

     


   }//Constructor

  // Gets the NeXus dimension fo the given node
  private int[] findDataDimension( NxNode NxDetectorNode, String fieldName){
     NxNode node =NxDetectorNode.getChildNode( fieldName);
     if( node == null){
        int[] Res = new int[1];
        Res[0]=0;
        return Res;
     }
     
     return node.getDimension();

  }
 }


