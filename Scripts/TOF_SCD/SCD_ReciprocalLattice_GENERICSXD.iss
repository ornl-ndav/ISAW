#
# @overview This script will load a list of SCD runs into the SCD Reciprocal plane viewer.
#           Any SCD instrument whose data can be read by ISAW should work.  Currently that
#           is the IPNS SCD and the LANSCE SCD. 
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
#                     For the IPNS SCD, this is currently "2".  For the LANSCE SCD, this is currently "3".
# @param threshold    The minimum value in a histogram bin that will be considered part of a peak.
#
# @author Dennis Mikkelson
# $Date: 2006/02/14 04:33:09 $
#
#$Category = Macros, Instrument Type, TOF_NSCD
$category=HiddenOperator
$directory DataDirectoryString(c:/ISAW/SampleRuns/ISIS_SXD/)  Data Directory Name
$prefix       String(SXD)                                   File Name Prefix
$runs         IntList(17210)                                       List of run numbers
$suffix       String(.RAW)                                            File Name Suffix
$tof_ds_index Integer(1)                                                 Index of TOF DataSet in File
$threshold    Float(30)                                                  Threshold for Peak

run_num_list = IntListToVector( runs )
data_sets = []

i = 0
DSS=[]
for run_num in run_num_list
  file_name = directory & prefix & run_num & suffix
  display "Loading run number " & run_num
  display "From file " & file_name
  data_sets[i]=OneDS( file_name, tof_ds_index,"2:12289")
 
  i = i + 1
endfor

Display_SCD_Reciprocal_Space( data_sets, threshold )
