/*
 * @(#)chop_evaluation.java  1.01 99/07/30  Dongfeng Chen, Alok Chatterjee
 *  This is a evaluation tools for modifying dataset by input bad detector array.
 */

package ChopTools;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.retriever.*;
import  DataSetTools.operator.*;

public class chop_evaluation 
{
  public static String  s; 
  
  public  static String runNumber; 
  
  public static int[] badID;
  
  public chop_evaluation(){}
  
  public static DataSet evaluator(DataSet data_set_H1, DataSet data_set_M1, float uplevel, float lowlevel){
      
        badID = chop_badDetectorFinder.maker(data_set_H1, data_set_M1, uplevel, lowlevel );

        AttributeList    attr_list;
        
        AttributeList    attr_listDataSet;
        
        Data data;
        
        int  num_data = data_set_H1.getNum_entries();
        
        DataSet new_ds = (DataSet)data_set_H1.clone(); 
      
        new_ds.addLog_entry( "Evaluate "+data_set_H1 +" data by " + data_set_M1  );
                 
        for (int j=badID.length-1; j>=0; j--)
        {
            new_ds.removeData_entry(badID[j]);
        }
                 
        System.out.println("The number of had detector is " + badID.length);

        System.out.println("This is the intergrated peak intensity: "+chop_calibraton.intergratedPeak1Intensity(data_set_M1));
        
        return new_ds;
       }
  
  
  
  /** The following  parts are used for debug and test! They will
  *   make a file whick can get information from attributes of new dataset
  *   and original one. 
  *
  */
 
    public static String checker(DataSet data_set_H1){
      
        Data             data;
        int              num_data = data_set_H1.getNum_entries();

        AttributeList    attr_list;
        AttributeList    attr_listDataSet;
        
        attr_listDataSet = data_set_H1.getAttributeList();
        String s=null;
        
        try{
            
          attr_listDataSet = data_set_H1.getAttributeList();
          
          for(int i=0; i<attr_listDataSet.getNum_attributes(); i++){
            
          String attr_nameDataSet = (attr_listDataSet.getAttribute(i)).getName();

          if(attr_nameDataSet == "Run Number")
          {
               runNumber="h"+(attr_listDataSet.getAttribute(i)).getValue();
               System.out.println("Got it ! Run number is"+runNumber); 
           }
          String a=attr_nameDataSet+"\t "+(attr_listDataSet.getAttribute(i)).getValue();
          System.out.println(a+"\n"); 
          }

          System.out.println(" "+  runNumber);
                
          File f= new File(".\\ChopTools\\e_checker"+runNumber+".opt");
          FileOutputStream op= new FileOutputStream(f);
          OutputStreamWriter opw = new OutputStreamWriter(op);
                
          for(int i=0; i<attr_listDataSet.getNum_attributes(); i++)
          {            
                String attr_nameDataSet = (attr_listDataSet.getAttribute(i)).getName();
                String a=attr_nameDataSet+"\t "+(attr_listDataSet.getAttribute(i)).getValue();
                opw.write(a+"\n"); 
           }
                 
           s="num_data is  "+num_data+"\n";
           opw.write(s);   System.out.println(s);
                 
           for ( int j = 0; j < num_data; j++ )
           {
                  data = data_set_H1.getData_entry( j );        // get reference to the data entry
                  attr_list = data.getAttributeList();
                  s=j+" DETECTOR_IDS "+((int [])(attr_list.getAttributeValue(Attribute.DETECTOR_IDS)))[0]+
                    "    GROUP_ID  "+attr_list.getAttributeValue(Attribute.GROUP_ID)+"\n";
                  opw.write(s); System.out.println(s);
           }

           opw.flush();
           opw.close();
             
         } catch ( Exception e ){}
        
        
        return s;
       }
 
       
 // For debug and test
 
 public static void main(String [] args)
  {
         RunfileRetriever data_retriever = null;
         DataSet   data_set_H1 = null;
         DataSet   data_set_M1 = null;
         float     uplevel     = 2000;
         float     lowlevel     = 1000;
         
         String runname = 
                         //".\\ChopTools\\HRCS0976.RUN;20";  
                         ".\\ChopTools\\HRCS2712.RUN;1";  
         try
         {
         data_retriever = new RunfileRetriever(runname);
         data_set_H1 = data_retriever.getDataSet(1); 
         data_set_M1 = data_retriever.getDataSet(0); 
         
         DataSet eval= evaluator(data_set_H1, data_set_M1 ,  uplevel, lowlevel);
         
         String e=eval.toString();
         String f=checker(eval);
         System.out.print("The DATA : "+ e);
        
         Thread.sleep(4000);
         
         }catch(Exception e){}
       
  }
   //*/

}
