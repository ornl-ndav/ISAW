#
# Demo script to fit polynomial of degree 4 to a double peak in a spectrum.
# This is probably NOT a good idea, but it does show that the polynomial 
# fitting routines work reasonably.  The result is a DataSet with two spectra
# with Group ID 54.  One has the complete raw data for Group ID 54, the other
# has the rightmost double peak replaced by a polynomial of degree 4. Zoom in on
# the image of the rightmost double peak of both spectra to see this better.
#
# $Date$

Load "C:\Documents and Settings\worlton\Desktop\ICANS\SampleRuns\gppd12358.run","ds"

spec_id = 54 
start   = 14921.0
end     = 15292.0
degree  = 4 

new_ds=FitPoly( ds[1], spec_id, start, end, degree, start, end, 0 );

new_ds = Merge( ds[1], new_ds )

#Sort( new_ds, "Group ID", true, false );

new_ds=ExtAtt( new_ds, "Group ID", true, 54.0, 54.0 );

Display new_ds




