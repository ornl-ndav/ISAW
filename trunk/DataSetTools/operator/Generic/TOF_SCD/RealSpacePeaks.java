/*
 * File:  RealSpacePeaks.java 
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
import DataSetTools.util.LoadFileString;
import DataSetTools.util.SharedData;
import DataSetTools.util.TextFileReader;
import DataSetTools.retriever.RunfileRetriever;
import java.util.*;
import java.util.Vector;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.io.*;

/** 
 * This operator is a small building block of an ISAW version of
 * A.J.Schultz's PEAKS program. While the original program found all
 * peaks within a particular time-slice then compared it to the
 * adjacent time-slices, this finds the peaks in a column slice and
 * compares them to adjacent columns. The difference is due to how
 * ISAW stores the dataset.
 */
public class RealSpacePeaks extends GenericTOF_SCD{
    private static final String     TITLE  = "Real Space Peaks";
    private static final SharedData shared = new SharedData();
    private static       float      ax     = 0f;
    private static       float      bx     = 0f;
    private static       float      ay     = 0f;
    private static       float      by     = 0f;
    private static       float      T0     = 0f;
    private static       float      L1     = 0f;


 /* ------------------------ Default constructor ------------------------- */ 
 /**
  *  Creates operator with title "Real Space Peaks" and a default list
  *  of parameters.
  */  
    public RealSpacePeaks()
    {
	super( TITLE );
    }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Creates operator with title "Real Space Peaks" and the specified
  *  list of parameters. The getResult method must still be used to
  *  execute the operator.
  *
  *  @param  data_set    DataSet to find peak in
  *  @param  min_count   Minimum number of counts peak must have
  */
    public RealSpacePeaks( Vector peaks, String calib_file){
	this(); 
	parameters = new Vector();
    addParameter( new Parameter("Vector of Peaks",  peaks)      );
    addParameter( new Parameter("Calibration File", calib_file) );
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "RealSpacePeaks", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "RealSpacePeaks";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Vector of Peaks",  new Vector() ) );
    addParameter( new Parameter("Calibration File", new LoadFileString() ) );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return If successful, this operator returns a vector of Peak
  *  objects.
  */
  public Object getResult()
  {
    Vector peaks      = (Vector)(getParameter(0).getValue());
    String calib_file = ((LoadFileString)(getParameter(1).getValue())).toString();

    if(!this.readCalib(calib_file)){
	return peaks;
    }
    //System.out.println(L1+" "+T0+" "+ax+" "+ay+" "+bx+" "+by);
    Peak peak;
    for( int i=0 ; i<peaks.size() ; i++ ){
	peak=(Peak)peaks.elementAt(i);
	peak.xcm(ax,bx);
	peak.ycm(ay,by);
	peak.wl(L1,T0);
    }

    return peaks;
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new RealSpacePeaks();
    op.CopyParametersFrom( this );
    return op;
  }

 /* -------------------------- private methods --------------------------- */ 
 /**
  * Read in the calibration parameters.
  */
    private boolean readCalib( String filename ){
	try{
	    TextFileReader reader=new TextFileReader(filename);
	    reader.read_line();  // let the first line drop on the floor
	    reader.read_int();   // skip over the detector number
	    reader.read_float(); // skip over the detector angle
	    reader.read_float(); // skip over the detector distance
	    L1=reader.read_float(); // the primary flight path
	    T0=reader.read_float(); // the T0 to add to all times
	    ax=reader.read_float(); // multiplicative factor for x
	    ay=reader.read_float(); // multiplicative factor for y
	    bx=reader.read_float(); // additive factor for x
	    by=reader.read_float(); // additive factor for y
	    reader.skip_blanks();
	    reader.read_line();  // let the description drop on the floor
	    reader.close();
	}catch(FileNotFoundException e){
	    shared.status_pane.add("FileNotFoundException: "+filename
				   +" skipping calibration");
	    return false;
	}catch(IOException e){
	    shared.status_pane.add("IOException in readCalib: "+e);
	    return false;
	}catch(Exception e){
	    shared.status_pane.add("Exception in readCalib: "+e);
	    return false;
	}
	return true;
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
	String calibfile="/IPNShome/pfpeterson/progs/scd/instprm.dat";

	FindPeaks fo = new FindPeaks(rds,10,1);
	Vector peaked=(Vector)fo.getResult();
	
	/* CentroidPeaks co=new CentroidPeaks(rds,peaked);
	   peaked=(Vector)co.getResult(); */

	RealSpacePeaks rso = new RealSpacePeaks(peaked,calibfile);
	peaked=(Vector)rso.getResult();

	Peak peak;
	for( int i=0 ; i<peaked.size() ; i++ ){
	    peak=(Peak)peaked.elementAt(i);
	    System.out.println(peak);
	}

    }
}
