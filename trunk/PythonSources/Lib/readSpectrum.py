#--------------------------------------------------------
#               function readSpectrum
#--------------------------------------------------------
# Read the spectrum file for each detector bank.
#--------------------------------------------------------
# Jython version:
#   A. J. Schultz,   March, 2010
#--------------------------------------------------------
#
#

from jarray import *

def readSpectrum(nod, initBankNo, directory_path):
    "Read the spectrum file for each detector bank."

    # define arrays for up to 100 detectors
    time = zeros(100, 'f')
    counts = zeros(100, 'f')
    
    for id in range(nod):
        
        iBank = id + initBankNo  # detector bank number
        siBank = str(iBank) # convert bank number to a string
        specNam = directory_path + 'Bank' + siBank + '_spectrum.asc'
        input = open( specNam, 'r')
        
        for i in range(6):  # skip the first 6 lines
            lineString = input.readline()
        
        lineString = input.readline()
        lineList = lineString.split()
        numTimeChannels = int(lineList[4])
        
        for i in range(7, numTimeChannels):
            lineString = input.readline()
            lineList = lineString.split()
            time[i] = float(lineList[0])
            counts[i] = float(lineList[1])

    input.close()
        
    return time, counts
