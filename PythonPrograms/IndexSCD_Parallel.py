
# File: ReduceSCD_Parallel.py
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
elif os.path.exists("/home/ajschultz/PythonPrograms"):
    sys.path.append("/home/ajschultz/PythonPrograms/PythonLibrary")
else:
    sys.path.append('C:\ISAW_repo\PythonPrograms\PythonLibrary')

import ReduceDictionary

start = time.time()

if os.path.exists("/opt/Mantid/bin"):
    sys.path.append("/opt/Mantid/bin")
    # sys.path.append("/opt/mantidnightly/bin")
else:
    sys.path.append("C:/MantidInstall/bin")

from mantid.simpleapi import *
from MantidFramework import mtd
mtd.initialise()

# -------------------------------------------------------------------------
# ProcessThread is a simple local class.  Each instance of ProcessThread is 
# a thread that starts a command line process to reduce one run.
#
class ProcessThread ( threading.Thread ):
   command = ""

   def setCommand( self, command="" ):
      self.command = command

   def run ( self ):
      print 'STARTING PROCESS: ' + self.command
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

exp_name              = params_dictionary[ "exp_name" ]
output_directory      = params_dictionary[ "output_directory" ]
reduce_one_run_script = params_dictionary[ "reduce_one_run_script" ]
slurm_queue_name      = params_dictionary[ "slurm_queue_name" ] 
max_processes         = int(params_dictionary[ "max_processes" ])
min_d                 = params_dictionary[ "min_d" ]
max_d                 = params_dictionary[ "max_d" ]
tolerance             = params_dictionary[ "tolerance" ]
cell_type             = params_dictionary[ "cell_type" ] 
centering             = params_dictionary[ "centering" ]
run_nums              = params_dictionary[ "run_nums" ]

#
# Make the list of separate process commands.  If a slurm queue name
# was specified, run the processes using slurm, otherwise just use
# multiple processes on the local machine.
#
list=[]
index = 0
for r_num in run_nums:
  list.append( ProcessThread() )
  cmd = 'python ' + reduce_one_run_script + ' ' + config_file_name + ' ' + str(r_num)
  if slurm_queue_name is not None:
    console_file = output_directory + "/" + str(r_num) + "_output.txt"
    cmd =  'srun -p ' + slurm_queue_name + \
           ' --cpus-per-task=3 -J ReduceSCD_Parallel.py -o ' + console_file + ' ' + cmd
  list[index].setCommand( cmd )
  index = index + 1

#
# Delete the monitorCtsPerRun.dat file if it exists.
#
try:
  os.remove(output_directory + '\monitorCtsPerRun.dat')
except:
  pass

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


end = time.time()
elapsed = end - start
print '\nElapsed time is %f seconds.' % elapsed
