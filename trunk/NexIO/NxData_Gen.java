/*
 * File:  NXData_Gen.java 
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
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
 *
 */
package NexIO;

import  DataSetTools.dataset.*;

/** Implementation of NxData that searches for the axes and data field names
 */
public class NxData_Gen extends NXData_util implements NxData
  {String ax1 , 
          ax2 , 
          dat;
   String errormessage;

   public NxData_Gen()
      {super();
       }
  /** Fills out an existing DataSet with information from the NXdata
   * section of a Nexus datasource
  *@param node  the current node positioned to an NXdata part of a datasource
  *@param  DS  the existing DataSet that is to be filled out
  *@return  error status: true if there is an error otherwise false
  */
   public boolean processDS( NxNode node , DataSet DS )
     {errormessage = "";
      if( !node.getNodeClass().equals( "NXdata" ) )
          {errormessage = "Improper node";
           return false;
          }
      ax1 = null; 
      ax2 = null;
      dat = null;
      int nchildren =  node.getNChildNodes();
    
      int i;
       for( i = 0 ; i < nchildren ; i++ )
         {NxNode N =  node.getChildNode( i );
         
          if( N == null )
            {errormessage = "improper child node";
             return false;
            }
          Object X = N.getAttrValue( "axis" );
          if( X!= null )
            {int u = cnvertoint( X );
             if( errormessage!= "" ) 
                u = -1;
             if( ( u!= 2 )&&( ax1 == null ) ) 
                ax1 = N.getNodeName();
             else ax2 = 
                N.getNodeName();
	   
             }
          X = N.getAttrValue( "signal" );
           if( X!= null ) 
               {dat = N.getNodeName();
	       //System.out.println( "   data set at node" + N.show() );
                }

          }//end for
      
        if( ( ax1 == null )||( ax2 == null )||( dat == null ) ) 
           {errormessage = "Axes not specified";
            return false;
           }
       if( super.processDS( node , ax1 , ax2 , dat , DS ) )
         {errormessage = super.getErrorMessage();
          return true;
         }
       
       return false;

      }
 
   /** Returns error or warning message or "" if none
  */
   public String getErrorMessage()
   {
        return errormessage;
    }

  /** Converts an object to a Float or null if not possible
  */
  public Float cnvertoFloat( Object X )
    {if( X instanceof Float )
           return ( Float )X;
     if( X instanceof float[] )
        {if( ( ( float[] )X ).length!= 1 ) 
                    return null;
         return new Float( ( ( float[] )X )[ 0 ] );
         }
     if( X instanceof String )
       try{
         return new Float( ( String ) X );
          }
       catch( Exception S )
         {return null;}
     if( X instanceof Double ) 
               return ( Float )X;
     if( X instanceof Number ) 
         return ( Float )X;
     return null;
    }

  /** Converts and Object to a String or null if not possible
  */
  public String cnvertoString( Object X )
    {if( X ==  null ) 
         return null;
     if( X instanceof String ) 
          return ( String ) X;
     if( X instanceof char[] )
          return new String( ( char[] )X );
     if( X instanceof byte[] )
               return new String( ( byte[] ) X );
     if( X instanceof short[] )
	 {byte b[]; b =  new byte[( ( short[] )X ).length];
	 try{
	 for( int i = 0; i <  b.length ; i++ )
	     {b[ i ] =  ( byte )( ( short[] )X )[ i ];
	     }
         return new String(  b );
            }
         catch( Exception s ){}
        } 
     // if( X instanceof Number ) return new Number( X ).toString( );
     errormessage = "cannot convert" +  X.getClass() + " to String";
     return null;
    }

 /** Converts and Object to an int 
* errormessage is not "" if it is not possible
 */
  public  int cnvertoint( Object X )
    {try{
       if( X instanceof byte[] )
         X =  new String( ( byte[] )X );
       
       if( X instanceof String )
          return new Integer( ( String )X ).intValue( );
       
       if( X instanceof Integer ) 
          return ( ( Integer )X ).intValue();

       if( X instanceof int[] )
	   if( ( ( int[] )X ).length == 1 ) 
                return ( ( int[] )X )[ 0 ];
       errormessage = "improper data type to convert to int";
       return -1;

    }
   catch( Exception s )
      {errormessage = s.getMessage();
      }
     return -1;
    }
}







