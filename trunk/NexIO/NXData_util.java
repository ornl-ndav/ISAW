/*
 * File:  NXData_util.java 
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
 * Revision 1.17  2003/10/15 03:05:47  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.16  2003/06/18 20:33:42  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.15  2003/03/05 20:51:44  pfpeterson
 * Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 * Revision 1.14  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.13  2002/11/20 16:14:38  pfpeterson
 * reformating
 *
 * Revision 1.12  2002/07/29 18:50:37  rmikk
 * Added a total count attribute if there is none
 * Eliminated the Time field type attribute
 * Group ID's now start at 1 for those without defined ID's
 *
 * Revision 1.11  2002/06/19 15:04:33  rmikk
 * Eliminated commented out code and fixed code spacing
 * and alignment.
 *
 * Revision 1.10  2002/04/01 20:16:42  rmikk
 * Added routines to determine the name of the polar_angle and azimuthal angle.
 * Reads in the slot, crate, and input values. Also the Data Label attribute is set.
 * The Group ID is now used if it is in the NeXus File.
 * Raw Angle is now converted to degrees
 * Code moved to other routines has been deleted
 * Set other Attribute method moved out of a loop
 *
 * Revision 1.9  2002/03/18 21:09:15  dennis
 * Now does getDimension immediately after it's set.
 *
 * Revision 1.8  2002/03/13 16:24:21  dennis
 * Converted to new abstract Data class.
 *
 * Revision 1.7  2002/02/26 15:37:40  rmikk
 * Added Code to incorporate the TOFNDGS instrument type
 * Added a debug field
 * Added a timeField field to put into the TimeField attribute.  All NXdata are
 *    merged.  To unmerge, extract with the TimefieldType  attribute
 * Several utility routines were made static
 * Previous NXattributes have become NeXus fields
 * When retrieving the data, getDimension is used.  The axes are matched to the
 *   dimensions.  There is now no need to determine if the axes are for
 *   histogram or  functions.
 *
 */
package NexIO;


//import NexIO.NDS.*;
import DataSetTools.dataset.*;
import DataSetTools.util.StringUtil;
import DataSetTools.viewer.*;
import DataSetTools.math.*;
//import NdsSvNode;

/**
 * A utility package used by many NxData implementers
 */
public class NXData_util{
  String errormessage;
  boolean debug = false;
  int timeFieldType;

  public NXData_util(){
    timeFieldType = -1;
    errormessage = "";
  }

  public void setTimeFieldType( int time_field_type ){
    timeFieldType = time_field_type;
  }


   /**
    * Returns error or warning messages of "" if none
    */
  public String getErrorMessage(){
    return errormessage;
  }


  /**
   * Converts an object into a float array(if possible) or null.
   *@see #getErrorMessage()
   */
  public static float[] Arrayfloatconvert( Object X ){
    int i;
    //errormessage = "";
    float res[];
    
    if( X == null ){
      return null;
    }else if( X instanceof int[] ){
      int b[];
      
      b = ( int[] )X;
      res = new float[b.length ];
      for( i = 0; i < b.length; i++ )
        res[ i ] = ( float )( b[i] );
    }else if( X instanceof byte[] ){
      byte b[];
      
      b = ( byte[] )X;
      res = new float[ b.length ];
      for( i = 0; i < b.length; i++ )
        res[ i ] = ( float )( b[i] );
      
    }else if( X instanceof long[] ){
      long b[];

      b = ( long[] )X;
      res = new float[ b.length ];
      for( i = 0; i < b.length; i++ )
        res[ i ] = ( float )( b[i] );
      
    }else if( X instanceof short[] ){
      short b[];

      b = ( short[] )X;
      res = new float[ b.length ];
      for( i = 0; i < b.length; i++ )
        res[ i ] = ( float )( b[i] );
      
    }else if( X instanceof float[] ){
      float b[];
      
      b = ( float[] )X;
      return b;
    }else if( X instanceof double[] ){
      double b[];
      
      b = ( double[] )X;
      res = new float[ b.length ];
      for( i = 0; i < b.length; i++ )
        res[ i ] = ( float )b[i];
    }else if( X instanceof Float ){
      float[] Res = new float[1];
      
      Res[0] = ( ( Float )X ).floatValue();
      return Res;
    }else if( X instanceof Integer ){
      float[] Res = new float[1];
      
      Res[0] = ( ( Integer )X ).floatValue();
      return Res;
    }else if( X instanceof Long ){
      float[] Res = new float[1];
      
      Res[0] = ( ( Long )X ).floatValue();
      return Res;
    }else{
      String S = "";
      
      S = X.getClass().toString();
      if( X instanceof Object[] )
        S = ( ( ( Object[] )X )[ 0 ] ).getClass().toString();
      
      return null;
    }
    
    return res;
    
  }


  /**
   * gets the index-th element of an array X( if possible) or 0.
   */
  public static float getfloatEntry( Object X, int index ){
    //errormessage = "";
    if( X instanceof int[] ){
      int b[];
      
      b = ( int[] )X;
      if( ( b.length <= index ) || ( index < 0 ) ){
        //errormessage = "index out of range"; 
        return Float.NaN;
      }
      return new Integer( b[ index ] ).floatValue();
    }else if( X instanceof byte[] ){
      byte b[];
      
      b = ( byte[] )X;
      if( ( b.length <= index ) || ( index < 0 ) ){
        //errormessage = "index out of range"; 
        return Float.NaN;
      }
      return new Byte( b[ index ] ).floatValue();
      
    }else if( X instanceof long[] ){
      long b[];
      
      b = ( long[] )X;
      if( ( b.length <= index ) || ( index < 0 ) ){
        //errormessage = "index out of range"; 
        return Float.NaN;
      }
      return new Long( b[ index ] ).floatValue();
      
    }else if( X instanceof float[] ){
      float b[];
      
      b = ( float[] )X;
      if( ( b.length <= index ) || ( index < 0 ) ){
        //errormessage = "index out of range"; 
        //System.out.println("Am returning NaN");
        return Float.NaN;
      }
      return new Float( b[ index ] ).floatValue();
      
    }else if( X instanceof double[] ){
      double b[];

      b = ( double[] )X;
      if( ( b.length <= index ) || ( index < 0 ) ){
        //errormessage = "index out of range"; 
        return Float.NaN;
      }
      return new Double( b[ index ] ).floatValue();
      
    }else{
      return Float.NaN;
    }
  }


  private String getPhiName( NxNode detNode ){
    NxNode nx = detNode.getChildNode( "two_theta" );
    
    if( nx != null )
      return "two_theta";
    
    nx = detNode.getChildNode( "polar_angle" );
    if( nx != null )
      return "polar_angle";
    
    return "phi";
    
  }


  private String getThetaName( NxNode detNode ){
    if( getPhiName( detNode ).equals( "two_theta" ) )
      return "phi";
    if( getPhiName( detNode ).equals( "polar_angle" ) )
      return "azimuthal_angle";
    return "theta";
  }


  /**
   * get attributes that are connected to the NXdetector node
   * corresponding to this node and assigns them to the appropriate
   * data blocks
   */
  public void setOtherAttributes( NxNode detNode, DataSet DS, int start_index,
                                  int end_index ){
    NxData_Gen DD = new NxData_Gen();
    float solidAngle[]   = null;
    float distance[]     = null;
    float theta[]        = null;
    float Raw_Angle[]    = null;
    float efficiency;
    float Delta_2Theta[] = null;
    float Total_Count[]  = null;
    float phi[]          = null;
    float slot[]         = null;
    float crate[]        = null;
    float input[]        = null;
    int   Time_Field[]   = null;
    int   Group_ID[]     = null;

    efficiency = -1;
    NxNode nx, ndis;
    
    if( detNode == null )
      return;
    ndis = detNode.getChildNode( "distance" );
    if( ndis == null )
      return;
    Object X = ( ndis.getNodeValue() );
    
    if( X == null )
      return;
    if( !( X instanceof float[] ) )
      return;
    distance = ( float[] )X;
    String S3 = DD.cnvertoString( ndis.getAttrValue( "units" ) );

    if( S3 != null )
      UnitsAdjust( distance, "m", S3 );
    String phin, thetn;

    phin = this.getPhiName( detNode );
    thetn = this.getThetaName( detNode );
    nx = detNode.getChildNode( thetn );
    if( nx != null ){
      X = Arrayfloatconvert( nx.getNodeValue() );
      if( X != null ) if( X instanceof float[] ){
        theta = ( float[] )X;
        String S = DD.cnvertoString( nx.getAttrValue( "units" ) );
        
        if( S != null )
          UnitsAdjust( theta, "radians", S );
      }

    }

    nx = detNode.getChildNode( phin );
    if( nx != null ){
      X = Arrayfloatconvert( nx.getNodeValue() );
      if( X != null ) if( X instanceof float[] ){
        phi = ( float[] )X;
        String S = DD.cnvertoString( nx.getAttrValue( "units" ) );
        
        if( S != null )
          UnitsAdjust( phi, "radians", S );
      }

    }

    NxNodeUtils nut = new NxNodeUtils();
    
    nx = ( detNode.getChildNode( "solid_angle" ) );
    
    if( nx != null ){
      X = Arrayfloatconvert( nx.getNodeValue() );

      if( X != null ){
        solidAngle = ( float[] )( X );
        String S = DD.cnvertoString( nx.getAttrValue( "units" ) );
        
        if( S != null )
          UnitsAdjust( solidAngle, "radians", S.trim() );
        
      }
    }
    
    nx = ( detNode.getChildNode( "integral" ) );
    if( nx != null ){
      X = Arrayfloatconvert( nx.getNodeValue() );
      if( X != null )
        Total_Count = ( float[] )X;
    }
    
    /*  X=(ndis.getAttrValue("delta_2theta"));
        if( X != null)if( X instanceof float[])
        Delta_2Theta =(float[])X;
        
        X = ndis.getAttrValue("time_field_type");
        if( X!= null)if( X instanceof int[])
        Time_Field =(int[])X;
    */
    nx = detNode.getChildNode( "id" );
    if( nx != null )if( nx.getNodeValue() != null ){
      if( nx.getNodeValue()  instanceof int[] )
        Group_ID = ( int[] )( nx.getNodeValue() );
    }

    nx = detNode.getChildNode( "efficiency" );
    if( nx != null ){
      X = nx.getNodeValue();
      Float ff = DD.cnvertoFloat( X ); //???????????
      
      if( ff != null )
        if( ff.floatValue() != Float.NaN )
          efficiency = ff.floatValue();
    }

    nx = detNode.getChildNode( "raw_angle" );
    if( nx != null ){
      X = Arrayfloatconvert( nx.getNodeValue() );
      if( X != null ){
        Raw_Angle = ( float[] )( X );
        String S = DD.cnvertoString( nx.getAttrValue( "units" ) );
        
        if( S != null ){
          UnitsAdjust( Raw_Angle, "radians", S );
        }
      }
    }
    
    nx = detNode.getChildNode( "slot" );
    if( nx != null ){
      X = Arrayfloatconvert( nx.getNodeValue() );
      if( X != null )
        slot = ( float[] )( X );
    }
    nx = detNode.getChildNode( "crate" );
    if( nx != null ){
      X = Arrayfloatconvert( nx.getNodeValue() );
      if( X != null )
        crate = ( float[] )( X );
      
    }
    nx = detNode.getChildNode( "input" );
    if( nx != null ){
      X = Arrayfloatconvert( nx.getNodeValue() );
      if( X != null )
        input = ( float[] )( X );
    }

    //newData.setGroup_ID( index );
    
    for( int index = start_index; index < end_index; index++ ){
      Data DB = DS.getData_entry( index );

      if( Group_ID != null ){
        DB.setGroup_ID( Group_ID[index - start_index] );
        DB.setAttribute( new StringAttribute( Attribute.LABEL,
                             "Group " + Group_ID[index - start_index] ) );
      }else{
        DB.setGroup_ID( index );
        DB.setAttribute( new StringAttribute( Attribute.LABEL,
                                              "Group " + index ) );
      }
      
      if( Raw_Angle != null )
        DB.setAttribute( new FloatAttribute( Attribute.RAW_ANGLE, 
                (float)(Raw_Angle[index-start_index]*180./java.lang.Math.PI)));

      if( solidAngle != null )// if( index <solidAngle.length)
        DB.setAttribute( new FloatAttribute(
                   Attribute.SOLID_ANGLE, solidAngle[ index - start_index] ) );

      if( Total_Count != null )// if( index <Total_Count.length){
        DB.setAttribute( new FloatAttribute(Attribute.TOTAL_COUNT,
                                            Total_Count[index-start_index]));
      else
        DB.setAttribute( new FloatAttribute( Attribute.TOTAL_COUNT,
                                             findTotCountDB(DB)) );

      if( slot != null )// if( index <slot.length)
        if( slot[index - start_index] >= 0 )
          DB.setAttribute( new IntAttribute( Attribute.SLOT,
                                             (int)(slot[index-start_index])));

      if( crate != null )// if( index <crate.length)
        if( crate[index - start_index] >= 0 )
          DB.setAttribute( new IntAttribute( Attribute.CRATE,
                                             (int)(crate[index-start_index])));

      if( input != null )// if( index <input.length)
        if( input[index - start_index] >= 0 )
          DB.setAttribute( new IntAttribute( Attribute.INPUT,
                                             (int)(input[index-start_index])));

      float d, p, t, s,ra;
      int   T;

      T = 0;
      d = p = t = 0.0f;
      if( distance != null )if( index - start_index < distance.length )
        d = distance[ index - start_index ];
      
      if( theta != null ) if( index - start_index < theta.length )
        t = theta[ index - start_index ];
      if( phi != null ) if( index - start_index < phi.length )
        p = phi[index - start_index ];
      
      Position3D p3d = new Position3D();
      
      p3d.setSphericalCoords( d, t, p );
      DB.setAttribute( new DetPosAttribute(
                                Attribute.DETECTOR_POS, convertToIsaw(d,p,t)));
    }

    /*      //May include later
            if(Delta_2Theta != null) 
            if( index <Delta_2Theta.length)
            s = Delta_2Theta[ index ]; 
            else s = 0.0f;
            if( Delta_2Theta != null)
            newData.setAttribute( new FloatAttribute(Attribute.DELTA_2THETA,
            s));
            
            
            
            if( efficiency >= 0.0f)
            newData.setAttribute( new FloatAttribute( 
            Attribute.EFFICIENCY_FACTOR,
            efficiency));
    */
  }

  private float  findTotCountDB(Data DB){
    float X = 0.0f;
    float[] yVals = DB.getY_values();
    if( yVals == null)
      return X;
    for( int i= 0; i < yVals.length; i++)
      X+= yVals[i];
    return X;
  }
    
  /**
   * Fills out an existing DataSet with information from the NXdata
   * section of a Nexus datasource
   *
   * @param node the current node positioned to an NXdata part of a
   * datasource
   * @param instrNode The corresponding NXinstrument node for the
   * NXentry
   * @param axis1 Name for axis 1
   * @param axis2 Name for axis 2
   * @param dataname the name of the NxData's (signal=1) field.
   * @param DS the existing DataSet that is to be filled out
   *
   * @return True if there an error ocurred during processing.
   */
   public boolean processDS( NxNode node, NxNode instrNode, String axis1,
                             String axis2, String dataname, DataSet DS ){
     errormessage = "";
     if( debug )
       System.out.println( "NXdata:Axes=" + axis1 + "," +
                           axis2 + "," + dataname );
     
     NxNode Ax1nd = ( NxNode )( node.getChildNode( axis1 ) );
     
     if( Ax1nd == null ){
       errormessage = node.getErrorMessage() + " for field " + axis1;
       return true;
     }

     NxNode Ax2nd = null;
     
     if( axis2 != null )
       Ax2nd = ( NxNode )( node.getChildNode( axis2 ) );
     
     if( debug )
       System.out.println( "ere get data node ptr" );
     
     NxNode datand = ( NxNode )( node.getChildNode( dataname ) );
     
     if( datand == null ){
       errormessage = node.getErrorMessage() + " for field " + dataname;
       return true;
     }

     float[] Ax1 = Arrayfloatconvert( Ax1nd.getNodeValue() );
     float[] fdata = Arrayfloatconvert( datand.getNodeValue() );
     int[] ndims = datand.getDimension();
     float[] Ax2;

     if( Ax2nd == null ){
       Ax2 = new float[1];
       Ax2[0] = 1;
     }else
       Ax2 = Arrayfloatconvert( Ax2nd.getNodeValue() );

     if( Ax1 == null ){
       errormessage = node.getErrorMessage() + " for field " + axis1;
       return true;
     }
     
     if( debug )
       System.out.println( "After get data node values" );
     if( ( fdata == null ) ){
       errormessage = node.getErrorMessage() + " for field " + dataname;
       return true;
     }
     
     float[] phi = null;
     float[] xvals = null;
     
     if( ndims == null ){
       errormessage = "Improper Node-No dimensions";
       return true;
     }
     if( ndims.length < 1 ){
       errormessage = "Improper Node-Null dimensions";
       return true;
     }
     int nx, ny;

     nx = ny = -1;
     if( ndims.length == 1 ){
       nx = ndims[0];
       ny = 1;
       if( java.lang.Math.abs( Ax1.length - nx ) > 1 ){
         if( Ax2 != null ){
           if( java.lang.Math.abs( Ax2.length - nx ) > 1 ){
             errormessage = "Dimensions do not match";
             return true;
           }else{
             float[] sv = Ax1;

             Ax1 = Ax2;
             Ax2 = sv;
             NxNode NN = Ax1nd;
             
             Ax1nd = Ax2nd;
             Ax2nd = NN;
           }
         }else{
           errormessage = "Dimensions do not match";
           return true;
         }
       }
     }else if( ndims.length == 2 ){
       nx = ndims[1];
       ny = ndims[0];
       if( java.lang.Math.abs( Ax1.length - nx ) > 1 ){
         if( Ax2 != null ){
           if( java.lang.Math.abs( Ax2.length - nx ) > 1 ){
             errormessage = "Dimensions do not match";
             return true;
           }else{
             float[] sv = Ax1;
             
             Ax1 = Ax2;
             Ax2 = sv;
             NxNode NN = Ax1nd;
             
             Ax1nd = Ax2nd;
             Ax2nd = NN;
           }
         }else{
           errormessage = "Dimensions do not match";
           return true;
         }
       }
       
     }else if( ndims.length > 2 ){
       nx = ndims[ndims.length - 1];
       ny = ndims[0];
       for( int kk = 1; kk < ndims.length - 1; kk++ )
         ny = ny * ndims[kk];
       if( java.lang.Math.abs( Ax1.length - nx ) > 1 ){
         if( Ax2 != null ){
           if( java.lang.Math.abs( Ax2.length - nx ) > 1 ){
             errormessage = "Dimensions do not match";
             return true;
           }else{
             float[] sv = Ax1;
             
             Ax1 = Ax2;
             Ax2 = sv;
             NxNode NN = Ax1nd;
             
             Ax1nd = Ax2nd;
             Ax2nd = NN;
           }
         }else{
           errormessage = "Dimensions do not match";
           return true;
         }
       }
       
     }

     // get nodes to correspond to dimensions
     
     xvals = Ax1;
     
     phi = Ax2;
     
     Object X = Ax1nd.getAttrValue( "long_name" );
     String S;
     NxData_Gen DD = new NxData_Gen();
     
     if( X != null ){
       S = DD.cnvertoString( X );
       if( S != null )
         DS.setX_label( S );
     }

     X = Ax1nd.getAttrValue( "units" );
     
     if( X != null ){
       S = DD.cnvertoString( X );
       if( S != null )
         DS.setX_units( S );
     }
     if( Ax2nd != null )
       X = Ax2nd.getAttrValue( "long_name" );
     if( X != null ){
       S = DD.cnvertoString( X );
       if( S != null )
         DS.setY_label( S );
     }

     X = datand.getAttrValue( "units" );
     if( X != null ){
       S = DD.cnvertoString( X );
       if( S != null )
         DS.setY_units( S );
     }

     if( debug )
       System.out.println( "NXdata: dimensions=" + xvals.length
                           + "," + phi.length + "," + fdata.length );

     xvals = HistogramOffsetadjust( xvals, Ax1nd );
     phi = HistogramOffsetadjust( phi, Ax2nd );
     if( xvals == null )
       return false;
     int xlength = xvals.length;
     int ylength = phi.length;
     int datalength = fdata.length;
      
     if( datalength <= 0 ){
       errormessage = "No Data -0 length";
       return true;
     }

     if( ylength <= 0 ){
       errormessage = "Axis 2 has no length";
       DataSetTools.util.SharedData.addmsg( errormessage );
       return true;
     }
     xlength = nx;
     
     int i;
     Data newData;
     boolean done = false;
     float yval = fdata[ 0 ];
     float yvals[];

     if( debug )
       System.out.println( "DIMENSIONS=" + StringUtil.toString( ndims ) );

     yvals = new float[ xlength];
     
     int group_id = 0;
     String ax1Link = Ax1nd.getLinkName();
     String ax2Link;

     if( Ax2nd != null )
       ax2Link = Ax2nd.getLinkName();
     else 
       ax2Link = null;

     NxNode detNode = getCorrespNxDetector( node, instrNode, DS, 
                                            ax1Link, ax2Link );
   
     if( debug )
       System.out.println( "NXdata:ere put into dataset" + xlength );

     errormessage = "";
     int startIndex = DS.getNum_entries();
     
     for( group_id = 0; group_id < ny; group_id++ ){
       System.arraycopy( fdata, group_id * xlength, yvals, 0, xlength );
       int xx = DS.getNum_entries() + 1;
       
       newData = Data.getInstance( new VariableXScale( xvals ), yvals, xx );
       
       //  if( timeFieldType >= 0 )
       //    newData.setAttribute( new IntAttribute( 
       //                           Attribute.TIME_FIELD_TYPE, timeFieldType ) );

       DS.addData_entry( newData );
     }
     int endIndex = DS.getNum_entries();

     if( detNode != null )
       setOtherAttributes( detNode, DS, startIndex, endIndex );

     if( debug )
       System.out.println( "After Save 1 NXdata, errormessage=" 
                           + errormessage + ",ngroups=" + ny );
     
     return false;
   }

  public static DetectorPosition convertToIsaw( float distance, float phi,
                                                float theta ){
    Position3D p3 = new Position3D();
    float coords[];
    
    coords = Types.convertFromNexus( distance, phi, theta );
    
    p3.setSphericalCoords( coords[0], coords[2], coords[1] );
    return new DetectorPosition( p3 );
    
  }

  /**
   * returns r, theta, phi
   */
  public float[] converToNex( DetectorPosition DP ){
    return DP.getSphericalCoords();
  }


  private float[] HistogramOffsetadjust( float[] xvals, NxNode Ax1nd ){
    if( xvals == null )
      return null;
    if( Ax1nd == null )
      return xvals;
    Object X = Ax1nd.getAttrValue( "histogram_offset" );
    float histogram = 0;
    Float F = new NxData_Gen().cnvertoFloat( X );
    
    if( F != null ){
      histogram = F.floatValue();
      float[] xvals1 = new float[xvals.length + 1];
      
      xvals1[0] = xvals[0] - histogram;
      
      for( int i = 0; i < xvals.length; i++ )
        xvals1[i + 1] = 2 * xvals[i] - xvals1[i];
      
      xvals = xvals1;
    }
    return xvals;
  }


  /**
   * Gets the corresponding NxDetector node for this NxData block
   */
  public NxNode getCorrespNxDetector( NxNode node,NxNode instrNode,DataSet DS,
                                      String ax1Link, String ax2Link ){
    //Check if there is a NxDetector class in NxData
    for( int i = 0; i < node.getNChildNodes(); i++ ){
      NxNode nx = node.getChildNode( i );

      if( nx.getNodeClass().equals( "NXdetector" ) )
        return nx;
    }

    //otherwise have NxInstrument find the Nxdetector with appropriate 
    //link names
    
    return new NxInstrument().matchNode( instrNode, ax1Link, ax2Link );
    
  }


  public static void UnitsAdjust(float[] d, String newUnits, String oldUnits){
    if( oldUnits == null )
      return;
    if( d == null )
      return;
    if( d.length < 1 )
      return;
    
    float f = NxNodeUtils.getConversionFactor( oldUnits, newUnits );
    if( f == Float.NaN )
      return;
    
    for( int i = 0; i < d.length; i++ )
      d[i] = f * d[i];
  }

  /**
   * Test program for NXData_util
   */
  public static void main( String args[] ){
    /*NDSClient nds;String filename = "lrcs3000.hdf";
      nds = new NDSClient( "mandrake.pns.anl.gov" , 6008 , 6081998 );
      nds.connect(  );
      NdsSvNode node = new NdsSvNode( filename , nds );
      NdsSvNode node1 = ( NdsSvNode )( node.getChildNode( "Histogram1" ) );
      node1 = ( NdsSvNode )( node1.getChildNode( "data" ) );
      System.out.println( "a" );
      NXData_util DD = new NXData_util();
      System.out.println( "c" );
      DataSet DS = new DataSet( "","" );
      //if( !DD.processDS(  node1 , "time_of_flight" , "phi" , "data" ,  DS ) )
      //    System.out.println( "error" + DD.getErrorMessage() );
      // else
      //  { ViewManager view_manager = new ViewManager(  DS ,  DataSetTools.viewer.IViewManager.IMAGE );
      
      //     }
      */}
}
