/*
 * File:  WritePeaks.java 
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
 * Contact : Peter Peterson <pfpeterson@anl.gov>
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
 */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import DataSetTools.util.SharedData;
import DataSetTools.retriever.RunfileRetriever;
import java.io.*;
import java.util.*;
import java.util.Vector;
import java.lang.reflect.Array;
import java.text.DecimalFormat;

/** 
 * This operator is a small building block of an ISAW version of
 * A.J.Schultz's PEAKS program. This operator writes out the
 * information in a format specified by Art.
 */
public class WritePeaks extends GenericTOF_SCD implements HiddenOperator{
    private static final String TITLE       = "Write Peaks";
    private static final SharedData shared= new SharedData();



 /* ------------------------ Default constructor ------------------------- */ 
 /**
  *  Creates operator with title "Write Peaks" and a default list of
  *  parameters.
  */  
    public WritePeaks()
    {
	super( TITLE );
    }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Creates operator with title "Write Peaks" and the specified list
  *  of parameters. The getResult method must still be used to execute
  *  the operator.
  *
  *  @param  file      Filename to print to
  *  @param  mon_data  Monitor DataSet
  *  @param  peaks     Vector of peaks
  *  @param  append    Whether to append to specified file
  */
    public WritePeaks( String file, DataSet mon_data, Vector peaks, 
		       Boolean append){
	this(); 
	parameters = new Vector();
	addParameter( new Parameter("File Name", file) );
	addParameter( new Parameter("Monitor", mon_data) );
	addParameter( new Parameter("Vector of Peaks",peaks) );
	addParameter( new Parameter("Append",append) );
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "WritePeaks", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "WritePeaks";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("File Name", "filename" ) );
    addParameter( new Parameter("Monitor", DataSet.EMPTY_DATA_SET ) );
    addParameter( new Parameter("Vector of Peaks", new Vector() ) );
    addParameter( new Parameter("Append", Boolean.FALSE) );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return If successful, this operator prints out a list of x,y,time
  *  bins and intensities.
  */
  public Object getResult()
  {
    String  file     = (String) (getParameter(0).getValue());
    DataSet mon_data = (DataSet)(getParameter(1).getValue());
    Vector  peaks    = (Vector) (getParameter(2).getValue());
    boolean append   = ((Boolean)(getParameter(3).getValue())).booleanValue();
    OutputStreamWriter outStream;

    // general information
    int nrun=((Peak)peaks.elementAt(0)).nrun();
    int detnum=((Peak)peaks.elementAt(0)).detnum();
    float deta=((Peak)peaks.elementAt(0)).detA();
    float deta2=((Peak)peaks.elementAt(0)).detA2();
    float detd=((Peak)peaks.elementAt(0)).detD();

    // sample orientation
    float chi=((Peak)peaks.elementAt(0)).chi();
    float phi=((Peak)peaks.elementAt(0)).phi();
    float omega=((Peak)peaks.elementAt(0)).omega();

    float moncnt=0.0f;
    if(mon_data != null){
	moncnt=((Float)
	    (mon_data.getData_entry_with_id(1))
	    .getAttributeValue(Attribute.TOTAL_COUNT)).floatValue();
	//moncnt=Moncnt.floatValue();
    }

    int seqnum_off=0;

    try{
	// open and initialize a buffered file stream
	FileOutputStream op = new FileOutputStream(file,append);
	outStream=new OutputStreamWriter(op);
   
	// general information header
	outStream.write("0  NRUN DETNUM    DETA   DETA2    DETD     CHI     "
			+"PHI   OMEGA   MONCNT"+"\n");

	// general information
	outStream.write("1"+format(nrun,6)
			+format(detnum,7)
			+format(deta,8)
                        +format(deta2,8)
			+format(detd,8)
			+format(chi,8)
			+format(phi,8)
			+format(omega,8)
			+format((int)moncnt,9)
			+"\n");

	// peaks field header
	outStream.write("2  SEQN   H   K   L      X      Y"
			+"      Z    XCM    YCM      WL   IPK"
			+"     INTI     SIGI RFLG  NRUN DN"+"\n");
	// write out the peaks
	for( int i=0 ; i<peaks.size() ; i++ ){
            if(((Peak)peaks.elementAt(i)).reflag()==20){
                seqnum_off++;
            }else{
                int seqnum=((Peak)peaks.elementAt(i)).seqnum()-seqnum_off;
                ((Peak)peaks.elementAt(i)).seqnum(seqnum);
                outStream.write(((Peak)peaks.elementAt(i)).toString()+"\n");
            }
	}

	// flush and close the buffered file stream
	outStream.flush();
	outStream.close();
    }catch(Exception e){
    }

    return file;
  }

 /* ----------------------------- formating ------------------------------ */ 
 /**
  * Format an integer by padding on the left.
  */
  static private String format(int number,int length){
      String rs=new Integer(number).toString();
      while(rs.length()<length){
	  rs=" "+rs;
      }
      return rs;
  }

 /**
  * Format a float by padding on the left.
  */
  static private String format(float number,int length){
      DecimalFormat df_ei_tw=new DecimalFormat("####0.00");
      String rs=df_ei_tw.format(number);
      while(rs.length()<length){
	  rs=" "+rs;
      }
      return rs;
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
      Operator op = new WritePeaks();
      op.CopyParametersFrom( this );
      return op;
  }

 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
    public static void main( String args[] ){
	
	String outfile="/IPNShome/pfpeterson/ISAW/DataSetTools/"
	    +"operator/Generic/TOF_SCD/lookatme.rfl";
	//String outfile="/IPNShome/pfpeterson/lookatme.rfl";
	String datfile="/IPNShome/pfpeterson/data/SCD/SCD06496.RUN";
	DataSet mds = (new RunfileRetriever(datfile)).getDataSet(0);
	DataSet rds = (new RunfileRetriever(datfile)).getDataSet(1);
	
	FindPeaks fo = new FindPeaks(rds,10,1,false);
	Vector peaked=(Vector)fo.getResult();
	
	/* CentroidPeaks co=new CentroidPeaks(rds,peaked);
	   peaked=(Vector)co.getResult(); */

	WritePeaks wo = new WritePeaks(outfile,mds,peaked,Boolean.FALSE);
	System.out.println(wo.getResult());
    }
}
