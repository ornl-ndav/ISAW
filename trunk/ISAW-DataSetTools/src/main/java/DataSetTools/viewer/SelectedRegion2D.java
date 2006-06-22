package DataSetTools.viewer;

import gov.anl.ipns.Util.Sys.*;

public class SelectedRegion2D implements ISelectedRegion{
  public int[] rows, cols;

  public SelectedRegion2D( int[] rows, int[] cols){
     this.rows = rows;
     this.cols = cols;
  }
  public void show(){
     System.out.println("------------------------------");
     System.out.println("rows"+StringUtil.toString( rows));
     System.out.println("cols"+StringUtil.toString( cols));
     System.out.println("---------------------------------");  

  }
}
