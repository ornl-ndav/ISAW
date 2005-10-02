#
# This is a simple-minded bash shell script that forms a temporary file with 
# four lines from four parameters listed on the command line.  This temporary
# file is the used as the input file for invoking the Load_HIPPO_convert_to_d
# script from a command line, using IsawLite.
#
echo $1 >  TempParameterFile.txt
echo $2 >> TempParameterFile.txt
echo $3 >> TempParameterFile.txt
echo $4 >> TempParameterFile.txt
#
#
java IsawGUI.IsawLite  -nogui -i TempParameterFile.txt  Load_HIPPO_convert_to_d
