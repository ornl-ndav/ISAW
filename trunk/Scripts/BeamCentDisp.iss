#                     Beam Center Finder
#@overview- This script finds the center of the beam in a sand instrument and displays the
#   Summed area data to compare
#@param DSfilename- The name of the DataSet used to determine the center of the beam
#@param SensFilename- the name of the file with the sensitivity data(from Flood Run)
#@param StartTimeChan- the starting time channel to use
#@param EndTimechan- the ending time channel to use
#@param Xoff- the initial guess for the X offset in cm
#@param Yoff- the intial guess for the Yoffset in cm
#

$ DSfilename   LoadFileString("C:\ISAW\SampleRuns\sand19879.run")   Enter DataSet Name
$ SensFilename LoadFileString("C:\ISAW\SampleRuns\sens19878.dat")  Enter Sensitivity FileName
$StartTimeChan  Integer(11)  Enter Starting TimeChannel
$EndTimeChan  Integer(68)   Enter EndTimeChannel
$Xoff     Float(0)    Enter Xoff estimate
$Yoff    Float(0)   Enter Yoff Estimate
$Command =CenterDisplay
$Title= Find and Show Center
$Category=Operator;Generic;TOF_SAD
V =BeamCenter(DSfilename, SensFilename, startTimeChan,EndTimeChan, Xoff,Yoff)
Display "X offset="&V[0]
Display "Y offset="&V[1]
ViewArray( V[2], "Center" ,V[3,0],V[3,1],V[3,2],V[3,3], "row","col","cm","cm")
return "Success"

