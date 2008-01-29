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
 * Revision 1.11  2008/01/29 19:18:42  rmikk
 * Repllaced Peak by IPeak
 *
 * Revision 1.10  2008/01/04 17:24:24  rmikk
 * Added the feature to read the paired xml file with more accurate detector
 *    information
 *
 * Revision 1.9  2007/03/13 22:04:11  rmikk
 * Made these implement HiddenOperator so they will not show up in the
 *    macros menu
 *
 * Revision 1.8  2006/07/10 21:48:01  dennis
 * Removed unused imports after refactoring to use New Parameter
 * GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.7  2006/07/10 16:26:01  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.6  2004/03/15 19:33:54  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.5  2004/03/15 03:28:39  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.4  2004/01/24 20:31:16  bouzekc
 * Removed/commented out unused variables/imports.
 *
 * Revision 1.3  2003/07/03 15:32:10  dennis
 * Fixed java docs on getResult() method.
 *
 * Revision 1.2  2003/06/05 14:48:55  dennis
 * Minor fix to javadocs. Shortened sb.append() statements to 80
 * characters.
 *
 * Revision 1.1  2003/01/31 21:11:16  pfpeterson
 * Added to CVS.
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.operator.*;
import DataSetTools.dataset.*;
import DataSetTools.instruments.*;
import gov.anl.ipns.Parameters.*;
import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.MathTools.Geometry.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

/** 
 * This operator reads in an ASCII file and converts its contents into
 * a vector of Peak objects.
 */
public class ReadPeaks extends GenericTOF_SCD implements HiddenOperator{
  private static final String TITLE       = "Read Peaks";
  
  /* ------------------------ Default constructor ------------------------- */ 
  /**
   *  Creates operator with title "Read Peaks" and a default list of
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
    sb.append("@overview This operator reads a \".peaks\" file and creates a ");
    sb.append("vector of peak objects to be worked on using other operators.");
    sb.append("In addition if the filename ends with .peaks.xml, the ");
    sb.append("corresponding .peaks file will be read and also the peaks.xml ");
    sb.append("file with more accurate information on the grids will be read");
    // assumptions
    sb.append("@assumptions Information such as calibration and orientation ");
    sb.append("matrix will be dealt with elsewhere. This only reads what is ");
    sb.append("in the one file.");
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
   *  Reads a list of peaks from a file of the form written by the WritePeaks
   *  operator.
   *
   *  @return A vector of peak objects, if the file was read successfully,
   *          or an ErrorString if the read failed.
   */
  public Object getResult(){
    // get the filename
    String  filename = getParameter(0).getValue().toString();
    boolean readXml = false;
    Node node = null;
   
    if( filename.toUpperCase().endsWith( ".PEAKS.XML" )){
       
       readXml = true;
       filename = filename.substring( 0, filename.length()-4 );
       
    }
    
    
    
    if(filename!=null && filename.length()>0){
      File file=new File(filename);
      if(! file.isFile() )
        return new ErrorString("Not regular file:"+filename);
      if(! file.canRead() )
        return new ErrorString("Cannot read file:"+filename);
    }else{
      return new ErrorString("Null or empty filename:"+filename);
    }
    Document xmlDoc = null;
    if( readXml){
       File filexml = new File( filename+".xml");
       if( !filexml.exists())
         readXml = false;
       else if(! filexml.isFile())
          readXml = false;
       else if( !filexml.canRead())
          readXml = false;
       if( readXml)
       try{
          DocumentBuilder dbuild = DocumentBuilderFactory.newInstance().
                                               newDocumentBuilder();
          xmlDoc = dbuild.parse( filexml );
          node = xmlDoc.getDocumentElement().getFirstChild();
          
          
       }catch(Exception ss){
          xmlDoc = null;
          node = null;
          readXml = false;
       }
       
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
      Vector DetInfo = null;
      while( ! tfr.eof() ){
        line=tfr.read_line();
        line=line.trim();
        if( line.length()<=0 ){ // skip empty lines
          continue;
        }else if( line.startsWith("0") ){ // skip header line
          continue;
        }else if( line.startsWith("1") ){ // change the factory for subsequent
          pkfac=createFactory(line); 
          DetInfo = NextDetector( node);
          // peaks to be created
        }else if( line.startsWith("2") ){ // skip column labels
          continue;
        }else if( line.startsWith("3") ){ // create a peak and add it to the
          peak=createPeak(line,pkfac);    // vector
          if(peak!=null && DetInfo!= null && DetInfo.size()==3 ){
             Peak_new peak1 = fixUpPeak( peak, DetInfo);
             peaks.add(peak1);
          }else
             peaks.add( peak );
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

  //Used for the reading of the parallel xml file
  //Returns the grid as the first element and ?? as second
  private Vector NextDetector(Node node) throws IOException{
     if(node == null)
        return null;
     
     Vector3D xdir = new Vector3D(1f,0f,0f);
     Vector3D ydir = new Vector3D(0f,1f,0f);
     Vector3D center = new Vector3D(1f,0f,0f);
     float width = 1f,
           height = 1f,
           depth  = .1f,
           InitialPath = 0f;
     int nrows =20, ncols = 20;
     float T0=0;
     XScale xscl = new UniformXScale( 0,100,10);
          
     //Read next detector info
     Node Nd = null;
     //Skip child node to a new element
     
     
     for( ;node != null && !(node instanceof Element) 
         && !(node.getNodeName().equals( "detector" ));
                     node = node.getNextSibling()){}
     
     if( node == null)
        return null;
     Element ENode = (Element)node;
     node = node.getNextSibling();
     int detectorID =0;
     int nrun = 0;
     NodeList children = ENode.getChildNodes();
     for( int i=0; i< children.getLength(); i++ ){
        Node N= children.item( i );
        if( N instanceof Element){
           float[] vals = GetNodeValue( N );
           String NodeName = N.getNodeName();
           if( NodeName.equals( "center" )){
              center = new Vector3D(vals[0],vals[1],vals[2]);
           }else if( NodeName.equals( "x_vec" )){ 
              xdir = new Vector3D(vals[0],vals[1],vals[2]); 
           }else if( NodeName.equals( "y_vec" )){
              ydir = new Vector3D(vals[0],vals[1],vals[2]);  
           }else if( NodeName.equals( "width" )){
              width = vals[0];
           }else if( NodeName.equals( "height" )){
              height= vals[0];  
           }else if( NodeName.equals( "depth" )){ 
              depth = vals[0]; 
           }else if( NodeName.equals( "nrows" )){  
              nrows = (int)vals[0];
           }else if( NodeName.equals( "ncols" )){  
              ncols = (int)vals[0]; 
           }else if( NodeName.equals( "T0" )){  
              T0 = (int)vals[0]; 
           }else if( NodeName.equals( "initialPath" )){  
              InitialPath = (int)vals[0]; 
           }else if( NodeName.equals( "detector" )){ 
              detectorID = (int) vals[0];
           }else if( NodeName.equals( "run" )){
              
           }else if( NodeName.equals( "xscale" )){
              String type = "Uniform";//((Element)N).getAttribute( "type" );
              if( type != null)
                 if( type.equals("Uniform")){
                    xscl = new UniformXScale(vals[0], vals[1], (int)vals[2]);
                 }else if( type.equals("Log")){
                    xscl = new GeometricProgressionXScale(vals[0], vals[1],
                                                            vals[2]);
                 }else if( type.equals("Variable")){
                    xscl = new VariableXScale(vals);
                 }
              
           }
        }
        }//for
     
      UniformGrid grid = new UniformGrid(detectorID, "m",center,xdir,ydir,
                                       width, height, depth, nrows, ncols);
      Vector V = new Vector();
      V.addElement( grid );
      V.addElement(  T0 );
      V.addElement(  InitialPath );
      V.addElement( xscl);
      return V;
     
  }
  
  // For an element node,  The value is the union of the #text nodes
  private float[]  GetNodeValue( Node N ){
     
     if( N== null)
        return null;
     String S="";
     if( N.getNodeName().equals("#text"))
        S = N.getNodeValue();
     else{
        NodeList children = N.getChildNodes();
        for(int i=0; i< children.getLength(); i++){
           Node NN = children.item(i);
           if(NN.getNodeName().equals("#text"))
              S +=NN.getNodeValue()+" ";
           else if( NN.getNodeName().equals( "#cdata-section" ))
              S += NN.getNodeValue()+" ";
        } 
     }
     S = S.trim();
     String S1 ="";
     char c=0;
     for( int i=0; i<S.length(); i++){
       char c1 = S.charAt(i);
       if(",:;".indexOf(c1)>=0)
          c1=' ';
       if( c1 > ' ' || c > ' '|| i==0)
          S1 +=c1;
       c = c1;
     }
     
     String[] SS = S1.split( " " );
     float[] Res = new float[SS.length];
     for( int i=0; i< SS.length; i++)
        Res[i] = (new Float(SS[i])).floatValue();
     
     return Res;
  }
  
  //used for fixing up a peak into a new peak.
  private Peak_new  fixUpPeak( IPeak peak, Vector DetInfo){
     
    if( DetInfo == null)
       return null;
    if( peak == null)
       return null;
    
    UniformGrid grid = (UniformGrid)DetInfo.firstElement();
    float timeAdjustment = ((Float)DetInfo.elementAt( 1 )).floatValue();
    float InitialPath =((Float)DetInfo.elementAt(2 )).floatValue();
    XScale xscl = (XScale)DetInfo.lastElement();
    
    Peak_new PP = new Peak_new(peak.x(), peak.y(),peak.z(),grid,
              new IPNS_SCD_SampleOrientation(peak.phi(), peak.chi(),peak.omega()),
              timeAdjustment, xscl, InitialPath);
    
    PP.nrun( peak.nrun());
    //PP.detnum( peak.detnum() );
    PP.inti( peak.inti() );
    PP.ipkobs( peak.ipkobs() );
    PP.monct( peak.monct() );
    PP.reflag( peak.reflag() );
    PP.seqnum( peak.seqnum() );
    PP.sigi( peak.sigi() );
    PP.UB( peak.UB() );
    return PP;
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
