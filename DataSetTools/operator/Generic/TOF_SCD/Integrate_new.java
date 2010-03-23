/*
 * File:  Integrate_new.java 
 *
 * Copyright (C) 2002, Peter Peterson, Ruth Mikkelson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * Some of this work was supported by the National Science Foundation under
 * grant number DMR-0218882
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *  $Author$
 *  $Date$            
 *  $Revision$
 *
 * $Log$
 * Revision 1.12  2008/01/29 20:40:28  rmikk
 * Replaced Peak by IPeak
 * Used the IPeak Create methods instead of setting values in original peaks
 *
 * Revision 1.11  2007/03/13 22:04:11  rmikk
 * Made these implement HiddenOperator so they will not show up in the
 *    macros menu
 *
 * Revision 1.10  2006/12/19 05:19:03  dennis
 * Factored out basic integration methods and logging methods into
 * the classes
 *             Operators/TOF_SCD/IntegrateUtils   and
 *             Operators/TOF_SCD/SCD_LogUtils
 *
 * Revision 1.9  2006/07/10 16:26:01  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.8  2006/02/13 05:07:45  dennis
 * Now uses new constructor for VecQToTOF that takes a DataSet
 * and a DataGrid (ie. detector).  This fixes a problem with
 * integrating IPNS data from the second area detector.
 *
 * Revision 1.7  2006/02/12 20:53:10  dennis
 * Now gets the initial path from Data block, if the initial path
 * attribute is not set on the whole DataSet.  This fixes a problem
 * with using Integrate_new.java on IPNS data, since the initial path
 * was not set on the whole DataSet, but on individual Data blocks
 * for IPNS data.
 *
 * Revision 1.6  2006/02/06 19:55:00  dennis
 * Removed unused private method and unused import.
 *
 * Revision 1.5  2006/02/06 03:45:12  dennis
 * Now finds the range of hkl covered by each run & detector
 * using the SCD_util.DetectorToMinMaxHKL(), rather than blindly
 * processing all HKL values in a wide range.
 *
 * Revision 1.4  2006/01/18 21:40:23  dennis
 * Switched default intervals around peak centers back to original
 * total length of 5.
 * Added informational print out of the peak algorithm selected.
 *
 * Revision 1.3  2006/01/18 00:23:59  dennis
 * Workable adaptation of IPNS SCD integrate routine to LANSCE SCD.
 * Lowered dependence on Peter's Peak object, that is IPNS specific.
 * Both MaxISigI and Shoebox integrate routines working.  Missing
 * features are:
 *   1. Logging not working
 *   2. Centroiding peak after shifting to max value and integrating
 *   3. More efficient choice of range of hkl values
 * The Wizard form still needs to be modified to pass all of the
 * control parameters in to this operator.
 *
 * Revision 1.8  2006/01/16 04:50:35  rmikk
 * Added documentation for the d_min argument
 *
 * Revision 1.7  2005/03/06 00:29:03  dennis
 * Added methods to check d-spacing for peaks and remove peaks for which
 * the d-spacing is less than a specified minimum. (Requested by Art Schultz.)
 * Added d_min as parameter to this operator.
 * Added old CVS log messages which were removed when this operator was
 * checked in separately from the original integrate operator.
 *
 * Revision 1.6  2004/08/19 20:28:16  rmikk
 * Eliminated some annoying prints with the experimental methods
 *
 * Revision 1.5  2004/08/19 19:16:54  rmikk
 * All logging information is logged to the Global logging file in
 *   gov.anl.ipns.Util.Sys.SharedMessages
 *
 * Revision 1.4  2004/08/03 00:07:58  rmikk
 * Replaced INTGT by TOFINT
 *
 * Revision 1.3  2004/07/29 14:01:44  rmikk
 * Fixed javadoc error
 *
 * Revision 1.2  2004/06/23 20:39:54  rmikk
 * Now includes all the options from the first integrate operator.
 * Adds two new Integrate One Point add-ins and allows for an
 *    experimental option
 *
 * Revision 1.1  2004/06/18 22:22:20  rmikk
 * Initial Checkin 
 *
 * MODIFIED VERSION OF DataSetTools/operator/Generic/TOF_SCD/Integrate.java
 * OLD LOG MESSAGES ARE LISTED BELOW:
 *
 * Revision 1.37  2004/05/04 19:02:25  dennis
 * Now clears DataSetPG after getting value, to avoid memory leak.
 *
 * Revision 1.36  2004/03/15 03:28:38  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.35  2004/03/02 17:15:55  dennis
 * Moves some prints into  if (DEBUG) statement.
 *
 * Revision 1.34  2004/03/01 20:51:22  dennis
 * Minor efficiency improvement in checkReal()
 * Removed extraneous {...} in integrateDetector()
 *
 * Revision 1.33  2004/01/24 20:20:45  bouzekc
 * Removed unused variables and unused imports.  Made unused private method
 * getObs a public static method.
 *
 * Revision 1.32  2003/09/23 21:07:56  dennis
 * Fixed index out of bounds error with shoebox integration.  The maximum
 * y_value[] index was calculated incorrectly.
 *
 * Revision 1.31  2003/09/20 23:09:05  dennis
 * Finished first version of shoebox integration routine.
 * No longer includes extra slice before and after (in TOF)
 * the specified region.
 * Logging is done in the same style as the code that maximizes
 * I/sigI, however, all slices are integrated and included.
 *
 * Revision 1.30  2003/09/19 21:26:12  dennis
 * Does integration over full shoebox region, including one extra time
 * slice in each direction.  Logging is not complete for shoebox region
 * integration.  This still needs to modified to do full logging for
 * integration over shoebox region and to NOT include extra time slices
 * before and after the region integrated.
 *
 * Revision 1.29  2003/09/15 22:21:08  dennis
 * Added spaces to improve readability.
 *
 * Revision 1.28  2003/09/15 03:26:11  dennis
 * Added parameters that allow a constant (shoe box) region of integration
 * to be used for all peaks.  (This option is not implemented yet.)
 *
 * Revision 1.27  2003/09/15 02:05:52  dennis
 * 1. Renamed compItoDI() method to increasingIsigI()
 * 2. Added method is_significant() that checks if I > 2 * sigI
 *    AND I > 0.01 * total I.
 * 3. Slices are now integrated and included if either they increase I/sigI
 *    or if is_significant() is true.
 *
 * Revision 1.26  2003/08/27 23:17:44  bouzekc
 * Added constructor to set the centering type parameter (similar to other full
 * constructor).
 *
 * Revision 1.25  2003/07/07 15:54:25  bouzekc
 * Fixed errors and added missing param tags in
 * getDocumentation().  Renamed parameter from "Center" to
 * "Centering Type."
 *
 * Revision 1.24  2003/06/03 22:59:19  bouzekc
 * Fixed full constructor to avoid excessive garbage
 * collection.
 *
 * Revision 1.23  2003/06/03 15:15:12  bouzekc
 * Fixed some documentation errors.
 * Reformatted getDocumentation() to stay within 80 columns.
 * Added a full constructor so that an Integrate instance
 * can be created without the need to send in
 * IParameterGUIs.
 *
 * Revision 1.22  2003/06/03 15:02:21  pfpeterson
 * Catches an ArrayIndexOutOfBounds exception in IntegratePeak.
 *
 * Revision 1.21  2003/05/20 19:19:44  pfpeterson
 * Added append parameter.
 *
 * Revision 1.20  2003/05/08 20:01:57  pfpeterson
 * Removed some debug statements.
 *
 * Revision 1.19  2003/05/08 19:52:12  pfpeterson
 * First pass at expanding to multiple detectors. There is a bug where it
 * does not find any peaks in the -120 deg detector.
 *
 * Revision 1.18  2003/04/21 15:07:30  pfpeterson
 * Integrates a fixed box for each time slice where the default integration
 * gives I/dI<5. Added ability to specify a specifix matrix file. Small bug
 * fixes, documentation updates, and code cleanup.
 *
 * Revision 1.17  2003/04/16 20:42:19  pfpeterson
 * Moves peak center of each slice during integration.
 *
 * Revision 1.16  2003/04/15 18:54:08  pfpeterson
 * Added option to increase integration size of time slice after maximizing
 * the ratio of I/dI for the time slice.
 *
 * Revision 1.15  2003/04/14 18:50:46  pfpeterson
 * Fixed bug where observed intensity was not always found.
 *
 * Revision 1.14  2003/04/14 16:14:05  pfpeterson
 * Reworked code that integrates time slices to parameterize the number
 * of slices to integrate.
 *
 * Revision 1.13  2003/04/11 16:52:59  pfpeterson
 * Added parameter to specify how many peaks are listed in log file.
 *
 * Revision 1.12  2003/03/26 16:29:37  pfpeterson
 * Fixed background calculation. Small code reorganization for improved
 * log writting.
 *
 * Revision 1.11  2003/03/25 22:36:34  pfpeterson
 * Corrected a spelling error.
 *
 * Revision 1.10  2003/03/20 22:05:51  pfpeterson
 * Added logfile to operator and added a couple of minor features.
 *
 * Revision 1.9  2003/03/14 21:19:26  pfpeterson
 * Added hooks for writing out a logfile.
 *
 * Revision 1.8  2003/03/14 16:18:13  pfpeterson
 * Added option to choose centering condition.
 *
 * Revision 1.7  2003/02/18 22:45:57  pfpeterson
 * Updated deprecated method.
 *
 * Revision 1.6  2003/02/18 20:21:01  dennis
 * Switched to use SampleOrientation attribute instead of separate
 * phi, chi and omega values.
 *
 * Revision 1.5  2003/02/13 17:04:33  pfpeterson
 * Added proper logic for summing of slice integrations.
 *
 * Revision 1.4  2003/02/12 21:48:47  dennis
 * Changed to use PixelInfoList instead of SegmentInfoList.
 *
 * Revision 1.3  2003/02/12 15:29:56  pfpeterson
 * Various improvements to user interface including autimatically loading
 * the oirentation matrix and calibration from experiment file if not
 * already preset, and writting the resultant peaks to a file.
 *
 * Revision 1.2  2003/02/10 16:03:41  pfpeterson
 * Fixed semantic error.
 *
 * Revision 1.1  2003/01/30 21:07:23  pfpeterson
 * Added to CVS.
 *  
 */
package DataSetTools.operator.Generic.TOF_SCD; 

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.instruments.*;
import DataSetTools.util.*;
import DataSetTools.trial.*;
import DataSetTools.retriever.RunfileRetriever;
import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Parameters.BooleanPG;
import gov.anl.ipns.Parameters.ChoiceListPG;
import gov.anl.ipns.Parameters.FloatPG;
import gov.anl.ipns.Parameters.IntArrayPG;
import gov.anl.ipns.Parameters.IntegerPG;
import gov.anl.ipns.Parameters.LoadFilePG;
import gov.anl.ipns.Parameters.SaveFilePG;
import gov.anl.ipns.Util.Sys.*;

import java.io.*;
import java.util.Vector;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import Operators.TOF_SCD.*;
import DataSetTools.operator.Generic.TOF_SCD.IPeak;
/** 
 * This is a ported version of A.J.Schultz's INTEGRATE program. 
 */
public class Integrate_new extends GenericTOF_SCD implements HiddenOperator{
  private static final String       TITLE       = "Integrate_new";
  private static       boolean      DEBUG       = false;
  private static       Vector       choices     = null;
  private              StringBuffer logBuffer   = null;
  private              int          listNthPeak = 3;
  private              int          centering   = 0;
  public static        String       OLD_INTEGRATE ="MaxIToSigI-old";
  public static        String       NEW_INTEGRATE ="MaxIToSigI";
  public static        String       TOFINT        ="TOFINT";
  public static        String       EXPERIMENTAL  ="EXPERIMENTAL";
  public static        String       SHOE_BOX      = "Shoe Box";
    
  
  /**
   * how much to increase the integration size after I/dI has been maximized
   */
  private              int          incrSlice   = 0;
  /**
   * uncertainty in the peak location
   */
  private static       int dX=2, dY=2, dZ=1;
  /**
   * integration range in z
   */
  private              int[] timeZrange={-1,3};  
  /**
   * integration range in x
   */
  private              int[] colXrange={-2,2};  
  /**
   * integration range in y
   */
  private              int[] rowYrange={-2,2};   

  private String PeakAlg = "MaxItoSigI";
  
  //private  IntegratePt opIntPt = new IntegratePt();

  /* ------------------------ Default constructor ------------------------- */ 
  /**
   * Creates operator with title "Integrate" and a default list of
   * parameters.
   */  
  public Integrate_new(){
    super( TITLE );
  }
  
  /** 
   * Creates operator with title "Integrate_new" and the specified list
   * of parameters. The getResult method must still be used to execute
   * the operator.
   *
   * @param ds DataSet to integrate
   */
  public Integrate_new( DataSet ds ){
    this(); 

    getParameter(0).setValue(ds);
    // parameter 1 keeps its default value
    // parameter 2 keeps its default value
    // parameter 3 keeps its default value
    // parameter 4 keeps its default value
    // parameter 5 keeps its default value
    // parameter 6 keeps its default value
  }

  /** 
   * Creates operator with title "Integrate_new" and the specified list
   * of parameters. The getResult method must still be used to execute
   * the operator.  This is a convenience constructor so that a full
   * Integrate_new Operator can be constructed without the need to 
   * pass in IParameterGUIs.
   *
   * @param ds          DataSet to integrate
   * @param integfile   The "integrate file" for the analysis.
   * @param matfile     The matrix file to use for the analysis.
   * @param slicerange  The time slice range.
   * @param slicedelta  The amount to increase slicesize by.
   * @param lognum      The peak multiples to log - i.e. 3 logs
   *                    1, 3, 6, 9...
   * @param append      Append to file (true/false);
   * @param PeakAlg     Name of algorithm used to integrate one peak
   * @param box_x_range The range of x (delta col) values to use around the  
   *                    peak position
   * @param box_y_range The range of y (delta row) values to use around the 
   *                    peak position
   * @param max_shoebox Maximum for shoebox integration
   */
  public Integrate_new( DataSet ds, 
                     String  integfile, 
                     String  matfile,
                     String  slicerange, 
                     int     slicedelta, 
                     int     lognum,
                     boolean append,
                     String  PeakAlg,
                     String  box_x_range,
                     String  box_y_range, 
                     float   max_shoebox )
  {
    this(ds); 

    getParameter(1).setValue(integfile);
    getParameter(2).setValue(matfile);
    getParameter(4).setValue(slicerange);
    getParameter(5).setValue(new Integer(slicedelta));
    getParameter(7).setValue(new Integer(lognum));
    getParameter(8).setValue(new Boolean(append));
    getParameter(9).setValue(new String( PeakAlg));
    getParameter(10).setValue(box_x_range);
    getParameter(11).setValue(box_y_range);
    getParameter(12).setValue(max_shoebox);
  }

  /** 
   * Creates operator with title "Integrate_new" and the specified list
   * of parameters. The getResult method must still be used to execute
   * the operator.  This is a convenience constructor so that a full
   * Integrate_new Operator can be constructed without the need to 
   * pass in IParameterGUIs.
   *
   * @param ds          DataSet to integrate
   * @param integfile   The "integrate file" for the analysis.
   * @param matfile     The matrix file to use for the analysis.
   * @param choice      number for the centering type
   * @param slicerange  The time slice range.
   * @param slicedelta  The amount to increase slicesize by.
   * @param d_min       Minimum d-spacing to use
   * @param lognum      The peak multiples to log - i.e. 3 logs
   *                    1, 3, 6, 9...
   * @param append      Append to file (true/false);
   * @param PeakAlg     String to specify using same-size shoebox around 
   *                    all peaks, rather than trying to maximize I/sigI
   * @param box_x_range The range of x (delta col) values to use around the 
   *                    peak position
   * @param box_y_range The range of y (delta row) values to use around the
   *                    peak position
   * @param max_shoebox Maximum for shoebox integration
   */
  public Integrate_new( DataSet ds, 
                     String  integfile, 
                     String  matfile,
                     int     choice, 
                     String  slicerange, 
                     int     slicedelta, 
                     float   d_min,
                     int     lognum,
                     boolean append,
                     String  PeakAlg,
                     String  box_x_range,
                     String  box_y_range, 
                     float   max_shoebox )
  {
    this(ds,
         integfile, matfile, 
         slicerange, slicedelta, 
         lognum, 
         append, 
         PeakAlg,
         box_x_range, box_y_range, max_shoebox ); 
    getParameter(3).setValue(choices.elementAt(choice));
    getParameter(6).setValue( new Float(d_min) );
  }
  
  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts: SCDIntegrate
   * 
   * @return  "SCDIntegrate_new", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "SCDIntegrate_new";
  }
  
  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets default values for the parameters.  This must match the data types 
   * of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();

    if( choices==null || choices.size()==0 ) init_choices();

    // parameter(0)
    addParameter( new DataSetPG("Data Set", null ) );

    // parameter(1)
    SaveFilePG sfpg=new SaveFilePG("Integrate File",null);
    sfpg.setFilter(new IntegrateFilter());
    addParameter(sfpg);

    // parameter(2)
    LoadFilePG lfpg=new LoadFilePG("Matrix File",null);
    lfpg.setFilter(new MatrixFilter());
    addParameter( lfpg );//new Parameter("Path",new DataDirectoryString()) );

    // parameter(3)
    ChoiceListPG clpg=new ChoiceListPG("Centering Type", choices.elementAt(0));
    clpg.addItems(choices);
    addParameter(clpg);

    // parameter(4)
    addParameter(new IntArrayPG("Time Slice Range","-1:3"));

    // parameter(5)
    addParameter(new IntegerPG("Increase Slice Size by",0));

    // parameter(6)
    addParameter(new FloatPG("Minimum d-spacing", 0));

    // parameter(7)
    addParameter(new IntegerPG("Log Every nth Peak",3));

    // parameter(8)
    addParameter(new BooleanPG("Append",false));

    // parameter(9)
    ChoiceListPG clPG = new ChoiceListPG( "Integrate 1 peak method",
                                           NEW_INTEGRATE);
    clPG.addItem(SHOE_BOX);
    clPG.addItem(OLD_INTEGRATE);
    
    clPG.addItem(TOFINT);
    clPG.addItem(EXPERIMENTAL);
	
    addParameter(clPG);

    // parameter(10)
    addParameter(new IntArrayPG("Box Delta x (col) Range","-2:2"));

    // parameter(11)
    addParameter(new IntArrayPG("Box Delta y (row) Range","-2:2"));

    // parameter(12)
    addParameter(new FloatPG("min for shoebox integration",0));
  }
  
  /**
   * Returns the documentation for this operator as a string. The
   * format follows standard JavaDoc conventions.
   */
  public String getDocumentation(){
    StringBuffer sb=new StringBuffer("");

    // overview
    sb.append("@overview This operator is a direct port of A.J.Schultz's ");
    sb.append("INTEGRATE program. This locates peaks in a DataSet for a ");
    sb.append("given orientation matrix and then determines the integrated ");
    sb.append("peak intensity.\n");
    // assumptions
    sb.append("@assumptions The matrix file must exist and be user ");
    sb.append("readable.  Also, the directory containing the matrix file ");
    sb.append("must be user writeable for the integrated intensities and log ");
    sb.append("file.\n");
    // algorithm
    sb.append("@algorithm First this Operator loads all the parameters.\n");
    sb.append("Then it gets a list of detectors and determines the unique ");
    sb.append("ones.  Next it determines the sample orientation and ");
    sb.append("orientation matrix.  Then it determines the initial flight ");
    sb.append("and integrates the peaks.  Finally it writes the integrated ");
    sb.append("peaks file.\n");
    // parameters
    sb.append("@param ds DataSet to integrate.\n");
    sb.append("@param intfile The integrate file to write to.\n");
    sb.append("@param matfile The matrix file to use.\n");
    sb.append("@param centerType The centering type to use (e.g. primitive, ");
    sb.append("a-centered, b-centered, etc.).\n");
    sb.append("@param timeSlice The time slice range to use.\n");
    sb.append("@param sliceDelta The incremental amount to increase the ");
    sb.append("slice size by.\n");
    sb.append("@param  d_min  the minimum d-spacing allowed");
    sb.append("@param logNPeak Log the \"nth\" peak.\n");
    sb.append("@param append Whether to append to the file.\n");
    sb.append("@param Integrate method: Either MaxIToSigI-old,MaxIToSigI,");
    sb.append( "TOFINT,EXPERIMENTAL, or Shoe Box");
    sb.append("@param Xrange the range of dx's around an x for shoe box method");
    sb.append("@param Yrange the range of dy's around a y for shoe box");
    
    // return
    sb.append("@return The name of the file that the integrated intensities ");
    sb.append("are written to.\n");
    // errors
    sb.append("@error First parameter is not a DataSet or the DataSet is ");
    sb.append("empty.\n");
    sb.append("@error Second parameter is null or does not specify a valid ");
    sb.append("integrate file.\n");
    sb.append("@error Third parameter is null or does not specify an ");
    sb.append("existing matrix file.\n");
    sb.append("@error When any errors occur while reading the matrix ");
    sb.append("file.\n");
    sb.append("@error Invalid time range specified.\n");
    sb.append("@error No detector calibration found in the DataSet.\n");
    sb.append("@error No orientation matrix found in the DataSet.\n");
    sb.append("@error No detector number found.\n");
    sb.append("@error When the initial flight path is zero.\n");

    return sb.toString();
  }

  /** 
   *  Executes this operator using the values of the current parameters.
   *
   *  @return If successful, this operator returns a vector of Peak
   *  objects.
   */
  public Object getResult(){
    Object val=null; // used for dealing with parameters
    String integfile=null;
    DataSet ds;
    String matfile=null;
    this.logBuffer=new StringBuffer();

    // first get the DataSet
    val=getParameter(0).getValue();                        // Parameter 0 ****
    ((DataSetPG)getParameter(0)).clear();    // needed to avoid memory leak

    if( val instanceof DataSet){
      if(((DataSet)val).getNum_entries()>0)
        ds=(DataSet)val;
      else
        return new ErrorString("Specified DataSet is empty");
    }else{
      return new ErrorString("Value of first parameter must be a dataset");
    }
    //String logfile = null;

    // then the integrate file
    val=getParameter(1).getValue();                        // Parameter 1 ****
    if(val!=null){
      integfile=val.toString();
      if(integfile.length()<=0)
        return new ErrorString("Integrate filename is null");
      integfile=FilenameUtil.setForwardSlash(integfile);
      //logfile=integfile;
      
      
    }else{
      //return new ErrorString("Integrate filename is null");
    }

    // then get the matrix file
    val=getParameter(2).getValue();                        // Parameter 2 ****
    if(val!=null){
      matfile=FilenameUtil.setForwardSlash(val.toString());
      File dir=new File(matfile);
      if(dir.isDirectory())
        matfile=null;
    }

    // then the centering condition
    val=getParameter(3).getValue().toString();             // Parameter 3 **** 
    centering=choices.indexOf(val);
    if( centering<0 || centering>=choices.size() ) centering=0;

    // then the time slice range
    {
      int[] myZrange=((IntArrayPG)getParameter(4)).getArrayValue();// Param 4 **
      if(myZrange!=null && myZrange.length>=2){
        timeZrange[0]=myZrange[0];
        timeZrange[1]=myZrange[myZrange.length-1];
      }else{
        return new ErrorString("Invalid time range specified");
      }
    }

    // then how much to increase the integration size
    incrSlice=((IntegerPG)getParameter(5)).getintValue();          // Param 5 **

    // then the d_min value
    float d_min = ((FloatPG)getParameter(6)).getfloatValue();      // Param 6 **

    // then how often to log a peak
    listNthPeak=((IntegerPG)getParameter(7)).getintValue();        // Param 7 **

    // then whether to append
    boolean append=((BooleanPG)getParameter(8)).getbooleanValue(); // Param 8 **
      
    PeakAlg =getParameter(9).getValue().toString();                // Param 9 **

    // then the x range
    {
      int[] myXrange=((IntArrayPG)getParameter(10)).getArrayValue();//Param 10**
      if(myXrange!=null && myXrange.length>=2){
        colXrange[0]=myXrange[0];
        colXrange[1]=myXrange[myXrange.length-1];
      }else{
        return new ErrorString("Invalid X range specified");
      }
    }

    // then the y range
    {
      int[] myYrange=((IntArrayPG)getParameter(11)).getArrayValue();//Param 11**
      if(myYrange!=null && myYrange.length>=2){
        rowYrange[0]=myYrange[0];
        rowYrange[1]=myYrange[myYrange.length-1];
      }else{
        return new ErrorString("Invalid Y range specified");
      }
    }
    float max_shoebox = ((FloatPG)getParameter(12)).getfloatValue();      // Param 12 **

   if ( DEBUG )
   {
     System.out.println("Peak Method = " + PeakAlg);
     System.out.println("X range = " + colXrange[0] + " to " + colXrange[1] );
     System.out.println("Y range = " + rowYrange[0] + " to " + rowYrange[1] );
     System.out.println("Z range = " + timeZrange[0] + " to " + timeZrange[1] );
   }
  
    // add the parameter values to the logBuffer
    logBuffer.append("---------- PARAMETERS\n");
    logBuffer.append(getParameter(0).getName()+" = "+ds.toString()+"\n");
    logBuffer.append(getParameter(1).getName()+" = "+integfile+"\n");
    logBuffer.append(getParameter(2).getName()+" = "+matfile+"\n");
    logBuffer.append(getParameter(3).getName()+" = "
                     +choices.elementAt(centering)+"\n");
    logBuffer.append(getParameter(4).getName()+" = "+timeZrange[0]+" to "
                     +timeZrange[1]+"\n");
    logBuffer.append(getParameter(5).getName()+" = "+incrSlice+"\n");
    logBuffer.append("Adjust center to nearest point with dX="+dX+" dY="+dY
                     +" dZ="+dZ+"\n");

    Data data = ds.getData_entry( 0 );

    // get the orientation matrix
    float[][] UB=null;
    if(matfile==null){ // only do this if matrix file not specified
      Object UBval=null;
      UBval=data.getAttributeValue(Attribute.ORIENT_MATRIX);
      if(UBval==null)
        UBval=ds.getAttributeValue(Attribute.ORIENT_MATRIX);
      if(UBval!=null && UBval instanceof float[][])
        UB=(float[][])UBval;
      UBval=null;
    }
    if( DEBUG)
       System.out.println("MatFile:"+matfile);
    if(UB==null){ // try loading it
      LoadOrientation loadorient=new LoadOrientation(ds, matfile);
      Object res=loadorient.getResult();
      if(res instanceof ErrorString) return res;
      // try getting the value again

      Object UBval=null;
      UBval=data.getAttributeValue(Attribute.ORIENT_MATRIX);
      if(UBval==null)
        UBval=ds.getAttributeValue(Attribute.ORIENT_MATRIX);
      if(UBval!=null && UBval instanceof float[][])
        UB=(float[][])UBval;
      UBval=null;
      if(UB==null) // now give up if it isn't there
        return new ErrorString("Could not load orientation matrix");
    }
    System.out.println("UB matrix is " );
    LinearAlgebra.print( UB );
   
    Object Res = integrate( ds,centering,timeZrange,incrSlice,
        d_min,listNthPeak,PeakAlg,colXrange,rowYrange,max_shoebox,10000, logBuffer);
    
    if( Res == null || !(Res instanceof Vector))
       return Res;
    //!!!!! Move this to end of getResult method.
   // String errmsg=this.writeLog(logfile,append);
  //  if(errmsg!=null)
  //    SharedData.addmsg(errmsg);
   
    // write out the peaks
    WritePeaks writer = new WritePeaks( integfile, (Vector)Res, append );
    gov.anl.ipns.Util.Sys.SharedMessages.LOGaddmsg(logBuffer.toString());
    return writer.getResult();
   
  }
  
  /**
   * Integrates one data set
   * 
   * @param ds          The data set with the information to integrate with 
   *                    the orientation attribute set
   * @param centering   integer 0->6 where primitive is 0, a centered is 1,
   *                    b centered is 2,c centered is 3,[f]ace centered is 4,
   *                    [i] body centered is 5, and [r]hombohedral centered is 6
   * @param timeZrange  left and right offset around Peak time channel to 
   *                    consider
   * @param incrSlice   The incremental amount to increase the slice size by.
   * @param d_min       the minimum d-spacing allowed
   * @param listNthPeak Log every nth peak
   * @param PeakAlg     Peak Algorithm identifier. Use only the Strings below
   *                    MaxItoSigI, Shoe Box, MaxIToSigI-old, TOFINT                  
   * @param colXrange   left and   right offset around Peak column to consider
   * @param rowYrange   left and right offset around Peak row  to consider
   * @param monCount    Monitor Count
   * @param logbuffer   if this a non-null StringBuffer, the log information
   *                    will be appended to it.
   * @return            A Vector containing all peaks that were integrated,
   *                    or an ErrorString if something went wrong.
   */
  public static Object integrate( DataSet ds, 
                                  int     centering,
                                  int[]   timeZrange, 
                                  int     incrSlice, 
                                  float   d_min, 
                                  int     listNthPeak,
                                  String  PeakAlg, 
                                  int[]   colXrange, 
                                  int[]   rowYrange, 
                                  float   max_shoebox,
                                  float   monCount,
                                  Object  logbuffer)
  {
    float[][]    UB;
      
    // get list of detectors
    int[] det_number = Grid_util.getAreaGridIDs(ds);

    if( det_number == null || det_number.length <= 0 )
      return new ErrorString("Could not determine detector numbers");
    
    Data data = ds.getData_entry( 0 );
    UB=(float[][])ds.getAttributeValue(Attribute.ORIENT_MATRIX);
    if( UB == null)
       UB= (float[][])data.getAttributeValue(Attribute.ORIENT_MATRIX);
    if( UB ==  null)
       return new ErrorString("UB matrix is not loaded into the dataset");

    if(DEBUG){
       System.out.println("DataSet:"+ds);
       System.out.print(  "DetNum :");
       for( int i=0 ; i<det_number.length ; i++ )
         System.out.print(det_number[i]+" ");
       System.out.println();
     }

    IntegratePt opIntPt = new IntegratePt();

    if( PeakAlg.equals(Integrate_new.OLD_INTEGRATE))
       opIntPt.setIntgratePkOp(new INTEG(),1,1,1);
    else if( PeakAlg.equals(Integrate_new.TOFINT))
       opIntPt.setIntgratePkOp(new TOFINT(),1,1,1);

    opIntPt.setDataSet(ds);
   
    // determine the initial flight path
    float init_path = AttrUtil.getInitialPath( ds );
    if ( Float.isNaN(init_path) )
      init_path = AttrUtil.getInitialPath( data );
    if ( Float.isNaN(init_path) )
      return new ErrorString("initial flight path is zero");

    // create a vector for the results
    Vector peaks=new Vector();
   
    // integrate each detector
    Vector innerPeaks=null;
    ErrorString error=null;
    for( int i = 0; i < det_number.length; i++ ){

      System.out.println("Processing detector " + det_number[i] );
     
      innerPeaks=new Vector();
      error=Integrate_new.integrateDetector(ds,
                                            innerPeaks,
                                            det_number[i],
                                            d_min,
                                            listNthPeak,
                                            centering, 
                                            UB,
                                            PeakAlg,
                                            timeZrange, 
                                            colXrange,rowYrange,
                                            max_shoebox,
                                            incrSlice,
                                            opIntPt,
                                            logbuffer) ;
      if(DEBUG) System.out.println("ERR="+error);
      if(error!=null) return error;
      if(DEBUG) System.out.println("integrated "+innerPeaks.size()+" peaks");
      if( innerPeaks.size()>0)
        peaks.addAll(innerPeaks);
    }

    for( int i=0; i< peaks.size(); i++){                // this is done in
       ((IPeak)peaks.elementAt( i )).monct( monCount);  // peak constructor
    }
       
    return peaks;
  }


  // ========== start of detector dependence
  //TODO eliminate UB, chi, phi, omega and opIntPt.
  //               Make public (for parallel)
  private static ErrorString integrateDetector(
                                        DataSet     ds, 
                                        Vector      peaks, 
                                        int         detnum,
                                        float       d_min,
                                        int         listNthPeak, 
                                        int         centering,
                                        float       UB[][],
                                        String      PeakAlg,
                                        int[]       timeZrange, 
                                        int[]       colXrange,
                                        int[]       rowYrange,
                                        float       max_shoebox,
                                        int         incrSlice,
                                        IntegratePt opIntPt,
                                        Object      logbuffer)
  {
    if(DEBUG) System.out.println("Integrating detector new "+detnum);
    StringBuffer logBuffer = new StringBuffer();
    
    // get the run number
    int nrun=0;
    int[] run_numbers = AttrUtil.getRunNumber(ds);
    if ( run_numbers != null && run_numbers.length > 0 )
      nrun = run_numbers[0];
   
    if(detnum<=0)
      return new ErrorString("invalid detector number: "+detnum);
    
    // create the lookup table
    int[][] ids=Util.createIdMap(ds,detnum);
    if(ids==null)
      return new ErrorString("Could not create pixel map for det "+detnum);

    // determine the boundaries of the matrix
    int[] rcBound=IntegrateUtils.getBounds(ids);
    for( int i=0 ; i<4 ; i++ ){
      if(rcBound[i]==-1)
        return new ErrorString("Bad boundaries on row column matrix");
    }

    // grab pixel 1,1 to get some 'global' attributes from 
    Data data=ds.getData_entry(ids[rcBound[0]][rcBound[1]]);
    if(data==null)
      return new ErrorString("no minimum pixel found");

    // get the xscale from the data to give to the new peaks objects
    XScale times=data.getX_scale();

    IDataGrid grid = Grid_util.getAreaGrid( ds, detnum ); 
    if ( grid == null )
      throw new IllegalArgumentException(
                "DataSet doesn't have detector grid in Integrate_new");
 
                                                // NOTE: to find hkl ranges,
    if ( grid instanceof RowColGrid )           // we need a uniform grid, so
    {                                           // make one now, if necessary
      UniformGrid ugrid = new UniformGrid( grid.ID(),
                                           grid.units(),
                                           grid.position(),
                                           grid.x_vec(),
                                           grid.y_vec(),
                                           grid.width(),
                                           grid.height(),
                                           grid.depth(),
                                           grid.num_rows(),
                                           grid.num_cols() );
      ugrid.setData_entries( ds );
      grid = ugrid;
    }

    // determine the min and max pixel-times
    int zmin=0;
    int zmax=times.getNum_x()-1;
    
    // get the sample orientation  
    SampleOrientation samp_or = AttrUtil.getSampleOrientation( ds );
    if ( samp_or == null )
      samp_or = AttrUtil.getSampleOrientation( data );
    float phi   = 0;
    float chi   = 0;
    float omega = 0;
    if ( samp_or != null )
    {
      phi   = samp_or.getPhi();
      chi   = samp_or.getChi();
      omega = samp_or.getOmega();
    }
    else
    {
      System.out.println("WARNING: Making default SNS SampleOrientation");
      samp_or = new DataSetTools.instruments.SNS_SampleOrientation(0f, 0f, 0f);
      ds.setAttribute( new SampleOrientationAttribute (
                                      Attribute.SAMPLE_ORIENTATION,
                                      samp_or) );
    }

    // add sample orientation and detector info to logBuffer
    logBuffer.append("---------- PHYSICAL PARAMETERS\n");
    logBuffer.append("RUN NUMBER :        " + nrun + "\n" );
    logBuffer.append("DETECTOR ID:        " + grid.ID() + "\n");
    logBuffer.append("SAMPLE ORIENTATION: chi = " +
                      chi + "  phi = " + phi + "  omega = " + omega + "\n");

    logBuffer.append("COLUMN/ROW RANGE:   "
                     +rcBound[0]+" "+rcBound[1]+"   "
                     +rcBound[2]+" "+rcBound[3]+"\n");
    //logBuffer.append("detD="+detD+"  detA="+detA+"  detA2="+detA2+"\n");
    logBuffer.append("detD:      " + 100 * grid.position().length() + "\n" );

    String format = "%10.5f %10.5f %10.5f\n";
    float[] coord = grid.position().get();
    String  out = String.format("Center:  " + format,
                            coord[0] * 100, coord[1] * 100, coord[2] * 100 );
    logBuffer.append( out );

    coord = grid.x_vec().get();
    out = String.format("Base:    " + format, coord[0], coord[1], coord[2] );
    logBuffer.append( out );

    coord = grid.y_vec().get();
    out = String.format("Up:      " + format, coord[0], coord[1], coord[2] );
    logBuffer.append( out );

    // determine the detector limits in hkl
    VecQToTOF transformer = new VecQToTOF( ds, grid );

    float UB_times_2PI[][] = new float[3][3];
    for ( int row = 0; row < 3; row++ )
      for ( int col = 0; col < 3; col++ )
        UB_times_2PI[row][col] = (float)( 2 * Math.PI * UB[row][col] );

    Tran3D orientation_tran = new Tran3D( UB_times_2PI );
    Tran3D inv_orientation_tran = new Tran3D( UB_times_2PI );
    inv_orientation_tran.invert();
    
    XScale x_scale = ds.getData_entry(0).getX_scale();

    float initial_path = AttrUtil.getInitialPath( ds );  // get initial path
    int index = 0;                                       // from ds or data
    while ( Float.isNaN( initial_path ) && index < ds.getNum_entries() )
    {
      initial_path = AttrUtil.getInitialPath( ds.getData_entry(index) );
      index++;
    }
    
    float t0;
    t0 = AttrUtil.getT0Shift( ds );
    if ( Float.isNaN( t0 ) )
      t0 = 0;

    float min_tof = x_scale.getStart_x();
    float max_tof = x_scale.getEnd_x();
    if ( min_tof < 1000 )
    {
      System.out.println("WARNING: setting min_tof to 1000, not " + min_tof );
      min_tof = 1000;
    }
    if ( max_tof > 16666 )
    {
      System.out.println("WARNING: setting max_tof to 16666, not " + max_tof );
      max_tof = 16666;
    }

    Tran3D gonRot    = samp_or.getGoniometerRotation();
    Tran3D gonRotInv = samp_or.getGoniometerRotationInverse();

    Vector3D min_max_hkl[] = SCD_util.DetectorToMinMaxHKL(
                                        grid,
                                        initial_path,
                                        min_tof,
                                        max_tof,
                                        gonRotInv,
                                        inv_orientation_tran );

    // TODO add another string buffer
    // System.out.println("MIN, MAX HKL FOR GRID ID " + grid.ID() );
    // System.out.println("  MIN: " + min_max_hkl[0] );
    // System.out.println("  MAX: " + min_max_hkl[1] );
    
    int min_h = Math.round(min_max_hkl[0].get()[0]) - 1;
    int max_h = Math.round(min_max_hkl[1].get()[0]) + 1;
    int min_k = Math.round(min_max_hkl[0].get()[1]) - 1;
    int max_k = Math.round(min_max_hkl[1].get()[1]) + 1;
    int min_l = Math.round(min_max_hkl[0].get()[2]) - 1;
    int max_l = Math.round(min_max_hkl[1].get()[2]) + 1;
/*
    System.out.println("min, max h = " + min_h + ", " + max_h);
    System.out.println("min, max k = " + min_k + ", " + max_k);
    System.out.println("min, max l = " + min_l + ", " + max_l);
*/
    // add the limits to the logBuffer
    logBuffer.append("---------- LIMITS\n");
    logBuffer.append("min hkl,  max hkl : " 
                     + min_h +" "+ min_k +" "+ min_l +"   "
                     + max_h +" "+ max_k +" "+ max_l +"\n");

    // add information about integrating the peaks
    logBuffer.append("\n");
    logBuffer.append("========== PEAK INTEGRATION ==========\n");
    logBuffer.append("listing information about every "+listNthPeak+" peak\n");

    boolean printPeak=false; // REMOVE
    IPeak peak=null;

/* DEBUG....
    if ( grid.ID() == 2 )
    {
      Vector3D  q_lab = new Vector3D();      // Q vector in "laboratory
                                             // coordinates
      Vector3D  q_crystal = new Vector3D();  // Q vector un-rotated by 
                                             // goniometer rotation, to
                                             // be in coordinate system 
                                             // attached to the crystal

      System.out.println("REVERSED Mapping h,k,l = -1,1,-4 TO row_col_ch");
      Vector3D hkl_vec = new Vector3D( -1,1,-4 );
      System.out.println("hkl = hkl_vec " + hkl_vec );

      orientation_tran.apply_to( hkl_vec, q_crystal );
      System.out.println("XTAL q_vec = " + q_crystal );

      gonRot.apply_to( q_crystal, q_lab );
      System.out.println("q_lab = " + q_lab );

      float row_col_tof[] = transformer.QtoRowColTOF( q_crystal );
      System.out.println( "row,col,chan = " + row_col_tof[0] + ", " +
                                            + row_col_tof[1] + ", " +
                                            + row_col_tof[2] );
      float row = row_col_tof[0];
      float col = row_col_tof[1];
      Vector3D pixel_vec = grid.position( row, col );

      Vector3D temp_hkl_vec = SCD_util.RealToHKL( pixel_vec,
                                             initial_path,
                                             row_col_tof[2],
                                             gonRotInv,
                                             inv_orientation_tran );

      System.out.println("Returned hkl = " + temp_hkl_vec );


      q_lab = tof_calc.DiffractometerVecQ( pixel_vec, 
                                           initial_path, row_col_tof[2] );
      System.out.println("q_lab = " + q_lab );

      gonRotInv.apply_to( q_lab, q_crystal );
      System.out.println("XTAL q_vec = " + q_crystal );

      inv_orientation_tran.apply_to( q_crystal, temp_hkl_vec );
      System.out.println("Recalculated  hkl = " + temp_hkl_vec );
    }

   END DEBUG....
*/

    int seqnum=1;
    // loop over all of the possible hkl values and create peaks
    for( int h = min_h; h <= max_h; h++ ){
      for( int k = min_k; k <= max_k; k++ ){
        for( int l = min_l; l <= max_l; l++ )
         {
          if( h==0 && k==0 && l==0 ) continue; // cannot have h=k=l=0

                                       //remove if fails centering conditions
          if( ! IntegrateUtils.checkCenter(h,k,l,centering) ){
            if(printPeak) System.out.println(" NO CENTERING"); 
            continue;
          }

          Vector3D hkl_vec = new Vector3D( h, k, l );
          Vector3D q_vec = new Vector3D();
          orientation_tran.apply_to( hkl_vec, q_vec );
          float row_col_ch[] = transformer.QtoRowColChan( q_vec );  

          if ( row_col_ch != null )             // peak is on the detector
          {
            float row  = row_col_ch[0];
            float col  = row_col_ch[1];
            float chan = row_col_ch[2];
                                                // only use it if it is not
                                                // too close to the edge
            float delta = 3;
            if ( row  > rcBound[0] + delta && row  < rcBound[2] - delta  &&
                 col  > rcBound[1] + delta && col  < rcBound[3] - delta  && 
                 chan > zmin + delta       && chan < zmax - delta         )
            {
              peak=new Peak_new( nrun,
                                 0,
                                 row_col_ch[1], 
                                 row_col_ch[0], 
                                 row_col_ch[2],
                                 grid, 
                                 samp_or,
                                 x_scale.getInterpolatedX( row_col_ch[2] ) + t0, 
                                 initial_path,
                                 t0 );
              peak.setFacility( AttrUtil.getFacilityName( ds ) );
              peak.setInstrument( AttrUtil.getInstrumentName( ds ) );
              peak.sethkl( h, k, l);
              peak.seqnum(seqnum);
              peak.reflag(10);                         // Mark as ok for now 
              peaks.add(peak);
              seqnum++;
            }
          }

          if ( DEBUG && h == 4 && k == 1 && l == -3 )
          {
            System.out.println( "RUN NUMBER = " + 
                                 AttrUtil.getRunNumber(ds)[0] );
            System.out.println( "HKL == 4, 1, -3" );
            System.out.println( "q_vec = " + q_vec );
            if ( row_col_ch != null )
              System.out.println( "col row ch = " + 
                                   row_col_ch[1] + ", " +
                                   row_col_ch[0] + ", " +
                                   row_col_ch[2] );
          }
        }
      }
    }

    // move peaks to the most intense point nearby
    
   for(int i=peaks.size()-1; i>=0; i--){
      IPeak P1=(IPeak)(peaks.elementAt(i));
      IPeak P=IntegrateUtils.maxClosePeak(P1,ds,ids,dX,dY,dZ);
      P.sethkl( P1.h() , P1.k() , P1.l() );
      peaks.setElementAt( P , i );
      peak=(IPeak)peaks.elementAt(i);
      for( int j=i+1 ; j<peaks.size() ; j++ ){ // remove peak if it gets
        if( peak.equals(peaks.elementAt(j)) ){ // shifted on top of another
          peaks.remove(j);
          break;
        }
      }
    }

    IntegrateUtils.RemovePeaksWithSmall_d( peaks, d_min, ds, detnum );

    /*
      System.out.println("NUMBER OF PEAKS TO INTEGRATE " + peaks.size() );
      peak = (Peak)peaks.elementAt(0);
      System.out.println("peak x,y,z = " + peak.x() + ", " + peak.y() + ", " 
                                         + peak.z() );
    */
    //TODO add to echo point
    //System.out.println("Integration Method: " + PeakAlg );

    // integrate the peaks
    StringBuffer dummy_buffer = new StringBuffer();
    StringBuffer my_buffer = dummy_buffer;
    String       my_method = null;
    for( int i=peaks.size()-1 ; i>=0 ; i-- )
    {
      if( i%listNthPeak == 0 )                   // integrate with logging
        my_buffer = logBuffer;
      else 
        my_buffer = dummy_buffer;
      
      if ( PeakAlg.equals(SHOE_BOX) )
      {
        my_method = SHOE_BOX;
        IntegrateUtils.integrateShoebox( (IPeak)peaks.elementAt(i),
                                          ds, ids,
                                          colXrange, rowYrange, timeZrange,
                                          my_buffer ); 
      }
      else if( PeakAlg.equals(NEW_INTEGRATE))
      {
        my_method = NEW_INTEGRATE; 
        IntegrateUtils.integratePeak( (IPeak)peaks.elementAt(i),
                                       ds, ids, 
                                       timeZrange, incrSlice,
                                       my_buffer) ;
        peak = (IPeak)(peaks.elementAt(i));
                                              // use shoe box for small peaks
                                              // (small peaks may have had 
                                              //  inti and sigi set to 0.0)
        if ( (max_shoebox > 0 && peak.sigi() <= 0 ) ||
              peak.inti() < peak.sigi() * max_shoebox )
        {
          my_method = SHOE_BOX; 
          IntegrateUtils.integrateShoebox( (IPeak)peaks.elementAt(i),
                                            ds, ids,
                                            colXrange, rowYrange, timeZrange,
                                            my_buffer ); 
        }
      }
      else 
      {
        my_method = EXPERIMENTAL; 
        integratePeakExp((IPeak)peaks.elementAt(i),
                          ds, ids,
                          colXrange, rowYrange, timeZrange, incrSlice,
                          opIntPt,
                          my_buffer );    
      }

      peak = (IPeak)(peaks.elementAt(i));

      if ( my_method.equals( SHOE_BOX ) )
        peak.reflag( 100 + peak.reflag() );
      else if ( my_method.equals( EXPERIMENTAL ) )
        peak.reflag( 200 + peak.reflag() );
      
      if( DEBUG )
      {
        System.out.println("Integrating with " + my_method);
        System.out.println("Integrating intensity   " + peak.inti());
        System.out.println("Integrating sigI        " + peak.sigi());
      }
    }

    if( logbuffer != null && logbuffer instanceof StringBuffer)
       ((StringBuffer)logbuffer).append(logBuffer);
    else
       SharedMessages.addmsg(  logBuffer );
              
    // centroid the peaks
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=Util.centroid((IPeak)peaks.elementAt(i),ds,grid);
      if(peak!=null){
        peak.seqnum(i+1); // renumber the peaks
        peaks.set(i,peak);
      }else{
        peaks.remove(i);
        i--;
      }
    }

    // for ( int i = 0; i < peaks.size(); i++ )
    //   System.out.println( (IPeak)(peaks.elementAt(i)) );

    // things went well so return null

    grid.clearData_entries();  // NOTE: Currently this is critical, since
                               //       the grid was given to all peaks and
                               //       so the peak objects contain references
                               //       to ALL the data.  This leads to a
                               //       MUCH bigger memory "footprint" for
                               //       integrate, than would normally be 
                               //       required.
    return null;
  }

  /**
   * Create the vector of choices for the ChoiceListPG of centering.
   */
  private void init_choices(){
    choices=new Vector();
    choices.add("primitive");               // 0 
    choices.add("a centered");              // 1
    choices.add("b centered");              // 2
    choices.add("c centered");              // 3
    choices.add("[f]ace centered");         // 4
    choices.add("[i] body centered");       // 5
    choices.add("[r]hombohedral centered"); // 6
  }

  /**
   * This method takes the log created throughout the calculation and
   * writes it to a file.
   *
   * @return a String if anything goes wrong, null otherwise.
   */
// private String writeLog(String logfile,boolean append){
  //  if( logBuffer==null || logBuffer.length()<=0 )
  //    return "No information in log buffer";

  /*
    FileOutputStream fout=null;
    try{
      fout=new FileOutputStream(logfile,append);
      fout.write(logBuffer.toString().getBytes());
      fout.flush();
    }catch(IOException e){
      return e.getMessage();
    }finally{
      if(fout!=null){
        try{
          fout.close();
        }catch(IOException e){
          // let it drop on the floor
        }
      }
    }
   */
   // gov.anl.ipns.Util.Sys.SharedMessages.LOGaddmsg(logBuffer.toString());
   //  return null;
 // }

  /**
    * This method integrates the peak by using an experimental integrate peak
    *  using the experimental IntegratePt information operator.  The low-level
    *  code can be plugged into IntegratePt using the setIntgratePkOp() method.
    */
   private  static void integratePeakExp( IPeak peak, DataSet ds, int[][] ids,
                         int[] colXrange, int[] rowYrange, int[] timeZrange, int increaseSlice, 
                         IntegratePt opIntPt,StringBuffer log){

     // set up where the peak is located
     
     int cenX= Math.round(peak.x());
     int cenY= Math.round(peak.y());
     int cenZ= Math.round(peak.z());
     Data D = ds.getData_entry( ids[cenX][cenY]);
     if( D == null)
        return;
     int indx = ds.getIndex_of_data(D);
     XScale xscl= D.getX_scale();
     float time = xscl.getX(cenZ);
     int minx = colXrange[0];
     int maxx = colXrange[1];
     int miny = rowYrange[0];
     int maxy = rowYrange[1];
     int minz = timeZrange[0];
     int maxz = timeZrange[1];

     SCD_LogUtils.addLogHeader( log, peak );

     // initialize variables for the slice integration
                
     Vector V = opIntPt.Integrate(time, indx, null, minx, maxx, miny, maxy, minz, maxz);
     
     try{                  
     float Itot = ((Float)(V.elementAt(0))).floatValue();
     float dItot =((Float)(V.elementAt(1))).floatValue();
     SCD_LogUtils.addLogPeakSummary( null, Itot, dItot );
     // change the peak to reflect what we just did
     peak.inti(Itot);
     peak.sigi(dItot);
     }catch(Exception ss){
        return;
     }
   }


  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Operator op = new Integrate_new();
    op.CopyParametersFrom( this );
    return op;
  }
  

  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] ){
    // create the parameters to pass to all of the different operators
    String prefix="/IPNShome/pfpeterson/data/SCD/";
    String datfile=prefix+"SCD06496.RUN";
    DataSet rds = (new RunfileRetriever(datfile)).getDataSet(1);
    
    // load the calibration file, note that we are using line 1
    LoadSCDCalib lsc=new LoadSCDCalib(rds,prefix+"instprm.dat",1,null);
    System.out.println("LoadSCDCalib.RESULT="+lsc.getResult());
    
    // load an orientation matrix
    LoadOrientation lo = 
              new LoadOrientation(rds,new LoadFileString(prefix+"quartz.mat"));
    System.out.println("LoadOrientation.RESULT="+lo.getResult());

    // integrate the dataset
    Integrate_new op = new Integrate_new( rds );
    Object res=op.getResult();
    // print the results
    System.out.print("Integrate.RESULT=");
    if(res instanceof ErrorString){
      System.out.println(res);
    }else if(res instanceof Vector){
      System.out.println("");
      Vector peaks=(Vector)res;
      for( int i=0 ; i<peaks.size() ; i++ ){
        System.out.println(peaks.elementAt(i));
      }
    }else{
      System.out.println(res.toString());
    }

    System.exit(0);
  }
}
