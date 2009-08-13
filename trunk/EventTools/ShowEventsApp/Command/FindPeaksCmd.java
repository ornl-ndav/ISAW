package EventTools.ShowEventsApp.Command;

public class FindPeaksCmd
{
   private boolean smoothData;
   private int     maxNumberOfPeaks;
   private int     minPeakIntensity;
   private String  logFileName;
   
   public FindPeaksCmd(boolean smoothData, int maxNumberOfPeaks,
                       int minPeakIntensity, String logFileName)
   {
      this.smoothData = smoothData;
      this.maxNumberOfPeaks = maxNumberOfPeaks;
      this.minPeakIntensity = minPeakIntensity;
      this.logFileName = logFileName;
   }
   
   public boolean getSmoothData()
   {
      return smoothData;
   }
   
   public int getMaxNumberPeaks()
   {
      return maxNumberOfPeaks;
   }
   
   public int getMinPeakIntensity()
   {
      return minPeakIntensity;
   }
   
   public String getLogFileName()
   {
      return logFileName;
   }
   
   public String toString()
   {
      return "\nSmooth Data: "   + getSmoothData()       + 
             "\nMax # Peaks: "   + getMaxNumberPeaks()   +
             "\nMin Peak Int.: " + getMinPeakIntensity() +
             "\nLog File: "      + getLogFileName();
   }
}
