# $Date$
$ path DataDirectoryString Path

atoms = 1.0
sample_crossection = 1.0
mask = "294"
##
## Set up "solid" parameters or by following way, just remove the #
##
instrument = "HRCS"
sample_runs = "2834:2837"
xmass=1.0
alpha=0.000001

##
## Do multiple file load of sample runs
##
Load SumFiles( path, instrument,sample_runs,mask, true),"sample"

##
## Display the sample run
##
Display sample[1]

##
## Calibrate the energy for the sample runs, using the summed sample run monitor
##
sample_mon_1_area = PeakA( sample[0], 1, 8.5 )
energy = Emon( sample[0] )
SetAttrs( sample[1], "Energy In", energy )
Display "Sample Energy In = "&energy
Display "Sample monitor 1 Area = "&sample_mon_1_area

##
## Calculate and display the double differential crossection
##
double_diff_cross_ds = DSDODE( sample[1], sample[1], false, sample_mon_1_area, atoms, true )
#Display double_diff_cross_ds

##
## Calculate and display scattering function
##
scat_fn_ds = ScatFun( double_diff_cross_ds, sample_crossection, true )
#Display scat_fn_ds

##
## Display QE
##

QE_ds = ToQE( scat_fn_ds, 2.5, 11.0, 75, 20.0, 150.0, 75 )
send QE_ds
#Display QE_ds











