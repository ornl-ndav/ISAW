
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
 * Revision 1.5  2003/02/18 19:33:50  dennis
 * Removed ^M characters.
 *
 * Revision 1.4  2003/02/10 18:58:42  pfpeterson
 * Uses the new version of tstvol.
 *
 * Revision 1.3  2003/02/10 18:36:18  pfpeterson
 * Reformatted code.
 *
 * Revision 1.2  2003/02/10 15:35:24  pfpeterson
 * No longer creates 'x.out' in the directory where the program was executed.
 *
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
  public static void blaue (Vector peaks,double[] xx,double[] yy,double[] zz, 
                            intW lmt, int[] seq, int input) {
    float[] angle= new float[ xx.length*3];

    int _xx_offset=0;
    int _yy_offset=0;
    int _zz_offset=0;
    int _seq_offset=0;
    int j=0,k=0,ilog=0,hstnum=0;
    double continuelmt= 0.0;
    hstnum = 0;
    // will get peaks and loop here
    lmt.val = 1;
    // NOW CALCULATE THE DIFFRACTION VECTORS XX,YY,ZZ FROM
    // XCM, YCM, AND WL AND ROTATE THEM TO ALL ANGLES ZERO
    // BY CALLING SUBROUTINE LAUE
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

    // END OF REFLECTION INPUT
    continuelmt = (double)(lmt.val-1);
    input=2;
    if (input != 2 && ilog != 1)  {
      System.out.println("      #          X           Y          WL" );
      System.out.println("      #          X           Y          WL" );
      for (j = 1; j <= lmt.val; j++) {
        System.out.print((j) + " ");
        for(k = 1; k <= 3; k++)
          System.out.print(angle[(j)- 1+(k- 1)*xx.length] + " ");
        System.out.print((xx[(j)- 1+ _xx_offset]) + " " 
                         +(yy[(j)-1+ _yy_offset])+" "+(zz[(j)-1+ _zz_offset]));

        System.out.println();
        // WRITE(16,*)J,(ANGLE(J,K),K=1,3),XX(J),YY(J),ZZ(J)
      }
    } else {
      System.out.println(" "+"\n"+"    #  SEQ       XCM       YCM      WL" );
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
      }
    }              //  Close else.
    // Manipulates the basis(the first 3 elements of xx,yy,and zz) so B*Tranps(B) about diagonal

    abid(lmt,xx,yy,zz);
    if( errormessage.length()>0)
      return;

    return;
  }



  /**
   * Sorts the peak vectors according to magnitude, then picks the
   * first 3 that are not coplanar.  Then aair manipulates these Basis
   * so that B*Transp(B) about diagonal.  The Basis vectors are stored
   * in the first three positions of XX,YY, and ZZ. The other vectors
   * are all moved up d
   */
  private static void abid (intW lmt, double[] xx, double[] yy, double[] zz ) {
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
    /*File ff= new File( "x.out");
      OutputStream fout;
      try{
      fout= new FileOutputStream( ff);
      }
      catch( Exception sss)
      {fout = System.out;
      }*/
    //Index sort of w, the magnitude of the the peaks, with index l 
    for (i = 1; i <= lmt.val; i++) {
      hm = xx[(i)- 1+ _xx_offset]*xx[(i)- 1+ _xx_offset]+yy[(i)- 1+ _yy_offset]*yy[(i)- 1+ _yy_offset]+zz[(i)- 1+ _zz_offset]*zz[(i)- 1+ _zz_offset];
      if( hm >= 1.0E9)
        System.out.println("abid hm value  is very large");
      Goto = 0;
      if( Double.isNaN(hm)) hm = .9E9;
      /*   try{
           fout.write(("hm="+hm+"\n").getBytes());
           }
           catch( Exception sss5){}*/
      for (j = 1; (j <= lmt.val) &&(Goto ==0); j++) {
        if ((hm < w[(j)- 1]) && (Goto == 0))  {
    
          for (idum = j; idum <= lmt.val; idum++) {
            k = lmt.val+j-idum;
            w[(k+1)- 1] = w[(k)- 1];
            l[(k+1)- 1] = l[(k)- 1];
          }              //  Close for() loop. 

          w[(j)- 1] = hm;
          l[(j)- 1] = i;
          Goto = 3;
          /*     try{
                 for( int ii=0; ii<lmt.val+1 ;ii++)fout.write((l[ii]+" ").getBytes());fout.write("\n".getBytes());
                 for( int ii=0; ii<lmt.val+1 ;ii++)fout.write((w[ii]+" ").getBytes());fout.write("\n".getBytes());
                 }
                 catch(Exception sss1){}*/
        }              // Close if()
      }              //  Close for() loop. 
    }              //  Close for() loop. 

    /*try{
      fout.close();
      }
      catch(Exception ssss3){}*/
    for (j = 1; j <= lmt.val; j++) { 
      xa[(j)- 1] = xx[(l[(j)- 1])- 1+ _xx_offset];
      ya[(j)- 1] = yy[(l[(j)- 1])- 1+ _yy_offset];
      za[(j)- 1] = zz[(l[(j)- 1])- 1+ _zz_offset];
    }
    // 
    k = 3;
    Goto=6;
    //While the proposed basis vectors are coplanar
    while( Goto==6){
      Goto=0;
      k = k+1;
   
      if (k > lmt.val)  {
        System.out.println(" ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING");
        System.out.println(" ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING");
        // 
        for (i = 1; i <= 3; i++) {
          System.out.println(" "+(xa[i-1])+"    "+(ya[i-1])+"    "+(za[i-1]));
          System.out.println(" "+(xa[i-1])+"    "+(ya[i-1])+"    "+(za[i-1]));
          // 
          System.out.println(" D="+d);
          System.out.println(" D="+d);
          //System.exit(1);
          errormessage="  ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING ";
          return;
        }
      }
      for (i = 0; i < 3; i++) {
        b[i + 0*3] = xa[i];
        b[i + 1*3] = ya[i];
        b[i + 2*3] = za[i];
      }              //  Close for() loop. 

      doubleW dA = new doubleW(d);
      mi(b,a,dA);

      d=dA.val;
      // d is the determinant of the basis vectors if close to zero,
      // then basis are coplanar.  Interchange the kth peak with one
      // of the basis.
      // 
      if (Math.abs(d) < 0.0001)  {
        if (Math.abs(xa[0]*ya[1]-xa[1]*ya[0]) >= 0.05 
            || Math.abs(xa[0]*za[1]-xa[1]*za[0]) >= 0.05 
            || Math.abs(ya[0]*za[1]-ya[1]*za[0]) >= 0.05)  {
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
        } else  {
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
    }

    //Manipulate b so that B*transpose(B) about diagonal
    aarr(b,vv,ll);

    for (idum = 1; idum <= lmt.val; idum++) {
      i = lmt.val+1-idum;


      xx[(i+3)- 1+ _xx_offset] = xx[(i)- 1+ _xx_offset];
      yy[(i+3)- 1+ _yy_offset] = yy[(i)- 1+ _yy_offset];
      zz[(i+3)- 1+ _zz_offset] = zz[(i)- 1+ _zz_offset];
    }

    for( i=0 ; i<3 ; i++ ){
      xx[i + _xx_offset] = b[i + 0*3];
      yy[i + _yy_offset] = b[i + 1*3];
      zz[i + _zz_offset] = b[i + 2*3];
    }
    // 
    lmt.val = lmt.val+3;

    return;
  }

  /**
   * Manipulate basis ab so that ab*transpose(ab) about (integer) diagonal
   */
  public static void aarr (double [] ab, double [] v, int [] l)  {
    int _ab_offset=0;
    int _v_offset=0;
    int _l_offset = 0;
    int j=0,m=0,k=0,i=0,kk=0;

    while( 3==3){ // ADDed for goto 1 at bottom
      for (j = 1; j <= 6; j++) {
        v[(j)- 1+ _v_offset] = 0.0;
      }              //  Close for() loop. 

      for (j = 1; j <= 3; j++) {
        m = j+1;
        if (m > 3)  
          m = m-3;

        for( i=0 ; i<3 ; i++ ){
          v[(j)- 1+ _v_offset] = v[(j)- 1+ _v_offset]+ab[(j)- 1+ i*3 + _ab_offset]*ab[(j)- 1+ i*3+ _ab_offset];
          v[(j+3)- 1+ _v_offset] = v[(j+3)- 1+ _v_offset]+ab[(j)- 1+ i*3 + _ab_offset]*ab[(m)- 1+ i*3+ _ab_offset];
        }              //  Close for() loop. 
      }              //  Close for() loop. 

      // 

      for (j = 1; j <= 3; j++) {
        m = j+1;
        if (m > 3)  
          m = m-3;
        if (v[(j+3)- 1+ _v_offset] >= 0.0)  {
          l[j-1+_l_offset]=(int)(v[j+2+_v_offset]/v[j-1+_v_offset]+0.498);
          l[j+2+_l_offset]=(int)(v[j+2+_v_offset]/v[m-1+_v_offset]+0.498);
        } else {
          l[j-1+_l_offset]=(int)(v[j+2+_v_offset]/v[j-1+_v_offset]-0.498);
          l[j+2+_l_offset]=(int)(v[j+2+_v_offset]/v[m-1+_v_offset]-0.498);
        }              //  Close else.
      }              //  Close for() loop. 

      // 
      l[6 + _l_offset] = 0;
      // 

      for (j = 1; j <= 6; j++) {
        kk = l[j-1 + _l_offset];
        if (kk < 0)  {
          kk = -kk;
        }              // Close if()
        if ((kk) > l[6 + _l_offset])  {
          l[6 + _l_offset] = (kk);
          k = j;
        }              // Close if()
      }              //  Close for() loop. 

      // 
      if (l[6 + _l_offset] == 0)  {
        return;//Dummy.go_to("Aarr",999999);
      }
      if (k >= 4)  {
        for( j=0 ; j<3 ; j++ ){
          m = k-2;
          if (m > 3)  
            m = m-3;
          ab[(k-4)+j*3+_ab_offset]=
            ab[(k-4)+j*3+_ab_offset]-l[k-1+_l_offset]*ab[m-1+j*3+_ab_offset];
        }              //  Close for() loop. 
      } else {
        for( j=0 ; j<3 ; j++ ){
          m = k+1;
          if (m > 3)  
            m = m-3;
          ab[m-1+j*3+_ab_offset]=
            ab[m-1+j*3+_ab_offset]-l[k-1+_l_offset]*ab[k-1+j*3+_ab_offset];
        }              //  Close for() loop. 

      }              //  Close else.
    }
  }

  /**
   *
   */
  public static void mi (double [] ad, double [] aid, doubleW dsing)  {
    int _ad_offset=0;
    int _aid_offset=0;
    int i=0,j=0,m=0,n=0,l=0,k=0;
    double [] ai= new double[(3) * (3)];
    double [] a= new double[(3) * (3)];
    double d=0.0f;

    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        a[i + j*3]  = ad[i + j*3 + _ad_offset];
        ai[i + j*3] = aid[i + j*3 + _aid_offset];
      }
    }
    d = dsing.val;
    i = 0;
    for (j = 1; j <= 3; j++) {
      m = j+1;
      if (m > 3)  
        m = m-3;
      n = j+2;
      if (n > 3)  
        n = n-3;
      for (i = 1; i <= 3; i++) {
        k = i+1;
        if (k > 3)  
          k = k-3;
        l = i+2;
        if (l > 3)  
          l = l-3;
        ai[i-1+(j- 1)*3]=
          a[m-1+(k- 1)*3]*a[n-1+(l- 1)*3]-a[m-1+(l-1)*3]*a[n-1+(k-1)*3];
      }
    }
    d = ai[0+0*3]*a[0+0*3]+ai[1+0*3]*a[0+1*3]+a[0+2*3]*ai[2+0*3];
    if (d == 0.0e0)  
      return;
    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        ai[i +j*3] = ai[i+j*3]/d;
      }
    }
    for( i=0 ; i<3; i++ ){
      for( j=0 ; j<3; j++ ){
        ad[i+j*3+ _ad_offset] = a[i+j*3];
        aid[i+j*3+ _aid_offset] = ai[i+j*3];
      }
    }

    dsing.val = d;
    return;
  }

  /**
   *
   */
  public static void bias (int lmt, double [] xx, double [] yy, double [] zz,
                           double [] b, double mw, double den, doubleW dd,
                           double dj, intW mj, int [] seq,  int input,
                           int expnum, int hstnum)  {
    int _xx_offset =0;
    int _yy_offset =0;
    int _zz_offset= 0;
    int _b_offset = 0;
    int _seq_offset =0;
    double [] hh= new double[3 * lmt];
    double [] a = new double[3 * 3];
    double d= 0.0;
    // 
    int [] jh= new int[3 * lmt];
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
    while( Goto==1){
      Goto=0;
      Goto = 0;
      for( j=0 ; j<3 ; j++ ){
        b[0+j*3+ _b_offset] = xx[j+ _xx_offset];
        b[1+j*3+ _b_offset] = yy[j+ _yy_offset];
        b[2+j*3+ _b_offset] = zz[j+ _zz_offset];
      }
      // 
      DW.val=d;
      mi(b,a,DW);
      d= DW.val;
      // 
      j = 0;
      for( j=0 ; j<3 ; j++ ){
        for( i=0 ; i<lmt ; i++ ) {
          hh[j+i*3] = a[j+0*3]*xx[i+_xx_offset]
            +a[j+1*3]*yy[i+_yy_offset]+a[j+2*3]*zz[i+ _zz_offset];
        }
      }
      mm = false;

      IW.val=iz;
      lcl(hh,lmt,dd,IW);
      if( errormessage.length()>0)
        return;
      //dd=FW.val;
      iz=IW.val;
      System.out.println("Aft LCL"+lmt+","+dd.val+","+iz);
      for(  i=0 ; i<hh.length ; i++ ) System.out.print(hh[i]+",");
      System.out.println("");
      if (Math.abs(dd.val-0.100) < 0.00001)  
        dd.val = 0.100;
      // 
      if (dd.val == 0.100)  {
        dd.val = -0.010;
        Goto = 1;
      }              // Close if()
      if (Goto == 0)  {
        printB("before Aaio,B=",b);
        aaio(xx,yy,zz,b,hh,lmt);
        // 
        printB("bef Aair,B=",b);
        aair(b,a);
        // 
        printB("before Thh,B=",b);
        booleanW BW= new booleanW(mm);
        thh(hh,xx,yy,zz,a,jh,BW,dd.val,lmt);
        mm= BW.val;

        // 
        printB("aft Thh,B=",b);
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
    }//while Goto==1
    System.out.println(" \n"+"******************" );
    System.out.println(" \n"  + " ERROR LIMIT="  + (dd.val) + " " );
    System.out.println(" \n"  + " REDUCED CELL" );
    lst(hh,xx,yy,zz,a,jh,mw,b,d,lmt,seq,den,input,expnum,hstnum);
    // 
    mj.val = -iz;
    return;
  }

  /**
   *
   */
  public static void lcl (double [] fh, int lmt, doubleW dd, intW iz)  {
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

    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<lmt ; j++ ){
        hh[i+j*3] = (int)(fh[i+j*3+ _fh_offset]*ni);
      }
    }

    ha = hh[0+(lmt- 1)*3];
    hb = hh[1+(lmt- 1)*3];
    hc = hh[2+(lmt- 1)*3];

    while( 3==3){
      if (Goto == 0 || Goto == 2) 
        Goto = 0;

      dd.val = dd.val+0.020;

      if (dd.val > 0.30)  {
        for( i=0 ; i<lmt ; i++ ){
          System.out.println(" " + (fh[0+i*3+ _fh_offset]) + " " );
          errormessage ="INITIAL NON-INTEGER INDICES ";
          return;
        }
      }

      iz.val = iz.val+1;
      da = (int)(dd.val*ni);
      kka = 0;
      for (mm = 1; mm <= 10; mm++) {
        if (Goto == 0)  
          kk = mm+1;
        for (iduma = 1; iduma <= kk; iduma++) {
          if (Goto == 0)  
            k = iduma-1;
          if (Goto == 0)  
            s1 = k*ha;
          for (idumb = 1; idumb <= kk; idumb++) {
            if (Goto == 0)  {
              l = idumb-1;
              s2 = l*hb;
            }              // Close if()
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
              while( Goto !=0){
                if (Goto == 0 || Goto == 14)  {
                  Goto = 0;
  
                  intW LB= new intW(lb);
                  if (equ(s1+s2+s3,LB,da) == 0)  {
                    lb=LB.val;
    
                    la[0] = k;
                    la[1] = l;
                    la[2] = m;
                    n = 2;
                    la[lmt- 1] = lb;

                    Goto = 30;
                  } 
                  else lb=LB.val;             // Close if()
                }              // Close if()
                if (Goto == 0 || Goto == 20)  {
                  Goto = 0;
                  if (l == 0 && Goto == 0)  
                    Goto = 21;
                  intW LB= new intW(lb);
                  if (Goto == 0 && equ(s1-s2+s3,LB,da) == 0)  {
                    lb=LB.val;
    
                    la[0] = k;
                    la[1] = -l;
                    la[2] = m;
                    n = 3;
                    la[(lmt)- 1] = lb;

                    Goto = 30;
                  }              // Close if()
                }              // Close if()

                if (Goto == 0 || Goto == 21)  {
                  Goto = 0;
                  if (k == 0 && Goto == 0)  
                    Goto = 803;
                  if (m != 0 && Goto == 0)  {
     
                    intW LB= new intW(lb);
                    if (equ(s1+s2-s3,LB,da) == 0)  {
                      lb=LB.val;
                      la[0] = k;
                      la[1] = l;
                      la[2] = -m;
                      n = 4;
                      la[lmt-1] = lb;

                      Goto = 30;
                    }              // Close if()
                  }              // Close if()
                }              // Close if()

                if (Goto == 0 || Goto == 17)  {
                  Goto = 0;
    
                  n = 5;
   
                  if (l == 0 || m == 0)  
                    Goto = 19;
                  intW LB= new intW(lb);
  
                  if (equ(s1-s2-s3,LB,da) != 0) {
                    Goto = 19;
                    lb=LB.val;
                  }

                  if (Goto == 0)  {
                    la[0] = k;
                    la[1] = -l;
                    la[2] = -m;
                    n = 5;
                    la[lmt-1] = lb;

                  }              // Close if()
                }              // Close if()
                if (Goto == 0 || Goto == 30)  {
                  Goto = 0;
 
                  jdum = lmt-1;

                  for (idum = 4; idum <= jdum; idum++) {
                    if (Goto == 0)  {
                      j = 3+lmt-idum;
                      kkdum = la[0]*hh[0+(j-1)*3]
                        +la[1]*hh[1+(j-1)*3]+la[2]*hh[2+(j-1)*3];
                      intW LB= new intW(lb);

                      if (equ(kkdum,LB,da) != 0) {
                        Goto = 19;
                      }
                      lb=LB.val;
                      if (Goto == 0)  
                        la[(j)- 1] = lb;
                    }
                  }

                  if (Goto == 0)  {
                    kka = kka+1;

                    for( j=0 ; j<lmt ; j++ ){
                      ll[kka-1+j*3] = la[j];
                    }
                  }
                } 
                if( Goto==0){
      
                }
                if (Goto == 0) { 
                  if (kka == 1)  
                    Goto = 19;

                }
                if (Goto == 0)  {
                  m1 = ll[0+0*3]*ll[1+1*3]-ll[0+1*3]*ll[1+0*3];
                  m2 = ll[0+0*3]*ll[1+2*3]-ll[0+2*3]*ll[1+0*3];
                  m3 = ll[0+1*3]*ll[1+2*3]-ll[0+2*3]*ll[1+1*3];
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
  
                  d = m1*ll[2+2*3]-m2*ll[2+1*3]+m3*ll[2+0*3];
                  if (d == 0)  {
                    kka = 2;
                    Goto = 19;
                  } else {
                    for( j=0 ; j<lmt ; j++ ){
                      for( i=0 ; i<3 ; i++ ){
                        fh[i+j*3+ _fh_offset] = (double)(ll[i+j*3]);
                      }
                    }

                    return;
                  }
                }

                if (Goto == 19 || Goto == 0)  {
                  Goto = 0;
  
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
                }
                if (Goto == 803)  
                  Goto = 0;
              }              //  Close for() loop. 
            }
          }
        }
      }              //  Close for() loop. 
    }//while 3==3 gotot 2.go_to("Lcl",2);
  }


  public static int equ (int s, intW lb, int da)  {

    int ni = 512;
    int nj = 256;
    int EQU = 0;

    if (s >= 0)  {
      lb.val = (s+nj)/ni;
    } else  {
      lb.val = (s-nj)/ni;
    }

    if (Math.abs(s-lb.val*ni) >= da)  {
      EQU = 1;
    } else  {
      EQU = 0;
    }

    return EQU;
  }


  // 

  public static void aair (double [] b, double [] a )  {
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



    for( i=1 ; i<=3 ; i++ ){
      j=1;
      boolean done=false;

      while(!done){
        if(v[i-1]<w[j-1]){
          for( idum=j;idum<=3;idum++){
            k=j+3-idum;
            w[k+1-1]=w[k-1];
            l[k+1-1]=l[k-1];
          }
          w[j-1] = v[i-1];
          l[j-1] = i;
          done=true;
        }else{
          j++;
          if( j>3)done=true;
        }
      }
    }

    // 

    System.out.println( "Aair,l="+l[0]+","+l[1]+","+l[2]+","+l[3]+","+l[4]+","+
                        l[5]+","+l[6]+","+a.length+","+ab.length);
    System.out.println("W="+w[0]+","+w[1]+","+w[2]+","+w[3]);
    for( i=0 ; i<3 ; i++ ){
      a[2+i*3+ _a_offset] = ab[(l[0])- 1+i*3];
      a[0+i*3+ _a_offset] = ab[(l[1])- 1+i*3];
      a[1+i*3+ _a_offset] = ab[(l[2])- 1+i*3];
    }
    System.out.println("aft 3, A=");
    for( i=0;i<a.length;i++)
      System.out.print(a[i]+",");
    System.out.println("");

    // 
    w[3] = v[4];
    v[4] = v[5];
    v[5] = w[3];
    if (v[l[0]+l[1]] > 0.0)  {
      for( i=0 ; i<3 ; i++ ){
        a[0+i*3+ _a_offset] = -a[0+i*3+ _a_offset];
        v[l[0]+l[1]] = -v[l[0]+l[1]];
        v[l[1]+l[2]] = -v[l[1]+l[2]];
      }
    }

    if (v[l[2]+l[0]] > 0.0)  {
        for( i=0 ; i<3 ; i++ ){
          a[1+i*3+ _a_offset] = -a[1+i*3+ _a_offset];
          v[l[2]+l[0]] = -v[l[2]+l[0]];
          v[l[1]+l[2]] = -v[l[1]+l[2]];
      }
    }

    printB("aft 11, A=",a);

    doubleW dW1= new doubleW( d);
    mi(a,b,dW1);
    d= dW1.val;

    if (d >= 0.0)  
      return;
    System.out.println("D, neg entries "+d);

    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        a[i+j*3+ _a_offset] = -a[i+j*3+ _a_offset];
        b[i+j*3+ _b_offset] = -b[i+j*3+ _b_offset];
      }
    }
  }

  /**
   *
   */
  public static void aaio (double [] xx, double [] yy, double [] zz, 
                           double [] b, double [] hh, int lmt)  {
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

    i = i+1;
    j = 0;
    j = j+1;
    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        a[i+j*3] = 0.0;
        b[i+j*3+ _b_offset] = 0.0;
        for( k=0 ; k<lmt ; k++ ){
          b[i+j*3+_b_offset]=
            b[i+j*3+_b_offset]+hh[i+k*3+_hh_offset]*hh[j+k*3+_hh_offset];
        }
      }
    }

    for( i=0 ; i<3 ; i++ ){
      for( k=0 ; k<lmt ; k++ ){
        a[0+i*3]=a[0+i*3]+xx[k+_xx_offset]*hh[i+k*3+_hh_offset];
        a[1+i*3]=a[1+i*3]+yy[k+_yy_offset]*hh[i+k*3+_hh_offset];
        a[2+i*3]=a[2+i*3]+zz[k+_zz_offset]*hh[i+k*3+_hh_offset];
      }
    }

    doubleW Dd= new doubleW(d);
    blind.mi(b,ai,Dd);
    d = Dd.val;

    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        b[i+j*3+_b_offset] = 0.0;
        for( k=0 ; k<3 ; k++ ){
            b[i+j*3+_b_offset]=b[i+j*3+_b_offset]+a[i+k*3]*ai[k+j*3];
        }
      }
    }

    return;
  }

  /**
   *
   */
  public static void thh (double[] hh, double[] xx, double[] yy, double[] zz, 
                          double [] a,  int [] jh, booleanW mm, double dd,
                          int lmt){
    int xx_offset=0;
    int yy_offset=0;
    int zz_offset=0;
    int jh_offset=0;
    int hh_offset=0;
    int a_offset=0;
    int i= 0;
    int j= 0;
    int lb= 0;

    for( i=3 ; i<lmt ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        hh[j+i*3+hh_offset]=a[j+0*3+a_offset]*xx[i+xx_offset]
          +a[j+1*3+a_offset]*yy[i+yy_offset]+a[j+2*3+a_offset]*zz[i+zz_offset];
        if (hh[j+i*3+hh_offset] >= 0.0)  {
          lb = (int)(hh[j+i*3+hh_offset]+0.5);
        } else {
          lb = (int)(hh[j+i*3+hh_offset]-0.5);
        }
        if (Math.abs(hh[j+i*3+hh_offset]-lb) > dd)  {
          mm.val = true;
        }
        jh[j+i*3+jh_offset] = lb;
      }
    }

    return;
  }

  /**
   *
   */
  public static void lst( double[] HH, double[] XX,double[] YY, double[] ZZ,
                          double[] A, int[] JH, double MW, double[] B,double D,
                          int LMT,int[] SEQ, double DEN, int INPUT, int EXPNUM,
                          int HSTNUM){
    double d=0;
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
    A2=AI[-3+3*1]*AI[-3+3*1]+AI[-3+3*2]*AI[-3+3*2]+AI[-3+3*3]*AI[-3+3*3];
    B2=AI[-2+3*1]*AI[-2+3*1]+AI[-2+3*2]*AI[-2+3*2]+AI[-2+3*3]*AI[-2+3*3];
    C2=AI[-1+3*1]*AI[-1+3*1]+AI[-1+3*2]*AI[-1+3*2]+AI[-1+3*3]*AI[-1+3*3];

    DAB= AI[-3+3*1]*AI[-2+3*1]+AI[-3+3*2]*AI[-2+3*2]+AI[-3+3*3]*AI[-2+3*3];
    DAC= AI[-3+3*1]*AI[-1+3*1]+AI[-3+3*2]*AI[-1+3*2]+AI[-3+3*3]*AI[-1+3*3] ;
    DBC= AI[-2+3*1]*AI[-1+3*1]+AI[-2+3*2]*AI[-1+3*2]+AI[-2+3*3]*AI[-1+3*3];
     
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
      System.out.println((i+1)+"  "+SEQ[i]+"  "+JH[(i+3)*3]+"  "+JH[3*(i+3)+1]
                         +"  "+JH[3*(i+3)+2]);
    System.out.println("");
     


    double[] orgmat= new double[9];
    u = new double[9];
    for(int i=1;i<=3;i++)
      for(int j=1;j<=3;j++)
        {orgmat[i-4+3*j]=B[j-4+3*i];
        u[i-4+3*j]=B[j-4+3*i];
        }
        
    double vol=subs.tstvol(orgmat);

    if( vol < 0) 
      System.out.println("Left handed system");
    for(int i=1;i<=3;i++)
      System.out.println( orgmat[i-4+3*1]+"  "+orgmat[i-4+3*2]+"  "
                          +orgmat[i-4+3*3]);
  }
   


  /**
   * blind main program only using the run file
   * @param args[0] should be the the name of the run file or peaks file
   * @param args[1] list of sequence numbers
   */
  public static void main( String args[]){
    String filename = args[0];
    int[] seq = DataSetTools.util.IntList.ToArray(args[1]);
    if( seq == null)
      System.exit(0);
    if( seq.length < 1)
      System.exit(0);

    Vector V=new Vector();
    if( !(filename.toUpperCase().indexOf(".PEA")>0)){
      DataSet[] DS = (new IsawGUI.Util()).loadRunfile( filename);
      int  k=DS.length-1;
      if( args.length >1)
        try{
          k= ( new Integer( args[1])).intValue();
        }catch(Exception ss){}

        DataSet ds = DS[k];
        // ds.setAttribute( Attribute.SCD_CALIB_FILE,"C:\\Ipns\\Isaw2\\SampleRuns\\instprm.dat");
        LoadSCDCalib lcab= new LoadSCDCalib( ds,"C:\\Ipns\\Isaw2\\SampleRuns\\instprm.dat",
                                             1,"0:7000");
        lcab.getResult(); 
        FindPeaks fp = new FindPeaks( ds,0, 15,1);
        V = (Vector)(fp.getResult());
        CentroidPeaks cp = new CentroidPeaks(ds, V);

        V = (Vector)(cp.getResult());

        for(int i=0;i<V.size();i++){
          Peak pk=(Peak)(V.elementAt(i));
          System.out.println("Pk i,xcm,ycm,wl="+pk.xcm()+","+pk.ycm()+","+pk.wl());
          System.out.println("    chi,phi,om,deta,detd="+pk.chi()+","+pk.phi()+","+
                             pk.omega()+","+pk.detA()+","+pk.detD());
          }
      } else { //we have a peaks file-faster for testing purposes
        float chi=0.0f,phi=0.0f,omega=0.0f,deta=0.0f,detd=0.0f;
     
        TextFileReader fin=null;
        try{ 
          fin= new TextFileReader( filename);
          fin.read_line();
          int nseq= 0; 
          while( (!fin.eof())&&(nseq < seq.length)){
            int kk = fin.read_int();
            if(kk==1){
              kk=fin.read_int();
              kk=fin.read_int();
              deta= (float)(fin.read_float() );// /180*java.lang.Math.PI);
              detd= (float) (fin.read_float());// /180*java.lang.Math.PI);
              detd= (float)(fin.read_float());// /180*java.lang.Math.PI);
              chi= (float)(fin.read_float());// /180*java.lang.Math.PI);
              phi=(float)( fin.read_float());// /180*java.lang.Math.PI);
              omega=(float) (fin.read_float());// /180*java.lang.Math.PI);

              fin.read_line();
              
            }else if( kk==3) {
              int seqnum=fin.read_int();
         
              boolean done = seqnum <= seq[nseq];
              while( !done) 
                {nseq ++;
                if(nseq >= seq.length)
                  done = true;
                else
                  done = seqnum <= seq[nseq];
                }
              if( nseq < seq.length){
                if( seqnum== seq[nseq]){
                  float[] dat = new float[9];
                  dat[ 8]  = seqnum;
                  kk = fin.read_int();
                  kk = fin.read_int();
                  kk = fin.read_int();

                  float x = fin.read_float();
                  float y = fin.read_float();
                  float z = fin.read_float();
           
                  dat[ 5 ] = fin.read_float();
                  dat[ 6 ] = fin.read_float();
                  dat[ 7 ] = fin.read_float();
                  dat[ 0 ] = chi;
                  dat[ 1 ] = phi;
                  dat[ 2 ] = omega;
                  dat[ 3 ] = deta;
                  dat[ 4]  = detd;
                  fin.read_line();
          
                  V.addElement(dat);
                }else{
                  fin.read_line();
                }
              }else{
                fin.read_line();
              }
            }else{
              fin.read_line();
            }
          }
        }catch( Exception s){
          System.out.println("error="+s);
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
    for( int i=0;i<3;i++){
      for (int j=0;j<3;j++)
        System.out.print(blind.u[3*j+i]+" ");
      System.out.println("");
    }

    System.out.println(blind.D1+" "+blind.D2+" "+blind.D3+" "+blind.D4+" "+
                       blind.D5+" "+blind.D6+" "+blind.cellVol);
  }

  private static void printB(String label, double[] b){
    System.out.println(label);
    for( int i=0 ; i<b.length ; i++ )
      System.out.print(b[i]+",");
    System.out.println("");
  }
} // End class.
