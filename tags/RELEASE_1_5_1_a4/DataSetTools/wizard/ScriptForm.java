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

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.util.*;
import DataSetTools.components.ParametersGUI.*;
import Command.ScriptOperator;

/**
  * The ScriptForm class is an extension of Form designed to work 
  * with Scripts.  Although a Form is an Operator, 
  * by creating an ScriptForm, it becomes easier to implement
  * many of the methods by using ScriptOperator.
 */

public class ScriptForm extends OperatorForm implements Serializable{
  /**
   *  Construct an ScriptForm with the given title.  
   *
   *  @param  title           The title to show on this form
   *
   */
  public ScriptForm()
  {
    super();   
  } 

  /**
   *  Construct an ScriptForm with the given filename.
   *  This creates a ScriptOperator, and allows the use of that 
   *  Operator for the getResult() method.  
   *
   *  @param  title           The title to show on this form
   *
   */
  public ScriptForm( String filename )
  {
    this();
    super.form_op = new ScriptOperator(filename);
    super.setDefaultParameters();
  }

}


