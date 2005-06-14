##############################
# Integer Data
##############################
Display "*********************************"
iData1 = 2
iData2 = iData1 * 5

Display "Integer Data 1 = " & iData1
Display "Integer Data 1 * 5 = " & iData2
#############################
# Integer Data
##############################
Display "*********************************"
pi = 3.14159
area = iData1 * iData1 * pi

Display "Area of circle with radius " & iData1 & " = " & area

############################
# String data
############################
Str1 = "Hello "
Str2 = "World"
Str3 = Str1 & Str2

Display "String 1 = " & Str1
Display "String 2 = " & Str2
Display "String 1 combined with String 2= " & Str3

iName = "glad"
runn = 6942
filename = iName & runn & ".run"
Display "Instrument name: " & iName
Display "Run Number: " & runn
Display "Filename: " & filename
##########################################
# Arrays
##########################################
iArr1 = [1,2,3,4]
iArr2 = [5:8]

sumArr = iArr1 + 5
multArr = iArr1 * 5
addArr = iArr1 + iArr2
combArr = iArr1 & iArr2

Display "Integer Array 1: " & iArr1
Display "Integer Array 2: " & iArr1
Display "Integer Array 1 + 5: " & sumArr
Display "Integer Array 1 * 5: " & multArr
Display "Integer Array 1 + Integer Array 2: " & addArr
Display "Combine Integer Array 1 & Integer Array 2:" & combArr
 
fArr1 = iArr1 * 1.0
Display "Integer array 1 converted to floats: " & fArr1

Display "Third element of float array: " & fArr1[2]

Display "Not all members of an array have to be of the same type"
arbArr = [3, 4.6, "Goodbye"]
Display "An arbitrary array: " & arbArr
Display "One element at a time"
for a in [0:2]
	Display arbArr[a]
endfor

########################################
# DataSets
########################################
datadir = getSysProp("Data_Directory")
fs = fileSep()
extFile = datadir & fs & filename

Display "Opening file: " & extFile

n = Load extFile, "runFile"
Display "Loaded " & " datasets"
Display runFile[1]
Send runFile[1]

tempDS = runFile[1] * 6

Display tempDS
