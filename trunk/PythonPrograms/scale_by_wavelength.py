#--------------------------------------------------------------------
#                             scale_by_wavelength.py
#--------------------------------------------------------------------

#   Script to assign scaling factors according to the wavelength range.

#   A. J. Schultz
#   August, 2011

# Open the instruction file
filename = 'scale_by_wavelength.inp'    # instruction input file
input = open(filename, 'r')

# Read the name of the input hkl file
lineString = input.readline()
lineList = lineString.split()
inputFile = lineList[0]      # the input hkl file

# Read the name of the output hkl file
lineString = input.readline()
lineList = lineString.split()
outputFile = lineList[0]     # the output hkl file

# hkl_output = open(outputFile, 'w')

for i in range(8):    # assume a maximum of 50 detectors
    wlmin = 3.5 + i*0.5
    wlmax = 3.5 + (i+1)*0.5

    hkl_output_wl = 'toho_d_3_wl_' + str(i+1) + '.hkl'
    print hkl_output_wl
    output = open(hkl_output_wl, 'w')
    hkl_input = open(inputFile, 'r')
    
    while True:
        lineString = hkl_input.readline()
        lineList = lineString.split()
        if len(lineList) == 0: break
        
        h = int(lineList[0])
        k = int(lineList[1])
        l = int(lineList[2])
        fsq = float(lineList[3])
        sigfsq = float(lineList[4])
        hstnum = int(lineList[5])
        wl = float(lineList[6])
        tbar = float(lineList[7])
        curhst = int(lineList[8])
        seqnum = int(lineList[9])
        transmission = float(lineList[10])
        dn = int(lineList[11])
        
        if wl > wlmin and wl < wlmax:
            iScale = i + 1
            output.write('%4d%4d%4d%8.2f%8.2f%4d%8.4f%7.4f%7d%7d%7.4f%4d\n' \
            % (h, k, l, fsq, sigfsq, iScale, wl, tbar, curhst, seqnum, transmission, dn))

    hkl_input.close()

    
lastLine = '   0   0   0    0.00    0.00   0  0.0000 0.0000      0      0 0.0000   0'
# print lastLine
hkl_output.write(lastLine)

print 'The End'

    
        
