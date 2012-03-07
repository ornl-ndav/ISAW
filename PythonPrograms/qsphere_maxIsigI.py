#-----------------------------------
#           qsphere_maxIsigI.py
#-----------------------------------

# Program to integrate spheres in q space of a peak
# from event data and to determine the best radius
# by maximizing I/sig(I).

# A. J. Schultz    March, 2012

import struct
import math
import numpy as np
from numpy import linalg
# from sys import exit, stdout
from time import clock

import crystal as xl
from read_detcal import *


def distance(vec1, vec2):
    """ Distance between two points. """
    dx = vec2[0] - vec1[0]
    dy = vec2[1] - vec1[1]
    dz = vec2[2] - vec1[2]
    dist = math.sqrt(dx**2 + dy**2 + dz**2)
    return dist
        
# Begin.................................................

start = clock()

# Open and read the user input file
user_input = open('qsphere_maxIsigI.inp', 'r')
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
radiusQ = float(user_param[8])
delta_radius = float(user_param[9])
num_radii = int(user_param[10])


# Read and write the instrument calibration parameters.
detcal_input = open(detcal_fname, 'r')
filename = expname + '.integrate'
output_refl = open(filename, 'w')
dc = ReadDetCal()
first_line = '# qplot integrate file'
dc.read_detcal(detcal_input, output_refl, first_line)
# create array of detector centers for later use
det_center = np.zeros((dc.nod, 3))
det_base = np.zeros((dc.nod, 3))
det_up = np.zeros((dc.nod, 3))
normal = np.zeros((dc.nod, 3))
for i in range(dc.nod):
    det_center[i] = [dc.centerX[i], dc.centerY[i], dc.centerZ[i]]
    det_base[i] = [dc.baseX[i], dc.baseY[i], dc.baseZ[i]]
    det_up[i] = [dc.upX[i], dc.upY[i], dc.upZ[i]]
    normal[i] = np.cross(det_base[i], det_up[i])

# Open matrix file
# filename = raw_input('Matrix file name: ')
UBinput = open(orient_fname,'r')

# Initialize UB_IPNS matrix
UB_IPNS = np.zeros((3,3))   # Although this is SNS data, the coordinate convention are IPNS.
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

hmax = int(a/dmin) + 1    # maximum h index
kmax = int(b/dmin) + 1    # maximum k index
lmax = int(c/dmin) + 1    # maximum l index
nh = 2 * hmax + 1
nk = 2 * kmax + 1
nl = 2 * lmax + 1
hklArray = np.zeros((nh, nk, nl))


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
            
            lenQpeak = math.sqrt(np.dot(Qpeak, Qpeak))   # magnitude or length of Qpeak
            
            dsp = 1.0 / lenQpeak
            if dsp < dmin: continue
     
			# ISAW uses IPNS coordinate system for SNS data
            peak_params = xl.calc_2th_wl_IPNS(Qpeak)
            two_theta = (peak_params[0] / 180.0) * math.pi
            wl = peak_params[1]
            if wl < wlmin: continue
            if wl > wlmax: continue
            
            # Create neutron vectorin Q space in SNS coordinates.
			# This is the scattered vector with the origin on the crystal.
            nvector = np.array([Qpeak[1], Qpeak[2], Qpeak[0]+1.0/wl])

            # create neutron vector in Q space with SNS coordinates
            nvecQ = np.zeros(3)
            nvecQ = np.array([Qpeak[1], Qpeak[2], Qpeak[0]+1.0/wl])

            # Determine which detector, if any, the peak hits
            for i in range(dc.nod):
                # First check if scattering vector is within 16 deg if center of detector
                cosAngle = (np.dot(det_center[i], nvecQ)) / (dc.detD[i] * (1.0 / wl))
                angle = math.degrees(math.acos(cosAngle))
                if angle < 16.0:
                    factor = np.dot(det_center[i], normal[i]) / np.dot(nvecQ, normal[i])
                    # nvecR is the neutron vector in real space coordinates
                    nvecR = factor * nvecQ
                    L2 = math.sqrt(np.dot(nvecR, nvecR))
                    # det_vector is the vector in the plan of the detector
                    det_vector = nvecR - det_center[i]
                    col = np.dot(det_vector, det_base[i])
                    col = 256. * (col / dc.width[i]) + 128.
                    if col < 0.0 or col > 255.: break
                    row = np.dot(det_vector, det_up[i])
                    row = 256. * (row / dc.height[i]) + 128.
                    if row < 0.0 or row > 255.: break
                    chan = (2.5282 * wl * (dc.L1 + L2)) / 10.0 # TOF in micorsec/10
                    az = math.atan(nvecR[1]/nvecR[0])
                    if nvecR[0] < 0.0: az = az + math.pi
                    aztemp = az
                    if aztemp > math.pi: az = az - (2.0 * math.pi)
                    if aztemp < -math.pi: az = az + (2.0 * math.pi)
					
                    # print '\n+++ peakct = ', peakct
                    peaks.append([h, k, l, col, row, chan, L2, two_theta, az, wl,
                                  dsp, ipk, intI, sigI, rflg, dc.detNum[i], 
                                  Qpeak])
                    # print '\nseqn = ', seqn
                    # print 'peaks = ', peaks[seqn]
                    # peaks[seqn][17] = 10.0
                    # print 'peaks = ', peaks[seqn]
                    # print '\npeakct = ', peakct
                    
                    hh = h + hmax
                    kk = k + kmax
                    ll = l + lmax
                    seqn = seqn + 1
                    hklArray[hh][kk][ll] = seqn
                    

numOfPeaks = len(peaks)
print '\nnumOfPeaks = ', numOfPeaks

UBinv = linalg.inv(UB_IPNS)

# Begin testing 3 radii
pkRadius = []
bgRadius1 = []
bgRadius2 = []
cubeRoot7 = 7.0**(1./3.)
for i in range(num_radii):
    pkRadius.append((radiusQ + (i * delta_radius)) / (2.0 * math.pi))
    # Calculate min and max background radii so that background volume
    # equals peak volume.
    bgRadius1.append(pkRadius[i] * cubeRoot7)
    bgRadius2.append(pkRadius[i] * 2.0)

peak_cts = np.zeros((numOfPeaks, num_radii))
background_cts = np.zeros((numOfPeaks, num_radii))
print ''
# for i in range(numberOfEvents):
for i in range(100000):
    
    if (i % 100000) == 0: print '\r Event', i,
    
    qxyz = np.zeros(3)
    qxyz[0] = QEvent[i][0] / (2.0 * math.pi)
    qxyz[1] = QEvent[i][1] / (2.0 * math.pi)
    qxyz[2] = QEvent[i][2] / (2.0 * math.pi)
    
    hklEV = np.dot(qxyz, UBinv)
    
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
        
    # Test for event within 2 times the maximum sphere radius
    Qpeak = [peaks[pki][16][0], peaks[pki][16][1], peaks[pki][16][2]]
    Qdist = distance(Qpeak, qxyz)
    if Qdist > (bgRadius2[num_radii - 1]): continue
    
    for j in range(num_radii):
        if Qdist < pkRadius[j]:
            peak_cts[pki][j] = peak_cts[pki][j] + 1
        if Qdist > bgRadius1[j] and Qdist < bgRadius2[j]:
            background_cts[pki][j] = background_cts[pki][j] + 1
 
    
    continue


# Begin writing peaks to the integrate file.
chi = 0.0
phi = 0.0
omega = 0.0
moncnt = 10000
seqn = 0

print ''

# Step through each detector
for i in range(dc.nod):
    if i == 0: output_refl.write('\n')
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
            
            for jj in range(num_radii):
                intI = peak_cts[j][jj] - background_cts[j][jj]
                sigI = math.sqrt(peak_cts[j][jj] + background_cts[j][jj])
            
                output_refl.write(
                    '3 %6d %4d %4d %4d %7.2f %7.2f %7.2f %8.3f %8.5f %8.5f %9.6f %8.4f %5d %9.2f %6.2f %4d\n' 
                    % (seqn, peaks[j][0], peaks[j][1], peaks[j][2], peaks[j][3], peaks[j][4], peaks[j][5], 
                    peaks[j][6], peaks[j][7], peaks[j][8], peaks[j][9], peaks[j][10], peaks[j][11], 
                    intI, sigI, jj))


end = clock()
elapsed = end - start
print '\nElapsed time is %f seconds.' % elapsed
print '\nAll done!' 






    



