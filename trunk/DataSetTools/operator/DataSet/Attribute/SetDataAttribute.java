/*
 * File:  SetDataAttribute.java
 *             
 * Copyright (C) 2000, Ruth Mikkelson,
 *                     Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2002/11/19 22:57:05  dennis
 *  Added getDocumentation() method and basic main test program. (Tyler Stelzer)
 *
 *  Revision 1.2  2002/09/19 16:00:08  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:00:11  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.7  2001/06/01 21:18:00  rmikk
 *  Improved documentation for getCommand() method
 *
 *  Revision 1.6  2001/04/26 19:10:44  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.5  2000/11/17 23:41:07  dennis
 *  Now constructs the attribute using the Attribute.Build() method.
 *
 *  Revision 1.4  2000/11/10 22:41:34  dennis
 *     Introduced additional abstract classes to better categorize the operators.
 *  Existing operators were modified to be derived from one of the new abstract
 *  classes.  The abstract base class hierarchy is now:
 *
 *   Operator
 *
 *    -GenericOperator
 *       --GenericLoad
 *       --GenericBatch
 *
 *    -DataSetOperator
 *      --DS_EditList
 *      --DS_Math
 *         ---ScalarOp
 *         ---DataSetOp
 *         ---AnalyzeOp
 *      --DS_Attribute
 *      --DS_Conversion
 *         ---XAxisConversionOp
 *         ---YAxisConversionOp
 *         ---XYAxesConversionOp
 *      --DS_Special
 *
 *     To allow for automatic generation of hierarchial menus, each new operator
 *  should fall into one of these categories, or a new category should be
 *  constructed within this hierarchy for the new operator.
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  * This operator sets a Data Attribute on a particular Data block 
  * in a DataSet
  *
  *  @see DS_Attribute 
  */

public class SetDataAttribute extends    DS_Attribute 
                              implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public SetDataAttribute( )
  {
    super( "Set Data Attribute" );
  }

  
/* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  index       The index of the data block whose attribute is to 
   *                      be set
   *  @parm   Attrib      The Attribute to be set.
   *  @param  new_Value   The new value of the Attribute
   */

  public SetDataAttribute  ( DataSet              ds,
                             Integer              index,  
                             AttributeNameString  Attrib,
                             Object               new_Value )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( index);

    parameter = getParameter( 1 );
    parameter.setValue( Attrib);

   
    parameter = getParameter(2);
    parameter.setValue(new_Value );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, SetAttr
   */
   public String getCommand()
   {
     return "SetAttr";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Index?", new Integer(0));
    addParameter(parameter);

    parameter = new Parameter( "Attribute?", new AttributeNameString("") );
    addParameter( parameter ); 
   
    
    parameter = new Parameter( " New Value?", null );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**@return sets a Data Attribute on a particular Data block 
  *          in a DataSet if successful.  Otherwise it returns an error string
  *          if there is an Improper Index
  */
  public Object getResult()
  {  
      DataSet ds = getDataSet();

      int index = ((Integer)getParameter(0).getValue()).intValue();
      String S = ((AttributeNameString)(getParameter(1).getValue())).toString();
      Object O = getParameter(2).getValue();

      Object A = Attribute.Build( S, O );
    
      if ( A instanceof ErrorString )
        return A;
     
      Data D = ds.getData_entry( index);
      if( D == null) 
        return new ErrorString("Improper Index"); 

      D.setAttribute( (Attribute)A );
      ds.addLog_entry( "SetDataAttribute for " +ds
                       +" data["+index+"]"+  " to: " + A);
      //ds.notifyIObservers( IObserver.ATTRIBUTE_CHANGED);

      return "Attribute Set";     
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SetDataAttribute Operator.  The list of 
   * parameters  and the reference to the DataSet to which it applies is 
   * copied.
   */
  public Object clone()
  {
    SetDataAttribute new_op    = new SetDataAttribute( );
                                                // copy the data set associated
                                                // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
  /* ------------------------------ getDocumentation ------------------- */
  public String getDocumentation()
  {
     StringBuffer Res = new StringBuffer();
     Res.append("@overview This operator sets an attribute of a DataSet.");
     
     Res.append("@algorithm Check to make sure the attribute and index are");
      Res.append(" valid.  If they are, it sets the data attribute of the");
      Res.append(" data block that is specified.");
     
     Res.append("@param ds - The DataSet to which the operation is applied");
     Res.append("@param index - The index of the data block whose attribute");
      Res.append(" is to be set.");
     Res.append("@parm   Attrib - The Attribute to be set.");
     Res.append("@param  new_Value - The new value of the Attribute");
     
     Res.append("@return sets a Data Attribute on a particular Data block");
      Res.append(" in a DataSet and returns the String \"Attribute Set\"");
      Res.append(" if successful.  Otherwise it returns an error string if");
      Res.append(" there is an Improper Index or an error in Attribute.Build");
      
     Res.append("@error Improper Index");
     Res.append("@error null value object");
     Res.append("@error null name object");
     Res.append("@error can't build Attribute for");
  
     return Res.toString();
  }
  
  /* ------------------------------ main ------------------------------- */
  public static void main(String [] args)
  {
  	SetDataAttribute op = new SetDataAttribute();
	String documentation = op.getDocumentation();
	
	System.out.println(documentation);
	
	//NOTE:  invalid default parameters
	//getResult returns "null value object"
	System.out.println(op.getResult().toString());
  }
}
