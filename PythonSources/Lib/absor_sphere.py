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
# !	Subroutine to calculate a spherical absorption correction
# !	and tbar. Based on values in:
# !
# !	C. W. Dwiggins, Jr., Acta Cryst. A31, 395 (1975).
# !
# !	In this paper, A is the transmission and A* = 1/A is
# !	the absorption correction.
#
# !	Input are the smu (scattering) and amu (absorption at 1.8 Ang.)
# !	linear absorption coefficients, the radius R of the sample
# !	the theta angle and wavelength.
# !	The absorption (absn) and tbar are returned.
#
# !	A. J. Schultz, June, 2008
#			
#	real mu, muR	!mu is the linear absorption coefficient,
#			        !R is the radius of the spherical sample.
#	
# !	For each of the 19 theta values in Dwiggins (theta = 0.0 to 90.0
# !	in steps of 5.0 deg.), the ASTAR values vs.muR were fit to a third
# !	order polynomial in Excel. These values are given below in the
# !	data statement.

from math import *

def absor_sphere(smu, amu, radius, twoth, wl):
    "Returns the absorption correction for a Bragg peak."
    
    
    # pc = third order polynomial coefficients
    pc = [ [ 0.9369,  2.1217, -0.1304,  1.1717 ],
           [ 0.9490,  2.0149,  0.0423,  1.0872 ],
           [ 0.9778,  1.7559,  0.4664,  0.8715 ],
           [ 1.0083,  1.4739,  0.9427,  0.6068 ],
           [ 1.0295,  1.2669,  1.3112,  0.3643 ],
           [ 1.0389,  1.1606,  1.5201,  0.1757 ],
           [ 1.0392,  1.1382,  1.5844,  0.0446 ],
           [ 1.0338,  1.1724,  1.5411, -0.0375 ],
           [ 1.0261,  1.2328,  1.4370, -0.0853 ],
           [ 1.0180,  1.3032,  1.2998, -0.1088 ],
           [ 1.0107,  1.3706,  1.1543, -0.1176 ],
           [ 1.0046,  1.4300,  1.0131, -0.1177 ],
           [ 0.9997,  1.4804,  0.8820, -0.1123 ],
           [ 0.9957,  1.5213,  0.7670, -0.1051 ],
           [ 0.9929,  1.5524,  0.6712, -0.0978 ],
           [ 0.9909,  1.5755,  0.5951, -0.0914 ],
           [ 0.9896,  1.5913,  0.5398, -0.0868 ],
           [ 0.9888,  1.6005,  0.5063, -0.0840 ],
           [ 0.9886,  1.6033,  0.4955, -0.0833 ] ]

    mu = smu + (amu/1.8)*wl

    muR = mu*radius
    
    theta = (twoth*180.0/pi)/2.0

# !	Using the polymial coefficients, calulate ASTAR (= 1/transmission) at
# !	theta values below and above the actual theta value.

    i = int(theta/5.0)
    astar1 = pc[i][0] + pc[i][1]*muR + pc[i][2]*muR**2 + pc[i][3]*muR**3

    i = i+1
    astar2 = pc[i][0] + pc[i][1]*muR + pc[i][2]*muR**2 + pc[i][3]*muR**3

# !	Do a linear interpolation between theta values.

    frac = (theta%5.0)/5.0

    astar = astar1*(1-frac) + astar2*frac		# astar is the correction

    trans = 1.0/astar	                        # trans is the transmission
                                                # trans = exp(-mu*tbar)
	
# !	Calculate TBAR as defined by Coppens.

    tbar = -log(trans)/mu

    
    return trans, tbar
    
    
    
    
