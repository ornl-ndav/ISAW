/*
 * File:  JoglPanel.java
 *
 * Copyright (C) 2004 Dennis Mikkelson
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
 * Modified:
 *
 * $Log: JoglPanel.java,v $
 * Revision 1.12  2006/07/25 01:58:18  dennis
 * Replaced call to deprecated show() method, with call to
 * setVisible(true).
 *
 * Revision 1.11  2006/07/20 14:33:13  dennis
 * Added constructor that allows specifying debug or trace mode.
 * Added isDrawing() method to check if a redraw has been requested but
 * not finished yet.
 *
 *
 * Revision 1.10  2005/08/03 16:58:45  dennis
 * Commented out gl.glEnable(GL_LIGHTING), so that now lighting is
 * turned off by default.  (Still need to implement a "nice" way to
 * turn lighting on/off.
 * Removed default printing of the capabilities of the display
 * device.
 *
 * Revision 1.9  2005/07/25 15:38:50  dennis
 * Now enables GL_COLOR_MATERIAL with the diffuse reflection component
 * of the front and back faces tracking the current color.  This allows
 * the Material class to just specify the diffuse color by calling
 * glColor3f() to set the current color.  Consequently, shapes can now
 * be seen whether lighting is on or off.
 *
 * Revision 1.8  2005/07/18 21:33:46  dennis
 * Temporarily commented out drag handler, pending checkin of
 * arc ball controller class.
 *
 * Revision 1.7  2005/07/18 21:31:11  dennis
 * Added extra camera to record a "home" position, so that the
 * view can be easily reset.
 *
 * Revision 1.6  2005/07/14 21:49:07  dennis
 * Switched from local copy of Vector3D, etc. to using Vector3D, etc.
 * from gov.anl.ipns.MathTools.Geometry.
 *
 * Revision 1.5  2005/07/08 15:31:26  dennis
 * Now can be either a heavy-weight GLCanvas, or a light-weight GLJPanel.
 * Added new constructor that takes a flag indicating which type of
 * panel is to be constructed.
 *
 * Revision 1.4  2005/01/21 02:56:56  dennis
 * Added waitForDraw() method.  This method is called when selecting
 * objects and when locating points, to give the drawing code time to
 * complete, before using the results.  The drawing code executes in
 * another thread.  This fixes a problem that only became apparent when
 * used with an ATI proprietary driver on Linux.  The earlier version
 * worked with NVIDIA and Mesa drivers.
 *
 * Revision 1.3  2004/12/06 20:03:11  dennis
 * Added methods to set & get the scene that is displayed.
 *
 * Revision 1.2  2004/10/27 19:18:31  dennis
 * Added pickedPoint() method to calculate 3D coordinates of a specified
 * pixel.
 *
 * Revision 1.1  2004/10/25 21:43:56  dennis
 * Added to CVS repositiory.
 *
 *
 * 10/25/04  Modified to work with new version of camera that does NOT
 *           set the matrix mode and load identity when making the 
 *           view or projection matrices.
 *           Added code to support selection of objects, based on x,y 
 *           coordinates on the screen.
 *
 * 10/19/04  Added code to initialize OpenGL lighting and control a 
 *           "headlight" and the global ambient light.
 *
 * 10/13/04  Modified JogleDriverProgram3D to use camera class to control
 *           COP, VRP, VUV and the view volume.  Changed name to JoglPanel.
 *
 *  9/13/04  Modified BasicJogl.java to draw a scene defined by another class,
 *           that implements the interface IGL_Renderable object.  Also added
 *           methods getDisplayComponent() and Draw() so that the canvas can be
 *           placed in any container and so that other code can request that
 *           the scene be redrawn. 
 *
 *  9/21/04  Modified JoglDriverProgram2D.  Added a very basic implementation
 *           for reshape(), that sets up a default viewing frustum.
 *
 *  9/27/04  Added method SetView() to specify different camera positions
 *           Added method SetViewVolume() to specify near & far clipping 
 *           planes and to specifiy the view volume.
 *
 *  9/30/04  Removed call to Draw() method at end of SetView() and
 *           SetViewVolume().  Now Draw() must be called separately, when
 *           needed, after changing the view or view volume.
 */
package SSG_Tools.Viewers;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.nio.*;

import net.java.games.jogl.*;
import net.java.games.jogl.util.*;

import SSG_Tools.*;
import SSG_Tools.Cameras.*;
import SSG_Tools.Utils.*;
import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.SSG_Nodes.Shapes.*;
import SSG_Tools.SSG_Nodes.Util.*;

/**
 *  This class provides a GLCanvas or GLJPanel that can be used  
 *  to render different objects.  The constructor is given an instance of an
 *  IGL_Renderable object that will be drawn.  Applications will use this
 *  class by implementing a specialized IGL_Renderable class that coordinates
 *  drawing the scene.  If no camera is specified, a default perspective 
 *  camera will be used.  Lighting is enabled by default, but can be disabled
 *  with the method supplied.  GL_LIGHT0 is used for a headlight that 
 *  appears to move with the observer.  GL_LIGHT0 should NOT be used for 
 *  other lights in the scene graph, if the headlight is enabled.  The 
 *  headlight can be disabled with the method supplied.
 */

public class JoglPanel 
{
  public static final int NORMAL_MODE = 0;  // flag use for normal (quiet) mode 
  public static final int DEBUG_MODE  = 1;  // flag to turn on debug mode
  public static final int TRACE_MODE  = 2;  // flat to turn on trace mode

  private GLDrawable     canvas;     // This will be either a  GLJPanel, 
                                     // which is a light weight component, or 
                                     // a GLCanvas, which is a heavy weight 
                                     // component that we will draw to, using 
                                     // jogl/OpenGL.

  private int            mode_flag = NORMAL_MODE;

  private IGL_Renderable my_scene;
  private Camera         my_camera;
  private Camera         home_camera;
  private boolean        lighting_on  = true;
  private boolean        ambient_on   = true;
  private boolean        headlight_on = true;
  private Color          ambient_color   = Color.GRAY;
  private Color          headlight_color = Color.GRAY;

  private boolean        draw_requested = false; // flag set true when draw is
                                                 // requested and tripped false
                                                 // when drawing is complete
 
  private boolean        do_locate = false;      // flag indicating that the
                                                 // display method should just
                                                 // calculate the world coord
                                                 // position of cur_x, cur_y
  private boolean        do_select = false;      // flag indicating whether or
                                                 // not the scene should be
                                                 // rendered in selection mode
  private final int HIT_BUFFER_SIZE = 512;
  private int n_hits = 0;
  private IntBuffer hit_buffer = BufferUtils.newIntBuffer( HIT_BUFFER_SIZE );

  private int cur_x,                              // x,y pixel coords of point
              cur_y;                              // used for picking and 
                                                  // locating objects 
  private float world_coords[] = new float[3];    // world coordinates that
                                                  // correspond to cur_x, cur_y
                                                  // when locating point.

  public  static final String PIXEL_DEPTH_SCALE_PROPERTY = "PixelDepthScale";
  private static float pixel_depth_scale_factor = 1;   // Use 256 to workaround
                                                       // quirk with ATI cards,
                                                       // use 1 otherwise.

  /* --------------------------- Constructor --------------------------- */
  /**
   *  This constructor constructs either a a heavy-weight GLCanvas object 
   *  or a light-weight GLJPanel object, using the default capabilities.
   *  Internally, it constructs a GLEventListener that will actually do 
   *  the drawing when the canvas is exposed, resized, etc. 
   *
   *  @param  scene      The scene graph that this GLCanvas object will 
   *                     render.
   *
   *  @param  is_heavy   Flag to determine which type of GLDrawable to 
   *                     construct.  If true a heavy-weight GLCanvas is 
   *                     constructed.  If false a light-weight GLJPanel is 
   *                     constructed.
   */
  public JoglPanel( IGL_Renderable scene, boolean is_heavy )
  {
    try
    {
      GLCapabilities capabilities = new GLCapabilities();

      if ( is_heavy )
        canvas = GLDrawableFactory.getFactory().createGLCanvas(capabilities);
      else
        canvas = GLDrawableFactory.getFactory().createGLJPanel(capabilities);

      canvas.addGLEventListener(new Renderer());
    }
    catch (Exception e)
    {
      System.out.println("ERROR constructing canvas");
      e.printStackTrace();
      System.exit(1);
    }

    my_scene    = scene;
    my_camera   = new PerspectiveCamera();
    home_camera = new PerspectiveCamera();

    setCurrentViewAsHome();

    String depth_scale_prop = System.getProperty(PIXEL_DEPTH_SCALE_PROPERTY);
    if ( depth_scale_prop != null )
    try
    {
      depth_scale_prop.trim();
      Double scale_d = new Double( depth_scale_prop );
      float scale_f = scale_d.floatValue();
      if ( scale_f > 0 )
      {
        pixel_depth_scale_factor = scale_f;
        System.out.println("Using non-standard " + PIXEL_DEPTH_SCALE_PROPERTY +
                           ": " + pixel_depth_scale_factor +
                           " from System Properties." );
      }
    }
    catch ( NumberFormatException e )
    {
      System.out.println( "Warning: invalid number " + depth_scale_prop +
                          " in " + PIXEL_DEPTH_SCALE_PROPERTY );
    }
  }


  /* --------------------------- Constructor --------------------------- */
  /**
   *  This constructor constructs a heavy weight GLCanvas object using the
   *  default capabilities, and internally, constructs a GLEventListener that
   *  will actually do the drawing when the canvas is exposed, resized, etc. 
   *
   *  @param  scene   The scene graph that this GLCanvas object will 
   *                  render.
   */
  public JoglPanel( IGL_Renderable scene )
  {
     this( scene, true );
  }


  /* --------------------------- Constructor --------------------------- */
  /**
   *  This constructor constructs either a a heavy-weight GLCanvas object 
   *  or a light-weight GLJPanel object, using the default capabilities,
   *  with DEBUG_MODE or TRACE_MODE turned on.  If DEBUG_MODE is turned on
   *  any error messages generated by OpenGL will be automatically printed.
   *  If TRACE_MODE is turned on, each OpenGL call will be printed.
   *
   *  @param  scene      The scene graph that this GLCanvas object will 
   *                     render.
   *
   *  @param  is_heavy   Flag to determine which type of GLDrawable to 
   *                     construct.  If true a heavy-weight GLCanvas is 
   *                     constructed.  If false a light-weight GLJPanel is 
   *                     constructed.
   *
   *  @param  mode       Flag to determine whether trace, debug, or
   *                     normal (quiet) mode will be used.  One of the 
   *                     values: TRACE_MODE, DEBUG_MODE or NORMAL_MODE
   *                     should be passed in for this paramter.  Other
   *                     values are ignored, and NORMAL_MODE will be used.
   * 
   */
  public JoglPanel( IGL_Renderable scene, boolean is_heavy, int mode )
  {
    this( scene, is_heavy );

    if ( mode >= NORMAL_MODE && mode <= TRACE_MODE )    // only record valid
      mode_flag = mode;                                 // mode values
  }


/* ---------------------------- getDisplayComponent ---------------------- */
/**
 *  Get the actual GLCanvas that this panel draws into.
 *
 *  @return the GL_Canvas for this panel.
 */
 public Component getDisplayComponent()
 {
   return (Component)canvas;
 }


/* ------------------------------- Draw ---------------------------------- */
/**
 *  Request that the the panel be cleared and redrawn.  
 */
 public void Draw()
 {
   draw_requested = true;
   canvas.display();
 }


/* ----------------------------- isDrawing ------------------------------ */
/**
 *  Check whether a redraw has been requested for this panel, but not
 *  yet completed.
 *
 *  @return true if a redraw has been scheduled, but not completed for 
 *          this panel, and false otherwise.
 */
 public boolean isDrawing()
 {
   return draw_requested;
 }


/* ------------------------------ setScene ------------------------------ */
/**
 *  Set a new IGL_Renderable object to be drawn by this panel.  Only one 
 *  IGL_Renderable can be drawn by this panel.  The change will not be
 *  visible until the panel is redrawn.
 *
 *  @param new_scene  The new IGL_Renderable that should be drawn by this 
 *                    panel.
 */
 public void setScene( IGL_Renderable new_scene )
 {
   my_scene = new_scene;
 }


/* ------------------------------ getScene ------------------------------ */
/**
 *  Get a reference to the IGL_Renderable object that is drawn by this panel.
 *
 *  @return A reference to the IGL_Renderable that is drawn by this panel.
 */
 public IGL_Renderable getScene()
 {
   return my_scene;
 }


/* ------------------------------ setCamera ------------------------------ */
/** 
 *  Set the camera to be used to determine the observer's point of view
 *  and view volume.  Note: This method records a reference to the specified
 *  camera.  Consequently, the same camera could be shared by several 
 *  JoglPanel objects. 
 *
 *  @param  camera   The camera object to use.
 */
 public void setCamera( Camera camera )
 {
   my_camera = camera;
 }


/* ------------------------------ getCamera ------------------------------ */
/** 
 *  Get the camera to be used to determine the observer's point of view
 *  and view volume.  Note: This method returns a reference to the camera
 *  currently used by this panel.  The VRP, COP, VUV and view volume
 *  can be specifed by getting the camera and using methods on the camera
 *  to change the values.
 *
 *  @return a reference to the current camera object in use.
 */
 public Camera getCamera()
 {
   return my_camera;
 }


/* ------------------------ setCurrentViewAsHome ------------------------- */
/** 
 *  Save the current view information as the "home" view, so that it can
 *  be easily reset.
 */
 public void setCurrentViewAsHome()
 {
   home_camera.set( my_camera );
 }


/* ------------------------- resetToHomeView ----------------------------- */
/**
 *  Copy the "home" view parameters into the current camera.  The panel
 *  will need to be drawn to make the new values effective.
 */
 public void resetToHomeView()
 {
   my_camera.set( home_camera );
 }


/* ------------------------- enableLighting ------------------------------ */
/** 
 *  Set flag indicating whether or not this panel should enable OpenGL 
 *  lighting.  Lighting is enabled by default. 
 *
 *  @param on_off  Flag indicting whether lighting should be on or off. 
 */
 public void enableLighting( boolean on_off )
 {
   lighting_on = on_off;
 }


/* ------------------------- enableHeadlight ----------------------------- */
/** 
 *  Set flag indicating whether or not this panel should include a 
 *  "headlight" that moves with the observer.  The headlight is on 
 *  by default.
 *
 *  @param on_off  Flag indicting whether the headlight should be on or off. 
 */
 public void enableHeadlight( boolean on_off )
 {
   headlight_on = on_off;
 }


/* ------------------------- enableAmbient ----------------------------- */
/** 
 *  Set flag indicating whether or not this panel should include a 
 *  "global" ambient light that applies to the whole scene.  The global 
 *  ambient light is on by default.
 *
 *  @param on_off  Flag indicting whether the ambient light should be on or off.
 */
 public void enableAmbient( boolean on_off )
 {
   ambient_on = on_off;
 }


/* ------------------------ setHeadlightColor -------------------------- */
/** 
 *  Specifiy the color for the headlight.
 *
 *  @param color New color to use for the headlight. 
 */
 public void setHeadlightColor( Color color )
 {
   headlight_color = color;
 }


/* ------------------------- setAmbientColor --------------------------- */
/** 
 *  Specifiy the color for the global ambient light.
 *
 *  @param color New color to use for the global ambient light. 
 */
 public void setAmbientColor( Color color )
 {
   ambient_color = color;
 }


/* --------------------------- getPickHitList ------------------------------ */
/**
 *  Get the OpenGL selection hit list for the specified window coordinates
 *  x,y.  The objects in the list of objects for this panel will be rendered
 *  using a special small viewing volume centered around the specified pixel.
 *  This should only be called from the event handling thread.
 *
 *  @param x  The pixel x (i.e. column) value
 *  @param y  The pixel y (i.e. row) value, in window coordinates.
 */
public HitRecord[] pickHitList( int x, int y )
{
  if ( do_select )             // ignore more requests to do selection
    return new HitRecord[0];   // if currently doing selection
    
  cur_x = x;
  cur_y = y;

  do_select      = true;
  draw_requested = true;
  canvas.display();         // this will cause Renderer.display(drawable) to be
                            // called with the correct drawable, GL and thread
  waitForDraw( 2000 );

  int hits[] = new int[ HIT_BUFFER_SIZE ];
  hit_buffer.get( hits );

  Vector    hit_list = new Vector();
  HitRecord hit_rec;
  int start = 0;
  for ( int i = 0; i < n_hits; i++ )
  {
    hit_rec = new HitRecord( hits, start );
    if ( hit_rec.numNames() > 0 )
      hit_list.add( hit_rec );
    start += hits[ start ] + 3;
  }

  HitRecord hit_recs[] = new HitRecord[ hit_list.size() ];
  for ( int i = 0; i < hit_recs.length; i++ )
    hit_recs[i] = (HitRecord)hit_list.elementAt(i);

  return hit_recs;
}


/* -------------------------- pickedPoint ---------------------------- */
/**
 *  Get a Vector3D for the point in 3D corresponding to the specified 
 *  pixel location.
 *
 *  @param x  The pixel x (i.e. column) value
 *  @param y  The pixel y (i.e. row) value, in window coordinates.
 */
public Vector3D pickedPoint( int x, int y )
{
  cur_x = x;
  cur_y = y;

  do_locate      = true;
  draw_requested = true;
  canvas.display();         // this will cause Renderer.display(drawable) to be
                            // called with the correct drawable, GL and thread
  waitForDraw( 2000 );
  return new Vector3D( world_coords );
}



  /**
   *  The class Renderer provides the interface to OpenGL through jogl.
   *  It's display() method is called by the system to do the OpenGL drawing.
   */
  public class Renderer implements GLEventListener
  {
    /* ---------------------------- init ----------------------------- */
    /**
     *  Called by the JOGL system when the panel is initialized. 
     *
     *  @param drawable  The GLDrawable for this canvas.
     */
    public void init( GLDrawable drawable )
    {
      // The default GL can be used, for maximum efficiency, or to aid in
      // debugging, we can change the GL object to be a "DebugGL", which 
      // will check and report any OpenGL error flags each time an OpenGL 
      // function is called.
      //
      if ( mode_flag == DEBUG_MODE )
      {
        GL gl = drawable.getGL();
        drawable.setGL( new DebugGL( gl ) );
      }

      // Alternatively, we can change the GL object to be a "TraceGL", which
      // will list each OpenGL function called to the specified PrintStream.

      else if ( mode_flag == TRACE_MODE )
      { 
        GL gl = drawable.getGL();
        drawable.setGL( new TraceGL( gl, System.out ) );
      }

    }


    /* ---------------------------- reshape ----------------------------- */
    /**
     *  Called by the JOGL system when the panel is resized, and by the
     *  display method to set up the projection matrix before drawing the
     *  scene.  For now we will just leave the projection matrix as the
     *  default (identity) matrix.  This is needed to preserve the aspect
     *  ratio.
     *
     *  @param drawable  The GLDrawable for this canvas.
     *  @param x         The x position of the window (typically 0)
     *  @param y         The y position of the window (typically 0)
     *  @param width     The width of the window in pixels
     *  @param height    The height of the window in pixels
     */
    public void reshape( GLDrawable drawable,
                         int x,
                         int y,
                         int width,
                         int height)
    {
      GL gl = drawable.getGL();

      gl.glViewport( 0, 0, width, height );  // use the full window dimensions

      gl.glMatrixMode(GL.GL_PROJECTION);     // set up the projection matrix
      gl.glLoadIdentity();                   // to use a view volume

      if ( do_select )
      {
        int viewport[] = { 0, 0, width, height };
        BasicGLU.gluPickMatrix( gl, cur_x, height-cur_y, 1, 1, viewport );
      }

      my_camera.MakeProjectionMatrix( drawable, width, height );
    }


    /* ---------------------- displayChanged --------------------------- */
    /**
     *  NOT CURRENTLY IMPLEMENTED, OR NEEDED.  Eventually, this will be
     *  called by the JOGL system when the panel is  moved to another 
     *  display monitor on a dual headed display.
     */
    public void displayChanged( GLDrawable drawable,
                                boolean modeChanged,
                                boolean deviceChanged)
    {
      // we'll ignore this, since we don't have dual headed displays AND
      // jogl doesn't currently support this anyway.
    }


    /* --------------------------- display ----------------------------- */
    /**
     *  Called by the JOGL system when the panel is to be redrawn.  This
     *  coordinates the "real" work.  That is, it typically clears the
     *  display, then calls any methods needed to draw and finally swaps
     *  the front and back buffers.
     *
     *  @param drawable  The GLDrawable for this canvas.
     */
    public void display(GLDrawable drawable)
    {   
                                           // Filter out some degenerate cases
      if ( drawable == null )
        return;

      Dimension size = drawable.getSize();
      if ( size.width <= 0 || size.height <= 0 )
        return;

      if ( drawable instanceof GLCanvas && !((GLCanvas)drawable).isShowing() )
        return;

                                              // the panel is ok, so proceed 
      GL gl = drawable.getGL();               // get the GL context to use

      draw_requested = true;                  // trip draw flag, in case this
                                              // was started by expose/resize

                                      // if just locating 3D point, get the
      if ( do_locate )                // projection info, unproject, and return
      {
        float depths[] = new float[1];                 // read back pixel depth
        gl.glReadPixels( cur_x, size.height-cur_y,     // from OpenGL
                         1, 1,
                         GL.GL_DEPTH_COMPONENT,
                         GL.GL_FLOAT,
                         depths );
        float cur_z = depths[0];
        cur_z *= pixel_depth_scale_factor;
                                                      // get current viewport
        int    viewport[] = new int[4];               // and matrics from OpenGL
        gl.glGetIntegerv( GL.GL_VIEWPORT, viewport );
        double model_view_mat[] = new double[16];
        double projection_mat[] = new double[16];
        gl.glGetDoublev( GL.GL_MODELVIEW_MATRIX, model_view_mat );
        gl.glGetDoublev( GL.GL_PROJECTION_MATRIX, projection_mat );

                                                    // use gluUnProject to map
        double world_x[] = new double[1];           // back to world coordinates
        double world_y[] = new double[1];
        double world_z[] = new double[1];
        BasicGLU.gluUnProject( cur_x, size.height-cur_y, cur_z,
                               model_view_mat, projection_mat, viewport,
                               world_x, world_y, world_z );
        world_coords[0] = (float)world_x[0];
        world_coords[1] = (float)world_y[0];
        world_coords[2] = (float)world_z[0];

        do_locate      = false;
        draw_requested = false;               // reset draw_requested flag,
                                              // now that we've finished drawing
        return;
      }

      gl.glEnable( GL.GL_DEPTH_TEST );
      if ( !do_select )
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
		
      reshape( drawable, 0, 0, size.width, size.height );

      if ( do_select )                        // set up the hit buffer
      {
        n_hits = 0;
        for ( int i = 0; i < HIT_BUFFER_SIZE; i++ )
          hit_buffer.put( i, 0 );
        hit_buffer.clear();

        gl.glSelectBuffer( HIT_BUFFER_SIZE, hit_buffer );
        gl.glRenderMode( GL.GL_SELECT );
        gl.glInitNames();
      }
      else if ( lighting_on )       // Don't need lighting in selection mode.
      {                             // Set global lighting parameters for the
                                    // whole scenegraph.
        gl.glLightModeli( GL.GL_LIGHT_MODEL_COLOR_CONTROL,
                          GL.GL_SEPARATE_SPECULAR_COLOR);

        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );

        gl.glEnable( GL.GL_LIGHTING );
                                              // Make diffuse material color
                                              // track current color, so shapes
                                              // appear with lighting on or off 
        gl.glColorMaterial( GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE );
        gl.glEnable( GL.GL_COLOR_MATERIAL );

        initializeAmbient( gl );
        initializeHeadlight( gl );
      }

                                              // now draw the scene in either
      gl.glMatrixMode( GL.GL_MODELVIEW );     // select mode or render mode 
      gl.glLoadIdentity();
      my_camera.MakeViewMatrix( drawable ); 
      my_scene.Render( drawable );
      gl.glFlush(); 

      if ( do_select )                        // switch back to render mode
      {                                       // to get the number of hits
        n_hits = gl.glRenderMode( GL.GL_RENDER );
        do_select = false;
      }

      draw_requested = false;                 // reset draw_requested flag,
                                              // now that we've finished drawing
    }
  }


  /* -----------------------------------------------------------------------
   *
   *  PRIVATE METHODS
   *
   */

  /* -------------------------- waitForDraw --------------------------- */
  /*
   *  Wait until the Renderer.display() method finishes, or for the specified
   *  time in milliseconds, whichever comes first.
   */ 
  private void waitForDraw( int max_time_ms )
  { 
    int SLEEP_TIME   = 10;                // sleep 1/100 sec before checking 
                                          // flag again.
    int elapsed_time = 0;                

    while ( draw_requested && (elapsed_time < max_time_ms ) )
    {
      try 
      {
        Thread.sleep( SLEEP_TIME );
      }
      catch ( Exception e )
      {
        System.out.println("Exception " + e );
        e.printStackTrace();
      }
      elapsed_time += SLEEP_TIME;
    }
  }

  /* ------------------------ initializeAmbient ----------------------- */
  /*
   *  Initialize the global ambient light, based on the current ambient color 
   *  and on/off flag.  
   */
  private void initializeAmbient( GL gl )
  {
    if ( ambient_on )
    {
      float color[] = new float[4];
      color[0] = ambient_color.getRed()/255f;
      color[1] = ambient_color.getGreen()/255f;
      color[2] = ambient_color.getBlue()/255f;
      color[3] = 1;
      gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT, color );
    }
  }


  /* ------------------------ initializeHeadlight ----------------------- */
  /*
   *  Initialize the headlight based on the current headlight color and
   *  on/off flag.  GL_LIGHT0 is the OpenGL light used for the "headlight".
   *  We put GL_LIGHT0 at the origin BEFORE making the viewing matrix RT.
   */
  private void initializeHeadlight( GL gl )
  {
    if ( headlight_on )
    { 
      gl.glMatrixMode( GL.GL_MODELVIEW );
      gl.glLoadIdentity();
      float l0_position[] = { 0, 0, 0, 1 };
      gl.glEnable( GL.GL_LIGHT0 );
      gl.glLightfv( GL.GL_LIGHT0, GL.GL_POSITION, l0_position );

      float color[] = new float[4];
      color[0] = headlight_color.getRed()/255f;
      color[1] = headlight_color.getGreen()/255f;
      color[2] = headlight_color.getBlue()/255f;
      color[3] = 1;
      gl.glLightfv( GL.GL_LIGHT0, GL.GL_AMBIENT, color );
      gl.glLightfv( GL.GL_LIGHT0, GL.GL_DIFFUSE, color );
      gl.glLightfv( GL.GL_LIGHT0, GL.GL_SPECULAR, color );
    }
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that creates a box centered at the origin and displays 
   *  it in 3D.   NOTE: The observer is at the center of the box.
   */
  public static void main( String args[] )
  {
    JoglPanel demo = new JoglPanel( new SolidBox( 1, 1, 4 ) );

    // demo.enableLighting( false );
    // demo.enableHeadlight( false );
    // demo.enableAmbient( false );
    demo.setHeadlightColor( Color.RED );
    demo.setAmbientColor( Color.BLUE );
  
    JFrame frame = new JFrame( "JoglPanel TEST" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}
