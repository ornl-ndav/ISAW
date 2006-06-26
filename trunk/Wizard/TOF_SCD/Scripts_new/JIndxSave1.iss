# This is a wrapper around JIndex(Vector Peaks...)that allows for
# saving the peaks and displaying a pop up of JIndex log 

#File: Wizard/TOF_SCD/Scripts_new/JIndxSave1.iss
#@param  peaks           the Peaks Vector
#@param  OrientMatFile   the orientation matrix
#@param RestrRuns        Run numbers to not include
#@param Deltah           the allowed error in h to index
#@param Deltak           the allowed error in k to index
#@param Deltal           the allowed error in l to index
#@param  path            the path for output files
#@param  expName         the name of the experiment
#@param  logfile         Pops up the file index.log when true
#@param peakFile         Pops up the indexed peaks file when true
#@return   The orientation matrix.  Also the peaks will be indexed


$peaks           PlaceHolder          Peaks
$OrientMatFile   LoadFile             Orientation Matrix file name
$RestrRuns       IntList("")          Restrict Runs
$Deltah          Float(.20)           Deltas h
$Deltak          Float(.20)           Deltas k
$Deltal          Float(.20)           Deltas l
$path            DataDirectoryString  Directory for log information
$expName         String               Experiment name
$logfile         Boolean(false)       Pop up index.log
$peakFile        Boolean(false)       Pop up Peaks File


$ Title = Index/Write Peaks
OrientMat = readOrient( OrientMatFile)
OpenLog( path&"index.log")

V = JIndex(peaks,OrientMat,RestrRuns, Deltah,Deltak,Deltal)

CloseLog()   

peakfilename = path&expName&".peaks" 
WritePeaks(peakfilename, peaks)
  
if logfile
  ViewASCII( path&"index.log")
endif 
 
 if peakFile
   ViewASCII( peakfilename )
 endif 
 
return "Success"
  

     
