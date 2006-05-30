# This is a wrapper around JIndex(Vector Peaks... that allows for
# saving the peaks and displaying JIndex log information in the
# status pane. This inputs the orientation matrix from a file
#@param  peaks   the Peaks Vector
#@param  OrientMatFile   the orientation matrix
#@param RestrRuns    Run numbers to not include
#@param Delta  the error in h,k,and l's to allow
#@param  peakfilename the name to save the peaks to(Use NONE if saving is not
#                             desired)
#@param  logfile     if true, the JIndex log file will be displayed in the
#                    status pane
#@return   The orientation matrix.  The peaks will be indexed


$peaks     PlaceHolder        Peaks
$OrientMatFile   LoadFile     Orientation Matrix file name
$RestrRuns   IntList          Restrict Runs
$Delta       Float(.20)       Deltas
$peakfilename    SaveFileString("NONE")   Filename to save peak to which contains directory for the index log file
$logfile    Boolean(true)   Show log info

$ CATEGORY = operator,Instrument Type, TOF_NSCD
$ Title = Index/Write Peaks
OrientMat = readOrient( OrientMatFile)
V = JIndex(peaks,OrientMat,RestrRuns, Delta,Delta,Delta)
Display peakfilename
if peakfilename <>"NONE"
 
  WritePeaks(peakfilename, peaks)
endif

if logfile  AND peakfilename <>"NONE"
  
   outputpath =  fSplit(peakfilename)
   S="/"
   if EndsWith( outputpath[0],S)
      S=""
   endif 
   OpenLog( outputpath[0]&S&index&".log")
    LogMsg( V)
  

endif
if logfile
  Display "--------------- index log ---------------------"
  Display V
return OrientMat
  

     
