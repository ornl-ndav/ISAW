/*
 * File:  LsqrsJForm.java
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
 * Revision 1.22  2003/09/11 21:22:32  bouzekc
 * Updated to work with new Form class.
 *
 * Revision 1.21  2003/08/14 19:41:15  bouzekc
 * Fixed javadoc error.
 *
 * Revision 1.20  2003/07/14 16:47:17  bouzekc
 * Added missing javadocs, reformatted to fit within 80 column
 * indent.
 *
 * Revision 1.19  2003/07/14 16:41:19  bouzekc
 * Added the LsqrsJ log file as a second "result."
 *
 * Revision 1.18  2003/07/08 22:56:54  bouzekc
 * Updated documentation.
 *
 * Revision 1.17  2003/06/27 20:30:31  bouzekc
 * Removed the temporary parameter validation for peaks
 * intensity and threshold.
 *
 * Revision 1.16  2003/06/26 22:25:25  bouzekc
 * Added code to deal with the threshold and pixel range
 * parameters and send them to LsqrsJ.
 *
 * Revision 1.15  2003/06/26 16:43:28  bouzekc
 * Now always uses identity matrix for calculations, per
 * A.J.Schultz's request.
 *
 * Revision 1.14  2003/06/25 21:30:39  bouzekc
 * Now contains (saveable) parameter hooks and documentation
 * for minimum peak threshold and channels to keep.
 *
 * Revision 1.13  2003/06/25 20:26:02  bouzekc
 * Unused private variables removed.
 *
 * Revision 1.12  2003/06/25 16:11:22  bouzekc
 * Reformatted for clarity.
 *
 * Revision 1.11  2003/06/18 23:34:26  bouzekc
 * Parameter error checking now handled by superclass Form.
 *
 * Revision 1.10  2003/06/18 19:58:23  bouzekc
 * Uses super.getResult() for initializing PropertyChanger
 * variables.  Now fires off property change events in a
 * semi-intelligent manner.
 *
 * Revision 1.9  2003/06/17 20:37:56  bouzekc
 * Fixed setDefaultParameters so all parameters have a
 * visible checkbox.  Added more robust error checking on
 * the raw and output directory parameters.  Fixed matrix
 * files parameter setting bug.
 *
 * Revision 1.8  2003/06/11 23:04:08  bouzekc
 * No longer uses StringUtil.setFileSeparator as DataDirPG
 * now takes care of this.
 *
 * Revision 1.7  2003/06/11 22:48:42  bouzekc
 * Added parameters so that the identity matrix is used for the
 * iteration step.  Added parameters so that the user can view
 * the scalar log file.  Updated documentation.  Moved calls
 * to setFileSeparator out of the loop.
 *
 * Revision 1.6  2003/06/10 21:56:08  bouzekc
 * Fixed problem where the matrix file name Vector was not
 * being set as a parameter.
 *
 * Revision 1.5  2003/06/10 20:31:41  bouzekc
 * Moved creation of lsqrsJ out of the for loop to avoid
 * excessive Object creation.  Now also outputs an overall
 * orientation matrix for the entire set of runs.
 *
 * Revision 1.4  2003/06/09 21:56:05  bouzekc
 * Updated documentation.
 * Added constructor to set HAS_CONSTANTS to reduce
 * the number of calls to setDefaultParameters().
 * Updated parameter names.
 * Removed code for the "firstTime" variable from
 * setDefaultParameters - no longer needed.
 * Overrode makeGUI() to set the transformation matrix
 * back to identity matrix if LsqrsJ is run more than once.
 *
 * Revision 1.3  2003/06/06 15:12:03  bouzekc
 * Added log message header to file.
 *
 */
package Wizard.TOF_SCD;

import DataSetTools.operator.Operator;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import DataSetTools.wizard.*;

import Operators.TOF_SCD.*;

import java.io.File;

import java.util.Vector;


/**
 * This is a Form to add extra functionality to LsqrsJ.  It outputs multiple
 * ls#.mat files, where # corresponds to a run number. Other than that, it
 * functions in a similar manner to LsqrsJ.
 */
public class LsqrsJForm extends Form {
  //~ Static fields/initializers ***********************************************

  protected static int RUN_NUMBER_WIDTH = 5;
  private static final String identmat  = "[[1,0,0][0,1,0][0,0,1]]";

  //~ Instance fields **********************************************************

  private boolean useIdentity        = false;
  private BooleanPG useIdentCheckBox;

  //~ Constructors *************************************************************

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */

  /**
   * Construct a Form with a default parameter list.
   */
  public LsqrsJForm(  ) {
    super( "LsqrsJForm" );
    this.setDefaultParameters(  );
  }

  /**
   * Construct a Form using the default parameter list.
   *
   * @param hasConstParams boolean indicating whether this Form should have
   *        constant parameters.
   */
  public LsqrsJForm( boolean hasConstParams ) {
    super( "LsqrsJForm", hasConstParams );
    this.setDefaultParameters(  );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */

  /**
   * Full constructor for LsqrsJForm.
   *
   * @param runNums The run numbers to use for naming the matrix files.
   * @param peaksPath The path where the peaks file is.
   * @param expName The experiment name.
   * @param restrictSeq The sequence numbers to restrict.
   * @param transform The transformation matrix to apply.
   */
  public LsqrsJForm( 
    String runNums, String peaksPath, String expName, String restrictSeq,
    String transform ) {
    this(  );
    getParameter( 0 )
      .setValue( runNums );
    getParameter( 1 )
      .setValue( peaksPath );
    getParameter( 2 )
      .setValue( expName );
    getParameter( 3 )
      .setValue( restrictSeq );
    getParameter( 4 )
      .setValue( transform );
  }

  //~ Methods ******************************************************************

  /**
   * @return the String command used for invoking this Form in a Script.
   */
  public String getCommand(  ) {
    return "JLSQRSFORM";
  }

  /**
   * Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters(  ) {
    parameters = new Vector(  );

    addParameter( new IntArrayPG( "Run Numbers", null, false ) );  //0

    addParameter( new DataDirPG( "Peaks File Path", null, false ) );  //1

    addParameter( new StringPG( "Experiment Name", null, false ) );  //2

    addParameter( 
      new IntArrayPG( 
        "Restrict Peaks Sequence Numbers (blank for all)", null, false ) );  //3

    addParameter( new ArrayPG( "Matrix Files", new Vector(  ), false ) );  //4

    addParameter( 
      new IntegerPG( "Minimum Peak Intensity Threshold", 0, false ) );  //5

    addParameter( 
      new IntArrayPG( "Pixel Rows and Columns to Keep", "0:100", false ) );  //6

    addParameter( new LoadFilePG( "JLsqrs Log File", " ", false ) );  //7

    if( HAS_CONSTANTS ) {
      setParamTypes( 
        new int[]{ 0, 1, 2 }, new int[]{ 3, 5, 6 }, new int[]{ 4, 7 } );
    } else {  //standalone or first time form
      setParamTypes( null, new int[]{ 0, 1, 2, 3, 5, 6 }, new int[]{ 4, 7 } );
    }
  }

  /**
   * @return javadoc formatted documentation for this OperatorForm.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer(  );

    s.append( "@overview This is a Form to add extra functionality to " );
    s.append( "LsqrsJ.  It use the given *.peaks file and the transformation " );
    s.append( "matrix for calculation.  In addition, it " );
    s.append( "\"knows\" which runs to restrict for each matrix file.  " );
    s.append( "It outputs a ls#expName.mat file for each run, and an " );
    s.append( "lsexpName.mat file which is produced for ALL runs." );
    s.append( "Other than that, it functions in a similar manner to " );
    s.append( "LsqrsJ.\n" );
    s.append( "@assumptions The peaks file exists and the transformation " );
    s.append( "matrix is valid." );
    s.append( 
      "@algorithm Using the given run numbers, transformation matrix, " );
    s.append( 
      "and peaks file, this Form calls LsqrsJ, creating the appropriate " );
    s.append( 
      "lsexpName#.mat file for each run number in the peaks file, as well " );
    s.append( "as an overall lsexpName.mat file.\n" );
    s.append( 
      "The user is given a choice as to what transformation matrix to " );
    s.append( "use for each run number (NOT IMPLEMENTED YET).\n" );
    s.append( "@param runnums The run numbers to use for naming the matrix " );
    s.append( "files.\n" );
    s.append( "@param peaksPath The path where the peaks file is.\n" );
    s.append( "@param expName The experiment name.\n" );
    s.append( "@param restrictSeq The sequence numbers to restrict.\n" );
    s.append( "@param matrixFiles The Vector of LsqrsJ output matrix files.\n" );
    s.append( "@param minThresh The minimum peak intensity threshold to " );
    s.append( "use.\n" );
    s.append( "@param keepPixels The detector pixel range to keep.\n" );
    s.append( "@param lsqrslog The log file from LsqrsJ.\n" );
    s.append( "@return A Boolean indicating success or failure of the Form's " );
    s.append( "execution.\n" );
    s.append( "@error Invalid peaks path.\n" );
    s.append( "@error Invalid experiment name.\n" );
    s.append( "@error Invalid transformation matrix.\n" );

    return s.toString(  );
  }

  /**
   * getResult() uses the given .peaks file and the identity matrix for
   * calculation.  In addition, it "knows" which runs to  restrict for each
   * matrix file.  "It outputs a ls#expName.mat file for each run, and an
   * lsexpName.mat file which is produced for ALL runs.  Other than that, it
   * functions in a similar manner to LsqrsJ.
   *
   * @return A Boolean indicating success or failure.
   */
  public Object getResult(  ) {
    SharedData.addmsg( "Executing..." );

    IParameterGUI param;
    String runNum;
    String peaksDir;
    String restrictSeq;
    String matFileName;
    String expName;
    String range;
    String peaksName;
    Vector matNamesVec  = new Vector( 20, 4 );
    Object obj;
    int[] runsArray;
    Integer threshold;
    LsqrsJ leastSquares;

    //gets the run numbers
    param       = ( IParameterGUI )getParameter( 0 );
    runsArray   = IntList.ToArray( param.getValue(  ).toString(  ) );

    //get input file directory 
    param      = ( IParameterGUI )super.getParameter( 1 );
    peaksDir   = param.getValue(  )
                      .toString(  );

    //gets the experiment name
    param     = ( IParameterGUI )super.getParameter( 2 );
    expName   = param.getValue(  )
                     .toString(  );

    /*get restricted sequence numbers - leave in String form
       for LsqrsJ*/
    param         = ( IParameterGUI )getParameter( 3 );
    restrictSeq   = param.getValue(  )
                         .toString(  );

    //get the peak intensity threshold
    param       = ( IParameterGUI )getParameter( 5 );
    threshold   = ( Integer )( param.getValue(  ) );

    //get the detector border range - leave in string form for LsqrsJ
    param   = ( IParameterGUI )getParameter( 6 );
    range   = ( ( IntArrayPG )param ).getStringValue(  );

    //peaks file
    peaksName   = peaksDir + expName + ".peaks";

    //call LsqrsJ - this is the same every time, so keep it out of the loop
    leastSquares = new LsqrsJ(  );
    leastSquares.getParameter( 0 )
                .setValue( peaksName );
    leastSquares.getParameter( 2 )
                .setValue( restrictSeq );
    leastSquares.getParameter( 3 )
                .setValue( identmat );
    leastSquares.getParameter( 5 )
                .setValue( threshold );
    leastSquares.getParameter( 6 )
                .setValue( range );

    //validate the parameters and init the progress bar variables
    Object validCheck = validateSelf(  );

    //had an error, so return
    if( validCheck instanceof ErrorString ) {
      return validCheck;
    }

    //set the increment amount
    increment = ( 1.0f / runsArray.length ) * 100.0f;

    for( int i = 0; i < runsArray.length; i++ ) {
      runNum   = DataSetTools.util.Format.integerPadWithZero( 
          runsArray[i], RUN_NUMBER_WIDTH );

      matFileName = peaksDir + "ls" + expName + runNum + ".mat";
      matNamesVec.add( matFileName );

      SharedData.addmsg( 
        "LsqrsJ is creating " + matFileName + " for " + peaksName );

      leastSquares.getParameter( 1 )
                  .setValue( runNum );
      leastSquares.getParameter( 4 )
                  .setValue( matFileName );

      obj = leastSquares.getResult(  );

      if( obj instanceof ErrorString ) {
        return errorOut( "LsqrsJ failed: " + obj.toString(  ) );
      }

      //fire a property change event off to any listeners
      oldPercent = newPercent;
      newPercent += increment;
      super.fireValueChangeEvent( ( int )oldPercent, ( int )newPercent );
    }

    //now put out an orientation matrix for all of the runs.
    matFileName = peaksDir + "ls" + expName + ".mat";
    matNamesVec.add( matFileName );
    leastSquares.getParameter( 1 )
                .setValue( "" );
    leastSquares.getParameter( 4 )
                .setValue( matFileName );
    obj = leastSquares.getResult(  );

    if( obj instanceof ErrorString ) {
      return errorOut( "LsqrsJ failed: " + obj.toString(  ) );
    }

    //set the matrix file name vector parameter
    param = ( IParameterGUI )getParameter( 4 );
    param.setValue( matNamesVec );
    param.setValid( true );

    //set the log file name parameter
    param = ( IParameterGUI )getParameter( 7 );
    param.setValue( obj.toString(  ) );
    param.setValid( true );

    SharedData.addmsg( "--- LsqrsJForm finished. ---" );

    return new Boolean( true );
  }
}
