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
 * Revision 1.17  2003/10/14 22:14:59  dennis
 * Fixed javadoc comment so that it builds cleanly on jdk 1.4.2
 *
 * Revision 1.16  2003/06/26 14:56:17  dennis
 * Fixed syntax error.
 *
 * Revision 1.15  2003/06/26 14:02:55  dennis
 * Trapped array index out of bounds problem in line 985,
 * when producing log file.  This is not a permanent fix
 * to the array index problem, but temporarily allows blind
 * to be used without crashing on some sequences of peaks.
 *
 * Revision 1.14  2003/06/20 16:04:55  dennis
 * Increased size of array jh[] to fix array index out of bounds
 * problem when certain peaks were used.
 *
 * Revision 1.13  2003/05/14 20:17:35  pfpeterson
 * Improved readability and encapsulation. No longer uses wrappers for int
 * or double, log file better matches FORTRAN counterpart, simplified loop
 * constructs when counter should decrease.
 *
 * Revision 1.12  2003/05/13 20:16:51  pfpeterson
 * Code cleanup. This includes changing variable and method names
 * related to blaue.
 *
 * Revision 1.11  2003/05/12 19:23:35  pfpeterson
 * Removed code that is no longer used.
 *
 * Revision 1.10  2003/05/09 18:58:36  pfpeterson
 * Improved log creation for easier code matenience.
 *
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
import DataSetTools.math.LinearAlgebra;
import DataSetTools.util.*;
import DataSetTools.operator.DataSet.Attribute.*;
import java.text.DecimalFormat;
import java.io.*;

public class blind {
  private static final boolean DEBUG=false;

  public  double[][]   UB           = null;
  private String       errormessage = "";
  private StringBuffer logBuffer    = null;

  public blind(){
    UB=null;
    errormessage="";

    // create the logfile contents
    logBuffer=new StringBuffer(50*19);
    int start=logBuffer.length();
    logBuffer.append("\n  *******LAUE INDEXER*******\n\n");
    System.out.print(logBuffer.substring(start));
  }

  /**
   * Finds the basis of the smallest non coplanar in Qx,Qy,Qz peaks.
   * The basis is manipulated so that B*Transpose(B) is "about" a
   * diagonal
   */ 
  public ErrorString blaue (Vector peaks, double[] xx, double[] yy,
                            double[] zz, int[] seq) {
    float[] angle= new float[ xx.length*3];
    int length=peaks.size();

    if(DEBUG) System.out.println("peaks size="+peaks.size());

    // set up the Q-vector arrays (xx,yy,zz)
    for( int i=0;i<length;i++){
        // copy information about the peak into local variables
        Peak peak=(Peak)peaks.elementAt(i);
        angle[i+0]= peak.xcm();
        angle[i+1*xx.length]=peak.ycm();
        angle[i+2*xx.length]=peak.wl();
        seq[i]= (int)peak.seqnum();
        if( peak.wl() <= .00001) continue;

        //Calculate the Qx,Qy,Qz value for this peak
        double[] Qvec=peak.getUnrotQ();
        String SS="";
        xx[i]=Qvec[0];
        yy[i]=Qvec[1];
        zz[i]=Qvec[2];
        // print out some debug info
        if(DEBUG)
          System.out.println("Q vals="+format(Qvec[0],10,4)+","
                             +format(Qvec[1],10,4)+","+format(Qvec[2],10,4));
        if(Double.isNaN(xx[i])) SS=SS+"xx";
        if(Double.isNaN(yy[i])) SS=SS+"yy";
        if(Double.isNaN(zz[i])) SS=SS+"zz";
        if( DEBUG && SS.length()>0 ) System.out.println("Laue "+ i+"::"+SS);
    }

    // print information about the peaks we are using
    int start=logBuffer.length();
    logBuffer.append("    #  SEQ       XCM       YCM      WL\n");
    for( int j=0 ; j<length ; j++ ){
      logBuffer.append(format(j+1,5,0)+format(seq[j],5,0));
      for( int k=0 ; k<2 ; k++ )
        logBuffer.append(format(angle[j+k*xx.length],10,3));
      logBuffer.append(format(angle[j+2*xx.length],10,4));
      logBuffer.append(format(xx[j],10,4));
      logBuffer.append(format(yy[j],10,4));
      logBuffer.append(format(zz[j],10,4));
      logBuffer.append("\n");
    }
    logBuffer.append("\n");
    System.out.print(logBuffer.substring(start));

    // Manipulates the basis(the first 3 elements of xx,yy,and zz) so
    // B*Tranps(B) about diagonal
    abid(xx,yy,zz);

    // return the error message if necessary
    if( errormessage.length()>0)
      return new ErrorString(errormessage);
    else
      return null;
  }

  /**
   * Creates and returns an array that indexes the arrays from
   * shortest (0th) to longest (nth).
   */
  private int[] sortQ(double[] Qx, double[] Qy, double[] Qz){
    int      length  = Qx.length-3; // -3 b/c a couple are added for
                                    // new basis vectors
    // indexing arrays where 0th is the shortest
    int[]    order   = new int[length+1];
    double[] Qsq     = new double[length+1];
    double   thisQsq = 0.;

    // initialize the lengths of the Q
    for( int i=0 ; i<length ; i++ )
      Qsq[i] =1.0E9;
    
    //Index sort of w, the magnitude of the the peaks, with index l 
    for( int i=0 ; i<length ; i++ ){
      // determine the magnitude of the Q-vector
      thisQsq = Qx[i]*Qx[i] + Qy[i]*Qy[i] + Qz[i]*Qz[i]; // Q dot Q
      if( DEBUG && thisQsq>=1.0E9)
        System.out.println("abid Qsq value  is very large");
      if( Double.isNaN(thisQsq)) thisQsq = .9E9;
      
      for( int j=0 ; j<length ; j++ ){
        if( thisQsq<Qsq[j] ){ // if Qsq is shorter than jth vector
          for( int k=order.length-1 ; k>j ; k-- ){
            Qsq[k]   = Qsq[k-1];
            order[k] = order[k-1];
          }
          Qsq[j] = thisQsq;
          order[j]   = i+1;
          break;
        }
      }
    }

    return order;
  }


  /**
   * Sorts the peak vectors according to magnitude, then picks the
   * first 3 that are not coplanar.  Then aair manipulates these Basis
   * so that B*Transp(B) about diagonal.  The Basis vectors are stored
   * in the first three positions of XX,YY, and ZZ. The other vectors
   * are all moved up d
   */
  private void abid (double[] Qx, double[] Qy, double[] Qz ) {
    int length=Qx.length-3; // -3 b/c a couple are added for new basis vectors
    double d=0.0;
    double[][] UB=new double[3][3]; // the ub matrix
    double [] ya= new double[length];
    double [] za= new double[length];
    double [] xa= new double[length];
    int [] l=sortQ(Qx,Qy,Qz);

    // initialize the sorted vector arrays
    for( int j=0 ; j<length ; j++ ){ 
      xa[j] = Qx[l[j]-1];
      ya[j] = Qy[l[j]-1];
      za[j] = Qz[l[j]-1];
    }

    //While the proposed basis vectors are coplanar
    int k = 3;
    while(true){
      k++;

      if (k > length)  {
        int start=logBuffer.length();
        logBuffer.append(" ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING\n");
        System.out.print(logBuffer.substring(start));
        errormessage="ALL REFLECTIONS COPLANAR";

        if(DEBUG){
          for( int i=0 ; i<3 ; i++ ){
            System.out.println(" "+xa[i]+"    "+ya[i]+"    "+za[i]);
            System.out.println(" D="+d);
          }
        }
        return;
      }
      for( int i=0 ; i<3 ; i++ ){
        UB[i][0]=xa[i];
        UB[i][1]=ya[i];
        UB[i][2]=za[i];
      }

      d=LinearAlgebra.determinant(UB);
      if (Math.abs(d) >= 0.0001) break;

      // d is the determinant of the basis vectors if close to zero,
      // then basis are coplanar.  Interchange the kth peak with one
      // of the basis.
      int    tempI=0;
      double tempD=0.0;
      if (Math.abs(xa[0]*ya[1]-xa[1]*ya[0]) >= 0.05 
          || Math.abs(xa[0]*za[1]-xa[1]*za[0]) >= 0.05 
          || Math.abs(ya[0]*za[1]-ya[1]*za[0]) >= 0.05)  {
        swap(xa,k-1,2);
        swap(ya,k-1,2);
        swap(za,k-1,2);
        swap(l,k-1,2);
      } else  {
        swap(xa,k-1,1);
        swap(xa,1,2);
        swap(ya,k-1,1);
        swap(ya,1,2);
        swap(za,k-1,1);
        swap(za,1,2);
        swap(l,k-1,1);
        swap(l,1,2);
      }
    }

    //Manipulate b so that B*transpose(B) about diagonal
    aarr(UB);

    // move the Q vectors to the end
    for( int i=length-1 ; i>=0 ; i--){
      Qx[i+3] = Qx[i];
      Qy[i+3] = Qy[i];
      Qz[i+3] = Qz[i];
    }

    // copy the orientation matrix into the first elements of the Q vector
    for( int i=0 ; i<3 ; i++ ){
      Qx[i] = UB[i][0];
      Qy[i] = UB[i][1];
      Qz[i] = UB[i][2];
    }

    return;
  }

  /**
   * Swap the values of array at index1 and index2 with each other.
   */
  private void swap(double[] array, int index1, int index2){
    double temp=array[index1];
    array[index1]=array[index2];
    array[index2]=temp;
  }

  /**
   * Swap the values of array at index1 and index2 with each other.
   */
  private void swap(int[] array, int index1, int index2){
    int temp=array[index1];
    array[index1]=array[index2];
    array[index2]=temp;
  }

  /**
   * Manipulate basis ab so that ab*transpose(ab) about (integer) diagonal
   */
  private void aarr(double[][] ab){
      int[]    ll = new int[7];
      double[] vv = new double[6];
      double [] b= new double[3*3];
      for( int i=0 ; i<3 ; i++ )
        for( int j=0 ; j<3 ; j++ )
          b[i+j*3]=ab[i][j];
      aarr(b,vv,ll);
      for( int i=0 ; i<3 ; i++ )
        for( int j=0 ; j<3 ; j++ )
          ab[i][j]=b[i+j*3];
    
  }

  /**
   * Manipulate basis ab so that ab*transpose(ab) about (integer) diagonal
   */
  public void aarr (double [] ab, double [] v, int [] l)  {
    int m=0;
    int k=0;
    int kk=0;

    while(true){
      // set v to zero
      for( int i=0 ; i<v.length ; i++ )
        v[i] = 0.0;

      for( int j=0 ; j<3 ; j++ ){
        m = (j+1)%3;
        for( int i=0 ; i<3 ; i++ ){
          v[j]=v[j]+ab[j+i*3]*ab[j+i*3];
          v[j+3]=v[j+3]+ab[j+i*3]*ab[m+i*3];
        }
      }

      for( int i=0 ; i<3; i++ ){
        m = (i+1)%3;
        double adder=0.498;
        if (v[i+3] < 0.0) adder=-1.0*adder;
        l[i+0]=(int)(v[i+3]/v[i]+adder);
        l[i+3]=(int)(v[i+3]/v[m]+adder);
      }

      l[6] = 0;

      for( int i=0 ; i<6 ; i++) {
        kk = (int)Math.abs(l[i]);
        if( kk>l[6] ){
          l[6] = kk;
          k = i+1;
        }
      }

      if(l[6] == 0)
        return;

      if( k>=4 ){
        for( int i=0 ; i<3 ; i++ ){
          m = (k-3)%3;
          ab[(k-4)+i*3]=ab[(k-4)+i*3]-l[k-1]*ab[m+i*3];
        }
      }else{
        for( int i=0 ; i<3 ; i++ ){
          m = k%3;
          ab[m+i*3]=ab[m+i*3]-l[k-1]*ab[k-1+i*3];
        }
      }
    }
  }

  /**
   * Determinant of ad. This method mutates the second parameter, aid.
   */
  public double mi(double [] ad, double [] aid){
    double [] ai= new double[3*3];
    double d=0.0f;

    // copy the input arrays
    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        ai[i+j*3] = aid[i+j*3];
      }
    }

    for( int j=0 ; j<3 ; j++ ){
      int m = (j+1)%3;
      int n = (j+2)%3;
      for( int i=0 ; i<3 ; i++ ){
        int k = (i+1)%3;
        int l = (i+2)%3;
        ai[i+j*3]=ad[m+k*3]*ad[n+l*3]-ad[m+l*3]*ad[n+k*3];
      }
    }

    // d is the product of the first column of a and ai
    d = ai[0+0*3]*ad[0+0*3]+ai[1+0*3]*ad[0+1*3]+ad[0+2*3]*ai[2+0*3];
    if (d == 0.0e0)  
      return d;

    // normalize ai with d and copy back the results
    for( int i=0 ; i<3; i++ )
      for( int j=0 ; j<3; j++ )
        aid[i+j*3] = ai[i+j*3]/d;

    return d;
  }

  /**
   *
   */
  public ErrorString bias (int lmt, double[] xx, double[] yy, double[] zz,
                           int[] seq){
    double dd=.08;
    double[] b=new double[9];
    double [] hh= new double[3 * lmt];
    double [] a = new double[3 * 3];
    double d= 0.0;

    int [] jh= new int[3 * lmt + 3];
    boolean Goto=true;

    if(DEBUG) System.out.println("Got into BIAS");

    while( Goto){
      Goto=false;
      for( int j=0 ; j<3 ; j++ ){
        b[0+j*3] = xx[j];
        b[1+j*3] = yy[j];
        b[2+j*3] = zz[j];
      }

      d=mi(b,a);

      for( int j=0 ; j<3 ; j++ )
        for( int i=0 ; i<lmt ; i++ )
          hh[j+i*3] = a[j+0*3]*xx[i]+a[j+1*3]*yy[i]+a[j+2*3]*zz[i];

      dd=lcl(hh,lmt,dd);
      if( errormessage.length()>0)
        return new ErrorString(errormessage);

      printB("Aft LCL"+lmt+","+dd,hh,1);
      if (Math.abs(dd-0.100) < 0.00001)  
        dd = 0.100;

      if (dd == 0.100)  {
        dd = -0.010;
        Goto = true;
      }
      if(!Goto){
        printB("before Aaio,B=",b);
        aaio(xx,yy,zz,b,hh,lmt);

        printB("bef Aair,B=",b);
        aair(b,a);

        printB("before Thh,B=",b);
        thh(hh,xx,yy,zz,a,jh,dd,lmt);

        printB("aft Thh,B=",b);
      }
    }
    int start=logBuffer.length();
    logBuffer.append("******************\n\n");
    logBuffer.append(" ERROR LIMIT="+format(dd,9,2).trim()+"\n\n");
    logBuffer.append(" REDUCED CELL\n\n");
    System.out.print(logBuffer.substring(start));
    lst(jh,b,seq);

    return null;
  }

  /**
   *
   */
  public double lcl(double [] fh, int lmt, double dd){
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

      dd = dd+0.020;

      if (dd > 0.30)  {
        for( i=0 ; i<lmt ; i++ ){
          if(DEBUG)
            System.out.println(" " + (fh[0+i*3+ _fh_offset]));
          errormessage ="INITIAL NON-INTEGER INDICES ";
          return dd;
        }
      }

      da = (int)(dd*ni);
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
  
                  lb=pre_equ(s1+s2+s3);
                  if( !equ(s1+s2+s3,lb,da) ){
    
                    la[0] = k;
                    la[1] = l;
                    la[2] = m;
                    n = 2;
                    la[lmt- 1] = lb;

                    Goto = 30;
                  } 
                }
                if (Goto == 0 || Goto == 20)  {
                  Goto = 0;
                  if (l == 0 && Goto == 0)  
                    Goto = 21;
                  int innerlb=pre_equ(s1-s2+s3);
                  if (Goto == 0 && ! equ(s1-s2+s3,innerlb,da) )  {
                    lb=innerlb;
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
     
                    int innerlb=pre_equ(s1+s2-s3);
                    if( !equ(s1+s2-s3,innerlb,da) ){
                      lb=innerlb;
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

                  int innerlb=pre_equ(s1-s2-s3);
                  if( equ(s1-s2-s3,innerlb,da) ){
                    lb=innerlb;
                    Goto = 19;
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

                      lb=pre_equ(kkdum);
                      if( equ(kkdum,lb,da) )
                        Goto = 19;
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

                    return dd;
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

  private int pre_equ(int s){
    int ni = 512;
    int nj = 256;

    if( s>=0 )
      return (s+nj)/ni;
    else
      return (s-nj)/ni;
  }

  /**
   * must run pre_equ first to set value of lb
   */
  public boolean equ(int s, int lb, int da){
    return (Math.abs(s-lb*512)>=da);
  }

  public void aair (double [] b, double [] a )  {
    double [] ab= new double[3*3];
    double [] v= new double[6];
    double d= 0.0;

    int [] l= new int[(7)];
    int i= 0;
    int j= 0;
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
      System.out.println("W="+format(w[0],10,4)+","+format(w[1],10,4)+","
                         +format(w[2],10,4)+","+format(w[3],10,4));
    }
    for( i=0 ; i<3 ; i++ ){
      a[2+i*3] = ab[l[0]-1+i*3];
      a[0+i*3] = ab[l[1]-1+i*3];
      a[1+i*3] = ab[l[2]-1+i*3];
    }
    printB("aft 3, A=",a);

    w[3] = v[4];
    v[4] = v[5];
    v[5] = w[3];
    if (v[l[0]+l[1]] > 0.0)  {
      for( i=0 ; i<3 ; i++ ){
        a[0+i*3] = -a[0+i*3];
        v[l[0]+l[1]] = -v[l[0]+l[1]];
        v[l[1]+l[2]] = -v[l[1]+l[2]];
      }
    }

    if (v[l[2]+l[0]] > 0.0)  {
        for( i=0 ; i<3 ; i++ ){
          a[1+i*3] = -a[1+i*3];
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
        a[i+j*3] = -a[i+j*3];
        b[i+j*3] = -b[i+j*3];
      }
    }
  }

  /**
   *
   */
  public void aaio (double [] xx, double [] yy, double [] zz, 
                           double [] b, double [] hh, int lmt)  {
    double [] a= new double[(3) * (3)];
    double [] ai= new double[(3) * (3)];
    double d= 0.0;

    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        a[i+j*3] = 0.0;
        b[i+j*3] = 0.0;
        for( int k=0 ; k<lmt ; k++ ){
          b[i+j*3]=b[i+j*3]+hh[i+k*3]*hh[j+k*3];
        }
      }
    }

    for( int i=0 ; i<3 ; i++ ){
      for( int k=0 ; k<lmt ; k++ ){
        a[0+i*3]=a[0+i*3]+xx[k]*hh[i+k*3];
        a[1+i*3]=a[1+i*3]+yy[k]*hh[i+k*3];
        a[2+i*3]=a[2+i*3]+zz[k]*hh[i+k*3];
      }
    }

    d=mi(b,ai);

    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        b[i+j*3] = 0.0;
        for( int k=0 ; k<3 ; k++ ){
            b[i+j*3]=b[i+j*3]+a[i+k*3]*ai[k+j*3];
        }
      }
    }

    return;
  }

  /**
   *
   */
  public boolean thh (double[] hh, double[] xx, double[] yy, double[] zz, 
                          double [] a,  int [] jh, double dd, int lmt){
    int lb= 0;
    boolean mm=false;

    for( int i=3 ; i<lmt ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        hh[j+i*3]=a[j+0*3]*xx[i]+a[j+1*3]*yy[i]+a[j+2*3]*zz[i];

        if (hh[j+i*3] >= 0.0)
          lb = (int)(hh[j+i*3]+0.5);
        else
          lb = (int)(hh[j+i*3]-0.5);

        if (Math.abs(hh[j+i*3]-lb) > dd)
          mm = true;

        jh[j+i*3] = lb;
      }
    }

    return mm;
  }

  /**
   * Create the majority of the log file contents.
   */
  public void lst( int[] JH, double[] B, int[] SEQ){
    double[] AI = new double[9];
    double[] abc=new double[7];

    // get the orientation matrix
    UB=new double[3][3];
    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        UB[j][i]=B[j-3+3*(i+1)];
      }
    }

    // get the lattice parameters
    abc=Util.abc(UB);

    // get the cell scalars
    double[] scalars=Util.scalars(abc);

    // print the volume and scalars
    int start=logBuffer.length();
    logBuffer.append(" CELL VOLUME=  "+format(abc[6],6,1)+"\n\n");
    logBuffer.append("*** CELL SCALARS ***\n ");
    for( int i=0 ; i<6 ; i++ ){
      logBuffer.append(format(scalars[i],8,2));
      if(i==2) logBuffer.append("\n ");
      else if(i==5) logBuffer.append("\n");
    }
    logBuffer.append("\n");

    // print the lattice parameters
    logBuffer.append(" A="+format(abc[0],8,3)+"   B="+format(abc[1],8,3)
                     +"   C="+format(abc[2],8,3)+"\n");
    logBuffer.append(" ALPHA="+format(abc[3],7,2)+"   BETA="+format(abc[4],7,2)
                     +"   GAMMA="+format(abc[5],7,2)+"\n\n");

    // print the peak indices
    logBuffer.append("      #   SEQ     H     K     L\n");
    for(int  i=0;i<SEQ.length; i++){
      if ( (i+3)*3+2 < JH.length )
        logBuffer.append(" "+format(i+1,6,0)+format(SEQ[i],6,0)
                       +format(JH[(i+3)*3+0],6,0)+format(JH[(i+3)*3+1],6,0)
                       +format(JH[(i+3)*3+2],6,0)+"\n");
      else
      {
        System.out.println("ERROR: Index out of bounds for seq[i] = " + SEQ[i]);
        System.out.println("i = " + i + 
                           ", JH.length = " + JH.length + 
                           ", SEQ.length = " + SEQ.length );
      } 
    }
    logBuffer.append("\n");
    System.out.print(logBuffer.substring(start));
     
    // print out a warning message
    if( abc[6] < 0) 
      if(DEBUG) System.out.println("Left handed system");

    // finally the orientation matrix
    start=logBuffer.length();
    logBuffer.append(" ORIENTATION MATRIX\n");
    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        logBuffer.append(format(UB[j][i],9,5));
        if(j!=2) logBuffer.append("  ");
      }
      logBuffer.append("\n");
    }
    logBuffer.append("\n");
    System.out.print(logBuffer.substring(start));
  }
   
  /**
   * Write out the log to the specified file.
   */
  public boolean writeLog(String filename){
    FileOutputStream fout=null;
    StringBuffer sb=new StringBuffer(logBuffer.toString());

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
   * @param args An array containg two entries, the name of the run file 
   *             or peaks file and the list of sequence numbers. 
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
     
    blind BLIND=new blind();

    ErrorString error=BLIND.blaue( V,xx,yy,zz,seq);
    if(error!=null){
      System.out.println(error.toString());
      System.exit(-1);
    }
    error=BLIND.bias(V.size()+3,xx,yy,zz,seq);
    if(error!=null){
      System.out.println(error.toString());
      System.exit(-1);
    }
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
        System.out.print(format(((double[])b)[i],5+dec,dec));
        if(i<length-1) System.out.print(",");

      }
    }else if(b instanceof int[]){
      length=((int[])b).length;
      for( int i=0 ; i<length ; i++ ){
        System.out.print(Format.integer(((int[])b)[i],dec));
        if(i<length-1) System.out.print(",");
      }
    }else{
      return;
    }
    System.out.println("");
  }

  private static String format(double num,int total, int dec){
    return Format.real(num,total,dec);
  }
}
