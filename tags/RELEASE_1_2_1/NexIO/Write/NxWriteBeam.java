/*
 * File:  NxWriteBeam.java 
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
 * Revision 1.1  2001/07/25 21:23:20  rmikk
 * Initial checkin
 *
 */


package NexIO.Write;
import DataSetTools.dataset.*;
import NexIO.*;
/** Handles writing information to the NxBeam portion of the Nexus Standard
*/
public class NxWriteBeam
{ String errormessage;

  public NxWriteBeam()
    {
      errormessage = "";
    }

/** Returns an error message or "" if there is no error
*/
  public String getErrorMessage()
   { 
     return errormessage;
   }

/** Extracts the information from the Data Set and writes it to
*   the NxBeam portion of a Nexus file
*@param  node    A node whose class is NXbeam
*@param  DS      The data set whose information is to be written
*/
  public boolean processDS( NxWriteNode node, DataSet DS )
   {errormessage = "Improper inputs to Write Beam ";
    if( node == null ) 
      return true;
    if( DS == null )
      return true;
    int rank[];
    float ff[];
    NxWriteNode n1;
    Object O = DS.getAttributeValue( Attribute.ENERGY_IN );
    if( O != null )
    {if( O instanceof Float )
      {float f = ( ( Float ) O ).floatValue();
       ff = new float[1];
       ff[0] = f;
       rank = new int[1];
       rank[0] = 1;
       n1 = node.newChildNode( "incident_energy","SDS" );
       n1.setNodeValue( ff, Types.Float, rank ); 
       if( n1.getErrorMessage() != "" );
         errormessage += ":"+n1.getErrorMessage();
      }  
    }
 //Energy out
    O = DS.getAttributeValue( Attribute.ENERGY_OUT );
    if( O != null )
     {if( O instanceof Float )
       {float f = ( ( Float )O ).floatValue();
        ff = new float[1];
        ff[0] = f;
        rank = new int[1];
        rank[0] = 1;
        n1 = node.newChildNode( "final_energy","SDS" );
        n1.setNodeValue( ff, Types.Float, rank ); 
        if( n1.getErrorMessage() != "" );
          errormessage += ":"+n1.getErrorMessage();
       }  
     } 
// duration 
    O = DS.getAttributeValue( Attribute.NUMBER_OF_PULSES );
    if( O != null )
     {if( O instanceof Number )
       {float f = ( ( Number )O ).floatValue();
        ff = new float[1];
        ff[0] = f/30.0f;
        rank = new int[1];
        rank[0] = 1;
        n1 = node.newChildNode( "duration" ,"SDS" );
        n1.setNodeValue( ff , Types.Float , rank ); 
        if( n1.getErrorMessage() != "" );
          errormessage += ":" + n1.getErrorMessage();
       }  
     } 
   return false;
   }//processDS


}//class NxWriteBeam
