# 
# Script to integrate peaks in multiple SCD files. 
# $Date: 2005/08/06 21:57:10 $
#
# First specify any parameters to the script, giving the variable name,
# data type and prompt string.  A dialog box will prompt the user for values
# for these parameters


# File: Wizard/TOF_SCD/Script_new/integrate_multiple_runs.iss
#
# Assumptions:
#  - Data of interest is in histogram 2
#  - The filename are a concatenation of the path, inst, run number and FileExt
#  - There is a "ls[expname]xxxx.mat" file for each xxxx run and an ls[expname].mat

$ path                DataDirectoryString    Raw Data Path
$ outpath             DataDirectoryString    Output Data Path
$ run_numbers         Array                  Run Number
$ expname             String                 Experiment Name
$ centering           Choice(["primitive","a centerped","b centered","c centered", "[f]ace centered","[i] body centered","[r]hombohedral centered" ])      Centering type
$ useCalibFile        BooleanEnable( [false,1,0])  Use the calibration file below
$ calibfile           LoadFileString         SCD Calibration File
$ time_slice_range    String(-1:3)           Time-slice range
$ increase            Integer(1)             Increase slice size by
$ inst                String("SCD0")         Instrument name
$ FileExt             String(".nx.hdf")      FileExtension
$ d_min             float(0.0)             Minimum d-spacing
$PeakAlg           Choice(["MaxIToSigI","Shoe Box","MaxIToSigI-old","TOFINT","EXPERIMENTAL"])  Integ 1 peak algorithm
$Xrange            IntArray(-3,3)            Range of x's around shoebox center
$Yrange            IntArray(-3,3)               Range of y's around shoebox center
$ShowLog           Boolean(false)             Pop Up Integrate.log
$ CATEGORY = operator,Instrument Type, TOF_NSCD
$title=Integrate Peaks
#$inst = "SCD"
Display "Instrument = "&inst
#Display "Calibration File ="&calibfile

# DataSet is number two
#dsnum=1  //Changes depending on when run file was produced

first=true
append=false
OpenLog( outpath&"integrate.log")
for i in run_numbers
  # load data
  filename=path&inst&i&FileExt
  Display "Loading "&filename
  Echo("Integrating peaks in "&filename)
  nn= load(filename,"ds")
  dsnum = nn-1
  # The calibration file "instprm.dat" must be in the outpath directory.
  #LoadSCDCalib(ds[dsnum],outpath&"instprm.dat",-1,"")
  if ( useCalibFile )
    Calib(ds[dsnum], calibfile)
  endif

  # integrate peaks
  #Display(ds[dsnum])
  
  #Gets matrix file "lsxxxx.mat" for each run
  #The "1" means that every peak will be written to the integrate.log file.
  Display "XRange = " & Xrange
  Display "YRange = " & Yrange
  SCDIntegrate_new(ds[dsnum],outpath&expname&".integrate",outpath&"/ls"&expname&i&".mat",centering,time_slice_range,increase,d_min,1,append,PeakAlg,Xrange,Yrange)
  Display "Through integrating run num "&i
  #Integrate
  # write out the results.Done in the integrate routine
  #WritePeaks(outpath&expname&"3.peaks",peaks,append)
  if first
    first=false
    append=true
  endif
endfor
CloseLog()
Echo("--- integrate_multiple_runs is done. ---")

ViewASCII(outpath&expname&".integrate")

if ShowLog
  ViewASCII( outpath&"integrate.log")
else
   Display "The log file is in integrate.log. Use the View menu to open it"
endif
# close the dialog automatically

