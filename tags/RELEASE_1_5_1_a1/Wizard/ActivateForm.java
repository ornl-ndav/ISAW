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
 * Revision 1.5  2003/04/02 15:02:46  pfpeterson
 * Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 *
 * Revision 1.4  2003/02/26 17:21:28  rmikk
 * Now writes to DataSetTools.util.SharedData.status_pane
 *
 * Revision 1.3  2002/11/27 23:31:16  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/06/06 16:21:33  pfpeterson
 * Now use new parameters.
 *
 * Revision 1.1  2002/05/28 20:35:08  pfpeterson
 * Moved files
 *
 *
 */

package Wizard;

import java.io.*;
import DataSetTools.wizard.*;
import DataSetTools.parameter.*;
import DataSetTools.util.ErrorString;
import DataSetTools.util.SharedData;
import Operators.Calculator.*;

/**
 *  This form calculates the contact dose, prompt activation, 
 *  and storage time for the specified sample.
 *  The instrument factors are:
    "IPNS CHEX     ",
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
   *  Construct an ActivateForm.  This constructor also
   *  calls setDefaultParameters in order to set the permission
   *  type of the parameters.
   *  
   */
  public ActivateForm( ){
      super("Calculate Activation of Sample");
  }
    
  /**
   *
   *  Attempts to set reasonable default parameters for this form.
   *  Included in this is the setting of the monitor DataSet list
   *  corresponding to the respective runfiles, as well as the 
   *  corresponding type of the parameter (editable, result, or
   *  constant).
   */
  public void setDefaultParameters()
  {
    addParameter(new MaterialPG("Sample composition", "La,Mn,O_3", false));
    addParameter(new FloatPG("Sample mass", 1.0f, false));
    addParameter(new FloatPG("Beam Current (in microAmp)", 16.0f, false));
    addParameter(new FloatPG("Instrument Factor (LANSCE HIPD=1.0)",
                             1.0f, false));
    addParameter(new StringPG("Contact Dose", "", false));
    addParameter(new StringPG( "Storage Time", "", false));
    addParameter(new StringPG( "Prompt Activation", "", false));
    setParamTypes(null,new int[]{0,1,2,3},new int[]{4,5,6});
  }

  /**
   *  Returns the String command used for invoking this
   *  Form in a Script.
   */
  public String getCommand()
  {
    return "ACTIVATEFORM";
  }

  /**
   *  Loads the specifed runfile's DataSets into an ArrayPG.  Each
   *  runfile's DataSet array occupies a space in the ArrayPG's
   *  Vector. 
   *
   *  @return A Boolean indicating success or failure.
   */
    public Object getResult(){
        IParameterGUI param;
        Object result;

        param=(IParameterGUI)super.getParameter(0);
        String material=(String)param.getValue();
        param.setValid(true);

        param=(IParameterGUI)super.getParameter(1);
        float mass=((FloatPG)param).getfloatValue();
        if(mass<=0f){
            param.setValid(false);
            DataSetTools.util.SharedData.addmsg("Invalid Mass Specified: "+mass);
            return new Boolean(false);
        }else{
            param.setValid(true);
        }

        param=(IParameterGUI)super.getParameter(2);
        float current=((FloatPG)param).getfloatValue();
        if(current<=0f){
            param.setValid(false);
            DataSetTools.util.SharedData.addmsg("Invalid Current Specified: "
                                         +current);
            return new Boolean(false);
        }else{
            param.setValid(true);
        }

        param=(IParameterGUI)super.getParameter(3);
        float inst_fac=((FloatPG)param).getfloatValue();
        if(inst_fac<=0f){
            param.setValid(false);
            DataSetTools.util.SharedData.addmsg("Invalid Instrument Factor "
                                         +"Specified: "+inst_fac);
            return new Boolean(false);
        }else{
            param.setValid(true);
        }

        param=(IParameterGUI)super.getParameter(4);
        ActivateContact ac=new ActivateContact(material,mass);
        result=ac.getResult();
        if( result instanceof ErrorString ){
          param.setValue(null);
          SharedData.addmsg(result);
          return Boolean.FALSE;
        }
        param.setValue(new String(result.toString()));

        param=(IParameterGUI)super.getParameter(5);
        ActivateStorage as=new ActivateStorage(material,current,inst_fac);
        result=as.getResult();
        if( result instanceof ErrorString ){
          param.setValue(null);
          SharedData.addmsg(result);
          return Boolean.FALSE;
        }
        param.setValue(result.toString());

        param=(IParameterGUI)super.getParameter(6);
        ActivatePrompt ap=new ActivatePrompt(material);
        result=ap.getResult();
        if( result instanceof ErrorString ){
          param.setValue(null);
          SharedData.addmsg(result);
          return Boolean.FALSE;
        }
        param.setValue(result);

        return Boolean.TRUE;
    } 
    
}
