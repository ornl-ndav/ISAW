/*
 * File:  LoadSCDCalib.java 
 *
 * Copyright (C) 2002, Peter Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.3  2003/02/06 20:34:47  dennis
 * Added getDocumentation() method. (Tyler Stelzer)
 *
 * Revision 1.2  2002/11/27 23:16:41  pfpeterson
 * standardized header
 *
 *
 */
package DataSetTools.operator.DataSet.Attribute;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
//import DataSetTools.util.LoadFileString;
import DataSetTools.operator.DataSet.Information.XAxis.SCDhkl;
import DataSetTools.util.*;
import DataSetTools.retriever.RunfileRetriever;
import java.util.*;
import java.util.Vector;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.io.*;

/** 
 * This operator will add the calibration information from the file
 * produced by A.J. Schultz's code. This sets the primary flight path,
 * detector center angle, secondary flight path, and the bin to real
 * space conversion information.
 */
public class LoadSCDCalib extends DS_Attribute{
    private static final String     TITLE  = "Load SCD Calibration";

    private int     detNum = 0;
    private float   detA   = 0f;
    private float   detD   = 0f;
    private float   L1     = 0f;
    private float[] calib = null;
    private float   T0     = 0f;
    private float   ax     = 0f;
    private float   ay     = 0f;
    private float   bx     = 0f;
    private float   by     = 0f;
    private String  descr  = null;

    /**
     *  Creates operator with title "Load SCD Calibration" and a
     *  default list of parameters.
     */  
    public LoadSCDCalib(){
	super( TITLE );
    }
    
    /** 
     *  Creates operator with title "Real Space Peaks" and the
     *  specified list of parameters. The getResult method must still
     *  be used to execute the operator.
     *
     *  @param calib_file Calibration file to use
     */
    public LoadSCDCalib( DataSet ds, String calib_file, int linenum,
                                                                 String groups){
	this(); 
	parameters = new Vector();
        addParameter( new Parameter("Calibration File",
                                           new LoadFileString(calib_file)));
        addParameter( new Parameter("Line to use", new Integer(linenum)));
        addParameter( new Parameter("Group IDs", new IntListString(groups)));

        setDataSet(ds); // record referece to the DataSet that this
                        // operator should operate on
    }

    /** 
     * Get the name of this operator to use in scripts
     * 
     * @return "LoadSCDCalib", the command used to invoke this
     * operator in Scripts
     */
    public String getCommand(){
        return "LoadSCDCalib";
    }

    /** 
     * Sets default values for the parameters.  This must match the
     * data types of the parameters.
     */
    public void setDefaultParameters(){
        parameters = new Vector();
        addParameter( new Parameter("Calibration File", new LoadFileString()) );
        addParameter( new Parameter("Line to use",      new Integer(1)));
        addParameter( new Parameter("Group IDs",        new IntListString() ) );
    }
    
    /** 
     *  Executes this operator using the values of the current parameters.
     *
     *  @return If successful, this operator returns a vector of Peak
     *  objects.
     */
    public Object getResult(){
        DataSet ds=getDataSet();
        Data    d=null;
        String calibfile   = getParameter(0).getValue().toString();
        int    linenum     = ((Integer)getParameter(1).getValue()).intValue();
        String list_string = getParameter(2).getValue().toString();

        File file=new File(calibfile);
        if(! file.exists() ){
            return new ErrorString("FAILURE: file does not exist");
        }else if(! file.canRead() ){
            return new ErrorString("FAILURE: cannot read file");
        }
        file=null;

        if(!this.readCalib(calibfile,linenum)){
            return new ErrorString("FAILURE");
        }

        if( list_string!=null && list_string.trim().length()!=0 ){
            // use the list we were given
            int[] ids=IntList.ToArray(list_string);
            for( int i=0 ; i<ids.length ; i++ ){
                d=ds.getData_entry_with_id(i);
                assoc(d,calibfile);
            }
        }else{
            // don't bother with the id list, it is empty
            for( int i=0 ; i<ds.getNum_entries() ; i++ ){
                d=ds.getData_entry(i);
                assoc(d,calibfile);
            }
        }

        return "Using '"+this.descr+"'";
    }
    
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){
        Operator op = new LoadSCDCalib();
        op.CopyParametersFrom( this );
        return op;
    }
    
    private void assoc( Data d, String filename ){
        FloatAttribute fa;
        StringAttribute sa;
        Float1DAttribute faa;
        if(d==null) return;
            
        Object detNumObj=d.getAttributeValue(Attribute.DETECTOR_IDS);
        int oldDetNum=0;
        if( detNumObj instanceof int[] ){
            oldDetNum=((int[])detNumObj)[0];
        }
        /*Vector detNums=
            (Vector)d.getAttributeValue(Attribute.DETECTOR_IDS);
        if(detNum != ((Integer)detNums.elementAt(0)).intValue() ){
            return;
            }*/

        if( oldDetNum!=0 && oldDetNum!=detNum ) return;

        //System.out.println(d);

        fa=new FloatAttribute(Attribute.DETECTOR_CEN_ANGLE,detA);
        d.setAttribute(fa);
        fa=new FloatAttribute(Attribute.DETECTOR_CEN_DISTANCE,detD);
        d.setAttribute(fa);
        fa=new FloatAttribute(Attribute.INITIAL_PATH,L1);
        d.setAttribute(fa);
        faa=new Float1DAttribute(Attribute.SCD_CALIB,this.calib);
        d.setAttribute(faa);
        sa=new StringAttribute(Attribute.SCD_CALIB_FILE,filename);
        d.setAttribute(sa);
    }

    /**
     * Read in the calibration parameters.
     */
    private boolean readCalib( String filename, int linenum ){
        StringBuffer calibline=null;
        TextFileReader tfr=null;

	try{
	    tfr=new TextFileReader(filename);
            String temp=null;
            for( int i=0 ; i<=linenum ; i++ ){
                temp=tfr.read_line();
            }
            if(temp!=null){
                calibline=new StringBuffer(temp);
            }
	}catch(IOException e){
            // something went wrong
	    return false;
	}finally{
            if(tfr!=null){
                try{
                    tfr.close();
                }catch(IOException e){
                }
            }else{
                return false;
            }
        }

        this.calib=new float[5];
        if(calibline!=null){
            StringUtil.trim(calibline);
            detNum=StringUtil.getInt(calibline);
            detA=StringUtil.getFloat(calibline);
            detD=StringUtil.getFloat(calibline);
            L1=StringUtil.getFloat(calibline);
            this.calib[0]=StringUtil.getFloat(calibline); // T0
            this.calib[1]=StringUtil.getFloat(calibline); // ax
            this.calib[2]=StringUtil.getFloat(calibline); // ay
            this.calib[3]=StringUtil.getFloat(calibline); // bx
            this.calib[4]=StringUtil.getFloat(calibline); // by
            descr=calibline.toString();
            
            detD=detD/100f;
            L1=L1/100f;

            return true;
        }else{
            return false;
        }
    }
    
    public String getDocumentation()
    {
      StringBuffer Res = new StringBuffer();
      Res.append("@overview This operator will add the calibration");
       Res.append(" information from the file produced by A.J. Schultz's code");

      Res.append("@algorithm This operator will add the calibration");
       Res.append(" information from the file produced by A.J. Schultz's");
       Res.append(" code. This sets the primary flight path, detector center");
       Res.append(" angle, secondary flight path, and the bin to real space");
       Res.append(" conversion information.");

      Res.append("@param ds The DataSet to operate on.");
      Res.append("@param calib_file Calibration file to use.");
      Res.append("@param linenum The line number to use.");
      Res.append("@param groups The group ID numbers to use."); 

      Res.append("@return Returns an error string if the file does not exist");
      Res.append(" or the file is not readable.  If it is successful, it");
      Res.append(" returns A vector of Peak objects.");

      Res.append("@error FAILURE: file does not exist");
      Res.append("@error FAILURE: file is not readable");
      Res.append("@error FAILURE");
  
     return Res.toString();
    }
}
