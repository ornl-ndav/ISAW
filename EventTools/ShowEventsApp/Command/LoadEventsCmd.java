package EventTools.ShowEventsApp.Command;

public class LoadEventsCmd
{
   private String eventFile;
   private String detFile;
   private String specFile;
   private String detEffFile;
   private String matFile;
   private float  maxQValue;
   private long   availableEvents;
   private long   firstEvent;
   private long   eventsToLoad;
   private long   eventsToShow;
   private int    numThreads;
   private float  scale_factor;
   
   public LoadEventsCmd( String eventFile, 
                         String detFile,
                         String specFile, 
                         String detEffFile, 
                         String matFile, 
                         float  maxQValue,
                         long   availableEvents, 
                         long   firstEvent, 
                         long   eventsToLoad, 
                         long   eventsToShow,
                         int    numThreads,
                         float  scale_factor)
   {
      this.eventFile       = eventFile;
      this.detFile         = detFile;
      this.specFile        = specFile;
      this.detEffFile      = detEffFile;
      this.matFile         = matFile;
      this.maxQValue         = maxQValue;
      this.availableEvents = availableEvents;
      this.firstEvent      = firstEvent;
      this.eventsToLoad    = eventsToLoad;
      this.eventsToShow    = eventsToShow;
      this.numThreads      = numThreads;
      this.scale_factor    = scale_factor;
   }
   public LoadEventsCmd( String eventFile, 
            String detFile,
            String specFile, 
            String detEffFile, 
            String matFile, 
            float  maxQValue,
            long   availableEvents, 
            long   firstEvent, 
            long   eventsToLoad, 
            long   eventsToShow,
            int    numThreads)
{
      this( eventFile, 
             detFile,
            specFile, 
            detEffFile, 
             matFile, 
            maxQValue,
            availableEvents, 
            firstEvent, 
            eventsToLoad, 
            eventsToShow,
            numThreads,
            -1f);

}
   public String getEventFile()
   {
      if (eventFile == null || eventFile.trim().equals(""))
        return null;

      return eventFile;
   }

   public String getDetFile()
   {
      if (detFile == null || detFile.trim().equals(""))
        return null;

      return detFile;
   }

   public String getIncSpectrumFile()
   {
      if (specFile == null || specFile.trim().equals(""))
         return null;

      return specFile;
   }
   
   public String getDetEffFile()
   {
      if (detEffFile == null || detEffFile.trim().equals(""))
        return null;

      return detEffFile;
   }
   
   public String getMatFile()
   {
      if (matFile == null || matFile.trim().equals(""))
        return null;

      return matFile;
   }

   
   public float getMaxQValue()
   {
      if (Float.isNaN( maxQValue )|| maxQValue <=0 )
        return Float.NaN;

      return maxQValue;
   }
   
   public long getAvailableEvents()
   {
      return availableEvents;
   }

   public long getFirstEvent()
   {
      return firstEvent;
   }

   public long getEventsToLoad()
   {
      return eventsToLoad;
   }

   public long getEventsToShow()
   {
      return eventsToShow;
   }

   public int getNumThreads()
   {
      return numThreads;
   }
   
   public float getScaleFactor()
   {
      return scale_factor;
   }
   
   public String toString()
   {
      return "\nEvent File  : " + getEventFile()       +
             "\nDet. File   : " + getDetFile()         +
             "\nSpec File   : " + getIncSpectrumFile() +
             "\nDet Eff File: " + getDetEffFile()      +
             "\nMatrix File : " + getMatFile()         +
             "\nMax Q Value : " + getMaxQValue()         +
             "\nNum Events  : " + getAvailableEvents() +
             "\nFirst Event : " + getFirstEvent()      +
             "\nNum to load : " + getEventsToLoad()    +
             "\nNum to show : " + getEventsToShow()    +
             "\nNum Threads : " + getNumThreads() +
             "\nScale Factor: " + getScaleFactor();
   }

}
