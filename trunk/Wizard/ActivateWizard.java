/*
 * File:  ActivateWizard.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
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
 * $Log$
 * Revision 1.4  2003/02/26 17:21:37  rmikk
 * Now writes to DataSetTools.util.SharedData.status_pane
 *
 * Revision 1.3  2002/11/27 23:31:16  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/06/06 16:21:34  pfpeterson
 * Now use new parameters.
 *
 * Revision 1.1  2002/05/28 20:35:09  pfpeterson
 * Moved files
 *
 *
 */

package Wizard;

import DataSetTools.wizard.*;
import DataSetTools.parameter.*;

/**
 *  This class has a main program that constructs a Wizard for doing add,
 *  subtract, multiply and divide operations on a specified list of parameters.
 */
public class ActivateWizard{
    /**
     *  The main program constructs a new Wizard, defines the parameters to
     *  be stored in the master parameter list, and constructs instances of
     *  of the forms that define the operations available.
     */
    public static void main( String args[] ){
        // build the wizard and specify the help messages.
        Wizard w = new Wizard( "Activate Wizard" ); 
        DataSetTools.util.SharedData.addmsg("ActivateWizard Main\n");
        w.setHelpMessage("This wizard will let you do arithetic operations");
        w.setAboutMessage("This is a simple Demonstation Wizard, 2/26/2002, D.M.");
        
        // define the entries in the master list
        w.setParameter( "Composition",
                   new MaterialPG("Sample Composition", 
                                       new String("La,Mn,O_3"), false));
        w.setParameter( "Mass",
                   new FloatPG("Sample Mass (in g)", 
                                       new Float(1),false));
        w.setParameter( "Current",
                    new FloatPG("Beam Current (in microAmp)",
                                        new Float(16),false));
        w.setParameter( "InstrumentFac",
                    new FloatPG("Instrument Factor (LANSCE HIPD=1.0)",
                                        new Float(1),false));
        
        w.setParameter("Contact",
                       new StringPG("Contact Dose", new String(""),false));
        w.setParameter("Storage",
                       new StringPG("Storage Time", new String(""),false));
        w.setParameter("Prompt",
                       new StringPG("Prompt Activation",new String(""),false));
        
        String edit_params[]={"Composition", "Mass", 
                              "Current", "InstrumentFac"};
        String out_params[]={"Contact","Storage","Prompt"};
        
        Form form0 = new ActivateForm(  edit_params, out_params, w );
        w.add( form0 );
        
        w.show(0);
    }
}
