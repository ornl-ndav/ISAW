
$Title = RotateSnapDetectors
$Command = RotateSnapDetectors
$Category = Macros,Instrument Type, TOF_NSCD

$fileOrig   LoadFile(${ISAW_HOME}/InstrumentInfo/SNS/SNAP)    Original DetCal File
$filenew    SaveFile                                          Rotated DetCalFile
$RotBank1   BooleanEnable([true,1])                                Rotate Bank 1(dets 1-9)
$newAngl1   Float                                             New Angle for Bank1 center(degrees)
$RotBank2   BooleanEnable([true,1])                                Rotate Bank 2(dets 10-18)
$newAngl2   Float                                             New Angle for Bank2 center(degrees)


File1 =CreateExecFileName( getSysProp("user.home"),"ISAW/tmp/ttt.DetCal")

if   Not RotBank1
   File1 = fileOrig
endif

if Not RotBank2
   File1 = filenew
endif 

on error
if RotBank1
    RotateDetectors( fileOrig,5,File1,newAngl1,0,0,[1:9])
endif
end error
on error
if RotBank2

    RotateDetectors(File1,14,filenew,newAngl2,0,0,[10:18])
endif 
end error
