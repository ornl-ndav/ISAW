# 
# Script to run reduce.iss in batch mode
# 09/24/03

$ number_of_runs         Integer(1)                Enter number of runs

#========================== Reduce run inputs =========================
Input_Path ="/IPNShome/sand/data/"
Output_Path ="/IPNShome/sand/chensept03/"

#Input_Path ="C:/new_das_runs/"
#Output_Path = "C:/test_output/"

inst= "sand"
ext= ".run"
Scale = 843000
Xoff = 0.000645
Yoff =0.006867
#==================== Start of Input files ==================
ThickA = [0.2,0.104]
# ThickA are the thickness of the samples

TransSFileA = [21581,21119,21121]
# TransSFileA are the Transmission runs for the samples

SampleFileA = [21582,21120,21122]
# SampleFileA are the Scattering runs for the samples

BackGroundTFileA = [21583,20529,20529] 
# BackGroundTFileA are the Background Transmission runs for the samples

BackGroundFileA = [21584,20530] 
# BackGroundFileA are the Background scattering runs for the samples

CadmiumTFileA = [21127,20535]  
# CadmiumTFileA are the Cadmium Transmission runs for the samples

CadmiumFileA = [21128,20536] 
# CadmiumFileA are the Cadmium scattering runs for the samples

useCadmiumRunA = [true, true] 
# useCadmiumRunA are boolean (true/false) values for using/not using the Cadmium runs 
# in the transmission calculations

CameraFileA = [21125,20531] 
# CameraFileA are the Camera transmission runs

EffFileA = [21115,20270] 
# EffFileA are the Efficiency .dat files

SensFileA = [20616,20616] 
# SensFileA are the sensitivity .dat files


#===================== End of Input files =======================
polyfitIndx1 =   11
polyfitIndx2  =  70
polyDegree   =   3
sqrtWeight   =   true
NeutronDelay =  0.0011

useEmptyCell =  false
useDefault    =  true

NQxBins = -200
NQyBins = -200

#======================== End of Reduce run inputs ====================

# loop over number of runs
for i in [0: number_of_runs-1]

Thick = ThickA[i]
TransSFile = TransSFileA[i] 
SampleFile = SampleFileA[i]

BackGroundTFile = BackGroundTFileA[0]
BackGroundFile = BackGroundFileA[0]

CadmiumTFile = CadmiumTFileA[0]
CadmiumFile = CadmiumFileA[0]
useCadmiumRun = useCadmiumRunA[0]

CameraFile = CameraFileA[0]

EffFile = EffFileA[0]
SensFile = SensFileA[0] 

Reduce(Input_Path, Output_Path, inst, ext, Thick, TransSFile, SampleFile,BackGroundTFile,BackGroundFile,CadmiumTFile,CadmiumFile,useCadmiumRun,CameraFile,polyfitIndx1, polyfitIndx2, polyDegree, sqrtWeight, NeutronDelay, useEmptyCell,useDefault,EffFile,SensFile,Scale,Xoff,Yoff,NQxBins, NQyBins)

endfor
Display "Finished"
