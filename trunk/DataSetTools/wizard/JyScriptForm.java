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

import DataSetTools.operator.PyOperatorFactory;

import DataSetTools.util.FilenameUtil;


/**
 * The JyScriptForm class is an extension of OperatorForm designed to work with
 * Jython scripts by using a Jython script to create an Operator and then a
 * Form.
 */
public class JyScriptForm extends OperatorForm {
  //~ Constructors *************************************************************

  /**
   * Construct a JyScriptForm with the given filename.  This uses
   * PyOperatorFactory to create an Operator and allows the use of that
   * Operator for the getResult() method.
   *
   * @param filename The Jython script file name to use.
   */
  public JyScriptForm( String filename ) {
    super(  );
    form_op = new PyOperatorFactory(  ).getInstance( filename );
    setDefaultParameters(  );
  }

  /**
   * Construct a JyScriptForm with the given Jython script file name and result
   * parameter type.
   *
   * @param filename The Jython script file name to use
   * @param type The IParameterGUI type of the result parameter.  e.g. for a
   *        LoadFilePG, use "LoadFile"
   * @param name The name of the result parameter. e.g. "log file"
   */
  public JyScriptForm( String filename, String type, String name ) {
    this( filename );
    setParamClass( type );

    result_param.setName( name );
    setDefaultParameters(  );
  }

  /**
   * Construct a JyScriptForm with the given Jython script file name and result
   * parameter type. This constructor allows setting of the constant
   * parameters.
   *
   * @param filename The Jython script file name to use for this form
   * @param type The IParameterGUI type of the result parameter.  e.g. for a
   *        LoadFilePG, use "LoadFile"
   * @param name The name of the result parameter. e.g. "log file"
   * @param indices The array of indices that represent constant parameters for
   *        this Form.
   */
  public JyScriptForm( 
    String filename, String type, String name, int[] indices ) {
    this( filename );
    setParamClass( type );
    result_param.setName( name );
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
