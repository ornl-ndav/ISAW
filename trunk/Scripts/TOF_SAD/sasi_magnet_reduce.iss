# 
# Script to run reduce.iss in batch mode
#
# $Date$

$Category=Macros, Instrument Type, TOF_NSAS

$ number_of_runs         Integer(1)                Enter number of runs
$ L1                     float(7.5)                Initial Path length (L1)
$ L2                     float(1.44)                Initial Path length (L2)
$ do_2D                  Boolean(false)            Make 2D S(Qx,Qy)?

#========================== Reduce run inputs =========================
#======================================================================
Xoff = 0.0001856
Yoff =0.00008747
#==================== Start of Input files ============================
#======================================================================
#======================================================================

#Input_Path ="/IPNShome/sasi/data/"
#Output_Path ="/IPNShome/sasi/GeorgeUser/"


Input_Path ="c:/sasi/"
Output_Path ="c:/sasi/"

# Sample thickness in cm:
ThickA = [0.1,0.1]


# Sample Scattering runs:
SampleFileA = ["0042","0042"]


# Background scattering runs:
BackGroundFileA = ["0023","0023"] 

#Transmission run for sample/camera
 TSC = ["T00420018", "T00420018"]

#Transmission run for buffer/camera
 TBC = ["T00230018","T00230018"]


# Cadmium scattering runs:
CadmiumFileA = ["0022","0022"] 

# Use Cadmium runs in transmission calculations (true/false)?
useCadmiumRunA = [true,true,true,true,true,true,true,true,true] 

# Camera transmission runs
CameraTFileA = ["0018","0018"] 

# Sensitivity .dat files (use Sensitivity.iss script to produce them)
SensFileA = ["0012","0012"]

# Efficiency .dat files (use Efficiency.iss sccript to produce them)
EffFileA = ["0016","0016"]



#===================== End of Input files =======================
#================================================================
#================================================================
polyfitIndx1 =   11
polyfitIndx2  =  40
polyDegree   =   3
NeutronDelay =  0.0042

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

 qu[0] = 0.0065f
# qu[0] = 0.08f
  for i in [1:120]
    qu[i] = qu[i - 1] * 1.05f
 #  qu[i] = qu[i-1]+.0004
  endfor
endif

sqrtWeight   =   true
useDefault    =  true
inst= "sasi"
ext= ".run"
Scale = 539700


#======================== End of Reduce run inputs ====================

# loop over number of runs
for i in [0: number_of_runs-1]

Thick = ThickA[i]
#TransSFile = TransSFileA[i] 
SampleFile = SampleFileA[i]

#BackGroundTFile = BackGroundTFileA[i]
BackGroundFile = BackGroundFileA[i]

#CadmiumTFile = CadmiumTFileA[i]
CadmiumFile = CadmiumFileA[i]
useCadmiumRun = useCadmiumRunA[i]

CameraFile = CameraTFileA[i]
#EmptyCellFile = EmptyCellFileA
EffFile = EffFileA[i]
SensFile = SensFileA[i] 

#================= End of Parameters ======================================
load Input_Path&inst&SampleFile&ext,"RUNSds"
Echo("Loading Sample Scattering "&RUNSds)
RUNSds[1] = SetL2(RUNSds[1],L2)


load Input_Path&inst&BackgroundFile&ext,"RUNBds"
Echo("Loading Background Scattering "&RUNBds)
RUNBds[1] = SetL2(RUNBds[1],L2)

load Input_Path&inst&CadmiumFile&ext,"RUNCds"
Echo("Loading Cadmium Scattering "&RUNCds)
RUNCds[1] = SetL2(RUNCds[1],L2)

SetAttrs( RUNSds[1], "Initial Path", L1)
SetAttrs( RUNBds[1], "Initial Path", L1)
SetAttrs( RUNCds[1], "Initial Path", L1)
# send RUNSds[1]
EFR = Output_Path&"EFR"&EffFile&".dat"
sensitivity = Output_Path&"sens"&SensFile&".dat"
Eff =Read3Col1D( EFR,"Efficiency")
Echo("Reading Efficiency Ratio file "&EFR)
Sens =ReadFlood(sensitivity, 256,256)
Echo("Reading Sensitivity file "&sensitivity)

TransS = ReadTransmission( Output_Path&TSC[i]&".cf", 60) 
TransB = ReadTransmission( Output_Path&TBC[i]&".cf", 60) 

 
#Zero( Eff,0,0,9)

# Added the beam stop radius as the last parameter for Reduce_KCL, it is 1.5 cm for SAND and 2.1 cm for SASI

Res=Reduce_KCL(TransS,TransB,Eff,Sens[0],qu,RUNSds[0],RUNSds[1],RUNBds[0],RUNBds[1],RUNCds[0],RUNCds[1],NeutronDelay,Scale,thick,Xoff,Yoff,NQxBins,NQybins,true,2.1)


#Display Res[0], "NEW Selected Graph View"
#Display Res[1], "NEW Selected Graph View"
#SelectGroups( Res[2], "Group ID",0.0,0.0,"Between Max and Min", "Set Select")
#Display Res[2], "NEW Selected Graph View"
for j in [0:2]
  if  NQxBins < 0
  send Res[2]
     Print3Col1D(Res[j], Output_Path&GetField(Res[j], "Title")&".dat","Reduce Results", NeutronDelay)
  else
    Print4Col2D1Chan( Res[j], Output_Path&GetField(Res[j], "Title")&".dat")
  endif
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
