# Beam Center Finder
#@overview- This script finds the center of the beam in a sand instrument
#@param DSfilename- The name of the DataSet used to determine the center of the beam
#@param SensFilename- the name of the file with the sensitivity data(from Flood Run)
#@param StartTimeChan- the starting time channel to use
#@param EndTimechan- the ending time channel to use
#@param Xoff- the initial guess for the X offset in cm
#@param Yoff- the intial guess for the Yoffset in cm
#@return the Vector returned by the Center operator

$ DSfilename   LoadFileString("C:\ISAW\SampleRuns\wrchen03\ins\sand19452.run")   Enter DataSet Name
$ SensFilename LoadFileString("C:\ISAW\SampleRuns\sens19878A.dat")  Enter Sensitivity FileName
$StartTimeChan  Integer(11)  Enter Starting TimeChannel
$EndTimeChan  Integer(68)   Enter EndTimeChannel
$Xoff     Float(0)    Enter Xoff estimate
$Yoff    Float(0)   Enter Yoff Estimate

n=load(DSfilename, "DS")

Sens = ReadFlood( SensFilename,128,128)
V = Center( DS[n-1],Sens[0],StartTimeChan,EndTimeChan,Xoff,Yoff)

Display V[0]
Display V[1]
Display "Finished"
Return V


