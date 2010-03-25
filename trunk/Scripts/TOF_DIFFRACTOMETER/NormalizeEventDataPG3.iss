#       Normalize time focused Event Data  with background, protons on target,and vanadium runs
#  
#  data set's selected( and possibly summed) groups.
# @param EventFile         Event FileName
# @param ProtEvents        Protons on Target for Event run
# @param useBackGround     Use Sample Backgroun run?
# @param BackEventFile     Background FileName
# @param BackEvents        Protons on Target for Background run
# @param useVanadium       Normalize with Vanadium 
# @param VanEventFile      Vanadium FileName
# @param VanEvents         Protons on Target for Vanadium run
# @param useVanBack        Use Vanadium backgroun run
# @param VanBackEventfile  Vanadium background FileName
# @param VanbackEvents     Protons on target for Vanadium backgroun

# @param useDefFiles       Use default DetCal,Bank, and Map files?
# @param DetCalFile        DetCal File Name
# @param BankFile          Bank File Name
# @param Mapfile           Mapping File Name
# @param firstEvent        First Event to load
# @param NEvents           Number of Events to load 
# @param FocAng            Focused Angle(degrees)
# @param FocPath           Focused Secondary Flight Path(m)
# @param MinTime           Min Time to Focus
# @param MaxTime           Max Time to Focus
# @param LogBinning        Logarithmic Binning?
# @param firstInt          Length of first Interval
# @param Nbins             Number of bins 

# @param useGhost          Subtract Ghost Histogram
# @param GhostFile         Ghost Information File Name
# @param NGhostIDs         Number of Ghost IDs
# @param NGhostsPerID      Number of Ghosts per ID

#@return  The resultant dataset


$category=Macros,Instrument Type,TOF_NPD,NEW_SNS
$title=Normalized Event Data(PG3)
$EventFile         LoadFile( ${Data_Directory})             Event FileName
$ProtEvents        Float(100)                              Protons on Target for Event run
$useBackGround     BooleanEnable([true,2,0])               Use Sample Background run
$BackEventFile     LoadFile(${Data_Directory})             Background FileName
$BackEvents        Float(100)                              Protons on Target for Background run
$useVanadium       BooleanEnable([true,5,0])               Normalize with Vanadium 
$VanEventFile      LoadFile( ${Data_Directory})             Vanadium FileName
$VanEvents         Float(100)                              Protons on Target for Vanadium run
$VanPeakFile       LoadFile(${ISAW_HOME}/Databases/VanadiumPeaks.dat)  File with Vanadium peaks to eliminate
$useVanBack        BooleanEnable([true,2,0])               Use Vanadium background run
$VanBackEventfile  LoadFile(${Data_Directory})             Vanadium background FileName
$VanbackEvents     Float(100)                             Protons on target for Vanadium background

$useDefFiles       BooleanEnable([true,0,3])               Use default DetCal,Bank, and Map files
$DetCalFile        LoadFile(${ISAW_HOME}/InstrumentInfo/SNS/PG3/PG3.DetCal)  DetCal File Name
$BankFile          LoadFile(${ISAW_HOME}/InstrumentInfo/SNS/PG3/PG3_bank.xml)  Bank File Name
$Mapfile           LoadFile(${ISAW_HOME}/InstrumentInfo/SNS/PG3/PG3_TS.dat)  Mapping File Name
$firstEvent        Float( 0)                               First Event to load
$NEvents           Float( 10000000)                        Number of Events to load 

$FocAng            Float( 90.0)                            Focused Angle(degrees)
$FocPath           Float(0.5)                              Focused Secondary Flight Path(m)
$MinTime           Float(1000)                             Min Time to Focus
$MaxTime           Float(30000.0)                          Max Time to Focus
$LogBinning        BooleanEnable([false,1,1])              Logarithmic Binning?
$firstInt          Float(0.2)                              Length of first Interval
$Nbins             Integer(10000)                          Number of bins 

$useGhost          BooleanEnable([false,3,0])              Subtract Ghost Histogram
$GhostFile         LoadFile(${ISAW_HOME}/InstrumentInfo/ ) Ghost Information File Name
$NGhostIDs         Integer(300000)                         Number of Ghost IDs
$NGhostsPerID      Integer(16)                             Number of Ghosts per ID

if useDefFiles
  BankFile =""
  DetCalFile =""
  MapFile =""
endif 

EvDS =MakeTimeFocusedDataSet( EventFile, DetCalFile,BankFile,MapFile,firstEvent,NEvents,FocAng, FocPath,MinTime,MaxTime,\
                             LogBinning,firstInt,Nbins,useGhost, NGhostIDs, NGhostsPerID)
DS = EvDS/ProtEvents

if  useBackground
   BackDS =MakeTimeFocusedDataSet( BackEventFile, DetCalFile,BankFile,MapFile,firstEvent,NEvents,FocAng,\
                              FocPath,MinTime,MaxTime,LogBinning,firstInt,Nbins,useGhost,\
                              NGhostIDs, NGhostsPerID)
    DS =(DS - BackDS/BackEvents)
endif

if useVanadium
    
    VanDS =MakeSmoothedVanadiumSpectrum(VanEventFile,useDefFiles, DetCalFile,BankFile,MapFile,firstEvent,NEvents,false,\
                             0,0,false,"",FocAng,FocPath,MinTime,MaxTime,false,0,0,0,LogBinning,firstInt,Nbins,VanPeakFile,\
                            .01,4,10,true,.02,2,false,"",false,"",useGhost,GhostFile,NGhostIDs, NGhostsPerID)

 
    if useVanBack
        VanBackDS =MakeTimeFocusedDataSet(VanBackEventfile, DetCalFile,BankFile,MapFile,firstEvent,NEvents,FocAng,FocPath,MinTime,MaxTime,LogBinning,\
                                          firstInt,Nbins,useGhost,NGhostIDs, NGhostsPerID)
        VanDS = VanDS - VanBackDS/VanbackEvents

    endif

    DS = DS/VanDS

endif

return DS

