/*
 * File: ClearSelect.java
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
 * Revision 1.2  2004/01/22 02:39:54  bouzekc
 * Removed/commented out unused imports/variables.
 *
 * Revision 1.1  2003/07/05 18:09:45  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import DataSetTools.dataset.DataSet;
import java.io.Serializable;
import java.lang.String;
import java.lang.Object;
import java.util.Vector;

/**
 * selects Data objects in a single DataSet object based on an attribute
 * name and range of float values for that attribute.
 */
public class ClearSelect
  extends    DS_Attribute
  implements Serializable
{

  public static final String TITLE = "Clear Groups";
 

  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */
  public ClearSelect()
  {
    super( TITLE );
  }


  /**
   *  Construct an operator for a specified DataSet. When the getResult
   *  method is called, all the select flags on all the groups of a
   *  data set.
   *
   *  @param  ds          The DataSet whose select flags will all be clearred
   */
  public ClearSelect( DataSet ds )
  {
    this();
    setDataSet( ds );
    setDefaultParameters();
    
  }

  public String getDocumentation()
    {
       StringBuffer Res = new StringBuffer();
       Res.append("@overview This operator, when the getResult method is ");
       Res.append("called clears all the select flags on all the groups ");
       Res.append("of a data set.");

       Res.append("@param ds\n");
       
       Res.append("@return Success"); 
      
       return Res.toString();

    }


  public void setDefaultParameters()
  {
    parameters = new Vector();  //clear old parameters
  }


  /**
   *  Clears all the select flags on all the groups of a
   *  data set
   */
  public Object getResult()
  { 
    DataSet DS = getDataSet();
    DS.clearSelections();
    
    return "Success";
  }

 
  
}
