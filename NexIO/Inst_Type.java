package NexIO;
import java.util.*;
import DataSetTools.instruments.*;
import java.io.*;
import java.util.*;
public class Inst_Type
{static Hashtable HT =null;
 static String  NxNames[] = {"MonoNXPD","TOFNDGS","TOFNIGS"};//MonoNXTAS
 static int  Isaw_inst_types[] ={InstrumentType.MONO_CHROM_DIFFRACTOMETER,   
                                // InstrumentType.UNKNOWN,
                                 InstrumentType.TOF_DG_SPECTROMETER ,
                                 InstrumentType.TOF_IDG_SPECTROMETER };



 public Inst_Type()
   { if( HT == null)
      {HT = new Hashtable();
       HT.put( "MonoNXPD", new Integer(0)); 
        HT.put( "MonoNXTAS", new Integer(1));  
        HT.put( "TOFNDGS", new Integer(2));  
         HT.put( "TOFNIGS", new Integer(3));  
      }
   }

 public int getIsawInstrNum( String NexusAnalysisName)
   {Integer I = (Integer)(HT.get( NexusAnalysisName));
   if( I == null)
     return InstrumentType.UNKNOWN;
   int i = I.intValue();
   if( i<0) return InstrumentType.UNKNOWN;
   if( i >= Isaw_inst_types.length)
       return InstrumentType.UNKNOWN;
   return Isaw_inst_types[i];
   }

public String getNexAnalysisName( int IsawInstrNum)
  {for( int i=0; i<Isaw_inst_types.length; i++)
     if( Isaw_inst_types[i] == IsawInstrNum)
        return NxNames[i];
   return null;
  }

/// more to get NxEntry Handlers, NxData Handlers, etc...


public static void main( String args[])
  { Inst_Type  X = new Inst_Type();
    char option=0;
    while( option !='x')
    { System.out.println( "Enter option");
      System.out.println(" a) Enter number");
      System.out.println(" b) Enter String");
      option = 0;
      try{
        while( option <32)
          option = (char)System.in.read();
         }
      catch(IOException s){option =0;}
     String S = "";
     char c=0;
     try{
        while( c<33)
          c =(char)System.in.read();
        while( c >=32)
          {S = S+c;
           c =(char)System.in.read();
           }
        }
     catch(IOException s){}
    if( option =='a')
     {int num=-1;
      try{
         num = (new Integer(S)).intValue();
         System.out.println("Result="+X.getNexAnalysisName( num));
         }
      catch( Exception s)
         {System.out.println("Error="+s);}
        
     }
    else if( option == 'b')
     {System.out.println("Res="+X.getIsawInstrNum( S));
     }

    }//while

  }

}
