# 
# Script to run reduce.iss in batch mode
# 03/22/04
#

$ number_of_runs         Integer(1)                Enter number of runs
$ do_2D                  Boolean(false)            Make 2D S(Qx,Qy)?

#========================== Reduce run inputs =========================
#======================================================================
Xoff = 0.001508991
Yoff =0.007103308

#======= Set Attribute Level to Avoid Loading Extra Attributes ========
#======================================================================
#======================================================================

SetAttrLevel( "TOFNSAS ANALYSIS" )

#==================== Start of Input files ============================
#======================================================================
#======================================================================

Input_Path ="/IPNShome/sand/data/"
Output_Path ="/IPNShome/sand/GeorgeUser/"

# Sample thickness in cm:
ThickA = [0.1,0.1,0.1,0.1,0.1]

# Sample Transmission runs:
TransSFileA = [22379,22365,22367,22369,22371]

# Sample Scattering runs:
SampleFileA = [22380,22366,22368,22370,22372]

# Background Transmission runs:
BackGroundTFileA = [22373,22373,22373,22373,22373] 

# Background scattering runs:
BackGroundFileA = [22374,22374,22374,22374,22374] 

# Cadmium Transmission runs:
CadmiumTFileA = [22225,22225,22225,22225,22225]  

# Cadmium scattering runs:
CadmiumFileA = [22226,22226,22226,22226,22226] 

# Use Cadmium runs in transmission calculations (true/false)?
useCadmiumRunA = [true,true,true,true,true,true,true,true,true] 

# Open Camera transmission runs
CameraTFileA = [22215,22215,22215,22215,22215] 

# Sensitivity .dat files (use Sensitivity.iss script to produce them)
SensFileA = [22205,22205,22205,22205,22205] 

# Efficiency .dat files (use Efficiency.iss sccript to produce them)
EffFileA = [22227,22227,22227,22227,22227] 

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
  DSS = CalcTransmission( Samp[0],Empty[0],Cadm[0],Data[1],useCAdmiumRun,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
else
   DSS = CalcTransmission( Samp[0],Empty[0],Samp[0] ,Data[1],false,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
endif
send DSS
Table(DSS, true, "File", Output_Path&"T"&TransSFile&CameraFile&".dat", "0:1", "HGT,F" , false)
TransSF = Output_Path&"T"&TransSFile&CameraFile&".cf"
PrintFlood( DSS,TransSF, "Transmission")

Echo("Sample/Camera Transmission done ")




#========== Calculation of transmission for background/camera ====================
if useCadmiumRun == true
DSC = CalcTransmission( Cell[0],Empty[0],Cadm[0],Data[1],useCAdmiumRun,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
else
DSC = CalcTransmission( Cell[0],Empty[0],Samp[0] ,Data[1],false,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
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
Sens =ReadFlood(sensitivity, 128,128)
Echo("Reading Sensitivity file "&sensitivity)
TransS = ReadTransmission( TransSF, 70) 
 
#Zero( Eff,0,0,9)

Res=Reduce_KCL(TransS,TransB,Eff,Sens[0],qu,RUNSds[0],RUNSds[1],RUNBds[0],RUNBds[1],RUNCds[0],RUNCds[1],NeutronDelay,Scale,thick,Xoff,Yoff,NQxBins,NQybins,useEmptyCell)


#Display Res[0], "NEW Selected Graph View"
#Display Res[1], "NEW Selected Graph View"
#SelectGroups( Res[2], "Group ID",0.0,0.0,"Between Max and Min", "Set Select")
#Display Res[2], "NEW Selected Graph View"
for i in [0:2]
  if  NQxBins < 0
  #send Res[i]
     Print3Col1D(Res[i], Output_Path&GetField(Res[i], "Title")&".dat","Reduce Results", NeutronDelay)
  else
    Print4Col2D1Chan( Res[i], Output_Path&GetField(Res[i], "Title")&".dat")
  endif
endfor
Echo("Finished REDUCE and written files: "  )
Echo (Output_Path&GetField(Res[0], "Title") )
Echo (Output_Path&GetField(Res[1], "Title") )
Echo (Output_Path&GetField(Res[2], "Title") )
#Display "Finished Reduce"
#ExitDialog()

endfor

Display "Finished"
Return "Finished"
