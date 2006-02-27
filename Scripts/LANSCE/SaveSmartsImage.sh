#
# This is a simple-minded bash shell script that forms a temporary file with 
# five lines from five parameters listed on the command line.  This temporary
# file is the used as the input file for invoking the Load_SMARTS_convert_to_d
# script from a command line, using IsawLite.
#
# The parameters are: 
#   $1    The name of the data file.
#   $2    A flag, true or false, that determines whether or not to load
#         the GSAS calibration information.
#   $3    The number of bins to use if rebinning when converting to "d"
#         The value 0 indicates that all of the original time-of-flight
#         bins are converted to "d" with NO rebinning. 
#   $4    The minimum d value to use, if the data is rebinned
#   $5    The maximum d value to use, if the data is rebinned
#
echo $1 >  TempSmartsParameterFile.txt
echo $2 >> TempSmartsParameterFile.txt
echo $3 >> TempSmartsParameterFile.txt
echo $4 >> TempSmartsParameterFile.txt
echo $5 >> TempSmartsParameterFile.txt
#
#
java IsawGUI.IsawLite  -nogui -i TempSmartsParameterFile.txt  Load_SMARTS_convert_to_d
