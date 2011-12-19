#--------------------------------------------------------------------
#                             setScaleID_SNAP_panel.py
#--------------------------------------------------------------------

#   A. J. Schultz
#   August, 2011

#   Script to set the scale identifier number.
#   This version sets a scale ID for each panel.


# Read the name of the input hkl file
#inputFile = raw_input('File name of input hkl file: ')
inputFile = 'natrolite.hkl'
hkl_input = open(inputFile, 'r')

# Read the name of the output hkl file
#outputFile = raw_input('File name of output hkl file: ')
outputFile = 'natrolite_2.hkl'
hkl_output = open(outputFile, 'w')

# Read the scale factor id number
#iScale = int( raw_input('Scale factor identifier number: ') )
#iScale = 2

jScale = -1
nrun = 0

for lineString in hkl_input:
    # lineString = hkl_input.readline()
    lineList = lineString.split()
    # if len(lineList) == 0: break
    
    h = int(lineString[0:5])
    k = int(lineString[4:8])
    l = int(lineString[8:12])
    if h == 0 and k == 0 and l == 0: break
    fsq = float(lineString[12:20])
    sigfsq = float(lineString[20:28])
    hstnum = int(lineString[28:32])
    wl = float(lineString[32:40])
    tbar = float(lineString[40:47])
    curhst = int(lineString[47:54])
    seqnum = int(lineString[54:61])
    transmission = float(lineString[61:68])
    dn = int(lineString[68:72])
    twoth = float(lineString[72:81])
    dsp = float(lineString[81:90])
    
    if nrun != curhst:
        nrun = curhst
        jScale = jScale + 2
    
    if dn < 10: iScale = jScale 
    else: iScale = jScale + 1
    
    hkl_output.write('%4d%4d%4d%8.2f%8.2f%4d%8.4f%7.4f%7d%7d%7.4f%4d%9.5f%9.4f\n' \
    % (h, k, l, fsq, sigfsq, iScale, wl, tbar, curhst, seqnum, transmission, dn, twoth, dsp))

hkl_input.close()

lastLine = '   0   0   0    0.00    0.00   0  0.0000 0.0000      0      0 0.0000   0  0.00000   0.0000'
hkl_output.write(lastLine)

hkl_output.close()


print 'The End'
