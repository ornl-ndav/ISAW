/*
 * File:  NxWriteMonitor.java 
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
import DataSetTools.math.*;
import NexIO.*;

/** A Class responsible for writing the NXmonitor data to a Nexus file
 */
public class NxWriteMonitor
{   String errormessage;
    NxNodeUtils nu;

    public NxWriteMonitor()
      {errormessage = "";
       nu = new NxNodeUtils();
      }

    /**Returns an errormessage or "" if none
   */
   public String getErrorMessage()
    {return errormessage;
    
    }

   /** Writes the information from the specified datablock to a NXMonitor
   *  section of a Nexus file.
   *@param node  a NXmonitor node
   *@param DS    the data set with the information to be saved
   *@param datablock the index of the Data block monitor
   */
   public boolean processDS(  NxWriteNode node , DataSet DS , int datablock )
    {int ndatablocks , 
         rank1[] , 
         intval[];

     rank1 = new int[ 1 ];
     intval = new int[ 1 ];
     errormessage = "Null inputs to Monitor";
     if( node == null )
       return true;
     if( DS == null )
       return true;
     errormessage = "No data Block";
     ndatablocks = DS.getNum_entries();
     if( datablock < 0 )
        return true;
     if( datablock >= ndatablocks )
        return true;
     errormessage = "";
     int i = datablock;
      
     NxWriteNode n1 = node.newChildNode( "time_of_flight" , "SDS" );
     String units , 
            longname;
     units = DS.getX_units();
     longname = DS.getX_label();
     if( units != null )
       {rank1[ 0 ]= units.length() + 1;
        n1.addAttribute( "units" , ( units + 0 ).getBytes() , 
                                     Types.Char , rank1 );
       }
     if( longname != null )
       {rank1[ 0 ]= longname.length() + 1;
        n1.addAttribute( "long_name" , ( longname + 0 ).getBytes() , 
                                     Types.Char , rank1 );
       } 
     rank1[ 0 ]= 1;
     intval[ 0 ]= 1;
     n1.addAttribute( "axis" , intval , Types.Int , rank1 ); 

     Data DB = DS.getData_entry( i );
     XScale X = DB.getX_scale(); 
     float[] xvals ;
     xvals = X.getXs();
     rank1[ 0 ] = xvals.length;
     n1.setNodeValue( xvals , Types.Float , rank1 );
     
     /*X = DB.getX_scale(); 
     xvals = new float[ 0 ];
     xvals = X.getXs();
     rank1[ 0 ] = xvals.length;
     n1.setNodeValue( xvals , Types.Float , rank1 );
   */

     NxWriteNode n2 = node.newChildNode( "data" , "SDS" );
     units = DS.getY_units();
     longname = DS.getY_label();
     if( units != null )
       {rank1[ 0 ] = units.length( ) + 1;
        n2.addAttribute( "units" , ( units + 0 ).getBytes() , 
                                Types.Char , rank1 );
       }
     if( longname != null )
       {
        byte LN[];
        LN = ( longname + 0 ).getBytes();
        rank1[ 0 ] = LN.length;       
        n2.addAttribute( "long_name" , LN , Types.Char , rank1 );
       } 

      rank1[ 0 ] = 1;
      intval = new int[ 1 ];
      intval[ 0 ] = 1;
      n2.addAttribute( "signal" , intval , Types.Int , rank1 ); 
      xvals = new float[ 0 ];
      xvals = DB.getY_values();
      rank1[ 0 ] = xvals.length;
      
      n2.setNodeValue( xvals , Types.Float , rank1 );
      NxWriteDetector.SetUpIsawAttributes( node , datablock, datablock+1,
             DS);
          
      return false;
  //Common code with NxDetector
  /*    DetectorPosition DP = 
               ( DetectorPosition )DB.getAttributeValue( 
                                      Attribute.DETECTOR_POS  );
      if( DP != null )
       {float coord[];
        NxWriteNode n3 = node.newChildNode( "distance" , "SDS" );
        coord = DP.getSphericalCoords();
        float ff[] ;
        ff = new float[ 1 ];
        ff[ 0 ] = coord[ 0 ];
        rank1[ 0 ] = 1;
        n3.setNodeValue( ff , Types.Float  , rank1 );    
        coord = Types.convertToNexus( coord[0] , coord[2], coord[1] ); 
        ff = new float[ 1 ];
        ff[ 0 ] = coord[ 1 ]; 
        n3.addaAttribute( "phi" ,ff, Types.Float , rank1 );
        ff = new float[ 1 ];
        ff[ 0 ] = coord[ 2 ];
        n3.addaAttribute( "theta" ,ff, Types.Float , rank1 );
         
       } 
     
    //get distance, tof arrays, etc. for this Monitor dataset

      // Group_id
     
      Object O = DB.getAttributeValue( Attribute.GROUP_ID );
      System.out.println("TRYING to add group_id"+O.getClass());
      if( O instanceof Integer)
        { int group_id[], rank[];
          group_id =new int[1];
          group_id[0] = ((Integer) O).intValue();
          rank = new int[1];
          rank[0] = 1;
          n2.addAttribute("group_id", group_id, Types.Int , 
                         rank );
          
        }
     */
   
    }

}





