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
 *  Revision 1.18  2003/12/15 01:56:37  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.17  2003/11/19 04:13:22  bouzekc
 *  Is now a JavaBean.
 *
 *  Revision 1.16  2003/10/20 22:27:44  bouzekc
 *  Now sets the internal value when a String is encountered during setValue.
 *
 *  Revision 1.15  2003/10/20 16:22:44  rmikk
 *  Used floatval for value in setValue
 *  Used super.getValue() to get initial value in getValue.  This checks the
 *     getEntryWidget()'s value and the value variable
 *
 *  Revision 1.14  2003/10/11 19:27:14  bouzekc
 *  Removed declaration of ParamUsesString as the superclass declares this
 *  already.  Removed clone() definition as the superclass implements this
 *  using reflection.  getValue() now returns a new Float(0.0) if the internal
 *  value is null.
 *
 *  Revision 1.12  2003/10/07 18:38:51  bouzekc
 *  Removed declaration of "implements ParamUsesString" as the
 *  StringEntryPG superclass now declares it.
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
import DataSetTools.util.FloatFilter;

/**
 * This is class is to deal with float parameters.
 */
public class FloatPG extends StringEntryPG {
    protected static final String         TYPE   = "Float";

    // ********** Constructors **********
    public FloatPG(String name, Object value){
        super(name,value);
        this.FILTER=new FloatFilter();
        this.setType(TYPE);
    }
    
    public FloatPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.FILTER=new FloatFilter();
        this.setType(TYPE);
    }
    
    public FloatPG(String name, float value){
        this(name, new Float(value));
    }

    public FloatPG(String name, float value, boolean valid){
        this(name, new Float(value),valid);
    }

    /**
     * Override the default method.  Note that if the internal value is null,
     * this will return 0.0.
     *
     * @return The value of this FloatPG.
     */
    public Object getValue() throws NumberFormatException{
        Object val = super.getValue();
       
        if( val == null)
          return new Float(0.0f);
        else if(val instanceof Float)
          return val;
        else if( val instanceof Number){
          return new Float( ((Number)val).floatValue() );
           
        }
        else if(val instanceof String)
          return new Float((String)val);
        else
          return new Float( 0.0f );
    }

    /**
     * Convenience method to get the proper type value right away.
     */
    public float getfloatValue(){
        return ((Float)this.getValue()).floatValue();
    }

    /**
     * Overrides the default version of setValue to properly deal with
     * floats.  This will be 0.0 if no other valid value is set.
     */
    public void setValue(Object val){
      Float floatval=null;
      if(val==null){
        floatval=new Float(Float.NaN);
      }else if(val instanceof Float){
        floatval=(Float)val;
      }else if(val instanceof Double){
        floatval=new Float(((Double)val).doubleValue());
      }else if(val instanceof Integer){
        floatval=new Float(((Integer)val).intValue());
      }else if(val instanceof String){
        this.setStringValue((String)val);
        return;
      }else{
        throw new ClassCastException("Could not coerce "
                                  +val.getClass().getName()+" into a Float");
      }

      if(this.getInitialized()){
        super.setEntryValue((floatval));
      }
      super.setValue( floatval );
     
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
      if(getInitialized()) {
        super.setEntryValue(val);
      }
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
}
