#
# Simple script to demonstrate concatenating strings.  The concatenation
# of the two parameter strings is displayed in the status pane, sent to
# the command window, and returned as the value of this script "operation".
#
#  @param x  the first  string to be concatenated
#  @param y  the second string to be concatenated
#
#  $Date$

$Title = Concatenate two Strings
$Category = Macros, Examples, Scripts ( ISAW )

$ x  String(default string) x =
$ y  String                 y =

#
# Show concatenated strings in status pane
#
Display x&y

#
# Send concatenated strings to command window 
#
Echo (x&y)

#
# return concatenated strings as value of this script
#
return( "result = "& x&y)
