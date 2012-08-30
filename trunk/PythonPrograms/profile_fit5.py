#!/usr/bin/env python
"""
Fit function to peak profile.
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

profile_fit4b.py
Includes the GSAS profile function 1 but with one exponential.
June 8, 2012

profile_fit4b_mr.py
mr is for mulitple runs. This version reads the profile file from 
get_profile_data_multiple_runs.py.
August, 2012

profile_fit5.py
Includes all features of profile_fit4b_mr.py plus the option of the
full GSAS profile function 1 which is a convolution of two
back-to-back exponentials with a Gaussian.
August, 2012
"""

import pylab
import math
import os
import scipy.special
import scipy.integrate
# from numpy import linalg
from time import clock
# from read_detcal import *
from scipy.optimize import curve_fit

from read_write_refl_header import *


# import crystal as xl

def gaussian(x, a, sig, mu, b, c):
    """ Gaussian function on a linear background."""
    sqrt2pi = 2.506628   # sqrt(2.0 * pi)
    f = (a / (sig * sqrt2pi) * pylab.exp(-0.5 * (x - mu)**2 / sig**2)) + (b * x) + c
    return f

def gauss_1_exp(x, scale, mu, alpha, sigma, slope, constant):
    """ From the GSAS manual, TOF profile funtion 1, page 143:
    This function is the result of convoluting one
    exponential with a Gaussian."""
    
    u = (alpha / 2.0) * (alpha * sigma**2 + 2.0 * (x - mu))
    y = (alpha * sigma**2 + (x - mu)) / math.sqrt(2.0 * sigma**2)
    
    a = pylab.exp(u)
    b = scipy.special.erfc(y)
    
    H = (scale * a * b) + (slope * x) + constant
    
    return H

    
def gauss_2_exps(x, scale, mu, alpha, beta, sigma, slope, constant):
    """ From the GSAS manual, TOF profile funtion 1, page 143:
    This function is the result of convoluting two back-to-back
    exponentials with a Gaussian."""
    
    # delta is the difference in steps between the 
    # peak max (mu) and the profile point (x).
    delta = x - mu
    
    u = (alpha / 2.0) * ((alpha * sigma**2) + (2.0 * delta))
    v = (beta / 2.0) * ((beta * sigma**2) - (2.0 * delta))
    
    y = (alpha * sigma**2 + delta) / math.sqrt(2.0 * sigma**2)
    z = (beta * sigma**2 - delta) / math.sqrt(2.0 * sigma**2)
    
    a = pylab.exp(u)
    b = scipy.special.erfc(y)
    c = pylab.exp(v)
    d = scipy.special.erfc(z)
    
    H = scale * ((a * b) + (c * d)) + (slope * x) + constant
    
    return H

# Begin.................................................

start = clock()

# Make a ./plots subdirectory for the profile plot files.
if not os.path.exists('./plots'):
    os.mkdir('./plots')

# Open and read the user input file
user_input = open('profile_fit5.inp', 'r')
user_param = []
while True:
    lineString = user_input.readline()
    lineList = lineString.split()
    if len(lineList) == 0: break
    user_param.append(lineList[0])
expname = user_param[0]
numSteps = int(user_param[1])
profile_length = float(user_param[2])
step_size = profile_length / numSteps
# profile_function:
#    0 = Gaussian
#    1 = convolution of one exponential with Gaussian
#    2 = convolution of two back-to-back exponentials with a Gaussian
profile_function = int(user_param[3]) 

# Read and write the instrument calibration parameters.
input_fname = expname + '.profiles'
input = open(input_fname, 'r')
output_fname = expname + '.integrate'
output = open(output_fname, 'w')
calibParam = read_write_refl_header(input, output)

# peaks = []

print ''
sqrt2pi = 2.506628   # sqrt(2.0 * pi)

numLines = int(numSteps/10)

numOfPeaks = 0

# Begin reading and fitting the profiles.
while True:

    lineString = input.readline()
    lineList = lineString.split()
    if len(lineList) == 0: break
    
    if lineList[0] == '1':
        nrun = int(lineList[1])
        dn = int(lineList[2])
        print 'nrun = %d, dn = %d' % (nrun, dn)
    
    if lineList[0] != '3':
        output.write(lineString)    
        continue
    
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
        
    x = range(numSteps)
    x = pylab.array(x)
    
    peak_profile = []
    for i in range(numLines):
        lineString = input.readline()
        lineList = lineString.split()
        for j in range(10):
            peak_profile.append(int(lineList[j+1]))
    
    yobs = pylab.array(peak_profile)

    # Gaussian profile
    if profile_function == 0:
    
        # popt is an array of the optimized parameters
        # pcov is the covariance matrix
        p0 = pylab.zeros(5)                 # initial values of parameters
        ymax = float(max(yobs))
        p0[0] = ymax * 2.5 * sqrt2pi   # initial value of aG
        p0[1] = 2.5                         # initial value of sigG
        p0[2] = yobs.argmax()
            
        try:
            popt, pcov = curve_fit(gaussian, x, yobs, p0)
            aG = popt[0]
            sigG = popt[1]
            muG = popt[2]
            bG = popt[3]
            cG = popt[4]
            
            # intI, sigI = scipy.integrate.quad(gaussian, 0, numSteps-1, 
                # args=(aG, sigG, muG, 0.0, 0.0))
                
            # print intI, sigI

            
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
        else:
            # output.write('%4d %4d %4d %12.4f  %12.4f  %12.4f  %12.4f  %12.4f %12.4f  %12.4f  %12.4f  %12.4f  %12.4f\n' 
                # %  (h, k, l, aG, sigG, muG, bG, cG, sig_aG, sig_sigG, sig_muG, 
                # sig_bG, sig_cG))
            print '%4d %4d %4d %12.4f' % (h, k, l, aG)
 
        intI = aG
        sigI = sig_aG
 
    # Convolution of Gaussian and one exponential
    if profile_function == 1:

        # popt is an array of the optimized parameters
        # pcov is the covariance matrix
        p0 = pylab.zeros(6)                 # initial values of parameters
        ymax = float(max(yobs))
        p0[0] = ymax                        # initial value of scale
        p0[1] = yobs.argmax()               # initial value of mu for exponential decay
        p0[2] = 1.0                         # initial value of alpha
        p0[3] = 1.0                         # initial value sigma
        p0[4] = 0.0                         # initial value of background slope
        p0[5] = 0.0                         # initial value of background constants
        
        try:
            popt, pcov = curve_fit(gauss_1_exp, x, yobs, p0)
            scale = popt[0]
            mu = popt[1]
            alpha = popt[2]
            sigma = popt[3]
            slope = popt[4]
            constant = popt[5]
            
            intI, sig_intI = scipy.integrate.quad(gauss_1_exp, 0, numSteps-1, 
                args=(scale, mu, alpha, sigma, 0.0, 0.0))
                                        
            # Get background counts
            background_total = 0.0
            for istep in range(numSteps):
                yc = gauss_1_exp(x[istep], scale, mu, alpha, sigma, slope, constant)
                background = slope * x[istep] + constant
                if yc > background:
                    background_total = background_total + background
            sigI = math.sqrt(abs(intI) + background_total)
            print '%4d %4d %4d %12.4f' % (h, k, l, intI)
                    
        except RuntimeError:
            print 'RuntimeError for peak %d %d %d' % (h, k, l)
            continue   
 
 
    # Convolution of Gaussian and two back-to-back exponentials
    if profile_function == 2:

        # popt is an array of the optimized parameters
        # pcov is the covariance matrix
        p0 = pylab.zeros(7)                 # initial values of parameters
        ymax = float(max(yobs))
        p0[0] = ymax                        # initial value of scale
        p0[1] = yobs.argmax()               # initial value of mu
        p0[2] = 1.0                         # initial value of alpha
        p0[3] = 1.0                         # initial value of beta
        p0[4] = 1.0                         # initial value sigma
        p0[5] = 0.0                         # initial value of background slope
        p0[6] = 0.0                         # initial value of background constants
        
        try:
            popt, pcov = curve_fit(gauss_2_exps, x, yobs, p0)
            scale = popt[0]
            mu = popt[1]
            alpha = popt[2]
            beta = popt[3]
            sigma = popt[4]
            slope = popt[5]
            constant = popt[6]
            
            intI, sig_intI = scipy.integrate.quad(gauss_2_exps, 0, numSteps-1, 
                args=(scale, mu, alpha, beta, sigma, 0.0, 0.0))
                                        
            # Get background counts
            background_total = 0.0
            for istep in range(numSteps):
                yc = gauss_2_exps(x[istep], scale, mu, alpha, beta, sigma, slope, constant)
                background = slope * x[istep] + constant
                if yc > background:
                    background_total = background_total + background
            sigI = math.sqrt(abs(intI) + background_total)
            print '%4d %4d %4d %12.4f' % (h, k, l, intI)
                    
        except RuntimeError:
            print 'RuntimeError for peak %d %d %d' % (h, k, l)
            continue        

    # Write to the integrate file
    output.write(
        '3 %6d %4d %4d %4d %7.2f %7.2f %7.2f %8.3f %8.5f %8.5f %9.6f %8.4f %5d %9.2f %6.2f %4d\n' 
        % (numOfPeaks, h, k, l, col, row, chan, L2, two_theta, az, wl, dsp, ipk, intI, sigI, rflg))

    # Begin plot        
    xcalc = []
    ycalc = []
    for i in range(100 * len(yobs)):
        xcalc.append(float(i)/100.0)
        if profile_function == 0:
            ycalc.append(gaussian(xcalc[i], aG, sigG, muG, bG, cG))
        if profile_function == 1:
            ycalc.append(gauss_1_exp(xcalc[i], scale, mu, alpha, sigma, slope, constant))
        if profile_function == 2:
            ycalc.append(gauss_2_exps(xcalc[i], scale, mu, alpha, beta, sigma, slope, constant))
    
    pylab.plot(xcalc, ycalc)
    
    pylab.plot(x, yobs, 'g^')
    
    pylab.xlabel('Q channel, 2pi/d')
    pylab.ylabel('Counts')
    pylab.grid(True)

    plotTitle = '%d %d %d' % (h, k, l)
    pylab.title(plotTitle)
    
    if profile_function == 0:

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
            
    if profile_function == 1:

        textString = 'Convolution of one \nexponential with a Gaussian.'
        pylab.figtext(0.15, 0.80, textString, fontsize='small')

        if scale > 0.0:
            textString = 'scale = %.2f\nmu = %.2f\nalpha = %.2f\nsigma = %.2f\n\nintI = %.2f\nsigI = %.2f' % (
                scale, mu, alpha, sigma, intI, sigI)            
            pylab.figtext(0.65, 0.65, textString, family='monospace')
            
            Q_calc = 2.0 * math.pi / dsp
            delta_Q = (mu - (0.5 * numSteps)) * step_size
            Q_obs = Q_calc + delta_Q
            dsp_obs = 2.0 * math.pi / Q_obs
            delta_d = dsp - dsp_obs
            textString = 'dsp calc = %.4f\ndelta_d = %.4f' % (dsp, delta_d)
            pylab.figtext(0.65, 0.55, textString, family='monospace')
            
        textString = 'run = %d\ndetector = %d' % (nrun, dn)
        pylab.figtext(0.65, 0.45, textString, family='monospace')

        filename = './plots/Profile_fit_%d_%d_%d' % (h, k, l)
        
    if profile_function == 2:

        textString = 'Convolution of two back-to-back \nexponentials with a Gaussian.'
        pylab.figtext(0.15, 0.80, textString, fontsize='small')

        if scale > 0.0:
            textString = 'scale = %.2f\nmu = %.2f\nalpha = %.2f\nbeta = %.2f\nsigma = %.2f\n\nintI = %.2f\nsigI = %.2f' % (
                scale, mu, alpha, beta, sigma, intI, sigI)            
            pylab.figtext(0.65, 0.65, textString, family='monospace')
            
            Q_calc = 2.0 * math.pi / dsp
            delta_Q = (mu - (0.5 * numSteps)) * step_size
            Q_obs = Q_calc + delta_Q
            dsp_obs = 2.0 * math.pi / Q_obs
            delta_d = dsp - dsp_obs
            textString = 'dsp calc = %.4f\ndelta_d = %.4f' % (dsp, delta_d)
            pylab.figtext(0.65, 0.55, textString, family='monospace')
            
        textString = 'run = %d\ndetector = %d' % (nrun, dn)
        pylab.figtext(0.65, 0.45, textString, family='monospace')

        filename = './plots/Profile_fit_%d_%d_%d' % (h, k, l)
            
    pylab.savefig(filename)
    pylab.clf()

print '\nNumber of peaks = %d' % numOfPeaks 
end = clock()
elapsed = end - start
print '\nElapsed time is %f seconds.' % elapsed
print '\nAll done!' 






    





