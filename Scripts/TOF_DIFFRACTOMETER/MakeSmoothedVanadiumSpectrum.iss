#        Make Processed Vanadium Data Set From Event Data
#  The process has options to time focus, eliminate selected peaks, filter results, apply ghosting
#  corrections, adjust binning of results( include log binning) and save an ascii version of the
#  data set's selected( and possibly summed) groups.
# @param VanEventFile    Vanadium Event FileName
# @param useDefFiles     Use default DetCal,Bank, and Map files
# @param DetCalFile      DetCal File Name
# @param BankFile        Bank File Name
# @param Mapfile         Mapping File Name
# @param firstEvent      First Event to load
# @param NEvents         Number of Events to load   


# @param Outputd         Output in d-spacing,vs time or wl

# @param MinD            Min d-spacing
# @param MaxD            Max d-spacing
# @param UseDmap         use d map file?
# @param DmapFile        d-space Mapping file

# @param FocAng          Focused Angle(degrees)
# @param FocPath         Focused Secondary Flight Path(m)
# @param MinTime         Min Time to Focus
# @param MaxTime         Max Time to Focus
# @param wl              Convert to wavelength(The IsawEV inc spectrum is in
#                           terms of wl)
# @param MinWL           Min WaveLength
# @param MaxWL           Maximum wavelength
# @param NWLbins         Number of uniform bins

# @param LogBinning      Logarithmic Binning?
# @param firstInt        Length of first Interval
# @param Nbins           Number of bins
 
# @param VanPeakFileName File with Vanadium Peak info
# @param PeakWidth       Relativ(to d) Width of peak to cut out
# @param NWidths         "Number" of peak widths to cut(left and right)
# @param NChanAve        Number of Channels to Average to replace cut peaks

# @param Filter          Filter Vanadium spectra
# @param cutoff          Butterworth cutoff
# @param order           order of the filter

# @param Ascii           Save as ASCII
# @param OutputIDs       Group ID's to output. Blank for all
# @param SumGroups       Output summed groups(Note if not summed
#                           Each group will have a 3(2) col section
#                           in the resultant file
# @param saveFileName    FileName to save ASCII to

# @param useGhost        Subtract Ghost Histogram
# @param GhostFile       Ghost Information File Name
# @param NGhostIDs       Number of Ghost IDs
# @param NGhostsPerID    Number of Ghosts per ID
#@return  The resultant dataset


$category=Macros,Instrument Type,TOF_NPD,NEW_SNS
$title=Make Smoothed Vanadium Spectrum
$VanEventFile    LoadFile(${Data_Directory})      Vanadium Event FileName
$useDefFiles    BooleanEnable([true,0,3])   Use default DetCal,Bank, and Map files
$DetCalFile     LoadFile(${ISAW_HOME}/InstrumentInfo/) DetCal File Name
$BankFile       LoadFile(${ISAW_HOME}/InstrumentInfo/)  Bank File Name
$Mapfile        LoadFile(${ISAW_HOME}/InstrumentInfo/)  Mapping File Name
$firstEvent     Float( 0)                   First Event to load
$NEvents        Float( 10000000)            Number of Events to load 
 
 
$Outputd      BooleanEnable([false,4,5])     Output in d-spacing,vs time or wl

$MinD         Float(0.2)                    Min d-spacing
$MaxD         Float(10.0)                   Max d-spacing
$UseDmap      BooleanEnable([false,1,0])    use d map file?
$DmapFile     LoadFile(${Data_Directory})   d-space Mapping file

$FocAng       Float( 90.0)                  Focused Angle(degrees)
$FocPath      Float(0.5)                    Focused Secondary Flight Path(m)
$MinTime      Float(1000)                   Min Time to Focus
$MaxTime      Float(30000.0)                Max Time to Focus
$wl           BooleanEnable([true,3,0])     Convert to wavelength(IsawEV inc spectrum)
$MinWL        Float(0)                      Min WaveLength
$MaxWL        Float(5)                      Maximum wavelength
$NWLbins      Integer( 1000)                Number of uniform bins


$LogBinning   BooleanEnable([false,1,1])    Logarithmic Binning?
$firstInt     Float(0.2)                    Length of first Interval
$Nbins        Integer(10000)                Number of bins
 
$VanPeakFileName LoadFile(${ISAW_HOME}/Databases/VanadiumPeaks.dat)      File with Vanadium Peak info
$PeakWidth       Float(.01)                 Relativ(to d) Width of peak to cut out
$NWidths         Float(4)                   "Number" of peak widths to cut(left and right)
$NChanAve        Integer(10)                Number of Channels to Average to replace cut peaks

$Filter         BooleanEnable([true,2,0])   Filter Vanadium spectra
$cutoff         Float(.02)                  Butterworth cutoff
$order          Float(2)                    order of the filter

$Ascii        BooleanEnable([true,3,0])     Save as ASCII
$OutputIDs    IntList                         Group ID's to output. Blank for all
$SumGroups    Boolean( true)                Output summed groups
$saveFileName  SaveFile(${Data_Directory})  FileName to save ASCII to

$useGhost     BooleanEnable([false,3,0])    Subtract Ghost Histogram
$GhostFile    LoadFile(${ISAW_HOME}/InstrumentInfo/ )                   Ghost Information File Name
$NGhostIDs    Integer(300000)               Number of Ghost IDs
$NGhostsPerID  Integer(16)                  Number of Ghosts per ID

if useDefFiles
  BankFile =""
  DetCalFile =""
  MapFile =""
endif 

if Outputd
  wl = false
endif

if Outputd
  DS = Make_d_DataSet(VanEventFile,DetCalFile,BankFile,MapFile,firstEvent,NEvents,MinD,MaxD,LogBinning,firstInt,Nbins,UseDmap,DmapFile,\
                        useGhost, GhostFile, NGhostIDs, NGhostsPerID)
else
  DS = MakeTimeFocusedDataSet(VanEventFile,DetCalFile,BankFile,MapFile,firstEvent,NEvents,FocAng,FocPath,MinTime,MaxTime,LogBinning,firstInt,Nbins,\
                        useGhost, GhostFile, NGhostIDs, NGhostsPerID)
endif

RemovePeaks( DS,VanPeakFileName, PeakWidth, NWidths, NChanAve)

if Filter
   LowPassFilter( DS, cutoff,order)

endif

if wl
  DS = ToWL(DS, MinWL,MaxWL,NWLbins)
endif

if Ascii
  NdataBlocks = GetField(DS, "Num Groups")
  ClearSelect( DS)
  if StringLength(OutputIDs) <=0
      SelectByIndex( DS, "0:"&(NdataBlocks-1),"Set Select")
  else
      SelectByGroup( DS, OutputIDs)
  endif
  
  if SumGroups
      DS1= SumSel( DS,true,true)
      ClearSelect( DS1)
      SelectByIndex(DS1,"0:2000", "Set Selected") 
  else
      DS1 = DS
  endif   

  SaveASCII( DS1,false,"",saveFileName)

endif



return DS
