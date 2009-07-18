#
#  Script to sum the pixels from a rectangular region of
#  an area detector at the SNS.  This script assumes
#  that the pixels in the area detector are ordered by
#  columns. 
#
#  Modified for SNAP data.
#  A. J. Schultz, December, 2008
#  D. Mikkelson, January, 2009
#
#  Modified for multiple detectors.
#  A. J. Schultz, July, 2009

$  path		DataDirectoryString(/SNS/users/ajschultz/SNAP/TiZr_2009-06)	Raw Data Path
$  runNum_1	INTEGER(732)		Run Number of Data File
#$  runNum_2	INTEGER(730)		Run Number of Background File
#$  data_set       SampleDataSet   Data Set
$  column_length  INTEGER(256)    Number of Pixels in one column
$  first_col      INTEGER(10)     First Col of Region (min X)
$  last_col       INTEGER(245)    Last  Col of Region (max X)
$  first_row      INTEGER(10)     First Row of Region (min Y)
$  last_row       INTEGER(245)    Last  Row of Region (max Y)

#
#  Load a data file
#
Load (path & "SNAP_" & runNum_1 & ".nxs", "ds")

# Begin for loop for each of 9 detectors.

	for bank in [10:18]
	  Display "Detector Bank = " & bank
	  data_set = ds[bank-9]

#
#  Select the pixels that were requested
#
#  First be sure any previously selections are cleared 
#
ClearSelect( data_set )
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
  SelectByIndex( data_set, range_string, "Set Selected" )
endfor

#  Sum up the spectra from the region and send the data
#  to the Isaw tree
#
sum_ds = SumSel(data_set, true, true)
Send sum_ds

#
#  Display the sum of the selected pixels
#
Display sum_ds, "Selected Graph View"
#
#  Save spectrum to an ASCII text file
#
SaveASCII(sum_ds, false, "%10.0f %10.0f", path & "Run" & runNum_1 & "_Bank" & bank & ".asc")
#
#
# End of for loop for each of 9 detectors.
	endfor
#
return "Finished"
