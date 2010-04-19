package EventTools.ShowEventsApp.Command;

/**
 *  This class has the information needed to index peaks using the
 *  index peaks with optimizer process.
 */
public class IndexPeaksCmd extends IndexPeaksLatticeParams
{
   private int   fixedPeakIndex;
   
   public IndexPeaksCmd( float a,     float b,    float c, 
                         float alpha, float beta, float gamma, 
                         float tolerance,
                         int   fixedPeakIndex, 
                         float requiredFraction )
   {
      super( a, b, c, alpha, beta, gamma, tolerance, requiredFraction );
      this.fixedPeakIndex = fixedPeakIndex;
   }

   public int getFixedPeakIndex()
   {
      return fixedPeakIndex;
   }
   
   public String toString()
   {
      return super.toString() + 
             "\nFixed peak index: "  + getFixedPeakIndex();
   }
}
