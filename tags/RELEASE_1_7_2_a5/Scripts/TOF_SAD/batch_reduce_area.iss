# 
# Script to run reduce.iss in batch mode
# $Date$ 

$Category=Macros, Instrument Type, TOF_NSAS

$ number_of_runs         Integer(1)                Enter number of runs
$ do_2D                  Boolean(false)            Make 2D S(Qx,Qy)?

#========================== Reduce run inputs =========================
#======================================================================
Xoff = 0.0008005
Yoff =0.00705559


#======= Set Attribute Level to Avoid Loading Extra Attributes ========
#======================================================================
#======================================================================

#SetAttrLevel( "TOFNSAS ANALYSIS" )

#==================== Start of Input files ============================
#======================================================================
#======================================================================
#Input_Path ="/IPNShome/sand/data/"
#Output_Path ="/IPNShome/sand/GeorgeUser/"

Input_Path ="c:/sand_lpsd_runs/"
Output_Path ="c:/sand_lpsd_runs/"

# Sample thickness in cm:
ThickA = [0.1,0.1,0.1,0.1,0.1]

# Sample Transmission runs:
TransSFileA = [24201,22367,22369,22371]

# Sample Scattering runs:
SampleFileA = [24201,22368,22370,22372]

# Background Transmission runs:
BackGroundTFileA = [24158,22373,22373,22373] 

# Background scattering runs:
BackGroundFileA = [24158,22374,22374,22374] 

# Cadmium Transmission runs:
CadmiumTFileA = [24166,22225,22225,22225]  

# Cadmium scattering runs:
CadmiumFileA = [24166,22226,22226,22226,22226] 

# Use Cadmium runs in transmission calculations (true/false)?
useCadmiumRunA = [true,true,true,true,true,true,true,true,true] 

# Open Camera transmission runs
CameraTFileA = [24158,22215,22215,22215,22215] 

# Sensitivity .dat files (use Sensitivity.iss script to produce them)
SensFileA = [22205,22205,22205,22205] 

# Efficiency .dat files (use Efficiency.iss sccript to produce them)
EffFileA = [24159,22227,22227,22227,22227] 

#For transmission: Is background different from empty camera?
useEmptyCellA = [false,false,false,false,false]

#===================== End of Input files =======================
#================================================================
#================================================================
polyfitIndx1 =   11
polyfitIndx2  =  70
polyDegree   =   3
NeutronDelay =  0.0011

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
Scale = 644100
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

#========== Calculation of transmission for sample/camera ====================
if useCadmiumRun == true
  load Input_Path&inst&CadmiumFile&ext, "Cadm"
  DSS = CalcTransmission( Samp[0],Empty[0],Cadm[0],Data[1],useCAdmiumRun,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight,1,3)
else
   DSS = CalcTransmission( Samp[0],Empty[0],Samp[0] ,Data[1],false,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight,1,3)
endif
send DSS
Table(DSS, true, "File", Output_Path&"T"&TransSFile&CameraFile&".dat", "0:1", "HGT,F" , false)
TransSF = Output_Path&"T"&TransSFile&CameraFile&".cf"
PrintFlood( DSS,TransSF, "Transmission")

Echo("Sample/Camera Transmission done ")




#========== Calculation of transmission for background/camera ====================
if useCadmiumRun == true
DSC = CalcTransmission( Cell[0],Empty[0],Cadm[0],Data[1],useCAdmiumRun,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight,1,3)
else
DSC = CalcTransmission( Cell[0],Empty[0],Samp[0] ,Data[1],false,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight,1,3)
endif
TransBFile = Output_Path&"T"&BackGroundTFile&CameraFile&".cf"
PrintFlood( DSC,TransBFile, "Transmission")
TransB = ReadTransmission( TransBFile, 70)
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

TransS = ReadTransmission( TransSF, 70) 
 
#Zero( Eff,0,0,9)

Res=Reduce_KCL(TransS,TransB,Eff,Sens[0],qu,RUNSds[0],RUNSds[1],RUNBds[0],RUNBds[1],RUNCds[0],RUNCds[1],NeutronDelay,Scale,thick,Xoff,Yoff,NQxBins,NQybins,useEmptyCell)


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


