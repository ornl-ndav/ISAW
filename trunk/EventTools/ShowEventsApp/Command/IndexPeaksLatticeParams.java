package EventTools.ShowEventsApp.Command;

/**
 *  This class has the lattice parameter and tolerance information needed 
 *  by the IndexPeaksCmd and ARCS_IndexPeaksCmd objects.  Those two classes
 *  extend this class. 
 */
public class IndexPeaksLatticeParams
{
   private float a;
   private float b;
   private float c;
   private float alpha;
   private float beta;
   private float gamma;
   private float tolerance;
   private float requiredFraction;
   
   public IndexPeaksLatticeParams( float a,     float b,    float c, 
                                   float alpha, float beta, float gamma, 
                                   float tolerance, float requiredFraction )
   {
      this.a = a;
      this.b = b;
      this.c = c;
      this.alpha = alpha;
      this.beta = beta;
      this.gamma = gamma;
      this.tolerance = tolerance;
      this.requiredFraction = requiredFraction;
   }

   public double[] getLatticeParameters()
   {
     double[] lat_par = { a, b, c, alpha, beta, gamma };
     return lat_par;
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

   public float getRequiredFraction()
   {
      return requiredFraction;
   }
   
   public String toString()
   {
      return "\na,b,c " + 
               String.format("%6.3f, %6.3f, %6.3f",
                              getA(), getB(), getC()) +
             "\nalpha, beta, gamma " + String.format("%6.3f, %6.3f, %6.3f", 
                                          getAlpha(), getBeta(), getGamma()) +
             "\nTolerance: "         + getTolerance()      + 
             "\nRequired fraction: " + getRequiredFraction();
   }

}
