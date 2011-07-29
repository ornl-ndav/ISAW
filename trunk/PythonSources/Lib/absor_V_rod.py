#--------------------------------------------------------
#               function absor_V_rod
#--------------------------------------------------------
# Function to calculate the absorption and of a vanadium rod.
#--------------------------------------------------------
#   A.J. Schultz,   September, 2010
#--------------------------------------------------------
# Subroutine to calculate the absorption correction
# for a vanadium rod. Based on values for cylinders in:
# 
# C. W. Dwiggins, Jr., Acta Cryst. A31, 146 (1975).
# 
# In this paper, A is the transmission and A* = 1/A is
# the absorption correction.
# 
# For each of the 19 theta values in Dwiggins (theta = 0.0 to 90.0
# in steps of 5.0 deg.), the ASTAR values vs.muR were fit to a fourth
# order polynomial in Excel. These values are given below in the
# data statement. (For a sphere, third order polynomials were
# sufficient, but not for the cylinder.)

from math import *

def absor_V_rod( angleH, angleV, wl):
    "Returns the absorption correction for a vanadium cylinder."
    
    # pc = fourth order polynomial coefficients
    pc = [ [ 1.000,   1.6516,   1.6251,   0.2971,   0.5155 ],
           [ 1.000,   1.7483,   1.2967,   0.6254,   0.3947 ],
           [ 1.000,   1.9176,   0.6903,   1.2585,   0.1359 ],
           [ 1.000,   1.9995,   0.3439,   1.6594,  -0.0884 ],
           [ 1.000,   1.9627,   0.3897,   1.6581,  -0.2035 ],
           [ 1.000,   1.8675,   0.6536,   1.3886,  -0.2274 ],
           [ 1.000,   1.7628,   0.9709,   1.0092,  -0.1983 ],
           [ 1.000,   1.6821,   1.2256,   0.6416,  -0.1503 ],
           [ 1.000,   1.6336,   1.3794,   0.3412,  -0.1026 ],
           [ 1.000,   1.6114,   1.4430,   0.1151,  -0.0620 ],
           [ 1.000,   1.6075,   1.4387,  -0.0451,  -0.0303 ],
           [ 1.000,   1.6146,   1.3900,  -0.1534,  -0.0067 ],
           [ 1.000,   1.6278,   1.3150,  -0.2219,   0.0097 ],
           [ 1.000,   1.6406,   1.2405,  -0.2719,   0.0230 ],
           [ 1.000,   1.6566,   1.1508,  -0.2901,   0.0293 ],
           [ 1.000,   1.7516,   0.8713,  -0.1695,   0.0086 ],
           [ 1.000,   1.6771,   1.0277,  -0.3139,   0.0380 ],
           [ 1.000,   1.6826,   0.9942,  -0.3185,   0.0399 ],
           [ 1.000,   1.6851,   0.9814,  -0.3190,   0.0403 ] ]

    # From Structure of Metals by Barrett and Massalksi:
    #                vanadium is b.c.c, a = 3.0282
    # V = 27.769, Z = 2
    # From lin_abs_coef in ISAW:
    smu = 0.367  # linear absorption coeff. for total scattering in cm_1
    amu = 0.366  # linear absorption coeff. for true absorption at 1.8 A in cm^-1
    radius = 0.407  # radius of the vanadium rod used for TOPAZ
    
    mu = smu + (amu/1.8)*wl

    muR = mu*radius
    
    theta = (angleH*180.0/pi)/2.0   # theta is the theta angle in the horizontal plane

    # ! Using the polymial coefficients, calulate ASTAR (= 1/transmission) at
    # ! theta values below and above the actual theta value.

    i = int(theta/5.0)
    astar1 = pc[i][0] + pc[i][1]*muR + pc[i][2]*muR**2 + pc[i][3]*muR**3

    i = i+1
    astar2 = pc[i][0] + pc[i][1]*muR + pc[i][2]*muR**2 + pc[i][3]*muR**3

    # !	Do a linear interpolation between theta values.

    frac = (theta%5.0)/5.0

    astar = astar1*(1-frac) + astar2*frac	# astar is the correction

    trans1 = 1.0/astar	                        # trans is the transmission
                                                # trans = exp(-mu*tbar)
	
    # !	Calculate TBAR as defined by Coppens.

    tbar1 = -log(trans1)/mu

    # Calculate total path length and transmission for scattered
    # beam out of the horizontal plane.
    
    tbar2 = tbar1 / cos( angleV )  # path length
    trans2 = exp( -mu * tbar2 )    # transmission
    
    return trans2

# test
    
# CenterX = -23.2175
# CenterY = 0.00
# CenterZ = 31.962
# wl = 2.0

# transmission = absor_V_rod( CenterX, CenterY, CenterZ, wl )
# print transmission
