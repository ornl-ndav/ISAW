/*
 * File:  SetNewIntegratePk.java
 *
 * Copyright (C) 2004 Ruth
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
 * Contact : Ruth Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.1  2004/06/18 22:22:41  rmikk
 * Initial Checkin
 *
 */

package Operators.TOF_SCD;

import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import Command.*;
import java.io.*;
import DataSetTools.dataset.*;


/**
 * This class lets the user plug in an alternate IntegratePeak routine of a
 * special format. It also adds the Information operator the given DataSet( if
 * it is not the Empty Data Set.
 * @author MikkelsonR
 *
 */
public class SetNewIntegratePk implements Wrappable {
 
  public DataSet DS;  // The Data Set to add the IntegratePk operator to
  public LoadFileString filename; //The filename(java or class) with the Wrappable
                                  // that integrates the peak

  /**
   *  Return SetIntegrate the name used to invoke this operator in scripts
   */
  public String getCommand(  ) {
    return "SetIntegrate";
  }

  
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer(  );
       s.append( "@overview This class lets the user plug in an alternate " );
       s.append(  "IntegratePeak routine of a special format. It also adds the "  );
       s.append(  "Information operator the given DataSet( if it is not the "); 
       s.append( "Empty Data Set." );
       s.append( "@algorithm This operator converts the filename to a possible" );
       s.append( "classname. Then it uses Class.forName to get the class, ");
       s.append(  "creating an instanceof this class. A lot of error checking");
       s.append(  " is done.Next the DataSet has the IntegratePt operator added");
       s.append(  " to it if it is not already one of its operators" );
       s.append( "@DS The DataSet to which the IntegratePt operator is to be");
       s.append(  "added. Use the Empty DataSet if this is not desired" );
       s.append( "@filename the .class or .java file with the integrate 1 peak");
        s.append(  "operator. There MUST be a .class file for this" );
      
       s.append( "@return Success or error messages. " );
       s.append( "@error Improper Filename; ClassPath is not set up; etc. " );
       s.append( "occur. For example: Error occurs if number of bins is zero." );
       return s.toString(  );

   
  }

  /**
   *  Sets the experimental Integrate one peak class and ensures that the 
   *  DataSet has the integrate information operator in it
   * 
   */
  public Object calculate(  ) {
    if( filename == null)
       return new ErrorString("Improper Filename");
    String fil = filename.toString();
    fil = fil.replace('\\','/');
    String Path = System.getProperty("java.class.path");
    if( Path == null)
       return new ErrorString("ClassPath is not set up");
    Path=Path.replace('\\','/');
    Path=Path.replace(java.io.File.pathSeparatorChar,';');
    if(!Path.endsWith(";"))
       Path = Path +";";
    int j=0;
    String ClassPart =null;
    for( int i =Path.indexOf(';',j); (j < Path.length())&&(i>=0)&&(ClassPart==null);
            i = Path.indexOf(';',i+1)){
      String P = Path.substring(j,i);
      j=i+1;
      int match = fil.indexOf(P);
      if( match ==0)
         ClassPart = fil.substring( P.length());
      
      
    }
    if(ClassPart == null)
      return new ErrorString("File is NOT on the class path");
      
    //------  Fix up ClassPart.  Eliminate leading junk -------------
    while((ClassPart.length()>0)&&!Character.isJavaIdentifierStart(ClassPart.charAt(0)))
       ClassPart = ClassPart.substring(1);
    if( ClassPart == null)
      return new ErrorString("Improper filename");
    if( ClassPart.length()<4)
      return new ErrorString("Improper filename");
    
    //---------------Eliminate trailing .class or .java
    
    if(ClassPart.endsWith(".class"))
       ClassPart = ClassPart.substring(0, ClassPart.length()-6);
    else if(ClassPart.endsWith(".java"))
      ClassPart = ClassPart.substring(0, ClassPart.length()-5);
    else
       return new ErrorString("File must be java or Class File");
       
    if( ClassPart.indexOf('.')>=0)
      return new ErrorString("Filename cannot have extra dots in them");
     
    //---------------- Create the Class---------------------------
    ClassPart = ClassPart.replace('/','.');
    Object operator=null;
    try{
      Class C = Class.forName( ClassPart);
      operator = C.newInstance();
    }catch(Exception s1){
      return new ErrorString("Cannot load the class file "+s1);
    }
    if( !(operator instanceof Wrappable))
      return new ErrorString("Class must be a Wrappable");
    DataSetTools.operator.DataSet.Conversion.XAxis.IntegratePt.setIntgratePkOp(
             (Wrappable)operator);
            
    //-------------------- Now add IntegratePt to the list of DataSet Operators---  
    if( DS != DataSet.EMPTY_DATA_SET){
    
      DataSetOperator op =DS.getOperator("Integrate1");
      if( op == null)
        DS.addOperator(new DataSetTools.operator.DataSet.Conversion.XAxis.IntegratePt());
    }
    return "Success";
  }
}
