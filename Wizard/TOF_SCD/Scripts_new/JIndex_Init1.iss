#
#Overview:  This is just a shell around the java operator IndexJ_base
#           to allow for the log information to be saved to a file and
#           Popped up


$peaks    PlaceHolder
 
$OrientMat Array     Orientation matrix
 
$RestrRuns String("") he run numbers to restrict the indexing of peaks to.
 
$deltah  Float(.2) delta h
 
$deltak     Float(.2) delta k
 
$deltal    Float(.2) delta l

$path      DataDirectoryString   Path to save log information

$expname   String     Experiment Name


OpenLog( path&"index1.log");
V = JIndex(peaks,OrientMat,RestrRuns, deltah,deltak,deltal)
CloseLog()
OpenLog( path&expname&".log")
ViewASCII(path&"index1.log")
Display "------------Finished Indexing -----------------------"
return


