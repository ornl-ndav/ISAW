/*
 * File:  CentroidPeaks.java 
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
 * Revision 1.9  2003/01/30 21:08:56  pfpeterson
 * Huge reduction in amount of code due to new Util class.
 *
 * Revision 1.8  2003/01/15 20:54:25  dennis
 * Changed to use SegmentInfo, SegInfoListAttribute, etc.
 *
 * Revision 1.7  2002/11/27 23:22:20  pfpeterson
 * standardized header
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SCD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Attribute.LoadSCDCalib;
import DataSetTools.instruments.*;
import DataSetTools.util.SharedData;
import DataSetTools.retriever.RunfileRetriever;
import java.util.*;
import java.util.Vector;
import java.lang.reflect.Array;
import java.text.DecimalFormat;

/** 
 * This operator is a small building block of an ISAW version of
 * A.J.Schultz's PEAKS program. This program takes a list of peaks and
 * calculates their centers using a centroid method.
 */
public class CentroidPeaks extends GenericTOF_SCD implements HiddenOperator{
  private static final String     TITLE                 = "Centroid Peaks";
  private static final int        time_notice_frequency = 20;
  private static final SharedData shared                = new SharedData();
  private              int        run_number            = -1;
  
  /**
   *  Creates operator with title "Centroid Peaks" and a default
   *  list of parameters.
   */  
  public CentroidPeaks(){
    super( TITLE );
  }
  
  /** 
   *  Creates operator with title "Centroid Peaks" and the specified
   *  list of parameters. The getResult method must still be used to
   *  execute the operator.
   *
   *  @param data_set DataSet to find peak in
   *  @param peaks Vector of peaks. Normally created by FindPeaks.
   */
  public CentroidPeaks( DataSet data_set, Vector peaks){
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("Histogram", data_set) );
    addParameter( new Parameter("Vector of Peaks", peaks) );
  }
  
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return "CentroidPeaks", the command used to invoke this
   * operator in Scripts
   */
  public String getCommand(){
    return "CentroidPeaks";
  }
  
  /** 
   * Sets default values for the parameters. This must match the
   * data types of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter( new Parameter("Histogram",DataSet.EMPTY_DATA_SET) );
    addParameter( new Parameter("Vector of Peaks", new Vector()) );
  }
  
  /** 
   * Executes this operator using the values of the current
   * parameters.
   *
   * @return If successful, this returns a vector of peaks
   */
  public Object getResult(){
    DataSet data_set = (DataSet)(getParameter(0).getValue());
    Vector  peaks    = (Vector) (getParameter(1).getValue());
    Vector  cpeaks   = new Vector();
    
    XScale times=data_set.getData_entry(0).getX_scale();
    int[][] ids=Util.createIdMap(data_set);
    
    Peak peak=null;
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=Util.centroid((Peak)peaks.elementAt(i),data_set,ids);
      peak.time(times);
    }
    
    return peaks;
  }
  
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Operator op = new CentroidPeaks();
    op.CopyParametersFrom( this );
    return op;
  }
  
  /** 
   * Test program to verify that this will complile and run ok.
   */
  public static void main( String args[] ){
    
    String datfile="/IPNShome/pfpeterson/data/SCD/SCD06496.RUN";
    DataSet rds = (new RunfileRetriever(datfile)).getDataSet(1);
    LoadSCDCalib lsc=new
      LoadSCDCalib(rds,"/IPNShome/pfpeterson/data/SCD/instprm.dat",1,null);
    lsc.getResult();
    
    FindPeaks fo = new FindPeaks(rds,138568f,10,1);
    Vector peaked=(Vector)fo.getResult();
    Peak peak=new Peak();
    for( int i=0 ; i<peaked.size() ; i++ ){
      System.out.println(peaked.elementAt(i));
    }
    System.out.println("done with FindPeaks");
    
    CentroidPeaks co = new CentroidPeaks();
    co = new CentroidPeaks( rds, peaked );
    peaked=(Vector)co.getResult();
    for( int i=0 ; i<peaked.size() ; i++ ){
      System.out.println(peaked.elementAt(i));
    }
    System.out.println("done with CentroidPeaks");
    System.exit(0);
  }
}
