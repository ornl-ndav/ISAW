/*
 * File:  SetDSAttribute.java 
 *             
 * Copyright (C) 2000, Ruth Mikkelson,
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
 * This operator sets a DataSet Attribute
 *
 *  $Log$
 *  Revision 1.6  2003/10/20 21:43:08  dennis
 *  Fixed minor javadoc error so it builds cleanly with
 *  jdk 1.4.2_01
 *
 *  Revision 1.5  2003/10/20 16:34:29  rmikk
 *  Fixed javadoc errors
 *
 *  Revision 1.4  2002/12/10 22:03:22  dennis
 *  Added getDocumentation() method. (Tyler Stelzer)
 *
 *  Revision 1.3  2002/11/27 23:16:41  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/09/19 16:00:06  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:00:09  pfpeterson
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
  *  Allows the user to set attributes on a DataSet
  *
  *  @see DS_Attribute 
  */

public class SetDSAttribute extends    DS_Attribute 
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

  public SetDSAttribute( )
  {
    super( "Set DataSet Attribute" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  Attrib      The Attribute to be set.
   *  @param  new_Value   The new value of the Attribute
   */
  public SetDSAttribute  ( DataSet              ds,
                           AttributeNameString  Attrib,
                           Object               new_Value )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( Attrib);

    parameter = getParameter( 1 );
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

    

    Parameter parameter = new Parameter( "Attribute?", 
                                          new AttributeNameString("") );
    addParameter( parameter ); 
   
    
    parameter = new Parameter( " New Value?", null );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**@return Sets an attribute of the given DataSet if successful.  Otherwise 
  *          it returns an error string if an error occurs in Attribute.Build()
  */
  public Object getResult()
  { 
     DataSet ds = getDataSet();

     String S = ((AttributeNameString)(getParameter(0).getValue())).toString();
     Object O = getParameter(1).getValue();

     Object A = Attribute.Build( S, O );
     
     if ( A instanceof ErrorString )
       return A; 
    
     ds.setAttribute( (Attribute)A );
     ds.addLog_entry( "SetDSAttribute for " +ds + " to: " + A);

     return "Attribute Set";     
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SetDSAttribute Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    SetDSAttribute new_op = new SetDSAttribute( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }
  
  public String getDocumentation()
  {
     StringBuffer Res = new StringBuffer();
     Res.append("@overview This operator sets an attribute of a DataSet.");
     
     Res.append("@algorithm Check to make sure the attribute is");
      Res.append(" valid.  If it is, it sets the data attribute of the");
      Res.append(" DataSet that is specified.");
     
     Res.append("@param  ds - The DataSet to which the operation is applied");
     Res.append("@param   Attrib - The Attribute to be set.");
     Res.append("@param  new_Value - The new value of the Attribute");
     
     Res.append("@return Sets a Data Attribute ");
      Res.append(" in a DataSet and returns the String \"SetDSAttribute for: \"");
      Res.append(" if successful.  Otherwise it returns an error string if");
      Res.append(" there is an error in Attribute.Build");
      
     Res.append("@error null value object");
     Res.append("@error null name object");
     Res.append("@error can't build Attribute for");
  
     return Res.toString();
  }
  
  /* ------------------------------ main ------------------------------- */
  public static void main(String [] args)
  {
  	SetDSAttribute op = new SetDSAttribute();
	String documentation = op.getDocumentation();
	
	System.out.println(documentation);
	
	System.out.println(op.getResult().toString());
  }

}
