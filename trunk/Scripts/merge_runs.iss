# 
# Script to load and merge a specified set of files
# select spectra around 90 degree two theta before merging
# First specify any parameters to the script, giving the variable name,
# data type and prompt string.  A dialog box will prompt the user for values
# for these parameters
# $Date$

$ run_numbers         Array                  Enter run numbers like [1,2:5]
$ lowAng              Float                  Low angle for merging
$ highAng             Float                  High angle for merging
$ path                DataDirectoryString    Path
$ instrument          InstrumentNameString   Instrument

first=true
for i in run_numbers
   file_name = path&instrument&i&".RUN"
   Echo(file_name)
   load file_name,"temp"
   num_pulses=GetAttr(temp[1],"Number of Pulses")
   if first
      merged_ds=ExtAtt(temp[1],"Raw Detector Angle",true,lowAng,highAng)
      first=false
      first_num_pulses=num_pulses
   elseif num_pulses<>0
      part_ds =ExtAtt(temp[1],"Raw Detector Angle",true,lowAng,highAng)
      scale_factor= first_num_pulses/(1.0*num_pulses)
      Mult(part_ds,scale_factor,false)
      merged_ds=Merge(merged_ds,part_ds)
   endif
#   Echo(instrument&" run "&i&" loaded.")
endfor

SetDataLabel(merged_ds,"Run Number","")
Display merged_ds
d_ds=ToD(merged_ds,.2,3.0,2000)
SetDataLabel(d_ds,"Run Number","")
Send d_DS
