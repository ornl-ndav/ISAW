#@overview - rought S(Q)

# GLAD instrument configuration:
# @param	ConfigFile -path of the GLAD configuration file;
# run files:
# @param	VanFile -path of the vanadium run file;
# @param	SmpFile -path of the sample run file;
# @param	CanFile -path of the can run file;
# @param   	BkgFile -path of the background run file;

# dead detector list:
# @param	REDPAR -path of the dead detector list file;

#================= Parameters ======================================

# config file:
$ConfigFile	LoadFileString("/IPNShome/taoj/cvs/ISAW/Databases/gladprops.dat") 	GLAD configuration file:

# run files:
$VanFile	LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad7867.run") 	Vanadium run file:
$SmpFile 	LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8086.run") 	Sample run file:
$CanFile	LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad8266.run")  	Can run file:
$BkgFile  	LoadFileString("/IPNShome/taoj/cvs/ISAW/SampleRuns/glad7821.run")	Background run file:

# dead detector list:
$REDPAR LoadFileString("/IPNShome/taoj/GLAD/gladrun.par") Bad detector list file:

# WEIGHTING (prepare the flux function for weighting) setup:
$Wmin 		Float(0.1)		Use data with wavelength larger than:
$Wmax		Float(6.0)		Use data with wavelength smaller than:

#COMBINE (merging IOQ from all the data groups with flux weighting) setup:
$DoMerge 	Boolean(true)	Merge data?

runinfo=GLAD_CONFIGURE()

mon_nrm_van=GLAD_CRUNCH(runinfo, VanFile, true, REDPAR)
mon_nrm_smp=GLAD_CRUNCH(runinfo, SmpFile)
mon_nrm_can=GLAD_CRUNCH(runinfo, CanFile)
mon_nrm_bkg=GLAD_CRUNCH(runinfo, BkgFile)

mon_van=mon_nrm_van[0]
nrm_van=mon_nrm_van[1]
nrm_smp=mon_nrm_smp[1]
nrm_can=mon_nrm_can[1]
nrm_bkg=mon_nrm_bkg[1]

Sub(nrm_van, nrm_bkg, false)
Sub(nrm_smp, nrm_can, false)
Div(nrm_smp, nrm_van, false)

GLAD_Weight(nrm_van, mon_van, Wmin, Wmax)
if DoMerge	
	nrm_smp=GLAD_COMBINE(nrm_smp, nrm_van)
endif
Send nrm_smp
