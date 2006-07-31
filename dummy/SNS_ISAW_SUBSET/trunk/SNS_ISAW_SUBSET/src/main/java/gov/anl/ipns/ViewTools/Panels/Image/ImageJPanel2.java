/*
 * File:  ImageJPanel2.java
 *
 * Copyright (C) 1999-2003, Dennis Mikkelson
 *               2005     , Mike Miller
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
 *  $Log: ImageJPanel2.java,v $
 *  Revision 1.3  2005/08/14 05:22:11  dennis
 *  Added public method RebuildImage() to force the visibile image
 *  to be recalculated, and call repaint().  This is needed for
 *  setting the image scrolling is done with a separate scroll bar.
 *
 *  Revision 1.2  2005/06/17 20:10:35  kramer
 *
 *  Modified the getThumbnail() method by adding a 'forceRedraw' parameter
 *  that can force the redraw of the thumbnail.  This fixes the problem of
 *  the thumbnail not changing after the color scale changes on the big
 *  image.  This procedure works but a more efficient solution should be
 *  found.
 *
 *  Revision 1.1  2005/03/07 16:58:42  millermi
 *  - New Version of ImageJPanel based on IVirtualArray2D. This version
 *    eliminates image size limitations presented by Java.
 *  - Added methods enableAutoDataRange(), isAutoDataRangeEnabled(),
 *    and setDataRange() to allow user to set colorscale mapping to
 *    a specified data range.
 *  - Revised getThumbnail() so makeImage() does not have to be called
 *    so many times, since it is an expensive operation.
 *  - Added super.paint(g) to beginning of paint().
 *  - Revised makeImage() so that subsampling of data occurs before generating
 *    an image.
 *  - Added private method subSample() to subsample data.
 *
 *
 *********************** Log from ImageJPanel.java *************************
 *  Revision 1.29  2004/11/12 17:26:07  millermi
 *  - Code in setLogScale() was factored out into PseudoLogScaleUtil.
 *    setLogScale() now used PseudoLogScaleUtil to do log mapping.
 *
 *  Revision 1.28  2004/05/03 18:09:41  dennis
 *  Removed unused constant NUM_PSEUDO_COLORS.
 *
 *  Revision 1.27  2004/03/19 17:24:27  dennis
 *  Removed unused variables
 *
 *  Revision 1.26  2004/03/15 23:53:55  dennis
 *  Removed unused imports, after factoring out the View components,
 *  Math and other utils.
 *
 *  Revision 1.25  2004/03/12 01:47:00  dennis
 *  Moved to ViewTools.Panels.Image package
 *
 *  Revision 1.24  2004/02/12 21:53:10  millermi
 *  - Added method getImageCoords() which returns the image bounds.
 *
 *  Revision 1.23  2004/01/29 23:26:41  millermi
 *  - Two-sided no longer ignored for default state.
 *
 *  Revision 1.22  2004/01/29 08:18:14  millermi
 *  - Updated the getObjectState() to include parameter for specifying
 *    default state.
 *  - Added static variables DEFAULT and PROJECT to IPreserveState for
 *    use by getObjectState()
 *
 *  Revision 1.21  2003/12/23 20:59:15  millermi
 *  - Fixed bug introduced in makeImage() that restricted temp > 0,
 *    now changed to temp > -(LOG_TABLE_SIZE-1) so negative values
 *    also show.
 *
 *  Revision 1.20  2003/11/21 00:39:12  millermi
 *  - Added method getThumbnail() to get a replica of the
 *    image displayed by the ImageJPanel
 *  - *** makeImage() should be made more generic so the image
 *    can be set to more than just the current zoomed in region.
 *    Once this is done, the getThumbnail() should be editted
 *    to make use of this generality. ***
 *
 *  Revision 1.19  2003/11/18 00:56:19  millermi
 *  - ObjectState for IndexColorModel now saved as a string name,
 *    since IndexColorModel isn't serializable.
 *  - Made color_model transient.
 *  - Line 671, added check to make sure temp is a valid index.
 *
 *  Revision 1.18  2003/10/23 05:44:14  millermi
 *  - Added getObjectState() and setObjectState() methods to
 *    allow for preservation of state.
 *  - Now implements IPreserveState interface
 *  - Added public static Strings as keys for accessing state
 *    information.
 *
 *  Revision 1.17  2003/10/22 20:26:47  millermi
 *  - Fixed java doc error.
 *
 *  Revision 1.16  2003/07/10 13:37:05  dennis
 *  - Added some functionality to main() for testing purposes
 *  - Added isTwoSided as private data member to be used by the makeImage()
 *  - Now supports either one-sided or two sided color scales.
 *    (The pix array in makeImage() is now filled using zero_index in
 *     place of ZERO_COLOR_INDEX.  If the color model is two-sided,
 *     zero_index = ZERO_COLOR_INDEX, else zero_index = 0.)
 *  (Mike Miller)
 *
 *  Revision 1.15  2003/07/05 19:15:37  dennis
 *  Added methods to get min and max of data.  Added parameter to the
 *  setNamedColorMode() method to control whether the color scale is
 *  suitable for images with positive data or with both positive and
 *  negative data. (Mike Miller)
 *  Merged with previous changes.
 *
 *  Revision 1.14  2003/04/18 15:20:42  dennis
 *  Vertical scrolling is no longer automatically set true.
 *
 *  Revision 1.13  2003/02/25 22:28:12  dennis
 *  Added java docs.  Set data method now rejects ragged arrays, or
 *  degenerate or empty arrays.
 *
 *  Revision 1.12  2002/11/27 23:13:18  pfpeterson
 *  standardized header
 *
 *  Revision 1.11  2002/07/17 15:18:00  dennis
 *  Now sets valid default for the color model.
 *
 *  Revision 1.10  2002/07/15 16:55:11  pfpeterson
 *  No longer sets its own default value.
 *
 *  Revision 1.9  2002/06/19 22:43:24  dennis
 *  Added some additional checks to keep row,col values in range.
 */

package gov.anl.ipns.ViewTools.Panels.Image;

import java.awt.*;
import java.io.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Panels.Transforms.*;
import gov.anl.ipns.Util.Numeric.*;

/**
 *    This class displays two dimensional arrays of floating point values as 
 *  pseudo-color images.  The pseudo-color scale may be specified to be 
 *  any of the named color scales from the IndexColorMaker class.  The
 *  range of values in the array is calculated and scaled logarithmically
 *  before being mapped to the pseudo-color scale.  IF the array contains
 *  negative values, one of the "dual" color models from the IndexColorMaker
 *  should be used.  The shape of the log function used for the scaling
 *  can be adjusted to control the apparent brightness of the image. 
 *    A "world coordinate" system can be applied to the image to map between
 *  row and column indices and "world coordinate" (x,y) values.  In addition
 *  cursor position information and zoom in/out is provided by methods from
 *  the base class, CoordJPanel.  Alternatively, methods to convert between
 *  image row and column values and pixel or world coordinate values are
 *  also provided.
 *
 *  @see CoordJPanel 
 *  @see IndexColorMaker
 */

public class ImageJPanel2 extends    CoordJPanel 
                         implements Serializable, IPreserveState
{
 // these variables preserve state for the ImageJPanel
 /**
  * "Color Model" - This constant String is a key for referencing the state
  * information about the IndexColorModel used by the ImageJPanel. The value
  * referenced by this key is a String name constant from IndexColorMaker.java.
  */
  public static final String COLOR_MODEL = "Color Model";
  
 /**
  * "Log Scale" - This constant String is a key for referencing the state
  * information about the log scale table used to map data values to colors
  * within the colorscale. The value referenced by this key is an array of
  * bytes.
  */
  public static final String LOG_SCALE   = "Log Scale";
  
 /**
  * "Two Sided" - This constant String is a key for referencing the state
  * information about displaying the data. If one-sided, negative data will
  * be mapped to zero and a one-sided color model will be used, where as
  * two-sided data will remain negative and be mapped to colors on a two-sided
  * color model.
  */
  public static final String TWO_SIDED   = "Two Sided";
  
  private final int       LOG_TABLE_SIZE      = 60000;
  private final int       NUM_POSITIVE_COLORS = 127; 
  private final byte      ZERO_COLOR_INDEX    = (byte)NUM_POSITIVE_COLORS; 
  private Image           image;
  private Image           rescaled_image = null;
  private Image           thumbnail_image;
  private IVirtualArray2D data;
  private float           min_data = 0;
  private float           max_data = 3;

  private transient IndexColorModel color_model;
  private String          color_model_string;
  private byte[]          log_scale;
  private boolean         isTwoSided = true;
  private boolean         makeThumbnail = false;
  private boolean         auto_scale_data = true; // Initially allow auto scale.
/**
 *  Construct an ImageJPanel with default values for the color scale, log
 *  scaling factor and array of values.  Most applications using this class
 *  will at least have to use the setData() method to provide the actual
 *  values to be displayed.
 */
  public ImageJPanel2()
  { 
    float[][] temp = { {0f,1f}, {2f,3f} };
    data = new VirtualArray2D( temp );
    
    color_model_string = IndexColorMaker.HEATED_OBJECT_SCALE_2;
    color_model =
    IndexColorMaker.getDualColorModel( color_model_string,
          NUM_POSITIVE_COLORS );
 
    log_scale = new byte[LOG_TABLE_SIZE];
    setLogScale( 0 );
  
    CJP_handle_arrow_keys = false;
    addKeyListener( new ImageKeyAdapter() );
  }

 /**
  * This method will set the current state variables of the object to state
  * variables wrapped in the ObjectState passed in.
  *
  *  @param  new_state
  */
  public void setObjectState( ObjectState new_state )
  {
    // since ImageJPanel extends CoordJPanel, set those state variables first.
    super.setObjectState(new_state);
    boolean redraw = false;  // if any values are changed, repaint.
    Object temp = new_state.get(LOG_SCALE);
    if( temp != null )
    {
      log_scale = (byte[])temp;
      redraw = true;  
    }  
    
    temp = new_state.get(TWO_SIDED);
    if( temp != null )
    {
      isTwoSided = ((Boolean)temp).booleanValue();
      redraw = true;  
    }
    
    temp = new_state.get(COLOR_MODEL);
    if( temp != null )
    {
      color_model_string = (String)temp;
      setNamedColorModel( color_model_string, isTwoSided, true );
      redraw = true;  
    }
    
    // may need changing
    if( redraw )
      repaint();
   
  } 
 
 /**
  * This method will get the current values of the state variables for this
  * object. These variables will be wrapped in an ObjectState.
  *
  *  @param  isDefault Should selective state be returned, that used to store
  *		       user preferences common from project to project?
  *  @return if true, the default state containing user preferences,
  *	     if false, the entire state, suitable for project specific saves.
  */ 
  public ObjectState getObjectState( boolean isDefault )
  {
    //get ObjectState of CoordJPanel
    ObjectState state = super.getObjectState(isDefault);
    state.insert( COLOR_MODEL, color_model_string );
    state.insert( LOG_SCALE, log_scale );
    state.insert( TWO_SIDED, new Boolean(isTwoSided) );
    
    return state;
  }

/* ------------------------- changeLogScale -------------------------- */
/**
 *  Change the control parameter for the logarithmic scaling applied to
 *  the image values.  If the image has negative values, the logarithmic
 *  scaling is applied to the absolute value of the values.
 *
 *  @param   s             The control parameter, s, clamped to the range
 *                         [0,100].  If s is 0, the scale is essentially 
 *                         linear.  If s is 100, the relative intensity of 
 *                         small values is greatly increased so the image 
 *                         is lightened.
 *  @param   rebuild_image Flag to determine whether the displayed image is
 *                         rebuilt with the new log scale factor, or if
 *                         rebuilding the displayed image should be delayed
 *                         since other changes will also be made before
 *                         rebuilding the image.  A value of "true" will
 *                         cause the image to be rebuilt immediately.
 */
  public void changeLogScale( double s, boolean rebuild_image )
  {                                       
    setLogScale( s );
    if ( rebuild_image )
      makeImage();
  }

/* -------------------------- setNamedColorModel --------------------------- */
/**
 *  Change the color model to used for the image.  If the data has negative
 *  values, one of the "Dual" color models should be used.
 *
 *  @param   color_scale_name  Name of the new color scale to use for the
 *                             image.  Supported color scales are listed in
 *                             the IndexColorMaker class.
 *  @param   twosided          Flag that determines whether a color scale 
 *                             that includes colors for both positive and 
 *                             negative values is used, or if only positive
 *                             values are represented.
 *  @param   rebuild_image     Flag to determine whether the displayed image is
 *                             rebuilt with the new log scale factor, or if
 *                             rebuilding the displayed image should be delayed
 *                             since other changes will also be made before 
 *                             rebuilding the image.  A value of "true" will
 *                             cause the image to be rebuilt immediately.
 * 
 *  @see IndexColorMaker
 */
  public void setNamedColorModel( String   color_scale_name,
                                  boolean  twosided,
                                  boolean  rebuild_image   )
  {
    isTwoSided = twosided;
    color_model_string =  color_scale_name;
    if( isTwoSided )
      color_model = IndexColorMaker.getDualColorModel( color_model_string,
                                                       NUM_POSITIVE_COLORS );
    else
      color_model = IndexColorMaker.getColorModel( color_model_string,
                                                   NUM_POSITIVE_COLORS );
    if ( rebuild_image )
    {
      makeImage();
    }
  }
  
 /**
  * Determine whether the minimum and maximum data values are calculated
  * by the setData() [True] or explicitly set by setDataRange() [False].
  * Initially, auto data range calculation is on. Be sure to call this
  * method only after the data min/max have been set either by the
  * setData() or setDataRange() methods.
  *
  *  @param  auto_range_on If true, data range is calculated by setData()
  *                        and will dynamically change each time setData()
  *                        is called.
  *                        If false, data range must be set explicitly
  *                        using the setDataRange() method.
  */
  public void enableAutoDataRange( boolean auto_range_on )
  {
    auto_scale_data = auto_range_on;
  }
  
 /**
  * Determine whether the minimum and maximum data values are calculated
  * by the setData() [True] or explicitly set by setDataRange() [False].
  *
  *  @return True if calculated by setData().
  *          False if set explicitly by setDataRange().
  */
  public boolean isAutoDataRangeEnabled()
  {
    return auto_scale_data;
  }
  
 /**
  * This method will set the data range to [data_min,data_max]. Calling this
  * method will disable auto data range calculation done when setData() is
  * called.
  *
  *  @param  data_min The minimum data value mapped to the minimum color.
  *  @param  data_max The maximum data value to be mapped to the max color.
  */
  public void setDataRange( float data_min, float data_max )
  {
    // Make sure data_min < data_max
    if( data_min > data_max )
    {
      float swap = data_min;
      data_min = data_max;
      data_max = swap;
    }
    // Prevent data_min = data_max
    if( data_min == data_max )
      data_max = data_min + 1;
    // Set min/max_data
    min_data = data_min;
    max_data = data_max;
    // turn off auto data range calculation.
    enableAutoDataRange(false);
  }

/* ------------------------------- setData -------------------------------- */
/**
 *  Change the array of floats that is displayed by this ImageJPanel.
 *
 *  @param   a2d           IVirtualArray2D containing a 2 dimensional array. 
 *                         an image.
 *
 *  @param   rebuild_image Flag to determine whether the displayed image is
 *                         rebuilt with the new log scale factor, or if
 *                         rebuilding the displayed image should be delayed
 *                         since other changes will also be made before 
 *                         rebuilding the image.  A value of "true" will
 *                         cause the image to be rebuilt immediately.
 */

  public void setData( IVirtualArray2D a2d, boolean rebuild_image  )
  {
    // if nothing was passed in, do nothing.
    if ( a2d == null || a2d.getNumRows() <= 0 || a2d.getNumColumns() <= 0)
    {
      System.out.println("ERROR: empty virtual array in ImageJPanel.setData");
      return;
    }
    data = a2d;
    
    // If auto calculate data range, do the code contained in this if statement.
    if( isAutoDataRangeEnabled() )
    {
     /* ########################### Consider Revising #########################
      * The code below will subsample the data in order to take advantage of
      * the virtual array. The min and max data will be taken from this
      * subsampled region. As a result the min/max may not be accurate. The
      * values will then be compared with the min/max provided by the
      * VirtualArray's data axis.
      */
      SetTransformsToWindowSize();
      // Get world_to_image transform, and local world coord bounds.
      CoordTransform world_to_image = getWorldToImageTransform();
      CoordBounds    bounds         = local_transform.getSource();
      // Convert local coord bounds to image row/column.
      bounds = world_to_image.MapTo( bounds );
      int start_row = Math.max( (int)(bounds.getY1() ), 0 );
      int end_row   = Math.min( (int)(bounds.getY2() ), data.getNumRows()-1 );
      int start_col = Math.max( (int)(bounds.getX1() ), 0 );
      int end_col   = Math.min( (int)(bounds.getX2() ), data.getNumColumns()-1);

      CoordBounds new_bounds = new CoordBounds(start_col+.001f,start_row+0.001f,
                                               end_col+0.999f, end_row+0.999f );
      new_bounds = world_to_image.MapFrom( new_bounds );
      setLocalWorldCoords( new_bounds );
      // Subsamble data if data exceeds bounds of monitor.
      // Get monitor dimensions.
      Dimension monitor_dim = Toolkit.getDefaultToolkit().getScreenSize();
    
      int h = Math.abs(end_row - start_row) + 1;
      int w = Math.abs(end_col - start_col) + 1;
      // Subsample step increments. If image exceeds size
      // of monitor screen, increase steps for subsampling. 
      int x_step = (int)Math.floor( ((double)w)/
                                    ((double)monitor_dim.getWidth() ) );
      int y_step = (int)Math.floor( ((double)h)/
                                    ((double)monitor_dim.getHeight()) );
      // If floor makes step less than one, force step to be 1.
      if( x_step < 1 )
        x_step = 1;
      if( y_step < 1 )
        y_step = 1;
    
      max_data = Float.NEGATIVE_INFINITY;
      min_data = Float.POSITIVE_INFINITY;
      float temp;
      int row_count = data.getNumRows();
      int col_count = data.getNumColumns();
      for ( int row = 0; row < row_count; row+=y_step )
        for ( int col = 0; col < col_count; col+=x_step )
        {
          temp = data.getDataValue(row,col);
          if ( temp > max_data )
            max_data = temp;
          if ( temp < min_data )
            min_data = temp;
        }/*
      // Now compare min/max to the min/max in the virtual array data axis.
      AxisInfo data_axis = data.getAxisInfo(AxisInfo.Z_AXIS);
      float axis_min = data_axis.getMin();
      float axis_max = data_axis.getMax();
      // Make sure neither are Float.NaN
      if( !(Float.isNaN(axis_min) || Float.isNaN(axis_max)) )
      {
        // Make sure axis_min < axis_max.
        if( axis_min > axis_max )
        {
          float swap = axis_min;
          axis_min = axis_max;
          axis_max = swap;
        }
        // Since min/max_data are only from a sampling, there could be other
        // values that are larger/smaller. Compare these to those supplied by
        // the virtual array.
        if( max_data < axis_max )
          max_data = axis_max;
        if( min_data > axis_min )
          min_data = axis_min;
      } // end if NaN*/
      /* #######################End of Revision######################
       * Old way
      for ( int row = 0; row < a2d.getNumRows(); row++ )
        for ( int col = 0; col < a2d.getNumColumns(); col++ )
        {
          if ( a2d.getDataValue(row,col) > max_data )
            max_data = a2d.getDataValue(row,col); 
          if ( a2d.getDataValue(row,col) < min_data )
            min_data = a2d.getDataValue(row,col); 
        }*/
      if ( min_data == max_data )    // avoid division by 0 when scaling data
        max_data = min_data + 1;
    } // End if( isAutoScaleDataEnabled() )
    if ( rebuild_image )
    {
      makeImage();
    }
  } // End setData()
  
 /**
  *  Get a thumbnail of the entire image shown by this ImageJPanel.
  *
  *  @param  width The desired width of the thumbnail.
  *  @param  height The desired height of the thumbnail.
  *  @return A thumbnail of the Image.
  */ 
  public Image getThumbnail(int width, int height, boolean forceRedraw)
  {
    Image thumbnail;
    // If thumbnail_image exists, scale it down the the desired size.
    if( thumbnail_image != null && !forceRedraw)
    {
      if( width == 0 || height == 0 )
        thumbnail = thumbnail_image.getScaledInstance( 100, 100,
	                                               Image.SCALE_DEFAULT );
      else
        thumbnail = thumbnail_image.getScaledInstance( width, height,
	                                               Image.SCALE_DEFAULT);
    }
    // If not yet created, set the local bounds to global bounds. This will
    // allow for getting the whole image. Then reset the bounds back to
    // the original local bounds. This process is expensive because the
    // makeImage() must be called twice.
    else
    {
      CoordTransform temp = new CoordTransform(local_transform);
      local_transform = new CoordTransform(global_transform);
      makeImage();
      if( width == 0 || height == 0 )
        thumbnail = image.getScaledInstance( 100, 100, Image.SCALE_DEFAULT );
      else
        thumbnail = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
      local_transform = new CoordTransform(temp);
      makeImage();
    }
    return thumbnail;
  } 


/* --------------------------- getNumDataRows ----------------------------- */
/**
 *  Get the number of rows in the data for this image panel.
 *
 *  @return  The number of rows.
 */
  public int getNumDataRows()
  {
    if ( data != null )
      return data.getNumRows();

    return 0;
  }


/* --------------------------- getNumDataColumns ------------------------ */
/**
 *  Get the number of columns in the data for this image panel.
 *
 *  @return  The number of columns.
 */
  public int getNumDataColumns()
  {
    if ( data != null )
      return data.getNumColumns();

    return 0;
  }


/* -------------------------------- update ------------------------------- */
/**
 *  Update method that just calls paint.
 */
  public void update( Graphics g )
  {
    paint(g);
  }

/* --------------------------------- paint ------------------------------- */
/**
 *  This method is invoked by swing to draw the image.  Applications must not
 *  call this directly.
 */
  public void paint( Graphics g )
  {
    // Call the paint() on the extended class.
    super.paint(g);
    stop_box( current_point, false );   // if the system redraws this without
    stop_crosshair( current_point );    // our knowlege, we've got to get rid
                                        // of the cursors, or the old position
                                        // will be drawn rather than erased 
                                        // when the user moves the cursor (due
                                        // to XOR drawing). 

    if ( rescaled_image == null )       // the component might not have been
      makeImage();                      // visible when makeImage was called

    if ( rescaled_image != null )       // the component must still not be 
    {                                   // visible
      prepareImage( rescaled_image, this );
      g.drawImage( rescaled_image, 0, 0, this ); 
    }
  }

/* -------------------------- ImageRow_of_PixelRow ----------------------- */
/**
 *  Get the row number in the data array of the specified pixel row.
 *
 *  @param  pix_row    Pixel "row" value, i.e. pixel y coordinate.
 *
 *  @return  the row number in the data array cooresponding to the specified
 *           pixel row.
 */
  public int ImageRow_of_PixelRow( int pix_row )
  {
    float WC_y = local_transform.MapYFrom( pix_row );
 
    return ImageRow_of_WC_y( WC_y );
  }

/* -------------------------- ImageRow_of_WC_y ----------------------- */
/**
 *  Get the row number in the data array corresponding to the specified
 *  world coordinate y value. 
 *
 *  @param  y  The world coordinate y value 
 *
 *  @return  the row number in the data array cooresponding to the specified
 *           pixel world coordinate y.
 */
  public int ImageRow_of_WC_y( float y )
  {
    CoordTransform world_to_image = getWorldToImageTransform();
   
    int   row = (int)( world_to_image.MapYTo( y ) );
    if ( row < 0 )
      row = 0;
    else if ( row > data.getNumRows() - 1 )
      row = data.getNumRows() - 1;
    return row;
  }

/* -------------------------- ImageCol_of_PixelCol ----------------------- */
/**
 *  Get the column number in the data array of the specified pixel col.
 *
 *  @param  pix_col    Pixel "col" value, i.e. pixel x coordinate.
 *
 *  @return  the column number in the data array cooresponding to the specified
 *           pixel column.
 */
  public int ImageCol_of_PixelCol( int pix_col )
  {
    float WC_x = local_transform.MapXFrom( pix_col );

    return  ImageCol_of_WC_x( WC_x );
  }

/* -------------------------- ImageCol_of_WC_x ----------------------- */
/**
 *  Get the column number in the data array corresponding to the specified
 *  world coordinate x value.
 *
 *  @param  x  The world coordinate x value
 *
 *  @return  the column number in the data array cooresponding to the specified
 *           pixel world coordinate x.
 */
  public int ImageCol_of_WC_x( float x )
  {
    CoordTransform world_to_image = getWorldToImageTransform();
   
    int col = (int)( world_to_image.MapXTo( x ) );
    if ( col < 0 )
      col = 0;
    else if ( col > data.getNumColumns() - 1 )
      col = data.getNumColumns() - 1;

    return col;
  }


/* -------------------------- ImageValue_at_Pixel ----------------------- */
/**
 *  Get the data value from the data array that cooresponds to the specified
 *  pixel position.
 *
 *  @param  pixel_pt   The 2D coordinates of the pixel
 *
 *  @return The data value drawn at that pixel location
 */
  public float ImageValue_at_Pixel( Point pixel_pt )
  {
    int row = ImageRow_of_PixelRow( pixel_pt.y );
    int col = ImageCol_of_PixelCol( pixel_pt.x );

    return data.getDataValue(row,col);
  }


/* -------------------------- ImageValue_at_Cursor ----------------------- */
/**
 *  Get the data value from the data array at the current cursor position
 *
 *  @return The data value drawn at the current cursor location
 */
  public float ImageValue_at_Cursor( )
  {
    return ImageValue_at_Pixel( getCurrent_pixel_point() );
  }


/* ---------------------------- getPreferredSize ------------------------- */
/**
 *  Get the preferred size of this component based on the number of rows
 *  and columns and on whether or not scrolling has been requested.
 *
 *  @see CoordJPanel
 *
 *  @return  (0,0) is returned if scrolling has not been requested.  If
 *           vertical scrolling has been requested, the actual number of 
 *           rows in the data array will be used instead of 0.  If horizontal
 *           scrolling has been requested, the actual number of columns in
 *           in the data array will be used instead of 0.
 */
public Dimension getPreferredSize()
{
    if ( preferred_size != null )     // if someone has specified a preferred
      return preferred_size;          // size, just use it.

    int rows, cols;                   // otherwise calculate the preferred
                                      // width based on the data dimensions
                                      // if scrolling is to be used.
    if ( v_scroll )
      rows = data.getNumRows();
    else
      rows = 0;

    if ( h_scroll )
      cols = data.getNumColumns();
    else
      cols = 0;

    return new Dimension( cols, rows );
}


/* -------------------------------- getDataMin --------------------------- */
/**
 *  Get the minimum value of the data represented by the image.
 *
 *  @return  min_data
 */
public float getDataMin()
{
  return min_data;
}


/* -------------------------------- getDataMax --------------------------- */
/**
 *  Get the maximum value of the data represented by the image.
 *
 *  @return  max_data
 */
public float getDataMax()
{
  return max_data;
}

/**
 *  Get the image number of rows/columns wrapped in a CoordBound object to
 *  allow for mapping to another coordinate system.
 *
 *  @return The image transformation.
 */
public CoordBounds getImageCoords()
{
  return getWorldToImageTransform().getDestination();
}


/* -------------------------- RebuildImage ---------------------------- */
/**
 *  Force rebuilding the visibile portion of the image and call repaint.
 *  This routine must be called after the zoom region is set.
 */
public void RebuildImage()
{
   makeImage();
   repaint();
}

/* ---------------------- LocalTransformChanged -------------------------- */

protected void LocalTransformChanged()
{
  makeImage();
}

/* -----------------------------------------------------------------------
 *
 * PRIVATE METHODS
 *
 */

/* ----------------------- getWorldToImageTransform ---------------------- */

  private CoordTransform getWorldToImageTransform()
  {
    CoordBounds     world_bounds;
    CoordBounds     image_bounds;

    SetTransformsToWindowSize();
    image_bounds = new CoordBounds( 0.001f, 0.001f, 
                                    data.getNumColumns()-0.001f,
				    data.getNumRows()-0.001f );
    world_bounds = getGlobal_transform().getSource();
    return( new CoordTransform( world_bounds, image_bounds ) );   
  }


/* ---------------------------------- makeImage --------------------------- */
 /*
  * This method will create a visible image based on the data passed in.
  * By calling subSample(), this method can display any size array
  * in approximately the same time frame.
  */
  private void makeImage()
  {
    if ( ! isVisible() )              // don't do it yet if it's not visible
      return;

    SetTransformsToWindowSize();
    // Get world_to_image transform, and local world coord bounds.
    CoordTransform world_to_image = getWorldToImageTransform();
    CoordBounds    bounds         = local_transform.getSource();
    // Convert local coord bounds to image row/column.
    bounds = world_to_image.MapTo( bounds );
    int start_row = Math.max( (int)(bounds.getY1() ), 0 );
    int end_row   = Math.min( (int)(bounds.getY2() ), data.getNumRows()-1 );
    int start_col = Math.max( (int)(bounds.getX1() ), 0 );
    int end_col   = Math.min( (int)(bounds.getX2() ), data.getNumColumns()-1 );
    
    // Convert global coord bounds to image row/column. Compare integer
    // row/columns because it is more consistent that comparing floats.
    CoordBounds    gbounds  = getGlobal_transform().getSource();
    gbounds = world_to_image.MapTo( gbounds );
    int gstart_row = Math.max((int)(gbounds.getY1() ), 0 );
    int gend_row   = Math.min((int)(gbounds.getY2() ), data.getNumRows()-1 );
    int gstart_col = Math.max((int)(gbounds.getX1() ), 0 );
    int gend_col   = Math.min((int)(gbounds.getX2() ), data.getNumColumns()-1 );
    // Make the thumbnail image if the local bounds are equal to global bounds.
    if( gstart_row == start_row && gend_row == end_row &&
        gstart_col == start_col && gend_col == end_col )
      makeThumbnail = true;
    
    CoordBounds new_bounds = new CoordBounds( start_col+.001f, start_row+0.001f,
                                              end_col+0.999f, end_row+0.999f );
    new_bounds = world_to_image.MapFrom( new_bounds );
    setLocalWorldCoords( new_bounds );
    subSample(start_row,end_row,start_col,end_col);

    stop_box( current_point, false );
    stop_crosshair( current_point );

    rescaleImage();
    repaint();
  }
 
 /*
  * This method will look at the monitor dimensions and determine the
  * amount of data that can be displayed on the screen. If the array
  * dimensions exceed the screen dimensions, the array is subsampled
  * to make drawing faster.
  */ 
  private void subSample( int start_row, int end_row,
                          int start_col, int end_col )
  {
    // Subsamble data if data exceeds bounds of monitor.
    // Get monitor dimensions.
    Dimension monitor_dim = Toolkit.getDefaultToolkit().getScreenSize();
    
    int h = Math.abs(end_row - start_row) + 1;
    int w = Math.abs(end_col - start_col) + 1;
    
    // Subsample step increments. If image exceeds size
    // of monitor screen, increase steps for subsampling. 
    int x_step = (int)Math.floor( ((double)w)/
                                  ((double)monitor_dim.getWidth() ) );
    int y_step = (int)Math.floor( ((double)h)/
                                  ((double)monitor_dim.getHeight()) );
    // If floor makes step less than one, force step to be 1.
    if( x_step < 1 )
      x_step = 1;
    if( y_step < 1 )
      y_step = 1;
    // Get number of rows and columns actually being displayed by the image.
    int num_displayed_cols = (int)Math.ceil( ((double)w)/((double)x_step));
    int num_displayed_rows = (int)Math.ceil( ((double)h)/((double)y_step));
    
    byte pix[] = new byte[num_displayed_rows*num_displayed_cols];
    int index = 0;
    float max_abs = 0;
    if ( Math.abs( max_data ) > Math.abs( min_data ) )
      max_abs = Math.abs( max_data );
    else
      max_abs = Math.abs( min_data );

    float scale_factor = 0;
    if ( max_abs > 0 )
      scale_factor = (LOG_TABLE_SIZE - 1) / max_abs;
    else
      scale_factor = 0;
    byte zero_index = 0;
    if( isTwoSided )
      zero_index = ZERO_COLOR_INDEX;
    float temp = 0;
    for (int y = start_row; y <= end_row; y=y+y_step)
    {
      for (int x = start_col; x <= end_col; x=x+x_step)
      {
        temp = data.getDataValue(y,x) * scale_factor;
	if( temp > LOG_TABLE_SIZE - 1 )
	  temp = LOG_TABLE_SIZE - 1;
        else if( temp < -(LOG_TABLE_SIZE - 1) )
	  temp = -(LOG_TABLE_SIZE - 1);
	
	if ( temp >= 0 )
          pix[index++] = (byte)(zero_index + log_scale[(int)temp]);
        else
          pix[index++] = (byte)(zero_index - log_scale[(int)(-temp)]);
        //System.out.println("Pix " + pix[index - 1] + " " + (index - 1) );
      }
    }
    // If local_bounds = global_bounds, remake the thumbnail_image.
    if( makeThumbnail )
    {
      thumbnail_image = createImage(new MemoryImageSource(num_displayed_cols,
                                                          num_displayed_rows,
					                  color_model, pix, 0,
					                  num_displayed_cols));
      makeThumbnail = false;
    }
    image = createImage(new MemoryImageSource(num_displayed_cols,
                                              num_displayed_rows,
					      color_model, pix, 0,
					      num_displayed_cols));
  }

/* ---------------------------- rescaleImage -------------------------- */

  private void rescaleImage()
  {
    int       new_width,
              new_height;

    SetTransformsToWindowSize();

    Dimension size = this.getSize();
    new_width  = size.width;
    new_height = size.height;

    if ( new_width == 0 || new_height == 0 )   // region not yet sized properly
      return;

    if ( image != null )
    {
      if ( v_scroll && new_height < data.getNumRows() )
        new_height = data.getNumRows();

      if ( h_scroll && new_width < data.getNumColumns() )
        new_width = data.getNumColumns();

      rescaled_image = image.getScaledInstance(new_width, new_height, 
                                                 Image.SCALE_DEFAULT );
    }
  }


/* ----------------------------- setLogScale -------------------------- */

  private void setLogScale( double s )
  {
    PseudoLogScaleUtil log_scaler = new PseudoLogScaleUtil(
                                          0f, (float)LOG_TABLE_SIZE,
					  0f, NUM_POSITIVE_COLORS );
    for( int i = 0; i < LOG_TABLE_SIZE; i++ )
      log_scale[i] = (byte)(log_scaler.toDest(i,s));
  }


/*-----------------------------------------------------------------------
 *
 *  INTERNAL CLASSES
 *
 */

class ImageKeyAdapter extends KeyAdapter
{
  public void keyPressed( KeyEvent e )
  {
    int code = e.getKeyCode();

    boolean  is_arrow_key;
    is_arrow_key = ( code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT ||
                     code == KeyEvent.VK_UP   || code == KeyEvent.VK_DOWN   );
                                                 // only process arrow keys
    if ( !is_arrow_key )
      return;

    CoordTransform world_to_image = getWorldToImageTransform();
    floatPoint2D cur_WC_point = getCurrent_WC_point();
    floatPoint2D cur_image_pix = world_to_image.MapTo( cur_WC_point );

    cur_image_pix.x = ImageCol_of_PixelCol( current_point.x ) + 0.5f;
    cur_image_pix.y = ImageRow_of_PixelRow( current_point.y ) + 0.5f;

    if ( code == KeyEvent.VK_UP )
    {
      if ( cur_image_pix.y > 1 )
        cur_image_pix.y = cur_image_pix.y - 1;
    }
    else if ( code == KeyEvent.VK_DOWN )
    {
      if ( cur_image_pix.y < data.getNumRows() - 1 )
        cur_image_pix.y = cur_image_pix.y + 1;
    }
    else if ( code == KeyEvent.VK_LEFT )
    {
      if ( cur_image_pix.x > 1 )
        cur_image_pix.x = cur_image_pix.x - 1;
    }
    else if ( code == KeyEvent.VK_RIGHT )
    { 
      if ( cur_image_pix.x < data.getNumColumns() - 1 )
        cur_image_pix.x = cur_image_pix.x + 1;
    }

    Point old_screen_pix_pt = getCurrent_pixel_point();
    cur_WC_point = world_to_image.MapFrom( cur_image_pix );
    setCurrent_WC_point( cur_WC_point );
    Point new_screen_pix_pt = getCurrent_pixel_point();

    if ( (new_screen_pix_pt.x == old_screen_pix_pt.x) &&
         (new_screen_pix_pt.y == old_screen_pix_pt.y)   )
    {
      if ( code == KeyEvent.VK_UP )
        new_screen_pix_pt.y--;
      else if ( code == KeyEvent.VK_DOWN )
        new_screen_pix_pt.y++;
      else if ( code == KeyEvent.VK_LEFT )
        new_screen_pix_pt.x--;
      else if ( code == KeyEvent.VK_RIGHT )
        new_screen_pix_pt.x++;
    }
    setCurrent_pixel_point( new_screen_pix_pt );

    int id = 0;                               // synthesize a mouse event and
    int modifiers = 0;                        // send it to this CoordJPanel
    int clickcount = 0;                       // to trigger the proper response

    if ( !isDoingBox() && !isDoingCrosshair() )
      id = MouseEvent.MOUSE_PRESSED;
    else
      id = MouseEvent.MOUSE_DRAGGED;

    if ( e.isShiftDown() )
      modifiers  = InputEvent.BUTTON2_MASK;
    else
      modifiers  = MouseEvent.BUTTON1_MASK;

    MouseEvent mouse_e = new MouseEvent( this_panel,
                                         id,
                                         e.getWhen(),
                                         modifiers,
                                         current_point.x,
                                         current_point.y,
                                         clickcount,
                                         false );
    this_panel.dispatchEvent( mouse_e );

    if ( id == MouseEvent.MOUSE_PRESSED )      // Also send dragged event
    {
       mouse_e = new MouseEvent( this_panel,
                                 MouseEvent.MOUSE_DRAGGED,
                                 e.getWhen()+1,
                                 modifiers,
                                 current_point.x,
                                 current_point.y,
                                 clickcount,
                                 false );
      this_panel.dispatchEvent( mouse_e );
    }
  }
}


/* -------------------------------------------------------------------------
 *
 * MAIN
 *
 */
 /* Basic main program for testing purposes only. */
  public static void main(String[] args)
  {
    int rows = 10000;
    int cols = 1000;  
    float test_array[][] = new float[rows][cols];

    for ( int i = 0; i < rows; i++ )
      for ( int j = 0; j < cols; j++ )
      {
        if ( i % 50 == 0 )
          test_array[i][j] = 20 * i;
        else if ( j % 50 == 0 )
          test_array[i][j] = 20 * j;
        else
          test_array[i][j] = i * j;
      }
    VirtualArray2D varray2d = new VirtualArray2D(test_array);
    JFrame f = new JFrame("Test for ImageJPanel2");
    f.setBounds(0,0,500,500);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ImageJPanel2 panel = new ImageJPanel2();
    panel.setData( varray2d, true );
    panel.setNamedColorModel( IndexColorMaker.HEATED_OBJECT_SCALE_2, 
                              true,
                              true );
    panel.setGlobalWorldCoords( new CoordBounds( 0, 0, 20, 20 ) );
    panel.setLocalWorldCoords( new CoordBounds( 10, 10, 12, 10.2f ) );
    f.getContentPane().add(panel);
    f.setVisible(true);
  }
}
