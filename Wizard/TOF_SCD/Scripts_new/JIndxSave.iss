# This is a wrapper around JIndex(Vector Peaks... that allows for
# saving the peaks and displaying JIndex log information in the
# status pane.

#File: Wizard/TOF_SCD/Scripts_new/JIndexSave.iss

#@param  peaks   the Peaks Vector
#@param  OrientMat   the orientation matrix
#@param RestrRuns    Run numbers to not include
#@param Delta  the error in h,k,and l's to allow
#@param  peakfilename the name to save the peaks to(Use NONE if saving is not
#                             desired)
#@param  logfile     if true, the JIndex log file will be displayed in the
#                    status pane
#@return   Finished

$title=Index Peaks
$peaks       PlaceHolder           Peaks
$OrientMat   Array                 Orientation Matrix
$RestrRuns   IntList               Restrict Runs
$Deltah       Float(.20)           Delta h
$Deltak       Float(.20)           Delta k
$Deltal       Float(.20)           Delta l
$path        DataDirectoryString   Output Data Path 
$expName     String                Experiment Name
$logfile     Boolean(false)        Pop Up Log Info 
$peakFile    Boolean( false)       Pop Up Peaks File

$ CATEGORY = operator,Instrument Type, TOF_NSCD
SS= path&"index.log"
OpenLog( SS)
V = JIndex(peaks,OrientMat,RestrRuns, Deltah,Deltak,Deltal)
CloseLog()

#WritePeaks(path&expName&".peaks", peaks)
WritePeaks_new(path&expName&".peaks", peaks, false)
Display "printed peaks to file"

if logfile
   ViewASCII( path&"index.log")
endif

if peakFile
   ViewASCII( path&expName&".peaks")
endif
return "Finished"
  

     
