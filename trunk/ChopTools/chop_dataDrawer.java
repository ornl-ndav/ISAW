  /*
 * @(#)chop_evaluation.java  0.1 99/08/02  5:44 pm  Dongfeng Chen
 *  A data sum tools for get x-y  plot for grouping detectors 
 *  
 *
 */

package ChopTools;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.retriever.*;
import  graph.*;
import  java.awt.*;

public class chop_dataDrawer
{
   chop_dataDrawer(){}
   
  protected static void SeeGraph(double[] dataArray)
  {
        GraphFrame graphFrame = new GraphFrame();
		
		G2Dint graph = new G2Dint();
		graphFrame.add(graph);
		graph.setDataBackground(Color.white);
        graph.setGraphBackground(Color.white);
        graph.setBounds(0,0,500,500);
        graph.DataSet dataSet = null;
        try{//create the dataSet
            dataSet = new graph.DataSet(dataArray, dataArray.length/2);
        }catch(Exception e){ System.out.println(e.toString()); }
        
        try{
            graph.attachDataSet(dataSet);
        } catch (Exception e){ System.out.println(e.toString()); }
        
        //create and configure xaxis
        Axis xaxis = graph.createXAxis();
        xaxis.attachDataSet(dataSet);
        xaxis.setTitleText("X");
        xaxis.setTitleColor(Color.black);
        xaxis.setTitleFont(new Font("TimesRoman", Font.ITALIC,25));
        xaxis.setLabelFont(new Font("TimesRoman", Font.PLAIN,20));
        xaxis.setLabelColor(Color.black);
        //create and configure yaxis
        Axis yaxis = graph.createYAxis();
        yaxis.attachDataSet(dataSet);
        yaxis.setTitleText("Y");
        yaxis.setTitleColor(Color.black);
        yaxis.setTitleFont(new Font("TimesRoman", Font.ITALIC, 25));
        yaxis.setLabelFont(new Font("TimesRoman", Font.PLAIN, 20));
        yaxis.setLabelColor(Color.black);
        graphFrame.setVisible(true);
        //graphFrame.invalidate();
        //graphFrame.validate();
        
		
  }


         public static void drawgraph (Data groupd)
         {
            XScale  x1=groupd.getX_scale();
         
            float stepx1=(x1.getEnd_x()- x1.getStart_x()+1)/x1.getNum_x();                 
         
         
                    int length1=2*groupd.getY_values().length;
                    double [] sg=new double[length1];
                    for(int i=0; i<length1; i++)
                    {
                        if(i%2==0)
                        {
                            sg[i]=x1.getStart_x()+(i/2)*stepx1 ;//channel
                        }
                        else
                        {
                            sg[i]=groupd.getY_values()[(i-1)/2];
                        }
                        
                    }
         
         
         //double [] graph = groupd.getGraphDataList();
         SeeGraph(sg);
         }
         
         
        public static void datawriter(Data groupd, DataSetTools.dataset.DataSet data_set_H1 )
                                                 throws IOException {
         
         AttributeList    attr_list;
         AttributeList    attr_listDataSet;
         String s=null;
         String runNumber=null;
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

              //  try{Thread.sleep(8000);}Catch
                
          File f= new File(".\\ChopTools\\g_grouper"+runNumber+".opt");
          FileOutputStream op= new FileOutputStream(f);
          OutputStreamWriter opw = new OutputStreamWriter(op);

          for(int i=0; i<attr_listDataSet.getNum_attributes(); i++)
          {
                String attr_nameDataSet = (attr_listDataSet.getAttribute(i)).getName();
                String a=attr_nameDataSet+"\t "+(attr_listDataSet.getAttribute(i)).getValue();
                opw.write(a+"\n"); 
          }
                System.out.println("We are here!+++++++++++++\n"); 
             //  Thread.sleep(1000);
           
                 
          float[] y=groupd.getCopyOfY_values();
          int length=y.length;
          XScale  x=groupd.getX_scale();
          float stepx=(x.getEnd_x()- x.getStart_x()+1)/x.getNum_x();                 
          opw.write(" stepx is " +stepx+ "\n ");
          
          for (int j=0; j<length; j++)
          {
                    opw.write(j+ "\t " +(x.getStart_x()+j*stepx )+"\t "+y[j]+"\n ");
          }
                 
         // s=badID.length+" Bad detectors: ";
          opw.write(s); System.out.println(s);

          opw.flush();
          opw.close();             
         } 
         
          //* 
 //The main is used for test , and it make a file named as .\\ChopTools\\e_maker*.opt
  public static void main(String [] args)
  {
      RunfileRetriever data_retriever = null;
      DataSetTools.dataset.DataSet   data_set_H1 = null;
      DataSetTools.dataset.DataSet e_loss_data_set = null ;
      
      String runname = 
                       // ".\\ChopTools\\HRCS0976.RUN;20";  
                         //  ".\\ChopTools\\HRCS2447.RUN;24";  
                          ".\\ChopTools\\HRCS2712.RUN;1";  
         
      try
      {
         data_retriever = new RunfileRetriever(runname);
         data_set_H1 = data_retriever.getDataSet(1); 
         DataSetTools.operator.Operator op = data_set_H1.getOperator("Convert To Energy Loss");
         if ( op != null )   e_loss_data_set = (DataSetTools.dataset.DataSet)op.getResult();    // DO OPERATION
         else    System.out.println( "Operator is null" );
          
         System.out.println( "data_set_H1     = " + data_set_H1 );
         System.out.println( "e_loss_data_set     = " + e_loss_data_set );
        
         Data data1 = data_set_H1.getData_entry( 0 );        // get reference to the data entry
         Data data2 = e_loss_data_set.getData_entry( 0 );        // get reference to the data entry
         
        // Data groupd = grouper(data_set_H1, uplevel, lowlevel );
         System.out.println( "data1.getY_values().length is :" + (data1.getY_values()).length );
        
         //chop_dataDrawer.drawgraph(data1);
         //chop_dataDrawer.drawgraphDataSet(data_set_H1);
         chop_dataDrawer.drawgraphDataSet(e_loss_data_set);
            //    Thread.sleep(8000);
        // chop_dataX_YplotWriter.datawriter(groupd,data_set_H1 );
         
         
 
         // System.out.println("This is the intergrated peak intensity: "+chop_calibraton.intergratedPeak1Intensity(data_set_M1));
         Thread.sleep(4000);
         
      }catch(Exception e){}
       
  }
         public static void drawgraphDataEntry (DataSetTools.dataset.DataSet ds, int data_ID)
         {
            
          //DataSetTools.dataset.DataSet ds = this.getDataSet();
          DataSetTools.dataset.DataSet new_ds = (DataSetTools.dataset.DataSet)ds.clone();
          //new_ds.copyOp_log( ds );
         // new_ds.addLog_entry( "Plot Data" );
    
          System.out.println("The H1 is : "+ds );
           // try{Thread.sleep(2000);}catch(Exception e){}
                
       //   if ( !ds.getX_units().equalsIgnoreCase("Time(us)")  ||
       //        !ds.getY_units().equalsIgnoreCase("Counts") )      // wrong units, so
       //     return null;        
      
  
         //  DataSet new_ds = ds;
         int  num_data =new_ds.getNum_entries();
   
         System.out.println(" num_data is : "+ num_data );
          
         // try{Thread.sleep(2000);}catch(Exception e){}
        
         Data   groupd=null ;
    
         // data = new_ds.getData_entry( 3 );        // get reference to the data entry
    
         for(int i=num_data; i>=0; i--)
         {
              groupd = new_ds.getData_entry( i );        // get reference to the data entry
              if (groupd!=null && i==data_ID)
              {
                    System.out.println(" find it : "+ i+" entry" );
                   // try{Thread.sleep(2000);}catch(Exception e){}
                    break;
                 
              }
         }
             
            
            XScale  x1=groupd.getX_scale();
         
            float stepx1=(x1.getEnd_x()- x1.getStart_x()+1)/x1.getNum_x();                 
         
         
                    int length1=2*groupd.getY_values().length;
                    double [] sg=new double[length1];
                    for(int i=0; i<length1; i++)
                    {
                        if(i%2==0)
                        {
                            sg[i]=x1.getStart_x()+(i/2)*stepx1 ;//channel
                        }
                        else
                        {
                            sg[i]=groupd.getY_values()[(i-1)/2];
                        }
                        
                    }
         
         
         //double [] graph = groupd.getGraphDataList();
         SeeGraph(sg);
         }
         
         public static void drawgraphDataSet (DataSetTools.dataset.DataSet ds)
         {
            
          //DataSetTools.dataset.DataSet ds = this.getDataSet();
          DataSetTools.dataset.DataSet new_ds = (DataSetTools.dataset.DataSet)ds.clone();
          //new_ds.copyOp_log( ds );
         // new_ds.addLog_entry( "Plot Data" );
    
          System.out.println("The H1 is : "+ds );
           // try{Thread.sleep(2000);}catch(Exception e){}
                
       //   if ( !ds.getX_units().equalsIgnoreCase("Time(us)")  ||
       //        !ds.getY_units().equalsIgnoreCase("Counts") )      // wrong units, so
       //     return null;        
      
  
         //  DataSet new_ds = ds;
         int  num_data =new_ds.getNum_entries();
   
         System.out.println(" num_data is : "+ num_data );
          
         // try{Thread.sleep(2000);}catch(Exception e){}
        
         Data   groupd=null ;
    
         // data = new_ds.getData_entry( 3 );        // get reference to the data entry
    
         for(int i=num_data; i>=0; i--)
         {
              groupd = new_ds.getData_entry( i );        // get reference to the data entry
              if (groupd!=null)
              {
                    System.out.println(" find it : "+ i+" entry" );
                   // try{Thread.sleep(2000);}catch(Exception e){}
                    break;
                 
              }
         }
             
            
            XScale  x1=groupd.getX_scale();
         
            float stepx1=(x1.getEnd_x()- x1.getStart_x()+1)/x1.getNum_x();                 
         
         
                    int length1=2*groupd.getY_values().length;
                    double [] sg=new double[length1];
                    for(int i=0; i<length1; i++)
                    {
                        if(i%2==0)
                        {
                            sg[i]=x1.getStart_x()+(i/2)*stepx1 ;//channel
                        }
                        else
                        {
                            sg[i]=groupd.getY_values()[(i-1)/2];
                        }
                        
                    }
         
         
         //double [] graph = groupd.getGraphDataList();
         SeeGraph(sg);
         }
         
         
         
}