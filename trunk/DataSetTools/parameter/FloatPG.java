/*
 * File:  FloatPG.java 
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
 *  Revision 1.13  2003/10/08 22:39:09  dennis
 *  Reverting to previous version, that was in ISAW 1.5.1 beta 8.
 *  The most recent checkin (10/07/03) also removed the clone() method
 *  and was not consistent with the version in CVS.  ISAW crashed on
 *  startup with null pointer exception.
 *
 *  Revision 1.11  2003/09/13 23:29:46  bouzekc
 *  Moved calls from setValid(true) to validateSelf().
 *
 *  Revision 1.10  2003/08/15 23:50:04  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.9  2003/07/23 21:54:31  rmikk
 *  Fixed an error that occurred but should not have happened
 *
 *  Revision 1.8  2003/06/12 18:58:27  bouzekc
 *  Fixed bug with setting value.
 *
 *  Revision 1.7  2003/06/06 18:50:59  pfpeterson
 *  Now extends StringEntryPG and implements ParamUsesString.
 *
 *  Revision 1.6  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.5  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/10/07 15:27:37  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.3  2002/09/30 15:20:47  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.2  2002/06/12 14:20:00  pfpeterson
 *  Added two convenience constructors to create the parameter
 *  with a float.
 *
 *  Revision 1.1  2002/06/06 16:14:30  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import javax.swing.*;
import java.util.Vector;
import java.lang.Float;
import java.beans.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;

/**
 * This is class is to deal with float parameters.
 */
public class FloatPG extends StringEntryPG implements ParamUsesString{
    protected static final String         TYPE   = "Float";

    // ********** Constructors **********
    public FloatPG(String name, Object value){
        super(name,value);
        this.FILTER=new FloatFilter();
        this.type=TYPE;
    }
    
    public FloatPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.FILTER=new FloatFilter();
        this.type=TYPE;
    }
    
    public FloatPG(String name, float value){
        this(name, new Float(value));
    }

    public FloatPG(String name, float value, boolean valid){
        this(name, new Float(value),valid);
    }

    /**
     * Override the default method.
     */
    public Object getValue() throws NumberFormatException{
        Object val=super.getValue();

        if(val instanceof Float)
          return val;
        else if( val instanceof Number){
          return new Float( ((Number)val).floatValue() );
           
        }
        else if(val instanceof String)
          return new Float((String)val);
        else
          throw new ClassCastException("Could not coerce "
                                    +val.getClass().getName()+" into a Float");
    }

    /**
     * Convenience method to get the proper type value right away.
     */
    public float getfloatValue(){
        return ((Float)this.getValue()).floatValue();
    }

    /**
     * Overrides the default version of setValue to properly deal with
     * floats.
     */
    public void setValue(Object value){
      Float floatval=null;

      if(value==null){
        floatval=new Float(Float.NaN);
      }else if(value instanceof Float){
        floatval=(Float)value;
      }else if(value instanceof Double){
        floatval=new Float(((Double)value).doubleValue());
      }else if(value instanceof Integer){
        floatval=new Float(((Integer)value).intValue());
      }else if(value instanceof String){
        this.setStringValue((String)value);
        return;
      }else{
        throw new ClassCastException("Could not coerce "
                                  +value.getClass().getName()+" into a Float");
      }

      if(this.initialized){
        super.setEntryValue(value);
      }else{
        this.value=value;
      }
      validateSelf();
    }

    /**
     * Convenience method to set the proper type value right away.
     */
    public void setfloatValue(float value){
        this.setValue(new Float(value));
    }
    
    // ********** ParamUsesString requirements **********
    public String getStringValue(){
      Object val=this.getValue();

      if(val instanceof String)
        return (String)val;
      else
        return val.toString();
    }

    public void setStringValue(String val) throws NumberFormatException{
      if(initialized)
        super.setEntryValue(val);
      else
        this.setValue(new Float(val.trim()));
    }

    /*
     * Testbed.
     */
    /*public static void main(String args[]){
        FloatPG fpg;

        fpg=new FloatPG("a",new Float(1f));
        System.out.println(fpg);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new FloatPG("b",new Float(10f));
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new FloatPG("c",new Float(1000f),false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new FloatPG("d",new Float(100f),true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.initGUI(null);
        fpg.showGUIPanel();
    }*/

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        FloatPG pg=new FloatPG(this.name,this.value,this.valid);
        pg.setDrawValid(this.getDrawValid());
        pg.initialized=false;
        return pg;
    }
}
