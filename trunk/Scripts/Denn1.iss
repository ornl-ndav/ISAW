# Put parameters here: 
# $Date$
#
$  path   DataDirectoryString       Path?
$  instrument  InstrumentNameString(hrcs)     Instrument?
$  background_runs   String(2444)    background runs
$  sample_runs   string(2447,2451)           sample runs
$  mask     String                       mask
$  atoms   float(2.5)                        atoms*10^-24

#
# Do multiple file load of sample and background runs
#
Load SumFiles( path, instrument,background_runs,mask, true ),"background"
Load SumFiles( path, instrument,sample_runs,mask, true),"sample"
Send  background[0]
Send  background[1]
Send sample[0]
Send sample[1]

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
#Display scale

#
#  Scale and subtract the background run
#
background[0] = scale * background[0]
background[1] = scale * background[1]
difference_ds = sample[1]-background[1]
#Display difference_ds

#
#  Calculate and display the double differential crossection
#
double_diff_cross_ds = DSDODE( difference_ds, difference_ds, false, sample_mon_1_area, atoms, true )
send double_diff_cross_ds
