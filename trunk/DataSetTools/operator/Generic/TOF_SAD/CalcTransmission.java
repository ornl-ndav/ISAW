
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
  *    @param  weight      use 1/|y| values for weights instead of 1.
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

     addParameter( new BooleanPG("Use 1/y weights", weight));



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
     DataSet Sample = ((MonitorDataSetPG)getParameter(0)).getDataSetValue();
     DataSet Empty = ((MonitorDataSetPG)getParameter(1)).getDataSetValue();
     DataSet Cadmium = ((MonitorDataSetPG)getParameter(2)).getDataSetValue();
     DataSet SampleDs =((SampleDataSetPG)getParameter(3)).getDataSetValue();
     boolean useCadmium = ((Boolean)(getParameter(4).getValue())).booleanValue();
     float NeutronDelay =((FloatPG)getParameter(5)).getfloatValue();
     int polyfitIndx1=((IntegerPG)getParameter(6)).getintValue();
     int polyfitIndx2=((IntegerPG)getParameter(7)).getintValue();
     int degree=((IntegerPG)getParameter(8)).getintValue();
     boolean weight = ((Boolean)(getParameter(9).getValue())).booleanValue();
   
     if( !useCadmium)
        Cadmium = null;
     //--------------- Neutron Delay
     Sample = (DataSet)(Sample.clone());
     Empty = (DataSet)(Empty.clone());
     Cadmium = (DataSet)(Cadmium.clone());
     if( NeutronDelay > 0){
       applyNeutronDelay( Sample, NeutronDelay );
       applyNeutronDelay( Empty, NeutronDelay );
       applyNeutronDelay( Cadmium, NeutronDelay );
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
      Sample.getData_entry(0).resample(xscl,IData.SMOOTH_NONE );
      Empty.getData_entry(0).resample(xscl,IData.SMOOTH_NONE);
      if( Cadmium != null)
         Cadmium.getData_entry(0).resample(xscl,IData.SMOOTH_NONE);
      Sample.getData_entry(2).resample(xscl,IData.SMOOTH_NONE);
      Empty.getData_entry(2).resample(xscl,IData.SMOOTH_NONE);
      if( Cadmium != null)
         Cadmium.getData_entry(2).resample(xscl,IData.SMOOTH_NONE);


    
     //------------ Calculate downstream monitor Relative monitors-------
     DataSet RelCadmium = null;
     Object Res = null;
     if(useCadmium){
       Res = ((new DataSetDivide_1( Cadmium, Cadmium, 1, true)).getResult());
       if( Res instanceof ErrorString)
          return Res;
       if( Res instanceof String)
          return new ErrorString((String) Res);
       RelCadmium = (DataSet) Res;
     }
     
     DataSet RelSamp;
     Res = (new DataSetDivide_1( Sample,Sample,1, true)).getResult();
     if( Res instanceof ErrorString)
        return Res;
     if( Res instanceof String)
          return new ErrorString( (String)Res);
     RelSamp = (DataSet) Res;
     DataSet RelEmpty;
     Res = (new DataSetDivide_1( Empty,Empty, 1, true)).getResult();
     if( Res instanceof ErrorString)
        return Res;
     if( Res instanceof String)
        return new ErrorString((String) Res);
     RelEmpty = (DataSet) Res;

   
     //----- Subtract off cadmium run ----------------------
     if( useCadmium){
       Res = (new DataSetSubtract_1( RelSamp, RelCadmium,3, true)).getResult();
       if( Res instanceof ErrorString )
          return Res;
       if( Res instanceof String)
          return new ErrorString( (String)Res);
       
       RelSamp =(DataSet)Res;

       Res = (new DataSetSubtract_1( RelEmpty, RelCadmium, 3,true)).getResult();
       if( Res instanceof ErrorString)
          return Res;
       if( Res instanceof String)
          return new ErrorString( (String)Res);
       RelEmpty =(DataSet)Res;
     }


     //----------------  Divide adjusted sample by ajusted empty------------
     Res = (new DataSetDivide_1( RelSamp, RelEmpty, 3,true)).getResult();
     if( Res instanceof ErrorString)
        return Res;
       if( Res instanceof String)
          return new ErrorString( (String)Res);
     DataSet Result_t = (DataSet) Res;
     Result_t.setTitle("tr_whole");
     Result_t.setSelectFlag(  2, true);
     
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
    if( polyfitIndx2 >= N)
       polyfitIndx2 = N-1;
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
    return tr;
    
     
    
  }

 //  Returns the Largest Run Number
 private int getRunNum( DataSet ds){
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
      Res.append( "@param  weight  - use 1/|y| values for weights instead of 1.");
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

}//CalcTransmisson
