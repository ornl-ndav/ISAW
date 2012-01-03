#-----------------------------------
#           hkl2qxyz.py
#-----------------------------------

# Program to test the various functions in crystal.py

# A. J. Schultz     first version: October, 2006

from math import *
from numpy import *
from numpy.linalg import *
from crystal import *

# Open matrix file
# filename = raw_input('Matrix file name: ')
filename = '3681_5M.mat'
input = open(filename,'r')

# Initialize UB_IPNS matrix
UB_IPNS = zeros((3,3))
print '\n Input from matrix file ' + filename + ':\n'

# Read matrix file into UB_IPNS matrix
for i in range(3):
    linestring = input.readline()
    print linestring.strip('\n')
    linelist = linestring.split()
    for j in range(3):
        UB_IPNS[i,j] = float(linelist[j])
# Read next 2 lines containing lattice constants and esd's
for i in range(2):
    linestring = input.readline()
    print linestring.strip('\n')
# End of reading and printing matrix file

# Caluclate unit cell parameters from UB matrix
a = abc(UB_IPNS)     # a is a 1-D array containing the unit cell parameters

print '\n Unit cell parameters calculated from UB_IPNS matrix:'
print '       a         b         c       alpha      beta     gamma    Volume'
print '%10.3F%10.3f%10.3f%10.3f%10.3f%10.3f%10.3f' \
      % (a[0],a[1],a[2],a[3],a[4],a[5],a[6])
print ''

# Calculate the UB_SNS matrix
UB_SNS = zeros((3,3))
UB_SNS = UB_IPNS_2_SNS(UB_IPNS)
print '\n UB_SNS matrix:\n'
for i in range(3):
    print UB_SNS[i,0],UB_SNS[i,1],UB_SNS[i,2]

# Caluclate unit cell parameters from UB_SNS matrix
a = abc(UB_SNS)     # a is a 1-D array containing the unit cell parameters

print '\n Unit cell parameters calculated from UB_SNS matrix:'
print '       a         b         c       alpha      beta     gamma    Volume'
print '%10.3F%10.3f%10.3f%10.3f%10.3f%10.3f%10.3f' \
      % (a[0],a[1],a[2],a[3],a[4],a[5],a[6])
print ''



#-----------------------------------------------

# Input setting angles
# omega = float(raw_input('Input omega: '))
# chi = float(raw_input('Input chi: '))
# phi = float(raw_input('Input phi: '))
omega = chi = phi = 0.0
print 'Crystal setting angles:'
print 'omega =',omega,'  chi =',chi,'  phi =',phi, '\n'

# newmat = rotate_matrix(UB_IPNS, omega, chi, phi)
newmat = rotate_matrix(UB_SNS, omega, chi, phi)

# Input detector angle and distance consistent with ISAW such
# that the two IPNS SCD detectors are at angles of -75 and -120.
# deta1 = float(raw_input('Input detector angle (e.g., -120): '))
# deta2 = 0.0
# detd = float(raw_input('Input detector distanc (e.g., 17): '))

# Input hkl
while True:
    print '\n--------------------------------\n'
    # linestring = raw_input('Input h k l: ')
    # linelist = linestring.split()
    # h = float(linelist[0])
    # k = float(linelist[1])
    # l = float(linelist[2])        
    h = 3.0
    k = -5.0
    l = -4.0
    print 'hkl =', h,k,l

    # q1 = huq(h, k, l, UB_IPNS)
    q1 = huq(h, k, l, UB_SNS)
    print 'From UB matrix: q1 = ', q1, '\n '
    dstar = sqrt(q1[0]**2 + q1[1]**2 + q1[2]**2)
    d = 1.0/dstar
    print '+++ d-spacing calculated from q vector: ', '%8.4f' %d, '\n'
    

    q2 = rotate_vector(q1, omega, chi, phi)
    print 'From rotate_vector: q2 = ', q2, '\n'
    dstar = sqrt(q2[0]**2 + q2[1]**2 + q2[2]**2)
    d = 1.0/dstar
    print '+++ d-spacing calculated from q vector: ', '%8.4f' %d, '\n'


    q3 = huq(h, k, l, newmat)
    print 'From newmat: q3 = ', q3, '\n'
    dstar = sqrt(q3[0]**2 + q3[1]**2 + q3[2]**2)
    d = 1.0/dstar
    print '+++ d-spacing calculated from q vector: ', '%8.4f' %d, '\n'

    # parameters for the 3 -5 -4 peak
    twoth = 1.78373
    az = 3.06379
    wl = 1.407872
    q4 = qvec(twoth, az, wl)
    print 'From qvec: q4 = ', q4, '\n'
    dstar = sqrt(q4[0]**2 + q4[1]**2 + q4[2]**2)
    d = 1.0/dstar
    print '+++ d-spacing calculated from q vector: ', '%8.4f' %d, '\n'
    
    # multiply by 2pi to correspond to IsawEV
    q4 = q4 * 2.0 * pi
    print 'q4 times 2pi = ', q4, '\n'

    break
    
    # print '\nType Ctrl-C to stop me!'




