#
# This is a simple-minded bash shell script that forms a temporary file with 
# four lines from four parameters listed on the command line.  This temporary
# file is the used as the input file for invoking the Load_HIPPO_convert_to_d
# script from a command line, using IsawLite.
#
# The parameters are: 
#   $1    The name of the directory containing the data files.
#   $2    The run number.  NOTE: The number of digits in the run number
#         must be coordinated with the "Load_HIPPO_convert_to_d.iss script.
#         In particular, the correct number of "0" characters must be included
#         in the base file name in the .iss script.
#   $3    The bank number.  Only 40, 90 and 150 are currently supported.
#   $4    The frame number.  This will be appended to the base name of the
#         jpeg file that is written.
#
echo $1 >  TempParameterFile.txt
echo $2 >> TempParameterFile.txt
echo $3 >> TempParameterFile.txt
echo $4 >> TempParameterFile.txt
#
#
java IsawGUI.IsawLite  -nogui -i TempParameterFile.txt  Load_HIPPO_convert_to_d
