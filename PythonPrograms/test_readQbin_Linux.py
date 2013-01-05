#!/usr/bin/env python
"""
Test for the same events in the Q vector files from ISAW and Mantid.
"""

import struct

numberOfEventsToTest = 20

for i in range(numberOfEventsToTest):
    # Read the events from ISAW binary file--------------------------------
    events_directory = '/SNS/users/ajschultz/profile_fit5/Ni_5678/'
    srun = '5678'
    events_fname = events_directory + 'TOPAZ_' + srun + '_EventsToQ.bin'
    input = open(events_fname, 'rb')
    print 'The EventsToQ.bin file is ' + events_fname
    print ''
    numberOfEvents = 0
    while True:
        # if numberOfEvents == 1e05: break
        
        lineString = input.read(12)
        if lineString == "": break
        Qxi, Qyi, Qzi = struct.unpack('>fff', lineString)  # unpack binary data
        numberOfEvents = numberOfEvents + 1
        if numberOfEvents == i + 1:
            print 'Qxi Qyi Qzi = %10.3e %10.3e %10.3e   %d' % (Qxi, Qyi, Qzi, numberOfEvents)
            print ''
            break
        
    input.close()

    # Read the events from Mantid binary file--------------------------------  
    events_fname = events_directory + 'TOPAZ_' + srun + '_SaveIsawQvector.bin'
    #events_fname = events_directory + 'TOPAZ_' + srun + '_SaveIsawQvectorPreNexus.bin'
    input = open(events_fname, 'rb')
    print 'The EventsToQ.bin file is ' + events_fname
    print ''
    numberOfEvents = 0
    while True:
        # if numberOfEvents == 1e05: break
        
        lineString = input.read(12)
        if lineString == "": break
        Qx, Qy, Qz = struct.unpack('<fff', lineString)  # unpack binary data
        numberOfEvents = numberOfEvents + 1
        # if numberOfEvents > 20: break
        
        if abs(Qxi - Qx) < 0.001 and abs(Qyi - Qy) < 0.001 and abs(Qzi - Qz) < 0.001:
            print 'Qx Qy Qz = %10.3e %10.3e %10.3e    %d' % (Qx, Qy, Qz, numberOfEvents)
        
    print '\ntotal numberOfEvents is ' + str(numberOfEvents)
    print '================================'
    
    input.close()

print '\nAll done!' 

