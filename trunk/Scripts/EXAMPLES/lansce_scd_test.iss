#
# @overview This operator loads an SCD data file from LANSCE and produces three 
#           different views of the data. 
#
# @param file  The name of the file containing the data
#
$file  LoadFile("/usr2/LANSCE_DATA/SCD/SCD_E000005_R000053.nx.hdf")  SCD File
$t_min        Float(1500)  Enter min time-of-flight(microseconds)
$t_max        Float(8000)  Enter max time-of-flight(microseconds)
$det_size     Float(0.2)   Enter detector size(meters)
$det_dist     Float(0.45)  Enter sample to detect distance(meters)
$initial_path Float(9)     Enter the initial flight path(meters)
#
$Category=Macros,Utils,Examples
#

load file,"ds"

vec[0] = Fix_Lansce_SCD_Data( ds[3], t_min, t_max, det_size, det_size, det_dist, initial_path )

Display_SCD_Reciprocal_Space( vec, 50 )

Display_As_Image( vec[0] )

Display vec[0], "3D View"

return "Success"
