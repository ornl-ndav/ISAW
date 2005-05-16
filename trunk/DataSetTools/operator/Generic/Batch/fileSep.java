/*
 * File:  fileSep.java   
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
 *  Revision 1.1  2005/05/16 20:48:31  hammonds
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

public class fileSep extends GenericBatch{

  /**
   *  Construct an operator with a default parameter list
   */
  public fileSep() {
    super("Get system file separator");
  }

  /**
   * Get the value to be used as the command name in scripts
   *  @return  "fString", the command to be used to invoke this operator in a 
   *              script.
   */
  public String getCommand(){
    return "fileSep";
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
    fileSep op = new fileSep();
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
    
  }

  /**
   *   get result does the proceesing.
   */
  public Object getResult(){
    return System.getProperty("file.separator");

  }
  
  /* ---------------------------- getDocumentation -------------------------- */
 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator returns a string representing the ");
    Res.append("character used to separate filenames from directories. ");
 
    Res.append("@algorithm call System.getProperty(\"file.separator\")");
    
    Res.append("@return Returns a string with the separator.");
    
    return Res.toString();
  }

  /**
   *   main method to test things out.
   */
  public static void main(String args[])
  {
    fileSep op = new fileSep();
      String fOut = (String)(op.getResult());
      System.out.println("System file seperator: " + fOut);

  }
}
