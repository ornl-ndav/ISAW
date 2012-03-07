#-----------------------------------
#           qplot5.py
#-----------------------------------

# Program to plot and integrate the q profile of a peak

# Version 5:
#    This version first generates a list of hkl peaks that
#    can be observed in the data. It then loops through the events
#    just once and applies events to the appropiate peaks.
# February, 2012

# A. J. Schultz     beginning December, 2011

import pylab
import struct
import math
# import numpy as np
from numpy import linalg
# from sys import exit, stdout
from time import clock

import crystal as xl
from read_detcal import *

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
        numBkgCh =(bkgMax - bkgMin + 1) - numPeakCh  # number of background channels
                
        for i in range(bkgMin, peakMin):
            bkg = bkg + y[i]
        
        for i in range(peakMin, peakMax+1):
            peak = peak + y[i]
            
        for i in range(peakMax+1, bkgMax+1):
            bkg = bkg + y[i]

        ratio = float(numPeakCh)/float(numBkgCh)
        
        self.intI = peak - bkg*(ratio)
        self.sigI = math.sqrt(peak + bkg*ratio**2)
    
# Begin.................................................

start = clock()

# Open and read the user input file
user_input = open('qplot5.inp', 'r')
user_param = []
while True:
    lineString = user_input.readline()
    lineList = lineString.split()
    if len(lineList) == 0: break
    user_param.append(lineList[0])
nrun = int(user_param[0])
expname = user_param[1]
detcal_fname = user_param[2]
events_fname = user_param[3]
orient_fname = user_param[4]
dmin = float(user_param[5])
wlmin = float(user_param[6])
wlmax = float(user_param[7])
deltaQ = float(user_param[8])
rangeQ = float(user_param[9])
radiusQ = float(user_param[10])

# Open the profile output file
filename = expname + '_qplot.dat'
output = open(filename, 'w')

# Read and write the instrument calibration parameters.
detcal_input = open(detcal_fname, 'r')
filename = expname + '.integrate'
output_refl = open(filename, 'w')
dc = ReadDetCal()
first_line = '# qplot integrate file'
dc.read_detcal(detcal_input, output_refl, first_line)
# create array of detector centers for later use
det_center = pylab.zeros((dc.nod, 3))
det_base = pylab.zeros((dc.nod, 3))
det_up = pylab.zeros((dc.nod, 3))
normal = pylab.zeros((dc.nod, 3))
for i in range(dc.nod):
    det_center[i] = [dc.centerX[i], dc.centerY[i], dc.centerZ[i]]
    det_base[i] = [dc.baseX[i], dc.baseY[i], dc.baseZ[i]]
    det_up[i] = [dc.upX[i], dc.upY[i], dc.upZ[i]]
    normal[i] = pylab.cross(det_base[i], det_up[i])

# Open matrix file
# filename = raw_input('Matrix file name: ')
UBinput = open(orient_fname,'r')

# Initialize UB_IPNS matrix
UB_IPNS = pylab.zeros((3,3))   # Although this is SNS data, the coordinate convention are IPNS.
print '\n Input from matrix file ' + orient_fname + ':\n'

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
input = open(events_fname, 'rb')
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

# dmin = 1.5                # minium d-spacing
hmax = int(a/dmin) + 1    # maximum h index
kmax = int(b/dmin) + 1    # maximum k index
lmax = int(c/dmin) + 1    # maximum l index
nh = 2 * hmax + 1
nk = 2 * kmax + 1
nl = 2 * lmax + 1
hklArray = pylab.zeros((nh, nk, nl))

# delta Q in units of 2pi/d
print '\ndeltaQ = ', deltaQ, '\n'
deltaQ = deltaQ / (2.0 * math.pi)
# rangeQ = length of cylinder in units of 2pi/d
rangeQ = rangeQ / (2.0 * math.pi)
numSteps = int(rangeQ / deltaQ)
print 'numSteps = ', numSteps, '\n'
# radius of cylinder in units of 2pi/d
print 'radiusQ = ', radiusQ
radiusQ = radiusQ / (2.0 * math.pi)

# Begin determining and storing peaks
peaks = []
seqn = 0
ipk = 0
intI = 0.0
sigI = 0.0
rflg = 0

for h in range(-hmax, hmax+1):
    for k in range(-kmax, kmax+1):
        for l in range(-lmax, lmax+1):
        
            if h == k == l == 0: continue
            
            # R centering for sapphire
            sumhkl = -h + k + l
            if sumhkl%3 != 0: continue
            
            Qpeak = xl.huq(h, k, l, UB_IPNS)  # units of 1/d
            
            if Qpeak[0] > 0.0: continue   # x pointing downstream
            if Qpeak[1] > 0.0: continue   # only detectors on -y side
            
            lenQpeak = math.sqrt(pylab.dot(Qpeak, Qpeak))   # magnitude or length of Qpeak
            
            dsp = 1.0 / lenQpeak
            if dsp < dmin: continue
     
			# ISAW uses IPNS coordinate system for SNS data
            peak_params = xl.calc_2th_wl_IPNS(Qpeak) # ISAW uses IPNS coordinate system for SNS data
            two_theta = (peak_params[0] / 180.0) * math.pi
            wl = peak_params[1]
            if wl < wlmin: continue
            if wl > wlmax: continue
            
            # Create neutron vectorin Q space in SNS coordinates.
			# This is the scattered vector with the origin on the crystal.
            nvector = pylab.array([Qpeak[1], Qpeak[2], Qpeak[0]+1.0/wl])

            # create neutron vector in Q space with SNS coordinates
            nvecQ = pylab.zeros(3)
            nvecQ = pylab.array([Qpeak[1], Qpeak[2], Qpeak[0]+1.0/wl])

            # Determine which detector, if any, the peak hits
            for i in range(dc.nod):
                # First check if scattering vector is within 16 deg if center of detector
                cosAngle = (pylab.dot(det_center[i], nvecQ)) / (dc.detD[i] * (1.0 / wl))
                angle = math.degrees(math.acos(cosAngle))
                if angle < 16.0:
                    factor = pylab.dot(det_center[i], normal[i]) / pylab.dot(nvecQ, normal[i])
                    # nvecR is the neutron vector in real space coordinates
                    nvecR = factor * nvecQ
                    L2 = math.sqrt(pylab.dot(nvecR, nvecR))
                    # det_vector is the vector in the plan of the detector
                    det_vector = nvecR - det_center[i]
                    col = pylab.dot(det_vector, det_base[i])
                    col = 256. * (col / dc.width[i]) + 128.
                    if col < 0.0 or col > 255.: break
                    row = pylab.dot(det_vector, det_up[i])
                    row = 256. * (row / dc.height[i]) + 128.
                    if row < 0.0 or row > 255.: break
                    chan = (2.5282 * wl * (dc.L1 + L2)) / 10.0 # TOF in micorsec/10
                    az = math.atan(nvecR[1]/nvecR[0])
                    if nvecR[0] < 0.0: az = az + math.pi
                    aztemp = az
                    if aztemp > math.pi: az = az - (2.0 * math.pi)
                    if aztemp < -math.pi: az = az + (2.0 * math.pi)
					
                    peaks.append([h, k, l, col, row, chan, L2, two_theta, az, 
                                  wl, dsp, ipk, intI, sigI, rflg, dc.detNum[i], 
                                  Qpeak])
                    hh = h + hmax
                    kk = k + kmax
                    ll = l + lmax
                    seqn = seqn + 1
                    hklArray[hh][kk][ll] = seqn

numOfPeaks = len(peaks)
print '\nnumOfPeaks = ', numOfPeaks
peak_profile = pylab.zeros((numOfPeaks, 40))  # array to store 1D peak profiles

UBinv = linalg.inv(UB_IPNS)
first_test_dist = (rangeQ / 2.0) * 1.2

print ''
for i in range(numberOfEvents):
    
    if (i % 100000) == 0: print '\revent ', i,
    
    qxyz = pylab.zeros(3)
    qxyz[0] = QEvent[i][0] / (2.0 * math.pi)
    qxyz[1] = QEvent[i][1] / (2.0 * math.pi)
    qxyz[2] = QEvent[i][2] / (2.0 * math.pi)
    
    hklEV = pylab.dot(qxyz, UBinv)
    
    ih = int(round(hklEV[0]))
    if abs(ih) > hmax: continue
    ik = int(round(hklEV[1]))
    if abs(ik) > kmax: continue
    il = int(round(hklEV[2]))
    if abs(il) > lmax: continue
    
    iIndex = ih + hmax
    kIndex = ik + hmax
    lIndex = il + lmax
    peaknum = hklArray[iIndex][kIndex][lIndex]
    if peaknum == 0: continue   # no hkl peak nearby
    
    pki = int(peaknum - 1)
		
	# Do initial test for event within 0.3 of the peak
    Qpeak = [peaks[pki][16][0], peaks[pki][16][1], peaks[pki][16][2]]
    if abs(Qpeak[0] - qxyz[0]) > first_test_dist: continue
    if abs(Qpeak[1] - qxyz[1]) > first_test_dist: continue
    if abs(Qpeak[2] - qxyz[2]) > first_test_dist: continue
	
    Qdata = [qxyz[0], qxyz[1], qxyz[2]]           # data point Q vector
    lenQdata = math.sqrt( pylab.dot(Qdata, Qdata) )  # length of data point Q vector
    lenQpeak = 1.0 / peaks[pki][10]               # 1/dsp
    
    cosAng = pylab.dot(Qpeak, Qdata) / (lenQpeak * lenQdata)
	
	# define a cylinder
    angle = math.acos(cosAng)
    lenPerpendicular = lenQdata * math.sin(angle)
    if lenPerpendicular > radiusQ: continue
    lenOnQpeak = lenQdata * cosAng   # projection of event on the Q vector
    Qdiff = lenQpeak - lenOnQpeak
    if abs(Qdiff) > (0.5 * rangeQ): continue
	
	# add event to appropriate y channel
    xchannel = int(round((Qdiff / deltaQ))) + 20
    if xchannel < 0 or xchannel > 39: continue
    peak_profile[pki][xchannel] = peak_profile[pki][xchannel] + 1
        
    continue

peakMin = 10   # for 40 channels, start of peak
peakMax = 29   # end of peak
bkgMin = 0     # start of background
bkgMax = 39    # end of background
x = range(40)

for i in range(numOfPeaks):

    BraggPeak = PeakPlot()
    BraggPeak.integrate( peak_profile[i], peakMin, peakMax, bkgMin, bkgMax)
    peaks[i][12] = BraggPeak.intI
    peaks[i][13] = BraggPeak.sigI
    h = peaks[i][0]
    k = peaks[i][1]
    l = peaks[i][2]
    output.write(' %4d %4d %4d %10.2f %8.2f' % (h, k, l, BraggPeak.intI, BraggPeak.sigI))
    for j in range(numSteps):
        if j%12 == 0: output.write('\n')
        output.write(' %5d' % peak_profile[i][j])
    output.write('\n')

# Begin writing peaks to the integrate file.
chi = 0.0
phi = 0.0
omega = 0.0
moncnt = 10000
seqn = 0
# Step through each detector
for i in range(dc.nod):
    output_refl.write('0 NRUN DETNUM    CHI    PHI  OMEGA MONCNT\n')
    output_refl.write('1 %4d %6d %6.2f %6.2f %6.2f %d\n' 
                      % (nrun, dc.detNum[i], chi, phi, omega, moncnt))
    output_refl.write('2   SEQN    H    K    L     COL     ROW    CHAN' + 
                      '       L2  2_THETA       AZ        WL        D' + 
                      '   IPK      INTI   SIGI RFLG\n')
    # Step through the list of peaks
    for j in range(numOfPeaks):
        if peaks[j][15] == dc.detNum[i]:
            seqn = seqn + 1
            output_refl.write(
                '3 %6d %4d %4d %4d %7.2f %7.2f %7.2f %8.3f %8.5f %8.5f %9.6f %8.4f %5d %9.2f %6.2f %4d\n' 
                % (seqn, peaks[j][0], peaks[j][1], peaks[j][2], peaks[j][3], peaks[j][4], peaks[j][5], 
                peaks[j][6], peaks[j][7], peaks[j][8], peaks[j][9], peaks[j][10], peaks[j][11], 
                peaks[j][12], peaks[j][13], peaks[j][14]))


end = clock()
elapsed = end - start
print '\nElapsed time is %f seconds.' % elapsed
print '\nAll done!' 






    



