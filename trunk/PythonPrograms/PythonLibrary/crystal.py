#--------------------------------------#
# Cystal function library.             #
# A. J. Schultz     from October, 2006 #
#--------------------------------------#
# Current revision date:
#    January 13, 2010

from math import *
from numpy import *
from numpy.linalg import *
#--------------------------------------------------------
#               function abc
#--------------------------------------------------------
# Function to obtain unit cell parameters from UB matrix.
# Same as the Fortran SUBROUTINE ABC.
#--------------------------------------------------------

def abc(UB):
    "Returns unit cell parameters calculated from UB matrix"
    
    UBt = transpose(UB) #UBt the the transpose of UB

    Gi = dot(UB,UBt)    #Gi is the reciprocal metric tensor
                        #Gi is the product of UB and the transpose of UB

    G = inv(Gi)         #G is the real metric tensor

    # Obtain unit cell parameters
    rad = 180./pi

    a = zeros((7))       
    a[0] = sqrt(G[0,0])                     #a
    a[1] = sqrt(G[1,1])                     #b
    a[2] = sqrt(G[2,2])                     #c
    a[3] = acos(G[1,2]/(a[1]*a[2]))*rad     #alpha
    a[4] = acos(G[0,2]/(a[0]*a[2]))*rad     #beta
    a[5] = acos(G[0,1]/(a[0]*a[1]))*rad     #gamma
    a[6] = sqrt(det(G))                     #Volume

    return a



#---------------------------------------------
#           function calc_2th_wl_IPNS
#---------------------------------------------
# Calculate two-theta and wavelength from
# q vector in the IPNS coordinate system
# with x along the beam.
#---------------------------------------------
def calc_2th_wl_IPNS(q):
    "Returns two-theta angle and wavelength for q-vector"
    rad = 180./pi

    dstar = sqrt(q[0]**2 + q[1]**2 + q[2]**2)
    d = 1.0/dstar

    b = zeros((2))

    theta = 90.0 - acos(-q[0]/dstar) * rad    # theta
    b[0] = 2.0 * theta    # two-theta
	
    b[1] = 2.0 * d * sin((theta) * pi/180.0) # wavelength
    b[1] = abs(b[1])

    return b
    
    
#---------------------------------------------
#           function calc_2th_wl_SNS
#---------------------------------------------
# Calculate two-theta and wavelength from
# q vector in the SNS coordinate system
# with z along the beam.
#---------------------------------------------
def calc_2th_wl_SNS(q):
    "Returns two-theta angle and wavelength for q-vector"
    rad = 180./pi

    dstar = sqrt(q[0]**2 + q[1]**2 + q[2]**2)
    d = 1.0/dstar

    b = zeros((2))

    theta = 90.0 - (acos( -q[2]/dstar) * rad)    # theta
    b[0] = 2.0 * theta    # two-theta
	
    b[1] = 2.0 * d * sin((theta) * pi/180.0) # wavelength
    b[1] = abs(b[1])

    return b

#---------------------------------------------
#           function det_coord
#---------------------------------------------
# Calculate detector positions (xcm,ycm)
# for the IPNS coordinate system.
#---------------------------------------------

def det_coord(q, wl, deta1, deta2, detd, det_rot_ang):
    "Returns detector coordinates xcm,ycm"

# The detector will be assumed to be centered at zero angles,
# with coordinates in q-space of (1,0,0). So the procedure is to rotate
# the q-vector for the hkl peak by the two detector angles.

# In ISAW, the coordinate system is x parallel
# to the beam, with positive x pointing downstream.
# Looking down the x-axis, +y is to the right and
# +z points up. This means that the IPNS SCD detectors
# are located at -120 and -75 degrees.

#   beam stop <... crystal ...> source
#            +x <----X
#                    |
#                    |
#                    v +y

# First translate the origin from the reciprocal
# lattice origin to the center of the sphere of
# reflection.
    xdp = q[0] + (1.0/wl)

# Angles and rotations as defined here:
# http://mathworld.wolfram.com/SphericalCoordinates.html
# and here:
# http://mathworld.wolfram.com/RotationMatrix.html

# Rotate to a deta1 to zero

    ang = radians(-deta1)               #Change sign of deta, 8/30/07
    if ang < 0.0: ang = ang + 2.0*pi    #This ensures a counterclockwise rotation around x
    xt = xdp
    yt = q[1]
    xdp = xt*cos(ang) - yt*sin(ang)
    ydp = xt*sin(ang) + yt*cos(ang)

    ang = radians(-deta2)               #Again counterclockwise rotation around y
    xt = xdp
    zt = q[2]
    xdp = xt*cos(ang) - zt*sin(ang)
    zdp = xt*sin(ang) + zt*cos(ang)    

# Calculate xcm and ycm

    xcm0 = -(ydp/xdp)*detd
    ycm0 = (zdp/xdp)*detd

#  If detector is rotated (usually by 45 deg.),
#  calculate detector coordinates.
    ang = radians(det_rot_ang)
    xcm = xcm0*cos(ang) - ycm0*sin(ang)
    ycm = xcm0*sin(ang) + ycm0*cos(ang)
    
    
# If xdp is negative, then diffracted ray is in the opposite direction
# of the detector.

    if xdp <= 0.0:
        xcm = ycm = 99.

    dc = zeros((2))
    dc = [xcm, ycm]

    return dc


#---------------------------------------------
#           function huq
#---------------------------------------------
#   Multiply hkl times UB matrix to obtain
#   qx,qy,qz.
#---------------------------------------------
def huq(h, k, l, UB):
    "Multiplies hkl times UB matrix to return q-vector"
    hh = [h, k, l]

    q = zeros((3))
    q = dot(hh, UB)

    return q


#---------------------------------------------
#       function polar_parameters
#---------------------------------------------
# Calculate coordinates for polar plots.
#---------------------------------------------
def polar_paramters (q):
    "Return coordinates for polar plot."

    pc = zeros((4))

    if q[0]!=0.0:
        pc[0] = degrees(atan(q[1]/q[0])) # polar plot theta angle
        if q[0]<0: pc[0] = pc[0] + 180.
    else:
        pc[0] = 90.0
        if q[1]<0: pc[0]=270.0
    
#  Spherical angle phi, which will be the polar coordinate R
    pc[1] = degrees(acos(q[2]/sqrt(q[0]**2 + q[1]**2 + q[2]**2)))
    
#  Cartesian coordinates xp,yp for plotting with some programs
    pc[2] = pc[1]*cos(radians(pc[0]))
    pc[3] = pc[1]*sin(radians(pc[0]))
    
    return pc


#---------------------------------------------
#       function qvec
#---------------------------------------------
# Calculate the q vector from two-theta, az 
# and wl for a peak.
#---------------------------------------------
def qvec(twoth, az, wl):
    "Return q vector for a peak in peaks file."
    
    q = zeros((3))
    
    # IPNS axes with x is the beam direction and z is vertically upward
    # q[0] = cos(az)*sin(twoth)/wl
    # q[1] = sin(az)*sin(twoth)/wl
    # q[2] = (cos(twoth) - 1.0)/wl
    
    # SNS axes with z is the beam direction and y is vertically upward
    q[0] = (cos(twoth) - 1.0)/wl
    q[1] = cos(az)*sin(twoth)/wl
    q[2] = sin(az)*sin(twoth)/wl
    
    return q
    
    
#--------------------------------------------------------
#               function rotate_matrix
#--------------------------------------------------------
# Same as Fortran SUBROUTINE NEWROT.
#C
#C   ROTATES A 3X3 MATRIX FOR WHICH ALL ANGLES ARE ZERO TO
#C   A 3X3 MATRIX FOR WHICH ALL ANGLES ARE NON ZERO. SEE W.
#C   C. HAMILTON, INT. TABLES. IV, PP 275-281
#C
#--------------------------------------------------------

def rotate_matrix(UB, omega, chi, phi):
    "Rotates UB matrix by setting angles"

    fmat = rotation_matrix(omega, chi, phi)

    newmat = zeros((3,3))
    newmat = dot(UB, fmat)

    return newmat

    
#---------------------------------------------
#       function rotate_vector
#---------------------------------------------
# Rotate q vector by angles omega, chi and phi
#---------------------------------------------
def rotate_vector(q, omega, chi, phi):
    "Rotates q-vector by setting angles"

    fmat = rotation_matrix(omega, chi, phi)
    
    newq = zeros((3))
    newq = dot(q, fmat)

    return newq

#---------------------------------------------
#       function rotation_matrix
#---------------------------------------------
# Calculate rotation matrix for omega, chi and phi angles
#---------------------------------------------
def rotation_matrix(omega, chi, phi):
    "Returns rotation matrix from setting angles"
    
    rad = 180./pi

    ph = phi/rad
    cp = cos(ph)
    sp = sin(ph)
    R_phi = zeros((3,3))
    R_phi[0,0] = cp
    R_phi[0,1] = sp
    R_phi[1,0] = -sp
    R_phi[1,1] = cp
    R_phi[2,2] = 1.0

    ch = chi/rad        #changed -chi to chi, 8/23/07
    cc = cos(ch)
    sc = sin(ch)
    R_chi = zeros((3,3))
    R_chi[0,0] = 1.0
    R_chi[1,1] = cc
    R_chi[1,2] = sc
    R_chi[2,1] = -sc
    R_chi[2,2] = cc

    om = -omega/rad
    co = cos(om)
    so = sin(om)
    R_om = zeros((3,3))
    R_om[0,0] = co
    R_om[0,1] = so
    R_om[1,0] = -so
    R_om[1,1] = co
    R_om[2,2] = 1.0

    fmat = zeros((3,3))
    fmat = dot(R_phi, R_chi)
    fmat = dot(fmat, R_om)

    return fmat

#--------------------------------------------------------
#               function UB_IPNS_2_SNS
#--------------------------------------------------------
#  Transform the IPNS UB matrix usually store in the mat
#  file to the SNS matrix.
#  These are actually the UB tramspose matrices. The
#  transformation is:
#  IPNS:
#     col1  col2  col3
#  SNS:
#     col2  col3  col1
#--------------------------------------------------------
def UB_IPNS_2_SNS(UB_IPNS):
    "Transform the IPNS UB matrix to the SNS matrix."

    UB_SNS = zeros((3,3))
    for i in range(3):
        UB_SNS[i,0] = UB_IPNS[i,1]
        UB_SNS[i,1] = UB_IPNS[i,2]
        UB_SNS[i,2] = UB_IPNS[i,0]
    
    return UB_SNS


#--------------------------------------------------------
#               function center
#--------------------------------------------------------
#  Test for centering.
#  Return True if peak is allowed.
#  Return False if peaks is not allowed.
#--------------------------------------------------------
def center(h, k, l, center_type):
    """ Function to test for allowed (True) and not allowed (False)
    peaks due to centering."""
    
    if center_type == 'P':
        return True
    
    if center_type == 'A':
        sum = k + l
        if (sum % 2) == 0: return True
        return False
        
    if center_type == 'B':
        sum = h + l
        if (sum % 2) == 0: return True
        return False
        
    if center_type == 'C':
        sum = h + k
        if (sum % 2) == 0: return True
        return False
        
    if center_type == 'F':
        sum = h + k
        if (sum % 2) != 0: return False
        sum = h + l
        if (sum % 2) != 0: return False
        sum = k + l
        if (sum % 2) != 0: return False
        return True
        
    if center_type == 'I':
        sum = h + k + l
        if (sum % 2) != 0: return False
        return True
        
    if center_type == 'R':
        sum = -h + k + l
        if (sum % 3) != 0: return False
        return True
        
    print 'Centering type not P, A, B, C, F, I or R.'
    

        

