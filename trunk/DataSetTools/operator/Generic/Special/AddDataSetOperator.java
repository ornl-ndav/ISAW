/*
 * File:  AddDataSetOperator.java 
 *
 * Copyright (C) 2002, Peter Peterson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2002/11/27 23:21:43  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/11/26 22:13:56  dennis
 * dded getDocumentation() method and modified main program.(Mike Miller)
 *
 * Revision 1.1  2002/07/31 14:54:50  pfpeterson
 * Added to CVS.
 *
 *
 */
package DataSetTools.operator.Generic.Special;

import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import java.util.Vector;

/** 
 *  This operator adds a DataSetOperator to a given DataSet. It first
 *  tries the specified classname, then tries prepending the
 *  appropriate amount of "DataSetTools.operator.DataSet" if that did
 *  not work.
 */
public class AddDataSetOperator extends GenericSpecial{
    private static final String  TITLE = "Add DataSet Operator";
    private static final boolean DEBUG = false;

    /**
     *  Creates operator with title "Add DataSet Operator" and a
     *  default list of parameters.
     */  
    public AddDataSetOperator(){
	super( TITLE );
    }
    
    /** 
     *  Creates an "AddDataSetOperator" operator with the specified
     *  list of parameters.  The getResult method must still be used
     *  to execute the operator.
     *
     *  @param  ds          DataSet to add an operator to.
     *  @param  opString    getCommand() string of operator to add.
     */
    public AddDataSetOperator( DataSet ds, String opString ){
	this(); 
	parameters = new Vector();
	addParameter( new Parameter("DataSet", ds) );
	addParameter( new Parameter("Operator Name", opString) );
    }
    
/* ---------------------------getDocumentation--------------------------- */
 /**
  *  Returns a string of the description/attributes of AddDataSetOperator
  *   for a user activating the Help System
  */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator adds a dataset operator to ");
    Res.append("a given dataset.\n");
    Res.append("@algorithm Given a data set and an operator name, ");
    Res.append("the operator will be added to the corresponding ");
    Res.append("dataset.\n");
    Res.append("@param ds\n");
    Res.append("@param opString\n");
    Res.append("@return a confirmation message, Successful or Failed.");
    Res.append("@error Invalid Operator Name: null or empty string \n"); 
    Res.append("@error Null DataSet in AddDataSetOperator \n");
    Res.append("@error Invalid Operator Name: op_name");
    Res.append("@error Invalid Operator Name: cannot be instatiated \n"); 
    
    return Res.toString();
    
  }
    
    /** 
     * Get the name of this operator to use in scripts. In this case
     * "addDataSetOp".
     */
    public String getCommand(){
	return "addDataSetOp";
    }
    
    /** 
     * Sets default values for the parameters.  This must match the
     * data types of the parameters.
     */
    public void setDefaultParameters(){
	parameters = new Vector();
	addParameter(new Parameter("DataSet",DataSet.EMPTY_DATA_SET ));
	addParameter(new Parameter("Operator Name", "") );
    }
    
    /** 
     *  Executes this operator using the values of the current parameters.
     *
     *  @return If successful, this operator produces a DataSet
     *  containing the the original DataSet minus the dead detectors.
     */
    public Object getResult(){
	DataSet ds        = (DataSet)(getParameter(0).getValue());
        String  opCommand = (String) (getParameter(1).getValue());
        Class  klass      = null;
        Object object     = null;

        // check that the operator is a non-null string
        if( (opCommand==null) || (opCommand.length()<=0) )
            return "Invalid Operator Name: null or empty string\n";

        // check that the dataset is non-null
        if( ds==null ) return "Null DataSet in AddDataSetOperator";

        // try to get the operator's class
        klass=getClass(opCommand);
        if(klass==null){
            String nextTry=null;
            if(opCommand.indexOf("DataSet.")<0)
                nextTry="DataSetTools.operator.DataSet."+opCommand;
            else if(opCommand.indexOf("operator.")<0)
                nextTry="DataSetTools.operator."+opCommand;
            else if(opCommand.indexOf("DataSetTools.")<0)
                nextTry="DataSetTools."+opCommand;

            klass=getClass(nextTry);
        }
            
        // get an instance of the operator
        if(klass==null){
          return "Invalid Operator Name: "+opCommand;
        }else{
            try{
                object=klass.newInstance();
            }catch(InstantiationException e){
                return "Invalid Operator Name("+opCommand
                    +"): cannot be instatiated";
            }catch(IllegalAccessException e){
                return "Invalid Operator Name("+opCommand
                    +"): cannot be instatiated";
            }
        }

        // add the operator to the dataset
        if(object!=null && object instanceof DataSetOperator){
            ds.addOperator((DataSetOperator)object);
            return "Successful";
        }else{
            return "Failed";
        }
    }
    
    /**
     * Method to encapsulate getting a class. 
     */
    private static Class getClass(String classname){
        Class klass=null;

        if(classname==null)return null;

        try{
            klass=Class.forName(classname);
        }catch(ClassNotFoundException e){
            return null;
        }
        
        return klass;
    }

    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){ 
	Operator op = new AddDataSetOperator();
	op.CopyParametersFrom( this );
	return op;
    }
    

    /** 
     * Test program to verify that this will complile and run ok.  
     *
     */
    public static void main( String args[] ){
	System.out.println("Test of AddDataSetOperator starting...");
        String opName="Conversion.XAxis.DiffractometerTofToQ";
	
        System.out.println("OPERATOR:"+opName);

	String filename="/home/groups/SCD_PROJECT/SampleRuns/GPPD12358.RUN";
	//String filename="/IPNShome/pfpeterson/data/CsC60/SEPD18805.RUN";
	//String filename="/IPNShome/pfpeterson/data/ge_10k/glad4606.run";
	RunfileRetriever rr = new RunfileRetriever( filename );
	DataSet ds = rr.getDataSet(1);

	AddDataSetOperator op = new AddDataSetOperator( ds, opName);
        System.out.println("RESULT: "+op.getResult());
	
	// Raw documentation data dumped to console
	System.out.println( op.getDocumentation() );
    }
}
