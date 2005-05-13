/*
 * File:  MaskedReduce.java 
 *             
 * Copyright (C) 2005, Ruth Mikkelson, Alok Chatterjee and Dennis Mikkelson
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
 * Revision 1.3  2005/05/13 15:53:49  dennis
 * Fixed error in javadoc name for parameter.
 *
 * Revision 1.2  2005/05/13 15:15:24  dennis
 * Removed some redundant debug prints.
 *
 * Revision 1.1  2005/05/13 00:44:47  dennis
 * This version of Reduce_KCL includes an extra parameter giving a list
 * of channels to be omitted.
 *
 * Revision 1.35  2005/05/11 22:51:56  dennis
 * Minor improvement to documentation.
 *
 * Revision 1.34  2005/02/09 20:16:47  dennis
 * Changed title of operator for Menu system to "Reduce_KCL"
 * to match the command name and to distinguish it from the
 * "Reduce" script which loads a lot of files and then calls
 * the Reduce_KCL operator.
 *
 * Revision 1.33  2005/02/08 21:28:45  dennis
 * Removed excessive tabs from getDocumentation method.
 *
 * Revision 1.32  2004/08/02 21:13:18  rmikk
 * Removed unused imports
 *
 * Revision 1.31  2004/06/02 15:41:21  rmikk
 * Added parameter(s) to specify monitor ID(s)
 *
 * Revision 1.30  2004/05/25 12:38:51  rmikk
 * Added the getDocumentation Method
 *
 * Revision 1.29  2004/04/27 15:25:32  dennis
 * Removed "timer" code that was temporarily used for performance
 * testing.  Removed toVec() method used by main program... now uses
 * toVec() from SAD_Util().
 *
 * Revision 1.28  2004/04/26 19:01:42  dennis
 * Removed redundant log message.
 *
 * Revision 1.27  2004/04/26 18:57:41  dennis
 *   Major restructuring of previous "prototype" version.  All low level
 * routines are now static methods in the SAD_Util class, so that they
 * can be reused and maintained more easily.
 *
 * Revision 1.26  2004/04/22 20:33:53  dennis
 * Removed unused variables
 *
 * Revision 1.25  2004/04/21 20:45:33  dennis
 * Changed default value for beamstop size to 1.5, instead of 0.
 * This allows the new version to work with old scripts for
 * SAND.
 *
 * Revision 1.24  2004/04/21 19:42:28  dennis
 * Added beamstop size to the test code in the main method.
 *
 * Revision 1.23  2004/04/21 15:48:44  chatterjee
 * Fixed 2D case binning out-of-bounds error
 *
 * Revision 1.22  2004/04/21 14:27:24  chatterjee
 * Added a new parameter beam_stop dimension (bs_dim).
 * Scripts calling Reduce_KCL will need to provide this number in cms.
 * For SAND the value is 1.5 and for SASI 2.1.
 *
 * Revision 1.21  2004/04/08 15:17:11  dennis
 * Now uses "new" DataSetPGs consistently and calls clear() after getting the
 * value from the DataSetPG, to avoid memory leaks.
 * Replaced all parameters with new ParameterGUI's for consistency.
 * Removed calls to System.gc() to improve performance.
 * Renamed methods to Build2D_DS() and CalculateResults() to improve
 * readability.
 *
 * Revision 1.20  2004/03/19 20:28:16  dennis
 * Fixed output file name in 2D case, so it uses the
 * correct run number, not always 19990.
 * Added correct javadocs on getResult() method.
 *
 * Revision 1.19  2004/03/15 03:28:37  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.18  2004/01/24 20:05:19  bouzekc
 * Removed unused imports.
 *
 * Revision 1.17  2003/12/02 15:58:18  rmikk
 * Changed (Float) to (Number) to eliminate a Class Cast exception when
 *   dealing with Python
 *
 * Revision 1.16  2003/10/28 19:56:40  rmikk
 * Fixed javadoc error
 *
 * Revision 1.15  2003/10/22 20:21:48  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.14  2003/10/02 15:38:17  rmikk
 * Fixed an error when the background transmission run is
 *    being used.
 *
 * Revision 1.13  2003/10/02 14:40:52  rmikk
 * Eliminated? an array out of bounds error
 *
 * Revision 1.12  2003/09/18 15:55:35  rmikk
 * -Added a parameter useTransB to the END of the argument list
 *
 * Revision 1.11  2003/09/11 19:52:18  rmikk
 * Added GetField to the new DataSets
 *
 * Revision 1.10  2003/09/11 14:59:45  rmikk
 * -Included the GPL
 * -eliminated the DataSet.Add, Subtract, etc. methods and did
 *   several steps at one time.
 * -Reduced memory requirements by eliminating the declaration
 *  of variables to MaxSlice, etc.  and introducing a memory
 * efficient convert and rebin to one final xscale.
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

//import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;

import java.util.Vector;

import Command.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.operator.DataSet.Math.DataSet.*;
import gov.anl.ipns.Util.SpecialStrings.*;


public class MaskedReduce  extends GenericTOF_SAD{

   public static final String Title = "MaskedReduce";

   public static final int DEFAULT_NEDGE = 1;  // mask off edge detectors 

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *  Default Constructor
    */
    public MaskedReduce()
    {
      super( Title );
    }

   /* ---------------------------- Constructor  -------------------------- */
   /**
    *    Constructor for MaskedReduce.
    *    @param TransS    The sample Transmission data set
    *    @param TransB    The background Transmission data set
    *    @param Eff       The Efficiency data set
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
    *    @param XOFF      The Xoffset of beam from the center in meters
    *    @param YOFF      The Yoffset of beam from center in meters
    *    @param NQxBins   The number of Qx bins if 2D,otherwise use a neg number
    *    @param NQyBins   The number of Qx bins if 2D,otherwise use a neg number
    *    @param useTransB Use the background Transmission run
    *    @param bs_dim    New beam stop dimensions
    *    @param upStreamMonID  Upstream monitor ID or -1
    *    @param chanMaskStr    String specifying list of channel numbers to 
    *                          mask.
    */
   public MaskedReduce( DataSet TransS, 
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
                      float   XOFF, 
                      float   YOFF, 
                      int     NQxBins, 
                      int     NQyBins, 
                      boolean useTransB, 
                      float   bs_dim,
                      int     upStreamMonID,
                      String  chanMaskStr ) 
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
        addParameter( new FloatPG("X offset of beam in m", new Float(XOFF)));
        addParameter( new FloatPG("Y offset of beam in m", new Float(YOFF)));
        addParameter( new IntegerPG("# Qx bins", new Integer(NQxBins)));
        addParameter( new IntegerPG("# Qy bins", new Integer(NQyBins)));
        addParameter( new BooleanPG("Use Background Transmission Run?", 
                                     new Boolean( useTransB)));
        addParameter( new FloatPG("Beam Stop Size", new Float(bs_dim)));
        addParameter( new IntegerPG("upStream Monitor ID", 
                                     new Integer(upStreamMonID)) );
        addParameter( new Parameter( "Channels to Mask", 
                                      new IntListString( chanMaskStr ) ));
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
       addParameter( new FloatPG("X offset of beam in m", new Float(0)));
       addParameter( new FloatPG("Y offset of beam in m", new Float(0)));
       addParameter( new IntegerPG("# Qx bins", new Integer(-1)));
       addParameter( new IntegerPG("# Qy bins", new Integer(-1)));
       addParameter( new BooleanPG("Use Background Transmission Run?", 
                                    new Boolean( true ) ) );
       addParameter( new FloatPG("Beam Stop Size", 
                                  new Float(1.5f)));
       addParameter( new IntegerPG("upStream Monitor ID", new Integer(-1)));
       addParameter( new Parameter( "Channels to Mask", 
                                     new IntListString("-1") ));
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
        float   XOFF      = ((Float)(getParameter(14).getValue())).floatValue();
        float   YOFF      = ((Float)(getParameter(15).getValue())).floatValue();
        int     NQxBins   = ((Integer)(getParameter(16).getValue())).intValue();
        int     NQyBins   = ((Integer)(getParameter(17).getValue())).intValue();
        boolean useTransB = 
                        ((Boolean)(getParameter(18).getValue())).booleanValue();
        float   bs_dim    = ((Float)(getParameter(19).getValue())).floatValue();
        int upStreamMonID =((Integer)(getParameter(20).getValue())).intValue();
        SCALE = SCALE / THICK;

        String  chanMaskStr = 
                      ((IntListString)getParameter(21).getValue()).toString(); 
        int[] chanMaskNums = IntList.ToArray( chanMaskStr );

        float Radmin = bs_dim/100;
        float Radmax = 100.0f / 100; 
        int   nedge  = DEFAULT_NEDGE;

        DataSet[] RUNSds = new DataSet[2];
        DataSet[] RUNBds = new DataSet[2];
        DataSet[] RUNCds = new DataSet[2];

        RUNSds[0] = RUNSds0;
        RUNSds[1] = RUNSds1;
        RUNBds[0] = RUNBds0;
        RUNBds[1] = RUNBds1;
        if( RUNCds0 != null)
        {
          RUNCds[0] = RUNCds0;
          RUNCds[1] = RUNCds1;
         }

        float qu[] = new float[ Qu.size() ];
        for( int i=0; i < Qu.size(); i++)
          qu[i] = ((Number)Qu.elementAt(i)).floatValue();
        
        boolean make_2D;
        if ((NQxBins < 1) || (NQyBins < 1))
            make_2D = false;
        else
            make_2D = true;

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
        System.out.println(" BEAM STOP RADIUS IN CM  =" + Radmin );
        System.out.println(" DELAYED NEUTRON CORRECTION IS MADE =" );
        System.out.println(" THE DELAYED NEUTRON FRACTION =" + BETADN );
        System.out.println(" MGO FILTER IS IN THE BEAM " );
        System.out.println(" Number of X and Y edge Chans masked for AD=" + 
                             nedge );

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
        float[] xvals= RUNSds[1].getData_entry(num_data/2).getX_scale().getXs();

        xvals = SAD_Util.ConvertXsToWL(xvals, RUNSds[1], num_data / 2, false);
        if( xvals[0] > xvals[1] ) 
          arrayUtil.Reverse( xvals );
                                                         // make mask array
        boolean[] use_chan = new boolean[ xvals.length - 1 ];
        for ( int i = 0; i < use_chan.length; i++ )
          use_chan[i] = true;
        int num_skipped = 0;
        for ( int i = 0; i < chanMaskNums.length; i++ )
        {
          int chan = chanMaskNums[i];
          if ( chan >= 0 & chan < use_chan.length )
            use_chan[ chan ] = false;
          num_skipped++;
        }
        System.out.println("Omitting " + num_skipped + " channels" );
        System.out.println( chanMaskStr );

        XScale xscl = new VariableXScale( xvals );
        
        SAD_Util.ConvertToWL( RUNSds[0], xscl, true );   // monitor
        SAD_Util.ConvertToWL( RUNSds[1], xscl, false );  // sample

        SAD_Util.ConvertToWL( RUNBds[0], xscl, true );   // monitor
        SAD_Util.ConvertToWL( RUNBds[1], xscl, false );  // background 

        SAD_Util.ConvertToWL( RUNCds[0], xscl, true );   // monitor
        SAD_Util.ConvertToWL( RUNCds[1], xscl, false );  // cadmium 
      
        float[] lambda = xscl.getXs();

        UniformGrid SampGrid = SAD_Util.SetUpGrid( RUNSds[1] );
        UniformGrid BackGrid = SAD_Util.SetUpGrid( RUNBds[1] );
        UniformGrid CadGrid  = SAD_Util.SetUpGrid( RUNCds[1] );
        UniformGrid SensGrid = SAD_Util.SetUpGrid( Sens );

        SAD_Util.FixGroupIDs( BackGrid, SampGrid );
        SAD_Util.FixGroupIDs( CadGrid,  SampGrid );
        SAD_Util.FixGroupIDs( SensGrid, SampGrid );

        int sensIndex[] = SAD_Util.BuildIndexOfID_Table( Sens );
        int cadIndex[]  = SAD_Util.BuildIndexOfID_Table( RUNCds[1] );

        SAD_Util.AdjustGrid( RUNSds[1], XOFF, YOFF );
        SAD_Util.AdjustGrid( RUNBds[1], XOFF, YOFF );

        SAD_Util.ZeroAreaDetSens( SensGrid, SampGrid, Radmin, Radmax, nedge );

        SAD_Util.CalcRatios( RUNSds, RUNCds, cadIndex, TransS, true, 
                             Eff, Sens, sensIndex, MonitorInd, SCALE );
        SAD_Util.CalcRatios( RUNBds, RUNCds, cadIndex, TransB, useTransB, 
                             Eff, Sens, sensIndex, MonitorInd, SCALE );
        
        DataSet SampSQ,
                BackSQ,
                DiffSQ;

        if ( make_2D ) 
        {
          CoordBounds bounds = SAD_Util.GetQRegion( SampGrid, lambda, qu );
          SampSQ = SAD_Util.SumQs_2D( RUNSds, Eff, Sens, sensIndex, MonitorInd,
                                      bounds, NQxBins, NQyBins, use_chan );
          BackSQ = SAD_Util.SumQs_2D( RUNBds, Eff, Sens, sensIndex, MonitorInd,
                                      bounds, NQxBins, NQyBins, use_chan );
          DiffSQ = SAD_Util.Build2D_Difference_DS( SampSQ, BackSQ  );
        }
        else
        {              
          xscl = new VariableXScale(qu);
          SampSQ = SAD_Util.SumQs_1D( RUNSds, Eff, Sens, sensIndex, MonitorInd,
                                      xscl, use_chan );
          BackSQ = SAD_Util.SumQs_1D( RUNBds, Eff, Sens, sensIndex, MonitorInd,
                                      xscl, use_chan );
          DiffSQ = (DataSet)((new DataSetSubtract( SampSQ, BackSQ, true ))
                                                                 .getResult());
        }

        String titles[] = new String[3];
        titles[0] = "s";
        titles[1] = "b";
        titles[2] = "sn";
        if ( make_2D )
          for ( int i = 0; i < 3; i++ )
            titles[i] += "2d";

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
    
      Res.append("@overview  SMALL ANGLE NEUTRON SCATTERING ANALYSIS ROUTINE ");
      Res.append("PRODUCE either A RADIALLY AVERAGED S(Q) VS Q ARRAY; ");
      Res.append("SN(RUN#).dat OR PRODUCING AN S(QX,QY) ARRAY. SN@D(RUN#).BIN");
      Res.append("mask of pixels to be used, based on a flood fill data set.");
 
      Res.append("@algorithm  First the Neutron Delay is applied to all ");
      Res.append("DataSets, and all Time channels are converted to a common ");
      Res.append("wavelength scale.  ");
      Res.append("Next the offset of the Beam center is applied. The ratios: ");
      Res.append("(Samp/MonSamp-Cadmium/MonCadm)/(TransSamp*sens*eff) and ");
      Res.append("(Back/MonBack-Cadmium/MonCadm)/(TransBack*sens*eff) are ");
      Res.append("applied.  ");
      Res.append("These are converted to Q(or Qx,Qy) and summed over the ");
      Res.append("given Q or (Qx,Qy) bins with weighting of ");
      Res.append("SampMonitor*sens*eff");
    
      Res.append("@param TransS The sample Transmission data set");
      Res.append("@param TransB The background Transmission data set");
      Res.append("@param  Eff   The Efficiency data set");
      Res.append("@param Sens   The sensitivity data set");
      Res.append("@param qu     The q bins if 1d or qxmin, qxmax, " );
      Res.append("              qymin, qymax, if 2d.");
      Res.append("@param RUNSds0   the monitor  for the sample");

      Res.append("@param RUNSds1  the Histogram  for the sample");
      Res.append("@param RUNBds0 the  monitor for the Background");
      Res.append("@param RUNBds1 the  Histogramfor the Background");
      Res.append("@param RUNCds0 null or the monitor for the  Cadmium run");
      Res.append("@param RUNCds1 null or the  Histogram for the  Cadmium run");
      Res.append("@param BETADN The delayed neutron fraction");
      Res.append("@param SCALE  The scale factor to be applied to all data");
      Res.append("@param  THICK The sample thickness in m");
      Res.append("@param  XOFF  The Xoffset of beam from the center in meters");
      Res.append("@param  YOFF  The Yoffset of beam from center in meters");
      Res.append("@param NQxBins  The number of Qx bins if 2D,otherwise use");
      Res.append(" a neg number");
      Res.append("@param NQyBins  The number of Qx bins if 2D,otherwise use a");
      Res.append(" neg number"); 
      Res.append("@param useTransB  Use the background Transmission run"); 
      Res.append("@param upStreamMonID  Upstream monitor ID or -1");
      Res.append("@return The vector of three DataSets, the Sample,");
      Res.append(" Background, and Samp-Back S(Q) or S(Qx,Qy)for 2D,");

      Res.append(" if no errors are encountered, otherwise an");
      Res.append("  ErrorString will be returned.");

      return Res.toString();
}
     

  /* ----------------------------- Main ---------------------------------- */
    /**
     *   Main program for testing purposes
     */
    public static void main(String[] args) 
    {
        IsawGUI.Util util = new IsawGUI.Util();

        DataSet[] RUNSds = null, RUNBds = null, RUNCds = null;
        DataSet[] TransS = null, TransB = null, Eff = null, Sens = null;
        float[] qu = new float[117];
        float BETADN, SCALE;

        BETADN = 0.0011f;
        SCALE = 843000f;
        //
        qu[0] = 0.0035f;
        for (int i = 1; i < 117; i++) {
            qu[i] = qu[i - 1] * 1.05f;
            //System.out.println("qu ....." +qu[i]);
        }
 
        String PATH_1 = "C:\\Argonne\\sand\\wrchen03\\";
        String PATH_2 = "C:\\ISAW\\DataSetTools\\operator\\Generic\\TOF_SAD\\";
        RUNSds = util.loadRunfile(PATH_1 + "sand19990.run");
        RUNBds = util.loadRunfile(PATH_1 + "sand19935.run");
        RUNCds = util.loadRunfile(PATH_1 + "sand19936.run");
        System.out.println("After loading runfiles, before loading isds");
        try {
            TransS = ScriptUtil.load(PATH_2 + "tr1999019934.isd");
            TransB = ScriptUtil.load(PATH_2 + "sr1993519934.isd");
            Eff = ScriptUtil.load(PATH_2 + "efr19452.isd");
            Sens = ScriptUtil.load("C:\\ISAW\\SampleRuns\\sens19878.isd");
        } catch (Exception sss) {
            System.out.println("Error:" + sss);
        }

        System.out.println("Before calling MaskedReducexxxxxxxxxxxxxxxxxxxxx");
        MaskedReduce reduce = new MaskedReduce( TransS[0], TransB[0],
                Eff[0], Sens[0], SAD_Util.toVec(qu), RUNSds[0], RUNSds[1],
                RUNBds[0], RUNBds[1], RUNCds[0], RUNCds[1], BETADN, SCALE, .1f,
                //     0f,0f);
                .000725f, .006909f, -200, -200, true, 1.5f, -1, "20:30" );
        Object O = reduce.getResult();
        //new float[]{-.5f,.5f,-.5f,.5f}
        System.out.println("Finished O=" + O);
        Vector V = (Vector) O;
        ScriptUtil.display(
             ((DataSet)(V.elementAt(0))).getAttributeValue(Attribute.RUN_NUM));
        ScriptUtil.display(V.elementAt(0));
        ScriptUtil.display(V.elementAt(1));
        ScriptUtil.display(V.elementAt(2));
    }

}
