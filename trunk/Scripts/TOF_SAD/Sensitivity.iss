#        Calculate Sensitivity
#
#@overview   This scripts calculates the Sensitivity data
#    then stores it in a dat file
#@algorithm  See the DetSens operator
#@param    runFileName   The name of the runfile with the flood data
#@param    SaveFileName  The name of the .dat file to save the information to
#@param    HotDetectorLevel  The level that indicates a detector is hot
#@param    DeadDetectorLevel  The level that indicates a detector is "dead"


$runFileName       LoadFileString("/IPNShome/sand/data/sand22205.run")  Enter Run file with info
$SaveFileName      SaveFileString("/IPNShome/sand/GeorgeUser/sens22205.dat") Enter file to save to
$HotDetectorLevel  Float(1.4)                     Enter the level indicating a hot detector
$DeadDetectorLevel Float( .6)                   Enter the level indicating a dead detector


$Title = Calculate Sensitivity
$Command = Sensitivity
$Category = Macros,Instrument Type, TOF_NSAS

n= load( runFileName, "SensData")

if  n <> 2
   return "Improper number of data sets loaded"
endif

DS = DetSens( SensData[1], DeadDetectorLevel, HotDetectorLevel)

PrintFlood( DS[0],SaveFileName, "Flood")
send DS[0]

Display DS[0], "Contour View"

return "Finished"
