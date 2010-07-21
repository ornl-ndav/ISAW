#-----------------------------------
#           anvred_to_gsas.py
#-----------------------------------
# @overview: Script to convert ANVRED files (.hkl) to GSAS .EXP and .S01 format.
#
# Copy this file to the ISAW scripts directory.
#   For my install, the folder is: /home/janik/Bin/ISAW/Scripts
#
# Converted by Janik Zikovsky to Jython; from anv2gsas.f, by Art Schulz.
#
# The file anv2gsas.py needs to be in the ISAW python path.
# Also, all the jython libs need to be in there too.
# For me, this means this line in IsawProps.dat (without the #)
# python.path=/usr/share/jython/Lib:/home/janik/Code/anv2gsas/src



class anvred_to_gsas(GenericTOF_SCD):

    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(LoadFilePG("Load the starting GSAS .EXP file:", "/home/janik/Code/anv2gsas/src/ox80.EXP"))
        self.addParameter(LoadFilePG("Load the ANVRED .hkl file:", "/home/janik/Code/anv2gsas/src/ox80.hkl"))
        self.addParameter(BooleanPG("The .hkl file came from INTSCD or INTEGRATE?", True))
        self.addParameter(FloatPG("SIG(Fo**2) = sqrt( SIGFOSQ**2 + ( P * FOSQ)**2 + K ) <--- P value", 0.0))
        self.addParameter(FloatPG("SIG(Fo**2) = sqrt( SIGFOSQ**2 + ( P * FOSQ)**2 + K ) <--- K value", 0.0))
   
    def getResult(self):
        #This function does the whole job of conversion
        from anv2gsas import convert_anvred_to_gsas
        import os.path
        
        #Read in all parameters
        exp_filename = self.getParameter(0).value
        hkl_filename = self.getParameter(1).value
        used_integrate = self.getParameter(2).value
        P = self.getParameter(3).value
        K = self.getParameter(4).value
        
        #This does all the conversion.
        convert_anvred_to_gsas(exp_filename, hkl_filename, P, K, used_integrate)
        #Don't have to return anything
        return exp_filename


    def getDocumentation(self):
        S =StringBuffer()
        S.append("Script to convert ANVRED files (.hkl) to GSAS .EXP and .S01 format.\n")
        S.append("@param  expname: The .EXP filename that we start with.")
        S.append("@param  hkl_filename: Path to the .hkl file produced by ANVRED.")
        S.append("@param  used_integrate: Set to True if the integrated intensities were obtained with INTSCD or INTEGRATE. False means they were obtained from PEAKINT.")
        S.append("@param  P and K: These two floats are used to correct the SIG(Fo**2) using the following equation: SIG(Fo**2) = sqrt( SIGFOSQ**2 + ( P * FOSQ)**2 + K )")
        S.append("@return Does not return anything, but saves 2 or more files. .S01 file contain the reflection data in binary. If there is more than one histogram, they are saved in .S02, .S03, etc. The expname_out.EXP file is a modified version of the input .EXP file.")
        return S.toString()

    def getCategoryList(self):
        return ["Macros","Single Crystal"]
        
    def __init__(self):
        Operator.__init__(self,"Anvred_to_GSAS_conversion")


