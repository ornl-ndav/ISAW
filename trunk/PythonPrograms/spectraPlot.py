#!/usr/bin/env python
"""
Plot the spectra for all of the detectors.
A. J. Schultz, April 2011
"""

from pylab import *
from Tkinter import *
import tkFileDialog
import tkSimpleDialog

ion() # enables interactive mode

root = Tk()
root.withdraw()

print
print '*** Select the spectrum file to plot. ***'
print
filename = tkFileDialog.askopenfilename( title = 'FILE NAME OF THE SPECTRUM FILE')
print

# open spectrum file
specInput = open( filename, 'r' )

print '*** Input the plot title, which will also be the saved file name prefix. ***'
# plotTitle = raw_input('Input the plot title: ')
plotTitle = tkSimpleDialog.askstring(title = 'Plot Title', 
    prompt = 'Input plot title',
    initialvalue = 'Spectra')
print

for i in range(8):   # skip the first 8 lines
    lineString = specInput.readline()

lineString = specInput.readline()   # read "Bank 1" line

i = 0
while True:

    # set arrays to zero
    wavelength = []
    counts = []
    
    if i < 9:
        print 'Reading spectrum for ' + lineString[0:20]
    else:
        print 'Reading spectrum for ' + lineString[0:21]
    
    lineList = lineString.split()
    DetNum = lineList[3]
    
    while True:
        lineString = specInput.readline()
        lineList = lineString.split()
        if len(lineList) == 0: break     # check for the end-of-file
        if lineList[0] == 'Bank': break  # check for the start of a new spectrum
        wavelength.append( float( lineList[2] ) )
        counts.append( float( lineList[1] ) )
        
#!!!! The next lines can be commented or uncommented to vary what is plotted. !!!!        
        
    plot( wavelength, counts, label=DetNum )     # plot all spectra
    
    # if i < 7: plot( wavelength, counts, label=DetNum )   # plot first seven spectra
    # if i > 6: plot( wavelength, counts, label=DetNum )   # plot last seven spectra
    
    # if DetNum == '36': plot( wavelength, counts, label=DetNum )   # plot selected spectra
    # if DetNum == '37': plot( wavelength, counts, label=DetNum )
    # if DetNum == '38': plot( wavelength, counts, label=DetNum )
    # if DetNum == '39': plot( wavelength, counts, label=DetNum )

    if len(lineList) == 0: break     # check for the end-of-file
    i = i + 1
        
specInput.close()

xlim( xmax=4.0 )     # set xmax to 4.0 Angstroms
# ylim( ymin=200 )     # turn on or off the ymin value

legend()

xlabel('Wavelength (A)')
ylabel('Counts')
title( plotTitle )
grid(True)
savefig( plotTitle )   # plot saved

show()

raw_input('Type RETURN to exit.')
exit()
