#
# Define the variables for the script
# $Date$
#
$ filepath   DataDirectoryString     Path
$ filename   String(sepd17123.run)   File name
$ neutfrac   Float(0.0074)           Delayed neutron fraction
$ dn_t_min   Float(3000.0)           Tmin for monitor integration
$ dn_t_max   Float(25000.0)          Tmax for monitor integration

#
#  Bring in the run file and Display the sample data
#
runfile=filepath&filename
Display "Focus test 3: "&runfile
Load runfile,"ds"
#Display ds[0]
Display ds[1]
#Send ds[1]

#
#  Calculate and display the up stream monitor sum.  
#  NOTE: the time interval is the start time of the first bin 
#        used in the integral to the end time of the last bin
#        used in the integral.
mon_id = UpMonitorID(ds[0])
sum_up_raw = IntegGrp( ds[0], mon_id, dn_t_min, dn_t_max )
Display "Sum up raw = "&sum_up_raw

#
#  Calculate and display the delayed neutrons value.
#
nbins=NumBins(ds[0],mon_id,dn_t_min,dn_t_max)
delayed_neutrons = neutfrac * sum_up_raw / nbins
Display "Delayed neutrons ="&delayed_neutrons

#
#  Subtract the delayed neutrons and extrapolate a quadratic over the
#  interval [1500, 2500] microseconds, using data from  
#
mon_spec = Sub( ds[0], delayed_neutrons, true );
mon_spec=FitPoly(mon_spec, mon_id, 2500.0, 4000.0, 2, 1500.0, 2500.0, 0 );

#
#  Focus the extrapolated monitor spectrum to the time of flight range
#  (and position) of the various detector banks. The banks are
#  numbered 1, 2, 3, 5, 7
#
foc_1 = IncSpecFocus(mon_spec, mon_id, ds[1], 1)
mon_spec = Merge( mon_spec, foc_1 )
foc_2 = IncSpecFocus(mon_spec, mon_id, ds[1], 2)
mon_spec = Merge( mon_spec, foc_2 )
foc_3 = IncSpecFocus(mon_spec, mon_id, ds[1], 3)
mon_spec = Merge( mon_spec, foc_3 )
foc_5 = IncSpecFocus(mon_spec, mon_id, ds[1], 5)
mon_spec = Merge( mon_spec, foc_5 )
#foc_7 = IncSpecFocus(mon_spec, mon_id, ds[1], 7)
#mon_spec = Merge( mon_spec, foc_7 )

#
# Merge the focussed monitor spectra with the raw monitor spectrum into one
# DataSet and display it.
# 
Display mon_spec 
#Send mon_spec

#
# Normalize the sample spectra from the banks by dividing by the foccused 
# monitor spectra and display the normalized spectra.
#
normalized_ds = Div( ds[1], mon_spec, true );
Display normalized_ds
Send normalized_ds

#
# Convert to D spacing and display.
#
#d_ds = ToD( normalized_ds, 0.6, 5.0, 0 );
#Display d_ds
#Send d_ds
