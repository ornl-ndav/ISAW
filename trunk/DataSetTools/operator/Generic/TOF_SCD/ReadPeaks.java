/*
 * File:  ReadPeaks.java 
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
 * Revision 1.1  2003/01/31 21:11:16  pfpeterson
 * Added to CVS.
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.util.*;
import java.io.*;
import java.util.*;

/** 
 * This operator reads in an ASCII file and converts its contents into
 * a vector of Peak objects.
 */
public class ReadPeaks extends GenericTOF_SCD{
  private static final String TITLE       = "Read Peaks";
  
  /* ------------------------ Default constructor ------------------------- */ 
  /**
   *  Creates operator with title "Write Peaks" and a default list of
   *  parameters.
   */  
  public ReadPeaks(){
    super( TITLE );
  }
  
  /* ---------------------------- Constructor ----------------------------- */ 
  /** 
   *  Creates operator with title "Read Peaks" and the specified list
   *  of parameters. The getResult method must still be used to execute
   *  the operator.
   *
   *  @param  filename Peaks file to read in
   */
  public ReadPeaks( String filename ){
    this(); 
    getParameter(0).setValue(filename);
  }

  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "ReadPeaks", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "ReadPeaks";
  }
  
  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets default values for the parameters.  This must match the data
   * types of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter( new LoadFilePG("Peaks File", null ));
  }
    /* ---------------------- getDocumentation --------------------------- */
    /**
     *  Returns the documentation for this method as a String.  The format
     *  follows standard JavaDoc conventions.
     */
  public String getDocumentation(){
    StringBuffer sb = new StringBuffer(100);

    // overview
    sb.append("@overview This operator reads a \".peaks\" file and creates a vector of peak objects to be worked on using other operators.");
    // assumptions
    sb.append("@assumptions Information such as calibration and orientation matrix will be dealt with elsewhere. This only reads what is in the one file.");
    // parameters
    sb.append("@param filename Name of the \".peaks\" file to load.");
    // return
    sb.append("@return A Vector of Peak objects.");
    // error
    sb.append("@error When anything is wrong with the file including:");
    sb.append(  "<UL><LI>does not exist</LI>");
    sb.append(      "<LI>not user readable</LI>");
    sb.append(      "<LI>ANY random IOException during reading</LI></UL>");

    return sb.toString();
  }	

  /* ----------------------------- getResult ------------------------------ */ 
  /** 
   *  Writes a list of x,y, and time bins and intensities to the specified
   *  file.
   *
   *  @return String containing the value of the specified file, which is 
   *  summary information for the peaks data written to the file as well as 
   *  the file's path. (if successful).
   */
  public Object getResult(){
    // get the filename
    String  filename = getParameter(0).getValue().toString();
    if(filename!=null && filename.length()>0){
      File file=new File(filename);
      if(! file.isFile() )
        return new ErrorString("Not regular file:"+filename);
      if(! file.canRead() )
        return new ErrorString("Cannot read file:"+filename);
    }else{
      return new ErrorString("Null or empty filename:"+filename);
    }

    // create some useful variables
    Vector         peaks = new Vector();
    Peak           peak  = null;
    PeakFactory    pkfac = null;
    TextFileReader tfr   = null;
    ErrorString    error_string = null;

    try{
      // open the file
      tfr=new TextFileReader(filename);

      // variables for dealing with the file contents
      String       line = null;
      StringBuffer sb   = null;

      while( ! tfr.eof() ){
        line=tfr.read_line();
        line=line.trim();
        if( line.length()<=0 ){ // skip empty lines
          continue;
        }else if( line.startsWith("0") ){ // skip header line
          continue;
        }else if( line.startsWith("1") ){ // change the factory for subsequent
          pkfac=createFactory(line);      // peaks to be created
        }else if( line.startsWith("2") ){ // skip column labels
          continue;
        }else if( line.startsWith("3") ){ // create a peak and add it to the
          peak=createPeak(line,pkfac);    // vector
          if(peak!=null)
            peaks.add(peak);
        }else{
          // do nothing
        }
      }

    }catch(IOException e){ // create an error string from the exception
      error_string=new ErrorString(e.getMessage());
    }finally{ // close the file
      if(tfr!=null){
        try{
          tfr.close();
        }catch(IOException e2){
          // let it drop on the floor
        }
      }
    }

    if(error_string!=null)
      return error_string;
    else if( peaks==null || peaks.size()<=0 )
      return new ErrorString("Could not load peaks");
    else // everything went well
      return peaks;
  }

  /**
   * From a simple String creates a PeakFactory. If anything goes
   * wrong this returns null.
   */
  private static PeakFactory createFactory( String line ){
    // make sure there is something to work with
    if(line==null) return null;

    // prepare the line for parsing
    StringBuffer sb=new StringBuffer(line);

    // chop off the line tag
    sb.delete(0,1);
    StringUtil.trim(sb);

    // set up all of our temporary variables
    int runNumber=0;
    int detNumber=0;
    float detA=0f;
    float detA2=0f;
    float detD=0f;
    float chi=0f;
    float phi=0f;
    float omega=0f;
    float moncount=0f;

    // parse the string
    try{
      runNumber = StringUtil.getInt(sb);
      detNumber = StringUtil.getInt(sb);
      detA      = StringUtil.getFloat(sb);
      detA2     = StringUtil.getFloat(sb);
      detD      = StringUtil.getFloat(sb);
      chi       = StringUtil.getFloat(sb);
      phi       = StringUtil.getFloat(sb);
      omega     = StringUtil.getFloat(sb);
      moncount  = StringUtil.getFloat(sb);
    }catch(NumberFormatException e){
      return null; // don't warn, just return nothing
    }

    // actually create the PeakFactory
    PeakFactory pkfac=new PeakFactory(runNumber,detNumber,0f,detD,detA,detA2);
    pkfac.monct(moncount);
    pkfac.sample_orient(chi,phi,omega);

    // return the result
    return pkfac;
  }

  /**
   * Uses the supplied String and PeakFactory to create a Peak. If
   * anything goes wrong this returns null.
   */
  private static Peak createPeak( String line, PeakFactory pkfac ){
    // make sure there is something to work with
    if(line==null) return null;
    if( pkfac==null ) return null;

    // prepare the line for parsing
    StringBuffer sb=new StringBuffer(line);

    // chop off the line tag
    sb.delete(0,1);
    StringUtil.trim(sb);

    // set up our temporary variables
    float h=0f,    k=0f,   l=0f;
    float x=0f,    y=0f,   z=0f;
    float xcm=0f,  ycm=0f, wl=0f;
    float inti=0f, sigi=0f;
    int   seqnum=0;
    int   ipkobs=0;
    int   reflag=0;

    // parse the string
    try{
      // sequence number
      seqnum=StringUtil.getInt(sb);
      // hkl representation
      h=StringUtil.getFloat(sb);
      k=StringUtil.getFloat(sb);
      l=StringUtil.getFloat(sb);
      // pixel representation
      x=StringUtil.getFloat(sb);
      y=StringUtil.getFloat(sb);
      z=StringUtil.getFloat(sb);
      z=z-1; // internally z is stored as one less than the file representation
      // real space representation
      xcm=StringUtil.getFloat(sb);
      ycm=StringUtil.getFloat(sb);
      wl=StringUtil.getFloat(sb);
      // intensity
      ipkobs=StringUtil.getInt(sb);
      inti=StringUtil.getFloat(sb);
      sigi=StringUtil.getFloat(sb);
      // reflection flag
      reflag=StringUtil.getInt(sb);
    }catch(NumberFormatException e){
      return null;
    }

    // actually create the Peak
    Peak peak=pkfac.getPixelInstance(x,y,z,0,0);
    peak.seqnum(seqnum);
    peak.real(xcm,ycm,wl);
    peak.sethkl(h,k,l);
    peak.ipkobs(ipkobs);
    peak.inti(inti);
    peak.sigi(sigi);
    peak.reflag(reflag);

    return peak;
  }
  
  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Operator op = new ReadPeaks();
    op.CopyParametersFrom( this );
    return op;
  }

  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] ){
    String filename="/IPNShome/pfpeterson/data/SCD/int_quartz.peaks";
    
    ReadPeaks rp = new ReadPeaks(filename);
    Object res=rp.getResult();
    System.out.print("RESULT:");
    if(res instanceof ErrorString || res==null){
      System.out.println(res);
    }else{
      System.out.println("");
      for( int i=0 ; i<((Vector)res).size() ; i++ )
        System.out.println(((Vector)res).elementAt(i));
    }
    
    System.exit(0);
  }
}
