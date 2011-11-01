#!/usr/bin/env python
"""
Plot the intensity variation for all of the detectors.
A. J. Schultz, April 2011
"""

from pylab import *
import numpy as np


# open data file
dataInput = open( 'Det_intensity_3023.dat', 'r' )


nod = 14     # the number of detectors

x = np.arange( 1, 33 )
y = np.arange( 1, 33 )

for i in range( nod ):
    # set arrays to zero
    counts = np.zeros(( 32, 32 ))
    
    plotTitle = dataInput.readline()   # read "Detector Bank" line
        
    for j in range(32):
        for k in range(32):
            lineString = dataInput.readline()
            lineList = lineString.split()
            if len(lineList) == 0: break     # check for the end-of-file
            counts[j][k] = float( lineList[2] )
     
    contourf( x, y, counts, 20 )
    colorbar()
    title( plotTitle )
    
    lineList = plotTitle.split()
    filename = 'Detector' + lineList[6] + '.png'
    savefig( filename )
    
    show()

dataInput.close()

show()
