package EventTools.ShowEventsApp.Command;

/**
 *  This class has the information needed to index peaks and refine the
 *  UB matrix 
 */
public class IndexAndRefineUBCmd 
{
   float tolerance;
   double[][] UB;

   public IndexAndRefineUBCmd( float tolerance )
   {
     this.tolerance = tolerance;
     this.UB = null;
   }

   public IndexAndRefineUBCmd( float[][] UB_float, float tolerance )
   {
     this.tolerance = tolerance;
     setUB( UB_float );
   }

   public float getTolerance()
   {
      return tolerance;
   }

   public void setUB( float[][] UB_float )
   {
     if (UB_float.length != 3 || UB_float[0].length != 3 )
       throw new IllegalArgumentException( "UB_float matrix not 3x3" );

     this.UB = new double[3][3];
     for ( int row = 0; row < 3; row++ )
       for ( int col = 0; col < 3; col++ )
         this.UB[row][col] = UB_float[row][col];
   }

   public double[][] getUB_double()
   {
      return UB;
   }
   
   public String toString()
   {
     String result = "Tolerance " + tolerance;
     return result;
   }
}
