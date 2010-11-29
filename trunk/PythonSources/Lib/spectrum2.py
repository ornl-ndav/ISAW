#--------------------------------------------------------
#               function spectrum2
#--------------------------------------------------------

#!  Obtain spectral correction from counts vs. time data
#!  in a Bankxx_spectrum.asc file.
#!  Fortran version: A. J. Schultz, July, 2009
#!  Jython version: A. J. Schultz, March, 2010

#  spectrum2 does not average over a +/- averageRange.
#  This is because TOPAZ_spectrum now includes
#  a Savitzky-Golay smoothing Filter.
#  A. J. Schultz, September, 2010


def spectrum2( wavelength, xtof, spect1, xtime, xcounts ):
    "Returns the relative spectrum and detector efficiency correction."
	
	
# TOF = WL * XTOF in units of microseconds
    TOF = wavelength * xtof    
	
    numTimeChannels = len( xtime )
    
    spect = 0.0
         
# begin determining the spectrum correction
    for j in range(numTimeChannels):
        
        if xtime[j] > TOF:
            deltaCounts = xcounts[j] - xcounts[j-1]
            deltaTime = xtime[j] - xtime[j-1]
            fraction = (TOF - xtime[j-1]) / deltaTime
            spect = xcounts[j-1] + deltaCounts*fraction # interpolate
            break
    
    spect = spect/spect1
    
    
    return spect
    
    