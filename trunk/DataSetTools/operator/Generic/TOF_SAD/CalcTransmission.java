
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


public class CalcTransmission extends GenericTOF_SAD implements IObservable{
  StringBuffer log = new StringBuffer();
  
  public CalcTransmission(){
     super("Calculate Transmission Run");
  }

  public CalcTransmission( MonitorDataSet Sample, MonitorDataSet Empty, MonitorDataSet Cadmium,
                SampleDataSet SampleDS,
                boolean useCadmium,float Neutron_Delay, int polyfitIndx1, int polyfitIndx2,
                int degree){
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



  } 
  IObserver iobs = null;
  public void addIObserver(IObserver iobs){
    this.iobs = iobs;
  }
  public void deleteIObserver(IObserver iobs){
     this.iobs = null;
  }
  public void deleteIObservers(){
     this.iobs = null;
  }
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

  }
 
  public String getLogString(){
     return log.toString();

  }
 
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

   
     if( !useCadmium)
        Cadmium = null;
     //--------------- Neutron Delay
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

      XScale xscl = SampleDs.getData_entry(0).getX_scale();
      Sample.getData_entry(0).resample(xscl,IData.SMOOTH_NONE );
      Empty.getData_entry(0).resample(xscl,IData.SMOOTH_NONE);
      if( Cadmium != null)
         Cadmium.getData_entry(0).resample(xscl,IData.SMOOTH_NONE);
      Sample.getData_entry(2).resample(xscl,IData.SMOOTH_NONE);
      Empty.getData_entry(2).resample(xscl,IData.SMOOTH_NONE);
      if( Cadmium != null)
         Cadmium.getData_entry(2).resample(xscl,IData.SMOOTH_NONE);


    /*  //Debug "print"( send to ISAW)
        if(iobs != null){
          Sample.setTitle("Sample-Lambd-resampled");
          Empty.setTitle("Empty-Lambd-resampled");
          Cadmium.setTitle("Cadmium-Lambd-resampled");
          iobs.update( this, Sample.clone());
          iobs.update( this, Empty.clone());
          iobs.update( this, Cadmium.clone());

        }*/
   
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
    //iobs.update( this, tr.clone() );
    float[] xvals = xscl1.getXs();
    float[] yvals = tr.getData_entry(0).getY_values();
    double[] trunc_yvals = new double[ polyfitIndx2 - polyfitIndx1+1];
    double[] trunc_xvals = new double[ polyfitIndx2 - polyfitIndx1+1];
    for( int ii=polyfitIndx1 ; ii<polyfitIndx2; ii++){
       trunc_xvals[ii-polyfitIndx1]=(xvals[ii]+xvals[ii +1])/2.0;
       trunc_yvals[ii-polyfitIndx1] =(double) yvals[ii];
    }
     
    double[] coeff = new double[degree + 1];
    
    double errr=DataSetTools.math.CurveFit.Polynomial(trunc_xvals,trunc_yvals,coeff);
    for( int ii = 0; ii< yvals.length; ii++){
      yvals[ii]=0;
      float xx = (xvals[ii]+xvals[ii+1])/2f;
      for(int trm=0; trm <=degree; trm++)
        yvals[ii] +=coeff[trm]* Math.pow(xx,0.0+trm);
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
    /* if( Sample == null)
       return ;
     for( int i = 0; i < Sample.getNum_entries() ; i++){
        Data D = Sample.getData_entry( i );
        float[] yvals = D.getY_values();
        XScale xscl= D.getX_scale();
        float TotCnt = ((Float)(D.getAttribute(Attribute.TOTAL_COUNT).getValue())).intValue();
        float TotTime =(float)( 1E6)/30; // 30 is pulse frequency
        for( int j = 0; j < yvals.length -1; j++)
           yvals[j] -= delay*TotCnt*(xscl.getX(j+1)-xscl.getX(j))/TotTime;
     } 
    */
    tof_data_calc.SubtractDelayedNeutrons(
           (TabulatedData) Sample.getData_entry(0),30f, delay);
    
    tof_data_calc.SubtractDelayedNeutrons(
            (TabulatedData) Sample.getData_entry(2),30f, delay);

  }

}//CalcTransmisson
