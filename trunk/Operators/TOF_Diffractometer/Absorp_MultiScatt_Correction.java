/*
 * File:  Absorp_MultiScatt_Correction.java
 *
 * Copyright (C) 2004, Alok Chatterjee
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 */
package Operators.TOF_Diffractometer;

import java.util.Vector;
import java.io.*;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.operator.Parameter;
import DataSetTools.operator.Generic.TOF_Diffractometer.*;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.viewer.IViewManager;
import DataSetTools.viewer.ViewManager;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;

/**
  *  This operator produces the vanadium parameter file required by GSAS 
  *  for GPPD runs
  */
public class Absorp_MultiScatt_Correction extends GenericTOF_Diffractometer
 {
    private static final String TITLE = "Abs_MS_Correction";
    private OutputStreamWriter outStream;
    float   CONVER   = 0.0039554f;
    float   COEFF4   = 1.1967f;
    float   COEFF5   = -0.8667f;

 /* float COEFF1   = 2.800f;
    float COEFF2   = 0.0721f;
    float COEFF3   = 4.930f;
    float FLTP     = 26.50000f;
 */

    private boolean debug = true;

    double [] C = {0.730284,-0.249987,0.019448,-0.000006,0.000249,-0.000004,
                 0.848859,-0.452690,0.056557,-0.000009,0.000000,-0.000006,
                 1.133129,-0.749962,0.118245,-0.000018,-0.001345,-0.000012,
       		 1.641112,-1.241639,0.226247,-0.000045,-0.004821,-0.000030,
       		 0.848859,-0.452690,0.056557,-0.000009,0.000000,-0.000006,
                 1.000006,-0.821100,0.166645,-0.012096,0.000008,-0.000126,
                 1.358113,-1.358076,0.348199,-0.038817,0.000022,-0.000021,
                 0.0,0.0,0.0,0.0,0.0,0.0,
                 1.133129,-0.749962,0.118245,-0.000018,-0.001345,-0.000012,
                 1.358113,-1.358076,0.348199,-0.038817,0.000022,-0.000021,
                 0.0,0.0,0.0,0.0,0.0,0.0,
                 0.0,0.0,0.0,0.0,0.0,0.0,
                 1.641112,-1.241639,0.226247,-0.000045,-0.004821,-0.000030,
                 0.0,0.0,0.0,0.0,0.0,0.0,
                 0.0,0.0,0.0,0.0,0.0,0.0,
                 0.0,0.0,0.0,0.0,0.0,0.0};

    double [] Z = {1.0,0.8488263632,1.0,1.358122181,2.0,3.104279270,
                       0.8488263632,0.0,0.0,0.0,0.0,0.0,
                       1.0,0.0,0.0,0.0,0.0,0.0,
                       1.358122181,0.0,0.0,0.0,0.0,0.0,
                       2.0,0.0,0.0,0.0,0.0,0.0,
                       3.104279270,0.0,0.0,0.0,0.0,0.0};

    /** 
     *  Creates operator with title "Abs_MS_Correction" and a default list of
     *  parameters.
     */  
    public Absorp_MultiScatt_Correction()
    {
        super( TITLE );
    }

    /** 
     *  Construct a Absorp_MultiScatt_Correction operator .
     *
     *  @param  van_ds    Vanadium DataSet 
     *  @param  angle_deg The scattering angle in degrees
     *  @param  rad       Radius of Vanadium Cylinder (cm)
     *  @param  COEFF1 	  Absorption Cross Section/1.81
     *  @param  COEFF2    Number Density of Compound
     *  @param  COEFF3    Scattering Cross Section
     *  @param  FLTP      Total Flight Path(L1+L2) 
     */
    public Absorp_MultiScatt_Correction(  DataSet van_ds, 
                                          float   angle_deg, 
                                          float   rad, 
                                          float   COEFF1, 
                                          float   COEFF2, 
                                          float   COEFF3, 
                                          float   FLTP )
   {
     this(); 
     parameters = new Vector();

     addParameter( new Parameter("Vanadium DataSet parameter", van_ds) );

     addParameter( new Parameter("New Angle(degrees)", new Float(angle_deg) ) );

     addParameter( new Parameter("New Radius(rad)", new Float(rad) ) );

     addParameter( new Parameter("New COEFF1", new Float(COEFF1) ) );
     addParameter( new Parameter("New COEFF2", new Float(COEFF2) ) );
     addParameter( new Parameter("New COEFF3", new Float(COEFF3) ) );
     addParameter( new Parameter("New FLTP", new Float(FLTP) ) );
  }

 /* ---------------------- ZSet ------------------------------- */
  /**
   * Evaluate the Z function for a give theta.
   *
   *  @param   angle_deg    the angle at which Z is to be evaluated.
   *
   *  @return  the summed value of the function Z(angle_deg).
   */
  public double[] ZSet( float angle_deg )
  {
  	                     // angle_deg is "two theata" in degrees
  	                     // new_theta is theta in radians
     double new_theta = (double)(angle_deg * Math.PI / 360 );
     int l, J;

     double sum;
     for(int i = 1; i<=4; i++)
     {
      for(int j = 1; j<=4; j++)  
      {
        int iplusj = i+j;
        if(iplusj <= 5)
        {
          l=0;
          J = 1+l+6*(i-1)+6*4*(j-1);
          sum = C[J-1];

          for(l =1; l<=5; l++)
            {
             J = 1+l+6*(i-1)+6*4*(j-1);
             sum = sum +C[J-1]*java.lang.Math.cos(l*new_theta);
             }
             J = 1+i+6*j;
             Z[J-1] = sum;      
        }
       }
     } 
/*
     for(int j = 0; j<=35; j++)  
       System.out.println("Z's = " +j+','+Z[j]);
*/
     return Z;
  }

    
 /* ---------------------- AttFac ------------------------------- */
 /**
  * Evaluate the AttFac function for a given sigir and sigsr.
  *  
  *  @param  sigir    one of the inputs to Attfac.
  *  @param  sigsr    one of the inputs to Attfac.
  *
  *  @return  the Attentuation factor.
  */
  public double AttFac(float sigir, float sigsr)
  {
    double facti = 1.0;
    double Att   = 0.0;
    
    for( int i = 0; i <= 5; i++ )
    {
       double facts = 1.0;
       for(int j = 0; j<=5; j++)
       {
          int iplusj = i+j;
          if ( iplusj <= 5 )
          {
            int J = 1+i+6*j;
            Att = Att + Z[J-1] * facts * facti;
            facts = -facts * sigsr / (j+1) ;
          }
       }
      facti = -facti * sigir / (i+1);
    }

 // System.out.println("Att : "+Att);
    return Att;
  }
    
  /* ---------------------------- getDocumentation -------------------------- */
 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator produces the vanadium parameter file ");
    Res.append("required by GSAS for GPPD runs ");

    return Res.toString();
  }
  
 /* ---------------------------- getCommand ------------------------------- */     
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return "Abs_MScatt_Correction", the command used to invoke this
  * operator in Scripts
  */
  public String getCommand()
  {
    return "Abs_MScatt_Correct";
  }

 /** 
  * Sets default values for the parameters.  This must match the
  * data types of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();

    addParameter( new Parameter("Vanadium DataSet parameter",
                                 DataSet.EMPTY_DATA_SET) );

    addParameter( new Parameter("New Angle(degrees)", new Float(147.86) ) );
    addParameter( new Parameter("New Radius(cm)", new Float(0.3175) ) );
    addParameter( new Parameter("New COEFF1", new Float(2.8) ) );
    addParameter( new Parameter("New COEFF2", new Float(0.072) ) );
    addParameter( new Parameter("New COEFF3", new Float(4.93) ) );
    addParameter( new Parameter("New FLTP", new Float(26.5) ) );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return If successful, this operator produces a vanadium
  *          parameter file
  */
  public Object getResult()
  {
    DataSet van_ds    =  (DataSet)(getParameter(0).getValue());
    float   angle_deg = ((Float)(getParameter(1).getValue())).floatValue();
    float   rad       = ((Float)(getParameter(2).getValue())).floatValue();
    float   COEFF1    = ((Float)(getParameter(3).getValue())).floatValue();
    float   COEFF2    = ((Float)(getParameter(4).getValue())).floatValue();
    float   COEFF3    = ((Float)(getParameter(5).getValue())).floatValue();
    float   FLTP      = ((Float)(getParameter(6).getValue())).floatValue();

    float Q      = 0.0039554f / FLTP;
    float Q2     = COEFF1 * COEFF2;
    float sigsct = COEFF2 * COEFF3;

    Data     data    = van_ds.getData_entry(0);
    float[]  x1_vals = data.getX_scale().getXs(); 
    float    min     = x1_vals[0], max = x1_vals[ x1_vals.length-1];
    double[] Z       = ZSet(angle_deg);

    System.out.println("AttFac(1,1) = " + AttFac(1,1) );

    DataSet copy_ds = (DataSet)van_ds.clone();
    DiffractometerTofToWavelength op =
                        new DiffractometerTofToWavelength(van_ds, min, max, 0);
    van_ds = (DataSet)op.getResult();

    int num_data = van_ds.getNum_entries(); 
    for ( int i = 0; i < num_data; i++ )
    {
      float delta,
            sigabs;
      data           = van_ds.getData_entry(i);
      float[] x_vals = data.getX_scale().getXs();
      float[] y_vals = copy_ds.getData_entry(i).getY_values();
      double  deltp;
      double  temp;
           
      for ( int j = 0; j < x_vals.length-1; j++ )
      {   
        sigabs = Q2*(x_vals[j] + x_vals[j+1])/2;  // use midpoint of x interval
        float sigir = (sigabs + sigsct)*rad;
        float sigsr = sigir;
        temp = AttFac(sigir, sigsr);
         
        delta = COEFF4*sigir+COEFF5*sigir*sigir;
        deltp = (delta*sigsct)/(sigsct+sigabs) ;
        y_vals[j] =(float)((y_vals[j]*(1.0-deltp))/temp);
      }
    } 

    copy_ds.addLog_entry("Applied Abs_MS_Correction with parameters\n" +
                         "angle_deg : " + angle_deg +
                         "rad       : " + rad +
                         "COEFF1    : " + COEFF1 +
                         "COEFF2    : " + COEFF2 +
                         "COEFF3    : " + COEFF3 +
                         "FLTP      : " + FLTP );
    return copy_ds;
  }


 /* ------------------------------- main --------------------------------- */
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    System.out.println("Test of Abs_MS_Correction starting...");
    if ( args.length == 1 )
    {
                                                // load a DataSet
      String vfilename = args[0];

      RunfileRetriever rr1 = new RunfileRetriever( vfilename );
      DataSet van_ds = rr1.getDataSet(1);
                                               // make operator and call it
      Absorp_MultiScatt_Correction opb = 
          new Absorp_MultiScatt_Correction( van_ds, 148.0f, 0.3175f, 2.3207f,
                                             0.0511f, 5.025f, 26.5f );
      Object obj = opb.getResult();

      if ( obj instanceof DataSet )            // we got a DataSet back
      {                                        // so show it and original
        DataSet new_ds = (DataSet)obj;
        new ViewManager( van_ds, IViewManager.IMAGE );
        new ViewManager( new_ds, IViewManager.IMAGE );
      }
      else
        System.out.println( "Operator returned " + obj );
    }
    else
    {
      System.out.println("USAGE: Abs_MS_Correction <vfilename>");
    }
            
    System.out.println("Test of Abs_MS_Correction done.");
  }
}
