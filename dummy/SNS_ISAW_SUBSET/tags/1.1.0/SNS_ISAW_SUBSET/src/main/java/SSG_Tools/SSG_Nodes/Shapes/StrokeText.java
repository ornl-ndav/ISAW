/*
 * File:  StrokeText.java
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log: StrokeText.java,v $
 * Revision 1.3  2005/01/10 16:10:26  dennis
 * Fixed minor error that would cause an extra glTranslate() to
 * be called when it would not be used.
 *
 * Revision 1.2  2004/06/18 19:58:51  dennis
 * Now imports Fonts package since StrokeFont.java moved
 * to Fonts directory.
 *
 * Revision 1.1  2004/06/18 19:21:29  dennis
 * Moved to Shapes package.
 *
 * Revision 1.2  2004/06/02 15:17:00  dennis
 * Added java docs with links to the java classes with the
 * Hershey font data.
 *
 * Revision 1.1  2004/06/01 03:43:33  dennis
 * Initial version of classes for drawing strings as sequences of
 * line segments, using the "Hershey" fonts.
 */

package SSG_Tools.SSG_Nodes.Shapes;

import java.awt.*;
import javax.swing.*;

import net.java.games.jogl.*;

import gov.anl.ipns.MathTools.Geometry.*;
import SSG_Tools.Fonts.*;

import SSG_Tools.Viewers.*;
import SSG_Tools.SSG_Nodes.*;
import SSG_Tools.SSG_Nodes.StateControls.*;
import SSG_Tools.Appearance.*;


/**
 *  A StrokeText object draws a character string with a specified size, 
 *  orientation and alignment, using a StrokedFont object to draw the 
 *  individual characters as a sequence of line segments.  
 *  The "Simplex" fonts are drawn with single
 *  lines.  The "Duplex" and "Complex" fonts are draw with pairs of lines,
 *  with "Complex" fonts being somewhat more elaborate than "Duplex" fonts
 *  of the same type.  For example, RomanComplex includes serifs on the
 *  characters whereas RomanDuplex does not.  "Triplex" fonts are drawn with
 *  sets of three lines.
 *
 *  @see SSG_Tools.Fonts.CyrilicComplex
 *  @see SSG_Tools.Fonts.GothicBritishTriplex
 *  @see SSG_Tools.Fonts.GothicGermanTriplex
 *  @see SSG_Tools.Fonts.GothicItalianTriplex
 *  @see SSG_Tools.Fonts.GreekComplex
 *  @see SSG_Tools.Fonts.GreekSimplex 
 *  @see SSG_Tools.Fonts.ItalicComplex 
 *  @see SSG_Tools.Fonts.ItalicTriplex 
 *  @see SSG_Tools.Fonts.RomanComplex 
 *  @see SSG_Tools.Fonts.RomanDuplex 
 *  @see SSG_Tools.Fonts.RomanSimplex 
 *  @see SSG_Tools.Fonts.RomanTriplex 
 *  @see SSG_Tools.Fonts.ScriptComplex 
 *  @see SSG_Tools.Fonts.ScriptSimplex 
 */

public class StrokeText extends Shape
{
  public static final byte HORIZ_LEFT   = 0;
  public static final byte HORIZ_CENTER = 1;
  public static final byte HORIZ_RIGHT  = 2;

  public static final byte VERT_TOP    = 0;
  public static final byte VERT_CAP    = 1;
  public static final byte VERT_HALF   = 2;
  public static final byte VERT_BASE   = 3;
  public static final byte VERT_BOTTOM = 4;

  private StrokeFont font;
  private String     str;
  private float      char_height = 1;
  private byte       h_align     = HORIZ_LEFT;
  private byte       v_align     = VERT_HALF;
  private Tran3D     orientation = new Tran3D();
  private Vector3D   position    = new Vector3D();

  private boolean    is_list     = false;
  private boolean    rebuild_list;
  private int        list_id;

  /* ------------------------ Constructor --------------------------- */
  /**
   *  Construct a StrokeText object for the specified string and font.  The
   *  string will have have character height 1, positioned at the origin 
   *  in the x,y plane with base in the +x direction and up in the +y
   *  direction by default.
   * 
   *  @param text  The text string to draw when this object is drawn.
   *  @param font  The font to use when this object is drawn.
   */
  public StrokeText( String text, StrokeFont font )
  {
    this.font = font;   
    this.str  = text;
    rebuild_list = true;
  }


  /* ------------------------ StringWidth --------------------------- */
  /**
   *  Calculate the width of the current string in 3D "world"
   *  coordinates.
   */ 
  public float StringWidth()
  {
    float width = 0;
    for ( int i = 0; i < str.length(); i++ )
      width += font.CharWidth( str.charAt(i) );
    width *= char_height / font.CharHeight();
    return width;
  }


  /* ------------------------ setText --------------------------- */
  /**
   *  Set a new string for this object.
   *
   *  @param text The text string to draw when this object is drawn.
   */
  public void setText( String text )
  {
    str = text;
    rebuild_list = true;
  }


  /* ------------------------ setFont --------------------------- */
  /**
   *  Set a new font to use for this object.
   *
   *  @param font  The font to use when this object is drawn.
   */
  public void setText( StrokeFont font )
  {
    this.font = font;
    rebuild_list = true;
  }


  /* ------------------------ setPosition --------------------------- */
  /**
   *  Set the position of this character string in 3D "world" coordinates.
   *  The string placement, relative to the specified position, is controlled
   *  by the alignment.
   *
   *  @param  position  The position of the "alignment point" for this string
   *                    in 3D "world" coordinates. 
   */
  public void setPosition( Vector3D position )
  {
    this.position = new Vector3D( position );
    rebuild_list = true;
  }


  /* ------------------------ setOrientation --------------------------- */
  /**
   *  Set the orientation of this character string in 3D "world" coordinates.
   *
   *  @param  base    The base vector this string
   *  @param  up      The up vector this string
   */
  public void setOrientation( Vector3D base, Vector3D up )
  {
    Vector3D zero_vec = new Vector3D( 0, 0, 0 );
    orientation.setOrientation( base, up, zero_vec );
    rebuild_list = true;
  }


  /* ------------------------ setHeight ----------------------------- */
  /**
   *  Set the height of this character string in 3D "world" coordinates.
   *
   *  @param  height  The new string height
   */
  public void setHeight( float height )
  {
    char_height = height;
    rebuild_list = true;    
  }


  /* ------------------------ setAlignment ----------------------------- */
  /**
   *  Set the horizontal and vertical alignment of this character string.
   *
   *  @param  horiz_align  The horizontal alignment to use for this
   *                       string.  This should be one of:  HORIZ_LEFT,
   *                       HORIZ_CENTER or HORIZ_RIGHT.
   *  @param  vert_align   The vertical alignment to use for this 
   *                       string.  This should be one of: VERT_TOP,
   *                       VERT_CAP, VERT_HALF, VERT_BASE or VERT_BOTTOM.
   */
  public void setAlignment( int horiz_align, int vert_align )
  {
    h_align = (byte)horiz_align;
    v_align = (byte)vert_align;
    rebuild_list = true;
  }


  /* ------------------------------ Render ---------------------------- */
  /**
   *  Actually draw the string in the specified font with the specified
   *  height, alignment, etc.  NOTE: This should not be called directly
   *  from application code; it is called "automatically" when the 
   *  window is rendered.
   *
   *  @param drawable  The drawable to which the text is drawn.
   */
  public void Render( GLDrawable drawable )
  {
    float center;
    float right;
    float scale;
    if ( char_height != 0 )
      scale = char_height / font.CharHeight();
    else
      scale = 1;

    GL gl = drawable.getGL();

    if ( !is_list || rebuild_list )
    {
      if ( !is_list )
        list_id = gl.glGenLists(1);

      gl.glNewList( list_id, GL.GL_COMPILE_AND_EXECUTE );

      preRender( drawable );
      gl.glLineWidth( 1 );

      gl.glEnable( GL.GL_LINE_SMOOTH );
      gl.glEnable( GL.GL_NORMALIZE );

      gl.glPushMatrix();
      gl.glTranslatef(position.get()[0], position.get()[1], position.get()[2]);
      gl.glScalef( scale, scale, scale );

      float m[][] = orientation.get();     // pack the orientation transform
      float vals[] = new float[16];        // into a list in column major order
      int k = 0;                           // for OpenGL
      for ( int col = 0; col < 4; col++ )
        for ( int row = 0; row < 4; row++ )
        {
          vals[k] = m[row][col];
          k++;
        }
      gl.glMultMatrixf( vals );

      switch ( v_align )
      {
        case VERT_TOP    : gl.glTranslatef( 0.0f, -font.Top(), 0.0f );   break;
        case VERT_CAP    : gl.glTranslatef( 0.0f, -font.Cap(), 0.0f );   break;
        case VERT_HALF   : gl.glTranslatef( 0.0f, -font.Half(), 0.0f );  break;
        case VERT_BASE   : gl.glTranslatef( 0.0f, -font.Base(), 0.0f );  break;
        case VERT_BOTTOM : gl.glTranslatef( 0.0f, -font.Bottom(), 0.0f );
      }

      switch ( h_align )
      {
        case HORIZ_LEFT   : gl.glTranslatef( -font.LeftEdge(), 0.0f, 0.0f );
                            break;
        case HORIZ_CENTER : center = StringWidth()/(2*scale) + font.LeftEdge();
                            gl.glTranslatef( -center, 0.0f, 0.0f );            
                            break;
        case HORIZ_RIGHT  : right = StringWidth()/scale + font.LeftEdge();
                            gl.glTranslatef( -right, 0.0f, 0.0f );
      }

      for ( int i = 0; i < str.length(); i++ )
      {
        font.DrawCharacter( drawable, str.charAt(i) );
        if ( i < str.length() - 1 )
          gl.glTranslatef( font.CharWidth( str.charAt(i) ), 0.0f, 0.0f );
      }
      gl.glPopMatrix();

      postRender( drawable );

      gl.glDisable( GL.GL_NORMALIZE );
      gl.glDisable( GL.GL_LINE_SMOOTH );
      gl.glEndList();

      is_list      = true;
      rebuild_list = false;
    }
    else
      gl.glCallList( list_id );
  }


  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program that tests this object by displaying it in a frame. 
   */
  public static void main( String args[] )
  {
    StrokeFont font = new RomanSimplex();
    StrokeText x_label = new StrokeText( "  X-Axis", font );
    StrokeText y_label = new StrokeText( "  Y-Axis", font );
    StrokeText z_label = new StrokeText( "  Z-Axis", font );

    Group group = new Group();
    group.addChild( new LightingOnOff(false) );
    group.addChild( x_label );
    group.addChild( y_label );
    group.addChild( new LightingOnOff(true) );
    group.addChild( z_label );

    Vector3D base = new Vector3D( 0, 1, 0 );
    Vector3D up   = new Vector3D( -1, 0, 0 );
    y_label.setOrientation( base, up );

    base = new Vector3D( 0, 0, -1 );
    up   = new Vector3D( 0, 1, 0 );
    z_label.setOrientation( base, up );

    Vector3D position = new Vector3D( 0, 0, 8 );
    z_label.setPosition( position );

    Material material = new Material();
    material.setColor( Color.RED );
    Appearance appearance = new Appearance();
    appearance.setMaterial( material );
    x_label.setAppearance( appearance );

    material = new Material();
    appearance = new Appearance();
    material.setColor( Color.GREEN );
    appearance.setMaterial( material );
    y_label.setAppearance( appearance );

    material = new Material();
    material.setEmission( Color.BLUE );
    appearance = new Appearance();
    appearance.setMaterial( material );
    z_label.setAppearance( appearance );

    JoglPanel demo = new JoglPanel( group );
    demo.setHeadlightColor( Color.WHITE );

    demo.getCamera().setCOP( new Vector3D( 8,9,10 ) );

    JFrame frame = new JFrame( "Font Test" );
    frame.setSize(500,517);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.getContentPane().add( demo.getDisplayComponent() );
    frame.setVisible(true);
  }

}
