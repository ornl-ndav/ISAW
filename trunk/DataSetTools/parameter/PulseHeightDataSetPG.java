/*
 * File:  PulseHeightDataSetPG.java 
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
 *  Revision 1.5  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/10/10 22:11:53  pfpeterson
 *  Fixed a bug with the clone method not getting the choices copied over.
 *
 *  Revision 1.3  2002/10/07 15:27:45  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.2  2002/09/30 15:20:55  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/08/01 18:40:06  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;

import DataSetTools.dataset.*;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.retriever.NexusRetriever;
import DataSetTools.util.SharedData;
import java.util.Vector;

/**
 * This is a superclass to take care of many of the common details of
 * Array Parameter GUIs.
 */
public class PulseHeightDataSetPG extends DataSetPG{
    // static variables
    private   static String TYPE     = "PulseHeightDataSet";
    protected static int    DEF_COLS = DataSetPG.DEF_COLS;

    // ********** Constructors **********
    public PulseHeightDataSetPG(String name, Object value){
        super(name,value);
        this.type=TYPE;
        if(!isPulseHeightDataSet(value)){
                SharedData.addmsg("WARN: Non-"+this.type
                                  +" in PulseHeightDataSetPG constructor");
        }
    }

    public PulseHeightDataSetPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.type=TYPE;
        if(!isPulseHeightDataSet(value)){
                SharedData.addmsg("WARN: Non-"+this.type
                                  +" in PulseHeightDataSetPG constructor");
        }
    }

    // ********** Methods to deal with the hash **********

    /**
     * Add a single DataSet to the vector of choices. This calls the
     * superclass's method once it confirms the value to be added is a
     * DataSet.
     */
    public void addItem( Object val){
        if(isPulseHeightDataSet(val)){
             super.addItem(val);
        }
    }

    /**
     * Checks that the given object is a DataSet with a SAMPLE_DATA as
     * its DS_TYPE Attribute.
     */
    private static boolean isPulseHeightDataSet( Object ds ){
        if(ds instanceof DataSet){
            String type = (String)
                ((DataSet)ds).getAttributeValue(Attribute.DS_TYPE);
            if(type.equals(Attribute.PULSE_HEIGHT_DATA)){
                return true;
            }
        }
        return false;
    }

    /**
     * Main method for testing purposes.
     */
    static void main(String args[]){
        PulseHeightDataSetPG fpg;
        int y=0, dy=70;

        // set up what files to read data from
        String runfile=null;
        String nexusfile=null;
        if(args.length>0){
            for( int i=0 ; i<args.length ; i++ ){
                if(args[i].indexOf("nx")>0){
                    nexusfile=args[i];
                }else if(args[i].indexOf(".run")>0){
                    runfile=args[i];
                }else if(args[i].indexOf(".RUN")>0){
                    runfile=args[i];
                }
                    
            }
        }
        if(runfile==null){
            runfile="/IPNShome/pfpeterson/data/CsC60/SEPD18805.RUN";
        }
        if(nexusfile==null){
            nexusfile="/IPNShome/pfpeterson/data/nexus/nexus_all.nxs";
        }

        // read in the data into the arrays
        RunfileRetriever rr = new RunfileRetriever(runfile);
        NexusRetriever   nr = new NexusRetriever(nexusfile);
        DataSet[] ds=new DataSet[rr.numDataSets()+nr.numDataSets()];
        if(rr!=null){
            for( int i=0 ; i<rr.numDataSets() ; i++ ){
                ds[i]=rr.getDataSet(i);
            }
        }
        if(nr!=null){
            for( int i=0 ; i<nr.numDataSets() ; i++ ){
                ds[i+rr.numDataSets()]=nr.getDataSet(i);
            }
        }

        // now actually test things
        fpg=new PulseHeightDataSetPG("a",ds[0]);
        System.out.println(fpg);
        fpg.init(ds);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new PulseHeightDataSetPG("b",ds[0]);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init(ds);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new PulseHeightDataSetPG("c",ds[0],false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init(ds);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new PulseHeightDataSetPG("d",ds[0],true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init(ds);
        fpg.showGUIPanel(0,y);
        y+=dy;

    }

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        PulseHeightDataSetPG pg=
            new PulseHeightDataSetPG(this.name,this.value,this.valid);
        pg.vals=(Vector)this.vals.clone();
        pg.setDrawValid(this.getDrawValid());
        pg.initialized=false;
        return pg;
    }
}
