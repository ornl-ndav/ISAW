#     Blind shell around JBlind_Base
#  This adds the functionality of reading in an existing file
#  This is used as a form of a wizard
#  @overview Blind takes a sequence of peaks and finds a Niggli-like basis in Q 
#     for these peaks( orientation matrix)
#  @@algorithm The peaks are sent through the Blind program. This 
#    is a program used at the IPNS division at Argonne National
#    Laboratory for this task.");
#  @assumptions The Peaks parameter must be a Vector of Peaks object
#  @param  Peaks a vector of Peaks object
#  @param  SeqNums- The list of sequence numbers to use. Eg 33:36,47,56
#  @param  MatFilename- The filename to store the orientation  matrix 
#             and the other cell parameters 
#  @param  useFile  if true the resultant orientation matrix will come from
#                   the MatFilename, not from Blind


$Peaks   PlaceHolder   Enter peaks
$Seq     IntArray      Sequence numbers
$file    SaveFile    Matrix file name
$useFile  Boolean(false)  Use Matrix from file


if useFile
  return readOrient( file)
endif

return JBlindB( Peaks,seq,file)



