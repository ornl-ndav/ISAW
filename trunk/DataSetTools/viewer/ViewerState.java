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
 *  Revision 1.34  2004/06/15 20:58:14  robertsonj
 *  Took out debugging system.out statements
 *
 *  Revision 1.33  2004/06/15 20:57:03  robertsonj
 *  Changed the setViewerState method so you may or maynot have spaces
 *  when using the viewerStates in a script
 *
 *  Revision 1.32  2004/06/15 20:20:20  robertsonj
 *  add setViewerState method used to set the viewer state from the 
 *  script language and from a StringPG
 *
 * Revision 1.32 2004/06/14 robertson
 * Added setViewerState(String) to allow stringPG's to be used to change the viewer state.
 * 
 *  Revision 1.31  2004/05/03 16:25:19  dennis
 *  Removed unused local variables.
 *
 *  Revision 1.30  2004/03/19 17:18:44  dennis
 *  Removed unused variables
 *
 *  Revision 1.29  2004/03/15 06:10:55  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.28  2004/03/15 03:28:59  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.27  2003/12/15 18:22:53  rmikk
 *  Added several more Contour view states
 *
 *  Revision 1.26  2003/12/15 00:37:39  rmikk
 *  Added a state for the Contour View to remember the new button, ShowAll in contour view
 *
 *  Revision 1.25  2003/10/28 16:19:10  rmikk
 *  Fixed some spelliing errors in state names
 *
 *  Revision 1.24  2003/10/28 15:50:12  rmikk
 *  Added new State Strings for the Table Views
 *
 *  Revision 1.23  2003/10/15 03:56:40  bouzekc
 *  Fixed javadoc errors.
 *
 *  Revision 1.22  2002/11/27 23:24:18  pfpeterson
 *  standardized header
 *
 *  Revision 1.21  2002/07/30 14:33:35  rmikk
 *  Added a ViewerState constant for the Contour View
 *
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
 */

package DataSetTools.viewer;

import gov.anl.ipns.ViewTools.Panels.Transforms.*;

import  java.io.*;
import  java.util.*;
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
  public static final String TIMEVSGROUPTABLE          = "Time vs Group Set";
  public static final String TIMEVSGROUPTABLE_SHOWALL     = "Time vs Group Show All";
    
  public static final String TIMEVSGROUPTABLE_SHOWERR = "Show Errors(Table time vs Group)";
  
  public static final String TIMEVSGROUPTABLE_SHOWIND  = "Show Indicies(Table time vs Group)";
 
   /** CONTOUR_STYLE(really "Contour.Style") is an int whose values can be
   *          AREA_FILL(1), AREA_FILL_CONTOUR(4) ,CONTOUR(2) ,RASTER(0), 
   *          or RASTER_CONTOUR(3) 
   */
  public static final String CONTOUR_STYLE     =  "Contour.Style";

  /**if false data has not been set
  */
  public static final String CONTOUR_DATA      =  "Contour.Data";
  public static final String CONTOUR_SHOWALL   ="Show All Groups";
  public static final String CONTOUR_DETNUM   ="Detector Number";
  public static final String CONTOUR_COLOR_SCALE   ="Contour Color Scale";
  public static final String TABLE_TS          ="Time Slice Table Data Set";
  public static final String TABLE_TS_ERR          ="TableTS_ShowError";
  public static final String TABLE_TS_IND          ="TableTS_ShowIndex";
  public static final String TABLE_TS_ROWMIN          ="TableTS_MinRow";
  public static final String TABLE_TS_ROWMAX          ="TableTS_MaxRow";
  public static final String TABLE_TS_COLMIN          ="TableTS_MinCol";
  public static final String TABLE_TS_COLMAX          ="TableTS_MaxCol";
  public static final String TABLE_TS_TIMEMIN          ="TABLE_TS_MIN_TIME";
  public static final String TABLE_TS_TIMEMAX          ="TABLE_TS_MAX_TIME";
  public static final String TABLE_TS_NSTEPS          ="TABLE_TS_NXSTEPS";
  public static final String TABLE_TS_CHAN          ="TableTS_TimeInd";
  public static final String TABLE_TS_DETNUM          ="TableTS_Detector Num";

  private Hashtable     state = null;

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
      SharedData.isaw_props.reload();
                                                       // color scale ......
      String scale_name = SharedData.getProperty( COLOR_SCALE ); 
      state.put( COLOR_SCALE, scale_name );
      state.put( CONTOUR_COLOR_SCALE , scale_name);
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

      state.put(CONTOUR_SHOWALL, new Boolean(true));
      state.put(CONTOUR_DETNUM, new Integer(-1));

      state.put( TABLE_TS, "");
      state.put( TABLE_TS_ERR , new Boolean( false)); 
      state.put( TABLE_TS_IND , new Boolean( false));
      state.put( TABLE_TS_ROWMIN , new Integer( -1));
      state.put( TABLE_TS_ROWMAX , new Integer( -1));
      state.put( TABLE_TS_COLMIN , new Integer( -1));
      state.put( TABLE_TS_COLMAX, new Integer( -1));
      state.put( TABLE_TS_TIMEMIN, new Float(0f));   
      state.put( TABLE_TS_TIMEMAX, new Float(0f)); 
      state.put( TABLE_TS_NSTEPS , new Integer( -1));
      state.put( TABLE_TS_CHAN, new Integer( -1));
      state.put( TABLE_TS_DETNUM , new Integer( -1));

      state.put( CONTOUR_DATA, new Boolean(false));

      state.put(TIMEVSGROUPTABLE, new Boolean( false));
      state.put(TIMEVSGROUPTABLE_SHOWALL , new Boolean( false));

      state.put( ViewerState.TIMEVSGROUPTABLE_SHOWERR, new Boolean(false));  
      state.put( ViewerState.TIMEVSGROUPTABLE_SHOWIND, new Boolean(false));

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
   * @return  The string value of the named entry as is set in this 
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
   * @return The value, true or false of the boolean state entry
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
   * @return The value of the named int state entry as is set in this 
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
   * @return The value of the named float state entry as is set in this
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
    *  @param  zoom_region Zoom region to be saved.
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
   /**
    * 
    * @param stateVariables String that holds your state information int the form of "Name Value,Name value,....,Name Value". 
    * Where the name and the value are both strings. 
    * 		The valid strings value pairs are:
    * 		ColorScale								Strings - ie. Heat1 or Rainbow
    * 		RebinFlag								Boolean
    * 		Brightness								int between 0 and 100
    * 		HScrollPosition							float between 0 and 1
    * 		PointedAtIndex							Positive integer, # of spectra
    * 		PointedAtX								float corresponding to x values
    * 		ViewAzimuthAngle						float, angle in degrees
    * 		ViewAltitudeAngle						float, angle in degrees
    * 		ViewDistance							float, distance in meters
    * 		ViewGroups								String 
    * 		ViewDetectors							String
    * 		Auto-Scale								float between 0 and 100
    * 		table_view Data							String
    * 		Contour.Style							String
    * 		ContourTimeMin							float min time
    * 		Time Slice Table Data Set				
    * 		TableTS_TimeInd							int pionted at time channel or slice channel
    * 		TableTS_MinRow							int min row to include
    * 		TableTS_MaxRow							int max row to include
    * 		TableTS_MinCol							int min column to include
    * 		TableTS_MaxCol							int max column to include
    * 		TABLE_TS_MIN_TIME						float min time to include
    * 		TABLE_TS_MAX_TIME						float max time to include
    * 		TABLE_TS_NXSTEPS						float # of time sterps for Xscale
    * @return The updated viewer state that can be used to change state information of a viewer.
    */
   public ViewerState setViewerState(String stateVariables)
   {
   	
   		String stateString = stateVariables.substring(21, (stateVariables.length() - 6)); //gets the substring that I want
    	String[] seperatedStates = stateString.split(",");
    	System.out.println("length of array "+seperatedStates.length);
    	String tempString;
	   	for(int i = 0; i <seperatedStates.length; i++)
	   	{
	   		tempString = seperatedStates[i].trim();
	   	
	   		if(tempString.startsWith("ColorScale")){
	   		
	   			set_String("ColorScale", tempString.substring(tempString.lastIndexOf(" ")+1));
	   		}else if(tempString.startsWith("RebinFlag")){
	   			if(tempString.endsWith("ue")){
	   				set_boolean("RebinFlag", true);
	   			}else
	   			{set_boolean("RebinFlag", false);
	   			}
	   		}else if(tempString.startsWith("Brightness")){
	   			set_int("Brightness", Integer.parseInt(tempString.substring(11).trim()));
	   		}else if(tempString.startsWith("HScrollPosition")){
	   			set_float("HScrollPosition", Float.parseFloat(tempString.substring(16).trim()));
	   		}else if(tempString.startsWith("PointedAtIndex")){
	   			set_int("PointedAtIndex", Integer.parseInt(tempString.substring(15).trim()));
	   		}else if(tempString.startsWith("ViewAzimuthAngle")){
	   			set_float("ViewAzimuthAngle", Float.parseFloat(tempString.substring(16).trim()));
	   		}else if(tempString.startsWith("ViewAltitudeAngle")){
	   			set_float("ViewAltitudeAngle", Float.parseFloat(tempString.substring(17).trim()));
	   		}else if(tempString.startsWith("ViewDistance")){
	   			set_float("ViewDistance", Float.parseFloat(tempString.substring(13).trim()));
	   		}else if(tempString.startsWith("ViewGroups")){
	   			set_String("ViewGroups", tempString.substring(11));
	   		}else if(tempString.startsWith("ViewDetectors")){
	   			set_String("ViewDetectors", tempString.substring(14));
	   		}else if(tempString.startsWith("Auto-Scale")){
	   			set_float("Auto-Scale", Float.parseFloat(tempString.substring(11).trim()));
	   		}else if(tempString.startsWith("table_view Data")){
	   			set_String("table_view Data", tempString.substring(16));
	   		}else if(tempString.startsWith("Contour.Style")){
	   			set_int("Contour.Style", Integer.parseInt(tempString.substring(14).trim()));
	   		}else if(tempString.startsWith("ContourTimeMin")){
				set_float("ContourTimeMin", Float.parseFloat(tempString.substring(15).trim()));	
	   		}else if(tempString.startsWith("Time Slice Table Data Set")){
	   			set_String("Time Slice Table Data Set", tempString.substring(26));
	   		}else if(tempString.startsWith("TableTS_TimeInd")){
	   			set_int("TableTS_TimeInd", Integer.parseInt(tempString.substring(15).trim()));
	   		}else if(tempString.startsWith("TableTS_MinRow")){
	   			set_int("TableTS_MinRow", Integer.parseInt(tempString.substring(15).trim()));
	   		}else if(tempString.startsWith("TableTS_MaxRow")){
	   			set_int("TableTS_MaxRow", Integer.parseInt(tempString.substring(15).trim()));
	   		}else if(tempString.startsWith("TableTS_MinCol")){
	   			set_int("TableTS_MinCol", Integer.parseInt(tempString.substring(15).trim()));
	   		}else if(tempString.startsWith("TableTS_MaxCol")){
	   			set_int("TableTS_MaxCol", Integer.parseInt(tempString.substring(15).trim()));	
	   		}else if(tempString.startsWith("TABLE_TS_MIN_TIME")){
	   			set_float("TABLE_TS_MIN_TIME", Float.parseFloat(tempString.substring(18)));
	   		}else if(tempString.startsWith("TABLE_TS_MAX_TIME")){
	   			set_float("TABLE_TS_MAX_TIME", Float.parseFloat(tempString.substring(18)));
	   		}else if(tempString.startsWith("TABLE_TS_NXSTEPS")){
	   			set_int("TABLE_TS_NXSTEPS", Integer.parseInt(tempString.substring(17)));
	   		}
	   		
	   	}
	return this;
   	}
   	
}

