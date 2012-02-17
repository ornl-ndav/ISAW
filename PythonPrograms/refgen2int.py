#-----------------------------------
#           refgen2int.py
#-----------------------------------

# Program to generate calculated peaks
# and output an integrate file.

# A. J. Schultz     beginning February, 2012

import math
import numpy as np
from sys import exit

import crystal as xl
from read_detcal import *

    
# Open and read the user input file
user_input = open('refgen.inp', 'r')
user_param = []
while True:
    lineString = user_input.readline()
    lineList = lineString.split()
    if len(lineList) == 0: break
    user_param.append(lineList[0])
nrun = int(user_param[0])
expname = user_param[1]
detcal_fname = user_param[2]
orient_fname = user_param[3]
dmin = float(user_param[4])
wlmin = float(user_param[5])
wlmax = float(user_param[6])

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
# Although this is SNS data, the coordinate convention are IPNS.
UB_IPNS = np.zeros((3,3))   
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

# Calculate range of h, k and l
hmax = int(a/dmin) + 1    # maximum h index
kmax = int(b/dmin) + 1    # maximum k index
lmax = int(c/dmin) + 1    # maximum l index

peaks = []
ipk = 0
intI = 0.0
sigI = 0.0
rflg = 0

# Begin determining and storing peaks
for h in range(-hmax, hmax+1):
    for k in range(-kmax, kmax+1):
        for l in range(-lmax, lmax+1):
        
            # h = 3    # for testing
            # k = -5
            # l = -4
            
            if h == k == l == 0: continue
            
            # R centering for sapphire
            sumhkl = -h + k + l
            if sumhkl%3 != 0: continue
            
            Qpeak = xl.huq(h, k, l, UB_IPNS)
            
            if Qpeak[0] > 0.0: continue   # x pointing downstream
            if Qpeak[1] > 0.0: continue   # only detectors on -y side
            
            lenQpeak = math.sqrt(np.dot(Qpeak, Qpeak))   # magnitude or length of Qpeak
            
            dsp = 1.0 / lenQpeak
            if dsp < dmin: continue
            
            # ISAW uses IPNS coordinate system for SNS data
            peak_params = xl.calc_2th_wl_IPNS(Qpeak) 
            two_theta = math.radians(peak_params[0])
            wl = peak_params[1]
            if wl < wlmin: continue
            if wl > wlmax: continue
            
            # create neutron vector in Q space with SNS coordinates
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
                   
                    peaks.append([h, k, l, col, row, chan, L2, two_theta, az, 
                                  wl, dsp, ipk, intI, sigI, rflg, dc.detNum[i], 
                                  Qpeak])
                    break


chi = 0.0
phi = 0.0
omega = 0.0
moncnt = 10000
seqn = 0
numPeaks = len(peaks)
print 'numPeaks = ', numPeaks

# Begin writing peaks to the integrate file.
# Step through each detector
for i in range(dc.nod):
    output_refl.write('0 NRUN DETNUM    CHI    PHI  OMEGA MONCNT\n')
    output_refl.write('1 %4d %6d %6.2f %6.2f %6.2f %d\n' 
                      % (nrun, dc.detNum[i], chi, phi, omega, moncnt))
    output_refl.write('2   SEQN    H    K    L     COL     ROW    CHAN' + 
                      '       L2  2_THETA       AZ        WL        D' + 
                      '   IPK      INTI   SIGI RFLG\n')
    # Step through the list of peaks
    for j in range(numPeaks):
        if peaks[j][15] == dc.detNum[i]:
            seqn = seqn + 1
            output_refl.write(
                '3 %6d %4d %4d %4d %7.2f %7.2f %7.2f %8.3f %8.5f %8.5f %9.6f %8.4f %5d %9.2f %6.2f %4d\n' 
                % (seqn, peaks[j][0], peaks[j][1], peaks[j][2], peaks[j][3], peaks[j][4], peaks[j][5], 
                peaks[j][6], peaks[j][7], peaks[j][8], peaks[j][9], peaks[j][10], peaks[j][11], 
                peaks[j][12], peaks[j][13], peaks[j][14]))

print '\nAll done!' 