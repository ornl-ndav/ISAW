# Load File
#    Can do different operations
# $Date$
$  path      DataDirectoryString   PathName
$  instrument   String    Instrument
$  runNumb     String   Run_Number

n=load (path&instrument&runNumb&".RUN", "vnam")
for i in [0:N-1]
  send (vnam[i])
endfor



