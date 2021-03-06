#-----------------------------------
#           lin_abs_coeff2.py
#-----------------------------------

# Program to calculate linear absorption coefficients and density.
# ISAW gui version in Jython.

# A. J. Schultz --  November, 2009
# R. Mikkelson -- gui construction:  November, 2009

import math

class lin_abs_coef(GenericTOF_SCD):
    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(StringPG("Chemical formula (click Help for examples):", "C2 O6 H6"))
        self.addParameter(FloatPG("Number of formula units in the unit cell (Z):", 2))
        self.addParameter(FloatPG("Unit cell volume (A^3):", 253))
        self.addParameter(BooleanEnablePG("Calculate the radius of a sphere?","[False, 1, 0]"))
        self.addParameter(FloatPG("Weight of the crystal in milligrams:", 1.0))
        self.addParameter(DataDirPG("Directory for log file:", "C:\Users\Arthur\Desktop"))
        
    def getResult(self):
    
        formulaString = self.getParameter(0).value
        formulaList = formulaString.split()
        numberOfIsotopes = len(formulaList)     # the number of elements or isotopes in the formula
        
        zParameter = self.getParameter(1).value # number if formulas in the unit cell
        unitCellVolume = self.getParameter(2).value # unit cell volume in A^3
        calcRadius = self.getParameter(3).value
        weight = self.getParameter(4).value
        logDirectory = self.getParameter(5).value

        sumScatXs = 0.0
        sumAbsXs = 0.0
        sumAtWt = 0.0
        
        logFileName = logDirectory + 'lin_abs_coef.log'
        logFile = open( logFileName, 'w' )
        logFile.write('Output from lin_abs_coef.py script:\n\n')
        
        logFile.write('Chemical formula: ' + formulaString + '\n')
        logFile.write('Number of formula units in the unit cell (Z): %6.3f\n' % zParameter)
        logFile.write('Unit cell volume (A^3): %8.2f\n' % unitCellVolume)
        
        logFile.write('\nCross sections in units of barns ( 1 barn = 1E-24 cm^2)\n')
        logFile.write('Absorption cross section for 2200 m/s neutrons (wavelength = 1.8 A)\n')
        logFile.write('For further information and references, see ...\ISAW\Databases\NIST_cross-sections.dat\n')

        print '\nAtom      ScatXs      AbsXs'	# print headings
        print   '----      ------      -----'
        logFile.write('\nAtom      ScatXs      AbsXs\n')
        logFile.write(  '----      ------      -----\n')

        # Except for hydrogen, cross-section values are from the NIST web site:
        # http://www.ncnr.nist.gov/resources/n-lengths/list.html
        # which are from:
        # V. F. Sears, Neutron News, Vol. 3, No. 3, 1992, pp. 29-37.
        # Hydrogen cross-sections are from:
        # Howard, J. A. K.; Johnson, O.; Schultz, A. J.; Stringer, A. M.
        #	J. Appl. Cryst. 1987, 20, 120-122.
        
        S = System.getProperty("ISAW_HOME")
        if( not S.endswith('/')):
            if(not S.endswith('\\')):
                S=S+'/'
        filename = S+'Databases/NIST_cross-sections.dat'

        # begin loop through each atom in the formula
        for i in range(numberOfIsotopes):
        
            lenAtom = len(formulaList[i])   # length of symbol plus number in formula
            
            # begin test for number of characters in the isotope name or symbol
            for j in range(lenAtom):          
                lenSymbol = lenAtom - j - 1
                if formulaList[i][lenSymbol].isalpha(): break
            lenSymbol = lenSymbol + 1
            
            input = open(filename, 'r')         # this has the effect of rewinding the file
            lineString = input.readline()       # read the first comment line
            while lineString[0] == '#':         # search for the end of the comments block
                lineString = input.readline()

            # Begin to search the table for element/isotope match.
            
            lineList = lineString.split()       # this should be the H atom

            while formulaList[i][0:lenSymbol] != lineList[0]:
                lineString = input.readline()
                lineList = lineString.split()

            scatteringXs = float(lineList[1])   # the total scattering cross section
            absorptionXs = float(lineList[2])   # the true absorption cross section at 1.8 A
            atomicWeight = float(lineList[4])   # atomic weight
            number = float(formulaList[i][lenSymbol:])   # the number of this nuclei in the formula
            
            print '%-5s %10.5f %10.5f' % (lineList[0], scatteringXs, absorptionXs)
            logFile.write('%-5s %10.5f %10.5f\n' % (lineList[0], scatteringXs, absorptionXs))
            
            sumScatXs = sumScatXs + ( number * scatteringXs )
            sumAbsXs = sumAbsXs + ( number * absorptionXs )
            sumAtWt = sumAtWt + ( number * atomicWeight )
            
            input.close()
        # end loop

        # Calculate the linear absorption coefficients in units of cm^-1
        muScat = sumScatXs * zParameter / unitCellVolume
        muAbs = sumAbsXs * zParameter / unitCellVolume
        
        # Calculate the density of the crystal in g/cc
        density = (sumAtWt / 0.6022) * zParameter / unitCellVolume

        # Print the results.
        print '\n'
        print 'The linear absorption coefficent for total scattering is %6.3f cm^-1' % muScat
        print 'The linear absorption coefficent for true absorption at 1.8 A is %6.3f cm^-1' % muAbs
        print 'The calculated density is %6.3f grams/cm^3' % density
        logFile.write('\n')
        logFile.write('The linear absorption coefficent for total scattering is %6.3f cm^-1\n' % muScat)
        logFile.write('The linear absorption coefficent for true absorption at 1.8 A is %6.3f cm^-1\n' % muAbs)
        logFile.write('\nThe calculated density is %6.3f grams/cm^3\n' % density)

        if calcRadius:
            crystalVolume = weight / (density)   # sample volume in mm^3
            print 'For a weight of %6.3f mg, the crystal volume is %6.3f mm^3' % (weight, crystalVolume)
            logFile.write('\nFor a weight of %6.3f mg, the crystal volume is %6.3f mm^3\n' % (weight, crystalVolume))
            crystalRadius = ( crystalVolume / ((4.0/3.0)*math.pi) )**(1.0/3.0)   # radius in mm
            print 'The crystal radius is %6.3f mm, or %6.4f cm' % (crystalRadius, crystalRadius/10.)
            logFile.write('The crystal radius is %6.3f mm, or %6.4f cm\n' % (crystalRadius, crystalRadius/10.))
#            volCalc = (4.0/3.0) * math.pi * crystalRadius**3
#            print 'volCalc = %6.3f' % volCalc
        
        logFile.close()
        return 'All done!'

    def  getDocumentation( self):
        S =StringBuffer()
        S.append("Sample Inputs of molecular formula\n")
        S.append(" For example, for oxalic acid dihydrate, C2O4H2.2H2O, or C2O6H6, input\n")
        S.append("C2 O4 H2 H4 O2    or    C2 O6 H6\n")
        S.append("For deuterated oxalic acid dihydrate, input\n")
        S.append("C2 O6 D6   or   C2 O6 2H6\n")
        S.append("For La2NiO4.2, input\n")
        S.append("La2 Ni1 O4.2\n")
        S.append("For boron-11 B4C, input\n")
        S.append("11B4 C1\n")
        S.append("@param  formula: The chemical formula input as described above.")
        S.append("@param  Z: The number of formula units in the unit cell. This can be a noninteger value.")
        S.append("@param  UnitVolume: The unit cell volume in units of Angstroms cubed.")
        S.append("@param  weight: Crystal weight in milligrams.")
        S.append("@return a float array with 3 entries, the total scattering and the true absorption\
        linear absorption coefficients, and the density")
        return S.toString()

    def getCategoryList( self):
       
        return ["Macros","Single Crystal"]
        
    def __init__(self):
        Operator.__init__(self,"lin_abs_coef")


