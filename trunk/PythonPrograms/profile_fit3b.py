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

profile_fit3b.py
Reads the output from get_profile_data.py rather than the EventsToQ file.
May 30, 2012


This is a test.
"""


import pylab
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
user_input = open('profile_fit3b.inp', 'r')
user_param = []
while True:
    lineString = user_input.readline()
    lineList = lineString.split()
    if len(lineList) == 0: break
    user_param.append(lineList[0])
nrun = int(user_param[0])
expname = user_param[1]
detcal_fname = user_param[2]
numSteps = int(user_param[3])

# Read and write the instrument calibration parameters.
detcal_input = open(detcal_fname, 'r')
filename = expname + '.integrate'
output_refl = open(filename, 'w')
dc = ReadDetCal()
first_line = '# qplot integrate file'
dc.read_detcal(detcal_input, output_refl, first_line)

peaks = []

print ''
sqrt2pi = 2.506628   # sqrt(2.0 * pi)

filename = expname + '_profile_data.dat'
input = open(filename, 'r')

filename = expname + '_profile_parameters.dat'
output = open(filename, 'w')

numLines = int(numSteps/10)

numOfPeaks = 0

while True:

    lineString = input.readline()    
    lineList = lineString.split()
    if len(lineList) == 0: break
    
    numOfPeaks = numOfPeaks + 1
    
    
    seqnum = int(lineList[1])
    
    h = int(lineList[2])
    k = int(lineList[3])
    l = int(lineList[4])
    col = float(lineList[5])
    row = float(lineList[6])
    chan = float(lineList[7])
    L2 = float(lineList[8])
    two_theta = float(lineList[9])
    az = float(lineList[10])
    wl = float(lineList[11])
    dsp = float(lineList[12])
    ipk = int(lineList[13])
    intI = float(lineList[14])
    sigI = float(lineList[15])
    rflg = int(lineList[16])
    dn = int(lineList[17])
    peaks.append([h, k, l, col, row, chan, L2, two_theta, az, 
                wl, dsp, ipk, intI, sigI, rflg, dn])
    
    x = range(numSteps)
    x = pylab.array(x)
    
    peak_profile = []
    for i in range(numLines):
        lineString = input.readline()
        lineList = lineString.split()
        for j in range(10):
            peak_profile.append(int(lineList[j]))
    
    yobs = pylab.array(peak_profile)

    # popt is an array of the optimized parameters
    # pcov is the covariance matrix
    p0 = pylab.zeros(5)                 # initial values of parameters
    ymax = float(max(yobs))
    p0[0] = ymax * 2.5 * sqrt2pi   # initial value of aG
    p0[1] = 2.5                         # initial value of sigG
    p0[2] = 0.5 * len(yobs)             # initial value of muG, middle of x range
        
    try:
        popt, pcov = curve_fit(gaussian, x, yobs, p0)
        aG = popt[0]
        sigG = popt[1]
        muG = popt[2]
        bG = popt[3]
        cG = popt[4]
    except RuntimeError:
        print 'RuntimeError for peak %d %d %d' % (h, k, l)
        continue        
        
    if aG == 0.0:
        print 'No counts for peak %d %d %d' % (h, k, l)
        continue
    else:
        sig_aG = math.sqrt(pcov[0][0])
        sig_sigG = math.sqrt(pcov[1][1])
        sig_muG = math.sqrt(pcov[2][2])
        sig_bG = math.sqrt(pcov[3][3])
        sig_cG = math.sqrt(pcov[4][4])

    if sig_sigG > sigG:
        print 'Rejected: sig error greater than sig for peak %d %d %d' % (h, k, l)
        # continue
    else:
        peaks[numOfPeaks-1][12] = aG
        peaks[numOfPeaks-1][13] = sig_aG
        
        output.write('%4d %4d %4d %12.4f  %12.4f  %12.4f  %12.4f  %12.4f %12.4f  %12.4f  %12.4f  %12.4f  %12.4f\n' 
            %  (h, k, l, aG, sigG, muG, bG, cG, sig_aG, sig_sigG, sig_muG, 
            sig_bG, sig_cG))
        print '%4d %4d %4d %12.4f' % (h, k, l, aG)

    xcalc = []
    ycalc = []
    for i in range(100 * len(yobs)):
        xcalc.append(float(i)/100.0)
        ycalc.append(gaussian(xcalc[i], aG, sigG, muG, bG, cG))
    
    pylab.plot(xcalc, ycalc)
    pylab.plot(x, yobs, 'g^')
    
    pylab.xlabel('Q channel, 2pi/d')
    pylab.ylabel('Counts')

    plotTitle = '%d %d %d' % (h, k, l)
    pylab.title(plotTitle)
    
    textString = 'f = (a/(sig * sqrt(2*pi)) * exp(-0.5 * (x - mu)**2 / sig**2)) + (b * x) + c'
    pylab.figtext(0.5, 0.85, textString, horizontalalignment='center', fontsize='small')

    textString = 'a = %.2f(%.2f)\nsig = %.2f(%.2f)\nmu = %.2f(%.2f)\nb = %.2f(%.2f)\nc = %.2f(%.2f)\n' % (
        aG, sig_aG, sigG, sig_sigG, muG, sig_muG, bG, sig_bG, cG, 
        sig_cG)
    pylab.figtext(0.65, 0.65, textString, family='monospace')
    
    if sig_sigG > sigG:
        filename = './plots/Rejected_%d_%d_%d' % (h, k, l)
    else:
        filename = './plots/Profile_fit_%d_%d_%d' % (h, k, l)
            
    pylab.savefig(filename)
    pylab.clf()


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






    





