/*
 * File: SharedData.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Last Modified:
 *
 *  $Author$
 *  $Date$            
 *  $Revision$
 *
 *  $Log$
 *  Revision 1.20  2004/08/18 20:17:46  rmikk
 *  Now the status_pane IS gov.anl.ipns.Util.Sys.SharedMessages status_pane
 *
 *  Revision 1.19  2004/03/15 03:28:54  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.18  2004/01/24 20:57:51  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.17  2003/06/18 20:36:56  pfpeterson
 *  Changed calls for NxNodeUtils.Showw(Object) to
 *  DataSetTools.util.StringUtil.toString(Object)
 *
 *  Revision 1.16  2003/03/06 15:47:53  pfpeterson
 *  Made the StatusPane a private variable that is not initialized until
 *  someone asks for a pointer to it. Also moved decision to print to
 *  STDOUT rather than StatusPane into addmsg(String).
 *
 *  Revision 1.15  2002/12/10 20:38:09  pfpeterson
 *  Removed static HelpSet instance by Ruth's recommendation.
 *
 *  Revision 1.14  2002/12/08 22:03:45  dennis
 *  Added shared instance of HTMLizer for the new help system. (Ruth)
 *
 *  Revision 1.13  2002/11/27 23:23:49  pfpeterson
 *  standardized header
 *
 *  Revision 1.12  2002/10/24 19:34:07  pfpeterson
 *  Implemented a debug flag for global use.
 *
 *  Revision 1.11  2002/10/24 16:51:53  pfpeterson
 *  Removed references to SpectrometerPlotter.
 *
 *  Revision 1.10  2002/10/07 16:33:26  pfpeterson
 *  Added an epoch time so System.currentTimeMillis() can be
 *  converted to an integer.
 *
 *  Revision 1.9  2002/07/12 18:21:23  pfpeterson
 *  Added convenience methods to releave the duty of casting from
 *  the caller.
 *
 *  Revision 1.8  2002/06/14 15:54:10  pfpeterson
 *  Added some convenience methods for getting system properties
 *  and adding a message to the status bar.
 *
 *  Revision 1.7  2002/03/07 22:24:32  pfpeterson
 *  Put global version information in here.
 *
 *  Revision 1.6  2002/02/22 20:36:43  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.5  2002/01/10 15:36:42  rmikk
 *  Added a Global StatusPane.  Everyone can write to this
 *  as follows:
 *     SharedData.status_pane.add( Value)
 *
 */

package DataSetTools.util;

import gov.anl.ipns.Util.Sys.*;
import IsawHelp.HelpSystem.*;
/**
 *  Objects of this class have one instance of objects that are to be shared
 *  by several packages.  The shared objects are instantiated one time 
 *  as static members of the class.
 */

public class SharedData implements java.io.Serializable 
{
 /**
  *  To guarantee that the IsawProps.dat file is loaded, construct an
  *  object of type SharedData.  eg: SharedData sd = new SharedData();
  */

  public static final String USER_PROPS_FILE = "IsawProps.dat";

  private static final PropertiesLoader isaw_props 
                               = new PropertiesLoader( USER_PROPS_FILE );
  
  public static boolean DEBUG = false;

  public static final String VERSION     = "Unknown_Version";
 
  public static final String BUILD_DATE = "Unknown_Build_Date";

  /** The Global StatusPane.  Everyone can "add"(Display) values on this pane
  * if Displayable or the Values will be displayed on System.out
  */
  private static gov.anl.ipns.Util.Sys.StatusPane status_pane=  null;

  /**
   * Returns a pointer to this classes (static) StatusPane for use in
   * GUIs. This will create the StatusPane if it does not already
   * exist.
   */
  public static gov.anl.ipns.Util.Sys.StatusPane getStatusPane(){
    if(status_pane==null)
      //status_pane=new gov.anl.ipns.Util.Sys.StatusPane( 30,70);
      status_pane = gov.anl.ipns.Util.Sys.SharedMessages.getStatusPane();
    return status_pane;
  }

  /** The static variable to handle the production of HTML pages
  *   from an operator
  */
  public static final HTMLizer HTMLPageMaker = new HTMLizer();


  /**
   * The time ISAW was started for timing routines to offset against.
   */
  public static final long start_time=System.currentTimeMillis();
 
    /**
     * Convenience method to ease adding to the status pane.
     */
    public static void addmsg(Object value){
      if( status_pane==null || ! status_pane.isDisplayable())
        System.out.println( StringUtil.toString( value) );  
      else
        status_pane.add(value);
    }


    /* -------------------- properties methods -------------------- */
    /**
     * Convenience method to consolidate getting system properties.
     */

    /**
     *  Reload the ISAW properties, first from a file defined by the
     *  System environmental SHARED_ISAW_PROPS, second from the file
     *  "IsawProps.dat" in the user's home directory.
     */
    public static void reloadProperties()
    {
      isaw_props.reload();
    }


    /**
     * Get the specifed property.  If the value of the property starts
     * with an expression of the form ${name}, and the specified name
     * is the name of a System environmental variable, or a Java
     * property, then ${name} will be replaced by the value of that
     * environmental, or Java property.
     *
     * @param  prop   The name of the property to get.
     *
     * @return A String with the value of the property if that 
     *         property is defined, or null if it's not defined.
     */
    public static String getProperty(String prop){
        return isaw_props.get(prop);
    }

    /**
     * Get the specifed property.  If the value of the property starts
     * with an expression of the form ${name}, and the specified name
     * is the name of a System environmental variable, or a Java
     * property, then ${name} will be replaced by the value of that
     * environmental, or Java property.
     *
     * @param  prop   The name of the property to get.
     * @param  def    Default value to return if the property is not
     *                defined.
     *
     * @return A String with the value of the property if that 
     *         property is defined, or null if it's not defined.
     */
    public static String getProperty(String prop, String def){
        return isaw_props.get(prop,def);
    }

    /**
     * Convenience method to get a boolean valued property.
     */
    public static boolean getbooleanProperty(String prop){
        return getBooleanProperty(prop).booleanValue();
    }

    /**
     * Convenience method to get a boolean valued property.
     */
    public static boolean getbooleanProperty(String prop, String def){
        return getBooleanProperty(prop,def).booleanValue();
    }

    /**
     * Convenience method to get a Boolean valued property.
     */
    public static Boolean getBooleanProperty(String prop){
       return isaw_props.getBoolean(prop);
    }

    /**
     * Convenience method to get a Boolean valued property.
     */
    public static Boolean getBooleanProperty(String prop, String def){
        return isaw_props.getBoolean(prop,def);
    }

    /**
     * Convenience method to get a double valued property.
     */
    public static double getdoubleProperty(String prop){
        return getDoubleProperty(prop).doubleValue();
    }

    /**
     * Convenience method to get a double valued property.
     */
    public static double getdoubleProperty(String prop, String def){
        return getDoubleProperty(prop,def).doubleValue();
    }

    /**
     * Convenience method to get a Double valued property.
     */
    public static Double getDoubleProperty(String prop){
       return isaw_props.getDouble(prop);
    }

    /**
     * Convenience method to get a Double valued property.
     */
    public static Double getDoubleProperty(String prop, String def){
        return isaw_props.getDouble(prop,def);
    }

    /**
     * Convenience method to get an int valued property.
     */
    public static int getintProperty(String prop){
        return getIntegerProperty(prop).intValue();
    }

    /**
     * Convenience method to get an int valued property.
     */
    public static int getintProperty(String prop, String def){
        return getIntegerProperty(prop,def).intValue();
    }

    /**
     * Convenience method to get an Integer valued property.
     */
    public static Integer getIntegerProperty(String prop){
        return isaw_props.getInteger(prop);
    }

    /**
     * Convenience method to get an Integer valued property.
     */
    public static Integer getIntegerProperty(String prop, String def){
        return isaw_props.getInteger(prop,def);
    }

    /**
     * Convenience method to get a float valued property.
     */
    public static float getfloatProperty(String prop){
        return getFloatProperty(prop).floatValue();
    }

    /**
     * Convenience method to get a float valued property.
     */
    public static float getfloatProperty(String prop, String def){
        return getFloatProperty(prop,def).floatValue();
    }

    /**
     * Convenience method to get a Float valued property.
     */
    public static Float getFloatProperty(String prop){
        return isaw_props.getFloat(prop);
    }

    /**
     * Convenience method to get a Float valued property.
     */
    public static Float getFloatProperty(String prop, String def){
        return isaw_props.getFloat(prop,def);
    }

}
