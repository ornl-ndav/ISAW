#     Blind shell around JBlind_Base

#  File: Wizard/TOF_SCD/Scripts_new/Blind.iss
#
#  @overview  This determines the orientation matrix from the Blind program or reads
#       it in from a file.  Blind takes a sequence of peaks and finds a Niggli-like basis in Q 
#        for these peaks( orientation matrix)
#  @algorithm If blind is  used, the peaks are sent through the Blind program.
#     This is a program used at the IPNS division at Argonne National
#    Laboratory for this task.
#  @assumptions The Peaks parameter must be a Vector of Peaks object
#  @param  Peaks a vector of Peaks object
#  @param  useFile  if true the resultant orientation matrix will come from
#                   the MatFilename, not from Blind
#  @param file1    The name of the file that has the orientation matrix
#  @param  SeqNums- The list of sequence numbers to use. Eg 33:36,47,56
#  @param  file- The filename to store the orientation  matrix 
#             and the other cell parameters
#  @param path   The path where output information goes
#  @param ShowLog  If true blind.log will pop up in a window
#                   NOTE, the file can be viewed from the view menu 
#                   using the view text submenu and selecting blind.log
#  @return   an orientation matrix either from blind or the file 

$Title= Blind ( Get Initial Orientation Matrix ) 
$Peaks   PlaceHolder   Enter peaks

$useFile  BooleanEnable([False,1,2])  Use Matrix From File
$file1    LoadFile               Input Orientation Matrix File ( .mat )    
$Seq      IntArray               Sequence Numbers For Blind
$file     SaveFile               Output Orientation Matrix File ( .mat ) 
$path     DataDirectoryString    Output Data Path 
$ShowLog  Boolean( false)        Pop Up blind.log 

if useFile
  return readOrient( file)
endif

X= JBlindB( Peaks, seq, file)

if ShowLog
 
   ViewASCII( path&"blind.log")
   
else
    Display "the file blind.log has the log information"
endif

   Display "To see cell parameters, etc.  select View then matrix file in the menu"
Display "------------ Finished with Blind -------------------"

return X


