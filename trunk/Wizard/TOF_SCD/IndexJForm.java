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
 * Revision 1.33  2006/07/10 16:26:13  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.32  2006/02/26 00:08:37  dennis
 * Now uses the minimun run number width (used to pad the run number
 * in the matrix file name) from LsqrsJ, so that the code that reads
 * the matrix file will have the same convention as the code that
 * writes.  NOTE: Padding the run number to 4 digits in the matrix
 * file name should NOT have been done in the first place, since it
 * just introduces another possible point of failure, with no benefit.
 * However, now that it is in use, this change fixes a bug in reading
 * and writing the matrix file, after the change from run number 9999
 * to 10000.
 * Did some additional code clean up.
 *
 * Revision 1.31  2004/03/15 03:37:40  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.30  2004/02/11 04:10:55  bouzekc
 * Uses the new Wizard classes that have indeterminate progress bars.
 *
 * Revision 1.29  2003/12/15 02:17:29  bouzekc
 * Removed unused imports.
 *
 * Revision 1.28  2003/11/11 20:36:43  bouzekc
 * Modified to work with new Form.addParameter().
 *
 * Revision 1.27  2003/11/05 02:20:30  bouzekc
 * Changed to work with new Wizard and Form design.
 *
 * Revision 1.26  2003/10/26 19:17:36  bouzekc
 * Now returns the name of the file written rather than Boolean.TRUE when
 * getResult() executes successfully.
 *
 * Revision 1.25  2003/10/06 23:26:11  bouzekc
 * Fixed bug where parameter did not properly disable when the RadioButton
 * value changed.
 *
 * Revision 1.24  2003/10/04 20:36:20  bouzekc
 * Made "Restrict Runs" non-editable when "From LsqrsJ" is selected.  This
 * corrects a previous oversight.
 *
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

import gov.anl.ipns.Parameters.IParameterGUI;
import gov.anl.ipns.Parameters.BooleanPG;
import gov.anl.ipns.Parameters.DataDirPG;
import gov.anl.ipns.Parameters.FloatPG;
import gov.anl.ipns.Parameters.IntArrayPG;
import gov.anl.ipns.Parameters.LoadFilePG;
import gov.anl.ipns.Parameters.RadioButtonPG;
import gov.anl.ipns.Parameters.StringPG;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.Messaging.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Vector;

import gov.anl.ipns.Parameters.IParameter;
import gov.anl.ipns.Parameters.IParameterGUI;
import DataSetTools.util.SharedData;
import DataSetTools.wizard.Form;
import Operators.TOF_SCD.IndexJ;


/**
 * This is a Form to add extra functionality to IndexJ.  If used with output
 * from LsqrsJForm, it "knows" which runs to restrict for each matrix file. If
 * used with output from BlindJ, it applies the orientation matrix data to the
 * entire peaks file. Other than that, it functions in a similar manner to
 * IndexJ.
 */
public class IndexJForm extends Form implements IObserver {
  //~ Static fields/initializers ***********************************************

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
    getParameter( 0 ).setValue( runnums );
    getParameter( 1 ).setValue( peaksPath );
    getParameter( 2 ).setValue( new Float( delta ) );
    getParameter( 3 ).setValue( new Float( delta ) );
    getParameter( 4 ).setValue( new Float( delta ) );
    getParameter( 5 ).setValue( new Boolean( update ) );
    getParameter( 6 ).setValue( expName );
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
    addParameter( new IntArrayPG( "Run Numbers", null ) );     //0
    addParameter( new DataDirPG( "Peaks File Path", null ) );  //1
    addParameter( new StringPG( "Experiment Name", null ) );   //2
    addParameter( new FloatPG( "Delta (h)", 0.10f ) );         //3
    addParameter( new FloatPG( "Delta (k)", 0.10f ) );         //4
    addParameter( new FloatPG( "Delta (l)", 0.10f ) );         //5
    addParameter( new BooleanPG( "Update Peaks File", true )); //6

    RadioButtonPG rpg = new RadioButtonPG( 
        "Get Matrix File From: ", choices  );

    addParameter( rpg );  //7
    //have to do this because Operator clones the parameters
    rpg = ( ( RadioButtonPG )getParameter( 7 ) );
    rpg.addIObserver( this );
    rpg.setValue( choices.get( 0 ) );

    addParameter( new LoadFilePG( "Matrix File to Load", "" ) );  //8
    addParameter( new IntArrayPG( "Restrict Runs", "" ) );  //9
    setResultParam( new LoadFilePG( "JIndex Log", " " ) );  //10

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
    ( ( IParameterGUI )getParameter( 8 ) ).setValidFlag( true );

    //#9 the restrict runs value will be validated later
    ( ( IParameterGUI )getParameter( 9 ) ).setValidFlag( true );

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
    indexJOp.getParameter( 0 ).setValue( peaksName );
    indexJOp.getParameter( 3 ).setValue( new Float( delta_h ) );
    indexJOp.getParameter( 4 ).setValue( new Float( delta_k ) );
    indexJOp.getParameter( 5 ).setValue( new Float( delta_l ) );
    indexJOp.getParameter( 6 ).setValue( new Boolean( update ) );

    //don't append to the log file when running initially
    indexJOp.getParameter( 7 ).setValue( new Boolean( false ) );

    //user wants to use a specified matrix file
    if( matToUse.equals( FROM_FILE ) ) {
      //get the matrix name make sure the matrix file exists
      param   = ( IParameterGUI )super.getParameter( 8 );
      matName = ( String )param.getValue(  ).toString(  );

      if( !( new File( matName ).exists(  ) ) ) {
        return errorOut( 
          param, "ERROR: The specified matrix file does not exist." );
      } else {
        param.setValidFlag( true );
      }

      //validate the restrict runs value
      param          = ( IParameterGUI )super.getParameter( 9 );
      restrictRuns   = param.getValue(  )
                            .toString(  );
      param.setValidFlag( true );
      SharedData.addmsg( 
        "IndexJ is updating " + peaksName + " with " + matName );
      indexJOp.getParameter( 1 ).setValue( matName );
      indexJOp.getParameter( 2 ).setValue( restrictRuns );
      obj = indexJOp.getResult(  );

      if( obj instanceof ErrorString ) {
        return errorOut( "IndexJ failed: " + obj.toString(  ) );
      }
    } else {

      boolean appendToLog = false;

      for( int i = 0; i < runsArray.length; i++ ) {
        indexJOp.getParameter( 7 ).setValue( new Boolean( appendToLog ) );

        //load the run numbers.  We don't want to remove the leading zeroes!
        runNum  = gov.anl.ipns.Util.Numeric.Format.integerPadWithZero(
          runsArray[i], LsqrsJForm.RUN_NUMBER_WIDTH );
 
        matName = peaksDir + "ls" + expName + runNum + ".mat";

        if( !( new File( matName ).exists(  ) ) ) {
        return errorOut(
          param,
          "ERROR: No least squares matrix files exist.  " +
          "Please specify a matrix file." );
        }

        SharedData.addmsg( 
          "IndexJ is updating " + peaksName + " with " + matName );

        //call IndexJ for the current run number and matrix file
        indexJOp.getParameter( 1 ).setValue( matName );
        indexJOp.getParameter( 2 ).setValue( runNum );
        obj = indexJOp.getResult(  );

        if( obj instanceof ErrorString ) {
          return errorOut( "IndexJ failed: " + obj.toString(  ) );
        }

        appendToLog = true;  //save the previously logged run data
      }
    }

    //set the indexj log file parameter
    param = ( IParameterGUI )getParameter( 10 );
    param.setValue( obj );
    param.setValidFlag( true );
    SharedData.addmsg( "--- IndexJForm finished. ---" );

    return obj.toString(  );
  }

  /**
   * Method to listen for changes on the RadioButtonPG.
   */
  public void update( Object source, Object reason ) {
    String newVal = ((IParameterGUI)source).getStringValue(  );

    if( newVal.equals(FROM_FILE) ) {
      ( ( IParameterGUI )getParameter( 8 ) ).setEnabled( true );
      ( ( IParameterGUI )getParameter( 9 ) ).setEnabled( true );
    } else if( newVal.equals(FROM_LSQRS) ) {
      ( ( IParameterGUI )getParameter( 8 ) ).setEnabled( false );
      ( ( IParameterGUI )getParameter( 9 ) ).setEnabled( false );
    }
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
