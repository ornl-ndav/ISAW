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
    
    /**
     * This constructor is in place just in case one is needed. It
     * actually does nothing
     */
    public gsas_filemaker(){}
    
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
	    System.out.println("The GSAS file name is " +filename);
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
     * The method called to write out the file. 
     */
    public void write(){
	this.printRunTitle();
	this.printMonitorCount();
	this.printBankInfo();
	this.printMonitorSpectrum();

	// write out the data
	for(int i=1; i<=data.getMaxGroupID() ; i++){
	    this.printBank(i);
	}
	try{
	    Thread.sleep(100);
	} catch(Exception d){}
    }
    
    /**
     * This method opens the banknum group and calls the other form of
     * printbank.
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
	if(ds==null){
	    return;
	}else{
	    // should choke
	}

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
	if(type=="STD"){
	    nrec=(int)((float)nchan/10.0+0.9);
	}else if(type=="ESD"){
	    nrec=(int)((float)nchan/5.0+0.9);
	}else{
	    // the data does not have error of a supported type
	    return;
	}
	try{
	    outStream.write("BANK   "+format(banknum,6)+"    "
			    +format(nchan,6)+"     "
			    +format(nrec,6)+formatc(bintype,8)
			    +format(bCoef1,14)+"     "
			    +format(bCoef2,10)+"  ");
            if(type.equals("STD")){
                outStream.write("       \n");
            }else{
                outStream.write(type+"    \n");
            }
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
	try{
	    for(int j=0; j<y.length; j+=10){
		for(int l=j+0; l<j+10; l++){
		    if(l>=y.length){
			outStream.write("        ");
		    }else{
			outStream.write("  "+format((int)y[l],6));
		    }
		}
		outStream.write("\n");
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
	try{
	    for(int j=0; j<y.length; j+=5){
		for(int l=j+0; l<j+5; l++){
		    if(l>=y.length){
			outStream.write("        ");
		    }else{
			outStream.write("  "+format((int)y[l],6)+"  "+format((int)dy[l],6));
		    }
		}
		outStream.write("\n");
	    }
	} catch(Exception d){}
    }

    /**
     * Format a string so it is padded with spaces on either side.
     */
    static private String formatc(String stuff, int length){
	String rs=stuff;
	while(rs.length()<length){
	    rs=rs+" ";
	    if(rs.length()<length){
		rs=" "+rs;
	    }
	}
	return rs;
    }

    /**
     * Format a string by padding on the right.
     */
    static private String format(String stuff, int length){
	String rs=stuff;
	while(rs.length()<length){
	    rs=rs+" ";
	}
	return rs;
    }

    /**
     * Format an integer by padding on the left.
     */
    static private String format(int number, int length){
	String rs=new Integer(number).toString();
	while(rs.length()<length){
	    rs=" "+rs;
	}
	return rs;
    }

    /**
     * Format a float by padding on the left and making it have seven
     * digits past the decimal.
     */
    static private String format(float number, int length){
	DecimalFormat df=new DecimalFormat("#####0.0000000");
	String rs=new String(df.format(number));

	while(rs.length()<length){
	    rs=" "+rs;
	}
	
	return rs;
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
	if(mon!=null){
	    dd = mon.getData_entry(monNum);
	    if(dd!=null){
		AttributeList attr_list = dd.getAttributeList();
		printBankInfoLine(0,attr_list);
	    }
	}

	// write the bank information
	for(int i=1; i<=data.getMaxGroupID(); i++){
	    dd = data.getData_entry_with_id(i);
	    if(dd!=null){
		AttributeList attr_list = dd.getAttributeList();
		printBankInfoLine(i,attr_list);
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
	    String S = (String)
		data.getAttributeList().getAttributeValue(Attribute.RUN_TITLE);
	    outStream.write( format(S,80) +"\n");
	    
	} catch(Exception d){}
	
    }

    /** 
     * Write the total monitor count on the second line of the file
     */
    private void printMonitorCount( ){
	try{
	    float monCount=this.getMonitorCount();
	    if(monCount>0.0f){
		outStream.write ("MONITOR: "+monCount+"\n");
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
	Data monD = mon.getData_entry(monNum);
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
	for( int i=0 ; i<mon.getNum_entries() ; i++ ){
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
	}
	
	// if there isn't then use the downstream monitor
	if( monCount <= 0.0f ){
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
	}
	
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
