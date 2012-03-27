#  detectorToQ.py
"""Converts detector and TOF channels to q vector.
A. J. Schultz
March 2012"""

import numpy
import math

width = 15.819
height = 15.819
detd = 45.5

# center = numpy.zeros(3)
# base = numpy.zeros(3)
# up = numpy.zeros(3)
center = numpy.array([-36.6976,  -24.1113,  -11.9240])
base = numpy.array([-0.13787,  0.59975, -0.78822])
up = numpy.array([-0.57490,  0.59976,  0.55658])

normal = numpy.zeros(3)
normal = numpy.cross(base, up)

col = 28.04
row = 207.13

colcm = (col - 128.0) * width / 256.0
rowcm = (row - 128.0) * height / 256.0

realVec = numpy.zeros(3)

realVec = center + (colcm * base) + (rowcm * up)
L2 = math.sqrt(numpy.dot(realVec, realVec))
print 'L2 = ', L2

cos2theta = realVec[2] / L2
two_theta = math.acos(cos2theta)
print 'two_theta = ', two_theta

wl = 1.392896
theta = 0.5 * two_theta
dsp = wl / (2.0 * math.sin(theta))
print 'dsp = ', dsp

qdist = 1.0 / dsp

factor = (1.0 / wl) / L2

recipVec = numpy.zeros(3)
recipVec = factor * realVec
# qVec = [recipVec[0], recipVec[1], (recipVec[2] - (1.0 / wl))]
qVec = [recipVec[0], recipVec[1], (recipVec[2] - (1.0 / wl))]
print 'qVec = ', qVec
qVec = [qVec[0]*2.0*math.pi, qVec[1]*2.0*math.pi, qVec[2]*2.0*math.pi]
print '2 pi * qVec = ', qVec
qdist = math.sqrt(numpy.dot(qVec, qVec))
print 1.0/qdist






