#--------------------------------------------------------------------
#                             fcf_2_hkl.py
#--------------------------------------------------------------------

#   A. J. Schultz
#   August, 2011

#   Script to rewrite a fcf file (LIST 4) to an hkl file (HKLF 4).
#   First run SHELX with MERG 0, HKLF 2 and LIST 4.

print ''
print 'Script to rewrite a fcf file (LIST 4) to an hkl file (HKLF 4).'
print 'First run SHELX with MERG 0, HKLF 2 and LIST 4.'
print ''

# Read the name of the input fcf file
inputFile = raw_input('File name of input fcf file: ')
hkl_input = open(inputFile, 'r')

# Read the name of the output hkl file
outputFile = raw_input('File name of output hkl file: ')
hkl_output = open(outputFile, 'w')

while True:
    lineString = hkl_input.readline()
    lineList = lineString.split()
    if len( lineList ) == 0: continue
    if lineList[0] == '_refln_observed_status': break

while True:
    lineString = hkl_input.readline()
    lineList = lineString.split()
    if len(lineList) == 0: break
    
    h = int( lineList[0] )
    k = int( lineList[1] )
    l = int( lineList[2] )
    fsq_calc = float( lineList[3] )
    fsq_obs = float( lineList[4] )
    sigfsq = float( lineList[5] )
    iScale = 1
    
    hkl_output.write('%4d%4d%4d%8.2f%8.2f%4d\n' \
    % (h, k, l, fsq_obs, sigfsq, iScale))

hkl_input.close()

lastLine = '   0   0   0    0.00    0.00   0  0.0000 0.0000      0      0 0.0000   0'
hkl_output.write(lastLine)
hkl_output.close()


print 'The End'

    
        
