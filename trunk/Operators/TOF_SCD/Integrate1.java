/*
 * File:  Integrate1.java 
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
 * Revision 1.1  2004/06/18 22:22:20  rmikk
 * Initial Checkin
 *
 *
 */
package Operators.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.instruments.*;
import DataSetTools.util.*;
import DataSetTools.retriever.RunfileRetriever;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import java.io.*;
import java.util.Vector;

/** 
 * This is a ported version of A.J.Schultz's INTEGRATE program. 
 */
public class Integrate1 extends GenericTOF_SCD{
  private static final String       TITLE       = "Integrate1";
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
  public Integrate1(){
    super( TITLE );
  }
  
  /** 
   * Creates operator with title "Integrate" and the specified list
   * of parameters. The getResult method must still be used to execute
   * the operator.
   *
   * @param ds DataSet to integrate
   */
  public Integrate1( DataSet ds ){
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
   * @param append      Append to file (true/false)
   * @param box_x_range The range of x (delta col) values to use around the peak 
   *                    position
   * @param box_y_range The range of y (delta row) values to use around the peak 
   *                    position
   */
  public Integrate1( DataSet ds, 
                    String  integfile, 
                    String  matfile,
                    String  slicerange, 
                    int     slicedelta, 
                    int     lognum,
                    boolean append,
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
   * @param box_x_range The range of x (delta col) values to use around the peak 
   *                    position
   * @param box_y_range The range of y (delta row) values to use around the peak 
   *                    position
   */
  public Integrate1( DataSet ds, 
                    String  integfile, 
                    String  matfile,
                    int     choice, 
                    String  slicerange, 
                    int     slicedelta, 
                    int     lognum,
                    boolean append,
                    String  box_x_range,
                    String  box_y_range )
  {
    this(ds,
         integfile, matfile, 
         slicerange, slicedelta, 
         lognum, 
         append,
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
    return "SCDIntegrate1";
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
    addParameter( new DataSetPG("Data Set", null, false ) );

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
      error=integrateDetector(ds,innerPeaks,pkfac,det_number[i], false);
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
    int[] rcBound=getBounds(ids);
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
    int[][] hkl_lim=minmaxhkl(pkfac, ids, times);
    float[][] real_lim=minmaxreal(pkfac, ids, times);

    // add the limits to the logBuffer
    logBuffer.append("---------- LIMITS\n");
    logBuffer.append("min hkl,  max hkl : "+hkl_lim[0][0]+" "+hkl_lim[1][0]
                     +" "+hkl_lim[2][0]+"   "+hkl_lim[0][1]+" "+hkl_lim[1][1]
                     +" "+hkl_lim[2][1]+"\n");
    logBuffer.append("min xcm ycm wl, max xcm ycm wl: "
                     +formatFloat(real_lim[0][0])+" "
                     +formatFloat(real_lim[1][0])+" "
                     +formatFloat(real_lim[2][0])+"   "
                     +formatFloat(real_lim[0][1])+" "
                     +formatFloat(real_lim[1][1])+" "
                     +formatFloat(real_lim[2][1])+"\n");

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
          if( h==0 && k==0 && l==0 ) continue; // cannot have h=k=l=0
          if( ! checkCenter(h,k,l,centering) ){ // fails centering conditions
            if(printPeak) System.out.println(" NO CENTERING"); // REMOVE
            continue;
          }
          peak=pkfac.getHKLInstance(h,k,l);
          if(printPeak) System.out.print(peak.toString()); // REMOVE
          peak.nearedge(rcBound[0],rcBound[2],rcBound[1],rcBound[3],
                        zmin,zmax);
          if( peak.nearedge()>=2f){
            if( (checkReal(peak,real_lim)) ){
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
      movePeak((Peak)peaks.elementAt(i),ds,ids,dX,dY,dZ);
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
        
          integratePeak((Peak)peaks.elementAt(i),ds,ids,timeZrange,incrSlice,
                        logBuffer);
      }
      else                                      // integrate but don't log
      {
       
          integratePeak((Peak)peaks.elementAt(i),ds,ids,timeZrange,incrSlice,
                        null);
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


  

  /**
   * This method integrates the peak by looking at five time slices
   * centered at the one the peak exsists on. It grows a rectangle on
   * each time slice to get the maximum I/dI for each time slice then
   * adds the results from each time slice to maximize the total I/dI.
   */
  private static void integratePeak( Peak peak, DataSet ds, int[][] ids,
                        int[] timeZrange, int increaseSlice, StringBuffer log){

   
    // set up where the peak is located
   
    int cenX=(int)Math.round(peak.x());
    int cenY=(int)Math.round(peak.y());
    int cenZ=(int)Math.round(peak.z());
    Data D = ds.getData_entry( ids[cenX][cenY]);
    if( D == null)
       return;
    int indx = ds.getIndex_of_data(D);
    XScale xscl= D.getX_scale();
    float time = xscl.getX(cenZ);

    addLogHeader( log, peak );

    // initialize variables for the slice integration
   
    System.out.println(formatInt(cenX)+"  "+formatInt(cenY)
                    +formatInt(getObs(ds,ids[cenX][cenY],cenZ),6));
    Vector V = (new DataSetTools.operator.DataSet.Conversion.XAxis.
          IntegratePt()).Integrate(ds,time,indx,null);
    try{                  
    
   float Itot = ((Float)(V.elementAt(0))).floatValue();
   float dItot =((Float)(V.elementAt(1))).floatValue();
   addLogPeakSummary( null, Itot, dItot );
    

    // change the peak to reflect what we just did
    peak.inti(Itot);
    peak.sigi(dItot);
    }catch(Exception ss){
       return;
    }
  }

 
 
  

  /**
   * Put the peak at the nearest maximum within the given deltas
   */
  private static void movePeak( Peak peak, DataSet ds, int[][] ids, 
                                int dx, int dy, int dz){
    int x=(int)Math.round(peak.x());
    int y=(int)Math.round(peak.y());
    int z=(int)Math.round(peak.z());

    int maxP=(int)Math.round(getObs(ds,ids[x][y],z));
    peak.ipkobs(maxP);
    int maxX=x;
    int maxY=y;
    int maxZ=z;

    float point=0f;
    for( int i=x-dx ; i<=x+dx ; i++ ){
      for( int j=y-dy ; j<=y+dy ; j++ ){
        for( int k=z-dz ; k<=z+dz ; k++ ){
          point=getObs(ds,ids[i][j],k);
          if(point>maxP){
            maxP=(int)Math.round(point);
            maxX=i;
            maxY=j;
            maxZ=k;
          }
          point=0f;
        }
      }
    }
    if(maxX!=x || maxY!=y || maxZ!=z){ // move to nearby maximum
      peak.pixel(maxX,maxY,maxZ);
      peak.ipkobs(maxP);
    }else{
      peak.pixel(x,y,z); // move it onto integer pixel postion
    }
  }

  /**
   * Determines whether the peak can be within the realspace limits specified
   */
  private static boolean checkReal(Peak peak, float[][] lim){
    float wl=peak.wl();
    if(wl==0f) return false;

    float xcm=peak.xcm();
    float ycm=peak.ycm();
    if( xcm>=lim[0][0] && xcm<=lim[0][1] ){
      if( ycm>=lim[1][0] && ycm<=lim[1][1] ){
        if( wl>=lim[2][0] && wl<=lim[2][1] ){
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Determine the edges of the detector in xcm, ycm, and wl. This
   * assumes that ids[0][0] and ids[maxrow][maxcol] are at
   * opposite corners of the detector
   *
   * @param pkfac A fully configure peak factory. It should contain
   * all of the information necessary to go from hkl to pixel.
   * @param ids The 2D matrix which maps row and column to the
   * linear index of datas in DataSet
   * @param times The detector is assumed to have the same x-axis for
   * all pixels. This should be a safe assumption.
   *
   * @return A 3x2 matrix of the limits in real space. 
   */
  private float[][] minmaxreal(PeakFactory pkfac, int[][] ids,XScale times){
    // Determine the limits in pixel and time. This is set up as
    // arrays to shorten later code in the method.
    int[]     x_lim ={1,ids.length-1};
    int[]     y_lim ={1,ids[0].length-1};
    int[]     z_lim ={0,times.getNum_x()-1};
    float[][] time  = {{times.getX(z_lim[0]),times.getX(z_lim[0]+1)},
                       {times.getX(z_lim[1]-1),times.getX(z_lim[1])}};

    // define a temporary peak that will be each of the corners
    Peak peak=null;

    // The real-space representation of the peaks will be stored in
    // this matrix. The first index is xcm=0, ycm=1, and wl=2. The
    // second index is just the number of the peak, it just needs to
    // be unique
    float[][] real=new float[3][8];

    // This looks scarier than it is. The three indices are x (i), y
    // (j), and z (k). It is set up to reduce the amount of typing
    // that needs to be done if there is an error in the code.
    for( int i=0 ; i<2 ; i++ ){
      for( int j=0 ; j<2 ; j++ ){
        for( int k=0 ; k<2 ; k++ ){
          peak=pkfac.getPixelInstance(x_lim[i],y_lim[j],z_lim[k],
                                      time[k][0],time[k][1]);
          real[0][i+2*j+4*k]=peak.xcm();
          real[1][i+2*j+4*k]=peak.ycm();
          real[2][i+2*j+4*k]=peak.wl();
        }
      }
    }

    // set the peak to null so the garbage collector can reclaim it
    peak=null;


    // the first index is h,k,l and the second index is min,max
    float[][] real_lim={{real[0][0],real[0][0]},
                        {real[1][0],real[1][0]},
                        {real[2][0],real[2][0]}};
    
    // sort out the min and max values
    for( int i=1 ; i<8 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        real_lim[j][0]=Math.min(real_lim[j][0],real[j][i]);
        real_lim[j][1]=Math.max(real_lim[j][1],real[j][i]);
      }
    }

    return real_lim;
  }

  /**
   * Determine the edges of the detector in xcm, ycm, and wl. This
   * assumes that ids[0][0] and ids[maxrow][maxcol] are at
   * opposite corners of the detector
   *
   * @param pkfac A fully configure peak factory. It should contain
   * all of the information necessary to go from hkl to pixel.
   * @param ids The 2D matrix which maps row and column to the
   * linear index of datas in DataSet
   * @param times The detector is assumed to have the same x-axis for
   * all pixels. This should be a safe assumption.
   *
   * @return A 3x2 matrix of the limits in hkl. 
   */
  private int[][] minmaxhkl(PeakFactory pkfac, int[][] ids, XScale times){
    // Determine the limits in pixel and time. This is set up as
    // arrays to shorten later code in the method.
    int[]     x_lim ={1,ids.length-1};
    int[]     y_lim ={1,ids[0].length-1};
    int[]     z_lim ={0,times.getNum_x()-1};
    float[][] time  = {{times.getX(z_lim[0]),times.getX(z_lim[0]+1)},
                       {times.getX(z_lim[1]-1),times.getX(z_lim[1])}};

    // define a temporary peak that will be each of the corners
    Peak peak=null;

    // The hkls of the peaks will be stored in this matrix. The first
    // index is h=0, k=1, and l=2. The second index is just the number
    // of the peak, it just needs to be unique
    int[][] hkl=new int[3][8];

    // This looks scarier than it is. The three indices are x (i), y
    // (j), and z (k). It is set up to reduce the amount of typing
    // that needs to be done if there is an error in the code.
    for( int i=0 ; i<2 ; i++ ){
      for( int j=0 ; j<2 ; j++ ){
        for( int k=0 ; k<2 ; k++ ){
          peak=pkfac.getPixelInstance(x_lim[i],y_lim[j],z_lim[k],
                                      time[k][0],time[k][1]);
          hkl[0][i+2*j+4*k]=Math.round(peak.h());
          hkl[1][i+2*j+4*k]=Math.round(peak.k());
          hkl[2][i+2*j+4*k]=Math.round(peak.l());
        }
      }
    }

    // set the peak to null so the garbage collector can reclaim it
    peak=null;

    // the first index is h,k,l and the second index is min,max
    int[][] hkl_lim={{hkl[0][0],hkl[0][0]},
                     {hkl[1][0],hkl[1][0]},
                     {hkl[2][0],hkl[2][0]}};
    
    // sort out the min and max values
    for( int i=1 ; i<8 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        hkl_lim[j][0]=Math.min(hkl_lim[j][0],hkl[j][i]);
        hkl_lim[j][1]=Math.max(hkl_lim[j][1],hkl[j][i]);
      }
    }

    return hkl_lim;
  }
  
  /**
   * Checks the allowed indices of hkl given the centering type.
   *
   * @param type the type of centering operation. Acceptable values
   * are primitive (1), a-centered (2), b-centered (3), c-centered
   * (4), [f]ace-centered (5), [i] body-centered (6), or
   * [r]hombohedral-centered (7)
   *
   * @return true if the hkl is allowed false otherwise
   */
  private boolean checkCenter(int h, int k, int l, int type){
    if(type==0){       // primitive
      return true;
    }else if(type==1){ // a-centered
      int kl=(int)Math.abs(k+l);
      return ( (kl%2)==0 );
    }else if(type==2){ // b-centered
      int hl=(int)Math.abs(h+l);
      return ( (hl%2)==0 );
    }else if(type==3){ // c-centered
      int hk=(int)Math.abs(h+k);
      return ( (hk%2)==0 );
    }else if(type==4){ // [f]ace-centered
      int hk=(int)Math.abs(h+k);
      int hl=(int)Math.abs(h+l);
      int kl=(int)Math.abs(k+l);
      return ( (hk%2)==0 && (hl%2)==0 && (kl%2)==0 );
    }else if(type==5){ // [i] body-centered
      int hkl=(int)Math.abs(h+k+l);
      return ( (hkl%2)==0 );
    }else if(type==6){ // [r]hombohedral-centered
      int hkl=Math.abs(-h+k+l);
      return ( (hkl%3)==0 );
    }

    return false;
  }

  /**
   * Determine the observed intensity of the peak at its (rounded)
   * pixel position.
   */
  public static void getObs(Peak peak, DataSet ds, int[][] ids)
                                         throws ArrayIndexOutOfBoundsException{
    if( ds==null || peak==null ) return;
    int id=ids[(int)Math.round(peak.x())][(int)Math.round(peak.y())];
    int z=(int)Math.round(peak.z());

    peak.ipkobs((int)getObs(ds,id,z));
  }

  /**
   * Determine the observed intensity of the given id and time slice
   * number.
   */
  private static float getObs(DataSet ds, int id, int z){
    if( ds==null ) return 0f;

    Data d=ds.getData_entry(id);
    if(d==null) return 0f;

    return (d.getY_values())[z];
  }

  /**
   * @return a 1D array of the form Xmin, Ymin,Xmax, Ymax
   */
  private static int[] getBounds(int[][] ids){
    int[] bounds={-1,-1,-1,-1}; // see javadocs for meaning
    
    // search for min
    outer: for( int i=0 ; i<ids.length ; i++ ){
      for( int j=0 ; j<ids[0].length ; j++ ){
        if(ids[i][j]==-1) continue; // there is nothing here
        bounds[0]=i;
        bounds[1]=j;
        break outer;
      }
    }

    // search for max
    outer: for( int i=ids.length-1 ; i>bounds[0] ; i-- ){
      for( int j=ids[0].length-1 ; j>bounds[1] ; j-- ){
        if(ids[i][j]==-1) continue; // there is nothing here
        bounds[2]=i;
        bounds[3]=j;
        break outer;
      }
    }
    
    if( (bounds[0]==-1 && bounds[2]==-1) || (bounds[0]==-1 && bounds[2]==-1) ){
      for( int i=0 ; i<4 ; i++ )
        bounds[i]=-1;
    }else{
      if(bounds[0]==-1)
        bounds[0]=bounds[2];
      else if(bounds[2]==-1)
        bounds[2]=bounds[0];
      if(bounds[1]==-1)
        bounds[1]=bounds[3];
      else if(bounds[3]==-1)
        bounds[3]=bounds[1];
    }

    return bounds;
  }

  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Operator op = new Integrate1();
    op.CopyParametersFrom( this );
    return op;
  }
  
  /* --------------------- logging utilities -------------------------- */

  private static String formatInt(double num){
    return formatInt(num,3);
  }

  private static String formatInt(double num, int width){
    return Format.integer(num,width);
  }

  private static String formatFloat(double num){
    StringBuffer text=new StringBuffer(Double.toString(num));
    int index=text.toString().indexOf(".");
    if(index<0){
      text.append(".");
      index=text.toString().indexOf(".");
    }
    text.append("00");
    text.delete(index+3,text.length());

    while(text.length()<7)
      text.insert(0," ");

    return text.toString();
  }

  private static void formatRange(int[] rng, StringBuffer log){
    if(log==null) return;
    final int MAX_LENGTH=14;

    if(log.length()>MAX_LENGTH)
      log.delete(MAX_LENGTH,log.length());
    log.append("  "+formatInt(rng[0])+" "+formatInt(rng[2])+"  "
               +formatInt(rng[1])+" "+formatInt(rng[3]));
  }

  private static void addLogHeader( StringBuffer log, Peak peak )
  {
    // add some information to the log file (if necessary)
    {
      System.out.println("\n******************** hkl = "+formatInt(peak.h())+" "
                 +formatInt(peak.k())+" "+formatInt(peak.l())
                 +"   at XYT = "+formatInt(peak.x())+" "+formatInt(peak.y())
                 +" "+formatInt(peak.z()+1)+" ********************\n");
      System.out.println("Layer  T   maxX maxY  IPK     dX       dY      Ihkl     sigI"
                 +"  I/sigI   included?\n");
    }
  }

  private static void addLogSlice( StringBuffer log, 
                                   int          layer, 
                                   int          cenZ, 
                                   int          cenX, 
                                   int          cenY, 
                                   int          slice_peak, 
                                   int          minX, 
                                   int          maxX, 
                                   int          minY, 
                                   int          maxY,
                                   float        sliceI, 
                                   float        slice_sigI, 
                                   String       included,
                                   boolean      border_peak )
  {
    if ( log != null )
    {
      log.append( formatInt(layer)              + 
                  "   "  + formatInt(cenZ)      +
                  "  "  + formatInt(cenX)       +
                  "  "  + formatInt(cenY)       +
                  "   " + formatInt(slice_peak) +
                  "  "  + formatInt(minX)       +
                  " "   + formatInt(maxX)       +
                  "  "  + formatInt(minY)       +
                  " "   + formatInt(maxY)       +
                  " " + formatFloat(sliceI)     +
                  "  " + formatFloat(slice_sigI) );
      if ( slice_sigI != 0 )
        log.append( " " + formatFloat(sliceI/slice_sigI) );
      else
        log.append( " " + formatFloat(0) );
 
      log.append( "      " + included );
      if ( border_peak )
        log.append(" *BP" + "\n");
      else
        log.append("\n");
    }
  }


  private static void addLogPeakSummary( StringBuffer log, 
                                         float        Itot, 
                                         float        sigItot )
  {
    
    {
      System.out.print("***** Final       Ihkl = "+formatFloat(Itot)+"       sigI = "
                 +formatFloat(sigItot)+"       I/sigI = ");
      if(sigItot>0f)
      System.out.print(formatFloat(Itot/sigItot));
      else
      System.out.print(formatFloat(0f));
      System.out.print(" *****\n");
    }
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
    Integrate1 op = new Integrate1( rds );
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
