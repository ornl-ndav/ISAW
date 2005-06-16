#     Jython Example 
#
#@overview  This is a simple Jython example that parallels typical ISAW Script
#     This Jython PROGRAM loads in a file, Displays it, Omits NUll data and
#     displays the result.
#No Parameters, no result is returned
#
#  Code can be converted to an Isaw operator form if useful
#  It can also be compiled to a Java class. Use method calls

from Command import *
#from Operators.Special import *
DSS=ScriptUtil.load("C:/ISAW-old/SampleRuns/GPPD12358.run")
ScriptUtil.display(DSS[1])


op=ScriptUtil.getOperator("OmitNullData",[DSS[1],Boolean(1)])
D=op.getResult()
#D=OmitNullData(DSS[1],0).getResult()
ScriptUtil.display(D)
