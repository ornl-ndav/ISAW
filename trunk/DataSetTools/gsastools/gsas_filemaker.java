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
/**
 * Transfer diffractometer Data Set histogram to GSAS file format.
 * @version 1.0
 */

public class gsas_filemaker
{                      
    public gsas_filemaker(){};
   
    public gsas_filemaker( DataSet mon_ds, DataSet ds, String filename )
    {
    File f= new File(filename);
    try{
        FileOutputStream op= new FileOutputStream(f);
        OutputStreamWriter opw = new OutputStreamWriter(op);
	System.out.println("The GSAS file name is " +filename);

	// write the tile of the run into the file
	String S = (String)ds.getAttributeList().getAttributeValue(Attribute.RUN_TITLE);
	for (int j = S.length(); j<80; j++)
	    S = S +" ";
	opw.write( S +"\n");
	
	// write the total counts in the upstream (2tth=0) monitor(s)
        if ( mon_ds != null )                // allow this to still work without
	    {                                // a monitor data set specified.
		//Data mon_1 = mon_ds.getData_entry(0);
		//Float result =(Float)
		//    (mon_1.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT));

		float result = 0.0f;

		for( int i=0 ; i<3 ; i++ ){
		    Data mon = mon_ds.getData_entry(i);
		    //DetectorPosition pos = (DetectorPosition)
		    //mon.getAttributeList().getAttributeValue(Attribute.DETECTOR_POS);
		    Float ang = (Float)
			mon.getAttributeList().getAttributeValue(Attribute.RAW_ANGLE);
		    if( ang.floatValue() == 0.0f ){
			Float count = (Float)
			    mon.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
			result=result+count.floatValue();
		    }
		}

		opw.write ("MONITOR: " +result);
		opw.write("\n");
	    }

	// write the bank information header
        opw.write("BANKS"  + "     Ref Angle" + "     Total length");
        opw.write("\n"); 
	int ein= ds.getNum_entries();
	// write the bank information
	for(int i=1; i<=ein; i++)
	    {
		DataSetTools.dataset.Data dd = ds.getData_entry(i-1);
		AttributeList attr_list = dd.getAttributeList();
		DetectorPosition position=(DetectorPosition)
		    attr_list.getAttributeValue( Attribute.DETECTOR_POS);
		
		Float initial_path_obj=(Float)
		    attr_list.getAttributeValue(Attribute.INITIAL_PATH);
		
		float initial_path       = initial_path_obj.floatValue();
		float spherical_coords[] = position.getSphericalCoords();
		float total_length       = initial_path + spherical_coords[0];
		float cylindrical_coords[] = position.getCylindricalCoords();
		float ref_angle = (float)(cylindrical_coords[1]*180.0/(java.lang.Math.PI));
		
		opw.write ("BANK" +i + "     "+ref_angle+"     "+total_length);
		opw.write("\n");
	    }
	
	int en= ds.getNum_entries();
	int bank=0;
	
	// write out the data
	for(int i=1; i<=en; i++)
	    {
		// format definitions
		DecimalFormat df=new DecimalFormat(  "000000");
		DecimalFormat dff=new DecimalFormat( "00.0000000");
		DecimalFormat dff1 =new DecimalFormat("000000.0000000");
		// the intensities
		float [] y = ds.getData_entry(i-1).getCopyOfY_values();
		
		DataSetTools.dataset.Data dd = ds.getData_entry(i-1);
		DataSetTools.dataset.XScale xx = dd.getX_scale();
		float binwidth = (xx.getEnd_x()-xx.getStart_x())/((float)xx.getNum_x() - 1);
		opw.write("BANK   "+df.format( i )+"    "+ df.format(y.length)+"     "
			  +df.format((int)(y.length/10.0+0.9))+" CONST  "
			  +dff1.format(xx.getStart_x())
			  +"  STD     "+dff.format(binwidth)+"    \n");
		
		loop:for(int j=0; j<y.length; j+=10)
		    {
			for(int l=j+0; l<j+10; l++)
			    {
				if(l>=y.length)  
				    {    
					opw.write("        ");                
				    }
				else 
				    opw.write("  "+df.format(y[l]));
			    }
			opw.write("\n");
		    }
	    }
        opw.flush();
        opw.close();
        Thread.sleep(100);
    } catch(Exception d){}
    }

    public static void main(String[] args){}
}
