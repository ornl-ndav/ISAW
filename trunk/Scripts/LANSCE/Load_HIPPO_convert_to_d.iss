#
# Load_HIPPO_convert_to_d.iss
#
# @overview     This operator will load, convert to d, and then save
#               an image of the spectra from a specified run and bank 
#               for the HIPPO instrument at LANSCE.  This script is
#               slightly more complicated than will be needed in the
#               future, to work around a couple of missing "features".
#
# @assumptions  Only banks at 40, 90 and 150 degrees are currently supported.
#               If particular spectra have not been calibrated, but do
#               have non-zero counts, they will need to be listed
#               in the list of bad_ids for each bank.
#             
# @algorithm    After loading the specified bank, the TofDifFix() operator
#               is used to load nominal detector positions and calibration
#               values for DiffC, DiffA and TZero.  Then each spectrum is
#               integrated and if it's total counts is too small, it is
#               discarded.  Finally, any spectra listed in the bad_ids[]
#               list are discarded.  The DataSet is then converted to "d"
#               and written to a file with a specified frame number.
#
# @param        data_directory   The directory containing the input file.
#
# @param        run_num          The run number, used to form the file name.
#
# @param        bank_num         The bank, 40, 90 or 150
#
# @parama       frame_num        A numeric suffix applied to the output
#                                file name, to allow writing a sequence
#                                of output image files for a "movie" 
#
# @author       Dennis Mikkelson
#

$category=Macros,File,Load,LANSCE

$data_directory  String            Data Directory 
$run_num         Integer(12066)    Run Number (e.g. 12066)
$bank_num        Integer(40)       Bank Name  (40, 90 or 150)
$frame_num       Integer(0)        Image Frame number

#
# If you have problems getting the parameters into IsawLite from the 
# shell script, it may be helpful to temporarily just trigger the script
# with hard coded values to see that it works.  To do this, comment out
# the input parameter declarations above and un-comment and modify the
# hardcoded values below.
#
# data_directory = "/usr3/home/dennis/LANSCE_DATA/HIPPO_GLAD/"
# run_num = 12066
# bank_num = 40
# frame_num = 0

#
# You will need to change this directory where the images are written
# to a convenient directory on your system.  You may also want to make
# it an additional parameter to the script. 
#
out_directory = "/usr3/home/dennis/LANSCE_DATA/HIPPO_GLAD/"

#
# Form the input file name and the save file name from the parameters.
#
file = "HIPPO_E000002_R00" & run_num & ".nx.hdf"
save_file = "HippoImage_" & frame_num & ".jpg"

#
# Set up some bank specific names and values.  In particular, the list of
# bad_ids is currently needed, since when the calibration info is loaded,
# the "bad" spectra are still retained.  Converting to D fails when 
# DifC = DifA = TZero = 0. 
#
if bank_num = 40 
  Display "Processing bank 40"
  bank_ds_number = 9
  bank_max_id = 288
  bank_ids = "1:" & bank_max_id
  bank_name = "40"
  bad_ids = [-1]
endif

if bank_num = 90
  Display "Processing bank 90"
  bank_ds_number = 3
  bank_max_id = 240
  bank_ids = "1:" & bank_max_id
  bank_name = "90"
  bad_ids = [1:7,19:30,43:48,96]
endif

if bank_num = 150 
  Display "Processing bank 150"
  bank_ds_number = 1
  bank_max_id = 192
  bank_ids = "1:" & bank_max_id
  bank_name = "150"
  bad_ids = [-1]
endif

#
# Load the requested bank, from the requested file.  The last parameter,
# specifying particular IDs to load is currently ignored by the NeXus reader.
#
ds = OneDS( data_directory & "/" & file, bank_ds_number, bank_ids )

#
# Now "fix" the DataSet by loading in "approximate" detector posisions
# and the calibration info from files in ISAW/InstrumentInfo/LANSCE/
#
gsas_param_version = "_040819"
TofDifFix( ds, "hippo", bank_name, "", true, gsas_param_version )

#
# We should be able to get rid of the spectra that have no counts using 
# either OmitNullData or Crunch, after the next build.  These operators
# currently depend on the TOTAL_COUNTS attribute being set, and as of 9/29/05
# these attributes are NOT being set when the LANSCE NeXus files are 
# loaded.  This will be fixed in the next release.
#
#OmitNullData( ds, false )
#Crunch( ds, 0.5, 2.0, false)
#
# So now, we will integrate each spectrum and discard those with low
# counts, here in the script.
#
Display "Step 2"
min_count = 1
for id in [1:bank_max_id]
  total_count = IntegGrp( ds, id, 0.0, 33333.0 )
  if total_count < min_count then
    DelAtt( ds, "Group ID", id, id, true, false )
    Display "Low Counts, so Deleted id " & id
  endif
endfor

#
# Some groups with non-zero counts also don't have diffC, etc.
# We remove these explicitly
#
for  id in bad_ids
  DelAtt( ds, "Group ID", id, id, true, false )
  Display "Deleting id in bad_id list " & id
endfor

#
# Now that the DataSet is "cleaned up" and only has properly calibrated
# Data blocks, we convert to d.  Since the number of bins is set to zero,
# the conversion will just map the original bin boundaries to d.  IF the
# last parameter was positive, the specified range would be uniformly divided
# into the specified number of bins.
#
min_d = 0.1
max_d = 8.0
num_bins = 0
ds_in_D = ToD( ds, min_d, max_d, num_bins )

#
# Now save the image, as an "ImageView".  Other views of the Data can also
# be saved.  Some "advanced" options can be specified for the saved view,
# such as color scale, etc.  The defaults are used here.
#
width = 800
height = 600
SaveImage(ds_in_D, "Image View", out_directory & "/" & save_file, " ", width, height) 

#
# Unfortunately, the thread that IsawLite is running in exits before the 
# saved image file is closed properly, if we don't wait.  The "quick and dirty"
# solution is to just introduce a pause to let the file finish writing.  
# However, it is not clear how long the pause should be.  A pause of 1000 ms
# works on my system, but you may need to increase the pause, if your system
# is heavily loaded.  As an additional "delay" you can un-comment the 
# Display command, which will temporarily put the image on the screen.  
# Presumably, if the system had time to get the display up on the screen,
# it should also have had time to finish writing the file.  We are working
# on a better fix for this.
#
#Display ds_in_D
Pause(1000)
