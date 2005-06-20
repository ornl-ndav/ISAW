$Category=Macros,Utils,Examples
$iData         Integer(3)                                     Enter an Integer
$pi            Float(3.14159)                                 Enter a Float
$decision      Boolean(True)                                  Should I do it or not?
$username      String("Your Name")                            Enter user name
$datadir       DataDir                                        Enter Directory to save data
$inFile        LoadFile("/top/myInFile")                      Enter Input file
$outFile       SaveFile(/top/myOutFile)                       Enter Output file
$iName         InstName                                       Enter Instrument Name
$arr           Array(2.0,5.0,10.0)                            Enter Array Values
$iList         IntArray(1:49,52)                              Enter  values
$choice        ChoiceList(["Choice1","Choice2","Choice3"])    Select a choice
$radio         RadioButton(["RadioButton1","RadioButton2","RadioButton3"])   Pick One

Display     "Integer data = " & iData
Display     "Float Pi = " & pi
Display     "Boolean = " & decision
Display     "String - A user name: " & username
Display     "Data Directory: " & datadir
Display     "LoadFile: " & inFile
Display     "SaveFile: " & outFile
Display     "Instrument Name: " & iName
Display     "Array: " & arr
Display     "IntArray:  " & iList
Display     "Choice: " & choice
Display     "RadioButton: " & radio
