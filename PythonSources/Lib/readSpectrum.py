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

def readSpectrum(nod, initBankNo, directory_path):
    "Read the spectrum file for each detector bank."

    spectra = []
    
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
        
        # set arrays to zero
        time = []
        counts = []
        
        for i in range(7, (numTimeChannels + 7)):
            lineString = input.readline()
            lineList = lineString.split()
            time.append( float(lineList[0]) )
            counts.append( float(lineList[1]) )
        
        spectra.append( [ time, counts ] )
        

        input.close()
        
    # "spectra" is an array spectra[i][j] where i is the number
    # of the detector bank starting at zero, and j = 0 for
    # the array of times and j = 1 for the array of counts
    
    return spectra

#--------------------------------------------------------
# test the function
#--------------------------------------------------------

# nod = 9
# initBankNo = 10
# directory_path = "C:/SNS/Jython/anvred/"

# spectra = readSpectrum( nod, initBankNo, directory_path)

# print spectra


