#  LoadTree.iss
#
#  @overview  This script loads a DataSet, applies the OmitNullData
#             operator to remove data from dead detectors, and sends the
#             the filtered DataSet to the tree holding the files loaded
#             into ISAW.  It also pops up views of the monitor and sample
#             data and demonstrates the use of the display operator for
#             DataSets and Strings, and the use of Echo for Strings.
#
#  $Date$

$Category = Macros, Examples, Scripts ( ISAW )

$  path        DataDirectoryString   PathName
$  instrument  String(GLAD)          Instrument
$  runNumb     String(6942)          Run_Number

n=load (path&instrument&runNumb&".RUN", "vnam")
for i in [0:N-1]
  OmitNullData(vnam[i], false)
  send (vnam[i])
endfor

display (vnam[0])
display (vnam[1])

display "DataSet displays produce a viewer"
display "String displays go to the Status pane"

Echo ("Echo output goes to the command window")

return("done")
