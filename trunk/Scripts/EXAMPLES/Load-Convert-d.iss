#  Load-Convert-d.iss
#
#  $Date$

$Category = Operator, Utils, Examples

$  path        DataDirectoryString   PathName
$  instrument  String(GPPD)          Instrument
$  runNumb     String(12358)         Run_Number
$  dmin        float(0.4)            d-min
$  dmax        float(2.5)            d-max
$  nbin        int(2000)             Num bins

n=load (path&instrument&runNumb&".RUN", "vnam")
for i in [0:N-1]
  OmitNullData(vnam[i], false)
  on error
    newds[i] = ToD(vnam[i], dmin, dmax, nbin)
  else error
    Display "No ToD operator in DataSet "& i
    newds[i] = vnam[i]
  end error
  send (newds[i])
  display( newds[i] )
endfor

display "DataSet displays produce a viewer"
display( newds[1],"THREE_D")
display( newds[1],"Instrument Table")
display "String displays go to the Status pane"

Echo ("Echo output goes to the command pane")

return("done")
