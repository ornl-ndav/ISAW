#!/usr/bin/env python
"""
Fit gaussian to peak profile.
A. J. Schultz, April 2012

This version reads the output from qplot5.py to obtain the counts.
"""

from scipy.optimize import curve_fit
import pylab
import math
import os

def gaussian(x, a, sig, mu, b, c):
    """ Gaussian function on a linear background."""
    sqrt2pi = 2.506628   # sqrt(2.0 * pi)
    f = (a / (sig * sqrt2pi) * pylab.exp(-0.5 * (x - mu)**2 / sig**2)) + (b * x) + c
    return f

# Open output file of the Gaussian coefficients.
output = open( 'gaussian_coefficients.dat', 'w' )

# Make a ./plots subdirectory for the profile plot files.
if not os.path.exists('./plots'):
    os.mkdir('./plots')

# sqrt(2.0 * pi)
sqrt2pi = 2.506628   

# open input file
input = open( '3681qplot_qplot.dat', 'r' )
    
# pylab.ion()

while True:

    line = input.readline()
    lineList = line.split()
    if len(lineList) == 0: break
    
    h = int(lineList[0])
    k = int(lineList[1])
    l = int(lineList[2])
    
    x = []
    yobs = []
    
    for i in range(4):       
        line = input.readline()
        lineList = line.split()
        
        for j in range(len(lineList)):
            yobs.append( float( lineList[j] ) )
        
    x = range(len(yobs))
    x = pylab.array(x)
    yobs = pylab.array(yobs)
    
    # popt is an array of the optimized parameters
    # pcov is the covariance matrix
    p0 = pylab.zeros(5)    # initial values of parameters
    p0[0] = max(yobs) * 2.5 * sqrt2pi
    p0[1] = 2.5
    p0[2] = 0.5 * len(yobs)
    
    if h == 4 and k == -8 and l == -3:
        print h, k, l
        print x
        print yobs
        print p0
    popt, pcov = curve_fit(gaussian, x, yobs, p0)
    
    a = popt[0]
    sig = popt[1]
    mu = popt[2]
    b = popt[3]
    c = popt[4]

    if a == 0.0:
        siga = 999.0
        sigsig = 999.0
        sigmu = 999.0
        sigb = 999.0
        sigc = 999.0
    else:
        siga = math.sqrt(pcov[0][0])
        sigsig = math.sqrt(pcov[1][1])
        sigmu = math.sqrt(pcov[2][2])
        sigb = math.sqrt(pcov[3][3])
        sigc = math.sqrt(pcov[4][4])
    
    output.write('%4d %4d %4d %12.4f  %12.4f  %12.4f  %12.4f  %12.4f %12.4f  %12.4f  %12.4f  %12.4f  %12.4f\n' 
        %  (h, k, l, a, sig, mu, b, c, siga, sigsig, sigmu, sigb, sigc))
    print '%4d %4d %4d %12.4f' % (h, k, l, a)

    xcalc = []
    ycalc = []
    for i in range(100 * len(yobs)):
        xcalc.append(float(i)/100.0)
        ycalc.append(gaussian(xcalc[i], a, sig, mu, b, c))
    
    # pylab.ion()    
    pylab.plot(xcalc, ycalc)
    pylab.plot(x, yobs, '+')
    pylab.xlabel('Q channel, 2pi/d')
    pylab.ylabel('Counts')

    plotTitle = '%d %d %d' % (h, k, l)
    pylab.title(plotTitle)
    
    textString = 'f = (a/(sig * sqrt(2*pi)) * exp(-0.5 * (x - mu)**2 / sig**2)) + (b * x) + c'
    pylab.figtext(0.5, 0.85, textString, horizontalalignment='center', fontsize='small')

    textString = 'a = %.2f(%.2f)\nsig = %.2f(%.2f)\nmu = %.2f(%.2f)\nb = %.2f(%.2f)\nc = %.2f(%.2f)\n' % (
        a, siga, sig, sigsig, mu, sigmu, b, sigb, c, sigc)
    pylab.figtext(0.65, 0.65, textString, family='monospace')
    
    filename = './plots/Profile_fit_%d_%d_%d' % (h, k, l)
    pylab.savefig(filename)
    pylab.close()
    
    # pylab.show()
    # raw_input('Type ENTER to continue.')
    
    # pylab.show()
    # pylab.ioff()
    # pylab.ion()


