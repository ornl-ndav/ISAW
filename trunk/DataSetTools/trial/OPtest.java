// Dongfeng Chen ... test program
import  IPNS.Runfile.*;
import  DataSetTools.dataset.*;
import  DataSetTools.retriever.*;
import  DataSetTools.operator.*;
import  IsawGUI.*;

import DataSetTools.viewer.*;

public class OPtest 
{
    
  public static void main(String [] args)
  {
       p("\n\n\n******************************************************************\n");
        String runname = 
                  "/IPNShome/dennis/ARGONNE_DATA/HRCS2445.RUN;19";
         
         RunfileRetriever data_retriever = null;
         DataSet   data_set = null;
         DataSet   energy_ds = null;
         DataSet   fudge_ds = null;
         
         ViewManager view_manager = null;
         try
         {
              data_retriever = new RunfileRetriever(runname);
              
              data_set = data_retriever.getDataSet(1);
              
              //get energy loss scale data set
              SpectrometerTofToEnergyLoss op = 
                    new SpectrometerTofToEnergyLoss(data_set, -120.0f, 120.0f, 140);
              energy_ds = (DataSet)op.getResult();
              
              //get fudge factor dataset
              SpectrometerDetectorNormalizationFactor fop = 
                    new SpectrometerDetectorNormalizationFactor(energy_ds);              
              fudge_ds = (DataSet)fop.getResult();
              
              
              p("DataSet is : "+data_set+"  "+ energy_ds+"  "+fudge_ds);
             
              ViewManager vm = new ViewManager( fudge_ds,IViewManager.IMAGE);
             
         }catch(Exception e){p("Err from:"+e);}
      
       p("\n******************************************************************\n\n\n");
      // System.exit(0);
  }
   
    
    public static void p(String string)
    {
        System.out.println(string);
    }
    
}
