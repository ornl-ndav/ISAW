
/* File: blind.java
 *  Produced by f2java.  f2java is part of the Fortran-
 *  -to-Java project at the University of Tennessee Netlib
 *  numerical software repository.

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
 * Revision 1.1  2003/01/20 16:18:56  rmikk
 * Initial Checkin
 *
 */

package IPNSSrc;
import java.lang.*;
//import org.netlib.util.*;
import java.util.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.operator.DataSet.Attribute.*;
import java.io.*;
public class blind {

// 
// 
     public static double[] u = null; //orientation matrix
//Cell Scalars
     public static double A2;
     public static double B2;
     public static double C2;

     public static double DAB;
     public static double DAC;
     public static double DBC;
     public static double cellVol;

//Cell dimensions
     public static double D1;
     public static double D2;
     public static double D3;
     public static double D4;
     public static double D5;
     public static double D6;
     public static String errormessage="";
/** Finds the basis of the smallest non coplanar in Qx,Qy,Qz peaks.
*  The basis is manipulated so that B*Transpose(B) is "about" a diagonal
*/ 
public static void blaue (Vector peaks, double [] xx, 
double [] yy, 
double [] zz, 
intW lmt,
int [] seq, 
int input)  {
float[] angle= new float[ xx.length*3];

int _xx_offset=0;
int _yy_offset=0;
int _zz_offset=0;
int _seq_offset=0;
int j=0,k=0,ilog=0,hstnum=0;
 double continuelmt= 0.0;
hstnum = 0;
// 
// C  will get peaks and loop here
lmt.val = 1;
// C  NOW CALCULATE THE DIFFRACTION VECTORS XX,YY,ZZ FROM
// C  XCM, YCM, AND WL AND ROTATE THEM TO ALL ANGLES ZERO
// C  BY CALLING SUBROUTINE LAUE
// C
System.out.println("peaks size="+peaks.size());
for( int i=0;i< peaks.size();i++)
  {
   doubleW x1= new doubleW(0);
   doubleW y1= new doubleW(0);
   doubleW z1= new doubleW(0);
   double xcm,ycm,wl;
   float[] pk = (float[])(peaks.elementAt(i));
   subs.chi=(double)(pk[0]);
   subs.phi=(double)(pk[1]);
   subs.omega=(double)(pk[2]);
   subs.deta=(double)(pk[3]);
   subs.detd=(double)(pk[4]);
   int sgn=1;
   if(pk[5]<0)sgn=-1;
   int LL= (int)(.5*sgn+ 100*pk[5]);
   xcm=(LL)/100.0;
   if(pk[6]<0)sgn=-1; else sgn=1;
   LL= (int)(.5*sgn+ 100*pk[6]);
   ycm=(LL/100.0); 
   if(pk[7]<0)sgn=-1;else sgn=1;
   LL= (int)(.5*sgn+ 10000*pk[7]);
   wl=(LL/10000.0);  
   angle[i+0]= pk[5];
   angle[i+1*xx.length]=pk[6];
   angle[i+2*xx.length]=pk[7];
   seq[lmt.val-1]= (int)pk[8];
   if( wl > .00001)
    {
  //Calculate the Qx,Qy,Qz value for this peak
   subs.laue((xcm),(ycm),(wl),x1,y1,z1);
   String SS="";
   if( Double.isNaN(xcm))SS+="xcm;";
    if( Double.isNaN(ycm))SS+="ycm;";
 if( Double.isNaN(wl))SS+="wl;";
// add the omeg phi, chi deta and detd values
//   xx[(lmt.val)- 1+ _xx_offset]=x1.val;
//   yy[(lmt.val)- 1+ _yy_offset]=y1.val;
//    zz[(lmt.val)- 1+ _zz_offset]=z1.val;
     xx[lmt.val - 1]=x1.val;
     yy[lmt.val-1]=y1.val;
     zz[lmt.val-1]=z1.val;
      System.out.println("Q vals="+x1.val+","+y1.val+","+z1.val);
     if( Double.isNaN(xx[i]))SS+="xx";
     if( Double.isNaN(yy[i]))SS+="yy";
    if( Double.isNaN(zz[i]))SS+="zz";
    if( SS.length() > 0)
     System.out.println("Laue "+ i+"::"+SS);
    lmt.val++;
    }
   }
 lmt.val--;
// 
// C
// C       END OF REFLECTION INPUT
// C
label99:
   ////Dummy.label("Blaue",99);
continuelmt = (double)(lmt.val-1);
input=2;
if (input != 2 && ilog != 1)  {
    System.out.println("      #          X           Y          WL" );
System.out.println("      #          X           Y          WL" );
{
forloop2:
for (j = 1; j <= lmt.val; j++) {
System.out.print((j) + " ");
for(k = 1; k <= 3; k++)
  System.out.print(angle[(j)- 1+(k- 1)*xx.length] + " ");
System.out.print((xx[(j)- 1+ _xx_offset]) + " " + (yy[(j)-1+ _yy_offset]) + " " + (zz[(j)-1+  _zz_offset]));

System.out.println();
// c	  WRITE(16,*)J,(ANGLE(J,K),K=1,3),XX(J),YY(J),ZZ(J)
//Dummy.label("Blaue",2);
}              //  Close for() loop. 
}
}              // Close if()
else  {
  label400:
   //Dummy.label("Blaue",400);
System.out.println(" "  + "\n"  + "    #  SEQ       XCM       YCM      WL" );

{
forloop420:
System.out.println( "lmt seq.length="+lmt.val+","+seq.length);
for (j = 1; j <= lmt.val; j++) {
System.out.print((j) + " " + (seq[(j)-1+ _seq_offset]) + " ");
for(k = 1; k <= 3; k++)
  System.out.print(angle[(j)- 1+(k- 1)*xx.length] + " ");

System.out.println();
System.out.print((j) + " " + (seq[(j)-1+  _seq_offset]) + " ");
for(k = 1; k <= 3; k++)
  System.out.print(angle[(j)- 1+(k- 1)*xx.length] + " ");

System.out.println();
//Dummy.label("Blaue",420);
}              //  Close for() loop. 
}
}              //  Close else.
label490:
   //Dummy.label("Blaue",490);

// Manipulates the basis(the first 3 elements of xx,yy,and zz) so B*Tranps(B) about diagonal

abid(lmt,xx,yy,zz);
if( errormessage.length()>0)
  return;
// 
//Dummy.go_to("Blaue",999999);
//Dummy.label("Blaue",999999);
return;
   }



/** Sorts the peak vectors according to magnitude, then picks the first 3 that are not coplanar.
*  Then aair manipulates these Basis so that B*Transp(B) about diagonal.  The Basis vectors are
* stored in the first three positions of XX,YY, and ZZ. The other vectors are all moved up d
*/
private static void abid (intW lmt,
double [] xx, 
double [] yy, 
double [] zz )  {
int _xx_offset=0;
int _yy_offset=0;
int _zz_offset=0;
int i=0,j=0,idum=0,m=0,k=0,Goto=0;
double hm=0.0,d=0.0;
double [] b= new double[(3) * (3)];
int [] ll= new int[(7)];
double [] vv= new double[(6)];
double [] ya= new double[(lmt.val)];
 double [] za= new double[(lmt.val)];
double [] xa= new double[(lmt.val)];
 double [] a= new double[(3) * (3)];
int [] l=new int[lmt.val+1];
double [] w=new double[lmt.val+1];
for( i=0;i<lmt.val+1;i++)
  {l[i]=0;
   w[i]=1.0E9;
  }
File ff= new File( "x.out");
OutputStream fout;
  try{
      fout= new FileOutputStream( ff);
     }
  catch( Exception sss)
    {fout = System.out;
    }
forloop3:
//Index sort of w, the magnitude of the the peaks, with index l 
for (i = 1; i <= lmt.val; i++) {
   hm = xx[(i)- 1+ _xx_offset]*xx[(i)- 1+ _xx_offset]+yy[(i)- 1+ _yy_offset]*yy[(i)- 1+ _yy_offset]+zz[(i)- 1+ _zz_offset]*zz[(i)- 1+ _zz_offset];
   if( hm >= 1.0E9)
      System.out.println("abid hm value  is very large");
   Goto = 0;
   if( Double.isNaN(hm)) hm = .9E9;
   try{
     fout.write(("hm="+hm+"\n").getBytes());
      }
    catch( Exception sss5){}
   forloop1:
   for (j = 1; (j <= lmt.val) &&(Goto ==0); j++) {
     if ((hm < w[(j)- 1]) && (Goto == 0))  {
    
      forloop2:
      for (idum = j; idum <= lmt.val; idum++) {
         k = lmt.val+j-idum;
         w[(k+1)- 1] = w[(k)- 1];
         l[(k+1)- 1] = l[(k)- 1];
        //Dummy.label("Abid",2);
      }              //  Close for() loop. 

      w[(j)- 1] = hm;
      l[(j)- 1] = i;
      Goto = 3;
     try{
     for( int ii=0; ii<lmt.val+1 ;ii++)fout.write((l[ii]+" ").getBytes());fout.write("\n".getBytes());
     for( int ii=0; ii<lmt.val+1 ;ii++)fout.write((w[ii]+" ").getBytes());fout.write("\n".getBytes());
         }
     catch(Exception sss1){}
    }              // Close if()
    //Dummy.label("Abid",1);
   }              //  Close for() loop. 

// 
//Dummy.label("Abid",3);
}              //  Close for() loop. 

try{
 fout.close();
  }
catch(Exception ssss3){}
// 
{
forloop5:
for (j = 1; j <= lmt.val; j++) { 
xa[(j)- 1] = xx[(l[(j)- 1])- 1+ _xx_offset];
ya[(j)- 1] = yy[(l[(j)- 1])- 1+ _yy_offset];
za[(j)- 1] = zz[(l[(j)- 1])- 1+ _zz_offset];

//Dummy.label("Abid",5);
}              //  Close for() loop.

}
// 
k = 3;
Goto=6;
//While the proposed basis vectors are coplanar
while( Goto==6){
  Goto=0;
label6:
  // Dummy.label("Abid",6);
k = k+1;
   
if (k > lmt.val)  {
    System.out.println(" "  + "ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING" );
System.out.println(" "  + "ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING" );
// 
{
forloop29:
for (i = 1; i <= 3; i++) {
label29:
  // Dummy.label("Abid",29);
System.out.println(" "  + (xa[(i)- 1]) + " "  + "   " + (ya[(i)- 1]) + " "  + "   " + (za[(i)- 1]) + " "  + "   ");
System.out.println(" "  + (xa[(i)- 1]) + " "  + "   " + (ya[(i)- 1]) + " "  + "   " + (za[(i)- 1]) + " "  + "   ");
// 
System.out.println(" "  + "D="  + (d) + " " );
System.out.println(" "  + "D="  + (d) + " " );
//System.exit(1);
 errormessage="  ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING ";
 return;
}}}              // Close if()
label20:
  // Dummy.label("Abid",20);
{
forloop8:
for (i = 1; i <= 3; i++) {
b[(i)- 1+(1- 1)*3] = xa[(i)- 1];
b[(i)- 1+(2- 1)*3] = ya[(i)- 1];
b[(i)- 1+(3- 1)*3] = za[(i)- 1];
//Dummy.label("Abid",8);
}              //  Close for() loop. 
}
// 


doubleW dA = new doubleW(d);
mi(b,a,dA);

d=dA.val;
// d is the determinant of the basis vectors
// if close to zero, then basis are coplanar.  Interchange the kth peak with one
//    of the basis.
// 
if (Math.abs(d) < 0.0001)  {
    if (Math.abs(xa[(1)- 1]*ya[(2)- 1]-xa[(2)- 1]*ya[(1)- 1]) >= 0.05 || Math.abs(xa[(1)- 1]*za[(2)- 1]-xa[(2)- 1]*za[(1)- 1]) >= 0.05 || Math.abs(ya[(1)- 1]*za[(2)- 1]-ya[(2)- 1]*za[(1)- 1]) >= 0.05)  {
    hm = xa[(k)- 1];
xa[(k)- 1] = xa[(3)- 1];
xa[(3)- 1] = hm;
hm = ya[(k)- 1];
ya[(k)- 1] = ya[(3)- 1];
ya[(3)- 1] = hm;
hm = za[(k)- 1];
za[(k)- 1] = za[(3)- 1];
za[(3)- 1] = hm;
m = l[(k)- 1];
l[(k)- 1] = l[(3)- 1];
l[(3)- 1] = m;
Goto = 6;
}              // Close if()
else  {
  label12:
  // Dummy.label("Abid",12);
hm = xa[(k)- 1];
xa[(k)- 1] = xa[(2)- 1];
xa[(2)- 1] = xa[(3)- 1];
xa[(3)- 1] = hm;
hm = ya[(k)- 1];
ya[(k)- 1] = ya[(2)- 1];
ya[(2)- 1] = ya[(3)- 1];
ya[(3)- 1] = hm;
hm = za[(k)- 1];
za[(k)- 1] = za[(2)- 1];
za[(2)- 1] = za[(3)- 1];
za[(3)- 1] = hm;
m = l[(k)- 1];
l[(k)- 1] = l[(2)- 1];
l[(2)- 1] = m;
Goto = 6;
}              //  Close else.
}              // Close if()
}//while Goto==6
//if (Goto == 6)  
   // Dummy.go_to("Abid",6);
label10:
  // Dummy.label("Abid",10);



//Manipulate b so that B*transpose(B) about diagonal
aarr(b,vv,ll);
// 
{
forloop15:
for (idum = 1; idum <= lmt.val; idum++) {
i = lmt.val+1-idum;


xx[(i+3)- 1+ _xx_offset] = xx[(i)- 1+ _xx_offset];
yy[(i+3)- 1+ _yy_offset] = yy[(i)- 1+ _yy_offset];
zz[(i+3)- 1+ _zz_offset] = zz[(i)- 1+ _zz_offset];
//Dummy.label("Abid",15);
}              //  Close for() loop. 
}
// 
{
forloop16:
for (i = 1; i <= 3; i++) {
xx[(i)- 1+ _xx_offset] = b[(i)- 1+(1- 1)*3];
yy[(i)- 1+ _yy_offset] = b[(i)- 1+(2- 1)*3];
zz[(i)- 1+ _zz_offset] = b[(i)- 1+(3- 1)*3];
//Dummy.label("Abid",16);
}              //  Close for() loop. 
}
// 
lmt.val = lmt.val+3;

// 
//Dummy.go_to("Abid",999999);
//Dummy.label("Abid",999999);
return;
   }

// 
/** Manipulate basis ab so that ab*transpose(ab) about (integer) diagonal
*/
public static void aarr (double [] ab, 
double [] v,
int [] l)  {
int _ab_offset=0;
int _v_offset=0;
int _l_offset = 0;
int j=0,m=0,Goto=0,k=0,i=0,kk=0;
label1:
  // Dummy.label("Aarr",1);
while( 3==3){ // ADDed for goto 1 at bottom

forloop2:
for (j = 1; j <= 6; j++) {
v[(j)- 1+ _v_offset] = 0.0;
//Dummy.label("Aarr",2);
}              //  Close for() loop. 

// 

forloop3:
for (j = 1; j <= 3; j++) {
m = j+1;
if (m > 3)  
    m = m-3;

forloop30:
for (i = 1; i <= 3; i++) {
v[(j)- 1+ _v_offset] = v[(j)- 1+ _v_offset]+ab[(j)- 1+(i- 1)*3+ _ab_offset]*ab[(j)- 1+(i- 1)*3+ _ab_offset];
v[(j+3)- 1+ _v_offset] = v[(j+3)- 1+ _v_offset]+ab[(j)- 1+(i- 1)*3+ _ab_offset]*ab[(m)- 1+(i- 1)*3+ _ab_offset];
//Dummy.label("Aarr",30);
}              //  Close for() loop. 

//Dummy.label("Aarr",3);
}              //  Close for() loop. 

// 

forloop4:
for (j = 1; j <= 3; j++) {
m = j+1;
if (m > 3)  
    m = m-3;
if (v[(j+3)- 1+ _v_offset] >= 0.0)  {
    l[(j)- 1+ _l_offset] = (int)(v[(j+3)- 1+ _v_offset]/v[(j)- 1+ _v_offset]+0.498);
l[(j+3)- 1+ _l_offset] = (int)(v[(j+3)- 1+ _v_offset]/v[(m)- 1+ _v_offset]+0.498);
}              // Close if()
else  {
  label5:
  // Dummy.label("Aarr",5);
l[(j)- 1+ _l_offset] = (int)(v[(j+3)- 1+ _v_offset]/v[(j)- 1+ _v_offset]-0.498);
l[(j+3)- 1+ _l_offset] = (int)(v[(j+3)- 1+ _v_offset]/v[(m)- 1+ _v_offset]-0.498);
}              //  Close else.
//Dummy.label("Aarr",4);
}              //  Close for() loop. 

// 
l[(7)- 1+ _l_offset] = 0;
// 

forloop8:
for (j = 1; j <= 6; j++) {
kk = l[(j)- 1+ _l_offset];
if (kk < 0)  {
    kk = -kk;
}              // Close if()
if ((kk) > l[(7)- 1+ _l_offset])  {
    l[(7)- 1+ _l_offset] = (kk);
k = j;
}              // Close if()
//Dummy.label("Aarr",8);
}              //  Close for() loop. 

// 
if (l[(7)- 1+ _l_offset] == 0)  {
    return;//Dummy.go_to("Aarr",999999);
}              // Close if()
if (k >= 4)  {
    // 

forloop21:
for (j = 1; j <= 3; j++) {
m = k-2;
if (m > 3)  
    m = m-3;
ab[(k-3)- 1+(j- 1)*3+ _ab_offset] = ab[(k-3)- 1+(j- 1)*3+ _ab_offset]-l[(k)- 1+ _l_offset]*ab[(m)- 1+(j- 1)*3+ _ab_offset];
//Dummy.label("Aarr",21);
}              //  Close for() loop. 

// 
Goto = 1;
}              // Close if()
else  {
  label13:
//   Dummy.label("Aarr",13);

forloop20:
for (j = 1; j <= 3; j++) {
m = k+1;
if (m > 3)  
    m = m-3;
ab[(m)- 1+(j- 1)*3+ _ab_offset] = ab[(m)- 1+(j- 1)*3+ _ab_offset]-l[(k)- 1+ _l_offset]*ab[(k)- 1+(j- 1)*3+ _ab_offset];
//Dummy.label("Aarr",20);
}              //  Close for() loop. 

}              //  Close else.
Goto = 1;
}//will goto 1
// 
//label11:
 //  Dummy.label("Aarr",11);
//Dummy.go_to("Aarr",999999);
//Dummy.label("Aarr",999999);
//return;
   }


public static void mi (double [] ad,
double [] aid,
doubleW dsing)  {
 int _ad_offset=0;
int _aid_offset=0;
int i=0,j=0,m=0,n=0,l=0,k=0;
double [] ai= new double[(3) * (3)];
double [] a= new double[(3) * (3)];
double d=0.0f;
{
//forloop10:
for (i = 1; i <= 3; i++) {
{
//forloop10:
for (j = 1; j <= 3; j++) {
a[(i)- 1+(j- 1)*3] = ad[(i)- 1+(j- 1)*3+ _ad_offset];
label10:
  // Dummy.label("Mi",10);
ai[(i)- 1+(j- 1)*3] = aid[(i)- 1+(j- 1)*3+ _aid_offset];
}}}}
d = dsing.val;
i = 0;
{
forloop1:
for (j = 1; j <= 3; j++) {
m = j+1;
if (m > 3)  
    m = m-3;
n = j+2;
if (n > 3)  
    n = n-3;
{
//forloop1:
for (i = 1; i <= 3; i++) {
k = i+1;
if (k > 3)  
    k = k-3;
l = i+2;
if (l > 3)  
    l = l-3;
ai[(i)- 1+(j- 1)*3] = a[(m)- 1+(k- 1)*3]*a[(n)- 1+(l- 1)*3]-a[(m)- 1+(l- 1)*3]*a[(n)- 1+(k- 1)*3];
//Dummy.label("Mi",1);
}              //  Close for() loop. 
}}}
d = ai[(1)- 1+(1- 1)*3]*a[(1)- 1+(1- 1)*3]+ai[(2)- 1+(1- 1)*3]*a[(1)- 1+(2- 1)*3]+a[(1)- 1+(3- 1)*3]*ai[(3)- 1+(1- 1)*3];
if (d == 0.0e0)  
   return;
{
//forloop3:
for (i = 1; i <= 3; i++) {
{
//forloop3:
for (j = 1; j <= 3; j++) {
ai[(i)- 1+(j- 1)*3] = ai[(i)- 1+(j- 1)*3]/d;
//Dummy.label("Mi",3);
}              //  Close for() loop. 
}}}
{
//forloop11:
for (i = 1; i <= 3; i++) {
{
//forloop11:
for (j = 1; j <= 3; j++) {
ad[(i)- 1+(j- 1)*3+ _ad_offset] = a[(i)- 1+(j- 1)*3];
label11:
  // Dummy.label("Mi",11);
aid[(i)- 1+(j- 1)*3+ _aid_offset] = ai[(i)- 1+(j- 1)*3];
}}}}
dsing.val = d;
label2:
  // Dummy.label("Mi",2);
//Dummy.go_to("Mi",999999);
//Dummy.label("Mi",999999);
return;
   }



public static void bias (int lmt,
double [] xx,
double [] yy,
double [] zz,
double [] b, 
double mw,
double den,
doubleW dd,
double dj,
intW mj,
int [] seq, 
int input,
int expnum,
int hstnum)  {
int _xx_offset =0;
int _yy_offset =0;
 int _zz_offset= 0;
int _b_offset = 0;
int _seq_offset =0;
double [] hh= new double[(3) * (lmt)];
double [] a= new double[(3) * (3)];
 double d= 0.0;
// 
 int [] jh= new int[(3) * (lmt)];
int i= 0;
 int j= 0;
 int Goto= 0;
 int iz= 0;
boolean mm= false;

System.out.println("Got into BIAS");
doubleW DW = new doubleW(0);
intW IW= new intW(0);

iz = (int)(Math.abs(mj.val));
Goto = 1;
label1:
   //Dummy.label("Bias",1);
while( Goto==1)
{ Goto=0;
Goto = 0;
{
forloop2:
for (j = 1; j <= 3; j++) {
b[(1)- 1+(j- 1)*3+ _b_offset] = xx[(j)- 1+ _xx_offset];
b[(2)- 1+(j- 1)*3+ _b_offset] = yy[(j)- 1+ _yy_offset];
b[(3)- 1+(j- 1)*3+ _b_offset] = zz[(j)- 1+ _zz_offset];
// c      c  IF (J .LT. 3) GO TO 2
//Dummy.label("Bias",2);
}              //  Close for() loop. 
}
// 
DW.val=d;
mi(b,a,DW);
d= DW.val;
// 
j = 0;
{
forloop3:
for (j = 1; j <= 3; j++) {
{
forloop4:
for (i = 1; i <= lmt; i++) {
hh[(j)- 1+(i- 1)*3] = a[(j)- 1+(1- 1)*3]*xx[(i)- 1+ _xx_offset]+a[(j)- 1+(2- 1)*3]*yy[(i)- 1+ _yy_offset]+a[(j)- 1+(3- 1)*3]*zz[(i)- 1+ _zz_offset];
// c      c  IF (I .LT. LMT) GOTO=4
//Dummy.label("Bias",4);
}              //  Close for() loop. 
}
//Dummy.label("Bias",3);
}              //  Close for() loop. 
}
// c      c  IF (J .LT. 3) GO TO 3
mm = false;
// 
//floatW FW = new floatW( dd);


IW.val=iz;
lcl(hh,lmt,dd,IW);
if( errormessage.length()>0)
  return;
//dd=FW.val;
iz=IW.val;
System.out.println("Aft LCL"+lmt+","+dd.val+","+iz);
for(  i=0;i<hh.length;i++)System.out.print(hh[i]+",");
System.out.println("");
if (Math.abs(dd.val-0.100) < 0.00001)  
    dd.val = 0.100;
// 
if (dd.val == 0.100)  {
    label10:
   //Dummy.label("Bias",10);
dd.val = -0.010;
Goto = 1;
}              // Close if()
if (Goto == 0)  {
    label11:
   //Dummy.label("Bias",11);
System.out.println("before Aaio,B=");
for( i=0;i<b.length;i++)System.out.print(b[i]+",");
System.out.println("");
aaio(xx,yy,zz,b,hh,lmt);
// 
System.out.println("bef Aair,B=");
for(i=0;i<b.length;i++)System.out.print(b[i]+",");
System.out.println("");
aair(b,a);
// 
System.out.println("before Thh,B=");
for(i=0;i<b.length;i++)System.out.print(b[i]+",");
System.out.println("");
booleanW BW= new booleanW(mm);
thh(hh,xx,yy,zz,a,jh,BW,dd.val,lmt);
mm= BW.val;

// 
System.out.println("aft Thh,B=");
for( i=0;i<b.length;i++)System.out.print(b[i]+",");
System.out.println("");
if (mw > 0.0 && mm)  
    Goto = 1;
if (mj.val < 0 && Goto == 0)  {
    //
DW.val=d; 
mi(b,a,DW);
d=DW.val;
System.out.println("d="+d);
// 
if (dj+0.1 > 1.0/Math.abs(d) && dj-0.1 < 1.0/Math.abs(d))  
    Goto = 1;
}              // Close if()
}              // Close if()
//label13:
   //Dummy.label("Bias",13);
}//while Goto==1
//if (Goto == 1)  
//    Dummy.go_to("Bias",1);
System.out.println(" "  + "\n"  + "******************" );
//System.out.println(" "  + "\n"  + "******************" );
// 
System.out.println(" "  + "\n"  + " ERROR LIMIT="  + (dd.val) + " " );
//System.out.println(" "  + "\n"  + " ERROR LIMIT="  + (dd.val) + " " );
// 
System.out.println(" "  + "\n"  + " REDUCED CELL" );
//System.out.println(" "  + "\n"  + " REDUCED CELL" );
// 
lst(hh,xx,yy,zz,a,jh,mw,b,d,lmt,seq,den,input,expnum,hstnum);
// 
mj.val = -iz;
//Dummy.go_to("Bias",999999);
//Dummy.label("Bias",999999);
return;
   }


// 


public static void lcl (double [] fh, 
int lmt,
doubleW dd,
intW iz)  {
int _fh_offset=0;
int ni = 512;
int [] hh= new int[(3) * (lmt)];
 int [] la= new int[(lmt)];
 int d= 0,s1= 0,s2= 0,s3= 0;
 int [] ll= new int[(3) * (lmt)];
 int ha= 0,hb= 0,hc= 0,da= 0,equ= 0,Goto= 0;
 boolean trace=false;
 int i= 0,j= 0,m1= 0,m2= 0,m3= 0,jdum= 0,idum= 0;
 int n= 0,kka= 0,lb= 0,kkdum= 0,m= 0,k= 0,l= 0,mm= 0;
 int idumc= 0,iduma= 0,kk= 0,idumb= 0;
// 
forloop1:
for (i = 1; i <= 3; i++) {

forloop101:
for (j = 1; j <= lmt; j++) {
hh[(i)- 1+(j- 1)*3] = (int)(fh[(i)- 1+(j- 1)*3+ _fh_offset]*ni);
//Dummy.label("Lcl",101);
}              //  Close for() loop. 

//Dummy.label("Lcl",1);
}              //  Close for() loop. 

// 
ha = hh[(1)- 1+(lmt- 1)*3];
hb = hh[(2)- 1+(lmt- 1)*3];
hc = hh[(3)- 1+(lmt- 1)*3];
// 
//label2:
   //Dummy.label("Lcl",2);
while( 3==3){
if (Goto == 0 || Goto == 2) 
    Goto = 0;
              // Close if()

dd.val = dd.val+0.020;

if (dd.val > 0.30)  {
    // 
//System.out.println(" PROGRAM TERMINATION" );
//System.out.println(" PROGRAM TERMINATION" );
// 
//System.out.println(" INITIAL NON-INTEGER INDICES" );
//System.out.println(" INITIAL NON-INTEGER INDICES" );
// 

forloop6:
for (i = 1; i <= lmt; i++) {
//label6:
   //Dummy.label("Lcl",6);
System.out.println(" "  + (fh[(1)- 1+(i- 1)*3+ _fh_offset]) + " " );
// 
//System.out.println(" "  + (fh[(1)- 1+(i- 1)*3+ _fh_offset]) + " " );
//System.exit(1);
errormessage ="INITIAL NON-INTEGER INDICES ";
return;
}
}             // Close if()
label4:
   //Dummy.label("Lcl",4);

iz.val = iz.val+1;
da = (int)(dd.val*ni);
kka = 0;
label10:
   //Dummy.label("Lcl",10);
{
forloop80:
for (mm = 1; mm <= 10; mm++) {
if (Goto == 0)  
    kk = mm+1;
// 
label11:
   //Dummy.label("Lcl",11);
{
forloop801:
for (iduma = 1; iduma <= kk; iduma++) {
if (Goto == 0)  
    k = iduma-1;
if (Goto == 0)  
    s1 = k*ha;
// 
label12:
   //Dummy.label("Lcl",12);
{
forloop802:
for (idumb = 1; idumb <= kk; idumb++) {
if (Goto == 0)  {
    l = idumb-1;
s2 = l*hb;
}              // Close if()
label13:
   //Dummy.label("Lcl",13);
{
forloop803:
for (idumc = 1; idumc <= kk; idumc++) {
 trace=false;
 if(mm==1)if(iduma==2)if(idumb==2)if(idumc==1) trace=true;
if (Goto == 0)  
    m = idumc-1;

if (k != mm && l != mm && m != mm && Goto == 0)  
    Goto = 803;
if (Goto == 0)  
    s3 = m*hc;
if (Goto == 0)  
    Goto = 14;
label14:
   //Dummy.label("Lcl",14);
while( Goto !=0){
// c      while goto!=0        
if (Goto == 0 || Goto == 14)  {
    Goto = 0;
  
  intW LB= new intW(lb);
if (equ(s1+s2+s3,LB,da) == 0)  {
    lb=LB.val;
    
    la[(1)- 1] = k;
la[(2)- 1] = l;
la[(3)- 1] = m;
n = 2;
la[(lmt)- 1] = lb;

Goto = 30;
} 
else lb=LB.val;             // Close if()
}              // Close if()
label20:
   //Dummy.label("Lcl",20);
if (Goto == 0 || Goto == 20)  {
    Goto = 0;
if (l == 0 && Goto == 0)  
    Goto = 21;
    intW LB= new intW(lb);
if (Goto == 0 && equ(s1-s2+s3,LB,da) == 0)  {
    lb=LB.val;
    
    la[(1)- 1] = k;
la[(2)- 1] = -l;
la[(3)- 1] = m;
n = 3;
la[(lmt)- 1] = lb;

Goto = 30;
}              // Close if()
}              // Close if()
label21:
   //Dummy.label("Lcl",21);
if (Goto == 0 || Goto == 21)  {
    Goto = 0;
if (k == 0 && Goto == 0)  
    Goto = 803;
if (m != 0 && Goto == 0)  {
     
    intW LB= new intW(lb);
    if (equ(s1+s2-s3,LB,da) == 0)  {
    lb=LB.val;
    la[(1)- 1] = k;
la[(2)- 1] = l;
la[(3)- 1] = -m;
n = 4;
la[(lmt)- 1] = lb;

Goto = 30;
}              // Close if()
}              // Close if()
}              // Close if()
label17:
   //Dummy.label("Lcl",17);
if (Goto == 0 || Goto == 17)  {
    Goto = 0;
    
n = 5;
   
if (l == 0 || m == 0)  
    Goto = 19;
   intW LB= new intW(lb);
  
if (equ(s1-s2-s3,LB,da) != 0)  
    {Goto = 19;
     lb=LB.val;
    }

if (Goto == 0)  {
    la[(1)- 1] = k;
la[(2)- 1] = -l;
la[(3)- 1] = -m;
n = 5;
la[(lmt)- 1] = lb;

}              // Close if()
}              // Close if()
if (Goto == 0 || Goto == 30)  {
    Goto = 0;
label30:
   //Dummy.label("Lcl",30);
 
jdum = lmt-1;
// 
{
forloop50:

for (idum = 4; idum <= jdum; idum++) {
if (Goto == 0)  {
    j = 3+lmt-idum;
kkdum = la[(1)- 1]*hh[(1)- 1+(j- 1)*3]+la[(2)- 1]*hh[(2)- 1+(j- 1)*3]+la[(3)- 1]*hh[(3)- 1+(j- 1)*3];
intW LB= new intW(lb);

if (equ(kkdum,LB,da) != 0)  
    {Goto = 19;
    }
 lb=LB.val;
if (Goto == 0)  
    la[(j)- 1] = lb;
}              // Close if()
//Dummy.label("Lcl",50);
}              //  Close for() loop. 
}

if (Goto == 0)  {
    kka = kka+1;
// 

forloop51:
for (j = 1; j <= lmt; j++) {
ll[(kka)- 1+(j- 1)*3] = la[(j)- 1];
//Dummy.label("Lcl",51);
}              //  Close for() loop. 

}              // Close if()
} 
if( Goto==0){
      
}
if (Goto == 0) { 
    if (kka == 1)  
    Goto = 19;

     }
if (Goto == 0)  {
    m1 = ll[(1)- 1+(1- 1)*3]*ll[(2)- 1+(2- 1)*3]-ll[(1)- 1+(2- 1)*3]*ll[(2)- 1+(1- 1)*3];
m2 = ll[(1)- 1+(1- 1)*3]*ll[(2)- 1+(3- 1)*3]-ll[(1)- 1+(3- 1)*3]*ll[(2)- 1+(1- 1)*3];
m3 = ll[(1)- 1+(2- 1)*3]*ll[(2)- 1+(3- 1)*3]-ll[(1)- 1+(3- 1)*3]*ll[(2)- 1+(2- 1)*3];
if (kka-2 != 0)  {
    Goto = 52;
}              // Close if()
else  {
  if (m1 != 0 || m2 != 0 || m3 != 0)  {
    Goto = 19;
}              // Close if()
else  {
  kka = 1;
}              //  Close else.
Goto = 19;
}              //  Close else.
}              // Close if()
if (Goto == 0 || Goto == 52)  {
    Goto = 0;
  
label52:
   //Dummy.label("Lcl",52);
d = m1*ll[(3)- 1+(3- 1)*3]-m2*ll[(3)- 1+(2- 1)*3]+m3*ll[(3)- 1+(1- 1)*3];
if (d == 0)  {
    kka = 2;
Goto = 19;
}              // Close if()
else  {
  label53:
   //Dummy.label("Lcl",53);

forloop60:
for (j = 1; j <= lmt; j++) {

forloop601:
for (i = 1; i <= 3; i++) {
fh[(i)- 1+(j- 1)*3+ _fh_offset] = (double)(ll[(i)- 1+(j- 1)*3]);
//Dummy.label("Lcl",601);
}              //  Close for() loop. 

//Dummy.label("Lcl",60);
}              //  Close for() loop. 

// 
return;//Dummy.go_to("Lcl",999999);
}              //  Close else.
}              // Close if()
if (Goto == 19 || Goto == 0)  {
    Goto = 0;
  
label19:
   //Dummy.label("Lcl",19);
//System.out.println("lbl 19,N,MM,IDUMA,IDUMB,IDUMC="+","+n+","+mm+","+iduma+","+idumb
 //                    +","+idumc);
if (n == 5)  
    Goto = 803;
             
else if (n == 1)  
    Goto = 14;
             
else if (n == 2)  
    Goto = 20;
            
else if (n == 3)  
    Goto = 21;
            
else if (n == 4) 
    Goto = 17;
         
}              // Close if()
if (Goto == 803)  
    Goto = 0;
}//while Goto!=0 goto lable 14
//Dummy.label("Lcl",803);
}              //  Close for() loop. 
}
//Dummy.label("Lcl",802);
}              //  Close for() loop. 
}
//Dummy.label("Lcl",801);
}              //  Close for() loop. 
}
// 
//Dummy.label("Lcl",80);
}              //  Close for() loop. 
}
// 
}//while 3==3 gotot 2.go_to("Lcl",2);

// 
//label201:
   //Dummy.label("Lcl",201);
//Dummy.go_to("Lcl",999999);
//Dummy.label("Lcl",999999);
//return;
   }


public static int equ (int s,
intW lb,
int da)  {

int ni = 512;
int nj = 256;
int EQU = 0;
// 
if (s >= 0)  {
    // 
lb.val = (s+nj)/ni;
}              // Close if()
else  {
  // 
label2:
  // Dummy.label("Equ",2);
lb.val = (s-nj)/ni;
}              //  Close else.
label3:
   //Dummy.label("Equ",3);
if (Math.abs(s-lb.val*ni) >= da)  {
    EQU = 1;
}              // Close if()
else  {
  // 
label6:
  // Dummy.label("Equ",6);
EQU = 0;
}              //  Close else.
label5:
//   Dummy.label("Equ",5);
//Dummy.go_to("Equ",999999);
//Dummy.label("Equ",999999);
return EQU;
   }


// 

public static void aair (double [] b, 
double [] a )  {
int _b_offset = 0;
int _a_offset = 0;

double [] ab= new double[(3) * (3)];
double [] v= new double[(6)];
double d= 0.0;
// 
int [] l= new int[(7)];
 int i= 0;
 int j= 0;
int Goto= 0;
int k= 0;
 int idum= 0;
// 
double [] w = {1.0E9, 1.0E9, 1.0E9, 1.0E9};
doubleW dW= new doubleW( d);
mi(b,ab,dW);
  d= dW.val;

aarr(ab,v,l);
System.out.println("aft aarr in aair b,ab,v");
for(  i=0;i<b.length;i++)
  System.out.print(b[i]+",");
System.out.println("");
for(  i=0;i<ab.length;i++)
  System.out.print(ab[i]+",");
System.out.println("");
for(  i=0;i<v.length;i++)
  System.out.print(v[i]+",");
System.out.println("");
for(  i=0;i<l.length;i++)
  System.out.print(l[i]+",");
System.out.println("");

// 

//forloop16:
/*for (i = 1; i <= 3; i++) {
// 
{
forloop1:
for (j = 1; j <= 3; j++) {
if (v[(i)- 1] < w[(j)- 1] && Goto == 0)  {
    {
forloop2:
for (idum = j; idum <= 3; idum++) {
k = j+3-idum;
w[(k+1)- 1] = w[(k)- 1];
l[(k+1)- 1] = l[(k)- 1];
//Dummy.label("Aair",2);
}              //  Close for() loop. 
}
w[(j)- 1] = v[(i)- 1];
l[(j)- 1] = i;
Goto = 16;
}              // Close if()
//Dummy.label("Aair",1);
}              //  Close for() loop. 
}
// 
//Dummy.label("Aair",16);
  Goto=0;
}
*/              //  Close for() loop. 



for(i=1;(i<=3);i++){
  j=1;
   boolean done=false;
  //done=v[(i)- 1] < w[(j)- 1];
  while(!done){
   if(v[i-1]<w[j-1])
    {for( idum=j;idum<=3;idum++)
       {k=j+3-idum;
        w[k+1-1]=w[k-1];
        l[k+1-1]=l[k-1];
        }
    w[(j)- 1] = v[(i)- 1];
    l[(j)- 1] = i;
    done=true;
    }
   else{
    j++;
    if( j>3)done=true;
     }
  
   }
}

// 

forloop3:
System.out.println( "Aair,l="+l[0]+","+l[1]+","+l[2]+","+l[3]+","+l[4]+","+
                    l[5]+","+l[6]+","+a.length+","+ab.length);
System.out.println("W="+w[0]+","+w[1]+","+w[2]+","+w[3]);
for (i = 1; i <= 3; i++) {
a[(3)- 1+(i- 1)*3+ _a_offset] = ab[(l[(1)- 1])- 1+(i- 1)*3];
a[(1)- 1+(i- 1)*3+ _a_offset] = ab[(l[(2)- 1])- 1+(i- 1)*3];
a[(2)- 1+(i- 1)*3+ _a_offset] = ab[(l[(3)- 1])- 1+(i- 1)*3];
//Dummy.label("Aair",3);
}              //  Close for() loop. 
System.out.println("aft 3, A=");
for( i=0;i<a.length;i++)
  System.out.print(a[i]+",");
System.out.println("");

// 
w[(4)- 1] = v[(5)- 1];
v[(5)- 1] = v[(6)- 1];
v[(6)- 1] = w[(4)- 1];
if (v[(l[(1)- 1]+l[(2)- 1]+1)- 1] > 0.0)  {
    // 
{
forloop21:
for (i = 1; i <= 3; i++) {
a[(1)- 1+(i- 1)*3+ _a_offset] = -a[(1)- 1+(i- 1)*3+ _a_offset];
v[(l[(1)- 1]+l[(2)- 1]+1)- 1] = -v[(l[(1)- 1]+l[(2)- 1]+1)- 1];
v[(l[(2)- 1]+l[(3)- 1]+1)- 1] = -v[(l[(2)- 1]+l[(3)- 1]+1)- 1];
//Dummy.label("Aair",21);
}              //  Close for() loop. 
}
}              // Close if()
label10:
  // Dummy.label("Aair",10);
if (v[(l[(3)- 1]+l[(1)- 1]+1)- 1] > 0.0)  {
    // 
{
forloop22:
for (i = 1; i <= 3; i++) {
a[(2)- 1+(i- 1)*3+ _a_offset] = -a[(2)- 1+(i- 1)*3+ _a_offset];
v[(l[(3)- 1]+l[(1)- 1]+1)- 1] = -v[(l[(3)- 1]+l[(1)- 1]+1)- 1];
v[(l[(2)- 1]+l[(3)- 1]+1)- 1] = -v[(l[(2)- 1]+l[(3)- 1]+1)- 1];
//Dummy.label("Aair",22);
}              //  Close for() loop. 
}
}              // Close if()
label11:;
  // Dummy.label("Aair",11);
   System.out.println("aft 11, A=");
for(  i=0;i<a.length;i++)
  System.out.print(a[i]+",");
System.out.println("");

doubleW dW1= new doubleW( d);
mi(a,b,dW1);
  d= dW1.val;
// 
if (d >= 0.0)  
    return;
System.out.println("D, neg entries "+d);
// 
{
forloop14:
for (i = 1; i <= 3; i++) {
{
forloop15:
for (j = 1; j <= 3; j++) {
a[(i)- 1+(j- 1)*3+ _a_offset] = -a[(i)- 1+(j- 1)*3+ _a_offset];
b[(i)- 1+(j- 1)*3+ _b_offset] = -b[(i)- 1+(j- 1)*3+ _b_offset];
//Dummy.label("Aair",15);
}              //  Close for() loop. 
}
//Dummy.label("Aair",14);
}              //  Close for() loop. 
}
// 
//label12:
  // Dummy.label("Aair",12);
//Dummy.go_to("Aair",999999);
//Dummy.label("Aair",999999);
//return;
   }



// 
// C-----------------------------------------------------------------------
// 
// 

public static void aaio (double [] xx, 
double [] yy, 
double [] zz, 
double [] b, 
double [] hh, 
int lmt)  {
int _xx_offset=0;
int _yy_offset=0;
int _zz_offset=0;
int _b_offset=0;
int _hh_offset=0;
double [] a= new double[(3) * (3)];
double [] ai= new double[(3) * (3)];
 double d= 0.0;
int i= 0;
int j= 0;
 int k= 0;



label1:
   //Dummy.label("Aaio",1);
i = i+1;
j = 0;
label2:
   //Dummy.label("Aaio",2);
j = j+1;
{
//forloop3:
for (i = 1; i <= 3; i++) {
{
//forloop3:
for (j = 1; j <= 3; j++) {
a[(i)- 1+(j- 1)*3] = 0.0;
b[(i)- 1+(j- 1)*3+ _b_offset] = 0.0;
// 
{
//forloop3:
for (k = 1; k <= lmt; k++) {
label3:
   //Dummy.label("Aaio",3);
b[(i)- 1+(j- 1)*3+ _b_offset] = b[(i)- 1+(j- 1)*3+ _b_offset]+hh[(i)- 1+(k- 1)*3+ _hh_offset]*hh[(j)- 1+(k- 1)*3+ _hh_offset];
}}}}}}// 
// 
// 
{
//forloop4:
for (i = 1; i <= 3; i++) {
{
//forloop4:
for (k = 1; k <= lmt; k++) {
a[(1)- 1+(i- 1)*3] = a[(1)- 1+(i- 1)*3]+xx[(k)- 1+ _xx_offset]*hh[(i)- 1+(k- 1)*3+ _hh_offset];
a[(2)- 1+(i- 1)*3] = a[(2)- 1+(i- 1)*3]+yy[(k)- 1+ _yy_offset]*hh[(i)- 1+(k- 1)*3+ _hh_offset];
a[(3)- 1+(i- 1)*3] = a[(3)- 1+(i- 1)*3]+zz[(k)- 1+ _zz_offset]*hh[(i)- 1+(k- 1)*3+ _hh_offset];
//Dummy.label("Aaio",4);
}              //  Close for() loop. 
}}}
// 
doubleW Dd= new doubleW(d);
blind.mi(b,ai,Dd);
d = Dd.val;
// 
{
//forloop5:
for (i = 1; i <= 3; i++) {
{
//forloop5:
for (j = 1; j <= 3; j++) {
b[(i)- 1+(j- 1)*3+ _b_offset] = 0.0;
{
//forloop5:
for (k = 1; k <= 3; k++) {
b[(i)- 1+(j- 1)*3+ _b_offset] = b[(i)- 1+(j- 1)*3+ _b_offset]+a[(i)- 1+(k- 1)*3]*ai[(k)- 1+(j- 1)*3];
//Dummy.label("Aaio",5);
}              //  Close for() loop. 
}}}}}
// 
//Dummy.go_to("Aaio",999999);
//Dummy.label("Aaio",999999);
return;
   }




// 
// 
// 

public static void thh (double [] hh, 
double [] xx,
double [] yy, 
double [] zz, 
double [] a, 
int [] jh, 
booleanW mm,
double dd,
int lmt)  {
 int _xx_offset=0;
 int _yy_offset=0;
 int _zz_offset=0;
 int _jh_offset=0;
int _hh_offset=0;
int _a_offset=0;
int i= 0;
 int j= 0;
int lb= 0;
{
//forloop1:
for (i = 4; i <= lmt; i++) {
{
//forloop1:
for (j = 1; j <= 3; j++) {
hh[(j)- 1+(i- 1)*3+ _hh_offset] = a[(j)- 1+(1- 1)*3+ _a_offset]*xx[(i)- 1+ _xx_offset]+a[(j)- 1+(2- 1)*3+ _a_offset]*yy[(i)- 1+ _yy_offset]+a[(j)- 1+(3- 1)*3+ _a_offset]*zz[(i)- 1+ _zz_offset];
if (hh[(j)- 1+(i- 1)*3+ _hh_offset] >= 0.0)  {
    lb = (int)(hh[(j)- 1+(i- 1)*3+ _hh_offset]+0.5);
}              // Close if()
else  {
  lb = (int)(hh[(j)- 1+(i- 1)*3+ _hh_offset]-0.5);
}              //  Close else.
if (Math.abs(hh[(j)- 1+(i- 1)*3+ _hh_offset]-lb) > dd)  {
    mm.val = true;
}              // Close if()
jh[(j)- 1+(i- 1)*3+ _jh_offset] = lb;
//Dummy.label("Thh",1);
}              //  Close for() loop. 
}}}
// 
//Dummy.go_to("Thh",999999);
//Dummy.label("Thh",999999);
return;
   }





public static void lst( double[] HH, double[] XX,double[] YY, double[] ZZ,
           double[] A, int[] JH, double MW, double[] B,double D,int LMT,
           int[] SEQ, double DEN, int INPUT, int EXPNUM, int HSTNUM)
   { double d=0;
     double[] AI = new double[9];
     doubleW dd= new doubleW(d);
     blind.mi(B,AI,dd);
     d=dd.val;
     cellVol = 1.0/d;
     System.out.println("Cell volume="+1.0/d);
    System.out.println("in aais,B,AI=");
     for(int i=0;i<B.length;i++)
            System.out.print(B[i]+",");
     System.out.println("");
    System.out.println("in aais,B,AI=");
     for(int i=0;i<AI.length;i++) 
            System.out.print(AI[i]+",");
     System.out.println("");

     System.out.println("Cell scalars");
      A2=AI[1-4+3*1]*AI[1-4+3*1]+AI[1-4+3*2]*AI[1-4+3*2]+AI[1-4+3*3]*AI[1-4+3*3];
      B2=AI[2-4+3*1]*AI[2-4+3*1]+AI[2-4+3*2]*AI[2-4+3*2]+AI[2-4+3*3]*AI[2-4+3*3];
      C2=AI[3-4+3*1]*AI[3-4+3*1]+AI[3-4+3*2]*AI[3-4+3*2]+AI[3-4+3*3]*AI[3-4+3*3];

      DAB= AI[1-4+3*1]*AI[2-4+3*1]+AI[1-4+3*2]*AI[2-4+3*2]+AI[1-4+3*3]*AI[2-4+3*3];
      DAC= AI[1-4+3*1]*AI[3-4+3*1]+AI[1-4+3*2]*AI[3-4+3*2]+AI[1-4+3*3]*AI[3-4+3*3] ;
      DBC= AI[2-4+3*1]*AI[3-4+3*1]+AI[2-4+3*2]*AI[3-4+3*2]+AI[2-4+3*3]*AI[3-4+3*3];
     
     System.out.println( A2+"  "+B2+"  "+C2);
     System.out.println(DBC+"   "+DAC+"  "+DAB);

     D1=Math.sqrt(A2);
     D2=Math.sqrt(B2);
     D3=Math.sqrt(C2);
     D4=DBC/(D2*D3);
     D5=DAC/(D1*D3);
     D6=DAB/(D1*D2);
     D4=57.296*Math.atan(Math.sqrt(1.-D4*D4)/D4);
     if (D4 < 0) D4=D4+180.;
       D5=57.296*Math.atan(Math.sqrt(1.-D5*D5)/D5);
       if (D5< 0) D5=D5+180.;
       D6=57.296*Math.atan(Math.sqrt(1.-D6*D6)/D6);
       if (D6 < 0) D6=D6+180.;
    System.out.println("Cell dimensions");
    System.out.println(D1+"   "+D2+"  "+D3);
    System.out.println(D4+"   "+D5+"   "+D6);


     System.out.println("");

      System.out.println("#   SEQ   H   K   L");
     for(int  i=0;i<SEQ.length; i++)
      System.out.println((i+1)+"  "+SEQ[i]+"  "+JH[(i+3)*3]+"  "+JH[3*(i+3)+1]+"  "+JH[3*(i+3)+2]);
     System.out.println("");
     


  double[] orgmat= new double[9];
  u = new double[9];
  for(int i=1;i<=3;i++)
    for(int j=1;j<=3;j++)
      {orgmat[i-4+3*j]=B[j-4+3*i];
       u[i-4+3*j]=B[j-4+3*i];
       }
        
  doubleW vol= new doubleW(0);
 subs.tstvol( orgmat,vol);

 if( vol.val < 0) 
   System.out.println("Left handed system");
  for(int i=1;i<=3;i++)
    System.out.println( orgmat[i-4+3*1]+"  "+orgmat[i-4+3*2]+"  "+orgmat[i-4+3*3]);

}
   


/** blind main program only using the run file
* @param args[0] should be the the name of the run file or peaks file
* @param args[1] list of sequence numbers
*/
public static void main( String args[])
  {  String filename = args[0];
     int[] seq = DataSetTools.util.IntList.ToArray(args[1]);
     if( seq == null)
        System.exit(0);
     if( seq.length < 1)
        System.exit(0);

     Vector V=new Vector();
     if( !(filename.toUpperCase().indexOf(".PEA")>0))
       {
        DataSet[] DS = (new IsawGUI.Util()).loadRunfile( filename);
        int  k=DS.length-1;
        if( args.length >1)
          try{
             k= ( new Integer( args[1])).intValue();
              }
            catch(Exception ss){}

        DataSet ds = DS[k];
        // ds.setAttribute( Attribute.SCD_CALIB_FILE,"C:\\Ipns\\Isaw2\\SampleRuns\\instprm.dat");
        LoadSCDCalib lcab= new LoadSCDCalib( ds,"C:\\Ipns\\Isaw2\\SampleRuns\\instprm.dat",
                                  1,"0:7000");
        lcab.getResult(); 
        FindPeaks fp = new FindPeaks( ds,0, 15,1);
        V = (Vector)(fp.getResult());
        CentroidPeaks cp = new CentroidPeaks(ds, V);

        V = (Vector)(cp.getResult());

        for(int i=0;i<V.size();i++)
         { Peak pk=(Peak)(V.elementAt(i));
            System.out.println("Pk i,xcm,ycm,wl="+pk.xcm()+","+pk.ycm()+","+pk.wl());
            System.out.println("    chi,phi,om,deta,detd="+pk.chi()+","+pk.phi()+","+
                                    pk.omega()+","+pk.detA()+","+pk.detD());
          }
        }
     else //we have a peaks file-faster for testing purposes
      {
       
      float chi=0.0f,phi=0.0f,omega=0.0f,deta=0.0f,detd=0.0f;
     
      TextFileReader fin=null;
      try{ 
       fin= new TextFileReader( filename);
       fin.read_line();
       int nseq= 0; 
       while( (!fin.eof())&&(nseq < seq.length))
        {int kk = fin.read_int();
         if(kk==1)
         
          {kk=fin.read_int();
           kk=fin.read_int();
           deta= (float)(fin.read_float() );// /180*java.lang.Math.PI);
           detd= (float) (fin.read_float());// /180*java.lang.Math.PI);
           detd= (float)(fin.read_float());// /180*java.lang.Math.PI);
           chi= (float)(fin.read_float());// /180*java.lang.Math.PI);
           phi=(float)( fin.read_float());// /180*java.lang.Math.PI);
           omega=(float) (fin.read_float());// /180*java.lang.Math.PI);

            fin.read_line();
            
           }
          else if( kk==3)
           {
           int seqnum=fin.read_int();
         
           boolean done = seqnum <= seq[nseq];
           while( !done) 
             {nseq ++;
              if(nseq >= seq.length)
                done = true;
              else
                done = seqnum <= seq[nseq];
              }
           if( nseq < seq.length)
            if( seqnum== seq[nseq])
               {
                float[] dat = new float[9];
                dat[ 8]  = seqnum;
                kk = fin.read_int();kk = fin.read_int() ; kk = fin.read_int();
                float x = fin.read_float();float y = fin.read_float();float z = fin.read_float();
           
                dat[ 5 ] = fin.read_float(); dat[ 6 ] = fin.read_float(); dat[ 7 ] = fin.read_float();
                dat[ 0 ] = chi; dat[ 1 ] = phi; dat[ 2 ] = omega;dat[ 3 ] = deta;dat[ 4]  = detd;
                fin.read_line();
          
                V.addElement(dat);
                }
             else  fin.read_line();  
            else fin.read_line();

           
          }
          else
            fin.read_line();
        }
        } 
      catch( Exception s)
        {System.out.println("error="+s);
         System.exit(0);
        }

       }
     if( V== null) 
        System.exit(0);
     if(V.size()<1)
        System.exit(0);
     double[] xx,yy,zz;
     xx = new double[V.size()+3];
     yy = new double[V.size()+3];
     zz = new double[V.size()+3];
     intW LMT = new intW(0);
     
     blind.blaue( V,xx,yy,zz,LMT,seq,1);
     double[] b= new double[9];
     doubleW dd= new doubleW(.08);
     intW mj= new intW(0);
    
     blind.bias(V.size()+3,xx,yy,zz,b,0,3,dd,4.0,mj,seq,1,123,0);
     System.out.println("Orientation matrix=");
     for( int i=0;i<3;i++)
       {for (int j=0;j<3;j++)
           System.out.print(blind.u[3*j+i]+" ");
        System.out.println("");
        }

      System.out.println(blind.D1+" "+blind.D2+" "+blind.D3+" "+blind.D4+" "+
                        blind.D5+" "+blind.D6+" "+blind.cellVol);
   }
} // End class.
