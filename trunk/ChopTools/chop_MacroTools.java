package  ChopTools;

import  java.io.*;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  ChopTools.*;
import  java.awt.*;


/**
 * Tools and methods to Calibrate, Evaluate, Normalize Chopper 
 * Spectrometer Data. Some of those method can also be used for
 * diffiractometer.
 *
 * @author Dongfeng Chen
 * @author Alok Chatterjee
 * @author Dennis Mikkelson
 * @author Tom Worlton
 * @version 1.0
 * @since August 10, 1999
 * @see DataSetTools.operator.SpectrometerEvaluator
 * @see DataSetTools.operator.SpectrometerNormalizer
 * @see DataSetTools.operator.SpectrometerTofToEnergyLoss
 */

public class chop_MacroTools 
{

  /**
   * *******************************
   * Construntor for chop_MacroRools
   * *******************************
   */
  public chop_MacroTools( ){}

  /**
   * *******************************************************
   * sumy is an array to hold the sum of Y_value data object
   * *******************************************************
   */
  
  public static  float [] sumy=null;

  /**
   * ***********************************************************
   * addednum is an integer to hold the sum of added data number
   * ***********************************************************
   */
  
  public static   int addednum=0;

  /**
   * *****************************************
   * Convert tof dataSet to S_E dataSet,   and
   * the incident energy use the attibute from
   * data object
   * -   mainly coming from Dennis Mikkelson's 
   *   Mikkelson's SpectrometerTofToEnergyLoss
   * *****************************************
   * 
   * @param ds DataSet ds hold the input dataset
   * @param min float min define the mininum of the energy plot. It is 
   * automatically calcalted by data attribute energy_in.
   * @param max max is the maxinum for energy plotting.
   * @return DataSet new_ds
   */
  
  public static Object toEnergyLoss(DataSet ds, float min, float max)
  {
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "meV",
                                     "EnergyLoss",
                                     "counts",
                                     "Scattering Intensity" );

    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Converted to Energy Loss" );
    new_ds.getAttributeList().addAttributes( ds.getAttributeList() );
    float min_E = min;
    float max_E = max;;
    int   num_E = 120;
    if ( min_E > max_E )             
    {
      float temp = min_E;
      min_E = max_E;
      max_E = temp;
    }

    XScale new_e_scale;
    if ( num_E <= 1.0 || min_E >= max_E )     
      new_e_scale = null;
    else
      new_e_scale = new UniformXScale( min_E, max_E, num_E );  
    Data             data,
                     new_data;
    DetectorPosition position;
    float            energy_in;
    Float            energy_in_obj;
    float            y_vals[];              
    float            e_vals[];             

    XScale           E_scale;
    float            spherical_coords[];
    int              num_data = ds.getNum_entries();
    AttributeList    attr_list;

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );      
      attr_list = data.getAttributeList();

      position=(DetectorPosition)
                   attr_list.getAttributeValue(Attribute.DETECTOR_POS);

      energy_in_obj=(Float)
                      attr_list.getAttributeValue(Attribute.ENERGY_IN);

      if( position != null && energy_in_obj != null)
      {                                               
        
        energy_in        = energy_in_obj.floatValue();

        spherical_coords = position.getSphericalCoords();
        e_vals           = data.getX_scale().getXs();
        for ( int i = 0; i < e_vals.length; i++ )
          e_vals[i] = energy_in - 
                      tof_calc.Energy( spherical_coords[0], e_vals[i] );
  
        E_scale = new VariableXScale( e_vals );
        y_vals  = data.getCopyOfY_values();

        new_data = new Data( E_scale, y_vals, data.getGroup_ID() ); 
                                                 
        new_data.setSqrtErrors();                
        new_data.setAttributeList( attr_list );
        
        if ( new_e_scale != null )               
          new_data.ReBin( new_e_scale );         

        new_ds.addData_entry( new_data );      
      }
    }

    return new_ds;
  }  

  /**
   * **************************************************
   * Nomalize the input dataset by its monotor datasets
   * **************************************************
   * 
   * @param ds input dataset
   * @param monitor_ds for scaling data by callibration
   * @return DataSet new_ds
   */

  
  public  static Object macroNomalizer(DataSet ds, DataSet monitor_ds )
  {                                   
    
     DataSet new_ds = (DataSet)ds.empty_clone(); 
     new_ds.addLog_entry( "Normalize Using " + monitor_ds );
     
     System.out.println("Normalize Using " + monitor_ds );
     
     float nomalizer=chop_calibraton.intergratedPeak1Intensity(monitor_ds);
     
     System.out.println("Nomalizer is:"+ nomalizer);
                                             
     float scale = nomalizer/500000;
     System.out.println("Scale is:"+ scale);
     
                                    
    if ( scale != 0 )                 
    {                                
      int num_data = ds.getNum_entries();
      Data data,
           new_data;
      for ( int i = 0; i < num_data; i++ )
      {
        data = ds.getData_entry( i );        
        new_data = data.divide( scale );    
                                             
        new_ds.addData_entry( new_data );      
        System.out.println("data.getY_values()[5] is :"+data.getY_values()[5]);
        System.out.println("new_data.getY_values()[5] is :"+new_data.getY_values()[5]);
        System.out.println("Scale is:"+ scale);
           // try{               Thread.sleep(3000);            }catch(Exception e){}
      
      }
        
    }

    return new_ds;
  }  

  /**
   * *********************************************************
   * Save the x and y array's from data object of dataset into
   * several files
   * *********************************************************
   * 
   * @param groupd input data or grouped data
   * @param data_set_H1 
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * dataset for getting attribute from it. It is better to 
   * re-classific some of those attribute to avoiding pass 
   * this parameter!
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * @param ID the output file ID to identify the ouput files
   * @exception java.io.IOException
   */
  

  public static void datawriterID(Data groupd, DataSetTools.dataset.DataSet data_set_H1, int ID )
                                                 throws IOException {
         
         try{
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

              //  Thread.sleep(8000);
                
          File f= new File(".\\ChopTools\\g_Q"+ID+"X-Y"+runNumber+".opt");
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
          
          
          for (int j=0; j<length-1; j++)
          {
                    opw.write(j+ "\t " +(x.getStart_x()+j*stepx )+"\t "+y[j]+"\n ");
          }
                 
          s="The end, Congradulations!";
          opw.write(s); System.out.println(s);

          opw.flush();
          opw.close();             
         }catch(Exception e){}
         } 

  /**
   * **********************************************
   * Group the datum which ID in the certain region
   * **********************************************
   * 
   * @param ds input dataset
   * @param uplevel the max detector angle of data
   * @param lowlevel the min detector angle of data
   * @return DataSet new_ds
   */
  
  public static Data grouperformacro(DataSetTools.dataset.DataSet ds,  float uplevel, float lowlevel)
  {

         int num_data = ds.getNum_entries();
         Data data,
         new_data;
         System.out.println("num_data is "+num_data);
           // try{Thread.sleep(1000);}catch(Exception e){}
         AttributeList    attr_list=null;
         XScale  x=null;
         
         Data data1 = ds.getData_entry( 0 );        // get reference to the data entry
         XScale x1=data1.getX_scale();
            
         sumy= new float[x1.getNum_x()];
         for ( int i = 0; i < num_data; i++ )
         {
            data = ds.getData_entry( i );        // get reference to the data entry
            attr_list = data.getAttributeList();
            int detnum =      ((int [])(attr_list.getAttributeValue(Attribute.DETECTOR_IDS))).length;
            float angle  =      ((Float)(attr_list.getAttributeValue(Attribute.RAW_ANGLE))).floatValue();
            
            
            if(angle >= lowlevel && angle <= uplevel)
            {
            System.out.println("(Detector "+detnum+" angel in specific subgroup: )angle = "+angle  );
                addednum++;
                addYformacro(data);
            }
            
         }
         
         for (int j = 0; j<sumy.length; j++){
            
                sumy[j]=sumy[j]/addednum;                
         }
         System.out.println(" Number of added detector "+addednum);         
         Data groupeddata=new Data(x1, sumy, 1000);
         groupeddata.setAttributeList( attr_list );
         groupeddata.setGroup_ID(1000); 
         return  groupeddata;
         
         }

       /**
        * ************************************************************
        * Tiny tool to add Y_value of a data object. It is useful for
        * both spectrometer and diffractometer
        * ************************************************************
        * 
        * @param data the data will be add to the original one
        */

       public static void addYformacro(Data data){
            
         float[] y=data.getCopyOfY_values();
         for (int j = 0; j<y.length; j++){
            
                sumy[j]+=y[j];                
         }
   }


 /**
  * ***************************************************************
  * Graph plotting tools for drawing single or multiple data curves
  * in a panel. By inputting a dataset.
  * ***************************************************************
  * 
  * @param ds input dataset
  */

 public GraphFrame drawAlldata (DataSetTools.dataset.DataSet ds)
 {
            
        DataSetTools.dataset.DataSet new_ds = (DataSetTools.dataset.DataSet)ds.clone();

        int  num_data =new_ds.getNum_entries();

        Data   groupd=null ;

        double[][] dataArray=new double[num_data][];
        double [] sg=null;
        for(int i=0;i<num_data; i++)
        {
             groupd = new_ds.getData_entry(i);        // get reference to the data entry
             if (groupd!=null)
             {
                    System.out.println(" find it : "+ i+" entry" );
                   // try{Thread.sleep(2000);}catch(Exception e){}
                    XScale  x1=groupd.getX_scale();
         
                    float stepx1=(x1.getEnd_x()- x1.getStart_x()+1)/x1.getNum_x();                 
         
         
                    int length1=2*groupd.getY_values().length;
                    
                    sg=new double[length1];
                    dataArray[i]=new double[length1];
                    for(int j=0; j<length1; j++)
                    {
                        if(j%2==0)
                        {
                            sg[j]=x1.getStart_x()+(j/2)*stepx1 ;//channel
                        }
                        else
                        {
                            sg[j]=groupd.getY_values()[(j-1)/2];
                        }
                        
                    }
                    dataArray[i]=sg;
                 
                    System.out.println(" The value dataArray["+i+"] is"+ dataArray[i][5]+ "\n length is "+dataArray[i].length);
             }
        }
             
            
       GraphFrame gf = (GraphFrame) SeeAllDatafromArray(dataArray, new_ds);
       return gf;
  }


  //public static void SeeAllDatafromArray(double[][] dataArray, DataSet new_ds)
  public GraphFrame SeeAllDatafromArray(double[][] dataArray, DataSet new_ds)
  {

        GraphFrame graphFrame = new GraphFrame("Chop Graph");
        graphFrame.setSize(600, 600);
		System.out.println("Before graph : ");
		graph.G2Dint graph = new graph.G2Dint();
		
		System.out.println("Past graph : ");
		
		graphFrame.add(graph);
        
		graph.setDataBackground(Color.white);
        graph.setGraphBackground(Color.white);
        graph.setBounds(0,0,600,600);
        graph.DataSet dataSet = null;
        graph.Axis xaxis = graph.createXAxis();
        graph.Axis yaxis = graph.createYAxis();
       
      
        
        
        System.out.println(" The dataArray.length is "+ dataArray.length );
        System.out.println(" The dataArray[0].length is "+ dataArray[0].length );
        
        //try{Thread.sleep(2000);}catch(Exception e){}
       
       // for(int i=0;i<dataArray.length;i++){
        for(int i=0;i<dataArray.length;i++){
        
        try{//create the dataSet
            dataSet = new graph.DataSet(dataArray[i], dataArray[i].length/2);
            graph.attachDataSet(dataSet);
        } catch (Exception e){ System.out.println(e.toString()); }
        xaxis.attachDataSet(dataSet);
        yaxis.attachDataSet(dataSet);
        
       }
        //create and configure xaxis
        
      //  Axis xaxis = graph.createXAxis();
       // xaxis.attachDataSet(dataSet);
       System.out.println("The labels are : "+new_ds.getX_label());
        xaxis.setTitleText(new_ds.getX_label()+" "+"("+new_ds.getX_units()+")");
        
        xaxis.setTitleColor(Color.black);
        xaxis.setTitleFont(new Font("TimesRoman", Font.ITALIC,25));
        xaxis.setLabelFont(new Font("TimesRoman", Font.PLAIN,20));
        xaxis.setLabelColor(Color.black);
   
        //create and configure yaxis
       // Axis yaxis = graph.createYAxis();
       // yaxis.attachDataSet(dataSet);
        yaxis.setTitleText(new_ds.getY_label()+ " "+"("+new_ds.getY_units()+")");
        yaxis.setTitleColor(Color.black);
        yaxis.setTitleFont(new Font("TimesRoman", Font.ITALIC, 25));
        yaxis.setLabelFont(new Font("TimesRoman", Font.PLAIN, 20));
        yaxis.setLabelColor(Color.black);
        graphFrame.setVisible(true);
        
        //graphFrame.invalidate();
        //graphFrame.validate();
       return graphFrame;
		
  }
         


}
