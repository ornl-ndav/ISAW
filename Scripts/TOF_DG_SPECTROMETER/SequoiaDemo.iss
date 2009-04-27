#
#  This script demonstrates some operators that help adapt DataSets built from SNS NeXus
#  files, to the conventions required by ISAW.
#  The script will load and display data from the monitors and some selected banks of
#  detectors on the SEQUOIA spectrometer at the SNS.  The incident energy is calculated
#  from the monitor data, based on an estimated incident energy.  The time-of-flight
#  for the sample histograms is adjusted for the delay of the neutrons leaving the moderator
#  and to represent the sample to detector time-of-flight, as required by the ISAW operators.
#  The instrument type is set to TOF_NDGS and the proper operators are added to the DataSet.
#  Data from individual detector banks are read into separate DataSets and those DataSets
#  are also merged efficiently,
#
#  $Date$
#
$Category=Macros, Instrument Type, TOF_NDGS

$ file_name       LoadFile("/usr2/SEQUOIA/SEQ_195.nxs")     NeXus run file
$ use_default     BooleanEnable(true,0,1)                   Use default NeXus cache file (~/ISAW/SEQ.startup)
$ cache_file      LoadFile("")                              NeXus cache file name
$ mon_ds_index    INTEGER(0)                                Index of Monitor Data in file
$ mon_ids         String("1:2")                             Monitor IDs     
$ Ein_estimate    Float(45)                                 Estimated Incident Energy
$ mon_delta_t     Float(700)                                Time(microseconds) around Ein
$ first_ds_index  Integer(3)                                First DataSet index to load ( >= 1 )
$ last_ds_index   Integer(10)                               Last DataSet index to load ( <= num det banks )
$ a               Float(92.0)                               Neutron Delay Coefficient 'a'
$ b               Float(81.3)                               Neutron Delay Coefficient 'b'
$ c               Float(0.00130)                            Neutron Delay Coefficient 'c'
$ d               Float(-751.0)                               Neutron Delay Coefficient 'd'
$ f               Float(1.65)                               Neutron Delay Coefficient 'f'
$ g               Float(-14.5)                              Neutron Delay Coefficient 'g'
$n_channels_delay Integer(200)                              Channels skipped after neutrons hit sample ( >10 )

#
# First load the monitor data and calculate the incident energy.  NOTE: The operator
# LoadNeXusDataSetsFast() requires a cache file holding the structure of the NeXus file.
# The cache file can by made using the menu option Macros->File->Save->Create_NeXus_Cache
# The default cache file is the user's home directory/ISAW/SEQ.startup
#
mon_ds_array = LoadNeXusDataSetsFast(file_name,mon_ds_index,mon_ids,use_default,cache_file)
mon_ds_vec = ToVec( mon_ds_array )
send mon_ds_vec[0]
Ein = EnergyFromMonitors(mon_ds_vec[0], Ein_estimate, mon_delta_t)
Display Ein

#
# Next load the specified detector banks.  Detector Banks are determined by their position (1,2,3...)
# in the NeXus file. ( The monitor DataSet has index 0.)  The group IDs parameter is left blank to
# load all of the pixels in the detector bank.  The LoadNeXusDataSetsFast() operator returns a java
# array of DataSets that must be converted to a java "Vector" which acts like an array in the
# ISAW scripting language.
#
num_ds = last_ds_index - first_ds_index + 1
sample_ds_indices = "" & first_ds_index & ":" &last_ds_index
sample_ids = ""
sample_ds_array=LoadNeXusDataSetsFast(file_name,sample_ds_indices,sample_ids,use_default,cache_file)
sample_ds_vec = ToVec( sample_ds_array )

#
# Now, the DataSets for each bank that was loaded must be adjusted.  The instrument type must
# be specified to add the correct operators.  The time-of-flight scale is adjusted for the
# delay leaving the moderator and switched to measure the sample to detector time-of-flight
# using the SetFinalTOF() operator.
#
for i in [0:num_ds-1] 
  setInstrumentType( sample_ds_vec[i], "TOF_NDGS" )
  SetFinalTOF( sample_ds_vec[i], Ein, a, b, c, d, f, g, n_channels_delay )
  send sample_ds_vec[i]
endfor

#
# Finally, the DataSets that were loaded can be merged together very efficiently using the
# DSArrayMerge() operator, so merge them, set the title on the merged DataSet and sent it
# to the tree as well.
#
merged_ds = DSArrayMerge( sample_ds_vec )
SetField( merged_ds, "Title", "Merged Data" )
send merged_ds
Display merged_ds, "Image View"
Display merged_ds, "3D View"

#
# Return a message to the Dialog box when we're done
#
return "Merged " & num_ds & " banks"
