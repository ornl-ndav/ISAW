#
# @overview This script will load an SCD run, calibration info and a 
#           corresponding orientation matrix.  It will the calculate
#           the un-normalized integral of the data in a rectangular
#           region specified by a range of h, k and l values.
#           In addition, the h, k and l "profiles" of the region are
#           calculated.  See the documentation for the IntegrateHKL operator
#           for details.
#
# @algorithm See the documentation for the IntegrateHKL operator. 
#
# @author Dennis Mikkelson
# $Date$
#
$Category = Macros, Instrument Type, TOF_NSCD

$run_file    LoadFile(/usr2/SCD_TEST/scd08336.run)     Runfile Name
$calib_file  LoadFile(/usr2/SCD_TEST/instprm.dat)      Calibration file name
$orient_file LoadFile(/usr2/SCD_TEST/lsquartz8336.mat) Orientation file name
$det_id      Integer(17)     DetectorID
$min_h       Float( -3.1 )   Region min h
$max_h       Float( -2.9 )   Region min h
$num_h_steps Integer( 10 )   Number of steps in h
$min_k       Float(  1.9 )   Region min k
$max_k       Float(  2.1 )   Region min k
$num_k_steps Integer( 10 )   Number of steps in k
$min_l       Float(  0.5 )   Region min l
$max_l       Float(  2.5 )   Region min l
$num_l_steps Integer( 100 )  Number of steps in l
$out_file    SaveFile(/home/dennis/integrated.data) File for intensities

#
# Load the data, calibrations and orientation matrix
#
Load run_file,"ds"
histogram = ds[2]
LoadSCDCalib( histogram, calib_file, -1, "" )
LoadOrientation( histogram, orient_file )

#
# Do the integration
#
results = IntegrateHKL( histogram, det_id, min_h, max_h, num_h_steps, min_k, max_k, num_k_steps, min_l, max_l, num_l_steps, out_file )

#
# Show the first 6 results that are simple pairs of labels and numbers
#
for i in [0:5]
  Display results[i]
endfor

#
# Show the labels and profile DataSets that are returned as pairs in
# the last 6 positions
#
Display "Pop up display of " & results[6]
Display results[7], "Selected Graph View"

Display "Pop up display of " & results[8]
Display results[9], "Selected Graph View"

Display "Pop up display of " & results[10]
Display results[11], "Selected Graph View"

#
# Send the DataSets up to the ISAW tree
#
Send results[7]
Send results[9]
Send results[11]

