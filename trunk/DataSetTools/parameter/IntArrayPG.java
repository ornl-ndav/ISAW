/*
 * File:  IntArrayPG.java 
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
 *  Revision 1.11  2003/10/07 18:38:51  bouzekc
 *  Removed declaration of "implements ParamUsesString" as the
 *  StringEntryPG superclass now declares it.
 *
 *  Revision 1.10  2003/09/13 23:29:47  bouzekc
 *  Moved calls from setValid(true) to validateSelf().
 *
 *  Revision 1.9  2003/08/15 23:50:05  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.8  2003/06/06 18:53:37  pfpeterson
 *  Now extends StringEntryPG and implements ParamUsesString.
 *
 *  Revision 1.7  2003/04/14 20:57:14  pfpeterson
 *  Added method to get value as int[].
 *
 *  Revision 1.6  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.5  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/10/23 19:03:03  pfpeterson
 *  Fixed a bug where the parameter did not work with a null value.
 *
 *  Revision 1.3  2002/10/07 15:27:40  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.2  2002/09/30 15:20:50  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/06/06 16:14:33  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import java.util.Vector;
import java.beans.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;

/**
 * This is class is to deal with Integer Arrays. Its value is stored
 * as a String.
 */
public class IntArrayPG extends StringEntryPG {
    private static final String TYPE="IntArray";

    // ********** Constructors **********
    public IntArrayPG(String name, Object value){
        super(name,value);
        this.type=TYPE;
        FILTER=new IntArrayFilter();
    }
    
    public IntArrayPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.type=TYPE;
        FILTER=new IntArrayFilter();
    }

    public void setValue(Object value){
      if(this.initialized)
        super.setEntryValue(value);
      else
        this.value=value;
    }

    public int[] getArrayValue(){
      String svalue=(String)getValue();
      if(svalue==null || svalue.length()<=0)
        return null;
      else
        return IntList.ToArray(svalue);
    }

    // ********** ParamUsesString requirements **********
    public String getStringValue(){
      return (String)this.getValue();
    }

    public void setStringValue(String val){
      this.setValue(val);
    }

    /*
     * Testbed.
     */
    /*public static void main(String args[]){
        IntArrayPG fpg;

        fpg=new IntArrayPG("a","0:1");
        System.out.println(fpg);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new IntArrayPG("b","0:2");
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new IntArrayPG("c","0:3",false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new IntArrayPG("d","0:4",true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.initGUI(null);
        fpg.showGUIPanel();
    }*/
}
