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
 * Revision 1.9  2003/04/30 19:56:23  pfpeterson
 * Code cleanup after finding out what some of the variable names
 * physically represent. This means D1,D2,...,D6 are replaced with
 * abc[7] and A2,..,DAB,... are replaced with scalars[6].
 *
 * Revision 1.8  2003/02/21 16:56:55  pfpeterson
 * Further completed writting the log file, now is just missing sequence
 * numbers. Changed methods to be instance rather than static. Also more
 * code cleanup including turning formatted printing into static method,
 * changing 1-indexed access into true 0-indexed access, and pushing
 * none-logfile output (to STDOUT) to be only when debug flag is set.
 *
 * Revision 1.7  2003/02/20 21:39:51  pfpeterson
 * First pass at printing a log file.
 *
 * Revision 1.6  2003/02/19 19:35:08  pfpeterson
 * Moderate reformatting. Includes changing for loops to start from 0
 * rather than 1.
 *
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

import java.util.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.operator.DataSet.Attribute.*;
import java.text.DecimalFormat;
import java.io.*;

public class blind {
  private static final boolean DEBUG=false;

  //orientation matrix
  public double[] u = null;
  //Cell dimensions
  public double[] abc; //a,b,c,alpha,beta,gamma,cellVol

  public String errormessage="";
  private StringBuffer logBuffer;

  public blind(){
    u=null;
    abc=new double[7];
    errormessage="";

    // create the logfile contents
    logBuffer=new StringBuffer(50*19);
    System.out.println();
    System.out.print("  *******LAUE INDEXER*******\n");
    System.out.println();
    System.out.print("    #  SEQ       XCM       YCM      WL\n");
    logBuffer.append("\n");
    logBuffer.append("  *******LAUE INDEXER*******\n");
    logBuffer.append("\n");
    logBuffer.append("    #  SEQ       XCM       YCM      WL\n");

  }

  /**
   * Finds the basis of the smallest non coplanar in Qx,Qy,Qz peaks.
   * The basis is manipulated so that B*Transpose(B) is "about" a
   * diagonal
   */ 
  public void blaue (Vector peaks,double[] xx,double[] yy,double[] zz, 
                            intW lmt, int[] seq) {
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
    if(DEBUG) System.out.println("peaks size="+peaks.size());
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
            if(DEBUG)
              System.out.println("Q vals="+format(x1.val,4)+","
                                 +format(y1.val,5)+","+format(z1.val,4));
            if( Double.isNaN(xx[i]))SS+="xx";
            if( Double.isNaN(yy[i]))SS+="yy";
            if( Double.isNaN(zz[i]))SS+="zz";
            if( SS.length() > 0)
              if(DEBUG) System.out.println("Laue "+ i+"::"+SS);
            lmt.val++;
          }
      }
    lmt.val--;

    // END OF REFLECTION INPUT
    continuelmt = (double)(lmt.val-1);
    if(DEBUG){
      if( ilog!=1 ){
        System.out.println("      #           X          Y         WL" );
        for( j=0 ; j<lmt.val ; j++ ){
          System.out.print(format(j,7,0)+" ");
          for( k=0; k<3 ; k++ )
            System.out.print(format(angle[j+k*xx.length],11,4));
          System.out.print(format(xx[j+_xx_offset],9,4)
                           +format(yy[j+ _yy_offset],9,4)
                           +format(zz[j+ _zz_offset],9,4));
          System.out.println();
        }
      } else {
        System.out.println(" "+"\n"+"    #  SEQ       XCM       YCM      WL" );
        System.out.println( "lmt seq.length="+lmt.val+","+seq.length);
        for( j=0 ; j<lmt.val ; j++ ){
          System.out.print((j+1)+" "+(seq[j+_seq_offset])+" ");
          for( k=0 ; k<3 ; k++ )
            System.out.print(angle[j+k*xx.length]+" ");
          System.out.println();
        }
      }
    }

    // Manipulates the basis(the first 3 elements of xx,yy,and zz) so
    // B*Tranps(B) about diagonal
    for( j=0 ; j<lmt.val ; j++ ){
      System.out.print(format(j+1,5,0)+format(seq[j+_seq_offset],5,0));
      logBuffer.append(format(j+1,5,0)+format(seq[j+_seq_offset],5,0));
      for( k=0 ; k<3 ; k++ ){
        if(k!=2){
          System.out.print(format(angle[j+k*xx.length],10,3));
          logBuffer.append(format(angle[j+k*xx.length],10,3));
        }else{
          System.out.print(format(angle[j+k*xx.length],10,4));
          logBuffer.append(format(angle[j+k*xx.length],10,4));
        }
      }
      System.out.println();
      logBuffer.append("\n");
    }
    System.out.println();
    logBuffer.append("\n");

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
  private void abid (intW lmt, double[] xx, double[] yy, double[] zz ) {
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
    for( i=0 ; i<lmt.val ; i++ ){
      l[i]=0;
      w[i]=1.0E9;
    }

    //Index sort of w, the magnitude of the the peaks, with index l 
    for( i=0 ; i<lmt.val ; i++ ){
      hm = xx[i+_xx_offset]*xx[i+_xx_offset]
        +yy[i+_yy_offset]*yy[i+_yy_offset]
        +zz[i+_zz_offset]*zz[i+_zz_offset];
      if( hm >= 1.0E9)
        if(DEBUG) System.out.println("abid hm value  is very large");
      Goto = 0;
      if( Double.isNaN(hm)) hm = .9E9;

      for( j=0 ; (j<lmt.val)&&(Goto==0) ; j++ ){
        if( (hm<w[j]) && (Goto==0) ){
          for( idum=j ; idum<lmt.val ; idum++ ){
            k=lmt.val+j-idum;
            w[k] = w[k-1];
            l[k] = l[k-1];
          }

          w[j] = hm;
          l[j] = i+1;
          Goto = 3;
        }
      }
    }

    for( j=0 ; j<lmt.val ; j++ ){ 
      xa[j] = xx[l[j]-1+_xx_offset];
      ya[j] = yy[l[j]-1+_yy_offset];
      za[j] = zz[l[j]-1+_zz_offset];
    }
    k = 3;
    Goto=6;

    //While the proposed basis vectors are coplanar
    while( Goto==6){
      Goto=0;
      k++;
   
      if (k > lmt.val)  {
        System.out.println(" ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING");
        logBuffer.append(" ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING\n");
        // 
        for( i=0 ; i<3 ; i++ ){
          if(DEBUG){
            System.out.println(" "+xa[i]+"    "+ya[i]+"    "+za[i]);
            System.out.println(" D="+d);
          }
        }
        return;
      }
      for( i=0 ; i<3 ; i++ ){
        b[i+0*3] = xa[i];
        b[i+1*3] = ya[i];
        b[i+2*3] = za[i];
      }              //  Close for() loop. 

      d=mi(b,a);

      // d is the determinant of the basis vectors if close to zero,
      // then basis are coplanar.  Interchange the kth peak with one
      // of the basis.
      if (Math.abs(d) < 0.0001)  {
        if (Math.abs(xa[0]*ya[1]-xa[1]*ya[0]) >= 0.05 
            || Math.abs(xa[0]*za[1]-xa[1]*za[0]) >= 0.05 
            || Math.abs(ya[0]*za[1]-ya[1]*za[0]) >= 0.05)  {
          hm = xa[k-1];
          xa[k-1] = xa[2];
          xa[2] = hm;
          hm = ya[k-1];
          ya[k-1] = ya[2];
          ya[2] = hm;
          hm = za[k-1];
          za[k-1] = za[2];
          za[2] = hm;
          m = l[k-1];
          l[k-1] = l[2];
          l[2] = m;
          Goto = 6;
        } else  {
          hm = xa[k-1];
          xa[k-1] = xa[1];
          xa[1] = xa[2];
          xa[2] = hm;
          hm = ya[(k)- 1];
          ya[k-1] = ya[1];
          ya[1] = ya[2];
          ya[2] = hm;
          hm = za[k-1];
          za[k-1] = za[1];
          za[1] = za[2];
          za[2] = hm;
          m = l[k-1];
          l[k-1] = l[1];
          l[1] = m;
          Goto = 6;
        }
      }
    }

    //Manipulate b so that B*transpose(B) about diagonal
    aarr(b,vv,ll);

    for (idum = 1; idum <= lmt.val; idum++) {
      i = lmt.val+1-idum;


      xx[i+2+_xx_offset] = xx[i-1+_xx_offset];
      yy[i+2+_yy_offset] = yy[i-1+_yy_offset];
      zz[i+2+_zz_offset] = zz[i-1+_zz_offset];
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
  public void aarr (double [] ab, double [] v, int [] l)  {
    int j=0,m=0,k=0,i=0,kk=0;

    while(true){ // ADDed for goto 1 at bottom
      for( j=0 ; j<6 ; j++ ){
        v[j] = 0.0;
      }

      for( j=0 ; j<3 ; j++ ){
        m = (j+1)%3;
        for( i=0 ; i<3 ; i++ ){
          v[j]=v[j]+ab[j+i*3]*ab[j+i*3];
          v[j+3]=v[j+3]+ab[j+i*3]*ab[m+i*3];
        }
      }

      for( j=0 ; j<3; j++ ){
        m = (j+1)%3;
        if (v[j+3] >= 0.0)  {
          l[j+0]=(int)(v[j+3]/v[j]+0.498);
          l[j+3]=(int)(v[j+3]/v[m]+0.498);
        } else {
          l[j+0]=(int)(v[j+3]/v[j]-0.498);
          l[j+3]=(int)(v[j+3]/v[m]-0.498);
        }
      }

      l[6] = 0;

      for( j=0 ; j<6 ; j++) {
        kk = l[j];
        if (kk < 0)  {
          kk = -kk;
        }
        if ((kk) > l[6])  {
          l[6] = (kk);
          k = j+1;
        }
      }

      if(l[6] == 0)
        return;

      if( k>=4 ){
        for( j=0 ; j<3 ; j++ ){
          m = (k-3)%3;
          ab[(k-4)+j*3]=ab[(k-4)+j*3]-l[k-1]*ab[m+j*3];
        }
      }else{
        for( j=0 ; j<3 ; j++ ){
          m = k%3;
          ab[m+j*3]=ab[m+j*3]-l[k-1]*ab[k-1+j*3];
        }
      }
    }
  }

  /**
   * This method does mutate both parameters
   */
  public double mi(double [] ad, double [] aid){
    int i=0,j=0,m=0,n=0,l=0,k=0;
    double [] ai= new double[(3) * (3)];
    double [] a= new double[(3) * (3)];
    double d=0.0f;

    // copy the input arrays
    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        a[i+j*3]  = ad[i+j*3];
        ai[i+j*3] = aid[i+j*3];
      }
    }

    for( j=0 ; j<3 ; j++ ){
      m = (j+1)%3;
      n = (j+2)%3;
      for( i=0 ; i<3 ; i++ ){
        k = (i+1)%3;
        l = (i+2)%3;
        ai[i+j*3]=a[m+k*3]*a[n+l*3]-a[m+l*3]*a[n+k*3];
      }
    }

    // d is the product of the first column of a and ai
    d = ai[0+0*3]*a[0+0*3]+ai[1+0*3]*a[0+1*3]+a[0+2*3]*ai[2+0*3];
    if (d == 0.0e0)  
      return d;

    // normalize ai with d
    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        ai[i +j*3] = ai[i+j*3]/d;
      }
    }

    // copy back the results
    for( i=0 ; i<3; i++ ){
      for( j=0 ; j<3; j++ ){
        ad[i+j*3] = a[i+j*3];
        aid[i+j*3] = ai[i+j*3];
      }
    }

    return d;
  }

  /**
   *
   */
  public void bias (int lmt, double [] xx, double [] yy, double [] zz,
                    double [] b, double mw, double den, doubleW dd,
                    double dj, intW mj, int [] seq, int expnum, int hstnum){
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

    if(DEBUG) System.out.println("Got into BIAS");
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

      d=mi(b,a);

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
      printB("Aft LCL"+lmt+","+dd.val+","+iz,hh,1);
      if (Math.abs(dd.val-0.100) < 0.00001)  
        dd.val = 0.100;

      if (dd.val == 0.100)  {
        dd.val = -0.010;
        Goto = 1;
      }
      if (Goto == 0)  {
        printB("before Aaio,B=",b);
        aaio(xx,yy,zz,b,hh,lmt);

        printB("bef Aair,B=",b);
        aair(b,a);

        printB("before Thh,B=",b);
        booleanW BW= new booleanW(mm);
        thh(hh,xx,yy,zz,a,jh,BW,dd.val,lmt);
        mm= BW.val;

        printB("aft Thh,B=",b);
        if (mw > 0.0 && mm)  
          Goto = 1;
        if (mj.val < 0 && Goto == 0)  {

          d=mi(b,a);
          if(DEBUG) System.out.println("d="+d);
          if (dj+0.1 > 1.0/Math.abs(d) && dj-0.1 < 1.0/Math.abs(d))  
            Goto = 1;
        }
      }
    }//while Goto==1
    System.out.print("******************\n\n" );
    System.out.print(" ERROR LIMIT="+format(dd.val,2)+"\n\n");
    System.out.print(" REDUCED CELL\n\n");
    logBuffer.append("******************\n\n");
    logBuffer.append(" ERROR LIMIT="+format(dd.val,2)+"\n\n");
    logBuffer.append(" REDUCED CELL\n\n");
    lst(hh,xx,yy,zz,a,jh,mw,b,d,lmt,seq,den,expnum,hstnum);
    // 
    mj.val = -iz;
    return;
  }

  /**
   *
   */
  public void lcl (double [] fh, int lmt, doubleW dd, intW iz)  {
    int _fh_offset=0;
    int ni = 512;
    int [] hh= new int[(3) * (lmt)];
    int [] la= new int[(lmt)];
    int d= 0,s1= 0,s2= 0,s3= 0;
    int [] ll= new int[(3) * (lmt)];
    int ha= 0,hb= 0,hc= 0,da= 0,Goto=0;
    boolean trace=false;
    int i= 0,j= 0,m1= 0,m2= 0,m3= 0,jdum= 0,idum= 0;
    int n= 0,kka= 0,lb= 0,kkdum= 0,m= 0,k= 0,l= 0,mm= 0;
    int idumc= 0,iduma= 0,kk= 0,idumb= 0;

    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<lmt ; j++ ){
        hh[i+j*3] = (int)(fh[i+j*3+ _fh_offset]*ni);
      }
    }

    ha = hh[0+(lmt-1)*3];
    hb = hh[1+(lmt-1)*3];
    hc = hh[2+(lmt-1)*3];

    while( 3==3){
      if (Goto == 0 || Goto == 2) 
        Goto = 0;

      dd.val = dd.val+0.020;

      if (dd.val > 0.30)  {
        for( i=0 ; i<lmt ; i++ ){
          if(DEBUG)
            System.out.println(" " + (fh[0+i*3+ _fh_offset]));
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
            }
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
                  if( !equ(s1+s2+s3,LB,da) ){
                    lb=LB.val;
    
                    la[0] = k;
                    la[1] = l;
                    la[2] = m;
                    n = 2;
                    la[lmt- 1] = lb;

                    Goto = 30;
                  } 
                  else lb=LB.val;
                }
                if (Goto == 0 || Goto == 20)  {
                  Goto = 0;
                  if (l == 0 && Goto == 0)  
                    Goto = 21;
                  intW LB= new intW(lb);
                  if (Goto == 0 && ! equ(s1-s2+s3,LB,da) )  {
                    lb=LB.val;
    
                    la[0] = k;
                    la[1] = -l;
                    la[2] = m;
                    n = 3;
                    la[(lmt)- 1] = lb;

                    Goto = 30;
                  }
                }

                if (Goto == 0 || Goto == 21)  {
                  Goto = 0;
                  if (k == 0 && Goto == 0)  
                    Goto = 803;
                  if (m != 0 && Goto == 0)  {
     
                    intW LB= new intW(lb);
                    if( !equ(s1+s2-s3,LB,da) ){
                      lb=LB.val;
                      la[0] = k;
                      la[1] = l;
                      la[2] = -m;
                      n = 4;
                      la[lmt-1] = lb;

                      Goto = 30;
                    }
                  }
                }

                if (Goto == 0 || Goto == 17)  {
                  Goto = 0;
    
                  n = 5;
   
                  if (l == 0 || m == 0)  
                    Goto = 19;
                  intW LB= new intW(lb);
  
                  if( equ(s1-s2-s3,LB,da) ){
                    Goto = 19;
                    lb=LB.val;
                  }

                  if (Goto == 0)  {
                    la[0] = k;
                    la[1] = -l;
                    la[2] = -m;
                    n = 5;
                    la[lmt-1] = lb;
                  }
                }
                if (Goto == 0 || Goto == 30)  {
                  Goto = 0;
 
                  jdum = lmt-1;

                  for (idum = 4; idum <= jdum; idum++) {
                    if (Goto == 0)  {
                      j = 3+lmt-idum;
                      kkdum = la[0]*hh[0+(j-1)*3]
                        +la[1]*hh[1+(j-1)*3]+la[2]*hh[2+(j-1)*3];
                      intW LB= new intW(lb);

                      if( equ(kkdum,LB,da) ){
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
                  if( kka-2!=0 ){
                    Goto = 52;
                  }else{
                    if (m1 != 0 || m2 != 0 || m3 != 0)  {
                      Goto = 19;
                    }else{
                      kka = 1;
                    }
                    Goto = 19;
                  }
                }
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
              }
            }
          }
        }
      }
    }//while 3==3 gotot 2.go_to("Lcl",2);
  }


  public boolean equ (int s, intW lb, int da)  {
    int ni = 512;
    int nj = 256;

    if( s>=0 )
      lb.val = (s+nj)/ni;
    else
      lb.val = (s-nj)/ni;

    return (Math.abs(s-lb.val*ni)>=da);
  }

  public void aair (double [] b, double [] a )  {
    int _b_offset = 0;
    int _a_offset = 0;

    double [] ab= new double[(3) * (3)];
    double [] v= new double[(6)];
    double d= 0.0;

    int [] l= new int[(7)];
    int i= 0;
    int j= 0;
    int Goto= 0;
    int k= 0;
    int idum= 0;

    double [] w = {1.0E9, 1.0E9, 1.0E9, 1.0E9};
    d=mi(b,ab);

    aarr(ab,v,l);
    if(DEBUG) System.out.println("aft aarr in aair b,ab,v");
    printB(null,b);
    printB(null,ab);
    printB(null,v);
    printB(null,l);

    for( i=0,j=0 ; i<3 ; i++ ){
      while(true){
        if(v[i]<w[j]){
          for( idum=j ; idum<3 ; idum++ ){
            k=j+3-idum;
            w[k]=w[k-1];
            l[k]=l[k-1];
          }
          w[j] = v[i];
          l[j] = i+1;
          break;
        }else{
          j++;
          if( j>2) break;
        }
      }
    }

    if(DEBUG){
      System.out.println("Aair,l="+l[0]+","+l[1]+","+l[2]+","+l[3]+","+l[4]+","
                         +l[5]+","+l[6]+","+a.length+","+ab.length);
      System.out.println("W="+format(w[0],4)+","+format(w[1],4)+","
                         +format(w[2],4)+","+format(w[3],4));
    }
    for( i=0 ; i<3 ; i++ ){
      a[2+i*3+ _a_offset] = ab[l[0]-1+i*3];
      a[0+i*3+ _a_offset] = ab[l[1]-1+i*3];
      a[1+i*3+ _a_offset] = ab[l[2]-1+i*3];
    }
    printB("aft 3, A=",a);

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

    d=mi(a,b);

    if (d >= 0.0)  
      return;
    if(DEBUG) System.out.println("D, neg entries "+d);

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
  public void aaio (double [] xx, double [] yy, double [] zz, 
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

    d=mi(b,ai);

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
  public void thh (double[] hh, double[] xx, double[] yy, double[] zz, 
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
   * Create the majority of the log file contents.
   */
  public void lst( double[] HH, double[] XX,double[] YY, double[] ZZ,
                   double[] A, int[] JH, double MW, double[] B,double D,
                   int LMT,int[] SEQ, double DEN, int EXPNUM, int HSTNUM){
    double d=0;
    double[] AI = new double[9];
    d=mi(B,AI);
    abc[6] = 1.0/d;
    System.out.print(" CELL VOLUME=  "+format(abc[6],1)+"\n\n");
    logBuffer.append(" CELL VOLUME=  "+format(abc[6],1)+"\n\n");
    printB("in aais,B,AI=",B,5);
    printB("in aais,B,AI=",AI,3);

    // calculate the cell scalars
    double A2,B2,C2,DAB,DAC,DBC;
    A2=AI[-3+3*1]*AI[-3+3*1]+AI[-3+3*2]*AI[-3+3*2]+AI[-3+3*3]*AI[-3+3*3];
    B2=AI[-2+3*1]*AI[-2+3*1]+AI[-2+3*2]*AI[-2+3*2]+AI[-2+3*3]*AI[-2+3*3];
    C2=AI[-1+3*1]*AI[-1+3*1]+AI[-1+3*2]*AI[-1+3*2]+AI[-1+3*3]*AI[-1+3*3];

    DAB= AI[-3+3*1]*AI[-2+3*1]+AI[-3+3*2]*AI[-2+3*2]+AI[-3+3*3]*AI[-2+3*3];
    DAC= AI[-3+3*1]*AI[-1+3*1]+AI[-3+3*2]*AI[-1+3*2]+AI[-3+3*3]*AI[-1+3*3] ;
    DBC= AI[-2+3*1]*AI[-1+3*1]+AI[-2+3*2]*AI[-1+3*2]+AI[-2+3*3]*AI[-1+3*3];
     
    System.out.print("*** CELL SCALARS ***\n");
    System.out.print(format(A2,9,2)+format(B2,9,2)+format(C2,9,2)+"\n");
    System.out.print(format(DBC,9,2)+format(DAC,9,2)+format(DAB,9,2)+"\n\n");
    logBuffer.append("*** CELL SCALARS ***\n");
    logBuffer.append(format(A2,9,2)+format(B2,8,2)+format(C2,8,2)+"\n");
    logBuffer.append(format(DBC,9,2)+format(DAC,8,2)+format(DAB,8,2)+"\n\n");


    // calculate the lattice parameters
    abc[0]=Math.sqrt(A2);
    abc[1]=Math.sqrt(B2);
    abc[2]=Math.sqrt(C2);
    abc[3]=DBC/(abc[1]*abc[2]);
    abc[4]=DAC/(abc[0]*abc[2]);
    abc[5]=DAB/(abc[0]*abc[1]);
    abc[3]=57.296*Math.atan(Math.sqrt(1.-abc[3]*abc[3])/abc[3]);
    if (abc[3] < 0) abc[3]=abc[3]+180.;
    abc[4]=57.296*Math.atan(Math.sqrt(1.-abc[4]*abc[4])/abc[4]);
    if (abc[4]< 0) abc[4]=abc[4]+180.;
    abc[5]=57.296*Math.atan(Math.sqrt(1.-abc[5]*abc[5])/abc[5]);
    if (abc[5] < 0) abc[5]=abc[5]+180.;
    System.out.println("A="+format(abc[0],8,3)+"   B="+format(abc[1],8,3)
                     +"   C="+format(abc[2],8,3));
    System.out.println("ALPHA="+format(abc[3],7,2)
                       +"   BETA="+format(abc[4],7,2)
                       +"   GAMMA="+format(abc[5],7,2));
    logBuffer.append("A="+format(abc[0],8,3)+"   B="+format(abc[1],8,3)
                     +"   C="+format(abc[2],8,3)+"\n");
    logBuffer.append("ALPHA="+format(abc[3],7,2)+"   BETA="+format(abc[4],7,2)
                     +"   GAMMA="+format(abc[5],7,2)+"\n\n");
    System.out.println("");

    System.out.print("     #   SEQ     H     K     L\n");
    logBuffer.append("     #   SEQ     H     K     L\n");
    for(int  i=0;i<SEQ.length; i++){
      System.out.print(format(i+1,6,0)+format(SEQ[i],6,0)
                       +format(JH[(i+3)*3+0],6,0)+format(JH[(i+3)*3+1],6,0)
                       +format(JH[(i+3)*3+2],6,0)+"\n");
      logBuffer.append(format(i+1,6,0)+format(SEQ[i],6,0)
                       +format(JH[(i+3)*3+0],6,0)+format(JH[(i+3)*3+1],6,0)
                       +format(JH[(i+3)*3+2],6,0)+"\n");

    }
    System.out.println("");
    logBuffer.append("\n");
     
    u = new double[9];
    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        u[i-3+3*(j+1)]=B[j-3+3*(i+1)];
      }
    }
 
    double vol=subs.tstvol(u);

    if( vol < 0) 
      if(DEBUG) System.out.println("Left handed system");

    System.out.print("ORIENTATION MATRIX\n");
    for(int i=0 ; i<3 ; i++ )
      System.out.println(format(u[i-3+3*1],10,5)+format(u[i-3+3*2],10,5)
                         +format(u[i-3+3*3],10,5));
  }
   
  /**
   * Write out the log to the specified file.
   */
  public boolean writeLog(String filename){
    FileOutputStream fout=null;
    StringBuffer sb=new StringBuffer(logBuffer.toString());

    // create the matrix file
    sb.append("ORIENTATION MATRIX\n");
    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ )
        sb.append(format(u[i+j*3],9,5)+"  ");
      sb.append("\n");
    }
    

    // write out the information
    try{
      fout=new FileOutputStream(filename);
      fout.write(sb.toString().getBytes());
      fout.flush();
    }catch(IOException e){
      return false;
    }finally{
      if(fout!=null){
        try{
          fout.close();
        }catch(IOException e){
          // let it drop on the floor
        }
      }
    }

    return true;
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
        LoadSCDCalib lcab= new LoadSCDCalib( ds,
                                    "C:\\Ipns\\Isaw2\\SampleRuns\\instprm.dat",
                                             1,"0:7000");
        lcab.getResult(); 
        FindPeaks fp = new FindPeaks( ds,0, 15,1);
        V = (Vector)(fp.getResult());
        CentroidPeaks cp = new CentroidPeaks(ds, V);

        V = (Vector)(cp.getResult());

        for(int i=0;i<V.size();i++){
          Peak pk=(Peak)(V.elementAt(i));
          System.out.println("Pk i,xcm,ycm,wl="+pk.xcm()+","+pk.ycm()+","
                             +pk.wl());
          System.out.println("    chi,phi,om,deta,detd="+pk.chi()+","+pk.phi()
                             +","+pk.omega()+","+pk.detA()+","+pk.detD());
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
              deta= (float)(fin.read_float() );
              detd= (float) (fin.read_float());
              detd= (float)(fin.read_float());
              chi= (float)(fin.read_float());
              phi=(float)( fin.read_float());
              omega=(float) (fin.read_float());

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
           
                  dat[5] = fin.read_float();
                  dat[6] = fin.read_float();
                  dat[7] = fin.read_float();
                  dat[0] = chi;
                  dat[1] = phi;
                  dat[2] = omega;
                  dat[3] = deta;
                  dat[4]  = detd;
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
      System.exit(-1);
    if(V.size()<1)
      System.exit(-1);
    double[] xx,yy,zz;
    xx = new double[V.size()+3];
    yy = new double[V.size()+3];
    zz = new double[V.size()+3];
    intW LMT = new intW(0);
     
    blind BLIND=new blind();

    BLIND.blaue( V,xx,yy,zz,LMT,seq);
    double[] b= new double[9];
    doubleW dd= new doubleW(.08);
    intW mj= new intW(0);
    
    BLIND.bias(V.size()+3,xx,yy,zz,b,0,3,dd,4.0,mj,seq,123,0);
    System.out.println("Orientation matrix=");
    for( int i=0;i<3;i++){
      for (int j=0;j<3;j++)
        System.out.print(BLIND.u[3*j+i]+" ");
      System.out.println("");
    }

    for( int i=0 ; i<BLIND.abc.length ; i++ )
      System.out.print(BLIND.abc[i]+" ");
    System.out.println();
  }

  private static void printB(String label, Object b){
    printB(label,b,4);
  }

  private static void printB(String label, Object b,int dec){
    if(!DEBUG) return;
    if( (!(b instanceof double[])) && (!(b instanceof int[])) )
      return;

    if(label!=null && label.length()>0)
      System.out.println(label);

    int length=0;
    if(b instanceof double[]){
      length=((double[])b).length;
      for( int i=0 ; i<length ; i++ ){
        System.out.print(format(((double[])b)[i],dec));
        if(i<length-1) System.out.print(",");

      }
    }else if(b instanceof int[]){
      length=((int[])b).length;
      for( int i=0 ; i<length ; i++ ){
        System.out.print(format(((int[])b)[i],0));
        if(i<length-1) System.out.print(",");
      }
    }else{
      return;
    }
    System.out.println("");
  }

  private static String format(double num,int dec){
    DecimalFormat df=new DecimalFormat("0.00000");
    df.setMinimumFractionDigits(dec);
    df.setMaximumFractionDigits(dec);
    if(num<0)
      return df.format(num);
    else
      return " "+df.format(num);
  }

  private static String format(double num,int total, int dec){
    DecimalFormat df=new DecimalFormat("0.00000");
    df.setMinimumFractionDigits(dec);
    df.setMaximumFractionDigits(dec);

    StringBuffer sb=new StringBuffer(df.format(num));
    while(sb.length()<total)
      sb.insert(0," ");

    return sb.toString();
  }
}
