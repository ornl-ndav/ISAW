#  Calculate Transmission from files
#@overview - Calculates relative transmission ratios for the sample relative to Empty runs. The 
# Cadmium run can optionally be subtracted from each
# @param   SampleFileName- The name of the Run file for the sample
# @param   EmptyFileName - The name of the Run file with an "Empty" run
# @param   useCadmiumRun - True if the cadmium run is to be used, otherwise false
# @param   CadmiumFileName - The name of the Run file for the Cadmium run
# @param   SaveFileName -The Name of the Dat file to save the result
# @param   NeutronDelay - The fraction for adjusting Neutron Delay
# @param   polyfitIndx1 - The first Channel to be used to fit a polynomial to the data. Use -1 to not fit
# @param   polyfitIndx2 - The last Channel to be used to fit a polynomial to the data. Use -1 to not fit
# @param   polyDegree  - The degree of the polynomial that will be used to fit the data
# @param  sqrtWeight   - Weight the bins by 1/sqrt(y) for errors
# @return  null or an ErrorString.   The result will be written to a file


$ SampleFileName    LoadFileString("C:\ISAW\SampleRuns\ins\sand20266.run")   Enter Sample run
$ EmptyFileName     LoadFileString("C:\ISAW\SampleRuns\ins\sand20275.run")    Enter Empty run
$ useCadmiumRun     Boolean( false)    Use CadmiumRun?
$ CadmiumFileName   LoadFileString("C:\ISAW\SampleRuns\ins\sand20281.run")    Enter Cadmium Run
$ SaveFileName      SaveFileString("C:\ISAW\SampleRuns\T1999019934.d2t")    Dat file to save Transm results
$ NeutronDelay      Float( .0011)     Neutron Delay Fraction
$ polyfitIndx1      Integer( 11)      First time channel for poly fit, or -1 if no fit
$ polyfitIndx2      Integer( 68)      Last time channel for poly fit, or -1 if no fit
$ polyDegree        Integer( 3)      The Degree of the fit polynomial
$ sqrtWeight        Boolean( true)    Use 1/sqrt(y) for weightin

$ Title = Calculate Transmission
$ Command = Transmission
$ Category = Operator, Generic, TOF_SAD, Scripts
load SampleFileName, "Samp"
load EmptyFileName, "Empty"
if useCadmiumRun == true
  load CadmiumFileName, "Cadm"
  DS = CalcTransmission( Samp[0],Empty[0],Cadm[0],Samp[1],useCAdmiumRun,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
else
   DS = CalcTransmission( Samp[0],Empty[0],Samp[0] ,Samp[1],false,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
endif
PrintFlood( DS,SaveFileName, "Transmission")
Display "Finished"



