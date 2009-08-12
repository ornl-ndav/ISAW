package EventTools.ShowEventsApp.Command;

public class FilterOptionsCmd
{
   private boolean alpha;
   private boolean filterMin;
   private boolean filterMax;
   private float   alphaValue;
   private boolean orthographic;
   
   public FilterOptionsCmd(boolean alpha,
                        float alphaValue,
                        boolean filterMin,
                        boolean filterMax,
                        boolean orthographic)
   {
      this.alpha = alpha;
      this.filterMin = filterMin;
      this.filterMax = filterMax;
      this.alphaValue = alphaValue;
      this.orthographic = orthographic;
   }
   
   public boolean getAlpha()
   {
      return alpha;
   }
   
   public boolean getFilterMin()
   {
      return filterMin;
   }
   
   public boolean getFilterMax()
   {
      return filterMax;
   }
   
   public float getAlphaValue()
   {
      return alphaValue;
   }
   
   public boolean getOrthographic()
   {
      return orthographic;
   }
   
   public String toString()
   {
      return "\nOrthographic     : " + getOrthographic() + "\n" +
               "Filter Above Max : " + getFilterMax()    + "\n" +
               "Filter Below Mim : " + getFilterMin()    + "\n" +
               "Use Alpha        : " + getAlpha()        + "\n" +
               "Alpha Value      : " + getAlphaValue()   + "\n";
   }
}
