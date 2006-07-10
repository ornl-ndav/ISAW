/*
 * File: SelectGroups.java
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.11  2006/07/10 16:25:53  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.10  2005/10/03 04:33:23  dennis
 * Now notifies any observers of the DataSet that the selection was
 * changed.  This MAY cause some performance problems if DataSets that
 * have been loaded into the tree, or are currently displayed, have
 * their selections changed.  However, typically when used in a script
 * the DataSets are not also displayed or in the tree.  When used
 * interactively from the ISAW menus, the tree and any active views
 * should be updated when selections are changed.
 *
 * Revision 1.9  2005/10/03 02:27:52  dennis
 * Now makes entry in the DataSet log.
 *
 * Revision 1.8  2005/10/03 01:38:22  dennis
 * Made title more descriptive: "Select Groups by Attribute" instead
 * of "Select Groups".
 * Changed parameters for the constructor with parameters, to specify
 * choices with simple Strings, instead of a full StringChoiceList
 * object.
 * Some documentation clean up.
 *
 * Revision 1.7  2005/09/27 16:52:04  dennis
 * Minor formatting improvements.
 *
 * Revision 1.6  2005/06/01 13:57:38  rmikk
 * Changed Attribute.getValue to Attribute.getaNumericValue so all attributes
 *    can be used for selection if the getNumericValue gives the correct
 *    ordering
 *
 * Revision 1.5  2004/03/15 06:10:44  dennis
 * Removed unused import statements.
 *
 * Revision 1.4  2004/03/15 03:28:23  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.3  2004/01/22 02:39:54  bouzekc
 * Removed/commented out unused imports/variables.
 *
 * Revision 1.2  2003/10/20 16:33:07  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.1  2003/07/05 18:10:02  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import gov.anl.ipns.Parameters.ChoiceListPG;
import gov.anl.ipns.Parameters.FloatPG;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Messaging.*;

import java.io.Serializable;
import java.util.Vector;
import DataSetTools.parameter.*;
import DataSetTools.operator.Parameter;
import Command.*;
import DataSetTools.components.ParametersGUI.*;
/**
 * Selects the Data objects in a single DataSet object based on an attribute
 * name and range of float values for that attribute.
 */

public class SelectGroups
  extends    DS_Attribute
  implements Serializable
{

  public static final String TITLE   = "Select Groups by Attribute";
  public static final String INT     = "Interval";
  public static final String INSIDE  = "Between Max and Min";
  public static final String OUTSIDE = "Outside Max and Min";
  public static final String CLEAR   = "Clear Selected";
  public static final String SET     = "Set Selected";
  String[] In_Out = {INSIDE, OUTSIDE};
  String[] Clear  = {CLEAR, SET};
  

  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */
  public SelectGroups()
  {
    super( TITLE );
  }


  /**
   *  Construct an operator for a specified DataSet. This operator, when
   *  the getResult method is invoked sets(clears) the selection flag on all
   *  data blocks whose given attribute has a value between( outside) 
   *  MinValue and MaxValue( inclusive for between). This is a "union" type
   *  operation in that the selection flags of groups that do not satisfy the
   *  criteria are NOT changed.
   *
   *  @param  ds         The DataSet to which the operation is applied
   *  @param  attribute  The name of the attribute used to determine values
   *  @param  MinValue   The minimum value to compare with the attribute value
   *  @param  MaxValue   The maximum value to compare with the attribute value
   *
   *  @param  Inside_Out Determines if the set or clear occurs when the
   *                     attribute value is between our outside the max and min.
   *                     Use "Between Max and Min" or "Outside Max and Min"
   *
   *  @param  Clear_Set  Determines if the select flag on a group will be
   *                     cleared (use "Clear Selected") or 
   *                     set (use "Set Selected")
   *
   */
  public SelectGroups( DataSet             ds, 
                       AttributeNameString attribute,
                       float               MinValue, 
                       float               MaxValue,
                       String              Inside_Out,  
                       String              Clear_Set)
  {
    this();
    setDataSet( ds );
    setDefaultParameters();
    getParameter(0).setValue( attribute);
    getParameter(1).setValue( new Float(MinValue));
    getParameter(2).setValue( new Float( MaxValue) );
    getParameter(3).setValue( Inside_Out );
    getParameter(4).setValue( Clear_Set);
  }


  /**
   *  Get the end user documentation for this operator.
   *
   *  @return   A multi-line string describing this operator.
   */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator, when the getResult method is ");
    Res.append("invoked sets(clears) the selection flag on all data ");
    Res.append("blocks whose given attribute has a value between (outside)"); 
    Res.append(" MinValue and MaxValue (inclusive for between). This is a ");
    Res.append( "\"union\" type operation in that the selection flags of ");
    Res.append("groups that do not satisfy the criteria are NOT changed.");
    Res.append("@param ds\n");
    Res.append("@param Attrib\n");
    Res.append("@param  MinValue    The minimum value to compare with the ");
    Res.append( "attribute value");
    Res.append("@param  MaxValue    The maximum value to compare with the ");
    Res.append("attribute value ");

    Res.append("@param  Inside_out   Determines if the set or clear occurs ");
    Res.append("when the attribute value is between our outside the max and ");
    Res.append("min. Use \"Between Max and Min\" or \"Outside Max and Min\"");

    Res.append("@param  Clear_Set   Determines if the select flag on a ");
    Res.append("group will be Cleared(use \"Clear Selected\") or set(use ");
    Res.append("\"Set Selected\")");
  
    Res.append("@return Success"); 
    Res.append("@error (Not reported) if attribute is missing or does not");
    Res.append("numeric values, it is ignored(neither set or clearred)");  
    
    return Res.toString();
   }


  /**
   *  Set up the parameter list for this operator.  
   */
  public void setDefaultParameters()
  { 
    parameters = new Vector();  //clear old parameters
   
    addParameter( new Parameter("Attribute",new AttributeNameString()));

    addParameter( new FloatPG( "Minimum Value", new Float(-50000.0)));

    addParameter( new FloatPG( "Maximum Value", new Float(500000.0)) );

    ChoiceListPG In_Out = new ChoiceListPG("Between?", INSIDE ) ;
      In_Out.addItem( INSIDE);
      In_Out.addItem(OUTSIDE);
      addParameter( In_Out);

    ChoiceListPG Clear = new ChoiceListPG("Clear?", SET ) ;
      Clear.addItem( SET);
      Clear.addItem(CLEAR);
      addParameter( Clear);
  }


  /**
   *  Sets or Clears the Select Flags on the indicated groups.
   *  @return  "Success"
   */
  public Object getResult()
  { 
    DataSet DS = getDataSet();
    String attribute = getParameter(0).getValue().toString();
    float minVal = ((FloatPG)getParameter(1)).getfloatValue();
    float maxVal = ((FloatPG)getParameter(2)).getfloatValue();
    String Inside = getParameter(3).getValue().toString();
    String Clear = getParameter( 4).getValue().toString();

    boolean select = true;
    if( Clear == CLEAR)
       select = false;
    
    boolean inside = true;
    if( Inside == OUTSIDE)
       inside = false;

    boolean changed_it = false;
    for( int i=0; i< DS.getNum_entries(); i++)
    {
      Data D = DS.getData_entry( i);
      Attribute A = D.getAttribute( attribute);
      if( A != null)
      { 
        float f = (float)(A.getNumericValue() );
        if( (f < minVal) )
        {
          if( !inside) 
          {
            DS.setSelectFlag( i, select);
            changed_it = true;
          }
        }
        else if ( f > maxVal)
        {
          if( !inside)
          {
            DS.setSelectFlag( i, select);
            changed_it = true;
          }
        }
        else if( inside)
        {
          DS.setSelectFlag( i, select);
          changed_it = true;
        }
      }
    }

    if ( select )
      DS.addLog_entry("Applied SelectGroups() operator, " +
                      " to select Data blocks with " + attribute + 
                      " between " + minVal + " and " + maxVal );
    else
      DS.addLog_entry("Applied SelectGroups() operator, " +
                      " to select Data blocks with " + attribute + 
                      " less than " + minVal + " or more than " + maxVal );

    if ( changed_it )
      DS.notifyIObservers( IObserver.SELECTION_CHANGED );

    return "Success";
  }


  /**
  *  Test program for this module and also ClearSelect
  *
  *   args[0] contains the filename to use
  *   The last data set from the file is used
  */
  public static void main( String args[] )
  {
    SelectGroups isop = new SelectGroups();
    ClearSelect isop1 = new ClearSelect();
    DataSet[] DSS = null;
    try{
        DSS = ScriptUtil.load( args[0]);
        }
    catch( Exception ss)
      { System.exit(0);
       }
    DataSet Ds = DSS[ DSS.length-1];
    Ds.addOperator( isop );
    Ds.addOperator( isop1);
    while( 3==3){
    new JParametersDialog(isop, null, null, null, true);
    ScriptUtil.display( "Seleceted indices=");
    ScriptUtil.display( Ds.getSelectedIndices() );

    new JParametersDialog(isop1, null, null, null, true);
    ScriptUtil.display( "Seleceted indices=");
    ScriptUtil.display( Ds.getSelectedIndices() );
    }
  }

}
