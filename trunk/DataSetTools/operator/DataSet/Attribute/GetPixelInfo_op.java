/*
 * File:  GetPixelInfo_op.java 
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
 * Revision 1.6  2004/01/22 02:39:54  bouzekc
 * Removed/commented out unused imports/variables.
 *
 * Revision 1.5  2003/12/30 13:09:18  rmikk
 * Fixed an error in the  non-default constructor
 *
 * Revision 1.4  2003/12/08 15:16:17  rmikk
 * The getResult method now returns a vector with 3 elements.  The last one
 * is the gridID.
 *
 * Revision 1.3  2003/06/18 20:36:13  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.2  2003/02/13 21:15:46  dennis
 * Fixed ordering to be "Col,Row" to match (x,y) convention
 * on area detectors.  Cleaned up docs after change from
 * detector to segment to pixel.
 *
 * Revision 1.1  2003/02/12 21:45:08  dennis
 * Copied and modified from GetSegmentInfo_op.
 *
 * Revision 1.2  2003/02/10 20:09:09  dennis
 * Added getDocumentation() method. (Tyler Stelzer)
 *
 * Revision 1.1  2003/01/15 20:21:51  dennis
 * Renamed from GetDetectorInfo_op
 *
 * Revision 1.3  2002/12/11 22:08:40  pfpeterson
 * Switched output from (row,col) to (col,row) in both numbers and label.
 *
 * Revision 1.2  2002/11/27 23:16:41  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/08/01 22:09:03  rmikk
 * Initial Checkin
 */
package DataSetTools.operator.DataSet.Attribute;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.Parameter;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import java.util.*;

/** 
 *  
 */
public class GetPixelInfo_op extends    DS_Attribute
                             implements IDataBlockInfo
{
  private static final String TITLE = "Pixel Info";

 /* ------------------------ Default constructor ------------------------- */ 
 /**
  *  Creates operator with title "Pixel Info" and a default list
  *  of parameters. This operator gets some of the Pixel Info 
  *  information for this data block.
  */  
  public GetPixelInfo_op()
  {
    super( TITLE );
    
  }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Creates operator that will give the column and row values corresponding
  *  to a given spectrum.  The getResult method returns the column and row 
  *  of the data block in a Vector of Integers.
  *
  * @param ds     The data set with the data block
  * @param index  the position of the desired data block in the data 
  *               block array<P>
  */
  public GetPixelInfo_op( DataSet ds, int index){
    this(); 
    this.setDataSet( ds);
    parameters = new Vector();
    parameters.addElement( new Parameter( "Data index=", new Integer( index)));
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "PixelInfo", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "PixelInfo";
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
  *  @return If successful, this operator returns a Vector containing the
  *  the column number, row number, and gridID of the data block indexed by the first
  *  parameter.  Otherwise an error string is returned.
  */
  public Object getResult(){
    DataSet ds = getDataSet();
    int index =((Integer)getParameter(0).getValue()).intValue();
    Data db = ds.getData_entry( index);
    if( db == null)
      return new DataSetTools.util.ErrorString(
                                  "No data block at position "+ index);
    Object O = db.getAttributeValue(Attribute.PIXEL_INFO_LIST);
    if( O == null)
      return new DataSetTools.util.ErrorString( 
                                  "Data block has no PixelInfoList Attribute");
    
    PixelInfoList pil = (PixelInfoList)O;
    if( pil.num_pixels() < 1)
      return  new DataSetTools.util.ErrorString( 
                                   "Data block has no PixelInfo in list");
    Vector V = new Vector();
    
    V.addElement(new Integer((int)pil.col()));
    V.addElement(new Integer((int)pil.row()));
    V.addElement( new Integer( pil.pixel(0).gridID()));
    return V;
    
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    DataSetOperator op = new GetPixelInfo_op();
    op.CopyParametersFrom( this );
    op.setDataSet( this.getDataSet());
    return op;
  }

 /** 
  *  Returns a String containing the column and row numbers for the data block 
  *  specified by the given index.  The specified index is set as the current
  *  value of parameter 0 in the operator.
  */
 public String DataInfo( int index )
  { 
    parameters= new Vector();
    parameters.addElement( new Parameter("index", new Integer(index)));
    Object O= getResult();

    if( O instanceof ErrorString) 
      return "NaN";
    else if( O == null)
      return "null";
    else if( ! (O instanceof Vector))
      return StringUtil.toString(O); 

    Vector V = (Vector)O;
    return V.elementAt(0)+","+V.elementAt(1);
   }

 
 /** 
  *  Returns the String to use as a label.
  */
  public String DataInfoLabel( int i )
  { 
    return "Col,Row";
  }
   
   
 public String getDocumentation()
    {
      StringBuffer Res = new StringBuffer();
      Res.append("@overview This operator returns a vector representing");
       Res.append(" the column and row corresponding to a given spectra");

      Res.append("@algorithm Checks to make sure all of the data exists.  If");
       Res.append(" it is, it will give the row corresponding to a given");
       Res.append(" spectra");

      Res.append("@param ds The data set with the data block");
      Res.append("@param index  the position of the desired data block in");
       Res.append(" the data block array");

      Res.append("@return If successful, this operator returns a Vector whose ");
      Res.append("entries are the column(Integer), row(Integer), then gridID.");
       Res.append(" Otherwise, it  returns an error string.");

      Res.append("@error No data block at position < index >");
      Res.append("@error Data block has no PixelInfoList Attribute");
  
     return Res.toString();
    }  
   
   
 }
