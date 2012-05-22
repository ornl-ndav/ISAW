#!/usr/bin/env python
"""
Fit gaussian to peak profile.
A. J. Schultz, April 2012

profile_fit.py
This version reads the output from qplot5.py to obtain the counts.

profile_fit2.py
Version 2 combines profile_fit and qplot5 in one script. It reads
the data from the EventsToQ file.

profile_fit3.py
Version 3 reads and processes each event, but does not store events in memory.
"""


import pylab
import struct
import math
import os
from numpy import linalg
from time import clock
from read_detcal import *
from scipy.optimize import curve_fit

import crystal as xl

def gaussian(x, a, sig, mu, b, c):
    """ Gaussian function on a linear background."""
    sqrt2pi = 2.506628   # sqrt(2.0 * pi)
    f = (a / (sig * sqrt2pi) * pylab.exp(-0.5 * (x - mu)**2 / sig**2)) + (b * x) + c
    return f

# Begin.................................................

start = clock()

# Make a ./plots subdirectory for the profile plot files.
if not os.path.exists('./plots'):
    os.mkdir('./plots')

# Open and read the user input file
user_input = open('profile_fit3.inp', 'r')
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
center_type = user_param[5]
dmin = float(user_param[6])
wlmin = float(user_param[7])
wlmax = float(user_param[8])
deltaQ = float(user_param[9])
rangeQ = float(user_param[10])
radiusQ = float(user_param[11])

# Open the profile output file
filename = expname + '_profile.dat'
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
numSteps = int(rangeQ / deltaQ) + 1
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
            
            # test for centering
            centering = xl.center(h, k, l, center_type)
            if centering == False: continue
            
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
                if cosAngle > 1.0: cosAngle = 1.0
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

# Read the events from binary file into memory --------------------------------
input = open(events_fname, 'rb')
QEvent = []
numberOfEvents = 0
while True:
    if numberOfEvents == 5e06: break
    
    lineString = input.read(12)
    if lineString == "": break
    Qx, Qy, Qz = struct.unpack('fff', lineString)  # unpack binary data
    numberOfEvents = numberOfEvents + 1
    if (numberOfEvents % 100000) == 0: print '\rEvent %.3e'  % numberOfEvents,

    
    qxyz = pylab.zeros(3)
    qxyz[0] = Qx / (2.0 * math.pi)
    qxyz[1] = Qy / (2.0 * math.pi)
    qxyz[2] = Qz / (2.0 * math.pi)
    
    hklEV = pylab.dot(qxyz, UBinv)
    
    ih = int(round(hklEV[0]))
    if abs(ih) > hmax: continue
    ik = int(round(hklEV[1]))
    if abs(ik) > kmax: continue
    il = int(round(hklEV[2]))
    if abs(il) > lmax: continue
    
    hIndex = ih + hmax
    kIndex = ik + hmax
    lIndex = il + lmax
    peaknum = hklArray[hIndex][kIndex][lIndex]
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
    Qdiff = lenOnQpeak - lenQpeak    # corrected sign of Qdiff, 5/3/2012
    if abs(Qdiff) > (0.5 * rangeQ): continue
	
	# add event to appropriate y channel
    xchannel = int(round((Qdiff / deltaQ))) + 20
    if xchannel < 0 or xchannel > 39: continue
    peak_profile[pki][xchannel] = peak_profile[pki][xchannel] + 1
        
    continue
print '\nnumberOfEvents = ', numberOfEvents

print ''

peakMin = 10   # for 40 channels, start of peak
peakMax = 29   # end of peak
bkgMin = 0     # start of background
bkgMax = 39    # end of background
# x = range(40)
sqrt2pi = 2.506628   # sqrt(2.0 * pi)


for i in range(numOfPeaks):

    x = range(len(peak_profile[i]))
    x = pylab.array(x)
    yobs = pylab.array(peak_profile[i])

    h = peaks[i][0]
    k = peaks[i][1]
    l = peaks[i][2]
    
    # popt is an array of the optimized parameters
    # pcov is the covariance matrix
    p0 = pylab.zeros(5)                 # initial values of parameters
    p0[0] = max(yobs) * 2.5 * sqrt2pi   # initial value of a_gauss
    p0[1] = 2.5                         # initial value of sig_gauss
    p0[2] = 0.5 * len(yobs)             # initial value of mu_gauss, middle of x range
        
    try:
        popt, pcov = curve_fit(gaussian, x, yobs, p0)
        a_gauss = popt[0]
        sig_gauss = popt[1]
        mu_gauss = popt[2]
        b_gauss = popt[3]
        c_gauss = popt[4]
    except RuntimeError:
        print 'RuntimeError for peak %d %d %d' % (h, k, l)
        continue        
        
    if a_gauss == 0.0:
        print 'No counts for peak %d %d %d' % (h, k, l)
        continue
    else:
        siga_gauss = math.sqrt(pcov[0][0])
        sigsig_gauss = math.sqrt(pcov[1][1])
        sigmu_gauss = math.sqrt(pcov[2][2])
        sigb_gauss = math.sqrt(pcov[3][3])
        sigc_gauss = math.sqrt(pcov[4][4])

    if sigsig_gauss > sig_gauss:
        print 'Sig error greater than sig for peak %d %d %d' % (h, k, l)
        continue
    
    peaks[i][12] = a_gauss
    peaks[i][13] = siga_gauss
    
    output.write('%4d %4d %4d %12.4f  %12.4f  %12.4f  %12.4f  %12.4f %12.4f  %12.4f  %12.4f  %12.4f  %12.4f\n' 
        %  (h, k, l, a_gauss, sig_gauss, mu_gauss, b_gauss, c_gauss, siga_gauss, sigsig_gauss, sigmu_gauss, 
        sigb_gauss, sigc_gauss))
    print '%4d %4d %4d %12.4f' % (h, k, l, a_gauss)

    xcalc = []
    ycalc = []
    for i in range(100 * len(yobs)):
        xcalc.append(float(i)/100.0)
        ycalc.append(gaussian(xcalc[i], a_gauss, sig_gauss, mu_gauss, b_gauss, c_gauss))
    
    pylab.plot(xcalc, ycalc)
    pylab.plot(x, yobs, 'g^')
    
    pylab.xlabel('Q channel, 2pi/d')
    pylab.ylabel('Counts')

    plotTitle = '%d %d %d' % (h, k, l)
    pylab.title(plotTitle)
    
    textString = 'f = (a/(sig * sqrt(2*pi)) * exp(-0.5 * (x - mu)**2 / sig**2)) + (b * x) + c'
    pylab.figtext(0.5, 0.85, textString, horizontalalignment='center', fontsize='small')

    textString = 'a = %.2f(%.2f)\nsig = %.2f(%.2f)\nmu = %.2f(%.2f)\nb = %.2f(%.2f)\nc = %.2f(%.2f)\n' % (
        a_gauss, siga_gauss, sig_gauss, sigsig_gauss, mu_gauss, sigmu_gauss, b_gauss, sigb_gauss, c_gauss, 
        sigc_gauss)
    pylab.figtext(0.65, 0.65, textString, family='monospace')
    
    filename = './plots/Profile_fit_%d_%d_%d' % (h, k, l)
    # if h == -12 and k == -12 and l == 2:
        # print filename
        # continue
            
    pylab.savefig(filename)
    pylab.clf()
    # pylab.close()


# Begin writing peaks to the integrate file.
chi = 0.0
phi = 0.0
omega = 0.0
moncnt = 10000
seqn = 0
# Step through each detector
output_refl.write('\n')
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






    





