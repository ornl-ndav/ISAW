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
#  @param     filename     a file with peaks
#  @param     method      Offers the following choices
#                         Blind- Enter Sequence Numbers
#                         Read from File- Enter filename in Input Orientation Matrix File
#                         Automatic - Just does it
#                         from Q Viewer-Allows user to select planes from reciprocal lattice viewer

#  @param     Seq        The list of sequence numbers to use. Eg 33:36,47
#  @param     path       The path where output information goes
#  @param     file       The filename to store the orientation  matrix 
#                          and the other cell parameters
#  @param     Max_dSpacing  Maximum d-Spacing

#                    NOTE, the file can be viewed from the view menu in the SCD Wizards
#                    using the view text submenu and selecting blind.log

#  @return   an orientation matrix either from blind or the file 

$Title= Initial Orientation Matrix  
$Command = FindOrientationMatrix
$category=operator,Instrument Type,TOF_NSCD,NEW_SNS


# ------Parameters ---------------------
$filename LoadFile("${Data_Directory}")                         Filename for Peaks

$method    ChoiceList(["Blind","Automatic","from Q Viewer"])  Method to use

$Seq      IntArray                                            Sequence Numbers(Blind Method only)

$path     DataDirectoryString("${Data_Directory}")             Output Data Path for log file
$file     String                                               Output Orientation Matrix File ( .mat ) 

$Max_dSpacing  Float(12)                                     Maximum d-Spacing



#-------------  Code ----------------

Peaks = ReadPeaks( filename)
ShowLog = true
file = path&file

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


