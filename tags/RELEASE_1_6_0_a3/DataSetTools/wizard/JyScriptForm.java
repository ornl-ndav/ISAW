/*
 * File:  JyScriptForm.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.8  2003/11/05 02:12:16  bouzekc
 * Changed constructor interfaces to reflect OperatorForm and Form changes.
 *
 * Revision 1.7  2003/10/10 00:56:06  bouzekc
 * Removed import of PyOperatorFactory.
 *
 * Revision 1.6  2003/08/11 18:02:42  bouzekc
 * Now uses PyScriptOperator.
 *
 * Revision 1.5  2003/07/09 23:15:40  bouzekc
 * Constructor now attempts to find the given filename. If not
 * found, it looks in the Script_Path directory.
 *
 * Revision 1.4  2003/07/09 22:25:11  bouzekc
 * Now implicitly uses OperatorForm's getTitle() to set the
 * Form title.
 *
 * Revision 1.3  2003/07/07 20:35:51  bouzekc
 * Now implicitly sets HAS_CONSTANTS by way of
 * setConstantParamIndices().  Now takes Script name command
 * line arguments.  Removed default constructor.
 *
 * Revision 1.2  2003/07/03 14:13:02  bouzekc
 * Added all missing javadoc comments and formatted existing
 * comments.
 *
 * Revision 1.1  2003/07/02 18:52:43  bouzekc
 * Added to CVS.
 *
 *
 */
package DataSetTools.wizard;

import DataSetTools.operator.PyScriptOperator;

import DataSetTools.parameter.IParameterGUI;

import DataSetTools.util.*;

import java.io.File;


/**
 * The JyScriptForm class is an extension of OperatorForm designed to work with
 * Jython scripts by using a Jython script to create an Operator and then a
 * Form.
 */
public class JyScriptForm extends OperatorForm {
  //~ Constructors *************************************************************

  /**
   * Construct a JyScriptForm with the given filename.  This uses
   * PyScriptOperator to create an Operator and allows the use of that
   * Operator for the getResult() method.
   *
   * @param filename The Jython script file name to use.
   */
  public JyScriptForm( String filename ) {
    super(  );

    if( !( new File( filename ).exists(  ) ) ) {
      String jyScriptsDir = SharedData.getProperty( "Script_Path" ) + "/";

      //Script.java, which is ultimately called to create a new Jython script, uses
      //forward slashes.  We are sending the String to setFileSeparator, however,
      //to remove extra slashes.
      filename = StringUtil.setFileSeparator( jyScriptsDir + filename );
    }
    form_op = new PyScriptOperator( filename );
    setDefaultParameters(  );
  }

  /**
   * Construct a JyScriptForm with the given Jython script file name and result
   * parameter.
   *
   * @param filename The Jython script file name to use
   * @param resultPG The IParameterGUI to use for the result.
   */
  public JyScriptForm( String filename, IParameterGUI resultPG ) {
    this( filename );
    setResultParam( resultPG );
    setDefaultParameters(  );
  }

  /**
   * Construct a JyScriptForm with the given Jython script file name and result
   * parameter. This constructor allows setting of the constant parameters.
   *
   * @param filename The Jython script file name to use for this form
   * @param resultPG The IParameterGUI to use for the result.
   * @param indices The array of indices that represent constant parameters for
   *        this Form.
   */
  public JyScriptForm( String filename, IParameterGUI resultPG, int[] indices ) {
    this( filename );
    setResultParam( resultPG );
    setConstantParamIndices( indices );
    setDefaultParameters(  );
  }

  //~ Methods ******************************************************************

  /**
   * Testbed.
   */
  public static void main( String[] args ) {
    JyScriptForm form = new JyScriptForm( args[0] );
    System.out.println( "The Script title is " + form.getTitle(  ) );
  }
}
