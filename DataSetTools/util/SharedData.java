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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
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
 *  Revision 1.4  2001/08/16 19:34:07  dennis
 *  Temporarily added an instance of Dongfeng's SpectrometerPlotter, so that
 *  DataSets from an old server could be received properly.
 *
 *  Revision 1.3  2001/07/27 22:18:26  dennis
 *  Added public final string BUILD_DATE.
 *
 *  Revision 1.2  2001/07/26 19:52:53  dennis
 *  Removed build date and shared version number.
 *
 *  Revision 1.1  2001/07/23 19:04:20  dennis
 *  Utility class to intialize and/or contain data of which there
 *  should be only one copy and that will be shared by several
 *  packages.
 *
 */

package DataSetTools.util;

import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.SpectrometerPlotter;

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
  public static final PropertiesLoader isaw_props 
                                        = new PropertiesLoader("IsawProps.dat");
  
 public static final String VERSION     = "Unknown_Version";
 
  public static final String BUILD_DATE = "Unknown_Build_Date";

  /** The Global StatusPane.  Everyone can "add"(Display) values on this pane
  * if Displayable or the Values will be displayed on System.out
  */
  public static final Command.StatusPane status_pane= 
              new Command.StatusPane( 30,70);

  // This is here to allow DataSets from an old version of the servers to
  // be received by this version.  SpectromterPlotter is obsolete and should
  // be removed.
  public static final Operator old_op = new SpectrometerPlotter();

  public static final long start_time=System.currentTimeMillis();
 
    /**
     * Convenience method to ease adding to the status pane.
     */
    public static void addmsg(Object value){
        status_pane.add(value);
    }


    /* -------------------- properties methods -------------------- */
    /**
     * Convenience method to consolidate getting system properties.
     */
    public static String getProperty(String prop){
        return isaw_props.get(prop);
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static String getProperty(String prop, String def){
        return isaw_props.get(prop,def);
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static boolean getbooleanProperty(String prop){
        return getBooleanProperty(prop).booleanValue();
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static boolean getbooleanProperty(String prop, String def){
        return getBooleanProperty(prop,def).booleanValue();
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static Boolean getBooleanProperty(String prop){
       return isaw_props.getBoolean(prop);
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static Boolean getBooleanProperty(String prop, String def){
        return isaw_props.getBoolean(prop,def);
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static double getdoubleProperty(String prop){
        return getDoubleProperty(prop).doubleValue();
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static double getdoubleProperty(String prop, String def){
        return getDoubleProperty(prop,def).doubleValue();
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static Double getDoubleProperty(String prop){
       return isaw_props.getDouble(prop);
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static Double getDoubleProperty(String prop, String def){
        return isaw_props.getDouble(prop,def);
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static int getintProperty(String prop){
        return getIntegerProperty(prop).intValue();
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static int getintProperty(String prop, String def){
        return getIntegerProperty(prop,def).intValue();
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static Integer getIntegerProperty(String prop){
        return isaw_props.getInteger(prop);
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static Integer getIntegerProperty(String prop, String def){
        return isaw_props.getInteger(prop,def);
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static float getfloatProperty(String prop){
        return getFloatProperty(prop).floatValue();
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static float getfloatProperty(String prop, String def){
        return getFloatProperty(prop,def).floatValue();
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static Float getFloatProperty(String prop){
        return isaw_props.getFloat(prop);
    }

    /**
     * Convenience method to consolidate getting system properties.
     */
    public static Float getFloatProperty(String prop, String def){
        return isaw_props.getFloat(prop,def);
    }

}
