/*
 * File:  ShowDoc.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2005/01/31 23:52:59  dennis
 * Added getCategoryList() method to explicitly place the operator
 * in Macros->Utils->System.
 *
 * Revision 1.1  2005/01/06 15:45:49  rmikk
 * Initial Checkin
 * This is an operator that will pop up simple help pages.  It can be called
 * from scripts
 *
 */

package DataSetTools.operator.Generic.Load;

import DataSetTools.parameter.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;
import javax.swing.*;
import Command.*;
import gov.anl.ipns.Util.SpecialStrings.*;
 
 /**
  * This class is an operator that will display a simple html help page with no 
  * navigation ability.
  * 
  * @author mikkelsonr
  * 
  */
public class ShowDoc extends GenericLoad{  
  
  
   /**
    *  Constructor
    */
   public ShowDoc(){

     super("Show Documentation");
     setDefaultParameters();

   }
   
   
   /**
    * Constructor to use this with Java and Jython code.
    * @param filename  The name of the html file
    */
   public ShowDoc( String filename){

      this();
      getParameter(0).setValue( new String( filename));

   }
   

   /**
    *  Sets the default parameters for this operator
    */
   public void setDefaultParameters(){

      this.clearParametersVector();
      addParameter( new LoadFilePG("Enter Filename", ""));

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
   *   Displays the help page
   */
  public Object getResult(){
    
    String filename = getParameter(0).getValue().toString();
 
    JFrame jf = new JFrame(filename);
    String S ="400"; 
    jf.setSize( SharedData.getintProperty("Isaw_Width", S)/2,
         SharedData.getintProperty("Isaw_Height", S )  );

    try{
       jf.getContentPane().add( new JScrollPane(
              new JEditorPane( "text/html",(new Script(filename)).toString())));
       jf.show();
    }catch(Exception ss){
       return new ErrorString( ss);
    }
   
    return "Success";
    }

    
  public String getDocumentation(){

    StringBuffer s= new StringBuffer( "");

    s.append("@overview This class is an operator that will display a simple ");

    s.append("html help page with no navigation ability. ");
   
    s.append("@assumptions The file is a legitimate html file viewable by ");
    s.append("Java's html\n");
    s.append("JEditor Pane viewer");
   
    s.append("@param filename  The name of the file to view\n");
    s.append("@return Success");
    s.append("@error Returns any error that occurs\n");
    return s.toString();

  }

 }
