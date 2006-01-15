#     Blind shell around JBlind_Base


$Peaks   PlaceHolder   Enter peaks
$Seq     IntArray      Sequence numbers
$file    SaveFile    Matrix file name
$useFile  Boolean(false)  Use Matrix from file


if useFile
  return readOrient( file)



endif

return JBlindB( Peaks,seq,file)



