package DataSetTools.components.ui;

import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.ViewTools.Panels.ThreeD.*;




public interface IMotion3D extends IThreeD_Panel
{
/**
 * A item in the 3D view is selected
 * @param id   The id of the item selected
 */
   public void SetSelectID( int id);
   
   
   /**
    * The user desires to rotate the current display. 
    * Assume |r| is a constant
    * 
    * @param dx  pixels in x direction of rotation
    * @param dy  pixels in y direction of rotation 
    */
   public void setRotate( float dx, float dy);
   
   /**
    * Zooms display toward(up <1) or away(up >1) from center
    * @param up  The "amount" to zoom.  
    * 
    * Note: Area(zoom) = up* Area(original) where Area means Area
    *  in display units that are visible.
    */
   public void setZoomRelative( float up);
   
   
   /**
    * Sets the new center on 2D display of 3D view and moves this point
    * to the center of the 2D display.
    * 
    * @param right   pixels to the right of the center of the display
    * @param up      pixels above the center of the display
    * @param infront pixels in front of the 2D display
    * @param relative right,up, infront are relative to the center 
    *                 of the display not absolute pixels.
    */
   public void setCenter( float right, float up, float infront ,boolean relative);
   
   
   /**
    * Returns a z coordinate for the selected object at PixelX,PixelY
    * @return  z coordinate for the selected object at PixelX,PixelY
    */
   public int  getSelPixZ( int PixelX, int PixelY) ;
}
