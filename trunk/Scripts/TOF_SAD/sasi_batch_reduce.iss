# 
# Script to run reduce.iss in batch mode
# 03/22/04
#

$ number_of_runs         Integer(1)                Enter number of runs
$ do_2D                  Boolean(false)            Make 2D S(Qx,Qy)?

#========================== Reduce run inputs =========================
#======================================================================
Xoff = 0.0001856
Yoff =0.00008747
#==================== Start of Input files ============================
#======================================================================
#======================================================================

Input_Path ="/IPNShome/sasi/data/"
Output_Path ="/IPNShome/sasi/GeorgeUser/"


#Input_Path ="c:/sasi/"
#Output_Path ="c:/sasi/"

# Sample thickness in cm:
ThickA = [0.1]


# Sample Scattering runs:
SampleFileA = "0042"


# Background scattering runs:
BackGroundFileA = "0023" 

#Transmission run for sample/camera
 TSC = "T00420018"

#Transmission run for buffer/camera
 TBC = "T00230018"






# Cadmium scattering runs:
CadmiumFileA = "0022" 

# Use Cadmium runs in transmission calculations (true/false)?
useCadmiumRunA = [true,true,true,true,true,true,true,true,true] 

# Camera transmission runs
CameraTFileA = "0018" 

# Sensitivity .dat files (use Sensitivity.iss script to produce them)
SensFileA = "0012"

# Efficiency .dat files (use Efficiency.iss sccript to produce them)
EffFileA = "0016"



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
SampleFile = SampleFileA

#BackGroundTFile = BackGroundTFileA[i]
BackGroundFile = BackGroundFileA

#CadmiumTFile = CadmiumTFileA[i]
CadmiumFile = CadmiumFileA
useCadmiumRun = useCadmiumRunA[i]

CameraFile = CameraTFileA
#EmptyCellFile = EmptyCellFileA
EffFile = EffFileA
SensFile = SensFileA 

#================= End of Parameters ======================================
load Input_Path&inst&SampleFile&ext,"RUNSds"
Echo("Loading Sample Scattering "&RUNSds)
load Input_Path&inst&BackgroundFile&ext,"RUNBds"
Echo("Loading Background Scattering "&RUNBds)
load Input_Path&inst&CadmiumFile&ext,"RUNCds"
Echo("Loading Cadmium Scattering "&RUNCds)

EFR = Output_Path&"EFR"&EffFile&".dat"
sensitivity = Output_Path&"sens"&SensFile&".dat"
Eff =Read3Col1D( EFR,"Efficiency")
Echo("Reading Efficiency Ratio file "&EFR)
Sens =ReadFlood(sensitivity, 256,256)
Echo("Reading Sensitivity file "&sensitivity)

TransS = ReadTransmission( Output_Path&TSC&".cf", 60) 
TransB = ReadTransmission( Output_Path&TBC&".cf", 60) 

 
#Zero( Eff,0,0,9)

# Added the beam stop radius as the last parameter for Reduce_KCL, it is 1.5 cm for SAND and 2.1 cm for SASI

Res=Reduce_KCL(TransS,TransB,Eff,Sens[0],qu,RUNSds[0],RUNSds[1],RUNBds[0],RUNBds[1],RUNCds[0],RUNCds[1],NeutronDelay,Scale,thick,Xoff,Yoff,NQxBins,NQybins,true,2.1)


#Display Res[0], "NEW Selected Graph View"
#Display Res[1], "NEW Selected Graph View"
#SelectGroups( Res[2], "Group ID",0.0,0.0,"Between Max and Min", "Set Select")
#Display Res[2], "NEW Selected Graph View"
for i in [0:2]
  if  NQxBins < 0
  send Res[2]
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
