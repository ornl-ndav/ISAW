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

Added an option to weight the counts in each step.
February, 2013

Added an option to use curve_fit or leastsq.
April, 2013
"""

import pylab
import math
import os
import sys
import scipy.special
import scipy.integrate
from time import clock

if os.path.exists('/SNS/TOPAZ/shared/PythonPrograms/PythonLibrary'):
    sys.path.append('/SNS/TOPAZ/shared/PythonPrograms/PythonLibrary')
    sys.path.append('/SNS/software/ISAW/PythonSources/Lib')
elif os.path.exists("/home/ajschultz/PythonPrograms"):
    sys.path.append("/home/ajschultz/PythonPrograms/PythonLibrary")
    sys.path.append("/home/ajschultz/ISAW/PythonSources/Lib")    
else:
    sys.path.append('C:\ISAW_repo\PythonPrograms\PythonLibrary')

import ReduceDictionary    
from read_write_refl_header import *

# import crystal as xl

def gaussian(x, a, sigma, mu, b, c):
    """ Gaussian function on a linear background."""
    sqrt2pi = 2.506628   # sqrt(2.0 * pi)
    f = (a / (sigma * sqrt2pi) * pylab.exp(-0.5 * (x - mu)**2 / sigma**2)) + (b * x) + c
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

# The following 3 functions are required for the leastsq optimize function.
def residuals_func0(p, yobs, x):
    a, sigma, mu, b, c = p
    ycalc = gaussian(x, a, sigma, mu, b, c)
    err = yobs - ycalc
    return err
        
def residuals_func1(p, yobs, x):
    scale, mu, alpha, sigma, slope, constant = p
    ycalc = gauss_1_exp(x, scale, mu, alpha, sigma, slope, constant)
    err = yobs - ycalc
    return err
        
def residuals_func2(p, yobs, x):
    scale, mu, alpha, beta, sigma, slope, constant = p
    ycalc = gauss_2_exps(x, scale, mu, alpha, beta, sigma, slope, constant)
    err = yobs - ycalc
    return err

    
# Begin.................................................

start = clock()

#
# Get the config file name and the run number to process from the command line
#
if (len(sys.argv) < 3):
  print "You MUST give the config file name and run number on the command line"
  exit(0)

config_file_name = sys.argv[1]
run              = sys.argv[2]
nrun = int(run)

#
# Load the parameter names and values from the specified configuration file 
# into a dictionary and set all the required parameters from the dictionary.
#
params_dictionary = ReduceDictionary.LoadDictionary( config_file_name )

expname                     = params_dictionary[ "expname" ]
numSteps                    = int( params_dictionary[ "numSteps" ] )
profile_length              = float( params_dictionary[ "profile_length" ] )
profile_function            = int( params_dictionary[ "profile_function" ] )
optimize_function           = int( params_dictionary[ "optimize_function" ] )
weights                     = int( params_dictionary[ "weights" ] )
reject_intI_zero            = bool( params_dictionary[ "reject_intI_zero" ] )
delta_x                     = float( params_dictionary[ "delta_x" ] )
reject_Gaussian_sigma_zero  = bool( params_dictionary[ "reject_Gaussian_sigma_zero" ] )
verbose                     = bool( params_dictionary[ "verbose" ] )

if verbose != True:
    import warnings
    warnings.filterwarnings('ignore')

if optimize_function == 0:
    from scipy.optimize import curve_fit
    opt_name = 'curve_fit'
else:
    from scipy.optimize import leastsq
    opt_name = 'leastsq'
    
step_size = profile_length / numSteps

# Make a ./plots subdirectory for the profile plot files.
if not os.path.exists('./plots'):
    os.mkdir('./plots')

# Read and write the instrument calibration parameters.
input_fname = expname + '.profiles'
input = open(input_fname, 'r')
output_fname = expname + '_' + run + '.integrate'
output = open(output_fname, 'w')
calibParam = read_write_refl_header(input, output)

# If verbose is True, write additional output to profile_fit.dat
if verbose:
    verbose_out = open('profile_fit_verbose_output_' + run + '.dat', 'w')
    verbose_out.write('  SEQN    H    K    L     COL     ROW    CHAN'
                    + '       L2  2_THETA       AZ        WL        D'
                    + '   IPK      INTI   SIGI RFLG    NRUN DETN')
    if profile_function == 0:
        verbose_out.write('     sigma        mu     slope  constant')
    if profile_function == 1:
        verbose_out.write('       scale        mu     alpha     sigma     slope  constant')
    if profile_function == 2:
        verbose_out.write('       scale        mu     alpha      beta     sigma     slope  constant')

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
        nrun_temp = int(lineList[1])
        if nrun_temp != nrun: continue
        dn = int(lineList[2])
        print 'nrun = %d, dn = %d' % (nrun, dn)
    
    if nrun_temp != nrun: continue

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
    
    # rflg is 10 if using curve_fit, 20 if using leastsq
    rflg = (optimize_function + 1) * 10
    # add 1, 2 or 3 for the profile function type
    rflg = rflg + profile_function + 1
        
    x = range(numSteps)
    x = pylab.array(x)
    
    peak_profile = []
    for i in range(numLines):
        lineString = input.readline()
        lineList = lineString.split()
        for j in range(10):
            peak_profile.append(int(lineList[j+1]))
    
    yobs = pylab.array(peak_profile)
    if weights:
        sig_yobs = pylab.zeros(numSteps)
        for i in range(numSteps):
            if yobs[i] == 0:
                sig_yobs[i] = 100.0
            else:
                sig_yobs[i] = math.sqrt(yobs[i])
            

    # Gaussian profile
    if profile_function == 0:
    
        # popt is an array of the optimized parameters
        # pcov is the covariance matrix
        p0 = pylab.zeros(5)                 # initial values of parameters
        ymax = float(max(yobs))
        p0[0] = ymax * 2.5 * sqrt2pi   # initial value of aG
        p0[1] = 2.5                         # initial value of Gaussian sigma
        p0[2] = yobs.argmax()
            
        try:
            if optimize_function == 0:       # curve_fit
                if weights:
                    popt, pcov = curve_fit(gaussian, x, yobs, p0, sig_yobs)
                else:
                    popt, pcov = curve_fit(gaussian, x, yobs, p0)
            else:                            # leastsq
                popt, success = leastsq(residuals_func0, p0, args=(yobs, x))
            
            intI = popt[0]
            sigma = popt[1]
            mu = popt[2]
            slope = popt[3]
            constant = popt[4]
            
            # Get background counts
            background_total = 0.0
            for istep in range(numSteps):
                yc = gaussian(x[istep], intI, sigma, mu, slope, constant)
                background = slope * x[istep] + constant
                if yc > background:
                    background_total = background_total + background
            sigI = math.sqrt(abs(intI) + background_total)
            # print '%4d %4d %4d %12.4f %12.4f' % (h, k, l, intI, sigI)
            
        except RuntimeError:
            if verbose:
                verbose_out.write('RuntimeError %d %d %d  intI = %.2f  sigma = %.2f' % (h, k, l, intI, sigma))
            continue        
            
        if intI == 0.0:
            print 'No counts for peak %d %d %d' % (h, k, l)
            continue
            
        if verbose:
            print ' %3d %3d %3d %10.2f' % (h, k, l, intI)
            verbose_out.write(
                '\n%6d %4d %4d %4d %7.2f %7.2f %7.2f %8.3f %8.5f %8.5f %9.6f %8.4f %5d %9.2f %6.2f %4d' 
                % (numOfPeaks, h, k, l, col, row, chan, L2, two_theta, az, wl, dsp, ipk, intI, sigI, rflg))
            verbose_out.write('  %6d  %3d  %8.2f  %8.2f  %8.2f  %8.2f'
                % (nrun, dn, sigma, mu, slope, constant))

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
            if optimize_function == 0:
                if weights:
                    popt, pcov = curve_fit(gauss_1_exp, x, yobs, p0, sig_yobs)
                else:
                    popt, pcov = curve_fit(gauss_1_exp, x, yobs, p0)
            else:
                popt, success = leastsq(residuals_func1, p0, args=(yobs, x))
                
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
            # print '%4d %4d %4d %12.4f' % (h, k, l, intI)
                    
        except RuntimeError:
            if verbose:
                verbose_out.write('RuntimeError %d %d %d  intI = %.2f  sigma = %.2f' % (h, k, l, intI, sigma))
            continue   
 
        if verbose:
            print ' %3d %3d %3d %10.2f' % (h, k, l, intI)
            
            verbose_out.write(
                '\n%6d %4d %4d %4d %7.2f %7.2f %7.2f %8.3f %8.5f %8.5f %9.6f %8.4f %5d %9.2f %6.2f %4d' 
                % (numOfPeaks, h, k, l, col, row, chan, L2, two_theta, az, wl, dsp, ipk, intI, sigI, rflg))
            
            verbose_out.write('  %6d  %3d  %10.2f  %8.2f  %8.2f  %8.2f  %8.2f  %8.2f'
                % (nrun, dn, scale, mu, alpha, sigma, slope, constant))
 
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
            if optimize_function == 0:
                if weights:
                    popt, pcov = curve_fit(gauss_2_exps, x, yobs, p0, sig_yobs)
                else:
                    popt, pcov = curve_fit(gauss_2_exps, x, yobs, p0)
            else:
                popt, success = leastsq(residuals_func2, p0, args=(yobs, x))
                    
            scale = popt[0]
            mu = popt[1]
            alpha = popt[2]
            beta = popt[3]
            sigma = popt[4]
            slope = popt[5]
            constant = popt[6]
            
            intI, sig_intI = scipy.integrate.quad(gauss_2_exps, 0, numSteps-1, 
                args=(scale, mu, alpha, beta, sigma, 0.0, 0.0))
                                     
            if intI < 0.0: continue
                                     
            # Get background counts
            background_total = 0.0
            for istep in range(numSteps):
                yc = gauss_2_exps(x[istep], scale, mu, alpha, beta, sigma, slope, constant)
                background = slope * x[istep] + constant
                if yc > background:
                    background_total = background_total + background
            sigI = math.sqrt(abs(intI) + background_total)
            # print '%4d %4d %4d %12.4f' % (h, k, l, intI)
                    
        except RuntimeError:
            if verbose:
                verbose_out.write('RuntimeError %d %d %d  intI = %.2f  sigma = %.2f' % (h, k, l, intI, sigma))
            continue        

        if verbose:
            print ' %3d %3d %3d %10.2f' % (h, k, l, intI)
            
            verbose_out.write(
                '\n%6d %4d %4d %4d %7.2f %7.2f %7.2f %8.3f %8.5f %8.5f %9.6f %8.4f %5d %9.2f %6.2f %4d' 
                % (numOfPeaks, h, k, l, col, row, chan, L2, two_theta, az, wl, dsp, ipk, intI, sigI, rflg))

            verbose_out.write('  %6d  %3d  %10.2f  %8.2f  %8.2f  %8.2f  %8.2f  %8.2f  %8.2f'
                % (nrun, dn, scale, mu, alpha, beta, sigma, slope, constant))
            
    #       
    # Write to the integrate file ----------------------------------
    #
    # First check for rejections
    if reject_intI_zero:
        if intI <= 0.0:
            if verbose:
                verbose_out.write('  ***Rejected intI <= 0.0')
            continue              # skip if intI == 0.0 or negative
    if abs(mu - 50.0) >= delta_x:
        if verbose:
            verbose_out.write('  ***Rejected abs(mu-50.0) >= delta_x')
        continue    # skip if peak is far from center
    if reject_Gaussian_sigma_zero:
        if sigma  <= 0.01:
            if verbose:
                verbose_out.write('  ***Rejected sigma <= 0.01')
            continue               # skip of Gaussian sigma <= 0
    
    output.write(
        '3 %6d %4d %4d %4d %7.2f %7.2f %7.2f %8.3f %8.5f %8.5f %9.6f %8.4f %5d %9.2f %6.2f %4d\n' 
        % (numOfPeaks, h, k, l, col, row, chan, L2, two_theta, az, wl, dsp, ipk, intI, sigI, rflg))

    #
    # Begin plot -------------------------------
    #    
    xcalc = []
    ycalc = []
    for i in range(100 * len(yobs)):
        xcalc.append(float(i)/100.0)
        if profile_function == 0:
            ycalc.append(gaussian(xcalc[i], intI, sigma, mu, slope, constant))
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
    filename = './plots/Profile_fit_%d_%d_%d_%d' % (h, k, l, nrun)
    
    Q_calc = 2.0 * math.pi / dsp
    delta_Q = (mu - (0.5 * numSteps)) * step_size
    Q_obs = Q_calc + delta_Q
    dsp_obs = 2.0 * math.pi / Q_obs
    delta_d = dsp - dsp_obs
    textString = 'dsp calc = %.4f\ndelta_d = %.4f' % (dsp, delta_d)
    pylab.figtext(0.65, 0.55, textString, fontsize='small', family='monospace')
            
    textString = 'run = %d\ndetector = %d\nopt_fun = %s' % (nrun, dn, opt_name)
    pylab.figtext(0.65, 0.45, textString, fontsize='small', family='monospace')
    
    textString = 'col = %6.2f\nrow = %6.2f' % (col, row)
    pylab.figtext(0.65, 0.35, textString, fontsize='small', family='monospace')

    if profile_function == 0:

        textString = 'f = (a/(sig * sqrt(2*pi)) * exp(-0.5 * (x - mu)**2 / sig**2)) + (b * x) + c'
        pylab.figtext(0.5, 0.85, textString, horizontalalignment='center', fontsize='small')

        textString = 'a = %.2f(%.2f)\nsig = %.2f\nmu = %.2f\nb = %.2f\nc = %.2f\n' % (
            intI, sigI, sigma, mu, slope, constant)
        pylab.figtext(0.65, 0.65, textString, fontsize='small', family='monospace')
                
            
    if profile_function == 1:

        textString = 'Convolution of one \nexponential with a Gaussian.'
        pylab.figtext(0.15, 0.80, textString, fontsize='small')

        textString = 'scale = %.2f\nmu = %.2f\nalpha = %.2f\nsigma = %.2f\n\nintI = %.2f\nsigI = %.2f' % (
            scale, mu, alpha, sigma, intI, sigI)            
        pylab.figtext(0.65, 0.65, textString, fontsize='small', family='monospace')
            
        
    if profile_function == 2:

        textString = 'Convolution of two back-to-back \nexponentials with a Gaussian.'
        pylab.figtext(0.15, 0.80, textString, fontsize='small')

        textString = 'scale = %.2f\nmu = %.2f\nalpha = %.2f\nbeta = %.2f\nsigma = %.2f\n\nintI = %.2f\nsigI = %.2f' % (
            scale, mu, alpha, beta, sigma, intI, sigI)            
        pylab.figtext(0.65, 0.65, textString, fontsize='small', family='monospace')
            
    
    pylab.savefig(filename)
    pylab.clf()

output.flush()
output.close()
verbose_out.flush()
verbose_out.close()

print '\nNumber of peaks = %d' % numOfPeaks 
end = clock()
elapsed = end - start
print '\nElapsed time is %f seconds.' % elapsed
print '\nAll done!' 






    





