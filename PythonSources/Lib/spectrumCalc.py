#--------------------------------------------------------
#               function spectrumCalc
#--------------------------------------------------------
# Function to calculate the spectrum using the
# coefficients for the GSAS Type 2 spectrum.
#--------------------------------------------------------
# Jython version:
#   A. J. Schultz,   February, 2010
#--------------------------------------------------------

from math import exp

def spectrumCalc(wavelength, calibParam, pj, id):
    "Calculate the spectrum using spectral coefficients for the GSAS Type 2 incident spectrum."

    hom = 0.39559974            # Planck's constant divided by neutron mass

    DETD = calibParam[9][id]   # Sample-to-detector distance
    
    L1 = float(calibParam[0])   # initial flight path length in cm
    xtof = (L1 + DETD) / hom    # xtof times wavelength equals the time-of-flight
    TOF = wavelength * xtof     # time-of-flight in microseconds
    T = TOF/1000.               # time-of-flight in milliseconds
    
    c1 = pj[id][0]
    c2 = pj[id][1]
    c3 = pj[id][2]
    c4 = pj[id][3]
    c5 = pj[id][4]
    c6 = pj[id][5]
    c7 = pj[id][6]
    c8 = pj[id][7]
    c9 = pj[id][8]
    c10 = pj[id][9]
    c11 = pj[id][10]
    
    spect = c1 + c2*exp(-c3/T**2)/T**5 \
    + c4*exp(-c5*T**2) \
    + c6*exp(-c7*T**3) \
    + c8*exp(-c9*T**4) \
    + c10*exp(-c11*T**5)
    
    return spect