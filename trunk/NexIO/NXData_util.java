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
 * Revision 1.2  2001/07/17 13:53:12  rmikk
 * Fixed error so group numbers increased
 *
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
 *
 */
package NexIO;
import NexIO.NDS.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
//import NDS.*;
//import NdsSvNode;

/** A utility package used by many NxData implementers
 */
public class NXData_util
 {String errormessage;

  public NXData_util()
   { 
     errormessage = "";
    }

  /**Returns error or warning messages of "" if none
 */
  public String getErrorMessage()
   {
     return errormessage;
   }

 /** Converts an object into a float array(if possible) or null.
 *@see #getErrorMessage()
*/
  public float[] Arrayfloatconvert( Object X )
    {int i;
      errormessage = "";
     float res[];
     if( X instanceof int[] ) 
       {int b[];  
        b = ( int[] )X; 
        res = new float[b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] =  getfloatEntry( X , i );
       }
     else if( X instanceof byte[] )
       {byte b[];  
        b = ( byte[] )X; 
        res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] =  getfloatEntry( X , i );

       }
      else if( X instanceof long[] )
       {long b[];  
         b = ( long[] )X; 
        res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] =  getfloatEntry( X , i );

       }
     else if( X instanceof long[] )
       {long b[];  
        b = ( long[] )X; 
        res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] =  getfloatEntry( X , i );

       }
      else if( X instanceof short[] )
       {short b[];  
        b = ( short[] )X; 
        res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] = getfloatEntry( X , i );

       }

     else if( X instanceof float[] )
       {float b[];  
        b = ( float[] )X; 
        res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] = getfloatEntry( X , i );

       }


     else if( X instanceof double[] )
       {double b[];  
        b = ( double[] )X; 
        res = new float[ b.length ];
        for( i = 0 ; i < b.length ; i++ )
          res[ i ] = getfloatEntry( X , i );
       }
     else
       {String S = "";
        S = X.getClass(  ).toString( );
        if(  X instanceof Object[] )
          S  = ( ( ( Object[] )X )[ 0 ] ).getClass().toString();
        errormessage = "Not an Array data type: " + X.getClass();
        return null;
         }
     return res;
   
    }

 /** gets the index-th element of an array X( if possible) or
* 0. See getErrorMessage
*/
  public float getfloatEntry( Object X ,  int index )
    {errormessage = "";
     if( X instanceof int[] )
      { int b[];  b = ( int[] )X; 
        if( ( b.length <= index )||( index < 0 ) )
            {errormessage = "index out of range"; 
             return 0;}
        return new Integer( b[ index ] ).floatValue();
       }
     else if( X instanceof byte[] )
      { byte b[];  b = ( byte[] )X; 
        if( ( b.length <= index )||( index < 0 ) )
            {errormessage = "index out of range"; 
             return 0;}
         return new Byte( b[ index ] ).floatValue();

       }
    else if( X instanceof long[] )
      { long b[];  b = ( long[] )X; 
        if( ( b.length <= index )||( index < 0 ) )
            {errormessage = "index out of range"; 
             return 0;}
         return new Long( b[ index ] ).floatValue();

       }
     else if( X instanceof float[] )
      { float b[];  b = ( float[] )X; 
        if( ( b.length <= index )||( index < 0 ) )
            {errormessage = "index out of range"; 
             return 0;}
         return new Float( b[ index ] ).floatValue();

       }


     else if( X instanceof double[] )
      { double b[];  b = ( double[] )X; 
        if( ( b.length <= index )||( index < 0 ) )
            {errormessage = "index out of range"; 
             return 0;}
         return new Double( b[ index ] ).floatValue();

       }
     else
       {errormessage = "Improper data type-not an Array";
        return 0;
       }
     }  

 /** Fills out an existing DataSet with information from the NXdata
   * section of a Nexus datasource
  *@param node  the current node positioned to an NXdata part of a datasource
  *@param  DS  the existing DataSet that is to be filled out
  *@return  error status: true if there is an error otherwise false
  */
  public boolean processDS(  NxNode node ,  String axis1 ,  
                       String axis2 ,  String dataname ,  DataSet DS )
    {errormessage = "";
    //System.out.println("Axes="+axis1+","+axis2+","+dataname);
     NxNode Ax1nd = ( NxNode )( node.getChildNode( axis1 ) );
     if( Ax1nd == null )
        {errormessage = node.getErrorMessage( ) + " for field " + axis1;
         return true;
        }
   

     NxNode Ax2nd = ( NxNode )( node.getChildNode( axis2 ) );
     if( Ax2nd == null )
        {errormessage = node.getErrorMessage() + " for field " + axis2;
         return true;
        }
    
     NxNode datand = ( NxNode )( node.getChildNode( dataname ) );
     if( datand == null )
        {errormessage = node.getErrorMessage() + " for field " + dataname;
         return true;
        }
    
     Object Ax1 = Ax1nd.getNodeValue( );
     if( Ax1 == null )
        {errormessage = node.getErrorMessage() + " for field " + axis1;
         return true;
        }
     //System.out.println( "ProcessDSE" + errormessage );

     Object Ax2 = Ax2nd.getNodeValue();
     if( Ax2 == null )
        {errormessage =  node.getErrorMessage() + " for field " + axis2;
         return true;
        }
 
     Object data = datand.getNodeValue();
  
     
     if((data == null ) )
        {errormessage = node.getErrorMessage() + " for field " + dataname;
         return true;
        }
     float fdata[];
     fdata = Arrayfloatconvert(data );
 
     if( fdata  == null ) return true;
     float phi[];
     phi = Arrayfloatconvert(Ax2 );
 
     if( phi == null )
        {errormessage = getErrorMessage();
         return true;
          }
     Object X = Ax1nd.getAttrValue( "long_name" );
     String S; 
     NxData_Gen DD = new NxData_Gen();    
     if( X != null )
      {S  = DD.cnvertoString(X );
       if( S!= null )
          DS.setX_label(S );
       }
     X = Ax2nd.getAttrValue( "units" );
      if( X != null )
      {S  = DD.cnvertoString(X );
       if( S!=  null )
          DS.setX_units(S );
       }
     X = Ax2nd.getAttrValue( "long_name" );
          
     if( X != null )
      {S = DD.cnvertoString(X );
       if( S!= null )
          DS.setY_label(S );
       }
     X = Ax2nd.getAttrValue( "units" );
      if( X != null )
      {S = DD.cnvertoString(X );
       if( S!= null )
          DS.setY_units(S );
       }
     
     float xvals[];
     xvals = Arrayfloatconvert( Ax1 );
  
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
        return true;
       }
     xlength = datalength/ylength;
     int i;
     Data newData;
     boolean done = false;
     float yval = fdata[ 0 ];
     float yvals[]; 
     yvals = new float[ xlength];
     
     if( errormessage!= "" ) return false;
     int group_id = 0;
    
     
     while( !done )
       {for( i = 0 ;( i < xlength )&&( errormessage == "" ) ; i ++ )
          yvals[ i ] =  getfloatEntry( data , i + group_id*xlength );
        
        if( errormessage == "" )
         newData = new Data( new VariableXScale( xvals ) , yvals , group_id );
        else 
	    { done = true;
	    if(  errormessage.equals(  "index out of range" ) )
              return false;
            return true;
          
           };
        DS.addData_entry( newData );
        
        group_id++;
       }
     
   return false;
   }

/** Test program for NXData_util
*/
public static void main( String args[] )
  {NDSClient nds;String filename = "lrcs3000.hdf";
   nds = new NDSClient( "mandrake.pns.anl.gov" , 6008 , 6081998 );
   nds.connect(  );
   NdsSvNode node = new NdsSvNode( filename , nds );
   NdsSvNode node1 = ( NdsSvNode )( node.getChildNode( "Histogram1" ) );
   node1 = ( NdsSvNode )( node1.getChildNode( "data" ) );
   System.out.println( "a" );
   NXData_util DD = new NXData_util();
   System.out.println( "c" );
   DataSet DS = new DataSet( "","" );
   if( !DD.processDS(  node1 , "time_of_flight" , "phi" , "data" ,  DS ) )
       System.out.println( "error" + DD.getErrorMessage() );
   else
    { ViewManager view_manager = new ViewManager(  DS ,  DataSetTools.viewer.IViewManager.IMAGE );

      }
   }

  }

