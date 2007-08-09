/*
 * File:  Tester.java
 *
 */
package SSG_Tools.SSG_Nodes.VerySimpleShapes;

import java.awt.*;
import javax.swing.*;

import net.java.games.jogl.*;

import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.Viewers.*;

import SSG_Tools.Cameras.*;
import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.Viewers.Controls.*;
import SSG_Tools.SSG_Nodes.StateControls.*;
//import SSG_Tools.SSG_Nodes.SimpleShapes.*;
import SSG_Tools.SSG_Nodes.Groups.Transforms.*;

/**
 *  This class draws a scene consisting of an array of colored shapes.
 *  It is just intended as a test/demo of one possible strategy for
 *  using OpenGL to render detector pixels.  Each shape has a separate
 *  color and the full list is one large display list.
 */

public class Tester extends Group
{
  public static final int SIZE = 256;

  private Color[] colors = { Color.WHITE, 
                             Color.RED,   Color.CYAN,
                             Color.GREEN, Color.MAGENTA,
                             Color.BLUE,  Color.YELLOW  };
                                 
  boolean is_list      = false;
  boolean rebuild_list = false;
  int     list_id = -1;

  int     n_rows,
          n_cols;
  int     num_render = 0;

  /* --------------------------- Constructor --------------------------- */
  /**
   *  Construct the scene objects with default sizes, centered at the
   *  origin.
   */
  public Tester( int n_rows, int n_cols )
  {
    Vector3D origin = new Vector3D( 0, 0, 0 );
    Vector3D x_axis = new Vector3D( 1, 0, 0 );
    Vector3D y_axis = new Vector3D( 0, 1, 0 );

    this.n_rows = n_rows;
    this.n_cols = n_cols;

    Vector3D base = new Vector3D( 1, 0, 0 );
    Vector3D up   = new Vector3D( 0, 1, 0 );

    SolidBox box = new SolidBox( 1, 1, 1 );

    int index = 1;
    for ( int row = 0; row < n_rows; row++ )
    for ( int col = 0; col < n_cols; col++ )
    {
      Vector3D position = new Vector3D(2*row, 2*col, 0 );

      OrientationTransform trans = new OrientationTransform(base, up, position);
      trans.setPickID( 2 * index + 1 );
      trans.addChild( box );
      addChild( trans );

      box.setPickID( 2 * index );

      if ( row == n_rows/2 && col == n_cols/2 )
        addChild( new LightingOnOff( false ) );

      index++;
    }

    addChild( new LightingOnOff( true ) );
  }


  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();
    int num_children = numChildren();

    float color = num_render % 20;
    num_render++;

    color = color + 10;
    color = color/30;
    System.out.println("Color = " + color );

    for ( int i = 0; i < num_children; i++ )
    {
      gl.glColor3f( color, color, color );
      getChild(i).Render(drawable);
    }
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that constructs an instance of the scene and displays 
   *  it in 3D for testing purposes.  
   */
  public static void main( String args[] )
  {
    System.out.println("Start of Tester main()");
    Tester scene = new Tester( SIZE, SIZE ); 

    JoglPanel demo = new JoglPanel( scene, true );
//    demo.enableHeadlight( true );

    Camera camera = demo.getCamera();
    camera.setVRP( new Vector3D( 200, 200, 0 ) );
    camera.setCOP( new Vector3D( 200, 200, 700 ) );
    camera.SetViewVolume( 20, 3000, 40 );
    demo.setCamera( camera );

    new MouseArcBall( demo );
  
    JFrame frame = new JFrame( "Shape Array" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible( true );
  }

}
