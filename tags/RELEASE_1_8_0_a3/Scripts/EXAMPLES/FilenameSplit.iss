
$Category = Macros, Examples, Scripts ( ISAW )

$Title=Demonstrate splitting filenames.

$filename      LoadFileString    Enter File Name

fParts = fSplit(filename)

numParts = ArrayLength(fParts)
Display "numParts: " &numParts
for ii in [0:numParts-1]
   Display fParts[ii]
Endfor

fSep = fileSep()

if numParts == 2
   Display fParts[0] & fSep & fParts[1] & "     This file has no extension"
else
   Display fParts[0] & fSep & fParts[1] & "." & fParts[2]
endif

myExtension = "myExt"

Display fParts[0] & fSep & fParts[1] & "." & myExtension

   
