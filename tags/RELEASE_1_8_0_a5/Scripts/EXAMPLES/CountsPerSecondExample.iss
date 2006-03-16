#@overview  calculate the total counts per second of detectors in a dataset
$ds  DataSet     Choose DataSet

$Title=Example of calculating data rate on detectors

$Category = Macros, Examples, Scripts ( ISAW )

sumDS=CrossSect(ds, 500.0, 30000.0, "Raw Detector Angle")
numPulse=GetAttr(ds, "Number of Pulses")

seconds=numPulse/30
Div(sumDS, seconds, false)
Display sumDS, "SELECTED_GRAPH"
