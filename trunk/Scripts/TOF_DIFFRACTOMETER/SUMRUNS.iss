# Sum multiple Run files and Save as a gsas file
# $Date$

#This script only works for files accessed by the remote server
#

$host            String                     Host
$port            Int                        port
$instrument      InstrumentNameString       instrument
$Server          ServerTypeString           Server      
$runs            Array([1109,1123:1125])    Run numbers(use a:b for ranges)
$extension       String(.run)               file name extensions
$outfile         String                     Enter Output filename


first = true
for  x in runs 
  fileName = instrument&x&extension  
  load LoadRemote( host,port,"","",filename, Server ),"DataSet"
  if  first 
     SummedSet = DataSet[1]
     first = false
  else
     SummedSet = SummedSet + DataSet[1]
  endif
  Display DataSet[1]
endfor

#Now Save the resultant Data Set to a gsas file
Display SummedSet
SaveGSAS(SummedSet,outfile)
return "Success"
     




