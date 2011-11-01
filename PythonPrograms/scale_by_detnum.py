#--------------------------------------------------------------------
#                             scale_by_detnum.py
#--------------------------------------------------------------------

#   Script to assign scaling factors according to the detector number.

#   A. J. Schultz
#   January, 2011

# Open the instruction file
filename = 'scale_by_detnum.inp'    # instruction input file
input = open(filename, 'r')

# Read the name of the input hkl file
lineString = input.readline()
lineList = lineString.split()
inputFile = lineList[0]      # the input hkl file

# Read the name of the output hkl file
lineString = input.readline()
lineList = lineString.split()
outputFile = lineList[0]     # the output hkl file

hkl_output = open(outputFile, 'w')

detnum = [17, 18, 26, 27, 36, 37, 38, 39, 46, 47, 48, 49, 57, 58]

for i in range(14):

    hkl_input = open(inputFile, 'r')
    
    while True:
        lineString = hkl_input.readline()
        lineList = lineString.split()
        if len(lineList) == 0: break
        
        h = int(lineString[0:5])
        k = int(lineString[4:8])
        l = int(lineString[8:12])
        fsq = float(lineString[12:20])
        sigfsq = float(lineString[20:28])
        hstnum = int(lineString[28:32])
        wl = float(lineString[32:40])
        tbar = float(lineString[40:47])
        curhst = int(lineString[47:54])
        seqnum = int(lineString[54:61])
        transmission = float(lineString[61:68])
        dn = int(lineString[68:72])
        
        if dn == detnum[i]:
            iScale = i + 1
            hkl_output.write('%4d%4d%4d%8.2f%8.2f%4d%8.4f%7.4f%7d%7d%7.4f%4d\n' \
            % (h, k, l, fsq, sigfsq, iScale, wl, tbar, curhst, seqnum, transmission, dn))

    hkl_input.close()

    
lastLine = '   0   0   0    0.00    0.00   0  0.0000 0.0000      0      0 0.0000   0'
hkl_output.write(lastLine)

print 'The End'

    
        
