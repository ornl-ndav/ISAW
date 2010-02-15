/* 
 * File: General_Utils.java
 *
 * Copyright (C) 2010, Ruth Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */

package Operators.TOF_SCD;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import DataSetTools.components.ParametersGUI.JParametersDialog;
import DataSetTools.dataset.UniformGrid;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;
import gov.anl.ipns.MathTools.Geometry.Tran3D;
import Operators.Special.*;

/**
 * This class contains utilities such as rotating the detectors
 * in a .DetCal file.
 * 
 * @author ruth 
 *
 */
public class General_Utils
{

   /**
    * Creates a new DetCal file if the detectors are rotated to the
    * specified angle in the scattering plane.
    * 
    * @param OrigDetCalFilename   The name of the original DetCal or new Peaks
    *                              file
    *                              
    * @param CenterBankID       The ID of the bank considered as the center
    * 
    * @param NewDetCalFilename   The name of the new DetCal file where the 
    *                          rotated detector information is stored.
    *                          
    * @param newCenterAngle    The angle in degrees where the new center will 
    *                          rotate to along a vertical axis.
    *                          
    *  @param xoffset         The distance in meters the sample is from the
    *                         center of rotation
    *                         
    *  @param beamOffset       The distance the sample is from the center of
    *                          rotation in the beam direction.
    * 
    * @return null for success or an ErrorString describing the problem
    * 
    * NOTE: The rotation is assumed to be around a vertical axis.
    */
   public static Object RotateDetectors( String OrigDetCalFilename, 
                                       int    CenterBankID,
                                       String NewDetCalFilename,
                                       float  newCenterAngle,
                                       float  xoffset,
                                       float  beamOffset)
   {
      newCenterAngle *= (float)(Math.PI/180);
      Scanner sc = null;
      try
      {
         sc = new Scanner(new java.io.File( OrigDetCalFilename) );
         
      }catch(Exception s3)
      {
         return new ErrorString("Cannot Read input file:"+s3);
      }
      
      Enumeration<UniformGrid> Grids = null;
      float[] L1_T0 = null;
      String[] headerInfo = null;
      FileOutputStream out = null;
      try
      {
         headerInfo = ReadFirstLine( sc );
         
         L1_T0 = Peak_new_IO.Read_L1_T0( sc );
         Grids = Peak_new_IO.Read_Grids( sc ).elements( );
         out = new FileOutputStream( NewDetCalFilename );
      }catch( Exception ss)
      {
         ss.printStackTrace();
         return new ErrorString( ss);
      }
      
      
      if( Grids == null )
         return new ErrorString("No Detectors Found ");
      
      UniformGrid grid = null;
      Vector<UniformGrid> VGrids = new Vector<UniformGrid>();
      for( ; Grids.hasMoreElements( ) ; )
      {
         UniformGrid gridt =Grids.nextElement( );
         VGrids.add( gridt );
         if( gridt.ID( ) == CenterBankID)
           grid = gridt;
      }
      
      if( grid == null )
        return new ErrorString(" Bank ID "+ CenterBankID + " is not found ");
      
      Grids =null;
      
      Vector3D Dir =  grid.position( ); 
      
      float thisCenterAngle = (float)Math.atan2( Dir.getY( ) , Dir.getX( ));
      
      float RotationAngle = newCenterAngle- thisCenterAngle;
      
      //Now get Transformation and apply it to all detectors
      Tran3D transformation = new Tran3D();
      transformation.setRotation( (float)(RotationAngle/Math.PI*180) , new Vector3D(0,0,1)  );
      Vector3D Xlate = new Vector3D( beamOffset, xoffset,0 );
      transformation.apply_to( new Vector3D(beamOffset, xoffset,0 ) , Xlate );
      System.out.println("Xlate rotated ="+ Xlate.toString( ));
      for( int i=0; i< VGrids.size( );i++)
      {
         UniformGrid G = VGrids.get(i);
         Vector3D xvec = new Vector3D();
         Vector3D yvec = new Vector3D();
         Vector3D center = new Vector3D();
         transformation.apply_to( G.position() , center );
         transformation.apply_to( G.x_vec( ) ,xvec );
         transformation.apply_to( G.y_vec( )  , yvec);
         center.add( Xlate );
         UniformGrid newG = new UniformGrid( G.ID( ),G.units( ),
              center, xvec , yvec , G.width( ), G.height( ), G.depth( ),
              G.num_rows( ), G.num_cols( ));
         VGrids.set( i , newG );
      }
      
      if( headerInfo == null)
      {
         headerInfo = new String[3];
         Arrays.fill( headerInfo , null );
      }
      try
      {
       //  if( headerInfo!= null && headerInfo.length >2)
       //    out.write( ("Version: "+headerInfo[0]+"  Facility:"+headerInfo[1]+
       //          "  Instrument:"+headerInfo[2]+"\n").getBytes());
      
         String CreateInfo ="created by rotation\n of the detector information" +
            " in the file "+OrigDetCalFilename.trim()+ " \n to "+
            (newCenterAngle*Math.PI/180)+" degrees";;
         Peak_new_IO.WriteHeaderInfo( out , CreateInfo ,
                headerInfo[0], headerInfo[1],headerInfo[2],L1_T0[0] , (int)L1_T0[1] );
       
         for ( int i =VGrids.size()-1;i>=0; i-- )
            out.write( (Peak_new_IO.GridString(VGrids.elementAt(i))+"\n").getBytes() );
          out.close();
      
      }catch(Exception ss)
      {
         JOptionPane.showMessageDialog( null , "Cannot Save file " +ss );
         ss.printStackTrace( );
         return new ErrorString("Cannot Save file: " +ss );
      }
      
      return null;
   }
   
   private static String[] ReadFirstLine( Scanner sc)
   {
      String[] Res = null;
      
      if( sc.findWithinHorizon( "#[0-9]" ,1 )!= null)
         return Res;
      String line = sc.nextLine( );
      
      if( line == null )
         return null;
      
      if( !line.toUpperCase().startsWith( "VERSION:" ))
         return null;
      line = line.substring( 9 ).trim( );
      int k= line.indexOf( ' ' );
      if( k <0) k= line.length( );
      
      Res = new String[3];
      
      Arrays.fill( Res , "" );
      Res[0] = line.substring( 0,k );
      line=line.substring( k ).trim( );
      
      if( line.length() <=0 )
         return Res;
      
      if(line.toUpperCase( ).startsWith( "FACILITY:" ))
      {
         line =line.substring( 10 ).trim( );
         k= line.indexOf( ' ' );
         if( k < 0) k= line.length( );
         Res[1] = line.substring( 0,k ).trim();
         if( line.length() > k)
            line = line.substring(k).trim();
         else
            line ="";
      }

      if( line.length() <=0 )
         return Res;
      
      if(line.toUpperCase( ).startsWith( "INSTRUMENT:" ))
      {
         line =line.substring( 12 ).trim( );
         k= line.indexOf( ' ' );
         if( k < 0) k= line.length( );
         Res[2] = line.substring( 0,k );
         if( line.length() > k)
            line = line.substring(k);
         else
            line ="";
      }
      return Res;
       
   }
   /**
    * Runs RotateDetectors in various modes.  
    *   -Command line arguments
    *   -JParametersDialog if there are no arguments
    *   - Shows help if there are fewer than 3 arguments and
    *      the first argument has the sequence of letters help
    *      
    * @param args
    */
   public static void main(String[] args)
   {
      String fileName1 = "C:/ISAW/InstrumentInfo/SNS/SNAP/SNAP.DetCal";
      String fileName2 ="C:/ISAW/InstrumentInfo/SNS/SNAP/SNAP1.DetCal";
      int CenterDetID = 14;
      float newAngle = 35.2f;
      float xoffset = .1f;
      float beamOffset =.2f;
      if( args.length >2)
      try
      {
         fileName1 = args[0];
         CenterDetID = Integer.parseInt(args[1].trim( ));
         fileName2 = args[2];
         newAngle = Float.parseFloat( args[3].trim() );
         if( args.length >4)
            xoffset = Float.parseFloat( args[4] .trim());
         if( args.length > 5)
            beamOffset= Float.parseFloat( args[5].trim() );
         args = new String[2];
         
      }catch(Exception s)
      {
         args = null;
      }
      if( args == null || args.length < 1)
      {
         JParametersDialog jp = new JParametersDialog( 
                          new RotateDetectors(), null, null, null);
      }else if( args.length < 3 || args[0].toUpperCase().indexOf("help")< 0)
      {
          System.out.println("Result="+
              General_Utils.RotateDetectors( fileName1 ,
            CenterDetID,fileName2,newAngle,xoffset,beamOffset) );
      }else if( args.length < 3 ||  args[0].toUpperCase().indexOf("help") >= 0)
      {
         
      }else
      {
         System.out.println(" This program takes one detector calibration"); 
         System.out.println("   rotates the system so the center of the");
         System.out.println("   detector is at the specified angle, then");
         System.out.println("   a new corresponding DetCal file. Arguments are");
         System.out.println(" 1) Original DetCal calibration file");
         System.out.println(" 2) The ID of the detector considered as "+
                             "the center");
         System.out.println(" 3) The name of the new DetCal filename");
         System.out.println(" 4) The new Angle( in degrees) the center "+
               "detector will go to ");
         System.out.println(" 5)(Optional) Sample x offset from beam(m)");
         System.out.println(" 6)(Optional) The sample beam offset(m)");
         
      }

   }

}
