$Title=Merge Fullprof data and Display
#Ashfia Huq September 2008
# Script to load and merge ascii files with tof, intensity, esd.   
# $Date: 2004/01/16 16:03:04 $

#===================================================================================================================

#$instrument		InstrumentNameString    	Instrument
$run_numbers		Array([1471:1475])		Enter run # like [1:3]
$path			DataDirectoryString(~3ah/)	Input run path
$Bank			Integer(1)			Input bank #
$Num_pts        	Integer(6000)                   Number of points in file
$One_D_plot		Boolean(false)			Draw 1D plot?

#===================================================================================

for i in run_numbers

#======================== LOAD DATA ================================================
  	file_name = path&i&"b"&Bank&".dat"
  	Echo(file_name)
  	rds=LoadASCII(file_name,6,Num_pts,1,2,3)

#======================= Merge Data =================================================
#  dvds = ToD(newData, dmin, dmax, 0)
  on error
  merged_ds = Merge(merged_ds, rds)
 else error
  merged_ds = rds
 end error
#====================================================================================

endfor
send merged_ds
Display merged_ds
send merged_ds
#if One_D_plot == true
#SelectGroups(merged_ds, "Group ID", bankNum, bankNum, "Between Max and Min", "Set Selected")
 # 	display(merged_ds,"Selected Graph View")

