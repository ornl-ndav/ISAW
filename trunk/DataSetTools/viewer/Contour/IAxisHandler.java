package DataSetTools.viewer.Contour;


public interface IAxisHandler
  {
   public String getAxisName();

   public String getAxisUnits();

   /** Gets the axis value for this Group and xvalue<P>
   * NOTE: The y value can be gotten with getX(i)
   */
   public float  getValue( int GroupIndex, int xIndex);

   /** Returns the xIndex that has the given axis value = Value for  
   *  this Group
   */
   public int  getXindex( int GroupIndex, float Value);

   public float getMaxAxisValue(int GroupIndex);
   public float getMinAxisValue(int GroupIndex);

   }

