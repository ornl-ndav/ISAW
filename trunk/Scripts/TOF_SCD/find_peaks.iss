# 
# Script to find peaks in a specified SCD dataset. 
# $Date$
#
# First specify any parameters to the script, giving the variable name,
# data type and prompt string.  A dialog box will prompt the user for values
# for these parameters
$category=HiddenOperator
#$ CATEGORY = operator,Instrument Type, TOF_NSCD
$ datadir             DataDirectoryString       Output Path
$ expname             String                 Experiment name
$ calibfile           LoadFileString         SCD Calibration File
$ mon                 MonitorDataSet         Monitor
$ ds                  SampleDataSet          Data Set
$ num_peaks           Integer(50)            Number of Peaks
$ min_int             Integer(3)             Minimum Peak intensity
$ min_time_chan       Integer(0)             Minimum time channel to use
$ max_time_chan       Integer(1000)          Maximum time channel to use
$ append              Boolean(false)         Append to File

#calibfile="/IPNShome/scd/instprm.dat"
# determine the monitor count 
monct=IntegGrp(mon,1,0,50000)
# load in the calibration information from first line of calib file)
LoadSCDCalib(ds,calibfile,-1,"")
# find the peaks
peaks=FindPeaks(ds,monct,num_peaks,min_int,min_time_chan,max_time_chan)
# determine the peak center using a centroid function
peaks=CentroidPeaks(ds,peaks)
# write out the result to a file
WritePeaks(datadir&expname&".peaks",peaks,append)
# write out the experiment file
WriteSCDExp(ds,mon,datadir&expname&".x",1,append)
# pop up a view of the file
ViewASCII(datadir&expname&".peaks")
