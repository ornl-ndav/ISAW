/*
 * File:  GetDetectorInfo_op.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *
 * $Log$
 * Revision 1.1  2002/08/01 22:09:03  rmikk
 * Initial Checkin
 *
 *
 */
package DataSetTools.operator.DataSet.Attribute;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Information.XAxis.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Parameter;
import DataSetTools.instruments.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import java.util.*;
import DataSetTools.materials.*;
import java.text.DecimalFormat;
/** 
 *  
 */
public class GetDetectorInfo_op extends DataSetTools.operator.DataSet.Attribute.DS_Attribute
                                implements IDataBlockInfo

{
  private static final String TITLE = "Det Info";

 /* ------------------------ Default constructor ------------------------- */ 
 /**
  *  Creates operator with titled "Get Det Info" and a default list
  *  of parameters. This operator gets some of the Detector Info information for this data block.
  */  
  public GetDetectorInfo_op()
  {
    super( TITLE );
    
  }

/* public java.lang.String getCategory()
  { return DataSetOperator.X_AXIS_INFORMATION ;
   }
  String[] clist = {Operator.OPERATOR, "Operator Add", DataSetOperator.X_AXIS_INFORMATION};
  public java.lang.String[] getCategoryList()
   { return clist;
   }
*/

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Creates operator that will give the row corresponding to a given spectra
  * @param ds   The data set with the data block
  * @param index  the position of the desired data block in the data block array<P>
  * The getResult metho returns the ROW of the data block
  */
  public GetDetectorInfo_op( DataSet ds, int index){
    this(); 
    this.setDataSet( ds);
    parameters.addElement( new Parameter( "Data index=", new Integer( index)));
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator, Row, to use in scripts
  * 
  * @return  "Row", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "DetInf";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters. During testing the
  * 
  */
  public void setDefaultParameters()
  { 
    parameters = new Vector();
    parameters.addElement( new Parameter( "Data index=", new Integer( 0)));
    
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return If successful, this operator returns the row(Integer) of the given data block
  *  
  */
  public Object getResult(){
    DataSet ds = getDataSet();
    int index =((Integer)getParameter(0).getValue()).intValue();
    Data db = ds.getData_entry( index);
    if( db == null)
      return new DataSetTools.util.ErrorString("No data block at position "+index);
    Object O = db.getAttributeValue(Attribute.DETECTOR_INFO_LIST);
    if( O == null)
      return new DataSetTools.util.ErrorString( "Data block has no DetectorInfo Attribute");
    
    DetectorInfo[] di =(DetectorInfo[])O;
    if( di.length < 1)
      return  new DataSetTools.util.ErrorString( "Data block has no DetectorInfo Attribute");
    Vector V = new Vector();
    
    V.addElement(new Integer(di[0].getRow()));
    V.addElement(new Integer(di[0].getColumn()));
    return V;
    
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    GetDetectorInfo_op op = new GetDetectorInfo_op();
    op.CopyParametersFrom( this );
    op.setDataSet( this.getDataSet());
    return op;
  }
/** Returns the Seg,row and col corresponding to the data block whose index is index
*/
 public String DataInfo( int index )
  { parameters= new Vector();
    parameters.addElement( new Parameter("index", new Integer(index)));
    Object O= getResult();
    if( O instanceof ErrorString) 
      return "NaN";
    else if( O == null)
      return "null";
    else if( ! (O instanceof Vector))
      
      return (new NexIO.NxNodeUtils()).Showw(O); 
    Vector V = (Vector)O;
    return V.elementAt(0)+","+V.elementAt(1);
      
   }


 
 /** Returns "Det Seg:Row,Col"
 */
 public String DataInfoLabel( int i )
  { return "Row,Col";
   }
 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  * @param  args[0]  The filename with the data set
  * @param  args[1]  The data set to have operators added {last one by default}
  */
 }
