/*
 * File:  NxWriteDetector.java 
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
 * Revision 1.6  2003/10/15 02:52:58  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.5  2002/11/27 23:29:19  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/11/20 16:15:38  pfpeterson
 * reformating
 *
 * Revision 1.3  2002/04/01 20:52:48  rmikk
 * Moved code into a method
 * Incorporated Group_ID
 *
 * Revision 1.2  2002/03/18 20:58:33  dennis
 * Added initial support for TOF Diffractometers.
 * Added support for more units.
 *
 */

package NexIO.Write;

import DataSetTools.dataset.*;
import DataSetTools.math.*;
import NexIO.*;
import IPNS.Runfile.*;

/**
 * A Class that is responsible for writing information from data sets
 * to the NXdetector section of a Nexus File
 */
public class NxWriteDetector{
  String errormessage;
  String axis1Link; 
  String axis2Link; 
  String  axis3Link;
  static int instrType;

  public NxWriteDetector(int instrType){
    errormessage = "";
    this.axis1Link = "axis1"; 
    this.axis2Link = "axis2"; 
    this.axis3Link = "axis3";
    this.instrType = instrType;
  }

  /**
   * Returns an error string or "" if none
   */
  public String getErrorMessage(){
    return errormessage;
  }

  /**
   * Sets the name of the axes that are linked with the NXdata fields
   *
   * @param axis1Link The node name of the 1st axis
   * @param axis2Link The node name of the 2nd axis
   * @param axis3Link The node name of the 3rd axis
   *
   * NOTE: Default values are "axis1","axis2","axis3"
   */
  public void setLinkNames(String axis1Link,String axis2Link,String axis3Link){
    this.axis1Link = axis1Link; 
    this.axis2Link = axis2Link; 
    this.axis3Link = axis3Link;
   }

  /**
   * Sets up a lot of Isaw attributes
   *
   * @param node a NXdetector or NXmonitor node
   * @param startIndex the index of the starting DataSet block
   * @param endIndex One larger than the last index of the ending
   * DataSet block
   * @param DS The data set with the information
   * @param monitor The monitor DataSet.
   *
   * NOTE: The Data Set Attributes saved here are Solid Angle, Raw
   * Angle, Delta 2theta, Time field Type, group id, Total Count,
   * distance.<br> The distance is a required field in a Nexus File.
   * The other attributes are attributes of this distance field along
   * with theta .
   */
  public static void SetUpIsawAttributes(NxWriteNode node, int startIndex,
                                      int endIndex,DataSet DS,boolean monitor){
    NxData_Gen ng = new NxData_Gen ();
    NXData_util nu = new NXData_util();
    Object XX;

    int array_length   = endIndex-startIndex;

    float distance[]   = new float[array_length];
    float phi[]        = new float[array_length];
    float theta[]      = new float[array_length];
    float solidAngle[] = new float[array_length];
    float rawAngle[]   = new float[array_length];
    float Det2Thet[]   = new float[array_length];
    float Tot_Count[]  = new float[array_length];

    int Group_ID[]     = new int[array_length];
    int[] slot         = new int[array_length];
    int[] crate        = new int[array_length];
    int[] input        = new int[array_length];

    for( int i = startIndex ; i < endIndex ; i++ ){
      Data DB = DS.getData_entry( i );
      XX = DB.getAttributeValue( Attribute.DETECTOR_POS );
      if( XX instanceof DetectorPosition ){
        float[] coords;
        coords = ( ( DetectorPosition )XX ).getSphericalCoords();
        float coords1[];
            
        coords1 = Types.convertToNexus( coords[0], coords[2], coords[1]);
        distance[ i -startIndex ] = coords1[ 0 ];
        theta [ i-startIndex ] = coords1[  2 ];
        phi[i-startIndex] = coords1[1];
        
      }else{
        distance[ i-startIndex ] = 0.0f;
        theta [ i-startIndex ] = 0.0f;
      }
      //solid angle
      XX = DB.getAttributeValue( Attribute.SOLID_ANGLE );
      if( XX == null) solidAngle = null;
      if( XX != null ){
        if( solidAngle == null ){
          //solidAngle = new float[ endIndex - startIndex ];
          for( int k = startIndex ; k < i ; k++ )
            solidAngle[ k -startIndex ] = -1;
        }
        Float F = ng.cnvertoFloat( XX );
        if( F != null )
          solidAngle[ i - startIndex ] = F.floatValue();
        else 
          solidAngle[ i -startIndex ] = -1.0f;
      }else if( solidAngle != null )
        solidAngle[ i - startIndex ] = -1.0f;

      /*//Delta_2Theta
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
      */
      //Raw Angle--------
      XX = DB.getAttributeValue( Attribute.RAW_ANGLE );
      if( XX == null) rawAngle = null;
      if( XX != null ){
        if( rawAngle == null ){
          //rawAngle = new float[ endIndex - startIndex ];
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

      /*//Time field type 
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
      */	
      //Group_ID
      /* XX = DB.getAttributeValue( Attribute.GROUP_ID );
         
      if( XX == null) Group_ID[i-startIndex] = -1;
      else if( XX != null )
      {
      int F = ng.cnvertoint( XX );
      if( F >= 0 )
      Group_ID[ i-startIndex ] = F;
      else 
      Group_ID[ i-startIndex ] = -1;
      }
      else if( Group_ID != null )
      Group_ID[ i-startIndex ] = -1;
      */
      Group_ID[i-startIndex] = DB.getGroup_ID();
      
      
      //Total counts
      XX = DB.getAttributeValue( Attribute.TOTAL_COUNT );
      if( XX == null) Tot_Count = null;
      if( XX != null ){
        if( Tot_Count  == null ){
          //Tot_Count = new float[ endIndex - startIndex ];
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
      }else if( Tot_Count != null )
        Tot_Count[ i-startIndex ] = -1.0f;

      //slot----
      XX = DB.getAttributeValue( Attribute.SLOT );
      if( XX == null)slot = null;
      if( XX != null ){
        if( slot == null ){
          slot = new int[ endIndex - startIndex ];
          for( int k = startIndex ; k < i ; k++ )
            slot[ k -startIndex ] = -1;
        }
        float[] F = nu.Arrayfloatconvert( XX );
        slot[ i-startIndex ] = -1;
        if( F != null )
          if(F.length >0)
            slot[ i - startIndex ] = (int)(F[0]);
        
      }else if( slot != null )
        slot[ i - startIndex ] = -1;

      //crate
      XX = DB.getAttributeValue( Attribute.CRATE );
      if( XX == null) crate = null;
      if( XX != null ){
        if( crate == null ){
          crate = new int[ endIndex - startIndex ];
          for( int k = startIndex ; k < i ; k++ )
            crate[ k -startIndex ] = -1;
        }
        float[] F = nu.Arrayfloatconvert( XX );
        crate[ i-startIndex ] = -1;
        if( F != null )
          if(F.length >0)
            crate[ i - startIndex ] =(int)( F[0]);
      }else if( crate != null )
        crate[ i - startIndex ] = -1;

      //input
      XX = DB.getAttributeValue( Attribute.INPUT );
      if( XX == null) input = null;
      if( XX != null ){
        if( input == null ){
          //input = new int[ endIndex - startIndex ];
          for( int k = startIndex ; k < i ; k++ )
            input[ k -startIndex ] = -1;
        }
        float[] F = nu.Arrayfloatconvert( XX );
        input[ i-startIndex ] = -1;
        if( F != null )
          if(F.length >0)
            input[ i - startIndex ] = (int)(F[0]);
      }else if( input != null )
        input[ i - startIndex ] = -1;
      
    }// for i = startIndex to endIndex
            
    if( distance != null){
      NxWriteNode nn = node.newChildNode("distance","SDS");
      nn.setNodeValue( distance,Types.Float,
                       Inst_Type.makeRankArray( distance.length,-1,-1,-1,-1));
      nn.addAttribute("units", ("m"+(char)0).getBytes(),Types.Char,
                      Inst_Type.makeRankArray(2,-1,-1,-1,-1));
    }
    if( phi!=null)if(!LinkAxisMatch("phi",instrType,1,4)){
      NxWriteNode nn = node.newChildNode("polar_angle","SDS");
         nn.setNodeValue( phi,Types.Float,
                          Inst_Type.makeRankArray( phi.length,-1,-1,-1,-1));
         nn.addAttribute("units", ("radians"+(char)0).getBytes(),Types.Char,
                         Inst_Type.makeRankArray(8,-1,-1,-1,-1));
    }
      
    if( !monitor)
      if( solidAngle != null){
        NxWriteNode nn = node.newChildNode("solid_angle","SDS");
        nn.setNodeValue( solidAngle,Types.Float,
                      Inst_Type.makeRankArray( solidAngle.length,-1,-1,-1,-1));
        nn.addAttribute("units", ("radians"+(char)0).getBytes(),Types.Char,
                        Inst_Type.makeRankArray(8,-1,-1,-1,-1));
      }
    if(rawAngle !=null){
      NxWriteNode nn = node.newChildNode("raw_angle","SDS");
      nn.setNodeValue( rawAngle,Types.Float,
                       Inst_Type.makeRankArray( rawAngle.length,-1,-1,-1,-1));
      nn.addAttribute("units", ("degrees"+(char)0).getBytes(),Types.Char,
                      Inst_Type.makeRankArray(8,-1,-1,-1,-1));
    }
    if( Tot_Count != null){
      NxWriteNode nn = node.newChildNode("integral","SDS");
      nn.setNodeValue( Tot_Count,Types.Float,
                       Inst_Type.makeRankArray( Tot_Count.length,-1,-1,-1,-1));
      nn.addAttribute("units", ("counts"+(char)0).getBytes(),Types.Char,
                      Inst_Type.makeRankArray(7,-1,-1,-1,-1));
    }
    if( Group_ID != null)
      if( monitor || !LinkAxisMatch( "id",instrType, 1,5)){
        NxWriteNode nn = node.newChildNode("id","SDS");
        nn.setNodeValue( Group_ID,Types.Int,
                         Inst_Type.makeRankArray(Group_ID.length,-1,-1,-1,-1));
      } 
    if( theta != null){
      NxWriteNode nn = node.newChildNode("azimuthal_angle","SDS");
      nn.setNodeValue( theta,Types.Float,
                       Inst_Type.makeRankArray( theta.length,-1,-1,-1,-1));
      nn.addAttribute("units", ("radians"+(char)0).getBytes(),Types.Char,
                      Inst_Type.makeRankArray(8,-1,-1,-1,-1));
    }
    if( slot != null){
      NxWriteNode nn = node.newChildNode("slot","SDS");
      nn.setNodeValue( slot,Types.Int,
                       Inst_Type.makeRankArray( slot.length,-1,-1,-1,-1));
      //nn.addAttribute("units", ("radians"+(char)0).getBytes(),Types.Char,
      // Inst_Type.makeRankArray(8,-1,-1,-1,-1));
    }

    if( crate != null){
      NxWriteNode nn = node.newChildNode("crate","SDS");
      nn.setNodeValue( crate,Types.Int,
                       Inst_Type.makeRankArray( crate.length,-1,-1,-1,-1));
      //nn.addAttribute("units", ("radians"+(char)0).getBytes(),Types.Char,
      // Inst_Type.makeRankArray(8,-1,-1,-1,-1));
    }
    if( input != null){
      NxWriteNode nn = node.newChildNode("input","SDS");
      nn.setNodeValue( input,Types.Int,
                       Inst_Type.makeRankArray( input.length,-1,-1,-1,-1));
      //nn.addAttribute("units", ("radians"+(char)0).getBytes(),Types.Char,
      //Inst_Type.makeRankArray(8,-1,-1,-1,-1));
    }
  }
  //obsolete
  private static void SetUpIsawAttributes1(NxWriteNode nxData_Monitor, 
                                    int startIndex, int endIndex, DataSet DS ){
    float coords[];
    DetectorPosition DP;
    NxWriteNode n1;
    Data DB;
    NxWriteNode node = nxData_Monitor;
   
    Object XX;

    NxData_Gen ng = new NxData_Gen ();

    int array_length=endIndex-startIndex;


    float distance[]        = new float[array_length];
    float phi[]             = new float[array_length]; //if not linked
    float theta[]           = new float[array_length];
    float solidAngle[]      = null;
    float rawAngle[]        = null;
    float Det2Thet[]        = null;
    float Tot_Count[]       = null;

    int   Time_Field_Type[] = null;
    int   Group_ID[]        = null;
    int   ndetectors        = DS.getNum_entries();

    for( int i = startIndex ; i < endIndex ; i++ ){
      DB = DS.getData_entry( i );
      XX = DB.getAttributeValue( Attribute.DETECTOR_POS );
      if( XX instanceof DetectorPosition ){
        coords = ( ( DetectorPosition )XX ).getSphericalCoords();
        float coords1[];
        
        coords1 = Types.convertToNexus( coords[0], coords[2], coords[1]);
        distance[ i -startIndex ] = coords1[ 0 ];
        theta [ i-startIndex ] = coords1[  2 ];
        phi[i-startIndex] = coords1[1];
      }else{
        distance[ i-startIndex ] = 0.0f;
        theta [ i-startIndex ] = 0.0f;
      }

      //solid angle
      XX = DB.getAttributeValue( Attribute.SOLID_ANGLE );
      if( XX != null ){
        if( solidAngle == null ){
          solidAngle = new float[ endIndex - startIndex ];
          for( int k = startIndex ; k < i ; k++ )
            solidAngle[ k -startIndex ] = -1;
        }
        Float F = ng.cnvertoFloat( XX );
        if( F != null )
          solidAngle[ i - startIndex ] = F.floatValue();
        else 
          solidAngle[ i -startIndex ] = -1.0f;
      }else if( solidAngle != null )
        solidAngle[ i - startIndex ] = -1.0f;

      //Delta_2Theta
      XX = DB.getAttributeValue( Attribute.DELTA_2THETA );
      if( XX != null ){
        if( Det2Thet == null ){
          Det2Thet = new float[ endIndex - startIndex ];
          for( int k = startIndex ; k < i ; k++ )
            Det2Thet[ k -startIndex ] = -1;
        }
        Float F = ng.cnvertoFloat( XX );
        if( F != null )
          Det2Thet[ i - startIndex ] = F.floatValue();
        else 
          Det2Thet[ i -startIndex ] = -1.0f;
      }else if( Det2Thet != null )
        Det2Thet[ i - startIndex ] = -1.0f;

      //Raw Angle--------
      XX = DB.getAttributeValue( Attribute.RAW_ANGLE );
      if( XX != null ){
        if( rawAngle == null ){
          rawAngle = new float[ endIndex - startIndex ];
          for( int k = startIndex ; k < i ; k++ )
            rawAngle[ k -startIndex ] = -1;
        }
        Float F = ng.cnvertoFloat( XX );
        if( F != null )
          rawAngle[ i - startIndex ] = F.floatValue();
        else 
          rawAngle[ i -startIndex ] = -1.0f;
      }else if( rawAngle != null )
        rawAngle[ i - startIndex ] = -1.0f;

      //Time field type 
      XX = DB.getAttributeValue( Attribute.TIME_FIELD_TYPE );
      if( XX != null ){
        if( Time_Field_Type  == null ){
          Time_Field_Type = new int[ endIndex - startIndex ];
          for( int k = startIndex ; k < i ; k++ )
            Time_Field_Type[ k -startIndex ] = -1;
        }
        int F = ng.cnvertoint( XX );
        if( F >= 0 )
          Time_Field_Type[ i-startIndex ] = F;
        else 
          Time_Field_Type[ i-startIndex ] = -1;
      }else if( Time_Field_Type != null )
        Time_Field_Type[ i-startIndex ] = -1;
	
      //Group_ID
      /* XX = DB.getAttributeValue( Attribute.GROUP_ID );
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
      */
      Group_ID[i-startIndex]=DB.getGroup_ID();

      //Total counts
      XX = DB.getAttributeValue( Attribute.TOTAL_COUNT );
      if( XX != null ){
        if( Tot_Count  == null ){
          Tot_Count = new float[ endIndex - startIndex ];
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
      }else if( Tot_Count != null )
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
      if( !LinkAxisMatch("id",instrType,1,5) )
        n1.addAttribute( "id" , Group_ID ,Types.Int ,  rank );
    if( Tot_Count != null )if( Tot_Count.length == rank[ 0 ] )
      n1.addAttribute( "total_count" , Tot_Count , Types.Float , rank );
    if( rawAngle != null )if( rawAngle.length == rank[ 0 ] )
      n1.addAttribute( "raw_angle" , rawAngle , Types.Float , rank );

    n1 = node.newChildNode( "theta" , "SDS" );
    
      
    rank = new int[ 1 ];
    rank[ 0 ] = theta.length;
    n1.setNodeValue( theta , Types.Float , rank  );
   
    Object X = null;
    if( DS.getNum_entries() > 0 ){
      DB = DS.getData_entry( 0 );
      X = DB.getAttributeValue( Attribute.EFFICIENCY_FACTOR );
    }
  
    if( X != null ){
      float x[];
      x = new float[ 1 ];
      if( ng.cnvertoFloat( X ) == null ){
        // do nothing
      }else if( ng.cnvertoFloat( X ).floatValue() != Float.NaN ){
        x[ 0 ] = ng.cnvertoFloat( X ).floatValue();
        rank = new int[ 1 ];
        rank[ 0 ] = 1;
        NxWriteNode n3 = node.newChildNode( "efficiency" , "SDS" );
        n3.setNodeValue( x  , Types.Float , rank );
      }
    }
  }

  private void PackInfo(DataSet DS,float distance[],float phi[],float theta[],
                 float solidAngle[],float rawAngle[],float Det2Thet[],
                 float Tot_Count[],int Group_ID[],int startIndex,int endIndex){
    NxData_Gen ng = new NxData_Gen ();
    Object XX;
    
    //distance = new float[ endIndex - startIndex ];
    //phi = new float[endIndex-startIndex];
    //theta = new float[ endIndex - startIndex ];
    //solidAngle = rawAngle = Det2Thet =  Tot_Count = null; 
    //Time_Field_Type = Group_ID = null;
    for( int i = startIndex ; i < endIndex ; i++ ){
      Data DB = DS.getData_entry( i );
      XX = DB.getAttributeValue( Attribute.DETECTOR_POS );
      if( XX instanceof DetectorPosition ){
        float[] coords;
        coords = ( ( DetectorPosition )XX ).getSphericalCoords();
        float coords1[];
            
        coords1 = Types.convertToNexus( coords[0], coords[2], coords[1]);
        distance[ i -startIndex ] = coords1[ 0 ];
        theta [ i-startIndex ] = coords1[  2 ];
        phi[i-startIndex] = coords1[1];
      }else{
        distance[ i-startIndex ] = 0.0f;
        theta [ i-startIndex ] = 0.0f;
      }

      //solid angle
      XX = DB.getAttributeValue( Attribute.SOLID_ANGLE );
      if( XX == null) solidAngle = null;
      if( XX != null ){
        if( solidAngle == null ){
          //solidAngle = new float[ endIndex - startIndex ];
          for( int k = startIndex ; k < i ; k++ )
            solidAngle[ k -startIndex ] = -1;
        }
        Float F = ng.cnvertoFloat( XX );
        if( F != null )
          solidAngle[ i - startIndex ] = F.floatValue();
        else 
          solidAngle[ i -startIndex ] = -1.0f;
      }else if( solidAngle != null )
        solidAngle[ i - startIndex ] = -1.0f;

      /*//Delta_2Theta
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
      */

      //Raw Angle--------
      XX = DB.getAttributeValue( Attribute.RAW_ANGLE );
      if( XX == null) rawAngle = null;
      if( XX != null ){
        if( rawAngle == null ){
          //rawAngle = new float[ endIndex - startIndex ];
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

      /*//Time field type 
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
      */	

      //Group_ID
      /* XX = DB.getAttributeValue( Attribute.GROUP_ID );
         
      if( XX == null) Group_ID[i-startIndex] = -1;
      else if( XX != null )
      {
      int F = ng.cnvertoint( XX );
      if( F >= 0 )
      Group_ID[ i-startIndex ] = F;
      else 
      Group_ID[ i-startIndex ] = -1;
      }
      else if( Group_ID != null )
      Group_ID[ i-startIndex ] = -1;
      */
      Group_ID[i-startIndex] = DB.getGroup_ID();

      //Total counts
      XX = DB.getAttributeValue( Attribute.TOTAL_COUNT );
      if( XX == null) Tot_Count = null;
      if( XX != null ){
        if( Tot_Count  == null ){
          //Tot_Count = new float[ endIndex - startIndex ];
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
      }else if( Tot_Count != null )
        Tot_Count[ i-startIndex ] = -1.0f;
    }// for i = startIndex to endIndex 
  }

  /**
   * Writes the NXDetector information in a Data Set to a Nexus file
   *
   * @param node a NXdetector node
   * @param DS The data set with the information
   * @param startIndex the starting index for the data blocks
   * @param endIndex One more than the ending Index of the data blocks
   *
   * NOTE: These data blocks must have the same X's. That is why they
   * are split up
   */
   public boolean processDS( NxWriteNode node, DataSet DS, int startIndex,
                             int endIndex ){
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
     boolean monitor = false;
     Attribute AA =DS.getAttribute( Attribute.DS_TYPE);
     if( AA != null)
       if( AA.getValue().equals( Attribute.MONITOR_DATA))
         monitor = true;
     errormessage = "";
     //Time of flight done
     SetUpIsawAttributes(node, startIndex,  endIndex, DS , monitor);
     /* float distance[], phi[],theta[],solidAngle[],rawAngle[],
        Det2Thet[], Tot_Count[];
        int Group_ID[];
        distance = new float[ endIndex - startIndex ];
        phi = new float[endIndex-startIndex];
        theta = new float[ endIndex - startIndex ];
        solidAngle = new float[endIndex-startIndex];
        rawAngle= new float[endIndex-startIndex];
        Det2Thet= new float[endIndex-startIndex];
        Tot_Count= new float[endIndex-startIndex];
        Group_ID= new int[endIndex-startIndex];
        
        PackInfo(DS,distance, phi,theta, solidAngle,rawAngle, Det2Thet,
        Tot_Count,  Group_ID, startIndex,endIndex);
        
        if( distance != null)
        {NxWriteNode nn = node.newChildNode("distance","SDS");
        nn.setNodeValue( distance,Types.Float,
        Inst_Type.makeRankArray( distance.length,-1,-1,-1,-1));
        nn.addAttribute("units", ("m"+(char)0).getBytes(),Types.Char,
        Inst_Type.makeRankArray(2,-1,-1,-1,-1));
        }
        if( phi!=null)if(!LinkAxisMatch("phi",instrType,1,4))
        {NxWriteNode nn = node.newChildNode("phi","SDS");
         nn.setNodeValue( phi,Types.Float,
         Inst_Type.makeRankArray( phi.length,-1,-1,-1,-1));
         nn.addAttribute("units", ("radians"+(char)0).getBytes(),Types.Char,
         Inst_Type.makeRankArray(8,-1,-1,-1,-1));
         }
      
         if( !monitor)
         if( solidAngle != null)
         {NxWriteNode nn = node.newChildNode("solid_angle","SDS");
         nn.setNodeValue( solidAngle,Types.Float,
         Inst_Type.makeRankArray( solidAngle.length,-1,-1,-1,-1));
         nn.addAttribute("units", ("radians"+(char)0).getBytes(),Types.Char,
         Inst_Type.makeRankArray(8,-1,-1,-1,-1));
         }
         if(rawAngle !=null)
         {NxWriteNode nn = node.newChildNode("raw_angle","SDS");
         nn.setNodeValue( rawAngle,Types.Float,
         Inst_Type.makeRankArray( rawAngle.length,-1,-1,-1,-1));
         nn.addAttribute("units", ("degrees"+(char)0).getBytes(),Types.Char,
         Inst_Type.makeRankArray(8,-1,-1,-1,-1));
         }
         if( Tot_Count != null)
         {NxWriteNode nn = node.newChildNode("integral","SDS");
         nn.setNodeValue( Tot_Count,Types.Float,
         Inst_Type.makeRankArray( Tot_Count.length,-1,-1,-1,-1));
         nn.addAttribute("units", ("counts"+(char)0).getBytes(),Types.Char,
         Inst_Type.makeRankArray(7,-1,-1,-1,-1));
         }
         if( Group_ID != null)
         if( monitor || LinkAxisMatch( "id",instrType, 1,5))
         {NxWriteNode nn = node.newChildNode("id","SDS");
         nn.setNodeValue( Group_ID,Types.Int,
         Inst_Type.makeRankArray( Group_ID.length,-1,-1,-1,-1));
        
         } 
         if( theta != null)
         {NxWriteNode nn = node.newChildNode("theta","SDS");
         nn.setNodeValue( theta,Types.Float,
         Inst_Type.makeRankArray( theta.length,-1,-1,-1,-1));
         nn.addAttribute("units", ("radians"+(char)0).getBytes(),Types.Char,
         Inst_Type.makeRankArray(8,-1,-1,-1,-1));
         }
     */
     return false;
     //  Get efficiencies  
     //Get crate
   }

  private static boolean LinkAxisMatch( String axisName, int instrType,
                                        int minAxisNum, int maxAxisNum){
    if(axisName ==  null) 
      return false;
    for( int i=minAxisNum; i <=maxAxisNum; i++){
      if( axisName.equals((new Inst_Type()).getLinkAxisName( instrType, i)))
        return true;
    }
    return false;
  }

  private boolean processDSx( NxWriteNode node, DataSet DS, int startIndex,
                              int endIndex ){
    int[] intval,rank1;
    char cc=0;
    /* //assumed set from NxData
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
       
       return true;
       }
        
       if( node.getErrorMessage() != "" )
       {errormessage = node.getErrorMessage();
       
       return true;
       }
    */
    float phi[] ;
    phi = new float[ endIndex-startIndex ];
   
    float coords[];
    DetectorPosition DP;
    for( int j = startIndex ; j < endIndex ; j++ ){
      Data DB2 = DS.getData_entry(  j );
      DP = ( DetectorPosition )DB2.getAttributeValue(  
                                          Attribute.DETECTOR_POS );
      if( DP == null ) 
        phi[ j ] = j;
      else{
        coords = DP.getSphericalCoords();
        float coords1[];
	  
        coords1 = Types.convertToNexus( coords[0] , coords[2], coords[1]);
          
        phi[ j-startIndex ] = coords1[ 1 ]; 
      }
    }//for j= startIndex to endIndex
        
    NxWriteNode n2 = node.newChildNode( "phi" , "SDS" );

    String units = DS.getY_units();
    String longname = DS.getY_label();
    if( units != null ){
      rank1 = new int[ 1 ];
      rank1[ 0 ]= units.length()+1;
      n2.addAttribute( "units", ( units+cc ).getBytes(), Types.Char , rank1 );
    }
          
    if( longname != null ){
      rank1 = new int[ 1 ];
      rank1[ 0 ] = longname.length()+1;
      n2.addAttribute("long_name",(longname+cc).getBytes(),Types.Char,rank1);
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
    if( n2.getErrorMessage() != "" ){
      errormessage = n2.getErrorMessage();
      return true;
    }
     
    if( node.getErrorMessage() != "" ){
      errormessage = node.getErrorMessage();
      return true;
    }

    Object XX;

    NxData_Gen ng = new NxData_Gen ();

    int   array_length      = endIndex-startIndex;

    float distance[]        = new float[array_length];
    float theta[]           = new  float[array_length];
    float solidAngle[]      = null;
    float rawAngle[]        = null;
    float Det2Thet[]        = null;
    float Tot_Count[]       = null;

    int   Time_Field_Type[] = null;
    int   Group_ID[]        = null;
    int   ndetectors        = DS.getNum_entries();

    for( int i = startIndex ; i < endIndex ; i++ ){
      Data DB = DS.getData_entry( i );
      XX = DB.getAttributeValue( Attribute.DETECTOR_POS );
      if( XX instanceof DetectorPosition ){
        coords = ( ( DetectorPosition )XX ).getSphericalCoords();
        float coords1[];
            
        coords1 = Types.convertToNexus( coords[0], coords[2], coords[1]);
        distance[ i -startIndex ] = coords1[ 0 ];
        theta [ i-startIndex ] = coords1[  2 ];
           
      }else{
        distance[ i-startIndex ] = 0.0f;
        theta [ i-startIndex ] = 0.0f;
      }

      //solid angle
      XX = DB.getAttributeValue( Attribute.SOLID_ANGLE );
      if( XX != null ){
        if( solidAngle == null ){
          solidAngle = new float[ endIndex - startIndex ];
          for( int j = startIndex ; j < i ; j++ )
            solidAngle[ j -startIndex ] = -1;
        }
        Float F = ng.cnvertoFloat( XX );
        if( F != null )
          solidAngle[ i - startIndex ] = F.floatValue();
        else 
          solidAngle[ i -startIndex ] = -1.0f;
      }else if( solidAngle != null )
        solidAngle[ i - startIndex ] = -1.0f;

      //Delta_2Theta
      XX = DB.getAttributeValue( Attribute.DELTA_2THETA );
      if( XX != null ){
        if( Det2Thet == null ){
          Det2Thet = new float[ endIndex - startIndex ];
          for( int j = startIndex ; j < i ; j++ )
            Det2Thet[ j -startIndex ] = -1;
        }
        Float F = ng.cnvertoFloat( XX );
        if( F != null )
          Det2Thet[ i - startIndex ] = F.floatValue();
        else 
          Det2Thet[ i -startIndex ] = -1.0f;
      }else if( Det2Thet != null )
        Det2Thet[ i - startIndex ] = -1.0f;

      //Raw Angle--------
      XX = DB.getAttributeValue( Attribute.RAW_ANGLE );
      if( XX != null ){
        if( rawAngle == null ){
          rawAngle = new float[ endIndex - startIndex ];
          for( int j = startIndex ; j < i ; j++ )
            rawAngle[ j -startIndex ] = -1;
        }
        Float F = ng.cnvertoFloat( XX );
        if( F != null )
          rawAngle[ i - startIndex ] = F.floatValue();
        else 
          rawAngle[ i -startIndex ] = -1.0f;
      }else if( rawAngle != null )
        rawAngle[ i - startIndex ] = -1.0f;

      //Time field type 
      XX = DB.getAttributeValue( Attribute.TIME_FIELD_TYPE );
      if( XX != null ){
        if( Time_Field_Type  == null ){
          Time_Field_Type = new int[ endIndex - startIndex ];
          for( int j = startIndex ; j < i ; j++ )
            Time_Field_Type[ j -startIndex ] = -1;
        }
        int F = ng.cnvertoint( XX );
        if( F >= 0 )
          Time_Field_Type[ i-startIndex ] = F;
        else 
          Time_Field_Type[ i-startIndex ] = -1;
      }else if( Time_Field_Type != null )
        Time_Field_Type[ i-startIndex ] = -1;
	
      //Group_ID
      XX = DB.getAttributeValue( Attribute.GROUP_ID );
      if( XX != null ){
        if( Group_ID  == null ){
          Group_ID = new int[ endIndex - startIndex ];
          for( int j = startIndex ; j < i ; j++ )
            Group_ID[ j -startIndex ] = -1;
        }
        int F = ng.cnvertoint( XX );
        if( F >= 0 )
          Group_ID[ i-startIndex ] = F;
        else 
          Group_ID[ i-startIndex ] = -1;
      }else if( Group_ID != null )
        Group_ID[ i-startIndex ] = -1;
	

      //Total counts
      XX = DB.getAttributeValue( Attribute.TOTAL_COUNT );
      if( XX != null ){
        if( Tot_Count  == null ){
          Tot_Count = new float[ endIndex - startIndex ];
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
      }else if( Tot_Count != null )
        Tot_Count[ i-startIndex ] = -1.0f;
    }// for i = startIndex to endIndex 

    NxWriteNode n1 = node.newChildNode( "distance" , "SDS" );
    int rank[];
    rank = new int[ 1 ];
    rank[ 0 ] = distance.length;
    
    n1.setNodeValue( distance , Types.Float  , rank );
    if( solidAngle != null )if( solidAngle.length == rank[ 0 ] )
      n1.addAttribute( "solid_angle" , solidAngle , Types.Float , rank );
    if( Det2Thet != null )if( Det2Thet.length == rank[ 0 ] )
      n1.addAttribute( "delta_2theta" , Det2Thet , Types.Float , rank );
    if( Time_Field_Type != null )if( Time_Field_Type.length ==  rank[ 0 ] )
      n1.addAttribute( "time_field_type", Time_Field_Type, Types.Int , rank );
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
    if( DS.getNum_entries() > 0 ){
      Data DB = DS.getData_entry( 0 );
      X = DB.getAttributeValue( Attribute.EFFICIENCY_FACTOR );
    }
  
    if( X != null ){
      float x[];
      x = new float[ 1 ];
      if( ng.cnvertoFloat( X ) == null ){
        // do nothing
      }else if( ng.cnvertoFloat( X ).floatValue() != Float.NaN ){
        x[ 0 ] = ng.cnvertoFloat( X ).floatValue();
        rank = new int[ 1 ];
        rank[ 0 ] = 1;
        NxWriteNode n3 = node.newChildNode( "efficiency" , "SDS" );
        n3.setNodeValue( x  , Types.Float , rank );
      }
    }
  
    return false;
  }//processDS
}
