#             Efficiency Calculation
# @overview - Calculates the efficiency ratio between area detectore a disk 
#    around the center and the first monitor
#
# @param  CadmiumFileName -The name of run file from the Cadmium mask run. Upstream and 
#          beam stop monitors have been removed
# @param  SensitivityFileName- The name of the .Dat file storing the sensitivity information
# @param  SaveFileName - The name of the Dat file to save the resultant Efficiencies.
# @param XOFF - the offset(in cm) in the det x direction that the beam is from the detector center
# @param YOFF - the offset(in cm) in the det y direction that the beam is from the detector center
# @param Radius - The radius of detectors about the center to use for the calculations
# @param DelayNeutron - The fraction of neutrons that are delayed

# @return   Success or an error string.  A Dat file containing the efficiencies is written  

$ CadmiumFileName     LoadFileString("/IPNShome/sand/data/sand22227.run") Cadmium with BS removed Filename
$ SensitivityFileName LoadFileString("/IPNShome/sand/GeorgeUser/sens22205.dat")   Sensitivity Dat File
$ SaveFileName        SaveFileString("/IPNShome/sand/GeorgeUser/EFR22227.dat")   Save Dat file to 
$ XOFF                Float(0.1508991)                         Enter Xoffset of beam in cm
$ YOFF                Float(0.7103308)                         Enter Yoffset of beam in cm
$ Radius              Float( 5.0)                              Radius to use
$ DelayNeutron        Float( .0011)                            Delayed Neutron Fraction

$ Title = Efficiency From File
$ Command = Efficiency
$ Category = Macros, Instrument Type, TOF_NSAS 


n= load( CadmiumFileName, "CadDS")

SensDS = ReadFlood( SensitivityFileName, 128,128)

DS = EffRatio( CadDS[1], CadDS[0], SensDS[0], XOFF, YOFF,Radius, DelayNeutron)

Print3Col1D( DS[1],SaveFileName, "Efficiency",DelayNeutron)

Display "Finished"

SelectGroups( DS[1], "Group ID",0.5,1.5,"Between Max and Min", "Set Selected")
Display DS[1], "Selected Graph View"
send DS[1]

return "Finished"

