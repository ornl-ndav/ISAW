#@overview - Calculate the neutron and background correction on files using the Reduce_KCL class
# @param   SampleFile- The name of the Run file for the sample
# @param   BackgroundFile - The name of the Run file to be used as the background run
# @param   CadmiumFile - The name of the Run file for the Cadmium run
# @param   TransSFile - The sample/empty camera transmission cf file
# @param   TransBFile - The empty cell/empty camera transmission cf file
# @param   EffFile - The efr#####.dat file produced using Efficiency.iss
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

SamplePath = "C:\\new_das_runs\\"
ResultsPath = "C:\\test_output\\"

$SampleFile      LoadFileString("C:/new_das_runs/sand20284.run") Enter Sample run
$BackGroundFile  LoadFileString("C:/new_das_runs/sand20288.run")  Enter Background run
$CadmiumFile     LoadFileString("C:/new_das_runs/sand20292.run") Enter Cadmium run

$TransSFile      LoadFileString("C:/test_output/t2028320287.cf") Enter sample/camera transmission file
$TransBFile      LoadFileString("C:/test_output/t2028520287.cf") Enter empty cell/camera transmission file

$EffFile         LoadFileString("C:/test_output/efr20270.dat") Enter efr file
$SensFile        LoadFileString("C:/test_output/sens20337.dat") Enter sensitivity file

$ qu   Qbins     Enter Q
#qu=[-.5,.5,-.5,.5]

$NeutronDelay Float( .0011) Neutron Delay Fraction
$Scale Float( 843000)  Scale factor
$Thick Float( .1) Thickness of sample
$Xoff Float(.0015) Beam center x cood
$Yoff Float( .0062) Beam center y cood
$NQxBins Integer(-200) Enter Num Q x bins
$NQyBins Integer(-200) Enter Num Q y bins

#------------------------- Code -----------------------
load SampleFile,"RUNSds"
load BackgroundFile,"RUNBds"
load CadmiumFile,"RUNCds"
TransS = ReadTransmission( TransSFile, 70) 
TransB= ReadTransmission(TransBFile,70)
Eff =Read3Col1D( EffFile,"Efficiency")
Sens =ReadFlood(SensFile, 128,128)
#Zero( Eff,0,0,9)
Res=Reduce_KCL(TransS,TransB,Eff,Sens[0],qu,RUNSds[0],RUNSds[1],RUNBds[0],RUNBds[1],RUNCds[0],RUNCds[1],NeutronDelay,Scale,thick,Xoff,Yoff,NQxBins,NQybins)

#Display Res[0]
#Display Res[1]
#Display Res[2]
for i in [0:2]
  if  NQxBins < 0

  send Res[i]
     Print3Col1D(Res[i], ResultsPath&GetField(Res[i], "Title")&".d1t","Reduce Results", NeutronDelay)
  else
    Print4Col2D1Chan( Res[i], ResultsPath&GetField(Res[i], "Title")&".d1t")
  endif
endfor
Display "Finished"



 



