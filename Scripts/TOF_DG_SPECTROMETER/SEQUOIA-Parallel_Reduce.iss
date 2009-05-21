#
# This script will do a basic reduction of SEQUOIA data to a "G Function",
# using SLURM to launch parallel processes to reduce subsets of the 
# detector banks in parallel.  It relies on the SequoiaPartialReduce
# script to do the calculation on subsets of detector banks.
#
#  $Date: 2009/05/21 18:41:12 $
#
$Category=Macros, Instrument Type, TOF_NDGS

$ file_name         LoadFile("/usr2/SEQUOIA/SEQ_223.nxs")   NeXus run file    
$ Ein_estimate      Float(92.0)                             Estimated Incident Energy
$ specify_t0_shift  BooleanEnable(true,1,6)                 Specify or Calculate t0_shift
$ t0_correction     Float(0.0)                              Specify Neutron Delay t0_shift 
$ a                 Float(92.0)                             Neutron Delay Coefficient 'a'
$ b                 Float(81.3)                             Neutron Delay Coefficient 'b'
$ c                 Float(0.00130)                          Neutron Delay Coefficient 'c'
$ d                 Float(-751.0)                           Neutron Delay Coefficient 'd'
$ f                 Float(1.65)                             Neutron Delay Coefficient 'f'
$ g                 Float(-14.5)                            Neutron Delay Coefficient 'g'

$ Emin_0            Float(-50.0)                            DSDODE Energy Range MIN
$ Emax_0            Float( 86.0)                            DSDODE Energy Range MAX
$ E_step            Float(  0.5)                            Energy Step Size

$ first_ds_index    Integer(3)                              Smallest Sample DataSet Index
$ last_ds_index     Integer(100)                            Largest DataSet Index ( num det banks )
$ banks_per_process Integer(5)                              Number of detector banks for each process
$ mem_per_process   Integer(4000)                           Megabytes per process
$ queue             String("sequoiaq")                      SLURM queue name
$ max_simultaneous_processes  Integer(20)                   Max number of cores to use
$ max_time                    Integer(600)                  Max run time in seconds 
$ out_dir           String("/home/dennis/ISAW/tmp")         Location for temporary files.

#
# First load the monitor data and calculate the incident energy.  NOTE: The operator
# LoadNeXusDataSetsFast() requires a cache file holding the structure of the NeXus file.
# The cache file can by made using the menu option Macros->File->Save->Create_NeXus_Cache
# The default cache file is the user's home directory/ISAW/SEQ.startup
#
use_default = true
cache_file =""
mon_ds_index=0
mon_ids = "1:2"
mon_delta_t = 700

mon_ds_array = LoadNeXusDataSetsFast(file_name,mon_ds_index,mon_ids,use_default,cache_file)
mon_ds_vec = ToVec( mon_ds_array )
send mon_ds_vec[0]
#
# NOTE: EnergyFromMonitors will return Ein_estimate, if the calculation 
#       fails due to missing monitor data
#
Ein = EnergyFromMonitors(mon_ds_vec[0], Ein_estimate, mon_delta_t)
Display "Using incident energy = " & Ein

#
# Now set the value for the t0_shift based on specified value or on
# value calculated from the modeled delay function.
#
if (specify_t0_shift)
  t0_shift = t0_correction
else 
  t0_shift = NDGS_t0_correction( Ein, a, b, c, d, f, g )
endif
Display "Using t0_shift of " & t0_shift

#
# Build the script command names along with the necessary parameters.
# These scripts will be run in parallel on the specified SLURM queue.
# The SequoiaPartial reduce script takes nine parameters: the file
# name, the incident energy estimate, the t0_shift, the Energy min,
# max and number of steps for the DSDODE, the directory 
# to write to and the first and last detector bank index.  
#
script_name = "SequoiaPartialReduce"
command = script_name&" "&file_name&" "&Ein&" "&t0_shift&" "&Emin_0&" "&Emax_0&" "&E_step&" "&out_dir

#
# Calculate the first and last detector bank index for each partial
# reduction.  Also construct a list of file names that will be written
# for each partial GFun.  
# NOTE: These output file names MUST be coordinated with the file names
#       constructed in the individual scripts SequoiaPartialReduce.
#
num_ds = last_ds_index - first_ds_index + 1
num_processes = num_ds/banks_per_process
for i in [0:num_processes]
  index_1 = i*banks_per_process + first_ds_index
  if ( index_1 <= last_ds_index )
    index_2 = (i+1)*banks_per_process + first_ds_index - 1
    if ( index_2 > last_ds_index )
      index_2 = last_ds_index
    endif
    commands[i] = command & " " & index_1 & " " & index_2
    GFunFile_name[i] = out_dir & "/GFun_" & index_1 & "-" & index_2 &".isd"
#   Display commands[i]
#   Display GFunFile_name[i]
  endif
endfor

#
# Now run the specified operators in parallel using SLURM
#
srunOps( queue, max_simultaneous_processes, max_time, mem_per_process, commands )

#
# Finally, gather the results from the partial files that were written.
# NOTE: Since these are not raw time-of-flight DataSets, we don't want 
# all of the DG_Spectrometer operators, so reset the instrument type
# to UNKNOWN.
#
first_time = true
for i in [0:num_processes]
  On Error
    Load GFunFile_name[i], "loaded_ds"
    setInstrumentType( loaded_ds[0], "UNKNOWN" )
    if ( first_time )
      result_ds = loaded_ds[0]
       first_time = false
    else
      result_ds = Merge( result_ds, loaded_ds[0] ) 
    endif
#   Display "Loaded " & GFunFile_name[i]
  End Error
endfor

#
# Save the collection of G Functions calculated from the sets of
# detctors.
#
SetField( result_ds, "Title", "G Functions from Sets of Detectors" )
send result_ds

#
# Sum and save the total of the the individually calculated G Functions
#
Gsum_ds =  SumAtt(result_ds,  "Group ID", true, 0.0, 5000000.0)
SetField( Gsum_ds, "Title", "Combined G Function" )
send Gsum_ds

return "COMPLETE"
