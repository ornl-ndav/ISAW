#  This script will reduce a set of raw event files to one integrated file
#  for subsequent processing by ANVRED.  The event files are first loaded,
#  indexed and integrated individually.  Since the raw event files don't have
#  any informaton about the goniometer angles or monitor counts, lists with
#  this information must be provided.  After integrating the peaks from each
#  run (using the Niggli reduced cell for each run) the resulting files are
#  merged into one file.  This merged file is then indexed consistently, using
#  a Niggli reduced cell.  If requested, a second .integrate file with indices
#  corresponding to a conventional cell will also be produced. 
#   This script reduces each run in a separate process, using SLURM, or local
#  processors.  If SLURM is used, the SLURM queue to use must be specified. 
#  Care must be taken with specifying the amount of memory used for each
#  process, and the number of simultaneous processors. If too many simultaneous
#  processes are used the system will get very slow and the total execution
#  time will rise dramatically.  If the number of simultaneous processes 
#  times the memory per process exceeds the available memory, the system
#  will swap, which will also prevent proper execution. Roughly 3,000 Mb
#  of memory is needed per process, if a 768x768x768 histogram is used. 
#  Since each process in turn launches multiple threads, the number of 
#  processes used should be substantially lower than the number of cores
#  in the system, if possible.
$Category=Macros, Instrument Type, TOF_NSCD

$ ExpName           String("Sapphire")                           Experiment Name (Prefix for File Names)
$ DataDir           LoadFile("/usr2/TOPAZ_SAPPHIRE_JUNE_2012/")  Directory With Raw Event Files 
$ OutputDir         SaveFile("/home/dennis/TEST_SCRIPT/")        Output Directory 
$ DetCal            LoadFile("Default")                          DetCal File Name 
$ Instrument        String("TOPAZ")                              Instrument Name (Raw Event File Prefix)

$ RunNums           IntList("5637:5644")                         List of Run Numbers
$ Phi               Array(-.02,-.02,-.02,-.02,-.02,-.02,45,60)   List of Phi Angles for Each Run
$ Chi               Array(135,135,135,135,135,135,135,135)       List of Chi Angles for Each Run
$ Omega             Array(0,60,120,180,-120,-60,0,20 )           List of Omega Angles for Each Run
$ Omega_corr        Float(-2.473)                                Correction to add to each omega angle
$ Monct             Array(5502329,5491718,5489163,5496778,5493993,5491470,5496153,2292919) List of Monitor Counts for Each Run
$ HistSize          Integer(768)                                 Histogram Subdivisions in One Direction

$ MaxQ              Float(25)                                    Maximum Q to Load
$ LamdaPower        Float(2.4)                                   Power On Lamda for Peak Search

$ NPeaks            Integer(500)                                 Number of Peaks to Find
$ Threshold         Float(5)                                     Threshold to Count as Peak

$ MinD              Float(3)                                     Min d (Less than a, b or c)
$ MaxD              Float(7)                                     Max d (More than a, b or c)
$ Tolerance         Float(0.12)                                  Tolerance for Indexing
$ ConvertToConv     BooleanEnable(false,3,0)                     Convert to Conventional Cell
$ CellType          ChoiceList(["Cubic","Rhombohedral","Tetragonal","Orthorhombic","Monoclinic","Hexagonal","Triclinic"])  Conventional Cell Type 
$ Centering         ChoiceList(["F Centered", "I Centered", "C Centered", "P Centered", "R Centered"])                     Conventional Cell Centering
$ RemoveUnindexed   Boolean(true)                                Remove Unindexed Peaks

$ IntegMethod       ChoiceList(["DET_X_Y_Q","SPHERE"])           Integration Method 
$ IntegRadius       Float(0.18)                                  Sphere Radius (for Sphere Integration) 
$ PredictPeaks      Boolean(false)                               Integrate ALL Predicted Peak Positions

$ DetCorr           BooleanEnable(false,1,0)                                      Apply Per-Detector Weight Factors
$ DetCorrFile       LoadFile("/usr2/TOPAZ_SAPPHIRE_JUNE_2012/WeightByGSAS.dat")   Per-Detector Weight File              

$ mem_per_process             Integer(8000)                      Megabytes per process
$ use_slurm                   BooleanEnable(false,1,0)           Use Slurm Instead of Local Processes
$ queue                       String("mikkcomp2")                SLURM queue name
$ max_simultaneous_processes  Integer(8)                         Max number of processes to use

#
# First build the parmeters needed for processing each run
#
RunNumList = IntListToVector( RunNums )
n_runs = ArrayLength( RunNumList )

for i in [0:n_runs-1]
  Omega[i] = Omega[i] + Omega_corr
endfor

base_file = DataDir & "/" & Instrument & "_"
suffix = "_neutron_event.dat"
for i in RunNumList
  runfile[i]= base_file &i &suffix
endfor

base_file = OutputDir & "/" & ExpName & "_" 
for i in RunNumList
  integ_file[i] = base_file & i & ".integrate"
  mat_file[i] = base_file & i & ".mat"
endfor

#
# Form a command line for each run that will be processed
#
for i in [0:n_runs-1]
  commands[i]="ReduceSCD3 " & Instrument & " " & DetCal & " " & HistSize & " " & MaxQ & " " & LamdaPower & " " & runfile[i]
  commands[i]=commands[i] & " " & NPeaks & " " & Threshold & " " &  RunNumList[i] & " " & Phi[i] & " " & Chi[i] & " " & Omega[i] & " " & Monct[i] 
  commands[i]=commands[i] & " " & MinD & " " & MaxD & " " & Tolerance & " " & mat_file[i] 
  commands[i]=commands[i] & " " & IntegMethod & " " & IntegRadius & " " & PredictPeaks & " " & integ_file[i]
endfor

#
# Call srunOps to use SLURM to run the commands in parallel, either using SLURM or Local Processes
# NOTE: max_time is essentially infinite, since this system cannot kill the processes after a specified
# amount of time has elapsed anyway.  
#
max_time = 1000000
if use_slurm
  srunOps( queue, max_simultaneous_processes, max_time, mem_per_process, commands )
else
  srunOps( "", max_simultaneous_processes, max_time, mem_per_process, commands )
endif

#
# Now pull the results together into one Merged file
#
MergedFile = OutputDir & "/" & ExpName & "_Merged.integrate"
MergeRuns( OutputDir, ExpName & "_", ".integrate", RunNums, MergedFile )

#
# Now do the per detector weight corrections, if requested
#
if DetCorr
  WeightPeaksByDetector( MergedFile, DetCorrFile, MergedFile )

#
# Now index the merged file consistently and convert to the conventional cell if requested
#
NiggliFile = ExpName & "_Niggli.integrate"
IndexMultipleRuns( MergedFile, true, MinD, MaxD, Tolerance, OutputDir, NiggliFile, ExpName ) 
if ConvertToConv
  NiggliFile = OutputDir & "/" & NiggliFile
  ConventionalFile =OutputDir & "/" &  ExpName & "_" & CellType & ".integrate"
  ConvertToConventionalCell( NiggliFile, CellType, Centering, RemoveUnindexed, ConventionalFile )
endif
