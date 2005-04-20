# focusNSumDtOvT.iss
#
# This script demonstrates the use of the SumLog operator used to focus and sum
# data that has been binned into constant dt/t steps.  The constants used here
# for the map, ref angle & length and resolution are what were needed on SEPD
# at IPNS in April 2005.  This will vary on instrument/run basis depending on
# setup
$Category = Macros, Utils, Examples
$Title= Focus & Sum dt/t data
# This input parameter is the name on a runfile to load
$filename    LoadFileString    Enter File Name

Display filename
Load filename, "nn"

# Define constants of the instrument
idMap=[[43:82],[3:12,14:42],[103:122],[83:102],[267:410],[123:266],[555:698],[411:554]]
refAng=[144.0,-144.0,90.0,-90.0,44.0,-44.0,22.0,-22.0]
refLen=[1.5,1.5,1.5,1.5,1.5,1.5,1.5,1.5]
res=[0.0004,0.0004,0.0008,0.0008,0.0016,0.0016,0.0032,0.0032]
#Run operator
newData = SumLog(nn[1], idMap, refAng,refLen,res)
#Send the result to the ISAW tree
Send newData
