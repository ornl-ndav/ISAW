package Operators.TOF_SCD;
import java.util.*;

import DataSetTools.operator.*;
import Command.JavaCC.Fortran.*;


public class TOFINT implements Wrappable,HiddenOperator{

  public float[][][] JHIST=new float[0][0][0];

  public int NXS=0;

  public int NYS=0;

  public int X=0;

  public int Y=0;

  public int Z=0;

  public int ITOT=0;

  public float SIGITOT=0f;

  public float REFLAG=0f;

  public int WLNUM=0;

  public int MAXX=0;

  public int MAXY=0;

  public int MAXZ=0;

  public int MINX=0;

  public int MINY=0;

  public int MINZ=0;

  public int MAXP1=0;

  public int IPFLAG=0;

  public int IADD=0;

  public int ISX=0;

  public int ISY=0;

  public int ISZ=0;

  public int IPK=0;

  public float[] NTIME=new float[0];

  public Object calculate(){

    float BKCTS=0f;

    float BKTIME=0f;

    int IFLAG=0;

    float INTI1=0f;

    float INTI2=0f;

    float INTI3=0f;

    int SEVEN=MAXZ-MINZ+1;
 
    int[] ISUM= new int[((SEVEN))-1+1];

    int IXCALC=0;

    int IYCALC=0;

    int IZCALC=0;

    int J=0;

    float[] JTIME= new float[((SEVEN))-1+1];

    float PKCTS=0f;

    float PKTIME=0f;

    float RATIO=0f;

    float RIS1=0f;

    float RIS2=0f;

    float RIS3=0f;

    float SIG1=0f;

    float SIG2=0f;

    float SIG3=0f;

    //-------------- code ------------
// JHIST,Nxs,Nys, X,Y,Z,ITOT,SIGITOT,REFLAG,WLNUM,MAXP1,IPFLAG,IADD,ISX,ISY,ISZ
//
//  INTGT. A ROUTINE CALLED BY REFGEN, AFTER REFGEN GENERATES
//  A REFLECTION, INTGT DOES A MINI SEARCH TO FIND THE PEAK MAX
//  (NEAR THE CALCULATED POSITION).
//
//  REFLAG = P*100 + Q*10 + IFLAG
//
//     IFLAG = 0 NORMAL
//           = 1 Peak max shifted
//               
//
//      INCLUDE 'INC:DATACOM.FOR/LIST'
//      INCLUDE 'INTCOM.FOR/LIST'
    IFLAG=(int)((0));
//      X = JNINT(X)
//      Y = JNINT(Y)
//      Z = JNINT(Z)
    IXCALC=(int)((X));
    IYCALC=(int)((Y));
    IZCALC=(int)((Z));
    if(MINZ<1||MAXZ>WLNUM||MINX<1||MAXX>NXS||MINY<1||MAXY>NYS){
      REFLAG=(float)((401));
      return null;
          }
//-------------------------------------------------------------------------------
//                    PROCEED TO INTEGRATE
//-------------------------------------------------------------------------------
    int IX,IY,IZ;
    IPK=(int)((0));
    for(IZ=(int)((MINZ));util.Sign(1)*IZ<=util.Sign(1)*((MAXZ));IZ+=(int)1){
      J=(int)((IZ-MINZ+1));
      JTIME[(int)(((J))-1)]=(float)((NTIME[(int)(((IZ+1))-1)]-NTIME[(int)(((IZ))-
        1)]));
      ISUM[(int)(((J))-1)]=(int)((0));
      for(IY=(int)((MINY));util.Sign(1)*IY<=util.Sign(1)*((MAXY));IY+=(int)1){
        for(IX=(int)((MINX));util.Sign(1)*IX<=util.Sign(1)*((MAXX));IX+=(int)1){
          ISUM[(int)(((J))-1)]=(int)((ISUM[(int)(((J))-1)]+JHIST[(int)(((IX))-1)]
            [(int)(((IY))-1)][(int)(((IZ))-1)]));
          if(((IPK)<(JHIST[(int)(((IX))-1)][(int)(((IY))-1)][(int)(((IZ))-1)])))
               IPK=(int)((JHIST[(int)(((IX))-1)][(int)(((IY))-1)][(int)(((IZ))-1)])
              );
}
}
}
    int ONE=1;
    int TWO=2;
    int THREE=3;
    int FOUR=4;
    int SIX=SEVEN-1;
    int FIVE=SEVEN-2;
    int FOURRT=SEVEN-3;
//  Find maximum I/sig(I)
//  Peak centered on peak time channel THREE of SEVEN.
    PKTIME=(float)JTIME[(int)(((TWO))-1)];
    for(J=THREE;J<=FOURRT;J++)
      PKTIME+=(float)JTIME[(int)(((J))-1)];
    BKTIME=(float)((JTIME[(int)(((ONE))-1)]+JTIME[(int)(((FIVE))-1)]));
    RATIO=(float)((PKTIME/BKTIME));
    PKCTS=(float)ISUM[(int)(((TWO))-1)];
    for(J=THREE;J<=FOURRT;J++)
      PKCTS+=(float)ISUM[(int)(((J))-1)];
    BKCTS=(float)((ISUM[(int)(((ONE))-1)]+ISUM[(int)(((FIVE))-1)]));
    INTI1=(float)((PKCTS-RATIO*BKCTS));
//      TYPE *,'INTI1,PKCTS,BKCTS,RATIO',INTI1,PKCTS,BKCTS,RATIO
    SIG1=(float)(((float)Math.sqrt((double)(((PKCTS+RATIO*RATIO*BKCTS))))));
    RIS1=(float)((INTI1/SIG1));
//      TYPE *,'INTI1,SIG1,RIS1',INTI1,SIG1,RIS1
//  Peak centered on peak time channel FOUR of SEVEN (calculated position).
    PKTIME=(float)JTIME[(int)(((THREE))-1)];
    for(J=FOUR;J<=FIVE;J++)
      PKTIME+=(float)JTIME[(int)(((J))-1)];
    BKTIME=(float)((JTIME[(int)(((TWO))-1)]+JTIME[(int)(((SIX))-1)]));
    RATIO=(float)((PKTIME/BKTIME));
    PKCTS=(float)ISUM[(int)(((THREE))-1)];
    for(J=FOUR;J<=FIVE;J++)
      PKCTS+=(float)ISUM[(int)(((J))-1)];
    BKCTS=(float)((ISUM[(int)(((TWO))-1)]+ISUM[(int)(((SIX))-1)]));
    INTI2=(float)((PKCTS-RATIO*BKCTS));
    SIG2=(float)(((float)Math.sqrt((double)(((PKCTS+RATIO*RATIO*BKCTS))))));
    RIS2=(float)((INTI2/SIG2));
//      TYPE *,'INTI2,SIG2,RIS2',INTI2,SIG2,RIS2
//  Peak centered on peak time channel FIVE of SEVEN.
    PKTIME=(float)JTIME[(int)(((FOUR))-1)];
    for(J=FIVE;J<=SIX;J++)
      PKTIME+=(float)JTIME[(int)(((J))-1)];
    BKTIME=(float)((JTIME[(int)(((THREE))-1)]+JTIME[(int)(((SEVEN))-1)]));
    RATIO=(float)((PKTIME/BKTIME));
    PKCTS=(float)ISUM[(int)(((FOUR))-1)];
    for(J=FIVE;J<=SIX;J++)
      PKCTS+=(float)ISUM[(int)(((J))-1)];
    BKCTS=(float)((ISUM[(int)(((THREE))-1)]+ISUM[(int)(((SEVEN))-1)]));
    INTI3=(float)((PKCTS-RATIO*BKCTS));
    SIG3=(float)(((float)Math.sqrt((double)(((PKCTS+RATIO*RATIO*BKCTS))))));
    RIS3=(float)((INTI3/SIG3));
//      TYPE *,'INTI3,SIG3,RIS3',INTI3,SIG3,RIS3
    if(((IPFLAG)==(0))){
        util.WRITESTRING(((0)),((
          "  Counts for Offsets from peak time:   ")));
        util.WRITELN(((0)));
        for(J=ONE;J<=SEVEN;J++)
          util.WRITEINT(((0)),((ISUM[(int)(((J))-1)])),(("I6")));
        util.WRITELN(((0)));
        util.WRITESTRING(((0)),((" Center T   X Y IPK    dX        dY  ")));
        util.WRITESTRING(((0)),((
          "     Ihkl       sigI          I/sigI   ")));
        util.WRITELN(((0)));
        util.WRITEINT(((0)),((ONE+FIVE)/2),(("I4")));
        util.WRITEINT(((0)),((Z)),(("I6")));
        util.WRITEINT(((0)),((X)),(("I5")));
        util.WRITEINT(((0)),((Y)),(("I5")));
        util.WRITEINT(((0)),((MAXP1)),(("I6")));
        util.WRITEINT(((0)),((MINX)),(("I5")));
        util.WRITEINT(((0)),((MAXX)),(("I5")));
        util.WRITEINT(((0)),((MINY)),(("I5")));
        util.WRITEINT(((0)),((MAXY)),(("I5")));
        util.WRITEFLOAT(((0)),((INTI1)),(("F10.2")));
        util.WRITEFLOAT(((0)),((SIG1)),(("F10.2")));
        util.WRITEFLOAT(((0)),((RIS1)),(("F10.2")));
        util.WRITESTRING(((0)),(("          ")));
        util.WRITELN(((0)));
        util.WRITEINT(((0)),((TWO+SIX)/2),(("I4")));
        util.WRITEINT(((0)),((Z)),(("I6")));
        util.WRITEINT(((0)),((X)),(("I5")));
        util.WRITEINT(((0)),((Y)),(("I5")));
        util.WRITEINT(((0)),((MAXP1)),(("I6")));
        util.WRITEINT(((0)),((MINX)),(("I5")));
        util.WRITEINT(((0)),((MAXX)),(("I5")));
        util.WRITEINT(((0)),((MINY)),(("I5")));
        util.WRITEINT(((0)),((MAXY)),(("I5")));
        util.WRITEFLOAT(((0)),((INTI2)),(("F10.2")));
        util.WRITEFLOAT(((0)),((SIG2)),(("F10.2")));
        util.WRITEFLOAT(((0)),((RIS2)),(("F10.2")));
        util.WRITESTRING(((0)),(("          ")));
        util.WRITELN(((0)));
        util.WRITEINT(((0)),((THREE+SEVEN)/2),(("I4")));
        util.WRITEINT(((0)),((Z)),(("I6")));
        util.WRITEINT(((0)),((X)),(("I5")));
        util.WRITEINT(((0)),((Y)),(("I5")));
        util.WRITEINT(((0)),((MAXP1)),(("I6")));
        util.WRITEINT(((0)),((MINX)),(("I5")));
        util.WRITEINT(((0)),((MAXX)),(("I5")));
        util.WRITEINT(((0)),((MINY)),(("I5")));
        util.WRITEINT(((0)),((MAXY)),(("I5")));
        util.WRITEFLOAT(((0)),((INTI3)),(("F10.2")));
        util.WRITEFLOAT(((0)),((SIG3)),(("F10.2")));
        util.WRITEFLOAT(((0)),((RIS3)),(("F10.2")));
        util.WRITESTRING(((0)),(("          ")));
        util.WRITELN(((0)));
        util.WRITELN(((0)));
    }
//  Find maximum I/Sig(I)
    if((((RIS2)>(RIS1)&&(RIS2)>(RIS3)))){
//            GO TO 500
      } else {
      if(((RIS1)>(RIS3))){
        if(((RIS1)>=(5.0))){
          ITOT=(int)((INTI1));
          SIGITOT=(float)((SIG1));
          REFLAG=(float)((101));
          return null;
                  }
        } else {
        if(((RIS3)>=(5.0))){
          ITOT=(int)((INTI3));
          SIGITOT=(float)((SIG3));
          REFLAG=(float)((101));
          return null;
                  }
              }
          }
    ITOT=(int)((INTI2));
    SIGITOT=(float)((SIG2));
    REFLAG=(float)((100));
    return null;

  }
  public String getCommand(){
    return "TOFINT";}


  public String getDocumentation(){
    return " Default Docs ";}
}
