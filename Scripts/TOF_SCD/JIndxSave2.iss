# This is a wrapper around JIndex(Vector Peaks... that allows for
# saving the peaks and displaying JIndex log information in the
# status pane. This inputs the orientation matrix from a file
#@param  peaks   the Peaks Vector
#@param Delta  the error in h,k,and l's to allow
#@param  UseLsqMats   Use Matrices from Least Square 
#@param  OrientMatFile   the orientation matrix
#@param OrientFileNames  Matrix Files to Load
#@param   RunNums      Run Numbers
#@param RestrRuns    Run numbers to not include
#@param  peakfilename the name to save the peaks to(Use NONE if saving is not
#                             desired)
#@param  logfile     if true, the JIndex log file will be displayed in the
#                    status pane
#@return   the Vector of peaks.


$peaks     PlaceHolder        Peaks
$Delta       Float(.20)       Deltas
$UseLsqMats  Boolean(false)   Use Least Square Matrices
$OrientMat   Array            Orientation Matrix file name

$OrientFileNames Array        Matrix Files to Load
$RunNums     Array          Run Numbers
$RestrRuns   IntList          Restrict Runs
$peakfilename    SaveFileString("NONE")   Filename to save peak to
$logfile    Boolean(true)   Show log info 

$ CATEGORY = operator,Instrument Type, TOF_NSCD
$ Title = Write Peaks
if !UseLsqMats 
  OrientMat = readOrient( OrientMatFile)
  V = JIndex(peaks,OrientMat,RestrRuns, Delta,Delta,Delta)
else
   RestrNums = IntListtoArray( RestrRuns)
   if( ArrayLength(RestrNums) <=0)
     RestrNums= IntListtoArray( RunNums)
   end if
   if ArrayLength(RestrNums) <> ArrayLength(OrientFileNames)
      ErrorMessage("Improper lengths for RunNums and Orientation Files")
      return
   end if   
   for i in[0:ArrayLength(RestrNums)]
      Mat= readOrient( RestrNums[i])
      Run = ""&RestrNums[i]
      V = JIndex(peaks,Mat,Run, Delta,Delta,Delta)
   end for
end if

  Display peakfilename
  if peakfilename <>"NONE"
 
     WritePeaks(peakfilename, peaks)
  endif

if logfile
   Display "Log information"
   Display V
endif

return peaks
  

     
