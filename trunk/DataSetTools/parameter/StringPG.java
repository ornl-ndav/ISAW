/*
 * File:  StringPG.java 
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
 *  $Log$
 *  Revision 1.11  2003/10/08 22:39:09  dennis
 *  Reverting to previous version, that was in ISAW 1.5.1 beta 8.
 *  The most recent checkin (10/07/03) also removed the clone() method
 *  and was not consistent with the version in CVS.  ISAW crashed on
 *  startup with null pointer exception.
 *
 *  Revision 1.9  2003/09/09 23:06:31  bouzekc
 *  Implemented validateSelf().
 *
 *  Revision 1.8  2003/08/15 23:50:06  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.7  2003/06/06 18:50:59  pfpeterson
 *  Now extends StringEntryPG and implements ParamUsesString.
 *
 *  Revision 1.6  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.5  2002/11/27 23:22:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/09/19 16:07:25  pfpeterson
 *  Changed to work with new system where operators get IParameters in stead of Parameters. Now support clone method.
 *
 *  Revision 1.3  2002/07/15 21:27:33  pfpeterson
 *  Factored out parts of the GUI.
 *
 *  Revision 1.2  2002/06/14 14:21:14  pfpeterson
 *  Added more checks to setValue() and getValue().
 *
 *  Revision 1.1  2002/06/06 16:14:37  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import javax.swing.*;
import java.util.Vector;
import java.lang.String;
import java.beans.*;
import DataSetTools.components.ParametersGUI.*;

/**
 * This is a superclass to take care of many of the common details of
 * StringPGs.
 */
public class StringPG extends StringEntryPG implements ParamUsesString{
    private   static final String TYPE     = "String";

    // ********** Constructors **********
    public StringPG(String name, Object value){
        super(name,value);
        this.type=TYPE;
    }

    public StringPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.type=TYPE;
    }

    // ********** IParameter requirements **********
    /**
     * Sets the value of the parameter.
     */
    public void setValue(Object value){
      String svalue=null;
        
      if(value==null)
        svalue=null;
      else if(value instanceof String)
        svalue=(String)value;
      else
        svalue=value.toString();

      if(this.initialized)
        super.setEntryValue(svalue);
      else
        this.value=svalue;
      this.setValid(true);
    }

    // ********** ParamUsesString requirements **********
    public String getStringValue(){
        Object ob=this.getValue();

        String svalue=null;

        if(ob==null)
            svalue=null;
        else if(ob instanceof String)
            svalue=(String)ob;
        else
            svalue=ob.toString();

        if(svalue==null || svalue.length()<=0)
          return null;
        else
          return svalue;
    }

    public void setStringValue(String val){
      this.setValue(val);
    }

    /*
     * Testbed.
     */
    /*public static void main(String args[]){
        StringPG fpg;

        fpg=new StringPG("a","1f");
        System.out.println(fpg);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new StringPG("b","10f");
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new StringPG("c","1000f",false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new StringPG("d","100f",true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.initGUI(null);
        fpg.showGUIPanel();
    }*/

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        StringPG spg=new StringPG(this.name,this.value,this.valid);
        spg.setDrawValid(this.getDrawValid());
        spg.initialized=false;
        return spg;
    }

    /**
     * Validates this StringPG.  A StringPG is considered valid if its
     * getValue() does not return null.
     */
    public void validateSelf(  ) {
      setValid( ( getValue(  ) != null ) );
    }
}
