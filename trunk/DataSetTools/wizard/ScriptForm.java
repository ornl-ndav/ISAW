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
 */
package DataSetTools.wizard;

import Command.ScriptOperator;

import DataSetTools.components.ParametersGUI.*;

import DataSetTools.operator.*;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;


/**
 * The ScriptForm class is an extension of Form designed to work
 * with Scripts.  Although a Form is an Operator,
 * by creating an ScriptForm, it becomes easier to implement
 * many of the methods by using ScriptOperator.
 */
public class ScriptForm extends OperatorForm implements Serializable {
  /**
   *  Construct an ScriptForm with the title "Script Form."
   *
   */
  public ScriptForm(  ) {
    super( "Script Form" );
  }

  /**
   *  Construct an ScriptForm with the given filename.
   *  This creates a ScriptOperator, and allows the use of that
   *  Operator for the getResult() method.
   *
   *  @param  filename           The Script file name to use.
   *
   */
  public ScriptForm( String filename ) {
    //must have super() call be the first, but we want only the Script
    //filename, so we'll trim off the rest.  I could have used File's getName()
    //method, but I did not want to create a File Object.
    super( 
      filename.substring( 
        ( FilenameUtil.setForwardSlash( filename ) ).lastIndexOf( '/' ) + 1,
        filename.length(  ) ) );
    form_op = new ScriptOperator( filename );
    setDefaultParameters(  );
  }

  /**
   *  Construct a ScriptForm with the given Script file nameand
   *  result parameter type.
   *
   *  @param  filename        The Script file name to use
   *
   *  @param  type            The IParameterGUI type of the result
   *                          parameter.  e.g. for a LoadFilePG,
   *                          use "LoadFile"
   *
   *  @param  name            The name of the result parameter.
   *                          e.g. "log file"
   *
   */
  public ScriptForm( String filename, String type, String name ) {
    this( filename );
    setParamClass( type );

    //after calling that, we need to call setDefaultParameters()
    result_param.setName( name );
    setDefaultParameters(  );
  }

  /**
   *  Construct a ScriptForm with the given Script file name and
   *  result parameter type. This constructor allows setting of the
   *  constant parameters.
   *
   *  @param  filename        The Script file name to use for this form
   *
   *  @param  type            The IParameterGUI type of the result
   *                          parameter.  e.g. for a LoadFilePG,
   *                          use "LoadFile"
   *
   *  @param  name            The name of the result parameter.
   *                          e.g. "log file"
   *
   *  @param indices          The array of indices that represent constant
   *                          parameters for this Form.
   *
   *
   */
  public ScriptForm( String filename, String type, String name, int[] indices ) {
    this( filename );
    setParamClass( type );

    //after calling that, we need to call setDefaultParameters()
    result_param.setName( name );
    HAS_CONSTANTS = true;
    this.setConstantParamIndices( indices );
    setDefaultParameters(  );
  }

  /**
   *  Testbed.
   */
  public static void main( String[] args ) {
    ScriptForm form = new ScriptForm( 
        "/IPNShome/bouzekc/ISAW/Scripts/find_multiple_peaks.iss" );

    System.out.println( "The Script title is " + form.getTitle(  ) );
  }
}
