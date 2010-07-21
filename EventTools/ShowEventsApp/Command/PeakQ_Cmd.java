

package EventTools.ShowEventsApp.Command;

import java.util.Vector;
import DataSetTools.operator.Generic.TOF_SCD.IPeakQ;

/**
 * This class holds the information sent as the value of a SET_PEAK_Q_LIST
 * command.
 */
public class PeakQ_Cmd
{
   private Vector<IPeakQ>       peaks;
   private Vector<float[][][]> regions;
   
   /**
    * Make a command object with the specified list of IPeakQ objects and
    * list of regions around the peaks.
    *
    * @param peaks   Vector of IPeakQ objects containing the peak positions
    *                in reciprocal space.  The number of peaks must match 
    *                the number of regions.
    * @param regions Vector of 3D float arrays containing values from the
    *                histogram around the peaks. The number of regions
    *                must match the number of peaks.
    */
   public PeakQ_Cmd( Vector<IPeakQ> peaks, Vector<float[][][]> regions )
   {
     this.peaks   = peaks;
     this.regions = regions;
   }

   /**
    *  Get the list of IPeakQ objects.
    *
    *  @return a reference to the Vector of peaks for this command.
    *          The calling code must NOT modify the Vector of peaks!
    */
   public Vector<IPeakQ> getPeaks()
   {
      return peaks;
   }

   /**
    *  Get the list of 3D arrays with values around the peaks. 
    *
    *  @return a reference to the Vector of regions for this command.
    *          The calling code must NOT modify the Vector of regions!
    */
   public Vector<float[][][]> getRegions()
   {
      return regions;
   }

   /**
    * Get a string giving the number of peaks and regions in this command
    * object.
    */
   public String toString()
   {
      String format = "Number of peaks = %d\n  Number of regions = %d\n";
      return String.format(format, peaks.size(), regions.size() );
   }
}
