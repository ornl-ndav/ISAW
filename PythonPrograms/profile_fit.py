#!/usr/bin/env python
"""
Fit function to peak profile.
A. J. Schultz, April 2012
"""

import numpy as np
from scipy.optimize import curve_fit
from pylab import *

def func(x, a, sig, mu, b):
    sqrt2pi = 2.506628   # sqrt(2.0 * pi)
    f = (a / (sig * sqrt2pi) * exp(-0.5 * (x - mu)**2 / sig**2)) + b
    return f

output = open( 'gaussian_coefficients.dat', 'w' )

sqrt2pi = 2.506628   # sqrt(2.0 * pi)
    
for i in range(19):

    # open input file
    input = open( 'input.dat', 'r' )

    x = []
    yobs = []
    
    line = input.readline() # read first line
    print line
    
    for line in input:
        # line = input.readline()
        lineList = line.split()
        # if len(lineList) == 0: break
        for i in range(len(lineList)):
            yobs.append( float( lineList[i] ) )
    
    x = range(len(yobs))
    x = np.array(x)
    yobs = np.array(yobs)
    print max(yobs)
    
    print x
    print yobs
    
    # popt is an array of the optimized parameters
    # pcov is the covariance matrix
    p0 = [1.0, 1.0, 1.0, 1.0]
    p0[0] = max(yobs) * 2.5 * sqrt2pi
    p0[2] = 0.5 * len(yobs)
    popt, pcov = curve_fit(func, x, yobs, p0)
    a = popt[0]
    sig = popt[1]
    mu = popt[2]
    b = popt[3]

    # print '%7.4f  %7.4f  %7.4f' %  (a, b, c)
    print '%7.4f  %7.4f  %7.4f  %7.4f' %  (a, sig, mu, b)
    # print pcov
    
    # output.write('[ 1.000,  %7.4f,  %7.4f,  %7.4f,  %7.4f ],\n' %  (a, b, c, d))

    xcalc = []
    for i in range(100 * len(yobs)):
        xcalc.append(float(i)/100.0)
        
    ycalc = func(xcalc, a, sig, mu, b)

    plot(xcalc, ycalc)
    plot(x, yobs, '+')
    show()
    
    input.close()
    exit()

# show()

