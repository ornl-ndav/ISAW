#  $Date$
#
#
# Data treatment for HRMECS measurements
#
$Category=Operator, Instrument Type, TOF_NDGS

#
# mask defines the detectors' numbers which should be excluded 
# from the data treatment
#
mask = "220:233"
temperature_s = 10.0
atoms = 1.
sample_crossection = 1.

$path DataDirectoryString Path
instrument = "HRCS"

#
# Input the runs' numbers for the spectra for vanadium, 
# empty-can for vanadium, sample and empty can for sample
#
vanadium_run = "3083,3090"
backgr_van_run = "3073"
sample_runs = "3085"
background_runs = "3084"

#
# alpha is the value of atomic <u^2> (in A^2) to be used 
# in Debye-Waller factor
# step is energy step in final data for S(Q,E) and G(E)
# Emin_0 and Emax_0 are min and max energy values for S(Q,E)
#
xmass = 1.0
step = 1.0
alpha = 0.000001

Emin_0 = -110.
Emax_0 = 190.

#
# Number = Integer((Emax_0 - Emin_0)/step) + 2
#
Number = 302
Emin   = Emin_0 - step/2.
Emax   = Emax_0 + step/2.
EmaxG  = Emax_0

#
# NumberG = Interge(EmaxG/step) + 1
#
NumberG = 191

#
# Angle is angle value between the neutron beam diraction and 
# normal to the sample plane
#
Angle = 45.0

#
# Do multiple file load of sample runs
#
Load SumFiles( path, instrument,background_runs,mask, true),"background_ds"
Load SumFiles( path, instrument,sample_runs,mask, true),"sample_ds"
Load SumFiles( path, instrument,vanadium_run,mask, true),"van_ds"
Load SumFiles( path, instrument,backgr_van_run,mask, true),"back_van"

#
# Calibrate the energy for the sample runs and vanadium runs, 
# using the summed sample (vanadium) run monitor
#
energy = Emon( sample_ds[0] )
Display "Sample Energy In = "&energy
sample = SetEin(sample_ds[1], energy, true)
background = SetEin(background_ds[1], energy, true)
#send sample
#Display sample

energy_van = Emon( van_ds[0])
Display "Vanadium Energy In = "&energy_van
van = SetEin(van_ds[1], energy_van, true)
back_v = SetEin(back_van[1], energy_van, true)
#send back_v
#Display back_v

#
#  Calculate the ratio of sample to background monitor peak areas
#
sample_mon_1_area = PeakA( sample_ds[0], 1, 8.5 )
background_mon_1_area = PeakA( background_ds[0], 1, 8.5 )
sample_ds=null
background_ds=null

scale = sample_mon_1_area / background_mon_1_area

van_mon_1_area = PeakA( van_ds[0], 1, 8.5)
back_v_mon_1_area = PeakA( back_van[0], 1, 8.5)
scale_van = van_mon_1_area / back_v_mon_1_area

van_ds=null
back_van=null

Display "Background monitor 1 peak area = " & background_mon_1_area
Display "Sample monitor 1 peak area = " & sample_mon_1_area
Display "Scale factor = " & scale
Display "scale_van = " & scale_van

back_ds = background * scale
background=null
samp_ds_1 = sample - back_ds
sample=null

#
# The operator "Crunch" removes the detectors, with total counting 
# less than 10.0 and ...
#
samp_ds = Crunch(samp_ds_1, 10.0, 20.0, true)
samp_ds_1 = null

back_v_sc = scale_van * back_v
van_sub_1 = van - back_v_sc
back_v = null
van    = null

#
# The operator "Crunch2" removes the detectors from it's first argument
# that have been removed from it's second argument by the "Crunch" operator
#
van_sub   = Crunch2(van_sub_1, samp_ds, true)
van_sub_1 = null
#send van_sub
#Display van_sub

#
# Transformation of time-of-flight vanadium spectrum to energy loss spectrum
#
van_en = ToEL(van_sub, -100., 400.0, 501)
van_sub=null
#send van_en
#Display van_en

#
# Calculation of Detector Normalization Factor
#
DNF = DetNormFac_test(van_en, Angle)
van_en=null
send DNF
#Display DNF 

#
# Calculate and display the double differential crossection
#
double_diff_cross_ds_1 = DSDODE(samp_ds, DNF, true, sample_mon_1_area, atoms, true )
double_diff_cross_ds_2 = ConvFunc(double_diff_cross_ds_1, 0.0, true, true)
double_diff_cross_ds_3 = Resample(double_diff_cross_ds_2, Emin, Emax, Number, true)
double_diff_cross_ds   = ConvHist(double_diff_cross_ds_3, true, true)
samp_ds=null
double_diff_cross_ds_1=null
double_diff_cross_ds_2=null
double_diff_cross_ds_3=null
#send double_diff_cross_ds
#Display double_diff_cross_ds

#
# Calculate and display scattering function
#
scat_fn_ds = ScatFun( double_diff_cross_ds, sample_crossection, true )
double_diff_cross_ds=null
send scat_fn_ds
#Display scat_fn_ds

#
# sum mid-angle and high angle detectors separately
#
Res_F_sub_1 = SumAtt(scat_fn_ds,  "Group ID", true,  3.0, 145.0)
Res_F_sub_2 = SumAtt(scat_fn_ds, "Group ID", true, 146.0, 305.0)
send Res_F_sub_1
send Res_F_sub_2
#Display Res_F_sub_1
#Display Res_F_sub_2

#
# Calculate and display the GFun function 
# (Generalized Vibrational Density of States)
#
GFun_ds = GFun(scat_fn_ds, temperature_s, xmass, alpha, true)
scat_fn_ds = null
#send GFun_ds
#Display GFun_ds

#
#  Resample and sum portions of the GFun DataSet
#
Res_GFun_ds = Resample(GFun_ds, 0., EmaxG, NumberG, true)
Gsum_1_ds = SumAtt(Res_GFun_ds, "Group ID", true,   3.0, 145.0)
Gsum_2_ds = SumAtt(Res_GFun_ds, "Group ID", true, 146.0, 305.0)
GFun_ds = null
send Gsum_1_ds
send Gsum_2_ds
#Display Gsum_1_ds
#Display Gsum_2_ds
