#
#
$ Category = Operator, Generic, TOF_SAD, Scripts
SamplePath = "C:\\Argonne\\sand\\wrchen03\\"
TransSFile="C:/ISAW/SampleRuns/t1999019934.cf"
TransBFile="C:/ISAW/SampleRuns/t1993519934.cf"
EffFile="C:/ISAW/SampleRuns/efr19452A.dat"
SensFile="C:/ISAW/SampleRuns/sens19878A.dat"
$ qu   Qbins     Enter Q
#qu=[-.5,.5,-.5,.5]
SampleFile=SamplePath&"sand19990.run"
BackGroundFile=SamplePath&"sand19935.run"
CadmiumFile =SamplePath&"sand19936.run"
NeutronDelay=.0011
Scale=843000
Thick=.1
Xoff=.000725
Yoff= .006909
NQxBins=-200
NQyBins=-200

#------------------------- Code -----------------------
load SampleFile,"RUNSds"
load BackgroundFile,"RUNBds"
load CadmiumFile,"RUNCds"
TransS = ReadTransmission( TransSFile, 68) 
TransB= ReadTransmission(TransBFile,68)
Eff =Read3Col1D( EffFile,"Efficiency")
Sens =ReadFlood(SensFile, 128,128)

Res=Reduce_KCL(TransS,TransB,Eff,Sens[0],qu,RUNSds[0],RUNSds[1],RUNBds[0],RUNBds[1],RUNCds[0],RUNCds[1],NeutronDelay,Scale,thick,Xoff,Yoff,NQxBins,NQybins)

Display Res[0]
Display Res[1]
Display Res[2]
for i in [0:2]
  if  NQxBins < 0
     Print3Col1D(Res[i], SamplePath&GetField(Res[0], "Title")&".d1t","Reduce Results", NeutronDelay)
  else
     Print4Col2D1Chan( Res[i], SamplePath&GetField(Res[0], "Title")&".d1t")
  endif
endfor
Display "Finished"




