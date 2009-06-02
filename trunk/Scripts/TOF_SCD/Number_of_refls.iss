# Script to calculate the number of possible reflections and the
# number of required observations.
# A. Schultz, June, 2009


#
$ Volume	Float(245)		Enter unit cell volume, Angstroms^3
$ NAtoms	Integer(7)		Enter of independent atoms (nearest integer)
$ dmin		Float(0.7)		Enter the minimum d-spacing, Angstroms
$ Sym_factor	Integer(4)		Enter the symmetry factor (triclinic=2, monoclinic=4, etc.)

pi = ACos(-1.0)

# Volume of the sphere of reflections
Sphere = (4.0/3.0) * pi * (1.0/dmin)^3

# Total number of reflections in the sphere
Total_refls = Sphere * Volume

display "Total number of reflections in the sphere of reflection = " & Total_refls

# Number of unique reflections in hemisphere, quadrant, octant, etc.
Unique_refls = Total_refls/Sym_factor

display "Number of unique reflections = " & Unique_refls
display ""

display "Assume about 10 variables per atom and a goal of 10 observations per parameter."
N_obs = 100.0 * Natoms
Ratio = 100.0 * N_obs/Unique_refls
display "Percent of observed reflections needed for an obs/parameter ratio of 10 is " & Ratio &"%"


