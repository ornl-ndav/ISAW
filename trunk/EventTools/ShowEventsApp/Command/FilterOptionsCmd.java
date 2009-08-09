package EventTools.ShowEventsApp.Command;

public class FilterOptionsCmd
{
   private boolean alpha;
   private boolean filterMin;
   private boolean filterMax;
   
   public FilterOptionsCmd(boolean alpha,
                        boolean filterMin,
                        boolean filterMax)
   {
      this.alpha = alpha;
      this.filterMin = filterMin;
      this.filterMax = filterMax;
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
   
   public String toString()
   {
      return "\n" + alpha + ", " + filterMin + ", " + filterMax + "\n";
   }
}
