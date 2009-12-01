#                  STATS
# A.J. Schultz, October, 2009
#@overview  This script is a front end for the statsSNS executable
#@assumption  The statsSNS executable is named {IsawHome}/stats/stats_? where
#              ? depends on the operating system
#
#@param DataDir         Directory with Input and output files
#@param IntegrateFile   The Integrated peaks file
#@param DMIN            Minimum d-spacing in Angstroms

#@return  Result OK or error in the program. anvred.log and {ExpName}.hkl appear in the OutputDir
#         Some information also appears on the console. 

$ Category = Macros, Single Crystal

$ DataDir         DataDir(C:\SNS\stats)		Directory with Input files
$ IntegrateFile   String("ox.integrate")	The Integrated peaks file
$ DMIN            float(.7)			Minimum d-spacing (Angstroms)

Input = DataDir & "stats.input"

OpenLog( Input, false)

  LogMsg(  DataDir &IntegrateFile &"\n")
  LogMsg(DMIN &"\n")

CloseLog()

  IsawHome = getSysProp("ISAW_HOME")
  stats = CreateExecFileName(IsawHome, "bin/stats", true)
  Exec( stats, Input, DataDir )

Return "Output in the stats.out file."



  


  


