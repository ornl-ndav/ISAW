/*
 * File:  ActivateForm.java
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
 * Revision 1.1  2002/05/28 20:35:08  pfpeterson
 * Moved files
 *
 *
 */

package Wizard;

import java.io.*;
import DataSetTools.wizard.*;
import Operators.Calculator.*;

/**
 *  This class defines a form for adding a list of numbers under the control
 *  of a Wizard.
 */
public class ActivateForm extends Form{
    private static String[] inst_facs={"IPNS CHEX     ",
                                       "IPNS GPPD     ",
                                       "IPNS HIPD     ",
                                       "IPNS SAD      ",
                                       "IPNS POSY     ",
                                       "IPNS POSY II  ",
                                       "IPNS SAND     ",
                                       "IPNS LRMECS   ",
                                       "IPNS SEPD     0.5",
                                       "IPNS SCD      ",
                                       "IPNS GLAD     ",
                                       "IPNS QENS     ",
                                       "IPNS HRMECS   ",
                                       "LANSCE HIPD   1.00 (reference)",
                                       "LANSCE SCD    1.44",
                                       "LANSCE FDS    0.50",
                                       "LANSCE CQS    1.65",
                                       "LANSCE NPD    0.025",
                                       "LANSCE PHAROS 0.060",
                                       "LANSCE LQD    0.033",
                                       "LANSCE SPEAR  0.041",};

    /**
     *  Construct an ActivateForm to add the parameters named in 
     *  the list operands[] and place the result in the parameter named by
     *  result[0].  This constructor basically just calls the super class
     *  constructor and builds an appropriate help message for the form.
     *
     *  @param  operands  The list of names of parameters to be added.
     *  @param  result    The list of names of parameters to be calculated,
     *                    in this case only result[0] is used.
     *  @param  w         The wizard controlling this form. 
     */
    public ActivateForm( String operands[], String results[], Wizard w ){
        super("Calculate Activation of Sample", null, operands, results, w );
        
        // Sample Composition                  - string
        // Sample Mass(in g)                   - float
        // Beam Current (in microAmp)          - float
        // Instrument Factor (LANSCE HIPD=1.0) - float
        
        // contact dose                        - string
        // storage time                        - string
        // prompt activation                   - string
        
        String help = "This form calculates the contact dose, prompt activation,\n"
            +"and storage time for the specified sample.\n\n"
            +"The instrument factors are:\n";
        for ( int i = 0; i < inst_facs.length; i++ )
            help = help + "  " + inst_facs[i] + "\n";
        setHelpMessage( help );
    }
    
    /**
     *  This overrides the execute() method of the super class and provides
     *  the code that actually does the calculation.
     *
     *  @return This always returns true, though a more robust version might
     *          check that the values were valid numbers and only set the
     *          result value and return true in that case.
     */
    public boolean execute(){
        WizardParameter param;
        String result;
        
        if( (editable_params.length!=4) || (result_params.length!=3) ){
            if(editable_params.length!=4)
                wizard.status_display.append("Wizard calling form "
                                             +"incorrectly: wrong number of "
                                             +"editable parameters\n");
            if(result_params.length!=3)
                wizard.status_display.append("Wizard calling form "
                                             +"incorrectly: wrong number of "
                                             +"result parameters\n");
            return false;
        }

        param=wizard.getParameter("Composition");
        String material=(String)param.getNewValue();

        param=wizard.getParameter("Mass");
        float mass=((Float)param.getNewValue()).floatValue();
        if(mass<=0f){
            param.unSet();
            wizard.status_display.append("Invalid Mass Specified: "+mass);
            return false;
        }

        param=wizard.getParameter("Current");
        float current=((Float)param.getNewValue()).floatValue();
        if(current<=0f){
            param.unSet();
            wizard.status_display.append("Invalid Current Specified: "
                                         +current);
            return false;
        }

        param=wizard.getParameter("InstrumentFac");
        float inst_fac=((Float)param.getNewValue()).floatValue();
        if(inst_fac<=0f){
            param.unSet();
            wizard.status_display.append("Invalid Instrument Factor "
                                         +"Specified: "+inst_fac);
            return false;
        }

        param=wizard.getParameter("Contact");
        ActivateContact ac=new ActivateContact(material,mass,current,inst_fac);
        result=(String)ac.getResult();
        param.setValue(new String(result));

        param=wizard.getParameter("Storage");
        ActivateStorage as=new ActivateStorage(material,current,inst_fac);
        result=(String)as.getResult();
        param.setValue(new String(result));

        param=wizard.getParameter("Prompt");
        ActivatePrompt ap=new ActivatePrompt(material,current,inst_fac);
        result=(String)ap.getResult();
        param.setValue(new String(result));

        return true;
    } 
    
}
