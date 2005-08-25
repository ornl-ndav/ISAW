#@overview This script loads a file and extracts detectors given a range of angle, converts this to q and then
# sums the converted data
#@algorithm Use ExtAtt, ToD, SumSel
#@assumptions  Loaded file has histogram data in the second dataset loaded
#@param file Name of runfile to be loaded
#@param minAngle   Minimum angle for sum
#@param maxAngle   Maximum angle for sum
#
$file      LoadFile    Enter Filename
$minAngle  Float(40)   Enter Minimum Angle
$maxAngle  Float(70)   Enter Maximum Angle

$Title=Select by Angle, Convert to D & Sum

$Category = Macros, Examples, Scripts ( ISAW )

#Load the selected file
Load file, "sample"

#Display the data histogram in 3D viewer
Display sample[1], "THREE_D"

#Extract spectra that lie between the specified angle
detOfInterest = ExtAtt(sample[1], "Raw Detector Angle", true, minAngle, maxAngle)

#Display the extracted data in 3D Viewer
Display detOfInterest, "THREE_D"

#Convert the extracted data to d spacing.  new data will be from 0.0 to 3.0
#invAngs with 1000 data points.
newInterest = ToD(detOfInterest, 0.0, 3.0, 1000)

#Display the d-space data with an image viewer
display newInterest

#Sum all of the detectors in the converted DataSet, The second argument 
#tells to sum data that is not selected (all of the data),  the third argument
#says to make a new dataset
sumInterest=SumSel(newInterest, false, true)

#Display the summed data in the selected graph view.
Display sumInterest, "SELECTED_GRAPH"

Return "Done"

