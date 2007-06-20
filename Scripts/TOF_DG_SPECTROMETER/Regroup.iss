#
# @overview  This operator will form a new DataSet consisting of sums of
#            sets of Data blocks from the specified DataSet.  The sets
#            of Data blocks are specified in a two dimensional array
#            passed in as a parameter.  "Row" zero of the array contains
#            the list of group IDs to be summed to form the first new group. 
#            The second "row" contains the list of group IDs to be summed
#            to form the second group, etc.  The rows of the array do NOT
#            have to have the same length.  That is, this array can be a
#            "ragged" array.
#
# @param  ds   The DataSet to be regrouped
#
# @param  IDs  The list of lists of group IDs that will be used to form
#              the groups.  IDs[0] is a list of group IDs for the first
#              group,, IDs[1] is a list of group IDs for the second
#              group, etc.
#
# @author Dennis Mikkelson
# $Date$
#
$Category = Macros, DataSet, Edit List
$Command = Regroup

$ ds   DataSet                        Select DataSet to regroup
$ IDs  Array([[3,5,7,9],[4,6,10,14]]) List of lists of IDs to group 

#
# Clear any selection flags and make an empty copy of the original DataSet,
#
ClearSelect( ds )
new_ds = DelSel( ds, false, true )

#
# For each new group, select the members of the new group in the 
# original DataSet, sum the selected Data blocks and merge them
# with the new DataSet being constructed.  Finally, clear the
# selected flags and proceed to select and sum the Data blocks
# in the next group.
#

num_groups = ArrayLength(IDs)

for i in [0:num_groups-1]
  ID_string = VectorToIntListString( IDs[i] )
  SelectByID( ds, ID_String, "Set Selected" )
  temp_ds = SumSel( ds, true, true )
  ClearSelect( ds )
  new_ds = Merge( new_ds, temp_ds )
endfor

ClearSelect( new_ds )

return new_ds
