# $Date$
$ Path DataDirectoryString Path
Load Path&"GLAD4701.RUN"
Load Path&"GLAD4696.RUN"
Load Path&"GLAD4699.RUN"

empty_can_count = IntegGrp( M1_glad4701, 119, 0.0, 30000.0 ) 
vanadium_count = IntegGrp( M1_glad4696, 119, 0.0, 30000.0 ) 
sample_count = IntegGrp( M1_glad4699, 119, 0.0, 30000.0 ) 

H1_glad4696 = (sample_count/vanadium_count)*H1_glad4696 - (sample_count/empty_can_count)*H1_glad4701

H1_glad4699 = H1_glad4699/H1_glad4696
Sort( H1_glad4699, "Effective Position", true, false )

s_of_q = ToQ( H1_glad4699, 0.0, 30.0, 500 )
Display s_of_q

summed_s_of_q = SumAtt( s_of_q, "Effective Position", true, 0.0, 180.0 )
Display summed_s_of_q

for i in [0:11]

min=0.0 + i * 10
max=10.0+ i * 10

Display i
ds[i] = SumAtt( s_of_q, "Raw Detector Angle", true, min, max )

if i=0
  merged_ds = ds[i]
else
  merged_ds = Merge( merged_ds, ds[i] )
endif  

endfor

Display merged_ds
