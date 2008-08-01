#
# @overview This script will load a list of SCD runs into the SCD Reciprocal plane viewer.
#           This version has been adapted to work for the SNS SCD instruments. 
#
# @algorithm Any histogram bin that is above the specified threshold will be mapped to
#            reciprocal space and displayed in a three dimensional viewer.  The viewer
#            supports various operations on the data, including the calculation of an
#            orientation matrix.
#
# @param directory    The fully qualified path to the data files
# @param prefix       The SCD data file prefix, such as "scd0"
# @param runs         The list of run numbers.  Commas separate individual run numbers.  
#                     Colons separate the first and last run numbers in a sequence of run numbers.
# @param suffx        The SCD data file extension, such as ".run"
# @param tof_ds_index The index of the time-of-flight DataSet in the list of DataSets in the data files.
#                     For SNS instruments, this will typically be a list of area detectors, or detector
#                     banks, corresponding to NxData blocks in the NeXus files.
#                     For the IPNS SCD, this is currently "2".  For the LANSCE SCD, this is currently "3".
# @param threshold    The minimum value in a histogram bin that will be considered part of a peak.
#
# @author Dennis Mikkelson
# $Date: 2008-06-01 $
#
$Category = Macros, Instrument Type, TOF_NSCD, NEW_SNS

$directory DataDirectoryString(/usr2/ARCS_SCD/)                          Data Directory Name
$prefix       String(ARCS_)                                              File Name Prefix
$runs         IntList(419)                                               List of run numbers
$suffix       String(.nxs)                                               File Name Suffix

$tof_ds_index IntList(1:114)                                             Indices of TOF DataSets in File

$loadCalib    BooleanEnable([false,1,0])                                 Load Calibration Info
$calibFile    LoadFileString(/usr2/SNS_SCD_TEST/IPNS_SNS.DetCal)         Calibration File (.DetCal) 
$threshold    Float(30)                                                  Threshold for Peak


run_num_list = IntListToVector( runs )
index_list   = IntListToVector( tof_ds_index )
Display "Loading files"  & run_num_list
Display "Loading DataSets" & index_list
data_sets = []

i = 0
for run_num in run_num_list
  file_name = directory & prefix & run_num & suffix
  display "Loading run number " & run_num
  display "From file " & file_name
  Load file_name, "ds"
  for index in index_list
    data_sets[i]=ds[ index ]
    ResampleOnGeometricProgression( data_sets[i], 500, 15000, 2, false )
    if loadCalib
      Calib(data_sets[i], calibFile)
    endif
    i = i + 1
  endfor
endfor

Display_SCD_Reciprocal_Space( data_sets, threshold )
