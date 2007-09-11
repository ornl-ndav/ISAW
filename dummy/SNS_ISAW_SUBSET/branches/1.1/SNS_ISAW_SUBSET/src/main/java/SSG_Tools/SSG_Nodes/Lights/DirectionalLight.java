/*
 * File:  DirectionalLight.java
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
 *  $Log: DirectionalLight.java,v $
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
 *  This class represents lights that have a direction only.  There is no
 *  finite position associated with the light.  The light can be thought of
 *  as being infinitely far away in the specified direction.
 */

public class DirectionalLight extends Light 
{
  private Vector3D direction; 

  /* -------------------------- constructor ------------------------ */
  /**
   *  Construct a directional light node, with light coming from the 
   *  specified direction.
   *
   *  @param  light_num  The number from 1 to 7 for this light.
   *  @param  direction  The direction that the light is coming from. 
   */
  public DirectionalLight( int light_num, Vector3D direction )
  {
    super( light_num );
    this.light_num = light_num;
    setDirection( direction );
  }

  /* -------------------------- setDirection -------------------------- */
  /**
   *  Change the direction of this light.
   *
   *  @param  direction  The direction that the light is coming from. 
   */
  public void setDirection( Vector3D direction )
  {
    if ( direction != null )
      this.direction = new Vector3D( direction );
    else
      this.direction = new Vector3D();
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
      direction_arr[3] = 0;                  // OpenGL uses homogeneous coord
                                             // w = 0 to for a directional light
      gl.glLightfv( light_num, GL.GL_POSITION, direction_arr );
    }
    else
      gl.glDisable( light_num );
  }

}
