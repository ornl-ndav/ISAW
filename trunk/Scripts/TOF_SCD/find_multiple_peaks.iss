# 
# Script to find peaks in multiple SCD files. 
# $Date$
#
# First specify any parameters to the script, giving the variable name,
# data type and prompt string.  A dialog box will prompt the user for values
# for these parameters
#
# Assumptions:
#  - Data of interest is in histogram 1
#  - The run number requires a '0' before it
#  - if the calibration file is not specified then the real space
#  conversion is not performed

$ CATEGORY = operator,Instrument Type, TOF_NSCD
$ path                DataDirectoryString    Raw Data Path
$ outpath             DataDirectoryString    Output Data Path
$ run_numbers         Array                  Run Number
$ expname             String                 Experiment Name
$ num_peaks           Integer(50)            Number of Peaks
$ min_int             Integer(3)             Minimum Peak intensity
$ min_time_chan       Integer(0)             Minimum time channel to use 
$ max_time_chan       Integer(1000)          Maximum time channel to use 
$ append              Boolean(true)          Append
$ calibfile           LoadFileString         SCD Calibration File

inst = "SCD"
#calibfile = "/IPNShome/scd/instprm.dat"
Display "Instrument="&inst
Display "Calibration File ="&calibfile
# DataSet is number two
# dsnum=1   //Changes depending on when the run file was made
first=true
for i in run_numbers
  # load data
  filename=path&inst&0&i&".RUN"
  Display "Loading "&filename
  Echo("Finding peaks in "&filename)
  nn=load(filename,"ds")
  dsnum = nn-1
  monct=IntegGrp(ds[0],1,0,50000)
  LoadSCDCalib(ds[dsnum],calibfile,-1,"")
  # find peaks
  peaks=FindPeaks(ds[dsnum],monct,num_peaks,min_int,min_time_chan,max_time_chan)
  peaks=CentroidPeaks(ds[dsnum],peaks)
  # write out the results
  WritePeaks(outpath&expname&".peaks",peaks,append)
  WriteSCDExp(ds[dsnum],ds[0],outpath&expname&".x",1,append)
  if first
    first=false
    append=true
  endif
endfor
Echo("--- find_multiple_peaks is done. ---")
# show the peaks file
ViewASCII(outpath&expname&".peaks")
Display("Peaks are listed in "&outpath&expname&".peaks")
# close the dialog automatically
ExitDialog()
