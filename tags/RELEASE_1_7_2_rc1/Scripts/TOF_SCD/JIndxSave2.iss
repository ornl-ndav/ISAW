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
#@param  expName    the name of the experiment(and part of the filename)
#                   for some of the orientation matrices
#@return   the Vector of peaks.


$peaks     PlaceHolder        Peaks
$Delta       Float(.20)       Deltas
$UseLsqMats  Boolean(false)   Use Least Square Matrices
$OrientMat   Array          Orientation Matrix
$OrientFileDir  DataDirectoryString        Dir with Matrices to Load
$RunNums     Array          Run Numbers
$RestrRuns   IntList          Restrict Runs
$peakfilename    SaveFileString("NONE")   Filename to save peak to
$logfile    Boolean(true)   Show log info 
$expName    String          Experiment name
$ CATEGORY = operator,Instrument Type, TOF_NSCD
$ Title = Index/Write Peaks

if Not UseLsqMats 
  
  V = JIndex(peaks,OrientMat,RestrRuns, Delta,Delta,Delta)
else
   RestrNums = IntListtoArray( RestrRuns)
   if( ArrayLength(RestrNums) <=0)
     RestrNums= IntListtoArray( RunNums)
   end if
      
   for i in RestrNums
      Mat= readOrient( OrientFileDir&"/ls"&expName&i&".mat")
      Run = ""&i
      V =V& JIndex(peaks,Mat,Run, Delta,Delta,Delta)
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
  

     
