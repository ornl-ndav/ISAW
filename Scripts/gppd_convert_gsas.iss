# 
# Script to load and save runfiles as gsas files
# $Date$

$ run_numbers         Array                  Enter run numbers like [1,2:5]
$ inpath              DataDirectoryString    Data Directory
$ outpath             DataDirectoryString    Output Directory
$ merge_sides         Boolean(true)          Group Sides

# parameters for the instrument
instrument="gppd"
ds_num=1
mds_num=0

# fix the path
inpath=inpath&"/"
outpath=outpath&"/"

# loop over the run_numbers
for i in run_numbers
   file_name = inpath&instrument&i&".run"
   Echo("Loading "&file_name)
   load file_name,"ds"
   if merge_sides
     # group 145deg banks
     ds[ds_num]=TimeFocusGID(ds[ds_num],"2,8",145.405452,1.5,false)
     ds[ds_num]=GroupDiffract(ds[ds_num],"2,8",2,false)
     # group 90deg banks
     ds[ds_num]=TimeFocusGID(ds[ds_num],"3,9",89.742416,1.5,false)
     ds[ds_num]=GroupDiffract(ds[ds_num],"3,9",3,false)
     # group 53deg banks
     ds[ds_num]=TimeFocusGID(ds[ds_num],"4,10",53.149233,1.5,false)
     ds[ds_num]=GroupDiffract(ds[ds_num],"4,10",4,false)
     # move 20deg bank
     ds[ds_num]=TimeFocusGID(ds[ds_num],"11",19.39,1.5,false)
   endif
   SaveGSAS(ds[mds_num],ds[ds_num],outpath&instrument&i&".gda",false,true)
endfor
