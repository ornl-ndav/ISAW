# Sum by Group ID and normalize with Solid angle HRCS macro
# $Date$
$  path               DataDirectoryString     Data File Path:
$  instrument         InstrumentNameString    Instrument Prefix(eg:hrcs)
$  sample_run         Integer		      Sample Run Number(3499, 3498 ...)
$  start_group        Integer                 Start Group ID to sum
$  end_group          Integer                 End Group ID to sum


#=============================
# Do file load of sample runs
#==============================
   load path&instrument&sample_run&".run", "sample"

#===============================================================================
# Sum & normalize by total solid angle the data from a range of detector groups
#===============================================================================
   summed_ds = SumAttNormSA( sample[1], "Group ID", true, start_group, end_group )
   send summed_ds
   tot_solid_ang = GetAttr(summed_ds, 0,"Total Solid Angle")
   Display(tot_solid_ang)


