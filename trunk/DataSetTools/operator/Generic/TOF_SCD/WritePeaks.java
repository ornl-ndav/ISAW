/*
 * File:  WritePeaks.java 
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
 * Revision 1.13  2003/05/22 22:22:56  pfpeterson
 * Fixed sequence number problem with second detector.
 *
 * Revision 1.12  2003/01/31 21:09:45  pfpeterson
 * No longer assumes that the vector of peaks all came from the same run
 * and detector. Does assume that the peaks from the same run and detector
 * are grouped together in the vector supplied. When either the run or
 * detector changes the file gets a new header line inserted.
 *
 * Revision 1.11  2003/01/28 23:01:38  dennis
 * Added getDocumentation() method.  Also, now closes file if an
 * exception is encountered. (Chris Bouzek)
 *
 * Revision 1.10  2002/11/27 23:22:20  pfpeterson
 * standardized header
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import DataSetTools.util.*;
import DataSetTools.retriever.RunfileRetriever;
import java.io.*;
import java.util.*;
import java.util.Vector;
import java.lang.reflect.Array;
import java.text.DecimalFormat;

/** 
 * This operator is a small building block of an ISAW version of
 * A.J.Schultz's PEAKS program. This operator writes out the
 * information in a format specified by Art.
 */
public class WritePeaks extends GenericTOF_SCD implements HiddenOperator{
  private static final String TITLE       = "Write Peaks";
  private static final SharedData shared= new SharedData();
  
  /* ------------------------ Default constructor ------------------------- */ 
  /**
   *  Creates operator with title "Write Peaks" and a default list of
   *  parameters.
   */  
  public WritePeaks(){
    super( TITLE );
  }
  
  /* ---------------------------- Constructor ----------------------------- */ 
  /** 
   *  Creates operator with title "Write Peaks" and the specified list
   *  of parameters. The getResult method must still be used to execute
   *  the operator.
   *
   *  @param  file      Filename to print to
   *  @param  peaks     Vector of peaks
   *  @param  append    Whether to append to specified file
   */
  public WritePeaks( String file, Vector peaks, Boolean append){
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("File Name", file) );
    addParameter( new Parameter("Vector of Peaks",peaks) );
    addParameter( new Parameter("Append",append) );
  }

  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "WritePeaks", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "WritePeaks";
  }
  
  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets default values for the parameters.  This must match the data
   * types of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter( new Parameter("File Name", "filename" ) );
    addParameter( new Parameter("Vector of Peaks", new Vector() ) );
    addParameter( new Parameter("Append", Boolean.FALSE) );
  }
    /* ---------------------- getDocumentation --------------------------- */
    /**
     *  Returns the documentation for this method as a String.  The format
     *  follows standard JavaDoc conventions.
     */
    public String getDocumentation()
    {
      StringBuffer s = new StringBuffer("");
      s.append("@overview This operator is a small building block of an ");
      s.append("ISAW version of A.J. Schultz's PEAKS program. This ");
      s.append("operator writes out a list of x, y, and time bins and ");
      s.append("intensities to the specified file in a format specified ");
      s.append("by Art.\n");
      s.append("@assumptions The specified file does not already exist.\n");
      s.append("Alternatively, if the specified file exists, it is OK to ");
      s.append("overwrite it.\n");
      s.append("It is furthermore assumed that the specified peaks Vector ");
      s.append("contains valid peak information.\n");
      s.append("@algorithm This operator first determines the last sequence ");
      s.append("number in the file, if we are appending to the file.\n");
      s.append("Then it obtains the general information, sample ");
      s.append("orientation, and the integrated monitor intensity from the ");
      s.append("peaks Vector.\n");
      s.append("Next it opens the specified file.\n");
      s.append("Then it writes a general information header and the general ");
      s.append("information to the file.  It also writes a peaks field ");
      s.append("header and peak information to the file.\n");
      s.append("Finally, it closes the file.\n");
      s.append("@param file Filename to print to.\n");
      s.append("@param peaks Vector of peaks.\n");
      s.append("@param append Value indicating whether to append to ");
      s.append("the specified file or not.\n");
      s.append("@return String containing the value of the specified file, ");
      s.append("which is summary information for the peaks data written to the ");
      s.append("file as well as the file's path.\n");
      s.append("@error If any error occurs, this operator simply lets it ");
      s.append("\"drop\" (i.e. execution stops).  Whatever was contained ");
      s.append("in the file at the time of the error is then returned.\n");
      return s.toString();
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
    String  file   = getParameter(0).getValue().toString();
    Vector  peaks  = (Vector)(getParameter(1).getValue());
    boolean append = ((Boolean)(getParameter(2).getValue())).booleanValue();
    OutputStreamWriter outStream = null;
    int     seqnum_off = 0;
    
    // determine the last sequence number in the file if we are appending
    if(append) seqnum_off=lastSeqNum(file);
    seqnum_off++;

    // general information
    int runNum=-1;
    int detNum=-1;

    // temporary variable
    Peak peak = null;

    try{
      // open and initialize a buffered file stream
      FileOutputStream op = new FileOutputStream(file,append);
      outStream=new OutputStreamWriter(op);
      
      // loop through the peaks
      for( int i=0 ; i<peaks.size() ; i++ ){
        peak=(Peak)peaks.elementAt(i);

        // check that we are in the same section
        if( (peak.nrun()!=runNum) || (peak.detnum()!=detNum) ){
          // write out the header
          writeHeader(outStream,(Peak)peaks.elementAt(i));
          // reinit run and detector numbers
          runNum=((Peak)peaks.elementAt(i)).nrun();
          detNum=((Peak)peaks.elementAt(i)).detnum();
        }

        // skip peaks with reflection flag of 20
        if(peak.reflag()==20){
          seqnum_off--;
          continue;
        }

        // write the peak to the file
        peak.seqnum(i+seqnum_off);
        outStream.write(peak.toString()+"\n");
      }

      // flush and close the buffered file stream
      outStream.flush();
      outStream.close();
    }catch(IOException e){
      //file may not be closed
      try{
        if( outStream != null )
	{
	  // flush and close the buffered file stream
          outStream.flush();
          outStream.close(); 
	}
      }
      
      catch(IOException e2){
        //let it drop on the floor
      }
    }
    
    return file;
  }
  
  /**
   * Determine the last sequence number used in the file
   *
   * @param filename the name of the file that is being appended to
   * and has peaks already listed in it.
   *
   * @return the last sequence number that appeared in the file. If
   * anything goes wrong it returns zero instead.
   */
  static private int lastSeqNum( String filename ){
    File peakF=new File(filename);
    if(! peakF.exists()) return 0;
    if(! peakF.canRead()) return 0;
    
    TextFileReader tfr=null;
    String line=null;
    try{
      tfr=new TextFileReader(filename);
      while(!tfr.eof()){ // last line is the important one
        line=tfr.read_line();
      }
    }catch(IOException e){
      // let it drop on the floor
    }finally{
      if(tfr==null){
        return 0;
      }else{
        try{
          tfr.close();
        }catch(IOException e){
          // let it drop on the floor
        }
      }
    }
    if(line!=null){
      StringBuffer sb=new StringBuffer(line.trim());
      StringUtil.getInt(sb); // record type
      return StringUtil.getInt(sb); // sequence number
    }else{
      return 0;
    }
  }
  
  /**
   * Write an formatted headder line to the ouput stream using the
   * supplied peak for information.
   */
  private static void writeHeader(OutputStreamWriter outStream, Peak peak)
                                                            throws IOException{
    // general information header
    outStream.write("0  NRUN DETNUM    DETA   DETA2    DETD     CHI     PHI   "
                    +"OMEGA   MONCNT"+"\n");
    // general information
    outStream.write("1"+format(peak.nrun(),6)
                    +format(peak.detnum(),7)
                    +format(peak.detA(),8)
                    +format(peak.detA2(),8)
                    +format(peak.detD(),8)
                    +format(peak.chi(),8)
                    +format(peak.phi(),8)
                    +format(peak.omega(),8)
                    +format((int)peak.monct(),9)
                    +"\n");
      
    // peaks field header
    outStream.write("2  SEQN   H   K   L      X      Y      Z    XCM    "
                    +"YCM      WL   IPK     INTI     SIGI RFLG  NRUN DN"+"\n");

  }

  /* ----------------------------- formatting ------------------------------ */
  /**
   * Format an integer by padding on the left.
   */
  static private String format(int number,int length){
    String rs=new Integer(number).toString();
    while(rs.length()<length){
      rs=" "+rs;
    }
    return rs;
  }
  
  /**
   * Format a float by padding on the left.
   */
  static private String format(float number,int length){
    DecimalFormat df_ei_tw=new DecimalFormat("####0.00");
    String rs=df_ei_tw.format(number);
    while(rs.length()<length){
      rs=" "+rs;
    }
    return rs;
  }
  
  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Operator op = new WritePeaks();
    op.CopyParametersFrom( this );
    return op;
  }

  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] ){
    Vector peaked=null;

    String inPeakFile="/IPNShome/pfpeterson/multi.peaks";
    String outfile="/IPNShome/pfpeterson/lookatme.peaks";
    
    // read the peaks in from a file
    ReadPeaks ro=new ReadPeaks(inPeakFile);
    peaked=(Vector)ro.getResult();
    
    // try out this operator
    WritePeaks wo = new WritePeaks(outfile,peaked,Boolean.FALSE);
    System.out.println(wo.getResult());
    
    /* -------------- added by Chris Bouzek --------------------- */
    //System.out.println("Documentation: " + wo.getDocumentation());
    /* ---------------------------------------------------------- */
    
    System.exit(0);
  }
}
