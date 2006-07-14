#     Blind shell around JBlind_Base
#
#  File: Wizard/TOF_SCD/Scripts_new/Blind.iss
#
#  @overview  This determines the orientation matrix from the Blind program 
#       or readsbasis in Q from a file.  Blind takes a sequence of peaks and 
#        finds a Niggli-like  for these peaks( orientation matrix)
#  @algorithm If blind is  used, the peaks are sent through the Blind 
#     program.This is a program used at the IPNS division at Argonne National
#      Laboratory.

#  @assumptions The Peaks parameter must be a Vector of Peaks object
#  @param      Peaks      a vector of Peaks object
#  @param      useFile    if true the resultant orientation matrix will
#                          come from the MatFilename, not from Blind
#  @param      file1      The file with the orientation matrix
#  @param     SeqNums    The list of sequence numbers to use. Eg 33:36,47
#  @param     file       The filename to store the orientation  matrix 
#                          and the other cell parameters
#  @param     ShowLog    If true blind.log will pop up in a window
#  @param     path       The path where output information goes

#                    NOTE, the file can be viewed from the view menu 
#                    using the view text submenu and selecting blind.log

#  @return   an orientation matrix either from blind or the file 

$Title= Blind ( Get Initial Orientation Matrix ) 

# ------Parameters ---------------------
$Peaks   PlaceHolder                  Enter peaks

$useFile  BooleanEnable([False,1,3])  Use Matrix From File
$file1    LoadFile               Input Orientation Matrix File ( .mat )    

$Seq      IntArray               Sequence Numbers For Blind
$file     SaveFile               Output Orientation Matrix File ( .mat ) 

$ShowLog  Boolean( false)        Pop Up blind.log 

$path     DataDirectoryString    Output Data Path 


#-------------  Code ----------------

if useFile

  return readOrient( file1 )
  
endif


X= JBlindB( Peaks, seq, file )


if ShowLog
 
   ViewASCII( path&"blind.log")
   
else
    Display "the file blind.log has the log information"
    
endif



Display "------------ Finished with Blind -------------------"

return X


