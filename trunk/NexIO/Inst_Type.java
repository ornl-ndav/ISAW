/*
 * File:  Inst_Type.java 
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
 * Revision 1.11  2003/11/23 23:58:17  rmikk
 * Updated the Instrument type conversions to all supported instruments
 *   in the nexus community.
 * Eliminated some debugging prints
 * Changed references from phi to polar_angle
 *
 * Revision 1.10  2003/03/05 20:44:06  pfpeterson
 * Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 * Revision 1.9  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.8  2002/11/20 16:14:37  pfpeterson
 * reformating
 *
 * Revision 1.7  2002/06/19 15:03:00  rmikk
 * Eliminated commented out code and fixed indenting and
 * code alignment
 *
 * Revision 1.6  2002/04/01 20:09:10  rmikk
 * Improved the Axis Write Handler.
 * Added getMonitorName and getLinkName for specific instrument types.
 *
 * Revision 1.5  2002/03/18 21:06:49  dennis
 * Fixed indexing error.
 *
 * Revision 1.4  2002/02/26 15:31:34  rmikk
 * Added Code to incorporate the TOFNDGS instrument type
 * Added utility routines to get link names, set up an xval node with
 *    all its attributes and values
 *
 */
package NexIO;


import java.util.*;
import DataSetTools.instruments.*;
import java.io.*;
import java.util.*;
import NexIO.Write.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;


/** This class currently maps correspondences between Nexus and Isaw
 *instrument types.  This class maps instrument types to
 * Handlers for various parts of the retrievers and writers
 */
public class Inst_Type{
  private static Hashtable HT = null;
  private
    static String NxNames[] = {"UNKNOWN","TOFNPD","TOFNSCD","TOFNSAS",
                                "TOFNREF","TOFNIGS","MONONXTAS","MONONXPD",
                                "MONONXSCD","MONONXSAS","MONONXREF"};

  private static int Isaw_inst_types[] = 
     {InstrumentType.UNKNOWN,
      InstrumentType.TOF_DIFFRACTOMETER,
       InstrumentType.TOF_SCD,
      InstrumentType.TOF_SAD,
      InstrumentType.TOF_REFLECTOMETER ,    
      InstrumentType.TOF_IDG_SPECTROMETER ,    
      InstrumentType.TRIPLE_AXIS_SPECTROMETER ,
      InstrumentType.MONO_CHROM_DIFFRACTOMETER ,
      InstrumentType.MONO_CHROM_SCD  ,
      InstrumentType.MONO_CHROM_SAD,
      InstrumentType.MONO_CHROM_REFLECTOMETER};


  /**
   * Initializes correspondence tables
   */
  public Inst_Type(){
    if( HT == null ){
      HT = new Hashtable();
      HT.put( "MonoNXPD", new Integer( 0 ) );
      HT.put( "UNKNOWN", new Integer( 1 ) );
      HT.put( "TOFNDGS", new Integer( 2 ) );
      HT.put( "TOFNIGS", new Integer( 3 ) );
      HT.put( "TOFNPD", new Integer( 4 ) );
    }
  }


   /**
    * Returns Isaw's instrument number that corresponds to Nexus'
    * analysis name
    */
  public int getIsawInstrNum( String NexusAnalysisName ){
    if( NexusAnalysisName == null)
      return InstrumentType.UNKNOWN;
    Integer I = ( Integer )( HT.get( NexusAnalysisName.trim() ) );

    if( I == null )
      return InstrumentType.UNKNOWN;
    int i = I.intValue();
    
    if( i < 0 )
      return InstrumentType.UNKNOWN;
    if( i >= Isaw_inst_types.length )
      return InstrumentType.UNKNOWN;
    return Isaw_inst_types[i];
  }


  /**
   * Returns the Nexus analysis field name corresponding to Isaw's
   * instrument number IsawInstrNum
   */
  public String getNexAnalysisName( int IsawInstrNum ){
    for( int i = 0; i < Isaw_inst_types.length; i++ )
      if( Isaw_inst_types[i] == IsawInstrNum )
        return NxNames[i];
    return null;
  }


  /// more to get NxEntry Handlers, NxData Handlers, etc...
  /**
   * Handles the node writing for the NxData axis entries.  Returns
   * Link Name or null of the data
   *
   * @param num axis number num
   * @param kk Each axisWrite handler has a different kk, So returned
   * links have diff names
   *
   * @return null or the link name Will expand to 3 axis and different
   * linkages Data Sets from begin to end index have all the same time
   * field type
   */
  public String AxisWriteHandler( int num, int kk, int instrType,
                                  NxWriteNode nxdata, NxWriteNode nxdetector,
                                  DataSet DS, int beginIndex, int endIndex ){
    if( DS == null ) 
      return null;
    if( ( beginIndex < 0 ) || ( beginIndex >= endIndex ) ) 
      return null;
    
    if( endIndex > DS.getNum_entries() ) 
      return null;
    if( beginIndex >= endIndex ) 
      return null;

    String S = getLinkAxisName( instrType, num );
    if( S == null ) 
      return null;
    
    if( S.equals( "time_of_flight" ) ){
      NxWriteNode ntof = makeXvalnode( DS, beginIndex, 
                                       endIndex, nxdetector );
      
      ntof.addAttribute( "axis", makeRankArray( 1, -1, -1, -1, -1 ), 
                         Types.Int, makeRankArray( 1, -1, -1, -1, -1 ) );
      
      ntof.setLinkHandle( ( "axis" + num ) + kk );
      return( "axis" + num ) + kk;
    }
    if( S.equals( "polar_angle" ) ){
      float[] phi = new float[endIndex - beginIndex];
      
      for( int i = beginIndex; i < endIndex; i++ ){
        Data D = DS.getData_entry( i );
        Attribute A = D.getAttribute( Attribute.DETECTOR_POS );
          
        if( A == null ){
          SharedData.addmsg( "No Detector position attribute" );
          return null;
        }
        phi[i - beginIndex] =
          ((DetPosAttribute)A).getDetectorPosition().getScatteringAngle();
        
      }
      NxWriteNode nphi = nxdetector.newChildNode( "polar_angle", "SDS" );
      
      nphi.setNodeValue( phi, Types.Float, 
                         makeRankArray( phi.length, -1, -1, -1, -1 ) );
      nphi.addAttribute( "units", ( "radians" + ( char )0 ).getBytes(),
                         Types.Char, makeRankArray( 8, -1, -1, -1, -1 ) );
      nphi.addAttribute( "axis", makeRankArray( 2, -1, -1, -1, -1 ),
                         Types.Int, makeRankArray( num, -1, -1, -1, -1 ) );
      nphi.addAttribute( "long_name",("Scattering Angle"+(char)0).getBytes(),
                         Types.Char,makeRankArray( 18, -1, -1, -1, -1 ) );
      nphi.setLinkHandle( ( "axis" + num ) + kk );
      byte[] nodename =(nxdetector.getNodeName()+(char)0).getBytes();
      nphi.addAttribute( "link", nodename, Types.Char,
                     makeRankArray( nodename.length,-1,-1,-1,-1));
      return( "axis" + num ) + kk;
      
    }
    if( S.equals( "id" ) ){
      int[] id = new int[endIndex - beginIndex];
      
      for( int i = beginIndex; i < endIndex; i++ ){
        Data D = DS.getData_entry( i );
        
        id[i - beginIndex] = D.getGroup_ID();
      }
      NxWriteNode ndId = nxdetector.newChildNode( "id", "SDS" );
      
      ndId.setNodeValue( id, Types.Int, 
                         makeRankArray( id.length, -1, -1, -1, -1 ) );
      ndId.addAttribute( "axis", makeRankArray( 2, -1, -1, -1, -1 ), 
                         Types.Int, makeRankArray( num, -1, -1, -1, -1 ) );
      ndId.setLinkHandle( ( "axis" + num ) + kk );
      byte[] nodename =(nxdetector.getNodeName()+(char)0).getBytes();
      ndId.addAttribute( "link", nodename, Types.Char,
                     makeRankArray( nodename.length,-1,-1,-1,-1));
      return( "axis" + num ) + kk;
    }
    //add more axis that can be linked here
    return null;
    
  }


  private static String AxisWriteHandler1( int num, int kk, int instrType,
                                           NxWriteNode nxdata,
                                           NxWriteNode nxdetector, DataSet DS,
                                           int beginIndex, int endIndex ){
   
    if( DS == null )
      return null;
    
    if( ( beginIndex < 0 ) || ( beginIndex >= endIndex ) ) 
      return null;
    
    if( endIndex > DS.getNum_entries() ) 
      return null;
    if( beginIndex >= endIndex ) 
      return null;
    
    if( num == 1 ){ // will be time_of_flight or xvals
      NxWriteNode ntof = makeXvalnode( DS, beginIndex, endIndex, nxdetector );

      ntof.addAttribute( "axis", makeRankArray( 1, -1, -1, -1, -1 ),
                         Types.Int, makeRankArray( 1, -1, -1, -1, -1 ) );
      ntof.setLinkHandle( "axis1" + kk );
      return "axis1" + kk;
      
    }else if( (num==2) && (instrType!=InstrumentType.TOF_DIFFRACTOMETER) ){
      //phi will be 2nd axis
      float[] phi = new float[endIndex - beginIndex];
      
      for( int i = beginIndex; i < endIndex; i++ ){
        Data D = DS.getData_entry( i );
        Attribute A = D.getAttribute( Attribute.DETECTOR_POS );
        
        if( A == null ){
          SharedData.addmsg( "No Detector position attribute" );
          return null;
        }
        phi[i - beginIndex] =
          ( ( DetPosAttribute )A ).getDetectorPosition().getScatteringAngle();

      }
      NxWriteNode nphi = nxdetector.newChildNode( "phi", "SDS" );
      
      nphi.setNodeValue( phi, Types.Float, 
                         makeRankArray( phi.length, -1, -1, -1, -1 ) );
      nphi.addAttribute( "units", ( "radians" + ( char )0 ).getBytes(), 
                         Types.Char, makeRankArray( 8, -1, -1, -1, -1 ) );
      nphi.addAttribute( "axis", makeRankArray( 2, -1, -1, -1, -1 ),
                         Types.Int, makeRankArray( 1, -1, -1, -1, -1 ) );
      nphi.addAttribute( "long_name", 
                         ( "Scattering Angle" + ( char )0 ).getBytes(),
                         Types.Char, makeRankArray( 18, -1, -1, -1, -1 ) );
      nphi.setLinkHandle( "axis2" + kk );
      nphi.setLinkHandle( "axis2" + kk );
      return "axis2" + kk;
      
    }else if( num == 2 ){ //Group Id is 2nd axis
      float[] id = new float[endIndex - beginIndex];
      
      for( int i = beginIndex; i < endIndex; i++ ){
        Data D = DS.getData_entry( beginIndex );
        Attribute A = D.getAttribute( Attribute.GROUP_ID );
        
        if( A == null ){
          SharedData.addmsg( "No GROUP_ID attribute" );
          id[i - beginIndex] = -1;;
        }else if( A instanceof IntAttribute )
          id[i - beginIndex] = ( ( IntAttribute )A ).getIntegerValue();
        else
          id[i - beginIndex] = -1;
        
      }
      NxWriteNode ndId = nxdetector.newChildNode( "id", "SDS" );
      
      ndId.setNodeValue( id, Types.Float,
                         makeRankArray( id.length, -1, -1, -1, -1 ) );
      ndId.addAttribute( "axis", makeRankArray( 2, -1, -1, -1, -1 ),
                         Types.Int, makeRankArray( 1, -1, -1, -1, -1 ) );
      ndId.setLinkHandle( "axis2" + kk );
      return "axis2" + kk;

    }
    else return null;
  }


  /**
   * Makes a node tied to the parent that has the xvals("centered with
   * offset" and the units longname and histogram offset fields
   * set. No linking *nxdetector is any parent- it could be a monitor
   */
  public static NxWriteNode makeXvalnode( DataSet DS, int beginIndex,
                                          int endIndex,NxWriteNode nxdetector){
    Data D = DS.getData_entry( beginIndex );
    
    float[] xvals = D.getX_scale().getXs();
    float[] yvals = D.getY_values();
    int hist = 0;
    
    if( xvals.length != yvals.length )
      hist = 1;
    float[] xcenter = new float[xvals.length - hist];
    int i;
    
    for( i = 0; i < xvals.length - hist; i++ ){
      xcenter[i] = ( xvals[i] + xvals[i + hist] ) / 2;
    }
    float offset = xcenter[0] - xvals[0];
    NxWriteNode ntof = nxdetector.newChildNode( "time_of_flight", "SDS" );
    
    ntof.setNodeValue( xcenter, Types.Float,
                       makeRankArray( xcenter.length, -1, -1, -1, -1 ) );
    if( DS.getX_units() != null ){
      ntof.addAttribute("units", ( DS.getX_units() + (char)0 ).getBytes(),
                        Types.Char,
                        makeRankArray(DS.getX_units().length()+1,-1,-1,-1,-1));
    }

    ntof.addAttribute( "long_name", ( DS.getX_label() + (char)0 ).getBytes(),
                       Types.Char,
                       makeRankArray(DS.getX_label().length()+1,-1,-1,-1,-1));
    float[] offs = new float[1];
    
    offs[0] = offset;
    if( offset != 0 )
      ntof.addAttribute( "histogram_offset", offs, Types.Float,
                         makeRankArray( 1, -1, -1, -1, -1 ) );
    return ntof;
  }


  /**
   * first negative in list is end --> [n1,n2,n3..]
   */
  public static int[] makeRankArray( int n1, int n2, int n3, int n4, int n5 ){
    int n = 0;
    
    if( n1 < 0 ) 
      n1 = 0;
    else if( n2 < 0 ) 
      n = 1;
    else if( n3 < 0 ) 
      n = 2;
    else if( n4 < 0 ) 
      n = 3;
    else if( n5 == 0 ) 
      n = 4;
    else 
      n = 5;
    int[] Result = new int[ n];
    
    /*if( n1 < 0 ){
      // do nothing
    }else if( n2 < 0 ) 
      Result[0] = n1;
    else if( n3 < 0 ) 
      Result[1] = n2;
    else if( n4 < 0 ) 
      Result[2] = n3;
    else if( n5 == 0 ) 
      Result[3] = n4;
    else 
      Result[4] = n5;
    */
     if( n1 >=0)
        Result[0] = n1;

     if( n2 >=0)
        Result[1] = n2;
     if( n3 >=0)
        Result[2] = n3;
     if( n4 >=0)
        Result[3] = n4;
     if( n5 >=0)
        Result[4] = n5;
    return 
      Result;

  }


  /**
   * Sets special monitor names associated with an instrType for
   * writing and suggestion for reading
   *
   * @param instrType the instrument type
   * @param GroupIndex The position of the Monitor in the Monitor data
   * set
   *
   * @return The string representation of the monitor name
   */
  public String getMonitorName( int instrType, int GroupIndex ){
    if( instrType == getIsawInstrNum( "TOFNPD" ) ){
      if( GroupIndex == 0 )
        return "upstream";
      else if( GroupIndex == 1 )
        return "downstream";
      
    }
    return "monitor" + GroupIndex;
    
  }


   /**
    * Returns axis names that are linked between NXdata and NXdetector
    */
  public String getLinkAxisName( int instrType, int axisNum ){
    if( instrType == getIsawInstrNum( "TOFNDGS" ) ){
      if( axisNum == 1 ) 
        return "time_of_flight";
      else 
        return "polar_angle";//changed from phi
      
    }else if( axisNum == 1 ) 
      return "time_of_flight";
    else if( axisNum == 2 ) 
      return "id";
    
    return "";

  }


   /**
    * Test program for procedures in this module
    */
  public static void main( String args[] ){
    Inst_Type X = new Inst_Type();
    char option = 0;
    
    while( option != 'x' ){
      System.out.println( "Enter option" );
      System.out.println( " a) Enter number" );
      System.out.println( " b) Enter String" );
      option = 0;
      try{
        while( option < 32 )
          option = ( char )System.in.read();
      }catch( IOException s ){
        option = 0;
      }
      String S = "";
      char c = 0;
      
      try{
        while( c < 33 )
          c = ( char )System.in.read();
        while( c >= 32 ){
          S = S + c;
          c = ( char )System.in.read();
        }
      }catch( IOException s ){
        // let it drop on the floor
      }
      if( option == 'a' ){
        int num = -1;
        
        try{
          num = ( new Integer( S ) ).intValue();
          System.out.println( "Result=" + X.getNexAnalysisName( num ) );
        }catch( Exception s ){
          System.out.println( "Error=" + s );
        }
        
      }else if( option == 'b' ){
        System.out.println( "Res=" + X.getIsawInstrNum( S ) );
      }
      
    }//while

  }

}
