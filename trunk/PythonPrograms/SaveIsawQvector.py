# SaveIsawQvector

import sys
import os

if os.path.exists("/opt/Mantid/bin"):
    sys.path.append("/opt/mantidnightly/bin")
    #sys.path.append("/opt/Mantid/bin")         # Linux cluster
    #sys.path.append('/opt/mantidunstable/bin')
else:
    sys.path.append("C:/MantidInstall/bin")     # Windows PC

# import mantid
from mantid.simpleapi import *

user_input = open('SaveIsawQvector.inp', 'r')

lineString = user_input.readline()
lineList = lineString.split()
data_directory = lineList[0]

lineString = user_input.readline()
lineList = lineString.split()
output_directory = lineList[0]

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
    full_name = data_directory + run_num + '_event.nxs'

    event_ws = 'TOPAZ_' + run_num

    LoadEventNexus( Filename = full_name, OutputWorkspace = event_ws,
        FilterByTofMin = min_tof, FilterByTofMax = max_tof,
        FilterByTimeStart = start_time, FilterByTimeStop = stop_time )

    outputFile = output_directory + run_num + '_SaveIsawQvector.bin'

    SaveIsawQvector(InputWorkspace = event_ws, 
        Filename = outputFile)
        
    DeleteWorkspace(Workspace = event_ws)

print 'All done!'
