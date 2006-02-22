#
#  Load_SMARTS_convert_to_d.iss
#
#  @overview  This operator will load the single-ended-detector
#             time-of-flight data from a SMARTS HDF file, assuming
#             that this data is the third entry in the HDF file.
#             
#  @assumptions  This script assumes that time-of-flight data is in
#                the third data entry of the HDF file.  It also
#                assumes that the detector position information
#                for SMARTS is in the ISAW/InstrumentInfo/LANSCE/
#                directory in the file smarts_detectors.dat.
#                Finally, it assumes that the GSAS calibration
#                information is in the file smarts_050207.prm
#                in the same directory.
#
#  @algorithm  If the GSAS calibration information is not loaded,
#              then the conversion to "d" will be done based on
#              the detector position information.  If the GSAS
#              calibration information is loaded, it will be 
#              used to convert to "d".  
#              The conversion to "d" will create a new DataSet
#              with the data re-binned into the specified number of
#              of bins, uniformly spaced between the min and max
#              "d" values.  If the number of bins is set to zero,
#              the original time-of-flight bin boundaries will 
#              all be mapped to "d".
#
#  @param   file_name       The name of the SMARTS data file to load
#  @param   use_GSAS_calib  Determines whether or not the GSAS 
#                           calibration is to loaded and used to
#                           convert from time-of-flight to "d".
#  @param   num_bins        The number of bins to use along the
#                           "d" axis, between min_d and max_d,
#                           or zero, if the original tof bins
#                           should just be mapped to d.
#  @param   min_d           The minimum value of "d" to use,
#                           if the data is re-binned in d.
#  @param   max_d           The maximum value of "d" to use,
#                           if the data is re-binned in d.
#
$file_name      LoadFile("/usr2/LANSCE_DATA/HDF/SMARTS-LANSCE-R23300.nx.hdf")  SMARTS File
$use_GSAS_calib Boolean(false)  Use GSAS calibration info 
$num_bins       Integer(0)      Number of bins in "d", or 0 to use all
$min_d          Float(0.1)      Minimum "d" value
$max_d          Float(3)        Maximum "d" value

#
# Load only the time-of-flight data from the HDF file.
# For now, we assume the tof data is the third data entry in the
# HDF file, and that IDs 1:384 are present.  These could be changed,
# or made extra parameters for the script.
#
data_set_index = 2
ids = "1:384"
tof_ds = OneDS( file_name, data_set_index, ids )

#
# Now "fix" the DataSet by loading the load the detector position 
# information and optionally, the GSAS calibration information.
# For now, we assume that the GSAS information is in a file in the
# ISAW distribution, ending with _050207.  This should be changed 
# if a newer file is available.
#
if use_GSAS_calib
 gsas_param_version = "_050207"
 TofDifFix( tof_ds, "smarts", "", "", false, "" )
else
 TofDifFix( tof_ds, "smarts", "", "", false, "" )
endif

#
# Display the time-of-flight DataSet, as an image.  Since the 
# detector position information has been loaded, the readouts of
# "d", "Q", energy, etc. should all work.  Also, you can switch to
# the 3D view to see the detectors in 3D.  Comment out this line
# if you DON'T want to see the time-of-flight data, or add a 
# parameter and "if" statement to enable/disable it when you 
# run the script.
#
Display tof_ds

#
# Now convert the time-of-flight DataSet to "d" and display it
#
ds_in_D = ToD( tof_ds, min_d, max_d, num_bins )
Display ds_in_D 
