/*
 * File: PropertiesLoader.java
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
 *  Revision 1.8  2002/07/15 14:40:20  rmikk
 *  The Contour.Style property's initial value can now come
 *    from the IsawProps.dat.  Also the default initial value is
 *    now Raster
 *
 *  Revision 1.7  2002/07/12 18:21:22  pfpeterson
 *  Added convenience methods to releave the duty of casting from
 *  the caller.
 *
 *  Revision 1.6  2002/07/12 15:31:56  pfpeterson
 *  Add default value for ColorScale property.
 *
 *  Revision 1.5  2002/06/14 15:55:41  pfpeterson
 *  Added default values for getProperty(String) for Isaw_Width,
 *  Isaw_Height, Tree_Width, and Status_Height.
 *
 *  Revision 1.4  2002/06/14 14:25:45  pfpeterson
 *  The start of a central location for getting system properties
 *  with appropriate default values.
 *
 *  Revision 1.3  2001/08/02 15:47:19  dennis
 *  Added is_loaded() method to check whether or not the properties file
 *  was found and loaded ok.
 *
 *  Revision 1.2  2001/07/24 16:33:49  dennis
 *  Added java docs for reload() method, now that it has
 *  been tested.
 *
 *  Revision 1.1  2001/07/23 19:03:18  dennis
 *  Utility class to load System properties one time.
 *
 */
package DataSetTools.util;

import java.util.*;
import java.io.*;
import IsawGUI.DefaultProperties;
import DataSetTools.viewer.ViewerState;

/**
 *  Constructing an object of this class will load the system properties
 *  from the specified properties file.  If the properties file is edited,
 *  the properties file may be re-read by calling the reload() method.
 */

public class PropertiesLoader implements java.io.Serializable 
{
  private String  f_name = "";
  private boolean loaded_ok = false;


  public PropertiesLoader( String file_name )
  {
    f_name = file_name;
    reload();
  }

/**
 *  Checks whether or not the properties file specified in the constructor
 *  was loaded correctly.
 *
 *  @return  true if the properties has been properly loaded, false otherwise.
 */
  public boolean is_loaded()
  {
    return loaded_ok;
  }


/**
 *  Load or reload the properties file that was specified when this 
 *  PropertiesLoader object was constructed.  NOTE: This may be called
 *  at any time, if the cost of re-reading the properties file is not
 *  to high and the user may have edited the properties file since it
 *  was last read.
 */
  public void reload(){
    String full_name = System.getProperty( "user.home" ) + "/" + f_name;
    try{
      FileInputStream input = new FileInputStream( full_name );
      Properties new_props = new Properties( System.getProperties() );
      new_props.load( input );
      System.setProperties( new_props );
      input.close();
      loaded_ok = true;
    }catch ( IOException e ){
      System.out.println("Properties file: " + f_name + " NOT FOUND" );
      DefaultProperties dp=new DefaultProperties();
      dp.write();
      loaded_ok = true;
      return;
    }  
  }
  
    /**
     * Private method containing all of the defaults that exists
     */
    private String getDefault(String prop){
        String def=null;
        if( prop.equals("Isaw_Width") ){
            def="0.8";
        }else if( prop.equals("Isaw_Height") ){
            def="0.4";
        }else if( prop.equals("Tree_Width") ){
            def="0.2";
        }else if( prop.equals("Status_Height") ){
            def="0.2";
        }else if( prop.equals(ViewerState.COLOR_SCALE) ){
            def=
             DataSetTools.components.image.IndexColorMaker.HEATED_OBJECT_SCALE;
        }else if( prop.equals(ViewerState.REBIN) ){
            def="true";
        }else if( prop.equals(ViewerState.H_SCROLL) ){
            def="false";
        }else if( prop.equals(ViewerState.H_SCROLL_POSITION) ){
            def="0";
        }else if( prop.equals(ViewerState.POINTED_AT_INDEX) ){
            def="0";
        }else if( prop.equals(ViewerState.V_AZIMUTH) ){
            def="45";
        }else if( prop.equals(ViewerState.V_ALTITUDE) ){
            def="20";
        }else if( prop.equals(ViewerState.V_DISTANCE) ){
            def="-1";
        }else if( prop.equals(ViewerState.V_DETECTORS) ){
            def="NOT_DRAWN";
        }else if( prop.equals(ViewerState.V_GROUPS) ){
            def="NOT_DRAWN";
        }else if( prop.equals(ViewerState.BRIGHTNESS) ){
            def="40";
        }else if( prop.equals(ViewerState.AUTO_SCALE) ){
            def="0";
        }else if( prop.equals( ViewerState.CONTOUR_STYLE) ){
           { def = ""+gov.noaa.pmel.sgt.GridAttribute.RASTER;
             
           }
        }
        return def;
    }

    /**
     * Convenience method that calls get(prop,def) with the
     * appropriate default value.
     */
    public String get(String prop){
        return this.get(prop,this.getDefault(prop));
    }

    /**
     * Determines the system property using appropriate default values
     * if necessary.
     */
    public String get(String prop, String def){
        String rs=null;
        if(!loaded_ok) reload();
        
        if(def==null || def.length()==0){
            rs=System.getProperty(prop);
        }else{
            rs=System.getProperty(prop,def);
        }
        return rs;
    }

    /**
     * Determines the system property using appropriate default values
     * if necessary.
     */
    protected Boolean getBoolean(String prop){
        return this.getBoolean(prop,this.getDefault(prop));
    }

    /**
     * Determines the system property using appropriate default values
     * if necessary.
     */
    protected Boolean getBoolean(String prop, String def){
        String property=this.get(prop,def);
        Boolean val=null;

        if(property!=null)
            val=new Boolean(prop);

        return val;
    }

    /**
     * Determines the system property using appropriate default values
     * if necessary.
     */
    protected Double getDouble(String prop){
        return this.getDouble(prop,this.getDefault(prop));
    }

    /**
     * Determines the system property using appropriate default values
     * if necessary.
     */
    protected Double getDouble(String prop, String def){
        String property=this.get(prop,def);
        Double val=null;

        if(property!=null)
            try{
                val=new Double(property);
            }catch(NumberFormatException e){
                // let it drop on the floor
            }

        if(val==null && def!=null)
            try{
                val=new Double(def);
            }catch(NumberFormatException e){
                // let it drop on the floor
            }
        return val;
    }

    /**
     * Determines the system property using appropriate default values
     * if necessary.
     */
    protected Float getFloat(String prop){
        return this.getFloat(prop,this.getDefault(prop));
    }

    /**
     * Determines the system property using appropriate default values
     * if necessary.
     */
    protected Float getFloat(String prop, String def){
        String property=this.get(prop,def);
        Float val=null;

        if(property!=null)
            try{
                val=new Float(property);
            }catch(NumberFormatException e){
                // let it drop on the floor
            }

        if(val==null && def!=null)
            try{
                val=new Float(def);
            }catch(NumberFormatException e){
                // let it drop on the floor
            }
        return val;
    }

    /**
     * Determines the system property using appropriate default values
     * if necessary.
     */
    protected Integer getInteger(String prop){
        return this.getInteger(prop,this.getDefault(prop));
    }

    /**
     * Determines the system property using appropriate default values
     * if necessary.
     */
    protected Integer getInteger(String prop, String def){
        String property=this.get(prop,def);

        Integer val=null;

        if(property!=null)
            try{
                val=new Integer(property);
            }catch(NumberFormatException e){
                // let it drop on the floor
            }

        if(val==null && def!=null)
            try{
                val=new Integer(def);
            }catch(NumberFormatException e){
                // let it drop on the floor
            }
        return val;
    }

}
