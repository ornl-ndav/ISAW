package EventTools.ShowEventsApp.Command;

public class DrawingOptionsCmd
{
   private boolean orthographic;
   private boolean filterMin;
   private boolean filterMax;
   private boolean showAxes;
   private float   pointSize;
   private boolean alpha;
   private float   alphaValue;
   
   public DrawingOptionsCmd(boolean orthographic,
                           boolean filterMin,
                           boolean filterMax,
                           boolean showAxes,
                           float   pointSize,
                           boolean alpha,
                           float   alphaValue)
   {
      this.orthographic = orthographic;
      this.filterMin = filterMin;
      this.filterMax = filterMax;
      this.showAxes = showAxes;
      this.pointSize = pointSize;
      this.alpha = alpha;
      this.alphaValue = alphaValue;
   }
   
   public boolean getOrthographic()
   {
      return orthographic;
   }
   
   public boolean getFilterMin()
   {
      return filterMin;
   }
   
   public boolean getFilterMax()
   {
      return filterMax;
   }
   
   public boolean getShowAxes()
   {
      return showAxes;
   }

   public float getPointSize()
   {
      return pointSize;
   }
   
   public boolean getAlpha()
   {
      return alpha;
   }
   
   public float getAlphaValue()
   {
      return alphaValue;
   }
   

   
   public String toString()
   {
      return "\nOrthographic     : " + getOrthographic() +
             "\nFilter Below Min : " + getFilterMin()    +
             "\nFilter Above Max : " + getFilterMax()    +
             "\nShow Axes        : " + getShowAxes()    +
             "\nPoint Size       : " + getPointSize()    +
             "\nUse Alpha        : " + getAlpha()        +
             "\nAlpha Value      : " + getAlphaValue();
   }
}
