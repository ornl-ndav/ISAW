/*
 * File:  ScriptForm.java
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
 * Revision 1.15  2003/11/05 02:12:16  bouzekc
 * Changed constructor interfaces to reflect OperatorForm and Form changes.
 *
 * Revision 1.14  2003/07/09 23:15:57  bouzekc
 * Constructor now attempts to find the given filename. If not
 * found, it looks in the Script_Path directory.  Removed default
 * constructor.
 *
 * Revision 1.13  2003/07/09 22:25:12  bouzekc
 * Now implicitly uses OperatorForm's getTitle() to set the
 * Form title.
 *
 * Revision 1.12  2003/07/07 20:34:32  bouzekc
 * Now implicitly sets HAS_CONSTANTS by way of
 * setConstantParamIndices().  Now takes Script name command
 * line arguments.
 *
 * Revision 1.11  2003/07/03 14:08:53  bouzekc
 * Added all missing javadoc comments and formatted existing
 * comments.
 *
 * Revision 1.10  2003/07/02 18:52:08  bouzekc
 * Fixed javadoc spelling error.
 *
 * Revision 1.9  2003/07/02 18:24:41  bouzekc
 * Removed unused imports.
 *
 * Revision 1.8  2003/07/02 18:17:40  bouzekc
 * No longer implements Serializable.
 *
 * Revision 1.7  2003/07/02 18:16:02  bouzekc
 * Added log header.
 *
 *
 */
package DataSetTools.wizard;

import Command.ScriptOperator;

import DataSetTools.parameter.IParameterGUI;

import DataSetTools.util.*;

import java.io.File;


/**
 * The ScriptForm class is an extension of Form designed to work with Scripts.
 * Although a Form is an Operator, by creating an ScriptForm, it becomes
 * easier to implement many of the methods by using ScriptOperator.
 */
public class ScriptForm extends OperatorForm {
  //~ Constructors *************************************************************

  /**
   * Construct an ScriptForm with the given filename. This creates a
   * ScriptOperator, and allows the use of that Operator for the getResult()
   * method.
   *
   * @param filename The Script file name to use.
   */
  public ScriptForm( String filename ) {
    super(  );

    if( !( new File( filename ).exists(  ) ) ) {
      String scriptsDir = SharedData.getProperty( "Script_Path" ) + "/";

      //Script.java, which is ultimately called to create a new ISS script, uses
      //forward slashes.  We are sending the String to setFileSeparator, however,
      //to remove extra slashes.
      filename = StringUtil.setFileSeparator( scriptsDir + filename );
    }
    form_op = new ScriptOperator( filename );
    setDefaultParameters(  );
  }

  /**
   * Construct a ScriptForm with the given Script file name and result
   * parameter.
   *
   * @param filename The Script file name to use
   * @param resultPG The IParameterGUI to use for the result.
   */
  public ScriptForm( String filename, IParameterGUI resultPG ) {
    this( filename );
    setResultParam( resultPG );
    setDefaultParameters(  );
  }

  /**
   * Construct a ScriptForm with the given Script file name and result
   * parameter. This constructor allows setting of the constant parameters.
   *
   * @param filename The Script file name to use for this form
   * @param resultPG The IParameterGUI to use for the result.
   * @param indices The array of indices that represent constant parameters for
   *        this Form.
   */
  public ScriptForm( String filename, IParameterGUI resultPG, int[] indices ) {
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
    ScriptForm form = new ScriptForm( args[0] );
    System.out.println( "The Script title is " + form.getTitle(  ) );
  }
}
