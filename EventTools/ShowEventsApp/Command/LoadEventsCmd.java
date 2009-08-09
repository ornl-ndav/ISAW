package EventTools.ShowEventsApp.Command;

public class LoadEventsCmd
{
   private String eventFile;
   private String detFile;
   private String specFile;
   private String detEffFile;
   private String matFile;
   private int    availableEvents;
   private int    firstEvent;
   private long   eventsToLoad;
   private int    eventsToShow;
   
   public LoadEventsCmd( String eventFile, 
                         String detFile,
                         String specFile, 
                         String detEffFile, 
                         String matFile, 
                         int    availableEvents, 
                         int    firstEvent, 
                         long   eventsToLoad, 
                         int    eventsToShow  )
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
   }

   public String getEventFile()
   {
      return eventFile;
   }

   public String getDetFile()
   {
      return detFile;
   }

   public String getIncSpectrumFile()
   {
      if (specFile == null || specFile.equals(""))
         return null;

      return specFile;
   }
   
   public String getDetEffFile()
   {
      if (detEffFile == null || detEffFile.equals(""))
        return null;

      return detEffFile;
   }
   
   public String getMatFile()
   {
      if (matFile == null || matFile.equals(""))
        return null;

      return matFile;
   }

   public int getAvailableEvents()
   {
      return availableEvents;
   }

   public int getFirstEvent()
   {
      return firstEvent;
   }

   public long getEventsToLoad()
   {
      return eventsToLoad;
   }

   public int getEventsToShow()
   {
      return eventsToShow;
   }

   public String toString()
   {
      return "Event File  : " + eventFile       + "\n" +
             "Det. File   : " + detFile         + "\n" +
             "Spec File   : " + specFile        + "\n" + 
             "Matrix File : " + matFile         + "\n" +
             "Det Eff File: " + detEffFile      + "\n" +
             "Num Events  : " + availableEvents + "\n" + 
             "First Event : " + firstEvent      + "\n" +
             "Num to load : " + eventsToLoad    + "\n" +
             "Num to show : " + eventsToShow    + "\n";
   }

}
