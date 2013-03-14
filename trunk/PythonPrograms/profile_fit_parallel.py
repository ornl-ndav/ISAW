
# File: profile_fit_parallel.py
#
# This script will run multiple instances of the script ReduceOneSCD_Run.py
# in parallel, using either local processes or a slurm partition.  After
# using the ReduceOneSCD_Run script to find, index and integrate peaks from
# multiple runs, this script merges the integrated peaks files and re-indexes
# them in a consistent way.  If desired, the indexing can also be changed to a
# specified conventional cell.
# Many intermediate files are generated and saved, so all output is written 
# to a specified output_directory.  This output directory must be created
# before running this script, and must be specified in the configuration file.
# The user should first make sure that all parameters are set properly in
# the configuration file for the ReduceOneSCD_Run.py script, and that that 
# script will properly reduce one scd run.  Once a single run can be properly
# reduced, set the additional parameters in the configuration file that specify 
# how the the list of runs will be processed in parallel. 
#
import os
import sys
import threading
import time

if os.path.exists('/SNS/TOPAZ/shared/PythonPrograms/PythonLibrary'):
    sys.path.append('/SNS/TOPAZ/shared/PythonPrograms/PythonLibrary')
    sys.path.append('/SNS/software/ISAW/PythonSources/Lib')
elif os.path.exists("/home/ajschultz/PythonPrograms"):
    sys.path.append("/home/ajschultz/PythonPrograms/PythonLibrary")
    sys.path.append("/home/ajschultz/ISAW/PythonSources/Lib")    
else:
    sys.path.append('C:\ISAW_repo\PythonPrograms\PythonLibrary')
    
import ReduceDictionary

start = time.time()

# -------------------------------------------------------------------------
# ProcessThread is a simple local class.  Each instance of ProcessThread is 
# a thread that starts a command line process to reduce one run.
#
class ProcessThread ( threading.Thread ):
   command = ""

   def setCommand( self, command="" ):
      self.command = command

   def run ( self ):
      print '\n\nSTARTING PROCESS: ' + self.command
      os.system( self.command )
# -------------------------------------------------------------------------

#
# Get the config file name from the command line
#
if (len(sys.argv) < 2):
  print "You MUST give the config file name on the command line"
  exit(0)

config_file_name = sys.argv[1]

#
# Load the parameter names and values from the specified configuration file 
# into a dictionary and set all the required parameters from the dictionary.
#

params_dictionary = ReduceDictionary.LoadDictionary( config_file_name )

output_directory            = params_dictionary[ "output_directory" ]
one_run_script              = params_dictionary[ "one_run_script" ]
max_processes               = int(params_dictionary[ "max_processes" ])
slurm_queue_name            = params_dictionary[ "slurm_queue_name" ] 
# instrument_name             = params_dictionary[ "instrument_name" ]
expname                     = params_dictionary[ "expname" ]
numSteps                    = int( params_dictionary[ "numSteps" ] )
profile_length              = float( params_dictionary[ "profile_length" ] )
profile_function            = int( params_dictionary[ "profile_function" ] )
weights                     = int( params_dictionary[ "weights" ] )
# detcal_fname                = params_dictionary[ "detcal_fname" ]
# events_directory            = params_dictionary[ "events_directory" ] 
# orientation_filename_suffix = params_dictionary[ "orientation_filename_suffix" ] 
# center_type                 = params_dictionary[ "center_type" ] 
# dmin                        = float(params_dictionary[ "dmin" ])
# wlmin                       = float(params_dictionary[ "wlmin" ])
# wlmax                       = float(params_dictionary[ "wlmax" ])
# deltaQ                      = float(params_dictionary[ "deltaQ" ])
# rangeQ                      = float(params_dictionary[ "rangeQ" ])
# radiusQ                     = float(params_dictionary[ "radiusQ" ])
# percentOneBackground        = float(params_dictionary[ "percentOneBackground" ])
run_nums                    = params_dictionary[ "run_nums" ]
python_version              = params_dictionary[ "python_version" ]

#
# Make the list of separate process commands.  If a slurm queue name
# was specified, run the processes using slurm, otherwise just use
# multiple processes on the local machine.
#
list=[]
index = 0
for r_num in run_nums:
    list.append( ProcessThread() )
    cmd = python_version + ' ' + one_run_script + ' ' + config_file_name + ' ' + str(r_num)
    if slurm_queue_name is not None:
        console_file = output_directory + "/" + str(r_num) + "_output.txt"
        cmd =  'srun -p ' + slurm_queue_name + \
               ' --cpus-per-task=3 -J ReduceSCD_Parallel.py -o ' + console_file + ' ' + cmd
    list[index].setCommand( cmd )
    index = index + 1

#
# Now create and start a thread for each command to run the commands in parallel, 
# starting up to max_processes simultaneously.  
#
all_done = False
active_list=[]
while not all_done:
  if ( len(list) > 0 and len(active_list) < max_processes ):
    thread = list[0]
    list.remove(thread)
    active_list.append( thread ) 
    thread.start()
  time.sleep(2)
  for thread in active_list:
    if not thread.isAlive():
      active_list.remove( thread )
  if len(list) == 0 and len(active_list) == 0 :
    all_done = True

print "************ Completed Parallel Execution, Starting to Combine Results ***************"

filename = expname + '.integrate'
output = open(filename, 'w')

# Read and write the first run profile file with header.
r_num = run_nums[0]
filename = expname + '_' + r_num + '.integrate'
input = open(filename, 'r')
file_all_lines = input.read()
output.write(file_all_lines)
input.close()
os.remove(filename)

# Read and write the rest of the runs without the header.
for r_num in run_nums[1:]:
    filename = expname + '_' + r_num + '.integrate'
    input = open(filename, 'r')
    for line in input:
        if line[0] == '0': break
    output.write(line)
    for line in input:
        output.write(line)
    input.close()
    os.remove(filename)

end = time.time()
elapsed = end - start
print '\nTotal elapsed time for parallel processing is %.1f seconds.' % elapsed
print '\nAll processes done!' 


