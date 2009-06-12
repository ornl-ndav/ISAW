#     Blind shell around JBlind_Base
#
#  File: Wizard/TOF_SCD/Scripts_new/Blind.iss
#
#  @overview  This determines the orientation matrix from the Blind program 
#       or reads basis in Q from a file.  Blind takes a sequence of peaks and 
#        finds a Niggli-like  for these peaks( orientation matrix)
#  @algorithm If blind is  used, the peaks are sent through the Blind 
#     program.This is a program used at the IPNS division at Argonne National
#      Laboratory.

#  @assumptions The Peaks parameter must be a Vector of Peaks object
#  @param      Peaks      a vector of Peaks object
#  @param     method      Offers the following choices
#                         Blind- Enter Sequence Numbers
#                         Read from File- Enter filename in Input Orientation Matrix File
#                         Automatic - Just does it
#                         from Q Viewer-Allows user to select planes from reciprocal lattice viewer
#  @param      file1      The file with the orientation matrix
#  @param     Seq         The list of sequence numbers to use. Eg 33:36,47
#  @param     file       The filename to store the orientation  matrix 
#                          and the other cell parameters
#  @param     Max_dSpacing  Maximum d-Spacing
#  @param     ShowLog    If true blind.log will pop up in a window
#  @param     path       The path where output information goes

#                    NOTE, the file can be viewed from the view menu 
#                    using the view text submenu and selecting blind.log

#  @return   an orientation matrix either from blind or the file 

$Title= Initial Orientation Matrix  

# ------Parameters ---------------------
$Peaks   PlaceHolder                  Enter peaks

$useFile  BooleanEnable([False,1,5])  Use Matrix From File
$file1    LoadFile(${Data_Directory})       Input Orientation Matrix File ( .mat )    
$method    ChoiceList(["Blind","Automatic","Automatic w Lat Params", "from Q Viewer"])  Method to use

$Seq      IntArray               Sequence Numbers(Blind Method only)
$file     String                 Output Orientation Matrix File ( .mat ) 

$Max_dSpacing  Float(12)         Maximum d-Spacing
$ShowLog  Boolean( false)        Pop Up blind.log 

$path     DataDirectoryString(${Data_Directory})    Output Data Path 


#-------------  Code ----------------

matPath=path&file
if useFile

  X= readOrient( file1 )
  WriteMatrix( file, X)
  return X
  
endif

file = path & file

Status =[1,2,3,4]
MaxXtalLength = Max_dSpacing

if method =="Blind"

   X= JBlindB( Peaks, seq, file )
   
elseif method =="Automatic"
   P= VectorTo_floatArray([1,2,3,4])

   X=GetUBMatrix( Peaks, MaxXtalLength, P)
   Status = ToVec(P)

elseif method =="from Q Viewer"
   X = GetUBFrRecipLatPlanes( Peaks,MaxXTalLength,Status)
  
elseif method =="Automatic w Lat Params"
   PkFile =CreateExecFileName(getSysProp("user.home"),"ISAW/xxx.peaks")
   WritePeaks_new(PkFile,Peaks,false)
   Out1File =CreateExecFileName(getSysProp("user.home"),"ISAW/xxx1.peaks")
   Prompts=["a","b ","c","alpha","beta","gamma"]
   InitVals =[4.9,4.9, 5.4,90.,90.,120.0000 ]
  
   InputBox("Enter Crystal Parameters",Prompts, InitVals,[]);
   IndexPeaksWithOptimizer( PkFile,matPath,Out1File,InitVals[0],InitVals[1], InitVals[2],InitVals[3],InitVals[4],InitVals[5])
    X= readOrient( matPath )
    return X
endif

if method =="Blind"
  if ShowLog
  
      ViewASCII( path&"blind.log")
  
   
  elseif method == "Blind"
      Display "the file blind.log has the log information"
    
  endif
elseif method <>"Automatic w Lat Params"
  WriteMatrix(matPath, X )
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


