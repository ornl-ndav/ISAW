/*
 * File:  FindPeaks.java 
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
 * Revision 1.13  2003/01/31 17:50:58  pfpeterson
 * Fixed bug with array indices going out of bounds.
 *
 * Revision 1.12  2003/01/30 21:09:44  pfpeterson
 * Takes advantage of the PeakFactory class and utility class.
 *
 * Revision 1.11  2003/01/15 20:54:26  dennis
 * Changed to use SegmentInfo, SegInfoListAttribute, etc.
 *
 * Revision 1.10  2002/11/27 23:22:20  pfpeterson
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
 * A.J.Schultz's PEAKS program. While the original program found all
 * peaks within a particular time-slice then compared it to the
 * adjacent time-slices, this finds the peaks in a column slice and
 * compares them to adjacent columns. The difference is due to how
 * ISAW stores the dataset.
 */
public class FindPeaks extends GenericTOF_SCD implements HiddenOperator{
  private static final String     TITLE                 = "Find Peaks";
  private              int        run_number            = -1;
  
  /* ------------------------ Default constructor ------------------------- */ 
  /**
   *  Creates operator with title "Find Peaks" and a default list of
   *  parameters.
   */  
  public FindPeaks(){
    super( TITLE );
  }
  
  /** 
   *  Creates operator with title "Find Peaks" and the specified list
   *  of parameters. The getResult method must still be used to execute
   *  the operator.
   *
   *  @param  data_set    DataSet to find peak in
   *  @param  min_count   Minimum number of counts peak must have
   */
  public FindPeaks( DataSet data_set, float moncount, int maxNumPeaks,
                    int min_count){
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("Histogram", data_set) );
    addParameter( new Parameter("Integrated Monitor", new Float(moncount)) );
    addParameter( new Parameter("Maximum Number of Peaks",
                                new Integer(maxNumPeaks)));
    addParameter( new Parameter("Minimum Counts", new Integer(min_count) ) );
  }
  
  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "FindPeaks", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand(){
    return "FindPeaks";
  }
  
  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets default values for the parameters.  This must match the data types 
   * of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter( new Parameter("Data Set", DataSet.EMPTY_DATA_SET ) );
    addParameter( new Parameter("Integrated Monitor", new Float(0f)) );
    addParameter( new Parameter("Maximum Number of Peaks", new Integer(1000)));
    addParameter( new Parameter("Minimum Counts", new Integer(0) ) );
  }
  
  /* ----------------------------- getResult ------------------------------ */ 
  /** 
   *  Executes this operator using the values of the current parameters.
   *
   *  @return If successful, this operator returns a vector of Peak
   *  objects.
   */
  public Object getResult(){
    DataSet data_set     =  (DataSet)(getParameter(0).getValue());
    float   moncount     =  ((Float)(getParameter(1).getValue())).floatValue();
    int     maxNumPeaks  = ((Integer)(getParameter(2).getValue())).intValue();
    int     min_count    = ((Integer)(getParameter(3).getValue())).intValue();
    
    //System.out.print("====================================");
    //System.out.println("==================================");
    Data data=data_set.getData_entry(0);
    int numData=data_set.getNum_entries();
    SegInfoListAttribute segI;
    SegmentInfo seg=null;
    int det_number=0;

    run_number=((int[])data_set.getAttributeValue(Attribute.RUN_NUM))[0];

    // create an array of for indexing into the data
    int[][] ids=Util.createIdMap(data_set);
    
    float init_path=((Float)data.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();

    int minColumn=1000;
    int maxColumn=0;
    int minRow=1000;
    int maxRow=0;
    int minTime=0;
    int maxTime=(data.getCopyOfY_values()).length;

    // position of detector center
    float detA  = Util.detector_angle(data_set);
    float detA2 = Util.detector_angle2(data_set);
    float detD  = Util.detector_distance(data_set,detA);

    // sample orientation
    float chi   = ((Float)data_set.getAttributeValue(Attribute.SAMPLE_CHI)).floatValue();
    float phi   = ((Float)data_set.getAttributeValue(Attribute.SAMPLE_PHI)).floatValue();
    float omega = ((Float)data_set.getAttributeValue(Attribute.SAMPLE_OMEGA)).floatValue();

    // determine the minimum and maximum row and columns
    outer: for( int i=0 ; i<ids.length ; i++ ){
      for( int j=0 ; j<ids[0].length ; j++ ){
        if(ids[i][j]==-1) continue; // there is nothing here
        minColumn=i;
        minRow=j;
        break outer;
      }
    }
    outer: for( int i=ids.length-1 ; i>minColumn ; i-- ){
      for( int j=ids[0].length-1 ; j>minRow ; j-- ){
        if(ids[i][j]==-1) continue; // there is nothing here
        maxColumn=i;
        maxRow=j;
        break outer;
      }
    }

    // the detector number
    if(seg!=null){
      det_number=seg.getDet_num();
    }else{
      segI=(SegInfoListAttribute)data_set.getData_entry(ids[minColumn][minRow])
        .getAttribute(Attribute.SEGMENT_INFO_LIST);
      seg=((SegmentInfo[])segI.getValue())[0];
      det_number=seg.getDet_num();
    }

    SharedData.addmsg("Columns("+minColumn+"<"+maxColumn
                      +") Rows("+minRow+"<"+maxRow
                      +") TimeIndices("+minTime+"<"+maxTime+")");
    
    float[] Dpp=null, Dtp=null, Dnp=null,
            Dpt=null, Dtt=null, Dnt=null,
 	    Dpn=null, Dtn=null, Dnn=null;
    
    Peak peak=new Peak();
    //int[] peak={0,0,0,0,0,0,0};
    Vector peaks=new Vector();
    int peakNum=0;
    float[] calib=(float[])data_set.getData_entry(ids[minColumn][minRow])
      .getAttributeValue(Attribute.SCD_CALIB);
    XScale xscale=data_set.getData_entry(ids[minColumn][minRow])
      .getX_scale();
    PeakFactory pkfac=new PeakFactory(run_number,det_number,init_path,
                                      detD,detA,detA2);
    pkfac.time(xscale);
    pkfac.calib(calib);
    pkfac.detA(detA);
    pkfac.detA2(detA2);
    pkfac.detD(detD);
    pkfac.sample_orient(chi,phi,omega);
    pkfac.monct(moncount);
    pkfac.L1(init_path);

    // stay off of the edges
    for( int i=minColumn+1 ; i<maxColumn-1 ; i++ ){  // loop over column
      for( int j=minRow+1 ; j<maxRow-1 ; j++ ){      // loop over row
        // set up datasets for adjacent pixels
        Dpp=data_set.getData_entry(ids[i-1][j-1]).getCopyOfY_values();
        Dpt=data_set.getData_entry(ids[i+0][j-1]).getCopyOfY_values();
        Dpn=data_set.getData_entry(ids[i+1][j-1]).getCopyOfY_values();
        Dtp=data_set.getData_entry(ids[i-1][j+0]).getCopyOfY_values();
        Dtt=data_set.getData_entry(ids[i+0][j+0]).getCopyOfY_values();
        Dtn=data_set.getData_entry(ids[i+1][j+0]).getCopyOfY_values();
        Dnp=data_set.getData_entry(ids[i-1][j+1]).getCopyOfY_values();
        Dnt=data_set.getData_entry(ids[i+0][j+1]).getCopyOfY_values();
        Dnn=data_set.getData_entry(ids[i+1][j+1]).getCopyOfY_values();
        for( int k=minTime+1 ; k<maxTime-1 ; k++ ){ // loop over timeslice
          float I=Dtt[k];
          if(I>min_count){
            if(I<Dpp[k-1]) continue;
            if(I<Dpp[k+0]) continue;
            if(I<Dpp[k+1]) continue;
            
            if(I<Dtp[k-1]) continue;
            if(I<Dtp[k+0]) continue;
            if(I<Dtp[k+1]) continue;
            
            if(I<Dnp[k-1]) continue;
            if(I<Dnp[k+0]) continue;
            if(I<Dnp[k+1]) continue;
            
            if(I<Dpt[k-1]) continue;
            if(I<Dpt[k+0]) continue;
            if(I<Dpt[k+1]) continue;
            
            if(I<Dtt[k-1]) continue;
            // this is the current one
            if(I<Dtt[k+1]) continue;
            
            if(I<Dnt[k-1]) continue;
            if(I<Dnt[k+0]) continue;
            if(I<Dnt[k+1]) continue;
            
            if(I<Dpn[k-1]) continue;
            if(I<Dpn[k+0]) continue;
            if(I<Dpn[k+1]) continue;
            
            if(I<Dtn[k-1]) continue;
            if(I<Dtn[k+0]) continue;
            if(I<Dtn[k+1]) continue;
            
            if(I<Dnn[k-1]) continue;
            if(I<Dnn[k+0]) continue;
            if(I<Dnn[k+1]) continue;
            
            peak=pkfac.getPixelInstance(i,j,k,0,0);
            peak.seqnum(peakNum);
            peak.ipkobs((int)Math.round(I));
            peak.nearedge(minColumn,maxColumn,minRow,maxRow,minTime,maxTime);
            peaks.add(peak.clone());
            peakNum++;
          }
          
          
        } // end of loop over timeslice
      }     // end of loop over row
    }         // end of loop over column
    SharedData.addmsg("Found "+peaks.size()+" peaks (maximum number "+
                      maxNumPeaks+")");
    
    SharedData.addmsg("Sorting peaks");
    peaks=sort(peaks,maxNumPeaks);
    
    if(peaks.size()>maxNumPeaks){
      for( int i=peaks.size()-1 ; i>=maxNumPeaks ; i-- ){
        peaks.remove(i);
      }
      SharedData.addmsg("Keeping "+peaks.size()+" peaks");
    }
    
    peaks=sortT(peaks);
    return peaks;
  }
  
  
  /* ------------------------------- sort --------------------------------- */ 
  private Vector sort(Vector peaks, int maxNumPeaks){
    Vector sortPeaks=new Vector();
    int origPeaksSize=peaks.size();
    Peak peak=new Peak();
    //int[] peak={0,0,0,0,0,0,0};
    int maxPeak=getMaxPeak(peaks);
    
    while(sortPeaks.size()<origPeaksSize && sortPeaks.size()<maxNumPeaks){
      for( int i=0 ; i<peaks.size() ; i++ ){
        if(((Peak)peaks.elementAt(i)).ipkobs()==maxPeak){
          peak=(Peak)peaks.elementAt(i);
          peak.seqnum(sortPeaks.size()+1);
          sortPeaks.add(peak.clone());
          peaks.remove(i);
          break;
        }
      }
      maxPeak=getMaxPeak(peaks);
    }
    
    if(origPeaksSize>maxNumPeaks){
      SharedData.addmsg("Keeping largest "+sortPeaks.size()
                        +" peaks");
    }
    
    return sortPeaks;
  }
  
  /* ------------------------------- sortT -------------------------------- */ 
  private Vector sortT(Vector peaks){
    Vector sortPeaks=new Vector();
    int origPeaksSize=peaks.size();
    Peak peak=new Peak();
    float minT=getMinT(peaks);
    
    while(sortPeaks.size()<origPeaksSize ){
      for( int i=0 ; i<peaks.size() ; i++ ){
        if(((Peak)peaks.elementAt(i)).z()==minT){
          peak=(Peak)peaks.elementAt(i);
          peak.seqnum(sortPeaks.size()+1);
          sortPeaks.add(peak.clone());
          peaks.remove(i);
          break;
        }
      }
      minT=getMinT(peaks);
    }
    
    /* if(origPeaksSize>maxNumPeaks){
       SharedData.addmsg("Keeping largest "+sortPeaks.size()
       +" peaks");
       } */
    
    return sortPeaks;
  }
  
  /* ---------------------------- getMaxPeak ------------------------------ */ 
  private int getMaxPeak(Vector peaks){
    int maxPeak=0;
    for( int i=0 ; i<peaks.size() ; i++ ){
      if(((Peak)peaks.elementAt(i)).ipkobs()>maxPeak){
        maxPeak=((Peak)peaks.elementAt(i)).ipkobs();
      }
    }
    return maxPeak;
  }
  
  /* ---------------------------- getMinT --------------------------------- */ 
  private float getMinT(Vector peaks){
    float minT=10000f;
    for( int i=0 ; i<peaks.size() ; i++ ){
      if(((Peak)peaks.elementAt(i)).z()<minT){
        minT=((Peak)peaks.elementAt(i)).z();
      }
    }
    return minT;
  }
  
  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Operator op = new FindPeaks();
    op.CopyParametersFrom( this );
    return op;
  }
  
  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] ){
    
    String datfile="/IPNShome/pfpeterson/data/SCD/SCD06496.RUN";
    DataSet mds = (new RunfileRetriever(datfile)).getDataSet(0);
    DataSet rds = (new RunfileRetriever(datfile)).getDataSet(1);
    //System.out.println(rds.getAttributeValue(Attribute.RUN_NUM));
    //Data data = rds.getData_entry_with_id(2523);
    float monct=138568f;
    
    LoadSCDCalib lsc=new 
      LoadSCDCalib(rds,"/IPNShome/pfpeterson/data/SCD/instprm.dat",1,null);
    lsc.getResult();
    
    FindPeaks op =null;
    
    //op = new FindPeaks( rds, 0 );
    //op.getResult();
    
    //System.out.println("Findpeaks("+datfile+",100,0)");
    op = new FindPeaks( rds, monct, 10, 20 );
    Vector peaked=(Vector)op.getResult();
    
    //System.out.println(((int[])rds.getAttributeValue(Attribute.RUN_NUM))[0]);
    
    for( int i=0 ; i<peaked.size() ; i++ ){
      System.out.println(peaked.elementAt(i));
    }
    System.exit(0);
  }
}
