/*
 * File:  BrowsePG.java 
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
 *  Revision 1.10  2003/06/06 18:49:44  pfpeterson
 *  Made abstract and removed clone method.
 *
 *  Revision 1.9  2003/06/02 20:15:41  bouzekc
 *  setFilter() now adds the given FileFilter if it does not
 *  yet exist in the FileFilter Vector.
 *
 *  Fixed ClassCastException in setValue().
 *
 *  Revision 1.8  2003/05/30 15:00:46  bouzekc
 *  Fixed bug where the entrywidget components overlapped
 *  when resizing.
 *
 *  Revision 1.7  2003/05/29 21:36:56  bouzekc
 *  Now allows multiple FileFilters to be used.  As a
 *  result, a Vector of FileFilters has replaced the
 *  single FileFilter used before.  Also added an
 *  AddFilter method and changed SetFilter to allow
 *  setting a default FileFilter.
 *
 *  Revision 1.6  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.5  2003/02/07 16:19:17  pfpeterson
 *  Fixed bug in constructor where the value of 'valid' was not properly set.
 *
 *  Revision 1.4  2002/11/27 23:22:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/10/23 18:50:42  pfpeterson
 *  Now supports a javax.swing.filechooser.FileFilter to be specified
 *  for browsing options. Also fixed bug where it did not automatically
 *  switch to the data directory if no value was specified.
 *
 *  Revision 1.2  2002/09/19 16:07:21  pfpeterson
 *  Changed to work with new system where operators get IParameters in stead of Parameters. Now support clone method.
 *
 *  Revision 1.1  2002/07/15 21:26:06  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.Vector;
import java.lang.String;
import java.beans.*;
import java.io.File;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.SharedData;
import DataSetTools.operator.Generic.TOF_SCD.*;

/**
 * This is a superclass to take care of many of the common details of
 * BrowsePGs.
 */
abstract public class BrowsePG extends ParameterGUI{
    private static String TYPE     = "Browse";

    protected static int VIS_COLS  = 12;
    protected static int HIDE_COLS = StringPG.DEF_COLS;

    protected StringEntry innerEntry  = null;
    protected JButton     browse      = null;
    protected Vector      filter_vector;
    protected BrowseButtonListener browselistener;
    protected int choosertype;
    
    private int defaultindex;

    // ********** Constructors **********
    public BrowsePG(String name, Object value){
        this(name,value,false);
        this.setDrawValid(false);
        this.type=TYPE;
    }

    public BrowsePG(String name, Object value, boolean valid){
        this.setName(name);
        this.setValue(value);
        this.setEnabled(true);
        this.setValid(valid);
        this.setDrawValid(true);
        this.type=TYPE;
        this.initialized=false;
        this.ignore_prop_change=false;
        if(this.value==null || value.toString().length()<=0){
          String datadir=SharedData.getProperty("Data_Directory");
          this.setValue(datadir);
        }
        this.setValid(valid);
        this.filter_vector = new Vector();
        choosertype = BrowseButtonListener.LOAD_FILE;
        defaultindex = -1;
    }

    // ********** IParameter requirements **********

    /**
     * Returns the value of the parameter. While this is a generic
     * object specific parameters will return appropriate
     * objects. There can also be a 'fast access' method which returns
     * a specific object (such as String or DataSet) without casting.
     */
    public Object getValue(){
        String value=null;
        if(this.initialized){
            value=((JTextField)this.innerEntry).getText();
        }else{
            value=(String)this.value;
        }
        return value;
    }

    public String getStringValue(){
        return (String)this.getValue();
    }

    /**
     * Sets the value of the parameter.
     */
    public void setValue(Object value){
        if(this.initialized){
            if(value==null){
                ((JTextField)this.innerEntry).setText("");
            }else{
                if(value instanceof String){
                    ((JTextField)this.innerEntry).setText((String)value);
                }else{
                    ((JTextField)this.innerEntry).setText(value.toString());
                }
            }
        }else{
            if(value==null){
              this.value = null;
            }else{
                if(value instanceof String){
                  this.value = value;
                }else{
                  this.value = value.toString();
                }
             }    
        }
        this.setValid(true);
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
        innerEntry=new StringEntry(this.getStringValue(),StringPG.DEF_COLS);
        innerEntry.addPropertyChangeListener(IParameter.VALUE, this);
        browse=new JButton("Browse");
        if(browselistener == null){
          browselistener = new BrowseButtonListener(innerEntry,
                                     choosertype,this.filter_vector);
          browselistener.setFileFilter(defaultindex);
        }
        browse.addActionListener(browselistener);
        entrywidget=new JPanel();
        //a Box is needed so that when the entrywidget is resized
        //smaller, the components will not overlap
        Box widgetbox = new Box(BoxLayout.X_AXIS);
        widgetbox.add(innerEntry);
        widgetbox.add(browse);
        entrywidget.add(widgetbox);
        this.setEnabled(this.getEnabled());
        super.initGUI();
    }

    /**
     * Set the enabled state of the EntryWidget. This produces a more
     * pleasant effect than the default setEnabled of the widget.
     */
    public void setEnabled(boolean enabled){
        this.enabled=enabled;
        if(this.innerEntry!=null){
            this.innerEntry.setEditable(enabled);
            if(enabled){
                this.innerEntry.setColumns(VIS_COLS);
            }else{
                this.innerEntry.setColumns(HIDE_COLS);
            }
        }
        if(this.browse!=null){
            this.browse.setVisible(enabled);
        }
    }

    /**
     * Set the FileFilter to be used when the browse button is pressed
     */
    public void setFilter( FileFilter filefilter){
      boolean found = false;
      for( int i = 0; i < filter_vector.size(); i ++ ){
        if(filter_vector.elementAt(i).getClass().isInstance(filefilter)){
          found = true;
          defaultindex = i;
          break;
        }
      }

      if(!found)
      {
        this.addFilter(filefilter);
        defaultindex = filter_vector.size() - 1;
      }
    }

    /**
     * Adds a FileFilter.
     */
    public void addFilter(FileFilter filefilter){
        filter_vector.add(filefilter);
    }
}
