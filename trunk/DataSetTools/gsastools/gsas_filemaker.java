/*
 * File:  gsas_filemaker.java
 *
 * Copyright (C) 1999, Dongfeng Chen, Ruth Mikkelson, Alok Chatterjee 
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
 *  Revision 1.22  2002/11/27 23:15:00  pfpeterson
 *  standardized header
 *
 *  Revision 1.21  2002/07/25 19:29:05  pfpeterson
 *  Many changes, the largest of which is time map support.
 *
 *  Revision 1.20  2002/07/17 20:16:13  pfpeterson
 *  Now uses DataSetTools.util.Format for string formatting.
 *  Determines if scale is constant by seeing if is instance
 *  of UniformXScale.
 *
 *  Revision 1.19  2002/07/10 16:02:49  pfpeterson
 *  Added to CVS.
 *
 *  Revision 1.18  2002/06/12 13:59:49  pfpeterson
 *  Modified the write so the data for each bank is in a single
 *  string buffer (only one write per bank).
 *
 *  Revision 1.17  2002/06/10 22:28:29  pfpeterson
 *  Now uses StringBuffer to speed up the writing and formating.
 *
 *  Revision 1.16  2002/05/24 14:42:40  pfpeterson
 *  Modified to use new monitor finding routines.
 *
 *  Revision 1.15  2002/05/20 20:24:13  pfpeterson
 *  Added checkbox to allow for numbering banks as the
 *  raw data does or sequentially. Also look for "IParmFile"
 *  system property and add it as the second line of the
 *  exported file if defined.
 *
 *  Revision 1.14  2002/05/17 22:19:49  pfpeterson
 *  Added checkbox for exporting monitor spectrum. The integrated
 *  monitor count is still included in the file.
 *
 *  Revision 1.13  2002/02/22 20:35:45  pfpeterson
 *  Fixed to work with GSAS (again) by removing STD from bank header.
 *
 *  Revision 1.12  2002/01/14 20:31:50  pfpeterson
 *  Modified to use writer interface for GSAS files.
 *  Now can be used to export files with constant t, d, Q and delta t/t.
 *  Errors for intensities are either sqrt(intensity) or specified.
 *
 *  Revision 1.11  2002/01/04 16:43:49  pfpeterson
 *  Modified format. Now more closely matches sumruns results.
 *
 */
package DataSetTools.gsastools;

import DataSetTools.dataset.*;
import DataSetTools.util.*; 
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.text.DateFormat;
//import java.text.*;
import DataSetTools.math.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.RunfileRetriever;
/**
 * Transfer diffractometer Data Set histogram to GSAS file
 * format. This class supports some of the full functionality of the
 * full GSAS file format. For full explanation of the format seek the
 * GSAS manual. The BINTYPs implemented are CONST, COND, CONQ, and
 * SLOG. The TYPEs implemented are STD and ESD.Only CONST has been
 * tested so far and TIME_MAP will eventually be implemented.
 */

public class gsas_filemaker
{                      
    // string constants for bintypes
    public static String COND    = GsasUtil.COND;
    public static String CONS    = GsasUtil.CONS;
    public static String CONQ    = GsasUtil.CONQ;
    public static String SLOG    = GsasUtil.SLOG;
    public static String TIMEMAP = GsasUtil.TIMEMAP;

    // string constants for types (how errors are written)
    public static String STD = GsasUtil.STD;
    public static String ESD = GsasUtil.ESD;

    // string constant for other stuff
    public static String BANK       = GsasUtil.BANK;
    public static String IPARM      = "Instrument parameter file:";
    public static String MONITOR    = "MONITOR";
    public static String REF_ANGLE  = "Ref Angle";
    public static String TOT_LENGTH = "Total length";

    private OutputStreamWriter outStream;
    private DataSet mon;
    private DataSet data;
    private String bintype;
    private String type;
    private int monNum;
    private boolean export_monitor;
    private boolean seq_numbers;
    
    /**
     * This constructor is in place just in case one is needed. It
     * actually does nothing
     */
    public gsas_filemaker(){
        this.export_monitor=true;
    }
    
    /**
     * This constructor sets the output filename and opens the output
     * file.
     */
    public gsas_filemaker(String filename){
	this();
	File f = new File(filename);
	try{
	    FileOutputStream op= new FileOutputStream(f);
	    outStream=new OutputStreamWriter(op);
            DataSetTools.util.SharedData.status_pane.add("The GSAS file "
                                                         +"name is "+filename);
	} catch(Exception e){}
    }
    
    /**
     * This constructor adds a dataset and calls the previous
     * constructor.
     */
    public gsas_filemaker( DataSet ds, String filename ){
	this(filename);
	this.setData(ds);
    }

    /**
     * This constructor adds a monitor and calls the previous
     * constructor.
     */
    public gsas_filemaker( DataSet mon_ds, DataSet ds, String filename ){
	this(ds,filename);
	this.setMon(mon_ds);
    }

    /**
     * This constructor allows for the exporting of the monitor and
     * method of bank numbering to be specified.
     */
    public gsas_filemaker( DataSet mon_ds, DataSet ds, String filename,
                           boolean em, boolean sn){
        this(mon_ds,ds,filename);
        this.export_monitor=em;
        this.seq_numbers=sn;
    }

    /** 
     * The method called to write out the file. 
     */
    public void write(){
        //long offset=System.currentTimeMillis();
	this.printRunTitle();
        this.printIParmFile();
	this.printMonitorCount();
	this.printBankInfo();

        XInfo[] tempinfolist=new XInfo[this.data.getNum_entries()];
        XInfo tempinfo=null;
        int[] infonum=new int[this.data.getNum_entries()];
        int infocount=-1;
        int timemap_count=0;
        for( int i=0 ; i<this.data.getNum_entries() ; i++ ){
            Data d=this.data.getData_entry(i);
            tempinfo=new XInfo(d.getX_scale(),this.data.getX_units(),
                               GsasUtil.getType(d));
            if(tempinfo.setTimeMapNum(timemap_count)){
                timemap_count++;
            }
            if(isUniqueInfo(tempinfo,tempinfolist)){
                infocount++;
                tempinfo.setTimeMapNum(timemap_count);
                tempinfolist[infocount]=tempinfo;
            }else{
                if(tempinfo.setTimeMapNum(timemap_count)){
                    timemap_count--;
                }
            }
            infonum[i]=infocount;
        }
        XInfo[] info=new XInfo[infocount+1];
        for( int i=0 ; i<infocount+1 ; i++ ){
            info[i]=tempinfolist[i];
        }
        printTimeMap(info);

        //System.out.println("(GF)NUMBERING: "+seq_numbers);
        if(export_monitor) this.printMonitorSpectrum();

	// write out the data
        int count=1;
        infocount=0;
	for(int i=1; i<=data.getMaxGroupID() ; i++){
            Data dd=data.getData_entry_with_id(i);
            if(dd!=null){
                printBank(count,dd,info[infonum[infocount]]);
                count++;
                infocount++;
            }else if(!seq_numbers){
                count++;
            }
	}
        //TimeMap tm=new TimeMap(data.getData_entry(0).getX_scale(),23);
        //System.out.print(tm);

        //System.out.println("time="+(System.currentTimeMillis()-offset));
    }
    
    private boolean isUniqueInfo( XInfo info, XInfo[] infolist){
        for( int i=0 ; i<infolist.length ; i++ ){
            if(infolist[i]!=null){
                if(info.equals(infolist[i]))return false;
            }
        }
        return true;
    }

    private void printTimeMap( XInfo[] info ){
        StringBuffer sb=new StringBuffer(info.length*80);
        TimeMap timemap=null;
            
        for( int i=0 ; i<info.length ; i++ ){
            timemap=info[i].timemap();
            if(timemap!=null){
                sb.append(timemap.toString());
            }
        }
        try{
            outStream.write(sb.toString());
        }catch(IOException e){
            SharedData.addmsg("Could not write "+TIMEMAP+" information: "+e.getMessage());
        }

    }

    /**
     * This method determines the BINTYPE and TYPE of the group
     * specified by ds then prints the entire bank information. The
     * value of banknum is strictly used for printing purposes and is
     * not otherwise needed.
     */
    private void printBank( int banknum, Data ds, XInfo info ){
	if(ds==null) return;
        if(info==null){
            info=new XInfo(ds.getX_scale(),data.getX_units(),
                           //ESD); // this line forces writing errors
                           GsasUtil.getType(ds));
        }

        bintype = info.bintype();
        type    = info.type();

        printBankHead(banknum,info);
	if(type.equals(STD)){
	    printSTDdata(ds.getCopyOfY_values());
	}else{
	    printESDdata(ds.getCopyOfY_values(),ds.getCopyOfErrors());
	}
	return;
    }

    /**
     * This method is for printing the bank header for the different
     * constant BINTYPES. This method is shared because all three have
     * only two parameters.
     */
    private void printBankHead( int banknum, XInfo info){
        StringBuffer sb=new StringBuffer(80);
        try{
            sb.append(GsasUtil.getBankHead(banknum,info));
            outStream.write(Format.string(sb,80,false)+"\n");
        }catch(IOException e){
            SharedData.addmsg("Could not write "+bintype+" bank header: "
                              +e.getMessage());
        }
    }

    /**
     * This method prints the table of intensities in proper GSAS STD
     * format.
     */
    private void printSTDdata( float[] y ){
        StringBuffer sb=new StringBuffer((int)(81*y.length/10));
        int colcount=0;
        for( int i=0 ; i<y.length ; i++ ){
            if(colcount>=80){
                sb.append("\n");
                colcount=0;
            }
            sb.append("  ").append(Format.integer(y[i],6));
            colcount+=8;
        }
        sb.append(Format.string("\n",81-colcount));
	try{
            outStream.write(sb.toString());
	}catch(IOException e){
            SharedData.addmsg("Could not write "+STD+" data: "+e.getMessage());
        }
    }

    /**
     * This method prints the table of intensities in proper GSAS ESD
     * format.
     */
    private void printESDdata( float[] y , float[] dy ){
        StringBuffer sb=new StringBuffer((int)(81*y.length/5));
        int colcount=0;
        for( int i=0 ; i<y.length ; i++ ){
            if(colcount>=80){
                sb.append("\n");
                colcount=0;
            }
            sb.append(Format.real(y[i],8)+Format.real(dy[i],8));
            //sb.append(" "+Format.real(y[i],7)+" "+Format.real(dy[i],7));
            colcount+=16;
        }
	try{
            outStream.write(sb.toString());
	} catch(Exception d){}
    }


    /**
     * Prints the short table of bank information near the top of the
     * file. Currently the bank information printed is number,
     * scattering angle (2theta), and total flight path.
     */
    private void printBankInfo(){
	try{
	// write the bank information header
	outStream.write(Format.string("#             "+REF_ANGLE+"  "
                                      +TOT_LENGTH,80,false)+"\n");
	Data dd=null;
	if(mon!=null && export_monitor){
	    dd = mon.getData_entry(monNum);
	    if(dd!=null){
		AttributeList attr_list = dd.getAttributeList();
		printBankInfoLine(0,attr_list);
	    }
	}

	// write the bank information
        int count=1;
	for(int i=1; i<=data.getMaxGroupID(); i++){
	    dd = data.getData_entry_with_id(i);
	    if(dd!=null){
		AttributeList attr_list = dd.getAttributeList();
                printBankInfoLine(count,attr_list);
                count++;
	    }else if(!seq_numbers){
                count++;
            }
	}
	}catch(Exception e){}
    }
	    
    /**
     * Writes the proper format for each line of printBankInfo
     */
    private void printBankInfoLine( int banknum, AttributeList al ){
	DetectorPosition position=(DetectorPosition)
	    al.getAttributeValue( Attribute.DETECTOR_POS);
	
	float initial_path =((Float)
			     al.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
	float spherical_coords[] = position.getSphericalCoords();
	float total_length       = initial_path + spherical_coords[0];
	float cylindrical_coords[] = position.getCylindricalCoords();
	float ref_angle = (float)(cylindrical_coords[1]*180.0/(java.lang.Math.PI));
	
	try{
	    outStream.write ("#"+BANK+" " +Format.integer(banknum,4)+"  "
                             +Format.real(ref_angle,12,7)+"  "
			     +Format.real(total_length,5,7)
                             +Format.string(" ",44)+"\n");
	}catch(Exception e){}
    }

    /**
     * Write the run title into the file with propper padding.
     */
    private void printRunTitle( ){
 	try{
	    
	    // write the tile of the run into the file
            StringBuffer sb=new StringBuffer(81);
            sb.append((String)
               data.getAttributeList().getAttributeValue(Attribute.RUN_TITLE));
	    outStream.write( Format.string(sb,80,false) +"\n");
	    
	} catch(Exception d){}
	
    }

    /**
     * Write the instrument parameter file with propper padding.
     */
    private void printIParmFile(){
        String S=(String)data.getAttributeValue(Attribute.GSAS_IPARM);
        if(S==null) S=System.getProperty("IParmFile");
        //System.out.println("IParm: "+S);
        if(S!=null){
            StringBuffer sb=new StringBuffer(80);
            sb.append(IPARM).append(S);
            try{
                outStream.write( Format.string(sb,80,false)+"\n");
            }catch(Exception e){}
        }
    }

    /** 
     * Write the total monitor count on the second line of the file
     */
    private void printMonitorCount( ){
        StringBuffer sb=new StringBuffer(80);
	try{
	    float monCount=this.getMonitorCount();
	    if(monCount>0.0f){
                sb.append(MONITOR+": ").append(monCount);
		outStream.write (Format.string(sb,80,false)+"\n");
	    }
	} catch(Exception d){}
    }

    /**
     * Write the monitor spectrum as bank zero.
     */
    private void printMonitorSpectrum( ){
	if(mon==null){ 
	    return; 
	}else{
	    Data monData = mon.getData_entry(monNum);
	    printBank(0,monData,null);
	}

	return;
    }

    /**
     * Determine the total monitor count.
     */
    private float getMonitorCount( ){
	Data monD = mon.getData_entry_with_id(monNum);
	Float count = (Float)
	    monD.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
	return count.floatValue();
    }

    /**
     * Accessor method for setting the data.
     */
    public void setData(DataSet myData){
	this.data=myData;
    }

    /**
     * Accessor method for setting the monitor.
     */
    public void setMon(DataSet myData){
	if(myData==null){
	    return;
	}
	this.mon=myData;
	
	int monDataNum=0;
	float monCount=-1.0f;

	// confirm that there is something in the upstream monitor
        GenericSpecial op = new UpstreamMonitorID(mon);
        monDataNum=((Integer)op.getResult()).intValue();
        if(monDataNum>=0){
          Data monD = mon.getData_entry_with_id(monDataNum);
          monCount=((Float)
                   monD.getAttributeValue(Attribute.TOTAL_COUNT)).floatValue();
        }
            
	// if there isn't then use the downstream monitor
        if(monCount<=0f){
            op = new DownstreamMonitorID(mon);
            monDataNum=((Integer)op.getResult()).intValue();
            if(monDataNum>=0){
                Data monD = mon.getData_entry_with_id(monDataNum);
                monCount=((Float)
                   monD.getAttributeValue(Attribute.TOTAL_COUNT)).floatValue();
            }
        }
            
	/*for( int i=0 ; i<mon.getNum_entries() ; i++ ){
          Data monD = mon.getData_entry(i);
          Float ang = (Float)
          monD.getAttributeList().getAttributeValue(Attribute.RAW_ANGLE);
          if( ang.floatValue() == 0.0f ){
          Float count = (Float)
          monD.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
          if(count.floatValue()>monCount){
          monCount=count.floatValue();
          monDataNum=i;
          }
          }
          } */
	
	// if there isn't then use the downstream monitor
	/*if( monCount <= 0.0f ){
          for( int i=0 ; i<mon.getNum_entries() ; i++ ){
          Data monD = mon.getData_entry(i);
          Float ang = (Float)
          monD.getAttributeList().getAttributeValue(Attribute.RAW_ANGLE);
          if( (ang.floatValue() == 180f) || (ang.floatValue()== -180f) ){
          Float count = (Float)
          monD.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
          if(count.floatValue()>monCount){
          monCount=count.floatValue();
          monDataNum=i;
          }
          }
          }
          }*/
	
	this.monNum=monDataNum;

    }

    /**
     * Close the output file
     */
    public void close(){
	try{
	    this.outStream.flush();
	    this.outStream.close();
	} catch(Exception e){}
    }

    /**
     * Main method used for testing.
     */
    public static void main(String[] args){
	String prefix="/IPNShome/pfpeterson/";
	String infile=prefix+"data/II_VI/SEPD/dec2001/sepd18124.run";
	String outfile=prefix+"ISAW/DataSetTools/gsastools/lookatme.gsa";
	//System.out.println(infile);
	//System.out.println(outfile);
	RunfileRetriever rr=new RunfileRetriever(infile);
	DataSet mds = rr.getDataSet(0);
	DataSet rds = rr.getDataSet(1);
	
	//gsas_filemaker gf = new gsas_filemaker(mds,rds,outfile);
	gsas_filemaker gf = new gsas_filemaker(rds,outfile);
	gf.write();
	gf.close();
        System.exit(0);
    }
}
