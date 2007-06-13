/*
 * File:  PointLight.java
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
 *  $Log: PointLight.java,v $
 *  Revision 1.3  2005/07/14 21:49:05  dennis
 *  Switched from local copy of Vector3D, etc. to using Vector3D, etc.
 *  from gov.anl.ipns.MathTools.Geometry.
 *
 *  Revision 1.2  2004/12/13 05:02:27  dennis
 *  Minor fix to documentation
 *
 *  Revision 1.1  2004/10/25 21:59:54  dennis
 *  Added to SSG_Tools CVS repository
 *
 */

package SSG_Tools.SSG_Nodes.Lights;

import net.java.games.jogl.*;
import gov.anl.ipns.MathTools.Geometry.*;

/**
 *  This class represents lights that have a particular position in the
 *  3D virtual world.  The position and the way that the light intensity
 *  is attenuated can be changed.   The methods that change the attenuation
 *  factors alter the coefficents k0, k1 and k2 on the factor 
 *  1/( k0 + k1*d + k2*d^2 ) in the OpenGL lighting equation.
 */

public class PointLight extends Light 
{
  private Vector3D position; 
  private float    constant_attenuation  = 1;
  private float    linear_attenuation    = 0;
  private float    quadratic_attenuation = 0;

  /* --------------------------- constructor ------------------------- */
  /**
   *  Construct a new point light node, using the position.
   *
   *  @param  light_num  The number from 1 to 7 for this light.
   *  @param  position   The position in 3D for the point light 
   */
  public PointLight( int light_num, Vector3D position )
  {
    super( light_num );
    this.light_num = light_num;
    setPosition( position );
  }

  /* -------------------------- setPosition -------------------------- */
  /**
   *  Change the position of this light.
   *
   *  @param  position   The position in 3D for the point light 
   */
  public void setPosition( Vector3D position )
  {
    if ( position != null )
      this.position = new Vector3D( position );
    else
      this.position = new Vector3D();
  }

  /* --------------------- setConstantAttenuation --------------------- */
  /**
   *  Set the constant attenuation factor in the OpenGL lighting model. 
   *  The default value is 1.
   *
   *  @param attenuation  The new value to use for the constant attenuation
   *                      coefficient.  This must be non-negative.
   */
  public void setConstantAttenuation( float attenuation )
  {
    if ( attenuation >= 0 )
      constant_attenuation = attenuation;
  }

  /* ----------------------- setLinearAttenuation ---------------------- */
  /**
   *  Set the linear attenuation factor in the OpenGL lighting model. 
   *  The default value is 0.
   *
   *  @param attenuation  The new value to use for the linear attenuation
   *                      coefficient.  This must be non-negative.
   */
  public void setLinearAttenuation( float attenuation )
  {
    if ( attenuation >= 0 )
      linear_attenuation = attenuation;
  }

  /* --------------------- setQuadraticAttenuation --------------------- */
  /**
   *  Set the quadratic attenuation factor in the OpenGL lighting model. 
   *  The default value is 0.
   *
   *  @param attenuation  The new value to use for the quadratic attenuation
   *                      coefficient.  This must be non-negative.
   */
  public void setQuadraticAttenuation( float attenuation )
  {
    if ( attenuation >= 0 )
      quadratic_attenuation = attenuation;
  }

  /* ---------------------------- Render ------------------------------ */
  /**
   *  Use OpenGL calls to use the specified light, if it is enabled.
   *
   *  @param  drawable  The drawable on which the light is to be used.
   */
  public void Render( GLDrawable drawable )
  {
    GL gl = drawable.getGL();
 
    if ( on )
    {
      super.Render( drawable );
      float position_arr[] = position.get();
      gl.glLightfv( light_num, GL.GL_POSITION, position_arr );
      gl.glLightf( light_num, GL.GL_CONSTANT_ATTENUATION, constant_attenuation);
      gl.glLightf( light_num, GL.GL_LINEAR_ATTENUATION, linear_attenuation );
      gl.glLightf( light_num, GL.GL_QUADRATIC_ATTENUATION, 
                                                        quadratic_attenuation);
    }
    else
      gl.glDisable( light_num );
  }

}
