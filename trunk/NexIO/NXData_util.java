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
 * Revision 1.6  2001/07/26 19:21:18  rmikk
 * Fixed a null pointer exception case that arise for files
 * with missing data
 *
 * Revision 1.5  2001/07/26 14:56:26  rmikk
 * Fixed utility to NOT change group_id's from defaults
 * unless information is in the nexus file
 *
 * Revision 1.4  2001/07/26 13:52:42  rmikk
 * Removed Dependence on NDS package
 *
 * Revision 1.3  2001/07/24 20:02:58  rmikk
 * Separated out common functionality so it can be used
 * by NxMonitor
 *
 * Revision 1.2  2001/07/17 13:53:12  rmikk
 * Fixed error so group numbers increased
 *
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
 *
 */
package NexIO;
//import NexIO.NDS.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.math.*;
//import NdsSvNode;

/** A utility package used by many NxData implementers
 */
public class NXData_util
 {String errormessage;
  boolean debug=false;
  int timeFieldType;
  public NXData_util()
   { timeFieldType = -1;
     errormessage = "";
    }

  public void setTimeFieldType( int time_field_type)
    {timeFieldType = time_field_type;}
  /**Returns error or warning messages of "" if none
 */
  public String getErrorMessage()
   {
     return errormessage;
   }

 /** Converts an object into a float array(if possible) or null.
 *@see #getErrorMessage()
*/
  public static float[] Arrayfloatconvert( Object X )
    {int i;
      //errormessage = "";
     float res[];
     if( X == null)
        return null;
     else if( X instanceof int[] ) 
       {int b[];  
        b = ( int[] )X; 
        res = new float[b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] =  (float)(b[i]);//getfloatEntry( X , i );
       }
     else if( X instanceof byte[] )
       {byte b[];  
        b = ( byte[] )X; 
        res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] = (float)(b[i]);// getfloatEntry( X , i );

       }
    
     else if( X instanceof long[] )
       {long b[];  
        b = ( long[] )X; 
        res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] =  (float)(b[i]);//getfloatEntry( X , i );

       }
      else if( X instanceof short[] )
       {short b[];  
        b = ( short[] )X; 
        res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] = (float)(b[i]);//getfloatEntry( X , i );

       }

     else if( X instanceof float[] )
       {float b[];  
        b = ( float[] )X; 
        return b;
        /*res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] = getfloatEntry( X , i );
       */

       }


     else if( X instanceof double[] )
       {double b[];  
        b = ( double[] )X; 
        res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] = (float)b[i];//getfloatEntry( X , i );
       }
     else
       {String S = "";
        S = X.getClass(  ).toString( );
        if(  X instanceof Object[] )
          S  = ( ( ( Object[] )X )[ 0 ] ).getClass().toString();
       //errormessage = "Not an Array data type: " + X.getClass();
        return null;
         }
     return res;
   
    }

 /** gets the index-th element of an array X( if possible) or
* 0. 
*/
  public static float getfloatEntry( Object X ,  int index )
    {//errormessage = "";
     if( X instanceof int[] )
      { int b[];  b = ( int[] )X; 
        if( ( b.length <= index )||( index < 0 ) )
            {//errormessage = "index out of range"; 
             return Float.NaN;}
        return new Integer( b[ index ] ).floatValue();
       }
     else if( X instanceof byte[] )
      { byte b[];  b = ( byte[] )X; 
        if( ( b.length <= index )||( index < 0 ) )
            {//errormessage = "index out of range"; 
             return Float.NaN;}
         return new Byte( b[ index ] ).floatValue();

       }
    else if( X instanceof long[] )
      { long b[];  b = ( long[] )X; 
        if( ( b.length <= index )||( index < 0 ) )
            {//errormessage = "index out of range"; 
             return Float.NaN;}
         return new Long( b[ index ] ).floatValue();

       }
     else if( X instanceof float[] )
      { float b[];  b = ( float[] )X; 
        if( ( b.length <= index )||( index < 0 ) )
            {//errormessage = "index out of range"; 
           //System.out.println("Am returning NaN");
             return Float.NaN;}
         return new Float( b[ index ] ).floatValue();

       }


     else if( X instanceof double[] )
      { double b[];  b = ( double[] )X; 
        if( ( b.length <= index )||( index < 0 ) )
            {//errormessage = "index out of range"; 
             return Float.NaN;}
         return new Double( b[ index ] ).floatValue();

       }
     else
       {//errormessage = "Improper data type-not an Array";
        return Float.NaN;
       }
     } 

/** get attributes that are connected to the distance field of this
*  node and assigns them to the appropriate data blocks
*/
public static void setOtherAttributes( NxNode detNode ,Data newData, int index)
 {     NxData_Gen DD = new NxData_Gen();       
     float solidAngle[], distance[], theta[],Raw_Angle[],efficiency,
              Delta_2Theta[], Total_Count[],phi[];
     int Time_Field[],Group_ID[];
     solidAngle = distance = theta = Raw_Angle= Delta_2Theta = Total_Count = null;
     phi = null;
     Time_Field = Group_ID=  null;
     efficiency = -1;
     NxNode nx,ndis;
     if(detNode ==null) return;
     ndis = detNode.getChildNode( "distance");
     if( ndis == null) return;
     Object X = (ndis.getNodeValue());
     if( X == null) return;
     if( !(X instanceof float[])) return;
     distance=(float[])X;
     UnitsAdjust( distance, "m",(String)( ndis.getAttrValue("units")));
     nx = detNode.getChildNode( "theta");
     if( nx != null)
        {X = Arrayfloatconvert(nx.getNodeValue());
         if( X != null) if( X instanceof float[])
            {theta = (float[] ) X;
             UnitsAdjust( theta,"radians",(String)( nx.getAttrValue("units")));
            }
         
        } 
     nx = detNode.getChildNode( "phi");
     if( nx != null)
        {X = Arrayfloatconvert(nx.getNodeValue());
         if( X != null) if( X instanceof float[])
            {phi = (float[] ) X;
             UnitsAdjust( phi,"radians",(String)( nx.getAttrValue("units")));
            }
         
        } 
	
      nx=( detNode.getChildNode("solid_angle"));
      if( nx != null)
        { X=Arrayfloatconvert(nx.getNodeValue());
          if( X != null)
	    {solidAngle =(float[])(X);
             UnitsAdjust( solidAngle,"radians", (String)(nx.getAttrValue("units")));
            }
         }

       nx=( detNode.getChildNode("integral"));
       if( nx != null)
         { X =Arrayfloatconvert(nx.getNodeValue());
           if( X != null)
	     Total_Count =(float[])X;
          }

     /*  X=(ndis.getAttrValue("delta_2theta"));
         if( X != null)if( X instanceof float[])
	    Delta_2Theta =(float[])X;

       X = ndis.getAttrValue("time_field_type");
        if( X!= null)if( X instanceof int[])
            Time_Field =(int[])X;
     */
       nx =  detNode.getChildNode("group_id");
       if( nx!= null)if( nx.getNodeValue() !=null) 
         if( nx.getNodeValue()  instanceof int[])
             Group_ID =(int[])X;
         
        nx = detNode.getChildNode( "efficiency");
        if( nx != null)
          {X = nx.getNodeValue();
           Float ff = DD.cnvertoFloat( X ); //???????????
           if( ff != null)
             if( ff.floatValue() != Float.NaN)
                efficiency = ff.floatValue();
           }
        nx =  detNode.getChildNode("raw_angle");
        if(nx !=null)
           { X =Arrayfloatconvert(nx.getNodeValue());
             if( X!= null)
                {Raw_Angle =(float[])(X);
                 UnitsAdjust( Raw_Angle,"radians", (String)(nx.getAttrValue("units")));
                }
            }

        //newData.setGroup_ID( index );
        if( Group_ID != null)
           if( index < Group_ID.length)
                newData.setGroup_ID( Group_ID[index] );
               
        float d,p,t,s,ra;
        int T;
        d= p = t = s =ra =0;
        T = 0;
        if( distance != null)if( index <distance.length)
               d= distance[ index];
       
       if( theta != null ) if( index < theta.length)
              t = theta[ index];
       if( phi != null) if( index < phi.length)
              p = phi[index];
        /*T=0;
        if(Time_Field != null) if( index <Time_Field.length)
            T = Time_Field[ index ];
        if( Time_Field != null)
           newData.setAttribute( new IntAttribute(Attribute.TIME_FIELD_TYPE,
                                     T));
        */
         if(Raw_Angle != null) if( index <Raw_Angle.length)
            ra = Raw_Angle[ index ];
        if( Raw_Angle != null)
           newData.setAttribute( new FloatAttribute(Attribute.RAW_ANGLE,
                                     ra));
        if(solidAngle != null) if( index <solidAngle.length)
            s = solidAngle[ index ];
        if( solidAngle != null)
           newData.setAttribute( new FloatAttribute(Attribute.SOLID_ANGLE,
                                     s));

        if(Total_Count != null) if( index <Total_Count.length)
	 s = Total_Count[ index ]; else s = 0;
        if( Total_Count != null)
           newData.setAttribute( new FloatAttribute(Attribute.TOTAL_COUNT,
                                     s));

/*        if(Delta_2Theta != null) if( index <Delta_2Theta.length)
            s = Delta_2Theta[ index ]; else s = 0.0f;
        if( Delta_2Theta != null)
           newData.setAttribute( new FloatAttribute(Attribute.DELTA_2THETA,
                                     s));
*/
       
        newData.setAttribute( new DetPosAttribute( 
                           Attribute.DETECTOR_POS, 
                        convertToIsaw( d ,p,t))); 
        if( efficiency >= 0.0f)
              newData.setAttribute( new FloatAttribute( 
                               Attribute.EFFICIENCY_FACTOR,
	                       efficiency));
 } 

 /** Fills out an existing DataSet with information from the NXdata
   * section of a Nexus datasource
  *@param node  the current node positioned to an NXdata part of a datasource
  *@param  DS  the existing DataSet that is to be filled out
  *@return  error status: true if there is an error otherwise false
  */
  public boolean processDS(  NxNode node , NxNode instrNode,  String axis1 ,  
                       String axis2 ,  String dataname ,  DataSet DS )
    {errormessage = "";
    if(debug) System.out.println("NXdata:Axes="+axis1+","+axis2+","+dataname);
     NxNode Ax1nd = ( NxNode )( node.getChildNode( axis1 ) );
     if( Ax1nd == null )
        {errormessage = node.getErrorMessage( ) + " for field " + axis1;
         return true;
        }
   
     
     NxNode Ax2nd=null ;
     if( axis2 !=null) Ax2nd= ( NxNode )( node.getChildNode( axis2 ) );
     if( Ax2nd == null )
        {//errormessage = node.getErrorMessage() + " for field " + axis2;
         //return true;
        }
     if(debug)System.out.println( "ere get data node ptr");
     NxNode datand = ( NxNode )( node.getChildNode( dataname ) );
     if( datand == null )
        {errormessage = node.getErrorMessage() + " for field " + dataname;
         return true;
        }
    
     float[] Ax1 = Arrayfloatconvert(Ax1nd.getNodeValue( ));
     float[] fdata = Arrayfloatconvert( datand.getNodeValue());
     float[] Ax2;
     if( Ax2nd==null)
          {Ax2= new float[1];
           Ax2[0]=1;}
      else Ax2= Arrayfloatconvert(Ax2nd.getNodeValue());

     int[] ndims=datand.getDimension();
     if( Ax1 == null )
        {errormessage = node.getErrorMessage() + " for field " + axis1;
         return true;
        }
     //System.out.println( "ProcessDSE" + errormessage );

     if(debug)System.out.println( "After get data node values");
     if((fdata == null ) )
        {errormessage = node.getErrorMessage() + " for field " + dataname;
         return true;
        }
     
     float[] phi=null;
     float[] xvals=null;
     if( ndims == null)
        {errormessage ="Improper Node-No dimensions";
         return true;
         }
     if(ndims.length < 1)
       {errormessage ="Improper Node-Null dimensions";
         return true;
         }
      int nx, ny;
      nx=ny = -1;
      if(ndims.length == 1)
        {nx= ndims[0];
         ny=1;
         if( java.lang.Math.abs(Ax1.length -nx)>1)
             {if(Ax2 != null)
              if( java.lang.Math.abs(Ax2.length -nx)>1)
                {errormessage="Dimensions do not match";
                 return true;
                }
               else
                 {  float[] sv= Ax1;
                    Ax1=Ax2;
                    Ax2 = sv;
                    NxNode NN= Ax1nd;
                    Ax1nd=Ax2nd;
                    Ax2nd= NN;
                  }
               else
                 {errormessage="Dimensions do not match";
                  return true;
                 }
               }
          }
      else if( ndims.length == 2)
         { nx=ndims[1];
           ny=ndims[0];
           if( java.lang.Math.abs(Ax1.length -nx)>1)
             {if(Ax2 != null)
                if( java.lang.Math.abs(Ax2.length -nx)>1)
                  {errormessage="Dimensions do not match";
                    return true;
                   }
                 else
                   {  float[] sv= Ax1;
                      Ax1=Ax2;
                      Ax2 = sv;
                      NxNode NN= Ax1nd;
                      Ax1nd=Ax2nd;
                      Ax2nd= NN;
                    }
               else
                 {errormessage="Dimensions do not match";
                  return true;
                 }
               }
           
         }
      else if( ndims.length >2)
         {nx = ndims[ndims.length-1];
          ny=ndims[0];
          for(int kk=1;kk<ndims.length-1;kk++) ny=ny*ndims[kk];
           if( java.lang.Math.abs(Ax1.length -nx)>1)
             {if(Ax2 != null)
                if( java.lang.Math.abs(Ax2.length -nx)>1)
                  {errormessage="Dimensions do not match";
                    return true;
                   }
                 else
                   {  float[] sv= Ax1;
                      Ax1=Ax2;
                      Ax2 = sv;
                      NxNode NN= Ax1nd;
                      Ax1nd=Ax2nd;
                      Ax2nd= NN;
                    }
               else
                 {errormessage="Dimensions do not match";
                  return true;
                 }
               }
                     
         }
         


         
    // get nodes to correspond to dimensions
          
     xvals = Ax1;
     //if( Ax2 != null) 
     phi = Ax2;


     Object X = Ax1nd.getAttrValue( "long_name" );
     String S; 
     NxData_Gen DD = new NxData_Gen();    
     if( X != null )
      {S  = DD.cnvertoString(X );
       if( S!= null )
          DS.setX_label(S );
       }
     X = Ax1nd.getAttrValue( "units" );
      if( X != null )
      {S  = DD.cnvertoString(X );
       if( S!=  null )
          DS.setX_units(S );
       }
     if( Ax2nd != null) X = Ax2nd.getAttrValue( "long_name" );
          
     if( X != null )
      {S = DD.cnvertoString(X );
       if( S!= null )
          DS.setY_label(S );
       }
     X = datand.getAttrValue( "units" );
      
      if( X != null )
      {S = DD.cnvertoString(X );
       if( S!= null )
          DS.setY_units(S );
       }
     
     
     
     if(debug)System.out.println("NXdata: dimensions="+xvals.length+","+phi.length+","+fdata.length);
     xvals = HistogramOffsetadjust( xvals,Ax1nd);
     phi = HistogramOffsetadjust( phi, Ax2nd);
     if( xvals == null ) return false;
     int xlength = xvals.length;
     int ylength = phi.length;
     int datalength = fdata.length;
     //System.out.println("lengths="+xlength+","+ylength+","+datalength);
     if(  datalength <= 0 )
       {errormessage = "No Data -0 length";
        return true;
       }
     
     if( ylength <= 0 )
       {errormessage = "Axis 2 has no length";
        System.out.println(errormessage);
        return true;
       }
     xlength =nx;// datalength/ylength;
     
     int i;
     Data newData;
     boolean done = false;
     float yval = fdata[ 0 ];
     float yvals[]; 
  
      if( debug)System.out.println( "DIMENSIONS="+ new NxNodeUtils().Showw(ndims));
    /* int nx,ny;
 
     if( ndims != null ) 
       if( ndims.length <2)
         {nx= ndims[0]; ny= 1;
          }
       else
           {nx= ndims[0];
            ny=ndims[1];
           }
     else
         {nx = -1;ny=-1;}
      if( ny!=ylength)
        if( ny >=0 )
          if( ylength-ny ==1)
            {ylength = ny; xlength =datalength/ylength;} 
          else
             {errormessage= "incorrect dimension";
              System.out.println(errormessage+" ylength,ny="+ylength+","+ny);
              return true;
              }
      if( xlength != nx)
        if( nx >=0)
         if( java.lang.Math.abs( xlength-nx)>1)
               {errormessage="incorrect dimensions";
                System.out.println(errormessage+" nx,xlength,ylength="+nx+","+xlength+","+nx);
                return true;
               }
         else xlength =  nx;
     */
     yvals = new float[ xlength];
     
     //if( errormessage!= "" ) return false;
     int group_id = 0;
   
     String ax1Link = Ax1nd.getLinkName();
   
     String ax2Link;
     if( Ax2nd !=null) ax2Link = Ax2nd.getLinkName();
     else ax2Link= null;
   
         


     NxNode detNode = getCorrespNxDetector( node, instrNode, DS,ax1Link, ax2Link);  
                       //new NxInstrument().matchNode(instrNode,ax1Link, ax2Link);
  
     
    /* float solidAngle[], distance[], theta[],Raw_Angle[],efficiency,
            Delta_2Theta[], Total_Count[];
     int Time_Field[],Group_ID[];
     solidAngle = distance = theta = Raw_Angle= Delta_2Theta =
         Total_Count = null;
     Time_Field = Group_ID=  null;
     efficiency = -1;
     NxNode nx,ndis;
     //System.out.println("Detector Node="+ax1Link+","+ax2Link+","+
     //              detNode.getNodeName() +","+node.getNodeName() );
     if( detNode != null)
	 { ndis = detNode.getChildNode( "distance");
	 if( ndis != null)
	     distance = Arrayfloatconvert(ndis.getNodeValue());
         nx = detNode.getChildNode( "theta");
	 if( nx != null)
	     theta = Arrayfloatconvert(nx.getNodeValue());
         
	 if( ndis != null)
	 { X=(ndis.getAttrValue("solid_angle"));
           if( X != null)if( X instanceof float[])
	     solidAngle =(float[])X;

	   X=(ndis.getAttrValue("total_count"));
           if( X != null)if( X instanceof float[])
	     Total_Count =(float[])X;

	   X=(ndis.getAttrValue("delta_2theta"));
           if( X != null)if( X instanceof float[])
	     Delta_2Theta =(float[])X;

            X = ndis.getAttrValue("time_field_type");
            if( X!= null)if( X instanceof int[])
               Time_Field =(int[])X;

            X = ndis.getAttrValue("group_id");
            if( X!= null)if( X instanceof int[])
               Group_ID =(int[])X;
         
            nx = detNode.getChildNode( "efficiency");
            if( nx != null)
             {X = nx.getNodeValue();
              Float ff = DD.cnvertoFloat( X );
              if( ff != null)
               if( ff.floatValue() != Float.NaN)
                 efficiency = ff.floatValue();
             }
             X = ndis.getAttrValue("raw_angle");
             if( X instanceof float[])
               Raw_Angle =(float[])X;
	 }//distance node != null
	 }//detNode != null
     */
    // float d,t,p,s,ra;
    // int T;
     group_id = 0;
     
   // if( Group_ID != null)
    //    if( Group_ID.length < phi.length)
    //       Group_ID = null;
    
    if(debug)System.out.println("NXdata:ere put into dataset"+xlength);
   
    errormessage="";
    
    for( group_id=0;group_id < ny;group_id++)
       {boolean error=false;
        /*for( i = 0 ;( i < xlength )&&( !error) && (i < phi.length) ; i++ )
          {yvals[ i ] =  fdata[i + group_id*xlength );
           if(new Float(yvals[i]).isNaN()) error=true;
           }
        //if(debug)System.out.print(";DB "+group_id+","+error);
        //if(debug) if( 20*(int)(group_id/20.0)==group_id)System.out.println("");
        if( !error )
	    {int xx=-1;
           //if( Group_ID != null)
           //    xx = Group_ID[ group_id ];
          // else
               xx = DS.getNum_entries();
           newData = new Data( new VariableXScale( xvals ) , 
                   yvals , xx );
           
            }
        else 
	    { done = true;
	      
              return false;
             
          
             };
        */
        System.arraycopy(fdata,group_id*xlength,yvals,0,xlength);
        int xx = DS.getNum_entries();
        newData = new Data( new VariableXScale( xvals ) , 
                   yvals , xx );
        setOtherAttributes(detNode ,newData, group_id);
        if(timeFieldType >=0)
           newData.setAttribute( new IntAttribute(Attribute.TIME_FIELD_TYPE,timeFieldType));
        DS.addData_entry( newData );
       }
   if(debug)System.out.println("After Save 1 NXdata, errormessage="+errormessage);
   return false;
   }
public static DetectorPosition convertToIsaw( float distance, float phi, float theta)
  {Position3D p3 = new Position3D();
  float coords[];
   coords = Types.convertFromNexus( distance , phi , theta );
   
   p3.setSphericalCoords( coords[0], coords[2], coords[1]);
   return new DetectorPosition( p3);

   }
/**  returns r, theta, phi
*/
public float[] converToNex( DetectorPosition DP)
  { return DP.getSphericalCoords();

  }
private float[] HistogramOffsetadjust(float[] xvals,  NxNode Ax1nd)
    {if(xvals == null) return null;
     if(Ax1nd == null) return xvals;
     Object X = Ax1nd.getAttrValue("histogram_offset");
     float histogram = 0;
     Float F = new NxData_Gen().cnvertoFloat( X); 
     if( F != null) 
          {   histogram = F.floatValue();
              float[] xvals1 = new float[xvals.length+1];
              xvals1[0] = xvals[0]-histogram;
              for (int i=0;i<xvals.length;i++)
                xvals1[i+1] = 2*xvals[i]-xvals1[i];
              xvals = xvals1;
      }
     return xvals;
           }

/** Gets the corresponding NxDetector node for this NxData block
*/
public NxNode getCorrespNxDetector( NxNode node, NxNode instrNode, DataSet DS,
           String ax1Link, String ax2Link )
   {  //Check if there is a NxDetector class in NxData
        for(int i = 0; i<node.getNChildNodes();i++)
          {NxNode nx = node.getChildNode( i );
           if( nx.getNodeClass().equals( "NXdetector"))
               return nx;
          }

     //otherwise have NxInstrument find the Nxdetector with appropriate link names
       return new NxInstrument().matchNode(instrNode,ax1Link, ax2Link);

    }
public static void UnitsAdjust( float[] d, String newUnits, String oldUnits)
  {if( oldUnits == null) return;
   if( d == null) return;
   if( d.length < 1) return;
   float f= NxNodeUtils.getConversionFactor( oldUnits, newUnits);
   if( f==Float.NaN) return;
   for( int i = 0; i< d.length; i++)
     d[i] = f*d[i];
   }
/** Test program for NXData_util
*/
public static void main( String args[] )
  {/*NDSClient nds;String filename = "lrcs3000.hdf";
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
   */
   }

  }
