/*
 * File:  SpectrometerDetectorNormalizationFactor.java   
 *
 * Copyright (C) 2000, Dongfeng Chen,
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.7  2002/11/27 23:21:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.6  2002/09/19 16:05:49  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.5  2002/07/29 21:44:02  dennis
 *  Now uses scattering angle, rather than angle PHI in plane
 *  for calculating multiple scattering corrections.  NOTE: This
 *  is not strictyly correct, but should be a useful approximation.
 *  Also, put debug printout in if (debug) statement.
 *
 *  Revision 1.4  2002/07/16 19:35:18  dennis
 *  SLABMS() now returns NaN if the angle PHI is too small.
 *  This is trapped in the calling code and cal_FF[j] is set
 *  to zero in that case.
 *
 *  Revision 1.3  2002/03/13 16:19:24  dennis
 *  Converted to new abstract Data class.
 *
 *  Revision 1.2  2002/03/05 19:28:07  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 20:58:33  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.Generic.Special;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
 * This operator calculates detector normalization factors for a direct 
 * geometry spectrometer based on the difference of vanadium run and 
 * background run DataSets.  The difference must first be converted to
 * energy loss.
 * 
 *  <p><b>Title:</b> Detector Normalization Factors 
 *
 *  <p><b>Command:</b> DetNormFac
 *
 *  <p><b>Usage:</b><ul>
 *    When used in a script, the parameters are as described in the
 *    documentation for the constructor, as listed below.
 *  </ul>
 *
 *  <p><b>Returns:</b><ul>
 *     This returns a new DataSet containing three Data blocks.  The first
 *     Data block has the experimentally determined efficiencies. The second
 *     has the calculated efficiencies based on Vineyard's approximation
 *     for multiple scattering ( J.R.D. Copley et al., Nucl. Instr. Method 
 *     107, 501(1973) ).  The third has the ratio of the calculated to 
 *     experimental efficiencies: cal_FF[j]/exp_FF[j]. If an error occurs, 
 *     this returns a message String.
 *   </ul>
 *
 *  @see DataSetTools.operator.DataSet.DataSetOperator
 *  @see DataSetTools.operator.Operator
 */

public class SpectrometerDetectorNormalizationFactor extends    GenericSpecial 
                                                     implements Serializable
{
  boolean debug = false;                // set true to enable some debug prints


  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public SpectrometerDetectorNormalizationFactor( )
  {
    super( "Detector Normalization Factors" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct operator to calculate detector normalization factors from 
   *  a difference between a vanadium run and a background run.  The
   *  result of the calculation is obtained by calling getResult().
   *
   *  @param  ds      This DataSet must contain the difference between
   *                  a vanadium run and a background run.  These DataSets
   *                  must have the same machine geometry and detector
   *                  grouping as the sample runs that the normalization
   *                  factors will be applied to. Also, the difference
   *                  must have been converted to energy loss.
   *
   *  @param theta    The angle between the normal to the sample slab
   *                  and the beam direction.
   *                      
   */

  public SpectrometerDetectorNormalizationFactor( DataSet ds, float theta )
  {
    this();

    IParameter parameter = getParameter(0);
    parameter.setValue( ds );

    parameter = getParameter(1);
    parameter.setValue( new Float( theta ) );
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: 
   *            in this case, DetNormFac
   */
   public String getCommand()
   {
     return "DetNormFac";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
     parameters = new Vector();  // must do this to create empty list of 
                                 // parameters

     Parameter parameter = new Parameter( 
                   "Vanadium Calib. Data, minus background (Energy Loss)",
                    DataSet.EMPTY_DATA_SET );
     addParameter( parameter );

     parameter = new Parameter( "Sample slab angle:", new Float(57) );
     addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
    final float  ECON   = 5.2276f*1000000,
                 LAMCON = 3.95541f/1000, 
                 WVCON  = 1.5885f*1000,
                 XKCON  = 0.086165f;
    
    float ELAM = ECON*LAMCON*LAMCON;
                                     // get the current data set
    DataSet ds  = (DataSet)(getParameter(0).getValue());
    float THETS = ((Float)(getParameter(1).getValue())).floatValue();
 
                                     // construct a new data set for the
                                     // normalization factors
    DataSetFactory factory = new DataSetFactory( 
                                 "Detector Normalizations from "+ds.getTitle(),
                                 "# of index",
                                 "Group ID Index",
                                 "arb. ",
                                 "Integrated Scattering Function" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Detector Normalization Factors" );

    // copy the attributes of the original data set
    new_ds.setAttributeList( ds.getAttributeList() );
   
    Data             data,
                     new_data;
    DetectorPosition position;
    float            energy_in;
    Float            energy_in_obj;
    float            y_vals[];              // y_values from one spectrum
    float            e_vals[];              // energy values at bin boundaries
                                            // calculated from tof bin bounds
    
    XScale           E_scale;
    float            spherical_coords[];
    float            scattering_angle;
    int              num_data = ds.getNum_entries();
    AttributeList    attr_list;
    float            cal_FF[] = new float[num_data]; 
    float            exp_FF[] = new float[num_data];
    float            fudge_FF[] = new float[num_data];
    

    for ( int j = 0; j < num_data; j++ )
    {
      data = ds.getData_entry( j );        // get reference to the data entry
      attr_list = data.getAttributeList();
                                           // get the detector position and
                                           // initial path length 
      position=(DetectorPosition)
                   attr_list.getAttributeValue(Attribute.DETECTOR_POS);

      energy_in_obj=(Float)
                      attr_list.getAttributeValue(Attribute.ENERGY_IN);

      if( position != null && energy_in_obj != null )
                                                       // has needed attributes 
      {                                                // so convert it to E
                                       // calculate energies at bin boundaries
        energy_in        = energy_in_obj.floatValue();
        float EI = energy_in;
  //    spherical_coords = position.getSphericalCoords();
        scattering_angle = position.getScatteringAngle();
        
        e_vals  = data.getX_scale().getXs();
        y_vals  = data.getCopyOfY_values();
        
        float EDUM = (float)(193.0f* Math.pow((energy_in/1000.0f), 0.36f));
        float EINTEG=EDUM;
        float DUM = -EINTEG;
        
        float sum = 0.0f;
        int numofe = e_vals.length;
        
       // System.out.print("EDUM EINTEG DUM numofe: " + EDUM +
       //                  "  " + EINTEG + "  " + DUM + " " + numofe);
       // System.out.print("spherical_coords =" + spherical_coords[0] + 
       //                  " " + spherical_coords[1]+ " "+spherical_coords[2]);
     
        if ( !data.isHistogram() )
          for ( int i = 0; i < numofe; i++ )
          {
            if (e_vals[i] >= -EINTEG && e_vals[i] <= EINTEG)
            {
              float EF = EI - e_vals[i];
              float LAMF = (float)Math.sqrt(ELAM/EF);
              float EF1 = 0;
              if (i >= numofe-1) 
                EF1 = EI-e_vals[i-1];
              else 
                EF1 = EI-e_vals[i+1];
              float LAMF1 = (float)Math.sqrt(ELAM/EF1);
              float DLAMF = LAMF1-LAMF;
              float HLAMF = 0.5f*DLAMF;
              float DEfudge =(float)( ELAM*(Math.pow((LAMF-HLAMF), -2.0f) -
                                            Math.pow((LAMF+HLAMF), -2.0f)));
              sum += DEfudge*y_vals[i]/1000;
            }
          }

        else                                   // use bin centers for histogram
          for ( int i = 0; i < numofe-1; i++ )
          {
            float e_ave = (e_vals[i] + e_vals[i+1])/2;
            if (e_ave >= -EINTEG && e_ave <= EINTEG)
            {
              float EF = EI - e_ave;
              float LAMF = (float)Math.sqrt(ELAM/EF);
              float EF1 = 0;
              float e_ave_1 = 0;
              if (i >= numofe-2) 
                e_ave_1 = (e_vals[i-1] + e_vals[i])/2;
              else                
                e_ave_1 = (e_vals[i+1] + e_vals[i+2])/2;
              EF1 = EI - e_ave_1;
              float LAMF1 = (float)Math.sqrt(ELAM/EF1);
              float DLAMF = LAMF1-LAMF;
              float HLAMF = 0.5f*DLAMF;
              float DEfudge =(float)( ELAM*(Math.pow((LAMF-HLAMF), -2.0f) -
                                            Math.pow((LAMF+HLAMF), -2.0f)));
              sum += DEfudge*y_vals[i]/1000;
            }
          }

//        float XMS = SLABMS(180.0f*spherical_coords[1] /
//                    (float)Math.PI, energy_in, THETS );
        float XMS = SLABMS(180.0f*scattering_angle /
                    (float)Math.PI, energy_in, THETS );
  
        if ( XMS == Float.NaN )            // 7/16/2002, D.M., use NaN to 
          cal_FF[j] = 0;                   // indicate invalid, and take 
        else                               // cal_FF = 0 in those cases
          cal_FF[j]= 1/XMS;

        /*                                 // older version, 1/XMS evaluates
        if (1/XMS>0.000001)                // to infinity in case XMS == 0, and
          cal_FF[j]= 1/XMS;                // so cal_FF[j] was set to infinity
        else
          cal_FF[j] = 0;
        */

        exp_FF[j]=sum;
        if( exp_FF[j] < 0.00001f || 1/XMS < 0.000001)
          fudge_FF[j]=0.0f;
        else 
          fudge_FF[j]=cal_FF[j]/exp_FF[j];

   //     System.out.print(j+"  "+(180.0f*spherical_coords[1]/(float)Math.PI)+
   //                       " "+cal_FF[j]+" "+exp_FF[j]+" "+fudge_FF[j]+"  \n");
      }
        
        if (j==(num_data-1))
        {
        Data ff_cal_data,ff_exp_data,ff_FF_data;    
        UniformXScale ff_scale = null;
        ff_scale = new UniformXScale(0, num_data-1,num_data);
        
        ff_exp_data = Data.getInstance( ff_scale, exp_FF, 1000 ); 
        ff_cal_data = Data.getInstance( ff_scale, cal_FF, 1001 ); 
        ff_FF_data  = Data.getInstance( ff_scale, fudge_FF, 1002 ); 
        
        new_ds.addData_entry( ff_exp_data );
        new_ds.addData_entry( ff_cal_data );      
        new_ds.addData_entry( ff_FF_data );      
        
        }
      
    }
//    ChopTools.chop_dataDrawer.drawgraphDataSet(new_ds);
    return new_ds;
  }  


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SpectrometerEvaluator Operator.  The list of
   * parameters is also copied.
   */

  public Object clone()
  {
    SpectrometerDetectorNormalizationFactor new_op = 
                         new SpectrometerDetectorNormalizationFactor( );

    new_op.CopyParametersFrom( this );

    return new_op;
 }



  private float SLABMS(float PHI, float energy, float THETS )
  {
    float ECON   = 5.2276f*1000000;
    float WVCON  = 1.5885f*1000;

    float XNI = (float)Math.sqrt(ECON/energy);
    float XKO = WVCON/XNI;

    int NALPHA = 50;
    int NMU  = 50;

    THETS = 90 - THETS;
    float T = 0.1575f;
    float SIGS = 5.20f;
    float SIGA = 5.08f;
    float RHON = 0.0170f;
    SIGS =SIGS*RHON;
    SIGA = RHON*SIGA*2200.0f*XNI/1000000.0f;
    float SIGR = SIGS +SIGA;
    float IRSW = THETS/(float)Math.abs(THETS);
    THETS=(float)Math.abs(THETS);
    float DW = 4.909865f/1000000.0f;

    float PI = 3.1415927f;
    float DALPHA = 2.0f*PI /NALPHA;
    float DMU = 2.0f/NMU;
    float DOM = DALPHA * DMU/4.0f/PI;
    float THETR =THETS *PI/180;
    float CT = (float)Math.cos(THETR);
    float ST = (float)Math.sin(THETR);
    float TP = T/ST;
    float EXPO = -SIGR*TP;
    float SCAT = SIGS/SIGR;
    float  XMS = 0;
/*  
    if ( Math.abs(PHI) < 0.01f )
    {
      System.out.println("Quit for samll PHI: PHI = " + PHI);
      return 1.0f;                           // 7/16/2002, D.M., just let the
    }                                        // calculation produce NaN and
*/                                           // trap NaN in the calling code
    float PHIR = PHI * PI/ 180.0f;
    float CP = (float)Math.cos(PHIR);
    float SP = (float)Math.sin(PHIR);
    float BETA = THETR - PI /2.0f + PHIR*IRSW;
    float CB = (float)Math.cos(BETA);
    float DEL = ST/CB;
    float ATT = (float)Math.exp(EXPO*DEL);

    if ( IRSW < 0) ATT =1;

  //System.out.println("SCAT = " + SCAT + " ATT = " + ATT + " EXPO = " + EXPO );
  //System.out.println("DEL = " + DEL + " DW = " +DW + " XKO = " + XKO +
  //                   " PHIR = " + PHIR );
    float FIRST = (float)(SCAT * ATT * ( 1.0f - Math.exp(EXPO * (1.0f-DEL)))/
                  ( 1.0f -DEL)* Math.exp(-4*DW*XKO*XKO*Math.sin(PHIR/2.0f) *
                  Math.sin(PHIR/2.0f) ));

    float EPS = 1.0f - DEL;
    float FAC = (float)( 1.0f - Math.exp(EXPO*EPS))/EPS;
    float SECOND;
    float ALPHA;
    float CA;
    float SA;
    float CMU;
    float CM;
    float SM;
    float EDW;
    float ITEST;
    float RHO;
    float EKSI;
    float SUB;
    float ETA;
  
    SECOND = 0;
    for ( int IAL = 1; IAL <= NALPHA; IAL++)
    {
      ALPHA = ( (float)IAL-0.5f) * DALPHA;
      CA = (float)Math.cos(ALPHA);
      SA = (float)Math.sin(ALPHA);

      // System.out.println("1.5.ALPHA CA SA : "+ALPHA+"  "+  CA +"  "+ SA);

      for (int IMU = 1; IMU <= NMU; IMU++)
      {
        CMU = NMU/2 + 0.5f;
        CM = ( (float)IMU - CMU )*DMU;
        SM = (float)Math.sqrt(1.0f -CM*CM);
        EDW = (float)Math.exp(-DW*4.0f*XKO*XKO*(1.0f-(CM + SP*SM*CA + CP*CM)
                                  /2.0f));
        ITEST = 1;
        RHO = SM * CA * CT/ST + CM;
        EKSI = 1.0f - 1.0f/RHO;
        SUB = 0;
        if ( Math.abs(RHO)<0.01f)  
          SECOND = SECOND + ( FAC - SUB) * EDW/ ( 1.0f - RHO* DEL);
        else
        {
          if ( RHO<0) ITEST = 0;
          ETA = ( 1.0f- RHO * DEL)/RHO;
          SUB = (float)( Math.exp(ITEST*EXPO*ETA)*
                        (1.0f-Math.exp(EXPO*EKSI))/EKSI);
          SECOND = SECOND + ( FAC - SUB) * EDW/ ( 1.0f - RHO* DEL);
        }

     }
   }

   SECOND = SECOND * SCAT * SCAT * ATT * DOM;
   // System.out.println("3. SECOND * SCAT * SCAT * ATT * DOM :"
   //           + SECOND +"  "+ SCAT +"  "+ ATT +"  "+DOM );

   float XINF = SECOND / (1.0f - SECOND / FIRST );
   float RATIO = SIGS * TP/ ( FIRST + XINF);

   //System.out.println("4. RATIO " +RATIO+ "   "+ SIGS + "   "+
   //            TP+ "   "+ FIRST+ "   "+  XINF);

   XMS = RATIO;

   if ( debug )
     System.out.println("5. MULT  SC  PHI = "  + PHI + " FIRST = " + FIRST +
                        " SECOND = " + SECOND + " XMS = " + XMS );
   return XMS;
  }
}
