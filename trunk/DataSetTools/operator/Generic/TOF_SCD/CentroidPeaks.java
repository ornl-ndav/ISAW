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
 * Revision 1.16  2004/07/14 17:10:16  rmikk
 * Commented out main program and fixed FindPeaks constructor
 *
 * Revision 1.15  2004/04/21 19:08:16  dennis
 * Changed main test program to work with new version of FindPeaks
 * that has the min and max time channel as parameters.
 *
 * Revision 1.14  2004/03/15 03:28:37  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.13  2004/01/24 20:31:15  bouzekc
 * Removed/commented out unused variables/imports.
 *
 * Revision 1.12  2003/12/15 01:45:30  bouzekc
 * Removed unused imports.
 *
 * Revision 1.11  2003/05/06 16:38:26  pfpeterson
 * Added multiple detector support.
 *
 * Revision 1.10  2003/02/06 18:03:27  dennis
 * Added getDocumentation() method.  (Shannon Hintzman)
 *
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
import DataSetTools.retriever.RunfileRetriever;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.util.Vector;

/** 
 * This operator is a small building block of an ISAW version of
 * A.J.Schultz's PEAKS program. This program takes a list of peaks and
 * calculates their centers using a centroid method.
 */
public class CentroidPeaks extends GenericTOF_SCD implements HiddenOperator{
  private static final String     TITLE                 = "Centroid Peaks";
  
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
  
  /* ---------------------------- getDocumentation -------------------------- */
 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This program takes a list of peaks and calculates ");
    Res.append("their centers using a centroid method.");
 
    Res.append("@algorithm The x,y time bin and intensity values are found ");
    Res.append("using various methods.  They are then each printed out with ");
    Res.append("a \"System.out.print\".  (Currently these are all commented ");
    Res.append("out.)  Finally a vector of x,y time bins and intensities is ");
    Res.append("returned.");
       
    Res.append("@param data_set - DataSet to find peak in.");
    Res.append("@param peaks - Vector of peaks. Normally created by ");
    Res.append("FindPeaks.");
    
    Res.append("@return Returns a vector of x,y,time bins and intensities.");
    
    return Res.toString();
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
    
    int detNum=((Peak)peaks.elementAt(0)).detnum();
    if(detNum<=0) return new ErrorString("Invalid detector number: "+detNum);
    int[][] ids=Util.createIdMap(data_set,detNum);
    if(ids==null) return new ErrorString("Could not initialize ids[][]");
    XScale times=data_set.getData_entry(ids[1][1]).getX_scale();
    if(times==null) return new ErrorString("Could not determine TOFs");

    Peak peak=null;
    for( int i=0 ; i<peaks.size() ; i++ ){
      peak=(Peak)peaks.elementAt(i);
      if(peak.detnum()!=detNum){
        detNum=peak.detnum();
        if(detNum<=0)
          return new ErrorString("Invalid detector number: "+detNum);
        ids=Util.createIdMap(data_set,detNum);
        if(ids==null) return new ErrorString("Could not initialize ids[][]");
        times=data_set.getData_entry(ids[1][1]).getX_scale();
        if(times==null) return new ErrorString("Could not determine TOFs");
      }
      peak=Util.centroid(peak,data_set,ids);
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
    
   /* String datfile="/IPNShome/pfpeterson/data/SCD/SCD06496.RUN";
    DataSet rds = (new RunfileRetriever(datfile)).getDataSet(1);
    LoadSCDCalib lsc=new
      LoadSCDCalib(rds,"/IPNShome/pfpeterson/data/SCD/instprm.dat",1,null);
    lsc.getResult();
    
    FindPeaks fo = new FindPeaks(rds,138568f,10,1,0,1000,"");
    Vector peaked=(Vector)fo.getResult();

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
    System.exit(0);*/
  }
}
