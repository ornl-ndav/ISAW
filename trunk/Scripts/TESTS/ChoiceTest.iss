# This Script demonstrates the "Choice" Parameter Data Type
# The result will ALWAYS be a string
# $Date$

$Category = Macros, Utils, Tests 

#
# The choice is a parameter of this script
#
$C      Choice(["Yes", "No","Maybe",3])     Enter Choice

#
# Dump the choice out to the command window, staus pane, and return it as
# the value of this script.
#
Echo(C)
Display "Choice ="&C
Return(C)
