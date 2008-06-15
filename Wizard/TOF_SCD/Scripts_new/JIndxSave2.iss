#       Index, Show and Write Peaks per Run
#@overview  This script will index the peaks in the peaks object using the
#           per run least squares matrices.  These matrices must be stored
#           in the directory specified by OrientFileDir. Their names must be
#           in the form ls[expname][runNum].mat

#File: Wizard/TOF_SCD/Scripts_new/JIndxSave2.iss

#@param  peaks          the Peaks Vector
#@param  Deltah         the allowed error in h to index
#@param  Deltak         the allowed error in k to index 
#@param  Deltal         the allowed error in l to index
#@param  OrientFileDir  The directory with the orientation files
#@param  RunNums        Run Numbers to use(blank if all)
#@param  RestrRuns      Run numbers to not include
#@param  logfile        if true, the JIndex log file will be displayed in the
#                           status pane
#@param  expName        the name of the experiment(and part of the filename)
#                           for some of the orientation matrices
#@return   the Vector of peaks( not used).


$peaks          PlaceHolder           Peaks
$Deltah         Float(.20)            Delta h
$Deltak         Float(.20)            Delta k
$Deltal         Float(.20)            Delta l

$OrientFileDir  DataDirectoryString   Dir with Matrices to Load
$RunNums        Array                 Run Numbers
$RestrRuns      IntList               Restrict Runs

$logfile        Boolean(true)         Pop Up Log Info 
$expName        String                Experiment name
$peakFile       Boolean(false)        Save tben Pop up Peak File

$ CATEGORY = operator,Instrument Type, TOF_NSCD
$ Title = Index/Write Peaks

Stats=[0,0]
UseLsqMats = true

path= OrientFileDir

OpenLog( path&"index.log")
if Not UseLsqMats 
  OrientMat = readOrient( OrientFileDir&"/ls"&expName&".mat")
  V = JIndex(peaks,OrientMat,RestrRuns, Deltah,Deltak,Deltal,Stats)
  NIndexed = NIndexed+Stats[0]
  NTried =NTried+Stats[1]
else
   Display "RestrRuns="&RestrRuns
   RestrNums = IntListToVector( RestrRuns)
   Display "RestrNums="&RestrNums
   Display "length="& ArrayLength(RestrNums)
   if ArrayLength(RestrNums) <=0
     RestrNums=  RunNums
   endif
   V="" 
   NIndexed=0
   NTried = 0
   for i in RestrNums
      Mat= readOrient( OrientFileDir&"/ls"&expName&i&".mat")
      Run = ""&i
      Display "Run ="&Run
      W = JIndex(peaks,Mat,Run,Deltah,Deltak,Deltal,Stats)
      NIndexed = NIndexed+Stats[0]
      NTried =NTried+Stats[1]
      #Display "Result of JIndex is "&W
      V =V & W
   endfor
   Display "Indexed "&NIndexed&" out of "& NTried
   
   LogMsg( "Indexed "&NIndexed&" out of "& NTried&"\n")
endif
CloseLog()
filename = OrientFileDir&expName&".peaks" 

#WritePeaks(filename, peaks)
WritePeaks_new(filename, peaks, false)
 
if logfile
  ViewASCII( path&"index.log")
endif

if peakFile
  ViewASCII(filename)

endif
return peaks
  

     
