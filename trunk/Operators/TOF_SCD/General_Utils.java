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

import DataSetTools.dataset.UniformGrid;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;
import gov.anl.ipns.MathTools.Geometry.Tran3D;
import Operators.Special.*;

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
    * @return null for success or an ErrorString describing the problem
    * 
    * NOTE: The rotation is assumed to be around a vertical axis.
    */
   public static Object RotateDetectors( String OrigDetCalFilename, 
                                       int    CenterBankID,
                                       String NewDetCalFilename,
                                       float  newCenterAngle)
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
      
      for( int i=0; i< VGrids.size( );i++)
      {
         UniformGrid G = VGrids.get(i);
         Vector3D xvec = new Vector3D();
         Vector3D yvec = new Vector3D();
         Vector3D center = new Vector3D();
         transformation.apply_to( G.position() , center );
         transformation.apply_to( G.x_vec( ) ,xvec );
         transformation.apply_to( G.y_vec( )  , yvec);
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
            " in the file "+OrigDetCalFilename.trim()+ " \n to "+newCenterAngle+" degrees";;
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
    * @param args
    */
   public static void main(String[] args)
   {
      System.out.println("Result="+
      General_Utils.RotateDetectors( "C:/ISAW/InstrumentInfo/SNS/SNAP.DetCal" ,
      		14,"C:/ISAW/InstrumentInfo/SNS/SNAP1.DetCal",35.2f));

   }

}
