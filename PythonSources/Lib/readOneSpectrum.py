#--------------------------------------------------------
#               function readOneSpectrum
#--------------------------------------------------------
# Read the spectrum file for a detector bank.
#--------------------------------------------------------
#   A. J. Schultz,   September, 2010
#--------------------------------------------------------
#
#

def readOneSpectrum(Bank, directory_path):
    "Read the spectrum file a detector bank."

    spectrum = []
        
    sBank = str(Bank) # convert bank number to a string
    specNam = directory_path + 'Bank' + sBank + '_spectrum.asc'
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
    
    input.close()
    
    return time, counts

#--------------------------------------------------------
# test the function
#--------------------------------------------------------

# Bank = 10
# directory_path = "C:/SNS/Jython/anvred/"

# "spectrum" is an array spectrum[i] where i = 0 for
# the array of times and i = 1 for the array of counts

# spectrum = readOneSpectrum( Bank, directory_path )
# time = spectrum[0]
# counts = spectrum[1]

# print time[1000], counts[1000]



