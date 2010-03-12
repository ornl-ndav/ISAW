#--------------------------------------------------------
#               function readrefl_header
#--------------------------------------------------------
# Function to read the header of a peaks or integrate
# file and return the detector calibartion parameters.
#--------------------------------------------------------
# Jython version:
#   A. J. Schultz,   November, 2009
#--------------------------------------------------------
#
# Comments from Fortran subroutine:
# !!!	This subroutine will read the first lines of a peaks or
# !!!	integrate file with the SNS format.
# !!!	A. J. Schultz	June, 2008
#
# !	The first variable of each record is ISENT.
#
# !	Linux version, January 2002, A.Schultz
#
# !	ISENT = 0 --> variable name list for describing the histogram
# !	      = 1 --> variable values of above list
# !	      = 2 --> variable name list for a reflection
# !	      = 3 --> variable values of above list
# !	      = 4 --> variable name list for parameters for detectors
# !	      = 5 --> variable values of parameters for detectors
# !	      = 6 --> variable name list: L1    T0_SHIFT
# !	      = 7 --> variable values: L1    T0_SHIFT
#

from jarray import *

def readrefl_header(input):
    "Returns the detector calibration info from the header of a peaks or integrate file."
    
# input is the input peaks or integrate file which is already open.    
    lineString = input.readline()           # read first header line from integrate file
    print 'Reflection file header: ' + lineString
    
    nod = 0     # number of detectors
    
# define arrays for up to 100 detectors
    detNum = zeros(100, 'i')
    nRows = zeros(100, 'i')
    nCols = zeros(100, 'i')
    width = zeros(100, 'f')
    height = zeros(100, 'f')
    depth = zeros(100, 'f')
    detD = zeros(100, 'f')
    centerX = zeros(100, 'f')
    centerY = zeros(100, 'f')
    centerZ = zeros(100, 'f')
    baseX = zeros(100, 'f')
    baseY = zeros(100, 'f')
    baseZ = zeros(100, 'f')
    upX = zeros(100, 'f')
    upY = zeros(100, 'f')
    upZ = zeros(100, 'f')
    
# begin reading the peaks or integrate file
    while True:
        lineString = input.readline()
        lineList = lineString.split()
        j = len(lineList)
        if j == 0: break                    # len = 0 if EOf
        
        formatFlag = int(lineList[0])       # test for line type
        if formatFlag == 0: break           # finished header, start of peaks
        
        if formatFlag == 7:
            L1 = float(lineList[1])         # L1 in centimeters
            t0_shift = float(lineList[2])   # t-zero offset in microseconds

        elif formatFlag == 5:
            nod = nod + 1
            i = nod - 1                     # array index starts at 0
            detNum[i] = int(lineList[1])    # store parameters in arrays
            nRows[i] = int(lineList[2])
            nCols[i] = int(lineList[3])
            width[i] = float(lineList[4])
            height[i] = float(lineList[5])
            depth[i] = float(lineList[6])
            detD[i] = float(lineList[7])
            centerX[i] = float(lineList[8])
            centerY[i] = float(lineList[9])
            centerZ[i] = float(lineList[10])
            baseX[i] = float(lineList[11])
            baseY[i] = float(lineList[12])
            baseZ[i] = float(lineList[13])
            upX[i] = float(lineList[14])
            upY[i] = float(lineList[15])
            upZ[i] = float(lineList[16])

# finished
    # return L1, t0_shift, nod,       \ i = 0, 1, 2
        # detNum, nRows, nCols,       \ i = 3, 4, 5
        # width, height, depth, detD, \ i = 6, 7, 8, 9
        # centerX, centerY, centerZ,  \ i = 10, 11, 12
        # baseX, baseY, baseZ,        \ i = 13, 14, 15
        # upX, upY, upZ                 i = 16, 17, 18
        
    return L1, t0_shift, nod,       \
        detNum, nRows, nCols,       \
        width, height, depth, detD, \
        centerX, centerY, centerZ,  \
        baseX, baseY, baseZ,        \
        upX, upY, upZ
