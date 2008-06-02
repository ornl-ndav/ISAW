#
#Overview:  This is just a shell around the java operator IndexJ_base
#           to allow for the log information to be saved to a file and
#           Popped up
#
# File: Wizard/TOF_SCD/Scripts_new/JIndex_Init1.iss
#
# @param peaks     A Vector of peak-new objects
# @param OrientMat   The orientation matrix as a Vector of Vector of floats
# @param RestrRuns   the run numbers to restrict the indexing of peaks to 
#                    as a String
# @param deltah     Delta h
# @param deltak     Delta k
# @param deltal     Delta l
# @param path       path to where output information is sent
# @param expname    the name of the experiment
# @param ShowLog    If trueindex1.log will pop up in a window
#                   NOTE, the file can be viewed from the view menu 
#                   using the view text submenu and selecting index1.log

$title= Index Peaks ( Using Initial Orientation Matrix )

#------------------------ Parameters------------------------
$peaks    PlaceHolder
 
$OrientMat Array     Orientation matrix
 
$RestrRuns String    Restrict Runs 
 
$deltah    Float(.2) Delta h
 
$deltak    Float(.2) Delta k
 
$deltal    Float(.2) Delta l

$path      DataDirectoryString   Output Data Path 

$expname   String             Experiment Name

$ShowLog   Boolean(false)     Pop Up index1.log

$ShowPeaks Boolean(false)    Pop Up Peaks file
$---------------------- Code-------------------------

OpenLog( path&"index1.log")
Stats = [0,0]
V = JIndex(peaks,OrientMat,RestrRuns, deltah,deltak,deltal, Stats)
CloseLog()

if ShowLog
   ViewASCII(path&"index1.log")
else
   Display "log information is in index1.log. Use the View menu to open"
endif

append = false
#WritePeaks(path&expname&".peaks",peaks,append)
WritePeaks_new(path&expname&".peaks",peaks,append)

if ShowPeaks
   ViewASCII(path&expname&".peaks")
else
   Display "The peak file is in "& expname&".peaks"
endif

Display "indexed "&Stats[0]&" out of " &Stats[1]
Display "------------Finished Indexing -----------------------"



return  "Success"
