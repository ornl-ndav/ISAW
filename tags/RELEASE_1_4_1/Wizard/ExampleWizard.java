/*
 * File: ExampleWizard.java
 *
 * Copyright (C) <year>, <original author>
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
 * Contact : <contact author> <email address>
 *           <street address>
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.3  2002/11/27 23:31:16  pfpeterson
 * standardized header
 *
 */
/**
 *  This is a very crude initial test of building a Wizard.
 */
package Wizard;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.wizard.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;
import DataSetTools.parameter.*;

public class ExampleWizard 
{

  public static void main( String args[] )
  {                                                     // build the wizard
    Wizard w = new Wizard( "Example Wizard" ); 
    Wizard.status_display.append("Example Wizard Main\n");

                                                        // define the master
                                                        // list of parameters
    w.setParameter( "Name", new StringPG( "My Name","Dennis", true ));
    w.setParameter( "Height", 
                   new FloatPG( "My Height", new Float(6.42), false ));
    w.setParameter( "Age", 
                   new FloatPG( "My Age", new Float( 51 ), false ));
    w.setParameter( "YourAge", 
          new FloatPG( "Your Age", new Float( 51 ), true ));

                                          // Create forms using the parameters
                                          // Since the base Form class execute()
                                          // method doesn't do anything these
                                          // forms also don't do anything
    String info_parms[] = { "Name" };
    String edit_parms[] = { "Height" };
    String out_parms[]  = {};
    Form form0 = new Form( "Form 0 title", 
                            info_parms, 
                            edit_parms, 
                            out_parms, 
                            w );
    form0.setHelpMessage("Help Message for Form 0");
    w.add( form0 );

    String info_parms_1[] = {};
    String edit_parms_1[] = { "Age" };
    String out_parms_1[]  = { "YourAge" };
    Form form1 = new Form( "Form 1 title", 
                            info_parms_1, 
                            edit_parms_1, 
                            out_parms_1, 
                            w );
    form1.setHelpMessage("Help Message for Form 1");
    w.add( form1 );

    w.show(0);
  }
}
