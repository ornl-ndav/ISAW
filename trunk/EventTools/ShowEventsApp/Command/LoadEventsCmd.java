package EventTools.ShowEventsApp.Command;

public class LoadEventsCmd
{
   private String eventFile;
   private String detFile;
   private String specFile;
   private String detEffFile;
   private String matFile;
   private long   availableEvents;
   private long   firstEvent;
   private long   eventsToLoad;
   private long   eventsToShow;
   private int    numThreads;
   
   public LoadEventsCmd( String eventFile, 
                         String detFile,
                         String specFile, 
                         String detEffFile, 
                         String matFile, 
                         long   availableEvents, 
                         long   firstEvent, 
                         long   eventsToLoad, 
                         long   eventsToShow,
                         int    numThreads)
   {
      this.eventFile       = eventFile;
      this.detFile         = detFile;
      this.specFile        = specFile;
      this.detEffFile      = detEffFile;
      this.matFile         = matFile;
      this.availableEvents = availableEvents;
      this.firstEvent      = firstEvent;
      this.eventsToLoad    = eventsToLoad;
      this.eventsToShow    = eventsToShow;
      this.numThreads      = numThreads;
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
   
   public String toString()
   {
      return "\nEvent File  : " + getEventFile()       +
             "\nDet. File   : " + getDetFile()         +
             "\nSpec File   : " + getIncSpectrumFile() +
             "\nDet Eff File: " + getDetEffFile()      +
             "\nMatrix File : " + getMatFile()         +
             "\nNum Events  : " + getAvailableEvents() +
             "\nFirst Event : " + getFirstEvent()      +
             "\nNum to load : " + getEventsToLoad()    +
             "\nNum to show : " + getEventsToShow()    +
             "\nNum Threads : " + getNumThreads();
   }

}
