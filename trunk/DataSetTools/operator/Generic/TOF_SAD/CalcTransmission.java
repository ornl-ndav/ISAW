
/*
 * File:  CalcTransmission.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.9  2003/09/08 22:19:08  rmikk
 * Made the method to find monitor ID's for upstream and
 *    downstream monitors public and static
 *
 * Revision 1.8  2003/09/05 21:36:35  rmikk
 * Fixed Groups in monitor to use
 * Fixed the nonCadmium calculations
 *
 * Revision 1.7  2003/08/19 20:54:15  rmikk
 * Eliminated a debug display in the middle of the code
 *
 * Revision 1.6  2003/07/25 16:31:11  rmikk
 * Log file information is now written to the console
 *
 * Revision 1.5  2003/07/24 18:48:42  rmikk
 * Fixed an array out of bounds error
 *
 * Revision 1.4  2003/07/23 15:03:41  rmikk
 * Set the errors after the clone to square root errors if the
 *   error array is null
 *
 * Revision 1.3  2003/07/23 14:35:11  rmikk
 * Incorporated the weighting for the polynomial fit
 *
 * Revision 1.2  2003/07/22 20:56:20  rmikk
 * Added documentation
 * Added another parameter to select weights when
 *   fitting polynomials to the data
 *
 * Revision 1.1  2003/07/22 13:40:45  rmikk
 * Initial Check in
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;
import DataSetTools.dataset.*;
import java.util.*;
import DataSetTools.math.*;
import DataSetTools.operator.DataSet.Math.DataSet.*;
import DataSetTools.operator.DataSet.Math.Analyze.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.util.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.EditList.*;

/**
*     This class creates a transmission data set in llambda and rebinned
*     to match the corresponding histogram when it is converted to llambda
*/
public class CalcTransmission extends GenericTOF_SAD {
  StringBuffer log = new StringBuffer();


  /**
  *    Constructor for this class
  */
  public CalcTransmission(){
     super("Calculate Transmission Run");
  }
  
  /**
  *     Constructor for this class
  *    @param Sample   The monitor data set for the sample run
  *    @param Empty   The monitor data set for the Empty run
  *    @param Cadmium   The monitor data set for the Cadmium run
  *    @param SampleDS   The Histogram data set for the sample run
  *    @param useCadmium  true if cadmium run is to be used
  *    @param Neutron_Delay The delay factor or 0 if no delay is needed
  *    @param polyfitIndx1   the start channel to be fit(in rebinned llambda). the first
  *                         channel is channel 0
  *    @param  polyfitIndx2  the last channed to be fit(in rebinned llambda).the first
  *                         channel is channel 0. This last channel is included.
  *    @param  degree      degree of the polynomial to fit or -1 if no fitting is desired
  *    @param  weight      use 1/sqrt|y| values for weights instead of 1.
  */

  public CalcTransmission( MonitorDataSet Sample, MonitorDataSet Empty, MonitorDataSet Cadmium,
                SampleDataSet SampleDS,
                boolean useCadmium,float Neutron_Delay, int polyfitIndx1, int polyfitIndx2,
                int degree, boolean weight){
     this();
     parameters = new Vector();
     addParameter( new MonitorDataSetPG("Enter Sample Monitor",Sample) );
     addParameter( new MonitorDataSetPG("Enter Empty Monitor", Empty));
     addParameter( new MonitorDataSetPG("Enter Cadmium Monitor", Cadmium));
     addParameter( new SampleDataSetPG("Enter Sample Data", SampleDS));
     addParameter( new BooleanPG("Use Cadmium Run", true));
     addParameter( new FloatPG("Neutron Delay", Neutron_Delay) );
     addParameter( new IntegerPG( "Polyfit indx 1", polyfitIndx1) );
     addParameter( new IntegerPG( "Polyfit indx 2", polyfitIndx2) );
     addParameter( new IntegerPG( "Polynomial degree 1", degree) );

     addParameter( new BooleanPG("Use 1/sqrty weights", weight));



  } 

  /**
  *    Sets the default parameters
  */
  public void setDefaultParameters(){
     parameters = new Vector();
     addParameter( new MonitorDataSetPG("Enter Sample Monitor", null));
     addParameter( new MonitorDataSetPG("Enter Empty Monitor", null));
     addParameter( new MonitorDataSetPG("Enter Cadmium Monitor", null));
     addParameter( new SampleDataSetPG("Enter Sample Data", null));
     addParameter( new BooleanPG("Use Cadmium Run", true));
     addParameter( new FloatPG("Neutron Delay", 0.0f) );
     addParameter( new IntegerPG( "Polyfit indx 1", -1) );
     addParameter( new IntegerPG( "Polyfit indx 2", -1) );
     addParameter( new IntegerPG( "Polynomial degree 1", -1) );

     addParameter( new BooleanPG("Use 1/sqrt y weights", false));

  }

  /**
  *     Returns a string with log information
  */ 
  public String getLogString(){
     return log.toString();

  }
 
  /**
  *     Calculates and returns the transmission data set that is converted to llambda;
  *     and rebinned to the corresponding histogram.
  */
  public Object getResult(){
     log = new StringBuffer(1000);
     DataSet Sample = (DataSet)((MonitorDataSetPG)getParameter(0)).
                              getDataSetValue();
     DataSet Empty = ((DataSetPG)getParameter(1)).getDataSetValue();
     DataSet Cadmium = ((DataSetPG)getParameter(2)).getDataSetValue();
     DataSet SampleDs =((SampleDataSetPG)getParameter(3)).getDataSetValue();
     boolean useCadmium = ((Boolean)(getParameter(4).getValue())).booleanValue();
     float NeutronDelay =((FloatPG)getParameter(5)).getfloatValue();
     int polyfitIndx1=((IntegerPG)getParameter(6)).getintValue();
     int polyfitIndx2=((IntegerPG)getParameter(7)).getintValue();
     int degree=((IntegerPG)getParameter(8)).getintValue();
     boolean weight = ((Boolean)(getParameter(9).getValue())).booleanValue();
     int Monitor0 = 0;
     int Monitor1 = 2;
     int SampMonitor0GroupID = 1;
     int CadMonitor0GroupID = 1;
     int EmpMonitor0GroupID = 1;
     int SampMonitor1GroupID = 3;
     int CadMonitor1GroupID = 3;
     int EmpMonitor1GroupID = 3;
     int[] MonIndx = setMonitorInd( Sample);
     if( MonIndx == null)
       return new ErrorString("Could not find matching upStream and downStream monitors");
     Monitor0 = MonIndx[0];
     Monitor1 = MonIndx[1];
     
      SampMonitor0GroupID = Sample.getData_entry(Monitor0).getGroup_ID();
      CadMonitor0GroupID = Cadmium.getData_entry(Monitor0).getGroup_ID();
      EmpMonitor0GroupID = Empty.getData_entry(Monitor0).getGroup_ID();
      SampMonitor1GroupID = Sample.getData_entry(Monitor1).getGroup_ID();
     
      if( useCadmium)
        CadMonitor1GroupID = Cadmium.getData_entry(Monitor1).getGroup_ID();
      EmpMonitor1GroupID = Empty.getData_entry(Monitor1).getGroup_ID();
    System.out.println("FF"+Monitor0+","+Monitor1+","+SampMonitor0GroupID+","+
           CadMonitor0GroupID+","+EmpMonitor0GroupID+","+SampMonitor1GroupID+","+
           CadMonitor1GroupID+","+EmpMonitor1GroupID);
     if( !useCadmium)
        Cadmium = null;
     log.append("number of monitor detectors :");
     log.append(Format.integer((double)( Sample.getNum_entries()),4)+"\n");
     //--------------- Neutron Delay
     Sample = (DataSet)(Sample.clone());
     Empty = (DataSet)(Empty.clone());
     if( Cadmium != null) Cadmium = (DataSet)(Cadmium.clone());
     setErrors( Sample);
     setErrors( Empty);
     if( Cadmium != null)setErrors( Cadmium);
     if( NeutronDelay > 0){
       applyNeutronDelay( Sample, NeutronDelay );
       applyNeutronDelay( Empty, NeutronDelay );
       if( Cadmium != null)applyNeutronDelay( Cadmium, NeutronDelay );
     }
 
     //------------Convert to llambda------------------
      Operator opSample =   Sample.getOperator( "Monitor to Wavelength");
      Operator opEmpty =   Empty.getOperator( "Monitor to Wavelength");
      Operator opCadmium = null;
      if( Cadmium != null)
            opCadmium =   Cadmium.getOperator( "Monitor to Wavelength");

             //Assume if op is null it is already converted to Wavelength
      Object Result = null;
      if( opSample != null){
        opSample.setDefaultParameters();
        opSample.getParameter(0).setValue(new Float( -1.0f));
        opSample.getParameter(1).setValue(new Float( -1.0f));
        opSample.getParameter(2).setValue( new Integer(0));
        Result = opSample.getResult();
        if( (Result  instanceof ErrorString)  )
           return Result ;
        if( Result  == null)
           return new ErrorString( "Could not Convert Sample to Llamda");
        Sample = (DataSet) Result;
        Sample.setTitle("Sample-lambda and scaled");
      }

     if( opEmpty != null){
        opEmpty.setDefaultParameters();
        opEmpty.getParameter(0).setValue(new Float( -1.0f));
        opEmpty.getParameter(1).setValue(new Float( -1.0f));
        opEmpty.getParameter(2).setValue( new Integer(0));
        Result = opEmpty.getResult();
        if( (Result  instanceof ErrorString)  )
           return Result ;
        if( Result  == null)
           return new ErrorString( "Could not Convert Sample to Llamda");
        Empty = (DataSet) Result;
        Empty.setTitle("Sample-lambda and scaled");
      }


      if( opCadmium != null){
        opCadmium.setDefaultParameters();
        opCadmium.getParameter(0).setValue(new Float( -1.0f));
        opCadmium.getParameter(1).setValue(new Float( -1.0f));
        opCadmium.getParameter(2).setValue(new Integer(0));
        Result  = opCadmium.getResult();
        if( (Result instanceof ErrorString)  )
           return Result ;
        if( Result  == null)
           return new ErrorString( "Could not Convert Cadmium to Llamda");
        Cadmium = (DataSet) Result;
        Cadmium.setTitle("Cadmium-lambda and scaled");

      }

  
 
  // Resample the monitor's time channels to agree with the SampleDS's XScale
    //----------Convert SampleDs's to wavelengt---------------------
     opSample =   SampleDs.getOperator( "Convert to Wavelength");
     if( opSample != null){
        opSample.setDefaultParameters();
        opSample.getParameter(0).setValue(new Float( -1.0f));
        opSample.getParameter(1).setValue(new Float( -1.0f));
        opSample.getParameter(2).setValue( new Integer(0));
        Result = opSample.getResult();
        if( (Result  instanceof ErrorString)  )
           return Result ;
        if( Result  == null)
           return new ErrorString( "Could not Convert Sample to Llamda");
        SampleDs = (DataSet) Result;
        SampleDs.setTitle("SampleDs-lambda and scaled");
      }

     //----------- resample Monitors----------------
      int n = SampleDs.getNum_entries();
      XScale xscl = SampleDs.getData_entry(n/2).getX_scale();
      Sample.getData_entry(Monitor0).resample(xscl,IData.SMOOTH_NONE );
      Empty.getData_entry(Monitor0).resample(xscl,IData.SMOOTH_NONE);
      if( Cadmium != null)
         Cadmium.getData_entry(Monitor0).resample(xscl,IData.SMOOTH_NONE);
      Sample.getData_entry(Monitor1).resample(xscl,IData.SMOOTH_NONE);
      Empty.getData_entry(Monitor1).resample(xscl,IData.SMOOTH_NONE);
      if( Cadmium != null)
         Cadmium.getData_entry(Monitor1).resample(xscl,IData.SMOOTH_NONE);
      
     

     ReportToLog1( Sample,Monitor0,Monitor1);
     ReportToLog2( Sample,Monitor0,Monitor1);

     ReportToLog2( Empty,Monitor0,Monitor1);

     if( Cadmium != null)
       ReportToLog2( Cadmium,Monitor0,Monitor1);
   
     ReportToLog3( Sample, Empty, Cadmium,Monitor0,Monitor1);
    
     //------------ Calculate downstream monitor Relative monitors-------
     DataSet RelCadmium = null;
     Object Res = null;
    
     
     if(useCadmium){
       Res = ((new DataSetDivide_1( Cadmium, Cadmium, CadMonitor0GroupID, true)).getResult());
       if( Res instanceof ErrorString)
          return Res;
       if( Res instanceof String)
          return new ErrorString((String) Res);
       RelCadmium = (DataSet) Res;
     }
     
     DataSet RelSamp;
     Res = (new DataSetDivide_1( Sample,Sample,SampMonitor0GroupID, true)).getResult();
     if( Res instanceof ErrorString)
        return Res;
     if( Res instanceof String)
          return new ErrorString( (String)Res);
     RelSamp = (DataSet) Res;

      
     
     DataSet RelEmpty;
     Res = (new DataSetDivide_1( Empty,Empty, EmpMonitor0GroupID, true)).getResult();
     if( Res instanceof ErrorString)
        return Res;
     if( Res instanceof String)
        return new ErrorString((String) Res);
     RelEmpty = (DataSet) Res;

   
     //----- Subtract off cadmium run ----------------------
     if( useCadmium){
       Res = (new DataSetSubtract_1( RelSamp, RelCadmium,CadMonitor1GroupID, true)).getResult();
       if( Res instanceof ErrorString )
          return Res;
       if( Res instanceof String)
          return new ErrorString( (String)Res);
       
       RelSamp =(DataSet)Res;

       Res = (new DataSetSubtract_1( RelEmpty, RelCadmium, CadMonitor1GroupID,true)).getResult();
       if( Res instanceof ErrorString)
          return Res;
       if( Res instanceof String)
          return new ErrorString( (String)Res);
       RelEmpty =(DataSet)Res;
     }

   
     //----------------  Divide adjusted sample by ajusted empty------------
     Res = (new DataSetDivide_1( RelSamp, RelEmpty, EmpMonitor1GroupID,true)).getResult();
     if( Res instanceof ErrorString)
        return Res;
       if( Res instanceof String)
          return new ErrorString( (String)Res);
     DataSet Result_t = (DataSet) Res;
     Result_t.setTitle("tr_whole");
    
     Result_t.setSelectFlag(  Monitor1, true);
    
     ExtractCurrentlySelected  opx = new ExtractCurrentlySelected(
                  Result_t,true, true);
      
     Object O= opx.getResult();
     if( O == null)
       return new ErrorString( "Tranmission data is empty");


    //------------ Set some attributes (do logging)-----------------
    DataSet tr = (DataSet) O;
    int runNum1 = getRunNum( Sample);
    int runNum2 = getRunNum( Empty);
    int runNum3 = getRunNum( Cadmium);
    tr.setTitle("tr-"+runNum1+""+runNum2);
    int[] runs;
    if( Cadmium == null)
      runs= new int[2];
    else
      runs = new int[3];
    runs[0] = runNum1;
    runs[1] = runNum2;
    if( Cadmium != null)
      runs[2] = runNum3;
    tr.setAttribute(new IntListAttribute(  Attribute.RUN_NUM, runs) );
   

    //-------------- Now apply the Poly fit---------------------
    if( degree <= 0)
     return tr;
    int N = tr.getData_entry(0).getX_scale().getNum_x();
    if(polyfitIndx1 < 0)
      polyfitIndx1 = 0;
    if( polyfitIndx2 >= N -1)
       polyfitIndx2 = N-2;
    if( polyfitIndx1 >= polyfitIndx2)
      return tr;
    int groupID = tr.getData_entry(0).getGroup_ID();
    XScale xscl1 = tr.getData_entry(0).getX_scale();

    float[] xvals = xscl1.getXs();
    float[] yvals = tr.getData_entry(0).getY_values();
    
    double[] trunc_yvals = new double[ polyfitIndx2 - polyfitIndx1+1];
    double[] trunc_xvals = new double[ polyfitIndx2 - polyfitIndx1+1];
    for( int ii=polyfitIndx1 ; ii<= polyfitIndx2; ii++){
       trunc_xvals[ii-polyfitIndx1]=(xvals[ii]+xvals[ii +1])/2.0;
       trunc_yvals[ii-polyfitIndx1] =(double) yvals[ii];
    } 
     
    double[] coeff = new double[degree + 1];
    
    double errr=DataSetTools.math.CurveFit.Polynomial(trunc_xvals,
                            trunc_yvals,coeff, weight);
    for( int ii = 0; ii< yvals.length; ii++){
      yvals[ii]=0f;
      float xx = (xvals[ii]+xvals[ii+1])/2f;
      float xterm = 1;
      for(int trm=0; trm <=degree; trm++){
        yvals[ii] +=coeff[trm]* xterm;
        xterm = xterm*xx;
      }
    }
    System.out.println( log.toString());
    return tr;
    
     
    
  }

 //  Returns the Largest Run Number
 private int getRunNum( DataSet ds){
   if( ds == null)
     return -1;
   Attribute A = ds.getAttribute( Attribute.RUN_NUM);
   if( A == null)
     A = ds.getData_entry(0).getAttribute( Attribute.RUN_NUM);
   if( A == null)
     return -1;
   int[] list = ((IntListAttribute)A).getIntegerValue();
   Arrays.sort(list);
   if( list == null)
     return -1;
   if( list.length < 1)
     return -1;
   return list[list.length -1];
 }
 // only works for monitor data sets
  private void applyNeutronDelay( DataSet Sample, float delay){
   
    tof_data_calc.SubtractDelayedNeutrons(
           (TabulatedData) Sample.getData_entry(0),30f, delay);
    
    tof_data_calc.SubtractDelayedNeutrons(
            (TabulatedData) Sample.getData_entry(2),30f, delay);

  }

  private void setErrors( DataSet D){
    if( D == null)
      return;
    for( int i=0; i< D.getNum_entries(); i++){
      Data db = D.getData_entry(i);
      if( db.getErrors() == null)
         db.setSqrtErrors( true );

    }

  }
  public String getDocumentation(){
      StringBuffer Res = new StringBuffer();
      Res.append( "@overview - This class creates a transmission data set in");
      Res.append( " llambda and rebinned to match the corresponding histogram");
      Res.append( " when it is converted to llambda.");
      Res.append( "@algorithm  Each monitor data set has the delay neutron ");
      Res.append( "fraction subtracted from its entries.  Then the monitor ");
      Res.append( "data is rebinned to match the corresponding histogram ");
      Res.append( "bins. Next the ratio of the upstream to down stream ");
      Res.append( "monitors are calculated for each monitor data set. The ");
      Res.append( "resultant data set is the quotient of the sample ratio ");
      Res.append( "minus the cadmium ratio  divided by the Empty ratio minus ");
      Res.append( "the cadmium ratio.  If polynomial fitting is chosen,");
      Res.append( "  it is done.");
      Res.append( "@param Sample -  The monitor data set for the sample run");
      Res.append( "@param Empty -  The monitor data set for the Empty run");
      Res.append( "@param Cadmium - The monitor data set for the Cadmium run");
      Res.append( " @param SampleDS - The Histogram data set for the sample run");
      Res.append( "@param useCadmium - true if cadmium run is to be used");
      Res.append( "@param Neutron_Delay -The delay factor or 0 if no delay is");
      Res.append( "needed.");
      Res.append( " @param polyfitIndx1 -  the start channel to be fit(in ");
      Res.append( "rebinned llambda). the first");
      Res.append( "                 channel is channel 0");
      Res.append( "@param  polyfitIndx2 - the last channed to be fit(in ");
      Res.append( "rebinned llambda).the first");
      Res.append( "                 channel is channel 0. This last channel is ");
      Res.append( "included.");
      Res.append( "@param  degree -  degree of the polynomial to fit or -1 if ");
      Res.append( "no fitting is desired");
      Res.append( "@param  weight  - use 1/sqrt|y| values for weights instead of 1.");
      Res.append( "@return  a Data Set of transmission ratios converted to llamda");
      Res.append( " and rebinned to match the corresponding sample histogram. ");

      Res.append( "The data is fitted to a polynomial if chosen.");
      Res.append( "@error Could not Convert Sample to Llamda");
      Res.append( "@error  See errors from DataSetDivide, Subtract etc.");
      Res.append( "@error Tranmission data is empty");
      Res.append( "@assumptions  The downstream monitor is 1 and the upstream");
      Res.append( "  is 3. There has to be three monitors included in each ");
      Res.append("    histogram data set");
      return Res.toString();

  
  }
  //Gives widths of time bins

  private void ReportToLog1( DataSet ds, int Mon0, int Mon1){
    
     log.append("    I          WIDTH-M1           WIDTH-M2\n");
     float initialPath1 = ((FloatAttribute)(ds.getData_entry(Mon0).
                        getAttribute( Attribute.INITIAL_PATH))).getFloatValue();

     float initialPath2 = ((FloatAttribute)(ds.getData_entry(Mon1).
                        getAttribute( Attribute.INITIAL_PATH))).getFloatValue();
     float[] lvals = ds.getData_entry(0).getX_scale().getXs();
     float[] xvals=cvrtToTime( lvals, initialPath1,ds.getData_entry(Mon0));
     float[] lvals1=ds.getData_entry(2).getX_scale().getXs();
     float[] xvals1 =cvrtToTime( lvals1, initialPath2,ds.getData_entry(Mon1));
     
     for( int i=0; i+1< xvals.length; i++){
        log.append(Format.integer( i+1.0,5));
        log.append( Format.doubleExp( (double)(xvals[i+1]-xvals[i]),19));
       
        log.append( Format.doubleExp( (double)(xvals1[i+1]-xvals1[i]),19));
        System.out.println(xvals[i]+","+xvals1[i]);
        log.append("\n");
     }
    log.append("\n\n");


  }

  private void ReportToLog2( DataSet ds, int Mon0, int Mon1){

   
    log.append("***********************************************\n");
    log.append(" LAMBDA    M1-pancake-sample   M2trans-BSMon-sample\n");
    
     float[] lvals = ds.getData_entry(Mon0).getX_scale().getXs();
     //float[] xvals=cvrtToTime( lvals, initialPath,ds.getData_entry(0));
     float[] lvals1=ds.getData_entry(Mon1).getX_scale().getXs();
     //float[] xvals1 =cvrtToTime( lvals1, initialPath,ds.getData_entry(2));
     float[] yvals1= ds.getData_entry(Mon0).getY_values();
     float[] yvals2 = ds.getData_entry(Mon1).getY_values();
     for( int i=0;i+1< lvals.length; i++){
        log.append(Format.real((lvals[i]+lvals[i+1])/2.0, 12,6));
        log.append(Format.integer( (double)yvals1[i],12));

        log.append(Format.integer( (double)yvals2[i],12));
       
        log.append("\n");
     }
    log.append("\n\n");
  }

  private double getTotCount( Data Db){
    double Res;
   return ((Float) (Db.getAttribute( Attribute.TOTAL_COUNT).getValue())).doubleValue();
  }
  private void ReportToLog3(DataSet Sample,DataSet Empty, DataSet Cadmium,
              int Mon0, int Mon1){
      int Mon3 =0;
      if( Mon0 ==0)Mon3 = 1;
      if( Mon1 ==0)Mon3 = 1;
      if( Mon0 ==1)if(Mon3==1)Mon3 = 2;
      if( Mon1 ==1)if(Mon3==1)Mon3 = 2;
      if( Mon0 ==2)if(Mon3==2)Mon3 = 3;
      if( Mon1 ==2)if(Mon3==2)Mon3 = 3;
      if( Mon0 ==3)if(Mon3==3)Mon3 = 4;
      if( Mon1 ==3)if(Mon3==3)Mon3 = 4;
      int Monx = -1;
      if( Sample.getData_entry(Mon3).getX_scale().getNum_x() == 
               Sample.getData_entry(Mon1).getX_scale().getNum_x())
          Monx =Mon3;
       if( Monx ==Mon3){
          if( Mon3==Monx)Mon3++;
          if( Mon0 == Mon3) Mon3++;
          if( Mon3==Monx)Mon3++;
          if( Mon1 == Mon3) Mon3++;
          if( Mon3==Monx)Mon3++;
          if( Mon0 == Mon3) Mon3++;
          
          if( Mon1 == Mon3) Mon3++;
        

       }
      log.append("         UPSTREAM MON      Beamstop MON             PROTONS\n");
      log.append(" SAMPLE  = "+Format.integer(getTotCount(Sample.getData_entry(Mon0)),10));
      log.append( Format.integer(getTotCount(Sample.getData_entry(Mon1)),20));
      
      log.append( Format.integer(getTotCount(Sample.getData_entry(1)),20)+"\n");


      log.append(" EMPTY   = "+Format.integer(getTotCount(Empty.getData_entry(Mon0)),10));
      log.append( Format.integer(getTotCount(Empty.getData_entry(Mon1)),20));
      
      log.append( Format.integer(getTotCount(Empty.getData_entry(1)),20)+"\n");

      if( Cadmium == null)
         return;
      log.append(" CADMIUM = "+Format.integer(getTotCount(Cadmium.getData_entry(Mon0)),10));
      log.append( Format.integer(getTotCount(Cadmium.getData_entry(Mon1)),20));
      
      log.append( Format.integer(getTotCount(Cadmium.getData_entry(1)),20)+"\n");
      

  }
  private float[] cvrtToTime( float[] lvals1, float initialPath, Data db){
    DetectorPosition dp =(DetectorPosition)
                  db.getAttributeValue(Attribute.DETECTOR_POS);
    float L2 = dp.getCartesianCoords()[0];
    float[] xvals = new float[ lvals1.length];
    for( int i = 0; i< xvals.length; i++)
      xvals[i] = tof_calc.TOFofWavelength( L2+initialPath, lvals1[i]);
    return xvals;
    
  }
 public static int[] setMonitorInd( DataSet ds){
    int[] Res = new int[2];
    Res[0] = 0;
    Data D;
    int nchannels = ds.getData_entry(0).getX_scale().getNum_x();
    for( int i= 0; i< ds.getNum_entries(); i++)
      if( i != Res[0]){
        D = ds.getData_entry( i );
        if( D.getX_scale().getNum_x() == nchannels)
          if(((Number)( D.getAttributeValue( Attribute.TOTAL_COUNT)))
                 .floatValue() > 0){
           Res[1] = i;
           return Res;
         }
     }
    
      
    return null;
    }//setMonitorInd

 

}//CalcTransmisson
