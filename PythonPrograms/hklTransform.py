#--------------------------------------------------------------------
#                             hklTransform.py
#--------------------------------------------------------------------

#   A. J. Schultz
#   November, 2012

#   Script to transform the hkl's in hkl file.

#######################
inputFile = 'CuT_monoc.hkl'   # change to the name of your input file
#######################

hkl_input = open(inputFile, 'r')

#######################
outputFile = 'CuT74D_P21a.hkl'   # change to the name of your output file
#######################

hkl_output = open(outputFile, 'w')


for lineString in hkl_input:
    lineList = lineString.split()
    
    h = int(lineString[0:4])
    k = int(lineString[4:8])
    l = int(lineString[8:12])
    if h == 0 and k == 0 and l == 0: break
    fsq = float(lineString[12:20])
    sigfsq = float(lineString[20:28])
    iScale = int(lineString[28:32])
    wl = float(lineString[32:40])
    tbar = float(lineString[40:48])
    curhst = int(lineString[48:55])
    seqnum = int(lineString[55:62])
    transmission = float(lineString[62:69])
    dn = int(lineString[69:73])
    twoth = float(lineString[73:82])
    dsp = float(lineString[82:91])
    
    #######################
    # transform the hkl's
    htemp = h
    h = l
    k = -k
    l = htemp
    #######################

    
    hkl_output.write('%4d%4d%4d%8.2f%8.2f%4d%8.4f%7.4f%7d%7d%7.4f%4d%9.5f%9.4f\n' \
    % (h, k, l, fsq, sigfsq, iScale, wl, tbar, curhst, seqnum, transmission, dn, twoth, dsp))

hkl_input.close()

lastLine = '   0   0   0    0.00    0.00   0  0.0000 0.0000      0      0 0.0000   0  0.00000   0.0000'
hkl_output.write(lastLine)

hkl_output.close()


print 'The End'

    
        
