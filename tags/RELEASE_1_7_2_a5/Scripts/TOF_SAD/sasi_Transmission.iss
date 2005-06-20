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


$ SampleFileName    LoadFileString("/IPNShome/sasi/data/sasi0017.run")    Enter Sample run
$ EmptyFileName     LoadFileString("/IPNShome/sasi/data/sasi0018.run")    Enter Empty Camera run
$ useCadmiumRun     Boolean( true)    Use CadmiumRun?
$ CadmiumFileName   LoadFileString("/IPNShome/sasi/data/sasi0022.run")        Enter Cadmium Run
$ SaveFileName      SaveFileString("/IPNShome/sasi/GeorgeUser/T00170018.cf") Dat file to save Transm results
$ NeutronDelay      Float( .0042)     Neutron Delay Fraction
$ polyfitIndx1      Integer(1)      First time channel for poly fit, or -1 if no fit
$ polyfitIndx2      Integer(60)      Last time channel for poly fit, or -1 if no fit
$ polyDegree        Integer(3)      The Degree of the fit polynomial
$ sqrtWeight        Boolean(true)    Use 1/sqrt(y) for weightin

$ Title = sasi Calculate Transmission
$ Command = sasi_Transmission
$ Category = Macros, Instrument Type, TOF_NSAS 

load SampleFileName, "Samp"
load EmptyFileName, "Empty"
#load DataFileName, "Data"
Data=Samp
if useCadmiumRun == true
  load CadmiumFileName, "Cadm"
  DS = CalcTransmission( Samp[0],Empty[0],Cadm[0],Data[1],useCAdmiumRun,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)

else
   DS = CalcTransmission( Samp[0],Empty[0],Samp[0] ,Data[1],false,NeutronDelay, polyfitIndx1,polyfitIndx2,polyDegree,sqrtWeight)
endif

send DS

PrintFlood( DS,SaveFileName, "Transmission")
Table(DS, true, "File", SaveFileName&".dat", "0:1", "HGT,F" , false)

Display "Finished"
Return "Finished"

