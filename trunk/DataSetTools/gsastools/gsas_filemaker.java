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
 * Contact : Alok Chatterjee <AChatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
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
 *  Revision 1.10  2001/11/21 19:59:10  pfpeterson
 *  Fixed bug of array indexing. Added feature where if 0deg monitor has zero counts 180deg monitor is printed in file.
 *
 *  Revision 1.9  2001/11/20 21:37:06  pfpeterson
 *  Modified GSAS data file format to reflect new found information.
 *
 *  Revision 1.8  2001/11/09 19:40:06  dennis
 *  Now allows the monitor DataSet to be null.
 *
 *  Revision 1.7  2001/11/08 22:28:49  chatterjee
 *  Added lines required to be read as a PDF file. GSAS will ignore the 
 *  extra lines.
 *
 *  Revision 1.6  2001/09/21 19:11:35  dennis
 *  Improved label on file name that's printed to the console.
 *
 *  Revision 1.5  2001/09/21 18:39:36  dennis
 *  Removed some debugging println() statements.
 *
 *  Revision 1.4  2001/06/25 20:11:44  chatter
 *  Added the header info in the GSAS output file
 *
 *  Revision 1.3  2001/06/08 23:24:25  chatter
 *  Fixed GSAS write file for SEPD
 *
 *  Revision 1.2  2001/04/25 19:26:07  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 */
package DataSetTools.gsastools;

import DataSetTools.dataset.*;
import DataSetTools.util.*; 
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.text.DateFormat;
import java.text.*;
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
    private OutputStreamWriter outStream;
    private DataSet mon;
    private DataSet data;
    private String bintype;
    private float bCoef1;
    private float bCoef2;
    private float bCoef3;
    private float bCoef4;
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
	this.printRunTitle();
        this.printIParmFile();
	this.printMonitorCount();
	this.printBankInfo();
        //System.out.println("(GF)NUMBERING: "+seq_numbers);
        if(export_monitor) this.printMonitorSpectrum();

	// write out the data
        int count=1;
	for(int i=1; i<=data.getMaxGroupID() ; i++){
            Data dd=data.getData_entry_with_id(i);
            if(dd!=null){
                printBank(count,dd);
                count++;
            }else if(!seq_numbers){
                count++;
            }
	}
	try{
	    Thread.sleep(100);
	} catch(Exception d){}
    }
    
    /**
     * This method opens the banknum group and calls the other form of
     * printbank.
     *
     * @deprecated This is a redundant function incorporated in the
     *             write() method
     */
    private void printBank(int banknum){
	Data dd = data.getData_entry_with_id(banknum);
	printBank(banknum,dd);
	return;
    }

    /**
     * This method determines the BINTYPE and TYPE of the group
     * specified by ds then prints the entire bank information. The
     * value of banknum is strictly used for printing purposes and is
     * not otherwise needed.
     */
    private void printBank( int banknum, Data ds ){
	if(ds==null) return;

	getBinType(ds,data.getX_units());
	getType(ds);

	if((bintype=="CONST")||(bintype=="COND")||(bintype=="CONQ")){
	    printCONSThead(banknum,ds.getCopyOfY_values().length);
	}
	if(type=="STD"){
	    printSTDdata(ds.getCopyOfY_values());
	}else{
	    printESDdata(ds.getCopyOfY_values(),ds.getCopyOfErrors());
	}
	return;
    }

    /**
     * Determine the TYPE. This is done by calculating the percent
     * difference between sqrt(I) and sigmaI. If it is more than
     * 0.0001 different then the bank is labeled ESD.
     */
    private void getType(Data data){
	float[] dI = data.getCopyOfErrors();
	float[] I  = data.getCopyOfY_values();
	boolean Etype=true;
	for( int i=0 ; i<dI.length ; i++ ){
	    if((0.9999f*(float)Math.sqrt((double)I[i])<dI[i])||(dI[i]==0.0f)){
		// do nothing
	    }else{
		System.out.println(Math.sqrt((double)I[i])+" not same as "+dI[i]);
		Etype=false;
		i=dI.length;
	    }
	}
	if(Etype){
	    type="STD";
	}else{
	    type="ESD";
	}
	return;
    }

    /**
     * This determines the BINTYPE of the data by first looking at the
     * units (only understands 'time' at the moment) then determining
     * if the x-values need to be scaled into microseconds. Then the
     * bining is determined to be constant or constant dT/T. Once the
     * BINTYPE is determined the parameters needed for the bank header
     * are also set.
     *
     * These method needs reasonable keys in the units tag to look for
     * to determine if data is in d or Q. Once this is done the
     * BINTYPE can be found to support these other values.
     */
    private void getBinType( Data data, String units ){
	int numX=data.getX_scale().getNum_x();
	float[] xval=data.getX_scale().getXs();
	float scale=0.0f;
	units=units.toLowerCase();

	// convert units and get scale
	if(units.indexOf("time")>=0){
	    if(units.indexOf("us")>0){
		scale=1.0f;
	    }else if(units.indexOf("ms")>0){
		scale=0.001f;
	    }
	    units="time";
	}else{
	    // unsuported right now
	}

	float minX=10000.0f;
	float maxX=-1.0f;

	for( int i=0 ; i<numX ; i++ ){
	    xval[i]=scale*xval[i];
	    if(xval[i]>maxX){
		maxX=xval[i];
	    }
	    if(xval[i]<minX){
		minX=xval[i];
	    }
	}
	//System.out.println("min="+minX+" max="+maxX);

	float dX=xval[1]-xval[0];
	for( int i=1 ; i<numX ; i++ ){
	    if((xval[i]-xval[i-1])==dX){
		// do nothing
	    }else{
		System.out.println("not constant steps "+(xval[i]-xval[i-1])+" not "+dX);
		dX=0.0f;
		i=numX;
	    }
	}

	// check that there was constant spacing
	if(dX>0.0f){
	    if(units=="time"){ // constant time binning found
		bintype="CONST";
		bCoef1=minX;
		bCoef2=dX;
		bCoef3=0.0f;
		bCoef4=0.0f;
		return;
	    }else if(units=="d"){ // constant d-spacing
		bintype="COND";
		bCoef1=minX;
		bCoef2=dX;
		bCoef3=0.0f;
		bCoef4=0.0f;
		return;
	    }else if(units=="q"){ // constant Q-spacing
		bintype="CONQ";
		bCoef1=minX;
		bCoef2=dX;
		bCoef3=0.0f;
		bCoef4=0.0f;
		return;
	    }
	}

	// untested but it should work
	dX=(xval[1]-xval[0])/xval[0];
	for( int i=1 ; i<numX ; i++ ){
	    if(((xval[i]-xval[i-1])/xval[i-1])==dX){
		// do nothing
	    }else{
		System.out.println("not constant steps "+((xval[i]-xval[i-1])/xval[i-1])+" not "+dX);
		dX=0.0f;
		i=numX;
	    }
	}
	
	// check that there was constant spacing
	if(dX>0.0f){
	    if(units=="time"){ // constant Dt/t found
		bintype="SLOG";
		bCoef1=minX;
		bCoef2=maxX;
		bCoef3=dX;
		bCoef4=0.0f;
		return;
	    }
	}else{ // must be a time-map
	    if(units=="time"){
		bintype="TIME_MAP";
		bCoef1=0.0f;
		bCoef2=0.0f;
		bCoef3=0.0f;
		bCoef4=0.0f;
		return;
	    }
	}

	return;
    }

    /**
     * This method is for printing the bank header for the different
     * constant BINTYPES. This method is shared because all three have
     * only two parameters.
     */
    private void printCONSThead( int banknum, int nchan){
	int nrec=0;
        StringBuffer sb=new StringBuffer(80);
	if(type=="STD"){
	    nrec=(int)((float)nchan/10.0+0.9);
	}else if(type=="ESD"){
	    nrec=(int)((float)nchan/5.0+0.9);
	}else{
	    // the data does not have error of a supported type
	    return;
	}
        sb.append("BANK   ").append(format(banknum,6)).append("    ")
            .append(format(nchan,6)).append("     ")
            .append(format(nrec,6)).append(formatc(bintype,8))
            .append(format(bCoef1,14)).append("     ")
            .append(format(bCoef2,10)).append("  ");
        if(type.equals("STD")){
            sb.append("       ");
        }else{
            sb.append(type).append("    ");
        }
	try{
            outStream.write(sb+"\n");
	}catch(Exception d){}
    }

    /**
     * This is a legacy method which simply retrieves banknum's
     * intensities and passes them to the full printSTDdata method.
     */
    private void printSTDdata( int banknum ){
	float [] y = data.getData_entry_with_id(banknum).getCopyOfY_values();
	printSTDdata(y);
    }

    /**
     * This method prints the table of intensities in proper GSAS STD
     * format.
     */
    private void printSTDdata( float[] y ){
        StringBuffer sb=new StringBuffer(80);
	try{
	    for(int j=0; j<y.length; j+=10){
		for(int l=j+0; l<j+10; l++){
		    if(l>=y.length){
                        sb.append("        ");
		    }else{
			sb.append("  ").append(format((int)y[l],6));
		    }
		}
		outStream.write(sb+"\n");
                sb.delete(0,sb.length());
	    }
	} catch(Exception d){}
    }

    /**
     * This is a legacy method which simply retrieves banknum's
     * intensities and errors then passes them to the full
     * printESDdata method.
     */
    private void printESDdata( int banknum ){
	float[] y  = data.getData_entry_with_id(banknum).getCopyOfY_values();
	float[] dy = data.getData_entry_with_id(banknum).getCopyOfErrors();
	printESDdata(y,dy);
    }

    /**
     * This method prints the table of intensities in proper GSAS ESD
     * format.
     */
    private void printESDdata( float[] y , float[] dy ){
        StringBuffer sb=new StringBuffer(80);
	try{
	    for(int j=0; j<y.length; j+=5){
		for(int l=j+0; l<j+5; l++){
		    if(l>=y.length){
			sb.append("        ");
		    }else{
			sb.append("  "+format((int)y[l],6)+"  "+format((int)dy[l],6));
		    }
		}
		outStream.write(sb+"\n");
                sb.delete(0,sb.length());
	    }
	} catch(Exception d){}
    }

    /**
     * Format a string so it is padded with spaces on either side.
     */
    static private String formatc(String stuff, int length){
	StringBuffer rs=new StringBuffer(length);
        rs.append(stuff);
	while(rs.length()<length){
	    rs.append(" ");
	    if(rs.length()<length){
		rs.insert(0," ");
	    }
	}
	return rs.toString();
    }

    /**
     * Format a string by padding on the right.
     */
    static private String format(String stuff, int length){
        StringBuffer sb=new StringBuffer(length);
        sb.append(stuff);
        return format(sb,length);
    }

    /**
     * Format a string buffer by padding on the right.
     */
    static private String format(StringBuffer stuff, int length){
        while(stuff.length()<length){
            stuff.append(" ");
        }
        return stuff.toString();
    }

    static private String formatl(StringBuffer stuff, int length){
        while(stuff.length()<length){
            stuff.insert(0," ");
        }
        return stuff.toString();
    }

    /**
     * Format an integer by padding on the left.
     */
    static private String format(int number, int length){
	StringBuffer rs=new StringBuffer(length);
        rs.append(number);
        return formatl(rs,length);
    }

    /**
     * Format a float by padding on the left and making it have seven
     * digits past the decimal.
     */
    static private String format(float number, int length){
    	DecimalFormat df=new DecimalFormat("#####0.0000000");
	StringBuffer rs=new StringBuffer(length);
        rs.append(df.format(number));
        return formatl(rs,length);
    }

    /**
     * Prints the short table of bank information near the top of the
     * file. Currently the bank information printed is number,
     * scattering angle (2theta), and total flight path.
     */
    private void printBankInfo(){
	try{
	// write the bank information header
	outStream.write(format("#             Ref Angle  Total length",80)+"\n");
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
	    outStream.write ("#BANK " +format(banknum,4)+"  "+format(ref_angle,12)+"  "
			     +format(total_length,5)+format(" ",44)+"\n");
	}catch(Exception e){}
    }

    /**
     * Write the run title into the file with propper padding.
     */
    private void printRunTitle( ){
 	try{
	    
	    // write the tile of the run into the file
            StringBuffer S=new StringBuffer(80);
            S.append((String)
               data.getAttributeList().getAttributeValue(Attribute.RUN_TITLE));
	    outStream.write( format(S,80) +"\n");
	    
	} catch(Exception d){}
	
    }

    /**
     * Write the instrument parameter file with propper padding.
     */
    private void printIParmFile(){
        String S=System.getProperty("IParmFile");
        //System.out.println("IParm: "+S);
        if(S!=null){
            StringBuffer sb=new StringBuffer(80);
            sb.append("Instrument parameter ").append(S);
            try{
                outStream.write( format(sb,80)+"\n");
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
                sb.append("MONITOR: ").append(monCount);
		outStream.write (format(sb,80)+"\n");
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
	    printBank(0,monData);
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
	String infile=prefix+"data/II_VI/SEPD/dec2001/runfiles/sepd18124.run";
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
    }
}
