/*
 * File:  LoadGsasCalib.java
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
 *           Intense Pulsed Neutron Source
 *           Argonne National Laboratory
 *           Argonne, IL 60439-4845
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2002/07/10 16:04:22  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.retriever.RunfileRetriever;
import  DataSetTools.gsastools.GsasCalib;

/**
 * This operator loads the time-of-flight to d-space conversion
 * parameters from a GSAS instrument parameter file. The only lines
 * that are read are the ICONS lines.
 */

public class LoadGsasCalib extends    DS_Attribute {
    private static boolean DEBUG=false;
    private boolean seq_numbering;

    /* ---------------------- DEFAULT CONSTRUCTOR ------------------------ */
    /**
     * Construct an operator with a default parameter list.  If this
     * constructor is used, the operator must be subsequently added to
     * the list of operators of a particular DataSet.  Also,
     * meaningful values for the parameters should be set ( using a
     * GUI ) before calling getResult() to apply the operator to the
     * DataSet this operator was added to.
     */

    public LoadGsasCalib( ){
        super( "Load Gsas Calibration" );
        this.seq_numbering=false;
    }
    
  
    /* -------------------- FULL CONSTRUCTOR -------------------------- */
    /**
     *  Construct an operator for a specified DataSet and with the
     *  specified parameter values so that the operation can be
     *  invoked immediately by calling getResult().
     *
     *  @param  ds          The DataSet to which the operation is applied
     *  @param  iparm       The name of the iparm file.
     */

    public LoadGsasCalib( DataSet ds, LoadFileString  iparm, boolean seq_num ){
        this();                       // do the default constructor, then set
                                      // the parameter value(s) by altering a
                                      // reference to each of the parameters

        parameters=new Vector();
        addParameter( new Parameter("Instrument Parameter File", iparm) );
        addParameter( new Parameter("Sequential Bank Numbering",
                                    new Boolean(seq_num)) );

        setDataSet( ds );         // record reference to the DataSet that
                                  // this operator should operate on
    }


    /* -------------------------- getCommand ----------------------------- */
    /**
     * @return     the command name to be used with script processor: in
     *             this case, LoadGsasCalib
     */
    public String getCommand(){
        return "LoadGsasCalib";
    }


    /* ------------------------ setDefaultParmeters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        parameters = new Vector();  // must do this to clear any old parameters
        
        addParameter(new Parameter( "Instrument Parameter File",
                                    new LoadFileString("")));
        addParameter( new Parameter("Sequential Bank Numbering", 
                                    new Boolean(false)) );
    }
    
    
    /* -------------------------- getResult ----------------------------- */
    
    public Object getResult(){  
        DataSet ds = getDataSet();
        String iparm = (String)getParameter(0).getValue();
        iparm=FilenameUtil.fixSeparator(iparm);
        this.seq_numbering=
            ((Boolean)getParameter(1).getValue()).booleanValue();
        if(DEBUG)System.out.println("seq_numbering="+this.seq_numbering);
        TextFileReader fr=null;

        try{
            fr = new TextFileReader(iparm);
            String temp;
            int bankNum;
            float dif_c,dif_a,t_zero;
            while(!fr.eof()){
                fr.skip_blanks();
                if(fr.read_line().indexOf("ICONS")>0){
                    fr.unread(); //found a good line, put it back in the stream
                    fr.read_String(); //skip the INS tag
                    temp=fr.read_String(); //read the next tag
                    if(temp.endsWith("ICONS")){
                        int index=temp.indexOf("ICONS");
                        temp=temp.substring(0,index);
                        bankNum=(new Integer(temp)).intValue();
                    }else{
                        fr.unread();
                        bankNum=fr.read_int();
                        fr.read_String();
                    }
                    dif_c=fr.read_float();
                    dif_a=fr.read_float();
                    t_zero=fr.read_float();
                    fr.read_line();
                    this.associate(bankNum,dif_c,dif_a,t_zero);
                }
            }
        }catch(IOException e){
            System.err.println("IOException: "+e.getMessage());
        }finally{
            if(fr!=null) try{
                fr.close();
            }catch(IOException e){
                System.err.println("Could not close "+iparm+":"
                                   +e.getMessage());
            }
        }
        int index=iparm.lastIndexOf("/");
        if(index>=0)iparm=iparm.substring(index+1,iparm.length());
        ds.setAttribute(new StringAttribute(Attribute.GSAS_IPARM,iparm));
        ds.addLog_entry("Read Instrument Parameter File: "+iparm);
        return "Read Instrument Parameter File";
    }  

    /**
     * Method to associate the gsas information with the data block
     * specified.
     */
    private void associate(int bankNum,float dif_c,float dif_a,float t_zero){
        DataSet ds = getDataSet();
        Data d=null;
        GsasCalibAttribute calibAttr;

        // get the appropriate data block
        if(DEBUG)System.out.print(bankNum+": ");
        if(this.seq_numbering){
            d=ds.getData_entry(bankNum-1);
        }else{
            d=ds.getData_entry_with_id(bankNum);
        }
        if(d==null){ // don't bother if can't do anything
            if(DEBUG)System.out.println("failed");
            return;
        }

        // add the attribute to the data block
        if(DEBUG)System.out.println(dif_c+", "+dif_a+", "+t_zero);
        calibAttr=new GsasCalibAttribute(Attribute.GSAS_CALIB,
                                         new GsasCalib(dif_c,dif_a,t_zero));
        d.setAttribute(calibAttr);
    }

    /* ---------------------------- clone ----------------------------- */
    /**
     * Get a copy of the current LoadGsasCalib Operator.  The list of
     * parameters and the reference to the DataSet to which it applies
     * is copied.
     */
    public Object clone(){
        LoadGsasCalib new_op    = new LoadGsasCalib( );
                                              // copy the data set associated
                                              // with this operator
        new_op.setDataSet( this.getDataSet() );
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }

    public static void main(String args[]){
        String runfile="/IPNShome/pfpeterson/data/II_VI/SEPD/"
            +"dec2001/runfiles/sepd18124.run";
        LoadFileString offfile
            =new LoadFileString("/IPNShome/pfpeterson/trial.offsets");
        //System.out.println(runfile);
        DataSet rds=(new RunfileRetriever(runfile)).getDataSet(1);
        
        LoadGsasCalib op=new LoadGsasCalib(rds,offfile,false);
        op.getResult();
    }
}
