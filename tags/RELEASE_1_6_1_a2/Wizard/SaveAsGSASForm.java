/*
 * File:  SaveAsGSASForm.java
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.15  2003/12/15 02:44:08  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.14  2003/09/11 21:21:44  bouzekc
 *  Updated to work with new Form class.
 *
 *  Revision 1.13  2003/08/14 19:43:56  bouzekc
 *  Fixed javadoc error.
 *
 *  Revision 1.12  2003/07/03 15:22:13  bouzekc
 *  Added and formatted class and javadoc comments.
 *
 *  Revision 1.11  2003/07/03 15:07:05  bouzekc
 *  Fixed odd CVS log entries due to double inclusion of the
 *  log header tag.
 *
 *  Revision 1.10  2003/06/25 20:24:43  bouzekc
 *  Unused private variables removed, reformatted for
 *  consistency.
 *
 *  Revision 1.9  2003/06/18 23:09:46  bouzekc
 *  Parameter error checking now handled by superclass Form.
 *
 *  Revision 1.8  2003/06/18 19:54:58  bouzekc
 *  Now pads run numbers less than 5 digits with zeroes.
 *  Uses errorOut() to indicate parameter errors.  More robust
 *  parameter error checking.  Now fires off property change
 *  events in a semi-intelligent way.  Uses super.getResult() for
 *  initializing PropertyChanger variables.
 *
 *  Revision 1.7  2003/06/03 23:04:28  bouzekc
 *  Fixed full constructor to avoid excessive garbage
 *  collection.
 *  Fixed documentation to reflect constructor
 *  parameter changes.
 *
 *  Revision 1.6  2003/06/02 22:25:05  bouzekc
 *  Fixed contact information.
 *  Added call to setDefaultParameters-needed to avoid
 *  NullPointerExceptions.
 *
 *  Revision 1.5  2003/04/24 18:57:56  pfpeterson
 *  Various small bug fixes. (Chris Bouzek)
 *
 *  Revision 1.4  2003/04/02 15:02:46  pfpeterson
 *  Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 *
 *  Revision 1.3  2003/03/19 15:07:04  pfpeterson
 *  Added the monitor DataSets as an explicit parameter and now creates
 *  filename from instrument name and run number. (Chris Bouzek)
 *
 *  Revision 1.2  2003/03/13 19:04:14  dennis
 *  Added log header to include revision information.
 *
 *  Revision 1.1  2003/03/11 19:49:52  pfpeterson
 *  Chris Bouzek's next version of the wizard.
 *
 */
package Wizard;

import java.io.Serializable;
import java.util.Vector;

import DataSetTools.dataset.DataSet;
import DataSetTools.operator.Operator;
import DataSetTools.operator.Generic.Save.WriteGSAS;
import DataSetTools.parameter.ArrayPG;
import DataSetTools.parameter.BooleanPG;
import DataSetTools.parameter.DataDirPG;
import DataSetTools.parameter.IParameterGUI;
import DataSetTools.parameter.InstNamePG;
import DataSetTools.parameter.IntArrayPG;
import DataSetTools.util.ErrorString;
import DataSetTools.util.IntList;
import DataSetTools.util.SharedData;
import DataSetTools.util.StringUtil;
import DataSetTools.wizard.Form;


/**
 * This class defines a form for saving DataSets in GSAS format under the
 * control of a Wizard.
 */
public class SaveAsGSASForm extends Form implements Serializable {
  //~ Constructors *************************************************************

  /**
   * Construct an SaveAsGSASForm to open a set of histograms which have been
   * time focused and grouped, and save them in GSAS format.
   */
  public SaveAsGSASForm(  ) {
    super( "Save as GSAS" );
    this.setDefaultParameters(  );
  }

  /**
   * Full constructor.  Uses the input parameters to create a SaveAsGSASForm
   * without the need to externally set the parameters.  getResult() may be
   * called immediately after using this constructor.
   *
   * @param tf_array The Vector of time focused histograms that you wish to
   *        save in GSAS format.
   * @param mon_array The Vector of monitor DataSets that you wish to use for
   *        the SaveAsGSAS operation.
   * @param run_nums The list of run numbers from the files that you loaded the
   *        histograms and monitors from.
   * @param inst_name The name of the instrument associated with the
   *        histograms.
   * @param gsas_dir The directory to store the GSAS files in.
   * @param export_mon Boolean indicating whether you want to export the
   *        monitor DataSets.
   * @param seq_num Boolean indicating whether you want to sequentially number
   *        the banks.
   */
  public SaveAsGSASForm( 
    Vector tf_array, Vector mon_array, String run_nums, String inst_name,
    String gsas_dir, boolean export_mon, boolean seq_num ) {
    this(  );
    getParameter( 0 )
      .setValue( tf_array );
    getParameter( 1 )
      .setValue( mon_array );
    getParameter( 2 )
      .setValue( run_nums );
    getParameter( 3 )
      .setValue( inst_name );
    getParameter( 4 )
      .setValue( gsas_dir );
    getParameter( 5 )
      .setValue( new Boolean( export_mon ) );
    getParameter( 6 )
      .setValue( new Boolean( seq_num ) );
  }

  //~ Methods ******************************************************************

  /**
   * @return the String command used for invoking this Form in a Script.
   */
  public String getCommand(  ) {
    return "SAVEASGSASFORM";
  }

  /**
   * Attempts to set reasonable default parameters for this form.
   */
  public void setDefaultParameters(  ) {
    parameters = new Vector(  );
    addParameter( 
      new ArrayPG( "Time focused histograms", new Vector(  ), false ) );
    addParameter( new ArrayPG( "Monitor DataSets", new Vector(  ), false ) );
    addParameter( new IntArrayPG( "Run Numbers", "12358", false ) );
    addParameter( new InstNamePG( "InstrumentName", null, false ) );
    addParameter( 
      new DataDirPG( "Directory to save GSAS files to", null, false ) );
    addParameter( new BooleanPG( "Export Monitor DataSet", false, false ) );
    addParameter( new BooleanPG( "Sequential bank numbering", false, false ) );
    setParamTypes( new int[]{ 0, 1, 2, 3 }, new int[]{ 4, 5, 6 }, null );
  }

  /**
   * @return documentation for this OperatorForm.  Follows javadoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer(  );

    s.append( "@overview This Form is designed for saving an ArrayPG of time " );
    s.append( "focused and grouped histograms into corresponding GSAS files, " );
    s.append( "under the control of a Wizard.\n" );
    s.append( "@assumptions It is assumed that the specified histograms " );
    s.append( "exist.  In addition, it is assumed that the a valid" );
    s.append( "monitor DataSet exists for each histogram.\n" );
    s.append( "@algorithm This Form converts each histogram to GSAS format " );
    s.append( "using the monitor DataSet and the user specifed parameters.  " );
    s.append( "Each histogram (which is a DataSet) is saved under a file " );
    s.append( "named <InstName><InstNum>.gsa (e.g. hrcs2447.gsa).\n" );
    s.append( "@param run_nums List of integers representing the runfile " );
    s.append( "numbers which you wish to load histograms from.\n" );
    s.append( "@param data_dir The directory from which to load the runfiles " );
    s.append( "from.\n" );
    s.append( 
      "@param tf_vector The Vector of time focused histograms that you " );
    s.append( "wish to save in GSAS format.\n" );
    s.append( 
      "@param mon_vector The Vector of monitor DataSets that you wish " );
    s.append( "to use for the SaveAsGSAS operation.\n" );
    s.append( "@param run_nums The run numbers from the files that you " );
    s.append( "loaded the histograms and monitors from.\n" );
    s.append( 
      "@param inst_name The name of the instrument associated with the " );
    s.append( "histograms.\n" );
    s.append( "@param gsas_dir The directory to store the GSAS files in.\n" );
    s.append( 
      "@param export_mon Boolean indicating whether you want to export " );
    s.append( "the monitor DataSets into the GSAS file.\n" );
    s.append( "@param seq_num Boolean indicating whether you want to " );
    s.append( "sequentially number the banks.\n" );
    s.append( "@return Presently, returns a Boolean which indicates either " );
    s.append( "success or failure.\n" );
    s.append( "@error Returns a Boolean false if any of the specified " );
    s.append( "histograms could not be saved in GSAS format.\n" );

    return s.toString(  );
  }

  /**
   * Saves the loaded time focused and grouped histograms in GSAS format.
   *
   * @return Boolean indicating success or failure.
   */
  public Object getResult(  ) {
    SharedData.addmsg( "Executing...\n" );

    ArrayPG gr_res;
    ArrayPG mds_pg;
    Vector grouped;
    Vector monitors;
    Operator op;
    Object obj;
    Object result;
    boolean export_mon;
    boolean seq_num;
    IParameterGUI param;
    DataSet mds;
    DataSet group_ds;
    String gsas_dir;
    String save_name;
    String inst_name;
    String runNum;
    int[] run_numbers;

    //get the results
    gr_res     = ( ArrayPG )super.getParameter( 0 );
    grouped    = ( Vector )gr_res.getValue(  );
    mds_pg     = ( ArrayPG )super.getParameter( 1 );
    monitors   = ( Vector )mds_pg.getValue(  );
    obj        = super.getParameter( 2 )
                      .getValue(  );
    run_numbers   = IntList.ToArray( obj.toString(  ) );
    inst_name     = ( ( IParameterGUI )super
                      .getParameter( 3 ) ).getValue(  )
                      .toString(  )
                      .toLowerCase(  );

    //get the user input parameters
    //get directory
    param      = ( IParameterGUI )super.getParameter( 4 );
    gsas_dir   = StringUtil.setFileSeparator( 
        param.getValue(  ).toString(  ) + "/" );

    param        = ( IParameterGUI )super.getParameter( 5 );
    export_mon   = ( ( BooleanPG )param ).getbooleanValue(  );

    param     = ( IParameterGUI )super.getParameter( 6 );
    seq_num   = ( ( BooleanPG )param ).getbooleanValue(  );

    Object validCheck = validateSelf(  );

    //had an error, so return
    if( validCheck instanceof ErrorString ) {
      return validCheck;
    }

    //set the increment amount
    increment = ( 1.0f / grouped.size(  ) ) * 100.0f;

    //go through the vector
    for( int i = 0; i < grouped.size(  ); i++ ) {
      //get the DataSet in each Vector "slot"
      group_ds   = ( DataSet )grouped.elementAt( i );
      mds        = ( DataSet )monitors.elementAt( i );

      runNum   = DataSetTools.util.Format.integerPadWithZero( 
          run_numbers[i], 5 );

      //save the GSAS file
      save_name   = gsas_dir + inst_name + runNum + ".gsa";
      op          = new WriteGSAS( 
          mds, group_ds, save_name, new Boolean( export_mon ),
          new Boolean( seq_num ) );
      result = op.getResult(  );

      if( result instanceof String && result.equals( "Success" ) ) {
        SharedData.addmsg( "File " + save_name + "saved." );
      } else {  //something went wrong

        return errorOut( "File " + save_name + "could not be saved." );
      }

      //fire a property change event off to any listeners
      oldPercent = newPercent;
      newPercent += increment;
      super.fireValueChangeEvent( ( int )oldPercent, ( int )newPercent );
    }

    SharedData.addmsg( "Finished saving GSAS files." );

    return Boolean.TRUE;
  }
}
