
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
 * Revision 1.1  2004/06/18 22:18:27  rmikk
 * Initial Checkin- Integrate Information operator
 *
 */

package DataSetTools.operator.DataSet.Conversion.XAxis;
import DataSetTools.dataset.*;
import Operators.TOF_SCD.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import java.util.*;
import DataSetTools.parameter.*;
import java.lang.reflect.*;
/**
 * @author mikkelsonr
 *
 * This Class gets the integrated value and deviation for a peak
 */
public class IntegratePt extends DataSetTools.operator.DataSet.Math.Analyze.AnalyzeOp  
                             implements IDataPointInfo {
  DataSet DS;
  private static Wrappable op = new INTEG();
  public IntegratePt(){
    super("Integrate1");
    DS = null;
  }

  
  
  /**
     *  Constructor 
     *  @param DS  The DataSet of interest
     *  @param x   The time of the associated peak
     *  @param i   The INDEX of the datablock where the peak is centered.
     *  @return  after getResult method-A Vector with two elements, ITOT(The 
     *      integrated value of the peak), SIGI(The standard deviation of ITOT),
     *       null, or an ErrorString
     */
  public IntegratePt( DataSet DS,  float time, int dataBlockIndex ){
    this();
    this.setDataSet(DS);
    this.DS = DS;
    parameters = new Vector();
    parameters.addElement(new IntegerPG("Group Index", 0));
    parameters.addElement(new FloatPG("Time", 0.0f));
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
   *  @return  The string representation of the total intensity(- background) at that
   *      point, or whatever the INTEG operator leaves in its variable ITOT
   */
  public java.lang.String PointInfo(float x,
                                    int i){
    DS = getDataSet();
    if( DS == null)
       return "";
    Vector V = Integrate( DS , x , i, op );
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
         s.append("@param i   The INDEX of the datablock where the peak is centered. ");
         s.append("@return  A Vector with two elements, ITOT(The  ");
         s.append("integrated value of the peak), SIGI(The standard deviation of ITOT), ");
         s.append("null, or an ErrorString ");
         return s.toString();

    
  }
  public Object getResult(){
       int GroupIndex = ((IntegerPG)getParameter(0)).getintValue();
       float time = ((FloatPG)getParameter(1)).getfloatValue();
       DS = getDataSet();
       if( DS == null)
         return new ErrorString("No DataSet Associated with this DataSet operator");
       Vector V = Integrate( DS, time, GroupIndex, op);
       if( V == null)
          V = new Vector();
       return V;     
  }
 //----------------------- Wrappable plug-ins----------------------
  public static void setIntgratePkOp( Wrappable op){
    if( op ==  null)
      IntegratePt.op = new INTEG();
    if( !check(op))
      IntegratePt.op = new INTEG();
    else
      IntegratePt.op = op;
    
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
      *   Applies the INTEG operator to the ith data block of the data set DS and time x
      *  @param DS  The DataSet of interest
      *  @param x   The time of the associated peak
      *  @param i   The INDEX of the datablock where the peak is centered.
      *  @param op  The wrappable that will actually integrate the peak
      *  @return  A Vector with two elements, ITOT, SIGI, null, or an ErrorString
      */
  public Vector Integrate(DataSet DS, float time, int dataBlockIndex, Wrappable op){
  
    if( dataBlockIndex < 0)
      return null;
    if( Float.isNaN(time))
      return null;
    if (op == null)
       op = new INTGT();
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
   
    
    int [][][]JHist = new int[numrows][numcols][nchannels];
    //gr.setData_entries(DS);
    for( int i=1; i<= numrows;i++)
      for( int j = 1; j <= numcols; j++)
        for( int k=0; k< nchannels -1; k++)
          JHist[i-1][j-1][k] =(int) gr.getData_entry(j,i).getY_values()[k];
   
    setintField(op,"ISX",1) ;
    setintField(op,"ISY",1) ;
    setintField(op,"ISZ",1) ;
    setintField(op,"X",col) ;
    setintField(op,"Y",row) ; 
    setintField(op,"Z",channel) ;
    setintField(op,"NXS",numcols) ;
    setintField(op,"NYS",numrows) ;
    setintField(op,"WLNUM",nchannels) ;
    setfloatField(op, "ITOT", 0f);
    setObjField(op ,"JHIST", JHist);
    setObjField(op,"NTIME", D.getX_scale().getXs());

    Object O = op.calculate();
    if( O instanceof ErrorString){
      DataSetTools.util.SharedData.addmsg("Integrate Error "+((ErrorString)O).toString());
      return null;
    }   
    Vector V = new Vector();
    V.addElement(getfloatField(op,"ITOT")); 
    V.addElement(getfloatField(op,"SIGITOT"));                     
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
}
