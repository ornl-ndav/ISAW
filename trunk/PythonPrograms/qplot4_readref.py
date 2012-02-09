#-----------------------------------
#           qplot4_readref.py
#-----------------------------------

# Program to plot the q profile of a peak

# A. J. Schultz     beginning December, 2011

# This version reads the hkl's from an integrate file.
# January, 2012

import pylab
import struct
import math
import numpy

import sys
import time

from read_refl_file import *
from peak_plot import *
    
def huq(h, k, l, UB):
    "Multiply hkl times UB matrix to return q-vector"
    hh = [h, k, l]

    q = numpy.dot(hh, UB) * 2.0 * math.pi

    return q



#------------------------------------------------------------------------------    
# Begin.................................................

start = time.time()

output = open('qplot.dat', 'w')

# Open matrix file
# filename = raw_input('Matrix file name: ')
filename = '3681_5M.mat'
UBinput = open(filename,'r')

# Initialize UB_IPNS matrix.
# Although this is SNS data, the coordinate convention are IPNS.
UB_IPNS = numpy.zeros((3,3))   
print '\n Input from matrix file ' + filename + ':\n'

# Read matrix file into UB_IPNS matrix
for i in range(3):
    lineString = UBinput.readline()
    print lineString.strip('\n')
    lineList = lineString.split()
    for j in range(3):
        UB_IPNS[i,j] = float(lineList[j])
# Read next 2 lines containing lattice constants and esd's
lineString = UBinput.readline()
print lineString.strip('\n')
lineList = lineString.split()
a = float(lineList[0])   # unit cell a-axis
b = float(lineList[1])
c = float(lineList[2])
lineString = UBinput.readline()   # read sigmas
print lineString.strip('\n')
# End of reading and printing matrix file

# Begin reading the integrate file.
input_refl = open('3681EV_020.integrate', 'r')
output_refl = open('3681qplot.integrate', 'w')
refl = ReadReflFile()
refl.readrefl_header(input_refl, output_refl)


# Read the events from binary file into memory
input = open('EventsToQ.bin', 'rb')
QEvent = []
numberOfEvents = 0

while True:
    lineString = input.read(12)
    if lineString == "": break
    Qx, Qy, Qz = struct.unpack('fff', lineString)  # unpack binary data
    QEvent.append([Qx, Qy, Qz])                    # store events in QEvent array
    numberOfEvents = numberOfEvents + 1

end = time.time()
elapsed = end - start
print '\nElapsed time to read events is %f seconds.' % elapsed

print '\nnumberOfEvents = ', numberOfEvents
# End of reading events

dmin = 0.5                # minium d-spacing
hmax = int(a/dmin) + 1    # maximum h index
kmax = int(b/dmin) + 1    # maximum k index
lmax = int(c/dmin) + 1    # maximum l index

deltaQ = 0.01           # delta Q in units of 2pi/d
print '\ndeltaQ = ', deltaQ, '\n'
rangeQ = 0.4   # length of cylinder in units of 2pi/d
numSteps = int( rangeQ/deltaQ )
print 'numSteps = ', numSteps, '\n'
radiusQ = 0.1   # radius of cylinder in units of 2pi/d

# Initialize parameters
eof = nrun = dn = moncnt = 0
chi = phi= omega = 0.0

numPeaks = 0
# Begin reading and integrate hkl's.
while True:
    peak = refl.readrefl_SNS(input_refl, output_refl, eof, nrun, dn, chi, phi, 
                             omega, moncnt)
    
    nrun = peak[0]
    dn = peak[1]
    chi = peak[2]
    phi = peak[3]
    omega = peak[4]
    moncnt = peak[5]
    seqnum = peak[6]
    h = peak[7]
    k = peak[8]
    l = peak[9]
    col = peak[10]
    row = peak[11]
    chan = peak[12]
    L2 = peak[13]
    twoth = peak[14]
    az = peak[15]
    wl = peak[16]
    dsp = peak[17]
    ipkobs = peak[18]
    inti_1 = peak[19]
    sigi_1 = peak[20]
    reflag = peak[21]
    eof = peak[22]
    
    if eof == 0: break
    if h == k == l == 0: break
    if dsp < dmin: continue
    
    # R centering
    sumhkl = -h + k + l
    if sumhkl%3 != 0: continue
    
    Qpeak = huq(h, k, l, UB_IPNS)
    
    if Qpeak[0] > 0.0: continue   # x pointing downstream
    if Qpeak[1] > 0.0: continue   # only detectors on -y side
    
    print '\n hkl =', h,k,l
    startPeak = time.time()
    
    lenQpeak = math.sqrt( numpy.dot(Qpeak, Qpeak) )   # magnitude or length of Qpeak
    
    #print 'Qpeak, lenQpeak = ', Qpeak, lenQpeak
    
    #d = 2.0 * math.pi / lenQpeak
    #if d < dmin: continue
    
    lenQpeakInt = int(lenQpeak * 100)
    midx = float(lenQpeakInt)
    midx = midx / 100.

    x = []
    y = []

    for i in range(numSteps):
        x.append( midx - 0.5*rangeQ + i*deltaQ ) 
        y.append( 0 )

    num = 0

    # input = open('EventsToQ.bin', 'rb')

    for i in range(numberOfEvents):
        Qx = QEvent[i][0]
        Qy = QEvent[i][1]
        Qz = QEvent[i][2]
        
        # do initial test for event within 0.5 of the peak
        if abs(Qpeak[0] - Qx) > 0.3: continue
        if abs(Qpeak[1] - Qy) > 0.3: continue
        if abs(Qpeak[2] - Qz) > 0.3: continue
        
        Qdata = [Qx, Qy, Qz]                # data point Q vector
        lenQdata = math.sqrt( numpy.dot(Qdata, Qdata) )  # length of data point Q vector
        
        cosAng = numpy.dot(Qpeak, Qdata) / (lenQpeak * lenQdata)
        
        # define a cylinder
        angle = math.acos( cosAng )
        lenPerpendicular = lenQdata * math.sin(angle)
        if lenPerpendicular > radiusQ: continue
        lenOnQpeak = lenQdata * cosAng   # projection of event on the Q vector
        if abs(lenQpeak - lenOnQpeak) > 0.5*rangeQ: continue
        num = num + 1
        
        # add event to appropriate y channel
        for i in range(numSteps):
            if x[i] > lenOnQpeak:
                y[i] = y[i] + 1
                break
                
    print '\nnum = ', num  
    if num == 0: continue

    peakMin = 10   # for 40 channels, start of peak
    peakMax = 29   # end of peak
    bkgMin = 0     # start of background
    bkgMax = 39    # end of background

    BraggPeak = PeakPlot()
    BraggPeak.integrate( y, peakMin, peakMax, bkgMin, bkgMax)
    output.write(' %4d %4d %4d %10.2f %8.2f' % (h, k, l,
                 BraggPeak.intI, BraggPeak.sigI))
    for i in range(numSteps):
        if i%12 == 0: output.write('\n')
        output.write(' %5d' % y[i])
    output.write('\n')
    print BraggPeak.intI, BraggPeak.sigI
    
    output_refl.write(
        '3 %6d %4d %4d %4d %7.2f %7.2f %7.2f %8.3f %8.5f %8.5f %9.6f %8.4f %5d %9.2f %6.2f %4d\n' % 
        (seqnum, h, k, l, col, row, chan, L2, twoth, az, wl, dsp, ipkobs, 
        BraggPeak.intI, BraggPeak.sigI, reflag))
    numPeaks += 1
    endPeak = time.time()
    elapsed = endPeak - startPeak
    print 'Elpased peak time is %f seconds.' % elapsed
    
    #BraggPeak.plotQprofile(x,y)
    
    #exit()     # for testing

print '\nTotal number of peaks is %d' % numPeaks
end = time.time()
elapsed = end - start
print '\nTotal elapsed time is %f seconds.' % elapsed
            

            
  
    
    



