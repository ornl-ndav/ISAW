/*
 * File:  Integrate.java 
 *
 * Copyright (C) 2002, Peter Peterson
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
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.39  2006/12/20 16:37:09  dennis
 * Removed code that is common to various Integrate versions.  Now uses
 * those common methods from IntegrateUtils and SCD_LogUtils.
 *
 * Revision 1.38  2006/07/10 16:26:01  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
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
 */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.instruments.*;
import DataSetTools.util.*;
import DataSetTools.retriever.RunfileRetriever;

import Operators.TOF_SCD.*;

import gov.anl.ipns.Parameters.BooleanPG;
import gov.anl.ipns.Parameters.ChoiceListPG;
import gov.anl.ipns.Parameters.IntArrayPG;
import gov.anl.ipns.Parameters.IntegerPG;
import gov.anl.ipns.Parameters.LoadFilePG;
import gov.anl.ipns.Parameters.SaveFilePG;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import java.io.*;
import java.util.Vector;

/** 
 * This is a ported version of A.J.Schultz's INTEGRATE program. 
 */
public class Integrate extends GenericTOF_SCD{
  private static final String       TITLE       = "Integrate";
  private static       boolean      DEBUG       = false;
  private static       Vector       choices     = null;
  private              StringBuffer logBuffer   = null;
  private              float        chi         = 0f;
  private              float        phi         = 0f;
  private              float        omega       = 0f;
  private              int          listNthPeak = 3;
  private              int          centering   = 0;
  /**
   * how much to increase the integration size after I/dI has been maximized
   */
  private              int          incrSlice   = 0;
  /**
   * uncertainty in the peak location
   */
  private              int dX=2, dY=2, dZ=1;
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

  /* ------------------------ Default constructor ------------------------- */ 
  /**
   * Creates operator with title "Integrate" and a default list of
   * parameters.
   */  
  public Integrate(){
    super( TITLE );
  }
  
  /** 
   * Creates operator with title "Integrate" and the specified list
   * of parameters. The getResult method must still be used to execute
   * the operator.
   *
   * @param ds DataSet to integrate
   */
  public Integrate( DataSet ds ){
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
   * Creates operator with title "Integrate" and the specified list
   * of parameters. The getResult method must still be used to execute
   * the operator.  This is a convenience constructor so that a full
   * Integrate Operator can be constructed without the need to 
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
   * @param use_shoebox Flag to specify using same-size shoebox around all peaks,
   *                    rather than trying to maximize I/sigI
   * @param box_x_range The range of x (delta col) values to use around the peak 
   *                    position
   * @param box_y_range The range of y (delta row) values to use around the peak 
   *                    position
   */
  public Integrate( DataSet ds, 
                    String  integfile, 
                    String  matfile,
                    String  slicerange, 
                    int     slicedelta, 
                    int     lognum,
                    boolean append,
                    boolean use_shoebox,
                    String  box_x_range,
                    String  box_y_range )
  {
    this(ds); 

    getParameter(1).setValue(integfile);
    getParameter(2).setValue(matfile);
    getParameter(4).setValue(slicerange);
    getParameter(5).setValue(new Integer(slicedelta));
    getParameter(6).setValue(new Integer(lognum));
    getParameter(7).setValue(new Boolean(append));
    getParameter(8).setValue(new Boolean(use_shoebox));
    getParameter(9).setValue(box_x_range);
    getParameter(10).setValue(box_y_range);
  }

  /** 
   * Creates operator with title "Integrate" and the specified list
   * of parameters. The getResult method must still be used to execute
   * the operator.  This is a convenience constructor so that a full
   * Integrate Operator can be constructed without the need to 
   * pass in IParameterGUIs.
   *
   * @param ds          DataSet to integrate
   * @param integfile   The "integrate file" for the analysis.
   * @param matfile     The matrix file to use for the analysis.
   * @param choice      number for the centering type
   * @param slicerange  The time slice range.
   * @param slicedelta  The amount to increase slicesize by.
   * @param lognum      The peak multiples to log - i.e. 3 logs
   *                    1, 3, 6, 9...
   * @param append      Append to file (true/false);
   * @param use_shoebox Flag to specify using same-size shoebox around all peaks,
   *                    rather than trying to maximize I/sigI
   * @param box_x_range The range of x (delta col) values to use around the peak 
   *                    position
   * @param box_y_range The range of y (delta row) values to use around the peak 
   *                    position
   */
  public Integrate( DataSet ds, 
                    String  integfile, 
                    String  matfile,
                    int     choice, 
                    String  slicerange, 
                    int     slicedelta, 
                    int     lognum,
                    boolean append,
                    boolean use_shoebox,
                    String  box_x_range,
                    String  box_y_range )
  {
    this(ds,
         integfile, matfile, 
         slicerange, slicedelta, 
         lognum, 
         append, 
         use_shoebox,
         box_x_range, box_y_range ); 
    getParameter(3).setValue(choices.elementAt(choice));
  }
  
  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts: SCDIntegrate
   * 
   * @return  "SCDIntegrate", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "SCDIntegrate";
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
    addParameter(new IntegerPG("Log Every nth Peak",3));

    // parameter(7)
    addParameter(new BooleanPG("Append",false));

    // parameter(8)
    addParameter(new BooleanPG("Use Shoe Box (NOT max I/sigI)",false));

    // parameter(9)
    addParameter(new IntArrayPG("Box Delta x (col) Range","-2:2"));

    // parameter(10)
    addParameter(new IntArrayPG("Box Delta y (row) Range","-2:2"));
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
    sb.append("@param logNPeak Log the \"nth\" peak.\n");
    sb.append("@param append Whether to append to the file.\n");
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
    val=getParameter(0).getValue();
    ((DataSetPG)getParameter(0)).clear();    // needed to avoid memory leak

    if( val instanceof DataSet){
      if(((DataSet)val).getNum_entries()>0)
        ds=(DataSet)val;
      else
        return new ErrorString("Specified DataSet is empty");
    }else{
      return new ErrorString("Value of first parameter must be a dataset");
    }

    // then the integrate file
    val=getParameter(1).getValue();
    if(val!=null){
      integfile=val.toString();
      if(integfile.length()<=0)
        return new ErrorString("Integrate filename is null");
      integfile=FilenameUtil.setForwardSlash(integfile);
    }else{
      return new ErrorString("Integrate filename is null");
    }


    // then get the matrix file
    val=getParameter(2).getValue();
    if(val!=null){
      matfile=FilenameUtil.setForwardSlash(val.toString());
      File dir=new File(matfile);
      if(dir.isDirectory())
        matfile=null;
    }else{
      matfile=null;
    }

    // then the centering condition
    val=getParameter(3).getValue().toString();
    centering=choices.indexOf((String)val);
    if( centering<0 || centering>=choices.size() ) centering=0;

    // then the time slice range
    {
      int[] myZrange=((IntArrayPG)getParameter(4)).getArrayValue();
      if(myZrange!=null && myZrange.length>=2){
        timeZrange[0]=myZrange[0];
        timeZrange[1]=myZrange[myZrange.length-1];
      }else{
        return new ErrorString("Invalid time range specified");
      }
    }

    // then how much to increase the integration size
    incrSlice=((IntegerPG)getParameter(5)).getintValue();

    // then how often to log a peak
    listNthPeak=((IntegerPG)getParameter(6)).getintValue();

    // then whether to append
    boolean append=((BooleanPG)getParameter(7)).getbooleanValue();

    // then whether to just use a "shoebox" instead of maximizing I/sigI
    boolean use_shoebox=((BooleanPG)getParameter(8)).getbooleanValue();

    // then the x range
    {
      int[] myXrange=((IntArrayPG)getParameter(9)).getArrayValue();
      if(myXrange!=null && myXrange.length>=2){
        colXrange[0]=myXrange[0];
        colXrange[1]=myXrange[myXrange.length-1];
      }else{
        return new ErrorString("Invalid X range specified");
      }
    }

    // then the y range
    {
      int[] myYrange=((IntArrayPG)getParameter(10)).getArrayValue();
      if(myYrange!=null && myYrange.length>=2){
        rowYrange[0]=myYrange[0];
        rowYrange[1]=myYrange[myYrange.length-1];
      }else{
        return new ErrorString("Invalid Y range specified");
      }
    }

   if ( DEBUG )
   {
     System.out.println("use shoebox = " + use_shoebox );
     System.out.println("X range = " + colXrange[0] + " to " + colXrange[1] );
     System.out.println("Y range = " + rowYrange[0] + " to " + rowYrange[1] );
     System.out.println("Z range = " + timeZrange[0] + " to " + timeZrange[1] );
   }

    // get list of detectors
    int[] det_number=null;
    {
      // determine all unique detector numbers
      Integer detNum=null;
      Vector innerDetNum=new Vector();
      for( int i=0 ; i<ds.getNum_entries() ; i++ ){
        detNum=new Integer(Util.detectorID(ds.getData_entry(i)));
        if( ! innerDetNum.contains(detNum) ) innerDetNum.add(detNum);
      }
      // copy them over to the detector number array
      det_number=new int[innerDetNum.size()];
      for( int i=0 ; i<det_number.length ; i++ )
        det_number[i]=((Integer)innerDetNum.elementAt(i)).intValue();
    }
    if(det_number==null)
      return new ErrorString("Could not determine detector numbers");

    if(DEBUG){
      System.out.println("DataSet:"+ds);
      System.out.println("MatFile:"+matfile);
      System.out.print(  "DetNum :");
      for( int i=0 ; i<det_number.length ; i++ )
        System.out.print(det_number[i]+" ");
      System.out.println();
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

    Data data=ds.getData_entry(0);

    // get the sample orientation
    {
      SampleOrientation orientation =
        (SampleOrientation)data.getAttributeValue(Attribute.SAMPLE_ORIENTATION);
      if ( orientation != null )
      {
        phi   = orientation.getPhi();
        chi   = orientation.getChi();
        omega = orientation.getOmega();
      }
      orientation = null;
    }

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

    // determine the initial flight path
    float init_path=0f;
    {
      Object L1=data.getAttributeValue(Attribute.INITIAL_PATH);
      if(L1!=null){
        if(L1 instanceof Float)
          init_path=((Float)L1).floatValue();
      }
      L1=null;
    }
    if(init_path==0f)
      return new ErrorString("initial flight path is zero");

    // get the run number
    int nrun=0;
    {
      Object Nrun=data.getAttributeValue(Attribute.RUN_NUM);
      if(Nrun!=null){
        if( Nrun instanceof Integer)
          nrun=((Integer)Nrun).intValue();
        else if( Nrun instanceof int[])
          nrun=((int[])Nrun)[0];
          }
      Nrun=null;
    }

    // create a PeakFactory for use throughout this operator
    PeakFactory pkfac=new PeakFactory(nrun,0,init_path,0f,0f,0f);
    pkfac.UB(UB);
    pkfac.sample_orient(chi,phi,omega);

    // create a vector for the results
    Vector peaks=new Vector();

    // integrate each detector
    Vector innerPeaks=null;
    ErrorString error=null;
    for( int i=0 ; i<det_number.length ; i++ ){
      if(DEBUG) System.out.println("integrating "+det_number[i]);
      innerPeaks=new Vector();
      error=integrateDetector(ds,innerPeaks,pkfac,det_number[i], use_shoebox);
      if(DEBUG) System.out.println("ERR="+error);
      if(error!=null) return error;
      if(DEBUG) System.out.println("integrated "+innerPeaks.size()+" peaks");
      if(innerPeaks!=null && innerPeaks.size()>0)
        peaks.addAll(innerPeaks);
    }

    
    // write out the logfile integrate.log
    String logfile=integfile;
    {
      int index=logfile.lastIndexOf("/");
      logfile=logfile.substring(0,index)+"/integrate.log";
    }
    String errmsg=this.writeLog(logfile,append);
    if(errmsg!=null)
      SharedData.addmsg(errmsg);

    // write out the peaks
    WritePeaks writer=new WritePeaks(integfile,peaks,new Boolean(append));
    return writer.getResult();
  }
// ========== start of detector dependence

  private ErrorString integrateDetector(DataSet     ds, 
                                        Vector      peaks, 
                                        PeakFactory pkfac, 
                                        int         detnum,
                                        boolean     use_shoebox )
  {
    if(DEBUG) System.out.println("Integrating detector "+detnum);

    // get the detector number
    if(detnum<=0)
      return new ErrorString("invalid detector number: "+detnum);
    pkfac.detnum(detnum);

    // create the lookup table
    int[][] ids=Util.createIdMap(ds,detnum);
    if(ids==null)
      return new ErrorString("Could not create pixel map for det "+detnum);

    // determine the boundaries of the matrix
    int[] rcBound = IntegrateUtils.getBounds(ids);
    for( int i=0 ; i<4 ; i++ ){
      if(rcBound[i]==-1)
        return new ErrorString("Bad boundaries on row column matrix");
    }

    // grab pixel 1,1 to get some 'global' attributes from 
    Data data=ds.getData_entry(ids[rcBound[0]][rcBound[1]]);
    if(data==null)
      return new ErrorString("no minimum pixel found");

    // get the calibration for this
    
    float[] calib=(float[])data.getAttributeValue(Attribute.SCD_CALIB);
    if(calib==null)
     return new ErrorString("Could not find calibration for detector " +detnum);
    pkfac.calib(calib);

    // get the xscale from the data to give to the new peaks objects
    XScale times=data.getX_scale();
    pkfac.time(times);

    // determine the detector postion
    float detA=Util.detector_angle(ds,detnum);
    float detA2=Util.detector_angle2(ds,detnum);
    float detD=Util.detector_distance(ds,detnum);
    pkfac.detA(detA);
    pkfac.detA2(detA2);
    pkfac.detD(detD);

    // determine the min and max pixel-times
    int zmin=0;
    int zmax=times.getNum_x()-1;

    // add the position number to the logBuffer
    logBuffer.append("---------- PHYSICAL PARAMETERS\n");
    logBuffer.append(" x/y min, x/y max: "+rcBound[0]+" "+rcBound[1]+"   "
                     +rcBound[2]+" "+rcBound[3]+"\n");
    logBuffer.append("chi="+chi+"  phi="+phi+"  omega="+omega+"\n");
    logBuffer.append("detD="+detD+"  detA="+detA+"  detA2="+detA2+"\n");

    // determine the detector limits in hkl
    int[][] hkl_lim = IntegrateUtils.minmaxhkl(pkfac, ids, times);
    float[][] real_lim = IntegrateUtils.minmaxreal(pkfac, ids, times);

    // add the limits to the logBuffer
    logBuffer.append("---------- LIMITS\n");
    logBuffer.append("min hkl,  max hkl : "+hkl_lim[0][0]+" "+hkl_lim[1][0]
                     +" "+hkl_lim[2][0]+"   "+hkl_lim[0][1]+" "+hkl_lim[1][1]
                     +" "+hkl_lim[2][1]+"\n");
    logBuffer.append("min xcm ycm wl, max xcm ycm wl: "
                     +SCD_LogUtils.formatFloat(real_lim[0][0])+" "
                     +SCD_LogUtils.formatFloat(real_lim[1][0])+" "
                     +SCD_LogUtils.formatFloat(real_lim[2][0])+"   "
                     +SCD_LogUtils.formatFloat(real_lim[0][1])+" "
                     +SCD_LogUtils.formatFloat(real_lim[1][1])+" "
                     +SCD_LogUtils.formatFloat(real_lim[2][1])+"\n");

    // add information about integrating the peaks
    logBuffer.append("\n");
    logBuffer.append("========== PEAK INTEGRATION ==========\n");
    logBuffer.append("listing information about every "+listNthPeak+" peak\n");

    boolean printPeak=DEBUG||false; // REMOVE
    Peak peak=null;
    int seqnum=1;
    // loop over all of the possible hkl values and create peaks
    for( int h=hkl_lim[0][0] ; h<=hkl_lim[0][1] ; h++ ){
      for( int k=hkl_lim[1][0] ; k<=hkl_lim[1][1] ; k++ ){
        for( int l=hkl_lim[2][0] ; l<=hkl_lim[2][1] ; l++ ){
          if( h==0 && k==0 && l==0 ) continue;          // cannot have h=k=l=0

          if( ! IntegrateUtils.checkCenter(h,k,l,centering) )
          {                                       // fails centering conditions
            if(printPeak) System.out.println(" NO CENTERING"); // REMOVE
            continue;
          }
          peak=pkfac.getHKLInstance(h,k,l);
          if(printPeak) System.out.print(peak.toString()); // REMOVE
          peak.nearedge(rcBound[0],rcBound[2],rcBound[1],rcBound[3],
                        zmin,zmax);
          if( peak.nearedge()>=2f){
            if( (IntegrateUtils.checkReal(peak,real_lim)) ){
              if(printPeak) System.out.println(" OK"); // REMOVE
              peak.seqnum(seqnum);
              peaks.add(peak);
              seqnum++;
            }else{
              if(printPeak) System.out.println(" NOT REAL"); // REMOVE
            }
          }else{
            if(printPeak) System.out.println(" BAD EDGE"); // REMOVE
          }
        }
      }
    }

    // move peaks to the most intense point nearby
    for( int i=peaks.size()-1 ; i>=0 ; i-- ){
      IntegrateUtils.movePeak((Peak)peaks.elementAt(i),ds,ids,dX,dY,dZ);
      peak=(Peak)peaks.elementAt(i);
      for( int j=i+1 ; j<peaks.size() ; j++ ){ // remove peak if it gets
        if( peak.equals(peaks.elementAt(j)) ){ // shifted on top of another
          peaks.remove(j);
          break;
        }
      }
    }

    // integrate the peaks
    for( int i=peaks.size()-1 ; i>=0 ; i-- )
    {
      if( i%listNthPeak == 0 )                   // integrate with logging
      {
        if ( use_shoebox )
          IntegrateUtils.integrateShoebox( (Peak)peaks.elementAt(i),
                                           ds, ids,
                                           colXrange, rowYrange, timeZrange,
                                           logBuffer ); 
        else
          IntegrateUtils.integratePeak( (Peak)peaks.elementAt(i),
                                        ds, ids,
                                        timeZrange, incrSlice,
                                        logBuffer);
      }
      else                                      // integrate but don't log
      {
        if ( use_shoebox )
          IntegrateUtils.integrateShoebox( (Peak)peaks.elementAt(i),
                                           ds, ids,
                                           colXrange, rowYrange, timeZrange,
                                           null );
        else
          IntegrateUtils.integratePeak( (Peak)peaks.elementAt(i),
                                        ds, ids,
                                        timeZrange, incrSlice,
                                        null );
      }
    }

    // centroid the peaks
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=Util.centroid((Peak)peaks.elementAt(i),ds,ids);
      if(peak!=null){
        peak.seqnum(i+1); // renumber the peaks
        peaks.set(i,peak);
      }else{
        peaks.remove(i);
        i--;
      }
    }

    // things went well so return null
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
  private String writeLog(String logfile,boolean append){
    if( logBuffer==null || logBuffer.length()<=0 )
      return "No information in log buffer";

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

    return null;
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
    Integrate op = new Integrate( rds );
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
