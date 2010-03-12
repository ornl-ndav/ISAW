#--------------------------------------------------------
#               function readSpecCoef
#--------------------------------------------------------
# Function to read the spectrum coefficients from a file.
# The coefficients are for the GSAS Type 2 spectrum.
#--------------------------------------------------------
# Jython version:
#   A. J. Schultz,   February, 2010
#--------------------------------------------------------

def readSpecCoef(specInput, logFile, nod):
    "Reads and returns the spectral coefficients for the GSAS Type 2 incident spectrum."


# "specInput" is the open input file.
# "nod" is the number of detectors.
# "pk" is an array of arrays of the spectrum coefficients.
    
    pj = []
    pk = []
    
    logFile.write('\n\nSpectral coefficients:')

    for j in range(nod):
        lineString = specInput.readline()
        logFile.write('\n' + lineString)

        lineString = specInput.readline()
        lineList = lineString.split()
        
        for k in range(11):
            pk.append(float(lineList[k]))
            
        logFile.write( lineString )
        pj.append(pk)

    return pj