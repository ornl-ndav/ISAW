# Beam Center Finder
#@overview- This script finds the center of the beam in a sand instrument
#@param DSfilename- The name of the DataSet used to determine the center of 
#                   the beam
#@param SensFilename- the name of the file with the sensitivity data
#                     (from Flood Run)
#@param StartTimeChan- the starting time channel to use
#@param EndTimechan- the ending time channel to use
#@param Xoff- the initial guess for the X offset in cm
#@param Yoff- the intial guess for the Yoffset in cm
#@return the Vector returned by the Center operator
#
# $Date$

$Category=Macros, Instrument Type, TOF_NSAS


$ DSfilename   LoadFileString("/IPNShome/sasi/data/sasi0017.run")   Enter DataSet Name
$ SensFilename LoadFileString("/IPNShome/sasi/GeorgeUser/sens0012.dat")  Enter Sensitivity FileName
$StartTimeChan  Integer(1)  Enter Starting TimeChannel
$EndTimeChan  Integer(60)   Enter EndTimeChannel
$Xoff     Float(0)    Enter Xoff estimate
$Yoff    Float(0)   Enter Yoff Estimate
$Xdim     Float(50.2)    Enter X dim in cm
$Ydim     Float(50.2)    Enter Y dim in cm
n=load(DSfilename, "DS")

Sens = ReadFlood( SensFilename,256,256)
V = Center( DS[n-1],Sens[0],StartTimeChan,EndTimeChan,Xoff,Yoff,Xdim, Ydim)
Display "X offset="&V[0]
Display "Y offset="&V[1]

Reverse(V[2])
ViewArray( V[2], "Center" ,V[3,0],V[3,1],V[3,2],V[3,3], "row","col","cm","cm")

return "X offset="&V[0]&"   Y offset="&V[1]

Display "Finished"
Return V




