/*
 * File:  IndexJForm.java
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
 * Revision 1.23  2003/09/16 22:50:58  bouzekc
 * Modified slightly to work with the upgraded RadioButtonPG.
 *
 * Revision 1.22  2003/09/13 23:08:34  bouzekc
 * Now sets the value of the matrix selection parameter to "From a File"
 * upon initialization.
 *
 * Revision 1.21  2003/09/11 21:22:29  bouzekc
 * Updated to work with new Form class.
 *
 * Revision 1.20  2003/08/27 23:22:49  bouzekc
 * Removed unnecessary String cast.
 *
 * Revision 1.19  2003/07/29 08:11:16  bouzekc
 * Now uses RadioButtonPG for its matrix file choices.
 *
 * Revision 1.18  2003/07/16 19:48:17  bouzekc
 * Now appends to log when using the series of matrix files
 * to index peaks.
 *
 * Revision 1.17  2003/07/14 16:33:39  bouzekc
 * Made log file parameter's initial value empty.
 *
 * Revision 1.16  2003/07/08 23:08:12  bouzekc
 * Removed brackets from within getDocumentation().
 *
 * Revision 1.15  2003/07/03 14:23:46  bouzekc
 * Added all missing javadoc comments.
 *
 * Revision 1.14  2003/06/25 20:25:35  bouzekc
 * Unused private variables removed, reformatted for
 * consistency.
 *
 * Revision 1.13  2003/06/18 23:34:24  bouzekc
 * Parameter error checking now handled by superclass Form.
 *
 * Revision 1.12  2003/06/18 19:57:03  bouzekc
 * Uses super.getResult() for initializing PropertyChanger
 * variables.  Now fires off property change events in a
 * semi-intelligent manner.
 *
 * Revision 1.11  2003/06/17 20:36:36  bouzekc
 * Fixed setDefaultParameters so all parameters have a
 * visible checkbox.  Added more robust error checking on
 * the raw and output directory parameters.
 *
 * Revision 1.10  2003/06/16 14:52:28  bouzekc
 * Changed the matrix file load parameter to a LoadFilePG.
 *
 * Revision 1.9  2003/06/11 23:04:06  bouzekc
 * No longer uses StringUtil.setFileSeparator as DataDirPG
 * now takes care of this.
 *
 * Revision 1.8  2003/06/11 22:43:59  bouzekc
 * Added code to input three delta values (h, k, and l).
 * Added code to allow specifying a matrix file to apply to
 * certain runs or to overall runs.  Updated documentation.
 * Moved calls to setFileSeparator() out of the loop.
 *
 * Revision 1.7  2003/06/10 21:55:07  bouzekc
 * Added parameter for IndexJ log file viewing.  Moved error
 * checking into loop to avoid a potential missed getResult()
 * error.
 *
 * Revision 1.6  2003/06/10 20:29:35  bouzekc
 * Fixed ClassCastException in getResult().
 *
 * Revision 1.5  2003/06/10 20:11:56  bouzekc
 * Moved IndexJ creation out of for loop to avoid
 * excessive Object creation.
 *
 * Revision 1.4  2003/06/09 21:50:39  bouzekc
 * Changed Form to run with BlindJ matrix files if it
 * could not find LsqrsJ matrix files rather than having
 * the user select.
 * Updated documentation.
 * Added constructor to set HAS_CONSTANTS to reduce
 * the number of calls to setDefaultParameters().
 *
 * Revision 1.3  2003/06/06 15:12:01  bouzekc
 * Added log message header to file.
 *
 */
package Wizard.TOF_SCD;

import DataSetTools.operator.Operator;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import DataSetTools.wizard.*;

import Operators.TOF_SCD.*;

import java.beans.*;

import java.io.File;

import java.util.Vector;


/**
 * This is a Form to add extra functionality to IndexJ.  If used with output
 * from LsqrsJForm, it "knows" which runs to restrict for each matrix file. If
 * used with output from BlindJ, it applies the orientation matrix data to the
 * entire peaks file. Other than that, it functions in a similar manner to
 * IndexJ.
 */
public class IndexJForm extends Form implements PropertyChangeListener {
  //~ Static fields/initializers ***********************************************

  protected static int RUN_NUMBER_WIDTH = 5;
  public static final String FROM_FILE  = "From a File";
  public static final String FROM_LSQRS = "From LsqrsJ";

  //~ Instance fields **********************************************************

  private IndexJ indexJOp;
  private Vector choices = null;

  //~ Constructors *************************************************************

  /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */

  /**
   * Construct a Form with a default parameter list.
   */
  public IndexJForm(  ) {
    super( "IndexJForm" );
    this.setDefaultParameters(  );
  }

  /**
   * Construct a Form using the default parameter list.
   *
   * @param hasConstParams boolean indicating whether this Form should have
   *        constant parameters.
   */
  public IndexJForm( boolean hasConstParams ) {
    super( "IndexJForm", hasConstParams );
    this.setDefaultParameters(  );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */

  /**
   * Full constructor for IndexJForm.
   *
   * @param runnums The run numbers used in naming the matrix files.
   * @param peaksPath The peaks file path.
   * @param delta Error parameter for indexing peaks.
   * @param update Whether to update the peaks file.
   * @param expName The experiment name.
   */
  public IndexJForm( 
    String runnums, String peaksPath, float delta, boolean update,
    String expName ) {
    this(  );
    getParameter( 0 )
      .setValue( runnums );
    getParameter( 1 )
      .setValue( peaksPath );
    getParameter( 2 )
      .setValue( new Float( delta ) );
    getParameter( 3 )
      .setValue( new Float( delta ) );
    getParameter( 4 )
      .setValue( new Float( delta ) );
    getParameter( 5 )
      .setValue( new Boolean( update ) );
    getParameter( 6 )
      .setValue( expName );
  }

  //~ Methods ******************************************************************

  /**
   * @return String command used for invoking this Form in a Script.
   */
  public String getCommand(  ) {
    return "JINDEXFORM";
  }

  /**
   * Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters(  ) {
    if( choices == null ) {
      initChoices(  );
    }
    parameters = new Vector(  );
    addParameter( new IntArrayPG( "Run Numbers", null, false ) );  //0
    addParameter( new DataDirPG( "Peaks File Path", null, false ) );  //1
    addParameter( new StringPG( "Experiment Name", null, false ) );  //2
    addParameter( new FloatPG( "Delta (h)", 0.10f, false ) );  //3
    addParameter( new FloatPG( "Delta (k)", 0.10f, false ) );  //4
    addParameter( new FloatPG( "Delta (l)", 0.10f, false ) );  //5
    addParameter( new BooleanPG( "Update Peaks File", true, false ) );  //6

    RadioButtonPG rpg = new RadioButtonPG( 
        "Get Matrix File From: ", choices, false );
    rpg.setValue( choices.get( 0 ) );
    addParameter( rpg );  //7
    addParameter( new LoadFilePG( "Matrix File to Load", "", false ) );  //8
    addParameter( new IntArrayPG( "Restrict Runs", "", false ) );  //9
    addParameter( new LoadFilePG( "JIndex Log", " ", false ) );  //10
    rpg.addPropertyChangeListener( IParameter.VALUE, this );

    if( HAS_CONSTANTS ) {
      setParamTypes( 
        new int[]{ 0, 1, 2 }, new int[]{ 3, 4, 5, 6, 7, 8, 9 }, new int[]{ 10 } );
    } else {
      setParamTypes( 
        null, new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, new int[]{ 10 } );
    }
  }

  /**
   * @return documentation for this OperatorForm.  Follows javadoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer(  );
    s.append( "@overview This is a Form to add extra functionality to " );
    s.append( "IndexJ.  If specMatrix is false, it uses multiple " );
    s.append( "lsexpName#.matrix files when getResult() is called.  " );
    s.append( "In addition, it \"knows\" which runs to restrict for each " );
    s.append( "matrix file.  If specMatrix is true, the matrix files and the " );
    s.append( "runs to restrict can be specified.  Other than that, " );
    s.append( "it functions in a similar manner to IndexJ.\n" );
    s.append( "@assumptions It is assumed that:\n" );
    s.append( 
      "The matrix files have the format \"experiment name + run number " );
    s.append( "+ .mat\" or \"ls + experiment name + run number + .mat\"." );
    s.append( "getResult() relies on this.\n" );
    s.append( "In addition, it is assumed that the peaks file and the matrix " );
    s.append( "files are in the same directory.\n" );
    s.append( "@algorithm Using the given run numbers and matrix files, " );
    s.append( "this Form calls IndexJ, giving it the appropriate matrix file " );
    s.append( "for each run number in the peaks file.\n" );
    s.append( "@param runnums The run numbers used for naming the matrix " );
    s.append( "files.\n" );
    s.append( "@param peaksPath The path where the peaks file is located.\n" );
    s.append( "@param expName The experiment name.\n" );
    s.append( "@param delta_h Error parameter for indexing peaks (h).\n" );
    s.append( "@param delta_k Error parameter for indexing peaks (k).\n" );
    s.append( "@param delta_l Error parameter for indexing peaks (l).\n" );
    s.append( "@param update Whether to update the peaks file.\n" );
    s.append( "@param specMatrix Whether to specify a matrix file or let " );
    s.append( "IndexJ apply the ls#expName.mat files to the *.peaks file.\n" );
    s.append( 
      "@param matFile If specMatrix is true, the matrix file to load.\n" );
    s.append( 
      "@param restrictRuns If specMatrix is true, the runs to restrict.\n" );
    s.append( "@param indexLog The log resulting from IndexJ's operation.\n" );
    s.append( "@return A Boolean indicating success or failure of the Form's " );
    s.append( "execution.\n" );
    s.append( "@error No valid run numbers are entered.\n" );
    s.append( "@error No valid experiment name is entered.\n" );
    s.append( "@error No valid input path is entered.\n" );

    return s.toString(  );
  }

  /**
   * getResult() takes the user input parameters and runs IndexJ, using a
   * lsexpName#.mat file (output from LsqrsJForm) for each run number.  In
   * addition, it sends its output to a index.log file. Note that it "knows"
   * which runs to restrict for each matrix file, based on the matrix file
   * names.
   *
   * @return A Boolean indicating success or failure.
   */
  public Object getResult(  ) {
    SharedData.addmsg( "Calculating h, k, and l values for each run..." );

    IParameterGUI param;
    String peaksDir;
    String expName;
    String peaksName;
    String runNum;
    String matName;
    String restrictRuns;
    String matToUse;
    Object obj      = null;
    float delta_h;
    float delta_k;
    float delta_l;
    boolean update;
    int[] runsArray;

    //gets the run numbers
    param       = ( IParameterGUI )super.getParameter( 0 );
    runsArray   = IntList.ToArray( param.getValue(  ).toString(  ) );

    //gets the input path
    param       = ( IParameterGUI )super.getParameter( 1 );
    peaksDir    = param.getValue(  )
                       .toString(  );

    //gets the experiment name
    param       = ( IParameterGUI )super.getParameter( 2 );
    expName     = param.getValue(  )
                       .toString(  );

    //gets the delta_h
    param       = ( IParameterGUI )super.getParameter( 3 );
    delta_h     = ( ( Float )param.getValue(  ) ).floatValue(  );

    //gets the delta_k
    param       = ( IParameterGUI )super.getParameter( 4 );
    delta_k     = ( ( Float )param.getValue(  ) ).floatValue(  );

    //gets the delta_l
    param       = ( IParameterGUI )super.getParameter( 5 );
    delta_l     = ( ( Float )param.getValue(  ) ).floatValue(  );

    //gets the update value 
    param       = ( IParameterGUI )super.getParameter( 6 );
    update      = ( ( BooleanPG )param ).getbooleanValue(  );

    //get the "use matrix" boolean value
    param       = ( IParameterGUI )super.getParameter( 7 );
    matToUse    = ( ( RadioButtonPG )param ).getStringValue(  );

    //#8 the matrix name will be validated later - setting it valid here
    //skips the Form's parameter checking for this parameter
    ( ( IParameterGUI )getParameter( 8 ) ).setValid( true );

    //#9 the restrict runs value will be validated later
    ( ( IParameterGUI )getParameter( 9 ) ).setValid( true );

    //peaks file name
    peaksName = peaksDir + expName + ".peaks";

    //validate the parameters and init the progress bar variables
    Object validCheck = validateSelf(  );

    //had an error, so return
    if( validCheck instanceof ErrorString ) {
      return validCheck;
    }

    //no need to continually recreate this Operator in a loop
    indexJOp = new IndexJ(  );
    indexJOp.getParameter( 0 )
            .setValue( peaksName );
    indexJOp.getParameter( 3 )
            .setValue( new Float( delta_h ) );
    indexJOp.getParameter( 4 )
            .setValue( new Float( delta_k ) );
    indexJOp.getParameter( 5 )
            .setValue( new Float( delta_l ) );
    indexJOp.getParameter( 6 )
            .setValue( new Boolean( update ) );

    //don't append to the log file when running initially
    indexJOp.getParameter( 7 )
            .setValue( new Boolean( false ) );

    //user wants to use a specified matrix file
    if( matToUse.equals( FROM_FILE ) ) {
      //get the matrix name make sure the matrix file exists
      param          = ( IParameterGUI )super.getParameter( 8 );
      matName        = ( String )param.getValue(  )
                                      .toString(  );

      if( !( new File( matName ).exists(  ) ) ) {
        return errorOut( 
          param, "ERROR: The specified matrix file does not exist." );
      } else {
        param.setValid( true );
      }

      //validate the restrict runs value
      param          = ( IParameterGUI )super.getParameter( 9 );
      restrictRuns   = param.getValue(  )
                            .toString(  );
      param.setValid( true );
      SharedData.addmsg( 
        "IndexJ is updating " + peaksName + " with " + matName );
      indexJOp.getParameter( 1 )
              .setValue( matName );
      indexJOp.getParameter( 2 )
              .setValue( restrictRuns );
      obj = indexJOp.getResult(  );

      if( obj instanceof ErrorString ) {
        return errorOut( "IndexJ failed: " + obj.toString(  ) );
      }
      super.fireValueChangeEvent( 0, 100 );
    } else {
      //try to find the matrix files.  If the lsqrs matrix files exist, 
      //this is their format:
      runNum      = formatRunNum( runsArray[0] );
      matName     = peaksDir + "ls" + expName + runNum + ".mat";

      if( !( new File( matName ).exists(  ) ) ) {
        return errorOut( 
          param,
          "ERROR: No least squares matrix files exist.  " +
          "Please specify a matrix file." );
      }

      //set the increment amount
      increment = ( 1.0f / runsArray.length ) * 100.0f;

      boolean appendToLog = false;

      for( int i = 0; i < runsArray.length; i++ ) {
        indexJOp.getParameter( 7 )
                .setValue( new Boolean( appendToLog ) );

        //load the run numbers.  We don't want to remove the leading zeroes!
        runNum    = formatRunNum( runsArray[i] );

        //the name of the matrix file
        matName   = peaksDir + "ls" + expName + runNum + ".mat";
        SharedData.addmsg( 
          "IndexJ is updating " + peaksName + " with " + matName );

        //call IndexJ
        indexJOp.getParameter( 1 )
                .setValue( matName );

        //synchronize the run number in the peaks and matrix file
        indexJOp.getParameter( 2 )
                .setValue( runNum );
        obj = indexJOp.getResult(  );

        if( obj instanceof ErrorString ) {
          return errorOut( "IndexJ failed: " + obj.toString(  ) );
        }

        //fire a property change event off to any listeners
        oldPercent = newPercent;
        newPercent += increment;
        super.fireValueChangeEvent( ( int )oldPercent, ( int )newPercent );
        appendToLog = true;  //save the previously logged run data
      }
    }

    //set the indexj log file parameter
    param = ( IParameterGUI )getParameter( 10 );
    param.setValue( obj );
    param.setValid( true );
    SharedData.addmsg( "--- IndexJForm finished. ---" );

    return Boolean.TRUE;
  }

  /**
   * Method to listen for changes on the RadioButtonPG.
   */
  public void propertyChange( PropertyChangeEvent pce ) {
    Object newVal = pce.getNewValue(  );
    if( newVal == FROM_FILE ) {
      ( ( IParameterGUI )getParameter( 8 ) ).setEnabled( true );
    } else if( newVal == FROM_LSQRS ) {
      ( ( IParameterGUI )getParameter( 8 ) ).setEnabled( false );
    }
  }

  /**
   * Utility method to ease "code eye."
   *
   * @param runNumber The RUN_NUMBER_WIDTH size number to pad with leading
   *        zeroes.
   *
   * @return The runNumber padded with leading zeroes.
   */
  private String formatRunNum( int runNumber ) {
    return DataSetTools.util.Format.integerPadWithZero( 
      runNumber, RUN_NUMBER_WIDTH );
  }

  /**
   * Initializes the radio button choices.
   */
  private void initChoices(  ) {
    choices = new Vector( 2, 2 );
    choices.add( FROM_FILE );
    choices.add( FROM_LSQRS );
  }
}
