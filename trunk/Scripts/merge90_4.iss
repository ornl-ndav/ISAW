# 
# Script to load and merge a specified set of files
# select spectra around 90 degree two theta before merging
# First specify any parameters to the script, giving the variable name,
# data type and prompt string.  A dialog box will prompt the user for values
# for these parameters

$ instrument          InstrumentNameString   Filename prefix
$ run_numbers         Array                  list numbers like [1,2:5]
#$ lowAng              Float                  Low angle for merging
#$ highAng             Float                  High angle for merging
$ path                DataDirectoryString    Path to data files

lowAng = 85.
highAng = 95.
first=true
count = 0
label=[]
for i in run_numbers
   file_name = path&instrument&i&".RUN"
#  Echo("file_name")
   load file_name,"temp"
   num_pulses=GetAttr(temp[1],"Number of Pulses")
   if num_pulses<>0
     Etime = GetAttr(temp[1], "End Time")
     Edate = GetAttr(temp[1], "End Date")
     label[count] = instrument&i&" "&Etime&" "&Edate
     Echo (label[count])
     if first
        merged_ds=ExtAtt(temp[1],"Raw Detector Angle",true,lowAng,highAng)
        SetDataLabel( merged_ds, label[count], "")
        first=false
        first_num_pulses=num_pulses
     else
        part_ds =ExtAtt(temp[1],"Raw Detector Angle",true,lowAng,highAng)
        scale_factor= first_num_pulses/(1.0*num_pulses)
        Mult(part_ds,scale_factor,false)
        merged_ds=Merge(merged_ds, part_ds)
     endif
     count = count + 1
   endif
#   Echo(instrument&" run "&i&" loaded.")
endfor

last = count - 1
for i in [0:last]
  SetAttr(merged_ds, i,"Group ID", i)
endfor

for i in [0:last]
  Echo( "Setting label to "&label[i] )
  SetDataLabel( merged_ds, label[i], ""&i )
endfor

#Display merged_ds

d_ds=ToD(merged_ds,.2,3.0,2000)
for i in [0:last]
  SetDataLabel( d_ds, label[i], ""&i )
endfor

Send d_ds
Display d_ds
ExitDialog()
#Echo ("-- end of script --")