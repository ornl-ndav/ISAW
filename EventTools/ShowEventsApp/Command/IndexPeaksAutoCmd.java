package EventTools.ShowEventsApp.Command;

/**
 *  This class has the information needed for doing the new Auto indexing
 *  using searches.
 */
public class IndexPeaksAutoCmd
{
   private float d_min;
   private float d_max;
   private float angle_step;
   private int   num_initial;
   private float tolerance;
   
   public IndexPeaksAutoCmd( float d_min,
                             float d_max, 
                             float angle_step, 
                             int   num_initial, 
                             float tolerance )
   {
      this.d_min = d_min;
      this.d_max = d_max;
      this.angle_step  = angle_step;
      this.num_initial = num_initial;
      this.tolerance   = tolerance;
   }

   public float getD_min()
   {
      return d_min;
   }

   public float getD_max()
   {
      return d_max;
   }

   public float getAngle_step()
   {
      return angle_step;
   } 

   public int getNum_initial()
   {
      return num_initial;
   }

   public float getTolerance()
   {
      return tolerance;
   }

   public String toString()
   {
      return "\nd_min : " + getD_min() +
             "\nd_max : " + getD_max() +
             "\nangle_step  : " + getAngle_step() +
             "\nnum_initial : " + getNum_initial() +
             "\nTolerance   : " + getTolerance();
   }

}
