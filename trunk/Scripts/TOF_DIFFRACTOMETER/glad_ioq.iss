#@overview - One button for all: calculate the interference part of differential cross section (IOQ) 

#configuration file:
# @param	Configfile -path of the GLAD configration file;

# run files:
# @param	VanFile -path of the vanadium run file;
# @param	SmpFile -path of the sample run file;
# @param	CanFile -path of the can run file;
# @param   	BkgFile -path of the background run file;

# dead detector list:
# @param	REDPAR -path of the dead detector list file;

#CRUNCH
# @param	ds0	-dummy dataset holding configuration info, output by GLAD_Config();
# @param	runfile -runfile absolute name;
# @param	noDeadDetList -true: new dead detector list, default is false;
# @param	redpar	-bad detector list file;
# @param	lcutoff -data groups with total counts lower than this value will be removed, default 200;
# @param	MonSmoothing -true for monitor spectrum smoothing, default false;
# @param	DetSmoothing -true for detector data smoothing, default false;

# VANCAL (vanadium calibration function) setup:
# @param	ds0	
# @param	nrm_van -van dataset;
# @param	DOSmooth -vanadium data smoothing switch, default false;
# @param	temperature -temperature;
# @param	mulstep -multiple scattering calculation step size;

# ANALYZE (sample and can multople scattering and attenuation correction) setup:
# @param	ds0
# @param	ds	-a dataset;
# @param	imask 	-type flag: 1 for sample rod, 2 for empty can, 3 for sample+can (normal sample)
# @param	mulstep -multiple scattering calculation step size, default 0.1;
# @param	absstep -attenuation calculation step size, default 0.02;
# @param	usemutfile -wether to use 3 column mut file or not, default false;
# @param	mutfile -path of the 3 column mut file;
# @param	minw, default 0.1;
# @param	maxw, default 4.3
# @param	dw, default 0.1;
# @param	scattererm, sample calibration constant, default is calculated as rho*bht*Pi*r^2;

# DISTINCT (subtract the self-scattering part from differential cross section, also prepare the flux function for weighting) setup:
# @param	ds0;
# @param	dcs_smp	-sample dataset;
# @param	smo_van	-van clibration function dataset;
# @param	dm_van	-van monitor dataset;
# @param	temperature -default 300.0;
# @param	wmin	-default 0.1;
# @param	wmax	-default 6.0;

#COMBINE (merging IOQ from all the data groups with flux weighting) setup:
# @param	int_smp -sample dataset;
# @param	flx_van	-van flux function dataset;
# @param	NUMQ    -default 40;
# @param	GLADQMAX -default 40.0;

#???@return  null or an ErrorString.   The result will be written to a file
$ Category = Macros, Instrument Type, TOF_NGLAD

#================= Parameters ======================================
#configuration:
#$Configfile LoadFileString() Configratuion file:
$vanopt ChoiceList(["1: 3/8 inch vanadium rod", "2: 1/4 inch vanadium rod", "x: others"]) Which calibration rod?
$smpcomposition String("Si 1.0 O 2.0") Sample composition:
$smprho Float(0.05) Sample effective density:
$canopt ChoiceList(["1: 3/8 inch vanadium can", "2: 1/4 inch vanadium can", "x: others"]) Which can?
$scc Float(0.0) SCC ("0" uses the default calculated value)
#$cancomposition String("V 1.0") Can composition:
#$canrho Float(0.07205) Can density:

# run files:
$VanFile	LoadFileString("C:\Isaw\SampleRuns\glad9007.run") Vanadium run file:
$SmpFile        LoadFileString("C:\Isaw\SampleRuns\glad9011.run") Sample run file:
$CanFile	LoadFileString("C:\Isaw\SampleRuns\glad9010.run")  Can run file:
$BkgFile        LoadFileString("C:\Isaw\SampleRuns\glad9006.run") Background run file:

# dead detector list:
$REDPAR LoadFileString("C:\Isaw\Databases\gladrun.par") Bad detector list file:
$lcutoff Float(20.0) lower cutoff value to remove a data block:

# VANCAL (vanadium calibration function) setup:
$doSmooth 	Boolean(false)	Smooth vanadium data?
$VanTemp 	Float(300.0)		vanadium temperature:
#$VanMulStep 	Float(0.1)			vanadium multiple scattering calculation step size (cm):

# ANALYZE (sample and can multople scattering and attenuation correction) setup:
#$ismptype	Integer(3) smptype: 1 for sample rod, 2 for empty can, 3 for sample+can (normal sample)
#$icantype       Integer(2) cantype: 2
#$SmpMulStep  Float(0.1) 			   Sample multiple scattering calculation step size (cm):
#$SmpAbsStep  Float(0.02)		   Sample attenuation calculation step size (cm):
$UseMutFile	Boolean(false) Use measured sample cross section values:
$MutFile	LoadFileString()		Sample cross section MUT file:
$MinW		Float(0.1)				Minimal wavelength for cross section calculation:
$MaxW		Float(4.3)				Maximal wavelength:
$DW		Float(0.1)				Wavelength step size:

# DISTINCT (subtract the self-scattering part from differential cross section, also prepare the flux function for weighting) setup:
$SmpTemp Float(300.0)	Sample temperature:
$Wmin 		Float(0.1)		Use data with wavelength larger than:
$Wmax		Float(6.0)		Use data with wavelength smaller than:

#COMBINE (merging IOQ from all the data groups with flux weighting) setup:
$DoMerge 	Boolean(true)	Merge data?

#Q2R (fourier transform S(Q) to D(R)) then caculate other real space functions)
$QCut	Float(25.0) Qcut:

runinfo=GLAD_CONFIGURE(vanopt, smpcomposition, smprho, canopt, scc)

mon_nrm_van=GLAD_CRUNCH(runinfo, VanFile, true, REDPAR)
mon_nrm_can=GLAD_CRUNCH(runinfo, CanFile)
mon_nrm_smp=GLAD_CRUNCH(runinfo, SmpFile)
mon_nrm_bkg=GLAD_CRUNCH(runinfo, BkgFile)

mon_van=mon_nrm_van[0]
nrm_van=mon_nrm_van[1]
nrm_can=mon_nrm_can[1]
nrm_smp=mon_nrm_smp[1]
nrm_bkg=mon_nrm_bkg[1]

Sub(nrm_van, nrm_bkg, false)
Sub(nrm_can, nrm_bkg, false)
Sub(nrm_smp, nrm_bkg, false)

smo_van=GLAD_VANCAL(runinfo, nrm_van, doSmooth, VanTemp)
Div(nrm_can, smo_van, false)
Div(nrm_smp, smo_van, false)

nrm_can=GLAD_ANALYZE(runinfo, nrm_can, 2)
Sub(nrm_smp, nrm_can, false)
nrm_smp=GLAD_ANALYZE(runinfo, nrm_smp, 3)

GLAD_DISTINCT(runinfo, nrm_smp, nrm_van, mon_van, SmpTemp, Wmin, Wmax)

if DoMerge	
	nrm_smp=GLAD_COMBINE(nrm_smp, nrm_van)
endif
send nrm_smp
GLAD_Q2R(runinfo, nrm_smp)
#GLAD_Q2R(runinfo, nrm_smp, 0.1)
