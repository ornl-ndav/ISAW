#@overview - Calculate the neutron and background correction on files using the Reduce_KCL class
# @param   SampleFile- The name of the Run file for the sample
# @param   BackgroundFile - The name of the Run file to be used as the background run
# @param   CadmiumFile - The name of the Run file for the Cadmium run
# @param   TransSFile - The sample/empty camera transmission cf file
# @param   TransBFile - The empty cell/empty camera transmission cf file
# @param   EffFile - The EFR#####.dat file produced using Efficiency.iss
# @param   SensFile - The sens#####.dat file produced using Sensitivity.iss
# @param   NeutronDelay - The fraction for adjusting Neutron Delay
# @param   qu - Qbins, can be one-dimensional(list of numbers) or 2D(Qxmin, Qxmax, Qymin, Qymax)
# @param Scale- Scale factor 
# @param Thick - Sample thickness
# @param Xoff - Beam center x cood found from the Center.java class
# @param Yoff - Beam center y cood found from the Center.java class
# @param NQxBins if positive then do the 2D case for Qbins
# @param NQyBins if positive then do the 2D case for Qbins

# @return  null or an ErrorString.   The result will be written to a file
$ Category = Operator, Generic, TOF_SAD, Scripts

#================= Parameters ======================================
$Input_Path  String( "C:/new_das_runs/")  Input path
$Output_Path  String( "C:/test_output/")  Output path
$inst String("sand") Instrument name
$ext String(".run") Runfile extension
$Thick Float( .1) Thickness of sample

$TransSFile      String("20283") Enter Sample transmission run number
$SampleFile      String("20284") Enter Sample scattering run number

$BackGroundTFile String("20285") Enter Background transmission run number
$BackGroundFile  String("20288")  Enter Background scattering run number

$CadmiumTFile    String("20291") Enter Cadmium transmission run number
$CadmiumFile     String("20292") Enter Cadmium scattering run number
$ useCadmiumRun  Boolean( true)    Use Cadmium transmission run?

$CameraFile      String("20287") Enter Empty Camera transmission run number

$ polyfitIndx1   Integer( 11)      First time channel for poly fit, or -1 if no fit
$ polyfitIndx2   Integer( 70)      Last time channel for poly fit, or -1 if no fit
$ polyDegree     Integer( 3)      The Degree of the fit polynomial
$ sqrtWeight     Boolean( true)    Use 1/sqrt(y) for weightin
$ NeutronDelay   Float( .0011)      Delayed Neutron Fraction

$ useEmptyCell      Boolean( true)    For transmission: Is background different from empty camera?

$ qu   Qbins     Enter Q
#qu=[-.5,.5,-.5,.5]
$ useDefault        Boolean( true)    Use Default Q's (0.0035, 1.04; 117)? Constant dQ/Q
$EffFile         String("20270") Enter Efficiency Ratio file number
$SensFile        String("20337") Enter Sensitivity file number

$Scale Float( 843000)  Scale factor
$Xoff Float(.0015) Beam center x cood
$Yoff Float( .0062) Beam center y cood
$NQxBins Integer(-200) Enter Num Q x bins
$NQyBins Integer(-200) Enter Num Q y bins

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
#send DSS
TransSFile = Output_Path&"T"&TransSFile&CameraFile&".cf"
PrintFlood( DSS,TransSFile, "Transmission")
Echo("Sample/Camera Transmission done ")
#========== Calculation of transmission for cell/camera ====================
load Input_Path&inst&SampleFile&ext,"RUNSds"
Echo("Loading Sample Scattering "&RUNSds)
GroupID =GetAttr(RUNSds[1],0,"Group ID")
n = NumBins( RUNSds[1],GroupID,0,200000)
Display "numBins="&n
 if useCadmiumRun == true
DSC = CalcTransmission( Cell[0],Empty[0],Cadm[0],Data[1],useCAdmiumRun,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
else
DSC = CalcTransmission( Cell[0],Empty[0],Samp[0] ,Data[1],false,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
endif
TransBFile = Output_Path&"T"&BackGroundTFile&CameraFile&".cf"
PrintFlood( DSC,TransBFile, "Transmission")
TransB = ReadTransmission( TransBFile, n)
send DSC

Echo("Cell/Camera Transmission done ")
Display "Finished Transmission runs"
#------------------------- Code for Reduce -----------------------

load Input_Path&inst&BackgroundFile&ext,"RUNBds"
Echo("Loading Background Scattering "&RUNBds)
load Input_Path&inst&CadmiumFile&ext,"RUNCds"
Echo("Loading Cadmium Scattering "&RUNCds)

EFR = Output_Path&"EFR"&EffFile&".dat"
sensitivity = Output_Path&"sens"&SensFile&".dat"
Eff =Read3Col1D( EFR,"Efficiency")
Echo("Reading Efficiency Ratio file "&EFR)
Sens =ReadFlood(sensitivity, 128,128)
Echo("Reading Sensitivity file "&sensitivity)
TransS = ReadTransmission( TransSFile, n) 
 
#Zero( Eff,0,0,9)

if useDefault == true
 qu[0] = 0.0035f
for i in [1:117]
     qu[i] = qu[i - 1] * 1.02f
endfor


for i in [118:157]
     qu[i] = qu[i - 1] * 1.05f
endfor
endif


Res=Reduce_KCL(TransS,TransB,Eff,Sens[0],qu,RUNSds[0],RUNSds[1],RUNBds[0],RUNBds[1],RUNCds[0],RUNCds[1],NeutronDelay,Scale,thick,Xoff,Yoff,NQxBins,NQybins,useEmptyCell)

SelectGroups( Res[2], "Group ID",0.0,0.0,"Between Max and Min", "Set Selected")
Display Res[2], "Selected Graph View"
for i in [0:2]
  if  NQxBins < 0
  send Res[i]
     Print3Col1D(Res[i], Output_Path&GetField(Res[i], "Title")&".d1t","Reduce Results", NeutronDelay)
  else
    Print4Col2D1Chan( Res[i], Output_Path&GetField(Res[i], "Title")&".d1t")
  endif
endfor
Echo("Finished REDUCE and written files: "  )
Echo (Output_Path&GetField(Res[0], "Title") )
Echo (Output_Path&GetField(Res[1], "Title") )
Echo (Output_Path&GetField(Res[2], "Title") )
Display "Finished Reduce"
#ExitDialog()

