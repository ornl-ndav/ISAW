# 
# Script to find peaks in a specified SCD dataset. 
# $Date$
#
# First specify any parameters to the script, giving the variable name,
# data type and prompt string.  A dialog box will prompt the user for values
# for these parameters

$ datadir             DataDirectoryString       Output Path
$ expname             String                 Experiment name
# $ filename            SaveFileString         Save File (scd#####.peaks)
# $ expfile             SaveFileString         Experiment File
$ calibfile           LoadFileString         SCD Calibration File
$ mon                 MonitorDataSet         Monitor
$ ds                  SampleDataSet          Data Set
$ num_peaks           Integer(50)            Number of Peaks
$ min_int             Integer(3)             Minimum Peak intensity
$ append              Boolean(false)         Append to File

#calibfile="/IPNShome/scd/instprm.dat"
# determine the monitor count 
monct=IntegGrp(mon,1,0,50000)
# load in the calibration information from first line of calib file)
LoadSCDCalib(ds,calibfile,-1,"")
# find the peaks
peaks=FindPeaks(ds,monct,num_peaks,min_int)
# determine the peak center using a centroid function
peaks=CentroidPeaks(ds,peaks)
# write out the result to a file
WritePeaks(datadir&expname&".peaks",peaks,append)
# write out the experiment file
WriteSCDExp(ds,mon,datadir&expname&".x",1,append)
# pop up a view of the file
ViewASCII(datadir&expname&".peaks")
