# 
# Script to integrate peaks in multiple SCD files. 
# $Date$
#
# First specify any parameters to the script, giving the variable name,
# data type and prompt string.  A dialog box will prompt the user for values
# for these parameters
#
# Assumptions:
#  - Data of interest is in histogram 2
#  - The run number requires a '0' before it
#  - There is a "lsxxxx.mat" file for each xxxx run.

$ path                DataDirectoryString    Raw Data Path
$ outpath             DataDirectoryString    Output Data Path
$ run_numbers         Array                  Run Number
$ expname             String                 Experiment Name
$ centering           String(primitive)      Centering type
#$ calibfile           LoadFileString         SCD Calibration File
$ time_slice_range    String(-1:3)           Time-slice range
$ increase            Integer(1)             Increase slice size by

inst = "SCD"
Display "Instrument = "&inst
#Display "Calibration File ="&calibfile

# DataSet is number two
dsnum=2

first=true
append=false

for i in run_numbers
  # load data
  filename=path&inst&0&i&".RUN"
  Display "Loading "&filename
  Echo("Integrating peaks in "&filename)
  load(filename,"ds")

  # The calibration file "instprm.dat" must be in the outpath directory.
  LoadSCDCalib(ds[dsnum],outpath&"instprm.dat",1,"")
  # LoadSCDCalib(ds[dsnum],outpath&"instprm.dat",2,"")

  # integrate peaks
  #Display(ds[dsnum])

  #Gets matrix file "lsxxxx.mat" for each run
  #The "1" means that every peak will be written to the integrate.log file.
  SCDIntegrate(ds[dsnum],outpath&expname&".integrate",outpath&"ls"&i&".mat",centering,time_slice_range,increase,1,append)

  #Integrate
  # write out the results
  #WritePeaks(outpath&expname&".peaks",peaks,append)
  if first
    first=false
    append=true
  endif
endfor

Echo("--- integrate_multiple_runs is done. ---")
# show the integrate file
ViewASCII(outpath&expname&".integrate")
Display("Peaks are listed in "&outpath&expname&".integrate")
# close the dialog automatically
ExitDialog()
