/*
 * @(#)SpectrometerFudgeFactor.java   0.1  00/08/01   Dongfeng Chen 
 *                                                    Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.1  2000/08/02 20:16:25  dennis
 *  SpectrometerFudgeFactor operator to calibrate detectors based on
 *  a vanadium run.
 * 
 *   
 */

package DataSetTools.operator;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;

/**
 * This operator converts a neutron Energy Loss DataSet to Fudge Factor.
 * 
 */

public class SpectrometerFudgeFactor extends    DataSetOperator 
                                     implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public SpectrometerFudgeFactor( )
  {
    super( "Fudge Factor" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   */

  public SpectrometerFudgeFactor( DataSet     ds)
  {
    this();                         
    setDataSet( ds );               
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * Returns the abbreviated command string for this operator.
   */
   public String getCommand()
   {
     return "FF";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
     parameters = new Vector();  // must do this to create empty list of 
                                 // parameters
  }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
    final float  ECON = 5.2276f*1000000,LAMCON = 3.95541f/1000, WVCON = 1.5885f*1000,
                        XKCON = 0.086165f;
    
    float ELAM = ECON*LAMCON*LAMCON;
    
                                     
                                     // get the current data set
    DataSet ds = this.getDataSet();
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSetFactory factory = new DataSetFactory( 
                                     ds.getTitle(),
                                     "# of index",
                                     "Group ID Index",
                                     "arb. ",
                                     "Integrated Scattering Function" );

    // #### must take care of the operation log... this starts with it empty
    DataSet new_ds = factory.getDataSet(); 
    new_ds.copyOp_log( ds );
    new_ds.addLog_entry( "Fudge Factor" );

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

      if( position != null && energy_in_obj != null)
                                                       // has needed attributes 
      {                                                // so convert it to E
                                       // calculate energies at bin boundaries
        energy_in        = energy_in_obj.floatValue();
        float EI = energy_in;
        spherical_coords = position.getSphericalCoords();
        
        
        
        e_vals           = data.getX_scale().getXs();
        y_vals  = data.getCopyOfY_values();
        
        float EDUM = (float)(193.0f* Math.pow((energy_in/1000.0f), 0.36f));
        float EINTEG=EDUM;
        float DUM = -EINTEG;
        
        float sum = 0.0f;
        int numofe = e_vals.length;
        
       // System.out.print("EDUM EINTEG DUM numofe: "+EDUM+"  "+EINTEG+"  "+DUM+ " "+numofe);
       // System.out.print("spherical_coords ="+spherical_coords[0]+ " " +spherical_coords[1]+ " "+spherical_coords[2]);
       // pause(3000);
        
        spherical_coords = position.getSphericalCoords();
        
        loope:for ( int i = 0; i < (numofe-1); i++ )
        {
            
            if (e_vals[i+1]<DUM || e_vals[i+1]>EINTEG) continue loope;
            float EF=EI - e_vals[i+1];
            float LAMF = (float)Math.sqrt(ELAM/EF);
            float EF1 =0;
            //if (i>=numofe) EF1 = EI-e_vals[i];
            //else if (i<numofe) 
            EF1 = EI-e_vals[i+2];
            float LAMF1 = (float)Math.sqrt(ELAM/EF1);
            float DLAMF = LAMF1-LAMF;
            float HLAMF = 0.5f*DLAMF;
            float DEfudge =(float)( ELAM*(Math.pow((LAMF-HLAMF), -2.0f)-Math.pow((LAMF+HLAMF), -2.0f)));
            sum += DEfudge*y_vals[i+1]/1000;
          // System.out.print(e_vals[i]+" "+sum+"\n");
           // pause(300);
        }

        
        //*/
            
            float XMS = SLABMS(180.0f*spherical_coords[1]/(float)Math.PI, energy_in );
            if (1/XMS>0.000001) 
            cal_FF[j]= 1/XMS;
            exp_FF[j]=sum;
            if(exp_FF[j]<0.00001f||1/XMS<0.000001)
            {
                fudge_FF[j]=0.0f;
            }else 
            fudge_FF[j]=cal_FF[j]/exp_FF[j];

            System.out.print(j+"  "+(180.0f*spherical_coords[1]/(float)Math.PI)+" "+cal_FF[j]+" "+exp_FF[j]+" "+fudge_FF[j]+"  \n");
      }
        
        if (j==(num_data-1))
        {
        Data ff_cal_data,ff_exp_data,ff_FF_data;    
        UniformXScale ff_scale = null;
        ff_scale = new UniformXScale(0,num_data-1,num_data-1);
        
        ff_exp_data = new Data( ff_scale, exp_FF, 1000 ); 
        ff_cal_data = new Data( ff_scale, cal_FF, 1001 ); 
        ff_FF_data = new Data( ff_scale, fudge_FF, 1002 ); 
        
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
   * Get a copy of the current SpectrometerFudgeFactor Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    SpectrometerFudgeFactor new_op = new SpectrometerFudgeFactor( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

public static void pause(int time)
{ 
 System.out.print("Pause for "+time/1000 +" second! ");
  try{Thread.sleep(time);}catch(Exception e){}
    
}

    static float SLABMS(float PHI, float energy)
    {
      float ECON = 5.2276f*1000000,LAMCON = 3.95541f/1000, WVCON = 1.5885f*1000,
                        XKCON = 0.086165f;
        
        //float XKO = 7.6038f;//120meV
        //float XNI = 208.9078f;//120meV
        
        float XKO = 9.825f;//200meV
        float XNI = 161.672f;//200meV
       // p("XKO XNI ="+XKO+"  "+XNI);
        
        XNI = (float)Math.sqrt(ECON/energy);
        XKO = WVCON/XNI;
        
     //   p("XKO XNI ="+XKO+"  "+XNI);
      //  pause(1000);
        int NALPHA = 50;
        int NMU  = 50;
        float THETS = -45.0f;
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
        if ( Math.abs(PHI)<0.01f) 
        {
            System.out.println("Quit for samll PHI");
            return 0.0f;
        }
        
        float PHIR = PHI * PI/ 180.0f;
        float CP = (float)Math.cos(PHIR);
        float SP = (float)Math.sin(PHIR);
        float BETA = THETR - PI /2.0f + PHIR*IRSW;
        float CB = (float)Math.cos(BETA);
        float DEL = ST/CB;
        float ATT = (float)Math.exp(EXPO*DEL);
       // p("PHIR CP SP BETA CB DEL ATT :"+            PHIR+ "  "+ CP + "  "+ SP + "  "+ BETA + "  "+ CB+ "  "+ DEL + "  "+ ATT);
        
        if ( IRSW < 0) ATT =1;
        float FIRST = (float)(SCAT * ATT * ( 1.0f - Math.exp(EXPO * (1.0f-DEL)))
             /( 1.0f -DEL)* Math.exp(-4*DW*XKO*XKO*Math.sin(PHIR/2.0f)*Math.sin(PHIR/2.0f) ));
             
        float EPS = 1.0f - DEL;
        float FAC = (float)( 1.0f - Math.exp(EXPO*EPS))/EPS;
        float SECOND = 0.0f;
       // p("FIRST EPS FAC :"+FIRST+" "+EPS+" "+FAC);
      //  System.out.println("1. SECOND  = "+SECOND);
        float ALPHA = 0.0f;
        float CA = 0.0f;
        float SA = 0.0f;
        float CMU = 0.0f;
        float CM = 0.0f;
        float SM = 0.0f;
        float EDW = 0.0f;
        float ITEST = 0.0f;
        float RHO = 0.0f;
        float EKSI = 0.0f;
        float SUB = 0.0f;
        float ETA = 0.0f;
   
        for ( int IAL = 1; IAL <= NALPHA; IAL++)
        {
             ALPHA = ( (float)IAL-0.5f) * DALPHA;
             CA = (float)Math.cos(ALPHA);
             SA = (float)Math.sin(ALPHA);
            
           // System.out.println("1.5.ALPHA  CA SA : " +ALPHA+"  "+  CA +"  "+  SA  );
            
            for (int IMU = 1; IMU <= NMU; IMU++)
            {
                 CMU = NMU/2 + 0.5f;
                 CM = ( (float)IMU - CMU )*DMU;
                 SM = (float)Math.sqrt(1.0f -CM*CM);
                 EDW = (float)Math.exp(-DW*4.0f*XKO*XKO*(1.0f-(CM + SP*SM*CA + CP*CM)/2.0f));
                 ITEST = 1;
                 RHO = SM * CA * CT/ST + CM;
                 EKSI = 1.0f - 1.0f/RHO;
                 SUB = 0;
                if ( Math.abs(RHO)<0.01f)  SECOND = SECOND + ( FAC - SUB) * EDW/ ( 1.0f - RHO* DEL);
                else
                {
                    if ( RHO<0) ITEST = 0;
                    ETA = ( 1.0f- RHO * DEL)/RHO;
                    SUB = (float)( Math.exp(ITEST*EXPO*ETA)*(1.0f-Math.exp(EXPO*EKSI))/EKSI);
                    SECOND = SECOND + ( FAC - SUB) * EDW/ ( 1.0f - RHO* DEL);
                }
                
              //  System.out.println("2.IMU  SECOND + ( FAC - SUB) * EDW/ ( 1.0f - RHO* DEL):"  
               //           +IMU + "  "+ SECOND +"  "+ FAC+"  "+ SUB +"  "+EDW +"  "+RHO +"  "+DEL );
               
             //  try{Thread.sleep(5000);}catch(Exception e){}
                
                
            }
        }
        
        SECOND = SECOND * SCAT * SCAT * ATT * DOM;
       // System.out.println("3. SECOND * SCAT * SCAT * ATT * DOM :"  
        //           + SECOND +"  "+ SCAT +"  "+ ATT +"  "+DOM );
        
        float XINF = SECOND / (1.0f - SECOND / FIRST );
        float RATIO = SIGS * TP/ ( FIRST + XINF);
        
        //System.out.println("4. RATIO = SIGS * TP/ ( FIRST + XINF)" +RATIO+ "   "+ SIGS + "   "+ 
        //            TP+ "   "+ FIRST+ "   "+  XINF);
        
        XMS = RATIO;
        
       // System.out.println("5. MULT  SC  PHI = "  + PHI + " FIRST = " + FIRST + 
        //      " SECOND = " + SECOND + " XMS = " + XMS );
        
        return XMS;
    }
    

}
