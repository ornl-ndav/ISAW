
$Category=Macros, Examples, Scripts ( ISAW )

$filename    LoadFileString    Enter File Name

Display filename
Load filename, "nn"

idMap=[[43:82],[3:12,14:42],[103:122],[83:102],[267:410],[123:266],[555:698],[411:554]]
refAng=[140.0,-140.0,90.0,-90.0,44.0,-44.0,22.0,-22.0]
refLen=[1.5,1.5,1.5,1.5,1.5,1.5,1.5,1.5]
#refLen=[15.688,15.688,15.688,15.688,15.688,15.688,15.688,15.688]
res=[0.0004,0.0004,0.0008,0.0008,0.0016,0.0016,0.0032,0.0032]
refInitFp = 14.188
newData = SumLog(nn[1], idMap, refAng,refLen,res, refInitFp)

Send newData
