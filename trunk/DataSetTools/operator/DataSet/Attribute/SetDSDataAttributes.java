/*
 * File:  SetDSDataAttributes.java 
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
 * $Log$
 * Revision 1.5  2003/10/20 16:37:16  rmikk
 * Fixed javadoc error
 *
 * Revision 1.4  2002/12/10 22:03:22  dennis
 * Added getDocumentation() method. (Tyler Stelzer)
 *
 * Revision 1.3  2002/11/27 23:16:41  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/09/19 16:00:07  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.1  2002/02/22 21:00:10  pfpeterson
 * Operator reorganization.
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
  *  Sets an attribute value on ALL Data blocks in a DataSet 
  *
  *  @see DS_Attribute
  */

public class SetDSDataAttributes extends    DS_Attribute
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

  public SetDSDataAttributes( )
  {
    super( "Set Data Attribute on ALL Groups" );
  }

  
/* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @parm   Attrib      The Attribute to be set.
   *  @param  new_Value   The new value of the Attribute
   */
  public SetDSDataAttributes  ( DataSet              ds,
                                AttributeNameString  Attrib,
                                Object               new_Value )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( Attrib);
   
    parameter = getParameter(1);
    parameter.setValue(new_Value );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case, SetAttrs
   */
   public String getCommand()
   {
     return "SetAttrs";
   }


 /* -------------------------- setDefaultParameters ------------------------ */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = 
              new Parameter( "Attribute?", new AttributeNameString("") );
    addParameter( parameter ); 
    
    parameter = new Parameter( " New Value?", null );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**@return Sets a Data Attribute on all Data blocks in a DataSet and returns
  *          \"Attribute Set\" if successful.  It may also return 
  *          \"NO DATA BLOCKS\" if unsuccessful.
  *           Otherwise it returns an error string
  *          if there is an error in Attribute.Build()
  */
  public Object getResult()
  { 
    DataSet ds = getDataSet();

    String S = ((AttributeNameString)(getParameter(0).getValue())).toString();
    Object O = getParameter(1).getValue();

    Object A = Attribute.Build( S, O );
    
    if ( A instanceof ErrorString )
      return A;

    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      Data d = ds.getData_entry( i );
      if( d != null) 
       d.setAttribute( (Attribute)A );
    }

    if (ds.getNum_entries() > 0 )
    {
      ds.addLog_entry("SetDataAttribute for "+ds+" on ALL Data blocks to: "+ A);
      return "Attribute Set";
    }
    else
      return "NO DATA BLOCKS";     
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current SetDSDataAttributes Operator.  The list of 
   * parameters  and the reference to the DataSet to which it applies is 
   * copied.
   */
  public Object clone()
  {
    SetDSDataAttributes new_op = new SetDSDataAttributes( );
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
     Res.append("@overview Sets an attribute value on ALL Data blocks in a");
      Res.append(" DataSet");
     
     Res.append("@algorithm Check to make sure there are valid DataBlocks");
      Res.append(" If there are, it sets the data attribute of all the");
      Res.append(" data blocks in the dataSet.");
     
     Res.append("@param ds - The DataSet to which the operation is applied");
     Res.append("@param   Attrib - The Attribute to be set.");
     Res.append("@param  new_Value - The new value of the Attribute");
     
     Res.append("@return Sets a Data Attribute on all Data blocks in a ");
      Res.append("DataSet and returns");
      Res.append("\"Attribute Set\" if successful.  It may also return"); 
      Res.append("\"NO DATA BLOCKS\" if unsuccessful.");
      Res.append("Otherwise it returns an error string");
      Res.append("if there is an error in Attribute.Build()");
      
     Res.append("@error null value object");
     Res.append("@error null name object");
     Res.append("@error can't build Attribute for");
  
     return Res.toString();
  }
  
  /* ------------------------------ main ------------------------------- */
  public static void main(String [] args)
  {
  	SetDSDataAttributes op = new SetDSDataAttributes();
	String documentation = op.getDocumentation();
	
	System.out.println(documentation);
	
	//NOTE:  invalid default parameters
	//getResult returns "null value object"
	System.out.println(op.getResult().toString());
  }

}
