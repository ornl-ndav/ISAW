/*
 * File:  FindMultiplePeaksForm.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
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
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * $Log$
 * Revision 1.23  2003/08/25 19:45:12  bouzekc
 * Changed minimum peak intensity to 10.
 *
 * Revision 1.22  2003/07/14 16:32:44  bouzekc
 * Made run number, experiment name, and peaks file
 * parameters' initial values empty.
 *
 * Revision 1.21  2003/07/09 19:57:42  bouzekc
 * Added pixel border restriction parameter.
 *
 * Revision 1.20  2003/07/09 14:20:10  bouzekc
 * No longer has a specific default directory for the SCD
 * instprm.dat file.
 *
 * Revision 1.19  2003/07/08 21:01:24  bouzekc
 * Changed default values for some parameters.
 *
 * Revision 1.18  2003/07/03 14:26:39  bouzekc
 * Added all missing javadoc comments and formatted existing
 * comments.
 *
 * Revision 1.17  2003/06/26 22:24:21  bouzekc
 * Added to getDocumentation() to explain error that occurred
 * when trying to append to a peaks file that does not exist.
 *
 * Revision 1.16  2003/06/25 20:25:34  bouzekc
 * Unused private variables removed, reformatted for
 * consistency.
 *
 * Revision 1.15  2003/06/20 16:32:20  bouzekc
 * Removed space from the "Peaks File " parameter name.
 *
 * Revision 1.14  2003/06/18 23:34:23  bouzekc
 * Parameter error checking now handled by superclass Form.
 *
 * Revision 1.13  2003/06/18 19:56:09  bouzekc
 * Uses super.getResult() for initializing PropertyChanger
 * variables.
 *
 * Revision 1.12  2003/06/17 20:34:54  bouzekc
 * Fixed setDefaultParameters so all parameters have a
 * visible checkbox.  Added more robust error checking on
 * the raw and output directory parameters.
 *
 * Revision 1.11  2003/06/17 16:49:56  bouzekc
 * Now uses InstrumentType.formIPNSFileName to get the
 * file name.  Changed to work with new PropChangeProgressBar.
 *
 * Revision 1.10  2003/06/16 23:04:30  bouzekc
 * Now set up to use the multithreaded progress bar in
 * DataSetTools.components.ParametersGUI.
 *
 * Revision 1.9  2003/06/11 23:04:05  bouzekc
 * No longer uses StringUtil.setFileSeparator as DataDirPG
 * now takes care of this.
 *
 * Revision 1.8  2003/06/11 22:39:20  bouzekc
 * Updated documentation.  Moved file separator "/" code out
 * of loop.
 *
 * Revision 1.7  2003/06/10 19:54:00  bouzekc
 * Fixed bug where the peaks file was not written with every
 * run.
 * Updated documentation.
 *
 * Revision 1.6  2003/06/10 16:45:48  bouzekc
 * Moved creation of Operators out of the for loop and
 * into a private method to avoid excessive Object re-creation.
 * Added parameter to specify line in SCD calibration file.
 *
 * Revision 1.5  2003/06/06 15:12:00  bouzekc
 * Added log message header to file.
 *
 */
package Wizard.TOF_SCD;

import DataSetTools.dataset.DataSet;

import DataSetTools.instruments.InstrumentType;

import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.operator.DataSet.Math.Analyze.*;

import DataSetTools.operator.Generic.Load.LoadMonitorDS;
import DataSetTools.operator.Generic.Load.LoadOneHistogramDS;
import DataSetTools.operator.Generic.TOF_SCD.*;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import DataSetTools.wizard.*;

import java.io.File;

import java.util.Vector;


/**
 * This Form is a "port" of the script used to find peaks in multiple SCD
 * files.
 */
public class FindMultiplePeaksForm extends Form {
  //~ Static fields/initializers ***********************************************

  protected static int RUN_NUMBER_WIDTH = 5;

  //~ Instance fields **********************************************************

  protected final String SCDName      = "SCD";
  private LoadOneHistogramDS loadHist;
  private LoadMonitorDS loadMon;
  private IntegrateGroup integGrp;
  private LoadSCDCalib loadSCD;
  private FindPeaks fPeaks;
  private CentroidPeaks cenPeaks;
  private WriteExp wrExp;
  private WritePeaks wrPeaks;

  //~ Constructors *************************************************************

  /**
   * Construct a Form with a default parameter list.
   */
  public FindMultiplePeaksForm(  ) {
    super( "FindMultiplePeaksForm" );
    this.setDefaultParameters(  );
  }

  /**
   * Full constructor for FindMultiplePeaksForm.
   *
   * @param rawpath The raw data path.
   * @param outpath The output data path for the .peaks file.
   * @param runnums The run numbers to load.
   * @param expname The experiment name (i.e. "quartz").
   * @param num_peaks The maximum number of peaks to return.
   * @param min_int The minimum peak intensity to look for.
   * @param append Append to file (yes/no).
   * @param line2use SCD calibration file line to use.
   * @param calibfile SCD calibration file.
   */
  public FindMultiplePeaksForm( 
    String rawpath, String outpath, String runnums, String expname,
    int num_peaks, int min_int, boolean append, int line2use, String calibfile ) {
    this(  );
    getParameter( 0 )
      .setValue( rawpath );
    getParameter( 1 )
      .setValue( outpath );
    getParameter( 2 )
      .setValue( runnums );
    getParameter( 3 )
      .setValue( expname );
    getParameter( 4 )
      .setValue( new Integer( num_peaks ) );
    getParameter( 5 )
      .setValue( new Integer( min_int ) );
    getParameter( 6 )
      .setValue( new Boolean( append ) );
    getParameter( 7 )
      .setValue( new Integer( line2use ) );
    getParameter( 8 )
      .setValue( calibfile );
  }

  //~ Methods ******************************************************************

  /**
   * @return the String command used for invoking this Form in a Script.
   */
  public String getCommand(  ) {
    return "FINDMULTIPEAKSFORM";
  }

  /**
   * Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters(  ) {
    parameters = new Vector(  );

    addParameter( new DataDirPG( "Raw Data Path", null, false ) );  //0

    addParameter( new DataDirPG( "Peaks File Output Path", null, false ) );  //1

    addParameter( new IntArrayPG( "Run Numbers", "", false ) );  //2

    addParameter( new StringPG( "Experiment name", "", false ) );  //3

    addParameter( 
      new IntegerPG( "Maximum Number of Peaks", new Integer( 30 ), false ) );  //4

    addParameter( 
      new IntegerPG( "Minimum Peak Intensity", new Integer( 10 ), false ) );  //5

    addParameter( 
      new BooleanPG( "Append Data to File?", new Boolean( false ), false ) );  //6

    addParameter( 
      new IntegerPG( 
        "SCD Calibration File Line to Use", new Integer( -1 ), false ) );  //7

    addParameter( new LoadFilePG( "SCD Calibration File", null, false ) );  //8

    addParameter( new LoadFilePG( "Peaks File", " ", false ) );  //9

    addParameter( 
      new IntArrayPG( "Pixel Rows and Columns to Keep", "0:100", false ) );  //10
    setParamTypes( 
      null, new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 10 }, new int[]{ 9 } );
  }

  /**
   * @return documentation for this OperatorForm.  Follows javadoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer(  );

    s.append( "@overview This Form is designed to find peaks from multiple" );
    s.append( "SCD RunFiles. " );
    s.append( "@assumptions It is assumed that:\n" );
    s.append( "1. Data of interest is in the first histogram.\n" );
    s.append( "2. If the calibration file is not specified then the real " );
    s.append( "space conversion is not performed.\n" );
    s.append( "@algorithm First the calibration data from the SCD file is " );
    s.append( "loaded.\n" );
    s.append( "Then the FindPeaks Operator is used to find the peaks, based " );
    s.append( "on user input.\n" );
    s.append( "Then the CentroidPeaks Operator is used to find the peak " );
    s.append( "centers.\n" );
    s.append( "Next it writes the results to the specified *.peaks file.\n" );
    s.append( "Finally it writes the SCD experiment (*.x) file.\n" );
    s.append( "@param rawpath The raw data path.\n" );
    s.append( "@param outpath The output data path for the *.peaks file.\n" );
    s.append( "@param runnums The run numbers to load.\n" );
    s.append( "@param expname The experiment name (i.e. \"quartz\").\n" );
    s.append( "@param num_peaks The maximum number of peaks to return.\n" );
    s.append( "@param min_int The minimum peak intensity to look for.\n" );
    s.append( "@param append Whether to append data to the peaks file.\n" );
    s.append( "@param line2use SCD calibration file line to use.\n" );
    s.append( "@param calibfile SCD calibration file.\n" );
    s.append( "@param peaksFile Peaks filename that data is written to.\n" );
    s.append( "@param keepPixels The detector pixel range to keep.\n" );
    s.append( "@return A Boolean indicating success or failure of the Form's " );
    s.append( "execution.\n" );
    s.append( "@error If you specify that you want to append to the peaks " );
    s.append( "file and the file does not exist, you will get an error " );
    s.append( "from WriteSCDExp saying that it cannot find the file.  " );
    s.append( "To fix this, uncheck the \"append to file\" box.\n" );
    s.append( "@error An error is returned if a valid experiment name is not " );
    s.append( "entered.\n" );
    s.append( "@error An error is returned if a valid number of peaks is not " );
    s.append( "entered.\n" );
    s.append( "@error An error is returned if a valid minimum peak intensity " );
    s.append( "is not entered.\n" );
    s.append( "@error An error is returned if a valid calibration file name " );
    s.append( "is not entered.\n" );

    return s.toString(  );
  }

  /**
   * getResult() finds multiple peaks using the following algorithm: First the
   * calibration data from the SCD file is loaded.  Then the FindPeaks
   * Operator is used to find the peaks, based on user input. Then the
   * CentroidPeaks Operator is used to find the peak centers. Next it writes
   * the results to the specified .peaks file.  Finally it writes the SCD
   * experiment (.x) file.
   *
   * @return A Boolean indicating success or failure.
   */
  public Object getResult(  ) {
    SharedData.addmsg( "Executing...\n" );

    IParameterGUI param;
    int maxPeaks;
    int minIntensity;
    int SCDline;
    int lowerLimit;
    int upperLimit;
    Float monCount;
    String rawDir;
    String outputDir;
    String saveName;
    String expName;
    String calibFile;
    String loadName;
    String expFile;
    String IPNSName;
    boolean appendToFile;
    boolean first;
    Vector peaksVec;
    DataSet histDS;
    DataSet monDS;
    Object obj;
    Peak peak = null;

    int[] runsArray;
    int[] keepRange;

    //get raw data directory
    param    = ( IParameterGUI )super.getParameter( 0 );
    rawDir   = param.getValue(  )
                    .toString(  );

    //get output directory
    param       = ( IParameterGUI )super.getParameter( 1 );
    outputDir   = param.getValue(  )
                       .toString(  );

    //gets the run numbers
    param       = ( IParameterGUI )super.getParameter( 2 );
    runsArray   = IntList.ToArray( param.getValue(  ).toString(  ) );

    //get experiment name
    param     = ( IParameterGUI )super.getParameter( 3 );
    expName   = param.getValue(  )
                     .toString(  );

    //get maximum number of peaks to find
    param      = ( IParameterGUI )super.getParameter( 4 );
    maxPeaks   = ( ( Integer )param.getValue(  ) ).intValue(  );

    //get minimum intensity of peaks
    param          = ( IParameterGUI )super.getParameter( 5 );
    minIntensity   = ( ( Integer )param.getValue(  ) ).intValue(  );

    //get append to file value
    param          = ( IParameterGUI )super.getParameter( 6 );
    appendToFile   = ( ( BooleanPG )param ).getbooleanValue(  );

    //get line number for SCD calibration file
    param     = ( IParameterGUI )super.getParameter( 7 );
    SCDline   = ( ( Integer )param.getValue(  ) ).intValue(  );

    //get calibration file name
    param       = ( IParameterGUI )super.getParameter( 8 );
    calibFile   = param.getValue(  )
                       .toString(  );

    //get the detector border range
    keepRange = ( ( IntArrayPG )getParameter( 10 ) ).getArrayValue(  );

    if( keepRange != null ) {
      lowerLimit   = keepRange[0];  //lower limit of range

      //upper limit of range
      upperLimit = keepRange[keepRange.length - 1];
    } else {  //shouldn't happen, but default to 0:MAX_VALUE
      lowerLimit   = 0;
      upperLimit   = Integer.MAX_VALUE;
    }

    //first time through the file
    first   = true;

    //the name for the saved file
    saveName   = outputDir + expName + ".peaks";
    expFile    = outputDir + expName + ".x";

    //to avoid excessive object creation, we'll create all of the 
    //Operators here, then just set their parameters in the loop
    createFindPeaksOperators( 
      calibFile, maxPeaks, minIntensity, saveName, expFile, SCDline );

    //validate the parameters and set the progress bar variables
    Object superRes = super.getResult(  );

    //had an error, so return
    if( superRes instanceof ErrorString ) {
      return superRes;
    }

    //set the increment amount
    increment = ( 1.0f / runsArray.length ) * 100.0f;

    for( int i = 0; i < runsArray.length; i++ ) {
      IPNSName   = InstrumentType.formIPNSFileName( SCDName, runsArray[i] );

      loadName = rawDir + IPNSName;

      SharedData.addmsg( "Loading " + loadName + "." );

      //load the histogram
      loadHist.getParameter( 0 )
              .setValue( loadName );
      obj = loadHist.getResult(  );

      //make sure it is a DataSet
      if( obj instanceof DataSet ) {
        histDS = ( DataSet )obj;
      } else {
        return errorOut( "LoadOneHistogramDS failed: " + obj.toString(  ) );
      }

      //load the monitor
      loadMon.getParameter( 0 )
             .setValue( loadName );
      obj = loadMon.getResult(  );

      //make sure it is a DataSet
      if( obj instanceof DataSet ) {
        monDS = ( DataSet )obj;
      } else {
        return errorOut( "LoadMonitorDS failed: " + obj.toString(  ) );
      }

      SharedData.addmsg( "Finding peaks for " + loadName + "." );

      //integrate
      integGrp.setDataSet( monDS );
      obj = integGrp.getResult(  );

      if( obj instanceof Float ) {
        monCount = ( Float )obj;
      } else {
        return errorOut( "IntegrateGroup failed: " + obj.toString(  ) );
      }

      //load calibration data 
      loadSCD.setDataSet( histDS );
      obj = loadSCD.getResult(  );

      if( obj instanceof ErrorString ) {
        return errorOut( "LoadSCDCalib failed: " + obj.toString(  ) );
      }

      // find peaks
      fPeaks.getParameter( 0 )
            .setValue( histDS );
      fPeaks.getParameter( 1 )
            .setValue( monCount );
      obj = fPeaks.getResult(  );

      if( obj instanceof Vector ) {
        peaksVec = ( Vector )obj;
      } else {
        return errorOut( "FindPeaks failed: " + obj.toString(  ) );
      }

      // trim out edge peaks (defined by the "pixels to keep" parameter)
      for( int k = peaksVec.size(  ) - 1; k >= 0; k-- ) {
        peak = ( Peak )peaksVec.elementAt( k );

        //see if the peak pixels are within the user defined array.  We are
        //assuming a SQUARE detector, so we'll reject it if the x or y position
        //is not within our range
        if( 
          ( peak.x(  ) > upperLimit ) || ( peak.x(  ) < lowerLimit ) ||
            ( peak.y(  ) > upperLimit ) || ( peak.y(  ) < lowerLimit ) ) {
          peaksVec.remove( k );
        }
      }

      //"centroid" (find the center) the peaks
      cenPeaks.getParameter( 0 )
              .setValue( histDS );
      cenPeaks.getParameter( 1 )
              .setValue( peaksVec );
      obj = cenPeaks.getResult(  );

      if( obj instanceof Vector ) {
        peaksVec = ( Vector )obj;
      } else {
        return errorOut( "CentroidPeaks failed: " + obj.toString(  ) );
      }

      SharedData.addmsg( "Writing peaks for " + loadName + "." );

      // write out the results to the .peaks file
      wrPeaks.getParameter( 1 )
             .setValue( peaksVec );
      wrPeaks.getParameter( 2 )
             .setValue( new Boolean( appendToFile ) );
      obj = wrPeaks.getResult(  );

      if( obj instanceof ErrorString ) {
        return errorOut( "WritePeaks failed: " + obj.toString(  ) );
      }

      //write the SCD experiment file
      wrExp.getParameter( 0 )
           .setValue( histDS );
      wrExp.getParameter( 1 )
           .setValue( monDS );
      wrExp.getParameter( 4 )
           .setValue( new Boolean( appendToFile ) );
      obj = wrExp.getResult(  );

      if( obj instanceof ErrorString ) {
        return errorOut( "WriteExp failed: " + obj.toString(  ) );
      }

      if( first ) {
        first          = false;
        appendToFile   = true;
      }

      //fire a property change event off to any listeners
      oldPercent = newPercent;
      newPercent += increment;
      super.fireValueChangeEvent( ( int )oldPercent, ( int )newPercent );
    }

    SharedData.addmsg( "--- Done finding peaks. ---" );

    SharedData.addmsg( "Peaks are listed in " );
    SharedData.addmsg( saveName );

    //set the peaks file name
    param = ( IParameterGUI )super.getParameter( 9 );
    param.setValue( saveName );
    param.setValid( true );

    return Boolean.TRUE;
  }

  /**
   * Creates the Operators necessary for this Form and sets their constant
   * values.
   *
   * @param calibFile SCD calibration file.
   * @param maxPeaks Maximum number of peaks.
   * @param minInten Minimum peak intensity.
   * @param peaksName Fully qualified peaks file name.
   * @param expFile Fully qualified experiment file name.
   * @param SCDline The line to use from the SCD calib file.
   */
  private void createFindPeaksOperators( 
    String calibFile, int maxPeaks, int minInten, String peaksName,
    String expFile, int SCDline ) {
    loadHist   = new LoadOneHistogramDS(  );
    loadMon    = new LoadMonitorDS(  );
    integGrp   = new IntegrateGroup(  );
    loadSCD    = new LoadSCDCalib(  );
    fPeaks     = new FindPeaks(  );
    cenPeaks   = new CentroidPeaks(  );
    wrExp      = new WriteExp(  );
    wrPeaks    = new WritePeaks(  );

    //LoadOneHistogramDS
    //get the histogram.  We want to retrieve the first one.
    loadHist.getParameter( 1 )
            .setValue( new Integer( 1 ) );

    /*If you want to be able to use a group mask,
       change the "" below to a String variable.
       I've been told this is not used. -CMB*/
    loadHist.getParameter( 2 )
            .setValue( "" );

    //IntegrateGroup
    integGrp.getParameter( 0 )
            .setValue( new Integer( 1 ) );
    integGrp.getParameter( 1 )
            .setValue( new Float( 0 ) );
    integGrp.getParameter( 2 )
            .setValue( new Float( 50000 ) );

    //LoadSCDCalib
    loadSCD.getParameter( 0 )
           .setValue( calibFile );
    loadSCD.getParameter( 1 )
           .setValue( new Integer( SCDline ) );
    loadSCD.getParameter( 2 )
           .setValue( "" );

    //FindPeaks
    fPeaks.getParameter( 2 )
          .setValue( new Integer( maxPeaks ) );
    fPeaks.getParameter( 3 )
          .setValue( new Integer( minInten ) );

    //WritePeaks
    wrPeaks.getParameter( 0 )
           .setValue( peaksName );

    //WriteExp
    wrExp.getParameter( 2 )
         .setValue( expFile );
    wrExp.getParameter( 3 )
         .setValue( new Integer( 1 ) );
  }
}
