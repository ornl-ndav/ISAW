#        Calculate Sensitivity
#
#@overview   This scripts calculates the Sensitivity data
#    then stores it in a dat file
#@algorithm  See the DetSens operator
#@param    runFileName   The name of the runfile with the flood data
#@param    SaveFileName  The name of the .dat file to save the information to
#@param    HotDetectorLevel  The level that indicates a detector is hot
#@param    DeadDetectorLevel  The level that indicates a detector is "dead"


$runFileName       LoadFileString("C:/sand_lpsd_runs/sand22403.run")  Enter Run file with info
$SaveFileName      SaveFileString("C:/sand_lpsd_runs/sens22403.dat") Enter file to save to
$HotDetectorLevel  Float(1.5)                     Enter the level indicating a hot detector
$DeadDetectorLevel Float( .5)                   Enter the level indicating a dead detector
$num_rows          Integer(16389)                   Enter the num of rows in the LPSDs
$num_cols          Integer(18948)                   Enter the num of cols in the LPSDs

$Title = lpsd Calculate Sensitivity
$Command = lpsd_Sensitivity
$Category = Operator, Instrument Type, TOF_NSAS

n= load( runFileName, "SensData")
#send SensData[1]
if  n <> 2
   return "Improper number of data sets loaded"
endif
 # SensData[1] = ExtAtt(SensData[1], "Group ID", true, 16389.0, 18948.0)
 #SensData[1] = ExtAtt(SensData[1], "Group ID", true, 16401.0, 18960.0)
 SensData[1] = ExtAtt(SensData[1], "Group ID", true, 16389.0, 18948.0)
#send SensData[1]
DS = LPSDSens( SensData[1], DeadDetectorLevel, HotDetectorLevel, num_rows,num_cols)
send DS[0]
#PrintFlood( DS[0],SaveFileName, "Flood")


Display DS[0], "3D View"

return "Finished"

