package NexIO;
import  neutron.nexus.*;
import ncsa.hdf.hdflib.HDFArray;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFConstants;



public class CNexusFile extends NexusFile
 {

    /*public static native int[] byteToInt( byte[] data );
    public static native float[] byteToFloat( byte[] data );
    public static native short[] byteToShort( byte[] data );
    public static native long[] byteToLong( byte[] data );
    public static native double[] byteToDouble( byte[] data );
   */

   public CNexusFile( String filename, int access) throws NexusException
     {super( filename,access);
     }
   
   public Object getData( int NxType, int length) throws NexusException
     {byte bdata[];
        if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
        
        try{int L=-1;
           if( NxType == NexusFile.NX_FLOAT32) L=4;
           else if( NxType == NexusFile.NX_CHAR) L=1;
           else if( NxType == NexusFile.NX_FLOAT64)L=8;
 
           else if(NxType == NexusFile.NX_INT16)L=4;
           else if( NxType == NexusFile.NX_INT32)L=8;
           else if( NxType == NexusFile.NX_INT8)L=2;
           else if( NxType == NexusFile.NX_UINT16)L=4;
           else if(NxType ==  NexusFile.NX_UINT32)L=8;
           else if(NxType == NexusFile.NX_UINT8)L=2;
          
           else throw new NexusException("NAPI-ERROR: Improper Data Type");
           bdata= new byte[L*length];
	    //HDFArray ha = new HDFArray(array);
            //bdata = ha.emptyBytes();
          
               nxgetdata(handle,bdata);
           
            //array = ha.arrayify(bdata);
           
          if( NxType == NexusFile.NX_FLOAT32) 
              return ncsa.hdf.hdflib.HDFNativeData.byteToFloat( bdata);
             
           else if( NxType == NexusFile.NX_FLOAT64)
                 return ncsa.hdf.hdflib.HDFNativeData.byteToDouble( bdata);
           else if((NxType == NexusFile.NX_INT16)||(NxType == NexusFile.NX_UINT16))
                return ncsa.hdf.hdflib.HDFNativeData.byteToShort( bdata);
           else if( (NxType == NexusFile.NX_INT32) || (NxType == NexusFile.NX_UINT32) )
              return ncsa.hdf.hdflib.HDFNativeData.byteToInt( bdata );
           else if( (NxType == NexusFile.NX_INT8) ||(NxType == NexusFile.NX_UINT8) )
               return bdata;
         
           
       if( (NxType ==NexusFile.NX_INT32) ||(NxType ==NexusFile.NX_UINT32)  )
          return ncsa.hdf.hdflib.HDFNativeData.byteToInt( bdata );
        if( NxType == NexusFile.NX_CHAR)
            {char[] cdata = new char[bdata.length];
             for( int i=0; i< bdata.length; i++)
               cdata[i]=(char)bdata[i];
             return cdata;
             }
    
         
	 }catch(Exception he) {
           throw new NexusException(he.getMessage());
	 }

       return null;
      }
  }
