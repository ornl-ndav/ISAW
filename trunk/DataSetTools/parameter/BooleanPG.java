/*
 * File:  BooleanPG.java 
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
 *  Revision 1.3  2003/06/06 18:54:39  pfpeterson
 *  Implements ParamUsesString.
 *
 *  Revision 1.2  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.1  2003/02/05 16:31:45  pfpeterson
 *  Added to CVS.
 *
 */

package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.Float;
import java.util.Vector;
import javax.swing.*;


/**
 * This is class is to deal with boolean parameters.
 */
public class BooleanPG extends ParameterGUI 
                                    implements ActionListener, ParamUsesString{
  private static final String TYPE="Boolean";

  // ********** Constructors **********
  public BooleanPG(String name, Object value){
    this(name,value,false);
    this.setDrawValid(false);
    this.type=TYPE;
  }
    
  public BooleanPG(String name, Object value, boolean valid){
    this.setName(name);
    this.setValue(value);
    this.setEnabled(true);
    this.setValid(valid);
    this.setDrawValid(true);
    this.type=TYPE;
    this.initialized=false;
    this.ignore_prop_change=false;
  }
    
  public BooleanPG(String name, boolean value){
    this(name, new Boolean(value));
  }

  public BooleanPG(String name, boolean value, boolean valid){
    this(name, new Boolean(value),valid);
  }

  /**
   * Override the default method.
   */
  public Object getValue(){
    Object val=null;
    if(this.initialized){
      val=new Boolean(((JCheckBox)this.entrywidget).isSelected());
    }else{
      val=this.value;
    }
    if( !(val instanceof Boolean) ) val=null;

    return val;
  }

  /**
   * Convenience method to get the proper type value right away.
   */
  public boolean getbooleanValue(){
    return ((Boolean)this.getValue()).booleanValue();
  }

  /**
   * Overrides the default version of setValue to properly deal with
   * booleans.
   */
  public void setValue(Object value){
    Boolean booval=null;
    
    if(value==null){
      booval=Boolean.FALSE;
    }else if(value instanceof Boolean){
      booval=(Boolean)value;
    }else if(value instanceof String){
      this.setStringValue((String)value);
    }else{
      throw new ClassCastException("Could not coerce "
                                +value.getClass().getName()+" into a Boolean");
    }

    if(this.initialized){
        boolean newval=booval.booleanValue();
        boolean oldval=((JCheckBox)this.entrywidget).isSelected();
        if(newval!=oldval)
          ((JCheckBox)this.entrywidget).doClick(); //setSelected(newval);
    }else{
      this.value=booval;
    }
  }

  /**
   * Convenience method to set the proper type value right away.
   */
  public void setbooleanValue(boolean value){
    this.setValue(new Boolean(value));
  }
    
  // ********** ParamUsesString requirements **********
  /**
   * Returns "TRUE" or "FALSE".
   */
  public String getStringValue(){
    return this.getValue().toString().toUpperCase();
  }

  /**
   * Sets the value by parsing the String using {@link
   * java.lang.Boolean#Boolean(String) Boolean}
   */
  public void setStringValue(String val){
    Boolean BooVal=new Boolean(val.trim());
    this.setValue(BooVal);
  }

  // ********** IParameterGUI requirements **********
  /**
   * Allows for initialization of the GUI after instantiation.
   */
  public void init(Vector init_values){
    if(this.initialized) return; // don't initialize more than once

    if(init_values!=null){
      if(init_values.size()==1){
        // the init_values is what to set as the value of the parameter
        this.setValue(init_values.elementAt(0));
      }else{
        // something is not right, should throw an exception
      }
    }
    entrywidget=new JCheckBox("",((Boolean)this.getValue()).booleanValue());
    ((JCheckBox)entrywidget).addActionListener(this);
    this.setEnabled(this.getEnabled());
    super.initGUI();
    this.initialized=true;
  }

  /**
   * Set the enabled state of the JCheckBox.
   */
  public void setEnabled(boolean enabled){
    this.enabled=enabled;
    if(this.getEntryWidget()!=null){
      ((JCheckBox)this.entrywidget).setEnabled(this.enabled);
    }
  }

  static void main(String args[]){
    BooleanPG fpg;

    fpg=new BooleanPG("a",new Boolean(false));
    System.out.println(fpg);
    fpg.init();
    fpg.showGUIPanel();

    fpg=new BooleanPG("b",new Boolean(true));
    System.out.println(fpg);
    fpg.setEnabled(false);
    fpg.init();
    fpg.showGUIPanel();

    fpg=new BooleanPG("c",new Boolean(true),false);
    System.out.println(fpg);
    fpg.setEnabled(false);
    fpg.init();
    fpg.showGUIPanel();

    fpg=new BooleanPG("d",new Boolean(false),true);
    System.out.println(fpg);
    fpg.setDrawValid(true);
    fpg.init();
    fpg.showGUIPanel();

  }

  /**
   * Definition of the clone method.
   */
  public Object clone(){
    BooleanPG pg=new BooleanPG(this.name,this.value,this.valid);
    pg.setDrawValid(this.getDrawValid());
    pg.initialized=false;
    return pg;
  }

  /**
   * Deal with the state changing. This sets valid to false.
   */
  public void actionPerformed(ActionEvent e){
    if(e.paramString().indexOf("ACTION_PERFORMED")==0)
      this.setValid(false);
  }
}
