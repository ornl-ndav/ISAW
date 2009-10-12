/**
 * 
 */
package DataSetTools.writer;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import DataSetTools.dataset.DataSet;
import NexIO.*;
import NexIO.Util.ConvertDataTypes;

import org.nexusformat.AttributeEntry;
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
   
   public static int ROW = 2; //Use these with dims[dims.length- 1-axes[ROW]]
   public static int COL = 1;
   public static int TIME =0;

   String data_destination_name;

   // Tests for bounds, index=ROW,COL,or TIME. Fix if missing some
   //  of these. Assume TIME always last position
   //returns index in dims or -1
   private static int dimIdx( int dimlength,int[] axes, int index){
      
      if( axes == null || axes.length< 1|| index < 0|| dimlength <1)
         return -1;
      if( index == TIME)
         return axes[TIME];
      if( index == COL){
           if(COL < axes.length && axes[COL]< dimlength)
              return axes[COL];
           else
              return -1;
      }
      if( index == ROW)
         if(ROW < axes.length ){
            if( axes[ROW] < dimlength)
               return axes[ROW];
            else
               return -1;
         } else if( COL < axes.length && axes[COL]<dimlength)
            return axes[COL];
         else 
            return -1;
      if( index < 0 || index >= dimlength)
         return -1;
      return index;
                           
      
       
   }
   
   public String getErrorMessage()
   {
      return "";
   }
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
    * Will sumbsample and rewrite NeXus file. A little long
    * 
    * @param data_destination_name
    *           The name of the NeXus file
    * 
    * @param NXentryName
    *           The name of the NXentry to subsample
    * 
    * @param NXinstrumentName
    *           The Name of the NXinstrument in this NXenty
    * 
    * @param NXdataName
    *           The NXdata name(null or "" for all)
    * 
    * @param isMonitor
    *           true if the NXdata is(are) really an NXmonitor
    * 
    * @param rowGrouping
    *           Number of rows to be combined( -1 for 1). Must evenly divide the
    *           total number of rows
    * 
    * @param colGrouping
    *           Number of cols to be combined( -1 for 1). Must evenly divide the
    *           total number of cols
    * 
    * @param timeGrouping
    *           Number of time channels to be combined( -1 to use rebin info
    *           below). Must evenly divide the total number of timeChannels
    * 
    * @param minTime
    *           if timeGrouping is -1, the min time to use for rebinning or -1
    *           if no rebinning.
    * 
    * @param maxTime
    *           if minTime >=0, the max time to use for rebinning or -1 if no
    *           rebinning. For logarithmic rebinning the last time <= maxTime
    * 
    * @param nTimes
    *           if minTime >=0, the number of time channels to use for rebinning
    *           or -1 if no rebinning.
    * 
    * @param isLog
    *           True if uniform log time binning otherwise a linear rebinning
    *           will be used.
    */
   public static void subSample( String data_destination_name, String NXentryName,
            String NXinstrumentName, String NXdataName, boolean isMonitor,
            int rowGrouping, int colGrouping, int timeGrouping, float minTime,
            float maxTime, int nTimes, boolean isLog){
      
  
      CNexusFile nxf = null;
      String NamesNXdata="";
      String NameNXinstrument="";
      String className ="NXdata";
      if( isMonitor )
         className = "NXmonitor";
      if( isMonitor && minTime < 0 && timeGrouping < 0 )
         return;//nothing to do
      String version="2.0.0";
      try{
         nxf = new CNexusFile( data_destination_name,
                                       org.nexusformat.NexusFile.NXACC_RDWR );
         if( NXentryName == null || NXentryName.length() < 1)
            NXentryName = "entry";
         
         nxf.opengroup( NXentryName,"NXentry");
         if( NXdataName != null)
            NamesNXdata = NXdataName+";";
         //--- getVersion information
         nxf.opendata( "definition" );
            Hashtable atts = nxf.attrdir();
            AttributeEntry v = (AttributeEntry)atts.get( "version" );
            if(v != null){
               int[] args = new int[Math.max( 2 , v.length )];
               args[0]=v.length; args[1]=v.type;
               Object SS = NexIO.Types.CreateArray(Externalize(v.type) , v.length );
               nxf.getattr( "version" ,SS , args );
               version = NexIO.Util.ConvertDataTypes.StringValue( SS );
            }
         nxf.closedata();
         //---Get names of NXdata, NXmonitor and NXinstrument -------
         Hashtable groups = nxf.groupdir();
         for( Enumeration keylist = groups.keys(); keylist.hasMoreElements();){
            String key = (String)keylist.nextElement();
            String Class = (String)groups.get( key );
            if( Class.equals("NXinstrument"))
               NameNXinstrument= key;
            else if( NXdataName == null && Class.equals("SDS")){
               if( isMonitor && Class.equals( "NXmonitor" ))
                  NamesNXdata +=key+";";
               else if( Class.equals("NXdata"));
                 NamesNXdata +=key+";";
            }
         }
         
      }catch(Exception s){
         JOptionPane.showMessageDialog( null , s.toString() );
         s.printStackTrace();
         return;
      }
     try{   
     for( int i=0; ( i >=0 )|| (i < NamesNXdata.length()); ){
        int j= NamesNXdata.indexOf(';',i);
        if( j >0){
           String name = NamesNXdata.substring(i,j);
           nxf.opengroup( name , className );  
           if( !isMonitor && (rowGrouping >= 1 || colGrouping >= 1 ) )
               ReGroupDetector( nxf, rowGrouping , colGrouping, 
                        Math.max(timeGrouping,1),version );
          
           if( minTime >=0 && timeGrouping <= 0)
              ReBinTime( nxf, minTime,maxTime,nTimes, isLog, version );
                 
           nxf.closegroup();
        }
        i=j+1;
       
     }
    
     }catch(Exception ss){
        System.out.println("Did Not change file(incorrect state) "+ss.toString());
        ss.printStackTrace();
       
     }
    try{
       nxf.close();
    }catch( Exception s3){
       System.out.println(" Could not close file "+data_destination_name);
       s3.printStackTrace();
    }
   }
   // nxf is at the NXdata level. Will regroup the data, errors and
   //  the axes
   private static void ReGroupDetector( CNexusFile nxf, int rowGrouping, 
                    int colGrouping, int nTimeGrouping,String version){
      
      if( rowGrouping ==1 && colGrouping ==1 && nTimeGrouping ==1)
         return;
      try {
         String axes=null;
         nxf.opendata( "data"  );
         Hashtable tb = nxf.attrdir();
         if( tb.get( "axes" )!= null){
             AttributeEntry attent = (AttributeEntry)tb.get("axes");
             if( attent.type == NexusFile.NX_CHAR){
                byte[] SS = new byte[ attent.length];
                int[] inf = new int[2];
                inf[0] = attent.length; inf[1]= attent.type;
                nxf.getattr( "axes" , SS , inf );
                axes = new String( SS);
             }
         }
         nxf.closedata();
         regroupdata( nxf , rowGrouping , colGrouping,nTimeGrouping,
                             version,"data" );
         

         
         regroupdata( nxf , rowGrouping , colGrouping , nTimeGrouping, 
                                               version,"errors");
        
         Hashtable htbl = nxf.groupdir();
         for( Enumeration keys = htbl.elements() ; keys.hasMoreElements() ; ) {
            String key = (String) keys.nextElement();
            if( key.equals( "data" ) ) {
            }
            else if( key.equals( "errors" ) ) {

            }
            else {
               String Class = (String) htbl.get( key );
               nxf.opendata( key );
                  try{
                     int axis = getAxisNum( nxf, key,axes,version);
                     nxf.closedata();
                     if( axis == ROW){
                        regroupdata( nxf , rowGrouping , 1,1,
                                 version,key );
                     }else if (axis == COL){
                        regroupdata( nxf , 1 , colGrouping,1,
                                 version,key);
                     }else if( axis == TIME){
                        regroupdata( nxf , 1 , 1,nTimeGrouping,
                                 version,key );
                     }
                     
                  }catch(Exception sss){
                     System.out.println("Cannot fix "+key+"::"+ sss.toString());
                     sss.printStackTrace();
                  }
            }


         }
      }
      catch( Exception s ) {
         System.out.println( "Error in regrouping detectors:" + s.toString() );
         s.printStackTrace();
      }
      
   }
   // returns negative the number  ROW,COL, or TIME 
   //nxf is at top of NXdata or NXmonitor
   private static int getAxisNum( CNexusFile nxf, String classname,String axes, String version){
      try{
         
        Hashtable Attrs = nxf.attrdir();
        int[] iDim = new int[6];
        int[] args = new int[6];
        nxf.getinfo( iDim , args );
        AttributeEntry AttInf = (AttributeEntry)Attrs.get( "axis" );
        int axis = -1;
        if( AttInf != null){
           if( AttInf.length ==1 && isInt(AttInf.type )){
              int[] ax = new int[1];
              int[] inf = new int[2];
              inf[0]= 1; inf[1]= AttInf.type;
              nxf.getattr( "axis" , ax , inf );
              axis = ax[0]-1;
              //Fix for version
              if( version != null && version.compareTo( "2" )>=0 ) {
                 axis = args[0]-(axis+1) ;
                 if( axis==ROW)
                    axis=COL;
                 else if( axis == COL)
                    axis = ROW;
                 
              }
              return axis;
           }
        }
       if( axis < 0 && axes== null || axes.length() < 2)
          return -1;
       if( axis < 0){
          int axis1 = 0;
          if( axes.startsWith( "[" ))
             axes = axes.substring( 1 );
          if( axes.endsWith("]"))
             axes = axes.substring(0, axes.length()-1);
          int i=0, j;
          for(  j=1; j<= axes.length() && j>=0 ; 
                       j= Math.max( axes.indexOf(';',j+1) , axes.indexOf(':',j+1) )){
             if( classname.equals( axes.substring( i,j )))
                axis=axis1;
             axis1++;
             i = j+1;
          }
          if( i < axes.length())
             if( classname.equals( axes.substring( i) ))
                axis = axis1;
          if( axis >=0)
             axis = axis1-axis;
          if( version != null && version.compareTo( "2" )>=0 )
             if( axis == ROW)
                axis = COL;
             else if( axis == COL)
                axis = ROW;
          
       }
       return axis;
      }catch(Exception s){
         s.printStackTrace();
         return -1;
      }

      
   }
   private static boolean isInt( int type){
      if( type == NexusFile.NX_INT16)
         return true;
      if( type == NexusFile.NX_INT8)
         return true;
       if( type == NexusFile.NX_INT32)
          return true;
       if( type == NexusFile.NX_UINT32)
          return true;

       if( type == NexusFile.NX_UINT16)
          return true;
       if( type == NexusFile.NX_UINT8)
          return true;
       return false;
   }
   private static void ReGroupTime( CNexusFile nxf, int timeGrouping, String version){
      
   }
   
   
   private static void ReBinTime( CNexusFile nxf, float minTime, float maxTime,
               int nTimes, boolean isLog, String version){
      
   }
   
   //nxf is at NXdata or NXmonitor level, will regoup data, errors ,times and all
   // entries with axis attribute
   
   
 // nxf should be opened in a NXdata object
   private static void regroupdata(CNexusFile  nxf ,int rowGrouping , 
                     int colGrouping, int nTimeGrouping, String version, String fieldName ){
      //[1] is  coldim,[2] is row dim, [0] is time dim in dimension array
  
      int[] axes = null;
      int[] dims;
      Hashtable tab = null;
      Hashtable values = null;
      boolean rowColFlip= false;
      int type, length;
      try{
         nxf.opendata( fieldName  );
         int[] iDim = new int[8];
         int[] Args = new int[8];
         nxf.getinfo( iDim , Args );
         dims= new int[ Args[0]]; 
         axes= new int[Args[0]];
         if( Args[0] >1 && Args[0] < 3 ){//not axis or area grid
            //Not an area grid
            nxf.closedata();
            return;
         }
            
         type = Args[1];
         System.arraycopy( iDim,0,dims,0,dims.length);

         tab = nxf.attrdir();
         values = getValues( tab, nxf);
      }catch(Exception ss){
         System.out.println("No data field in this NXdata ");
         ss.printStackTrace();
         return;
      }
        
      int i;
      
      if( version == null || version.compareTo( "2" ) < 0){
         axes[TIME] = dims.length -1;
         axes[COL]=  axes[TIME];
         if( dims.length-2 >=0)
            axes[COL] = dims.length-2;
         axes[ROW] =axes[COL];
         if( dims.length-3 >=0)
         axes[ROW] = dims.length-3;
              
         
              
      }else{

         axes[TIME] = dims.length -1;
         axes[ROW] =axes[TIME];
         if(dims.length-2 >=0)
            axes[ROW] = dims.length-2;
         if(dims.length-3 >=0)
             axes[COL]= dims.length-3;
         else
            axes[COL]= axes[ROW];
      }
      //TODO fix for non area grids //assume that only area grid
      int NROWS=1, NCOLS=1, NTIMES = 1;
      NTIMES = dims[axes[TIME]];
      NROWS = dims[axes[ROW]];
      NCOLS = dims[axes[COL]];
      try{
      if( rowGrouping*(NROWS/rowGrouping) != NROWS){
         System.out.println("Groups of rows does not evenly divide the number of rows");
         nxf.closedata();
         return;
      }   
      
      if( colGrouping*(NCOLS/colGrouping) != NCOLS){
         System.out.println("Groups of columns does not evenly divide the"+
         	    " number of columnss");
         nxf.closedata();
         return;
      }// is histogram
      
      if( NTIMES - nTimeGrouping*(NTIMES/nTimeGrouping) > 1){
         System.out.println("Groups of time channels do not evenly divide the"+
                " number of time channels");
         nxf.closedata();
         return;
      }
      }catch(Exception ss){
        //already closed the data
     }
      
      length = 1;
      for( i=0; i< dims.length;i++)
         length *=dims[i];
      
     float[] Data = null;
     try{
        
        Data =getData(nxf,type , length );
        nxf.closedata(); 
     }catch(Exception s){
        System.out.println( "Cannot get or close in data "+s.toString());
        s.printStackTrace();
        return;
     }
     int nrows=1, ncols=1, ntimes =1;
     
     if( ROW < dims.length)nrows =NROWS/rowGrouping;
     if(COL < dims.length) ncols =NCOLS/colGrouping;
     if( TIME < dims.length)ntimes = NTIMES/nTimeGrouping;
     float[] Data1 = new float[ nrows*ncols*ntimes];
     
     int rowMult = axisMult(dims,axes,ROW),
        colMult=axisMult(dims,axes,COL),
        timeMult=axisMult(dims,axes,TIME);// row +rowMult start of next row
                                          // in DATA the bigger one
     
     float Sum = 0;
     int T1=0,R1=0,C1=0;
     
      
     for(  R1=0; R1< nrows; R1++)
     for( C1=0; C1< ncols; C1++)
     for( T1=0; T1 < ntimes; T1++){
       int rr1,cc1,tt1;
       int topLeft = R1*rowGrouping*rowMult+ C1*colGrouping*colMult+
           T1*nTimeGrouping*timeMult;
       int n=0;
       for( int r1 =0; r1< rowGrouping;r1++){
          int RR = topLeft+r1*rowMult;
          for( int c1=0; c1< colGrouping; c1++){
             int CC = RR +c1*colMult;
             for( int t1=0; t1<nTimeGrouping; t1++){
                Sum += Data[CC+t1];//assumes timeMult = 1;
                n++;
             }//time in grouping
            
          }//col in groupint
          
       }// row in grouping
       Data1[i]= Sum/Math.max( 1 , n);
       Sum = 0;
       n=0;
     }//R1
   
   try{
    
     int[] dim1= new int[dims.length];
     java.util.Arrays.fill(  dim1 , 1 );
     for( i=0; i< dims.length; i++)
        if(ROW < axes.length && i==axes[ROW])
           dim1[i]=dims[i]/rowGrouping;
        else if( COL < axes.length &&i== axes[COL])
           dim1[i]= dims[i]/colGrouping;
        else if( TIME < axes.length && i== axes[TIME])
           dim1[i]= dims[i]/nTimeGrouping;
        else
           dim1[i]= dims[i];
     nxf.makedata( fieldName , type , dims.length , dim1 );
     nxf.opendata( fieldName );
     
     nxf.putdata( Data1 );
     for( Enumeration x= tab.keys(); x.hasMoreElements();){
        String key = (String)x.nextElement();
        AttributeEntry value = (AttributeEntry)tab.get( key );
        Object array = values.get( key );
        if( array != null)
           try{
               nxf.putattr( key , array , value.type );
           }catch(Exception sss){
              System.out.println("cannot save attribute "+ key);
              sss.printStackTrace();
           }
     }
     nxf.closedata();
   }catch( Exception ss){
      System.out.println("error makeing "+fieldName +"."+ss.toString());
      ss.printStackTrace();
   }
    
      
   }
   
   
   //index is ROW,COL or TIME im stamdard form. Will go thru Xlate ere used in
   // dims
   private static int axisMult( int[] dims, int[] Xlate, int index){
      if( index >= dims.length || dims == null || index < 0)
         return 0;
      int Mult=1;
      for( int i= Xlate[index];i < dims.length; i++)
         Mult *= dims[Xlate[i]];
      return Mult;
   }
   
   
   private static Hashtable getValues( Hashtable tab , CNexusFile nxf){
      Hashtable Res = new Hashtable();
      for( Enumeration x= tab.keys(); x.hasMoreElements();){
         String key = (String)x.nextElement();
         AttributeEntry inf =(AttributeEntry)tab.get( key );
         int[] dims = new int[Math.max( 2 , inf.length)];
         dims[0]= inf.length;
         dims[1] = inf.type;
         Object array = NexIO.Types.CreateArray( Externalize( inf.type)
                           , inf.length );
         try{
            nxf.getattr( key , array , dims );
            Res.put( key , array );
         }catch(Exception s){
            System.out.println("cannot save attr "+key);
            s.printStackTrace();
         }
         
      }
      return Res;
   }
   
   
   private static float[] getData(CNexusFile nxf,int Nextype , int length ){
      try{
     
      
      Object array = Types.CreateArray( Externalize( Nextype) , length );
      nxf.getdata(  array );
      return ConvertDataTypes.floatArrayValue( array );
      }catch(Exception ss){
         return null;
      }
      
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
   
   private static int Externalize( int NexusType){
      return NexIO.NXutil.ConvertNxDataType2IsawDataType( NexusType );
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
