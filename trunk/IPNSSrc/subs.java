/* File: subs.java
 *  Produced by f2java.  f2java is part of the Fortran-
 *  -to-Java project at the University of Tennessee Netlib
 *  numerical software repository.
 *
 *  Original authorship for the BLAS and LAPACK numerical
 *  routines may be found in the Fortran source, available at
 *  www.netlib.org.
 *
 
 *  The f2j compiler code was written by
 *  David M. Doolin (doolin@cs.utk.edu) and
 *  Keith  Seymour (seymour@cs.utk.edu)

 * The original code is part of a suite of FORTRAN blind analysis package
 * for Single Crystal Diffractometer data at the IPNS division of
 * Argonne National Laboratory. The original code was
 *	Written by R. A. Jacobson
 * 
 *	Modified by: 
 *		G. Anderson
 *		A. J. Schultz
 *		R. G. Teller
 *
 *	Current version:	August, 1999	A. J. Schultz
 *	Linux version:		January, 2002	A. J. Schultz
 *      Modified Subroutine Laue for axes change  
 *                              January  2003,  A.J.Schultz
 *      Converted to Java, eliminated Goto's, Fixed Do loops, added while loops,etc.
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.5  2003/02/21 16:54:05  pfpeterson
 * Removed commented out code.
 *
 * Revision 1.4  2003/02/18 19:33:50  dennis
 * Removed ^M characters.
 *
 * Revision 1.3  2003/02/10 18:58:06  pfpeterson
 * Created a second version of tstvol which returns the value rather
 * than augmenting one of the arguments.
 *
 * Revision 1.2  2003/02/10 18:36:18  pfpeterson
 * Reformatted code.
 *
 * Revision 1.1  2003/01/20 16:19:44  rmikk
 * Initial Checkin
 *
 */

package IPNSSrc;
import java.lang.*;


/* *******************   SUBROUTINE LAUE   ********************
 * THIS SUBROUTINE WILL CALCULATE THE DIFFRACTION VECTOR
 * COORDINATES IN THE CRYSTAL-FIXED ORTHONORMAL SYSTEM
 * FOR A REFLECTION.  THE CODE IS TAKEN FROM THE UMAT3
 * PROGRAM.
 *
 *  A.J. SCHULTZ      12/7/80
 */
public class subs {

  static final double rad = 180./Math.PI;

  static double chi,phi,omega,deta,detd;

  /**
   * CALCULATE DIFFRACTION VECTORS IN THE DIFFRACTOMETER-FIXED
   * SYSTEM.  THE SYSTEM USED HERE IS SLIGHTLY DIFFERENT THAN
   * THAT IN INTERNATIONAL TABLES, VOL. IV, PAGE 277.  IN THIS
   * SYSTEM, THE UNIT VECTORS ARE FIXED WITH AD ALWAYS
   * DIRECTED TOWARD THE SOURCE FROM THE CRYSTAL, BD IN THE
   * HORIZONTAL PLANE AT TWO THETA OF +90 DEGREES, AND CD
   * POINTING UP AT CHI OF 180 DEGREES.
   */
  public static void laue (double xcm, double ycm, double wl,
                           doubleW xg, doubleW yg, doubleW zg)  {
    double xy= 0.0;
    double yt= 0.0;
    double zt= 0.0;
    double xd= 0.0;
    double yd= 0.0;
    double zd= 0.0;
    double xt= 0.0;

    double co= 0.0;
    double so= 0.0;
    double sc= 0.0;
    double cc= 0.0;
    double sp= 0.0;
    double cp= 0.0;
    double ang= 0.0;
    double r2= 0.0;
    double r1= 0.0;
    r1 = Math.sqrt(xcm*xcm+ycm*ycm);
    r2 = Math.sqrt(r1*r1+detd*detd);
    zd = (ycm/r2)/wl;

    /*
     * INITIALLY ASSUME DETA = 0.0 DEG. AND THE ORIGIN IS AT THE REAL
     * CRYSTAL.
     */
    ang = deta/rad;
    xd = detd*Math.cos(ang)+ xcm*Math.sin(ang);
    yd = detd*Math.sin(ang) -ycm*Math.cos(ang);
    
    yd = yd/r2/wl;
    xd  = xd/r2/wl;

    // TRANSLATE ORIGIN TO RECIPROCAL LATTICE ORIGIN
    xd=-(1.0/wl)+xd;

    /*
     * ROTATE INTO THE CRYSTAL-FIXED ORTHONORMAL SYSTEM.
     * THE DIRECTIONS OF POSITIVE ROTATION FOR CHI AND
     * OMEGA ARE OPPOSITE TO THOSE IN THE INTERNATIONAL
     * TABLES, VOL. IV, PAGE 277.
     */
    cp = Math.cos(phi/rad);
    sp = Math.sin(phi/rad);
    
    cc = Math.cos(chi/rad);
    sc = Math.sin(chi/rad);
    co = Math.cos(-omega/rad);
    so = Math.sin(-omega/rad);
    // OMEGA ROTATION
    xt = xd;
    yt = yd;
    xg.val = xt*co+yt*so;
    yg.val = -xt*so+yt*co;
    // CHI ROTATION
    yt = yg.val;
    zt = zd;
    yg.val = yt*cc+zt*sc;
    zg.val = -yt*sc+zt*cc;
    // PHI ROTATION
    xt = xg.val;
    yt = yg.val;
    xg.val = xt*cp+yt*sp;
    yg.val = -xt*sp+yt*cp;
    //System.out.println("Laue--"+cp+","+sp+","+cc+","+sc+","+co+","+so);
    //System.out.println("      "+xt+","+yt+","+yg.val+","+zg.val+","+xg.val);
    return;
  }

  /**
   * @see #tstvol(double[])
   */
  public static void tstvol (double [] u, doubleW volume)  {
    volume.val=tstvol(u);
    return;
  }

  /**
   *
   */
  public static double tstvol (double [] u){
    int u_offset=0;
    double [] a= new double[(3)];
    double [] b= new double[(3)];
    double [] c= new double[(3)];
    double [] axb= new double[(3)];
    double volume=0.0;

    a[0] = u[0 + 0*3 + u_offset];
    a[1] = u[0 + 1*3 + u_offset];
    a[2] = u[0 + 2*3 + u_offset];
    b[0] = u[1 + 0*3 + u_offset];
    b[1] = u[1 + 1*3 + u_offset];
    b[2] = u[1 + 2*3 + u_offset];
    c[0] = u[2 + 0*3 + u_offset];
    c[1] = u[2 + 1*3 + u_offset];
    c[2] = u[2 + 2*3 + u_offset];

    cross(b,c,axb);

    volume = a[0]*axb[0]+a[1]*axb[1]+a[2]*axb[2];
    volume = 1./volume;

    return volume;
  }

  /**
   * Calculate the cross product C = A X B
   */
  public static void cross (double [] a, double [] b, double [] c)  {
    int a_offset=0;
    int b_offset=0;
    int c_offset=0;

    c[0+c_offset] = a[1+a_offset]*b[2+b_offset]-a[2+a_offset]*b[1+b_offset];
    c[1+c_offset] = a[2+a_offset]*b[0+b_offset]-a[0+a_offset]*b[2+b_offset];
    c[2+c_offset] = a[0+a_offset]*b[1+b_offset]-a[1+a_offset]*b[0+b_offset];

    return;
  }
}
