/*
 * File:  DataDirPG.java 
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
 *  Revision 1.17  2003/11/19 04:13:22  bouzekc
 *  Is now a JavaBean.
 *
 *  Revision 1.16  2003/10/11 19:19:15  bouzekc
 *  Removed clone() as the superclass now implements it using reflection.
 *
 *  Revision 1.15  2003/09/09 23:06:28  bouzekc
 *  Implemented validateSelf().
 *
 *  Revision 1.14  2003/08/15 23:50:04  bouzekc
 *  Modified to work with new IParameterGUI and ParameterGUI
 *  classes.  Commented out testbed main().
 *
 *  Revision 1.13  2003/08/14 18:43:20  bouzekc
 *  Modified getValue() to put a trailing slash on when one does not exist.
 *
 *  Revision 1.12  2003/07/15 22:56:13  bouzekc
 *  getValue() now uses forward slashes rather than system
 *  dependent slashes.
 *
 *  Revision 1.11  2003/06/11 23:03:00  bouzekc
 *  Now returns the directory name with the system-appropriate
 *  separator appended to the end.
 *
 *  Revision 1.10  2003/06/02 22:10:57  bouzekc
 *  Fixed ClassCastException in constructor.
 *
 *  Revision 1.9  2003/05/29 21:39:38  bouzekc
 *  Removed the init(Vector init_values) method.  Now uses
 *  BrowsePG's init method, and sets the file selection type
 *  in the constructor.
 *
 *  Revision 1.8  2003/03/03 16:32:06  pfpeterson
 *  Only creates GUI once init is called.
 *
 *  Revision 1.7  2003/02/07 16:19:17  pfpeterson
 *  Fixed bug in constructor where the value of 'valid' was not properly set.
 *
 *  Revision 1.6  2002/11/27 23:22:42  pfpeterson
 *  standardized header
 *
 *  Revision 1.5  2002/10/23 18:50:43  pfpeterson
 *  Now supports a javax.swing.filechooser.FileFilter to be specified
 *  for browsing options. Also fixed bug where it did not automatically
 *  switch to the data directory if no value was specified.
 *
 *  Revision 1.4  2002/10/10 19:15:13  pfpeterson
 *  Fixed a bug where the Data_Directory was not used if a value
 *  was not specified.
 *
 *  Revision 1.3  2002/10/07 15:27:35  pfpeterson
 *  Another attempt to fix the clone() bug.
 *
 *  Revision 1.2  2002/09/30 15:20:45  pfpeterson
 *  Update clone method to return an object of this class.
 *
 *  Revision 1.1  2002/07/15 21:26:07  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.parameter;
import javax.swing.*;
import java.util.Vector;
import java.lang.String;
import java.beans.*;
import java.io.File;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;
import DataSetTools.operator.Generic.TOF_SCD.*;

/**
 * This is a particular case of the BrowsePG used for loading a single
 * file. The value is a string.
 */
public class DataDirPG extends BrowsePG{
    private static String TYPE     = "DataDir";

    // ********** Constructors **********
    public DataDirPG(String name, Object value){
        this(name,value,false);
        this.setDrawValid(false);
    }
    
    public DataDirPG(String name, Object value, boolean valid){
        super(name,value,valid);
        if(value!=null)
        {
          if(!(value instanceof String))
            value = value.toString();
          if( ((String)value).length()>0 )
          {
            File file=new File((String)value);
            if(file.exists()){
              if(file.isFile()){
                this.setValue(file.getParent());
              }
            }
          }
        }
        this.setType(TYPE);
        this.setValid(valid);
        super.choosertype = BrowseButtonListener.DIR_ONLY;
    }

    // ********** IParameter requirements **********

    /**
     * Returns the value of the parameter. While this is a generic
     * object specific parameters will return appropriate
     * objects. There can also be a 'fast access' method which returns
     * a specific object (such as String or DataSet) without casting.
     */
    public Object getValue()
    {
      String str = FilenameUtil.setForwardSlash(
                     super.getValue().toString());
      if( !str.endsWith("/") ){
        str += "/";
      }
      
      return str;
    }

    /*
     * Testbed.
     */
    /*public static void main(String args[]){
        DataDirPG fpg;
        //y position and delta y, so that multiple windows can 
        //be displayed without too much overlap
        int y=0, dy=70;
        
        String defString="/IPNShome/bouzekc/IsawProps.dat";

        fpg=new DataDirPG ("Enabled, not valid, no filters",defString);
        System.out.println(fpg);
        fpg.initGUI(null);
        fpg.showGUIPanel(0,y);
        y+=dy;
        
        //disabled browse button GUI
        fpg=new DataDirPG ("Disabled, not valid, no filters",defString);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(null);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new DataDirPG ("Disabled, not valid, no filters",defString,false);
        System.out.println(fpg);
        fpg.setEnabled(false);
        fpg.initGUI(null);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new DataDirPG ("Valid, enabled, no filters",defString,true);
        System.out.println(fpg);
        fpg.setDrawValid(true);
        fpg.initGUI(null);
        fpg.showGUIPanel(0,y);
        
        fpg=new DataDirPG ("Enabled, not valid, multiple filters",defString);
        System.out.println(fpg);
        //add some FileFilters
        fpg.addFilter(new ExpFilter());
        fpg.addFilter(new IntegrateFilter());
        fpg.addFilter(new MatrixFilter());
        fpg.initGUI(null);
        fpg.showGUIPanel(0,y);
        fpg.showGUIPanel(0,y);
        y+=dy;

        fpg=new DataDirPG ("Enabled, not valid, one filter",defString);
        System.out.println(fpg);
        //add some FileFilters
        fpg.addFilter(new IntegrateFilter());
        fpg.initGUI(null);
        fpg.showGUIPanel(0,y);
        y+=dy;
    }*/

    /**
     * Validates this DataDirPG.  A DataDirPG is considered valid if and only
     * if getValue() returns a String which references an actual directory.  
     */
    public void validateSelf(  ) {
      Object val = getValue(  );
      
      if( val != null ) {
      
        File file = new File( val.toString(  ) );
        
        if( file.exists(  ) && file.isDirectory(  ) ) { 
          setValid( true );
        } else {
          setValid( false );
        }
      } else {
        setValid( false );
      }
    }
}
