# Testing script for loading and displaying files
# $Date$
$  path      DataDirectoryString   Data Directory

# first a run file
filename=path&"/GPPD12358.RUN"
load(filename,"ds")
display(ds[1],"THREE_D")

# then a friendly nexus file
filename=path&"/lrcs3000.nxs"
load(filename,"nexus")
display(nexus[1])

# then an sdds file
filename=path&"/hrcs3118.sdds"
load(filename,"sdds")
send(sdds[5])
