#  HRMECS data reduction
#@overview - 

atoms = 1.
sample_crossection = 1.
mask = "220:233"
#306,703,735,783:1482"

temperature_s=10.0

$path DataDirectoryString Path
instrument = "HRCS"

vanadium_run = "3083,3090"
backgr_van_run = "3073"

sample_runs = "3085"
background_runs = "3084"

xmass=1.0
alpha=0.000001

step=1.0
Emin_0 = -110.
Emax_0 = 190.
#Number = Interger((Emax_0 - Emin_0)/step) + 2
Number = 302
Emin   = Emin_0 - step/2.
Emax   = Emax_0 + step/2.
EmaxG  = Emax_0
#NumberG = Interge(EmaxG/step) + 1
NumberG = 191

Angle = 45.0

##
## Do multiple file load of sample runs
##

Load SumFiles( path, instrument,background_runs,mask, true),"background_ds"
Load SumFiles( path, instrument,sample_runs,mask, true),"sample_ds"
Load SumFiles( path, instrument,vanadium_run,mask, true),"van_ds"
Load SumFiles( path, instrument,backgr_van_run,mask, true),"back_van"

#
#back_van[0] =SetDetPos(back_van[0],  1, 5.147, 0.0, 0.0)
#van_ds[0]   =SetDetPos(van_ds[0],    1, 5.147, 0.0, 0.0)
#sample_ds[0]=SetDetPos(sample_ds[0], 1, 5.147, 0.0, 0.0)
#background_ds[0]=SetDetPos(background_ds[0],1,5.147,0.0,0.0)

## Calibrate the energy for the sample runs, using the summed sample run monitor

energy = Emon( sample_ds[0] )
Display "Sample Energy In = "&energy
#energy = 281.99667
sample = SetEin(sample_ds[1], energy, true)
background = SetEin(background_ds[1], energy, true)

energy_van = Emon( van_ds[0])
Display "Vanadium Energy In = "&energy_van
van = SetEin(van_ds[1], energy_van, true)
back_v = SetEin(back_van[1], energy_van, true)

##
##  Calculate the ratio of sample to background monitor peak areas
##

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

samp_ds = Crunch(samp_ds_1, 10.0, 20.0, true)
samp_ds_1
#SetAttrs(samp_ds, "Number of Pulses", 1000000)

back_v_sc = scale_van * back_v
van_sub_1 = van - back_v_sc
back_v=null
van=null

van_sub = Crunch(van_sub_1, samp_ds, true)
van_sub_1=null

van_en = ToEL(van_sub, -100., 400.0, 501)
van_sub=null

DNF = DetNormFac_test(van_en, Angle)
#send DNF
van_en=null

##
## Calculate and display the double differential crossection
##
#double_diff_cross_ds_1 = DSDODE(samp_ds,samp_ds,false,sample_mon_1_area,atoms,true)
double_diff_cross_ds_1 = DSDODE(samp_ds, DNF, true, sample_mon_1_area, atoms, true )
double_diff_cross_ds_2 = ConvFunc(double_diff_cross_ds_1, 0.0, true, true)
double_diff_cross_ds_3 = Resample(double_diff_cross_ds_2, Emin, Emax, Number, true)
double_diff_cross_ds   = ConvHist(double_diff_cross_ds_3, true, true)
#send double_diff_cross_ds

samp_ds=null
double_diff_cross_ds_1=null
double_diff_cross_ds_2=null
double_diff_cross_ds_3=null

##
## Calculate and display scattering function
##

scat_fn_ds = ScatFun( double_diff_cross_ds, sample_crossection, true )
send scat_fn_ds
double_diff_cross_ds=null

Res_F_sub_1 = SumAtt(scat_fn_ds,  "Group ID", true,  3.0, 145.0)
Res_F_sub_2 = SumAtt(scat_fn_ds, "Group ID", true, 146.0, 305.0)
#Res_F_sub_s = SumAtt(scat_fn_ds, "Group ID", true, 306.0, 2000.0)
send Res_F_sub_1
send Res_F_sub_2
#send Res_F_sub_s

#
## Calculate and display  GFun function
#

GFun_ds = GFun(scat_fn_ds, temperature_s, xmass, alpha, true)
#cat_fn_ds=null

Res_GFun_ds = Resample(GFun_ds, 0., EmaxG, NumberG, true)
Gsum_1_ds = SumAtt(Res_GFun_ds, "Group ID", true,   3.0, 145.0)
Gsum_2_ds = SumAtt(Res_GFun_ds, "Group ID", true, 146.0, 305.0)
#Gsum_s_ds = SumAtt(Res_GFun_ds, "Group ID", true, 306.0, 2000.0)
send Gsum_1_ds
send Gsum_2_ds
#send Gsum_s_ds

