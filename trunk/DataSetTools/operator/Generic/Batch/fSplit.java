/*
 * File:  fSplit.java   
 *
 * Copyright (C) 2005, John Hammonds, Tom Worltin
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
 * Contact : John Hammonds<JPHammonds@anl.gov>, Tom Worlton<TWorlton@anl.gov>
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2005/05/16 20:48:29  hammonds
 *  First in classes to work more efficiently with filenames.
 *
 *
 */
package DataSetTools.operator.Generic.Batch;
import java.io.*;
import java.net.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import java.util.Vector;
import gov.anl.ipns.Util.SpecialStrings.LoadFileString;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

public class fSplit extends GenericBatch{

  /**
   *  Construct an operator with a default parameter list
   */
  public fSplit() {
    super("Split Filename into parts");
  }

  /*
   *Contruct a real operator that takes a string and places this in a parameter
   */
  public fSplit(String fileName) {
    this();
    getParameter(0).setValue(new String(fileName));
  }
  
  /**
   * Get the value to be used as the command name in scripts
   *  @return  "fString", the command to be used to invoke this operator in a 
   *              script.
   */
  public String getCommand(){
    return "fSplit";
  }

  /**
   * Get an array of strings listing the operator category names  for 
   * this operator. The first entry in the array is the 
   * string: Operator.OPERATOR. Subsequent elements of the array determine
   * which submenu this operator will reside in.
   * 
   * @return  A list of Strings specifying the category names for the
   *          menu system 
   *        
   */
  public String[] getCategoryList()
  {
    return Operator.UTILS_SYSTEM;
  }

  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    fSplit op = new fSplit();
    op.CopyParametersFrom( this );
    return op;
  }


  /* ------------------------- setDefaultParameters ---------------------- */
  /**
   *  Set the parameters to default values.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to create empty list of
    // parameters
    
    parameters=new Vector();
    addParameter( new Parameter("File name to split",new LoadFileString("")));
  }

  public Object getResult(){
    String fileName = getParameter(0).getValue().toString();
    File f = new File(fileName);
    File fabs = f.getAbsoluteFile();
    //    System.out.println("fabs = " + fabs);
    if (!fabs.isFile()){
      return new ErrorString("no filename was given");
    }
    String nsep = fabs.separator;
    String parent = fabs.getParent();
    //    System.out.println("Directory = " + parent + nsep);
    String name = fabs.getName();
    String [] parts = null;
    parts = name.split("\\.");
    int np = parts.length;
    Vector fOut = new Vector();
    fOut.add( parent );
    if (np > 0) {
      fOut.add( parts[0]);
      if (np >1)
	fOut.add(parts[1]);
    }
    else {
      return new ErrorString("no filename was given");
    }
	     
    
    return fOut;

  }
  
  /* ---------------------------- getDocumentation -------------------------- */
 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator takes in a filename and splits it");
    Res.append("into three parts: directory, filename and extension.");

    Res.append("@return Returns a Vector (an array to the scripting");
    Res.append(" language) which has as elements, the directory, name and ");
    Res.append("extension(if one exists) of the input file.  If the result ");
    Res.append("is not a valid filename, an ErrorString is returned.");
    
    return Res.toString();
  }

  /**
   *  main methof to test things out
   */
  public static void main(String args[])
  {
    String[] partNames = {"Directory", "FileName", "Extension"};
    if (args.length !=1) {
      System.err.println("missing filename");
      System.exit(1);
    }

    fSplit op = new fSplit(args[0]);
    Vector fOut = new Vector();
    try {
      fOut = (Vector)(op.getResult());
    }
    catch (ClassCastException ex) {
      System.out.println(op.getResult().toString());
    }
    
    int numParts = fOut.size();
   
    for (int ii = 0; ii < numParts; ii++){
      System.out.println(partNames[ii] + ": " + (String)(fOut.elementAt(ii)));
    }

  }
}
