package EventTools.ShowEventsApp.Command;

public class FindPeaksCmd
{
   private boolean smoothData;
   private boolean markPeaks;
   private int     maxNumberOfPeaks;
   private float   minPeakIntensity;
   private String  logFileName;
   
   public FindPeaksCmd( boolean smoothData,
                        boolean markPeaks,
                        int     maxNumberOfPeaks,
                        float   minPeakIntensity, 
                        String  logFileName )
   {
      this.smoothData = smoothData;
      this.markPeaks  = markPeaks;
      this.maxNumberOfPeaks = maxNumberOfPeaks;
      this.minPeakIntensity = minPeakIntensity;
      this.logFileName = logFileName;
   }
   
   public boolean getSmoothData()
   {
      return smoothData;
   }
   
   public boolean getMarkPeaks()
   {
      return markPeaks;
   }
   
   public int getMaxNumberPeaks()
   {
      return maxNumberOfPeaks;
   }
   
   public float getMinPeakIntensity()
   {
      return minPeakIntensity;
   }
   
   public String getLogFileName()
   {
      return logFileName.trim();
   }
   
   public String toString()
   {
      return "\nMark Peaks   : " + getMarkPeaks()        +
             "\nMax # Peaks  : " + getMaxNumberPeaks()   +
             "\nMin Peak Int.: " + getMinPeakIntensity() +
             "\nLog File     : " + getLogFileName()      + "\n";
   }
}
