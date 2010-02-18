/* 
 *  * File: DumpGrids.java
 * 
 * Copyright (C) 2009, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.EventList;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.MathTools.Geometry.Position3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.util.*;
import java.io.*;

import org.nexusformat.NexusFile;

import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.Generic.TOF_SCD.*;

import NexIO.NexApi.*;
import NexIO.Util.ConvertDataTypes;
import NexIO.Util.NexUtils;
import NexIO.*;

/**
 * Write out the detector grid information from a NeXus file, in the form
 * required by the QMapper (.DetCal).
 */
public class DumpGrids
{

   /**
    * Gets a Vector of Uniform grids from the NeXus file
    * 
    * @param filename
    *           The name of the NeXus file
    * 
    * @return A Vector consisting of L1 as the first element and the Uniform
    *         grids as the second element or an ErrorString if this is not
    *         possible.
    * 
    *         Assumes that there is only one NXinstrument in the file and it
    *         occurs at the top level or under an NXentry node.
    */
   public static Object GetGrids(String filename)
   {

      if ( filename == null || filename.length( ) < 1 )
         return new ErrorString( "Improper filename" );
      NxNode nex = null;
      NxNode SourceNode = null;
      NxNode InstrNode = null;
      NxNode topNode = null;
      try
      {
         nex = new NexNode( filename );
         topNode = nex;
         String err = nex.getErrorMessage( );

         if ( err != null && err.length( ) > 0 )
            return RetClose( new ErrorString( " Cannot open file:" + err ) ,
                  topNode );

         int nchildren = nex.getNChildNodes( );
         boolean found = false;
         for( int child = 0 ; !found ; )
         {
            NxNode node = nex.getChildNode( child );
           // System.out.println("class-name:"+node.getNodeClass()+"-"+node.getNodeName());
            if ( node == null )
            {
               found = true;
               nex = null;

            } else if ( node.getNodeClass( ).equals( "NXinstrument" ) )
            {
               InstrNode = node;
               nex = InstrNode;
               child = 0;

            } else if ( node.getNodeClass( ).equals( "NXentry" ) )
            {
               nex = node;
               nchildren = nex.getNChildNodes( );
               child = 0;

            } else if ( node.getNodeClass( ).equals( "NXsource" ) )
            {
               if ( SourceNode == null )
                  SourceNode = node;
               
               child++ ;

            } else if ( node.getNodeClass( ).equals( "NXmoderator" ) )
            {
               SourceNode = node;
               child++ ;

            } else

            {
               child++ ;
               if ( child >= nchildren )
               {
                  found = true;
                  nex = null;
               }
            }
         }
         if ( InstrNode == null || SourceNode == null )
            return RetClose( new ErrorString( "Cannot find an NXinstrument " ) ,
                  topNode );

      } catch( Exception s )
      {
         return RetClose( new ErrorString( "Cannot open file:" + s ) , topNode );
      }
      // InstrNode is an NXinstrument Node

      float L1 = ConvertDataTypes.floatValue( NexUtils.getFloatFieldValue(
            SourceNode , "distance" ) );

      Vector< IDataGrid > grids = new Vector< IDataGrid >( );
      int nchildren = InstrNode.getNChildNodes( );
      
      for( int i = 0 ; i < nchildren ; i++ )
      {
         NxNode nxdet = InstrNode.getChildNode( i );
         
         if ( nxdet.getNodeClass( ).equals( "NXdetector" ) )
         {
            Object res = GetDetectors( nxdet , grids );
            
            if ( res instanceof ErrorString )
               return RetClose( res , topNode );
         }
      }

      IDataGrid[] Grids = grids.toArray( new IDataGrid[ 0 ] );
      Arrays.sort( Grids , new GridNumComparator( ) );

      grids = new Vector< IDataGrid >( );
      for( int i = 0 ; i < Grids.length ; i++ )
         grids.add( Grids[i] );

      Vector Res = new Vector( 2 );
      Res.add( L1 );
      Res.add( grids );
      topNode.close( );
      return Res;

   }

   private static Object RetClose(Object Result, NxNode node)
   {

      if ( node != null )
         node.close( );
      return Result;
   }

   public static Object PrintDetCalFile(String NexusFileName,
         String DetCalFileName)
   {

      Object Res = GetGrids( NexusFileName );

      if ( Res instanceof ErrorString )
         return Res;

      float L0 = ( ( Float ) ( ( ( Vector ) Res ).firstElement( ) ) )
            .floatValue( );
      
      Vector< IDataGrid > grids = ( Vector< IDataGrid > ) ( ( ( Vector ) Res )
            .lastElement( ) );
      
      L0 = Math.abs( L0 );
      try
      {
         String outfilename = DetCalFileName;
         PrintStream out = new PrintStream( outfilename );

         out.println( "#" );
         out
               .println( "# Detector Position Information Extracted From NeXus file: " );
         out.println( "# " + NexusFileName );
         out.println( "#" );
         out.println( "# Lengths are in centimeters." );
         out
               .println( "# Base and up give directions of unit vectors for a local" );
         out.println( "# x,y coordinate system on the face of the detector." );
         out.println( "#" );
         out.println( "#" );
         out.println( "# " + ( new Date( ) ).toString( ) );
         out.println( "6         L1     T0_SHIFT" );
         out.printf( "7 %10.4f            0\n" , L0 * 100 );
         out
               .println( "4 DETNUM  NROWS  NCOLS  WIDTH   HEIGHT   DEPTH   DETD   "
                     + "CenterX   CenterY   CenterZ    BaseX    BaseY    BaseZ    "
                     + "  UpX      UpY      UpZ" );

         for( int i = 0 ; i < grids.size( ) ; i++ )
            out.println( Peak_new_IO.GridString( grids.elementAt( i ) ) );
         out.close( );
      } catch( Exception s )
      {
         s.printStackTrace( );
         return new ErrorString( s );
      }
      return null;

   }

   /**
    * Adds new Area or RowColGrids grids that are in this detectorNode to the
    * grids vector
    * 
    * @param detectorNode
    *           The current detector Node
    * 
    * @param grids
    *           The list of grids
    * 
    * @return null or an ErrorString
    */
   public static Object GetDetectors(NxNode detectorNode,
         Vector< IDataGrid > grids)
   {

      // will currently only do RowColGrids or 1x1 grid if distance is 1D
      // later check the nxGeometry name= geometry if not enough info given
      NxNode distNode = null;
      NxNode aziNode = null;
      NxNode polarNode = null;
      NxNode geomNode = null;

      if ( detectorNode == null || grids == null )
         return new ErrorString( " Invalid Detector Node " );

      int nchildren = detectorNode.getNChildNodes( );
      for( int i = 0 ; i < nchildren ; i++ )
      {
         NxNode nex = detectorNode.getChildNode( i );

         if ( nex.getNodeClass( ).equals( "NXgeometry" ) )
         {
            if ( nex.getNodeName( ).equals( "geometry" ) )

               geomNode = nex;

         } else if ( nex.getNodeClass( ).equals( "SDS" ) )
         {
            String NodeName = nex.getNodeName( );
            if ( NodeName.equals( "distance" ) )

               distNode = nex;

            else if ( NodeName.equals( "azimuthal_angle" ) )

               aziNode = nex;

            else if ( NodeName.equals( "polar_angle" ) )

               polarNode = nex;
         }
      }
      if ( distNode == null || aziNode == null || polarNode == null )
         return new ErrorString( "Not enough position info given" );

      // assume geometry = null;
      int[] dimDist = distNode.getDimension( );
      int[] dimAzi = aziNode.getDimension( );
      int[] dimPolar = polarNode.getDimension( );

      if ( dimDist == null || dimAzi == null || dimPolar == null )
         return new ErrorString( "Not enough position info given" );

      if ( dimDist.length == dimAzi.length && dimAzi.length == dimPolar.length )
      {
      } else

         return new ErrorString( "Dimensions not the same length" );

      for( int i = 0 ; i < dimDist.length ; i++ )
         if ( dimDist[i] != dimAzi[i] || dimAzi[i] != dimPolar[i] )

            return new ErrorString( "Dimensions of position info do not match " );

      String name = detectorNode.getNodeName( );
      int ID = ExtGetDS.getBankNum( name );

      if ( dimDist.length == 1 )

         return Create1by1Grids( distNode , aziNode , polarNode , grids , ID );

      else

         return CreateRowColGrids( distNode , aziNode , polarNode , grids , ID );

   }

   private static Object Create1by1Grids(NxNode dimDist, NxNode dimAzi,
         NxNode dimPolar, Vector< IDataGrid > grids, int ID)
   {

      return null;
   }

   private static Object CreateRowColGrids(NxNode dimDist, NxNode dimAzi,
         NxNode dimPolar, Vector< IDataGrid > grids, int ID)
   {

      float[] distances = ConvertDataTypes.floatArrayValue( dimDist
            .getNodeValue( ) );
      
      float[] azimuthals = ConvertDataTypes.floatArrayValue( dimAzi
            .getNodeValue( ) );
      
      float[] polars = ConvertDataTypes.floatArrayValue( dimPolar
            .getNodeValue( ) );
      

      if ( distances == null || azimuthals == null || polars == null )
         return new ErrorString( "Not enough position info in file" );

      if ( distances.length != azimuthals.length
            || azimuthals.length != polars.length )
         
         return new ErrorString(
               "Position informations have inconsistent dimensions" );
      
      
      int k = 0;
      int[] dimension = dimDist.getDimension( );
      int ncols = dimension[0];
      int nrows = dimension[1];
      
      if ( ID < 0 || dimension.length > 2 )
         ID = grids.size( );

      RowColGrid detector = new RowColGrid( nrows , ncols , ID );
      UniformXScale xscl = new UniformXScale( 0 , 1 , 2 );
      
      float[] xvals = new float[]
      { 3f };
      float[] yvals = xvals;
      
      while( k < distances.length )
      {
         DataSet DS = new DataSet( );
         
         for( int c = 0 ; c < ncols ; c++ )
            for( int r = 0 ; r < nrows ; r++ )
            {
               HistogramTable D = new HistogramTable( xscl , xvals , yvals , k );
               DS.addData_entry( D );
               
               Position3D pos = new Position3D( );
               pos
                     .setSphericalCoords( distances[k] , azimuthals[k] ,
                           polars[k] );
               k++ ;
               float[] coords = pos.getCartesianCoords( ); // in SNS
               
               UniformGrid grid = new UniformGrid( ID , "m" , new Vector3D(
                     coords[2] , coords[0] , coords[1] ) , new Vector3D( 1 , 0 ,
                     0 ) , new Vector3D( 0 , 1 , 0 ) , .01f , .01f , .01f , 1 ,
                     1 );

               DetectorPixelInfo det = new DetectorPixelInfo( k ,
                     ( short ) ( r + 1 ) , ( short ) ( c + 1 ) , grid );
               
               D.setAttribute( new PixelInfoListAttribute(
                     Attribute.PIXEL_INFO_LIST , new PixelInfoList( det ) ) );
               
               D.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS ,
                     new DetectorPosition( grid.position( ) ) ) );
               
               detector.setOneData( D , r , c );

            }

         detector.setPixelPositions( DS );
         
         grids.add( RowColGrid.GetDataGrid( detector , .02f ) );

         ID++ ;
         detector = new RowColGrid( nrows , ncols , ID );

      }

      return null;
   }

   public static void main(String args[])
   {

      String NexFileName = "C:/ISAW/Sampleruns/SNS/Snap/SNAP_263.nxs";
      String DetCalFileName = "C:/ISAW/Sampleruns/SNS/Snap/SNAP.DETCAL";
      System.out.println( "Done Res="
            + DumpGrids.PrintDetCalFile( NexFileName , DetCalFileName ) );
   }

   /**
    * Dump out a file with Detector Position information from the specified
    * NeXus file. The file that is written will have the name of the input
    * .NeXus file plus the extension ".grids". This file has the form of
    * Detector Calibration file (.DetCal), but just has the geometry info from
    * the NeXus, and a T0_SHIFT value of 0.
    * 
    * @param args
    *           Array of command line arguments. args[0] must be the fully
    *           qualified name of the .nxs file that has the required geometry
    *           information.
    */
   public static void main1(String args[]) throws Exception
   {

      Vector< IDataGrid > grids = new Vector< IDataGrid >( );
      float initial_path = 0;
      boolean have_initial_path = false;

      if ( args.length <= 0 )
         throw new IllegalArgumentException(
               "First argument must be the file name" );
      String filename = args[0];

      NexusRetriever nr = new NexusRetriever( filename );
      // nr.RetrieveSetUpInfo( null );

      int num_ds = nr.numDataSets( );
      System.out.println( "Number of DataSets = " + num_ds );

      for( int i = 1 ; i < num_ds ; i++ )
      {
         DataSet ds = nr.getDataSet( i );
         // System.out.println("DataSet " + i + " has title " + ds.getTitle() );
         if ( ds.getNum_entries( ) <= 0 )
            System.out.println( "NO DATA ENTRIES IN " + ds.getTitle( ) );
         else
         {
            Data data = ds.getData_entry( 0 );

            if ( !have_initial_path )
            {
               float temp = AttrUtil.getInitialPath( data );
               if ( !Float.isNaN( temp ) )
               {
                  initial_path = temp;
                  have_initial_path = true;
               }
            }

            PixelInfoList pil = AttrUtil.getPixelInfoList( data );
            if ( pil == null )
               System.out.println( "NO PIXEL INFO IN " + ds.getTitle( ) );
            {
               IPixelInfo pi = pil.pixel( 0 );
               if ( pi == null )
                  System.out.println( "NULL PIXEL INFO IN " + ds.getTitle( ) );
               else
               {
                  IDataGrid grid = pi.DataGrid( );
                  System.out.println( "For DS : " + ds.getTitle( )
                        + " Got grid " + grid.ID( ) );
                  grids.add( grid );
               }
            }
         }
      }

      nr.close( );

      String outfilename = filename + ".grids";
      PrintStream out = new PrintStream( outfilename );

      out.println( "#" );
      out
            .println( "# Detector Position Information Extracted From NeXus file: " );
      out.println( "# " + filename );
      out.println( "#" );
      out.println( "# Lengths are in centimeters." );
      out.println( "# Base and up give directions of unit vectors for a local" );
      out.println( "# x,y coordinate system on the face of the detector." );
      out.println( "#" );
      out.println( "#" );
      out.println( "# " + ( new Date( ) ).toString( ) );
      out.println( "6         L1     T0_SHIFT" );
      out.printf( "7 %10.4f            0\n" , initial_path * 100 );
      out.println( "4 DETNUM  NROWS  NCOLS  WIDTH   HEIGHT   DEPTH   DETD   "
            + "CenterX   CenterY   CenterZ    BaseX    BaseY    BaseZ    "
            + "  UpX      UpY      UpZ" );

      for( int i = 0 ; i < grids.size( ) ; i++ )
         out.println( Peak_new_IO.GridString( grids.elementAt( i ) ) );
      out.close( );

      System.exit( 0 );
   }

   static class GridNumComparator implements Comparator
   {

      @Override
      public int compare(Object arg0, Object arg1)
      {

         int ID1 = ( ( IDataGrid ) arg0 ).ID( );
         int ID2 = ( ( IDataGrid ) arg1 ).ID( );
         if ( ID1 < ID2 )
            return -1;
         else if ( ID1 > ID2 )
            return 1;
         return 0;

      }

   }
}
