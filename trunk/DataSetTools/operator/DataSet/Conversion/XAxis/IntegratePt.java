
/*
 * File:  IntegratePt.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.10  2006/03/15 14:53:48  dennis
 * Changed to use an array of floats instead of an array of ints,
 * to avoid having to make a new copy of the data as ints.
 *
 * Revision 1.9  2006/03/14 23:35:22  dennis
 * Made some improvements to the efficiency of the calculation.
 * 1. When the data grid is changed, the data is copied more
 *    efficiently.
 * 2. The value is not recalculated if the Data block index is
 *    not changed and the time-of-flight value is not changed.
 * Also converted from DOS to UNIX text.
 *
 * Revision 1.8  2005/01/02 17:54:05  rmikk
 * Trapped for a null pointer exception error
 *
 * Revision 1.7  2004/08/17 20:38:29  rmikk
 * Set the operator several times
 *
 * Revision 1.6  2004/08/13 03:28:35  millermi
 * - Fixed javadoc errors.
 * - Split up lines exceeding 80 characters.
 *
 * Revision 1.5  2004/08/02 20:09:00  rmikk
 * ISX, ISY, and ISZ can now be set
 * The Integrate one peak is now NOT static to this class
 * A clone method now copies all the information of the Integrate one peak
 * method
 *
 * Revision 1.4  2004/07/30 14:51:35  rmikk
 * Removed unused imports
 *
 * Revision 1.3  2004/07/30 14:09:50  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.2  2004/06/23 20:37:26  rmikk
 * Reduced the number of times the data has to be copied to an arrray
 * Improved Documentation
 * Removed a DataSet parameter in the base method for this class
 *
 * Revision 1.1  2004/06/18 22:18:27  rmikk
 * Initial Checkin- Integrate Information operator
 *
 */

package DataSetTools.operator.DataSet.Conversion.XAxis;
import DataSetTools.dataset.*;
import Operators.TOF_SCD.*;
import DataSetTools.operator.*;
//import DataSetTools.operator.DataSet.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import java.util.*;
import DataSetTools.parameter.*;
import java.lang.reflect.*;

/**
 * @author mikkelsonr
 *
 * This Class gets the integrated value and deviation for a peak
 */
public class IntegratePt extends
                          DataSetTools.operator.DataSet.Math.Analyze.AnalyzeOp  
                         implements IDataPointInfo {
  DataSet DS;
  float [][][] JHist = null;
  int id =-1;
  private Wrappable op;
  int ISX = 1;
  int ISY = 1;
  int ISZ = 1;

  float     last_time = -1; 
  int       last_dataBlockIndex = -1;
  Wrappable last_op1    = null;
  Vector    last_result = null;

  public IntegratePt(){
    super("IntegratePt");
    DS = null;
    setIntgratePkOp(op,1,1,1);
  }
  

  /**
     *  Constructor 
     *  @param DS  The DataSet of interest
     *  @param time   The time of the associated peak
     *  @param dataBlockIndex   The INDEX of the datablock where the peak is
     *                          centered.
     */
  public IntegratePt( DataSet DS,  float time, int dataBlockIndex ){
    this();
    this.setDataSet(DS);
    this.DS = DS;
    parameters = new Vector();
    parameters.addElement(new IntegerPG("Group Index", 0));
    parameters.addElement(new FloatPG("Time", 0.0f));
    setIntgratePkOp(new INTEG(), 1,1,1);
    // Set default parameters to the above
    // getResult will integrate the given peak
  }
  
  
  /**
   *   Sets the DataSet in this data set operator
   */
  public void setDataSet( DataSet DS){
    this.DS = DS;
    super.setDataSet( DS);
  }


 //--------------------- IDataInfo methods---------------------- 
  /**
   *   Applies the INTEG operator to the ith data block and time x
   *  @param x   The time of the associated peak
   *  @param i   The INDEX of the datablock where the peak is centered.
   *  @return  The string representation of the total intensity(- background)
   *  at that point, or whatever the INTEG operator leaves in its variable ITOT
   */
  public java.lang.String PointInfo(float x,
                                    int i){
    DS = getDataSet();
    if( DS == null)
       return "";
    Vector V = Integrate( x , i, op );
    if( V == null)
      return "";
    if( V.size()<1)
      return "";
    return V.elementAt(0).toString();
  }
  

  public String PointInfoLabel(float x, int i ){
     return "integrate";
  }
 
   //---------------------- operator methods ----------------
   public void setDefaultParameters(){
     parameters = new Vector();
     parameters.addElement(new IntegerPG("Group Index", 0));
     parameters.addElement(new FloatPG("Time", 0.0f));
   }


   public String getCommand(){
      return "IntegratePt";
   }


  public String getDocumentation(){
    StringBuffer s = new StringBuffer("");
      s.append("@overview This operator gets the integrated value and ");
      s.append("its error for a peak");
      s.append("@algorithm  Converts the given information to the information");
      s.append("needed by the Operators.TOF_SCD.INTEG operator to calculate ");
      s.append("the desired result. Changing the INTEG operator can change ");
      s.append("the integrate peak algorithm. This INTEG operator may become ");
      s.append("a variable in the future");
      s.append("@param DS  The DataSet of interest ");
      s.append("@param x   The time of the associated peak ");
      s.append("@param i   The INDEX of the datablock where the peak is ");
      s.append("centered. @return  A Vector with two elements, ITOT(The  ");
      s.append("integrated value of the peak), SIGI(The standard deviation ");
      s.append("of ITOT), null, or an ErrorString ");
      return s.toString();
  }


  public Object getResult(){
       int GroupIndex = ((IntegerPG)getParameter(0)).getintValue();
       float time = ((FloatPG)getParameter(1)).getfloatValue();
       DS = getDataSet();
       if( DS == null)
         return new ErrorString("No DataSet Associated with this DataSet "+
	                        "operator");
       Vector V = Integrate( time, GroupIndex, op);
       if( V == null)
          V = new Vector();
       return V;     
  }


 //----------------------- Wrappable plug-ins----------------------
 public void setIntgratePkOp( Wrappable op1, int ISX ,int ISY,int ISZ){
	if( op1 ==  null)
	  op = new INTEG();
	if( !check(op1))
	  op = new INTEG();
	else
	  op = op1;
	this.ISX = ISX;
	this.ISY = ISY;
	this.ISZ = ISZ;
    
  }

  
  private static boolean errmsg( String Message){
    DataSetTools.util.SharedData.addmsg(Message);
    return false;
  }
  
  
  private static boolean check( Wrappable op){
    if( op == null)
      return false;
    String FieldName=null;
    try{
      FieldName = "JHIST";
      Field F = op.getClass().getField("JHIST");
      int mods = F.getModifiers();
      if(! Modifier.isPublic(mods))
        return errmsg("No public JHIST Field");

      FieldName = "X";
      F = op.getClass().getField("X");
      mods = F.getModifiers();
      if(! Modifier.isPublic(mods))
        return errmsg("No public X Field");

      FieldName = "Y";
      F = op.getClass().getField("Y");
      mods = F.getModifiers();
      if(! Modifier.isPublic(mods))
        return errmsg("No public Y Field");
        

      FieldName = "Z";
      F = op.getClass().getField("Z");
      mods = F.getModifiers();
      if(! Modifier.isPublic(mods))
        return errmsg("No public Z Field");
        

      FieldName = "ITOT";
      F = op.getClass().getField("ITOT");
      mods = F.getModifiers();
      if(! Modifier.isPublic(mods))
        return errmsg("No public ITOT Field");

      FieldName = "SIGITOT";
      F = op.getClass().getField("SIGITOT");
      mods = F.getModifiers();
      if(! Modifier.isPublic(mods))
        return errmsg("No public SIGITOT Field");
    }catch( Exception ss){
      if( FieldName == null)
         return false;
      if( ss instanceof NoSuchFieldException )
         return errmsg( "No "+FieldName+" Field");
      else
         return errmsg("Access to "+FieldName+" denied");
    }
    
    return true;
  }


 //--------------------- Base method for all interfaces -------------- 
  /**
    *	Applies the INTEG operator to the ith data block of the data set DS
    *	and time x
    *
    *  @param time   The time of the associated peak
    *  @param dataBlockIndex   The INDEX of the datablock where the peak is
    *                          centered.
    *  @param op1  The wrappable that will actually integrate the peak
    *  @return  A Vector with two elements, ITOT, SIGI, null, or an ErrorString
    */
  public Vector Integrate( float time, int dataBlockIndex, Wrappable op1){
  
    if( dataBlockIndex < 0)
      return null;

    if( Float.isNaN(time))
      return null;

    if (op1 == null)
      if( op != null)
         op1 = op;
      else
         op1 = new INTEG();
                                                   // if this is the same time
                                                   // data block and op, just
                                                   // return the same value
    if ( time == last_time                     &&
         dataBlockIndex == last_dataBlockIndex &&
         op1            == last_op1            )
    {
      if ( last_result == null )
        return null; 

      Vector saved_result = new Vector();
      for ( int i = 0; i < last_result.size(); i++ )
        saved_result.add( last_result.elementAt(i) );
      return saved_result;
    }


    //INTGT op = new INTGT();
   
    Data D = DS.getData_entry( dataBlockIndex);
    if( D == null)
      return null;
    int row, col;
    PixelInfoList pilist=(PixelInfoList)D.getAttributeValue
                                  (Attribute.PIXEL_INFO_LIST);
    IDataGrid gr= pilist.pixel(0).DataGrid() ; 
    row= (int)pilist.pixel(0).row();
    col= (int)pilist.pixel(0).col(); 
    if( (row <1)||(col<1))
      return null;
       
    int numrows =gr.num_rows();
    int numcols = gr.num_cols();
    XScale xscl=D.getX_scale();
    int nchannels = xscl.getNum_x()-1;
    int channel = xscl.getI(time);
    if( channel < 1 )
       return null;
    if( channel > nchannels)
       return null;
   
    if( (JHist == null) ||(gr.ID()!=id)){
     
      JHist = new float[numrows][numcols][nchannels];
      id = gr.ID();
      //gr.setData_entries(DS);
      if( gr == null)
         return  null;
      for( int i=1; i<= numrows;i++)
        for( int j = 1; j <= numcols; j++)
        {
          if(gr.getData_entry(j,i) != null)
          {
            float y_vals[] = gr.getData_entry(j,i).getY_values();
            if ( y_vals != null )
              JHist[i-1][j-1] = y_vals;
          }
        }
    }
    setintField(op1,"ISX",ISX) ;
    setintField(op1,"ISY",ISY) ;
    setintField(op1,"ISZ",ISZ) ;
    setintField(op1,"X",col) ;
    setintField(op1,"Y",row) ; 
    setintField(op1,"Z",channel) ;
    setintField(op1,"NXS",numcols) ;
    setintField(op1,"NYS",numrows) ;
    setintField(op1,"WLNUM",nchannels) ;
    setfloatField(op1, "ITOT", 0f);
    setObjField(op1 ,"JHIST", JHist);
    setObjField(op1,"NTIME", D.getX_scale().getXs());

    Object O = op.calculate();
    if( O instanceof ErrorString){
      DataSetTools.util.SharedData.addmsg("Integrate Error "+
                                          ((ErrorString)O).toString());
      return null;
    }   
    Vector V = new Vector();
    V.addElement(getfloatField(op,"ITOT")); 
    V.addElement(getfloatField(op,"SIGITOT"));                     

    last_time           = time;                    // save parameters and
    last_dataBlockIndex = dataBlockIndex;          // results so that we don't
    last_op1            = op1;                     // keep recalculating them
    last_result         = new Vector( V.size() );  // when this is called for
    for ( int i = 0; i < V.size(); i++ )           // windows being uncovered
      last_result.addElement( V.elementAt(i) ); 

    return V; 
  }


  private void setintField( Wrappable op, String Name, int val){
    try{
      Field F =  op.getClass().getField(Name);
      F.set(op,new Integer(val));  
    }catch(Exception ss){
    }
  }

  
  private Object getfloatField( Wrappable op, String Name){
     try{
        Field F = op.getClass().getField(Name);
        float f = F.getFloat( op);
        return new Float(f);
     }catch(Exception s){
       return null;
     }
  }


  private void setfloatField( Wrappable op, String Name, float val){
    try{
      Field F =  op.getClass().getField(Name);
      F.set(op,new Float(val));  
    }catch(Exception ss){
    }
  }
  

  private void setObjField( Wrappable op, String Name, Object val){
    try{
      Field F =  op.getClass().getField(Name);
      F.set(op,(val));  
    }catch(Exception ss){
    }
  }

  
  public Object clone(){
  	 IntegratePt Res = new IntegratePt();
  	 Res.setDataSet( getDataSet());
  	 Res.setIntgratePkOp( op, ISX,ISY,ISZ);
  	 return Res;
  	  
  }
}
