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
$peaks     PlaceHolder        Peaks
$OrientMat   Array            Orientation Matrix
$RestrRuns   IntList          Restrict Runs
$Delta       Float(.20)       Deltas
$path    DataDirectoryString   path to where the output information is written
$expName   String             Experiment Name
$logfile    Boolean(false)   Show log info 

$ CATEGORY = operator,Instrument Type, TOF_NSCD

V = JIndex(peaks,OrientMat,RestrRuns, Delta,Delta,Delta)


WritePeaks(path&expName&".peaks", peaks)
Display "printed peaks to file"


SS= path&"index.log"
OpenLog( SS)

LogMsg( V)
CloseLog()

if logfile
   ViewASCII( path&"index.log")
endif
return "Finished"
  

     
