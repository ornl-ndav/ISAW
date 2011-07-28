#--------------------------------------------------------
#               function absor_sphere
#--------------------------------------------------------
# Function to calculate the absorption and  of a sherical
# crystal.
#--------------------------------------------------------
# Jython version:
#   A.J. Schultz,   November, 2009
#--------------------------------------------------------
# Comments from the Fortran source code in anvredSNS.f:
# ! Subroutine to calculate a spherical absorption correction
# ! and tbar. Based on values in:
# !
# ! C. W. Dwiggins, Jr., Acta Cryst. A31, 395 (1975).
# !
# ! In this paper, A is the transmission and A* = 1/A is
# ! the absorption correction.
#
# ! Input are the smu (scattering) and amu (absorption at 1.8 Ang.)
# ! linear absorption coefficients, the radius R of the sample
# ! the theta angle and wavelength.
# ! The absorption (absn) and tbar are returned.
#
# ! A. J. Schultz, June, 2008
#
#   real mu, muR    !mu is the linear absorption coefficient,
#                   !R is the radius of the spherical sample.
#
# ! For each of the 19 theta values in Dwiggins (theta = 0.0 to 90.0
# ! in steps of 5.0 deg.), the ASTAR values vs.muR were fit to a third
# ! order polynomial in Excel. These values are given below in the
# ! data statement.

from math import *

def absor_sphere(smu, amu, radius, twoth, wl):
    "Returns the absorption correction for a Bragg peak."
    
    
    # pc = third order polynomial coefficients
    pc = [ [ 1.0000,  1.9368,  0.0145,  1.1386 ],
           [ 1.0000,  1.8653,  0.1596,  1.0604 ],
           [ 1.0000,  1.6908,  0.5175,  0.8598 ],
           [ 1.0000,  1.4981,  0.9237,  0.6111 ],
           [ 1.0000,  1.3532,  1.2436,  0.3798 ],
           [ 1.0000,  1.2746,  1.4308,  0.1962 ],
           [ 1.0000,  1.2530,  1.4944,  0.0652 ],
           [ 1.0000,  1.2714,  1.4635, -0.0198 ],
           [ 1.0000,  1.3093,  1.3770, -0.0716 ],
           [ 1.0000,  1.3559,  1.2585, -0.0993 ],
           [ 1.0000,  1.4019,  1.1297, -0.1176 ],
           [ 1.0000,  1.4434,  1.0026, -0.1153 ],
           [ 1.0000,  1.4794,  0.8828, -0.1125 ],
           [ 1.0000,  1.5088,  0.7768, -0.1073 ],
           [ 1.0000,  1.5317,  0.6875, -0.1016 ],
           [ 1.0000,  1.5489,  0.6159, -0.0962 ],
           [ 1.0000,  1.5608,  0.5637, -0.0922 ],
           [ 1.0000,  1.5677,  0.5320, -0.0898 ],
           [ 1.0000,  1.5700,  0.5216, -0.0892 ] ]

    mu = smu + (amu/1.8)*wl

    muR = mu*radius
    
    theta = (twoth*180.0/pi)/2.0

# ! Using the polymial coefficients, calulate ASTAR (= 1/transmission) at
# ! theta values below and above the actual theta value.

    i = int(theta/5.0)
    astar1 = pc[i][0] + pc[i][1]*muR + pc[i][2]*muR**2 + pc[i][3]*muR**3

    i = i+1
    astar2 = pc[i][0] + pc[i][1]*muR + pc[i][2]*muR**2 + pc[i][3]*muR**3

# ! Do a linear interpolation between theta values.

    frac = (theta%5.0)/5.0

    astar = astar1*(1-frac) + astar2*frac	# astar is the correction

    trans = 1.0/astar                          # trans is the transmission
                                               # trans = exp(-mu*tbar)

# ! Calculate TBAR as defined by Coppens.

    if mu == 0.0:
        tbar = 0.0
    else:
        tbar = -log(trans)/mu


    return trans, tbar
    
    
    
    
