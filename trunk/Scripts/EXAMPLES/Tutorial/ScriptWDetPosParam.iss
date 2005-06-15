#          Demo of a script with a parameter with a PlaceHolder(opaque) data type
#@overview This script demonstrates the syntax to use if a parameter has a data type
#          that is not String, DataSet, Integer, Float, Vector(Array), or Boolean
#          -This parameter is INPUT only( unless a method changes part of its internals)
#          -ISAW Scripts are used a shells to get parameters correct.  This type of parameter
#              extends this feature
#
#@param  SampleOrientation  A Sample Orientation
#@param  Message     A Message

$SampleOrientation   PlaceHolder   Sample Orientation?
$Message             String        Enter Message
$category= HiddenOperator

return Message&" xxx"
