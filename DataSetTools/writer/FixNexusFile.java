/**
 * 
 */
package DataSetTools.writer;

import javax.swing.JOptionPane;

import DataSetTools.dataset.DataSet;

import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;



/**
 * This class has utility methods to change and/or add minor information 
 * in a NeXus file
 * 
 * @author Ruth
 *
 */
public class FixNexusFile extends Writer {

   String data_destination_name;

   /**
    * @param data_destination_name
    */
   public FixNexusFile( String data_destination_name ) {

      super( data_destination_name );
      this.data_destination_name = data_destination_name;
     
   }


   /* Does nothing necause only changes part of the file.
    * (non-Javadoc)
    * @see DataSetTools.writer.Writer#writeDataSets(DataSetTools.dataset.DataSet[])
    */

   public void writeDataSets( DataSet[] ds ) {

   return;

   }

   /**
    * Adds or modifies the sample orientation part of a NeXus file.
    * 
    * @param data_destination_name  The name of the file
    * @param NXentryName            The name of the NXentry class 
    *                                   or null for default(entry)
    * @param NXsampleName           The name of the NXsample class 
    *                                   or null for default(sample)
    *                                   under the NXentry class
    * @param phi                 The phi  value for the sample orientation
    *                                       in degrees
    * @param chi                 The chi  value for the sample orientation
    *                                       in degrees
    * @param omega               The omega  value for the sample orientation
    *                                       in degrees
    *NOTE: Each facility interprets phi,chi, and omega differently. The chi
    *   phi and omega given should correspond to the facility specified in the
    *   Nexus File.   
    *   
    * NOTE: Does not work.
    */
   public static void AddSampleOrientation( String data_destination_name,
                                            String NXentryName,
                                            String NXsampleName,
                                            float phi, float chi, float omega){
      NexusFile nxf = null;
      try{
         nxf = new NexusFile( data_destination_name,
                                       org.nexusformat.NexusFile.NXACC_RDWR );
         if( NXentryName == null || NXentryName.length() < 1)
            NXentryName = "entry";
         if( NXsampleName == null || NXsampleName.length() < 1)
            NXsampleName ="sample";
         
         nxf.opengroup( NXentryName,"NXentry");
         nxf.opengroup( NXsampleName , "NXsample" );
         try{
            nxf.opendata( "sample_orientation" );
            
         }catch( NexusException NoSampOr){
            int[]dim = new int[1];
            dim[0] = 3;
            nxf.makedata( "sample_orientation" ,
                     NexusFile.NX_FLOAT32, 1, dim );
            nxf.opendata( "sample_orientation" );
         }
        
      }catch( Exception create){
         JOptionPane.showMessageDialog( null, create.toString() ,
                       "File Not Found", JOptionPane.WARNING_MESSAGE );
         
         return;
      }
      
      float[] samp_orient = new float[3];
      samp_orient[0]= phi;
      samp_orient[1]= chi;
      samp_orient[2]= omega;
     
      try{
         
         nxf.putdata( samp_orient );
         nxf.putattr( "units" , "degree".getBytes() ,NexusFile.NX_CHAR);
      }catch(Exception PutData){
         JOptionPane.showMessageDialog( null , PutData.toString(), 
             "Cannot record Data",JOptionPane.WARNING_MESSAGE  );
      }
      
     try{
        nxf.close();
     }catch(Exception s){
        System.out.println("Cannot close the file");
     }
     
          
   }
   /**
    * Will add/change small parts of a NeXus file
    * @param args
    *     args[0] Procedure name, SampOrient only on so far
    *     args[1] filename
    *     args[2] NXentry name
    *     args[3] NXsample name
    *     args[4] phi in degrees
    *     args[5] chi in degrees
    *     args[6] omrga in degrees
    *     
    * It assumes that phi, chi, and omega are according to the facility
    * determined by the rest of the NeXus file.
    */
   public static void main( String[] args ) {

     if( args.length <1){
        System.out.println("Usage for  FixNeXusFile");
        System.out.println("1st argument is prodedure- now only SampOrient");
        System.exit( 0 );
     }
     if( !args[0].toUpperCase().equals( "SAMPORIENT" )){
        System.out.println("No operation "+args[0]+" in this class");
        System.out.println(" Only the operation SampOrient is known so far");
        System.exit(0);
     }
     if( args.length < 7){
        System.out.println("Arguments for SampeOrient are");
        System.out.println("   1: filename");
        System.out.println("   2: NXentry name");
        System.out.println("   3: NXsample name");
        System.out.println("   4: phi in degrees");
        System.out.println("   5: chi in degrees");
        System.out.println("   6: omega in degrees");
        System.exit(0);
        
     }
     	
     String filename = args[1];
     String NXentryName = args[2];
     String NXsampleName = args[3];
     float phi = Float.parseFloat( args[4] );
     float chi = Float.parseFloat( args[5] );
     float omega = Float.parseFloat( args[6] );
     
     FixNexusFile.AddSampleOrientation( filename , NXentryName , NXsampleName , phi , chi , omega );

   }

}
