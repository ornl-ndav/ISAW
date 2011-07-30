#     Blind shell around JBlind_Base
#
#  File: Wizard/TOF_SCD/Scripts_new/Blind.iss
#
#  @overview  This determines the orientation matrix from with options to 
#  use the Blind program , read it from an external file, calculate it 
#  automatically using a Rossman type alogrithm or one based on given 
#  lattice parameters, or use an interactive viewer of the peaks in Q space.
#
#   Blind takes a sequence of peaks and  finds a Niggli-like  for these 
#      peaks( orientation matrix)
#
#  @algorithm If blind is  used, the peaks are sent through the Blind 
#     program.This is a program was used at the IPNS division at Argonne
#      National Laboratory.

#  @assumptions The Peaks parameter must be a Vector of Peaks object
#  @param      Peaks      a vector of Peaks object
#  @param     useFile     if true an outside file will be read and passed through
#  @param     FileName1   The name of the above file
#  @param     method      Offers the following choices
#                         Blind- Enter Sequence Numbers
#                         Automatic - Just does it
#                         Auto w Lattice Params-Uses lattice parameter info
#                         from Q Viewer-Allows user to select planes from  reciprocal lattice viewer
#  @param     Seq         The list of sequence numbers to use. Eg 33:36,47

#  @param     Max_dSpacing  Maximum d-Spacing
#  @param     Min_dSpacing  Minimum d-Spacing
#  @param     MinDegree_between_directions     Minimum degrees between initial searching directions
#  @param     LatParams    The lattice parameters( angles are in degrees)
#                          (For Auto w Lattice Params method)
#  @param     file       The filename to store the orientation  matrix 
#                          and the other cell parameters
#  @param     ShowLog    If true blind.log will pop up in a window

#  @param     path       The path where output information goes

#                    NOTE, the files can be viewed from the view menu 
#                    using the view text submenu and selecting blind.log

#  @return   an orientation matrix either from blind or the file 

$Title= Initial Orientation Matrix  

# ------Parameters ---------------------
$Peaks   PlaceHolder                  Enter peaks

$useFile  BooleanEnable([False,1,5])  Use Matrix From File
$file1    LoadFile(${Data_Directory})       Input Orientation Matrix File ( .mat )    
$method    ChoiceList(["Blind","Automatic","new Automatic","Auto w Lattice Params", "from Q Viewer"])  Method to use

$Seq      IntArray               Sequence Numbers(Blind Method only)

$Max_dSpacing  Float(12)         Maximum d-Spacing

$Min_dSpacing  Float(1  )         Minimum d-Spacing
$MinDegree_between_directions  Float(3)   Minimum degrees between directions
$LatParams   Array([4.9,4.9, 5.4,90.,90.,120.0000 ])           Enter Lattice Parameters(Auto w Lat..)
$file     String                 Output Orientation Matrix File ( .mat ) 

$ShowLog  Boolean( false)        Pop Up log info 

$path     DataDirectoryString(${Data_Directory})    Output Data Path 


#-------------  Code ----------------

matPath=path&file
if useFile

  X= readOrient( file1 )
  WriteMatrix( file, X)
  if ShowLog
     ViewASCII( file)
  endif
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
   
  WriteMatrix( file, X)
  
  
elseif method =="Auto w Lattice Params"
#   ClearFiles("xxx","peaks")
#   ClearFiles("xxx1","peaks")
#   PkFile =CreateExecFileName(getSysProp("user.home"),"ISAW/tmp/xxx.peaks")
#   WritePeaks_new(PkFile,Peaks,false)
#   Out1File =CreateExecFileName(getSysProp("user.home"),"ISAW/tmp/xxx1.peaks")
    
#   Display "PeakFile"&PkFile
#   Display "Peak1File"&Out1File
#   Res =IndexPeaksWithOptimizer( PkFile,matPath,Out1File,LatParams[0],LatParams[1],LatParams[2],LatParams[3],LatParams[4],LatParams[5])
#   Display Res
#    X= readOrient( matPath )
    
#    WriteMatrix( file, X)
   
#    if showLog
#       ViewASCII(file)
#    endif
#              -------------------- new Auto with indexing -------------------
   
    Arg1 = Convert2Tran3D([1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0])
    Qvecs =getPeakQVals(Peaks)
    Seq1 =IntListToVector(Seq)
    NN= ArrayLength(Seq1)
    if NN > 0
       NN= Seq1[0]
    else
       NN = -1
    endif
   
    RR = Find_UB(Arg1,Qvecs, LatParams[0],LatParams[1],LatParams[2],LatParams[3],LatParams[4],LatParams[5],.12,NN,15,MinDegree_between_directions)
    
    X = Convert2floatArrayArray(Arg1)
    WriteMatrix( file, X)
elseif  method =="new Automatic"
    Arg1 = Convert2Tran3D([1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0])
    Qvecs =getPeakQVals(Peaks)
    Seq1 =IntListToVector(Seq)
    NN= ArrayLength(Seq1)
    if NN > 0
       NN= Seq1[0]
    else
       NN = -1
    endif
    RR= Find_UB( Arg1,Qvecs,Min_dSpacing,Max_dSpacing,.12,NN,15,MinDegree_between_directions )
    X = Convert2floatArrayArray(Arg1)
    WriteMatrix( file, X)
endif

if method =="Blind"
  if ShowLog
  
      ViewASCII( path&"blind.log")
  
   
  elseif method == "Blind"
      Display "the file blind.log has the log information"
    
  endif
else
  if showLog
    ViewASCII(file)
  endif
  if method == "Automatic" OR method=="from Q Viewer"
     Display "Fit Stats for Result"
     n=ArrayLength(Status)-1
     for i in [0:n] 
        Display "Fraction within "&(10*(i+1))&"% of Planes="&Status[i]
  
     endfor
  endif
 
endif


Display "------------ Finished with the Blind Form-------------------"

return X


