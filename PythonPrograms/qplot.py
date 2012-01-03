#-----------------------------------
#           qplot.py
#-----------------------------------

# Program to plot the q profile of a peak

# A. J. Schultz     December, 2011

# from math import *
# from numpy import *
# from numpy.linalg import *
from crystal import *
from pylab import *
from sys import exit
import struct

# input = open('Qxyz.dat', 'r')
input = open('EventsToQ.bin', 'rb')
output = open('qplot.dat', 'w')

# Q vector for the 3 -5 -4 peak.
h = 3.0
k = -5.0
l = -4.0
print '\nhkl =', h,k,l, '\n'
Qpeak = [-5.40603101, -4.34890569,  0.33904078]   # units of 2pi/d
print 'Qpeak = ', Qpeak, '\n'
lenQpeak = sqrt( dot(Qpeak, Qpeak) )

d = 2.0 * pi / lenQpeak
print 'd = ', d, '\n'

deltaQ = 0.005
print 'deltaQ = ', deltaQ, '\n'

lenQpeakInt = int(lenQpeak * 100)
midx = float(lenQpeakInt)
midx = midx / 100.

x = []
y = []

rangeQ = 0.3   # length of cylinder
numSteps = int( rangeQ/deltaQ )
print 'numSteps = ', numSteps, '\n'
radiusQ = 0.1   # radius of cylinder

for i in range(numSteps):
    x.append( midx - 0.5*rangeQ + i*deltaQ ) 
    y.append( 0 )

num = 0

# for line in input:
while True:
    lineString = input.read(12)
    if lineString == "": break
    Qx, Qy, Qz = struct.unpack('fff', lineString)

    # lineList = line.split()
    # Qx = float( lineList[0] )   # Q in units of 2pi/d
    # Qy = float( lineList[1] )
    # Qz = float( lineList[2] )
    
    # do initial test for event within 0.5 of the peak
    if abs( Qpeak[0] - Qx ) > 0.3: continue
    if abs( Qpeak[1] - Qy ) > 0.3: continue
    if abs( Qpeak[2] - Qz ) > 0.3: continue
    
    Qdata = [ Qx, Qy, Qz ]                # data point Q vector
    lenQdata = sqrt( dot(Qdata, Qdata) )  # length of data point Q vector
    
    cosAng = dot(Qpeak, Qdata) / (lenQpeak * lenQdata)
    
    # define a cylinder
    angle = acos( cosAng )
    lenPerpendicular = lenQdata * sin(angle)
    if lenPerpendicular > radiusQ: continue
    lenOnQpeak = lenQdata * cosAng
    if abs(lenQpeak - lenOnQpeak) > 0.5*rangeQ: continue
    num = num + 1
    
    for i in range(numSteps):
        if x[i] > lenOnQpeak:
            y[i] = y[i] + 1
            break

for i in range(numSteps):
    output.write( '%8.3f %8d\n' % (x[i], y[i]) )    

print 'num = ', num  

plot( x, y )
# xlim( xmin = 6.8, xmax = 7.1 )
xlabel('Q, 2pi/d')
ylabel('Counts')
plotTitle = 'Peak profile vs. Q, Sapphire Run 3681'
title( plotTitle )
s1 = 'hkl = %d %d %d\ndeltaQ = %5.3f\ncyl length = %5.3f\ncyl radius = %5.3f' % \
    (h,k,l, deltaQ, rangeQ, radiusQ)
figtext(0.65, 0.7, s1)
grid(True)
savefig( 'Qprofile' )   # plot saved
show()    
    
  
    
    



