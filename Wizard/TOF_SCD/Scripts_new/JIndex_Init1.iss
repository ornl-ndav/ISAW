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
#                       as a String
# @param deltah     delta h
# @param deltak     delta k
# @param deltal      delta l
# @param path      path to where output information is sent
# @param expname   the name of the experiment
# @param ShowLog   If trueindex1.log will pop up in a window
#                   NOTE, the file can be viewed from the view menu 
#                   using the view text submenu and selecting index1.log

$title= Index Peaks
$peaks    PlaceHolder
 
$OrientMat Array     Orientation matrix
 
$RestrRuns  String the run numbers to restrict the indexing of peaks to.
 
$deltah  Float(.2) delta h
 
$deltak     Float(.2) delta k
 
$deltal    Float(.2) delta l

$path      DataDirectoryString   Path to save log information

$expname   String     Experiment Name

$ShowLog   Boolean( false)    Show index1.log


OpenLog( path&"index1.log");
V = JIndex(peaks,OrientMat,RestrRuns, deltah,deltak,deltal)
CloseLog()

OpenLog( path&expname&".log", true)
if ShowLog
   ViewASCII(path&"index1.log")
else
   Display "log information is in index1.log. Use the View menu to open"
endif
Display "------------Finished Indexing -----------------------"

return  "Success"


