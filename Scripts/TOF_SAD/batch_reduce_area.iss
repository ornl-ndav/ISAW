# 
# Script to run reduce.iss in batch mode
# $Date$ 

$Category=Macros, Instrument Type, TOF_NSAS

$ number_of_runs         Integer(1)                Enter number of runs
$ do_2D                  Boolean(false)            Make 2D S(Qx,Qy)?
#$ TransID           Integer(2)       Enter ID for trans mon (USE 3 for BS det)
$channel_mask        String("1")       Channels to mask

#========================== Reduce run inputs =========================
#======================================================================
Xoff = 0.001533
Yoff =0.003799
Scale = 687352
#Change only if transmission ID changes.
TransID=2
#Change only if the starting group # for the area detector changes.
AreaStartGroup=5

#polyfitIndx1 & polyfitIndx are the first and last time channel no. to be
#used by the CalcTransmission routine.  Note that if these are set to -1 the
#limits will automatically be set to 1 & number of time channels for the area
#detector
polyfitIndx1 = -1
polyfitIndx2 = -1
#setting polyDegree to -1 sets no fitting of the transmission data.
polyDegree = -1
#======= Set Attribute Level to Avoid Loading Extra Attributes ========
#======================================================================
#======================================================================

#SetAttrLevel( "TOFNSAS ANALYSIS" )

#==================== Start of Input files ============================
#======================================================================
#======================================================================
Input_Path ="/IPNShome/sand/data/"
Output_Path ="/IPNShome/sand/GeorgeUser/"

#Input_Path ="c:/sand_lpsd_runs/"
#Output_Path ="c:/sand_lpsd_runs/"

# Sample thickness in cm:
ThickA = [0.1]

# Sample Transmission runs:
TransSFileA = [29196]

# Sample Scattering runs:
SampleFileA = [29197]

# Background Transmission runs:
BackGroundTFileA = [29188] 

# Background scattering runs:
BackGroundFileA = [29189] 

# Cadmium Transmission runs:
CadmiumTFileA = [29192]  

# Cadmium scattering runs:
CadmiumFileA = [29193] 

# Use Cadmium runs in transmission calculations (true/false)?
useCadmiumRunA = [true] 

# Open Camera transmission runs
CameraTFileA = [29186] 

# Sensitivity .dat files (use Sensitivity.iss script to produce them)
SensFileA = [29177] 

# Efficiency .dat files (use Efficiency.iss sccript to produce them)
EffFileA = [29183] 

#For transmission: Is background different from empty camera?
useEmptyCellA = [true]

#===================== End of Input files =======================
#================================================================
#================================================================
NeutronDelay =  0.0011

#check for consistancy in the number of files.
numThickA = ArrayLength(ThickA)
numTransSFileA = ArrayLength(TransSFileA)
numSampleFileA = ArrayLength(SampleFileA)
numBkgdTFileA = ArrayLength(BackGroundTFileA)
numBkgdFileA = ArrayLength(BackGroundFileA)
numCdTFileA = ArrayLength(CadmiumTFileA)
numCdFileA = ArrayLength(CadmiumFileA)
numUseCdRunA = ArrayLength(useCadmiumRunA)
numCameraTFileA = ArrayLength(CameraTFileA)
numSensFileA = ArrayLength(SensFileA)
numEffFileA = ArrayLength(EffFileA)
numUseEmptyCellA = ArrayLength(useEmptyCellA)

if numSampleFileA <> numThickA
  return "Number of entries for Sample Thickness does not match number of samples"
endif
if numSampleFileA <> numTransSFileA
  return "Number of entries for Transmission files does not match number of samples"
endif
if numSampleFileA <> numBkgdTFileA
  return "Number of entries for Background Transmission files does not match number of samples"
endif
if numSampleFileA <> numBkgdFileA
  return "Number of entries for background files does not match number of samples"
endif
if numSampleFileA <> numCdTFileA
  return "Number of entries for Cadmium Transmission files does not match number of samples"
endif
if numSampleFileA <> numCdFileA
  return "Number of entries for Cadmium files does not match number of samples"
endif


#
# For 2D case, use 200x200 square array extending +- 0.5 inverse angstroms
# in both the x and y directions
#
if do_2D == true
  qu=[-.5,.5,-.5,.5]
  NQxBins = 200
  NQyBins = 200

#
# For 1D case, mark the size of the square array as invalid and build the
# default set of q bins, increasing by 5% each step.
#
else
  NQxBins = -200
  NQyBins = -200

  qu[0] = 0.0035f
  for i in [1:117]
    qu[i] = qu[i - 1] * 1.05f
  endfor
endif

sqrtWeight   =   true
useDefault    =  true
inst= "sand"
ext= ".run"
#======================== End of Reduce run inputs ====================

# loop over number of runs
for i in [0: number_of_runs-1]

Thick = ThickA[i]
TransSFile = TransSFileA[i] 
SampleFile = SampleFileA[i]

BackGroundTFile = BackGroundTFileA[i]
BackGroundFile = BackGroundFileA[i]

CadmiumTFile = CadmiumTFileA[i]
CadmiumFile = CadmiumFileA[i]
useCadmiumRun = useCadmiumRunA[i]

CameraFile = CameraTFileA[i]

EffFile = EffFileA[i]
SensFile = SensFileA[i] 

useEmptyCell = useEmptyCellA[i]

#================= End of Parameters ======================================
load Input_Path&inst&TransSFile&ext, "Samp"
Echo("Loading Sample Transmission "&Samp)

load Input_Path&inst&CameraFile&ext, "Empty"
Echo("Loading Camera Transmission "&Empty)

load Input_Path&inst&SampleFile&ext, "Data"
Echo("Loading Sample Scattering "&Data)

load Input_Path&inst&BackGroundTFile&ext, "Cell"
Echo("Loading Background Transmission "&Cell)

#determine the number of points in the transmission file from the number of 
# time channels in the spectrum.
nPtsTransFile = NumBins(Data[1], areaStartGroup, 0.0, 33333.0)
if polyfitIndx1 = -1
   polyfitIndx1 =   1
endif
if polyfitIndx2 = -1
   polyfitIndx2  =  nPtsTransFile
endif

#========== Determine which dataset will be used for cadmium monitor run in 
#calculatation of transmission
if useCadmiumRun == true
  load Input_Path&inst&CadmiumFile&ext, "Cadm"
  cdMonPlaceHolder = Cadm[0]
else
  cdMonPlaceHolder = Samp[0]
endif

#========== Calculation of transmission for sample/camera ====================
DSS = CalcTransmission( Samp[0],Empty[0],cdMonPlaceHolder,Data[1],useCAdmiumRun,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight,1,TransID)
send DSS

Table(DSS, true, "File", Output_Path&"T"&TransSFile&CameraFile&".dat", "0:1", "HGT,F" , false)

TransSF = Output_Path&"T"&TransSFile&CameraFile&".cf"

PrintFlood( DSS,TransSF, "Transmission")

Echo("Sample/Camera Transmission done ")




#========== Calculation of transmission for background/camera ====================
DSC = CalcTransmission( Cell[0],Empty[0],cdMonPlaceHolder,Data[1],useCAdmiumRun,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight,1,TransID)

TransBFile = Output_Path&"T"&BackGroundTFile&CameraFile&".cf"

PrintFlood( DSC,TransBFile, "Transmission")

TransB = ReadTransmission( TransBFile, nPtsTransFile)

Table(DSC, true, "File", Output_Path&"T"&BackGroundTFile&CameraFile&".dat", "0:1", "HGT,F" , false)

send DSC

Echo("Background/Camera Transmission done ")

Display "Finished Transmission runs"
#------------------------- Code for Reduce -----------------------
load Input_Path&inst&SampleFile&ext,"RUNSds"
Echo("Loading Sample Scattering "&RUNSds)

load Input_Path&inst&BackgroundFile&ext,"RUNBds"
Echo("Loading Background Scattering "&RUNBds)

load Input_Path&inst&CadmiumFile&ext,"RUNCds"
Echo("Loading Cadmium Scattering "&RUNCds)

EFR = Input_Path&"EFR"&EffFile&".dat"
sensitivity = Input_Path&"sens"&SensFile&".dat"

Eff =Read3Col1D( EFR,"Efficiency")
Echo("Reading Efficiency Ratio file "&EFR)

#
#  For now calculate area sensitivity "on the fly", 
#  since a file format for it has not been determined yet
#
sensitivity = Input_Path&"sand"&SensFile&".run"
load sensitivity, "sens_run"
first_area_id = 5
last_area_id = 16388
dead_level = 0.5
hot_level = 1.5
Sens = DetSens( sens_run[1], dead_level, hot_level )
send Sens[0] 

RUNSds[1]= ExtAtt(RUNSds[1], "Group ID", true, first_area_id, last_area_id)
RUNBds[1]= ExtAtt(RUNBds[1], "Group ID", true, first_area_id, last_area_id)
RUNCds[1]= ExtAtt(RUNCds[1], "Group ID", true, first_area_id, last_area_id)



#Sens =ReadFlood(sensitivity, 128,128)
#Echo("Reading Sensitivity file "&sensitivity)

TransS = ReadTransmission( TransSF, nPtsTransFile) 
 
#Zero( Eff,0,0,9)
# Added the beam stop radius as the last parameter for Reduce_KCL, it is 1.5 cm for SAND and 2.1 cm for SASI
#Res=Reduce_KCL(TransS,TransB,Eff,Sens[0],qu,RUNSds[0],RUNSds[1],RUNBds[0],RUNBds[1],RUNCds[0],RUNCds[1],NeutronDelay,Scale,thick,Xoff,Yoff,NQxBins,NQybins,useEmptyCell,1.5)
Res=MaskedReduce(TransS,TransB,Eff,Sens[0],qu,RUNSds[0],RUNSds[1],RUNBds[0],RUNBds[1],RUNCds[0],RUNCds[1],NeutronDelay,Scale,thick,Xoff,Yoff,NQxBins,NQybins,useEmptyCell,1.5, -1, channel_mask)
 


#Display Res[0], "NEW Selected Graph View"
#Display Res[1], "NEW Selected Graph View"
#SelectGroups( Res[2], "Group ID",0.0,0.0,"Between Max and Min", "Set Select")
#Display Res[2], "NEW Selected Graph View"
for j in [0:2]
  if  NQxBins < 0
  send Res[j]
     Print3Col1D(Res[j], Output_Path&GetField(Res[j], "Title")&".dat","Reduce Results", NeutronDelay)
  else
    Print4Col2D1Chan( Res[j], Output_Path&GetField(Res[j], "Title")&".dat")
  endif
endfor
if do_2D == true
    ss = SWV(Output_Path&GetField(Res[2], "Title")&".dat")
  endif
Echo("Finished REDUCE and written files: "  )
Echo (Output_Path&GetField(Res[0], "Title") )
Echo (Output_Path&GetField(Res[1], "Title") )
Echo (Output_Path&GetField(Res[2], "Title") )
#Display "Finished Reduce"
#ExitDialog()
Res=null
RunSDS = null
RUNCds = null
RunBDS = null
endfor

Display "Finished"
Return "Finished"


