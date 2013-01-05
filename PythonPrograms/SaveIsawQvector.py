# test SaveIsawQvector

import sys

try:
    sys.path.append("/opt/mantidnightly/bin")
    #sys.path.append("/opt/Mantid/bin")
    #sys.path.append('/opt/mantidunstable/bin')
except:
    sys.path.append("C:/MantidInstall/bin")     # Windows


# import mantid
from mantid.simpleapi import *

input_run_nums = open('monitorCtsAndAngles.dat', 'r')

min_tof = 2000
max_tof = 16500

start_time = 0.0
stop_time = 1.0e06

while True:

    lineString = input_run_nums.readline()
    lineList = lineString.split()
    if len(lineList) == 0: break
    run_num = lineList[0]
    print run_num
    full_name = '/SNS/TOPAZ/2012_1_12_SCI/data/TOPAZ_' + run_num + '_event.nxs'

    event_ws = 'TOPAZ_' + run_num

    LoadEventNexus( Filename = full_name, OutputWorkspace = event_ws,
        FilterByTofMin=min_tof, FilterByTofMax=max_tof,
        FilterByTimeStart = start_time, FilterByTimeStop = stop_time )

    outputFile = '/SNS/users/ajschultz/profile_fit5/Ni_5678/TOPAZ_' + run_num + '_SaveIsawQvector.bin'

    SaveIsawQvector(InputWorkspace = event_ws, 
        Filename = outputFile)
        
    #DeleteWorkspace(InputWorkspace = event_ws)

print 'All done!'
