package DataSetTools.viewer.Contour;

import DataSetTools.dataset.*;

public interface IAxesHandler
  {

   public void setTransformation( float[][]Transformation, String[] Names, String[] Units);
   public IAxisHandler getAxis( int n);
  }
