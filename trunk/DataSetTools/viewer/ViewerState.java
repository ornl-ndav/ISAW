/*
 * @(#)ViewerState.java
 *
 *  Programmer: Dennis Mikkelson
 *
 *    Class to preserve state information for DataSetViewers.  This will allow
 *  switching between viewers without losing all of the preferences that were
 *  set in a particular viewer.
 *
 *  $Log$
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
  private CoordBounds   zoom_region;
 
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
    *  @return  The last saved zoom region.
    */
   public CoordBounds getZoomRegion()
   {
     return zoom_region;
   }

   /**
    *  Save the specified zoom region.
    *
    *  @param  bounds Zoom region to be saved.
    */
   public void setZoomRegion( CoordBounds zoom_region )
   {
      this.zoom_region = (CoordBounds)(zoom_region.clone());
   }

}
