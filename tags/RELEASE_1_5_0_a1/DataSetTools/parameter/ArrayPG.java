/*
 * File:  ArrayPG.java 
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
 *  Revision 1.5  2003/02/24 21:01:36  pfpeterson
 *  Major reworking. This version is completely incompatible with previous
 *  versions. Value changed to a vector which cannot be changed in the GUI.
 *  This should be subclassed for particular types of vectors in order to
 *  make the value changable in the GUI.
 *
 *  Revision 1.4  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/10/10 22:11:49  pfpeterson
 *  Fixed a bug with the clone method not getting the choices copied over.
 *
 *  Revision 1.2  2002/09/19 16:07:20  pfpeterson
 *  Changed to work with new system where operators get IParameters in stead of Parameters. Now support clone method.
 *
 *  Revision 1.1  2002/08/01 18:40:01  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;

import java.util.Vector;
import DataSetTools.components.ParametersGUI.HashEntry;
import javax.swing.JLabel;

/**
 * This is a superclass to take care of many of the common details of
 * Array Parameter GUIs.
 */
public class ArrayPG extends ParameterGUI{
    // static variables
    private   static String TYPE     = "Array";
    protected static int    DEF_COLS = 20;

    // instance variables
    protected Vector value=null;

    // ********** Constructors **********
    public ArrayPG(String name, Object value){
        this(name,value,false);
        this.setDrawValid(false);
        this.type=TYPE;
    }

    public ArrayPG(String name, Object value, boolean valid){
        this.setName(name);
        this.setValue(value);
        this.setEnabled(true);
        this.setValid(valid);
        this.setDrawValid(true);
        this.type=TYPE;
        this.initialized=false;
        this.ignore_prop_change=false;
    }

    // ********** Methods to deal with the hash **********

    /**
     * Add a single item to the vector
     */
    public void addItem( Object val){
        if(this.value==null) this.value=new Vector();// initialize if necessary
        if(val==null) return; // don't add null to the vector
        if(this.value.indexOf(val)<0) this.value.add(val); // add if unique
        if(this.initialized)
          ((JLabel)this.entrywidget).setText(this.stringVersion());
    }

    /**
     * Add a set of items to the vector of choices at once.
     */
    public void addItems( Vector values){
        for( int i=0 ; i<values.size() ; i++ ){
            addItem(values.elementAt(i));
        }
    }

    /**
     * Remove an item from the hash based on its key.
     */
    public void removeItem( Object val ){
        int index=this.value.indexOf(val);
        this.removeItem(index);
    }

    public void removeItem(int index){
      if(index>=0 && index<value.size())
        value.remove(index);
    }

    /**
     * Calls Vector.clear() on the value.
     */
    public void clearValue(){
      if(this.value==null) return;

      this.value.clear();
      if(this.initialized)
        ((JLabel)this.entrywidget).setText(this.stringVersion());
    }

    // ********** IParameter requirements **********

    /**
     * Returns the value of the parameter. While this is a generic
     * object specific parameters will return appropriate
     * objects. There can also be a 'fast access' method which returns
     * a specific object (such as String or DataSet) without casting.
     */
    public Object getValue(){
        return this.value;
    }
    
    public Vector getVectorValue(){
      return this.value;
    }

    /**
     * Sets the value of the parameter.
     */
    public void setValue(Object value){
      if(value==null)
        this.value=null;
      else if(value instanceof Vector)
        this.value=(Vector)value;
      else
        return;

      if(this.initialized)
        ((JLabel)this.entrywidget).setText(this.stringVersion());

      this.setValid(true);
    }

    // ********** IParameterGUI requirements **********
    /**
     * Allows for initialization of the GUI after instantiation.
     */
    public void init(Vector init_values){
        if(this.initialized) return; // don't initialize more than once

        this.entrywidget=new JLabel(this.stringVersion());
        this.packupGUI();
        this.initialized=true;
    }

    /**
     * Since this is an array parameter, better allow an array to
     * initialize the GUI.
     */
    public void init(Object init_values[]){
        Vector init_vec=new Vector();
        for( int i=0 ; i<init_values.length ; i++ ){
            init_vec.add(init_values[i]);
        }
        this.init(init_vec);
    }

    /**
     * This is an empty method. It is not used because the value
     * cannot be changed from the GUI.
     */
    public void setEnabled(boolean enabled){
    }

    /**
     * Creates the string to be placed in the label for the GUI
     */
    private String stringVersion(){
      if(this.value==null || this.value.size()<=0){
        return "";
      }else{
        StringBuffer result=new StringBuffer();
        int numElements=this.value.size();
        int start=0;
        int index=0;
        while(start<numElements){
          index=checkSame(this.value,start);
          result.append(shortName(this.value.elementAt(index+start-1))
                     +"["+index+"]");
          start=start+index;
          if(start<numElements)
            result.append(", ");
          index=0;
        }

        if(result.length()>0)
          return result.toString();
        else
          return this.value.toString();
      }
    }

    /**
     * Determines how many elements are identical.
     */
    private int checkSame(Vector vals, int start){
      if(vals==null || vals.size()<=0) return -1;
      if(start>=vals.size()) return -1;

      int same=1;

      String first=vals.elementAt(start).getClass().getName();
      for( int i=1 ; i<vals.size() ; i++ ){
        if(first.equals(vals.elementAt(i).getClass().getName()))
          same++;
        else
          return same;
      }

      return same;
    }

    /**
     * Create a short version of a classname based on the object provided.
     */
    private String shortName(Object obj){
      // get the name of the class
      String res=obj.getClass().getName();

      // determine what to trim off
      int start=res.lastIndexOf(".");
      int end=res.length();
      if(res.endsWith(";"))end--;

      // return the trimmed version
      if(start>=0&&end>=0)
        return res.substring(start+1,end);
      else
        return res;
    }

    /**
     * Main method for testing purposes.
     */
    static void main(String args[]){
        ArrayPG fpg;
        int y=0, dy=70;

        Vector vals=new Vector();
        vals.add("bob");
        vals.add("bob");
        vals.add("doug");

        fpg=new ArrayPG("a",vals);
        System.out.println(fpg);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        vals.add(new StringBuffer("tim"));
        fpg=new ArrayPG("b",vals);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        vals=new Vector();
        for( int i=1 ; i<=20 ; i++ )
          vals.add(new Integer(i));
        fpg=new ArrayPG("c",vals,false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.init();
        fpg.showGUIPanel(0,y);
        y+=dy;

        vals=new Vector();
        for( float f=1f ; f<100 ; f*=2 )
          vals.add(new Float(f));
        fpg=new ArrayPG("d",vals,true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.init(vals);
        fpg.showGUIPanel(0,y);
        y+=dy;

    }

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        ArrayPG apg=new ArrayPG(this.name,this.value,this.valid);
        apg.setDrawValid(this.getDrawValid());
        apg.initialized=false;
        return apg;
    }
}
