# This is a wrapper around JIndex(Vector Peaks... that allows for
# saving the peaks and displaying JIndex log information in the
# status pane.
#@param  peaks   the Peaks Vector
#@param  OrientMat   the orientation matrix
#@param RestrRuns    Run numbers to not include
#@param Delta  the error in h,k,and l's to allow
#@param  peakfilename the name to save the peaks to(Use NONE if saving is not
#                             desired)
#@param  logfile     if true, the JIndex log file will be displayed in the
#                    status pane
#@return   Finished


$peaks     PlaceHolder        Peaks
$OrientMat   Array            Orientation Matrix
$RestrRuns   IntList          Restrict Runs
$Delta       Float(.20)       Deltas
$peakfilename    SaveFileString("NONE")   Filename to save peak to
$logfile    Boolean(true)   Show log info 

$ CATEGORY = operator,Instrument Type, TOF_NSCD

V = JIndex(peaks,OrientMat,RestrRuns, Delta,Delta,Delta)
Display "peakfilename="&peakfilename
if peakfilename <>"NONE"
  WritePeaks(peakfilename, peaks)
  Display "printed peaks to file"
endif

if logfile
   Display "Save the Status pane"
   Display V
endif
CloseLog()
return "Finished"
  

     
