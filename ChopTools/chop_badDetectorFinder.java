/*
 * @(#)chop_evaluation.java  0.1 99/07/30  4:30 pm  Dongfeng Chen, Alok Chatterjee
 *  A evaluation filter taking monitor and detector dataset and outputing 
 *  a bad detector array.
 *
 */

package ChopTools;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.retriever.*;

public class chop_badDetectorFinder 
{
  public  static String runNumber;
  
  public static int[] badID;
  
  public  chop_badDetectorFinder(){}
    
  public static int[] maker(DataSet ds, DataSet data_set_M1, float uplevel, float lowlevel){
        
         float nomalizor = chop_calibraton.intergratedPeak1Intensity(data_set_M1);
         System.out.println("This is the intergrated peak intensity: "+nomalizor);
        
         
         System.out.println( "data_set_H1     = " + ds );
         System.out.println( "data_set_M1     = " + data_set_M1 );

         int num_data = ds.getNum_entries();
         Data data,
         new_data;
         int badnum=0; float[] valueSum = new float[num_data]; int[] badarray=new int[num_data];
         System.out.println("num_data is "+num_data);
           // try{Thread.sleep(1000);}catch(Exception e){}
         AttributeList    attr_list;
         
         for ( int i = 0; i < num_data; i++ )
         {
            data = ds.getData_entry( i );        // get reference to the data entry
            attr_list = data.getAttributeList();
            int detnum =      ((int [])(attr_list.getAttributeValue(Attribute.DETECTOR_IDS))).length;
            System.out.println("(Detector number in specific subgroup: )detnum = "+detnum );
            
           // try{Thread.sleep(200);}catch(Exception e){}
            
            
            valueSum [i]=sum100(data)*10000000/nomalizor/detnum;
            System.out.println("valueSum [i] "+ i+" is "+valueSum [i]+" number of detectors "+detnum);
            
           // try{Thread.sleep(400);}catch(Exception e){}
            
           // if(valueSum [i]>8||valueSum [i]<0.1)
            if(valueSum [i]>uplevel||valueSum [i]<lowlevel)
            {
                badnum++;
                badarray[badnum]=i;
            }
         }
         
         int [] bad= new int[badnum];
         for ( int i = 0; i < badnum; i++ )
         {
            bad[i]=badarray[i];
         }
            System.out.println(" Number of bad  detector "+badnum);
         
            //try{Thread.sleep(300);}catch(Exception e){}
         
         return  bad;
         
         /* // the following array is come from all of the bad detector from
            // hrcs2712.run which is used for debug
         int[] d={
            45,73,74,87,103,198,209,236,254,305,
            45,74,103,176,198,209,254,305, 
            17 ,18,19,20,21,22,23,24,26,28,30,32,34,36,39,47,54,57,59,
            62,64,75,82,90,93,94,98,124,128,138,145,156,186,187,212,214,
            224 , 225,228,232,237,239,244,251,257,258,259,278,281,288,289,290,291,293,306, 308,312,319,325,334,342 
         };
         
         return d;
         //*/
         }
         
 public static float sum100(Data data){
            
         float[] y=data.getCopyOfY_values();
         XScale  x=data.getX_scale();
         float sum=0;
         for (int j = y.length-1; j>=(y.length-100);j--){
                sum+=y[j];                
         }
            
         float sumvalue=sum/100;
            
         return sumvalue;  
            
   }
         
 //* 
 //The main is used for test , and it make a file named as .\\ChopTools\\e_maker*.opt
  public static void main(String [] args)
  {
      RunfileRetriever data_retriever = null;
      DataSet   data_set_H1 = null;
      DataSet   data_set_M1 = null;
      float     uplevel     = 200;
      float     lowlevel     = 100;
      
      String runname = 
                        ".\\ChopTools\\HRCS0976.RUN;20";  
                         //  ".\\ChopTools\\HRCS2447.RUN;24";  
                         // ".\\ChopTools\\HRCS2712.RUN;1";  
         
      try
      {
         data_retriever = new RunfileRetriever(runname);
         data_set_H1 = data_retriever.getDataSet(1); 
         data_set_M1 = data_retriever.getDataSet(0); 
         
         System.out.println( "data_set_H1     = " + data_set_H1 );
         System.out.println( "data_set_M1     = " + data_set_M1 );
        
         badID = maker(data_set_H1, data_set_M1, uplevel, lowlevel );
         AttributeList    attr_list;
         AttributeList    attr_listDataSet;
         String s=null;
        
         attr_listDataSet = data_set_H1.getAttributeList();
         for(int i=0; i<attr_listDataSet.getNum_attributes(); i++)
         {
            
                 String attr_nameDataSet = (attr_listDataSet.getAttribute(i)).getName();
                 if(attr_nameDataSet == "Run Number")
                 {
                     runNumber="h"+(attr_listDataSet.getAttribute(i)).getValue();
                     System.out.println("Got it ! Run number is"+runNumber); 
                 }
                 String a=attr_nameDataSet+"\t "+(attr_listDataSet.getAttribute(i)).getValue();
                 System.out.println(a+"\n"); 
          }

               // Thread.sleep(8000);
                
          File f= new File(".\\ChopTools\\e_maker"+runNumber+".opt");
          FileOutputStream op= new FileOutputStream(f);
          OutputStreamWriter opw = new OutputStreamWriter(op);

          for(int i=0; i<attr_listDataSet.getNum_attributes(); i++)
          {
                String attr_nameDataSet = (attr_listDataSet.getAttribute(i)).getName();
                String a=attr_nameDataSet+"\t "+(attr_listDataSet.getAttribute(i)).getValue();
                opw.write(a+"\n"); 
          }
                 
          for (int j=badID.length-1; j>=0; j--)
          {
                    opw.write(j+ " " +badID[j]+"\n ");
          }
                 
          s=badID.length+" Bad detectors: ";
          opw.write(s); System.out.println(s);

          opw.flush();
          opw.close();             
           
          System.out.println("This is the intergrated peak intensity: "+chop_calibraton.intergratedPeak1Intensity(data_set_M1));
          Thread.sleep(4000);
         
      }catch(Exception e){}
       
  }
 
         
         
  
}
