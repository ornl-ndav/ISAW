/*
 * File:  GsasRetriever.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.5  2003/02/17 22:24:12  pfpeterson
 *  Updated deprecated method calls to what is now used.
 *
 *  Revision 1.4  2002/11/27 23:23:15  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/10/03 15:50:50  dennis
 *  Replace call to Data.setSqrtErrors() to Data.setSqrtErrors(true)
 *
 *  Revision 1.2  2002/09/27 17:48:52  pfpeterson
 *  Now supports loading files with TIME_MAPs
 *
 *  Revision 1.1  2002/08/06 22:01:51  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.retriever;

import java.io.*;
import java.util.Vector;
import DataSetTools.dataset.*;
import DataSetTools.gsastools.*;
import DataSetTools.instruments.InstrumentType;
import DataSetTools.operator.Parameter;
import DataSetTools.operator.DataSet.Attribute.LoadGsasCalib;
import DataSetTools.operator.DataSet.DataSetOperator;
import DataSetTools.util.*;

/**
 * Base class for objects that retrieve DataSet objects from files, or via 
 * a network connection.  Derived classes for particular types of data
 * sources must actually implement the methods to get specified DataSets
 * and their types.
 */

public class GsasRetriever extends Retriever{
    private static final int  INVALID_DS   = Retriever.INVALID_DATA_SET;
    private static final int  MONITOR_DS   = Retriever.MONITOR_DATA_SET;
    private static final int  HISTOGRAM_DS = Retriever.HISTOGRAM_DATA_SET;

    // string constants for bintypes
    private static String COND    = GsasUtil.COND;
    private static String CONS    = GsasUtil.CONS;
    private static String CONQ    = GsasUtil.CONQ;
    private static String SLOG    = GsasUtil.SLOG;
    private static String TIMEMAP = GsasUtil.TIMEMAP;

    // string constants for types (how errors are written)
    private static String STD = GsasUtil.STD;
    private static String ESD = GsasUtil.ESD;

    // constants for labeling things
    private static String BANK  = GsasUtil.BANK;
    private static String IPARM = "Instrument parameter file:";
    private static String MONITOR = "MONITOR";

    // set debug mode
    private static boolean  DEBUG = false;

    // instance variables
    private int       numDataSet = 0;
    private String    runtitle;
    private String    iparmfile;
    private DataSet[] dataset;
    private Vector    timemaps;

    /* ------------------------ Constructor -------------------------- */
    /**
     * Construct the retriever for the specified source name.
     *
     * @param data_source_name   This identifies the data source.  For file
     *                           data retrievers, this should be the fully 
     *                           qualified file name
     */
    public GsasRetriever( String filename ){
        super(filename);
        //System.err.println("IN CONSTRUCTOR");
        this.numDataSet       = 0;
        this.runtitle         = null;
        this.iparmfile        = null;
        Vector data             = new Vector();
        Vector data_xunit       = new Vector();
        TextFileReader reader = null;
        try{
            // create a new reader object
            reader=new TextFileReader(filename);
            String line=null;

            // read in the run title
            line=reader.read_line();
            this.runtitle=line.trim();

            // read in the iparm file (if it is there)
            line=reader.read_line();
            line=line.trim();
            if(line.startsWith(IPARM)){
                this.iparmfile=line.substring(IPARM.length(),line.length());
                line=reader.read_line();
            }else{
                reader.unread();
            }

            // read the banks of data into the two datasets
            while(true){
                //System.out.print("b");
                line=reader.read_line();
                line=line.trim();
                //System.out.println(line);
                if(line.startsWith(TIMEMAP)){
                    this.readTimeMap(reader,line);
                }
                if(line.startsWith(BANK)){
                    //System.out.println(line);
                    reader.unread();
                    this.readBank(reader,data,data_xunit);
                }
            }
        }catch(IOException e){
            // let it drop on the floor
        }finally{
            //System.out.println("c");
            if(reader!=null){
                try{
                    reader.close();
                }catch(IOException e){
                    // let it drop on the floor
                }
            }
        }
        if(DEBUG){
            System.out.println("TITLE:"+this.runtitle);
            System.out.println("IPARM:"+this.iparmfile);
            for( int i=0 ; i<data.size() ; i++ ){
                System.out.println("DATA("+i+"):"+data.elementAt(i));
            }
        }
        this.initDataSets(data,data_xunit);

    }


    /* ------------------------ numDataSets -------------------------- */
    /**
     * Get the number of distinct DataSets that can be obtained from the
     * current data source.
     *
     *  @return The number of distinct DataSets available.  This function
     *          may return values < 0 as an error code if there are no
     *          DataSets available.
     */
    public int numDataSets(){
        return this.numDataSet;
    }

    
    /* -------------------------- getDataSet ---------------------------- */
    /**
     * Get the specified DataSet from the current data source.
     *
     * @param data_set_num  The number of the DataSet in this runfile
     *                      that is to be read from the runfile.  data_set_num
     *                      must be between 0 and numDataSets()-1
     *
     * @return The specified DataSet, if it exists, or null if no such
     *         DataSet exists.
     */
    public DataSet getDataSet( int data_set_num ){
        if(data_set_num>numDataSet){
            return null;
        }else{
            return dataset[data_set_num];
        }
    }


    /* ---------------------------- getType ------------------------------ */
    /**
     *  Get the type code of a particular DataSet in this runfile.
     *  The type codes include:
     *
     *     Retriever.INVALID_DATA_SET
     *     Retriever.MONITOR_DATA_SET
     *     Retriever.HISTOGRAM_DATA_SET
     *     Retriever.PULSE_HEIGHT_DATA_SET
     *
     *  @param  data_set_num  The number of the DataSet in this runfile whose
     *                        type code is needed.  data_set_num must be between
     *                        0 and numDataSets()-1
     *
     *  @return the type code for the specified DataSet.
     */
    public int getType( int data_set_num ){
        if(data_set_num>this.numDataSet){
            return INVALID_DS;
        }else{
            return HISTOGRAM_DS;
        }
    }

    /**
     * This constructs an XScale from the time_map read in and adds it
     * to the timemaps vector at the mapnum position.
     */
    private boolean readTimeMap(TextFileReader reader, String header){
        // convert the header into a StringBuffer for parsing
        StringBuffer sb=new StringBuffer(header);

        // parse the header to get some useful parameters
        StringUtil.getString(sb); // drop the TIME_MAP on the floor;
        int mapnum=StringUtil.getInt(sb);
        int nval=StringUtil.getInt(sb);
        nval=(nval-1)/3;
        int nrec=StringUtil.getInt(sb);
        StringUtil.getString(sb); // drop the TIME_MAP on the floor;
        float clockwidth=StringUtil.getFloat(sb);
        clockwidth=clockwidth/1000f; // convert from ns to us

        if(DEBUG){
            System.out.println("TIMEMAP="+mapnum+" "+nval+" "+nrec+" "
                               +clockwidth);
        }

        // create arrays to hold the values
        float[] time  = new float[nval];
        float[] dt    = new float[nval];
        float   etime = 0f;
        
        // read in the time_map
        String line=null;
        float temp=0f;
        float nchan=0f;
        for( int i=0, j=0, k=0 ; i<nrec ; i++ ){
            try{
                line=reader.read_line();
            }catch(IOException e){
                return false;
            }
            sb=new StringBuffer(line);
            sb.delete(0,1);  // get rid of the space the is added by read_line
            for( int l=0 ; l<10 ; l++ ){
                temp=StringUtil.getFloat(sb,8);
                if(j==nval){ // the last number is the end time
                    etime=temp;
                    break;
                }
                if(k==0){ // the first in each triplet is the channel
                    nchan=temp;
                    k++;
                }else if(k==1){  // the second is the time
                    time[j]=temp;
                    k++;
                }else if(k==2){ // the third is the bin width
                    dt[j]=temp;
                    k=0;
                    j++;
                }
            }
        }

        // generate the x-values from the time map
        int x_size=(int)nchan+(int)((etime-time[nval-1])/dt[nval-1]);
        float[] xval=new float[x_size];
        xval[0]=time[0];
        boolean first=true;
        for( int i=1, j=0 ; i<x_size ; i++ ){
            if(j+1==time.length){
                if(xval[i-1]+dt[j]>=etime){
                    xval[i]=etime;
                }else{
                    xval[i]=xval[i-1]+dt[j];
                }
            }else{
                xval[i]=xval[i-1]+dt[j];
                if(xval[i]+dt[j]>=time[j+1]){
                    j++;
                }
            }
        }

        // multiply by the clock width
        for( int i=0 ; i<xval.length ; i++ ){
            xval[i]=xval[i]*clockwidth;
        }
        
        // get the xscale
        XScale xscale=XScale.getInstance(xval);
        if(xscale!=null){
            if(timemaps==null) timemaps=new Vector();
            while(timemaps.size()<mapnum){
                timemaps.add(null);
            }
            timemaps.add(mapnum,xscale);
        }else{
            return false;
        }

        return true;
    }

    /**
     * Method to read in a single bank.
     */
    private boolean readBank(TextFileReader reader, Vector data,
                             Vector data_xunit){
        String line=null;
        int bankNum=-1;
        Data datablock=null;
        try{
            reader.read_String(5);     // get rid of the "BANK" tag
            bankNum=reader.read_int(); // determine the bank number
            line=reader.read_line();   // get the rest for XInfo
        }catch(IOException e){
            return false;
        }
        XInfo info=null;
        if(line!=null){
            info=new XInfo(line);
        }
        //System.out.println("INFO("+bankNum+"):"+info.nchan()+" "+info.nrec()+" "+info);
        XScale xscale=null;
        if(info.bintype().equals(TIMEMAP)){
            xscale=(XScale)timemaps.elementAt((int)info.coef1());
        }else{
            xscale=GsasUtil.getXScale(info);
        }
        float[] y_val  = new float[info.nchan()];
        float[] dy_val = new float[info.nchan()];
        int chan_per_line=0;
        boolean is_esd=false;
        if(info.type().equals(STD)){
            chan_per_line=10;
        }else{
            chan_per_line=5;
            is_esd=true;
        }
        //System.out.println("reading in data for bank "+bankNum);
        try{
            if(is_esd){
                int count=0;
                StringBuffer sb;
                for( int i=0 ; i<info.nrec() ; i++ ){
                    sb=new StringBuffer(reader.read_line());
                    sb.deleteCharAt(0);
                    for( int j=0 ; j<chan_per_line ; j++ ){
                        if(count>=info.nchan()){
                            i=info.nrec();
                            j=chan_per_line;
                            break;
                        }
                        y_val[count]  = StringUtil.getFloat(sb,8);
                        dy_val[count] = StringUtil.getFloat(sb,8);
                        count++;
                    }
                }
            }else{
                for( int i=0 ; i<info.nchan() ; i++ ){
                    y_val[i]=reader.read_float();
                }
            }
        }catch(IOException e){
            // let it drop on the floor but return failure
            SharedData.addmsg("IOException:"+e.getMessage());
            return false;
        }catch(NumberFormatException e){
            // let it drop on the floor but return failure
            SharedData.addmsg("NumberFormatException:"+e.getMessage());
            return false;
        }
        if(xscale==null) return false; // something went wrong
        if(is_esd){
            datablock=new HistogramTable(xscale,y_val,dy_val,bankNum);
        }else{
            datablock=new HistogramTable(xscale,y_val,bankNum);
            datablock.setSqrtErrors(true);
        }
        data.add(datablock);
        data_xunit.add(GsasUtil.getUnit(info));
        //System.out.println("INFO("+bankNum+"):        "+info);
        //System.out.println("XSCALE :"+xscale);
        //System.out.println("DATA   :"+datablock);
        return true;
    }

    /**
     * Method to initialize the two datasets (monitor and sample).
     */
    private boolean initDataSets(Vector data, Vector xunits){
        DataSetFactory ds_factory=null;
        // quick checks to die on
        if( (data.size()<=0) || (xunits.size()<=0) ){
            SharedData.addmsg("Could not load data");
            return false;
        }
        if( data.size()!=xunits.size() ){
            SharedData.addmsg("data and units not of same length");
            return false;
        }

        // determine how many datasets to make
        boolean has_monitor=false;
        Vector reduced_units=new Vector();
        for( int i=0 ; i<data.size() ; i++){
            if(((Data)data.elementAt(i)).getGroup_ID()==0){
                has_monitor=true;
            }else{
                if(!reduced_units.contains(xunits.elementAt(i))){
                    reduced_units.add(xunits.elementAt(i));
                }
            }
        }
        this.numDataSet=reduced_units.size();
        if(has_monitor) this.numDataSet++;
        dataset=new DataSet[this.numDataSet];

        // set up the default dataset factory
        int db_num=-1;
        int ds_count=0;
        String unit=(String)xunits.elementAt(0);
        ds_factory=new DataSetFactory(null,unit,getLabel(unit),
                                      "Counts","Scattering Intensity");

        // set up some of the shared attributes
        StringAttribute mon_dsA = new StringAttribute(Attribute.DS_TYPE, 
                                                      Attribute.MONITOR_DATA);
        StringAttribute sam_dsA = new StringAttribute(Attribute.DS_TYPE, 
                                                      Attribute.SAMPLE_DATA);
        StringAttribute title_A = new StringAttribute(Attribute.RUN_TITLE,
                                                      this.runtitle);
        StringAttribute file_A  = new StringAttribute(Attribute.FILE_NAME,
                                                      this.data_source_name);
        StringAttribute iparm_A = new StringAttribute(Attribute.GSAS_IPARM,
                                                      this.iparmfile);
        IntAttribute    insttype_A = new IntAttribute(Attribute.INST_TYPE,
                                             InstrumentType.TOF_DIFFRACTOMETER);

        // make the monitor dataset if we can
        if(has_monitor){
            for( int i=0 ; i<data.size() ; i++ ){
                if(((Data)data.elementAt(i)).getGroup_ID()==0){
                    db_num=i;
                    break;
                }
            }
            unit=(String)xunits.elementAt(db_num);
            ds_factory.setTitle("Monitor");
            ds_factory.setX_units(unit);
            ds_factory.setX_label(getLabel(unit));
            dataset[ds_count]=
                ds_factory.getTofDataSet(InstrumentType.TOF_DIFFRACTOMETER);
            dataset[ds_count].addData_entry((Data)data.elementAt(db_num));
            dataset[ds_count].setAttribute(mon_dsA);
            data.remove(db_num);
            xunits.remove(db_num);
            ds_count++;
        }

        // make the other datasets
        while(true){
            unit=(String)xunits.elementAt(0);
            ds_factory.setTitle("Sample");
            ds_factory.setX_units(unit);
            ds_factory.setX_label(getLabel(unit));
            dataset[ds_count]=
                ds_factory.getTofDataSet(InstrumentType.TOF_DIFFRACTOMETER);
            if(this.iparmfile!=null) dataset[ds_count].setAttribute(iparm_A);
            dataset[ds_count].setAttribute(sam_dsA);
            for( int i=0 ; i<data.size() ; i++ ){
                if(unit.equals(xunits.elementAt(i))){
                    dataset[ds_count].addData_entry((Data)data.elementAt(i));
                    data.remove(i);
                    xunits.remove(i);
                    i--;
                }
            }
            ds_count++;
            if(data.size()<=0) break;
        }

        // add some attributes to all of the datasets and their data
        DataSet ds = null;
        Data    d  = null;
        for( int i=0 ; i<dataset.length ; i++ ){
            ds=dataset[i];
            ds.setAttribute(title_A);
            ds.setAttribute(file_A);
            ds.setAttribute(insttype_A);
            for( int j=0 ; j<ds.getNum_entries() ; j++ ){
                d=ds.getData_entry(j);
                d.setAttribute(title_A);
                d.setAttribute(file_A);
                d.setAttribute(insttype_A);
            }
        }

        // look for the instrument parameter file and add its attributes
        if(this.iparmfile!=null){
            String parmfile=StringUtil.setFileSeparator(this.data_source_name);
            int index       = parmfile.lastIndexOf("/");
            if(index>=0){
                parmfile=parmfile.substring(0,index+1);
            }else{
                parmfile="";
            }
            parmfile=parmfile+this.iparmfile;
            //System.out.println("PARM:"+parmfile);
            DataSetOperator op=null;
            int start=0;
            int end=this.numDataSet;
            if(has_monitor) start++;
            for( int i=start ; i<end ; i++ ){
                op=dataset[i].getOperator("Load Gsas Calibration");
                op.setParameter(new Parameter("Instrument Parameter File",
                                              new LoadFileString(parmfile)),0);
                op.setParameter(new Parameter("Sequential Bank Numbering",
                                              new Boolean(false)),1);
                Object res=op.getResult();
                if( res==null || res instanceof ErrorString ){
                    dataset[i].removeAttribute(Attribute.GSAS_IPARM);
                }
            }
        }
        // return sucess
        return true;
    }

    /**
     * Centralize what the label is for a given unit
     */
    private static String getLabel(String units){
        if(units.equals("Counts"))
            return "Scattering Intensity";
        else if(units.equals("Time(us)"))
            return "Time-of-flight";
        else if(units.equals("Inverse Angstroms"))
            return "Q";
        else if(units.equals("Angstroms"))
            return "d-spacing";
        else
            return null;
    }

    /**
     * Main method for testing purposes only.
     */
    public static void main(String args[]){
        String filename=null;
        if(args.length>0){
            filename=args[0];
        }else{
            filename="/IPNShome/pfpeterson/data/CsC60/sepd18805.gsa";
        }
        System.out.println("FILE: "+filename);
        GsasRetriever gr=new GsasRetriever(filename);
        System.out.println("NUMDS:"+gr.numDataSets());
        DataSet ds=null;
        int num=0;
        for(int i=0 ; i<gr.numDataSets() ; i++ ){
            ds=gr.getDataSet(i);
            num=ds.getNum_entries();
            System.out.println("DS["+i+"]="+ds);
            for( int j=0 ; j<num ; j++ ){
                System.out.println("     "+ds.getData_entry(j));
            }
        }
        System.exit(0);
    }
}

