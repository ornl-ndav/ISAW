#-----------------------------------
#           lin_abs_coeff2.py
#-----------------------------------

# Program to calculate linear absorption coefficients.
# ISAW gui version in Jython.

# A. J. Schultz      first version: November, 2009
# R. Mikkelson -- gui construction  November, 2009

class lin_abs_coef2(GenericTOF_SCD):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(StringPG("Chemical formula (click Help for examples):", None))
        self.addParameter(FloatPG("Number of formula units in the unit cell (Z):", 0))
        self.addParameter(FloatPG("Unit cell volume (A^3):", 0))
        
    def getResult(self):
    
        # formulaString = raw_input('Input formula: ')
        formulaString = self.getParameter(0).value
        formulaList = formulaString.split()
        numberOfIsotopes = len(formulaList)/2	# the number of elements or isotopes in the formula

        # zParameter = int(raw_input('Input the number of formula units in the unit cell (Z): '))
        zParameter = self.getParameter(1).value

        # unitCellVolume = float(raw_input('Input the unit cell volume (A^3): '))
        unitCellVolume = self.getParameter(2).value

        sumScatXs = 0.0
        sumAbsXs = 0.0

        print '\nAtom      ScatXs      AbsXs'	# print headings
        print   '----      ------      -----'

        # Except for hydrogen, cross-section values are from the NIST web site:
        # http://www.ncnr.nist.gov/resources/n-lengths/list.html
        # which are from:
        # V. F. Sears, Neutron News, Vol. 3, No. 3, 1992, pp. 29-37.
        # Hydrogen cross-sections are from:
        # Howard, J. A. K.; Johnson, O.; Schultz, A. J.; Stringer, A. M.
        #	J. Appl. Cryst. 1987, 20, 120-122.

        filename = 'C:\ISAW\Databases\NIST_cross-sections.dat'

        # begin loop through each atom in the formula
        for i in range(numberOfIsotopes):
            j = 2*i
            input = open(filename, 'r')			# this has the effect of rewinding the file
            lineString = input.readline()		# read the first line for H atoms
            lineList = lineString.split()

            # Search table for element/isotope.
            while formulaList[j] != lineList[0]:
                lineString = input.readline()
                lineList = lineString.split()

            scatteringXs = float(lineList[1])	# the total scattering cross section
            absorptionXs = float(lineList[2])	# the true absorption cross section at 1.8 A
            number = float(formulaList[j+1])	# the number of this nuclei in the formula
            
            print '%-5s %10.5f %10.5f' % (lineList[0], scatteringXs, absorptionXs)
            
            sumScatXs = sumScatXs + ( number * scatteringXs )
            sumAbsXs = sumAbsXs + ( number * absorptionXs )
            
            input.close()
        # end loop

        # Calculate the linear absorption coefficients in units of cm^-1
        muScat = sumScatXs * zParameter / unitCellVolume
        muAbs = sumAbsXs * zParameter / unitCellVolume

        # Print the results.
        print '\n'
        print 'The linear absorption coefficent for total scattering is %f cm^-1' % muScat
        print 'The linear absorption coefficent for true absorption is %f cm^-1' % muAbs


    def  getDocumentation( self):
        S =StringBuffer()
        S.append("Sample Inputs of molecular formula\n")
        S.append(" For example, for oxalic acid dihydrate, C2O4H2.2H2O, or C2O6H6, input\n")
        S.append("C 2 O 4 H 2 H 4 O 2    or    C 2 O 6 H 6\n")
        S.append("For deuterated oxalic acid dihydrate, input\n")
        S.append("C 2 O 6 D 6   or   C 2 O 6 2H 6\n")
        S.append("For La2NiO4.2, input\n")
        S.append("La 2 Ni 1 O 4.2\n")
        S.append("@param  formula  The chemical formula input as described above")
        S.append("@param   Z number of formula units")
        S.append("@param  UnitVolume  the unit cell volume")
        S.append("@return a float array with 2 entries, the total scattering and the true absorption")
        return S.toString()

    def getCategoryList( self):
        S = String[3]
        S[0]="Macros"
        S[1] ="Instrument Type"
        S[2] = "TOF_NSD"
        return S
        
    def __init__(self):
        Operator.__init__(self,"lin_abs_coef2")


