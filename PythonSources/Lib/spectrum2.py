#--------------------------------------------------------
#               function spectrum2
#--------------------------------------------------------

#!  Obtain spectral correction from counts vs. time data
#!  in a Bankxx_spectrum.asc file.
#!  Fortran version: A. J. Schultz, July, 2009
#!  Jython version: A. J. Schultz, March, 2010

#  Also returns the relative sigma of the spectral correction.
#  A. J. Schultz, April, 2011

#  spectrum2 does not average over a +/- averageRange.
#  This is because TOPAZ_spectrum now includes
#  a Savitzky-Golay smoothing Filter.
#  A. J. Schultz, September, 2010

#  Parameters:
#  wavelength = wavelength in Angstroms
#  xtof = (L1 + detD)/hom; TOF = wl * xtof
#  spect1 = spectrum at normalization wavlength, usually 1 Angstrom
#  xtime = spectrum TOF array
#  xcounts = spectrum counts array

from math import *

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
            spectx = xcounts[j-1] + deltaCounts*fraction # interpolate
            break
    
    spect = spectx / spect1
    
    # relative sigma for spect
    # relSigSpect**2 = (sqrt(spectx)/spectx)**2 + (sqrt(spect1)/spect1)**2
    relSigSpect = sqrt((1.0/spectx) + (1.0/spect1))
    
    
    return spect, relSigSpect
    
    