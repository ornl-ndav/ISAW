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

  public float ITOT=0;

  public float SIGITOT=0f;

  public float RIS1=0f;

  public float REFLAG=0f;

  public int WLNUM=0;

  public int MAXX=0;

  public int MAXY=0;

  public int MAXZ=0;

  public int MINX=0;

  public int MINY=0;

  public int MINZ=0;

  public String LOG=null;

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
      LOG=String.format("Data range off edge of grid: MINZ %d MaXZ %d MINX %d MAXX %d MINY %d MAXY %d",MINZ,MAXZ,MINX,MAXX,MINY,MAXY);
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
    int SIX=SEVEN-1;
//  Find maximum I/sig(I)
//  Peak centered on peak time channel THREE of SEVEN.
    PKTIME=(float)JTIME[(int)(((TWO))-1)];
    for(J=THREE;J<=SIX;J++)
      PKTIME+=(float)JTIME[(int)(((J))-1)];
    BKTIME=(float)((JTIME[(int)(((ONE))-1)]+JTIME[(int)(((SEVEN))-1)]));
    RATIO=(float)((PKTIME/BKTIME));
    PKCTS=(float)ISUM[(int)(((TWO))-1)];
    for(J=THREE;J<=SIX;J++)
      PKCTS+=(float)ISUM[(int)(((J))-1)];
    BKCTS=(float)((ISUM[(int)(((ONE))-1)]+ISUM[(int)(((SEVEN))-1)]));
    ITOT=(float)((PKCTS-RATIO*BKCTS));
    SIGITOT=(float)(((float)Math.sqrt((double)(((PKCTS+RATIO*RATIO*BKCTS))))));
    RIS1=(float)((ITOT/SIGITOT));
    if(((IPFLAG)==(0))){
        LOG=String.format("%3d%5d%7d%5d%5d%4d%4d%4d%4d %9.2f%9.2f%8.2f%s\n",
          SEVEN,Z,X,Y,(int)JHIST[X-1][Y-1][Z-1],MINX,MAXX,MINY,MAXY,ITOT,SIGITOT,RIS1,"   Yes");
        LOG+="Counts for Offsets from peak time:\n ";
        for(J=ONE;J<=SEVEN;J++)
          LOG+="  "+ISUM[J-1];
        LOG+="  "+"\n ";
    }
    REFLAG=(float)((100));
    return null;

  }
  public String getCommand(){
    return "TOFINT";}


  public String getDocumentation(){
    return " Default Docs ";}
}
