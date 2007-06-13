/*
 * File:  Material.java
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
 * Modified:
 *           
 *  $Log: Material.java,v $
 *  Revision 1.6  2005/07/25 13:58:32  dennis
 *  If the diffuse color is set, this now calls glColor3fv() for the
 *  specified diffuse color.  This assumes that glColorMaterial has
 *  been enabled and set to force the diffuse color to track the current
 *  color.  glColorMaterial() is now called in the JoglPanel.  This allows
 *  the same shapes to appear, whether or not OpenGL lighting is enabled.
 *
 *  Revision 1.5  2004/12/13 05:02:27  dennis
 *  Minor fix to documentation
 *
 *  Revision 1.4  2004/12/06 18:37:32  dennis
 *  Fixed minor error in javadoc comment.
 *
 *  Revision 1.3  2004/11/15 19:18:15  dennis
 *  Minor reformatting.
 *
 *  Revision 1.2  2004/10/25 21:59:13  dennis
 *  Added to SSG_Tools CVS repository
 *
 */
package SSG_Tools.Appearance;

import java.awt.*;
import net.java.games.jogl.*;

import SSG_Tools.*;

public class Material implements IGL_Renderable
{
  private Color ambient; 
  private Color diffuse;
  private Color specular;
  private Color emission  = null;
  private int   shininess = 64;

  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a new material object with a GRAY color by default.
   */
  public Material()
  {
    setColor( Color.GRAY );
  }

  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a new material object, using the specified color for the
   *  ambient and diffuse color, and using WHITE for the specular color.
   *
   *  @param  color  the new color for this object
   */
  public Material( Color color )
  {
    setColor( color );
  }

  /* ------------------------- setColor --------------------------- */
  /**
   *  Convenience method to set the ambient and diffuse and specular
   *  colors based on a specified color.  The diffuse color is set
   *  to the specified color.  The ambient color is set to a lower 
   *  intensity version of the specified color.  The specular color
   *  is set to LIGHT_GRAY.
   *
   *  @param color  The new color to use for this object for ambient 
   *                light and diffuse reflection.
   */
  public void setColor( Color color )
  {
    ambient = new Color( color.getRed()/5, 
                         color.getGreen()/5, 
                         color.getBlue()/5 );
    diffuse  = color;
    specular = Color.LIGHT_GRAY;
  }

  /* ------------------------- setAmbient -------------------------- */
  /**
   *  Set the "color" of this object for reflection of ambient light.
   *
   *  @param color  The new color to use for this object for ambient light.
   */
  public void setAmbient( Color color )
  {
    ambient = color;
  }

  /* ------------------------- setDiffuse -------------------------- */
  /**
   *  Set the "color" of this object for diffuse reflection of light.
   *
   *  @param color  The new color to use for diffuse reflection.
   */
  public void setDiffuse( Color color )
  {
    diffuse = color;
  }

  /* ------------------------- setSpecular -------------------------- */
  /**
   *  Set the "color" of this object for specular reflection of light.
   *
   *  @param color  The new color to use for specular reflection.
   */
  public void setSpecular( Color color )
  {
    specular = color;
  }

  /* ------------------------- setShininess -------------------------- */
  /**
   *  Set the exponent to use for the shininess calculaton.  The value
   *  will be clamped to the range 0 to 128.  Larger values will make the
   *  surface appear more shiny.
   *
   *  @param exponent  The "shininess" exponent to use for specular reflection.
   */
  public void setShininess( int exponent )
  {
    if ( exponent < 0 )
      shininess = 0;

    else if ( exponent > 128 )
      shininess = 128;

    else
      shininess = exponent;
  }

  /* ------------------------- setEmission -------------------------- */
  /**
   *  Set the "color" that this object emits.
   *
   *  @param color  The new color to use for emission.
   */
  public void setEmission( Color color )
  {
    emission = color;
  }

  /* ------------------------- getAmbient -------------------------- */
  /**
   *  Get the "color" of this object for reflection of ambient light.
   *
   *  @return  the color used for this object for ambient light.
   */
  public Color getAmbient()
  {
    return ambient;
  }

  /* ------------------------- getDiffuse -------------------------- */
  /**
   *  Get the "color" of this object for diffuse reflection of light.
   *
   *  @return  the color used for this object for diffuse reflection.
   */
  public Color getDiffuse()
  {
    return diffuse; 
  }

  /* ------------------------- getSpecular -------------------------- */
  /**
   *  Get the "color" of this object for specular reflection of light.
   *
   *  @return  the color used for this object for specular reflection.
   */
  public Color getSpecular()
  {
    return specular;
  }

  /* ------------------------- getShininess -------------------------- */
  /**
   *  Get the exponent used for the shininess calculaton.  
   *
   *  @return exponent  The exponent used for specular reflection
   *                    clamped to the range [0,128].
   */
  public int getShininess()
  {
    return shininess;
  }


  /* ------------------------- getEmission -------------------------- */
  /**
   *  Get the "color" that this object emits.
   *
   *  @return  the color this object emits.
   */
  public Color getEmission()
  {
    return emission;
  }


  /* ----------------------- copy constructor ----------------------- */
  /**
   *  Construct a new material object with the same values as the 
   *  specified material.
   *
   *  @param material  The material whose values are to be copied.
   */
  public Material( Material material )
  {
    ambient  = material.ambient;
    diffuse  = material.diffuse; 
    specular = material.specular;
    emission = material.emission;

    shininess = material.shininess;
  }


  /* ---------------------------- Render ------------------------------ */
  /**
   *  Use OpenGL calls to set the material properties.
   *
   *  @param  drawable  The drawable for which the material properties are set. 
   */
  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();

    float mat[] = new float[3];

    if ( ambient != null ) 
    {
      mat[0] = ambient.getRed()/255f;
      mat[1] = ambient.getGreen()/255f;
      mat[2] = ambient.getBlue()/255f;
      gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, mat );
    }
                              // NOTE: The JoglPanel enables ColorMaterial with
                              //       the material diffuse color tracking
                              //       the current color set by glColor3f()
                              //       so here we can just call glColor3f()
    if ( diffuse != null )
    {
      mat[0] = diffuse.getRed()/255f;
      mat[1] = diffuse.getGreen()/255f;
      mat[2] = diffuse.getBlue()/255f;
      gl.glColor3fv( mat ); 
    }

    if ( specular != null )
    {
      mat[0] = specular.getRed()/255f;
      mat[1] = specular.getGreen()/255f;
      mat[2] = specular.getBlue()/255f;
      gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, mat );
    }

    if ( emission != null )
    {
      mat[0] = emission.getRed()/255f;
      mat[1] = emission.getGreen()/255f;
      mat[2] = emission.getBlue()/255f;
      gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, mat );
    }

    gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess );
  }
  
}
