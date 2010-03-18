#        Make incident spectrum for  Vanadium  Event Data
#  The  Vanadium is  to time focused, eliminate selected Vanadium  peaks, filter results,
#  log  binn results, convert to wave length  and save an ascii version of the
#  data set

# @param VanEventFile    Vanadium Event FileName
# @param VanPeakFileName File with Vanadium Peak infofor all
# @param Angle           Angle to rotate center detector to
# @param VanPeakFileName   File with Vanadium peak info
# @param SumGroups       Output summed groups(Note if not summed
#                           Each group will have a 3(2) col section
#                           in the resultant file
# @param saveFileName    FileName to save Ascii results

#@return  The resultant dataset


$category=Macros,Instrument Type,TOF_NPD,NEW_SNS
$title=Vanadium Incident Spectra(wl))

$VanEventFile    LoadFile(${Data_Directory})      Vanadium Event FileName
$Angle           Float(24)                    New Angle of Center Detector(degrees)

$VanPeakFileName LoadFile(${Data_Directory})  File with Vanadium Peak info


$SumGroups    Boolean( true)                Output summed groups
$saveFileName  SaveFile(${Data_Directory})  FileName to save ASCII table to



wl=true         
MinWL=.1     
MaxWL=20
NWLbins= 2000
useDefFiles=true
DetCalFile = CreateExecFileName( getSysProp("ISAW_HOME"),"InstrumentInfo/SNS/SNAP/SNAP.DETCAL", false)
BankFile =""
Mapfile  =""
firstEvent=   0
NEvents   =     10000000 
 
Outputd  =   false

MinD  =       0.2
MaxD  =       10.0
UseDmap=      false
DmapFile=    ""

FocAng  =     Angle
FocPath =     0.5
MinTime =     200
MaxTime  =    33333


LogBinning =true
firstInt  =2
Nbins=100000
PeakWidth =.01
NWidths  =4
NChanAve  =10

Filter=true
cutoff =.02
order=2
 
Ascii =true
OutputIDs =""

useGhost=false
GhostFile=""
NGhostIDs=300000
NGhostsPerID=16
SaveDetCalFile = CreateExecFileName(getSysProp("user.home"),"ISAW/tmp/save.DETCAL",false)
RotateDetectors(DetCalFile,14, SaveDetCalFile,Angle,0,0)
DetCalFile =SaveDetCalFile

return MakeSmoothedVanadiumSpectrum(VanEventFile,useDefFiles,DetCalFile,BankFile,Mapfile,firstEvent,NEvents,\
                   Outputd,MinD,MaxD,UseDmap,DmapFile,FocAng,FocPath,MinTime,MaxTime,wl,MinWL,MaxWL,NWLbins,\
                   LogBinning,firstInt,Nbins,VanPeakFileName,PeakWidth,NWidths,NChanAve,Filter,cutoff,order,\
                   Ascii,OutputIDs,SumGroups,saveFileName,useGhost,GhostFile,NGhostIDs,NGhostsPerID)
