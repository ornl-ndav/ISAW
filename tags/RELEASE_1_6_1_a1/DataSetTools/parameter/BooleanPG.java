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
 *  Revision 1.18  2003/12/15 01:45:30  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.17  2003/11/19 04:13:21  bouzekc
 *  Is now a JavaBean.
 *
 *  Revision 1.16  2003/11/04 15:56:41  rmikk
 *  Took control of PropertyChange listeners and events
 *  Created a new clone method
 *
 *  Revision 1.15  2003/10/11 18:57:56  bouzekc
 *  Removed clone() as ParameterGUI now implements it.
 *
 *  Revision 1.14  2003/09/16 22:46:52  bouzekc
 *  Removed addition of this as a PropertyChangeListener.  This is already done
 *  in ParameterGUI.  This should fix the excessive events being fired.
 *
 *  Revision 1.13  2003/09/13 23:29:46  bouzekc
 *  Moved calls from setValid(true) to validateSelf().
 *
 *  Revision 1.12  2003/09/13 23:16:39  bouzekc
 *  Removed calls to setEnabled in initGUI(Vector), since ParameterGUI.init()
 *  already calls this.
 *
 *  Revision 1.11  2003/09/09 23:06:27  bouzekc
 *  Implemented validateSelf().
 *
 *  Revision 1.10  2003/08/28 02:28:09  bouzekc
 *  Removed setEnabled() method.
 *
 *  Revision 1.9  2003/08/28 01:43:55  bouzekc
 *  Modified to work with new ParameterGUI.
 *
 *  Revision 1.8  2003/08/22 20:12:08  bouzekc
 *  Modified to work with EntryWidget.
 *
 *  Revision 1.7  2003/08/16 02:23:37  bouzekc
 *  BooleanPG now has PropertyChangeListener support.
 *
 *  Revision 1.6  2003/08/15 23:50:04  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.5  2003/08/15 03:52:32  bouzekc
 *  Removed unnecessary initialization=true statement.
 *
 *  Revision 1.4  2003/06/11 22:17:49  pfpeterson
 *  Fixed bug with setting value. Added functionality to deal with integers
 *  in setValue.
 *
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
import java.util.Vector;
import javax.swing.*;


/**
 * This is class is to deal with boolean parameters.
 */
public class BooleanPG extends ParameterGUI 
                                    implements ParamUsesString{
  private static final String TYPE="Boolean";
  // ********** Constructors **********
  public BooleanPG(String name, Object value){
    super( name, value );
    this.setType(TYPE);
  }
    
  public BooleanPG(String name, Object value, boolean valid){
    super( name, value, valid );
    this.setType(TYPE);
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
    Object val=super.getValue();
    
    //update if a GUI exists
    if(this.getInitialized()){
      JCheckBox wijit = ( JCheckBox ) getEntryWidget().getComponent( 0 );
      val = new Boolean( wijit.isSelected(  ) );
    }

    if( !(val instanceof Boolean) ) val = null;

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
   *
   * For integers, zero is false, everything else is true.
   */
  public void setValue(Object val){
    Boolean booval=null;
    
    if(val==null){
      booval=Boolean.FALSE;
    }else if(val instanceof Boolean){
      booval=(Boolean)val;
    }else if(val instanceof String){
      this.setStringValue((String)val);
      return;
    }else if(val instanceof Integer){
      int intval=((Integer)val).intValue();
      if(intval==0)
        booval=Boolean.FALSE;
      else
        booval=Boolean.TRUE;
    }else{
      throw new ClassCastException("Could not coerce "
                                +val.getClass().getName()+" into a Boolean");
    }

    //update the visual checkbox
    if(this.getInitialized()){
        boolean newval=booval.booleanValue();
        boolean oldval = 
          ( ( JCheckBox )( getEntryWidget().getComponent( 0 ) ) ).isSelected(  );
        if(newval!=oldval)
          ( ( JCheckBox )( getEntryWidget().getComponent( 0 ) ) ).doClick(  ); 
    }
    //always update the internal value
    super.setValue(booval);
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
  public void initGUI(Vector init_values){
    if(this.getInitialized()) return; // don't initialize more than once

    if(init_values!=null){
      if(init_values.size()==1){
        // the init_values is what to set as the value of the parameter
        this.setValue(init_values.elementAt(0));
      }else{
        // something is not right, should throw an exception
      }
    }
    setEntryWidget(new EntryWidget( 
                new JCheckBox("", ((Boolean)this.getValue()).booleanValue()))); 
    
    super.initGUI();
  }

  /*
   * Testbed.
   */
  public static void main(String args[]){
    BooleanPG fpg;

    fpg=new BooleanPG("a",new Boolean(false));
    System.out.println(fpg);
    fpg.initGUI(null);
    fpg.showGUIPanel();

    fpg=new BooleanPG("b",new Boolean(true));
    System.out.println(fpg);
    fpg.setEnabled(false);
    fpg.initGUI(null);
    fpg.showGUIPanel();

    fpg=new BooleanPG("c",new Boolean(true),false);
    System.out.println(fpg);
    fpg.setEnabled(false);
    fpg.initGUI(null);
    fpg.showGUIPanel();

    fpg=new BooleanPG("d",new Boolean(true),true);
    System.out.println(fpg);
    fpg.setDrawValid(true);
    fpg.initGUI(null);
    fpg.showGUIPanel();
  }

  /**
   * Validates this BooleanPG.  In general, if getValue() does not return a
   * null or a non-Boolean, this BooleanPG is considered valid.
   */
  public void validateSelf(  ) {
    Object val = getValue(  );
    if( val != null && val instanceof Boolean) {
      setValid( true );
    } else {
      setValid( false );
    }
  }
}
