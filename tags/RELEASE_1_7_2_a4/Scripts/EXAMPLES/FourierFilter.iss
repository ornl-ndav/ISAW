#  FourierFilter.iss
#
#  This script demonstrates a low-pass Butterworth filter applied to
#  a single vanadium spectrum.  Several different cutoff frequencies are
#  used.  For each cutoff frequency, the filtered and original Data blocks
#  are merged into one DataSet so that the results can be compared.
#
#  $Date$

$Category = Macros, Utils, Examples 

#
#  1. Load the data from a simple ASCII file and mark the first group as
#     selected, so it shows up int the Selected Graph View
#
ds = LoadASCII( "/home/dennis/VANADIUM_DATA/8094_5nrm.txt" )
SelectGroups( ds, "Group ID", 1,1, "Between Max and Min", "Set Selected")

#
#  2. Make an array of different "normalized" cutoff frequencies to try
#
cutoff = [ 0.1, 0.05, 0.02, 0.01, 0.005, 0.002 ]

#
#  3. For the original DataSet and each possible cutoff value, construct
#     a title and display the DataSet.  If a filter was used, make the
#     the Group ID of the filtered Data block 2 and merge in the original
#     Data, so that both the original and filtered Data appear on the same 
#     graph.
#
for i in [-1:5]
  ds2 = ds
  if i = -1
    title = ["8094 Unfiltered"]
  else
    title = ["8094 cutoff = "&cutoff[i]]
    LowPassFilter( ds2, cutoff[i], 2 )
    SetAttr( ds2, 0, "Group ID", 2 )
    ds2 = Merge(ds2, ds)
  endif

  SetField( ds2, "Title", title )
  Display ds2, "Selected Graph View"
endfor
