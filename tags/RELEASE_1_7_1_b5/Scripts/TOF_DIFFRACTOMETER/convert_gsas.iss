# 
# Script to load and save runfiles as gsas files
# $Date$

$ run_numbers         Array                  Enter run numbers like [1,2:5]
$ path                DataDirectoryString    Path
$ instrument          InstrumentNameString   Instrument

for i in run_numbers
   file_name = path&instrument&i&".run"
   Echo(file_name)
   load file_name,"temp"
   SaveGSAS(temp[0],temp[1],path&instrument&i&".gda",false,true)
endfor
