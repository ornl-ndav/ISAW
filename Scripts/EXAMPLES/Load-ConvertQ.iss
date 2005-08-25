#  Load-ConvertQ.iss
#
#  $Date$

$Category = Macros, Examples, Scripts ( ISAW )

$  path        DataDirectoryString   PathName
$  instrument  String(GLAD)          Instrument
$  runNumb     String(6942)          Run_Number
$  qmin        float(1.0)            Qmin
$  qmax        float(21.0)           Qmax
$  nbin        int(2000)             Num bins

n=load (path&instrument&runNumb&".RUN", "vnam")
for i in [0:N-1]
  OmitNullData(vnam[i], false)
  on error
    newds[i] = ToQ(vnam[i], qmin, qmax, nbin)
  else error
    Display "No ToQ operator in DataSet "& i
    newds[i] = vnam[i]
  end error
  send (newds[i])
  display( newds[i] )
endfor

#display (vnam[0])
#display (vnam[1])

display "DataSet displays produce a viewer"
display "String displays go to the Status pane"

Echo ("Echo output goes to the terminal window")

return("done")
