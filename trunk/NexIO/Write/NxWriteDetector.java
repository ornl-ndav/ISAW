/*
 * File:  NxWriteDettector.java 
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

/** A Class that is responsible for writing information from data sets
 *  to the NXdetector section of a Nexus File
 */
public class NxWriteDetector
{   String errormessage;
    String axis1Link; 
    String axis2Link; 
    String  axis3Link;

    public NxWriteDetector()
      {errormessage = "";
       this.axis1Link = "axis1"; 
       this.axis2Link = "axis2"; 
       this.axis3Link = "axis3";
     }

    /** Returns an error string or "" if none
   */
   public String getErrorMessage()
    {
      return errormessage;
    }

   /** Sets the name of the axes that are linked with the NXdata fields
   *@param axis*Link  the node name of the 1st axis, 2nd axis, etc.
   *NOTE: Default values are "axis1","axis2","axis3"
   */
   public void setLinkNames( String axis1Link , String axis2Link , 
                     String axis3Link )
   { this.axis1Link = axis1Link; 
     this.axis2Link = axis2Link; 
     this.axis3Link = axis3Link;
     
   }

  /** Sets up a lot of Isaw attributes
  *@param  nxData_Monitor  a NXdata or NXmonitor node
  *@param  startIndex    the index of the starting DataSet block
  *@param  endIndex      One larger than the last index of the
  *                       ending DataSet block
  *@param DS            The data set with the information
  *NOTE: The Data Set Attributes saved here are Solid Angle, Raw Angle,
  *Delta 2theta, Time field Type, group id, Total Count, distance<BR>
  *The distance is a required field in a Nexus File.  The other attributes
  * are attributes of this distance field along with theta .
  */
  public static void SetUpIsawAttributes(NxWriteNode nxData_Monitor, 
           int startIndex, int endIndex, DataSet DS )
  { float coords[];
    DetectorPosition DP;
    NxWriteNode n1;
    Data DB;
    NxWriteNode node = nxData_Monitor;
   
   Object XX;

    NxData_Gen ng = new NxData_Gen ();
    float distance[] ,
          theta[] , 
          solidAngle[] , 
          rawAngle[] , 
          Det2Thet[]  , 
         Tot_Count[];
    int  Time_Field_Type[] ,
          Group_ID[];
    int ndetectors = DS.getNum_entries();
    distance = new float[ endIndex - startIndex ];
    theta = new float[ endIndex - startIndex ];
    solidAngle = rawAngle = Det2Thet =  Tot_Count = null; 
    Time_Field_Type = Group_ID = null;
    for( int i = startIndex ; i < endIndex ; i++ )
       {DB = DS.getData_entry( i );
        XX = DB.getAttributeValue( Attribute.DETECTOR_POS );
        if( XX instanceof DetectorPosition )
           { 
            coords = ( ( DetectorPosition )XX ).getSphericalCoords();
            float coords1[];
            
            coords1 = Types.convertToNexus( coords[0], coords[2], coords[1]);
            distance[ i -startIndex ] = coords1[ 0 ];
            theta [ i-startIndex ] = coords1[  2 ];
           
           }
         else
           {distance[ i-startIndex ] = 0.0f;
            theta [ i-startIndex ] = 0.0f;
           }
//solid angle
         XX = DB.getAttributeValue( Attribute.SOLID_ANGLE );
         if( XX != null )
          {if( solidAngle == null )
            {solidAngle = new float[ endIndex - startIndex ];
             for( int k = startIndex ; k < i ; k++ )
               solidAngle[ k -startIndex ] = -1;
             }
           Float F = ng.cnvertoFloat( XX );
           if( F != null )
              solidAngle[ i - startIndex ] = F.floatValue();
           else 
              solidAngle[ i -startIndex ] = -1.0f;
          }
         else if( solidAngle != null )
            solidAngle[ i - startIndex ] = -1.0f;

//Delta_2Theta
         XX = DB.getAttributeValue( Attribute.DELTA_2THETA );
         if( XX != null )
          {if( Det2Thet == null )
            {Det2Thet = new float[ endIndex - startIndex ];
             for( int k = startIndex ; k < i ; k++ )
               Det2Thet[ k -startIndex ] = -1;
             }
           Float F = ng.cnvertoFloat( XX );
           if( F != null )
              Det2Thet[ i - startIndex ] = F.floatValue();
           else 
              Det2Thet[ i -startIndex ] = -1.0f;
          }
        else if( Det2Thet != null )
            Det2Thet[ i - startIndex ] = -1.0f;

//Raw Angle--------
        XX = DB.getAttributeValue( Attribute.RAW_ANGLE );
        if( XX != null )
         {if( rawAngle == null )
          {rawAngle = new float[ endIndex - startIndex ];
           for( int k = startIndex ; k < i ; k++ )
             rawAngle[ k -startIndex ] = -1;
           }
          Float F = ng.cnvertoFloat( XX );
          if( F != null )
            rawAngle[ i - startIndex ] = F.floatValue();
          else 
            rawAngle[ i -startIndex ] = -1.0f;
         }
        else if( rawAngle != null )
          rawAngle[ i - startIndex ] = -1.0f;

//Time field type 
        XX = DB.getAttributeValue( Attribute.TIME_FIELD_TYPE );
        if( XX != null )
         {if( Time_Field_Type  == null )
           {Time_Field_Type = new int[ endIndex - startIndex ];
            for( int k = startIndex ; k < i ; k++ )
              Time_Field_Type[ k -startIndex ] = -1;
            }
          int F = ng.cnvertoint( XX );
          if( F >= 0 )
             Time_Field_Type[ i-startIndex ] = F;
          else 
              Time_Field_Type[ i-startIndex ] = -1;
         }
        else if( Time_Field_Type != null )
           Time_Field_Type[ i-startIndex ] = -1;
	
//Group_ID
        XX = DB.getAttributeValue( Attribute.GROUP_ID );
        if( XX != null )
         {if( Group_ID  == null )
           {Group_ID = new int[ endIndex - startIndex ];
            for( int k = startIndex ; k < i ; k++ )
              Group_ID[ k -startIndex ] = -1;
            }
          int F = ng.cnvertoint( XX );
          if( F >= 0 )
             Group_ID[ i-startIndex ] = F;
          else 
             Group_ID[ i-startIndex ] = -1;
         }
        else if( Group_ID != null )
            Group_ID[ i-startIndex ] = -1;
	

//Total counts
        XX = DB.getAttributeValue( Attribute.TOTAL_COUNT );
        if( XX != null )
         {if( Tot_Count  == null )
           {Tot_Count = new float[ endIndex - startIndex ];
            for( int k = startIndex ; k < i ; k++ )
              Tot_Count[ k -startIndex ] = -1.0f;
            }
          Float  F = ng.cnvertoFloat(  XX );
          if( F == null )
             Tot_Count[ i-startIndex ] = -1.0f;
          else if( F.floatValue() >= 0 )
             Tot_Count[ i-startIndex ] = F.floatValue();
          else 
             Tot_Count[ i-startIndex ] = -1.0f;
         }
        else if( Tot_Count != null )
           Tot_Count[ i-startIndex ] = -1.0f;
	
     }// for i = startIndex to endIndex 

    n1 = node.newChildNode( "distance" , "SDS" );
    int rank[];
    rank = new int[ 1 ];
    rank[ 0 ] = distance.length;
    
    n1.setNodeValue( distance , Types.Float  , rank );
    if( solidAngle != null )if( solidAngle.length == rank[ 0 ] )
           n1.addAttribute( "solid_angle" , solidAngle , Types.Float , rank );
    if( Det2Thet != null )if( Det2Thet.length == rank[ 0 ] )
           n1.addAttribute( "delta_2theta" , Det2Thet , Types.Float , rank );
    if( Time_Field_Type != null )if( Time_Field_Type.length ==  rank[ 0 ] )
         n1.addAttribute( "time_field_type" , Time_Field_Type , 
                                       Types.Int , rank );
    if( Group_ID != null )if( Group_ID.length == rank[ 0 ] )
         n1.addAttribute( "group_id" , Group_ID ,Types.Int ,  rank );
    if( Tot_Count != null )if( Tot_Count.length == rank[ 0 ] )
         n1.addAttribute( "total_count" , Tot_Count , Types.Float , rank );
    if( rawAngle != null )if( rawAngle.length == rank[ 0 ] )
         n1.addAttribute( "raw_angle" , rawAngle , Types.Float , rank );

    n1 = node.newChildNode( "theta" , "SDS" );
    
      
    rank = new int[ 1 ];
    rank[ 0 ] = theta.length;
    n1.setNodeValue( theta , Types.Float , rank  );
   
    Object X = null;
    if( DS.getNum_entries() > 0 )
	{ DB = DS.getData_entry( 0 );
         X = DB.getAttributeValue( Attribute.EFFICIENCY_FACTOR );
        }
  
    if( X != null )
      {float x[];
       x = new float[ 1 ];
       if( ng.cnvertoFloat( X ) == null )
          {}
       else if( ng.cnvertoFloat( X ).floatValue() != Float.NaN )
        {x[ 0 ] = ng.cnvertoFloat( X ).floatValue();
         rank = new int[ 1 ];
         rank[ 0 ] = 1;
         NxWriteNode n3 = node.newChildNode( "efficiency" , "SDS" );
         n3.setNodeValue( x  , Types.Float , rank );
        }

       }
  
   }

   /** Writes the NXDetector information in a Data Set to a Nexus file
   *@param node   a NXdetector node
   *@param DS     The data set with the information
   *@param startIndex  the starting index for the data blocks
   *@param endIndex    One more than the ending Index of the data blocks
   *NOTE: These data blocks must have the same X's. That is why they are split
   *up
   */
   public boolean processDS( NxWriteNode node , DataSet DS , 
                           int startIndex , int endIndex )
    {
    errormessage = "Improper NxDetector inputs";
    char cc = 0;
    if( node == null )
	return true;
    if( DS == null )
	return true;
    if( DS.getNum_entries() <= 0 )
        return true;
    if( endIndex >  DS.getNum_entries() )
	return true;
    if( startIndex < 0 )
       return true;
     if( startIndex >= endIndex )
      return true;
     errormessage = "";
   
     int rank1[] , intval[];
     Data DB = DS.getData_entry( startIndex );
     XScale XU = DB.getX_scale();
     float xvals[];
     xvals =  XU.getXs();
     NxWriteNode n1 = node.newChildNode( "time_of_flight" , "SDS" );
     String units , longname;
     units = DS.getX_units();
     longname = DS.getX_label();
     if( units != null )
       {rank1 = new int[ 1 ];
       rank1[ 0 ] = units.length()+1;
       n1.addAttribute( "units" , ( units+cc ).getBytes() , 
                           Types.Char , rank1 );
        }
     if( longname != null )
       {rank1 = new int[ 1 ];
        rank1[ 0 ] = longname.length()+1;
        n1.addAttribute( "long_name" , ( longname+cc ).getBytes() ,
                 Types.Char , rank1 );
       } 
     rank1 = new int[ 1 ];
     rank1[ 0 ] = 1;
     intval = new int[ 1 ];
     intval[ 0 ] = 1;
     n1.addAttribute( "axis" , intval , Types.Int , rank1 ); 
        
     rank1 = new int[ 1 ];     
     rank1[ 0 ] = xvals.length;
     n1.setNodeValue( xvals  , Types.Float , rank1 );
     n1.setLinkHandle( axis1Link );
     if( n1.getErrorMessage() != "" )
       {errormessage = n1.getErrorMessage();
        System.out.println( "NxData A"+errormessage );
        return true;
       }
        
     if( node.getErrorMessage() != "" )
      {errormessage = node.getErrorMessage();
        System.out.println( "NxData B" );
         return true;
      }
    
    float phi[] ;
    phi = new float[ endIndex-startIndex ];
   
    float coords[];
    DetectorPosition DP;
    for( int j = startIndex ; j < endIndex ; j++ )
      {Data DB2 = DS.getData_entry(  j );
       DP = ( DetectorPosition )DB2.getAttributeValue(  
                                          Attribute.DETECTOR_POS );
       if( DP == null ) 
          phi[ j ] = j;
       else
         {
          coords = DP.getSphericalCoords();
          float coords1[];
	  
          coords1 = Types.convertToNexus( coords[0] , coords[2], coords[1]);
          
          phi[ j-startIndex ] = coords1[ 1 ]; 
         }
      }//for j= startIndex to endIndex
        
    NxWriteNode n2 = node.newChildNode( "phi" , "SDS" );

    units = DS.getY_units();
    longname = DS.getY_label();
    if( units != null )
        {rank1 = new int[ 1 ];
         rank1[ 0 ]= units.length()+1;
         n2.addAttribute( "units" , ( units+cc ).getBytes() , 
                                 Types.Char , rank1 );
        }
          
    if( longname != null )
       {
        rank1 = new int[ 1 ];
        rank1[ 0 ] = longname.length()+1;
        n2.addAttribute( "long_name" , ( longname+cc ).getBytes() , 
                                   Types.Char , rank1 );
       } 
          
     rank1 = new int[ 1 ];
     rank1[ 0 ] = 1;
     intval = new int[ 1 ];
     intval[ 0 ] = 2;
     n2.addAttribute( "axis" , intval , Types.Int , rank1 ); 
        
     rank1 = new int[ 1 ];
     rank1[ 0 ] = phi.length;
         
     n2.setNodeValue( phi , Types.Float , rank1 );
     n2.setLinkHandle( axis2Link );
     if( n2.getErrorMessage() != "" )
        {errormessage = n2.getErrorMessage();
         return true;
        }
     
    if( node.getErrorMessage() != "" )
      {errormessage = node.getErrorMessage();
       return true;
      }

   Object XX;

    NxData_Gen ng = new NxData_Gen ();
    float distance[] ,
          theta[] , 
          solidAngle[] , 
          rawAngle[] , 
          Det2Thet[]  , 
         Tot_Count[];
    int  Time_Field_Type[] ,
          Group_ID[];
    int ndetectors = DS.getNum_entries();
    distance = new float[ endIndex - startIndex ];
    theta = new float[ endIndex - startIndex ];
    solidAngle = rawAngle = Det2Thet =  Tot_Count = null; 
    Time_Field_Type = Group_ID = null;
    for( int i = startIndex ; i < endIndex ; i++ )
       {DB = DS.getData_entry( i );
        XX = DB.getAttributeValue( Attribute.DETECTOR_POS );
        if( XX instanceof DetectorPosition )
           { 
            coords = ( ( DetectorPosition )XX ).getSphericalCoords();
            float coords1[];
            
            coords1 = Types.convertToNexus( coords[0], coords[2], coords[1]);
            distance[ i -startIndex ] = coords1[ 0 ];
            theta [ i-startIndex ] = coords1[  2 ];
           
           }
         else
           {distance[ i-startIndex ] = 0.0f;
            theta [ i-startIndex ] = 0.0f;
           }
//solid angle
         XX = DB.getAttributeValue( Attribute.SOLID_ANGLE );
         if( XX != null )
          {if( solidAngle == null )
            {solidAngle = new float[ endIndex - startIndex ];
             for( int j = startIndex ; j < i ; j++ )
               solidAngle[ j -startIndex ] = -1;
             }
           Float F = ng.cnvertoFloat( XX );
           if( F != null )
              solidAngle[ i - startIndex ] = F.floatValue();
           else 
              solidAngle[ i -startIndex ] = -1.0f;
          }
         else if( solidAngle != null )
            solidAngle[ i - startIndex ] = -1.0f;

//Delta_2Theta
         XX = DB.getAttributeValue( Attribute.DELTA_2THETA );
         if( XX != null )
          {if( Det2Thet == null )
            {Det2Thet = new float[ endIndex - startIndex ];
             for( int j = startIndex ; j < i ; j++ )
               Det2Thet[ j -startIndex ] = -1;
             }
           Float F = ng.cnvertoFloat( XX );
           if( F != null )
              Det2Thet[ i - startIndex ] = F.floatValue();
           else 
              Det2Thet[ i -startIndex ] = -1.0f;
          }
        else if( Det2Thet != null )
            Det2Thet[ i - startIndex ] = -1.0f;

//Raw Angle--------
        XX = DB.getAttributeValue( Attribute.RAW_ANGLE );
        if( XX != null )
         {if( rawAngle == null )
          {rawAngle = new float[ endIndex - startIndex ];
           for( int j = startIndex ; j < i ; j++ )
             rawAngle[ j -startIndex ] = -1;
           }
          Float F = ng.cnvertoFloat( XX );
          if( F != null )
            rawAngle[ i - startIndex ] = F.floatValue();
          else 
            rawAngle[ i -startIndex ] = -1.0f;
         }
        else if( rawAngle != null )
          rawAngle[ i - startIndex ] = -1.0f;

//Time field type 
        XX = DB.getAttributeValue( Attribute.TIME_FIELD_TYPE );
        if( XX != null )
         {if( Time_Field_Type  == null )
           {Time_Field_Type = new int[ endIndex - startIndex ];
            for( int j = startIndex ; j < i ; j++ )
              Time_Field_Type[ j -startIndex ] = -1;
            }
          int F = ng.cnvertoint( XX );
          if( F >= 0 )
             Time_Field_Type[ i-startIndex ] = F;
          else 
              Time_Field_Type[ i-startIndex ] = -1;
         }
        else if( Time_Field_Type != null )
           Time_Field_Type[ i-startIndex ] = -1;
	
//Group_ID
        XX = DB.getAttributeValue( Attribute.GROUP_ID );
        if( XX != null )
         {if( Group_ID  == null )
           {Group_ID = new int[ endIndex - startIndex ];
            for( int j = startIndex ; j < i ; j++ )
              Group_ID[ j -startIndex ] = -1;
            }
          int F = ng.cnvertoint( XX );
          if( F >= 0 )
             Group_ID[ i-startIndex ] = F;
          else 
             Group_ID[ i-startIndex ] = -1;
         }
        else if( Group_ID != null )
            Group_ID[ i-startIndex ] = -1;
	

//Total counts
        XX = DB.getAttributeValue( Attribute.TOTAL_COUNT );
        if( XX != null )
         {if( Tot_Count  == null )
           {Tot_Count = new float[ endIndex - startIndex ];
            for( int j = startIndex ; j < i ; j++ )
              Tot_Count[ j -startIndex ] = -1.0f;
            }
          Float  F = ng.cnvertoFloat(  XX );
          if( F == null )
             Tot_Count[ i-startIndex ] = -1.0f;
          else if( F.floatValue() >= 0 )
             Tot_Count[ i-startIndex ] = F.floatValue();
          else 
             Tot_Count[ i-startIndex ] = -1.0f;
         }
        else if( Tot_Count != null )
           Tot_Count[ i-startIndex ] = -1.0f;
	
     }// for i = startIndex to endIndex 

    n1 = node.newChildNode( "distance" , "SDS" );
    int rank[];
    rank = new int[ 1 ];
    rank[ 0 ] = distance.length;
    
    n1.setNodeValue( distance , Types.Float  , rank );
    if( solidAngle != null )if( solidAngle.length == rank[ 0 ] )
           n1.addAttribute( "solid_angle" , solidAngle , Types.Float , rank );
    if( Det2Thet != null )if( Det2Thet.length == rank[ 0 ] )
           n1.addAttribute( "delta_2theta" , Det2Thet , Types.Float , rank );
    if( Time_Field_Type != null )if( Time_Field_Type.length ==  rank[ 0 ] )
         n1.addAttribute( "time_field_type" , Time_Field_Type , 
                                       Types.Int , rank );
    if( Group_ID != null )if( Group_ID.length == rank[ 0 ] )
         n1.addAttribute( "group_id" , Group_ID ,Types.Int ,  rank );
    if( Tot_Count != null )if( Tot_Count.length == rank[ 0 ] )
         n1.addAttribute( "total_count" , Tot_Count , Types.Float , rank );
    if( rawAngle != null )if( rawAngle.length == rank[ 0 ] )
         n1.addAttribute( "raw_angle" , rawAngle , Types.Float , rank );

    n1 = node.newChildNode( "theta" , "SDS" );
    
      
    rank = new int[ 1 ];
    rank[ 0 ] = theta.length;
    n1.setNodeValue( theta , Types.Float , rank  );
   
    Object X = null;
    if( DS.getNum_entries() > 0 )
	{ DB = DS.getData_entry( 0 );
         X = DB.getAttributeValue( Attribute.EFFICIENCY_FACTOR );
        }
  
    if( X != null )
      {float x[];
       x = new float[ 1 ];
       if( ng.cnvertoFloat( X ) == null )
          {}
       else if( ng.cnvertoFloat( X ).floatValue() != Float.NaN )
        {x[ 0 ] = ng.cnvertoFloat( X ).floatValue();
         rank = new int[ 1 ];
         rank[ 0 ] = 1;
         NxWriteNode n3 = node.newChildNode( "efficiency" , "SDS" );
         n3.setNodeValue( x  , Types.Float , rank );
        }

       }
  
    return false;
    }//processDS

}
