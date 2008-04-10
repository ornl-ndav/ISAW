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

#$useFile  BooleanEnable([False,1,3])  Use Matrix From File
$method    ChoiceList(["Blind","Read from File","Automatic","from Q Viewer"])  Method to use
$file1    LoadFile               Input Orientation Matrix File ( .mat )    

$Seq      IntArray               Sequence Numbers(Blind Method only)
$file     SaveFile               Output Orientation Matrix File ( .mat ) 

$ShowLog  Boolean( false)        Pop Up blind.log 

$path     DataDirectoryString    Output Data Path 


#-------------  Code ----------------

if method == "Read from File"

  X= readOrient( file1 )
  WriteMatrix( file, X)
  return X
  
endif

Status =[1,2,3,4]
MaxXtalLength = 12.0

if method =="Blind"

   X= JBlindB( Peaks, seq, file )
   
elseif method =="Automatic"
   P= VectorTo_floatArray([1,2,3,4])

   X=GetUBMatrix( Peaks, MaxXtalLength, P)
   Status = ToVec(P)

elseif method =="from Q Viewer"
   X = GetUBFrRecipLatPlanes( Peaks,MaxXTalLength,Status)
   
endif

if method =="Blind"
  if ShowLog
  
      ViewASCII( path&"blind.log")
  
   
  elseif method == "Blind"
      Display "the file blind.log has the log information"
    
  endif
else
  WriteMatrix(file, X )
  Display "Fit Stats for Result"
  n=ArrayLength(Status)-1
  for i in [0:n] 
     Display "Fraction within "&(10*(i+1))&"% of Planes="&Status[i]
  
  endfor
  if showLog
    ViewASCII(file)
  endif
endif


Display "------------ Finished with the Blind Form-------------------"

return X


