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

  static double [] sig= new double[(7)];

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


  static double [] a= new double[(3) * (6)];
  static double [] celsca= new double[(7)];
  static double [] curmat= new double[(3) * (3)];
  static double [] derv= new double[(3) * (7)];
  static double [] dv= new double[(12)];
  static double [] oi= new double[(3) * (3)];
  static double [] v= new double[(12)];
  static double [] vc= new double[(3) * (3)];
  static int [] jexp= new int[(10)];
  static int [] jhmin= new int[(10)];
  static int [] jhmax= new int[(10)];
  static int [] nseq= new int[(1000)];
  static int [] nhst= new int[(100)];
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

    //EasyIn _f2j_in = new EasyIn();
    doubleW Diffxx = new doubleW(0),
      Diffyy=new doubleW(0),Diffzz=new doubleW(0);
    doubleW A= new doubleW(0),B= new doubleW(0),C= new doubleW(0);

    doubleW Alpha=new doubleW(0),
      Beta= new doubleW(0),Gamma= new doubleW(0),Vol= new doubleW(0);
    intW Ih,Ik,Il;

    {
 
      forloop10000:
      for (index = 1; index <= 3; index++) {
        {
          forloop10001:
          for (jndex = 1; jndex <= 3; jndex++) {
            curmat[(index)- 1+(jndex- 1)*3] = 0.0;
            orgmat[(index)- 1+(jndex- 1)*3] = 0.0;
            //Dummy.label("Lsqrs",10001);
          }              //  Close for() loop. 
        }
        //Dummy.label("Lsqrs",10000);
      }              //  Close for() loop. 
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
    // c,$)
    expnam=readans();

    /*expnam = _f2j_in.readChars(14);
      _f2j_in.skipRemaining(); REMOVE */
    // 
    System.out.println(" \n Is input on a\n"
                       + "       (1) PEAKS file?\n"
                       + "    a  (2) PEAKINT output file?\n"
                       + "    a  (3) INTSCD output file?\n"
                       + " or a  (4) INTEGRATE output file?\n"
                       + " Enter <1>, 2, 3 or 4: " );
    // c,$)
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
    /*nchrs=answr.length(); REMOVE
      ifile = 1;
      if (nchrs > 0)  
      ifile = _f2j_in.readInt();
      _f2j_in.skipRemaining();*/
    // 
    lcs =lnblnk(expnam);
    if (ifile == 1)  
      brefl = expnam.substring((1)-1,lcs)+".peaks";
    if (ifile == 2)  
      brefl = expnam.substring((1)-1,lcs)+".peakint";
    if (ifile == 3)  
      brefl = expnam.substring((1)-1,lcs)+".intscd";
    if (ifile == 4)  
      brefl = expnam.substring((1)-1,lcs)+".integrate";
    // 
    i = i+1;
    jexp[(i)- 1] = expnum;
    // 
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
    //nrun1 = _f2j_in.readInt(); REMOVE
    //_f2j_in.skipRemaining();
    // 
    System.out.println("\n RUN number of first histogram (NRUN1) is "
                       + (nrun1) + " " );
    // 
    System.out.println("\n The RUN number of the first histogram in this "
                       +"experiment is NRUN1 = "  + (nrun1) + " " );
    // c      CALL KEYWRT (45,2,0,0)
    getinstcal(iuexp);
    // 
    System.out.println(" \n Do you want to select individual histograms"
                       +" (Y,<N>)? " );
    // c,$)
    yn = "N";
    answr=readans();
    if(answr!=null && answr.length()>0)
      yn=answr;
    else
      yn="N";
    /*nchrs=answr.length(); REMOVE
      if (nchrs > 0)  
      yn = _f2j_in.readChars(1);
      _f2j_in.skipRemaining();*/
    mhst = 0;
    // 
    if (yn.trim().equalsIgnoreCase("Y".trim())
                                  || yn.trim().equalsIgnoreCase("y".trim()))  {
      // 
      jnum = 1;
      prompt = " Input NRUN (terminate with <0>): ";
      lcs =lnblnk(prompt);
      mhst = 1;
      iq = 0;
      // c!!make into while loop while goto=8069 or 0
      {
        forloop8071:
        //for (Goto = 0; Goto <= 8069; Goto++) 
        while((Goto==0) ||(Goto==8069))
          {
            Goto = 0;
            label8069:
            //Dummy.label("Lsqrs",8069);
            iq = iq+1;
            // 
            System.out.println((prompt.substring((1)-1,(lcs+1))) + " " );
            // c,$)
            // 
            nhst[(iq)- 1] = 0;
            answr=readans();
            if(answr!=null && answr.length()>0){
              try{
                nhst[(iq)-1]=Integer.parseInt(answr);
              }catch(NumberFormatException e){
                // let it drop on the floor
              }
            }
            /*nchrs=answr.length(); REMOVE
              if (nchrs > 0)  
              nhst[(iq)- 1] = _f2j_in.readInt();
              _f2j_in.skipRemaining();*/
            // 
            if (nhst[(iq)- 1] != 0)  {
              if (jnum == 1)  
                nhst[(iq)- 1] = nhst[(iq)- 1]-nrun1+1;
              Goto = 8069;
            }              // Close if()
            //Dummy.label("Lsqrs",8071);
          }              //  Close for() loop. 
      }
      if (Goto == 0)  {
        numh = iq-1;
        System.out.println((" "));
      }              // Close if()
    }              // Close if()
    // 
    // C
    // C  CHECK TO SEE IF THE USER WISHES TO RESTRICT THE DATA INDEXED
    // C
    label300:
    //Dummy.label("Lsqrs",300);
    if (Goto == 0)  
      iexp = i;
    if (Goto == 0)  {
      System.out.println("\n Only include reflections with wavelengths between"
                         + "\n WLMIN and WLMAX of <0.0,10.0>: " );
      // c,$)
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

      /*nchrs=answr.length(); REMOVE
        if (nchrs > 0)  
        wlmin = _f2j_in.readDouble();
        wlmax = _f2j_in.readDouble();
        _f2j_in.skipRemaining();*/
      // 
      System.out.println(" Minimum peak count <0>: " );
      // c,$)
      mincnt = 0;
      answr=readans();
      if(answr!=null && answr.length()>0){
        try{
          mincnt=Integer.parseInt(answr.trim());
        }catch(NumberFormatException e){
          // let it drop on the floor
        }
      }
      /* nchrs=answr.length(); REMOVE
         if (nchrs > o)  
         mincnt = _f2j_in.readInt();
         _f2j_in.skipRemaining();*/
    }              // Close if()
    if (ifile == 1 && Goto == 0)  {
      System.out.println(" Crystal No. <1>: " );
      // c,$)
      ixln = 1;
      answr=readans();
      if(answr!=null && answr.length()>0){
        try{
          ixln=Integer.parseInt(answr.trim());
        }catch(NumberFormatException e){
          // let it drop on the floor
        }
      }
      /*nchrs=answr.length(); REMOVE
        if (nchrs > 0)  
        ixln = _f2j_in.readInt();
        _f2j_in.skipRemaining();*/
    }              // Close if()
    // 
    // C
    // C     WHERE WILL REFLECTIONS BE LISTED?
    // C
    if (Goto == 0)  {
      System.out.println((" "));
      System.out.println(" List reflections (1) on the terminal\n"
                         +"                  (2) in a lsqrs.log file\n"
                         +"                  (3) or both?\n"
                         +" Input 1, 2 or <3>: " );
      // c,$)
      ip = 3;
      answr=readans();
      if(answr!=null && answr.length()>0){
        try{
          ip=Integer.parseInt(answr.trim());
        }catch(NumberFormatException e){
          // let it drop on the floor
        }
      }
      /*nchrs=answr.length(); REMOVE
        if (nchrs > 0)  
        ip = _f2j_in.readInt();
        _f2j_in.skipRemaining();*/
      // c!!! fix could not translate
      // c      IF (IP.GT.1) OPEN(UNIT=16,FILE='lsqrs.log',TYPE='UNKNOWN')
      unit16 = wopen("lsqrs.log");
      // C
      // C      GET H,K,L TRANSFORMATION MATRIX FROM USER
      // C
      label20:
      //Dummy.label("Lsqrs",20);
      jindex();
      // C
      // C    CALCULATE RUN NOS
      // C
      {
        forloop13000:
        for (j = 1; j <= 6; j++) {
          {
            forloop12000:
            for (i = 1; i <= 3; i++) {
              label12000:
              //Dummy.label("Lsqrs",12000);
              a[(i)- 1+(j- 1)*3] = 0.0;
              label13000:;
              //Dummy.label("Lsqrs",13000);
              // C
              // C    DETERMINE STATUS OF REFL FILE
              // C
            }
          }
        }
      }
    }              // Close if()
    xx1 = 0;
    // c!!convert to while loop xx1=0 or goto=200
    {
      forloop203:
      //for (Goto = xx; Goto <= 200; Goto++) 
      while( (xx1==0)||(Goto==200))
        {
          xx1 = 1;
          {
            forloop400:
            for (ii = 1; ii <= iexp; ii++) {
              if (Goto == 400)  
                Goto = 0;
              if (Goto == 0)  {
                expnum = jexp[(ii)- 1];
                lcs = lnblnk(expnam);
              }              // Close if()
              if (ifile == 1 && Goto == 0)  {
                // 
                brefl = expnam.substring((1)-1,lcs)+".peaks";
                // c!!!
                // c        OPEN(UNIT=1,NAME=BREFL,ERR=200,TYPE='OLD')
                unit1 = ropen(brefl);
                if (unit1 == null)  
                  Goto = 200;
                // c        GO TO 2011
                if (Goto == 200)  
                  Goto = 0;
                if (Goto == 0)  {
                  label200:
                  //Dummy.label("Lsqrs",200);
                  System.out.println((brefl) + "  NULL   does not exist." );
                  System.out.println(" Input complete reflection filename: " );
                  // c,$)
                  brefl =readans();
                  /*_f2j_in.readChars(40); REMOVE
                    _f2j_in.skipRemaining();*/
                  // c!!!        OPEN(UNIT=1,NAME=BREFL,ERR=200,TYPE='OLD')
                  unit1 = ropen(brefl);
                  if (unit1 == null)  
                    Goto = 200;
                }              // Close if()
                else  {
                  Goto = 2011;
                }              //  Close else.
              }              // Close if()
              // c!!! make into while loop while goto=2050 or xx=0
              xx = 0;
              {
                forloop2051:
                //for (Goto = 2050; Goto <= xx; Goto++)
                while( (Goto == 2050) || (xx==0) ){
                  xx = 1;
                  if (ifile == 2 && (Goto == 0 || Goto == 2050))  {
                    if (Goto == 0)  
                      brefl = expnam.substring((1)-1,lcs)+".peakint";
                    // c!!!      OPEN(UNIT=1,NAME=BREFL,ERR=2050,TYPE='OLD')
                    if (Goto == 0)  
                      unit1 = ropen(brefl);
                    if (unit1 == null)  
                      Goto = 2050;
                    if (unit1 != null)  {
                      Goto = 2011;
                    }              // Close if()
                    else  {
                      if (Goto == 2050)  
                        Goto = 0;
                      label2050:
                      //Dummy.label("Lsqrs",2050);
                      System.out.println((brefl) + "  NULL   does not exist.");
                      System.out.println(" Input complete reflection "
                                         +"filename: " );
                      // c!!!       OPEN(UNIT=1,NAME=BREFL,ERR=2050,TYPE='OLD')
                      unit1 = ropen(brefl);
                      if (unit1 == null)  
                        Goto = 2050;
                    }              //  Close else.
                  }              // Close if()
                  if (ifile == 3 && Goto == 0)  {
                    brefl = expnam.substring((1)-1,lcs)+".intscd";
                    // c!!!      OPEN(UNIT=1,NAME=BREFL,ERR=2060,TYPE='OLD')
                    unit1 = ropen(brefl);
                    if (unit1 != null)  {
                      Goto = 2011;
                    }              // Close if()
                    else  {
                      label2060:
                      //Dummy.label("Lsqrs",2060);
                      System.out.println((brefl) + "  NULL   does not exist.");
                      System.out.println(" Input complete reflection "
                                         +"filename: " );
                      // c!!!      OPEN(UNIT=1,NAME=BREFL,ERR=2050,TYPE='OLD')
                      unit1=ropen(brefl);
                      if (unit1==null)  
                        Goto = 2050;
                    }              //  Close else.
                  }              // Close if()
                  if (ifile == 4 && Goto == 0)  {
                    brefl = expnam.substring((1)-1,lcs)+".integrate";
                    // c!!!      OPEN(UNIT=1,NAME=BREFL,ERR=2070,TYPE='OLD')
                    unit1 = ropen(brefl);
                    if (unit1 != null)  {
                      Goto = 2011;
                    }              // Close if()
                    else  {
                      label2070:
                      //Dummy.label("Lsqrs",2070);
                      System.out.println(brefl + "  NULL   does not exist." );
                      System.out.println(" Input complete reflection "
                                         +"filename: " );
                      // c!!!         OPEN(UNIT=1,NAME=BREFL,ERR=2050,TYPE='OLD')
                      unit1 = ropen(brefl);
                      if (unit1 == null)  
                        Goto = 2050;
                    }              //  Close else.
                  }              // Close if()
                  //Dummy.label("Lsqrs",2051);
                }              //  Close for() loop. 
              }
              // C
              // C   PERFORM LS ON LATTICE PARAMETERS AND O. M.
              // C
              // C
              if (Goto == 2011)  
                Goto = 0;
              if (Goto == 0)  {
                label2011:
                //Dummy.label("Lsqrs",2011);
                System.out.println("\n"  + " Do you want to select "
                                   +"individual SEQNUMs (Y,<N>)? " );
                // c,$)
                yn = "N";
                answr=readans();
                if(answr!=null && answr.length()>0)
                  yn=answr.substring(0,1);
                /*nchrs=answr.length(); REMOVE
                  if (nchrs > 0)  
                  yn = _f2j_in.readChars(1);
                  _f2j_in.skipRemaining();*/
                // 
                mseq = 0;
                jnseq = 0;
              }              // Close if()
              if (Goto == 0 && (yn.trim().equalsIgnoreCase("Y".trim())
                                   || yn.trim().equalsIgnoreCase("y".trim()))){
                // 
                mseq = 1;
                System.out.println("\n SEQNUMs from <1> terminal or (2) file"
                                   +" seqnum.dat?: " );
                // c,$)
                isin = 1;
                answr=readans();
                if(answr!=null && answr.length()>0){
                  try{
                    isin=Integer.parseInt(answr.trim());
                  }catch(NumberFormatException e){
                    // let it drop on the floor
                  }
                }
                /*nchrs=answr.length(); REMOVE
                  if (nchrs > 0)  
                  isin = _f2j_in.readInt();
                  _f2j_in.skipRemaining();*/
                // c!!! IF(ISIN.EQ.2)OPEN(UNIT=15,TYPE='OLD',FILE='SEQNUM.DAT')
                if (isin == 2)  
                  unit15 = ropen("SEQNUM.DAT");
                // 
              }              // Close if()
              // c make into a while loop and goto.eq.0 here
              {
                forloop202:
                for (Goto = 201; Goto <= 13601; Goto++) {
                  label201:
                  //Dummy.label("Lsqrs",201);
                  if (Goto == 201)  
                    Goto = 0;
                  if (mseq == 1 && Goto == 0)  {
                    // 
                    // c!!!      REWIND 1
                    unit1 = rewind(unit1);
                    // 
                    if (isin == 1)  {
                      System.out.println("$Enter SEQNUM (terminate with "
                                         +"<0>): ");
                      // c,$)
                      mnseq = 0;
                      answr=readans();
                      if(answr!=null && answr.length()>0){
                        try{
                          mnseq=Integer.parseInt(answr.trim());
                        }catch(NumberFormatException e){
                          // let it drop on the floor
                        }
                      }
                      /*nchrs=answr.length(); REMOVE
                        if (nchrs > 0)  
                        mnseq = _f2j_in.readInt();
                        _f2j_in.skipRemaining();*/
                    }              // Close if()
                    // c!!!      IF (ISIN.EQ.2.and.not.eof) READ (15,*) MNSEQ 
                    if( isin==2) 
                      try{mnseq=unit15.readInt();
                      }catch(Exception s){Goto=400;}
                    if (eof(unit15))  
                      Goto = 400;
                    // c READ (15,*,END=400) MNSEQ 
                    if( Goto==0) 
                      {  try{
                        mnseq = unit15.readInt();
                      }
                      catch( Exception s){Goto=400;}
                      if (eof(unit15))  
                        Goto=400;
                      }
                    if ((mnseq == 0)&&(Goto==0))  {
                      Goto = 400;
                    }              // Close if()
                    else  {
                      jnseq = jnseq+1;
                      nseq[(jnseq)- 1] = mnseq;
                    }              //  Close else.
                  }              // Close if()
                  if (Goto == 13601)  
                    Goto = 0;
                  if (Goto == 0)  {
                    // c!!! mispelled ReadRefl
                    label13601:
                    //Dummy.label("Lsqrs",13601);
                    redrefl(1,ieof);
                    if (ieof == 1)  
                      Goto = 400;
                    if (Goto == 0)  {
                      hstnum = nrun-nrun1+1;
                      // 
                      if (ifile == 1)  {
                        if (reflag != ixln)  
                          Goto = 13601;
                      }              // Close if()
                    }              // Close if()
                    if (mseq == 1 && Goto == 0)  {
                      if (seqnum == mnseq)  
                        Goto = 13602;
                      if (Goto == 0)  
                        Goto = 13601;
                    }              // Close if()
                    // 
                    if (mhst == 1 && Goto == 0)  {
                      // c!!!   make into a while loop
                      {
                        forloop13607:
                        for (i = 1; (i <= numh)&&(Goto==0); i++) {
                          if (hstnum == nhst[(i)- 1] && Goto == 0)  
                            Goto = 13595;
                          //Dummy.label("Lsqrs",13607);
                        }              //  Close for() loop. 
                      }
                      if (Goto == 0)  
                        Goto = 13601;
                    }              // Close if()
                  }              // Close if()
                  label13595:
                  //Dummy.label("Lsqrs",13595);
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
                  }              // Close if()
                  if (Goto == 13602)  
                    Goto = 0;
                  if (Goto == 0)  {
                    label13602:
                    //Dummy.label("Lsqrs",13602);
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
                    v[(1)- 1] = h;
                    v[(2)- 1] = k;
                    v[(3)- 1] = l;
                    v[(4)- 1] = diffxx;
                    v[(5)- 1] = diffyy;
                    v[(6)- 1] = diffzz;
                    // 
                    {
                      forloop15000:
                      for (j = 1; j <= 6; j++) {
                        {
                          forloop14000:
                          for (i = 1; i <= 3; i++) {
                            a[(i)- 1+(j- 1)*3] = a[(i)- 1+(j- 1)*3]
                              +v[(i)- 1]*v[(j)- 1];
                            //Dummy.label("Lsqrs",14000);
                          }              //  Close for() loop. 
                        }
                        //Dummy.label("Lsqrs",15000);
                      }              //  Close for() loop. 
                    }
                    Goto = 201;
                  }              // Close if()
                  // c     if goto=201 loop while      
                  //Dummy.label("Lsqrs",202);
                }              //  Close for() loop. 
              }
              if (Goto == 400)  
                Goto = 0;
              //Dummy.label("Lsqrs",400);
            }              //  Close for() loop. 
          }
          // 
          // 
          // C
          // C     SAVE THE LEFT HAND SIDE AS THE VARIENCE-COVARIENCE MATRIX
          // C
          label16001:
          //Dummy.label("Lsqrs",16001);
          {
            forloop18000:
            for (j = 1; j <= 3; j++) {
              {
                forloop17000:
                for (i = 1; i <= 3; i++) {
                  vc[(i)- 1+(j- 1)*3] = a[(i)- 1+(j- 1)*3];
                  //Dummy.label("Lsqrs",17000);
                }              //  Close for() loop. 
              }
              //Dummy.label("Lsqrs",18000);
            }              //  Close for() loop. 
          }
          // C
          // C     SOLVE THE NORMAL EQUATIONS AND THE THREE RIGHT HAND SIDES
          // C
          {
            forloop22000:
            for (i = 1; i <= 3; i++) {
              piv = a[(i)- 1+(i- 1)*3];
              if (Math.abs(piv) < 0.00001)  
                unit16.println(" PIVOT ELEMENT IS SMALL: LEAST SQUARES "
                               +"INACCURATE" );
              if (Math.abs(piv) < 0.00001)  
                piv = 0.00001;
              {
                forloop19000:
                for (j = 1; j <= 6; j++) {
                  label19000:
                  //Dummy.label("Lsqrs",19000);
                  a[(i)- 1+(j- 1)*3] = a[(i)- 1+(j- 1)*3]/piv;
                }
              }
              {
                forloop21000:
                for (j = 1; j <= 3; j++) {
                  if (j == i)  
                    Goto = 21000;
                  if (Goto == 0)  {
                    ql = a[(j)- 1+(i- 1)*3];
                    {
                      forloop20000:
                      for (m = 1; m <= 6; m++) {
                        a[(j)- 1+(m- 1)*3] = a[(j)- 1+(m- 1)*3]-ql*a[(i)- 1+(m- 1)*3];
                        //Dummy.label("Lsqrs",20000);
                      }              //  Close for() loop. 
                    }
                  }              // Close if()
                  if (Goto == 21000)  
                    Goto = 0;
                  //Dummy.label("Lsqrs",21000);
                }              //  Close for() loop. 
              }
            }
          }

          label22000:
          //Dummy.label("Lsqrs",22000);
          {
            forloop23000:
            for (j = 1; j <= 3; j++) {
              {
                forloop23001:
                for (i = 1; i <= 3; i++) {
                  orgmat[(j)- 1+(i- 1)*3] = a[(i)- 1+(j+3- 1)*3];
                  //Dummy.label("Lsqrs",23001);
                }              //  Close for() loop. 
              }
              //Dummy.label("Lsqrs",23000);
            }              //  Close for() loop. 
          }
          // C
          // C     TRANSPOSETO OBTAIN THE ORIENTATION MATRIX
          // C
          {
            forloop23002:
            for (j = 1; j <= 3; j++) {
              {
                forloop23003:
                for (i = 1; i <= 3; i++) {
                  oi[(i)- 1+(j- 1)*3] = orgmat[(j)- 1+(i- 1)*3];
                  //Dummy.label("Lsqrs",23003);
                }              //  Close for() loop. 
              }
              //Dummy.label("Lsqrs",23002);
            }              //  Close for() loop. 
          }
          // C     STORE ORGMAT IN OI AND INVERT
          // C
          {
            forloop24000:
            for (j = 1; j <= 3; j++) {
              {
                forloop24001:
                for (i = 1; i <= 3; i++) {
                  orgmat[(i)- 1+(j- 1)*3] = oi[(i)- 1+(j- 1)*3];
                  //Dummy.label("Lsqrs",24001);
                }              //  Close for() loop. 
              }
              //Dummy.label("Lsqrs",24000);
            }              //  Close for() loop. 
          }
          matinv(oi);
          abc(orgmat,A,B,C,Alpha,Beta,Gamma,Vol);
          b[(1)- 1]=A.val;b[(2)- 1]=B.val;b[(3)- 1]=C.val;b[(4)- 1]=Alpha.val;
          b[(5)- 1]=Beta.val;b[(6)- 1]=Gamma.val;b[(7)- 1]=Vol.val;
          System.out.println("\n\n LATTICE PARAMETERS:\n"  + (b) + "  NULL  ");
          matinv(vc);
          // C
          // C   COMPARE CALCULATED AND OBSERVED VALUES
          // C
          label13700:
          //Dummy.label("Lsqrs",13700);
          line = 0;
          ncntr = 0;
          del = 0.0;
          // c!!!      REWIND 1
          unit1 = rewind(unit1);
          curhst = 0;
          delhst = 0.0;
          nref = 0;
          // 
          // c!! make while loop goto==200 do not go through
          {
            forloop500:
            for (ii = 1; (ii <= iexp) &&(Goto !=200); ii++) {
              if (Goto == 500)  
                Goto = 0;
              expnum = jexp[(ii)- 1];
              // 
              if (ifile == 2)  {
                // c!!!      OPEN(UNIT=1,NAME=BREFL,ERR=500,TYPE='OLD')
                unit1 = ropen(brefl);
                if (unit1 == null)  
                  Goto = 500;
              }              // Close if()
              else  {
                // c      CALL REFFIL(EXPNUM,BREFL,IFILE)
                // c!!!      OPEN(UNIT=1,NAME=BREFL,ERR=200,TYPE='OLD')
                unit1 =ropen(brefl);
                if (unit1 == null)  
                  Goto = 200;
              }              //  Close else.
              // c!! make a while loop
              {
                forloop499:
                for (Goto = 1; Goto <= 30000; Goto++) {
                  if (Goto == 30000)  
                    Goto = 0;
                  if (Goto == 0)  {
                    // c!!! rename ReadRefl
                    label30000:
                    //Dummy.label("Lsqrs",30000);
                    redrefl(1,ieof);
                    // 
                    if (eof(unit1))  {
                      Goto = 500;
                    }              // Close if()
                    else  {
                      hstnum = nrun-nrun1+1;
                      // 
                      // c           HSTNUM = MOD(HSTNUM,100)
                      // 
                      if (ifile == 1 && reflag != ixln)  
                        Goto = 30000;
                      // 
                      if (wl < wlmin || wl > wlmax && Goto == 0)  
                        Goto = 30000;
                      // 
                      if (mseq == 1 && Goto == 0)  {
                        {
                          forloop172:
                          for (ja = 1; ja <= jnseq; ja++) {
                            if (seqnum == nseq[(ja)- 1])  
                              Goto = 30111;
                            //Dummy.label("Lsqrs",172);
                          }              //  Close for() loop. 
                        }
                        if (Goto == 0)  
                          Goto = 30000;
                      }
                    }              // Close if()
                  }              //  Close else.
                  // C          if( goto.eq.0)IHST=MOD(HSTNUM,100)
                  // 
                  if (mhst == 1 && Goto == 0)  {
                    {
                      forloop173:
                      for (jh = 1; jh <= numh; jh++) {
                        if (hstnum == nhst[(jh)- 1])  
                          Goto = 30111;
                        //Dummy.label("Lsqrs",173);
                      }              //  Close for() loop. 
                    }
                    if (Goto == 0)  
                      Goto = 30000;
                  }              // Close if()
                  // Close if()!XXX dropped
                  if (Goto == 30111)  
                    Goto = 0;
                  if (Goto == 0)  {
                    label30111:
                    //Dummy.label("Lsqrs",30111);
                    ip2 = 0;
                    // C      IF(IHST.GT.JHMAX(II).OR.IHST.LT.JHMIN(II)) GO TO 30000
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
                    }              // Close if()
                    if (Goto == 0)  {
                      curhst = hstnum;
                      // 
                      // c        CALL KEYWRT (45,3,0,0)
                      gettof(iuexp);
                      // 
                      if (nref == 0)  
                        Goto = 13800;
                    }              // Close if()
                    if (Goto == 0)  {
                      delhst = delhst/nref;
                      if (ip != 2)  
                        System.out.println(" DELHST = "  + (delhst) + " " );
                      if (ip != 1)  
                        unit16.println("original format did not work"
                                       + (delhst) + " " );
                      delhst = 0.0;
                      nref = 0;
                    }              // Close if()
                  }              // Close if()
                  // C
                  // C
                  label13800:
                  //Dummy.label("Lsqrs",13800);
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
                  }              // Close if()
                  if (Goto == 13501)  
                    Goto = 0;
                  if (Goto == 0)  {
                    label13501:
                    //Dummy.label("Lsqrs",13501);
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
                      System.out.println("  " + (seqnum) + " "  + (h) + " "
                                         + (k) + " "  + (l) + " "  + (x) + " "
                                         + (y) + " "  + (z) + " " );
                    if (ip != 1)  
                      unit16.println("  " + (seqnum) + " "  + (h) + " "  + (k)
                                     + " "  + (l) + " "  + (x) + " "  + (y)
                                     + " "  + (z) + " " );
                    // C
                    // C     CALCULATE THE DIFFRACTION VECTOR COSINES FROM THE MATIX
                    // C     AND THE HKL VECTOR
                    // C
                    {
                      forloop27001:
                      for (i = 1; i <= 3; i++) {
                        {
                          forloop27003:
                          for (j = 1; j <= 3; j++) {
                            curmat[(i)- 1+(j- 1)*3] = orgmat[(i)- 1+(j- 1)*3];
                            //Dummy.label("Lsqrs",27003);
                          }              //  Close for() loop. 
                        }
                        //Dummy.label("Lsqrs",27001);
                      }              //  Close for() loop. 
                    }
                    newrot(curmat,chi,phi,omega);
                    {
                      forloop27000:
                      for (j = 1; j <= 3; j++) {
                        v[(j+9)- 1] = curmat[(1)- 1+(j- 1)*3]*h+curmat[(2)- 1+(j- 1)*3]*k+curmat[(3)- 1+(j- 1)*3]*l;
                        //Dummy.label("Lsqrs",27000);
                      }              //  Close for() loop. 
                    }
                    {
                      forloop27002:
                      for (j = 1; j <= 3; j++) {
                        dv[(j+9)- 1] = orgmat[(1)- 1+(j- 1)*3]*h+orgmat[(2)- 1+(j- 1)*3]*k+orgmat[(3)- 1+(j- 1)*3]*l;
                        //Dummy.label("Lsqrs",27002);
                      }              //  Close for() loop. 
                    }
                    // C
                    // C     CALCULATE THE XCM,YCM,WL VALUES
                    // C    USE HAMILTONS CONVENTION (COORDINATE SYSTEM)
                    // C
                    hh = (int)(Math.pow(v[(10)- 1], 2)+Math.pow(v[(11)- 1], 2)+Math.pow(v[(12)- 1], 2));
                    wl = 2.0*v[(10)- 1]/hh;
                    // C
                    // C CALCULATE XCM AND YCM DET COODINATES, BUT FIRST
                    // C TRANSLATE ORIGIN FROM RECIPROCAL LATTICE ORIGIN
                    // C TO THE CRYSTAL ORIGIN
                    // C
                    xdp = v[(10)- 1]-(1.0/wl);
                    // C
                    // C  ROTATE TO A DETECTOR ANGKLE OF ZERO
                    // C
                    ang = -deta/rad;
                    xt = xdp;
                    yt = v[(11)- 1];
                    xdp = xt*Math.cos(ang)+yt*Math.sin(ang);
                    ydp = -xt*Math.sin(ang)+yt*Math.cos(ang);
                    zdp = v[(12)- 1];
                    // C
                    // C  CALCULATE XCM AND YCM
                    // C
                    xcm = -(ydp/xdp)*detd;
                    ycm = -(zdp/xdp)*detd;
                    // 
                    // C  CALCULATE CHANNEL NUMBERS X,Y,Z
                    // 
                    xcalc = ((xcm-xleft)/x2cm)+0.5;
                    ycalc = ((ycm-ylower)/y2cm)+0.5;
                    xl = l1+Math.sqrt(Math.pow(detd, 2)+Math.pow(xcm, 2)+Math.pow(ycm, 2));
                    if (wl < wlmin)  
                      Goto = 1001;
                    if (wl > wlmax)  
                      Goto = 1001;
                  }              // Close if()
                  if (Goto == 0)  {
                    tobs = wl*(xl/hom)-tzero;
                    // 
                    ntime[(wlnum+1)- 1] = tmax;
                    kkk = -1;
                    {
                      forloop1100:
                      for (i = 1; i <= (wlnum+1); i++) {
                        if (tobs < ntime[(i)- 1] && kkk == -1)  {
                          kkk = i;
                        }              // Close if()
                        //Dummy.label("Lsqrs",1100);
                      }              //  Close for() loop. 
                    }
                    i = kkk;
                    label1101:
                    //Dummy.label("Lsqrs",1101);
                    delta = (double)(ntime[(i)- 1]-ntime[(i-1)- 1]);
                    zcalc = i-1.0+(tobs-ntime[(i-1)- 1])/delta;
                    Goto = 1002;
                  }              // Close if()
                  label1001:
                  //Dummy.label("Lsqrs",1001);
                  if (Goto == 1001)  
                    Goto = 0;
                  if (Goto == 0)  
                    z = 0.0;
                  if (Goto == 1002)  
                    Goto = 0;
                  if (Goto == 0)  {
                    label1002:
                    //Dummy.label("Lsqrs",1002);
                    fit = "       ";
                    delx = Math.abs(x-xcalc);
                    dely = Math.abs(y-ycalc);
                    delz = Math.abs(z-zcalc);
                    if (delx > 2. || dely > 2. || delz > 2.)  
                      fit = "BAD FIT";
                    // 
                    if (ip != 2)  
                      System.out.println("   " + (astar) + "               "
                                         + (xcalc) + " "  + (ycalc) + " "
                                         + (zcalc) + "   " + (xcm) + " " );
                    if (ip != 1)  
                      unit16.println("   " + (astar) + "               "
                                     + (xcalc) + " "  + (ycalc) + " "
                                     + (zcalc) + "   " + (xcm) + " " );
                    // 
                    // C     CALCULATE INDICES
                    h = oi[(1)- 1+(1- 1)*3]*diffxx+oi[(2)- 1+(1- 1)*3]*diffyy+oi[(3)- 1+(1- 1)*3]*diffzz;
                    k = oi[(1)- 1+(2- 1)*3]*diffxx+oi[(2)- 1+(2- 1)*3]*diffyy+oi[(3)- 1+(2- 1)*3]*diffzz;
                    l = oi[(1)- 1+(3- 1)*3]*diffxx+oi[(2)- 1+(3- 1)*3]*diffyy+oi[(3)- 1+(3- 1)*3]*diffzz;
                    // 
                    delhkl = Math.abs(h-ih)+Math.abs(k-ik)+Math.abs(l-il);
                    if (delhkl < 0.1)  {
                      if (ip != 2)  
                        System.out.println("     " + (h) + "      DELHKL = "
                                           + (k) + "     " + (l) + " " );
                      if (ip != 1)  
                        unit16.println("     " + (h) + "      DELHKL = "  + (k)
                                       + "     " + (l) + " " );
                      Goto = 490;
                    }              // Close if()
                    if (delhkl < 0.2 && Goto == 0)  {
                      if (ip != 2)  
                        System.out.println("     " + (h) + "      DELHKL = "
                                           + (k) + "     " + (l) + " " );
                      if (ip != 1)  
                        unit16.println("     " + (h) + "      DELHKL = " + (k)
                                       + "     " + (l) + " " );
                      Goto = 490;
                    }              // Close if()
                    if (delhkl < 0.3 && Goto == 0)  {
                      if (ip != 2)  
                        System.out.println("     " + (h) + "      DELHKL = "  
                                           + (k) + "     " + (l) + " " );
                      if (ip != 1)  
                        unit16.println("     " + (h) + "      DELHKL = "  + (k)
                                       + "     " + (l) + " " );
                      Goto = 490;
                    }              // Close if()
                  }              // Close if()
                  // C      DELHKL .GE. 0.3
                  if (Goto == 0)  {
                    if (ip != 2)  
                      System.out.println("     " + (h) + "      DELHKL = "  
                                         + (k) + "     " + (l) + " " );
                    if (ip != 1)  
                      unit16.println("     " + (h) + "      DELHKL = "  + (k) 
                                     + "     " + (l) + " " );
                  }              // Close if()
                  if (Goto == 490)  
                    Goto = 0;
                  if (Goto == 0)  {
                    label490:
                    //Dummy.label("Lsqrs",490);
                    ncntr = ncntr+1;
                    del = Math.pow((diffxx-dv[(10)- 1]), 2)+Math.pow((diffyy-dv[(11)- 1]), 2)+Math.pow((diffzz-dv[(12)- 1]), 2)+del;
                    delhst = delhst+delhkl;
                    nref = nref+1;
                    Goto = 30000;
                  }              // Close if()
                  if (Goto == 500)  
                    Goto = 0;
                  // c do 499 is for label 30000
                }
              }
              //Dummy.label("Lsqrs",499);
            }              //  Close for() loop. 
          }
          //Dummy.label("Lsqrs",500);
        }              //  Close for() loop. 
    }
    // c for goto 200
    label203:
    //Dummy.label("Lsqrs",203);
    //TO TEST
    // C
    // C     BEGIN FINAL SUMMARY AND TYPEOUT
    // C
    if (Goto != 0)  
      return;// Dummy.go_to("Lsqrs",999999);
    label30001:
    //Dummy.label("Lsqrs",30001);
    delhst = delhst/nref;
    if (ip != 2)  
      System.out.println(" DELHST = "  + (delhst) + " " );
    if (ip != 1)  
      unit16.println("original format did not work"  + (delhst) + " " );
    {
      forloop31000:
      for (i = 1; i <= 7; i++) {
        sig[(i)- 1] = 0.0;
        celsca[(i)- 1] = b[(i)- 1];
        //Dummy.label("Lsqrs",31000);
      }              //  Close for() loop. 
    }
    {
      forloop37000:
      for (ii = 1; ii <= 3; ii++) {
        {
          forloop33000:
          for (jj = 1; jj <= 3; jj++) {
            orgmat[(jj)- 1+(ii- 1)*3] = orgmat[(jj)- 1+(ii- 1)*3]+small;
            abc(orgmat,A,B,C,Alpha,Beta,Gamma,Vol);
            b[(1)- 1]=A.val;
            b[(2)- 1]=B.val;
            b[(3)- 1]=C.val;
            b[(4)- 1]=Alpha.val;
            b[(5)- 1]=Beta.val;
            b[(6)- 1]=Gamma.val;
            b[(7)- 1]=Vol.val;
            orgmat[(jj)- 1+(ii- 1)*3] = orgmat[(jj)- 1+(ii- 1)*3]-small;
            {
              forloop32000:
              for (kk = 1; kk <= 7; kk++) {
                derv[(jj)- 1+(kk- 1)*3] = (b[(kk)- 1]-celsca[(kk)- 1])/small;
                //Dummy.label("Lsqrs",32000);
              }              //  Close for() loop. 
            }
            //Dummy.label("Lsqrs",33000);
          }              //  Close for() loop. 
        }
        // C     ACCUMULATE SIGMAS
        {
          forloop36000:
          for (m = 1; m <= 7; m++) {
            {
              forloop35000:
              for (i = 1; i <= 3; i++) {
                {
                  forloop34000:
                  for (j = 1; j <= 3; j++) {
                    sig[(m)- 1] = sig[(m)- 1]+derv[(i)- 1+(m- 1)*3]*vc[(i)- 1+(j- 1)*3]*derv[(j)- 1+(m- 1)*3];
                    //Dummy.label("Lsqrs",34000);
                  }              //  Close for() loop. 
                }
                //Dummy.label("Lsqrs",35000);
              }              //  Close for() loop. 
            }
            //Dummy.label("Lsqrs",36000);
          }              //  Close for() loop. 
        }
        //Dummy.label("Lsqrs",37000);
      }              //  Close for() loop. 
    }
    // C     CALCULATE THE NUMBER OF DEGREES OF FREEDOM
    fi = 3.0*(ncntr-3);
    if (fi < 0.0)  
      System.out.println(" INSUFFICIENT DATA FOR STANDARD DEVIATIONS" );
    if (fi < 0.0)  
      System.exit(1);
    del = del/fi;
    {
      forloop38000:
      for (i = 1; i <= 7; i++) {
        sig[(i)- 1] = Math.sqrt(del*sig[(i)- 1]);
        //Dummy.label("Lsqrs",38000);
      }              //  Close for() loop. 
    }
    label39001:
    //Dummy.label("Lsqrs",39001);
    System.out.println("\n\n LEAST SQUARES SUMMARY:\n" );
    // c!!!      WRITE (*, 41000)((ORGMAT(I,J),J=1,3),I=1,3)
    for( i=1;i<=3;i++)
      {for( j=1;j<=3;j++)
        System.out.print( orgmat[i-1+(j-1)*3]);
      System.out.println("");
      }
    System.out.println("\n\n LATTICE PARAMETERS:\n"  + (b) + "  NULL  " );
    System.out.println(" LATTICE PARAMETER STANDARD DEVIATIONS:\n"  + (sig) 
                       + "  NULL  " );
    System.out.println("\n\n MINCNT"+mincnt+"     TOTAL NUMBER OF REFLECTIONS "
                       +ncntr+" ");
    System.out.println(" WLMIN, WLMAX"  + (wlmin) + " " );
    if (ip == 1)  
      Goto = 39010;
    if (Goto == 0)  {
      unit16.println("1LEAST SQUARES SUMMARY:\n\n\n" );
      // c!!!      WRITE (16,41000) ((ORGMAT(I,J),J=1,3),I=1,3)
      for( i=1;i<=3;i++)
        {for( j=1;j<=3;j++)
          unit16.print( orgmat[i-1+(j-1)*3]);
        unit16.println("");
        }
      unit16.println("\n\n LATTICE PARAMETERS:\n"  + (b) + "  NULL  " );
      unit16.println(" LATTICE PARAMETER STANDARD DEVIATIONS:\n"  + (sig) 
                     + "  NULL  " );
      unit16.println("\n\n MINCNT"  + (mincnt) + "     TOTAL NUMBER OF "
                     +"REFLECTIONS "  + (ncntr) + " " );
      unit16.println(" WLMIN, WLMAX"  + (wlmin) + " " );
      // C
      // C     DOES THE USER WANT TO SAVE THE MATRIX AND CELL PARAMETERS?
      // C
    }              // Close if()
    if (Goto == 39010)  
      Goto = 0;
    if (Goto == 0)  {
      label39010:
      //Dummy.label("Lsqrs",39010);
      System.out.println();
      // c60000 FORMAT (/,
      // c     1 Store matrix and cell parameters in a matrix file ')
      // c     2'(Y,<N>)? ')
      // c,$)
      ans = "N";
      answr=readans();
      if(answr!=null && answr.length()>0)
        ans=answr.trim().substring(0,1);
      /*nchrs=answr.length(); REMOVE
        if (nchrs > 0)  
        ans = _f2j_in.readChars(1);
        _f2j_in.skipRemaining();*/
      // 
      if (ans.trim().equalsIgnoreCase("Y".trim()) || ans.trim().equalsIgnoreCase("y".trim()))  {
        label375:
        //Dummy.label("Lsqrs",375);
        System.out.println(" ENTER filename.filetype: " );
        // c,$)
        fname=readans().trim();
        /*fname = _f2j_in.readChars(40); REMOVE
          _f2j_in.skipRemaining();*/
        // c!!!      OPEN(UNIT=4,NAME=FNAME,FORM='FORMATTED',TYPE='UNKNOWN')
        unit4 = wopen(fname);
        // c!!!      WRITE (4,61000) ((ORGMAT(I,J),J=1,3),I=1,3)
        for( i=1;i<=3;i++)
          {for( j=1;j<=3;j++)
            unit4.print( orgmat[i-1+(j-1)*3]);
          unit4.println("");
          }
        unit4.print("");
        for(i = 1; i <= 7; i++)
          unit4.print(b[(i)- 1] + " ");

        unit4.println();
        unit4.println((sig) + " "  + " NULL " + " " );
      }              // Close if()
    }              // Close if()
    // 
    label380:
    //Dummy.label("Lsqrs",380);
    System.out.println("\n Save matrix and cell parameters in the expnam.x "
                       + "file (Y,<N>)? " );
    // c,$)
    ans = "N";
    answr=readans();
    nchrs=answr.length();
    // c!!!      IF (NCHRS.GT.0) READ(ANSWR,'(A)') ANS
    // 
    if (ans.trim().equalsIgnoreCase("Y".trim()) || ans.trim().equalsIgnoreCase("y".trim()))  {
      // 
      //putunitcell(iuexp);
      //!!Sortx.sortx(iuexp);
      return;//Dummy.go_to("Lsqrs",999999);
      // 
    }              // Close if()
    // 
    // C
    // C           FORMAT STATEMENTS
    // C
    // c46100 FORMAT('0DELHST = ( SUM ( ABS(H-HCALC) + ABS(K-KCALC) + ABS(L-LCA
    // c     1LC) ) ) / (NO. OF REFLS)'/,'        = ',F5.2)
    // 
    label90000:
    //Dummy.label("Lsqrs",90000);
    System.exit(1);
    //Dummy.label("Lsqrs",999999);
    return;

  }

  public static String readans()
  {
    char c=0;
    String Res="";
    try{
      c =(char) System.in.read();
      while( c >=32)
        {
          Res+=c;
          c =(char) System.in.read();
        }

    }
    catch(Exception ss)
      {return Res;}
    return Res;
  }

  public static minputStream ropen( String filename)
  {try{
    return new minputStream( ( filename));
  }
  catch( Exception s){return null;}

  }

  public static PrintStream wopen( String filename)
  {
    try{
      return new PrintStream( new FileOutputStream( filename));
    }
    catch( Exception s){return null;}

  }

  public static minputStream rewind( minputStream dinput)
  {try{String filename= dinput.filename;
  dinput.close();
  return ropen( filename);
  }catch(Exception ss){return null;}
  }

  static class minputStream extends DataInputStream
  {  String filename;

    public minputStream( String filename) throws IOException
    {super( new FileInputStream(filename));
    this.filename=filename;
    }
  }

  public static void abc (double [] u,
                          doubleW a,
                          doubleW b,
                          doubleW c,
                          doubleW alpha,
                          doubleW beta,
                          doubleW gamma,
                          doubleW vol)  {
    double [] ut= new double[(3) * (3)];
    double [] g= new double[(3) * (3)];
    double [] gi= new double[(3) * (3)];
    double rad = 57.295779513;
    int i= 0;
    int j= 0;
    double arg1= 0.0;
    double arg2= 0.0;
    double arg3= 0.0;
    double cstar= 0.0;
    int _u_offset=0;

    {
      forloop600:
      for (i = 1; i <= 3; i++) {
        {
          forloop601:
          for (j = 1; j <= 3; j++) {
            ut[(i)- 1+(j- 1)*3] = u[(j)- 1+(i- 1)*3+ _u_offset];
            //Dummy.label("Abc",601);
          }              //  Close for() loop. 
        }
        //Dummy.label("Abc",600);
      }              //  Close for() loop. 
    }
    matmul(u,ut,gi);
    {
      forloop610:
      for (i = 1; i <= 3; i++) {
        {
          forloop611:
          for (j = 1; j <= 3; j++) {
            g[(i)- 1+(j- 1)*3] = gi[(i)- 1+(j- 1)*3];
            //Dummy.label("Abc",611);
          }              //  Close for() loop. 
        }
        //Dummy.label("Abc",610);
      }              //  Close for() loop. 
    }
    matinv(g);
    a.val = Math.sqrt(g[(1)- 1+(1- 1)*3]);
    b.val = Math.sqrt(g[(2)- 1+(2- 1)*3]);
    c.val = Math.sqrt(g[(3)- 1+(3- 1)*3]);
    arg1 = g[(2)- 1+(3- 1)*3]/(b.val*c.val);
    arg2 = g[(1)- 1+(3- 1)*3]/(a.val*c.val);
    arg3 = g[(1)- 1+(2- 1)*3]/(a.val*b.val);
    // c accos changed to cos to get thru f2java next 3 lines
    alpha.val = (Math.acos(arg1))*rad;
    beta.val = (Math.acos(arg2))*rad;
    gamma.val = (Math.acos(arg3))*rad;
    // 
    // C***  CALCULATE VOLUME
    // 
    cstar = Math.sqrt(gi[(3)- 1+(3- 1)*3]);
    vol.val = a.val*b.val*Math.sin(gamma.val/rad)*(1.0/cstar);
    // 
    //Dummy.go_to("Abc",999999);
    //Dummy.label("Abc",999999);
    return;
  }
  public static void matmul( double[]A, double[]B, double[]C)
  {    

    for( int i=1;i<=3;i++)
      for( int j=1;j<=3;j++)
        C[j-1+3*(i-1)]=0;

    for( int i=1;i<=3;i++)
      for( int j=1;j<=3;j++)      
        for(int k=1;k<=3;k++)
          C[j-1+(i-1)*3]+=A[k-1+(i-1)*3]*B[j-1+3*(i-1)];


  }
  public static void matinv (double [] matrix) 

  {
    double [] a= new double[(3) * (3)];
    double [] ai= new double[(3) * (3)];
    double d= 0.0;
    int i= 0;
    int j= 0;
    int k= 0;
    int m= 0;
    int n= 0;
    int l= 0;
    int _matrix_offset=0;
    forloop11:
    {
      for (i = 1; i <= 3; i++) {
        {
          forloop10:
          for (j = 1; j <= 3; j++) {
            a[(i)- 1+(j- 1)*3] = (matrix[(i)- 1+(j- 1)*3+ _matrix_offset]);
            //Dummy.label("Matinv",10);
          }              //  Close for() loop. 
        }
        //Dummy.label("Matinv",11);
      }              //  Close for() loop. 
    }
    i = 0;
    {
      forloop2:
      for (j = 1; j <= 3; j++) {
        m = j+1;
        if (m > 3)  
          m = m-3;
        n = j+2;
        if (n > 3)  
          n = n-3;
        {
          forloop1:
          for (i = 1; i <= 3; i++) {
            k = i+1;
            if (k > 3)  
              k = k-3;
            l = i+2;
            if (l > 3)  
              l = l-3;
            ai[(i)- 1+(j- 1)*3] = a[(m)- 1+(k- 1)*3]*a[(n)- 1+(l- 1)*3]
              -a[(m)- 1+(l- 1)*3]*a[(n)- 1+(k- 1)*3];
            //Dummy.label("Matinv",1);
          }              //  Close for() loop. 
        }
        //Dummy.label("Matinv",2);
      }              //  Close for() loop. 
    }
    d = ai[(1)- 1+(1- 1)*3]*a[(1)- 1+(1- 1)*3]
      +ai[(2)- 1+(1- 1)*3]*a[(1)- 1+(2- 1)*3]
      +a[(1)- 1+(3- 1)*3]*ai[(3)- 1+(1- 1)*3];
    if (Math.abs(d) < 0.00000001)  
      System.out.println(" INVERSION OF MATRIX IS INDETERMINANT" );
    {
      forloop4:
      for (i = 1; i <= 3; i++) {
        {
          forloop3:
          for (j = 1; j <= 3; j++) {
            ai[(i)- 1+(j- 1)*3] = ai[(i)- 1+(j- 1)*3]/d;
            matrix[(i)- 1+(j- 1)*3+ _matrix_offset] = (ai[(i)- 1+(j- 1)*3]);
            //Dummy.label("Matinv",3);
          }              //  Close for() loop. 
        }
        //Dummy.label("Matinv",4);
      }              //  Close for() loop. 
    }

    return;
  }
  public static void newrot (double [] u,
                             double chi,
                             double phi,
                             double om)  {
    int _u_offset=0;
    double [] v= new double[(3) * (3)];
    double [] f= new double[(3) * (3)];
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
    // C
    // C  CONSTRUCT THE ROTATION MATRIX F
    // C
    f[(1)- 1+(1- 1)*3] = cp*co-sp*so*cc;
    f[(1)- 1+(2- 1)*3] = cp*so+sp*co*cc;
    f[(1)- 1+(3- 1)*3] = sp*sc;
    f[(2)- 1+(1- 1)*3] = -sp*co-cp*so*cc;
    f[(2)- 1+(2- 1)*3] = -sp*so+cp*co*cc;
    f[(2)- 1+(3- 1)*3] = cp*sc;
    f[(3)- 1+(1- 1)*3] = sc*so;
    f[(3)- 1+(2- 1)*3] = -sc*co;
    f[(3)- 1+(3- 1)*3] = cc;
    // C
    // C  THE ROTATED MATRIX IS THE PRODUCT OF U X F
    // C
    matmul(u,f,v);
    {
      forloop101:
      for (i = 1; i <= 3; i++) {
        {
          forloop100:
          for (j = 1; j <= 3; j++) {
            label100:
            //Dummy.label("Newrot",100);
            u[(i)- 1+(j- 1)*3+ _u_offset] = v[(i)- 1+(j- 1)*3];
          }
        }
      }
    }

    return;
  }

  static double [] trans = {1. 
                            , 0. , 0. , 0. , 1. , 0. 
                            , 0. , 0. , 1. };
  public static void jindex ()  {
    // EasyIn _f2j_in = new EasyIn(); REMOVE
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

    // 
    // c9	WRITE (*, 10) ((TRANS(I,J),J=1,3),I=1,3)
    Goto=9;
    while(Goto==0){
      Goto=0;
      System.out.println("INDEX TRANFORMATION MATRIX:");
      for( i=1;i<=3;i++)
        {for( j=1;j<=3;j++)
          System.out.print( trans[j-1+(i-1)*3]);
        System.out.println("");
        }
      System.out.println(" Do you want to input a new transformation "  
                         + "matrix (Y,<N>)? " );
      // c,$)
      ans = "N";
      answr=readans();
      if(answr!=null && answr.length()>0)
        ans=answr.trim().substring(0,1);
      /*nchrs=answr.length(); REMOVE
        if (nchrs > 0)  
        ans = _f2j_in.readChars(1);
        _f2j_in.skipRemaining();*/
      if (!ans.trim().equalsIgnoreCase("Y".trim()) && !ans.trim().equalsIgnoreCase("y".trim()))  
        return;
      //      Dummy.go_to("Jindex",999999);
      System.out.println(" INPUT NEW TRANSFORMATION MATRIX" );
      // 
      {
        forloop20:
        for (i = 1; i <= 3; i++) {
          System.out.println(" ROW"  + (i) + " "  + ": " );
          // c,$)
          int index=-1;
          answr=readans().trim();
          for(j = 1; j <= 3; j++){
            trans[i- 1+(j- 1)*3]=Double.parseDouble(answr.substring(0,index));
            answr=answr.substring(index+1).trim();
          }
          /*trans[(i)- 1+(j- 1)*3] = _f2j_in.readDouble(); REMOVE
            _f2j_in.skipRemaining();*/
          //Dummy.label("Jindex",20);
        }              //  Close for() loop. 
      }
      //
      Goto=9; 
    }
  }//end jindex
  // 
  // c	ENTRY JINDEX2(H,K,L)
  public static void jindex2( intW h, intW k, intW l){
    int ih = h.val;
    int ik = k.val;
    int il = l.val;
    h.val = (int)(trans[(1)- 1+(1- 1)*3]*ih+trans[(1)- 1+(2- 1)*3]*ik+trans[(1)- 1+(3- 1)*3]*il);
    k.val = (int)(trans[(2)- 1+(1- 1)*3]*ih+trans[(2)- 1+(2- 1)*3]*ik+trans[(2)- 1+(3- 1)*3]*il);
    l.val = (int)(trans[(3)- 1+(1- 1)*3]*ih+trans[(3)- 1+(2- 1)*3]*ik+trans[(3)- 1+(3- 1)*3]*il);

    return;
  }
  public static void gettof( int iuexp)
  {
    System.out.println("Peter, I did not do this yet. Maybe you already have");
    System.out.println("  to read these files: subroutin gettof");
  }
  public static void redrefl( int i, int ieof)
  {
    System.out.println("Peter, I did not do this yet. Maybe you already have");
    System.out.println("  to read these files: subroutin readrefl");
  }

  public static boolean eof( minputStream f)
  {
    try{
      return f.available() >0;
    }
    catch( Exception ss){return false;}


  }
  public static int lnblnk( String prompt)
  { System.out.println("Peter, I did not do this yet. Maybe you already have");
  System.out.println("  to read these files: subroutin lnblnk");
  return 0;
  }

  public static void getinstcal( int iuexp)
  { System.out.println("Peter, I did not do this yet. Maybe you already have");
  System.out.println("  to read these files: subroutin getinstcal");
  }

  public static int readexp( int iuexp, String key76, String text)
  {System.out.println("Peter, I did not do this yet. Maybe you already have");
  System.out.println("  to read these files: subroutin readexp");
  return 0;

  }
  public static void opnx( int iuexp, String expnam)
  { System.out.println("Peter, I did not do this yet. Maybe you already have");
  System.out.println("  to read these files: subroutin opnx");
  }
} // End class.
