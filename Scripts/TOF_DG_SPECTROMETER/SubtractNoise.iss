#
# @overview  This script will integrate each group over the specified 
#            time range to estimate the electronic noise in the group.
#            That integrated noise value is then scaled by the histogram
#            bin size divided by the time bin size, to estimate the
#            noise for each bin.  The estimate noise per bin is then
#            subtracted from the group.  The result is placed in a new
#            DataSet without altering the original DataSet
#
# @return    A new DataSet with new Data blocks obtained by subtracting
#            an estimate of the electronic noise level from the Data
#            blocks of the original DataSet.
#
# @assumptions  The time bins are assumed to be of uniform width
#
# @param  ds    The DataSet to process 
#
# @param  t0    The start of the time-of-flight interval to integrate.
#
# @param  t1    The end of the time-of-flight interval to integrate.
#
# @param  bin_width  The width of each time bin
#
# @author Dennis Mikkelson
# $Date$
#
$Category = Macros, DataSet, Analyze 
$Command = SubtractNoise 

$ ds         DataSet        DataSet to process
$ t0         Float(2500.0)  Integration interval start
$ t1         Float(2900.0)  Integration interval end 
$ bin_width  Float(2.0)     Width of one histogram bin

new_ds = ds
num_groups = GetField( new_ds, "Num Groups" )
ID_string  = GetField( new_ds, "Group IDs" )
IDs        = IntListToVector( ID_string );

scale_factor = bin_width/(t1 - t0)
#
# For each group, integrate over the requested range, scale by the bin
# width / time interval and subtract from the group
#

for i in [0:num_groups-1]
  noise = scale_factor * IntegGrp( new_ds, IDs[i], t0, t1 )
  Sub( new_ds, noise, i, false )
endfor

return new_ds
