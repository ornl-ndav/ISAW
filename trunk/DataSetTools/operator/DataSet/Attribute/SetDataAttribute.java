/*
 * File:  SetDataAttribute.java
 *             
 * Copyright (C) 2000, Ruth Mikkelson, Dennis Mikkelson
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.6  2003/10/20 16:33:51  rmikk
 *  Fixed javadoc errors
 *
 *  Revision 1.5  2002/12/06 14:58:24  dennis
 *  Fixed spelling error in java doc tag. (Chris Bouzek)
 *
 *  Revision 1.4  2002/11/27 23:16:41  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/11/19 22:57:05  dennis
 *  Added getDocumentation() method and basic main test program. (Tyler Stelzer)
 *
 *  Revision 1.2  2002/09/19 16:00:08  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:00:11  pfpeterson
 *  Operator reorganization.
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
     Res.append("@param   Attrib - The Attribute to be set.");
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
