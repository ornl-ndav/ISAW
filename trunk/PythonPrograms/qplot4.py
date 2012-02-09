#-----------------------------------
#           qplot4.py
#-----------------------------------

# Program to plot and integrate the q profile of a peak

# A. J. Schultz     beginning December, 2011

import pylab
import struct
import math
import numpy
from sys import exit

class PeakPlot:
    """
    Integrate and plot a peak in Q space from event data.
    """
    def __init__(self):
        self.intI = 0.0
        self.sigI = 0.0
        
    def plotQprofile( self, x, y ):
        """Plot intensity vs. Q for a peak."""
        pylab.plot( x, y )
        pylab.xlabel('Q, 2pi/d')
        pylab.ylabel('Counts')
        plotTitle = 'Peak profile vs. Q, Sapphire Run 3681'
        pylab.title( plotTitle )
        s1 = 'hkl = %d %d %d\ndeltaQ = %5.3f\ncyl length = %5.3f\ncyl radius = %5.3f' % \
            (h,k,l, deltaQ, rangeQ, radiusQ)
        pylab.figtext(0.65, 0.7, s1)
        pylab.grid(True)
        pylab.savefig( 'Qprofile' )   # plot saved
        pylab.show()
        # return    

    def integrate( self, y, peakMin, peakMax, bkgMin, bkgMax):
        """
        Obtain the integrated intensity and the sigma of a peak
        in Q space.
        """
        peak = 0.0                # peak counts
        bkg = 0.0                 # background counts
        numPeakCh = peakMax - peakMin + 1            # number of peak channels
        print '\nnumPeakCh = ', numPeakCh
        numBkgCh =(bkgMax - bkgMin + 1) - numPeakCh  # number of background channels
        print 'bkgMax, bkgMin = ', bkgMax, bkgMin
        print 'numBkgCh = ', numBkgCh
                
        for i in range(bkgMin, peakMin):
            bkg = bkg + y[i]
        
        for i in range(peakMin, peakMax+1):
            peak = peak + y[i]
            
        for i in range(peakMax+1, bkgMax+1):
            bkg = bkg + y[i]

    
        print 'peak = ', peak
        print 'bkg = ', bkg
        ratio = float(numPeakCh)/float(numBkgCh)
        print 'ratio = ', ratio
        
        self.intI = peak - bkg*(ratio)
        self.sigI = math.sqrt(peak + bkg*ratio**2)
        

    
def huq(h, k, l, UB):
    "Multiply hkl times UB matrix to return q-vector"
    hh = [h, k, l]

    q = numpy.dot(hh, UB) * 2.0 * math.pi

    return q

    
# Begin.................................................

# Open the profile output file
output = open('qplot.dat', 'w')

# Read and write the instrument calibration parameters.
input_refl = open('3681EV_020.integrate', 'r')
output_refl = open('3681qplot.integrate', 'w')
refl = ReadReflFile()
refl.readrefl_header(input_refl, output_refl)


# Open matrix file
# filename = raw_input('Matrix file name: ')
filename = '3681_5M.mat'
UBinput = open(filename,'r')

# Initialize UB_IPNS matrix
UB_IPNS = numpy.zeros((3,3))   # Although this is SNS data, the coordinate convention are IPNS.
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

print '\nnumberOfEvents = ', numberOfEvents
# End of reading events

dmin = 1.5                # minium d-spacing
hmax = int(a/dmin) + 1    # maximum h index
kmax = int(b/dmin) + 1    # maximum k index
lmax = int(c/dmin) + 1    # maximum l index

deltaQ = 0.01           # delta Q in units of 2pi/d
print '\ndeltaQ = ', deltaQ, '\n'
rangeQ = 0.4   # length of cylinder in units of 2pi/d
numSteps = int( rangeQ/deltaQ )
print 'numSteps = ', numSteps, '\n'
radiusQ = 0.1   # radius of cylinder in units of 2pi/d

for h in range(-hmax, hmax+1):
    for k in range(-kmax, kmax+1):
        for l in range(-lmax, lmax+1):
        
#            h = 3    # for testing
#            k = -5
#            l = -4
            
            if h == k == l == 0: continue
            
            # R centering
            sumhkl = -h + k + l
            if sumhkl%3 != 0: continue
            
            Qpeak = huq(h, k, l, UB_IPNS)
            
            if Qpeak[0] > 0.0: continue   # x pointing downstream
            if Qpeak[1] > 0.0: continue   # only detectors on -y side
            
            print '\n hkl =', h,k,l
            
            lenQpeak = math.sqrt( numpy.dot(Qpeak, Qpeak) )   # magnitude or length of Qpeak
            
            print 'Qpeak, lenQpeak = ', Qpeak, lenQpeak
            
            d = 2.0 * math.pi / lenQpeak
            if d < dmin: continue
            
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
                
                Qdata = [ Qx, Qy, Qz ]                # data point Q vector
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
            output.write(' %4d %4d %4d %10.2f %8.2f' % (h, k, l, BraggPeak.intI, BraggPeak.sigI))
            for i in range(numSteps):
                if i%12 == 0: output.write('\n')
                output.write(' %5d' % y[i])
            output.write('\n')
            print BraggPeak.intI, BraggPeak.sigI
            
            #BraggPeak.plotQprofile(x,y)
            
            #exit()     # for testing
            

            
  
    
    



