#
# Demo script to fit polynomial of specified degree to various portions of
# a monitor spectrum.  The polynomial replaces the portion of the spectrum
# specified.  A new DataSet is formed containing all of the modified spectra.
#
# To see the limitations of the algorithms used, change the degree to 10. The
# first and last case evaluate the polynomial outside of the data interval.  
# This extrapolation is very unstable and the 10th degree polynomials take on
# very large values.  On the interior of the data interval, the quality of the
# polynomial approximation degrades due to rounding errors as the interval used
# moves to larger TOF values.  The later modified spectra show a lot of "noise"
# in the evaluation of the polynomial.  Considering that the values are 
# obtained by adding and subtracting quantities and that the largest power of
# t is on the order of 10^43 while the result is values on the order of 10^3, 
# it's actually surprising that results are as good as they are. 
#
# $Date$

$ filename LoadFileString Runfile

Load filename,"ds"
degree=2

for i in [2:27]
  start = 1000.0 * i
  end   = 1000.0 * (i+1)
  temp_ds=FitPoly( ds[0], 75, start,  end, degree, start, end, 0 );
  ds[0]=Merge( ds[0], temp_ds )
endfor

#Display ds[0]
send ds[0]




