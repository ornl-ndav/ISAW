#
# Rudimentary test script for the Display system.  
#
# $Date$
#

$  path        DataDirectoryString   Path to Test Data
$  runfile     String(GPPD12358.RUN) Test Data File

load (path&runfile, "datasets")

SelectByIndex(datasets[1],"50","Set Selected")
SelectByIndex(datasets[1],"55","Set Selected")

screen_dev =DisplayDevice("Screen")
preview_dev=DisplayDevice("Preview")
file_dev   =DisplayDevice("File","TestFile.jpg")

displayable=Displayable(datasets[1],"Graph")

setLineAttribute( displayable, 1, "Line Color", "Blue")
setLineAttribute( displayable, 2, "Line Color", "Red")

DisplayGraph(screen_dev,displayable,true)
#DisplayGraph(preview_dev,displayable,false)

#DisplayGraph(file_dev,displayable,false)
#print(file_dev)
