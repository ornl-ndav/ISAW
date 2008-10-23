# This is a wrapper around JIndex(Vector Peaks...)that allows for
# indexing saving the peaks in the new file format.
# The log file and peaks file will automatically pop up.


#@param  peakfilename    Filename for Peaks
#@param  OrientMatFile   the orientation matrix
#@param RestrRuns        Run numbers to not include
#@param Deltah           the allowed error in h to index
#@param Deltak           the allowed error in k to index
#@param Deltal           the allowed error in l to index
#@param  path            the path for output files
#@return   The orientation matrix.  Also the peaks will be indexed


$peakfilename         LoadFile                Filename for Peaks
$OrientMatFile   LoadFile             Orientation Matrix file name
$RestrRuns       IntList("")          Restrict Runs
$Deltah          Float(.20)           Deltas h
$Deltak          Float(.20)           Deltas k
$Deltal          Float(.20)           Deltas l
$path            DataDirectoryString  Directory for log information


$ Title = Index/Write Peaks
$ command = indexPeaks
$category=operator,Instrument Type,TOF_NSCD,NEW_SNS

logfile = true
peakFile = true
Peaks = ReadPeaks( peakfilename)
OrientMat = readOrient( OrientMatFile)
OpenLog( path&"index.log")

V = JIndex(peaks,OrientMat,RestrRuns, Deltah,Deltak,Deltal)

CloseLog()   

#WritePeaks(peakfilename, peaks)
WritePeaks_new(peakfilename, peaks, false)
  
if logfile
  ViewASCII( path&"index.log")
endif 
 
 if peakFile
   ViewASCII( peakfilename )
 endif 
 
return "Success"
  

     
