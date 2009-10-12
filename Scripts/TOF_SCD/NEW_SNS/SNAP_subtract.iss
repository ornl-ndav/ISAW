#  SNAP_subtract

#  Script to subtract empty cell data from sample data.

#  A. J. Schultz, October 2009

$ Category = Macros, SNAP

$  path		DataDirectoryString(/SNS/users/ajschultz/SNAP/TiZr_2009-06)	Raw Data Path
$  runNum_1		INTEGER(732)	Run Number of Data File
$  runNum_2		INTEGER(730)	Run Number of Background File
$  column_length  INTEGER(256)	Number of Pixels in one column or row
$  first_col      INTEGER(1)     	First Col of Region (min X)
$  last_col       INTEGER(256)    	Last  Col of Region (max X)
$  first_row      INTEGER(1)     	First Row of Region (min Y)
$  last_row       INTEGER(256)    	Last  Row of Region (max Y)


#  Obtain scaling factor from beam monitor data

mon_1 = OneDS(path & "SNAP_" & runNum_1 & ".nxs", 0, "")
monitorSum1 = IntegGrp(mon_1, 1, 1000.0, 16000.0)
Display "monitorSum1 = " & monitorSum1

mon_2 = OneDS(path & "SNAP_" & runNum_2 & ".nxs", 0, "")
monitorSum2 = IntegGrp(mon_2, 1, 1000.0, 16000.0)
Display "monitorSum2 = " & monitorSum2

scale = monitorSum1 / monitorSum2
Display "scale = " & scale


# Begin for loop for each of 9 detectors.

	for bank in [10:18]
	  Display "Detector Bank = " & bank
	  DSnum = bank - 9
	  ds_1 = OneDS(path & "SNAP_" & runNum_1 & ".nxs", DSnum, "")
	  ds_2 = OneDS(path & "SNAP_" & runNum_2 & ".nxs", DSnum, "")

#
#  Select the pixels that were requested
#
#  First be sure any previously selections are cleared 
#
ClearSelect( ds_1 )
ClearSelect( ds_2 )
#
#  The select the pixels in the region, by index, since
# each detector's DataSet has indices from 0 to 65535, we
# don't need to worry about what pixel IDs are in what
# area detector.
#
for col in [first_col:last_col]
  first_index = (col-1)*column_length + first_row-1
  last_index  = (col-1)*column_length + last_row-1
  range_string = "" & first_index & ":" & last_index
  SelectByIndex( ds_1, range_string, "Set Selected" )
  SelectByIndex( ds_2, range_string, "Set Selected" )
endfor

#  Scale the background spectrum
ds_2_scaled = Mult( ds_2, scale, true)

#  Subtract the background from the sample data
ds_3 = Sub( ds_1, ds_2_scaled, true)
Send ds_3

#
# End of for loop for each of 9 detectors.
	endfor
#
return "Finished"
