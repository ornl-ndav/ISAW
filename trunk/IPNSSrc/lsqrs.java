/* =========================================================== *
 *  THIS IS A PRELIMINARY VERSION THAT SHOULD NOT BE RELEASED  *
 * =========================================================== */

/*
 *  Produced by f2java.  f2java is part of the Fortran-
 *  -to-Java project at the University of Tennessee Netlib
 *  numerical software repository.
 *
 *  Original authorship for the BLAS and LAPACK numerical
 *  routines may be found in the Fortran source, available at
 *  www.netlib.org.
 *
 *  Fortran input file: lsqrs.f
 *
 *  The f2j compiler code was written by
 *  David M. Doolin (doolin@cs.utk.edu) and
 *  Keith  Seymour (seymour@cs.utk.edu)
 */

package IPNSSrc;

import java.lang.*;
//import org.netlib.util.*;
import java.io.*;


public class lsqrs {

  /*
   *      Contributors:
   *            J. Marc Overhage      May 1979
   *      and
   *            G. Anderson
   *            P. C. W. Leung
   *            R. G. Teller
   *            A. J. Schultz
   *
   *      This program will calculate the best orientation matrix and
   *      cell parameters from a list of index reflections.
   *
   *      Modified by:      A. Schultz      October 1987
   *
   *      Modified by:       A. Schultz       September 1989
   *                  Modified to print observed and calculated
   *                  X,Y,Z channels rather than diffraction vectors.
   *
   *                  November 1996
   *                  SEQNUMs can be input from file SEQNUM.DAT 
   */
  // 
  //common all
  static int xnum= 0;
  static int ynum= 0;
  static int wlnum= 0;
  static double run= 0.0;
  static int i84= 0;

  //common cell
  static double [] b= new double[(7)];
  static double [] bs= new double[(7)];


  //common data1
  static double [] orgmat= new double[9];
  static int idum=0;
  static String descr="";
  static String user="";

  //common data8
  static int expnum=0;
  static int hstnum=0;

  //common data11
  static int nrun=0;
  static int nrun1=0;
  static int detnum=0;
  static String expnam="";

  //common data12
  static int seqnum= 0;
  static int ih= 0;
  static int ik= 0;
  static int il= 0;
  static double x= 0.0;
  static double y= 0.0;
  static double z= 0.0;
  static double xcm= 0.0;
  static double ycm= 0.0;
  static double wl= 0.0;
  static int ipk= 0;
  static double inti= 0.0;
  static double sigi= 0.0;
  static int reflag= 0;

  //common inspar

  static double l1= 0.0;
  static double tzero= 0.0;
  static double xboxcm= 0.0;
  static double yboxcm= 0.0;
  static double xbias= 0.0;
  static double ybias= 0.0;
  static double xleft= 0.0;
  static double xright= 0.0;
  static double ylower= 0.0;
  static double yupper= 0.0;
  static double x2cm= 0.0;
  static double y2cm= 0.0;

  //common set 

  static double chi= 0.0;
  static double phi= 0.0;
  static double omega= 0.0;
  static double deta= 0.0;
  static double detd= 0.0;
  static double deta2= 0.0;


  //common sig

  static double [] sig= new double[7];

  //common sig from datacom
  //ppp_sig

  static double siga= 0.0;
  static double sigb= 0.0;
  static double sigc= 0.0;
  static double sigalp= 0.0;
  static double sigbet= 0.0;
  static double siggam= 0.0;
  static double sigvol= 0.0;

  //common times 
  static int tmin= 0;
  static int tmax= 0;

  //common time
  static int[] ntime= new int[256];
  static int ndim= 0;
  static int ndmax= 0;
  //end commons


  static double [] a= new double[3*6];
  static double [] celsca= new double[7];
  static double [] curmat= new double[3*3];
  static double [] derv= new double[3*7];
  static double [] dv= new double[12];
  static double [] oi= new double[3*3];
  static double [] v= new double[12];
  static double [] vc= new double[3*3];
  static int [] jexp= new int[10];
  static int [] jhmin= new int[10];
  static int [] jhmax= new int[10];
  static int [] nseq= new int[1000];
  static int [] nhst= new int[100];
  static int i= 0;
  static int mincnt= 0;
  static int ncntr= 0;
  static int ip= 0;
  static int nchrs= 0;
  static int m= 0;
  static int j= 0;
  static int jj= 0;
  static int kk= 0;
  static int ii= 0;
  static int Goto= 0;
  static int nref= 0;
  static int kkk= 0;
  static int hh= 0;
  static int ip2= 0;
  static int jh= 0;
  static int kh= 0;
  static int mhst= 0;
  static int ja= 0;
  static int numh= 0;
  static int mseq= 0;
  static int jnseq= 0;
  static int ieof= 0;
  static int ifile= 0;
  static int iexp= 0;
  static int ixln= 0;
  static int line= 0;
  static int iflag= 0;
  static int mnseq= 0;
  static int isin= 0;
  static int openerr= 0;
  static int lcs= 0;
  static int iq= 0;
  static int index= 0;
  static int jnum= 0;
  static int isam= 0;
  static int iuexp= 0;
  static int maxhst= 0;
  static int minhst= 0;
  static int minseq= 0;
  static int maxseq= 0;
  static int jndex= 0;
  static int lnblnk= 0;
  static minputStream unit1;
  static PrintStream unit16= null;
  static minputStream unit15= null;
  static PrintStream unit4= null;
  static int xx= 0;
  static int xx1= 0;
  static double del= 0.0;
  static double fi= 0.0;
  static double wlmin= 0.0;
  static double delhst= 0.0;
  static double delhkl= 0.0;
  static double diffxx= 0.0;
  static double diffyy= 0.0;
  static double diffzz= 0.0;
  static double delx= 0.0;
  static double dely= 0.0;
  static double delz= 0.0;
  static double zcalc= 0.0;
  static double ycalc= 0.0;
  static double xcalc= 0.0;
  static double ang= 0.0;
  static double xdp= 0.0;
  static double ydp= 0.0;
  static double zdp= 0.0;
  static double xl= 0.0;
  static double wlmax= 0.0;
  static double tobs= 0.0;
  static double delta= 0.0;
  static double yt= 0.0;
  static double xt= 0.0;
  static double zt= 0.0;
  static double piv= 0.0;
  static double ql= 0.0;
  static double eof= 0.0;
  static double o= 0.0;
  static double readexp= 0.0;
  // 
  // c      BYTE NAM(17)
  // 
  static double h= 0.0;
  static double k= 0.0;
  static double l= 0.0;
  // 
  static int gentxt= 0;
  static int gentex= 0;
  static int curhst= 0;
  // 
  static String brefl= new String("                                        ");
  static String fname= new String("                                        ");
  static String yn= new String(" ");
  static String ans= new String(" ");
  static String fit= new String("       ");
  static String prompt= new String("                                          "
                                   +"                                      ");
  static String answr= new String("                                           "
                                  +"                 ");
  static String text= new String("                                            "
                                 +"                        ");
  static String key76= new String("            ");
  static String twoa= new String("    ");
  static String astar= new String("    ");
  // 
  static String yes = new String("Y");

  // c      EQUIVALENCE (RA,B(1)),(RB,B(2)),(RC,B(3)),(RALPHA,B(4)),
  // c     B (RBETA,B(5)),(RGAMMA,B(6)),(RVOL,B(7))
  // 
  // C     DATA AND DEFAULT VALUES
  static double rad = 57.29577951;
  static double small = 1.525878906E-5;
  // c      DATA ANS/'    '/
  static String star = new String("----");
  static String blank = new String("    ");
  static String onea = new String("*   ");
  static String threea = new String("**  ");
  static double hom = 0.39559974;
  // 
  // c      rvrs=char(27)//'[7m'
  // c      off=char(27)//'[0m'
  // 

  public static void main (String [] args)  {

    doubleW Diffxx = new doubleW(0),
      Diffyy=new doubleW(0),Diffzz=new doubleW(0);
    doubleW A= new doubleW(0),B= new doubleW(0),C= new doubleW(0);

    doubleW Alpha=new doubleW(0),
      Beta= new doubleW(0),Gamma= new doubleW(0),Vol= new doubleW(0);
    intW Ih,Ik,Il;

    for( int i=0 ; i<3 ; i++ ){
      for( int j=0 ; j<3 ; j++ ){
        curmat[i+j*3] = 0.0;
        orgmat[i+j*3] = 0.0;
      }
    }
    minseq = 1;
    maxseq = 9999;
    minhst = 0;
    maxhst = 999;
    hstnum = 1;
    i = 0;
    // C
    // C     GET INITIAL VALUES FROM USER AND SYSTEM
    // C
    System.out.println(" Experiment name (expnam)? " );
    expnam=readans();

    System.out.println(" \n Is input on a\n"
                       + "       (1) PEAKS file?\n"
                       + "    a  (2) PEAKINT output file?\n"
                       + "    a  (3) INTSCD output file?\n"
                       + " or a  (4) INTEGRATE output file?\n"
                       + " Enter <1>, 2, 3 or 4: " );

    answr=readans();
    if(answr!=null && answr.length()>0){
      try{
        ifile=Integer.parseInt(answr.trim());
      }catch(NumberFormatException e){
        ifile=1;
      }
    }else{
      ifile=1;
    }

    lcs =lnblnk(expnam);
    if (ifile == 1)  
      brefl = expnam.substring((1)-1,lcs)+".peaks";
    if (ifile == 2)  
      brefl = expnam.substring((1)-1,lcs)+".peakint";
    if (ifile == 3)  
      brefl = expnam.substring((1)-1,lcs)+".intscd";
    if (ifile == 4)  
      brefl = expnam.substring((1)-1,lcs)+".integrate";

    i = i+1;
    jexp[(i)- 1] = expnum;

    // c      CALL OPNEXP (45,EXPNAM)
    // c      call getrun1(nrun1)
    iuexp = 45;
    opnx(iuexp,expnam);
    key76 = "      RUN1  ";
    isam = readexp(iuexp,key76,text);
    answr=readans();
    if(answr!=null && answr.length()>0){
      try{
        nrun1=Integer.parseInt(answr.trim());
      }catch(NumberFormatException e){
        nrun1=0;
      }
    }else{
      nrun1=0;
    }

    System.out.println("\n RUN number of first histogram (NRUN1) is "
                       + (nrun1) + " " );

    System.out.println("\n The RUN number of the first histogram in this "
                       +"experiment is NRUN1 = "  + (nrun1) + " " );
    // c      CALL KEYWRT (45,2,0,0)
    getinstcal(iuexp);
    // 
    System.out.println(" \n Do you want to select individual histograms"
                       +" (Y,<N>)? " );

    yn = "N";
    answr=readans();
    if(answr!=null && answr.length()>0)
      yn=answr.trim().substring(0,1);

    mhst = 0;
    if (yn.equalsIgnoreCase("Y") || yn.equalsIgnoreCase("y"))  {
      jnum = 1;
      prompt = " Input NRUN (terminate with <0>): ";
      lcs =lnblnk(prompt);
      mhst = 1;
      iq = 0;
      // c!!make into while loop while goto=8069 or 0

      while(true){
        Goto = 0;
        iq = iq+1;

        System.out.println((prompt.substring(0,(lcs+1))) + " " );

        nhst[(iq)- 1] = 0;
        answr=readans();
        if(answr!=null && answr.length()>0){
          try{
            nhst[(iq)-1]=Integer.parseInt(answr);
          }catch(NumberFormatException e){
            // let it drop on the floor
          }
        }
        if (nhst[(iq)- 1] != 0)  {
          if (jnum == 1)  
            nhst[(iq)- 1] = nhst[(iq)- 1]-nrun1+1;
          Goto = 8069;
        }else{
          break;
        }
      }

      if (Goto == 0)  {
        numh = iq-1;
        System.out.println((" "));
      }
    }

    // CHECK TO SEE IF THE USER WISHES TO RESTRICT THE DATA INDEXED
    if (Goto == 0)  {
      iexp = i;
      System.out.println("\n Only include reflections with wavelengths between"
                         + "\n WLMIN and WLMAX of <0.0,10.0>: " );
      wlmin = 0.0;
      wlmax = 10.0;
      answr=readans();
      if(answr!=null && answr.length()>0){
        answr=answr.trim();
        int index=answr.indexOf(" ");
        if(index>0){
          try{
            wlmin=Double.parseDouble(answr.substring(0,index).trim());
            wlmin=Double.parseDouble(answr.substring(index+1).trim());
          }catch(NumberFormatException e){
            // let it drop on the floor
          }
        }
      }

      System.out.println(" Minimum peak count <0>: " );
      mincnt = 0;
      answr=readans();
      if(answr!=null && answr.length()>0){
        try{
          mincnt=Integer.parseInt(answr.trim());
        }catch(NumberFormatException e){
          // let it drop on the floor
        }
      }
    }

    if (ifile == 1 && Goto == 0)  {
      System.out.println(" Crystal No. <1>: " );
      ixln = 1;
      answr=readans();
      if(answr!=null && answr.length()>0){
        try{
          ixln=Integer.parseInt(answr.trim());
        }catch(NumberFormatException e){
          // let it drop on the floor
        }
      }
    }

    // WHERE WILL REFLECTIONS BE LISTED?
    if (Goto == 0)  {
      System.out.println((" "));
      System.out.println(" List reflections (1) on the terminal\n"
                         +"                  (2) in a lsqrs.log file\n"
                         +"                  (3) or both?\n"
                         +" Input 1, 2 or <3>: " );
      ip = 3;
      answr=readans();
      if(answr!=null && answr.length()>0){
        try{
          ip=Integer.parseInt(answr.trim());
        }catch(NumberFormatException e){
          // let it drop on the floor
        }
      }
      unit16 = wopen("lsqrs.log");

      // GET H,K,L TRANSFORMATION MATRIX FROM USER
      jindex();

      // CALCULATE RUN NOS
      for (j = 1; j <= 6; j++) {
        for (i = 1; i <= 3; i++) {
          a[(i)- 1+(j- 1)*3] = 0.0;
          // DETERMINE STATUS OF REFL FILE
        }
      }
    }

    xx1 = 0;
    // c!!convert to while loop xx1=0 or goto=200
    //for (Goto = xx; Goto <= 200; Goto++) 
    while( xx1==0 || Goto==200 ){
      xx1 = 1;
      // -------------------- BEGIN OF REGION
      for (ii = 1; ii <= iexp; ii++) {
        if (Goto == 400)  
          Goto = 0;
        if (Goto == 0)  {
          expnum = jexp[(ii)- 1];
          lcs = lnblnk(expnam);
        }
        if (ifile == 1 && Goto == 0)  {
          brefl = expnam.substring((1)-1,lcs)+".peaks";

          unit1 = ropen(brefl);
          if(unit1 == null)  
            Goto = 200;
          if(Goto == 200)  
            Goto = 0;
          if( Goto==0 ){
            System.out.println((brefl) + "  NULL   does not exist." );
            System.out.println(" Input complete reflection filename: " );
            brefl =readans();
            unit1 = ropen(brefl);
            if (unit1 == null)  
              Goto = 200;
          }else{
            Goto = 2011;
          }
        }

        // -------------------- BEGIN OF REGION
        xx = 0;
        //for (Goto = 2050; Goto <= xx; Goto++)
        while( (Goto == 2050) || (xx==0) ){
          xx = 1;
          if (ifile == 2 && (Goto == 0 || Goto == 2050))  {
            if (Goto == 0)  
              brefl = expnam.substring((1)-1,lcs)+".peakint";
            if (Goto == 0)  
              unit1 = ropen(brefl);
            if (unit1 == null)  
              Goto = 2050;
            if (unit1 != null)  {
              Goto = 2011;
            }else{
              if (Goto == 2050)  
                Goto = 0;
              System.out.println((brefl) + "  NULL   does not exist.");
              System.out.println(" Input complete reflection "
                                 +"filename: " );
              unit1 = ropen(brefl);
              if (unit1 == null)  
                Goto = 2050;
            }
          }

          if( ifile==3 && Goto==0 ){
            brefl = expnam.substring((1)-1,lcs)+".intscd";
            unit1 = ropen(brefl);
            if (unit1 != null)  {
              Goto = 2011;
            }else{
              System.out.println((brefl) + "  NULL   does not exist.");
              System.out.println(" Input complete reflection "
                                 +"filename: " );
              unit1=ropen(brefl);
              if (unit1==null)  
                Goto = 2050;
            }
          }

          if( ifile==4 && Goto==0 ){
            brefl = expnam.substring((1)-1,lcs)+".integrate";
            unit1 = ropen(brefl);
            if (unit1 != null)  {
              Goto = 2011;
            }else{
              System.out.println(brefl + "  NULL   does not exist." );
              System.out.println(" Input complete reflection "
                                 +"filename: " );
              unit1 = ropen(brefl);
              if (unit1 == null)  
                Goto = 2050;
            }
          }
        }

        // PERFORM LS ON LATTICE PARAMETERS AND O. M.
        if (Goto == 2011)  
          Goto = 0;
        if (Goto == 0)  {
          System.out.println("\n"  + " Do you want to select "
                             +"individual SEQNUMs (Y,<N>)? " );
          yn = "N";
          answr=readans();
          if(answr!=null && answr.length()>0)
            yn=answr.substring(0,1);
          mseq = 0;
          jnseq = 0;
        }

        if( Goto==0 && (yn.equalsIgnoreCase("Y") || yn.equalsIgnoreCase("y"))){
          mseq = 1;
          System.out.println("\n SEQNUMs from <1> terminal or (2) file"
                             +" seqnum.dat?: " );
          isin = 1;
          answr=readans();
          if(answr!=null && answr.length()>0){
            try{
              isin=Integer.parseInt(answr.trim());
            }catch(NumberFormatException e){
              // let it drop on the floor
            }
          }
          if (isin == 2)  
            unit15 = ropen("SEQNUM.DAT");
        }

        // -------------------- REGION
        for (Goto = 201; Goto <= 13601; Goto++) {
          if (Goto == 201)  
            Goto = 0;
          if (mseq == 1 && Goto == 0)  {
            unit1 = rewind(unit1);

            if (isin == 1)  {
              System.out.println("$Enter SEQNUM (terminate with <0>): ");
              mnseq = 0;
              answr=readans();
              if(answr!=null && answr.length()>0){
                try{
                  mnseq=Integer.parseInt(answr.trim());
                }catch(NumberFormatException e){
                  // let it drop on the floor
                }
              }
            }
            if( isin==2) 
              try{mnseq=unit15.readInt();
              }catch(Exception s){Goto=400;}
            if (eof(unit15))  
              Goto = 400;
            if( Goto==0){
              try{
                mnseq = unit15.readInt();
              }catch( Exception s){
                Goto=400;
              }
              if (eof(unit15))  
                Goto=400;
            }
            if ( mnseq==0 && Goto==0 ){
              Goto = 400;
            }else{
              jnseq = jnseq+1;
              nseq[(jnseq)- 1] = mnseq;
            }
          }
          if (Goto == 13601)  
            Goto = 0;
          if (Goto == 0)  {
            redrefl(1,ieof);
            if (ieof == 1)  
              Goto = 400;
            if (Goto == 0)  {
              hstnum = nrun-nrun1+1;

              if (ifile == 1)  {
                if (reflag != ixln)  
                  Goto = 13601;
              }
            }
            if (mseq == 1 && Goto == 0)  {
              if (seqnum == mnseq)  
                Goto = 13602;
              if (Goto == 0)  
                Goto = 13601;
            }

            if (mhst == 1 && Goto == 0)  {
              for( i=0 ; (i<numh)&&(Goto==0) ; i++) {
                if (hstnum == nhst[i] && Goto == 0)  
                  Goto = 13595;
              }
              if (Goto == 0)  
                Goto = 13601;
            }
          }
          if (Goto == 13595)  
            Goto = 0;
          if (Goto == 0)  {
            if (ipk < mincnt)  
              Goto = 13601;
            if (Goto == 0 && (wl < wlmin || wl > wlmax))  
              Goto = 13601;
            if (Goto == 0)  
              iflag = (reflag)%(100) ;
            // C   ONLY INTERPOLATED PEAKS ARE USED IN L.S.
            if (reflag > 399 && Goto == 0)  
              Goto = 13601;
            if (ih == 0 && ik == 0 && il == 0 && Goto == 0)  
              Goto = 13601;
          }
          if (Goto == 13602)  
            Goto = 0;
          if (Goto == 0)  {
            subs.chi=chi;
            subs.phi=phi;
            subs.omega=omega;
            subs.deta=deta;
            subs.detd=detd;

            subs.laue(xcm,ycm,wl,Diffxx,Diffyy,Diffzz);
            diffxx=Diffxx.val;diffyy=Diffyy.val;diffzz=Diffzz.val;
            Ih= new intW(ih);Ik=new intW(ik); Il=new intW(il);
            jindex2(Ih,Ik,Il);
            ih=Ih.val;ik=Ik.val;il=Il.val;
            h = 0.0+(ih);
            k = 0.0+(ik);
            l = 0.0+(il);
            v[0] = h;
            v[1] = k;
            v[2] = l;
            v[3] = diffxx;
            v[4] = diffyy;
            v[5] = diffzz;

            for( j=0 ; j<6 ; j++ ){
              for( i=0 ; i<3 ; i++ ){
                a[i+j*3] = a[i+j*3]+v[i]*v[j];
              }
            }
            Goto = 201;
          }
        }
        if (Goto == 400)  
          Goto = 0;
      }

      // SAVE THE LEFT HAND SIDE AS THE VARIENCE-COVARIENCE MATRIX
      for( j=0 ; j<3 ; j++ ){
        for( i=0 ; i<3 ; i++ ){
          vc[i+j*3] = a[i+j*3];
        }
      }

      // SOLVE THE NORMAL EQUATIONS AND THE THREE RIGHT HAND SIDES
      for( i=0 ; i<3 ; i++ ){
        piv = a[i+i*3];
        if (Math.abs(piv) < 0.00001)  
          unit16.println(" PIVOT ELEMENT IS SMALL: LEAST SQUARES "
                         +"INACCURATE" );
        if (Math.abs(piv) < 0.00001)  
          piv = 0.00001;

        for( j=0 ; j<6 ; j++ ){
          a[i+j*3] = a[i+j*3]/piv;
        }

        for( j=0 ; j<3 ; j++ ){
          if (j == i)  
            Goto = 21000;
          if (Goto == 0)  {
            ql = a[j+i*3];
            for( m=0 ; m<6 ; m++ ){
              a[j+m*3] = a[j+m*3]-ql*a[i+m*3];
            }
          }
          if (Goto == 21000)  
            Goto = 0;
        }
      }

      for( j=0; j<3 ; j++ ){
        for( i=0 ; i<3 ; i++ ){
          orgmat[j+i*3] = a[i+(j+3)*3];
        }
      }

      // TRANSPOSETO OBTAIN THE ORIENTATION MATRIX
      for( j=0 ; j<3 ; j++ ){
        for( i=0 ; i<3 ; i++ ){
          oi[i+j*3] = orgmat[j+i*3];
        }
      }

      // STORE ORGMAT IN OI AND INVERT
      for( j=0 ; j<3 ; j++ ){
        for( i=0 ; i<3 ; i++ ){
          orgmat[i+j*3] = oi[i+j*3];
        }
      }

      matinv(oi);
      abc(orgmat,A,B,C,Alpha,Beta,Gamma,Vol);
      b[0]=A.val;
      b[1]=B.val;
      b[2]=C.val;
      b[3]=Alpha.val;
      b[4]=Beta.val;
      b[5]=Gamma.val;
      b[6]=Vol.val;
      System.out.println("\n\n LATTICE PARAMETERS:\n"  + (b) + "  NULL  ");
      matinv(vc);

      // COMPARE CALCULATED AND OBSERVED VALUES
      line = 0;
      ncntr = 0;
      del = 0.0;

      unit1 = rewind(unit1);
      curhst = 0;
      delhst = 0.0;
      nref = 0;

      // make while loop goto==200 do not go through
      for (ii = 1; (ii <= iexp) &&(Goto !=200); ii++) {
        if (Goto == 500)  
          Goto = 0;
        expnum = jexp[(ii)- 1];

        if (ifile == 2)  {
          unit1 = ropen(brefl);
          if (unit1 == null)  
            Goto = 500;
        }else{
          unit1 =ropen(brefl);
          if (unit1 == null)  
            Goto = 200;
        }

        // c!! make a while loop
        for (Goto = 1; Goto <= 30000; Goto++) {
          if (Goto == 30000)  
            Goto = 0;
          if (Goto == 0)  {
            redrefl(1,ieof);

            if(eof(unit1)){
              Goto = 500;
            }else{
              hstnum = nrun-nrun1+1;

              // HSTNUM = MOD(HSTNUM,100)
              if (ifile == 1 && reflag != ixln)  
                Goto = 30000;

              if (wl < wlmin || wl > wlmax && Goto == 0)  
                Goto = 30000;

              if (mseq == 1 && Goto == 0)  {
                for( ja=0 ; ja<jnseq ; ja++ ){
                  if (seqnum == nseq[ja])  
                    Goto = 30111;
                }
                if (Goto == 0)  
                  Goto = 30000;
              }
            }
          }

          // if( goto.eq.0)IHST=MOD(HSTNUM,100)
          if (mhst == 1 && Goto == 0)  {
            for( jh=0 ; jh<numh ; jh++ ){
              if (hstnum == nhst[jh])  
                Goto = 30111;
            }
            if (Goto == 0)  
              Goto = 30000;
          }

          // Close if()!XXX dropped
          if (Goto == 30111)  
            Goto = 0;
          if (Goto == 0)  {
            ip2 = 0;
            // IF(IHST.GT.JHMAX(II).OR.IHST.LT.JHMIN(II)) GO TO 30000
            if (ipk < mincnt)  
              Goto = 30000;
            if (Goto == 0)  {
              astar = blank;
              if (ipk >= mincnt)  
                astar = star;
              diffxx=Diffxx.val;diffyy=Diffyy.val;diffzz=Diffzz.val;
              Ih= new intW(ih);Ik=new intW(ik); Il=new intW(il);
              jindex2(Ih,Ik,Il);
              ih=Ih.val;ik=Ik.val;il=Il.val;
              if (hstnum == curhst)  
                Goto = 13501;
            }
            if (Goto == 0)  {
              curhst = hstnum;

              // CALL KEYWRT (45,3,0,0)
              gettof(iuexp);

              if (nref == 0)  
                Goto = 13800;
            }
            if (Goto == 0)  {
              delhst = delhst/nref;
              if (ip != 2)  
                System.out.println(" DELHST = "  + (delhst) + " " );
              if (ip != 1)  
                unit16.println("original format did not work"
                               + (delhst) + " " );
              delhst = 0.0;
              nref = 0;
            }
          }

          if (Goto == 13800)  
            Goto = 0;
          if (Goto == 0)  {
            if (ip != 2)  
              System.out.println(" LEAST SQUARES OUTPUT FOR RUN "
                                 + (nrun) + "  HISTOGRAM "  + (hstnum)
                                 + " \n   #    H    K    L       X   "
                                 +"   Y      Z      XCM     YCM     "
                                 +" WL      IPK\n" );
            if (ip2 == 1)  
              Goto = 13501;
            if (ip != 1 && Goto == 0)  
              unit16.println("1LEAST SQUARES OUTPUT FOR RUN "  + (nrun)
                             + "  HISTOGRAM "  + (hstnum) + " \n"
                             + "   #    H    K    L       X      Y    "
                             +"  Z      XCM     YCM      WL      IPK"
                             + "\n" );
          }
          if (Goto == 13501)  
            Goto = 0;
          if (Goto == 0)  {
            subs.chi=chi;
            subs.phi=phi;
            subs.omega=omega;
            subs.deta=deta;
            subs.detd=detd;
            subs.laue(xcm,ycm,wl,Diffxx,Diffyy,Diffzz);
            diffxx=Diffxx.val;diffyy=Diffyy.val;diffzz=Diffzz.val;
            h = 0.0+(ih);
            k = 0.0+(ik);
            l = 0.0+(il);
            if (ip != 2)  
              System.out.println("  "+seqnum+" "+h+" "+k+" "+l+" "+x+" "
                                 +y+" "+z+" ");
            if (ip != 1)  
              unit16.println("  "+seqnum+" "+h+" "+k+" "+l+" "+x+" "+y+" "+z
                             +" ");

            // CALCULATE THE DIFFRACTION VECTOR COSINES FROM THE MATIX
            // AND THE HKL VECTOR
            for( i=0 ; i<3 ; i++ ){
              for( j=0 ; j<3 ; j++ ){
                curmat[i+j*3] = orgmat[i+j*3];
              }
            }

            newrot(curmat,chi,phi,omega);
            for( j=0 ; j<3 ; j++ ){
              v[j+9] = curmat[0+j*3]*h+curmat[1+j*3]*k+curmat[2+j*3]*l;
            }

            for( j=0 ; j<3 ; j++ ){
              dv[j+9] = orgmat[0+j*3]*h+orgmat[1+j*3]*k+orgmat[2+j*3]*l;
            }
            // CALCULATE THE XCM,YCM,WL VALUES
            // USE HAMILTONS CONVENTION (COORDINATE SYSTEM)
            hh = (int)((v[9]*v[9])+(v[10]*v[10])+(v[11]*v[11]));
            wl = 2.0*v[(10)- 1]/hh;

            // CALCULATE XCM AND YCM DET COODINATES, BUT FIRST
            // TRANSLATE ORIGIN FROM RECIPROCAL LATTICE ORIGIN
            // TO THE CRYSTAL ORIGIN
            xdp = v[9]-(1.0/wl);

            // ROTATE TO A DETECTOR ANGKLE OF ZERO
            ang = -deta/rad;
            xt = xdp;
            yt = v[10];
            xdp = xt*Math.cos(ang)+yt*Math.sin(ang);
            ydp = -xt*Math.sin(ang)+yt*Math.cos(ang);
            zdp = v[11];

            // CALCULATE XCM AND YCM
            xcm = -(ydp/xdp)*detd;
            ycm = -(zdp/xdp)*detd;

            // CALCULATE CHANNEL NUMBERS X,Y,Z
            xcalc = ((xcm-xleft)/x2cm)+0.5;
            ycalc = ((ycm-ylower)/y2cm)+0.5;
            xl = l1+Math.sqrt((detd*detd)+(xcm*xcm)+(ycm*ycm));
            if (wl < wlmin)  
              Goto = 1001;
            if (wl > wlmax)  
              Goto = 1001;
          }

          if (Goto == 0)  {
            tobs = wl*(xl/hom)-tzero;

            ntime[(wlnum+1)- 1] = tmax;
            kkk = -1;
            for( i=1 ; i<(wlnum+1) ; i++ ){
              if (tobs < ntime[i] && kkk == -1)  {
                kkk = i+1;
              }
            }
            i = kkk;

            delta = (double)(ntime[(i)- 1]-ntime[(i-1)- 1]);
            zcalc = i-1.0+(tobs-ntime[(i-1)- 1])/delta;
            Goto = 1002;
          }              // Close if()

          if (Goto == 1001)  
            Goto = 0;
          if (Goto == 0)  
            z = 0.0;
          if (Goto == 1002)  
            Goto = 0;
          if (Goto == 0)  {
            fit = "       ";
            delx = Math.abs(x-xcalc);
            dely = Math.abs(y-ycalc);
            delz = Math.abs(z-zcalc);
            if (delx > 2. || dely > 2. || delz > 2.)  
              fit = "BAD FIT";

            if (ip != 2)  
              System.out.println("   "+astar+"               "+xcalc+" "
                                 +ycalc+" "+zcalc+"   "+xcm+" ");
            if (ip != 1)  
              unit16.println("   "+astar+"               "+xcalc+" "+ycalc+" "
                             +zcalc+"   "+xcm+" ");

            // CALCULATE INDICES
            h = oi[0+0*3]*diffxx+oi[1+0*3]*diffyy+oi[2+0*3]*diffzz;
            k = oi[0+1*3]*diffxx+oi[1+1*3]*diffyy+oi[2+1*3]*diffzz;
            l = oi[0+2*3]*diffxx+oi[1+2*3]*diffyy+oi[2+2*3]*diffzz;

            delhkl = Math.abs(h-ih)+Math.abs(k-ik)+Math.abs(l-il);
            if (delhkl < 0.1)  {
              if (ip != 2)  
                System.out.println("     "+h+"      DELHKL = "+k+"     "+l);
              if (ip != 1)  
                unit16.println("     "+h+"      DELHKL = "+k+"     "+l+" ");
              Goto = 490;
            }
            if (delhkl < 0.2 && Goto == 0)  {
              if (ip != 2)  
                System.out.println("     "+h+"      DELHKL = "+k+"     "+l);
              if (ip != 1)  
                unit16.println("     "+h+"      DELHKL = "+k+"     "+l+" ");
              Goto = 490;
            }
            if (delhkl < 0.3 && Goto == 0)  {
              if (ip != 2)  
                System.out.println("     "+h+"      DELHKL = "+k+"     "+l);
              if (ip != 1)  
                unit16.println("     "+h+"      DELHKL = "+k+"     "+l+" ");
              Goto = 490;
            }
          }
          if (Goto == 0)  {
            if (ip != 2)  
              System.out.println("     "+h+"      DELHKL = "+k+"     "+l+" ");
            if (ip != 1)  
              unit16.println("     "+h+"      DELHKL = "+k+"     "+l+" ");
          }
          if (Goto == 490)  
            Goto = 0;
          if (Goto == 0)  {
            ncntr = ncntr+1;
            del = square(diffxx-dv[9])+square(diffyy-dv[10])
              +square(diffzz-dv[11])+del;
            delhst = delhst+delhkl;
            nref = nref+1;
            Goto = 30000;
          }
          if (Goto == 500)  
            Goto = 0;
        }
      }
    }

    // BEGIN FINAL SUMMARY AND TYPEOUT
    if (Goto != 0)  
      return;
    delhst = delhst/nref;
    if (ip != 2)  
      System.out.println(" DELHST = "  + (delhst) + " " );
    if (ip != 1)  
      unit16.println("original format did not work"  + (delhst) + " " );

    for( i=0 ; i<7 ; i++ ){
      sig[i] = 0.0;
      celsca[i] = b[i];
    }

    for( ii=0 ; ii<3 ; ii++ ){
      for( jj=0 ; jj<3 ; jj++ ){
        orgmat[jj+ii*3] = orgmat[jj+ii*3]+small;
        abc(orgmat,A,B,C,Alpha,Beta,Gamma,Vol);
        b[(1)- 1]=A.val;
        b[(2)- 1]=B.val;
        b[(3)- 1]=C.val;
        b[(4)- 1]=Alpha.val;
        b[(5)- 1]=Beta.val;
        b[(6)- 1]=Gamma.val;
        b[(7)- 1]=Vol.val;
        orgmat[jj+ii*3] = orgmat[jj+ii*3]-small;
        for( kk=0 ; kk<7 ; kk++ ){
          derv[jj+kk*3] = (b[kk]-celsca[kk])/small;
        }
      }
      // ACCUMULATE SIGMAS
      for( m=0 ; m<7 ; m++ ){
        for( i=0 ; i<3 ; i++ ){
          for( j=0 ; j<3 ; j++ ){
            sig[m] = sig[m]+derv[i+m*3]*vc[i+j*3]*derv[j+m*3];
          }
        }
      }
    }

    // CALCULATE THE NUMBER OF DEGREES OF FREEDOM
    fi = 3.0*(ncntr-3);
    if (fi < 0.0)  
      System.out.println(" INSUFFICIENT DATA FOR STANDARD DEVIATIONS" );
    if (fi < 0.0)  
      System.exit(1);
    del = del/fi;
    for( i=0 ; i<7 ; i++ ){
      sig[i] = Math.sqrt(del*sig[i]);
    }

    System.out.println("\n\n LEAST SQUARES SUMMARY:\n" );
    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ )
        System.out.print( orgmat[i+j*3]);
      System.out.println("");
    }
    System.out.println("\n\n LATTICE PARAMETERS:\n"+b+"  NULL  ");
    System.out.println(" LATTICE PARAMETER STANDARD DEVIATIONS:\n"+sig
                       +"  NULL  ");
    System.out.println("\n\n MINCNT"+mincnt+"     TOTAL NUMBER OF REFLECTIONS "
                       +ncntr+" ");
    System.out.println(" WLMIN, WLMAX"+wlmin+" ");
    if (ip == 1)  
      Goto = 39010;
    if (Goto == 0)  {
      unit16.println("1LEAST SQUARES SUMMARY:\n\n\n" );
      for( i=0 ; i<3 ; i++ ){
        for( j=0 ; j<3 ; j++ )
          unit16.print( orgmat[i+j*3]);
        unit16.println("");
      }
      unit16.println("\n\n LATTICE PARAMETERS:\n"+b+"  NULL  ");
      unit16.println(" LATTICE PARAMETER STANDARD DEVIATIONS:\n"+sig 
                     +"  NULL  ");
      unit16.println("\n\n MINCNT"+mincnt+"     TOTAL NUMBER OF REFLECTIONS "
                     +ncntr+" ");
      unit16.println(" WLMIN, WLMAX"+wlmin+" ");
    }
    // DOES THE USER WANT TO SAVE THE MATRIX AND CELL PARAMETERS?
    if (Goto == 39010)  
      Goto = 0;
    if (Goto == 0)  {
      System.out.println();
      ans = "N";
      answr=readans();
      if(answr!=null && answr.length()>0)
        ans=answr.trim().substring(0,1);

      if(ans.equalsIgnoreCase("Y") || ans.equalsIgnoreCase("y")){
        System.out.println(" ENTER filename.filetype: " );
        fname=readans().trim();
        unit4 = wopen(fname);
        for( i=0 ; i<3 ; i++ ){
          for( j=0 ; j<3 ; j++ )
            unit4.print( orgmat[i+j*3]);
          unit4.println("");
        }
        unit4.print("");
        for( i=0 ; i<7 ; i++ )
          unit4.print(b[i]+" ");

        unit4.println();
        unit4.println(sig+"  NULL  ");
      }
    }

    System.out.println("\n Save matrix and cell parameters in the expnam.x "
                       + "file (Y,<N>)? " );
    ans = "N";
    answr=readans();
    if(answr!=null && answr.length()>0)
      ans=aswr.trim().substring(0,1);
    if (ans.equalsIgnoreCase("Y") || ans.equalsIgnoreCase("y")){
      return;
    }
    // FORMAT STATEMENTS
    System.exit(1);
    return;

  }

  public static String readans(){
    char c=0;
    String Res="";
    try{
      c =(char) System.in.read();
      while( c >=32){
        Res+=c;
        c =(char) System.in.read();
      }

    }catch(Exception ss){
      return Res;
    }
    return Res;
  }

  public static minputStream ropen( String filename){
    try{
      return new minputStream(filename);
    }catch( Exception s){
      return null;
    }
  }

  public static PrintStream wopen( String filename){
    try{
      return new PrintStream( new FileOutputStream(filename));
    }catch( Exception s){
      return null;
    }
  }

  public static minputStream rewind( minputStream dinput){
    try{
      String filename= dinput.filename;
      dinput.close();
      return ropen( filename);
    }catch(Exception ss){
      return null;
    }
  }

  static class minputStream extends DataInputStream{
    String filename;

    public minputStream( String filename) throws IOException{
      super( new FileInputStream(filename));
      this.filename=filename;
    }
  }

  public static void abc(double [] u, doubleW a, doubleW b, doubleW c,
                      doubleW alpha, doubleW beta, doubleW gamma, doubleW vol){
    double [] ut= new double[3*3];
    double [] g= new double[3*3];
    double [] gi= new double[3*3];
    double rad = 57.295779513;
    int i= 0;
    int j= 0;
    double arg1= 0.0;
    double arg2= 0.0;
    double arg3= 0.0;
    double cstar= 0.0;
    int _u_offset=0;

    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        ut[i+j*3] = u[j+i*3+_u_offset];
      }
    }

    matmul(u,ut,gi);
    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        g[i+j*3] = gi[i+j*3];
      }
    }
    matinv(g);
    a.val = Math.sqrt(g[0+0*3]);
    b.val = Math.sqrt(g[1+1*3]);
    c.val = Math.sqrt(g[2+2*3]);
    arg1 = g[1+2*3]/(b.val*c.val);
    arg2 = g[0+2*3]/(a.val*c.val);
    arg3 = g[1+1*3]/(a.val*b.val);
    // accos changed to cos to get thru f2java next 3 lines
    alpha.val = (Math.acos(arg1))*rad;
    beta.val = (Math.acos(arg2))*rad;
    gamma.val = (Math.acos(arg3))*rad;

    // CALCULATE VOLUME
    cstar = Math.sqrt(gi[(3)- 1+(3- 1)*3]);
    vol.val = a.val*b.val*Math.sin(gamma.val/rad)*(1.0/cstar);
    return;
  }

  public static void matmul( double[]A, double[]B, double[]C){
    for( int i=0 ; i<3 ; i++ )
      for( int j=0 ; j<3 ; j++ )
        C[j+i*3]=0;

    for( int i=0 ; i<3 ; i++ )
      for( int j=0 ; j<3 ; j++ )      
        for( int k=0 ; k<3 ;k++ )
          C[j+i*3]+=A[k+i*3]*B[j+3*i];


  }

  public static void matinv (double [] matrix){
    double [] a= new double[3*3];
    double [] ai= new double[3*3];
    double d= 0.0;
    int i= 0;
    int j= 0;
    int k= 0;
    int m= 0;
    int n= 0;
    int l= 0;
    int _matrix_offset=0;

    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        a[i+j*3] = matrix[i+j*3+ _matrix_offset];
      }
    }

    for( j=0 ; j<3 ; j++ ){
      m = j+2;
      if (m > 3)  
        m = m-3;
      n = j+3;
      if (n > 3)  
        n = n-3;
      for( i=0 ; i<3 ; i++ ){
        k = i+2;
        if (k > 3)  
          k = k-3;
        l = i+3;
        if (l > 3)  
          l = l-3;
        ai[i+j*3] = a[(m-1)+(k-1)*3]*a[(n-1)+(l-1)*3]
          -a[(m-1)+(l-1)*3]*a[(n-1)+(k-1)*3];
      }
    }

    d = ai[0+0*3]*a[0+0*3]+ai[1+0*3]*a[0+1*3]+a[0+2*3]*ai[2+0*3];
    if (Math.abs(d) < 0.00000001)  
      System.out.println(" INVERSION OF MATRIX IS INDETERMINANT" );

    for( i=0 ; i<3 ; i++ ){
      for( j=0 ; j<3 ; j++ ){
        ai[i+j*3] = ai[i+j*3]/d;
        matrix[i+j*3+ _matrix_offset] = ai[i+j*3];
      }
    }

    return;
  }

  public static void newrot (double [] u, double chi, double phi, double om){
    int _u_offset=0;
    double [] v= new double[3*3];
    double [] f= new double[3*3];
    int i= 0;
    int j= 0;
    double p= 0.0;
    double cp= 0.0;
    double sp= 0.0;
    double c= 0.0;
    double cc= 0.0;
    double sc= 0.0;
    double o= 0.0;
    double co= 0.0;
    double so= 0.0;
    double rad = 57.29578;

    p = phi/rad;
    cp = Math.cos(p);
    sp = Math.sin(p);
    c = -chi/rad;
    cc = Math.cos(c);
    sc = Math.sin(c);
    o = -om/rad;
    co = Math.cos(o);
    so = Math.sin(o);

    // CONSTRUCT THE ROTATION MATRIX F
    f[0+0*3] = cp*co-sp*so*cc;
    f[0+1*3] = cp*so+sp*co*cc;
    f[0+2*3] = sp*sc;
    f[1+0*3] = -sp*co-cp*so*cc;
    f[1+1*3] = -sp*so+cp*co*cc;
    f[1+2*3] = cp*sc;
    f[2+0*3] = sc*so;
    f[2+1*3] = -sc*co;
    f[2+2*3] = cc;

    // THE ROTATED MATRIX IS THE PRODUCT OF U X F
    matmul(u,f,v);
    for( i=1 ; i<3 ; i++ ){
      for( j=1 ; j<3 ; j++ ){
        u[i+j*3+ _u_offset] = v[i+j*3];
      }
    }

    return;
  }

  static double [] trans = {1., 0., 0.,
                            0., 1., 0.,
                            0., 0., 1.};

  public static void jindex ()  {
    int i= 0;
    int j= 0;
    int Goto=0;
    String ans= new String(" ");
    String answr= new String("                                                "
                             +"            ");
    int nchrs= 0;
    int h= 0;
    int k= 0;
    int l= 0;
    int ih= 0;
    int ik= 0;
    int il= 0;

    Goto=9;
    while(Goto==0){
      Goto=0;
      System.out.println("INDEX TRANFORMATION MATRIX:");
      for( i=0 ; i<3 ; i++ ){
        for( j=0 ; j<3 ; j++ )
          System.out.print(trans[j+i*3]);
        System.out.println("");
      }
      System.out.println(" Do you want to input a new transformation "  
                         + "matrix (Y,<N>)? " );
      ans = "N";
      answr=readans();
      if(answr!=null && answr.length()>0)
        ans=answr.trim().substring(0,1);
      if (!ans.equalsIgnoreCase("Y") && !ans.equalsIgnoreCase("y"))  
        return;

      System.out.println(" INPUT NEW TRANSFORMATION MATRIX" );
      for( i=0 ; i<3 ; i++ ){
        System.out.println(" ROW"+(i+1)+" : ");
        int index=-1;
        answr=readans().trim();
        for( j=0 ; j<3 ; j++ ){
          trans[i+j*3]=Double.parseDouble(answr.substring(0,index));
          answr=answr.substring(index+1).trim();
        }
      }

      Goto=9; 
    }
  }

  public static void jindex2( intW h, intW k, intW l){
    int ih = h.val;
    int ik = k.val;
    int il = l.val;
    h.val = (int)(trans[0+0*3]*ih+trans[0+1*3]*ik+trans[0+2*3]*il);
    k.val = (int)(trans[1+0*3]*ih+trans[1+1*3]*ik+trans[1+2*3]*il);
    l.val = (int)(trans[2+0*3]*ih+trans[2+1*3]*ik+trans[2+2*3]*il);

    return;
  }

  private static double square(double a){
    return a*a;
  }

  public static void gettof( int iuexp){
    System.out.println("Peter, I did not do this yet. Maybe you already have");
    System.out.println("  to read these files: subroutin gettof");
  }

  public static void redrefl( int i, int ieof){
    System.out.println("Peter, I did not do this yet. Maybe you already have");
    System.out.println("  to read these files: subroutin readrefl");
  }

  public static boolean eof( minputStream f){
    try{
      return f.available() >0;
    }catch( Exception ss){
      return false;
    }
  }

  public static int lnblnk( String prompt){
    System.out.println("Peter, I did not do this yet. Maybe you already have");
    System.out.println("  to read these files: subroutin lnblnk");
    return 0;
  }

  public static void getinstcal( int iuexp){
    System.out.println("Peter, I did not do this yet. Maybe you already have");
    System.out.println("  to read these files: subroutin getinstcal");
  }

  public static int readexp( int iuexp, String key76, String text){
    System.out.println("Peter, I did not do this yet. Maybe you already have");
    System.out.println("  to read these files: subroutin readexp");
    return 0;
  }

  public static void opnx( int iuexp, String expnam){
    System.out.println("Peter, I did not do this yet. Maybe you already have");
    System.out.println("  to read these files: subroutin opnx");
  }
}
