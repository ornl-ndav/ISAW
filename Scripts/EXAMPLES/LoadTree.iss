#  LoadTree.iss
#  $Date$
#
$  path      DataDirectoryString   PathName
$  instrument   String(GLAD)    Instrument
$  runNumb     String(6942)   Run_Number

n=load (path&instrument&runNumb&".RUN", "vnam")
for i in [0:N-1]
  OmitNullData(vnam[i], false)
  send (vnam[i])
endfor

display (vnam[0])
display (vnam[1])

display "DataSet displays produce a viewer"
display "String displays go to the Status pane"

Echo ("Echo output goes to the command pane")

return("done")
