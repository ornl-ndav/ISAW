# Sample HRCS macro
# Load and sum multiple HRCS sample and background run files
# $Date$
$  path               DataDirectoryString     Data File Path:
$  instrument         InstrumentNameString    Instrument Prefix(eg:hrcs)
$  background_runs    Array                   Background Run Numbers(,:)
$  sample_runs        Array                   Sample Run Numbers(,:)
$  mask               Array                   Group IDs to omit(,:)
$  atoms              float                   Number of atoms in Sample *10^24
$  sample_crossection float                   Sample Crossection
#
if atoms = 0.0
  atoms = 1.0
endif

if sample_crossection = 0.0
  sample_crossection = 1.0
endif
#
# Do multiple file load of sample and background runs
#
Load SumFiles( path, instrument,background_runs,mask, true ),"background"
Load SumFiles( path, instrument,sample_runs,mask, true),"sample"
#
# We could display any of the four DataSets, just remove the #  
#
#Display background[0]
#Display background[1]
#Display sample[0]
#Display sample[1]
#
# Calibrate the energy using the summed sample run monitors
#
energy = Emon( sample[0] )
SetAttrs( sample[0], "Energy In", energy )
SetAttrs( sample[1], "Energy In", energy )
#
#  Calculate the ratio of sample to background monitor peak areas
#
background_mon_1_area = PeakA( background[0], 1, 8.5 )
sample_mon_1_area = PeakA( sample[0], 1, 8.5 )
scale = sample_mon_1_area/background_mon_1_area

Display "Background monitor 1 peak area = " & background_mon_1_area 
Display "Sample monitor 1 peak area = " & sample_mon_1_area 
Display "Scale factor = " & scale
#
#  Scale and subtract the background run
#
background[1] = scale * background[1]
difference_ds = sample[1]-background[1]
#Display difference_ds
#
#  Calculate and display the double differential crossection
#
double_diff_cross_ds = DSDODE( difference_ds, sample_mon_1_area, atoms, true )
Display double_diff_cross_ds
#
# Calculate and display scattering function
#
scat_fn_ds = ScatFun( double_diff_cross_ds, sample_crossection, true )
Display scat_fn_ds
#
# Convert to energy loss and smooth the data
#
e_loss = ToEL( scat_fn_ds, -120.0, 120.0, 0 )
ConvHist( e_loss, false  , false )
Resample( e_loss, -120.0, 120.0, 240, false )
Display e_loss
#
# Sum the data from all detector groups
#
summed_ds = SumAtt( e_loss, "Group ID", true, 0.0, 55.0 )
Display summed_ds