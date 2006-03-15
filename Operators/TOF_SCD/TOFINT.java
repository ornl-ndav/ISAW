package Operators.TOF_SCD;

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

    int[] ISUM= new int[((7))-1+1];

    int IXCALC=0;

    int IYCALC=0;

    int IZCALC=0;

    int J=0;

    float[] JTIME= new float[((7))-1+1];

    int MAXX=0;

    int MAXY=0;

    int MAXZ=0;

    int MINX=0;

    int MINY=0;

    int MINZ=0;

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
    MINZ=(int)((Z-3));
    MAXZ=(int)((Z+3));
    MINX=(int)((X-2));
    MAXX=(int)((X+2));
    MINY=(int)((Y-2));
    MAXY=(int)((Y+2));
    if((((((((MINZ)<(1)||(MAXZ)>(WLNUM))||(MINX)<(1))||(MAXX)>(85))||(MINY)<(1))
      ||(MAXY)>(85)))){
      REFLAG=(float)((401));
      return null;
          }
//-------------------------------------------------------------------------------
//                    PROCEED TO INTEGRATE
//-------------------------------------------------------------------------------
    IPK=(int)((0));
    for(Z=(int)((MINZ));util.Sign(1)*Z<=util.Sign(1)*((MAXZ));Z+=(int)1){
      J=(int)((Z-MINZ+1));
      JTIME[(int)(((J))-1)]=(float)((NTIME[(int)(((Z+1))-1)]-NTIME[(int)(((Z))-
        1)]));
      ISUM[(int)(((J))-1)]=(int)((0));
      for(Y=(int)((MINY));util.Sign(1)*Y<=util.Sign(1)*((MAXY));Y+=(int)1){
        for(X=(int)((MINX));util.Sign(1)*X<=util.Sign(1)*((MAXX));X+=(int)1){
          ISUM[(int)(((J))-1)]=(int)((ISUM[(int)(((J))-1)]+JHIST[(int)(((X))-1)]
            [(int)(((Y))-1)][(int)(((Z))-1)]));
          if(((IPK)<(JHIST[(int)(((X))-1)][(int)(((Y))-1)][(int)(((Z))-1)])))
               IPK=(int)((JHIST[(int)(((X))-1)][(int)(((Y))-1)][(int)(((Z))-1)])
              );
}
}
}
//  Find maximum I/sig(I)
//  Peak centered on peak time channel 3 of 7.
    PKTIME=(float)((JTIME[(int)(((2))-1)]+JTIME[(int)(((3))-1)]+JTIME[(int)(((4)
      )-1)]));
    BKTIME=(float)((JTIME[(int)(((1))-1)]+JTIME[(int)(((5))-1)]));
    RATIO=(float)((PKTIME/BKTIME));
    PKCTS=(float)((ISUM[(int)(((2))-1)]+ISUM[(int)(((3))-1)]+ISUM[(int)(((4))-1)
      ]));
    BKCTS=(float)((ISUM[(int)(((1))-1)]+ISUM[(int)(((5))-1)]));
    INTI1=(float)((PKCTS-RATIO*BKCTS));
//      TYPE *,'INTI1,PKCTS,BKCTS,RATIO',INTI1,PKCTS,BKCTS,RATIO
    SIG1=(float)(((float)Math.sqrt((double)(((PKCTS+RATIO*RATIO*BKCTS))))));
    RIS1=(float)((INTI1/SIG1));
//      TYPE *,'INTI1,SIG1,RIS1',INTI1,SIG1,RIS1
//  Peak centered on peak time channel 4 of 7 (calculated position).
    PKTIME=(float)((JTIME[(int)(((3))-1)]+JTIME[(int)(((4))-1)]+JTIME[(int)(((5)
      )-1)]));
    BKTIME=(float)((JTIME[(int)(((2))-1)]+JTIME[(int)(((6))-1)]));
    RATIO=(float)((PKTIME/BKTIME));
    PKCTS=(float)((ISUM[(int)(((3))-1)]+ISUM[(int)(((4))-1)]+ISUM[(int)(((5))-1)
      ]));
    BKCTS=(float)((ISUM[(int)(((2))-1)]+ISUM[(int)(((6))-1)]));
    INTI2=(float)((PKCTS-RATIO*BKCTS));
    SIG2=(float)(((float)Math.sqrt((double)(((PKCTS+RATIO*RATIO*BKCTS))))));
    RIS2=(float)((INTI2/SIG2));
//      TYPE *,'INTI2,SIG2,RIS2',INTI2,SIG2,RIS2
//  Peak centered on peak time channel 5 of 7.
    PKTIME=(float)((JTIME[(int)(((4))-1)]+JTIME[(int)(((5))-1)]+JTIME[(int)(((6)
      )-1)]));
    BKTIME=(float)((JTIME[(int)(((3))-1)]+JTIME[(int)(((7))-1)]));
    RATIO=(float)((PKTIME/BKTIME));
    PKCTS=(float)((ISUM[(int)(((4))-1)]+ISUM[(int)(((5))-1)]+ISUM[(int)(((6))-1)
      ]));
    BKCTS=(float)((ISUM[(int)(((3))-1)]+ISUM[(int)(((7))-1)]));
    INTI3=(float)((PKCTS-RATIO*BKCTS));
    SIG3=(float)(((float)Math.sqrt((double)(((PKCTS+RATIO*RATIO*BKCTS))))));
    RIS3=(float)((INTI3/SIG3));
//      TYPE *,'INTI3,SIG3,RIS3',INTI3,SIG3,RIS3
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
