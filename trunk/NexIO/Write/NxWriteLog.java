package NexIO.Write;
import DataSetTools.dataset.*;
import NexIO.*;
import java.util.*;
import java.text.*;


public class NxWriteLog{ 
  String errormessage;

  public NxWriteLog(int instrType){
    errormessage = "";
  }

  /**
   * Returns an error message or "" if there is no error
   */
  public String getErrorMessage(){
    return errormessage;
  }

  /**
   * Extracts the information from the Data Set and writes it to the
   * NxBeam portion of a Nexus file
   *
   * @param  NxLognode    A node whose class is NXlog
   * @param  DS      The data set whose information is to be written
   * @return true if an error occurred otherwise false
   */
  public boolean processDS( NxWriteNode NxLognode, DataSet DS, int k ){
    errormessage ="";
    byte[][] time = new byte[20][50];
    DateFormat df = DateFormat.getDateTimeInstance( DateFormat.LONG,
                                         DateFormat.LONG);

    float[]  temperature = new float[20],
        electric_field = new float[20];
    long t = (new Date()).getTime();
    for( int i=0; i<20; i++){
       String S=df.format ((new Date( t+100*k+ 5*i)));
       S = S+(char)0;
       byte[] b = S.getBytes();
       System.arraycopy( b,0,time[i],0,b.length);
       temperature[i]=100+k+i;
       electric_field[i] = .01f+ .3f*k +.5f*i;
     }

    NxWriteNode timeNode = NxLognode.newChildNode( "time","SDS");
    timeNode.setNodeValue(time, Types.Char, NexIO.Inst_Type.makeRankArray(
                   20,50,-1,-1,-1));
    NxWriteNode tempNode = NxLognode.newChildNode( "temperature","SDS");
    tempNode.setNodeValue( temperature, Types.Float, NexIO.Inst_Type.makeRankArray(20,
           -1,-1,-1,-1));
    tempNode.addAttribute( "units",("Fahrenheit"+(char)0).getBytes(), Types.Char,
         NexIO.Inst_Type.makeRankArray( 7,-1,-1,-1,-1));
    NxWriteNode elecNode = NxLognode.newChildNode( "electric_field","SDS");
    elecNode.setNodeValue( electric_field, Types.Float, NexIO.Inst_Type.makeRankArray(20,
           -1,-1,-1,-1));
    return false;

  }

  private boolean setErrorMessage( String err){
     errormessage = err;
     return true;
  }



}
