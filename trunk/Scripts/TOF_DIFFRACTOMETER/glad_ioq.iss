#@overview - One button for all: calculate the interference part of differential cross section (IOQ) 

# run files:
# @param	VanFile -path of the vanadium run file;
# @param	SmpFile -path of the sample run file;
# @param	CanFile -path of the can run file;
# @param   BkgFile -path of the background run file;

# dead detector list:
# @param	REDPAR -path of the dead detector list file;

# VANCAL (vanadium calibration function) setup:
# @param	VanISVac1 -boolean indicating VAC 1;
# @param	ISVanSize1 -boolean indicating vanadium calibration rod 1 (3/8");
# @param	ISVanSize2 -1/4" vanadium calibration rod;
# @param	DOSmooth -vanadium data smoothing switch;
# @param	VanTemp -temperature;
# @param	VanMulStep -multiple scattering calculation step size;

# ANALYZE (sample and can multople scattering and attenuation correction) setup:
# @param	SmpISVac1 -sample at VAC 1;
# @param	ISCanSize1 -vanadium can size 1 (3/8");
# @param	ISCanSize2 -1/4" can;
# @param	SmpComposition -sample composition in the format (H 1 O 2.0);
# @param	SmpNumberDensity -sample number density (atoms per cubic angstrom);
# @param	SmpMulStep -multiple scattering calculation step size;
# @param	SmpAbsStep -attenuation calculation step size;

# DISTINCT (subtract the self-scattering part from differential cross section, also prepare the flux function for weighting) setup:
# @param	SmpTemp -temperature for Placzek correction;
# @param	Wmin
# @param	Wmax -use data between the wavelength range [Wmin, Wmax];

#COMBINE (merging IOQ from all the data groups with flux weighting) setup:
# @param DoMerge -data merging switch;

#???@return  null or an ErrorString.   The result will be written to a file
#???$ Category = Operator, Generic, TOF_DIFFRACTOMETER, Scripts

#================= Parameters ======================================

# run files:
$VanFile	LoadFileString() Vanadium run file:
$SmpFile LoadFileString() Sample run file:
$CanFile	LoadFileString()  Can run file:
$BkgFile  LoadFileString()	Background run file:

# dead detector list:
#$REDPAR LoadFileString() Bad detector list file:

# VANCAL (vanadium calibration function) setup:
$VanISVac1 	Boolean(true) 		GLAD at VAC1 (vanadium)?
$REDPAR LoadFileString() 		Bad detector list file:
$ISVanSize1 	Boolean(true)		3/8" vanadium calibration rod?
$ISVanSize2 	Boolean(false)	1/4" vanadium calibration rod?
$DOSmooth 	Boolean(false)	Smooth vanadium data?
$VanTemp 		Float(300.0)		vanadium temperature:
$VanMulStep 	Float(0.1)			vanadium multiple scattering calculation step size (cm):

# ANALYZE (sample and can multople scattering and attenuation correction) setup:
$SmpISVac1 	Boolean(true)		GLAD at VAC1 (sample)?
$ISCanSize1 	Boolean(true)		3/8" vanadium can?
$ISCanSize2 	Boolean(false)	1/4" vanadium can?
$SmpHeight Float(6.0)				Sample Height:
$SmpComposition String			Sample composition (Format: H 1 O 2.0):
$SmpNumberDensity Float(0.05) Sample number density:
$SmpMulStep  Float(0.1) 			   Sample multiple scattering calculation step size (cm):
$SmpAbsStep  Float(0.02)		   Sample attenuation calculation step size (cm):

# DISTINCT (subtract the self-scattering part from differential cross section, also prepare the flux function for weighting) setup:
$SmpTemp Float(300.0)	Sample temperature:
$Wmin 		Float(0.1)		Use data with wavelength larger than:
$Wmax		Float(6.0)		Use data with wavelength smaller than:

#COMBINE (merging IOQ from all the data groups with flux weighting) setup:
$DoMerge 	Boolean(true)	Merge data?

nrm_van=GLAD_CRUNCH(VanFile, true,REDPAR)
nrm_smp=GLAD_CRUNCH(SmpFile, false, REDPAR)
nrm_can=GLAD_CRUNCH(CanFile, false, REDPAR)
nrm_bkg=GLAD_CRUNCH(BkgFile, false, REDPAR)
Sub(nrm_van, nrm_bkg, false)
Sub(nrm_smp, nrm_bkg, false)
Sub(nrm_can, nrm_bkg, false)
smo_van =GLAD_VANCAL(VanISVac1, nrm_van, DOSmooth, ISVanSize1, ISVanSize2, VanTemp, VanMulStep)
Div(nrm_smp, smo_van, false)
Div(nrm_can, smo_van, false)
nrm_can=GLAD_ANALYZE(SmpISVac1, true, nrm_can, ISCanSize1, ISCanSize2, SmpHeight, SmpComposition, SmpNumberDensity, SmpMulStep, SmpAbsStep, false)
Echo("subtract the can contribution from the sample data...")
Sub(nrm_smp, nrm_can, false)
Echo("Done.")
nrm_smp=GLAD_ANALYZE(SmpISVac1, false, nrm_smp, ISCanSize1, ISCanSize2, SmpHeight, SmpComposition, SmpNumberDensity, SmpMulStep, SmpAbsStep, false)
GLAD_DISTINCT(nrm_smp, nrm_van, SmpComposition, SmpTemp, Wmin, Wmax)
if DoMerge	
	nrm_smp=GLAD_COMBINE(nrm_smp, nrm_van)
endif
Display nrm_smp


