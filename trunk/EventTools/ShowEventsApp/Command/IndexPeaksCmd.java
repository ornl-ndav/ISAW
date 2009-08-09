package EventTools.ShowEventsApp.Command;

public class IndexPeaksCmd
{
   private float a;
   private float b;
   private float c;
   private float alpha;
   private float beta;
   private float gamma;
   private float tolerance;
   private int   fixedPeakIndex;
   private float requiredFraction;
   
   public IndexPeaksCmd(float a, float b, float c, float alpha,
                        float beta, float gamma, float tolerance,
                        int fixedPeakIndex, float requiredFraction)
   {
      this.a = a;
      this.b = b;
      this.c = c;
      this.alpha = alpha;
      this.beta = beta;
      this.gamma = gamma;
      this.tolerance = tolerance;
      this.fixedPeakIndex = fixedPeakIndex;
      this.requiredFraction = requiredFraction;
   }

   public float getA()
   {
      return a;
   }

   public float getB()
   {
      return b;
   }

   public float getC()
   {
      return c;
   }

   public float getAlpha()
   {
      return alpha;
   }

   public float getBeta()
   {
      return beta;
   }

   public float getGamma()
   {
      return gamma;
   }

   public float getTolerance()
   {
      return tolerance;
   }

   public int getFixedPeakIndex()
   {
      return fixedPeakIndex;
   }

   public float getRequiredFraction()
   {
      return requiredFraction;
   }
   
   public String toString()
   {
      return "\na,b,c " + a + ", " + b + ", " + c +
             "\nalpha, beta, gamma " + alpha + ", " + beta + ", " + gamma +
             "\ntolerance " + tolerance + 
             "\nfixed peak index " + fixedPeakIndex +
             "\nrequired fraction " + requiredFraction;
   }
}
