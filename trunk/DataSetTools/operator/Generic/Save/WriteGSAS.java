/*
 * File:  gsas.java 
 *
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.7  2003/02/03 21:49:07  dennis
 * Added getDocumentationMethod() and java docs for getResult().
 * (Joshua Olson)
 *
 * Revision 1.6  2002/11/27 23:21:28  pfpeterson
 * standardized header
 *
 * Revision 1.5  2002/11/26 17:10:19  pfpeterson
 * Small changes for stability and uses a SaveFilePG for selecting the filename.
 *
 * Revision 1.4  2002/11/26 16:52:42  pfpeterson
 * reformating
 *
 * Revision 1.3  2002/05/20 20:23:12  pfpeterson
 * Added checkbox to allow for numbering banks as the
 * raw data does or sequentially.
 *
 * Revision 1.2  2002/05/17 22:20:20  pfpeterson
 * Added checkbox for exporting monitor spectrum. The integrated
 * monitor count is still included in the file.
 *
 * Revision 1.1  2002/02/22 20:58:15  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.5  2002/01/14 20:28:49  pfpeterson
 * Modified to use writer interface for GSAS files
 *
 */

package DataSetTools.operator.Generic.Save;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.writer.*;
import DataSetTools.gsastools.*;
import java.util.*;
 
/**
 * This is an operator shell around a GsasWriter object and is invoked by 
 * the Save gsas File menu option in ISAW.  The Title in Menu's that refers 
 * to this is <B>Save As gsas</b>. The Command in Scripts used to refer to 
 * this operation is <B>SaveGSAS</b>.
 */
public class WriteGSAS extends GenericSave{
  public WriteGSAS(){
    super( "Save as GSAS " );
    setDefaultParameters();
  }

  /** 
   * @param MS A monitor data set
   * @param DS The data set that is to be saved in gsas format
   * @param filename The name of the file where the data will be saved
   * @param em Whether to export the monitor.
   * @param sn Whether to sequentially number the banks.
   */
  public WriteGSAS( DataSet MS, DataSet DS, String filename, Boolean em,
                    Boolean sn ){
    this();
    getParameter(0).setValue(MS);
    getParameter(1).setValue(DS);
    getParameter(2).setValue(filename);
    getParameter(3).setValue(em);
    getParameter(4).setValue(sn);
  }

  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter( new Parameter("Monitor" , new DataSet("","") ));
    addParameter( new Parameter("Data Set" , new DataSet("","") ));
    //addParameter( new SaveFilePG("Output File", "filename"));
    SaveFilePG sfpg=new SaveFilePG("Output File",null);
    sfpg.setFilter(new GsasFileFilter());
    addParameter( sfpg );
    addParameter( new Parameter("Export Monitor", Boolean.TRUE));
    addParameter( new Parameter("Sequential Bank Numbering", Boolean.FALSE));
  }  
   
  /** 
   * Returns <B>SaveGSAS</b>, the command used by scripts to refer to this
   * operation
   */ 
  public String getCommand(){
    return "SaveGSAS";
  }

 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */                                                                 				         
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");                                                 
    s.append("@overview This is an operator shell around a GsasWriter ");
    s.append("object and is invoked by the Save gsas File menu option in ");
    s.append("ISAW. \n This operator writes a DataSet to a file in GSAS ");
    s.append("format. ");
    s.append("@assumptions The given data set DS is not empty. \n");    
    s.append("The specified filename either does not exist, ");
    s.append("or it is acceptable to overwrite it.\n");                                                                 
    s.append("@algorithm This operator simply saves a data set in GSAS ");
    s.append("format.  An object of type GsasWriter is created, and it ");
    s.append("is used to call the writeDataSets method.");
    s.append("@param MS A monitor data set");
    s.append("@param DS The data set that is to be saved in GSAS format");
    s.append("@param filename The name of the file where the data ");
    s.append("will be saved");
    s.append("@param em Indicates whether or not to write the monitor ");
    s.append("data to the file.");
    s.append("@param sn Indicates whether or not to sequentially number ");
    s.append("the banks.");    
    s.append("@return Always returns the string 'Success'. ");
    return s.toString();
  }

  /** 
   * executes the gsas command, saving the data to the file in gsas
   * form.
   *
   * @return  "Success" only
   */
  public Object getResult(){
    DataSet MS       =(DataSet)( getParameter(0).getValue());
    DataSet DS       =(DataSet)( getParameter(1).getValue());
    String  filename =getParameter(2).getValue().toString();
    boolean em       =((Boolean)(getParameter(3).getValue())).booleanValue();
    boolean sn       =((Boolean)(getParameter(4).getValue())).booleanValue();

    //System.out.println("(WG)NUMBERING: "+sn);
    GsasWriter gw=new GsasWriter(filename,em,sn);
    gw.writeDataSets(new DataSet[] {MS , DS});

    return "Success";
  }

  /** 
   * Creates a clone of this operator.
   */
  public Object clone(){
    WriteGSAS W = new WriteGSAS();
    W.CopyParametersFrom( this );
    return W;
  }

  public static void main( String args[]){
    System.out.println("WriteGSAS test... operator compiled and can run");
  }
}
