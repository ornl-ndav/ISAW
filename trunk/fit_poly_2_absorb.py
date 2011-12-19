#!/usr/bin/env python
"""
Fit muR vs absorption to a polynomial with an intercept of 1.0.
A. J. Schultz, August 2011
"""

import numpy as np
from scipy.optimize import curve_fit
from pylab import *

def func(x, a, b, c, d):
    f = 1.0 + a*x + b*x**2 + c*x**3 + d*x**4
    return f

output = open( 'polynomial_coefficients.dat', 'w' )
    
for i in range(19):

    # open input file
    # input = open( 'absorption_sphere.dat', 'r' )
    input = open( 'absorption_rod.dat', 'r' )

    x = []
    yobs = []

    while True:
        lineString = input.readline()
        lineList = lineString.split()
        if len(lineList) == 0: break     # check for the end-of-file
        x.append( float( lineList[0] ) )
        yobs.append( float( lineList[i+1] ) )

    x = np.array(x)
    yobs = np.array(yobs)
    
    # popt is an array of the optimized parameters
    # pcov is the covariance matrix
    popt, pcov = curve_fit(func, x, yobs)
    a = popt[0]
    b = popt[1]
    c = popt[2]
    d = popt[3]

    # print '1.0  %7.4f  %7.4f  %7.4f' %  (a, b, c)
    print '1.0  %7.4f  %7.4f  %7.4f  %7.4f' %  (a, b, c, d)
    # print pcov
    
    output.write('[ 1.000,  %7.4f,  %7.4f,  %7.4f,  %7.4f ],\n' %  (a, b, c, d))

    ycalc = func(x, a, b, c, d)

    plot(x, ycalc)
    plot(x, yobs, '+')
    
    input.close()

show()

