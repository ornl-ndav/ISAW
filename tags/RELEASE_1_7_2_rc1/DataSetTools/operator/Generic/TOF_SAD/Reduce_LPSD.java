/*
 * File:  Reduce_LPSD.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson, Alok Chatterjee and Dennis Mikkelson
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
 * Modified:
 *
 * $Log$
 * Revision 1.4  2005/11/23 17:22:41  hammonds
 * Make small changes to take out edit only differences between Reduce_KCL and Reduce_LPSD.
 * The only variation from this is to change the variable tofs in Reduce_LPSD to xvals in order to more closely match the syntax in Reduce_KCL therby reducing differences in the files.
 *
 * Revision 1.3  2004/06/02 15:41:22  rmikk
 * Added parameter(s) to specify monitor ID(s)
 *
 * Revision 1.2  2004/04/27 21:51:38  dennis
 * The Efficiency, sample and background transmission DataSets
 * are now interpolated to cover the needed range of wavelengths.
 *
 * Revision 1.1  2004/04/27 15:27:29  dennis
 * Initial version of Reduce_LPSD, adapted from Reduce_KCL.
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import gov.anl.ipns.Util.Numeric.*;

import java.util.Vector;

import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.operator.DataSet.Math.DataSet.*;


public class Reduce_LPSD  extends GenericTOF_SAD{

   public static final String Title = "Reduce_LPSD";

   public static final int DEFAULT_NEDGE = 1;  // mask off edge detectors 

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *  Default Constructor
    */
    public Reduce_LPSD()
    {
      super( Title );
    }

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *    Constructor for Reduce_LPSD.
    *    @param TransS    The sample Transmission data set
    *    @param TransB    The background Transmission data set
    *    @param  Eff      The Efficiency data set
    *    @param Sens      The sensitivity data set
    *    @param quV       The q bins if 1d or qxmin,qxmax, qymin, qymax
    *    @param RUNSds0   The monitor for the sample
    *    @param RUNSds1   The Histogram for the sample
    *    @param RUNBds0   The  monitor for the Background
    *    @param RUNBds1   The  Histogramfor the Background
    *    @param RUNCds0   null or the monitor for the Cadmium run
    *    @param RUNCds1   null or the  Histogram for the Cadmium run
    *    @param BETADN    The delayed neutron fraction
    *    @param SCALE     The scale factor to be applied to all data
    *    @param THICK     The sample thickness in m
    *    @param useTransB Use the background Transmission run
    *    @param upStreamMonID  Upstream monitor ID or -1
   */
   public Reduce_LPSD( DataSet TransS, 
                      DataSet TransB, 
                      DataSet Eff, 
                      DataSet Sens,
                      Vector  quV, 
                      DataSet RUNSds0, 
                      DataSet RUNSds1, 
                      DataSet RUNBds0, 
                      DataSet RUNBds1, 
                      DataSet RUNCds0,
                      DataSet RUNCds1, 
                      float   BETADN, 
                      float   SCALE, 
                      float   THICK,
                      boolean useTransB,
                      int upStreamMonID ) 
     {
        
        super( Title );
        parameters = new Vector();
        addParameter( new DataSetPG("Sample Transmission DS", TransS));
        addParameter( new DataSetPG("Background Transmission DS", TransB));
        addParameter( new DataSetPG("Efficiency Data Set", Eff));
        addParameter( new DataSetPG("Sensitivity Data Set", Sens));
        addParameter( new QbinsPG  ("Enter Q bins", quV));
        addParameter( new MonitorDataSetPG("Sample Monitor DS", RUNSds0));
        addParameter( new SampleDataSetPG ("Sample Histogram DS", RUNSds1));
        addParameter( new MonitorDataSetPG("Background Monitor DS", RUNBds0));
        addParameter( new SampleDataSetPG ("Backgrond Histogram DS", RUNBds1));
        addParameter( new MonitorDataSetPG("Cadmium Monitor DS", RUNCds0));
        addParameter( new SampleDataSetPG ("Cadmium Histogram DS", RUNCds1));
        addParameter( new FloatPG("Neutron Delay Fraction", new Float(BETADN)));
        addParameter( new FloatPG("Scale Factor", new Float(SCALE)));
        addParameter( new FloatPG("Thickness in m", new Float(THICK)));
        addParameter( new BooleanPG("Use Background Transmission Run?", 
				    new Boolean( useTransB)));

        addParameter( new IntegerPG("upStream Monitor ID", 
				    new Integer(upStreamMonID)));
      }


   /* ----------------------- setDefaultParameters  ------------------------ */

    public void setDefaultParameters()
    {
       parameters = new Vector();
       addParameter( new DataSetPG("Sample Transmission DS", null));
       addParameter( new DataSetPG("Background Transmission DS", null));
       addParameter( new DataSetPG("Efficiency Data Set", null));
       addParameter( new DataSetPG("Sensitivity Data Set", null));
       addParameter( new QbinsPG("Enter Q bins", null));
       addParameter( new MonitorDataSetPG("Sample Monitor DS", null));
       addParameter( new SampleDataSetPG ("Sample Histogram DS", null));
       addParameter( new MonitorDataSetPG("Background Monitor DS", null));
       addParameter( new SampleDataSetPG ("Backgrond Histogram DS", null));
       addParameter( new MonitorDataSetPG("Cadmium Monitor DS", 
                         DataSet.EMPTY_DATA_SET));
       addParameter( new SampleDataSetPG("Cadmium Histogram DS", 
                         DataSet.EMPTY_DATA_SET));
       addParameter( new FloatPG("Neutron Delay Fraction", new Float(.0011f)));
       addParameter( new FloatPG("Scale Factor",null));
       addParameter( new FloatPG("Thickness in m", null));
       addParameter( new BooleanPG("Use Background Transmission Run?", 
                                    new Boolean( true ) ) );
      addParameter( new IntegerPG("upStream Monitor ID", new Integer(-1)));
    }

  /* ---------------------------- getResult ------------------------------- */
    /**  
     *  Builds three DataSets containing the corrected scattering results
     *  for the sample, background and sample minus background, respectively.
     *  These DataSets are returned in a java Vector.   If either of the
     *  "size" parameters NQxBins or NQyBins are less than 1, the one
     *  dimensional S(Q) will be calculated and returned in the three
     *  DataSets.  If both NQxBins and NQyBins are at least 1, then the
     *  two dimensional S(Qx,Qy) will be calculated instead.
     *
     *  @return The vector of three DataSets will be returned if 
     *          no errors are encountered, otherwise an ErrorString
     *          will be returned. 
     */
     public Object getResult()
     {
        DataSet TransS=(DataSet)(getParameter(0).getValue());
        ((DataSetPG)getParameter(0)).clear();

        DataSet TransB=(DataSet)(getParameter(1).getValue());
        ((DataSetPG)getParameter(1)).clear();

        DataSet Eff=(DataSet)(getParameter(2).getValue());
        ((DataSetPG)getParameter(2)).clear();

        DataSet Sens=(DataSet)(getParameter(3).getValue());
        ((DataSetPG)getParameter(3)).clear();
        
        Vector Qu=(Vector)(getParameter(4).getValue());

        DataSet RUNSds0=(DataSet)(getParameter(5).getValue());
        ((DataSetPG)getParameter(5)).clear();

        DataSet RUNSds1=(DataSet)(getParameter(6).getValue());
        ((DataSetPG)getParameter(6)).clear();

        DataSet RUNBds0=(DataSet)(getParameter(7).getValue());
        ((DataSetPG)getParameter(7)).clear();

        DataSet RUNBds1=(DataSet)(getParameter(8).getValue());
        ((DataSetPG)getParameter(8)).clear();

        DataSet RUNCds0=(DataSet)(getParameter(9).getValue());
        ((DataSetPG)getParameter(9)).clear();

        DataSet RUNCds1=(DataSet)(getParameter(10).getValue());
        ((DataSetPG)getParameter(10)).clear();

        float   BETADN    = ((Float)(getParameter(11).getValue())).floatValue();
        float   SCALE     = ((Float)(getParameter(12).getValue())).floatValue();
        float   THICK     = ((Float)(getParameter(13).getValue())).floatValue();
        boolean useTransB = 
                        ((Boolean)(getParameter(14).getValue())).booleanValue();
        int upStreamMonID =((Integer)(getParameter(15).getValue())).intValue();
        SCALE = SCALE / THICK;

        DataSet[] RUNSds = new DataSet[2];
        DataSet[] RUNBds = new DataSet[2];
        DataSet[] RUNCds = new DataSet[2];
                                             // Record our list of DataSet AND
                                             // remove the area detector data
        RUNSds[0] = RUNSds0;
        RUNSds[1] = SAD_Util.RemoveAreaDetectorData( RUNSds1 );
        RUNBds[0] = RUNBds0;
        RUNBds[1] = SAD_Util.RemoveAreaDetectorData( RUNBds1 );
        if( RUNCds0 != null)
        {
          RUNCds[0] = RUNCds0;
          RUNCds[1] = SAD_Util.RemoveAreaDetectorData( RUNCds1 );
         }

        float qu[] = new float[ Qu.size() ];
        for( int i = 0; i < Qu.size(); i++)
          qu[i] = ((Number)Qu.elementAt(i)).floatValue();

        System.out.println("TransS info: ------------------------------------ ");
        System.out.println(" XScale = " + TransS.getData_entry(0).getX_scale() );
        System.out.println(" Ngroup = " + TransS.getNum_entries() );
        System.out.println("TransB info: ------------------------------------ ");
        System.out.println(" XScale = " + TransB.getData_entry(0).getX_scale() );
        System.out.println(" Ngroup = " + TransB.getNum_entries() );
        System.out.println("Eff info: ------------------------------------ ");
        System.out.println(" XScale = " + Eff.getData_entry(0).getX_scale() );
        System.out.println(" Ngroup = " + Eff.getNum_entries() );
        

        int MonitorInd[];        //set to contain the index of the upstream mon
        if( upStreamMonID <0)
           MonitorInd = CalcTransmission.setMonitorInd( RUNSds0 );
        else{
           MonitorInd = new int[1];
           MonitorInd[0] = RUNSds0.getIndex_of_data(
               RUNSds0.getData_entry_with_id(upStreamMonID));
        }

        DataSet ds_list[] = new DataSet[3];
        ds_list[0] = RUNSds0;
        ds_list[1] = RUNBds0;
        ds_list[2] = RUNCds0;
        int RunNums[] = SAD_Util.BuildRunNumList( ds_list );

        System.out.println("CERTAIN STARTING PARAMETERS ARE THE FOLLOWING");
        System.out.println();
        System.out.println(" DELAYED NEUTRON CORRECTION IS MADE =" );
        System.out.println(" THE DELAYED NEUTRON FRACTION =" + BETADN );
        System.out.println(" MGO FILTER IS IN THE BEAM " );

        for( int i = 0; i < 1; i++ )
        {
          tof_data_calc.SubtractDelayedNeutrons(
           (TabulatedData)RUNSds[0].getData_entry( MonitorInd[i]), 30f, BETADN);
          
          tof_data_calc.SubtractDelayedNeutrons(
           (TabulatedData)RUNBds[0].getData_entry( MonitorInd[i]), 30f, BETADN);
          
          if ( RUNCds0 != null )
            tof_data_calc.SubtractDelayedNeutrons(
           (TabulatedData)RUNCds[0].getData_entry( MonitorInd[i]), 30f, BETADN);
         }      

        for( int i = 0; i < RUNSds[1].getNum_entries(); i++ )
        {
          tof_data_calc.SubtractDelayedNeutrons(
                      (TabulatedData)RUNSds[1].getData_entry(i),30f, BETADN);
          
          tof_data_calc.SubtractDelayedNeutrons(
                      (TabulatedData)RUNBds[1].getData_entry(i),30f, BETADN);
          
          if ( RUNCds0 != null )
            tof_data_calc.SubtractDelayedNeutrons(
                      (TabulatedData) RUNCds[1].getData_entry(i),30f, BETADN);
         }

        int num_data = RUNSds[1].getNum_entries();
        float[] xvals = RUNSds[1].getData_entry(num_data/2).getX_scale().getXs();
        xvals = SAD_Util.ConvertXsToWL(xvals, RUNSds[1], num_data / 2, false);

        if( xvals[0] > xvals[1] ) 
          arrayUtil.Reverse( xvals );

        XScale xscl = new VariableXScale( xvals );
        System.out.println(" ReBinning to XScale " + xscl );

        SAD_Util.ConvertToWL( RUNSds[0], xscl, true );   // monitor
        SAD_Util.ConvertToWL( RUNSds[1], xscl, false );  // sample

        SAD_Util.ConvertToWL( RUNBds[0], xscl, true );   // monitor
        SAD_Util.ConvertToWL( RUNBds[1], xscl, false );  // background 

        SAD_Util.ConvertToWL( RUNCds[0], xscl, true );   // monitor
        SAD_Util.ConvertToWL( RUNCds[1], xscl, false );  // cadmium 
      
        int sensIndex[] = SAD_Util.BuildIndexOfID_Table( Sens );
        int cadIndex[]  = SAD_Util.BuildIndexOfID_Table( RUNCds[1] );

        Eff    = SAD_Util.InterpolateDataSet( Eff,    xscl );
        TransS = SAD_Util.InterpolateDataSet( TransS, xscl );
        TransB = SAD_Util.InterpolateDataSet( TransB, xscl );

        SAD_Util.CalcRatios( RUNSds, RUNCds, cadIndex, TransS, true, 
                             Eff, Sens, sensIndex, MonitorInd, SCALE );
        SAD_Util.CalcRatios( RUNBds, RUNCds, cadIndex, TransB, useTransB, 
                             Eff, Sens, sensIndex, MonitorInd, SCALE );
        
        DataSet SampSQ,
                BackSQ,
                DiffSQ;

        xscl = new VariableXScale(qu);
        SampSQ = SAD_Util.SumQs_1D( RUNSds, Eff, Sens, sensIndex, MonitorInd,
                                    xscl );
        BackSQ = SAD_Util.SumQs_1D( RUNBds, Eff, Sens, sensIndex, MonitorInd,
                                    xscl );
        DiffSQ = (DataSet)((new DataSetSubtract( SampSQ, BackSQ, true ))
                                                                .getResult());

        String titles[] = new String[3];
        titles[0] = "s";
        titles[1] = "b";
        titles[2] = "sn";

        Vector V = new Vector();
        V.addElement( SampSQ );
        V.addElement( BackSQ );
        V.addElement( DiffSQ );

        for ( int i = 0; i < 3; i++ )
        {
          DataSet SQds = (DataSet)V.elementAt(i);
          DataSetFactory.addOperators( SQds );
          SQds.addOperator( new GetPixelInfo_op() );
 
          Attribute attr = new IntListAttribute( Attribute.RUN_NUM, RunNums );
          SQds.setAttribute( attr );
          for ( int k = 0; k < SQds.getNum_entries(); k++ )
            SQds.getData_entry(k).setAttribute( attr );
          
          SQds.setTitle( titles[i] + RunNums[0] );
        }

        return V;
    }


    /**
     *  Get the multi-line documentation String for this operator.
     *
     *  @return the documentation String.
     */
    public String getDocumentation() {

      StringBuffer Res = new StringBuffer();
    

      return Res.toString();
}


  /* ----------------------------- Main ---------------------------------- */
    /**
     *   Main program for testing purposes
     */
    public static void main(String[] args) 
    {

    }

}
