#@overview This script loads a file and extracts detectors given a range of angle, converts this to q and then
# sums the converted data
#@algorithm Use ExtAtt, ToD, SumSel
#@assumptions  Loaded file has histogram data in the second dataset loaded
#@param file Name of runfile to be loaded
#@param minAngle   Minimum angle for sum
#@param maxAngle   Maximum angle for sum
#
$file    LoadFile   Enter Filename
$minAngle  Float(40)   Enter Minimum Angle
$maxAngle  Float(70)   Enter Maximum Angle
$Title=Select by Angle, Convert to D & Sum
$Category=Macros,Utils,Examples
Load file, "sample"

Display sample[1], "THREE_D"

detOfInterest = ExtAtt(sample[1], "Raw Detector Angle", true, minAngle, maxAngle)

Display detOfInterest, "THREE_D"

newInterest = ToD(detOfInterest, 0.0, 3.0, 1000)

display newInterest
sumInterest=SumSel(newInterest, false, true)
Display sumInterest, "SELECTED_GRAPH"
