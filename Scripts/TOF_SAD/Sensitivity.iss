#        Calculate Sensitivity
#
#@overview   This scripts calculates the Sensitivity data
#    then stores it in a dat file
#@algorithm  See the DetSens operator
#@param    runFileName   The name of the runfile with the flood data
#@param    SaveFileName  The name of the .dat file to save the information to
#@param    HotDetectorLevel  The level that indicates a detector is hot
#@param    DeadDetectorLevel  The level that indicates a detector is "dead"


$runFileName       LoadFileString("C:/new_das_runs\sand20337.run")  Enter Run file with info
$SaveFileName      SaveFileString("C:/test_output/sens20337.dat") Enter file to save to
$HotDetectorLevel  Float(1.4)                     Enter the level indicating a hot detector
$DeadDetectorLevel Float( .6)                   Enter the level indicating a dead detector


$Title = Calculate Sensitivity
$Command = Sensitivity
$Category=Operator,Generic,TOF_SAD,Scripts

n= load( runFileName, "SensData")

if  n <> 2
   return "Improper number of data sets loaded"
endif

DS = DetSens( SensData[1], DeadDetectorLevel, HotDetectorLevel)
PrintFlood( DS[0],SaveFileName, "Flood")
send DS[0]
display DS[0], "Contour View"
return "Success"


