/*
 * File:  MaterialPG.java 
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
 *  Revision 1.14  2003/11/19 04:13:22  bouzekc
 *  Is now a JavaBean.
 *
 *  Revision 1.13  2003/11/05 04:36:09  bouzekc
 *  Code cleanup in validateSelf().
 *
 *  Revision 1.12  2003/10/11 19:24:33  bouzekc
 *  Removed declaration of "ParamUsesString" as the superclass declares it
 *  already.  Removed clone() definition as the superclass implements it
 *  using reflection.
 *
 *  Revision 1.10  2003/10/07 18:38:51  bouzekc
 *  Removed declaration of "implements ParamUsesString" as the
 *  StringEntryPG superclass now declares it.
 *
 *  Revision 1.9  2003/09/09 23:06:29  bouzekc
 *  Implemented validateSelf().
 *
 *  Revision 1.8  2003/08/15 23:50:05  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.7  2003/06/06 18:51:47  pfpeterson
 *  Removed unneeded code due to new abstract grandparent.
 *
 *  Revision 1.6  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.5  2003/02/04 20:32:52  pfpeterson
 *  Now uses the MaterialFilter in the GUI.
 *
 *  Revision 1.4  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/10/07 15:27:43  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.2  2002/09/30 15:20:53  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/06/06 16:14:35  pfpeterson
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
 * This is class is to deal with float parameters.
 */
public class MaterialPG extends StringPG {
    private static final String TYPE="Material";

    // ********** Constructors **********
    public MaterialPG(String name, Object value){
        super(name,value);
        FILTER=new MaterialFilter();
        this.setType(TYPE);
    }
    
    public MaterialPG(String name, Object value, boolean valid){
        super(name,value,valid);
        FILTER=new MaterialFilter();
        this.setType(TYPE);
    }

    /*
     * Testbed.
     */
    /*public static void main(String args[]){
        MaterialPG fpg;

        fpg=new MaterialPG("a","1f");
        System.out.println(fpg);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new MaterialPG("b","10f");
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new MaterialPG("c","100f",false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(null);
        fpg.showGUIPanel();

        fpg=new MaterialPG("d","1000f",true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.initGUI(null);
        fpg.showGUIPanel();
    }*/

    /**
     * Validates this MaterialPG.  A valid MaterialPG is one where getValue()
     * will pass through a MaterialFilter.
     */
    public void validateSelf(  ) {
      StringFilterer sf = getStringFilter(  );

      if( sf == null ) {
        setValid( false );
      } else {
        setValid( sf.isOkay( 0, getValue(  ).toString(  ), "" ) );
      }
    }

}
