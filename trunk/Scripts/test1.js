/*

  This is a sample java script including features to integrate into a java 
  application.  These features include

  1. Use Packages.application_class_name to access application data
     There is also an importPackage and importClass available

  2. To integrate with ISAW incorporate the variables NumParameters,
     Parameter0, Parameter1,...  NumParameters should store a number
     and the Parameterx variables must be IParameters.

  3. To get the JParametersDialog you need a method setDefaultParameters()
     which sets up NumParameters, and the Parameter'x' variables.

  4. To return a value, a method getResult that returns a value is necessary.

  5. Methods getTitle(), getCommand(), and getDocumentation() will be used
     if available. 

  6. These operators are not "installed" yet so they cannot be invoked
     by other operators in the Isaw scripting language. So getCategoryList
     will not be used at present.  If they become installed, the method
     getCategoryList will be used if available. 
     

*/  
   

var x =23;
var NumParameters = 2;
var Parameter0;
var Parameter1;
function setDefaultParameters(){
   
   Parameter0=new Packages.gov.anl.ipns.Parameters.StringPG('ENter x','HI');
   Parameter1 = new Packages.gov.anl.ipns.Parameters.StringPG('ENter y','There');
   
}
function getResult(){
  print('Hello, world!\n');

  
  return Parameter0.getValue()+' '+Parameter1.getValue();
  
}

