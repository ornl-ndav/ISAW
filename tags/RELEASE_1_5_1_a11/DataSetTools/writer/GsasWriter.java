/*
 * File:  GsasWriter.java
 *
 * Copyright (C) 2001, Peter Peterson
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
 * Revision 1.4  2002/11/27 23:26:45  pfpeterson
 * standardized header
 *
 *
 */

package DataSetTools.writer;

import  DataSetTools.dataset.*;
import  java.io.*;
import  DataSetTools.gsastools.*;

/**
 * Class for writing DataSet objects to files in GSAS format. The
 * engine for this class is DataSetTools.gsastools.gsas_filemaker.
 */

public class GsasWriter extends Writer
{
    boolean export_monitor;
    boolean seq_numbers;
    /**
     * Construct the Writer for the specified destination name.
     *
     * @param data_destination_name  This identifies the data destination.  
     *                               For file data writers, this should be 
     *                               the fully qualified file name.
     */

    public GsasWriter( String data_destination_name ){
        this(data_destination_name,true,false);
    }

    /**
     * Construct the Writer for the specified destination name.
     *
     * @param data_destination_name  This identifies the data destination.  
     *                               For file data writers, this should be 
     *                               the fully qualified file name.
     * @param em                     Whether to export the monitor.
     * @param sn                     Whether to sequentially number the banks
     */
    public GsasWriter( String data_destination_name, boolean em, boolean sn ){
        super(data_destination_name);
        export_monitor=em;
        seq_numbers=sn;
    }

    /**
     * Send the specified array of data sets to the current data
     * destination.  If an array of DataSets includes both monitor and
     * histogram DataSets the recommended convention is to list the
     * monitor DataSet in the array before the list of histogram
     * DataSets to which it applies.  That is M1, H1, H2, H3, M2, H3,
     * H4 would be interpreted to mean that M1 is the monitor DataSet
     * for histograms H1, H2, H3 and the M2 is the monitor DataSet for
     * histograms H3 and H4. If there is more than one Histogram being
     * saved that they are saved in different files with an index in
     * the filename to denote which histogram is saved.
     */
    public void writeDataSets( DataSet ds[] ){
        //System.out.println("(GW)NUMBERING: "+seq_numbers);
	String dsType="";
	gsas_filemaker gf;

	if( ds==null ){
	    return;
	}

	if(ds.length<=0){
	    return;
	}else if(ds.length==1){
	    gf=new gsas_filemaker(ds[0],data_destination_name);
	}else if(ds.length==2){
	    gf=new gsas_filemaker(ds[0],ds[1],data_destination_name,
                                  export_monitor,seq_numbers);
	    gf.write();
	    gf.close();
	}else{
	    DataSet mon=null;
	    for( int i=0 ; i<ds.length ; i++ ){
		if(isMonitor(ds[i])){
		    mon=ds[i];
		}else{
		    String outfile=
			outString(data_destination_name,ds[i].toString(),i);
		    if(mon!=null){
			gf=new gsas_filemaker(mon,ds[i],outfile,
                                              export_monitor,seq_numbers);
		    }else{
			gf=new gsas_filemaker(ds[i],outfile);
		    }
		    gf.write();
		    gf.close();
		}

	    }

	}
    }

    /**
     * Determine if the DataSet is a monitor.
     */
    private static boolean isMonitor(DataSet ds){
	String dsType=(String)
	    ds.getAttributeList().getAttributeValue(Attribute.DS_TYPE);
	dsType=dsType.toLowerCase();

	return ( dsType.indexOf("monitor")>=0 );
    }

    /**
     * Create the outputfilename when there is more than one DataSet
     * being saved.
     */
    private static String outString(String origfile, String datastring, int i){
	String outfile
	    =origfile.substring(0,origfile.lastIndexOf("."));
	int index1=datastring.indexOf(":");
	int index2=datastring.indexOf("_");
	if(index2>0){
	    if(index1<0){ index1=0; }
	    outfile=outfile+"_"+datastring.substring(index1+1,index2);
	}else{
	    outfile=outfile+"_"+i;
	}
	outfile=outfile+origfile.substring(origfile.lastIndexOf("."));

	return outfile;
    }

    /**
     * This method is used for testing the writer.
     */
    public static void main( String args[] ){
	String prefix="/IPNShome/pfpeterson/";
	String infile=prefix+"data/II_VI/SEPD/dec2001/runfiles/sepd18124.run";
	String outfile=prefix+"ISAW/DataSetTools/gsastools/lookatme.gsa";
	DataSetTools.retriever.RunfileRetriever rr
	    =new DataSetTools.retriever.RunfileRetriever(infile);
	DataSet mds=rr.getDataSet(0);
	DataSet rds=rr.getDataSet(1);

	GsasWriter gw=new GsasWriter(outfile);
	gw.writeDataSets(new DataSet[] {mds,rds});
    }

}
