#
# Rudimentary test script for the Display system.  
#
# $Date$
#

$  path        DataDirectoryString   Path to Test Data
$  runfile     String(GPPD12358.RUN) Test Data File
$ PrintName    PrinterName       Name of Printer
load (path&runfile, "datasets")

SelectByIndex(datasets[1],"50","Set Selected")
SelectByIndex(datasets[1],"55","Set Selected")

screen_dev =DisplayDevice("Screen")
preview_dev=DisplayDevice("Preview")
file_dev   =DisplayDevice("File","C:/TestFile.jpg")
Display "Screen Bounds"& getBounds(screen_dev)
#Display "Screen Bounds"& getBounds(file_dev)
Display "preview Bounds"& getBounds(preview_dev)
displayable=Displayable(datasets[1],"Graph")

setLineAttribute( displayable, 1, "Line Color", "Blue")
setLineAttribute( displayable, 1, "Mark type", "plus")
setLineAttribute( displayable, 1, "Mark color", "black")
setLineAttribute( displayable, 1, "transparent", "true")
setLineAttribute( displayable, 2, "Line Color", "Red")

DisplayGraph(screen_dev,displayable,true)
DisplayGraph(preview_dev,displayable,false)

DisplayGraph(file_dev,displayable,false)
print(file_dev)
#-----------Printer testing-------------
print_dev = DisplayDevice("Printer", PrintName)
Display "printer Bounds"& getBounds(print_dev)

DisplayGraph( print_dev, displayable, false)
print(print_dev)
#   ------ multi object printing------
close( print_dev)
print_dev = DisplayDevice("Printer", PrintName)
setDeviceAttribute( print_dev, "orientation","landscape")
PBounds =ToVec( VectorTo_intArray(getBounds(print_dev)))
w=2
h=2
w=PBounds[0]
h=PBounds[1]/2
setRegion( print_dev, 0,0,w,h)
DisplayGraph( print_dev, displayable, false)
setRegion( print_dev, 0,h,w,h)
DisplayGraph( print_dev, displayable, false)
print( print_dev)

Display "DONE"
