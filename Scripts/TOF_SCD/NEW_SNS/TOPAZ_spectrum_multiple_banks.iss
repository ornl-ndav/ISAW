#  TOPAZ_spectrum_multiple_banks

#  Script to obtain spectrum for each detector (Bank) and
#  write output to ASCII files. Input is a data file of
#  vanadium or TiZr, and a background file.

#  Based on SumXYvsT_SNS_multiple_banks.iss script.

#  A. J. Schultz, July 2009
#  TOPAZ version: December 2009

#  Added Savitzky-Golay Filter for smoothing
#  A. J. Schultz, August 2010


########## Comments from SumXYvsT_SNS_multiple_banks.iss ##########
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
###################################################################

$ Category = Macros, Single Crystal

$  path           DataDirectoryString(/SNS/users/ajschultz/spectrum/TOPAZ_1268_nxs/Savitzky-Golay/)   Raw Data Path
$  runNum_1       INTEGER(1268)    Run Number of Data File
$  runNum_2       INTEGER(1270)    Run Number of Background File
$  column_length  INTEGER(256)    Number of Pixels in one column
$  first_col      INTEGER(10)     First Col of Region (min X)
$  last_col       INTEGER(245)    Last  Col of Region (max X)
$  first_row      INTEGER(10)     First Row of Region (min Y)
$  last_row       INTEGER(245)    Last  Row of Region (max Y)
$  number_of_detectors INTEGER(14) Number of detectors
$  doSmoothing    BooleanEnable([true,3,0])   Apply Savitzky-Golay smoothing filter
$  points_left    INTEGER(10)     Smoothing filter: Number of points left of center
$  points_right   INTEGER(10)     Smoothing filter: Number of points right of center
$  poly_degree    INTEGER(3)      Smoothing filter: Degree of smoothing polynomial


#  Obtain scaling factor from beam monitor data

ds_1 = OneDS(path & "TOPAZ_" & runNum_1 & ".nxs", 0, "")
Send ds_1
monitorSum1 = IntegGrp(ds_1, 2, 1000.0, 16000.0)
Display "monitorSum1 = " & monitorSum1

ds_2 = OneDS(path & "TOPAZ_" & runNum_2 & ".nxs", 0, "")
monitorSum2 = IntegGrp(ds_2, 2, 1000.0, 16000.0)
Display "monitorSum2 = " & monitorSum2

scale = monitorSum1 / monitorSum2

Display "scale = " & scale


# Begin for loop for each detector.

	for bank in [1:number_of_detectors]
	  Display "Detector Bank = " & bank
	  DSnum = bank
	  ds_1 = OneDS( path & "TOPAZ_" & runNum_1 & ".nxs", DSnum, "" )
	  ds_2 = OneDS( path & "TOPAZ_" & runNum_2 & ".nxs", DSnum, "" )

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

#  Sum up the spectra from the region and send the data
#  to the Isaw tree
#
sum_ds_1 = SumSel(ds_1, true, true)
sum_ds_2 = SumSel(ds_2, true, true)

#
#  Scale the background spectrum
sum_ds_2_scaled = Mult( sum_ds_2, scale, true)

#  Subtract the background from the TiZr or V data to 
#  obtain the spectrum
spectrum = Sub( sum_ds_1, sum_ds_2_scaled, true)

#  Convert counts to counts per microsecond
newspec = DivideByDeltaX( spectrum, true )

if doSmoothing
#  Apply Savitzky-Golay Filter
    GroupID = GetAttr( newspec, 0, "Group ID" )
    Display "Group ID = " & GroupID
    #points_left = 10
    #points_right = 10
    #poly_degree = 3
    Display "*** Applying Savitzky-Golay Filter with points_left = " & points_left & "; points_right = " & points_right & "; poly_degree = " & poly_degree
    SavitzkyGolayFilter( newspec, points_left, points_right, poly_degree, ""&GroupID , 0., 20000., false, false )
endif

Send newspec
Display newspec, "Selected Graph View"

#
#  Save spectrum to an ASCII text file
#
SaveASCII(newspec, false, "%12.3f %12.3f", path & "Bank" & bank & "_spectrum.asc")

#
#
# End of for loop for each of 9 detectors.
	endfor
#
return "Finished"
