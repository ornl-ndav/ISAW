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
  private String   color_scale;
  private boolean  horizontal_scrolling;
  private float    horizontal_scroll_fraction;

    /** 
     * Constructs a ViewerState object with default values for the
     * various state fields.  
     */
    public ViewerState( )
    {
      color_scale                = IndexColorMaker.HEATED_OBJECT_SCALE;
      horizontal_scrolling       = false;
      horizontal_scroll_fraction = 0.5f;
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


}
