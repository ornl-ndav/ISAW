/*
 * File:  ViewerState.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 *  Revision 1.20  2002/07/26 22:00:10  rmikk
 *  Added a Constant for states in the time slice table
 *
 *  Revision 1.19  2002/07/23 18:18:09  dennis
 *  Added names for V_SCROLL_POSITION and POINTED_AT_X
 *
 *  Revision 1.18  2002/07/15 14:37:32  rmikk
 *  The Contour.Style property's initial value can now come
 *    from the IsawProps.dat.  Also the default initial value is
 *    now Raster
 *
 *  Revision 1.17  2002/07/12 18:32:13  rmikk
 *  Added and initialized the TABLE_DATA
 *      and CONTOUR_STYLE  state fields
 *
 *  Revision 1.16  2002/07/12 18:14:54  pfpeterson
 *  Now only uses SharedData.getProperty().
 *
 *  Revision 1.15  2002/07/12 15:35:42  pfpeterson
 *  Uses SharedData.getProperty() instead of System.getProperty().
 *
 *  Revision 1.14  2002/06/17 20:30:51  dennis
 *  ZoomRegion now only preserved if the number of spectra is the same
 *  (in addition to having the same title, axis labels and units).
 *
 *  Revision 1.13  2001/07/27 15:57:10  dennis
 *  Removed debug print.
 *
 *  Revision 1.12  2001/07/26 14:25:55  dennis
 *  Added Brightnes & Auto-Scale state variables.
 *
 *  Revision 1.11  2001/07/25 18:08:30  dennis
 *  Now initializes state from the system properties and
 *  stores most state info in a heap.  Added generic
 *  methods to get/set Strings, ints and floats by name
 *  in the state object.
 *
 *  Revision 1.10  2001/07/23 18:34:07  dennis
 *  Now gets default color scale from System.properties, if possible.
 *
 *  Revision 1.9  2001/07/17 20:36:47  dennis
 *  Changed default Rebin state to true.
 *
 *  Revision 1.8  2001/06/08 21:58:41  dennis
 *  Now also requires the DataSet titles to match before
 *  restoring the Zoom region state.
 *
 *  Revision 1.7  2001/06/04 20:04:20  dennis
 *  Added rebin flag for GraphView.
 *
 *  Revision 1.6  2001/04/26 14:21:42  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.5  2001/03/02 17:03:30  dennis
 *  Now checks whether the current DataSet uses the same axis units and
 *  labels before returning the zoom region.
 *
 *  Revision 1.4  2001/03/01 23:14:10  dennis
 *  Now allows saving one zoom region for an ImageView.
 *
 *  Revision 1.3  2001/03/01 22:33:07  dennis
 *  Now saves the last "Pointed At" index.
 *
 *  Revision 1.2  2001/01/29 21:26:50  dennis
 *  Now uses CVS version numbers.
 *
 *  Revision 1.1  2000/12/07 23:02:19  dennis
 *  Class to hold and transfer viewer options between various viewers.
 *
 */

package DataSetTools.viewer;

import  java.io.*;
import  java.util.*;
import  DataSetTools.components.image.*;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;

/**
 *  A ViewerState object preserves the state for a DataSetViewer so
 *  that the view manager can switch from and back to a viewer without losing
 *  the preferences that a user set.
 *
 *  @see DataSetTools.viewer.ViewManager
 *  @see DataSetTools.viewer.DataSetViewer
 */ 

public class ViewerState  implements Serializable
{
  public static final int    ERROR_INT         = Integer.MIN_VALUE;

  public static final String COLOR_SCALE       = "ColorScale";
  public static final String REBIN             = "RebinFlag";
  public static final String H_SCROLL          = "HScrollFlag";
  public static final String H_SCROLL_POSITION = "HScrollPosition";
  public static final String V_SCROLL_POSITION = "VScrollPosition";
  public static final String POINTED_AT_INDEX  = "PointedAtIndex";
  public static final String POINTED_AT_X      = "PointedAtX";
  public static final String V_AZIMUTH         = "ViewAzimuthAngle";
  public static final String V_ALTITUDE        = "ViewAltitudeAngle";
  public static final String V_DISTANCE        = "ViewDistance";
  public static final String V_GROUPS          = "ViewGroups";
  public static final String V_DETECTORS       = "ViewDetectors";
  public static final String BRIGHTNESS        = "Brightness";
  public static final String AUTO_SCALE        = "Auto-Scale";
  public static final String TABLE_DATA        = "table_view Data";
   /** CONTOUR_STYLE(really "Contour.Style") is an int whose values can be
   *          AREA_FILL(1), AREA_FILL_CONTOUR(4) ,CONTOUR(2) ,RASTER(0), 
   *          or RASTER_CONTOUR(3) 
   */
  public static final String CONTOUR_STYLE     =  "Contour.Style";

  public static final String TABLE_TS          ="Time Slice Table";

  private Hashtable     state = null;
  private int           pointed_at_index;

  private CoordBounds   zoom_region;                // the image zoom region
  private String        ds_x_label;                 // should only be restored
  private String        ds_y_label;                 // if the DataSet has the
  private String        ds_x_units;                 // same units and labels
  private String        ds_y_units; 
  private String        ds_name;                    // and the names match
  private int           ds_n_rows;                  // and have the same #rows

    /** 
     * Constructs a ViewerState object with default values for the
     * various state fields.  
     */
    public ViewerState( )
    {
      state = new Hashtable();
                                                    // initialize state from
                                                    // IsawProps.dat file, if
                                                    // possible.
      SharedData sd = new SharedData();            
      SharedData.isaw_props.reload();
                                                       // color scale ......
      String scale_name = SharedData.getProperty( COLOR_SCALE ); 
      state.put( COLOR_SCALE, scale_name );

                                                       // rebin ......
      Boolean rebin_flag = SharedData.getBooleanProperty( REBIN );
      state.put( REBIN, rebin_flag );

                                                       // h_scroll ......
      Boolean scroll_flag = SharedData.getBooleanProperty( H_SCROLL );
      state.put( H_SCROLL, scroll_flag );

      Float h_scroll_position = 
          SharedData.getFloatProperty( H_SCROLL_POSITION );
      state.put( H_SCROLL_POSITION, h_scroll_position );  

      Integer pointed_at_index = 
          SharedData.getIntegerProperty( POINTED_AT_INDEX );
      state.put( POINTED_AT_INDEX, pointed_at_index );

      Float azimuth = SharedData.getFloatProperty( V_AZIMUTH );
      state.put( V_AZIMUTH, azimuth ); 

      Float altitude = SharedData.getFloatProperty( V_ALTITUDE );
      state.put( V_ALTITUDE, altitude );

      Float distance = SharedData.getFloatProperty( V_DISTANCE );
      state.put( V_DISTANCE, distance );

      String v_detectors = SharedData.getProperty( V_DETECTORS );
      state.put( V_DETECTORS, v_detectors );

      String v_groups = SharedData.getProperty( V_GROUPS );
      state.put( V_GROUPS, v_groups );

      Integer brightness = SharedData.getIntegerProperty( BRIGHTNESS );
      state.put( BRIGHTNESS, brightness );

      Float auto_scale = SharedData.getFloatProperty( AUTO_SCALE );
      state.put( AUTO_SCALE, auto_scale );

      state.put( TABLE_DATA, "");
      Integer contour_style=SharedData.getIntegerProperty(CONTOUR_STYLE) ;
      state.put(CONTOUR_STYLE, contour_style );

      state.put( TABLE_TS, "");

      zoom_region                = new CoordBounds( 0, 1000, 0, 1000 );
      ds_x_label = "";
      ds_y_label = "";
      ds_x_units = "";
      ds_y_units = "";
    }

  /* ---------------------------- set_String --------------------------- */
  /**
   * Set the named String entry in this ViewerState object.
   *
   * @param  name   The name of the String state entry to set.
   * @param  string The value of the String entry to set.
   */
   public void set_String( String name, String string  )
   {
     state.put( name, string );
   }

  /* ---------------------------- get_String --------------------------- */
  /**
   * Get the named string entry from this ViewerState object.
   *
   * @param  name  The name of the string state entry to get.
   *
   * @eturn  The string value of the named entry as is set in this 
   *         ViewerState object.  If the named entry does not exist, 
   *         a blank string is returned.
   */
   public String get_String( String name )
   {
     Object value = state.get( name );
     if ( value == null )
     {
        System.out.println("ERROR: in ViewerState.get_string " +
                            name + " NOT FOUND" );
        return "";
     }

     if ( value instanceof String )
       return (String)value;

     System.out.println("ERROR: in ViewerState.get_string " +
                         name + " ENTRY IS NOT A STRING" );
     return "";
   }


  /* ---------------------------- set_boolean --------------------------- */
  /**
   * Set the named boolean entry in this ViewerState object.
   *
   * @param  name  The name of the boolean state entry to set.
   * @param  flag  The value, true or false of the boolean state entry to set.
   */
   public void set_boolean( String name, boolean flag )
   {
     state.put( name, new Boolean( flag ));
   }


  /* ---------------------------- get_boolean --------------------------- */
  /**
   * Get the named boolean entry from this ViewerState object.
   *
   * @param  name  The name of the boolean state entry to get.
   *
   * @eturn The value, true or false of the boolean state entry
   *        as is set in this ViewerState object.  If the named
   *        entry does not exist, false is returned.
   */
   public boolean get_boolean( String name )
   {
     Object  value = state.get( name );
     if ( value == null )
     {
        System.out.println("ERROR: in ViewerState.get_boolean " + 
                            name + " NOT FOUND");
        return false;
     }

     if ( value instanceof Boolean )
       return ((Boolean)value).booleanValue();

     System.out.println("ERROR: in ViewerState.get_boolean " +
                         name + " ENTRY IS NOT A BOOLEAN" );
     return false;
   }



  /* ------------------------------ set_int ----------------------------- */
  /**
   * Set the named int entry in this ViewerState object.
   *
   * @param  name  The name of the int state entry to set.
   * @param  i_val The value of the int state entry to set.
   */
   public void set_int( String name, int i_val )
   {
     state.put( name, new Integer( i_val ));
   }


  /* ------------------------------- get_int ----------------------------- */
  /**
   * Get the named int entry from this ViewerState object.
   *
   * @param  name  The name of the int state entry to get.
   *
   * @eturn The value of the named int state entry as is set in this 
   *        ViewerState object.  If the named entry does not exist
   *        ERROR_INT is returned.
   */
   public int get_int( String name )
   {
     Object  value = state.get( name );
     if ( value == null )
     {
        System.out.println("ERROR: in ViewerState.get_int " +
                            name + " NOT FOUND");
        return ERROR_INT;
     }

     if ( value instanceof Integer )
       return ((Integer)value).intValue();

     System.out.println("ERROR: in ViewerState.get_int " +
                         name + " ENTRY IS NOT AN INTEGER" );
     return ERROR_INT;
   }


  /* ------------------------------ set_float --------------------------- */
  /**
   * Set the named float entry in this ViewerState object.
   *
   * @param  name  The name of the float state entry to set.
   * @param  f_val The value of the float state entry to set.
   */
   public void set_float( String name, float f_val )
   {
     state.put( name, new Float( f_val ));
   }


  /* ------------------------------- get_float ----------------------------- */
  /**
   * Get the named float entry from this ViewerState object.
   *
   * @param  name  The name of the float state entry to get.
   *
   * @eturn The value of the named float state entry as is set in this
   *        ViewerState object.  If the named entry does not exist
   *        Float.NaN is returned.
   */
   public float get_float( String name )
   {
     Object  value = state.get( name );
     if ( value == null )
     {
        System.out.println("ERROR: in ViewerState.get_float " +
                            name + " NOT FOUND");
        return Float.NaN;
     }

     if ( value instanceof Float )
       return ((Float)value).floatValue();

     System.out.println("ERROR: in ViewerState.get_float " +
                         name + " ENTRY IS NOT A FLOAT" );
     return Float.NaN;
   }


   /**
    *  Get the last zoom region that was saved.
    *
    *  @param  ds     The current DataSet being viewed.  The zoom region should
    *                 only be restored provided the new DataSet given to the
    *                 viewer is using the same units and axis labels, has
    *                 the same basic title ( eg. for IPNS runfiles this is
    *                 the instrument name and run number ) and has the same
    *                 number of rows.  The title, axis labels and units and 
    *                 number of rows of this DataSet are compared to 
    *                 those of the old DataSet that was passed to the 
    *                 setZoomRegion() method. 
    *
    *  @return  The last saved zoom region, provided the current DataSet has
    *           the same name, axis labels units and number of rows as the 
    *           previous DataSet for which the zoom region was saved.  If the 
    *           values don't match, this method returns null.
    */
   public CoordBounds getZoomRegion( DataSet ds )
   {
     boolean units_match = false;
     if ( ds_x_label.equalsIgnoreCase( ds.getX_label() )   &&
          ds_y_label.equalsIgnoreCase( ds.getY_label() )   &&
          ds_x_units.equalsIgnoreCase( ds.getX_units() )   &&
          ds_y_units.equalsIgnoreCase( ds.getY_units() )    )        
       units_match = true;

     if ( !units_match )
       return null;

     if ( ds.getNum_entries() != ds_n_rows )
       return null;

     if ( ds_name.equals( ds.getTitle() ) )
       return zoom_region;
     else
       return null;
   }

   /**
    *  Save the specified zoom region.
    *
    *  @param  bounds Zoom region to be saved.
    *  @param  ds     The current DataSet being viewed.  The zoom region should
    *                 only be restored provided the new DataSet given to the
    *                 viewer is using the same units and axis labels.  The
    *                 axis labels and units of this DataSet are saved and 
    *                 compared to those of the current DataSet by the 
    *                 getZoomRegion() method. 
    */
   public void setZoomRegion( CoordBounds zoom_region, DataSet ds )
   {
      this.zoom_region = (CoordBounds)(zoom_region.clone());
      ds_x_label = ds.getX_label();
      ds_y_label = ds.getY_label();
      ds_x_units = ds.getX_units();
      ds_y_units = ds.getY_units();
      ds_name    = ds.getTitle();
      ds_n_rows  = ds.getNum_entries();
   }
}
