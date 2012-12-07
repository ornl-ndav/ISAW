# File: ReduceOneSCD_Run.py
#
# This script will reduce one SCD run.  The configuration file name and
# the run to be processed must be specified as the first two command line 
# parameters.  This script is intended to be run in parallel using the 
# ReduceSCD_Parallel.py script, after this script and configuration file has 
# been tested to work properly for one run. This script will load, find peaks,
# index and integrate either found or predicted peaks for the specified run.  
# Either sphere integration or the Mantid PeakIntegration algorithms are 
# currently supported, but it may be updated to support other integration 
# methods.  Users should make a directory to hold the output of this script, 
# and must specify that output directory in the configuration file that 
# provides the parameters to this script.
#
# NOTE: All of the parameters that the user must specify are listed with 
# instructive comments in the sample configuration file: ReduceSCD.config.
#
import os
import sys
import shutil
import ReduceDictionary

from time import clock
start = clock()

# sys.path.append("/opt/mantidnightly/bin")
#sys.path.append("/opt/Mantid/bin")
sys.path.append("C:/MantidInstall/bin")

from mantid.simpleapi import *
from MantidFramework import mtd
mtd.initialise()
# from mantidsimple import *



#
# Get the config file name and the run number to process from the command line
#
if (len(sys.argv) < 3):
  print "You MUST give the config file name and run number on the command line"
  exit(0)

config_file_name = sys.argv[1]
run              = sys.argv[2]

#
# Load the parameter names and values from the specified configuration file 
# into a dictionary and set all the required parameters from the dictionary.
#
params_dictionary = ReduceDictionary.LoadDictionary( config_file_name )

instrument_name           = params_dictionary[ "instrument_name" ]
data_directory            = params_dictionary[ "data_directory" ]
output_directory          = params_dictionary[ "output_directory" ]
min_tof                   = params_dictionary[ "min_tof" ] 
max_tof                   = params_dictionary[ "max_tof" ] 
monitor_index             = params_dictionary[ "monitor_index" ] 
cell_type                 = params_dictionary[ "cell_type" ] 
centering                 = params_dictionary[ "centering" ]
num_peaks_to_find         = params_dictionary[ "num_peaks_to_find" ]
min_d                     = params_dictionary[ "min_d" ]
max_d                     = params_dictionary[ "max_d" ]
tolerance                 = params_dictionary[ "tolerance" ]
apply_transform_to_hkl    = params_dictionary[ "apply_transform_to_hkl" ]
HKL_Transform_matrix      = params_dictionary[ "HKL_Transform_matrix" ]

#
# Get the fully qualified input run file name, either from a specified data 
# directory or from findnexus
#
if data_directory is not None:
  full_name = data_directory + "/" + instrument_name + "_" + run + "_event.nxs"
else:
  temp_buffer = os.popen("findnexus --event -i "+instrument_name+" "+str(run) )
  full_name = temp_buffer.readline()
  full_name=full_name.strip()
  if not full_name.endswith('nxs'):
    print "Exiting since the data_directory was not specified and"
    print "findnexus failed for event NeXus file: " + instrument_name + " " + str(run)
    exit(0)

print "\nProcessing File: " + full_name + " ......\n"

#
# Name the temporary workspaces that will be used 
# 
event_ws              = instrument_name + "_" + run
MDEW_ws               = event_ws + "_MDEW"
weighted_MDEW         = event_ws + "_WMDEW"
peaks_ws              = event_ws + '_peaks'
monitor_ws            = "monitor_" + run
integrated_monitor_ws = "integrated_monitor_" + run

#
# Name the files to write for this run
#
run_niggli_matrix_file = output_directory + "/" + run + "_Niggli.mat"
run_niggli_integrate_file = output_directory + "/" + run + "_Niggli.integrate"

#
# Load the run data and find the total monitor counts
#
event_ws = LoadEventNexus( Filename=full_name, 
                FilterByTofMin=min_tof, FilterByTofMax=max_tof )

                
LoadNexusMonitors( Filename=full_name, OutputWorkspace=monitor_ws )
Integration( InputWorkspace=monitor_ws, OutputWorkspace=integrated_monitor_ws, 
             RangeLower=min_tof, RangeUpper=max_tof, 
             StartWorkspaceIndex=monitor_index, EndWorkspaceIndex=monitor_index )

monitor_count = mtd[integrated_monitor_ws].dataY(0)[0]
print "\n", run, " has calculated monitor count", monitor_count, "\n"

omega_deg = event_ws.run()['omega'].value[0]
chi_deg = event_ws.run()['chi'].value[0]
phi_deg = event_ws.run()['phi'].value[0]
print 'omega = %f deg' % omega_deg
print 'chi = %f deg' % chi_deg
print 'phi = %f deg\n' % phi_deg

monitorFile = open('monitorCtsAndAngles.dat', 'a')
monitorFile.write('%s   %d   %f   %f   %f\n' % (run, monitor_count, omega_deg, chi_deg, phi_deg))
monitorFile.close()

#
# Make MD workspace using Lorentz correction, to find peaks 
#
ConvertToDiffractionMDWorkspace(
                    InputWorkspace=event_ws, OutputWorkspace=weighted_MDEW, 
   	            LorentzCorrection='1', OutputDimensions='Q (sample frame)', 
                    SplitInto='2', SplitThreshold='50',MaxRecursionDepth='11' )
#
# Find the requested number of peaks.  Once the peaks are found, we no longer
# need the weighted MD event workspace, so delete it.
#
distance_threshold = 0.9 * 6.28 / float(max_d)
FindPeaksMD( InputWorkspace=weighted_MDEW, MaxPeaks=num_peaks_to_find, 
             OutputWorkspace=peaks_ws, 
             PeakDistanceThreshold=distance_threshold )
mtd.deleteWorkspace( weighted_MDEW )

#
# Find a Niggli UB matrix that indexes the peaks in this run
#
FindUBUsingFFT( PeaksWorkspace=peaks_ws, MinD=min_d, MaxD=max_d, Tolerance=tolerance )
IndexPeaks( PeaksWorkspace=peaks_ws, Tolerance=tolerance )

#
# Save UB and peaks file, so if something goes wrong latter, we can at least 
# see these partial results
#
SaveIsawUB( InputWorkspace=peaks_ws,Filename=run_niggli_matrix_file )

#
# If requested, also switch to the specified conventional cell and save the
# corresponding matrix and integrate file
#
if (not cell_type is None) and (not centering is None) :
  run_conventional_matrix_file = output_directory + "/" + run + "_" +    \
                                 cell_type + "_" + centering + ".mat"
  SelectCellOfType( PeaksWorkspace=peaks_ws, 
                    CellType=cell_type, Centering=centering, 
                    Apply=True, Tolerance=tolerance )
  SaveIsawUB( InputWorkspace=peaks_ws, Filename=run_conventional_matrix_file )
  
if apply_transform_to_hkl is True:
  TransformHKL( PeaksWorkspace = peaks_ws,
                Tolerance = tolerance,
                HKL_Transform = HKL_Transform_matrix )
  SaveIsawUB( InputWorkspace=peaks_ws, Filename=run_conventional_matrix_file )

  
end = clock()
elapsed = end - start
print '\nElapsed time is %f seconds.' % elapsed
