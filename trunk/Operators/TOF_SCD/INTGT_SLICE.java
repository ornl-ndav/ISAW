package Operators.TOF_SCD;
import java.util.*;

import DataSetTools.operator.*;

import Command.JavaCC.Fortran.*;

import gov.anl.ipns.Util.SpecialStrings.*;

public class INTGT_SLICE implements Wrappable,HiddenOperator{

  public int[][][] JHIST=null;

  public int[][] RANGE=null;

  public int[] KRANGE=null;

  public int IZ=0;

  public float[] IHKL=null;

  public float[] SIGI=null;

  public float[] ITOSIGI=null;

  public Object calculate(){

    int I=0;

    int IBCKTOT=0;

    int IBTOT=0;

    int IBXMAX=0;

    int IBXMIN=0;

    int IBYMAX=0;

    int IBYMIN=0;

    int ISIGTOT=0;

    int ISTOT=0;

    int J=0;

    float STOB=0f;

    float STOB2=0f;

    //-------------- code ------------
//----------------------------------------------------------------------
      IBXMIN=(int)((RANGE[(int)(((1))-1)][(int)(((IZ))-1)]-1));
      IBXMAX=(int)((RANGE[(int)(((2))-1)][(int)(((IZ))-1)]+1));
      IBYMIN=(int)((RANGE[(int)(((3))-1)][(int)(((IZ))-1)]-1));
      IBYMAX=(int)((RANGE[(int)(((4))-1)][(int)(((IZ))-1)]+1));
      ISIGTOT=(int)(((((RANGE[(int)(((2))-1)][(int)(((IZ))-1)]-RANGE[(int)(((1)
        )-1)][(int)(((IZ))-1)]+1)))*(((RANGE[(int)(((4))-1)][(int)(((IZ))-1)]-
          RANGE[(int)(((3))-1)][(int)(((IZ))-1)]+1)))));
      IBCKTOT=(int)(((((IBXMAX-IBXMIN+1)))*(((IBYMAX-IBYMIN+1)))-ISIGTOT));
      STOB=(float)(((float)(((ISIGTOT)))/(float)(((IBCKTOT)))));
      IBTOT=(int)((0));
      ISTOT=(int)((0));
      for(J=(int)((IBYMIN));util.Sign(1)*J<=util.Sign(1)*((IBYMAX));J+=(int)1){
        for(I=(int)((IBXMIN));util.Sign(1)*I<=util.Sign(1)*((IBXMAX));I+=(int)1)
          {
          IBTOT=(int)((IBTOT+JHIST[(int)(((I))-1)][(int)(((J))-1)][(int)(((
            KRANGE[(int)(((IZ))-1)]))-1)]));
          if((((((J)>=(RANGE[(int)(((3))-1)][(int)(((IZ))-1)])&&(J)<=(RANGE[(
            int)(((4))-1)][(int)(((IZ))-1)]))&&(I)>=(RANGE[(int)(((1))-1)][(int)
              (((IZ))-1)]))&&(I)<=(RANGE[(int)(((2))-1)][(int)(((IZ))-1)])))){
            ISTOT=(int)((ISTOT+JHIST[(int)(((I))-1)][(int)(((J))-1)][(int)(((
              KRANGE[(int)(((IZ))-1)]))-1)]));
            }
}
}
      STOB2=(float)((Math.pow(STOB,2)));
      IBTOT=(int)((IBTOT-ISTOT));
      IHKL[(int)(((1))-1)]=(float)((ISTOT-STOB*IBTOT));
      SIGI[(int)(((1))-1)]=(float)(((float)Math.sqrt((double)(((ISTOT+STOB2*
        IBTOT))))));
      ITOSIGI[(int)(((1))-1)]=(float)((IHKL[(int)(((1))-1)]/SIGI[(int)(((1))-1)]
        ));
      return null;

    return null;

  }
  public String getCommand(){
    return "INTGT_SLICE";}


  public String getDocumentation(){
    return " Default Docs ";}
}
