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

def absor_V_rod(CenterX, CenterY, CenterZ, wl):
    "Returns the absorption correction for a vanadium cylinder."
    
    
    # pc = fourth order polynomial coefficients             # theta:
    pc = [ [ 1.0016,  1.6444,  1.6350,  0.2919,  0.5164 ],  #  0
           [ 0.9924,  1.7836,  1.2483,  0.6507,  0.3903 ],  #  5
           [ 0.9776,  2.0219,  0.5477,  1.3329,  0.1228 ],  # 10
           [ 0.9721,  2.1296,  0.1660,  1.7523, -0.1048 ],  # 15
           [ 0.9776,  2.0673,  0.2467,  1.7328, -0.2167 ],  # 20
           [ 0.9873,  1.9268,  0.5725,  1.4309, -0.2348 ],  # 25
           [ 0.9970,  1.7768,  0.9518,  1.0192, -0.2001 ],  # 30
           [ 1.0038,  1.6645,  1.2497,  0.6291, -0.1481 ],  # 35
           [ 1.0072,  1.6001,  1.4252,  0.3173, -0.0984 ],  # 40
           [ 1.0082,  1.5730,  1.4955,  0.0877, -0.0572 ],  # 45
           [ 1.0078,  1.5712,  1.4883, -0.0711, -0.0257 ],  # 50
           [ 1.0066,  1.5838,  1.4323, -0.1755, -0.0028 ],  # 55
           [ 1.0051,  1.6041,  1.3474, -0.2388,  0.0127 ],  # 60
           [ 1.0035,  1.6242,  1.2628, -0.2835,  0.0250 ],  # 65
           [ 1.0023,  1.6459,  1.1654, -0.2978,  0.0306 ],  # 70
           [ 0.9911,  1.7932,  0.8144, -0.1398,  0.0033 ],  # 75
           [ 1.0005,  1.6746,  1.0311, -0.3157,  0.0384 ],  # 80
           [ 1.0001,  1.6823,  0.9946, -0.3187,  0.0400 ],  # 85
           [ 0.9998,  1.6858,  0.9803, -0.3185,  0.0402 ] ] # 90


    # From Structure of Metals by Barrett and Massalksi:
    #                vanadium is b.c.c, a = 3.0282
    # V = 27.769, Z = 2
    # From lin_abs_coef in ISAW:
    smu = 0.367  # linear absorption coeff. for total scattering in cm_1
    amu = 0.366  # linear absorption coeff. for true absorption at 1.8 A in cm^-1
    
    mu = smu + (amu/1.8)*wl

    radius = 0.5  # radius of the vanadium rod used for TOPAZ
    muR = mu*radius
    
    angle1 = atan( CenterX / CenterZ )  # the scattering angle in the horizontal plane
    
    theta = (angle1*180.0/pi)/2.0   # theta is the theta angle in the horizontal plane

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

    tbar1 = -log(trans)/mu

    # Calculate total path length and transmission for scattered
    # beam out of the horizontal plane.
    
    diagonal = sqrt( CenterX**2 + CenterZ**2 )
    angle2 = atan( CenterY / diagonal )  # angle out of the plane
    tbar2 = tbar1 / cos( angle2 )  # path length
    trans2 = exp( -mu * tbar2 )    # transmission
    
    return trans2

# test
    
# CenterX = -23.2175
# CenterY = 0.00
# CenterZ = 31.962
# wl = 2.0

# transmission = absor_V_rod( CenterX, CenterY, CenterZ, wl )
# print transmission
