/*
 * @(#)chop_calibraton.java  0.1 99/07/19  Dongfeng Chen
 */

package ChopTools;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.retriever.*;

/**
  *  Calibration tools 
  */

public class chop_calibraton implements Serializable
{
  /* --------------------------- CONSTRUCTOR ------------------------------ */

  private static String  s; 
  
  public chop_calibraton(){}
    
  
 //* 
  public static void main(String [] args)
  {
         RunfileRetriever data_retriever = null;
         DataSet   data_set_M1 = null;
        //String runname = ".\\DataSetTools\\choptools\\Hrcs2350.run";
         String runname = ".\\ChopTools\\HRCS2712.RUN;1";
         try
         {
          data_retriever = new RunfileRetriever(runname);
          data_set_M1 = data_retriever.getDataSet(0); 
         
         
         System.out.println("This is the calibrated energy: "+ chop_calibraton.getCalculatedEnergy(data_set_M1));
         System.out.println("This is the intergrated peak intensity: "+chop_calibraton.intergratedPeak1Intensity(data_set_M1));
         
        
         Thread.sleep(8000);
         
         }catch(Exception e){}
       
  }
  
    public static float  getCalculatedEnergy(DataSet data_set_M1){
        return calib(data_set_M1 )[0];
    }
    
    public static float  intergratedPeak1Intensity(DataSet data_set_M1){
        return calib(data_set_M1 )[1];
    }
    
    
    public static float[] calib(DataSet data_set_M1){
      
      
        Data             data;
        float[] y1, y2;
        float [][] moni=new float[2][];
            
        XScale  x1, x2;// x1,x2 is wrong number of min and max values for hrnmecs
                           // any way we will use the relative value max-min later.
                           // for test can use the revised runfileretriever
        data = data_set_M1.getData_entry( 0 );        

        y1 = data_set_M1.getData_entry(0).getCopyOfY_values();
        y2 = data_set_M1.getData_entry(1).getCopyOfY_values();
        x1 = data_set_M1.getData_entry(0).getX_scale();
        x2 = data_set_M1.getData_entry(1).getX_scale();
        
        float [][] t=tof(x1,x2);
        
        moni[0] =y1;
        moni[1] =y2;
       /*    
        s= "From TOF float array: \n"+
                         "TOF of M1: (Min, Max, Num, Step ) "+
                         t[0][0]+" "+t[0][1]+" "+t[0][2]+" "+t[0][3]+"\n"+
                         "TOF of M2: (Min, Max, Num, Step ) "+
                         t[1][0]+" "+t[1][1]+" "+t[1][2]+" "+t[1][3]+"\n ";
                         
             //System.out.println(s);
        s= "The length of array moni[0][] and moni[1][]: \n"+
                         moni[0].length +", "+moni[1].length +"\n";
        //*/
        int[] ma=MainPeak(moni,t);
             //System.out.println(s);
        int[] ha=HalfHigh(moni,t);
             //System.out.println(s);
        float[] ce=CentRoid(moni,t);
            // System.out.println(s);
        float[] in =  InverseV(moni,t);    
            // System.out.println(s);
        
        int peak1Positioin  =ma[0];
        int peak1High       =ma[1];
        int peak2Positioin  =ma[2];
        int peak2High       =ma[3];
        
        float realcentroid1 =ce[0];
        float realcentroid2 =ce[1];
        float width1        =ce[2];
        float width2        =ce[3];
        float area1         =ce[4];
        float area2         =ce[5];
        float idealcentroid1=ce[6];
        float idealcentroid2=ce[7];
        
        float inversev       =in[0];
        float inenergy       =in[1];
        float wavelength     =in[2];
        float inputTimefromSampleToDetector=in[3];
        float realTimefromSampleToDetector=in[4];
        float timeDifferenceFromSourceToSampleBetweenInputAndReal=in[5];
            
        float [] calib=new float[10];
            
        calib[0]=inenergy;
        calib[1]=area1;
            
        return calib;
       }
  
  
      
       protected static float[][] tof(XScale x1, XScale x2)
       {
        float[][] tofi=
        {  {x1.getStart_x(),x1.getEnd_x(),x1.getNum_x(),(x1.getEnd_x()-x1.getStart_x())/(x1.getNum_x()-1)},
           {x2.getStart_x(),x2.getEnd_x(),x2.getNum_x(),(x2.getEnd_x()-x2.getStart_x())/(x1.getNum_x()-1)}};
               
               String s= "From tof (x1,x2) method\n"+
                         "TOF of M1: (Min, Max, Num, Step ) "+
                         tofi[0][0]+" "+tofi[0][1]+" "+tofi[0][2]+" "+tofi[0][3]+"\n"+
                         "TOF of M2: (Min, Max, Num, Step ) "+
                         tofi[1][0]+" "+tofi[1][1]+" "+tofi[1][2]+" "+tofi[1][3]+"\n ";
               //System.out.println(s);
        
        return tofi;
       }
  

    //+++++++++++++++++++++++++++ from native copy++++++++++++++++++++++++++
   public  static  int[] MainPeak(float[][] mon, float[][] ec1)
   {
        float H1=0; float H2=0;
        int k=0; int l=0;   float[] spectrum=null;
        float HighofMainPeak1=0;
        float HighofMainPeak2=0;
        
        try{        
           
           spectrum=mon[0];
           
           for (int j=1; j<spectrum.length;j++)
                {
                  if (spectrum[j]>HighofMainPeak1)
                   {
                  HighofMainPeak1=Math.max(HighofMainPeak1, spectrum[j]);
   
                  // HighofMainPeak=spectrum[j];
                   H1=HighofMainPeak1;
                   k=j;
                   }
                 //*/  
                }

           spectrum=mon[1];//r.getSpectrum32(2,1);
           for (int j=1; j<spectrum.length;j++)
                {
                   if (spectrum[j]>HighofMainPeak2)
                   {
                  HighofMainPeak2=Math.max(HighofMainPeak2, spectrum[j]);
                    
                  // HighofMainPeak=spectrum[j];
                   H2=HighofMainPeak2;
                   l=j;
                   }
                }
            
            s="HighofMainPeak1: "+HighofMainPeak1+"    H1: "+H1+"    j:"+k+"\n"+
              "HighofMainPeak2: "+HighofMainPeak2+"    H2: "+H2+"    j:"+l+"\n";
            //system.out.println(s);
                   

         } catch(Exception e){}

       int[] mp= new int[4];
       mp[0]=k;
       mp[1]=l;
       mp[2]=(int)H1;
       mp[3]=(int)H2;
       return mp;
   }




 

   public static int[] HalfHigh(float[][] mon, float[][] ec1)
   {
    int MainPeak[] = MainPeak(mon,ec1);
    float[] spectrum=null;
    float halfh1=0;
    float halfh2=0;
    
    try{
        spectrum=mon[0];//r.getSpectrum32(1,1);
        halfh1=(float)spectrum[MainPeak[0]]/2;

        spectrum=mon[1];//r.getSpectrum32(2,1);
        halfh2=(float)spectrum[MainPeak[1]]/2;
        }catch(Exception e){}
   s="Half high of M1 and M2: "+halfh1+ ", "+halfh2+" \n";
   //system.out.println(s);
   
   int[] hh=new int[2];
   hh[0]=(int)halfh1;
   hh[1]=(int)halfh2;
   return hh;
   }


   public static float[] CentRoid(float[][] mon, float[][] ec1)
   {

    int HalfHigh[]=HalfHigh(mon, ec1);
    float[] spectrum=null;
    float[] spectrumf=null;
    
    float realcentroid1=0;
    float idealcentroid1=0;
    int start1=0;
    int  end1=0;
    float area1=0;
    float B1=0;
    int width1=0;

    
    try{
        int start1h=0;
        int end1h=0;
        spectrum=mon[0];//r.getSpectrum32(1,1);
        
         spectrumf = new float[ spectrum.length ];
          for ( int j = 0; j < spectrumf.length; j++ )
         { spectrumf[j] =spectrum[j];}
        
        
        for (int j=1; j<spectrum.length; j++)
        {
            if(spectrum[j]>=HalfHigh[0])
            {start1h=j;
            break;
            }
        }
        
        for (int j=start1h; j<spectrum.length; j++)
        {
            if(spectrum[j]<=HalfHigh[0])
            {end1h=j-1;
            break;
            }
        }
         start1=start1h-2*(end1h-start1h);
         end1=end1h+2*(end1h-start1h);
         width1=end1h-start1h;
        
         if (start1<12) start1=0;
         if (end1>spectrum.length) end1=spectrum.length;
        
        }catch(Exception e){}


      try{
         for( int k=start1; k<=end1; k++)
         {
         area1=area1+spectrum[k];
         B1=B1+spectrumf[k]*((float)k-1/2);
         }
         }catch(Exception e){}

      try{
        //  realcentroid1=B1/area1+r.getSpectrometerTimeFieldData(1)[0];
       //  idealcentroid1=29807.954f/(float)(Math.sqrt((double)r.EnergyIn()));
        
        realcentroid1=B1/area1+ec1[0][0]+100.4f;
         }catch(Exception e){}

 // monitor 2
  //  int HalfHigh[]=HalfHigh(r);
   // int[] spectrum=null;
   // float[] spectrumf=null;
    
    float realcentroid2=0;
    float idealcentroid2=0;
    int start2=0;
    int  end2=0;
    float area2=0;
    float B2=0;
    int     width2=0;

    
    
    
    try{
        int start2h=0;
        int end2h=0;
        spectrum=mon[1];//r.getSpectrum32(2,1);
        
         spectrumf = new float[ spectrum.length ];
          for ( int j = 0; j < spectrumf.length; j++ )
         { spectrumf[j] =spectrum[j];}
        
        
        for (int j=1; j<spectrum.length; j++)
        {
            if(spectrum[j]>=HalfHigh[1])
            {start2h=j;
            break;
            }
        }
        
        for (int j=start2h; j<spectrum.length; j++)
        {
            if(spectrum[j]<=HalfHigh[1])
            {end2h=j-1;
            break;
            }
        }
         start2=start2h-2*(end2h-start2h);
         end2=end2h+2*(end2h-start2h);
         width2=end2h-start2h;
        
         if (start2<12) start2=0;
         if (end2>spectrum.length) end2=spectrum.length;
        
        }catch(Exception e){}


      try{
         for( int k=start2; k<=end2; k++)
         {
         area2=area2+spectrum[k];
       //  B2=B2+spectrumf[k]*((float)k-1/2);
         B2=B2+spectrumf[k]*((float)k-1/2);
         }
         }catch(Exception e){}

      try{
        //  realcentroid2=B2/area2+r.getSpectrometerTimeFieldData(2)[0] ;
       // idealcentroid2=43405.6f/(float)(Math.sqrt((double)r.EnergyIn()));
        realcentroid2=B2/area2+ec1[1][0]+100.4f ;
         }catch(Exception e){}

   //String 
   s="realcentroid1; realcentroid2; area1; area2; width1; width2;"+
            "idealcentroid1;idealcentroid2;\n"+
            realcentroid1+
            " "+realcentroid2+
            " "+area1+
            " "+area2+
            " "+width1+
            " "+width2+
            " "+idealcentroid1+
            " "+idealcentroid2+"\n";
            
   //system.out.println(s);

   float[] cr=new float[8];
   cr[0]=realcentroid1;
   cr[1]=realcentroid2;
   cr[2]=width1;
   cr[3]=width2;
   cr[4]=area1;
   cr[5]=area2;
   cr[6]=idealcentroid1;
   cr[7]=idealcentroid2;
   
   
   return cr;
   }
 //**
   public static float[] InverseV(float[][] mon, float[][] ec1)
   {
    float CentRoid[]=CentRoid(mon,ec1);
    float inversev=0;
    float  inenergy=0;
    float wavelength=0;
    float TOFM12=0;
    float DM12=0;
    float inputTimefromSampleToDetector=0;
    float realTimefromSampleToDetector=0;
    float timeDifferenceFromSourceToSampleBetweenInputAndReal=0;
       
    try{
        TOFM12=CentRoid[1]-CentRoid[0];
       // Runfile r=new Runfile(".\\DataSetTools\\choptools\\HRCS2712.RUN;1");
        DM12=5.9535f;
        //System.out.println("DM12="+DM12);
        inversev=TOFM12/DM12;
        inenergy=5227600/(inversev*inversev);
        wavelength =3955*inversev/1000000;
       // TimefromSampleToDetector=4.0f*inversev;
        //inputTimefromSampleToDetector=4.0f*(float)Math.sqrt(5227600/r.EnergyIn());
        realTimefromSampleToDetector=4.0f*inversev;
        //timeDifferenceFromSourceToSampleBetweenInputAndReal=13.8f*((float)Math.sqrt(5227600/r.EnergyIn())-inversev);
       
    }catch(Exception e){}

   // String 
    s="inversev; inenergy; wavelength; inputTimefromSampleToDetector; "+
             " realTimefromSampleToDetector; timeDifferenceFromSourceToSampleBetweenInputAndReal;\n"+
             inversev+
             " "+inenergy+
             " "+wavelength+
             " "+inputTimefromSampleToDetector+
             " "+realTimefromSampleToDetector+
             " "+timeDifferenceFromSourceToSampleBetweenInputAndReal+"\n";
    
    //system.out.println(s);

    float[] iv=new float[6];
    iv[0]=(float)inversev;
    iv[1]=(float)inenergy;
    iv[2]=(float)wavelength;
    iv[3]=(float)inputTimefromSampleToDetector;
    iv[4]=(float)realTimefromSampleToDetector;
    iv[5]=(float)timeDifferenceFromSourceToSampleBetweenInputAndReal;
    return iv;
   }
  
  
}
