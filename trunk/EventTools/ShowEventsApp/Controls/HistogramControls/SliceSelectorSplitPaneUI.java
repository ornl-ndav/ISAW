package EventTools.ShowEventsApp.Controls.HistogramControls;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.*;
import gov.anl.ipns.ViewTools.UI.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import EventTools.ShowEventsApp.Command.Commands;

import java.io.*;

/**
 *  This class provides a user interface for specifying the plane, size and
 *  resolution of an image of a rectangular slab in 3D.
 */

public class SliceSelectorSplitPaneUI extends    ActiveJPanel
                             implements ISlicePlaneSelector,
                                        Serializable 
{
  private static final long  serialVersionUID = 1L;
  private static final String HKL_TITLE  = "Slice in HKL Space";
  private static final String QXYZ_TITLE = "Slice in Qxyz Space";

  private HKLorQ_SelectorUI display_mode;
  private HKLorQ_SelectorUI slice_mode;
  private SlicePlane3D_UI   plane_selector;
  private SliceImageUI      image_selector;
  private FrameController   frame_control;
  private JButton           apply;

  private boolean           send_messages;
  private int               num_pages = 0;


  /*-------------------------- constructor ----------------------- */
  /**
   *  Construct a SliceSelectorUI in either HKL or QXYZ mode, as specified.
   *
   *  @param  mode  Integer code should be one of HKL_MODE or QXYZ_MODE, or
   *                HKL_MODE will be used by default.
   */
  public SliceSelectorSplitPaneUI( int mode )
  {
    if ( mode != HKL_MODE && mode != QXYZ_MODE )
      mode = HKL_MODE;

    display_mode   = new HKLorQ_SelectorUI( "Display in " );
    slice_mode     = new HKLorQ_SelectorUI( "Select  in " );
    plane_selector = new SlicePlane3D_UI( mode );
    image_selector = new SliceImageUI( "Select Plane Size" );
    frame_control  = new FrameController();
    
    JPanel button  = new JPanel();
    apply  = new JButton( "Apply" );
    button.add(apply);

    setDisplayMode( mode, true );
    setSliceMode( mode, true );
    
    Box box = new Box( BoxLayout.Y_AXIS );
    box.add( display_mode );
    box.add( slice_mode );
    box.add( plane_selector );
    box.add( image_selector );
    box.add( button );
    box.add( frame_control );

    setLayout( new GridLayout(1,1) );
    add(box);

    ValueListener value_listener = new ValueListener();
    slice_mode.addActionListener(value_listener);
    apply.addActionListener(value_listener);
    
    frame_control.addActionListener( new StepListener() );
  }

  /* --------------------------- setDisplayMode --------------------------- */
  /**
   *  Set the display mode choice either hkl or Qxzy mode, enabling or 
   *  disabling the control based on the enable flag.
   *
   *  @param  mode   Integer code must be one of HKL_MODE or QXYZ_MODE, other
   *                 values will be ignored.
   *
   *  @param  enable If true, the user will be able to change the mode,
   *                 if false, the specified mode will be set but the
   *                 control will be disabled.
   */
  public void setDisplayMode( int mode, boolean enable )
  {
    if ( mode != HKL_MODE && mode != QXYZ_MODE )
      return;

    display_mode.setMode( mode );
    display_mode.setEnabled( enable );
  }


  /* ---------------------------- getDisplayMode ------------------------- */
  /**
   *  Get the currently specified slice display mode, HKL_MODE or QXYZ_MODE.
   *
   *  @return the current mode.
   */
  public int getDisplayMode()
  {
    return display_mode.getMode();
  }


  /* --------------------------- setSliceMode --------------------------- */
  /**
   *  Set the mode used for specifying a slice to either hkl or Qxzy
   *  mode.
   *
   *  @param  mode  Integer code must be one of HKL_MODE or QXYZ_MODE, other
   *                values will be ignored.
   *
   *  @param  enable If true, the user will be able to change the mode,
   *                 if false, the specified mode will be set but the
   *                 control will be disabled.
   */
  public void setSliceMode( int mode, boolean enable )
  {
    if ( mode != HKL_MODE && mode != QXYZ_MODE )
      return;

    send_messages = false;

    plane_selector.setMode( mode );

    String title = HKL_TITLE;
    if ( mode == QXYZ_MODE )
      title = QXYZ_TITLE;

    TitledBorder border =
                 new TitledBorder(LineBorder.createBlackLineBorder(), title );
    border.setTitleFont( FontUtil.BORDER_FONT );
    setBorder( border );

    slice_mode.setMode( mode );
    slice_mode.setEnabled( enable );

    send_messages = true;
  }


  /* ---------------------------- getSliceMode --------------------------- */
  /**
   *  Get the currently specified slice selection mode, HKL_MODE or QXYZ_MODE.
   *
   *  @return the current mode.
   */
  public int getSliceMode()
  {
    return slice_mode.getMode();
  }


  /* ---------------------------- setPlane ----------------------------- */
  /**
   *  Set the plane whose parameters are to be displayed.
   *
   *  @param new_plane  The plane to display.
   */
  public void setPlane( SlicePlane3D new_plane )
  {
    plane_selector.setPlane( new_plane );
  }


  /* ---------------------------- getPlane ----------------------------- */
  /**
   *  Get the currently specified plane, which may be null if the user
   *  supplied values don't specifiy a valid plane.
   *
   *  @return the currently specified plane, or null if there is no valid
   *          plane specified.
   */
  public SlicePlane3D getPlane()
  {
    return plane_selector.getPlane();
  }

  public void setImageData()
  {

  }

  /* ------------------------- setStepSize --------------------------- */
  /**
   *  Set the current step size, MUST be positive.
   *
   *  @param new_step  The new step size to use, MUST be positive.
   */
  public void setStepSize( float new_step )
  {
    image_selector.setStepSize( new_step );
  }


  /* ---------------------------- getStepSize ---------------------------- */
  /**
   *  Get the currently selected step size.
   *
   *  @return the currently selected steps/unit.
   */
  public float getStepSize()
  {
    return image_selector.getStepSize();
  }


  /* --------------------------- setSliceWidth --------------------------- */
  /**
   *  Set the current width, MUST be positive.
   *
   *  @param  new_width  The new width to use, MUST be positive.
   */
  public void setSliceWidth( float new_width )
  {
    image_selector.setSliceWidth( new_width );
  }


  /* --------------------------- getSliceWidth --------------------------- */
  /**
   *  Get the currently selected width.
   *
   *  @return the currently selected width.
   */
  public float getSliceWidth()
  {
    return image_selector.getSliceWidth();
  }


  /* -------------------------- setSliceHeight --------------------------- */
  /**
   *  Set the current height, MUST be positive.
   *
   *  @param  new_height  The new height to use, MUST be positive.
   */
  public void setSliceHeight( float new_height )
  {
     image_selector.setSliceHeight( new_height );
  }


  /* -------------------------- getSliceHeight --------------------------- */
  /**
   *  Get the currently selected height.
   *
   *  @return the currently selected height.
   */
  public float getSliceHeight()
  {
    return image_selector.getSliceHeight();
  }


  /* ------------------------- setSliceThickness ------------------------- */
  /**
   *  Set the current thickness, MUST be positive.
   *
   *  @param  new_thickness  The new thickness to use, MUST be positive.
   */
  public void setSliceThickness( float new_thickness )
  {
    image_selector.setSliceThickness( new_thickness );
  }


  /* ------------------------- getSliceThickness ------------------------- */
  /**
   *  Get the currently selected thickness.
   *
   *  @return the currently selected thickness.
   */
  public float getSliceThickness()
  {
    return image_selector.getSliceThickness();
  }

  public void setFrameData()
  {

  }

  public int getSliceNumber()
  {
     return frame_control.getFrameNumber();
  }
  
  /* ----------------------------- toString ------------------------------ */
  /**
   *  Return a string form of this plane.
   */
  public String toString()
  {
    return "" + plane_selector.toString() + "\n" 
              + image_selector.toString() + "\n" 
              + frame_control.getFrameNumber() + "\n";
  }


  /* -----------------------------------------------------------------------
   *
   *  PRIVATE CLASSES
   *
   */

  /* ------------------------ ValueListener ------------------------------ */
  /*
   *  Listen for a new value.
   */ 
  private class ValueListener implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      if ( e.getSource() == slice_mode )
      {
        setSliceMode( slice_mode.getMode(), slice_mode.isEnabled() );
        if ( send_messages )
          send_message(Commands.SLICE_MODE_CHANGED);
      }
      else if(e.getSource() == apply)
      {
         if(send_messages)
            send_message(Commands.PLANE_CHANGED);
      }
    }
  }

  /* ------------------------ StepListener ------------------------------ */
  /*
   *  Listen for a new value.
   */
  private class StepListener implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
       SlicePlane3D plane = getPlane();
       
       Vector3D normal = plane.getNormal();
       normal.multiply(getSliceThickness());

       Vector3D center = plane.getOrigin();
       center.add(normal);
       
       plane.setOrigin(center);
       setPlane(plane);
       
       send_message( Commands.SET_SLICE_1);
    }
  }


  /* ------------------------------ main --------------------------------- */
  /**
   *  Main program providing basic functionality test.
   */
  public static void main( String args[] )
  {
    JFrame  f = new JFrame("Test for SliceSelectorUI");
    f.setBounds( 0, 0, 210, 500 ); 
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    // final SliceSelectorUI test = new SliceSelectorUI( HKL_MODE );
    final SliceSelectorSplitPaneUI test = 
       new SliceSelectorSplitPaneUI( QXYZ_MODE);

    test.addActionListener( new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        System.out.println("New Values ----------------------------" );
        System.out.println("" + test );
        System.out.println("--------------------------------------" );
      }
    });

    f.getContentPane().setLayout( new GridLayout(1,1) );
    f.getContentPane().add( test );
    f.setVisible(true);
  }

}
