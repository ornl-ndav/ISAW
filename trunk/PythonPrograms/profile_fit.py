#!/usr/bin/env python
"""
Fit function to peak profile.
A. J. Schultz, April 2012
"""

from scipy.optimize import curve_fit
import pylab

def func(x, a, sig, mu, b, c):
    sqrt2pi = 2.506628   # sqrt(2.0 * pi)
    f = (a / (sig * sqrt2pi) * pylab.exp(-0.5 * (x - mu)**2 / sig**2)) + (b * x) + c
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
        lineList = line.split()
        for i in range(len(lineList)):
            yobs.append( float( lineList[i] ) )
    
    x = range(len(yobs))
    x = pylab.array(x)
    yobs = pylab.array(yobs)
    
    # popt is an array of the optimized parameters
    # pcov is the covariance matrix
    p0 = pylab.zeros(5)    # initial values
    p0[0] = max(yobs) * 2.5 * sqrt2pi
    p0[1] = 2.5
    p0[2] = 0.5 * len(yobs)
    popt, pcov = curve_fit(func, x, yobs, p0)
    a = popt[0]
    sig = popt[1]
    mu = popt[2]
    b = popt[3]
    c = popt[4]

    # print '%7.4f  %7.4f  %7.4f' %  (a, b, c)
    print '%7.4f  %7.4f  %7.4f  %7.4f  %7.4f' %  (a, sig, mu, b, c)
    print pcov
    
    # output.write('[ 1.000,  %7.4f,  %7.4f,  %7.4f,  %7.4f ],\n' %  (a, b, c))

    xcalc = []
    ycalc = []
    for i in range(100 * len(yobs)):
        xcalc.append(float(i)/100.0)
        ycalc.append(func(xcalc[i], a, sig, mu, b, c))
        
    pylab.plot(xcalc, ycalc)
    pylab.plot(x, yobs, '+')
    pylab.show()
    
    input.close()
    exit()

# pylab.show()

