#--------------------------------------------------------
#               function spectrum
#--------------------------------------------------------

#!  Obtain spectral correction from counts vs. time data
#!  in a Bankxx_spectrum.asc file.
#!  Fortran version: A. J. Schultz, July, 2009
#!  Jython version: A. J. Schultz, March, 2010


def spectrum( wavelength, xtof, averageRange, spect1, xtime, xcounts ):
    "Returns the relative spectrum and detector efficiency correction."
	
	
# TOF = WL * XTOF in units of microseconds
    TOF = wavelength * xtof    
	
    numTimeChannels = len( xtime )
    
    spect = 0.0
     
# check for out-of-range TOF value
    if xtime[averageRange+1] > TOF:
        print 'Too small, TOF = %f, wavelength = %f' % (TOF, wavelength)
        return spect
    if xtime[numTimeChannels - averageRange -1] < TOF:
        print 'Too large, TOF = %f, wavelength = %f' % (TOF, wavelength)
        return spect
    
# begin deteriming the spectrum correction
    for j in range(numTimeChannels):
        
        if xtime[j] > TOF:
            # print 'In spectrum: j xtime[j] TOF = %d %f %f' % (j, xtime[j], TOF)
            sum = 0.0
            for jj in range(-averageRange, averageRange+1):
                sum = sum + xcounts[j + jj]
                # print 'In spectrum: jj, xcounts[j+jj], sum = %d %f %f' % (jj, xcounts[j+jj], sum)
            spect = sum / (2*(averageRange) + 1)
            # print 'In spectrum: spect = %f' % spect
            break
    
    spect = spect/spect1
    # print 'In spectrum: spect1, spect = %f %f' % (spect1, spect)
    
    return spect
    # return
    
    