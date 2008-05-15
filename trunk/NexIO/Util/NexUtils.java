

/*
 * File:  NexUtils.java
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * $Log: NexUtils.java,v $
 * Revision 1.22  2008/02/23 21:32:39  rmikk
 * Fixed an off by one error in assigning attributes to a data block
 *
 * Revision 1.21  2008/02/13 16:31:26  rmikk
 * -Incorporated change in row and column positions in ntuples for versions of 2
 *   or greater
 * -Eliminated a lot of warnings
 * -Created a grid for a detector for a case that currently was not covered.
 *
 * Revision 1.20  2008/01/11 17:29:22  rmikk
 * Implemented Detector ID numbers
 * Included RowColGrid's if NXdata dimensions indicate such a need.  Also
 *   1x1 grids are now put on every  DataSet retrieved from newer NeXus files
 *
 * Revision 1.19  2007/08/16 17:42:16  rmikk
 * Took care of the version 2 requirement that there are only trailing "*"'s in
 *   fields like distance, azimuthal_angle, poloar_angle and several of the
 *
 * Revision 1.18  2007/07/11 18:30:20  rmikk
 * Added a lot of documentation for utility methods
 * Now uses default detector ID's so that each ID is used only once per NXentry
 *
 * Revision 1.17  2007/07/04 17:48:43  rmikk
 * Fixed major errors in copying over the errors
 *
 * Revision 1.16  2007/01/12 14:48:47  dennis
 * Removed unused imports.
 *
 * Revision 1.15  2006/07/27 18:33:55  rmikk
 * Added some documentations and some spacing in the code to make it more
 * readable
 *
 * Revision 1.14  2006/07/25 12:40:48  rmikk
 * Changed the NXgeometry.NXtranslate.distances field back to distances
 *
 * Revision 1.13  2006/07/25 00:13:39  rmikk
 * Added code to update to the new standard.  Moved some code into
 * separate methods.
 *
 * Revision 1.12  2005/06/17 13:23:54  rmikk
 * Added a method getIntArrayAttriuteValue
 *
 * Revision 1.11  2005/06/06 21:58:27  rmikk
 * Undid the Fortan Fix for incorrect ordering of array dimensions
 *
 * Revision 1.10  2005/06/04 20:07:03  rmikk
 * Now Fixes in ISAW the dimensions of the NXdata.data node if possible.
 *   This works for newer files only( those that have links to the NXdetector)
 *
 * Revision 1.9  2005/01/14 23:38:26  rmikk
 * Fixed several errors in retrieving NXdata with several detectors.
 *
 * Revision 1.8  2004/12/23 18:41:34  rmikk
 * Implements some of the NeXus standard version 1.0 changes like
 * NXgeomentry, description, etc.
 *
 * Revision 1.7  2004/05/14 15:02:52  rmikk
 * Removed unused variables
 *
 * Revision 1.6  2004/03/15 19:37:54  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.5  2004/03/15 03:36:02  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.4  2003/12/21 18:27:00  rmikk
 * Reduced Class Cast Exceptions by using ConvertDataTypes.StringValue method
 *   instead of (String) cast
 * Improved the tolerance on the bounds of an array
 *
 * Revision 1.3  2003/12/18 15:20:00  rmikk
 * Replaces some (String) by ConvertDataType.StringValue
 *   to reduce ClassCastExceptions
 *
 * Revision 1.2  2003/11/23 23:51:31  rmikk
 * Eliminated some debugging prints
 * DataSets are not saved as Grids unless there is more than 1 row
 *   or more than 1 column
 *
 * Revision 1.1  2003/11/16 21:48:02  rmikk
 * Initial Checkin
 *
 */

package NexIO.Util;


import NexIO.*;
import NexIO.State.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.MathTools.Geometry.*;
//import java.lang.reflect.*;
import java.util.*;

//TODO get InvertRowCol fix for cases where there are area detectors
//   make sure that xvec and yvec correspond.
/**
 *  This class contains the Generic implementations of the methods in 
 *  the interface INexUtils. Also it has other utility routines related
 *  to NeXus files.
 *  @see INexUtils
 */
public class NexUtils implements INexUtils {
 
    public String xUnits = "us";
    public String angleUnits = "radian";

    String errormessage = "";
    boolean InvertRowCol = false;
    // NOTE that in the code row corresponds to slowest changing position
    //    in dimension and col corresponds to fastest changing position
    //    This makes for 1-1 correspondence between data elements and
    //    positions, etc.
    /**
     *  return the NXdetector in NxInstrument with the given Name, LinkName. 
     *  If there is no node, null is returned.
     *  @param LinkName the NXdetector name in NXinstruent
     *  @param NxInstrumentNode  An NxNode that gives access to an NXinstrumetn class
     *  
     *  @return  the NxNode whose class is NxDetector with the given LinkName
     *             or null if none
     */
    public static NxNode getCorrespondingNxDetector( String LinkName , 
        NxNode NxInstrumentNode ) {
          
        if ( LinkName == null )
            return null;
            
        if ( NxInstrumentNode == null )
            return null;
            
        return NxInstrumentNode.getChildNode( LinkName.trim() );
    }

    
    private void Incr_detDig( int[] detDig, int[]dims){
       
       if( detDig.length > 0 ){ //increment w. carry detDig
          int kk = detDig.length - 1;
          detDig[ kk ]++;
          while( ( kk >=  0 ) &&(  detDig[ kk ] >= dims[ kk ])){
             detDig[kk]= 0;
             if( kk >= 1 )
                detDig[ kk - 1 ]++;
             kk--;
          }
       }
    }
    
    //Access to row and col taking into account their rearrangement
    private  int Row( int row, int col){
       if( InvertRowCol)
          return col;
       return row;
    }
    
  //Access to row and col taking into account their rearrangement
    private  int Col( int row, int col){
       if( InvertRowCol)
          return row;
       return col;
    }
    
    private  int rowIndex( int[] dims){
       if( dims.length<3)
          return -1;
       if( InvertRowCol)
          return 1;
       return 2;
    }
    
    private int colIndex( int[] dims){
       if( dims.length<3)
          return -1;
       if( InvertRowCol)
          return 2;
       return 1;
    }

    /**
     *  Currently invoked by the standard Process1Nxdata. It adds all the 
     *  data from the data field of NXdetector to the DataSet DS 
     *  @param DS  the DataSet that is having information added to
     *  @param NxDataNode The NxNode containing information about an NeXus
     *                      NXdata class
     *  @param NxDetector The NxNode containing information about an NeXus
     *                      NXdetector class
     *  @param startDSindex  the first GroupIndex to process
     *  @param States   The linked list of state information
     *  This method sets up grids with default ID's of 1,2,3,...
     *  NOTE: This assumes the time axis is axis 1, and the order of the
     *        next dimension represent col then row, then detectors. col
     *        or row dimensions can be missing.
     */
    public boolean setUpNXdetectorAttributes( DataSet DS , NxNode NxDataNode ,
            NxNode NxDetector , int startDSindex , NxfileStateInfo States ) {

      NxDataStateInfo dataState = NexUtils.getDataStateInfo( States );  
      if( dataState == null )
         return setErrorMessage( " No state for NXdata "
                  + NxDataNode.getNodeName() );
      NxDetectorStateInfo detState = NexUtils.getDetectorStateInfo( States );
      NxEntryStateInfo NxEntryState = NexUtils.getEntryStateInfo( States );
      int startPixelIndex = dataState.startGroupID;
      String version = null;

      if( NxEntryState != null )
         version = NxEntryState.version;
      
      if( version != null && version.compareTo( "2")>=0)
         InvertRowCol = true;
    

      if( detState == null )
         return setErrorMessage( " No state for NXdetector "
                  + NxDetector.getNodeName() );

      float[] crate = NexUtils.getFloatArrayFieldValue( NxDetector , "crate" );
      float[] slot = NexUtils.getFloatArrayFieldValue( NxDetector , "slot" );
      float[] input = NexUtils.getFloatArrayFieldValue( NxDetector , "input" );
      float[] solAng = NexUtils.getFloatArrayFieldValue( NxDetector ,
               "solid_angle" );

      float[] width , height , depth , diameter , orientation;

      int[] widthDim , heightDim , depthDim , diameterDim , x_dirDim;

      Vector3D[] x_dir = null , y_dir = null;

      width = height = depth = diameter = null;
      String detType = "nxbox";

      widthDim = heightDim = depthDim = diameterDim = x_dirDim = null;

      if( ( detState.NxGeometryName == null ) || !detState.NxGeometryName.equals( "geometry" )) {

         width = NexUtils.getFloatArrayFieldValue( NxDetector , "width" );
         height = NexUtils.getFloatArrayFieldValue( NxDetector , "height" );
         depth = NexUtils.getFloatArrayFieldValue( NxDetector , "depth" );

         if( width != null )
            if( version == null || version.compareTo( "2" ) < 0 )
               widthDim = Util.GetDimension(
                        NxDetector.getChildNode( "width" ) , dataState , 0 , 1 );
            else
               widthDim = NxDetector.getChildNode( "width" ).getDimension();

         if( height != null )
            if( version == null || version.compareTo( "2" ) < 0 )
               heightDim = Util.GetDimension( NxDetector
                        .getChildNode( "height" ) , dataState , 0 , 1 );
            else
               heightDim = NxDetector.getChildNode( "height" ).getDimension();


         if( depth != null )
            if( version == null || version.compareTo( "2" ) < 0 )
               depthDim = Util.GetDimension(
                        NxDetector.getChildNode( "depth" ) , dataState , 0 , 1 );
            else
               depthDim = NxDetector.getChildNode( "depth" ).getDimension();


         
         NxNode orientNode = NxDetector.getChildNode( "orientation" );

         if( orientNode != null ) {

            orientation = ConvertDataTypes.floatArrayValue( orientNode
                     .getNodeValue() );


            int[] d = orientNode.getDimension();
            x_dirDim = new int[ d.length - 1 ];
            System.arraycopy( d , 0 , x_dirDim , 0 , x_dirDim.length );


            ConvertDataTypes.UnitsAdjust( orientation , ConvertDataTypes
                     .StringValue( orientNode.getAttrValue( "units" ) ) ,
                     "radians" , 1.0f , 0.0f );

         }
         else
            orientation = null;

         if( orientation != null ) {

            x_dir = new Vector3D[ orientation.length / 3 ];
            y_dir = new Vector3D[ orientation.length / 3 ];

            for( int i = 0 ; i < x_dir.length ; i++ ) {
               x_dir[ i ] = Rotate( orientation , i , 0 , - 1 , 0 );
               y_dir[ i ] = Rotate( orientation , i , 0 , 0 , 1 );
            }
         }
      }
      else {// some or all data in in NXgeometry

         NxNode geom = NxDetector.getChildNode( detState.NxGeometryName );
         //if( geom == null )
         //   geom = detState.NxGeometryNode_origin;

         if( geom == null ) {

            //width = height = depth = orientation = null;

         }
         else {

            width = NexUtils.getNxGeometryInfo( geom , "length" , detState );
            height = NexUtils.getNxGeometryInfo( geom , "height" , detState );
            depth = NexUtils.getNxGeometryInfo( geom , "width" , detState );
            diameter = NexUtils
                     .getNxGeometryInfo( geom , "diameter" , detState );

            int p = NexUtils.getNextChildNode( geom , "NXshape" , 0 );
            if( p < 0 ) {

               setErrorMessage( errormessage + ":: No NXshape in NXdetector" );

            }
            else {

               NxNode shp = geom.getChildNode( p );
               if( shp.getChildNode( "shape" ) != null )
                  detType = ConvertDataTypes.StringValue( shp.getChildNode(
                           "shape" ).getNodeValue() );

               if( detType == null )
                  detType = "nxbox";
               if( width != null ) {
                  widthDim = heightDim = depthDim = Util.GetDimension( shp
                           .getChildNode( "size" ) , dataState , 0 , 1 );
               }
               else if( height != null )
                  diameterDim = heightDim = shp.getChildNode( "size" )
                           .getDimension();
               else
                  diameterDim = shp.getChildNode( "size" ).getDimension();

            }

            float[] xx = NexUtils.getNxGeometryInfo( geom , "x_dir" , detState );
            float[] yy = NexUtils.getNxGeometryInfo( geom , "y_dir" , detState );


            if( ( xx != null ) && ( yy != null ) && ( xx.length == yy.length ) ) {

               x_dir = new Vector3D[ xx.length / 3 ];
               y_dir = new Vector3D[ yy.length / 3 ];

               for( int i = 0 ; i < xx.length / 3 ; i++ ) {

                  x_dir[ i ] = new Vector3D( xx[ 3 * i + 2 ] , xx[ 3 * i ] ,
                           xx[ 3 * i + 1 ] );
                  y_dir[ i ] = new Vector3D( yy[ 3 * i + 2 ] , yy[ 3 * i ] ,
                           yy[ 3 * i + 1 ] );

               }
            }
         }

         if( x_dir != null ) {

            int p = NexUtils.getNextChildNode( geom , "NXorientation" , 0 );
            if( p < 0  || geom == null)
               return setErrorMessage( " No NXorientation in NXdetector" );
            
            NxNode shp = geom.getChildNode( p );
            x_dirDim = Util.GetDimension( shp.getChildNode( "value" ) ,
                     dataState , 0 , 1 );
         }

      }

      float[] x_offsets = NexUtils.getFloatArrayFieldValue( NxDetector ,
               "x_pixel_offset" );

      float[] y_offsets = NexUtils.getFloatArrayFieldValue( NxDetector ,
               "y_pixel_offset" );

      if( x_offsets != null )
         if( x_offsets.length != dataState.dimensions[ 
                           this.colIndex( dataState.dimensions) ] )
            x_offsets = null;

      if( y_offsets != null )
         if( y_offsets.length != dataState.dimensions[ 
                            rowIndex(dataState.dimensions) ] )
            y_offsets = null;
      int NGroups = getNGroups( dataState.dimensions );
   
      int[] ids = NexUtils.getIntArrayFieldValue( NxDetector ,
              "id" );
      if( ids == null){
         ids = NexUtils.getIntArrayFieldValue( NxDetector ,
         "detector_number" );
        if( ids != null && ids.length< NGroups)
            ids = null;
      }

      

      if( NGroups < 0 )
         return setErrorMessage( " Improper dimensions in "
                  + NxDataNode.getNodeName() );

      NxNode dist = NxDetector.getChildNode( "distance" );
      int[] distDimensions = null;
      if( version == null || version.compareTo( "2" ) < 0 )

         distDimensions = dist.getDimension();// Util.GetDimension(dist,dataState,0);
      else
         distDimensions = Util.GetDimension( dist , dataState , 0 );

      NxNode az = null;

      float[] distance , azimuth , polar;
      int[] azimuthDim , polarDim;

      distance = azimuth = polar = null;
      azimuthDim = polarDim = null;

      if( dist != null ) {

         distance = ConvertDataTypes.floatArrayValue( dist.getNodeValue() );

         String Xunits = ConvertDataTypes.StringValue( dist
                  .getAttrValue( "units" ) );

         if( distance != null )
            ConvertDataTypes
                     .UnitsAdjust( distance , Xunits , "m" , 1.0f , 0.0f );

      }
      az = NxDetector.getChildNode( "azimuthal_angle" );

      if( az != null ) {

         azimuth = ConvertDataTypes.floatArrayValue( az.getNodeValue() );
         String Xunits = ConvertDataTypes.StringValue( az
                  .getAttrValue( "units" ) );

         if( version == null || version.compareTo( "2" ) < 0 )
            azimuthDim = Util.GetDimension( az , dataState , 0 );
         else
            azimuthDim = az.getDimension();


         ConvertDataTypes.UnitsAdjust( azimuth , Xunits , "radians" , 1.0f ,
                  0.0f );
      }

      az = NxDetector.getChildNode( "polar_angle" );

      if( az != null ) {

         if( version == null || version.compareTo( "2" ) < 0 )
            polarDim = Util.GetDimension( az , dataState , 0 );
         else
            polarDim = az.getDimension();// Util.GetDimension( az, dataState,
                                          // 0) ;
         polar = ConvertDataTypes.floatArrayValue( az.getNodeValue() );
         String Xunits = ConvertDataTypes.StringValue( az
                  .getAttrValue( "units" ) );
         ConvertDataTypes
                  .UnitsAdjust( polar , Xunits , "radians" , 1.0f , 0.0f );
      }


      for( int i = startDSindex ; i < DS.getNum_entries() ; i++ ) {

         Data db = DS.getData_entry( i );
         int TotPos = i - startDSindex;

         if( States.Spectra != null )
            TotPos = States.Spectra[ i - startDSindex ];

         setFloatAttr( db , Attribute.CRATE , crate , TotPos );
         setFloatAttr( db , Attribute.INPUT , input , TotPos );
         setFloatAttr( db , Attribute.SLOT , slot , TotPos );
         setFloatAttr( db , Attribute.SOLID_ANGLE , solAng , TotPos );

         if( ids != null )
            if( ids.length > TotPos )
               db.setGroup_ID( ids[ TotPos ] );
      }


      // ------------ set up grids and pixel info list attributes ---------
      int nrows , ncols;

      
      if( detState.hasLayout == null )

         if( distance == null )
            return false;

      // ------- Determine the number of detectors and rows and cols
      // -----------------------for this NXdata--------x

      int startGridNum = dataState.startDetectorID;
      if( startGridNum < 0 )
         startGridNum = Maxx( DS , startDSindex );

      // if( detState.hasLayout == null){
      if( distDimensions == null ) {

         return setErrorMessage( "distDimensions is null" );

      }
      // }


      String layout = detState.hasLayout;
      int[] inf = get_nRowsCols( dataState , widthDim , heightDim , depthDim ,//### get_n... may have problems
               diameterDim , polarDim , azimuthDim , distDimensions , x_dirDim ,
               layout , detState , NxEntryState );
      if( inf == null )
         return true; // get_nRowsCols sets the errormessage

      nrows = inf[ 2 ];
      ncols = inf[ 1 ];
      int ngrids = inf[ 0 ];
      int row = 1 , col = 1 , grid = - 1;
      IDataGrid Grid = null;
      float[] dd;

      dd = null;
      if( detState.hasLayout != null ) {
         NxNode geom = detState.NxGeometryNode_geometry;

         if( geom == null )
            geom = detState.NxGeometryNode_origin;
         if( geom == null )
            return setErrorMessage( "Translationn information missing in NXgeometry" );
         int p = NexUtils.getNextChildNode( geom , "NXtranslation" , 0 );

         if( p > 0 ) {

            NxNode node = geom.getChildNode( p );
            node = node.getChildNode( "distances" );
            if( node != null ) {

               dd = ConvertDataTypes.floatArrayValue( node.getNodeValue() );
               String Units = NexUtils.getStringAttributeValue( node , "units" );
               if( dd != null )
                  if( Units != null )
                     ConvertDataTypes.UnitsAdjust( dd , Units , "meters" , 1f ,
                              0f );
            }
         }
      }
      dd = null; // translation in NXgeometry does not affect distance,polar &
      // azimuthal angles

      int[] dims = dataState.dimensions;

      // Find number of dimensions for the detectors
      int P = 1;
      int i;
      for( i = 0 ; ( i < dims.length - 1 ) && ( P < ngrids ) ; i++ ) {
         P *= dims[ i ];
      }

      if( P != ngrids ) {
         errormessage = "Dimensions are out of order";
         return false;
      }

      int[] detDig = new int[ i ];
      java.util.Arrays.fill( detDig , 0 );
      int nspectra = 0; // Determines next grid for RowColGrid
      int GridNSpectraCount = 0;
      if( dims.length >= 3 )
         GridNSpectraCount = dims[ dims.length - 2 ] * dims[ dims.length - 3 ];
      GridNSpectraCount /= nrows * ncols;

      int NNrows = nrows;
      int NNcols = ncols;
      if( ( x_dir == null || y_dir == null ) && dims.length >= 3 ) {
         if( nrows <= 1 && ncols <= 1 ) {
            NNrows = dims[ dims.length - 3 ];//###
            NNcols = dims[ dims.length - 2 ];//###
         }
        // if( Grid != null)
       //     Grid.setData_entries( DS );
         if( nrows <=1  && ncols <=1){
            Grid = new RowColGrid( Row(NNrows , NNcols),
                     Col(NNrows , NNcols), startGridNum );
            grid++ ;
         }

      }
      int rrow  = row, 
          ccol  = col-1;
      if( NNrows >1 || NNcols> 1 || dims.length >2)
         if( DS.getOperator( "Pixel Info" )== null)
            DS.addOperator(  new DataSetTools.operator.DataSet.Attribute.
                         GetPixelInfo_op()); 
      boolean start =true;//Should not incremet detDig the first time
      for( i = startDSindex ; i < DS.getNum_entries() ; i++ ) {

         Data db = DS.getData_entry( i );
         if( nrows >1 ||  ncols > 1 || dims.length <3 ){
            rrow = row;
            ccol  = col;
         }// Other case done in specially two places below
            
         if( ( row == 1 ) && ( col == 1 ) ) {
            // set up new grid
            // after done with previous


            if( !start){
               Incr_detDig( detDig , dataState.dimensions );
               
            }else
               start = false;
            
            if( Grid != null && !(nrows<=1 && ncols<=1 && dims.length >=3) ) {
               
               Grid.setData_entries( DS );
              
            }

            Position3D center = null;


            center = ConvertDataTypes.convertToIsaw( Val( detDig , distance ,
                     distDimensions , dims , 0 , .5f ) , Val( detDig , polar ,
                     polarDim , dims , 0 , 0f ) , Val( detDig , azimuth ,
                     azimuthDim , dims , 0 , 0f ) );


            if( nrows > 1 || ncols > 1 || dims.length < 3 ) {
               Grid = null;
               grid++ ;
            }
            else {
               nspectra++ ;
               if( nrows <2 && ncols <2){
                  ccol ++;
                  if( ccol >NNcols){
                     ccol=1;
                     rrow++;
                  }
               }
            }

            if( ( x_dir != null ) && ( y_dir != null ) )

               Grid = getGrid( startGridNum + grid , "m" ,
                        new Vector3D( center ) , Val( detDig , x_dir ,
                                 x_dirDim , dims , 0 ) , Val( detDig , y_dir ,
                                 x_dirDim , dims , 0 ) , Val( detDig , width ,
                                 widthDim , dims , 0 , 1f ) , Val( detDig ,
                                 height , heightDim , dims , 0 , 1f ) , Val(
                                 detDig , depth , depthDim , dims , 0 , .1f ) ,
                        Val( detDig , diameter , diameterDim , dims , 0 , 0f ) ,
                        detType , Row(nrows , ncols),Col(nrows,ncols) , x_offsets , 
                        y_offsets 
                        );

            else if( nrows <= 1 && ncols <= 1 && dims.length < 3 )// Have a
               // Bunch of
               // 1x1 pixels

               Grid = new UniformGrid( startGridNum + grid , "m" ,
                        new Vector3D( center ) , new Vector3D( 1f , 0f , 0f ) ,
                        new Vector3D( 0f , 1f , 0f ) , Val( detDig , width ,
                                 widthDim , dims , 0 , 1f ) , Val( detDig ,
                                 height , heightDim , dims , 0 , 1f ) , Val(
                                 detDig , depth , depthDim , dims , 0 , .1f ) ,
                        1 , 1 );

            else if( nrows>1 || ncols > 1)  //Other case t RowColGrid 
               
                Grid = new RowColGrid( Row(nrows, ncols),
                         Col(nrows,ncols), startGridNum+ grid);
            
            else if( nspectra > GridNSpectraCount  ) {// Can get proximity
               if( Grid != null )
                  Grid.setData_entries( DS );
               Grid = new RowColGrid( Row(NNrows , NNcols),
                                    Col(NNrows , NNcols), startGridNum + grid );
               grid++ ;
               nspectra = 0;
               //rrow=1;
              // ccol=1;
            }
         }
         if( Grid != null ) {
            
            DetectorPixelInfo detPix = new DetectorPixelInfo(startPixelIndex++ , 
                     (short) Row(rrow,ccol) , (short)Col(rrow, ccol) , Grid );

            DetectorPixelInfo[] piList = new DetectorPixelInfo[ 1 ];

            piList[ 0 ] = detPix;

            db.setAttribute( new PixelInfoListAttribute(
                     Attribute.PIXEL_INFO_LIST , new PixelInfoList( piList ) ) );

          //  db.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS ,
          //           new DetectorPosition( Grid.position( Row(row , col),
          //                    Col(row,col)) ) ) );

         }

         ConvertDataTypes.addAttribute( db , ConvertDataTypes
                  .CreateDetPosAttribute( Attribute.DETECTOR_POS ,
                           ConvertDataTypes.convertToIsaw(
                                    Val( detDig , distance , distDimensions ,
                                             dims , 0 , 1f ) ,
                                    Val( detDig , polar , polarDim , dims , 0 ,
                                             0f ) , Val( detDig , azimuth ,
                                             azimuthDim , dims , 0 , 0f ) ) ) );
         if( distance == null || polar == null || azimuth == null)
              db.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS ,
                       new DetectorPosition( Grid.position( Row(row , col),
                                Col(row,col)) ) ) );
         col++ ;

         if( col > ncols ) {
            col = 1;
            row++ ;
            if( row > nrows ) {
               row = 1;
            }
         }
      }
      if( Grid != null)
         Grid.setData_entries( DS );
      dataState.endDetectorID = grid+startGridNum;
      return false;
   }

    
    
    //
    /**
       * Finds the value of a list whose dims have -1 entries( * entries)
       * 
       * @param grid
       *           the "digits" in ALLDims to determine the position in the list
       * @param list
       *           the list of floats that contains the entry desired
       * @param listDims
       *           the dimensions of this list with -1 entries for "*"'s
       * @param AllDims
       *           the longest dimension for trailing "*"'s
       * @param nXtralistDims
       *           The number of trailing entries of listDim that do not
       *           correspond to entries in AllDims
       * @param Default
       *           the default value to return if the entry is not in the list.
       */
    private float Val( int[] grid, float[] list, int[] listDims, int[] AllDims, 
                                             int nXtralistDims, float Default){
       if( listDims == null )
          return Default;
       
       if( list.length < 1 )
          return Default;
       
       if( list.length == 1 )
          return list[ 0 ];
       if(  ( listDims.length < 1 ) )
          return list[ 0 ];
       
       if( grid == null )
          return list[ 0 ];
       
       if( grid.length < 1 )
          return list[ 0 ];
       
       int indx = 0;
       int mult = 1;
       
       for( int i = listDims.length - 1 ; i >= 0 ; i-- )
          if( listDims[ i ] >= 0 ){
             indx += mult* grid[ i ];
             mult *= listDims[ i ];
          }
       
       if( indx < list.length )
          return list[ indx ];
       
        return list[ list.length  - 1 ];
    }
    
    
    // Finds the value of a list whose dims have -1 entries( * entries)
    // see float Val(...) documentation
    private Vector3D Val( int[] grid , Vector3D[] list , int[] listDims ,  
                                            int[] AllDims , int nXtralistDims){
       if( listDims == null )
          return new Vector3D();
       
       if( list.length < 1 )
          return new Vector3D();
       
       if( list.length == 1 )
          return list[ 0 ];
       
       if( ( listDims.length < 1 ) )
          return list[ 0 ];
       
       int indx = 0;
       int mult = 1;
       
       for( int i = listDims.length  - 1 ; i >= 0 ; i-- )
          if( listDims[ i ] >= 0 ){
             indx += mult * grid[ i ];
             mult *= listDims[ i ];
          }
       
       if( indx < list.length )
          return list[ indx ];
      
       return list[ list.length - 1 ];
    }
    
    
    //deprecated
   /* private float Xval( float[] data , int grid , int ngrids , int[] dims ,
                                                            float Default){
       
       if(  (data == null ) || ( data.length <1 ) )
          return Default;
       
       if( grid < 0 )if( grid >= ngrids )
          return Default;
       
       if( data.length == ngrids )
          return data[ grid ];
       
       int ndims_per_grid = 0;
       int product = 1;
       int jump = 1;
       for( int i= 0 ; ( i <  dims.length ) && ( product < ngrids ) ;  i++ ){
          product *= dims[ i ];
          if( product <=  ngrids )
             ndims_per_grid++;
          if( i > data.length )
             jump *= dims[ i ];
       }
       if( product != ngrids )
          return Default;
       if( grid/jump < data.length )
          return data[ grid/jump ];
       
       return Default;
    }
    
    
    
    //deprecated
    private Vector3D   Xval(Vector3D[] data , int grid , int ngrids , 
                                              int[] dims , Vector3D Default){
       
       if( ( data == null ) ||( data.length < 1 ) )
          return Default;
       
       if( grid < 0 )if( grid >= ngrids )
          return Default;
          
       if( data.length == ngrids )
          return data[grid];
       
       int ndims_per_grid = 0;
       int product = 1;
       int jump=  1;
       for( int i = 0; ( i< dims.length ) && ( product < ngrids ) ;  i++ ){
          product *= dims[ i ];
          if( product <= ngrids )
             ndims_per_grid++;
          if( i> data.length )
             jump *= dims[ i ];
       }
       if( product != ngrids )
          return Default;
       if( grid/jump < data.length )
          return data[ grid/jump ];
       
       return Default;
    }
    
    */
    
    
    
    
    /**
     *  Tries to determine the grouping for this information across several conventions
     *  Is it a bunch of pixel elements, tubes or area detectors???
     * @param dataState  A information block about the Data blocks state
     * @param width      A width array for widths of a detector
     * @param height     A height array for the height of a detector
     * @param depth      A depth array for the depth of a detector
     * @param diameter   An array giving the diameter of a detector
     * @param polar      An array giving the polar angles  to the center of ???
     * @param azimuth    An array giving the azimuthal angles  of the center of ???
     * @param distance   An array giving the distance to the center of ???
     * @param detState   An information block with detector information
     * @return  a 1D array giving the  [#dets, #rows, #cols]
     */
     public  int[] get_nRowsCols( NxDataStateInfo dataState ,  int[] width,
              int[] height , int[] depth, int[] diameter ,
              int[] polar , int[] azimuth , int[] distance , int[] x_dir ,
              String layout ,
              NxDetectorStateInfo detState , NxEntryStateInfo EntryInfo){
        
           boolean hasNXgeometry = (detState.NxGeometryNode_geometry != null ) ;
           int geomDimFix = 0;
           if( hasNXgeometry )
              geomDimFix = 1; //extra dimension in NXgeometry vs corresponding value
                              // that would be in NXdetector
           
           int TotDims = dataState.dimensions.length - 1;
           int PolarDims = 0;
         
           if( polar != null)
                   PolarDims = polar.length;
           if( PolarDims == 1 )
              if(polar == null ||polar.length<1|| polar[0] == 1 )
                PolarDims = 0;
           if( azimuth != null )
              if( azimuth.length > PolarDims )
                 if((( azimuth.length > 1 )) || ( ( azimuth.length == 1 ) &&( azimuth[0] > 1 )))
                   PolarDims = azimuth.length;
           
           if( distance != null)
              if( distance.length > PolarDims )
                 if( (( distance.length > 1 )) || ( ( distance.length == 1 ) &&( distance[ 0 ] > 1 )))
                     PolarDims = distance.length;
           
           int widthDims = 0;
           
           if( width != null )
              if( width.length - geomDimFix > widthDims )
                 if(((width.length - geomDimFix > 1 ))||( (width.length - geomDimFix == 1) &&(width[ 0 ] > 1 )))
                 widthDims = width.length - geomDimFix;
             
           if( height != null)
              if( height.length - geomDimFix  > widthDims )
                 if(((height.length - geomDimFix > 1 )) || ( (height.length - geomDimFix == 1) &&( height[ 0 ] > 1 )))
                 widthDims = height.length - geomDimFix ;
             
           if( depth != null)
              if( depth.length - geomDimFix > widthDims )
                 if((( depth.length - geomDimFix > 1 ))||(( depth.length - geomDimFix == 1 ) &&(depth[ 0 ] > 1 )))
                 widthDims = depth.length;
             
  
              
           if( x_dir != null )
             if( x_dir.length - geomDimFix > widthDims )
                if( ( x_dir.length == 1 ) && ( x_dir[ 0 ] > 1 ) )
                   widthDims = x_dir.length -  geomDimFix ;
          
           if( layout == null )
           if( PolarDims > TotDims )
              return (int[])setErrorMessageReturnNull( 
                                    "Too many dimensions for position info ") ;
           if( layout == null )
           if( widthDims > TotDims )
              return (int[])setErrorMessageReturnNull( 
                                    "Too many dimensions for position info " ) ;
           int NN = TotDims - Math.max( PolarDims , widthDims );
           if( layout == null )
           if( ( NN < 0 ) || ( NN > 2 ))
              return (int[])setErrorMessageReturnNull( "Improper dimensions" );
           int[] Res = new int[ 3 ];
           if( layout != null )
           if( layout.equals( "point" ) )
              NN = 0;
           else if( layout.equals( "linear" ) )
              NN = 1;
           else if( layout.equals( "area" ) )
              NN = 2;
           
           if( NN == 0 ){
              Res[ 2 ] = 1;
              Res[ 1 ] = 1;
              Res[ 0 ] = prod( dataState.dimensions , 0 , TotDims - 1 );
           }else if( NN == 1  ){
              Res[ 2 ] = 1;
              Res[ 1 ] = dataState.dimensions[ TotDims - 1 ];
              Res[ 0 ] = prod(dataState.dimensions, 0 , TotDims - 2);
              
           }else if ( NN == 2 ){
              Res[ 2 ] = dataState.dimensions[ TotDims - 1 ];
              Res[ 1 ] = dataState.dimensions[ TotDims - 2  ];
              Res[ 0 ] = prod( dataState.dimensions , 0 , TotDims - 3 ) ;
              
           }           
           return Res;
        
     }
     
     
     
     
     private int prod( int[] list , int ind1 , int ind2 ){
        if( list == null )
           return 0;
        if( ind2 >= list.length )
           ind2 = list.length - 1;
        if( ind1 >= list.length )
           ind1 = list.length - 1;
        int Res = 1;
        for( int i = ind1 ;  i<= ind2 ; i++ )
           Res *= list[ i ];
        return Res;
        
     }
     /**
      * Returns an IDataGrid with the properties described by the arguments.
      * Currently only deals with UniformGrids
      * @param grid   The number associated with the new grid
      * @param units   The units for lengths in the grid
      * @param center  the center of the grid
      * @param x_dir  the direction of increasing x( column) values
      * @param y_dir  the direction of increasing y( row) values
      * @param width  the width of the grid in x direction
      * @param height  the height of the grid in y direction
      * @param depth   the depth of the grid
      * @param diameter  the diameter for circular/spherical grids
      * @param type      the type(NeXus) of the grid(nxcylinder, sphere,)
      * @param nrows    number of rows in the grid
      * @param ncols    number of columns in the grid
      * @param x_offsets  xoffsets (for sphere and cylindrical grids)
      * @param y_offsets  yoffsets(for sphere and cylindrical grids)
      * @return an IDataGrid with the properties described by the arguments.
      */
     public static IDataGrid getGrid( int grid  , String units , 
              Vector3D  center , Vector3D x_dir , 
              Vector3D y_dir ,  float width  , float height  ,
              float depth ,  float diameter, String type,
              int nrows , int ncols, float[] x_offsets, float[] y_offsets ){
       
        float Width = 1, 
              Height = 1;
        float Depth = .1f;
        if( type.equals( "nxcylinder" )){
           
              if( ( x_offsets != null ) ){
                 Width = x_offsets[  x_offsets.length -  1 ] -  2*x_offsets[ 0 ]+
                    x_offsets[ 1 ];
              }
                 
             
                 Height = height;
              
           }else if( type .equals( "sphere" )){
              if( ( x_offsets != null ) )
                 Width = x_offsets[  x_offsets.length -  1 ] -  2*x_offsets[ 0 ]+
                    x_offsets[ 1 ];
             if( ( y_offsets != null) )
                    Height = y_offsets[ y_offsets.length -  1 ] -  2 * y_offsets[ 0 ]+
                       y_offsets[ 1 ];
           }else{
              
                   Width = width;
               
                   Height = height;
        }
        
        return new UniformGrid( grid , units , center , x_dir , y_dir , Width, 
                                               Height , Depth ,  nrows ,ncols);
       
      
     }
     
     
     
     
    //Euler rotations , version null's orientation
    private Vector3D Rotate( float[] orientations , int grid , float x , float y ,
        float z ) {
     
        if ( orientations == null )
            return new Vector3D( x , y , z );
        if ( orientations.length < 3 )
            return new Vector3D( x , y , z );
 
        int p = 3 * grid;

        if ( p + 2 >= orientations.length ) {
            p = orientations.length / 3 - 1;
        }

        Tran3D Matrix = new Tran3D(  );

        Matrix.setRotation( (float ) ( orientations[ p ] * 180 /  
                            java.lang.Math.PI ) ,new Vector3D( 1 , 0 , 0 ) );
     
        Vector3D res = new Vector3D();
        Tran3D next = new Tran3D();

        next.setRotation( (float) ( orientations[ p + 1 ] * 
                           180 / java.lang.Math.PI ) ,new Vector3D( 0 , 1 , 0 ) );
        next.multiply_by( Matrix );
        Matrix = next;
     
        next = new Tran3D();
        next.setRotation( (float) ( orientations[ p + 2 ] *
                            180 / java.lang.Math.PI ) , new Vector3D( 1 , 0 , 0 ) );
        next.multiply_by( Matrix );
        Matrix = next;
    
        Matrix.apply_to( new Vector3D( x , y , z ) , res );
        return res;
     
    }

    /**
     *     Grid_util.getAreaGridIDs does NOT WORK if each detector has
     *     only one row and one column
     */
    private int Maxx( DataSet DS , int maxIndex ) {
        int res = 0;

        for ( int i = 0; i < maxIndex; i++ ) {
            Data db = DS.getData_entry( i );
            Object v = db.getAttributeValue( Attribute.PIXEL_INFO_LIST );

            if ( v != null ) {
                IPixelInfo ipInf = ( (PixelInfoList) v ).pixel( 0 );

                if ( ipInf != null )
                    if ( ipInf.gridID() > res )
                        res = ipInf.gridID();

            }
        }
        return res;

    }

    // returns the maximum number in an array
 /*   private int Maxx( int[] array ) {
        if ( array == null )
            return 0;
        int res = 0;

        for ( int i = 0; i < array.length; i++ )
            if ( array[ i ] > res )
                res = array[ i ];

        return res;

    }
  */
    //returns the index-th value in the array ir .01f if index is improper
  /*  private float Aval( float[] array , int index ) {
    
        if ( array == null )
     
            return 0.01f;
        
        if ( array.length < 1 )
     
            return 0.01f;
        
        if ( index < 0 )
     
            return 0.01f;
        
        if ( index > array.length )
     
            return array[ array.length - 1 ];
        
        return array[ index ];
        
    }
*/
    /**
     *  Currently invoked by the standard ProcessNXentry. It adds all the 
     *  data the NXmonitor class to the DataSet DS 
     *  @param DS  the DataSet that is having information added to
     *  @param NxMonitorNode The NxNode containing information about an NeXus
     *                      NXmonitor class
     *  @param startDSindex  the first GroupIndex to process
     *  @param States   The linked list of state information
     */
    public boolean setUpNXmonitorAttributes( DataSet DS , NxNode NxMonitorNode , 
        int startDSindex , NxfileStateInfo States ) {
        
        Data DB = DS.getData_entry( startDSindex );

        if ( DB == null )
            return setErrorMessage( "No Data block " + startDSindex );
        
        float x = NexUtils.getFloatFieldValue( NxMonitorNode , "distance" )
                                                            .floatValue();
        float[] yvals= DB.getY_values(); 
        float totCount =0;
        for( int kk=0; kk< yvals.length;kk++)
           totCount +=yvals[kk];
        DB.setAttribute( new FloatAttribute( Attribute.TOTAL_COUNT, totCount) );
        if ( !Float.isNaN( x ) )
            ConvertDataTypes.addAttribute( DB , new DetPosAttribute(
                    Attribute.DETECTOR_POS , new DetectorPosition(
                        new Position3D( new Vector3D( x , 0f , 0f ) ) ) ) );
       
        return false;
    }

    /**
     *  Currently invoked by the standard Process1Nxdata. It adds all the 
     *  data from the data field of NXdata to the DataSet DS using default
     *  GroupID's.  Units are changed to ISaw units
     *  @param DS  the DataSet that is having information added to
     *  @param NxDataNode The NxNode containing information about an NeXus
     *                      NXdata class
     *  @param startGroupID  the default GroupID for the first new DataBlock 
     *                       added by this method
     *  @param States   The linked list of state information
     */
    public boolean setUpNxData( DataSet DS , NxNode NxDataNode , 
        int startGroupID , NxfileStateInfo States ) {

        //int startDSindex = DS.getNum_entries();
        NxDataStateInfo DataInf = NexUtils.getDataStateInfo( States );

        if ( DataInf == null )
            return setErrorMessage( "No State info for NXdata " + NxDataNode.
                                                             getNodeName() );
        
        NxNode dataNode = NxDataNode.getChildNode( "data" );
     
        int[] dimensions = DataInf.dimensions;//dataNode.getDimension();
     
        int NGroups = getNGroups( dimensions );
     
        if ( NGroups < 0 )
            return setErrorMessage( " Improper Dimensions for NXdata " + 
                    NxDataNode.getNodeName() );
                             
        int length = dimensions[ dimensions.length - 1 ];
        float[] data = ConvertDataTypes.floatArrayValue( dataNode.
                                                          getNodeValue() );
    
        if ( data == null ) 
            return setErrorMessage( "No data in NxData" );
        
        if ( ( DataInf.axisName == null ) || ( DataInf.axisName.length < 1 ) ) 
            return setErrorMessage( "No axis 1 information in NXnode " + 
                    NxDataNode.getNodeName() );
        int n = 0;
        if( DataInf.XlateAxes != null)
           n = DataInf.XlateAxes[0]- 1;
        NxNode tofNode = NxDataNode.getChildNode( DataInf.axisName[ n] );

        if ( tofNode == null )
            return setErrorMessage( "No tof axis named " + 
                 DataInf.axisName[ n ] + "in SetupNxData" );
                 
        float[] xvals = ConvertDataTypes.floatArrayValue( tofNode.
                                                     getNodeValue() );
        
        xvals = MakeHistogram( xvals , tofNode );
        String Xunits = ConvertDataTypes.StringValue(
                tofNode.getAttrValue( "units" ) ); 

        ConvertDataTypes.UnitsAdjust( xvals , Xunits , xUnits , 1.0f , 0.0f );

        NxNode errNode = NxDataNode.getChildNode( "errors" );
        float[] evals = null;
     
        if ( errNode != null ) {
            evals = ConvertDataTypes.floatArrayValue( errNode.getNodeValue() );
      
        }
     
        if ( xvals == null )
            return setErrorMessage( "no x values" );
     
        if ( xvals.length != length )
            if ( xvals.length != length + 1 )
                return setErrorMessage( "Improper length for axis 1" );
    
        XScale xsc = new VariableXScale( xvals );

        for ( int i = 0 ; i < NGroups; i++ ) {
            int id = startGroupID + i;  //will have NXdetector change groupID

            if ( ( States.Spectra == null ) ||
                ( Arrays.binarySearch( States.Spectra , id ) >= 0 ) ) {
               
                float[] yvals = new float[ length ];
                float[] errs  = new float[length ];
                
                HistogramTable DB ;
                System.arraycopy( data , i * length , yvals , 0 , length );
                
                if( evals != null){
                   
                    System.arraycopy( evals , i * length , errs , 0 , length );
                    DB = new HistogramTable( xsc , yvals , errs , id );
                    
                }else{
                   
                   errs = null;
                   DB = new HistogramTable( xsc , yvals ,  id );
                }
                float totCount =0;
                for( int kk=0; kk< yvals.length;kk++)
                   totCount +=yvals[kk];
                DB.setAttribute( new FloatAttribute( Attribute.TOTAL_COUNT, totCount) );
                DS.addData_entry( DB );
            }
            
        }  
     
        return false;
    }



    /**
     * Gets the value of a subnode. This method handles nulls
     * 
     * @param parentNode  parent Node. Can be null
     * @param subNodeName Thw subnode name
     * @return The value of the subnode
     */
    public static Object getSubNodeValue( NxNode parentNode , String subNodeName ){
      
        NxNode subNode = parentNode.getChildNode( subNodeName );

        if ( subNode == null )
            return null;
        return subNode.getNodeValue();
    }
    
    
    // sets error message and returns null
    private Object setErrorMessageReturnNull( String err){
       errormessage = err;
       return null;
    }

    //Sets error message and returns true
    private boolean setErrorMessage( String err ) {
        errormessage = err;
        return true;
    }

    /**
     *  returns an errormessage or an empty string if there is no error 
     */
    public String getErrorMessage() {
        return errormessage;
    }
    
    

    /**
     *    Gets the given attribute and converts it to an Integer.
     *    This method is null passing
     *    
     *    @param Node  the node with the attribute( can be null)
     *    
     *    @param AttributeName the name of the attribute
     *    
     *    @return an Integer or null if the node or Attribute name is
     *            null or the resulting attribute cannot be converted
     *            to an Integer.
     */
    public static Integer getIntAttributeValue( NxNode Node , 
                                              String AttributeName ){
                                                
        if ( Node == null )
            return null;
        
        if ( AttributeName == null )
            return null;
        
        Object Val = Node.getAttrValue( AttributeName );
        int n = ConvertDataTypes.intValue( Val );
     
        if ( n == Integer.MIN_VALUE )
            return null;
        
        return new Integer( n );
    }

    /**
     *    Gets the given attribute and converts it to a Float.
     *    This method is null passing
     *    
     *    @param Node  the node with the attribute( can be null)
     *    
     *    @param AttributeName the name of the attribute
     *    
     *    @return a Float or null if the node or Attribute name is
     *            null or the resulting attribute cannot be converted
     *            to a  Float.
     */
    public static Float getFloatAttributeValue( NxNode Node , 
                                                   String AttributeName ) { 
                                                      
        if ( Node == null )
            return null;
        
        if ( AttributeName == null )
            return null;
        
        Object Val = Node.getAttrValue( AttributeName );
     
        float f = ConvertDataTypes.floatValue( Val );

        if ( Float.isNaN( f ) )
            return null;
        
        return new Float( f );

    }

    /**
     *    Gets the given attribute and converts it to a String.
     *    This method is null passing
     *    
     *    @param Node  the node with the attribute( can be null)
     *    
     *    @param AttributeName the name of the attribute
     *    
     *    @return a String or null if the node or Attribute name is
     *            null or the resulting attribute cannot be converted
     *            to a String.
     */
    public static String getStringAttributeValue( NxNode Node , 
                                             String AttributeName ) {
                                               
        if ( Node == null )
            return null;
        
        if ( AttributeName == null )
            return null;
        
        Object Val = Node.getAttrValue( AttributeName );

        return  ConvertDataTypes.StringValue( Val );

    }    
  
    /**
     *    Gets the given field value and converts it to an Integer.
     *    This method is null passing
     *    
     *    @param Node  the node with the field( can be null)
     *    
     *    @param FieldName the name of the field
     *    
     *    @return an Integer or null if the node or field name is
     *            null or the resulting field cannot be found or
     *            converted to an Integer.
     */
    public static Integer getIntFieldValue( NxNode Node , String FieldName ) {
        if ( Node == null )
            return null;
        
        if ( FieldName == null )
            return null;
        
        
        if( Node.getNodeClass() .equals("SDS"))
           return null;
        
        NxNode Child = Node.getChildNode( FieldName );

        if ( Child == null )
            return null;
        
        int n = ConvertDataTypes.intValue( Child.getNodeValue(  ) );

        if ( n == Integer.MIN_VALUE )
            return null;
        
        return new Integer( n );

    }

    /**
     *    Gets the given field value and converts it to an int array.
     *    This method is null passing
     *    
     *    @param Node  the node with the field( can be null)
     *    
     *    @param FieldName the name of the field
     *    
     *    @return an int[] or null if the node or field name is
     *            null or the resulting field cannot be found or
     *            converted to an int[].
     */
    public static int[] getIntArrayFieldValue( NxNode Node , String FieldName ) {
        if ( Node == null )
            return null;
        
        if ( FieldName == null )
            return null;
        
        
        if( Node.getNodeClass() .equals("SDS"))
           return null;
        
        NxNode Child = Node.getChildNode( FieldName );

        if ( Child == null )
            return null;
        
        return ConvertDataTypes.intArrayValue( Child.getNodeValue() );

    }




    /**
     *    Gets the given attribute and converts it to a int Array.
     *    This method is null passing
     *    
     *    @param Node  the node with the attribute( can be null)
     *    
     *    @param AttrName the name of the attribute
     *    
     *    @return an int[] or null if the node or Attribute name is
     *            null or the resulting attribute cannot be converted
     *            to a String.
     */
  public static int[] getIntArrayAttributeValue( NxNode Node , String AttrName ) {
      if ( Node == null )
          return null;
        
      if ( AttrName == null )
          return null;
        
     Object Attr = Node.getAttrValue( AttrName );

      if (Attr == null )
          return null;
        
      return ConvertDataTypes.intArrayValue( Attr );

  }

  /**
   *    Gets the given field value and converts it to a Float.
   *    This method is null passing
   *    
   *    @param Node  the node with the field( can be null)
   *    
   *    @param FieldName the name of the field
   *    
   *    @return a Float or null if the node or field name is
   *            null or the resulting field cannot be found or
   *            converted to a Float.
   */
    public static Float getFloatFieldValue( NxNode Node , String FieldName ) {
        if ( Node == null )
            return null;
        
        if ( FieldName == null )
            return null;
        
        if( Node.getNodeClass() .equals("SDS"))
           return null;
        
        NxNode Child = Node.getChildNode( FieldName );

        if ( Child == null )
            return null;
        
        float f = ConvertDataTypes.floatValue( Child.getNodeValue() );

        if ( Float.isNaN( f ) )
            return null;
        
        return new Float( f );

    }

    /**
     *    Gets the given field value and converts it to an float array.
     *    This method is null passing
     *    
     *    @param Node  the node with the field( can be null)
     *    
     *    @param FieldName the name of the field
     *    
     *    @return an float[] or null if the node or field name is
     *            null or the resulting field cannot be found or
     *            converted to an float[].
     */
    public static float[] getFloatArrayFieldValue( NxNode Node  , 
                                                    String FieldName ) {
                                                      
        if ( Node == null )
            return null;
        
        if ( FieldName == null )
            return null;
        
        
        if( Node.getNodeClass() .equals("SDS"))
           return null;
        NxNode Child = Node.getChildNode( FieldName );

        if ( Child == null )
            return null;
        
        return ConvertDataTypes.floatArrayValue( Child.getNodeValue() );
    }



    /**
     *    Gets the given field value and converts it to an String.
     *    This method is null passing
     *    
     *    @param Node  the node with the field( can be null)
     *    
     *    @param FieldName the name of the field
     *    
     *    @return a String or null if the node or field name is
     *            null or the resulting field cannot be found or
     *            converted to a String.
     */
    public static String getStringFieldValue( NxNode Node  , String FieldName ) {
    
        if ( Node == null )
            return null;
        
        if ( FieldName == null )
            return null;
        
        if( Node.getNodeClass() .equals("SDS"))
           return null;
        
        NxNode Child = Node.getChildNode( FieldName );

        if ( Child == null )
            return null;
        
        return ConvertDataTypes.StringValue( Child.getNodeValue() );

    }    



    private void setFloatAttr( Data db , String AttributeName , float[] crate ,
        int TotPos ) {
            
        if ( crate == null )
            return;
        
        if ( db == null )
            return;
        
        if ( AttributeName == null )
            return;
        
        if ( TotPos < 0 )
            return;
        
        if ( crate.length <= TotPos )
            return;
        
        db.setAttribute( new FloatAttribute( AttributeName , crate[ TotPos ] ) );

    }
    
    

    /**
     *    Makes a histogram from function values. tofNode has an attribute
     *    histogram_offset that can be used to get the left boundary
     *    
     *    @param xvals  The list of xvals("times")
     *    @param tofNode The time of flight NeXus node with the 
     *                  histgram_offset attribute
     *     @return a new set of xvals that are histogrammed if needed
     *    
     */
    public static float[] MakeHistogram( float[] xvals , NxNode tofNode ) {
    
        if ( xvals == null )
            return null;
        
        float histOffset = ConvertDataTypes.floatValue(
                tofNode.getAttrValue( "histogram_offset" ) );

        if ( Float.isNaN( histOffset ) )
            return xvals;
        
        if ( histOffset == 0 )
            return xvals;

        float[] Res = new float[ xvals.length + 1 ];
     
        float left = xvals[ 0 ] - histOffset;

        Res[ 0 ] = left;
        for ( int i = 0; i < xvals.length ; i++ ) {
       
            float right = xvals[ i ] - left + xvals[ i ];

            Res[ i + 1 ] = right;
            left = right;
        } 
       
        return Res;
    }
 
 
 
  
    private int getNGroups( int[] dimension ) {
    
        if ( dimension == null )
            return -1;
        
        if ( dimension.length < 1 ) 
            return -1;
        
        int NGroups = 1;

        for ( int i = 0; i < dimension.length - 1; i++ )
            NGroups *= dimension[ i ];
        
        return NGroups;

    }

    /**
     *   Returns the first NxEntryStateInfo in the fileState link list of state
     *   information
     *   
     *   @param fileState The starting State in the linked list
     */
    public static NxEntryStateInfo getEntryStateInfo(
                                         NxfileStateInfo fileState ) {
                                           
        if ( fileState == null )
            return null;
     
        for ( StateInfo inf = fileState; inf != null; inf = inf.getNext() ) {
            if ( inf instanceof NxEntryStateInfo )
                return (NxEntryStateInfo) inf;
        }
   
        return null;
    }
 
 
 
 
    /**
     *   Returns the first NxDataStateInfo in the fileState link list of state
     *   information
     *   
     *   @param fileState The starting State in the linked list
     */
    public static NxDataStateInfo getDataStateInfo( NxfileStateInfo fileState ) {
      
        if ( fileState == null )
            return null;
     
        for ( StateInfo inf = fileState ; inf != null; inf = inf.getNext() ) {
            if ( inf instanceof NxDataStateInfo )
                return (NxDataStateInfo) inf;
        }
   
        return null;
    
    }
    
    
    

    /**
     *   Returns the first NxDetectorStateInfo in the fileState link list of state
     *   information
     *   
     *   @param fileState The starting State in the linked list
     */
    public static NxDetectorStateInfo getDetectorStateInfo(
                                            NxfileStateInfo fileState ) {
                                              
        if ( fileState == null )
            return null;
     
        for ( StateInfo inf = fileState; inf != null; inf = inf.getNext() ) {
            if ( inf instanceof NxDetectorStateInfo )
                return ( NxDetectorStateInfo ) inf;
        }
   
        return null;
    
    }
 
    /**
     * Gets data out of a NeXus NXgeometry node. So far it only supports shapes
     * of nxbox, nxcylinder,and nxsphere.  The dataDescr for the shapes are length,
     * width, height, and diameter.  The dataDescr supported by the field with
     * the class NXorientation is x_dir and y_dir which give the components in
     * ISAW coordinates of the direction cosines.
     * 
     * @param geomNode   a node corresponding to an NXgeometry node
     * 
     * @param dataDescr  A description of the data to be extracted. Must be
     *      length, width, height, diameter, x_dir , or y_dir
     *      
     * @param detState  the state info for this detector
     * 
     * @return  the corresponding data or null if the data is not present
     */
    public static float[] getNxGeometryInfo( NxNode geomNode ,
               String dataDescr , NxDetectorStateInfo detState) {
       
        if ( geomNode == null )
               return null;
     
        if ( dataDescr == null )
            return null;
     
        if ( "length;width;height;diameter;".indexOf( dataDescr + ";" ) >= 0 ) {//data in shape
            int p = NexUtils.getNextChildNode( geomNode , "NXshape" , 0 );

            if ( p < 0 )
                return null;
        
            NxNode node = geomNode.getChildNode( p );
            String  type = NexUtils.getStringFieldValue( node , "shape" );

            if ( type == null )
                return null;
       
            int nparms = -1, 
                pos_parm = -1;

            if (type.equals( "nxbox" ) ) {
       
                nparms = 3;
                if (dataDescr.equals( "length" ) )
                    pos_parm = 0;
                else if (dataDescr.equals( "width" ) )
                    pos_parm = 1;
                else if (dataDescr.equals( "height" ) )
                    pos_parm = 2;
            } else if (type.equals( "nxcylinder" ) ) {
       
                nparms = 2;
                if (dataDescr.equals( "diameter" ) )
                    pos_parm = 0;
                else if (dataDescr.equals ("height" ) )
                    pos_parm = 1;
       
            } else if (type.equals( "nxsphere" ) ) {
       
                nparms = 1;
                if (dataDescr.equals( "diameter" ))
                    pos_parm = 0;
            }
     
            if (nparms < 0 )
                return null;
       
            if (pos_parm < 0 )
                return null;
       
            float[] dat = NexUtils.getFloatArrayFieldValue( node , "size" );

            if (dat == null)
                return null;
        
            float[] res = new float[ dat.length / nparms ];

            for (int  i = 0; i < res.length; i++ )
                res[ i ] = dat[ nparms*i + pos_parm ];
       
            return res;
            
        } else if ( "x_dir;y_dir;".indexOf( dataDescr + ";" ) >= 0 ) {
                                                    //info in NXorientation 
            int p = NexUtils.getNextChildNode(geomNode , "NXorientation" , 0);

            if (p < 0)
                return null;
       
            float[] orient = NexUtils.getFloatArrayFieldValue(geomNode.
                                 getChildNode(p) , "value" );
    
            if (orient == null)
                return null;
        
            float res[] = new float[ orient.length / 2 ];

            p = -1;
            if (dataDescr.equals( "x_dir" ))
                p = 0;
            else if (dataDescr.equals( "y_dir" ))
                p = 3;
            if (p < 0 )
                return null;
       
            int j = 0;

            for (int i = 0; i < orient.length; i += 6 ) {
       
                if (p == 0) {
          
                    res[ j + 0 ] = orient[ i ];
                    res[ j + 1 ] = orient[ i + 1 ];
                    res[ j + 2 ] = orient[ i + 2 ];
          
                } else {
                    res[ j ] = orient[ i + 3 ];
                    res[ j + 1 ] = orient[ i + 4 ];
                    res[ j + 2 ] = orient[ i + 5 ];
                }
                j += 3;
            }
     
            return res;  
        }
   
        return null;
    }
 
    
    
    /**
     * Returns the index of the next child node with index more than or equal
     * to startIndex that has the given class name.
     * @param node   The Nexus Node
     * 
     * @param className  The className that is being sought
     * 
     * @param startIndex  The index in the fields of node to start with
     * 
     * @return  the index of the next node with the given class name or -1 if 
     *           none
     */
    public static int getNextChildNode( NxNode node , String className , 
        int startIndex ) {
       
        if ( node == null )
            return -1;
     
        if ( className == null )
            return -1;
   
        for ( int i = startIndex; i < node.getNChildNodes(); i++ )
            if ( node.getChildNode(i).getNodeClass().equals( className ))
                return i;
         
        return -1;
    }
    
    
   /**
    *  The dimension of NXdata blocks are sometimes reversed by Fortran
    *  programmers 
    *  
    * @param dim  The array of the dimensions as given by NeXus
    * 
    * @param leadLength  The length of the fastest changing dimension
    * 
    * NOTE if leadLength or leadLength-1 does not match the fist
    * or last entry of dim( it should match the last), nothing is changed
    */
    public static void disFortranDimension( int[] dim, int leadLength){
        return;/*//Was not the real problem
        if( dim == null)
           return;
        if(leadLength < 0)
           return;
         if( dim[dim.length-1]==leadLength)
            return;

         if( dim[dim.length-1]==leadLength-1)
           return;
           
         if( (dim[0]!=leadLength)&&(dim[0]!=leadLength-1))
            return;
         for( int i=0; i< dim.length/2; i++){
           int x = dim[i];
           dim[i]=dim[dim.length-1-i];
           dim[dim.length-1-i]=x;
         }
        */    
    }
}
