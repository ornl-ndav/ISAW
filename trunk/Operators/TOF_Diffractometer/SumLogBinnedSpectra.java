/*
 * File:  SumLogBinnedSpectra.java 
 *        (Generic operator adapted from the corresponding DataSetOperator)
 * Copyright (C) 2005, John Hammonds
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
 * Contact : John Hammonds <mikkelsond@uwstout.edu>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *             
 *  $Log$
 *  Revision 1.2  2005/04/21 02:08:32  hammonds
 *  Fixed Chop of tail end of spectrum by taking out *2 and adding -1 on the calculation of the new number of channels.
 *
 *  Revision 1.1  2005/04/20 16:34:59  hammonds
 *  Operator added to Sum dt/t binned data into banks.
 *
 *
 */

package Operators.TOF_Diffractometer;

import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import  java.io.*;
import  java.lang.reflect.Array;
import  java.lang.*;
import  java.util.Vector;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.retriever.*;
import  DataSetTools.operator.*;
import  DataSetTools.operator.Generic.TOF_Diffractometer.*;
import  DataSetTools.parameter.*;
import  DataSetTools.viewer.*;

/**
  */

public class SumLogBinnedSpectra extends GenericTOF_Diffractometer
                                        implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   */

  public SumLogBinnedSpectra( )
  {
    super( "Focus & Sum Log Binned Spectra" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /** Use Map to map detectors into banks.  Banks are focused in the proccess
   *  @param  ds
   *
   *  @param  int[][] mapArray to map IDs->bank
   *
   *  @param  float[]  angArray  refence angles for detector banks
   *  @param  float[]  lenArray  refence length for detector banks
   *  @param  float[]  resArray  dt/t for detector banks
   */

  public SumLogBinnedSpectra( DataSet    ds,
                                         int[][]     vecArray, 
			      float[]     angArray,
			      float[]     lenArray,
			      float[]     resArray)
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    IParameter parameter = getParameter( 0 );
    parameter.setValue( ds );
   
    addParameter( new ArrayPG("ID->Bank Map", vecArray  ) );

    
    addParameter( new FloatArrayPG( "Focus Angle for Banks", angArray ) );
    addParameter( new FloatArrayPG( "Focus Length for Banks", lenArray ) );
    addParameter( new FloatArrayPG( "dt/t for Banks", resArray ) );

  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   * in this case, ScatFun
   */
   public String getCommand()
   {
     return "SumLog";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Sample Data", DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    int [][] vecArray;
    vecArray = new int[4][];
    vecArray[0] = new int[] {3,4};
    vecArray[1] = new int[] {4,6,8};
    vecArray[2] = new int[] {3,6,9,12};
    vecArray[3] = new int[] {4,8,12,16};
    
    
    addParameter( new ArrayPG("ID->Bank Map", vecArray  ) );

    Vector angArray = new Vector();
    angArray.add(new Float(140.0f));
    angArray.add(new Float(90.0f));
    angArray.add(new Float(44.0f));
    angArray.add(new Float(20.0f));
    
    addParameter( new FloatArrayPG( "Focus Angle for banks", angArray ) );
        Vector lenArray = new Vector();
    lenArray.add(new Float(1.5f));
    lenArray.add(new Float(1.5f));
    lenArray.add(new Float(1.5f));
    lenArray.add(new Float(1.5f));
    
    FloatArrayPG lenPG = new FloatArrayPG( "Focus Length for Banks", lenArray );
    addParameter( lenPG );
    Vector resArray = new Vector();
    resArray.add(new Float(0.0004f));
    resArray.add(new Float(0.0008f));
    resArray.add(new Float(0.0016f));
    resArray.add(new Float(0.0032f));
    addParameter( new FloatArrayPG( "dt/t for Bank", resArray ) );
    
  }

 /* ---------------------- getDocumentation --------------------------- */
 /**
 *  Returns the documentation for this method as a String.  The format
 *  follows standard JavaDoc conventions.
 */
 public String getDocumentation()
 {
   StringBuffer s = new StringBuffer("");
   s.append("@overview A DataSet is focussed and summed into banks.  Banks ");
   s.append("are defined by mapping detectors to a bank number.  Focussing ");
   s.append("lines up features(peaks) in different spectra to make them appear ");
   s.append("as though they are all coming from the same place.\n");
   s.append("@assumptions Calibration data in the DataSet is good enough to ");
   s.append("assure that conversions between time and d-spacing are correct.  ");
   s.append("Since data is binned in dt/t steps, diffraction peaks from ");
   s.append("detectors at different angles can be made to line up for adding ");
   s.append("if the spectra are simply shifted relative to each other by an ");
   s.append("integer number of time steps.\n");
   s.append("@algorithm Shift parameters are easily calculated by the using the ");
   s.append("formula shift = log(lsin-theta(actual)/lsin-theta(reference))/C ");
   s.append("where C is the dt/t constant used in binning the data.  A constant ");
   s.append("reference angle is used for each detector in a bank.  Once shift ");
   s.append("constants are calculated and overall shift is calculated since ");
   s.append("detectors will shift both ways with respect to a reference angle ");
   s.append("in the middle of the bank.  Once shifts are calculated there will ");
   s.append("be a number of channels where only some of the spectra will ");
   s.append("not contribute to the sum.  These channels are trimmed from the ");
   s.append("ends as the specta are added.  The number of channels to be ");
   s.append("trimmed from each end can be calculated by taking the difference ");
   s.append("between the maximum shift for the bank and the shift for the ");
   s.append("individual detector.  This gives a starting point for data in this");
   s.append("spectrum that contributes to the sum.  The number of points that ");
   s.append("will contribute to the sum is given by numChannels(orig) - ");
   s.append("(maxShift for bank - min Shift for bank).\n");
   s.append("@param ds The sample DataSet for which the focused sum ");
   s.append("is to be calculated.\n");
   s.append("@param mapArray - a 2D array used to map groupIDs in the dataset ");
   s.append("to banks.  Detectors in each bank will be focused and then summed\n");
   s.append("@param refAngle - a 1D array of floats that contains 1 reference ");
   s.append("angle for each bank.  The bank is focused to this angle\n");
   s.append("@param refLength - a 1D array of floats that contains 1 reference ");
   s.append("secondary flight path length for each bank.  The bank is focused ");
   s.append("to this angle\n");
   s.append("@param resArray - a 1D array of floats that contains 1 resolution ");
   s.append("constant for each bank.  This constant should be the same as the ");
   s.append("dt/t constant used for the time bins for this data\n");

   s.append("@return \n");
   s.append("@error \n");
   return s.toString();
 }
  
  /* ---------------------------- getResult ------------------------------- */
  /** 
    *  Calculates the scattering function for the input DataSet ds.
    *
    *  @return A DataSet containing the focused/summed banks
    */

  public Object getResult()
  {       
    DataSet ds         = (DataSet)(getParameter(0).getValue());
    Vector  mapArray   = (Vector)(getParameter(1).getValue());
    Vector  refAngle   = (Vector)getParameter(2).getValue();
    Vector  refLength   = (Vector)getParameter(3).getValue();
    Vector  resArray   = (Vector)getParameter(4).getValue();
    
    if (mapArray.size() != refAngle.size() ) {
      return new ErrorString( "SumLog: #of banks in map must = #of ref angles " + mapArray.size() + ", " + refAngle.size() + "\n");
    }

    int[][] bankMap = new int[mapArray.size()][];
    // Sort the ID->bank Map into an int[][]
    for (int ii =0; ii< mapArray.size(); ii++) {
      Vector tempVect = (Vector)mapArray.elementAt(ii);
      bankMap[ii] = new int[ tempVect.size()];
      for (int jj = 0; jj < tempVect.size(); jj++ ) { 	
	try {
	  bankMap[ii][jj] = ((Integer)tempVect.elementAt(jj)).intValue();
	}
	catch (ClassCastException ex) {
	  return new ErrorString( "SumLog: Problem with map Array @index (" + ii 
				  + ", " + jj + ")\n");
	}
      }
    }
    //sort the reference angles into a float array
    float[] refAng = new float[refAngle.size()];
    for (int ii =0; ii< refAngle.size(); ii++) {
      try {
	Float tempFloat = (Float)(refAngle.elementAt(ii));
	refAng[ii] = tempFloat.floatValue();
      }
      catch (ClassCastException ex) {
	return new ErrorString( "SumLog - Problem with ref angle array @index (" 
				+ ii + ")\n");
      }
    }

    //sort the reference lengths into a float array
    float[] refLen = new float[refLength.size()];
    for (int ii =0; ii< refLength.size(); ii++) {
      try {
	Float tempFloat = (Float)(refLength.elementAt(ii));
	refLen[ii] = tempFloat.floatValue();
      }
      catch (ClassCastException ex) {
	return new ErrorString( "SumLog - Problem with ref length array @index (" 
				+ ii + ")\n");
      }
    }

    //sort the bank resolution into a float array
    float[] bankRes = new float[refAngle.size()];
    for (int ii =0; ii< refAngle.size(); ii++) {
      try {
	Float tempFloat = (Float)(resArray.elementAt(ii));
	bankRes[ii] = tempFloat.floatValue();
      }
      catch (ClassCastException ex) {
	return new ErrorString( "SumLog - Problem with resolution array @index (" 
				+ ii + ")\n");
      }
    }

    //Set up a dummy dataset to place results in
    DataSet newDataSet = (DataSet)ds.empty_clone();
    //Separate into groups and then process the sum
    for (int ii=0; ii <refAng.length ; ii++ ) {
      //      System.out.println("Bank: " + (ii+1));
      Data[] bankData = new Data[bankMap[ii].length];
      float[] angle = new float[bankMap[ii].length];
      float[] fp = new float[bankMap[ii].length];
      float[] times = new float[0];
      int[] shift = new int[bankMap[ii].length];
      int numChannels = 0;
      int maxShift = -999999;
      int minShift = 999999;
      Attribute initFpAttr = new FloatAttribute();
      // Calculate channel shift between reference angle and this angle
      for ( int index = 0; index < bankMap[ii].length; index++ ){
	bankData[index] = ds.getData_entry_with_id(bankMap[ii][index]);
	if ( index == 0) {
	  initFpAttr = bankData[index].getAttribute(Attribute.INITIAL_PATH);
	  numChannels = bankData[index].getX_scale().getNum_x();
	  times=new float[numChannels];
	  times=bankData[index].getX_values();
	}
	else {
	  int dum =0;  //do some error checking add later
	}
	//Need detector position to calculate shift
	DetectorPosition pos = (DetectorPosition)
	  bankData[index].getAttributeValue(Attribute.DETECTOR_POS );
	angle[index] = pos.getScatteringAngle();
	fp[index] = pos.getDistance();
	double LsinAng = Math.abs(Math.sin((double)angle[index]/2.0)) * fp[index];
	double LsinRef = Math.abs(Math.sin(Math.toRadians((double)refAng[ii]/2)))* refLen[ii];
	shift[index] = (int)(Math.round(Math.log(LsinRef/LsinAng)/bankRes[ii]));
	//	System.out.println(shift[index]+ ", " + Math.toDegrees(angle[index]));
	//keep track of actual max and min shift for this bank
	maxShift = Math.max(maxShift, shift[index]);
	minShift = Math.min(minShift, shift[index]);
      }
      // Equal # of channels eliminated on each side
      int newChannels = numChannels - (maxShift-minShift) -1;
      float[] newData = new float[newChannels];
      float[] newTimes = new float[newChannels +1];
      //copy time data into new array for this detector
      System.arraycopy(times, -1*minShift, newTimes, 0, newChannels+1);
      
      for ( int index = 0; index < bankMap[ii].length; index++ ){
	int startChan = maxShift - shift[index];
	int endChan = startChan + newChannels;
	float[] yData = new float[numChannels];
	yData = bankData[index].getY_values();
	for (int chanInd =0; chanInd < newData.length; chanInd++) {
	  int oldChan = startChan + chanInd;
	  newData[chanInd] += (yData[oldChan]/(times[oldChan+1] - times[oldChan]))*
	    (newTimes[chanInd+1]-newTimes[chanInd]);
	}
	
      }
      VariableXScale newScale = new VariableXScale(newTimes);
      newDataSet.addData_entry(Data.getInstance(newScale, newData, ii+1));
      Data tempData = newDataSet.getData_entry_with_id(ii+1);
      DetectorPosition newPos = new DetectorPosition();
      newPos.setCylindricalCoords(refLen[ii], (float)(refAng[ii]*Math.PI/180.0f), 0.0f);
      Attribute pos_attr = new DetPosAttribute( Attribute.DETECTOR_POS, newPos);
      tempData.setAttribute(pos_attr);
      tempData.setAttribute(initFpAttr);
      //      tempData.setAttribute(
    }
    

    
    return newDataSet;
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    Operator new_op = new SumLogBinnedSpectra( );
    new_op.CopyParametersFrom( this );
    return new_op;
  }

 /* ------------------------------- main ---------------------------------- */
 /**
  *  Main program for testing purposes.
  */
  public static void main( String args[] )
  {
    System.out.println("Test of LogBinneSpectra" );
    System.out.println("Main Method Not Completed" );
   
  }

}
