#Ashfia Huq November 2004 
# Script to extract Monitor counts and Number of pulses from a series of run files.
# $Date$

$Category=Macros, Instrument Type, TOF_NPD


$run_numbers		Array([25256:25257])          		Enter run numbers like [21378]
$path                	DataDirectoryString(C:/material)	Inputname
$path_archive		DataDirectoryString(C:/material)	path_archive
$instrument          	InstrumentNameString    		Instrument
  
$Current		Boolean(true)		Is this a Current Run ?


 for i in run_numbers

	if Current == true
  		file_name = path&instrument&i&".RUN"
  		Echo(file_name)
  		load file_name,"temp"

	elseif Current == false
		file_name = path_archive&instrument&i&".RUN"
  		Echo(file_name)
  		load file_name,"temp"
	endif


#Write the monitor information
	ds_mon_downstream = GetAttr(temp[0],0,"Total Count" )
	ds_mon_upstream = GetAttr(temp[0],1,"Total Count")
	ds_pulse=GetAttr(temp[1], "Number of Pulses")


#Display 

Display "Run Number,pulse,DSmon,USmon = "&i&","  &ds_pulse&","  &ds_mon_downstream&","    &ds_mon_upstream&"," 
#Display "Number of Pulses =" &ds_pulse 
#Display "Down Stream Monitor Counts = " &ds_mon_downstream
#Display "Up Stream Monitor Counts = "&ds_mon_upstream

endfor	

Display "Done"
