from DataSetTools.dataset.DataSet   import EMPTY_DATA_SET
from gov.anl.ipns.Util.SpecialStrings import *

class NormalizeDS(GenericOperator):

    def setDefaultParameters(self):
    
        self.super__clearParametersVector()
        
        self.addParameter(DataSetPG("Sample",EMPTY_DATA_SET))
        self.addParameter(DataSetPG("Sample BackGround",EMPTY_DATA_SET))
        self.addParameter(DataSetPG("Standard",EMPTY_DATA_SET))
        self.addParameter(DataSetPG("Standard Background",EMPTY_DATA_SET))
        self.addParameter(BooleanPG("Clamp Zeroes", 1))

        
    def getResult(self):
    
        Sample = self.getParameter(0).value        # Sample Data Set
        SampleBack = self.getParameter(1).value    # Background for Sample Data Set
        Standard = self.getParameter(2).value      # Standard( Vanadium, inc spectr..) Data Set
        StandardBack = self.getParameter(3).value  # Background for Standard
        clamp = self.getParameter(4).value         # clamp negative numbers to zero

        if Sample is None:
            return ErrorString("No Sample")
        
        Sample=Sample.clone()
        if SampleBack.equals( EMPTY_DATA_SET):
           SampleBack = None
        
        Sample = Sample.clone()
        if SampleBack  is not None:
           op = DataSetSubtract(Sample, SampleBack,0)
           X = op.getResult()
           if isinstance(X, ErrorString):
               return X
           if clamp:
               Sample.clampToZero()

        if Standard is not None and Standard.equals(EMPTY_DATA_SET):

           Standard = None
           StandardBack = None

        else:

           if StandardBack is not None and StandardBack.equals(EMPTY_DATA_SET):
              StandardBack = None

        if Standard is not None and StandardBack is not None:
           Standard = Standard.clone()
           op = DataSetSubtract(Standard, StandardBack,0)
           X = op.getResult()

           if isinstance(X, ErrorString):
               return X

           if clamp:
               Standard.clampToZero()

        if Standard is not None:
            op = Sample.getOperator("Divide by a DataSet")           
            op.getParameter(0).setValue(Standard)
            X = op.getResult()
            if isinstance(X, ErrorString):
               return X
        
        return Sample

        

    def  getDocumentation( self):
        S =StringBuffer()
        S.append("Performs the standard normalization on a data set.\n")
        S.append("@param  Sample        The sample data set to be normalized.")
        S.append("@param  SampleBack    The background dataset corresponding to the Sample")
        S.append("@param  Standard      A Standard dataset(Uniform scatterer...)")
        S.append("@param  StandardBack  The Background data set for the Standard")
        S.append("@param  ClampZeroes   Eliminate negative numbers when subtracting")
        S.append("@return a Data Set normalized by subtracting corresponding backgrounds from \
                         the Sample and Standard and then Dividing these two")
        return S.toString()

    def getCategoryList( self):
       
        return ["Macros","DataSet","Tweak"]
        
    def __init__(self):
        Operator.__init__(self,"Normalize a data set")


