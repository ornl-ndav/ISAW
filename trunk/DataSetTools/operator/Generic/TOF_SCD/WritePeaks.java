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
 * Revision 1.17  2008/01/04 17:25:33  rmikk
 * Added the feature to create a paired xml file( if it has more info) with more
 *    detailed information about the detector.
 *
 * Revision 1.16  2004/03/15 03:28:39  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.15  2004/01/24 20:31:16  bouzekc
 * Removed/commented out unused variables/imports.
 *
 * Revision 1.14  2003/12/15 02:38:18  bouzekc
 * Removed unused imports.
 *
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

import gov.anl.ipns.Util.File.TextFileReader;
import gov.anl.ipns.Util.Sys.StringUtil;
import gov.anl.ipns.MathTools.Geometry.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import DataSetTools.operator.HiddenOperator;
import DataSetTools.operator.Operator;
import DataSetTools.operator.Parameter;
import DataSetTools.dataset.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
/** 
 * This operator is a small building block of an ISAW version of
 * A.J.Schultz's PEAKS program. This operator writes out the
 * information in a format specified by Art.
 */
public class WritePeaks extends GenericTOF_SCD implements HiddenOperator{
  private static final String TITLE       = "Write Peaks";
  
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
      s.append("by Art.\n If the filename ends with .peaks.xml then two");
      s.append("files may be written. The first is the regular peaks ");
      s.append(" file( no .xml) and the 2nd contains detailed position "); 
      s.append("information about the data grids.");
      s.append("@assumptions If the specified file already exists, it will\n");
      s.append("be overwritten if append is false and appended if append"); 
      s.append("is true.\n Furthermore if the file extension is peaks.xml a ");
      s.append("2nd file with more accurate detector information \"may\" be written "); 
      s.append("along with the file with the .peaks extension.\n"); 
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
    
    boolean writeXml = false;
  
    Document xmlDoc = null;
    
    
    if( file == null)
       return new gov.anl.ipns.Util.SpecialStrings.ErrorString
                                                ("Cannot save to a null file");
    if( file.toUpperCase().endsWith(".PEAKS.XML")){
    		writeXml = true;
    		file = file.substring(0,file.length()-4);
    }
    if( peaks == null)
       return new gov.anl.ipns.Util.SpecialStrings.ErrorString
       ("Cannot save 0 peaks to a file");
    if( writeXml)
       for( int i=0; i< peaks.size() && writeXml; i++)
          if( !(peaks.elementAt(i) instanceof Peak_new))
                   writeXml = false;
         
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
      if( writeXml){
         xmlDoc = InitXml( file,append);
         if( xmlDoc == null)
            writeXml = false;
       
      }
      // loop through the peaks
      for( int i=0 ; i<peaks.size() ; i++ ){
        peak=(Peak)peaks.elementAt(i);

        // check that we are in the same section
        if( (peak.nrun()!=runNum) || (peak.detnum()!=detNum) ){
          // write out the header
          writeHeader(outStream,(Peak)peaks.elementAt(i));
          writeHeaderXml(xmlDoc,  (Peak)peaks.elementAt( i ));
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
    FinishXml( xmlDoc, file );
    return file;
  }
  
  private Document InitXml( String file, boolean append)
                        throws IOException{
    File F = new File( file+".xml");
    if( !F.exists())
       append = false;
    
    Document xmlDoc = null;
    if( append )
       try{
          DocumentBuilder dbuild = DocumentBuilderFactory.newInstance().
                                               newDocumentBuilder();
          xmlDoc = dbuild.parse( F );
         
          
          
       }catch(Exception ss){
          xmlDoc = null;
         
          append = false;
       }
     if( !append)
        try{
           DocumentBuilder dbuild = DocumentBuilderFactory.newInstance().
                                                       newDocumentBuilder(); 
           xmlDoc = dbuild.newDocument();
           xmlDoc.appendChild( xmlDoc.createElement("data"));
        }catch( Exception ss){
           
        }
    F=null;
    
    return xmlDoc;
  }
  
  private void writeHeaderXml( Document xmlDoc,  Peak P)
                                     throws IOException{
     if( xmlDoc == null || P == null)
        return;
     
     if( !(P instanceof Peak_new))
        throw new IllegalArgumentException(" Internal error A. WritePeaks");
     
     Node node = xmlDoc.createElement( "detector" );
       AddSpaceNL(xmlDoc,node,3); 
       Node attribute = xmlDoc.createElement( "run" );
         Text childVal=xmlDoc.createTextNode( ""+P.nrun() );
         attribute.appendChild( childVal );
          node.appendChild( attribute );

       AddSpaceNL(xmlDoc,node,3); 
       attribute = xmlDoc.createElement( "detector" );
          childVal=xmlDoc.createTextNode(""+P.detnum() );
          attribute.appendChild( childVal );
          node.appendChild( attribute );

      
     AddSpaceNL(xmlDoc,xmlDoc.getDocumentElement(),1);     
     xmlDoc.getDocumentElement().appendChild( node );
     AddSpaceNL(xmlDoc,node,3);
 
     IDataGrid grid = ((Peak_new)P).getGrid();
     XScale xscl =((Peak_new)P).getXscale(); 
     float[] center = grid.position().get();
     Node child = xmlDoc.createElement( "center" );
     childVal = xmlDoc.createTextNode(  center[0]+"  "+center[1]+"  "
              +center[2]);
         child.appendChild( childVal );
         node.appendChild( child);

     AddSpaceNL(xmlDoc,node,3);
     child = xmlDoc.createElement( "x_vec" );
        center= grid.x_vec().get();
        childVal = xmlDoc.createTextNode(   center[0]+"  "+center[1]+"  "
              +center[2]);
        child.appendChild( childVal );
        node.appendChild( child);
        

     AddSpaceNL(xmlDoc,node,3);
     child = xmlDoc.createElement( "y_vec" );
        center= grid.y_vec().get();
        childVal = xmlDoc.createTextNode(  center[0]+"  "+center[1]+"  "
              +center[2]);
        child.appendChild( childVal );
        node.appendChild( child);

     AddSpaceNL(xmlDoc,node,3);
     if( !Float.isNaN(  grid.width() )){
        child = xmlDoc.createElement( "width" );
        childVal = xmlDoc.createTextNode(grid.width()+"");
        child.appendChild( childVal );
        node.appendChild( child);
     }                                         

     if( !Float.isNaN(  grid.height() )){

        AddSpaceNL(xmlDoc,node,3);
        child = xmlDoc.createElement( "depth" );
        childVal = xmlDoc.createTextNode(grid.height()+"");
        child.appendChild( childVal );
        node.appendChild( child);
                                                
     }
     if( !Float.isNaN(  grid.depth() )){

        AddSpaceNL(xmlDoc,node,3);
        child = xmlDoc.createElement( "depth" );
        childVal = xmlDoc.createTextNode(grid.depth()+"");
        child.appendChild( childVal );
        node.appendChild( child);
     }

     AddSpaceNL(xmlDoc,node,3);
     child = xmlDoc.createElement( "T0" );
       childVal = xmlDoc.createTextNode(((Peak_new)P).timeAdjust()+"");
       child.appendChild( childVal );
       node.appendChild( child);

     AddSpaceNL(xmlDoc,node,3);  
     child = xmlDoc.createElement( "initialPath" ); 
        childVal = xmlDoc.createTextNode(""+((Peak_new)P).L1());
        child.appendChild( childVal );
        node.appendChild( child);

     AddSpaceNL(xmlDoc,node,3);   
     child = xmlDoc.createElement( "nrows");
        childVal = xmlDoc.createTextNode(""+grid.num_rows());
        child.appendChild( childVal );
        node.appendChild( child);
        

     AddSpaceNL(xmlDoc,node,3);   
     child = xmlDoc.createElement( "ncols");
        childVal = xmlDoc.createTextNode(""+grid.num_cols());
        child.appendChild( childVal );
        node.appendChild( child);

     AddSpaceNL(xmlDoc,node,3);   
     child = xmlDoc.createElement( "xscale");
          AddSpaceNL(xmlDoc,child,6);
          attribute = xmlDoc.createElement( "type");
          childVal = xmlDoc.createTextNode("Uniform");
          attribute.appendChild( childVal );
          child.appendChild( attribute );
          AddSpaceNL(xmlDoc,child,6);
          childVal = xmlDoc.createTextNode(""+xscl.getStart_x()+
                "  "+xscl.getEnd_x()+"  "+xscl.getNum_x());
          child.appendChild( childVal );
          node.appendChild( child);

     AddSpaceNL(xmlDoc,node,1);

  }
  
  private void AddSpaceNL( Document xmlDoc, Node node, int nspaceAfter){
     if( nspaceAfter < 0)
        nspaceAfter = 0;
     if( nspaceAfter > 70)
        nspaceAfter = 70;
     String S ="";
     for( int i=0; i< nspaceAfter; i++)S +=' ';
     Text tnode =xmlDoc.createTextNode( "\n"+ S);
     node.appendChild( tnode );
     
  }
  private void FinishXml( Document xmlDoc, String fileName){
     
     if( xmlDoc == null)
        return;
     if( fileName == null)
        return;
     try{
       Transformer trans = TransformerFactory.newInstance().newTransformer();
       trans.transform( new DOMSource(xmlDoc), new StreamResult( new File(fileName+".xml")));
        
     }catch(Exception ss){
        javax.swing.JOptionPane.showMessageDialog( null , "Cannot Save detector info" );
     }
     
     
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
