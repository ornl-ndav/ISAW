package Operators.TOF_SCD;
import java.util.*;

import DataSetTools.operator.*;

import Command.JavaCC.Fortran.*;

import gov.anl.ipns.Util.SpecialStrings.*;

public class RANGE_TEST implements Wrappable,HiddenOperator{

  public int[][] RANGE=null;

  public int IZ=0;

  public int REFLAG=0;

  public int WLNUM=0;

  public Object calculate(){

    int RANGE_TESTresult=0;

    //-------------- code ------------
//----------------------------------------------------------------------
      REFLAG=(int)((0));
      if((((((RANGE[(int)(((1))-1)][(int)(((IZ))-1)])<(2)||(RANGE[(int)(((2))-1)
        ][(int)(((IZ))-1)])>(84))||(RANGE[(int)(((3))-1)][(int)(((IZ))-1)])<(2))
          ||(RANGE[(int)(((4))-1)][(int)(((IZ))-1)])>(84))))
               RANGE_TESTresult=(int)((500));
      if(((RANGE[(int)(((1))-1)][(int)(((IZ))-1)])>=(RANGE[(int)(((2))-1)][(int)
        (((IZ))-1)])))
             RANGE_TESTresult=(int)((500));
      if(((RANGE[(int)(((3))-1)][(int)(((IZ))-1)])>=(RANGE[(int)(((4))-1)][(int)
        (((IZ))-1)])))
             RANGE_TESTresult=(int)((500));

    return new Integer(RANGE_TESTresult);

  }
  public String getCommand(){
    return "RANGE_TEST";}


  public String getDocumentation(){
    return " Default Docs ";}
}
