/*
 * File:  SpotLight.java
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
 *  $Log: SpotLight.java,v $
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
 *  This class represents spot lights that have both a position and direction.
 */

public class SpotLight extends PointLight 
{
  private Vector3D direction; 
  private float    cutoff_angle;
  private int      spot_exponent;

  /* --------------------------- constructor ---------------------------- */
  /**
   *  Construct a SpotLight light node, with the light at a specified 
   *  position pointing in a specified direction.
   *
   *  @param  light_num  The number from 1 to 7 for this light.
   *  @param  position   The position in 3D for the point light
   *  @param  direction  The direction that the spotlight is pointing towards. 
   */
  public SpotLight( int light_num, Vector3D position, Vector3D direction )
  {
    super( light_num, position );
    this.light_num = light_num;
    setDirection( direction );
    setSpotExponent( 10 );
    setCutoffAngle( 0.2f );
  }

  /* --------------------------- setDirection --------------------------- */
  /**
   *  Set the direction this spotlight is pointing towards.
   *
   *  @param  direction  The direction that the spotlight is pointing towards. 
   */
  public void setDirection( Vector3D direction )
  {
    if ( direction != null )
      this.direction = new Vector3D( direction );
    else
      this.direction = new Vector3D();
  }

  /* ------------------------- setSpotExponent -------------------------- */
  /**
   *  Set the exponent used to calculate the rate at which the light
   *  falls off away from the center of the beam.
   *
   *  @param  exponent  The exponent in [0,128] that controls how the light
   *                    intensity decreases away from the center of the beam. 
   */
  public void setSpotExponent( int exponent )
  {
    if ( exponent < 0 )
      exponent = 0;
    else if ( exponent > 128 )
      exponent = 128;

    spot_exponent = exponent;
  }

  /* ------------------------ setCutoffAngle ------------------------ */
  /**
   *  Set the maximum angle (in radians) away from the center of the beam
   *  where the spotlight intensity will be used.
   *
   *  @param  angle  The angle in [0,pi/2] outside of which the intensity
   *                 of the spotlight is taken to be zero.  Values are
   *                 clamped to the interval [0,pi/2]. 
   */
  public void setCutoffAngle( float angle )
  {
    if ( angle < 0 )
      angle = 0;
    else if ( angle > Math.PI/2 )
      angle = (float)(Math.PI/2);

    cutoff_angle = (float)(angle * 180 / Math.PI/2);
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
      float direction_arr[] = direction.get();
      gl.glLightfv( light_num, GL.GL_SPOT_DIRECTION, direction_arr );
      gl.glLightf( light_num, GL.GL_SPOT_CUTOFF, cutoff_angle );
      gl.glLighti( light_num, GL.GL_SPOT_EXPONENT, spot_exponent );
    }
    else
      gl.glDisable( light_num );
  }

}
