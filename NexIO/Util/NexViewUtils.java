/*
 * File:  NexViewUtils.java
 *
 * Copyright (C) 2005, Ruth Mikkelson
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.4  2007/08/16 17:43:44  rmikk
 *  Reformatted code
 *
 *  Revision 1.3  2006/10/22 18:17:40  rmikk
 *  Added GPL
 *
 *  */
package NexIO.Util;
import java.util.*;
import NexIO.*;
import Operators.Generic.MakeDataSet;
import gov.anl.ipns.Util.SpecialStrings.*;
import DataSetTools.dataset.*;

/**
 * 
 * @author MikkelsonR
 *
 *
 */
public class NexViewUtils {

   // && these with error codes to determine order
   public static int NO_SIGNAL              = 1;
   public static int MISSING_AXIS           = 2;
   public static int DIMS_OUT_OF_ORDER      = 4;

   public static int AXES_IN_WRONG_PLACE    = 8;
   public static int TOO_MANY_SIGNALS       = 16;
   public static int TOO_MANY_AXIS_SPECS    = 32;
   public static int AXES_NUMS_OUT_OF_RANGE = 64;


   /**
    * Returns a Vector of information on each NXdata. Each element contains
    * information on one NXdata in Vector form as follows: 1. Name of NXdata 2.
    * Dimensions in form of NeXus 3. check code 4. String message
    * 
    * @param filename
    *           The name of the file
    *           
    * @return The Vector with the given information or A Vector with one element
    *         which is a gov.anl.pns.... ErrorString
    */
   public static Vector getNXdataInfo( String filename , String NXentryName ) {

      NxNode top = (NxNode) ( new NexIO.NexApi.NexNode( filename ) );
      
      if( top.getErrorMessage().length() < 1 ) {
         
         Vector V = new Vector();
         V.addElement( new ErrorString( top.getErrorMessage() ) );
         return V;
         
      }
      
      NxNode NxEntryNode = null;
      for( int i = 0 ; ( i < top.getNChildNodes() ) && ( NxEntryNode == null ) ; i++ ) {
         
         NxNode node = top.getChildNode( i );
         if( node != null )
            if( node.getClass().equals( "NXentry" ) )
               if( node.getNodeName() != null )
                  if( node.getNodeName().equals( NXentryName ) )
                     NxEntryNode = node;
                 
      }

      // Now look for the NXdata
      Vector Res = new Vector();
      for( int i = 0 ; i < NxEntryNode.getNChildNodes() ; i++ ) {
         
         NxNode node = NxEntryNode.getChildNode( i );
         if( node.getClass().equals( "NXdata" ) )
            Res.addElement( UpdateNXdataInf( node ) );
      }
      
      return Res;
   }


   // node is an NXdata node
   private static Vector UpdateNXdataInf( NxNode node ) {

      Vector V = new Vector();
      V.addElement( node.getNodeName() );

      int[] dim = null;

      for( int i = 0 ; i < node.getNChildNodes() ; i++ ) {
         
         Integer sig = NexUtils.getIntAttributeValue( node , "signal" );
         if( sig != null ) 
            dim = node.getDimension();
         
      }
      if( dim == null ) {
         
         V.addElement( dim );
         V.addElement( new Integer( NO_SIGNAL ) );
         return V;
         
      }
      
      V.addElement( dim );
      int[] axes = new int[ dim.length ];
      Arrays.fill( axes , - 1 );
      for( int i = 0 ; i < node.getNChildNodes() ; i++ ) {

         Integer ax = NexUtils.getIntAttributeValue( node , "axis" );
         if( ax != null ) {
            
            if( ( ax.intValue() < 1 ) || ( ax.intValue() > dim.length ) ) {
               
               V.addElement( new Integer( AXES_NUMS_OUT_OF_RANGE ) );
               return V;
               
            }
            if( axes[ ax.intValue() - 1 ] < 0 ) {
               
               V.addElement( new Integer( TOO_MANY_AXIS_SPECS ) );
               return V;
               
            }
            axes[ ax.intValue() - 1 ] = node.getDimension()[ 0 ];
         }
      }
      
      
      for( int i = 0 ; i < dim.length ; i++ ) {
         
         if( axes[ i ] < 0 ) {
            
            V.addElement( new Integer( MISSING_AXIS ) );
            return V;
            
         }
         
         if( axes[ i ] == dim[ dim.length - 1 - i ] ) {
         }
         
         if( Math.abs( axes[ i ] - dim[ dim.length - 1 - i ] ) > 1 ) {
            
            V.addElement( new Integer( AXES_IN_WRONG_PLACE ) );
            return V;
            
         }


      }
      V.addElement( new Integer( 0 ) );
      return V;

   }


   /**
    * returns an error String or the dataset in a NeXus file with the
    * given filename, NXentry Name and NXdata name
    * 
    * @param filename      The name of the NeXus file
    * 
    * @param NXEntryName   The name of the NXentry block in the file
    * 
    * @param NXdataName    The name of the NXdata block in the NXentry block
    * 
    * @return  The dataset or an error string if it was not possible to
    *           create the data set
    *           
    * NOTE:  Assumes axis=1 is the tof axis
    */
   public static Object getNxData( String filename , String NXEntryName ,
            String NXdataName ) {

      NxNode top = (NxNode) ( new NexIO.NexApi.NexNode( filename ) );
      if( top.getErrorMessage().length() > 0 ) 
         return top.getErrorMessage(); 
      
      
      //--------Find the NXentry node ---------------
      NxNode NxEntryNode = null;
      for( int i = 0 ; ( i < top.getNChildNodes() ) && ( NxEntryNode == null ) ; i++ ) {
         NxNode node = top.getChildNode( i );
         if( node != null )
            if( node.getNodeClass().equals( "NXentry" ) )
               if( node.getNodeName() != null )
                  if( node.getNodeName().equals( NXEntryName ) ) {
                     NxEntryNode = node;
                  }
      }
      
      if( NxEntryNode == null ) 
         return "Could Not find Entry Node";
      
      //--- Find the NXdata node ----------------
      NxNode nxDataNode = null;
      for( int i = 0 ; ( i < NxEntryNode.getNChildNodes() )
               && ( nxDataNode == null ) ; i++ ) {
         NxNode nxnode = NxEntryNode.getChildNode( i );
         if( nxnode.getNodeClass().equals( "NXdata" ) )
            if( nxnode.getNodeName().equals( NXdataName ) )
               nxDataNode = nxnode;
      }
      if( nxDataNode == null ) 
         return "could not find data node";
      
      //------- determine the axes and data node in  the NXdata node -------
      NxNode dataNode = null;
      NxNode axis1Node = null;
      for( int i = 0 ; ( i < nxDataNode.getNChildNodes() )
               && ( ( dataNode == null ) || axis1Node == null ) ; i++ ) {
         
         NxNode child = nxDataNode.getChildNode( i );
         if( NexUtils.getIntArrayAttributeValue( child , "signal" ) != null )
            dataNode = child;
         
         Integer ax = NexUtils.getIntAttributeValue( child , "axis" );
         if( ax != null ) if( ax.intValue() == 1 ) 
            axis1Node = child;
         
      }
      if( dataNode == null )
         return "NXdata node has no data";
      
      
      Object O = dataNode.getNodeValue();
      int[] dim = dataNode.getDimension();
      O = NexIO.Types.MultiPackLinear( O , java.lang.reflect.Array
               .getLength( O ) , dim );
      
      String yunits = NexUtils.getStringAttributeValue( dataNode , "units" );
      if( yunits == null )
         yunits = "Y units";
      
      if( O == null ) 
         return "The data in the data node is not good";
      
      float[] xscale = NexUtils.getFloatArrayFieldValue( nxDataNode , axis1Node
               .getNodeName() );
      
      String xunits = NexUtils.getStringAttributeValue( axis1Node , "units" );
      
      if( xunits == null ) 
         xunits = "x units";


      MakeDataSet mkDS = new MakeDataSet();
      Vector Vxscale = new Vector();
      Vxscale.addElement( xscale );
      Vector VYvalues = new Vector();
      VYvalues.addElement( O );
      
      Object DS = mkDS.calculate( Vxscale , VYvalues , null , nxDataNode
               .getNodeName() , axis1Node.getNodeName() , xunits , "" , yunits ,
               null , null );

      if( ! ( DS instanceof DataSet ) ) 
         return DS;
      if( DS == null ) 
         return null;
      // new ViewManager((DataSet)DS, IViewManager.IMAGE);

      return DS;
   }


   /**
    * Test program for various functions in this class
    * 
    * @param args
    */
   public static void main( String[] args ) {

      Command.ScriptUtil.display( NexViewUtils.getNxData( args[ 0 ] ,
               args[ 1 ] , args[ 2 ] ) );
   }
}
