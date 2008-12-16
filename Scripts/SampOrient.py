
from  DataSetTools.writer import *
# This script adds or changes the sample orientation information in SNS SCD 
# Nexus files.  Just set up the the data in the input section below.
#------------------------ Inputs ----------------------------------
filename ="c:/ISAW/SampleRuns/SNS/Snap/QuartzRunsFixed/SNAP_237.nxs"
phi=10.0
chi=0.0
omega=270.0
#-----------------------End Inputs------------------------------------

FixNexusFile.AddSampleOrientation(filename,"entry","sample",phi,chi,omega)
print "Success"