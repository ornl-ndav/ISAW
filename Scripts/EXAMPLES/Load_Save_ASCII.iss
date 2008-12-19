#  Load_Save_ASCII.iss
#
#  @overview  This script loads one DataSet corresponding to one
#             detector, then selects, sums and saves a subset of
#             the spectra from that detector.
#
#  $Date: 2008-12-19$

$Category = Macros, Examples, Scripts ( ISAW )

$ input_file      LoadFile("/SNS/SNAP/2008_3_3_SCI/3/245/NeXus/SNAP_245.nxs")  SNAP run file
$ output_file     SaveFile("ASCII_sum.dat")                                    File to write
$ width           Integer(20)    Width of Region to Sum
$ height          Integer(20)    Height of Region to Sum
$ pixels_per_col  Integer(256)   Number of pixels in each column
$ first_id        Integer(45445) ID of pixel at lower left corner of Region

#
# Load one detector. NOTE: the ds_index and id_range need to be coordinated
# The Display command to pop up the 3D View is present as a check on
# the progress of the script and can be removed.
#
ds_index = 1
id_range = "2:65537"
ds = OneDS( input_file, ds_index, id_range )
Display( ds, "3D View" )

#
# The DataSet can be placed in the "tree" to work with it interactively in ISAW
# by un-commenting the send command below.
#
# send ds

#
# We need to select a rectangular region on the detector.  Since the pixels are
# stored sequentially by column, starting in the lower left hand corner, we
# need to select one portion of each column, for each of the columns in the 
# region.  The bottom pixels in successive colunms are separated by the number
# of pixels in a column.
# The ranges being selected can be displayed for debugging purposes  
#
for i in [0:width-1]
 start = first_id + i * pixels_per_col
 end = start + height
 range_string = ""&start&":"&end
 Display range_string
 SelectByID( ds, range_string, "Set Selected" )
endfor

#
# We can now extract the selected Data and display it for testing purposes
#
use_selected=true
create_new_ds=true
small_ds = ExtSel( ds, use_selected, create_new_ds )
Display( small_ds, "3D View" )

#
# We can also sum the selected Data and display the sum.
#
summed_ds = SumSel( ds, use_selected, create_new_ds )
Display( summed_ds, "Image View" )

#
# Finally, select the first DataSet entry (index = 0) and save it
# as a two column ASCII file.
#
SelectByIndex( summed_ds, "0", "Set Selected" )
include_errors = false
format_string = "%8.1f  %8.0f"
result = SaveASCII( summed_ds, include_errors, format_string, output_file )

return result
