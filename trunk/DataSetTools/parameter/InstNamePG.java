/*
 * File:  InstNamePG.java 
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
 *  Revision 1.7  2003/02/07 16:19:17  pfpeterson
 *  Fixed bug in constructor where the value of 'valid' was not properly set.
 *
 *  Revision 1.6  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.5  2002/10/07 15:27:39  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.4  2002/09/30 15:20:49  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.3  2002/06/14 15:56:41  pfpeterson
 *  Use the ShareData.getProperty() method to get default
 *  instrument name.
 *
 *  Revision 1.2  2002/06/14 14:24:49  pfpeterson
 *  Uses appropriate default value if an empty or null string is
 *  given to the constructor.
 *
 *  Revision 1.1  2002/06/06 16:14:32  pfpeterson
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
public class InstNamePG extends StringPG{
    private static String TYPE="InstName";
    private static String propName="Default_Instrument";
    private static final SharedData share=new SharedData();

    // ********** Constructors **********
    public InstNamePG(String name, Object value){
        super(name,value);
        this.type=TYPE;
        if( value==null ){
            this.setValue(share.isaw_props.get(propName));
        }else{
            String temp=this.getStringValue();
            if(temp==null || temp.length()==0){
                this.setValue(share.getProperty(propName));
            }
        }
    }
    
    public InstNamePG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.type=TYPE;
        if( value==null ){
            this.setValue(share.isaw_props.get(propName));
        }else{
            String temp=this.getStringValue();
            if(temp==null || temp.length()==0){
                this.setValue(share.getProperty(propName));
            }
        }
        this.setValid(valid);
    }

    static void main(String args[]){
        InstNamePG fpg;

        fpg=new InstNamePG("a","1f");
        System.out.println(fpg);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new InstNamePG("b","10f");
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new InstNamePG("c","100f",false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel();

        fpg=new InstNamePG("d","1000f",true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init();
        fpg.showGUIPanel();

    }

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        InstNamePG pg=new InstNamePG(this.name,this.value,this.valid);
        pg.setDrawValid(this.getDrawValid());
        pg.initialized=false;
        return pg;
    }
}
