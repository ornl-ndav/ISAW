#
#  This script will calculate the G_Function, using a subset of the detectors
#  from SEQUOIA. It will load and display data from the monitors and some selected banks of
#  detectors on the SEQUOIA spectrometer at the SNS.  The incident energy is calculated
#  from the monitor data, based on an estimated incident energy.  The time-of-flight
#  for the sample histograms is adjusted for the delay of the neutrons leaving the moderator
#  and to represent the sample to detector time-of-flight, as required by the ISAW operators.
#  The instrument type is set to TOF_NDGS and the proper operators are added to the DataSet.
#  Data from individual detector banks are read into separate DataSets and those DataSets
#  are also merged efficiently,
#
#  NOTE: When run using SLURM, none of the "Display" or "send" comands will have any
#        effect, since there is not an X-Window connection back to the console.
#        This is actually somewhat convenient, since this script can be separately
#        tested with "Display" and "send" commands to provide chacks on intermediate
#        steps, and the script does not have to be changed when run using SLURM.
#
#  $Date: 2009/05/21 18:41:12 $
#
$Category=Macros, Instrument Type, TOF_NDGS

$ file_name       LoadFile("/usr2/SEQUOIA/SEQ_223.nxs")  NeXus Run File
$ Ein_estimate    Float(92.0)                            Estimated Incident Energy
$ t0_correction   Float(0.0)                             Neutron Delay Correction to t0

$ Emin_0          Float(-50.0)                           DSDODE Energy Range MIN
$ Emax_0          Float( 86.0)                           DSDODE Energy Range MAX
$ E_step          Float(  0.5)                           Energy Step Size

$ out_dir         String("/home/dennis/ISAW/tmp")        Location for Temporary Files.

$ first_ds_index  Integer(3)                             First DataSet Index to Load ( >= 1 )
$ last_ds_index   Integer(10)                            Last DataSet Index to Load ( <= num det banks )

#
# First load the monitor data and calculate the incident energy.  NOTE: The operator
# LoadNeXusDataSetsFast() requires a cache file holding the structure of the NeXus file.
# The cache file can by made using the menu option Macros->File->Save->Create_NeXus_Cache
# The default cache file is the user's home directory/ISAW/SEQ.startup
#
use_default = true
cache_file = ""
mon_ds_index = 0
mon_ids = "1:2"
mon_delta_t = 700

mon_ds_array = LoadNeXusDataSetsFast(file_name,mon_ds_index,mon_ids,use_default,cache_file)
mon_ds_vec = ToVec( mon_ds_array )
Ein = EnergyFromMonitors(mon_ds_vec[0], Ein_estimate, mon_delta_t)
Display "Using incident energy = " & Ein
send mon_ds_vec[0]

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
sample_ds_array = LoadNeXusDataSetsFast(file_name,sample_ds_indices,sample_ids,use_default,cache_file)
sample_ds_vec = ToVec( sample_ds_array )

#
# Now, the DataSets for each bank that was loaded must be adjusted.  The instrument type must
# be specified to add the correct operators.  The time-of-flight scale is adjusted for the
# delay leaving the moderator and switched to measure the sample to detector time-of-flight
# using the SetFinalTOF() operator.
#
n_channels_delay = 200
for i in [0:num_ds-1] 
  setInstrumentType( sample_ds_vec[i], "TOF_NDGS" )
  SetFinalTOF_2( sample_ds_vec[i], Ein, t0_correction, n_channels_delay )
endfor

#
# Next, the DataSets that were loaded can be merged together very efficiently using the
# DSArrayMerge() operator, so merge them, set the title on the merged DataSet and sent it
# to the tree as well.
#

merged_ds = DSArrayMerge( sample_ds_vec )
SetField( merged_ds, "Title", "Merged Raw Data" )
send merged_ds
#Display merged_ds, "Image View"
#Display merged_ds, "3D View"

#
# Use the Crunch operator to remove any spectra that do not appear to
# have valid data, since they have very low counts
#
Crunch(merged_ds, 10.0, 5.0, false)

#
# Calculate and display the double differential crossection
# (currently not using a Vanadium normalization.
#
# NOTE: Monitor IDs are currently reversed, with monitor 2 being
#       the upstream monitor.
#
mon_id = 2
sample_mon_1_area = MonitorPeakArea( mon_ds_vec[0], mon_id, Ein, 500, 8.5 )
Display "Monitor Peak Area = " & sample_mon_1_area

# atoms == sample weight
atoms = 25.0
#
Emin   = Emin_0 - E_step/2.
Emax   = Emax_0 + E_step/2.
EmaxG  = Emax_0
#
Number = 274
NumberG = 173

double_diff_cross_ds_1 = DSDODE(merged_ds,merged_ds,false,sample_mon_1_area,atoms,true)
SetField( double_diff_cross_ds_1, "Title", "First Double Differential Cross-section" )
send double_diff_cross_ds_1

double_diff_cross_ds_2 = ConvFunc(double_diff_cross_ds_1, 0.0, true, true)
double_diff_cross_ds_1 = null

double_diff_cross_ds_3 = Resample(double_diff_cross_ds_2, Emin, Emax, Number, true)
double_diff_cross_ds_2 = null

double_diff_cross_ds   = ConvHist(double_diff_cross_ds_3, true, true)
double_diff_cross_ds_3 = null

SetField( double_diff_cross_ds, "Title", "Resampled Double Differential Cross-section" )
send double_diff_cross_ds

#
# Calculate and display the scattering function
#
sample_crossection = 1.
scat_fn_ds = ScatFun(double_diff_cross_ds, sample_crossection, true )
double_diff_cross_ds = null

SetField( scat_fn_ds, "Title", "Scattering Function" )
send scat_fn_ds

scat_fn_ds_ext_1 = ExtAtt(scat_fn_ds, "Group ID", true, 0.0, 5000000.0)
ScaleBySolidAngle (scat_fn_ds_ext_1, false)

Res_F_sub_1 = SumAtt(scat_fn_ds_ext_1,  "Group ID", true, 0.0, 5000000.0)
SetField( Res_F_sub_1, "Title", "Scaled Scattering Function" )
send Res_F_sub_1

#
# Calculate and write the GFun function DataSet to a temporary
# "Isaw DataSet" (.isd) file.  The name of the output file MUST be
# coordinated with the script that will read it in and combine
# it with the results from other sets of detectors. 
#
temperature_s = 8.0
alpha = 0.00000
xmass = 1.0
GFun_ds = GFun(scat_fn_ds, temperature_s, xmass, alpha, true)
scat_fn_ds = null

Res_GFun_ds = Resample(GFun_ds, 0., EmaxG, NumberG, true)
SetField( Res_GFun_ds, "Title", "G Function" )
send Res_GFun_ds

Res_GFun_ds_ext_1 = ExtAtt(Res_GFun_ds, "Group ID", true, 0.0, 5000000.0)
ScaleBySolidAngle (Res_GFun_ds_ext_1, false)

Gsum_1_ds = SumAtt(Res_GFun_ds_ext_1,  "Group ID", true, 0.0, 5000000.0)
SetField( Gsum_1_ds, "Title", "Scaled G Function" )
send Gsum_1_ds

#
# Now save the G Function to a file so it can be combined with the
# results from other subsets of detectors.
#
outfile_name = out_dir & "/GFun_"&first_ds_index&"-"&last_ds_index&".isd"
Save Gsum_1_ds, outfile_name

#
# When testing this separately, we can load the G-Function we wrote to 
# verify that it was written correctly.
#
# Display "Loading " & outfile_name
# Load outfile_name, "loaded_ds"
# setInstrumentType( loaded_ds[0], "UNKNOWN" )
# send loaded_ds[0]

return "Wrote "&outfile_name
