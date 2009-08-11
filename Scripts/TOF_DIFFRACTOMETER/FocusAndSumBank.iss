#  This script will time-focus and sum the spectra from a particular bank of
# detectros and save the sum to a file in a specified directory.  This can
# be easily run in parallel using SLUM to form a new DataSet that contains the
# focused and summed banks from the full instrument.  See the script
# ParallelFocusAndSumBanks.iss
#
#  NOTE: When run using SLURM, none of the "Display" or "send" comands 
#        will have any effect, since there is not an X-Window connection 
#        back to the console.  This is actually somewhat convenient, 
#        since this script can be separately tested with "Display" and 
#        "send" commands to provide chacks on intermediate steps, and 
#        the script does not have to be changed when run using SLURM.
#        Commenting out Display commands that generate viewers could 
#        save some time however.
#
#  $Date: 2009/05/21 18:41:12 $
#
$Category=Macros, Instrument Type, TOF_NPD

$ file_name  LoadFile("/usr2/SNAP_2/QUARTZ/SNAP_240.nxs")  NeXus Run File
$ out_dir    String("/home/dennis/ISAW/tmp")     Location for Temporary Files.
$ out_prefix String("Bank")                      Prefix for output file
$ ds_index   Integer(1)                          DataSet Index to Load ( >= 1 )

#
# Load the specified detector data.  Detector Banks are determined by their
# position (1,2,3...) in the NeXus file. ( The monitor DataSet has index 0.)
# The group IDs parameter is left blank to load all of the pixels in the 
# detector bank.  The LoadNeXusDataSetsFast() operator returns a java
# array of DataSets that must be converted to a java "Vector" which acts 
# like an array in the ISAW scripting language.  Attempting to use the 
# default cache file in ~/ISAW will fail gracefully if there is no cache file.
#
pixel_ids = ""
use_default = true
cache_file = ""

sample_ds_array = LoadNeXusDataSetsFast( file_name, ds_index, pixel_ids, use_default, cache_file )
sample_ds_vec = ToVec( sample_ds_array )
ds = sample_ds_vec[0]

#
# Since we don't get the instrument type information from the NeXus file it 
# must be specified to add the correct operators. 
#
# NOTE: We get the group IDs assuming that the IDs are in increasing order.  
# There is also an operator, TimeFocusIndex() that uses indices instead 
# of pixel IDs.  That operator does not require IDs, so it would be
# somewhat easier to use in this context.
#
setInstrumentType( ds, "TOF_NPD" )

#Display ds

first_group_id = GetAttr( ds, 0, "Group ID" )
num_groups     = GetField( ds, "Num Groups" )
last_group_id  = first_group_id + num_groups - 1

#Display "first group id = " & first_group_id
#Display "num groups     = " & num_groups
#Display "last group id  = " & last_group_id

middle_index = num_groups/2
middle_detector_position = GetAttr( ds, middle_index, "Effective Position" )

#Display middle_detector_position

#
# The DetectorPosition object contains 10 values in IPNS coordinate system.
# First  three are spherical coordinates, r, theta, phi
# Second three are cartesian coordinates, x, y, z
# Third  three are cylindrical coordinates, r, theta, z
# Last one is the POSITIVE scattering angle, two_theta
# ALL ANGLES ARE IN RADIANS
#
position_components = DetectorPositionToVector( middle_detector_position )
middle_L2      = position_components[0]
middle_2_theta = position_components[9] * 180 / 3.14159265

#Display "L2        = " & middle_L2
#Display "2_theta   = " & middle_2_theta

#
# Focus to the scattering angle of the middle pixel using IDs, 
#

id_range = "" & first_group_id & ":" & last_group_id
focused_ds = TimeFocusGID( ds, id_range, middle_2_theta, middle_L2, true )

#Display focused_ds

grouped_ds = GroupDiffract( focused_ds, id_range, ds_index, true )
SetField( grouped_ds, "Title", "Focused and Summed Bank #" & ds_index )
SetDataLabel(grouped_ds,"Bank "& ds_index & " Sum","")
SetAttr( grouped_ds, 0, "Group ID", ds_index )
send grouped_ds
#Display grouped_ds

#
# Now save the focussed and grouped spectrum to a file so it can be combined 
# with results from other subsets of detectors.  Unless there is a 
# significant reason not to, temporary files should be saved to ISAW/tmp.
# This directory can be cleared from ISAW scripts, but arbitrary directories
# can't be cleared, as a safety policy.
#
out_file_name = out_dir & "/" & out_prefix & ds_index & ".isd"
#Display out_file_name

Save grouped_ds, out_file_name

return "Saved sum to " & out_file_name

