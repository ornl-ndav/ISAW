# 
# Script to run reduce.iss in batch mode
# 09/24/03

$ number_of_runs         Integer(1)                Enter number of runs

#========================== Reduce run inputs =========================
#Input_Path ="/IPNShome/sand/data/"
#Output_Path ="/IPNShome/sand/chensept03/"

Xoff = 0.000645
Yoff =0.006867
#==================== Start of Input files ==================

Input_Path ="C:/johan/"
Output_Path = "C:/johan/output/"

# Sample thickness in cm:
ThickA = [0.2,0.104]

# Sample Transmission runs:
TransSFileA = [21581,21119]

# Sample Scattering runs:
SampleFileA = [21582,21120]

# Background Transmission runs:
BackGroundTFileA = [21125,20529] 

# Background scattering runs:
BackGroundFileA = [21126,20530] 

# Cadmium Transmission runs:
CadmiumTFileA = [21127,20535]  

# Cadmium scattering runs:
CadmiumFileA = [21128,20536] 

# Use/Not use (true/false) Cadmium runs for transmission calculations
useCadmiumRunA = [true, true] 

# Camera transmission runs
CameraTFileA = [21125,20531] 

# SensFileA are the sensitivity .dat files (use Sensitivity.iss script to produce them)
SensFileA = [20616,20616] 

# EffFileA are the Efficiency .dat files (use Efficiency.iss sccript to produce them)
EffFileA = [21115,20270] 

#===================== End of Input files =======================
polyfitIndx1 =   11
polyfitIndx2  =  70
polyDegree   =   3
NeutronDelay =  0.0011
NQxBins = -200
NQyBins = -200
sqrtWeight   =   true
useDefault    =  true
inst= "sand"
ext= ".run"
Scale = 843000
#======================== End of Reduce run inputs ====================
if useDefault == false
$ qu   Qbins     Enter Q 
endif

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

Reduce(Input_Path, Output_Path, inst, ext, Thick, TransSFile, SampleFile,BackGroundTFile,BackGroundFile,CadmiumTFile,CadmiumFile,useCadmiumRun,CameraFile,polyfitIndx1, polyfitIndx2, polyDegree, sqrtWeight, NeutronDelay, true,qu,useDefault,EffFile,SensFile,Scale,Xoff,Yoff,NQxBins, NQyBins)

endfor
Display "Finished"


