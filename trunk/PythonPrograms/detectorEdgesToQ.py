#  detectorEdgesToQ.py
""" Calculate the outline of the detectors in Q space.
A. J. Schultz
March, 2012 """

import numpy
import math

def calc_qvec(col, row, tof, L1, nRows, nCols, height, width, center, base, up):
    """ Calculate q vector from row,col channels and tof."""
    
    colcm = (col - ((0.5 * nCols) - 0.5)) * width / nCols
    rowcm = (row - ((0.5 * nRows) - 0.5)) * height / nRows
    
    realVec = numpy.zeros(3)
    realVec = center + (colcm * base) + (rowcm * up)
    
    L2 = math.sqrt(numpy.dot(realVec, realVec))
    # print 'L2 = ', L2
    wl = 0.39549 * tof / (L1 + L2)
    # print 'wl = ', wl
    
    factor = (1.0 / wl) / L2

    recipVec = numpy.zeros(3)
    recipVec = factor * realVec
    qVec = [recipVec[0], recipVec[1], (recipVec[2] - (1.0 / wl))]
    qVec = [qVec[0]*2.0*math.pi, qVec[1]*2.0*math.pi, qVec[2]*2.0*math.pi]
    
    return qVec


# ------------ Begin -------------

# Open and read the user input file
user_input = open('detectorEdgesToQ.inp', 'r')
user_param = []
while True:
    lineString = user_input.readline()
    lineList = lineString.split()
    if len(lineList) == 0: break
    user_param.append(lineList[0])
detcal_fname = user_param[0] # detector calibration file name
tof = float(user_param[1])   # time-of-flight in microseconds

input = open(detcal_fname, 'r')

det_outline = []    # array of points that outline the detector in q space

nod = 0     # number of detectors

output = open('det_outline.dat', 'w')

# begin reading the calibration file
while True:
    lineString = input.readline()
    lineList = lineString.split()
    j = len(lineList)
    if j == 0: break                    # len = 0 if EOF
            
    if lineList[0] == '#': continue
    formatFlag = int(lineList[0])       # test for line type
    
    if formatFlag == 7:
        L1 = float(lineList[1])         # L1 in centimeters
        t0_shift = float(lineList[2])   # t-zero offset in microseconds

    elif formatFlag == 5:
        nod += 1
        detNum = int(lineList[1])
        nRows = int(lineList[2])
        nCols = int(lineList[3])
        width = float(lineList[4])
        height = float(lineList[5])
        depth = float(lineList[6])
        detD = float(lineList[7])
        centerX = float(lineList[8])
        centerY = float(lineList[9])
        centerZ = float(lineList[10])
        baseX = float(lineList[11])
        baseY = float(lineList[12])
        baseZ = float(lineList[13])
        upX = float(lineList[14])
        upY = float(lineList[15])
        upZ = float(lineList[16])
        
        center = numpy.array([centerX, centerY, centerZ])
        base = numpy.array([baseX, baseY, baseZ])
        up = numpy.array([upX, upY, upZ])
        
        output.write('Detector Number %d\n' % nod)
        
        for i in range(nCols):
            col = i + 1
            row = 1
            qVec = calc_qvec(col, row, tof, L1, nRows, nCols, height, width, 
                             center, base, up)
            det_outline.append(qVec)
            output.write('%f %f %f\n' % (qVec[0], qVec[1], qVec[2]))
            
        for i in range(nRows):
            col = nCols
            row = i + 1
            qVec = calc_qvec(col, row, tof, L1, nRows, nCols, height, width, 
                             center, base, up)
            det_outline.append(qVec)
            output.write('%f %f %f\n' % (qVec[0], qVec[1], qVec[2]))
            
        for i in range(nCols):
            col = nCols - i
            row = nRows
            qVec = calc_qvec(col, row, tof, L1, nRows, nCols, height, width, 
                             center, base, up)
            det_outline.append(qVec)
            output.write('%f %f %f\n' % (qVec[0], qVec[1], qVec[2]))
            
        for i in range(nRows):
            col = 1
            row = nRows - i
            qVec = calc_qvec(col, row, tof, L1, nRows, nCols, height, width, 
                             center, base, up)
            det_outline.append(qVec)
            output.write('%f %f %f\n' % (qVec[0], qVec[1], qVec[2]))
            
        # col = 1
        # row = 1
        # qVec = calc_qvec(col, row, tof, L1, nRows, nCols, height, width, 
                         # center, base, up)
        # col = 1
        # row = 256
        # qVec = calc_qvec(col, row, tof, L1, nRows, nCols, height, width, 
                         # center, base, up)
        # col = 256
        # row = 256
        # qVec = calc_qvec(col, row, tof, L1, nRows, nCols, height, width, 
                         # center, base, up)
        # col = 256
        # row = 1
        # qVec = calc_qvec(col, row, tof, L1, nRows, nCols, height, width, 
                         # center, base, up)
                         
        # exit()

