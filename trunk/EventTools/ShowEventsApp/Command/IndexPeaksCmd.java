package EventTools.ShowEventsApp.Command;

/**
 *  This class has the information needed to index peaks using the
 *  index peaks with optimizer process.
 */
public class IndexPeaksCmd extends IndexPeaksLatticeParams
{
   private int   fixedPeakIndex;
   private float required_fraction;
   
   public IndexPeaksCmd( float a,     float b,    float c, 
                         float alpha, float beta, float gamma, 
                         float tolerance,
                         int   fixedPeakIndex, 
                         float requiredFraction )
   {
      super( a, b, c, alpha, beta, gamma, tolerance );
      this.fixedPeakIndex = fixedPeakIndex;
      this.required_fraction = requiredFraction;
   }

   public int getFixedPeakIndex()
   {
      return fixedPeakIndex;
   }

   public float getRequiredFraction()
   {
      return required_fraction;
   }
   
   public String toString()
   {
      return super.toString() + 
             "\nRequired fraction: "  + getRequiredFraction() +
             "\nFixed peak index:  "  + getFixedPeakIndex();
   }
}
