# 
# Script to run reduce.iss in batch mode
# 03/22/04
#

$ number_of_runs         Integer(1)                Enter number of runs

#========================== Reduce run inputs =========================
#======================================================================
Xoff = 0.001508991
Yoff =0.007103308
#==================== Start of Input files ============================
#======================================================================
#======================================================================

#Input_Path ="/IPNShome/sand/data/"
#Output_Path ="/IPNShome/sand/GeorgeUser/"

Input_Path ="C:/sand_lpsd_runs/"
Output_Path ="C:/sand_lpsd_runs/"

#Input_Path ="/home/dennis/SAND_LPSD_RUNS/"
#Output_Path ="/home/dennis/SAND_LPSD_RUNS/"

SamplePath = Input_Path
# Sample thickness in cm:
ThickA = [0.2]

# Sample Transmission runs:
TransSFileA = [22842]

# Sample Scattering runs:
SampleFileA = [22843]

# Background Transmission runs:
BackGroundTFileA = [22582] 

# Background scattering runs:
BackGroundFileA = [22583] 

# Cadmium Transmission runs:
CadmiumTFileA = [22738]  

# Cadmium scattering runs:
CadmiumFileA = [22739] 

# Use Cadmium runs in transmission calculations (true/false)?
useCadmiumRunA = [true] 

# Camera transmission runs
CameraTFileA = [22832] 

# Sensitivity .dat files (use Sensitivity.iss script to produce them)
SensFileA = [22403]

# Efficiency .dat files (use Efficiency.iss sccript to produce them)
EffFileA = [22404] 

#===================== End of Input files =======================
#================================================================
#================================================================
polyfitIndx1 =   11
polyfitIndx2  =  70
polyDegree   =   3
NeutronDelay =  0.0011

#qu[0] = 0.05f
#for i in [1:95]
#  qu[i] = qu[i - 1] * 1.05f
#endfor

qu[0] = 0.05f
for i in [1:230]
  qu[i] = qu[i - 1] * 1.02f
endfor


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

TransSF = Output_Path&"T"&TransSFile&CameraFile&".cf"
PrintFlood( DSS, TransSF, "Transmission")
Table(DSS, true, "File", Output_Path&"T"&TransSFile&CameraFile&".dat", "0:1", "HGT,F" , false)
send DSS
#
#  Use the sample transmission DataSet directly, since writing
#  and then reading from file replaces the XScale in wavelength
#  with a uniform scale in channel number 0..70.
#
TransS = DSS 

Echo("Sample/Camera Transmission done ")

#========== Calculation of transmission for background/camera ====================
if useCadmiumRun == true
  DSC = CalcTransmission( Cell[0],Empty[0],Cadm[0],Data[1],useCAdmiumRun,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
else
  DSC = CalcTransmission( Cell[0],Empty[0],Samp[0] ,Data[1],false,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
endif

TransBFile = Output_Path&"T"&BackGroundTFile&CameraFile&".cf"
PrintFlood( DSC,TransBFile, "Transmission")
Table(DSC, true, "File", Output_Path&"T"&BackGroundTFile&CameraFile&".dat", "0:1", "HGT,F" , false)
send DSC
#
#  Use the background (or cadmium) transmission DataSet directly, since writing
#  and then reading from file replaces the XScale in wavelength
#  with a uniform scale in channel number 0..70.
#
TransB = DSC 
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
Eff = Read3Col1D( EFR,"Efficiency")
Echo("Reading Efficiency Ratio file "&EFR)
#
#  For now calculate lpsd sensitivity "on the fly", 
#  since a file format for it has not been determined yet
#
sensitivity = Input_Path&"sand"&SensFile&".run"
load sensitivity, "sens_run"
first_lpsd_id = 16389
last_lpsd_id = 18948
dead_level = 0.6
hot_level = 1.4
Sens = LPSDSens( sens_run[1], dead_level, hot_level, first_lpsd_id, last_lpsd_id )
send Sens[0] 

RUNSds[1]= ExtAtt(RUNSds[1], "Group ID", true, first_lpsd_id, last_lpsd_id)
RUNBds[1]= ExtAtt(RUNBds[1], "Group ID", true, first_lpsd_id, last_lpsd_id)
RUNCds[1]= ExtAtt(RUNCds[1], "Group ID", true, first_lpsd_id, last_lpsd_id)

Res=Reduce_LPSD(TransS,TransB,Eff,Sens[0],qu,RUNSds[0],RUNSds[1],RUNBds[0],RUNBds[1],RUNCds[0],RUNCds[1],NeutronDelay,Scale,thick,true)

send Res[0]
send Res[1]
send Res[2]
#Display Res[0], "NEW Selected Graph View"
#Display Res[1], "NEW Selected Graph View"
#SelectGroups( Res[2], "Group ID",0.0,0.0,"Between Max and Min", "Set Select")
#Display Res[2], "NEW Selected Graph View"
for j in [0:2]
  Print3Col1D(Res[j], Output_Path&GetField(Res[j], "Title")&".dat","Reduce Results", NeutronDelay)
endfor
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

