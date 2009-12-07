#
# This script will time focus and sum the specified banks in an instrument
# using SLURM to launch parallel processes to reduce individual 
# detector banks in parallel.  It uses the FocusAndSumBank.iss
# script to do the calculation for each detector bank.
# If specified, this script will also write the data to a three column
# GSAS file.
#
#  $Date: 2009/05/21 18:41:12 $
#
$Category=Macros, Instrument Type, TOF_NPD

$ file_name         LoadFile("/usr2/POWGEN/PG3_509.nxs")   NeXus run file    
$ first_ds_index    Integer(1)                      Smallest Sample DataSet Index
$ last_ds_index     Integer(20)                     Largest DataSet Index ( num det banks )
$ mem_per_process   Integer(5000)                   Megabytes per process
$ queue             String("mikkcomp")              SLURM queue name
$ max_simultaneous_processes  Integer(4)            Max number of cores to use
$ max_time                    Integer(600)          Max run time in seconds 
$ out_prefix        String("Bank")                  Prefix for output file
$ out_dir           String("/home/dennis/ISAW/tmp") Location for temporary files
$ send_to_isaw      Boolean(true)                   Send result to ISAW "tree"
$ write_gsas_file   BooleanEnable(false,1,0)        Write 3 column GSAS file
$ gsas_file_name    SaveFile("/home/dennis/test.gsa") GSAS file name  

#
# Build the script command names along with the necessary parameters.
# These scripts will be run in parallel on the specified SLURM queue.
# The FocusAndSumBank script takes four parameters: the file,
# the directory to write to, the output file name prefix and 
# the detector bank index.  
#
script_name = "FocusAndSumBank"
base_command = script_name&" "&file_name&" "&out_dir&" "&out_prefix

#
# Construct the list of complete commands and a list of the 
# file names that will be written for each bank.
# NOTE: These output file names MUST be coordinated with the file names
#       constructed in the individual scripts FocusAndSumBank.iss.
#
ds_index = first_ds_index
num_processes = last_ds_index - first_ds_index + 1 
for i in [0:num_processes-1]
    commands[i]      = base_command & " " & ds_index 
    sum_file_name[i] = out_dir & "/" & out_prefix & ds_index & ".isd"
    Display commands[i]
    Display sum_file_name[i]
    ds_index = ds_index + 1
endfor

#
# Clear out the temporary files from ~/ISAW/tmp, so we only load in new
# ones created during this run.  If you don't put the temporary files here
# you may need to manually clear the files between runs of the script
#
ClearFiles( "", ".isd" )
ClearFiles( "", "returned.txt" )

#
# Now run the specified operators in parallel using SLURM
#
srunOps(queue, max_simultaneous_processes, max_time, mem_per_process, commands)

#
# Finally, gather the results from the partial files that were written.
#
first_file = true
for i in [0:num_processes-1]

  Display sum_file_name[i]

  On Error
    Load sum_file_name[i], "loaded_ds"
    setInstrumentType( loaded_ds[0], "TOF_NPD" )
    if ( first_file )
      result_ds = loaded_ds[0]
      first_file = false
    else
      result_ds = Merge( result_ds, loaded_ds[0] ) 
    endif

  End Error

endfor

#
# Get the monitor DataSet
#
mon_ds_index = 0
pixel_ids = ""
use_default = true
cache_file = ""

mon_ds_array = LoadNeXusDataSetsFast( file_name, mon_ds_index, pixel_ids, use_default, cache_file )
mon_ds_vec = ToVec( mon_ds_array )
mon_ds = mon_ds_vec[0]

#
# Set title on summed banks
#
SetField( result_ds, "Title", "Sums of Focused Banks" )

#
# Send results to ISAW if so requested
#
if ( send_to_isaw )
  send mon_ds
  send result_ds
endif

#
# Write a GSAS file if so requested 
#
if ( write_gsas_file )
  Save3ColGSAS( mon_ds, result_ds, gsas_file_name, false )
endif

if ( write_gsas_file )
  return "Wrote GSAS file:" & gsas_file_name
endif

return "COMPLETE" 
