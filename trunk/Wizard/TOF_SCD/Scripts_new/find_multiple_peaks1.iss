# 
# Script to find peaks in multiple SCD or SXD files. 
# $Date: 2004/07/12  $
#
# First specify any parameters to the script, giving the variable name,
# data type and prompt string.  A dialog box will prompt the user for values
# for these parameters
#
# Assumptions:
#  - Data of interest is in histogram 1
#  - The run number requires a '0' before it for SCD runs
#  - The Generic Calib operator is used to read the calibration file

$ CATEGORY = operator,Instrument Type, TOF_NSCD
$ path                DataDirectoryString    Raw Data Path
$ outpath             DataDirectoryString    Output Data Path
$ run_numbers         Array                  Run Number
$ expname             String                 Experiment Name
$ num_peaks           Integer(50)            Number of Peaks
$ min_int             Integer(3)             Minimum Peak intensity
$ min_time_chan       Integer(0)             Minimum time channel to use 
$ max_time_chan       Integer(1000)          Maximum time channel to use 
$ calibfile           LoadFileString         Calibration File
$ RowColKeep          IntList( 1:200)        Row,Col's to keep
$ inst                String("SCD0")         Instrument
$ DatFileExtension    String(".RUN")         Extension on Data Files
#calibfile = "/IPNShome/scd/instprm.dat"
Display "Instrument="&inst
Display "Calibration File ="&calibfile
# DataSet is number two
# dsnum=1   //Changes depending on when the run file was made
first=true
append = false
for i in run_numbers
  # load data
  filename=path&inst&i&DatFileExtension
  Display "Loading "&filename
  Echo("Finding peaks in "&filename)
  nn=load(filename,"ds")
  dsnum = nn-1
  # -----Hack to get SXD's to run---------
  monct=100000.0
  #monct=IntegGrp(ds[0],1,0,50000)
  Calib(ds[dsnum], calibfile)

  #LoadSCDCalib(ds[dsnum],calibfile,-1,"")
  # find peaks
  peaks1=GetCentroidPeaks(ds[dsnum],monct,num_peaks,min_int,min_time_chan,max_time_chan, RowColKeep)
  
  if first
    peaks=peaks1
  else
    peaks = peaks & peaks1
  endif
  # write out the results
  SetAttr( ds[dsnum],"User Name","George User")
  WritePeaks(outpath&expname&".peaks",peaks1,append)
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

OpenLog(outpath&expname&".log")

return peaks

ExitDialog()
