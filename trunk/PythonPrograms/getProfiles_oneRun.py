#!/usr/bin/env python
"""
Get profile data and output to a profile file.
A. J. Schultz, 2012

Version 2:
This version can process multiple runs.
It replaces get_profile_data.py and get_profile_data_multiple_runs.py.

Version 3:
This version reads the diffractometer angles and rotates the UB matrix.

getProfileData3_oneRun.py
This version processes one run with using the dictionary and a config file for parameters.
This script can then be used in the parallel runs script.
February, 2013
"""


import numpy
import struct
import math
from numpy import linalg
import time
import sys
import os

if os.path.exists('/SNS/TOPAZ/shared/PythonPrograms/PythonLibrary'):
    sys.path.append('/SNS/TOPAZ/shared/PythonPrograms/PythonLibrary')
else:
    sys.path.append('C:/ISAW_repo/PythonPrograms/PythonLibrary')
    
import ReduceDictionary    
from integrate_1d_peak import *
from read_detcal import *
import crystal as xl

# Begin.................................................

start = time.time()

#
# Get the config file name and the run number to process from the command line
#
if (len(sys.argv) < 3):
  print "You MUST give the config file name and run number on the command line"
  exit(0)

config_file_name = sys.argv[1]
run              = sys.argv[2]

#
# Load the parameter names and values from the specified configuration file 
# into a dictionary and set all the required parameters from the dictionary.
#
params_dictionary = ReduceDictionary.LoadDictionary( config_file_name )

instrument_name             = params_dictionary[ "instrument_name" ]
expname                     = params_dictionary[ "expname" ]
detcal_fname                = params_dictionary[ "detcal_fname" ]
events_directory            = params_dictionary[ "events_directory" ] 
orientation_filename_suffix = params_dictionary[ "orientation_filename_suffix" ] 
center_type                 = params_dictionary[ "center_type" ] 
dmin                        = float(params_dictionary[ "dmin" ])
wlmin                       = float(params_dictionary[ "wlmin" ])
wlmax                       = float(params_dictionary[ "wlmax" ])
deltaQ                      = float(params_dictionary[ "deltaQ" ])
rangeQ                      = float(params_dictionary[ "rangeQ" ])
radiusQ                     = float(params_dictionary[ "radiusQ" ])
percentOneBackground        = float(params_dictionary[ "percentOneBackground" ])

monitorAngles_input = open('monitorCtsAndAngles.dat', 'r')
# Read run numbers, monitor counts and angles
while True:
    lineString = monitorAngles_input.readline()
    lineList = lineString.split()
    if len(lineList) == 0:
        print 'End of monitorCtsAndAngles.dat file.'
        break
    if lineList[0] == run:
        monitor_counts = int(lineList[1])
        omega_deg = float(lineList[2])
        chi_deg = float(lineList[3])
        phi_deg = float(lineList[4])
        break
monitorAngles_input.close()

# delta Q in units of 2pi/d
print '\ndeltaQ = ', deltaQ, '\n'
deltaQ = deltaQ / (2.0 * math.pi)
# rangeQ = length of cylinder in units of 2pi/d
rangeQ = rangeQ / (2.0 * math.pi)
numSteps = int(rangeQ / deltaQ) + 1

bkgMin = 0
peakMin = int(numSteps * percentOneBackground / 100)
peakMax = int(numSteps - peakMin - 1)
bkgMax = numSteps - 1

print 'numSteps = ', numSteps, '\n'
# radius of cylinder in units of 2pi/d
print 'radiusQ = ', radiusQ
radiusQ = radiusQ / (2.0 * math.pi)

# Read the instrument calibration parameters.
detcal_input = open(detcal_fname, 'r')
filename = expname + '.profiles'
output = open(filename, 'w')
dc = ReadDetCal()
first_line = '# get_profile_data file'
dc.read_detcal(detcal_input, output, first_line)

# create array of detector centers for later use
det_center = numpy.zeros((dc.nod, 3))
det_base = numpy.zeros((dc.nod, 3))
det_up = numpy.zeros((dc.nod, 3))
normal = numpy.zeros((dc.nod, 3))
for i in range(dc.nod):
    det_center[i] = [dc.centerX[i], dc.centerY[i], dc.centerZ[i]]
    det_base[i] = [dc.baseX[i], dc.baseY[i], dc.baseZ[i]]
    det_up[i] = [dc.upX[i], dc.upY[i], dc.upZ[i]]
    normal[i] = numpy.cross(det_base[i], det_up[i])

# Open matrix file
orient_fname = run + orientation_filename_suffix
UBinput = open(orient_fname,'r')

# Initialize UB_IPNS matrix
UB_IPNS = numpy.zeros((3,3))   # Although this is SNS data, the coordinate convention are IPNS.
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

# Rotate the orientation matrix
UB_rotated = numpy.zeros((3,3))
UB_rotated = xl.rotate_matrix(UB_IPNS, omega_deg, chi_deg, phi_deg, 'SNS')

hmax = int(a/dmin) + 1    # maximum h index
kmax = int(b/dmin) + 1    # maximum k index
lmax = int(c/dmin) + 1    # maximum l index
nh = 2 * hmax + 1
nk = 2 * kmax + 1
nl = 2 * lmax + 1
hklArray = numpy.zeros((nh, nk, nl))

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
            
            Qpeak = xl.huq(h, k, l, UB_rotated)  # units of 1/d
            
            if Qpeak[0] > 0.0: continue   # x pointing downstream
            if Qpeak[1] > 0.0: continue   # only detectors on -y side
            
            lenQpeak = math.sqrt(numpy.dot(Qpeak, Qpeak))   # magnitude or length of Qpeak
            
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
            nvector = numpy.array([Qpeak[1], Qpeak[2], Qpeak[0]+1.0/wl])

            # create neutron vector in Q space with SNS coordinates
            nvecQ = numpy.zeros(3)
            nvecQ = numpy.array([Qpeak[1], Qpeak[2], Qpeak[0]+1.0/wl])

            # Determine which detector, if any, the peak hits
            for i in range(dc.nod):
                # First check if scattering vector is within 16 deg if center of detector
                cosAngle = (numpy.dot(det_center[i], nvecQ)) / (dc.detD[i] * (1.0 / wl))
                if cosAngle > 1.0: cosAngle = 1.0
                if cosAngle < -1.0: cosAngle = -1.0
                angle = math.degrees(math.acos(cosAngle))
                if angle < 16.0:
                    factor = numpy.dot(det_center[i], normal[i]) / numpy.dot(nvecQ, normal[i])
                    # nvecR is the neutron vector in real space coordinates
                    nvecR = factor * nvecQ
                    L2 = math.sqrt(numpy.dot(nvecR, nvecR))
                    # det_vector is the vector in the plan of the detector
                    det_vector = nvecR - det_center[i]
                    col = numpy.dot(det_vector, det_base[i])
                    col = 256. * (col / dc.width[i]) + 128.
                    if col < 0.0 or col > 255.: break
                    row = numpy.dot(det_vector, det_up[i])
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
peak_profile = numpy.zeros((numOfPeaks, numSteps))  # array to store 1D peak profiles

UBinv = linalg.inv(UB_rotated)
first_test_dist = (rangeQ / 2.0) * 1.2

print ''

# Read the events from binary file--------------------------------
events_fname = events_directory + run + '_SaveIsawQvector.bin'
input = open(events_fname, 'rb')
print 'The EventsToQ.bin file is ' + events_fname
print ''
numberOfEvents = 0
two_pi = 2.0 * math.pi
while True:
    # if numberOfEvents == 1e06: break
    
    lineString = input.read(12)
    if lineString == "": break
    # if file_type == 0: Qx, Qy, Qz = struct.unpack('<fff', lineString)  # unpack Binary_Little_Endian(PC) data
    Qx, Qy, Qz = struct.unpack('<fff', lineString)  # unpack Binary_Little_Endian(PC) data
    # if file_type == 1: Qx, Qy, Qz = struct.unpack('>fff', lineString)  # unpack Binary_Big_Endian(Java) data
    numberOfEvents = numberOfEvents + 1
    
    if (numberOfEvents % 100000) == 0:
        millionEvents = float(numberOfEvents) / 1000000,
        print '\r%.1f million events' % millionEvents,
        sys.stdout.flush()
    
    qxyz = numpy.zeros(3)
    qxyz[0] = Qx / two_pi
    qxyz[1] = Qy / two_pi
    qxyz[2] = Qz / two_pi

    hklEV = numpy.dot(qxyz, UBinv)
    
    ih = int(round(hklEV[0]))
    if abs(ih) > hmax: continue
    ik = int(round(hklEV[1]))
    if abs(ik) > kmax: continue
    il = int(round(hklEV[2]))
    if abs(il) > lmax: continue
    
    hIndex = ih + hmax
    kIndex = ik + kmax
    lIndex = il + lmax
    peaknum = hklArray[hIndex][kIndex][lIndex]
    if peaknum == 0: continue   # no hkl peak nearby
    
    pki = int(peaknum - 1)
        
    # Do initial test
    Qpeak = [peaks[pki][16][0], peaks[pki][16][1], peaks[pki][16][2]]
    if abs(Qpeak[0] - qxyz[0]) > first_test_dist: continue
    if abs(Qpeak[1] - qxyz[1]) > first_test_dist: continue
    if abs(Qpeak[2] - qxyz[2]) > first_test_dist: continue
    
    Qdata = [qxyz[0], qxyz[1], qxyz[2]]           # data point Q vector
    lenQdata = math.sqrt( numpy.dot(Qdata, Qdata) )  # length of data point Q vector
    lenQpeak = 1.0 / peaks[pki][10]               # 1/dsp
    
    cosAng = numpy.dot(Qpeak, Qdata) / (lenQpeak * lenQdata)
    
    # define a cylinder
    angle = math.acos(cosAng)
    lenPerpendicular = lenQdata * math.sin(angle)
    if lenPerpendicular > radiusQ: continue
    lenOnQpeak = lenQdata * cosAng   # projection of event on the Q vector
    Qdiff = lenOnQpeak - lenQpeak    # corrected sign of Qdiff, 5/3/2012
    if abs(Qdiff) > (0.5 * rangeQ): continue
    
    # add event to appropriate y channel
    xchannel = int(round((Qdiff / deltaQ))) + (numSteps / 2)
    if xchannel < 0 or xchannel > (numSteps - 1): continue
    peak_profile[pki][xchannel] = peak_profile[pki][xchannel] + 1
        
    continue
print '\nnumberOfEvents = ', numberOfEvents

print ''

# Begin writing peaks to the integrate file.
seqn = 0

# Step through each detector
# output.write('\n')
for i in range(dc.nod):
    output.write('0 NRUN DETNUM    CHI    PHI  OMEGA MONCNT\n')
    nrun = int(run)
    output.write('1 %4d %6d %6.2f %6.2f %6.2f %d\n' 
                      % (nrun, dc.detNum[i], chi_deg, phi_deg, omega_deg, monitor_counts))
    output.write('2   SEQN    H    K    L     COL     ROW    CHAN' + 
                      '       L2  2_THETA       AZ        WL        D' + 
                      '   IPK      INTI   SIGI RFLG\n')
    # Step through the list of peaks
    for j in range(numOfPeaks):
        if peaks[j][15] == dc.detNum[i]:
            if sum(peak_profile[j]) == 0: continue    # skip peak with all zero counts
            seqn = seqn + 1
            intI, sigI = integrate_1d_peak(peak_profile[j], peakMin, peakMax, bkgMin, bkgMax)
            output.write(
                '3 %6d %4d %4d %4d %7.2f %7.2f %7.2f %8.3f %8.5f %8.5f %9.6f %8.4f %5d %9.2f %6.2f %4d\n' 
                % (seqn, peaks[j][0], peaks[j][1], peaks[j][2], peaks[j][3], peaks[j][4], peaks[j][5], 
                peaks[j][6], peaks[j][7], peaks[j][8], peaks[j][9], peaks[j][10], peaks[j][11], 
                intI, sigI, peaks[j][14]))
            output.write('8')
            for jj in range(numSteps):
                if jj != 0 and jj%10 == 0: output.write('\n8')
                output.write(' %8d' % peak_profile[j][jj])
            output.write('\n')

output.flush()
output.close()

end = time.time()
elapsed = end - start
print '\nElapsed time is %f seconds.' % elapsed
print '\nAll done!' 






    





