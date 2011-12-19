# Jython-ISAW-template

# A. J. Schultz		November, 2009

print 'Message 1'

class jython_isaw_template(GenericTOF_SCD):

# This function creates the user input gui.
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(StringPG("Input your name:",""))

# This function gets the parameters from the gui and
# does whatever the program does.
    def getResult(self):
        nameString = self.getParameter(0).value

        print 'Hello ' + nameString

# I don't know what this does.
    def __init__(self):
        Operator.__init__(self,"Jython-ISAW-template")

