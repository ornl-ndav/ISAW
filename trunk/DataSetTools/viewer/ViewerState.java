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
import  DataSetTools.components.image.*;
import  DataSetTools.dataset.*;

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
  private String        color_scale;
  private boolean       horizontal_scrolling;
  private float         horizontal_scroll_fraction;
  private int           pointed_at_index;

  private CoordBounds   zoom_region;                // the image zoom region
  private String        ds_x_label;                 // should only be restored
  private String        ds_y_label;                 // if the DataSet has the
  private String        ds_x_units;                 // same units and labels
  private String        ds_y_units; 

    /** 
     * Constructs a ViewerState object with default values for the
     * various state fields.  
     */
    public ViewerState( )
    {
      color_scale                = IndexColorMaker.HEATED_OBJECT_SCALE;
      horizontal_scrolling       = false;
      horizontal_scroll_fraction = 0.5f;
      pointed_at_index           = 0;
      zoom_region                = new CoordBounds( 0, 1000, 0, 1000 );
      ds_x_label = "";
      ds_y_label = "";
    }

   /**
    *  Get the name of the color scale to use
    *
    *  @return  The name of the color scale to use.  This will be one of
    *           the color scales supported by the IndexColorMaker class.
    *
    *  @see DataSetTools.components.image.IndexColorMaker
    */
   public String getColor_scale()
   {
     return color_scale;
   }

   /**
    *  Set the name of the color scale to use.
    *
    *  @param  scale_name  This should be one of the color scale names
    *                      supported by the IndexColorMaker class
    *
    *  @see DataSetTools.components.image.IndexColorMaker
    */
   public void setColor_scale( String scale_name )
   {
     color_scale = scale_name;
   }

   /**
    *  Get the state of the horizontal scrolling flag. 
    *
    *  @return  the value of the horizontal scrolling flag.
    */
   public boolean getHorizontal_scrolling()
   {
     return horizontal_scrolling;
   }

   /**
    *  Set the state of the horizontal scrolling flag.
    *
    *  @param  horizontal_scrolling  flag indicating whether or not horizontal
    *                                scrolling should be used. 
    */
   public void setHorizontal_scrolling( boolean horizontal_scrolling )
   {
     this.horizontal_scrolling = horizontal_scrolling;
   }

   /**
    *  Get the relative position of the horizontal scrolling bar.
    *
    *  @return  A value between 0.0 and 1.0 giving the relative position
    *           of the horizontal scroll bar.
    */
   public float getHorizontal_scroll_fraction()
   {
     return horizontal_scroll_fraction;
   }

   /**
    *  Set the relative position of the horizontal scrolling bar.
    *
    *  @param  position  A value between 0.0 and 1.0 giving the relative 
    *                    position of the horizontal scroll bar.
    */
   public void setHorizontal_scroll_fraction( float position )
   {
     this.horizontal_scroll_fraction = position;
   }


   /**
    *  Get the last "POINTED AT" index that was saved.
    *
    *  @return  The last saved "POINTED AT" index.
    */
   public int getPointedAtIndex()
   {
     return pointed_at_index;
   }

   /**
    *  Save the specified "POINTED AT" index.
    *
    *  @param  index  The "POINTED AT" index to be saved. 
    */
   public void setPointedAtIndex( int index )
   {
      pointed_at_index = index;
   }

   /**
    *  Get the last zoom region that was saved.
    *
    *  @param  ds     The current DataSet being viewed.  The zoom region should
    *                 only be restored provided the new DataSet given to the
    *                 viewer is using the same units and axis labels.  The
    *                 axis labels and units of this DataSet are compared to 
    *                 those of the old DataSet that was passed to the 
    *                 setZoomRegion() method. 
    *
    *  @return  The last saved zoom region, provided the current DataSet has
    *           the same axis labels as the previous DataSet for which the
    *           zoom region was saved.  If the labels don't match, this 
    *           method returns null.
    */
   public CoordBounds getZoomRegion( DataSet ds )
   {
     if ( ds_x_label.equalsIgnoreCase( ds.getX_label() )   &&
          ds_y_label.equalsIgnoreCase( ds.getY_label() )   &&
          ds_x_units.equalsIgnoreCase( ds.getX_units() )   &&
          ds_y_units.equalsIgnoreCase( ds.getY_units() )    )        
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
   }

}
