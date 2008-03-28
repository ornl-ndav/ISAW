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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.16  2005/06/15 13:49:39  dennis
 *  Commented out informational message indicating where the
 *  properties file is being loaded from.
 *
 *  Revision 1.15  2005/06/03 21:35:54  dennis
 *  Added print statements showing what IsawProps.dat file is read.
 *  Added debug flag.  If set true, the original System properties
 *  will be printed, followed the the new System properties after
 *  addding the properties from IsawProps.dat
 *
 *  Revision 1.14  2004/05/29 16:39:26  rmikk
 *  Fixed an error in getting Boolean properties
 *
 *  Revision 1.13  2004/03/15 03:28:53  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.12  2003/02/13 17:29:11  pfpeterson
 *  Moved decision making for image directory from SplashWindowFrame.
 *
 *  Revision 1.11  2003/01/20 17:27:20  pfpeterson
 *  Specified the default for 'Default_Ext' to be 'ipns'.
 *
 *  Revision 1.10  2002/11/27 23:23:49  pfpeterson
 *  standardized header
 *
 *  Revision 1.9  2002/07/18 21:30:52  dennis
 *  Corrected spelling of "NOT DRAWN".  Also, now trims the
 *  String returned by System.getProperty().
 *
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
  private boolean debug  = false;
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

      Properties new_props = new Properties( System.getProperties() );
      if ( debug )
      {
        System.out.println("========= " +
                           "Initial properties are" +
                           " ==========" );
        new_props.list(System.out);
      }

//    System.out.println("LOADING PROPERTIES FROM " + full_name + "....");
      FileInputStream input = new FileInputStream( full_name );
      new_props.load( input );
      if ( debug )
      {
        System.out.println("========== " + 
                           "After reading IsawProps.dat, properties are" +
                           " ==========" );
        new_props.list(System.out);
      }

      System.setProperties( new_props );
      input.close();
      loaded_ok = true;

    }catch ( IOException e ){
      System.out.println("Properties file: " + full_name + " NOT FOUND" );
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
        }else if( prop.equals("IMAGE_DIR") ){
            def=get("ISAW_HOME")+"/images/";
        }else if( prop.equals(ViewerState.COLOR_SCALE) ){
            def=
             gov.anl.ipns.ViewTools.Panels.Image.IndexColorMaker.HEATED_OBJECT_SCALE;
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
            def="NOT DRAWN";
        }else if( prop.equals(ViewerState.V_GROUPS) ){
            def="NOT DRAWN";
        }else if( prop.equals(ViewerState.BRIGHTNESS) ){
            def="40";
        }else if( prop.equals(ViewerState.AUTO_SCALE) ){
            def="0";
        }else if( prop.equals("Default_Ext") ){
            def="nexus";
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
        if ( rs != null )
        {
          rs = rs.trim();
          rs = TranslateSysPropertyPrefix( rs );
        }

        return rs;
    }


    /**
     *  A property can reference a java property or environmental 
     *  variable such as "${user.home}/data" or ${HOME}/data.  This,
     *  method checks first for a java property with the specified 
     *  name and if it is not found, it next checks for a system 
     *  environment variable with the name.  If is is found, the 
     *  tag ${....} substring will be replaced with the property or
     *  environmental value that was found.  If it is not found, the
     *  original string will be returned.
     *
     *  @param rs   A trimmed "raw" property string as returned by
     *              System.getProperty()
     *
     *  @return If the String rs, starts with a sequence of characters
     *          like: ${name}, a new string is returned with that 
     *          sequence replaced by the corresponding java property
     *          or system environment value.  If the property is not
     *          found, then the original rs is returned. If the String
     *          rs is null, or doesn't start with a "$" then the
     *          original rs is also returned.
     */ 
    public String TranslateSysPropertyPrefix( String rs )
    {
      if ( rs == null )
        return rs;

      if ( ! rs.startsWith("$") )      // no embedded system property
        return rs;
 
      int index_1 = rs.indexOf( "{" );  
      int index_2 = rs.indexOf( "}" );  
                                       // make sure we have a reasonably valid
                                       // form for the property name
      if ( index_1 < 0 || index_2 < 0 || index_1 > index_2 )  
        return rs;

      String property_name  = rs.substring( index_1 + 1, index_2 );
      String property_value = System.getProperty( property_name );
      if ( property_value == null )
        property_value = System.getenv( property_name );

      if ( property_value == null )
        return rs;

      String remaining_chars = rs.substring( index_2 + 1 );

      return property_value + remaining_chars;
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
            val=new Boolean(property);

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
