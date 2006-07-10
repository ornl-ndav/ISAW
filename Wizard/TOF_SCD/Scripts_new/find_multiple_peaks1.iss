# 
# Script to find peaks in multiple files with Single Crystal data. 
# $Date: 2006/06/09 $
#
# File: Wizard/TOF_SCD/Scripts_new/find_multiple_peaks.iss
#
# First specify any parameters to the script, giving the variable name,
# data type and prompt string.  A dialog box will prompt the user for values
# for these parameters
#
# Assumptions:
#  - Data of interest is in the last histogram 
#  - The filenames are concatenations of the path, inst, run_numbers, then
#    the DatFileExtension. Make sure that the /,\, and . are there
#  - The Generic Calib operator is used to read the calibration file

$ CATEGORY = operator,Instrument Type, TOF_NSCD
$ Title=Find Peaks

#---------------------------Parameters---------------------
$ path                DataDirectoryString    Raw Data Path
$ outpath             DataDirectoryString    Output Data Path
$ run_numbers         Array                  Run Numbers
$ expname             String                 Experiment Name
$ num_peaks           Integer(50)            Maximum Number of Peaks
$ min_int             Integer(3)             Minimum Peak intensity
$ min_time_chan       Integer(0)             Minimum Time Channel
$ max_time_chan       Integer(1000)          Maximum Time Channel 
$ useCalibFile        BooleanEnable( [false,1,0])  Use the calibration file below
$ calibfile           LoadFileString         Calibration File
$ RowColKeep          IntList(1:128)         Pixel Rows and Columns to Keep 
$ inst                String("SCD_E000005_R000")  Instrument
$ DatFileExtension    String(".nx.hdf")         Extension on Data Files
#------------------------ Code ---------------------

#calibfile = "/IPNShome/scd/instprm.dat"
Display "Instrument="&inst
if  useCalibFile
  Display "Calibration File ="&calibfile
endif
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
  if useCalibFile
    Calib(ds[dsnum], calibfile)
  endif

  #LoadSCDCalib(ds[dsnum],calibfile,-1,"")
  # find peaks
  peaks1=GetCentroidPeaks1(ds[dsnum],monct,num_peaks,min_int,min_time_chan,max_time_chan, RowColKeep)
  
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

return peaks

