/*
 * File:  ParameterGUI.java 
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
 *  Revision 1.4  2002/11/27 23:22:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/09/19 16:07:24  pfpeterson
 *  Changed to work with new system where operators get IParameters in stead of Parameters. Now support clone method.
 *
 *  Revision 1.2  2002/07/15 21:27:07  pfpeterson
 *  Factored out parts of the GUI.
 *
 *  Revision 1.1  2002/06/06 16:14:36  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import javax.swing.*;
import java.awt.*;
import java.beans.*;
import DataSetTools.util.PropertyChanger;
import java.beans.PropertyChangeListener;
import DataSetTools.util.PropertyChanger;

/**
 * This is a superclass to take care of many of the common details of
 * ParameterGUIs.
 */
public abstract class ParameterGUI implements IParameterGUI, PropertyChanger,
                                              PropertyChangeListener{
    // instance variables for IParameter
    protected String     name;
    protected Object     value;
    protected boolean    valid;
    protected String     type;
    // instance variables for IParameterGUI
    protected JLabel     label;
    protected JComponent entrywidget;
    protected JPanel     guipanel;
    protected boolean    enabled;
    protected boolean    drawvalid;
    protected JCheckBox  validcheck;
    // extra instance variables
    protected boolean    initialized;
    protected boolean    ignore_prop_change;

    // ********** IParameter requirements **********
    /**
     * Returns the name of the parameter. This is normally used as the
     * title of the parameter.
     */
    public String getName(){
        return this.name;
    }

    /**
     * Set the name of the parameter.
     */
    public void setName(String name){
        this.name=name;
        if(this.label==null){
            label=new JLabel();
        }
        label.setText("  "+this.getName());
    }

    /**
     * Returns the value of the parameter. While this is a generic
     * object specific parameters will return appropriate
     * objects. There can also be a 'fast access' method which returns
     * a specific object (such as Float or DataSet) without casting.
     */
    /* public Object getValue(){
       return value;
       } */

    /**
     * Sets the value of the parameter.
     */
    /*public void setValue(Object value){
      this.value=value;
      }*/

    /**
     * Returns whether or not the parameter is valid. Currently used
     * only by wizards.
     */
    public boolean getValid(){
        return this.valid;
    }

    /**
     * Set the valid state of the parameter.
     */
    public void setValid(boolean valid){
        this.valid=valid;
        this.updateDrawValid();
    }
    
    /**
     * Returns the string used in scripts to denote the particular
     * parameter.
     */
    public String getType(){
        return this.type;
    }
    
    // ********** IParameterGUI requirements **********
    /**
     * Allows for initialization of the GUI after instantiation.
     */
     /*public void init(java.util.Vector init_values){
       }*/

    /**
     * Method for producing an alternative layout of the GUI.
     */
    public JLabel getLabel(){
        return label;
    }

    /**
     * Method for producing an alternative layout of the GUI.
     */
    public JComponent getEntryWidget(){
        return entrywidget;
    }

    /**
     * Method for obtaining the default layout of the GUI.
     */
    public JPanel getGUIPanel(){
        return guipanel;
    }
    
    /**
     * Determine if the entry widget is enabled.
     */
    public boolean getEnabled(){
        return enabled;
    }

    /**
     * Set the enabled state of the EntryWidget. This produces a more
     * pleasant effect that the default setEnabled of the widget.
     */
    /*public void setEnabled(boolean enable){
      this.enable=enable;
      }*/

    /**
     * Determine if the 'valid' checkbox will be drawn.
     */
    public boolean getDrawValid(){
        return drawvalid;
    }

    /**
     * Specify if the valid checkbox will be drawn.
     */
    public void setDrawValid(boolean draw){
        this.drawvalid=draw;
        this.updateDrawValid();
    }

    public void init(){
        this.init(null);
    }

    // ********** methods for PropertyChangeListener **********
    public void propertyChange(PropertyChangeEvent ev){
        if(this.ignore_prop_change) return;
        this.setValid(false);
        /* this.setValue(ev.getNewValue());
           this.setValid(false); */
    }

    // ********** methods for PropertyChanger **********
    // implementation of DataSetTools.util.PropertyChanger interface

    /**
     * @param pcl The property change listener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.entrywidget.removePropertyChangeListener(pcl);
    }
    
    /**
     * @param pcl The property change listener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.entrywidget.addPropertyChangeListener(pcl);
    }
    
    /**
     * @param pcl  The property change listener to be added.
     * @param prop The property to listen for.
     */
    public void addPropertyChangeListener(String prop, 
                                          PropertyChangeListener pcl) {
        this.entrywidget.addPropertyChangeListener(prop,pcl);
    }


    // ********** methods to make life easier **********

    /**
     * Utility method to centralize dealing with the checkbox.
     */
    private void updateDrawValid(){
        if(this.validcheck==null){     // make the checkbox if it dne
            this.validcheck=new JCheckBox("");
        }
        this.validcheck.setSelected(this.getValid());
        this.validcheck.setEnabled(false);
        this.validcheck.setVisible(this.getDrawValid());
        this.setName(this.getName());
    }

    /**
     * Method to pack up everything in the frame.
     */
    protected void packupGUI(){
        if( this.getLabel()!=null && this.getEntryWidget()!=null
                                                    && this.validcheck!=null ){
            this.guipanel=new JPanel();
            this.guipanel.setLayout(new BorderLayout());
            JPanel innerpanel=new JPanel(new GridLayout(1,2));
            innerpanel.add(this.getLabel());
            innerpanel.add(this.getEntryWidget());
            JPanel checkpanel=new JPanel(new GridLayout(1,1));
            checkpanel.add(this.validcheck);
            this.guipanel.add(innerpanel,BorderLayout.CENTER);
            this.guipanel.add(checkpanel,BorderLayout.EAST);
            /* this.guipanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,2));
               JPanel innerpanel=new JPanel(new GridLayout(1,2));
               innerpanel.add(this.getLabel());
               innerpanel.add(this.getEntryWidget());
               this.guipanel.add(innerpanel);
               this.guipanel.add(this.validcheck); */
        }else{
            System.err.println("cannot construct GUI component of "
                               +this.getType()+" "+this.getName());
        }
    }

    // ********** convienience testing methods **********
    public String toString(){
        String rs=this.getType()+": \""+this.getName()+"\" "+this.getValue()
            +" "+this.getValid();
        return rs;
    }
    protected void showGUIPanel(){
        this.showGUIPanel(0,0);
    }
    protected void showGUIPanel(int x, int y){
        if(this.getGUIPanel()!=null){
            JFrame mw=new JFrame("Test Display of "+this.getType());
            mw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mw.getContentPane().add(this.getGUIPanel());
            mw.pack();
            Rectangle pos=mw.getBounds();
            pos.setLocation(x,y);
            mw.setBounds(pos);
            mw.show();
        }
    }

    /**
     * Definition of the clone method.
     */
    public Object clone(){
        return this.clone();
        /*ParameterGUI pg=new ParameterGUI(this.name,this.value,this.valid);
          pg.setDrawValid(this.getDrawValid());
          pg.initialized=false;
          return pg;*/
    }
}
