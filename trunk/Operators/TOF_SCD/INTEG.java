package Operators.TOF_SCD;
import java.util.*;

import DataSetTools.operator.*;

import Command.JavaCC.Fortran.*;

import gov.anl.ipns.Util.SpecialStrings.*;

public class INTEG implements Wrappable,HiddenOperator{

  public int[][][] JHIST=new int[0][0][0];

  public int NXS=0;

  public int NYS=0;

  public int X=0;

  public int Y=0;

  public int Z=0;

  public float ITOT=0f;

  public float SIGITOT=0f;

  public int REFLAG=0;

  public int WLNUM=0;

  public int MAXP1=0;

  public int IPFLAG=0;

  public int IADD=0;

  public int ISX=0;

  public int ISY=0;

  public int ISZ=0;

  public Object calculate(){

    float[] A= new float[((1))-1+1];

    float[] B= new float[((1))-1+1];

    float[] C= new float[((1))-1+1];

    int GOTO=0;

    int I=0;

    int[] IBCKTOT= new int[((5))-1+1];

    int[] IBTOT= new int[((5))-1+1];

    int IFLAG=0;

    float[] IHKL= new float[((5))-1+1];

    float IHKLX=0f;

    int II=0;

    int IJJ=0;

    int IPASS=0;

    int[] ISIGTOT= new int[((5))-1+1];

    int[] ISTOT= new int[((5))-1+1];

    float[] ITOSIGI= new float[((5))-1+1];

    float ITOSIGIX=0f;

    int IZ=0;

    int J=0;

    int JJ=0;

    int K=0;

    int[] KRANGE= new int[((5))-1+1];

    int[] MAXP= new int[((5))-1+1];

    int[] MAXX= new int[((5))-1+1];

    int[] MAXY= new int[((5))-1+1];

    int MAXZ=0;

    int OLDITOSIGI=0;

    int OLDITOT=0;

    int OLDSIGITOT=0;

    int[][] PEAK= new int[((4))-1+1][((5))-1+1];

    String[] PORM= new String[((5))-1+1];

    int[][] RANGE= new int[((4))-1+1][((5))-1+1];

    float[][] SDATA= new float[((85))-1+1][((85))-1+1];

    float[] SIGI= new float[((5))-1+1];

    float SIGIX=0f;

    int[] STEP= new int[((4))-1+1];

    float[] STOB= new float[((5))-1+1];

    float TOTITOSIGI=0f;

    int[] TSLICE= new int[((5))-1+1];

    int[] XDISP= new int[((5))-1+1];

    int[] YDISP= new int[((5))-1+1];

    //-------------- code ------------
// put in bounds checkin for JHIST
    if(((X-ISX)<(1)))
         return null;
    if(((X+ISX)>(NXS)))
         return null;
    if(((Y-ISY)<(1)))
         return null;
    if(((Y+ISY)>(NYS)))
         return null;
    if(((Z-ISZ)<(1)))
         return null;
    if(((Z+ISZ)>(WLNUM)))
         return null;
    TSLICE[(int)(((1))-1)]=(int)((0));
    TSLICE[(int)(((2))-1)]=(int)((-1));
    TSLICE[(int)(((3))-1)]=(int)((1));
    TSLICE[(int)(((4))-1)]=(int)((2));
    TSLICE[(int)(((5))-1)]=(int)((3));
//
//      Search the data for the absolute peak position in the time slices 
//      of interest.  Initialally, the search will range from z-1 to z+1
//      in time channels, y-1 to y+1 and x-1 to x+1 in position channels.
//
//      MAXP(K) = maximum count on time-slice K.
//      MAXX(K) = X channel of MAXP on time-slice K.
//              = PEAK(1,K) = PEAK(2,K)
//      MAXY(K) = Y channel of MAXP on time-slice K.
//              = PEAK(3,K) = PEAK(4,K)
//      MAXZ = Z (time) channel for MAXP(1), which is max for entire peak.
    MAXP1=(int)((JHIST[(int)(((X))-1)][(int)(((Y))-1)][(int)(((Z))-1)]));
    REFLAG=(int)((0));
    MAXP[(int)(((1))-1)]=(int)((0.0));
    for(K=(int)(((((Z-ISZ)))));util.Sign(1)*K<=util.Sign(1)*(((((Z+ISZ)))));K+=(
      int)1){
      for(J=(int)(((((Y-ISY)))));util.Sign(1)*J<=util.Sign(1)*(((((Y+ISY)))))
        ;J+=(int)1){
        for(I=(int)(((((X-ISX)))));util.Sign(1)*I<=util.Sign(1)*(((((X+ISX)))))
          ;I+=(int)1){
          if(((JHIST[(int)(((I))-1)][(int)(((J))-1)][(int)(((K))-1)])>(MAXP[(
            int)(((1))-1)]))){
            MAXP[(int)(((1))-1)]=(int)((JHIST[(int)(((I))-1)][(int)(((J))-1)][(
              int)(((K))-1)]));
            MAXX[(int)(((1))-1)]=(int)((I));
            MAXY[(int)(((1))-1)]=(int)((J));
            MAXZ=(int)((K));
            PEAK[(int)(((1))-1)][(int)(((1))-1)]=(int)((I));
            PEAK[(int)(((3))-1)][(int)(((1))-1)]=(int)((J));
            PEAK[(int)(((2))-1)][(int)(((1))-1)]=(int)((PEAK[(int)(((1))-1)][(
              int)(((1))-1)]));
            PEAK[(int)(((4))-1)][(int)(((1))-1)]=(int)((PEAK[(int)(((3))-1)][(
              int)(((1))-1)]));
            }
}
}
}
    MAXP1=(int)((MAXP[(int)(((1))-1)]));
    if((((((((MAXX[(int)(((1))-1)])<(5)||(MAXX[(int)(((1))-1)])>(81))||(MAXY[(
      int)(((1))-1)])<(5))||(MAXY[(int)(((1))-1)])>(81))||(MAXZ)<(2))||(MAXZ)>((
        ((WLNUM-3))))))){
      REFLAG=(int)((500));
      return null;
      }
    KRANGE[(int)(((1))-1)]=(int)((MAXZ));
    KRANGE[(int)(((2))-1)]=(int)((MAXZ-1));
    KRANGE[(int)(((3))-1)]=(int)((MAXZ+1));
    KRANGE[(int)(((4))-1)]=(int)((MAXZ+2));
    KRANGE[(int)(((5))-1)]=(int)((MAXZ+3));
//      Find MAXP for each time slice individually.
    for(K=(int)((2));util.Sign(1)*K<=util.Sign(1)*((5));K+=(int)1){
      MAXP[(int)(((K))-1)]=(int)((0));
      II=(int)((MAXX[(int)(((1))-1)]));
      JJ=(int)((MAXY[(int)(((1))-1)]));
//      For slices 4(+2) and 5(+3), start search from MAX of
//      previous time-slice.
      if(((K)>=(4))){
        II=(int)((MAXX[(int)(((K-1))-1)]));
        JJ=(int)((MAXY[(int)(((K-1))-1)]));
        }
      for(J=(int)((JJ-1));util.Sign(1)*J<=util.Sign(1)*((JJ+1));J+=(int)1){
        for(I=(int)((II-1));util.Sign(1)*I<=util.Sign(1)*((II+1));I+=(int)1){
          if(((((((((I)>(0)&&(J)>(0))&&(K)>(0))&&(KRANGE[(int)(((K))-1)])>(0))
            &&(I)<=(NXS))&&(J)<=(NYS))&&(KRANGE[(int)(((K))-1)])<=(WLNUM)))){
            if(((JHIST[(int)(((I))-1)][(int)(((J))-1)][(int)(((KRANGE[(int)(((K)
              )-1)]))-1)])>(MAXP[(int)(((K))-1)]))){
              MAXP[(int)(((K))-1)]=(int)((JHIST[(int)(((I))-1)][(int)(((J))-1)][
                (int)(((KRANGE[(int)(((K))-1)]))-1)]));
              MAXX[(int)(((K))-1)]=(int)((I));
              MAXY[(int)(((K))-1)]=(int)((J));
              PEAK[(int)(((1))-1)][(int)(((K))-1)]=(int)((I));
              PEAK[(int)(((3))-1)][(int)(((K))-1)]=(int)((J));
              PEAK[(int)(((2))-1)][(int)(((K))-1)]=(int)((PEAK[(int)(((1))-1)][(
                int)(((K))-1)]));
              PEAK[(int)(((4))-1)][(int)(((K))-1)]=(int)((PEAK[(int)(((3))-1)][(
                int)(((K))-1)]));
              }
            }
}
}
}
//      Set initial peak signal ranges.
    for(K=(int)((1));util.Sign(1)*K<=util.Sign(1)*((5));K+=(int)1){
      RANGE[(int)(((1))-1)][(int)(((K))-1)]=(int)((PEAK[(int)(((1))-1)][(int)(((
        K))-1)]-2));
      RANGE[(int)(((2))-1)][(int)(((K))-1)]=(int)((PEAK[(int)(((1))-1)][(int)(((
        K))-1)]+2));
      RANGE[(int)(((3))-1)][(int)(((K))-1)]=(int)((PEAK[(int)(((3))-1)][(int)(((
        K))-1)]-2));
      RANGE[(int)(((4))-1)][(int)(((K))-1)]=(int)((PEAK[(int)(((3))-1)][(int)(((
        K))-1)]+2));
      try{
        REFLAG=(int)((((Number)util.exec("RANGE_TEST",util.appendVect(
          util.appendVect(util.appendVect(util.appendVect(util.appendVect(
            util.appendVect(new Vector(), util.cvrtObject(((RANGE))))
              , util.cvrtObject(((NXS)))), util.cvrtObject(((NYS))))
                , util.cvrtObject(((K)))), util.cvrtObject(((REFLAG))))
                  , util.cvrtObject(((WLNUM)))))).intValue()));
      }catch(Throwable Thrvar0){
          return new ErrorString( Thrvar0);
          }
      if(((REFLAG)==(500)))
           return null;
}
//
//
    for(IZ=(int)((1));util.Sign(1)*IZ<=util.Sign(1)*((5));IZ+=(int)1){
//
//      This is the initial step.  We will calculate the value of I(hkl)/SIG(I)
//      for the default range of channels in X  and Y.  We will use this as our
//      the first value in our maximization of I(hkl)/SIG(I).
//      Calculate the following:
//      IBXMIN      Min X Background Channel
//      IBXMAX      Max X Background Channel
//      IBYMIN      Min Y Background Channel
//      IBYMAX      Max Y Background Channel
//      ISIGTOT      Total number of signal channels
//      IBCKTOT      Total number of background channels
//      STOB            Ratio of signal to background channels
//      IBTOT            Total counts in background region
//      ISTOT            Total counts in signal region
//      
      A[(int)(((1))-1)]=(float)((IHKL[(int)(((IZ))-1)]));
      B[(int)(((1))-1)]=(float)((SIGI[(int)(((IZ))-1)]));
      C[(int)(((1))-1)]=(float)((ITOSIGI[(int)(((IZ))-1)]));
      try{
        util.exec("INTGT_SLICE",util.appendVect(util.appendVect(util.appendVect(
          util.appendVect(util.appendVect(util.appendVect(util.appendVect(
            new Vector(), util.cvrtObject(((JHIST)))), util.cvrtObject(((RANGE))
              )), util.cvrtObject(((KRANGE)))), util.cvrtObject(((IZ))))
                , util.cvrtObject(((A)))), util.cvrtObject(((B))))
                  , util.cvrtObject(((C)))));
      }catch(Throwable Thrvar1){
          return new ErrorString( Thrvar1);
          }
      IHKL[(int)(((IZ))-1)]=(float)((A[(int)(((1))-1)]));
      SIGI[(int)(((IZ))-1)]=(float)((B[(int)(((1))-1)]));
      ITOSIGI[(int)(((IZ))-1)]=(float)((C[(int)(((1))-1)]));
      if(((ITOSIGI[(int)(((IZ))-1)])<(3.0))){
        IPASS=(int)((2));
        } else {
//
//      Start the search by varying XMIN.  Move XMIN out (more negative)
//      unless the initial step decreases I(hkl)/SIG(I).  If the first step
//      decreases I(hkl)/SIG(I), reverse the direction of the step.  Continue 
//      this process with XMAX (stepping positive), YMIN (stepping negative) and
//      YMAX (stepping positive).
//
//    !Number of passes to integrate time-slice
        IPASS=(int)((1));
        for(IPASS=(int)((1));util.Sign(1)*IPASS<=util.Sign(1)*((2));IPASS+=(int)
          1){
          STEP[(int)(((1))-1)]=(int)((-1));
          STEP[(int)(((2))-1)]=(int)((1));
          STEP[(int)(((3))-1)]=(int)((-1));
          STEP[(int)(((4))-1)]=(int)((1));
          for(II=(int)((1));util.Sign(1)*II<=util.Sign(1)*((4));II+=(int)1){
            IFLAG=(int)((0));
            GOTO=(int)((280));
            while(((GOTO)==(280))){
                GOTO=(int)((0));
                RANGE[(int)(((II))-1)][(int)(((IZ))-1)]=(int)((RANGE[(int)(((II)
                  )-1)][(int)(((IZ))-1)]+STEP[(int)(((II))-1)]));
                try{
                  REFLAG=(int)((((Number)util.exec("RANGE_TEST",util.appendVect(
                    util.appendVect(util.appendVect(util.appendVect(
                      util.appendVect(util.appendVect(new Vector()
                        , util.cvrtObject(((RANGE)))), util.cvrtObject(((NXS))))
                          , util.cvrtObject(((NYS)))), util.cvrtObject(((IZ))))
                            , util.cvrtObject(((REFLAG)))), util.cvrtObject(((
                              WLNUM)))))).intValue()));
                }catch(Throwable Thrvar2){
                    return new ErrorString( Thrvar2);
                    }
                if(((REFLAG)==(500)))
                     return null;
                A[(int)(((1))-1)]=(float)((IHKLX));
                B[(int)(((1))-1)]=(float)((SIGIX));
                C[(int)(((1))-1)]=(float)((ITOSIGIX));
                try{
                  util.exec("INTGT_SLICE",util.appendVect(util.appendVect(
                    util.appendVect(util.appendVect(util.appendVect(
                      util.appendVect(util.appendVect(new Vector()
                        , util.cvrtObject(((JHIST)))), util.cvrtObject(((RANGE))
                          )), util.cvrtObject(((KRANGE)))), util.cvrtObject(((
                            IZ)))), util.cvrtObject(((A)))), util.cvrtObject(((
                              B)))), util.cvrtObject(((C)))));
                }catch(Throwable Thrvar3){
                    return new ErrorString( Thrvar3);
                    }
                IHKLX=(float)((A[(int)(((1))-1)]));
                SIGIX=(float)((B[(int)(((1))-1)]));
                ITOSIGIX=(float)((C[(int)(((1))-1)]));
                if(((ITOSIGIX)>(ITOSIGI[(int)(((IZ))-1)]))){
//      The step increased I/SIGI.  Transfer results to arrays
//      and go back to try another step in the same direction.
                  ITOSIGI[(int)(((IZ))-1)]=(float)((ITOSIGIX));
                  IHKL[(int)(((IZ))-1)]=(float)((IHKLX));
                  SIGI[(int)(((IZ))-1)]=(float)((SIGIX));
                  GOTO=(int)((280));
                  } else {
//
//      The first step outward reduced I(hkl)/SIG(I).
//
                  if((((RANGE[(int)(((II))-1)][(int)(((IZ))-1)])==(PEAK[(int)(((
                    II))-1)][(int)(((IZ))-1)]+3*STEP[(int)(((II))-1)])&&(
                      IFLAG)==(0)))){
//
//      Reduce the range, reverse the step and try again.
//
                    RANGE[(int)(((II))-1)][(int)(((IZ))-1)]=(int)((PEAK[(int)(((
                      II))-1)][(int)(((IZ))-1)]+2*STEP[(int)(((II))-1)]));
                    try{
                      REFLAG=(int)((((Number)util.exec(
                        "RANGE_TEST",util.appendVect(util.appendVect(
                          util.appendVect(util.appendVect(util.appendVect(
                            util.appendVect(new Vector(), util.cvrtObject(((
                              RANGE)))), util.cvrtObject(((NXS))))
                                , util.cvrtObject(((NYS)))), util.cvrtObject(((
                                  IZ)))), util.cvrtObject(((REFLAG))))
                                    , util.cvrtObject(((WLNUM)))))).intValue()))
                                      ;
                    }catch(Throwable Thrvar4){
                        return new ErrorString( Thrvar4);
                        }
                    if(((REFLAG)==(500)))
                         return null;
                    STEP[(int)(((II))-1)]=(int)((-1*STEP[(int)(((II))-1)]));
                    IFLAG=(int)((1));
                    GOTO=(int)((280));
                    }
                  }
}
//
//      This was a good step.
//
//                  ELSE
              if(((GOTO)==(0))){
                RANGE[(int)(((II))-1)][(int)(((IZ))-1)]=(int)((RANGE[(int)(((II)
                  )-1)][(int)(((IZ))-1)]-STEP[(int)(((II))-1)]));
                try{
                  REFLAG=(int)((((Number)util.exec("RANGE_TEST",util.appendVect(
                    util.appendVect(util.appendVect(util.appendVect(
                      util.appendVect(util.appendVect(new Vector()
                        , util.cvrtObject(((RANGE)))), util.cvrtObject(((NXS))))
                          , util.cvrtObject(((NYS)))), util.cvrtObject(((IZ))))
                            , util.cvrtObject(((REFLAG)))), util.cvrtObject(((
                              WLNUM)))))).intValue()));
                }catch(Throwable Thrvar5){
                    return new ErrorString( Thrvar5);
                    }
                if(((REFLAG)==(500)))
                     return null;
                }
}
            }
//*****************  INCLUDE TEST3PLOT.FOR HERE  *********************
}
        if(((IADD)!=(1))){
//      Increase X and Y dimensions by 1 on each side to make sure the
//      entire peak is integrated.
          RANGE[(int)(((1))-1)][(int)(((IZ))-1)]=(int)((RANGE[(int)(((1))-1)][(
            int)(((IZ))-1)]-1));
          RANGE[(int)(((2))-1)][(int)(((IZ))-1)]=(int)((RANGE[(int)(((2))-1)][(
            int)(((IZ))-1)]+1));
          RANGE[(int)(((3))-1)][(int)(((IZ))-1)]=(int)((RANGE[(int)(((3))-1)][(
            int)(((IZ))-1)]-1));
          RANGE[(int)(((4))-1)][(int)(((IZ))-1)]=(int)((RANGE[(int)(((4))-1)][(
            int)(((IZ))-1)]+1));
          try{
            REFLAG=(int)((((Number)util.exec("RANGE_TEST",util.appendVect(
              util.appendVect(util.appendVect(util.appendVect(util.appendVect(
                util.appendVect(new Vector(), util.cvrtObject(((RANGE))))
                  , util.cvrtObject(((NXS)))), util.cvrtObject(((NYS))))
                    , util.cvrtObject(((IZ)))), util.cvrtObject(((REFLAG))))
                      , util.cvrtObject(((WLNUM)))))).intValue()));
          }catch(Throwable Thrvar6){
              return new ErrorString( Thrvar6);
              }
          if(((REFLAG)==(500)))
               return null;
          A[(int)(((1))-1)]=(float)((IHKL[(int)(((IZ))-1)]));
          B[(int)(((1))-1)]=(float)((SIGI[(int)(((IZ))-1)]));
          C[(int)(((1))-1)]=(float)((ITOSIGI[(int)(((IZ))-1)]));
          try{
            util.exec("INTGT_SLICE",util.appendVect(util.appendVect(
              util.appendVect(util.appendVect(util.appendVect(util.appendVect(
                util.appendVect(new Vector(), util.cvrtObject(((JHIST))))
                  , util.cvrtObject(((RANGE)))), util.cvrtObject(((KRANGE))))
                    , util.cvrtObject(((IZ)))), util.cvrtObject(((A))))
                      , util.cvrtObject(((B)))), util.cvrtObject(((C)))));
          }catch(Throwable Thrvar7){
              return new ErrorString( Thrvar7);
              }
          IHKL[(int)(((IZ))-1)]=(float)((A[(int)(((1))-1)]));
          SIGI[(int)(((IZ))-1)]=(float)((B[(int)(((1))-1)]));
          ITOSIGI[(int)(((IZ))-1)]=(float)((C[(int)(((1))-1)]));
          }
}
//           End of loop integrating each of 5 time-slices.
//
//      Maximize the sum of the five I(hkl)/SIG(I)'s. Start with JDIR=-1
//      to check 0,-1,1,2,3.  If this fails on the 0,-1 check, switch to
//      JDIR=1 to check 0,1,2,3.  J=0 indicates the initial step (in either
//      direction).  
//
      PORM[(int)(((1))-1)]=(String)(("Yes"));
      PORM[(int)(((2))-1)]=(String)(("No "));
      PORM[(int)(((3))-1)]=(String)(("No "));
      PORM[(int)(((4))-1)]=(String)(("No "));
      PORM[(int)(((5))-1)]=(String)(("No "));
      ITOT=(float)((IHKL[(int)(((1))-1)]));
      SIGITOT=(float)((SIGI[(int)(((1))-1)]));
      TOTITOSIGI=(float)((ITOSIGI[(int)(((1))-1)]));
      if(((IHKL[(int)(((1))-1)])<(0.0))){
        GOTO=(int)((690));
        }
      if(((GOTO)==(0))){
        for(IJJ=(int)((2));util.Sign(1)*IJJ<=util.Sign(1)*((3));IJJ+=(int)1){
          if(((IHKL[(int)(((1))-1)])<(IHKL[(int)(((IJJ))-1)]))){
            REFLAG=(int)((500));
            return null;
            }
}
        PORM[(int)(((2))-1)]=(String)(("Yes"));
        ITOT=(float)((IHKL[(int)(((1))-1)]+IHKL[(int)(((2))-1)]));
        SIGITOT=(float)(((float)Math.sqrt((double)(((Math.pow(SIGI[(int)(((1))-
          1)],2)+Math.pow(SIGI[(int)(((2))-1)],2)))))));
        TOTITOSIGI=(float)((ITOT/SIGITOT));
// !Test to see if I/sig(I) is increasing.
        if(((TOTITOSIGI)<(ITOSIGI[(int)(((1))-1)]))){
// !If not, add in anyway if intensity is more than 1%      
          if((((ITOSIGI[(int)(((2))-1)])>(2.0)&&(IHKL[(int)(((2))-1)]/IHKL[(int)
            (((1))-1)])>(0.01)))){
            GOTO=(int)((680));
            } else {
//  !of total intensity at this point.      
            ITOT=(float)((IHKL[(int)(((1))-1)]));
            SIGITOT=(float)((SIGI[(int)(((1))-1)]));
            TOTITOSIGI=(float)((ITOSIGI[(int)(((1))-1)]));
            PORM[(int)(((2))-1)]=(String)(("No "));
            }
          }
        }
      if((((GOTO)==(0)||(GOTO)==(680)))){
        GOTO=(int)((0));
        OLDITOT=(int)((ITOT));
        OLDSIGITOT=(int)((SIGITOT));
        OLDITOSIGI=(int)((TOTITOSIGI));
        PORM[(int)(((3))-1)]=(String)(("Yes"));
        ITOT=(float)((ITOT+IHKL[(int)(((3))-1)]));
        SIGITOT=(float)(((float)Math.sqrt((double)(((Math.pow(SIGITOT,2)+
          Math.pow(SIGI[(int)(((3))-1)],2)))))));
        TOTITOSIGI=(float)((ITOT/SIGITOT));
        if(((TOTITOSIGI)<(OLDITOSIGI))){
          if((((ITOSIGI[(int)(((3))-1)])>(3.0)&&(IHKL[(int)(((3))-1)]/OLDITOT)>(
            0.01)))){
            GOTO=(int)((682));
            } else {
            ITOT=(float)((OLDITOT));
            SIGITOT=(float)((OLDSIGITOT));
            TOTITOSIGI=(float)((OLDITOSIGI));
            PORM[(int)(((3))-1)]=(String)(("No "));
            GOTO=(int)((690));
            }
          }
        }
      if((((GOTO)==(0)||(GOTO)==(682)))){
        GOTO=(int)((0));
        OLDITOT=(int)((ITOT));
        OLDSIGITOT=(int)((SIGITOT));
        OLDITOSIGI=(int)((TOTITOSIGI));
        if(((IHKL[(int)(((1))-1)])<(IHKL[(int)(((4))-1)])))
             GOTO=(int)((690));
        }
      if(((GOTO)==(0))){
        PORM[(int)(((4))-1)]=(String)(("Yes"));
        ITOT=(float)((ITOT+IHKL[(int)(((4))-1)]));
        SIGITOT=(float)(((float)Math.sqrt((double)(((Math.pow(SIGITOT,2)+
          Math.pow(SIGI[(int)(((4))-1)],2)))))));
        TOTITOSIGI=(float)((ITOT/SIGITOT));
        if(((TOTITOSIGI)<(OLDITOSIGI))){
          if((((ITOSIGI[(int)(((4))-1)])>(3.0)&&(IHKL[(int)(((4))-1)]/OLDITOT)>(
            0.01)))){
            GOTO=(int)((684));
            } else {
            ITOT=(float)((OLDITOT));
            SIGITOT=(float)((OLDSIGITOT));
            TOTITOSIGI=(float)((OLDITOSIGI));
            PORM[(int)(((4))-1)]=(String)(("No "));
            GOTO=(int)((690));
            }
          }
        }
      if((((GOTO)==(684)||(GOTO)==(0)))){
        GOTO=(int)((0));
        OLDITOT=(int)((ITOT));
        OLDSIGITOT=(int)((SIGITOT));
        OLDITOSIGI=(int)((TOTITOSIGI));
        if(((IHKL[(int)(((1))-1)])<(IHKL[(int)(((5))-1)])))
             GOTO=(int)((690));
        }
      if(((GOTO)==(0))){
        PORM[(int)(((5))-1)]=(String)(("Yes"));
        ITOT=(float)((ITOT+IHKL[(int)(((5))-1)]));
        SIGITOT=(float)(((float)Math.sqrt((double)(((Math.pow(SIGITOT,2)+
          Math.pow(SIGI[(int)(((5))-1)],2)))))));
        TOTITOSIGI=(float)((ITOT/SIGITOT));
        if(((TOTITOSIGI)<(OLDITOSIGI))){
          if((((ITOSIGI[(int)(((5))-1)])>(3.0)&&(IHKL[(int)(((5))-1)]/OLDITOT)>(
            0.01)))){
            GOTO=(int)((690));
            } else {
            ITOT=(float)((OLDITOT));
            SIGITOT=(float)((OLDSIGITOT));
            TOTITOSIGI=(float)((OLDITOSIGI));
            PORM[(int)(((5))-1)]=(String)(("No "));
            }
          }
        }
      if(((GOTO)==(690)))
           GOTO=(int)((0));
      if(((IPFLAG)==(0))){
      util.WRITESTRING(((0)),((" Layer  T   MaxX MaxY  IPK     dX      dY  ")))
        ;
        util.WRITESTRING(((0)),((
          "     Ihkl      sigI    I/sigI   Included in I(hkl)?")));
        util.WRITELN(((0)));
        util.WRITEINT(((0)),((TSLICE[(int)(((2))-1)])),(("I4")));
        util.WRITEINT(((0)),((MAXZ-1)),(("I6")));
        util.WRITEINT(((0)),((MAXX[(int)(((2))-1)])),(("I5")));
        util.WRITEINT(((0)),((MAXY[(int)(((2))-1)])),(("I5")));
        util.WRITEINT(((0)),((MAXP[(int)(((2))-1)])),(("I6")));
        util.WRITEINT(((0)),((RANGE[(int)(((1))-1)][(int)(((2))-1)])),(("I5")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((2))-1)][(int)(((2))-1)])),(("I3")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((3))-1)][(int)(((2))-1)])),(("I5")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((4))-1)][(int)(((2))-1)])),(("I3")))
          ;
        util.WRITEFLOAT(((0)),((IHKL[(int)(((2))-1)])),(("F10.2")));
        util.WRITEFLOAT(((0)),((SIGI[(int)(((2))-1)])),(("F10.2")));
        util.WRITEFLOAT(((0)),((ITOSIGI[(int)(((2))-1)])),(("F10.2")));
        util.WRITESTRING(((0)),(("          ")));
        util.WRITESTRING(((0)),((PORM[(int)(((2))-1)])));
        util.WRITELN(((0)));
        util.WRITEINT(((0)),((TSLICE[(int)(((1))-1)])),(("I4")));
        util.WRITEINT(((0)),((MAXZ)),(("I6")));
        util.WRITEINT(((0)),((MAXX[(int)(((1))-1)])),(("I5")));
        util.WRITEINT(((0)),((MAXY[(int)(((1))-1)])),(("I5")));
        util.WRITEINT(((0)),((MAXP[(int)(((1))-1)])),(("I6")));
        util.WRITEINT(((0)),((RANGE[(int)(((1))-1)][(int)(((1))-1)])),(("I5")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((2))-1)][(int)(((1))-1)])),(("I3")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((3))-1)][(int)(((1))-1)])),(("I5")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((4))-1)][(int)(((1))-1)])),(("I3")))
          ;
        util.WRITEFLOAT(((0)),((IHKL[(int)(((1))-1)])),(("F10.2")));
        util.WRITEFLOAT(((0)),((SIGI[(int)(((1))-1)])),(("F10.2")));
        util.WRITEFLOAT(((0)),((ITOSIGI[(int)(((1))-1)])),(("F10.2")));
        util.WRITESTRING(((0)),(("          ")));
        util.WRITESTRING(((0)),((PORM[(int)(((1))-1)])));
        util.WRITELN(((0)));
        util.WRITEINT(((0)),((TSLICE[(int)(((3))-1)])),(("I4")));
        util.WRITEINT(((0)),((MAXZ+1)),(("I6")));
        util.WRITEINT(((0)),((MAXX[(int)(((3))-1)])),(("I5")));
        util.WRITEINT(((0)),((MAXY[(int)(((3))-1)])),(("I5")));
        util.WRITEINT(((0)),((MAXP[(int)(((3))-1)])),(("I6")));
        util.WRITEINT(((0)),((RANGE[(int)(((1))-1)][(int)(((3))-1)])),(("I5")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((2))-1)][(int)(((3))-1)])),(("I3")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((3))-1)][(int)(((3))-1)])),(("I5")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((4))-1)][(int)(((3))-1)])),(("I3")))
          ;
        util.WRITEFLOAT(((0)),((IHKL[(int)(((3))-1)])),(("F10.2")));
        util.WRITEFLOAT(((0)),((SIGI[(int)(((3))-1)])),(("F10.2")));
        util.WRITEFLOAT(((0)),((ITOSIGI[(int)(((3))-1)])),(("F10.2")));
        util.WRITESTRING(((0)),(("          ")));
        util.WRITESTRING(((0)),((PORM[(int)(((3))-1)])));
        util.WRITELN(((0)));
        util.WRITEINT(((0)),((TSLICE[(int)(((4))-1)])),(("I4")));
        util.WRITEINT(((0)),((MAXZ+2)),(("I6")));
        util.WRITEINT(((0)),((MAXX[(int)(((4))-1)])),(("I5")));
        util.WRITEINT(((0)),((MAXY[(int)(((4))-1)])),(("I5")));
        util.WRITEINT(((0)),((MAXP[(int)(((4))-1)])),(("I6")));
        util.WRITEINT(((0)),((RANGE[(int)(((1))-1)][(int)(((4))-1)])),(("I5")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((2))-1)][(int)(((4))-1)])),(("I3")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((3))-1)][(int)(((4))-1)])),(("I5")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((4))-1)][(int)(((4))-1)])),(("I3")))
          ;
        util.WRITEFLOAT(((0)),((IHKL[(int)(((4))-1)])),(("F10.2")));
        util.WRITEFLOAT(((0)),((SIGI[(int)(((4))-1)])),(("F10.2")));
        util.WRITEFLOAT(((0)),((ITOSIGI[(int)(((4))-1)])),(("F10.2")));
        util.WRITESTRING(((0)),(("          ")));
        util.WRITESTRING(((0)),((PORM[(int)(((4))-1)])));
        util.WRITELN(((0)));
        util.WRITEINT(((0)),((TSLICE[(int)(((5))-1)])),(("I4")));
        util.WRITEINT(((0)),((MAXZ+3)),(("I6")));
        util.WRITEINT(((0)),((MAXX[(int)(((5))-1)])),(("I5")));
        util.WRITEINT(((0)),((MAXY[(int)(((5))-1)])),(("I5")));
        util.WRITEINT(((0)),((MAXP[(int)(((5))-1)])),(("I6")));
        util.WRITEINT(((0)),((RANGE[(int)(((1))-1)][(int)(((5))-1)])),(("I5")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((2))-1)][(int)(((5))-1)])),(("I3")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((3))-1)][(int)(((5))-1)])),(("I5")))
          ;
        util.WRITEINT(((0)),((RANGE[(int)(((4))-1)][(int)(((5))-1)])),(("I3")))
          ;
        util.WRITEFLOAT(((0)),((IHKL[(int)(((5))-1)])),(("F10.2")));
        util.WRITEFLOAT(((0)),((SIGI[(int)(((5))-1)])),(("F10.2")));
        util.WRITEFLOAT(((0)),((ITOSIGI[(int)(((5))-1)])),(("F10.2")));
        util.WRITESTRING(((0)),(("          ")));
        util.WRITESTRING(((0)),((PORM[(int)(((5))-1)])));
        util.WRITELN(((0)));
        util.WRITELN(((0)));
        }

    return null;

  }
  public String getCommand(){
    return "INTEG";}


  public String getDocumentation(){
    return " Default Docs ";}
}
