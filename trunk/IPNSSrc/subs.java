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
 * Revision 1.1  2003/01/20 16:19:44  rmikk
 * Initial Checkin
 *
 */

package IPNSSrc;
import java.lang.*;
//import org.netlib.util.*;



public class subs {

// C
// C*******************   SUBROUTINE LAUE   ********************
// C
// C***  THIS SUBROUTINE WILL CALCULATE THE DIFFRACTION VECTOR
// C***  COORDINATES IN THE CRYSTAL-FIXED ORTHONORMAL SYSTEM
// C***  FOR A REFLECTION.  THE CODE IS TAKEN FROM THE UMAT3
// C***  PROGRAM.
// C***
// C***  A.J. SCHULTZ      12/7/80
// C
// C
static double rad = 57.29578;
// C
// 

static double chi,phi,omega,deta,detd;
// C***  CALCULATE DIFFRACTION VECTORS IN THE DIFFRACTOMETER-FIXED
// C***  SYSTEM.  THE SYSTEM USED HERE IS SLIGHTLY DIFFERENT THAN
// C***  THAT IN INTERNATIONAL TABLES, VOL. IV, PAGE 277.  IN THIS
// C***  SYSTEM, THE UNIT VECTORS ARE FIXED WITH AD ALWAYS
// C***  DIRECTED TOWARD THE SOURCE FROM THE CRYSTAL, BD IN THE
// C***  HORIZONTAL PLANE AT TWO THETA OF +90 DEGREES, AND CD
// C***  POINTING UP AT CHI OF 180 DEGREES.
// C

public static void laue (double xcm,
double ycm,
double wl,
doubleW xg,
doubleW yg,
doubleW zg)  {
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
r1 = Math.sqrt(Math.pow(xcm, 2)+Math.pow(ycm, 2));
r2 = Math.sqrt(Math.pow(r1, 2)+Math.pow(detd, 2));
zd = (ycm/r2)/wl;

// C
// C***  INITIALLY ASSUME DETA = 0.0 DEG. AND THE ORIGIN IS AT THE REAL
// C***  CRYSTAL.
// C
//yd = (xcm/r2)/wl;
//$ ARt xd = -(detd/r2)/wl;
//xd =detd/r2/wl;
//if (deta != 0.0)  {
    // C
// C***  ROTATE TO CORRECT DETECTOR ANGLE
// C
//Art$$ ang = -deta/rad;
//ang= deta/rad;
//xt = xd;
//yt = yd;
//xd = xt*Math.cos(ang)+yt*Math.sin(ang);
//yd = -xt*Math.sin(ang)+yt*Math.cos(ang);
//***************** NEW *********************************
ang = deta/rad;
xd = detd*Math.cos(ang)+ xcm*Math.sin(ang);
yd = detd*Math.sin(ang) -ycm*Math.cos(ang);

yd = yd/r2/wl;
xd  = xd/r2/wl;
// C
// C***  TRANSLATE ORIGIN TO RECIPROCAL LATTICE ORIGIN
 
      // Close if()
// C
label390:
   //Dummy.label("Laue",390);
//Art xd = (1.0/wl)+xd;
 xd=-(1.0/wl)+xd;
// C
// C***  ROTATE INTO THE CRYSTAL-FIXED ORTHONORMAL SYSTEM.
// C***  THE DIRECTIONS OF POSITIVE ROTATION FOR CHI AND
// C***  OMEGA ARE OPPOSITE TO THOSE IN THE INTERNATIONAL
// C***  TABLES, VOL. IV, PAGE 277.
// C

cp = Math.cos(phi/rad);
sp = Math.sin(phi/rad);
/*  $$Arts. Note the transformation is the inverse
cc = Math.cos(-chi/rad);
sc = Math.sin(-chi/rad);
*/

cc = Math.cos(chi/rad);
sc = Math.sin(chi/rad);
co = Math.cos(-omega/rad);
so = Math.sin(-omega/rad);
// C***  OMEGA ROTATION
xt = xd;
yt = yd;
xg.val = xt*co+yt*so;
yg.val = -xt*so+yt*co;
// C***  CHI ROTATION
yt = yg.val;
zt = zd;
yg.val = yt*cc+zt*sc;
zg.val = -yt*sc+zt*cc;
// C***  PHI ROTATION
xt = xg.val;
yt = yg.val;
xg.val = xt*cp+yt*sp;
yg.val = -xt*sp+yt*cp;
//System.out.println("Laue--"+cp+","+sp+","+cc+","+sc+","+co+","+so);
//System.out.println("      "+xt+","+yt+","+yg.val+","+zg.val+","+xg.val);
// C
//Dummy.go_to("Laue",999999);
//Dummy.label("Laue",999999);
return;
   }


// 

// 

public static void tstvol (double [] u, 
doubleW volume)  {
int _u_offset=0;
double [] a= new double[(3)];
double [] b= new double[(3)];
double [] c= new double[(3)];
double [] axb= new double[(3)];
a[(1)- 1] = u[(1)- 1+(1- 1)*3+ _u_offset];
a[(2)- 1] = u[(1)- 1+(2- 1)*3+ _u_offset];
a[(3)- 1] = u[(1)- 1+(3- 1)*3+ _u_offset];
b[(1)- 1] = u[(2)- 1+(1- 1)*3+ _u_offset];
b[(2)- 1] = u[(2)- 1+(2- 1)*3+ _u_offset];
b[(3)- 1] = u[(2)- 1+(3- 1)*3+ _u_offset];
c[(1)- 1] = u[(3)- 1+(1- 1)*3+ _u_offset];
c[(2)- 1] = u[(3)- 1+(2- 1)*3+ _u_offset];
c[(3)- 1] = u[(3)- 1+(3- 1)*3+ _u_offset];
// 
// C***	V = A dot (B cross C)
// 
cross(b,c,axb);
// 
volume.val = a[(1)- 1]*axb[(1)- 1]+a[(2)- 1]*axb[(2)- 1]+a[(3)- 1]*axb[(3)- 1];
volume.val = 1./volume.val;
// 
//Dummy.label("Tstvol",999999);
return;
   }

// 
// C***	Calculate the cross product C = A X B
// 
// 

public static void cross (double [] a,
double [] b,
double [] c)  {
 int _a_offset=0;
 int _b_offset=0;
int _c_offset=0;
c[(1)- 1+ _c_offset] = a[(2)- 1+ _a_offset]*b[(3)- 1+ _b_offset]-a[(3)- 1+ _a_offset]*b[(2)- 1+ _b_offset];
c[(2)- 1+ _c_offset] = a[(3)- 1+ _a_offset]*b[(1)- 1+ _b_offset]-a[(1)- 1+ _a_offset]*b[(3)- 1+ _b_offset];
c[(3)- 1+ _c_offset] = a[(1)- 1+ _a_offset]*b[(2)- 1+ _b_offset]-a[(2)- 1+ _a_offset]*b[(1)- 1+ _b_offset];
// 
//Dummy.label("Cross",999999);
return;
   }


} // End class.
