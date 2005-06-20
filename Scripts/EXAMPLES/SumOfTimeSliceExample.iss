$ds    DataSet   Select Data Set
$minID   Integer(5)   Minimum group ID
$maxID   Integer(7229)   Maximum group ID
$Title=Calculate sum area pixels in a time slice
$Category=Macros,Utils,Examples

#Select pixels that are in the time slice
SelectGroups(ds, "Group ID", minID, maxID, "Between Max and Min", "Set Selected")
#Sum over the selected Pixels
tsSum=SumSel(ds, true, true)

#Diplay the sum with the selected graph view
Display tsSum, "SELECTED_GRAPH"
