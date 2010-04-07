#--------------------------------------------------------
#               function spectrum
#--------------------------------------------------------

#!  Obtain spectral correction from counts vs. time data
#!  in a Bankxx_spectrum.asc file.
#!  Fortran version: A. J. Schultz, July, 2009
#!  Jython version: A. J. Schultz, March, 2010


def spectrum( wavelength, xtof, averageRange, spect1, xtime, xcounts ):
    "Returns the relative spectrum correction."
	
	
# c++	TOF = WL * XTOF
    TOF = wavelength * xtof
	# T = TOF			# T is in units of microseconds
	
    numTimeChannels = len( xtime )
    
    for j in range(numTimeChannels):
        
        if xtime[j] > TOF:
            sum = 0.0
            for jj in range(-averageRange, averageRange):
                sum = sum + xcounts[j + jj]
                spect = sum / (2*(averageRange) + 1)
            break
    
    spect = spect/spect1
    
    return spect
    # return
    
    