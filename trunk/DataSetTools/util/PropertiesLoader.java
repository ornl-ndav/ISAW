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
     * Convenience method that calls get(prop,def) with the
     * appropriate default value.
     */
    public String get(String prop){
        String def=null;
        if( prop.equals("Isaw_Width") ){
            def="0.8";
        }else if( prop.equals("Isaw_Height") ){
            def="0.4";
        }else if( prop.equals("Tree_Width") ){
            def="0.2";
        }else if( prop.equals("Status_Height") ){
            def="0.2";
        }
        /*if(def!=null){  // testing information
          System.out.println(prop+" gets default "+def);
          } */

        return this.get(prop,def);
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

}
