/*
 * File:  DataSetPG.java 
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
 *  Revision 1.10  2003/08/22 20:12:07  bouzekc
 *  Modified to work with EntryWidget.
 *
 *  Revision 1.9  2003/08/15 23:50:04  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.8  2003/03/26 23:19:37  pfpeterson
 *  Implements IObserver so can drop references to DESTROYed DataSets.
 *  Also improved error checking in addItem and setValue.
 *
 *  Revision 1.7  2003/03/25 19:40:47  pfpeterson
 *  Sets value to EMPTY_DATA_SET when attempt is made to set value to null.
 *
 *  Revision 1.6  2003/02/24 20:59:14  pfpeterson
 *  Now extends ChooserPG rather than ArrayPG.
 *
 *  Revision 1.5  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.4  2002/10/10 22:11:50  pfpeterson
 *  Fixed a bug with the clone method not getting the choices copied over.
 *
 *  Revision 1.3  2002/10/07 15:27:36  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.2  2002/09/30 15:20:46  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/08/01 18:40:03  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;

import DataSetTools.components.ParametersGUI.HashEntry;
import DataSetTools.dataset.*;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.util.SharedData;
import DataSetTools.util.IObserver;
import java.util.Vector;

/**
 * This is a superclass to take care of many of the common details of
 * Array Parameter GUIs.
 */
public class DataSetPG extends ChooserPG implements IObserver{
    // static variables
    private   static String TYPE     = "DataSet";
    protected static int    DEF_COLS = ChooserPG.DEF_COLS;

    // ********** Constructors **********
    public DataSetPG(String name, Object value){
        super(name,value);
        this.type=TYPE;
        if(value==null || value==DataSet.EMPTY_DATA_SET) return;
        if(!(value instanceof DataSet))
            SharedData.addmsg("WARN: Non-"+this.type
                              +" in DataSetPG constructor");
    }

    public DataSetPG(String name, Object value, boolean valid){
        super(name,value,valid);
        this.type=TYPE;
        if(value==null || value==DataSet.EMPTY_DATA_SET) return;
        if(!(value instanceof DataSet))
            SharedData.addmsg("WARN: Non-"+this.type
                              +" in DataSetPG constructor");
    }

    // ********** Methods to deal with the hash **********

    /**
     * Add a single DataSet to the vector of choices. This calls the
     * superclass's method once it confirms the value to be added is a
     * DataSet.
     */
    public void addItem( Object val){
      if(val==null){
        super.addItem(DataSet.EMPTY_DATA_SET);
      }else{
        if(val instanceof DataSet){
          super.addItem(val);
          ((DataSet)val).addIObserver(this);
        }else{
          throw new ClassCastException(val+" cannot be cast as a DataSet");
        }
      }
    }

    // ********** IObserver requirements **********
    public void update(Object observed, Object reason){
      if( !(reason instanceof String) ) return; // reason should be a string
      if( ! (IObserver.DESTROY.equals((String)reason)) )
        return;                                      // must be a destroy event
      if( !(observed instanceof DataSet) ) return; // must be a DataSet

      // -- remove references to the DataSet
      // from choices
      this.vals.remove(observed);
      // from GUI
      if(this.initialized) ((HashEntry)(entrywidget.getComponent(0))).removeItem(observed);
      // from the value      
      if(this.value==observed){
        if(this.vals!=null && this.vals.size()>0)
          this.value=this.vals.elementAt(0); // set to first choice
        else
          this.value=DataSet.EMPTY_DATA_SET; // or empty dataset
      }

      // stop listening
      ((DataSet)observed).deleteIObserver(this);
    }

    // ********** IParameter requirements **********

    /**
     * Returns the value of the parameter. While this is a generic
     * object specific parameters will return appropriate
     * objects. There can also be a 'fast access' method which returns
     * a specific object (such as String or DataSet) without casting.
     */
    public DataSet getDataSetValue(){
        Object value=this.getValue();
        if(value instanceof DataSet){
            return (DataSet)value;
        }else{
            return null;
        }
    }

    /**
     * Calls the parent method with DataSet.EMPTY_DATA_SET if value is
     * null.
     */
    public void setValue(Object value){
      if(value==null){
        super.setValue(DataSet.EMPTY_DATA_SET);
      }else{
        if( value instanceof DataSet )
          super.setValue(value);
        else
          throw new ClassCastException(value+" cannot be cast as a DataSet");
      }
    }

    /*
     * Main method for testing purposes.
     */
    /*public static void main(String args[]){
        DataSetPG fpg;
        int y=0, dy=70;

        String filename=null;
        if(args.length==1){
            filename=args[0];
        }else{
            filename="/home/students/bouzekc/ISAW/SampleRuns/SCD06497.RUN";
        }

        RunfileRetriever rr=new RunfileRetriever(filename);
        DataSet[] ds=new DataSet[rr.numDataSets()];
        for( int i=0 ; i<rr.numDataSets() ; i++ ){
            ds[i]=rr.getDataSet(i);
        }

        fpg=new DataSetPG("a",ds[0]);
        System.out.println(fpg);
        fpg.initGUI(ds);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new DataSetPG("b",ds[0]);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(ds);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new DataSetPG("c",ds[0],false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(ds);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new DataSetPG("d",ds[0],true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.initGUI(ds);
        fpg.showGUIPanel(0,y);
        y+=dy;
    }*/

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        DataSetPG pg=new DataSetPG(this.name,this.value,this.valid);
        pg.vals=(Vector)this.vals.clone();
        pg.setDrawValid(this.getDrawValid());
        pg.initialized=false;
        return pg;
    }
}
