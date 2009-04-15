#
#  Script to sum the pixels from a rectangular region of
#  an area detector at the SNS.  This script assumes
#  that the pixels in the area detector are ordered by
#  columns. 
#
#  Modified for SNAP data.
#  A. J. Schultz, December, 2008
#  D. Mikkelson, January, 2009

$  data_set       SampleDataSet   Data Set
$  column_length  INTEGER(256)    Number of Pixels in one column
$  first_col      INTEGER(10)     First Col of Region (min X)
$  last_col       INTEGER(245)    Last  Col of Region (max X)
$  first_row      INTEGER(10)     First Row of Region (min Y)
$  last_row       INTEGER(245)    Last  Row of Region (max Y)
$  to_waveln      BOOLEAN(false)  Convert to Wavelength?

#
#  Load a data file
#

#Load "/usr/local/ARGONNE_DATA/SCD_QUARTZ_2_DET/scd08339.run","ds"
#data_set = ds[2]

#
#  To sum different regions from the same runfile, WITHOUT having to 
#  reload the file every time, you can: 
#    1. comment out the Load statement, and "data_set=..." 
#       lines above this comment
#    2. load the run into ISAW, using the file menu
#    3. uncomment the following statment, replacing the number
#       in variable name isawdsNN with the DataSet number 
#       (before the colon) for the DataSet in the ISAW tree.
#    

#data_set=isawds4
#
#  Convert to wavelength, if requested,
#  NOTE: Currently SNS NeXus files do not identify the type of 
#  instrument, so we need to specify the instrument type to
#  get the correct conversion operators associated with the 
#  DataSet. (Since Sample Orientation NOT present yet, for now
#  use "TOF_NPD"
#
if ( to_waveln )
  setInstrumentType( data_set, "TOF_NPD" )
  data_set = ToWL( data_set, 0.5, 6, 0 )
endif

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
Display sum_ds

return "Finished"