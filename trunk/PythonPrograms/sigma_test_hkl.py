#--------------------------------------------------------------------
#                             sigma_test.py
#--------------------------------------------------------------------

#   Script to write hkl file for peaks above sigma level.

#   A. J. Schultz
#   August, 2011


# Read the name of the input hkl file
inputFile = 'TOHO_all_EV.hkl'      # the input hkl file
hkl_input = open( inputFile, 'r' )

# Read the name of the output hkl file
outputFile = 'TOHO_10sig_EV.hkl'     # the output hkl file
hkl_output = open( outputFile, 'w' )

    
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
    
    if fsq > (10.0*sigfsq):
        hkl_output.write('%4d%4d%4d%8.2f%8.2f%4d%8.4f%7.4f%7d%7d%7.4f%4d\n' \
        % (h, k, l, fsq, sigfsq, hstnum, wl, tbar, curhst, seqnum, transmission, dn))

hkl_input.close()

    
lastLine = '   0   0   0    0.00    0.00   0  0.0000 0.0000      0      0 0.0000   0'
# print lastLine
hkl_output.write(lastLine)

print 'The End'

    
        
