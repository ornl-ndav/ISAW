package EventTools.ShowEventsApp.Command;

public class LoadEventsCmd
{
   private String eventFile;
   private String detFile;
   private String specFile;
   private String detEffFile; 
   private String bankFile;
   private String IDmapFile;
   private String matFile;
   private float  absorption_radius;
   private float  absorption_smu;
   private float  absorption_amu;
   private float  maxQValue;
   private long   availableEvents;
   private long   firstEvent;
   private long   eventsToLoad;
   private long   eventsToShow;
   private int    numThreads;
   private float  scale_factor;

   /**
    *  Constructor 
    * @param eventFile    The name of the file to load 
    * @param detFile      The detectorFile(DETCAL)
    * @param specFile     The name of the spec File
    * @param detEffFile   The name of the detector efficiency file private 
    * @param bankFile     The name of the file with bank vs pixel_id's
    * @param IDMapFile    The name of the file that maps DAS ID's to NeXus ID's
    * @param matFile      The name of a matrix file
    * @param radius       The radius for the absorption correction
    * @param smu          The total scattering for absorption correction
    * @param amu          True absorption at lambda = 1.8 Angstoms
    * @param maxQValue    The maximum Q value to load
    * @param availableEvents The number of events in the file
    * @param firstEvent      The first event to load
    * @param eventsToLoad    The number of events to load
    * @param nEventsToShow   The number of events to show in the 3D view
    * @param numThreads      The number of theads to use when loading the file
    * @param scale_factor    1/protons on target, or -1 if not available
    */
   public LoadEventsCmd( String eventFile, 
                         String detFile,
                         String specFile, 
                         String detEffFile, 
                         String bankFile,
                         String IDmapFile,
                         String matFile, 
                         float  radius,
                         float  smu,
                         float  amu,
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
      
      this.bankFile         = bankFile ;
      this.IDmapFile        = IDmapFile ;
      this.matFile          = matFile;
      this.absorption_radius= radius;
      this.absorption_smu   = smu;
      this.absorption_amu   = amu;
      this.maxQValue        = maxQValue;
      this.availableEvents  = availableEvents;
      this.firstEvent       = firstEvent;
      this.eventsToLoad     = eventsToLoad;
      this.eventsToShow     = eventsToShow;
      this.numThreads       = numThreads;
      this.scale_factor     = scale_factor;
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
   

   public String getBankFile()
   {
      if (bankFile == null || bankFile.trim().equals(""))
        return null;

      return bankFile;
   }
   

   public String getIDMapFile()
   {
      if (IDmapFile == null || IDmapFile.trim().equals(""))
        return null;

      return IDmapFile;
   }
   public String getMatFile()
   {
      if (matFile == null || matFile.trim().equals(""))
        return null;

      return matFile;
   }

   
   public float getAbsorptionRadius()
   {
      if (Float.isNaN( absorption_radius )|| absorption_radius < 0 )
        return Float.NaN;

      return absorption_radius;
   }

   public float getAbsorptionSMU()
   {
      if (Float.isNaN( absorption_smu )|| absorption_smu < 0 )
        return Float.NaN;

      return absorption_smu;
   }

   public float getAbsorptionAMU()
   {
      if (Float.isNaN( absorption_amu )|| absorption_amu < 0 )
        return Float.NaN;

      return absorption_amu;
   }


   public float getMaxQValue()
   {
      if ( Float.isNaN( maxQValue ) || maxQValue <= 0 )
        return 1000000;                                 // use all Q's

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
             "\nBank File   : " + getBankFile()      +
             "\nID map File : " + getIDMapFile()      +
             "\nMatrix File : " + getMatFile()         +
             "\nAbsorpRadius: " + getAbsorptionRadius()+
             "\nTotalAbsorp : " + getAbsorptionSMU() +
             "\nAbsorpTrue  : " + getAbsorptionAMU()  +
             "\nMax Q Value : " + getMaxQValue()         +
             "\nNum Events  : " + getAvailableEvents() +
             "\nFirst Event : " + getFirstEvent()      +
             "\nNum to load : " + getEventsToLoad()    +
             "\nNum to show : " + getEventsToShow()    +
             "\nNum Threads : " + getNumThreads() +
             "\nScale Factor: " + getScaleFactor();
   }

}
