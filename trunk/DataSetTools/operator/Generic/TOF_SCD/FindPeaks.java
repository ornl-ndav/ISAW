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
    DetInfoListAttribute detI;
    DetectorInfo det;
    
    run_number=((int[])data_set.getAttributeValue(Attribute.RUN_NUM))[0];
    
    int[] dims={numData,5};
    int [][] pos=(int[][])Array.newInstance(int.class,dims);
    
    for( int i=0 ; i<numData ; i++ ){
      data=data_set.getData_entry(i);
      detI=(DetInfoListAttribute)
        data.getAttribute(Attribute.DETECTOR_INFO_LIST);
      det=((DetectorInfo[])detI.getValue())[0];
      pos[i][0]=i;
      pos[i][1]=det.getDet_num();
      pos[i][2]=det.getColumn();
      pos[i][3]=det.getRow();
      pos[i][4]=det.getDet_num();
    }
    
    float init_path=((Float)data.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();

    int minColumn=1000;
    int maxColumn=0;
    int minRow=1000;
    int maxRow=0;
    int minTime=0;
    int maxTime=(data.getCopyOfY_values()).length;

    float[] times=data.getX_scale().getXs();
    
    // position of detector center
    float detA  = detector_angle(data_set);
    float detA2 = 0f;
    float detD  = detector_distance(data_set,detA);
    // sample orientation
    float chi   = ((Float)data_set.getAttributeValue(Attribute.SAMPLE_CHI)).floatValue();
    float phi   = ((Float)data_set.getAttributeValue(Attribute.SAMPLE_PHI)).floatValue();
    float omega = ((Float)data_set.getAttributeValue(Attribute.SAMPLE_OMEGA)).floatValue();

    for( int i=0 ; i<numData ; i++ ){
      if(pos[i][2]>maxColumn) maxColumn=pos[i][2];
      if(pos[i][2]<minColumn) minColumn=pos[i][2];
      if(pos[i][3]>maxRow) maxRow=pos[i][3];
      if(pos[i][3]<minRow) minRow=pos[i][3];
    }
    
    String instName=(String)
      (data_set.getAttribute(Attribute.INST_NAME)).getValue();
    if(instName.equals("SCD")){
      if(maxRow==87){
        SharedData.addmsg("Instrument is SCD, reseting"
                          +" maxRow from 87 to 85");
        maxRow=85;
      }
    } 
    
    SharedData.addmsg("Columns("+minColumn+"<"+maxColumn
                      +") Rows("+minRow+"<"+maxRow
                      +") TimeIndices("+minTime+"<"+maxTime+")");
    
    int iPrev,iNext;
    int jPrev,jNext;
    int kPrev,kNext;
    
    /* Data Dpp,Dtp,Dnp,
       Dpt,Dtt,Dnt,
       Dpn,Dtn,Dnn; */
    
    float[] Dpp,Dtp,Dnp,
            Dpt,Dtt,Dnt,
 	    Dpn,Dtn,Dnn;
    
    Dpp=data_set.getData_entry(0).getCopyOfY_values();
    Dtp=Dpp; Dnp=Dpp;
    Dpt=Dpp; Dtt=Dpp; Dnt=Dpp;
    Dpn=Dpp; Dtn=Dpp; Dnn=Dpp;
    
    
    int  Ipp,Itp,Inp,
         Ipt,Itt,Int,
         Ipn,Itn,Inn;
    Ipp=0; Itp=0; Inp=0;
    Ipt=0; Itt=0; Int=0;
    Ipn=0; Itn=0; Inn=0;
    
    Peak peak=new Peak();
    //int[] peak={0,0,0,0,0,0,0};
    Vector peaks=new Vector();
    int peakNum=0;
    float[] calib=null;
    XScale xscale=data_set.getData_entry(0).getX_scale();
    
    for( int i=minRow ; i<=maxRow ; i++ ){    // loop over row
      iPrev=i-1;
      iNext=i+1;
      if(iPrev<minRow){
        iPrev=minRow;
      }else if(iNext>maxRow){
        iNext=maxRow;
      }
      for( int j=minColumn ; j<=maxColumn ; j++ ){      // loop over column
        jPrev=j-1;
        jNext=j+1;
        if(jPrev<minColumn){
          jPrev=minColumn;
        }else if(jNext>maxColumn){
          jNext=maxColumn;
        }
        
        // set up datasets for adjacent pixels
        for( int m=0 ; m<numData ; m++ ){
          if(pos[m][3]==jPrev){       // is it in the p row
            if(pos[m][2]==iPrev){       // is it the pp element
              Dpp=data_set.getData_entry(m).getCopyOfY_values();
              Ipp=m;
            }else if(pos[m][2]==i){     // is it the pt element
              Dpt=data_set.getData_entry(m).getCopyOfY_values();
              Ipt=m;
            }else if(pos[m][2]==iNext){ // is it the pn element
              Dpn=data_set.getData_entry(m).getCopyOfY_values();
              Ipn=m;
            }
          }else if(pos[m][3]==j){     // is it in the t row
            if(pos[m][2]==iPrev){       // is it the tp element
              Dtp=data_set.getData_entry(m).getCopyOfY_values();
              Itp=m;
            }else if(pos[m][2]==i){     // is it the tt element
              Dtt=data_set.getData_entry(m).getCopyOfY_values();
              Itt=m;
              calib=(float[])data_set.getData_entry(m)
                .getAttributeValue(Attribute.SCD_CALIB);
            }else if(pos[m][2]==iNext){ // is it the tn element
              Dtn=data_set.getData_entry(m).getCopyOfY_values();
              Itn=m;
            }
          }else if(pos[m][3]==jNext){ // is it in the n row
            if(pos[m][2]==iPrev){       // is it the np element
              Dnp=data_set.getData_entry(m).getCopyOfY_values();
              Inp=m;
            }else if(pos[m][2]==i){     // is it the nt element
              Dnt=data_set.getData_entry(m).getCopyOfY_values();
              Int=m;
            }else if(pos[m][2]==iNext){ // is it the nn element
              Dnn=data_set.getData_entry(m).getCopyOfY_values();
              Inn=m;
            }
          }
        }
        
        for( int k=minTime ; k<maxTime ; k++ ){ // loop over timeslice
          kPrev=k-1;
          kNext=k+1;
          if(kPrev<minTime){
            kPrev=minTime;
          }else if(kNext>maxTime-1){
            kNext=maxTime-1;
          }
          
          float I=Dtt[k];
          if(I>min_count){
            if(I<Dpp[kPrev]) continue;
            if(I<Dpp[k])     continue;
            if(I<Dpp[kNext]) continue;
            
            if(I<Dtp[kPrev]) continue;
            if(I<Dtp[k])     continue;
            if(I<Dtp[kNext]) continue;
            
            if(I<Dnp[kPrev]) continue;
            if(I<Dnp[k])     continue;
            if(I<Dnp[kNext]) continue;
            
            if(I<Dpt[kPrev]) continue;
            if(I<Dpt[k])     continue;
            if(I<Dpt[kNext]) continue;
            
            if(I<Dtt[kPrev]) continue;
            // this is the current one
            if(I<Dtt[kNext]) continue;
            
            if(I<Dnt[kPrev]) continue;
            if(I<Dnt[k])     continue;
            if(I<Dnt[kNext]) continue;
            
            if(I<Dpn[kPrev]) continue;
            if(I<Dpn[k])     continue;
            if(I<Dpn[kNext]) continue;
            
            if(I<Dtn[kPrev]) continue;
            if(I<Dtn[k])     continue;
            if(I<Dtn[kNext]) continue;
            
            if(I<Dnn[kPrev]) continue;
            if(I<Dnn[k])     continue;
            if(I<Dnn[kNext]) continue;
            
            peak=new Peak(peakNum,pos[Itt][2],pos[Itt][3],k,
                          (int)I,run_number,pos[Itt][4]);
            peak.nearedge(minColumn, maxColumn,
                          minRow,    maxRow,
                          minTime,   maxTime);
            //peak.t(times[k]);
            peak.detA(detA);
            peak.detA2(detA2);
            peak.detD(detD);
            peak.sample_orient(chi,phi,omega);
            peak.monct(moncount);
            peak.L1(init_path);
            peak.times(xscale.getX(k),xscale.getX(k+1));
            peak.calib(calib);
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
  
  
  /* ------------------------------ printme ------------------------------- */ 
  private void printme( int x, int y, int z, int I){
    System.out.println("("+x+","+y+","+z+") "+I);
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
  
  /* -------------------------- detector position ------------------------- */ 
  /**
   * Find the detector angle by averaging over pixel angles.
   */
  static private float detector_angle(DataSet ds){
    DetInfoListAttribute detI;
    DetectorInfo det;
    Data data=ds.getData_entry(0);
    float angle=0f;
    Float Fangle=new Float(0f);
    int total=0;
    //System.out.print("ANGLE="+angle);
    Fangle=(Float)data.getAttributeValue(Attribute.DETECTOR_CEN_ANGLE);
    if(Fangle!=null){
      angle=Fangle.floatValue();
    }
    //System.out.print("->"+angle);
    if(angle==0f){
      for( int i=0 ; i< ds.getNum_entries() ; i++ ){
        data=ds.getData_entry(i);
        detI=(DetInfoListAttribute)
          data.getAttribute(Attribute.DETECTOR_INFO_LIST);
        det=((DetectorInfo[])detI.getValue())[0];
        angle+=det.getPosition().getScatteringAngle();
        total++;
        //System.out.println(total+":"+angle);
      }
      angle=(180*angle)/((float)(total+1)*(float)Math.PI);
    }
    //System.out.println("->"+angle);
    
    return angle;
  }
  
  /**
   * Find the detector angle by averaging over pixel angles.
   */
  static private float detector_angle2(DataSet ds){
    return 0f;
  }
  
 /**
  * Find the detector distance by averaging over perpendicular pixel
  * distance.
  */
  static private float detector_distance(DataSet ds, float avg_angle){
    DetInfoListAttribute detI;
    DetectorInfo det;
    Data data=ds.getData_entry(0);
    float angle=0f;
    float distance=0f;
    Float Fdistance=new Float(distance);
    int total=0;
    
    Fdistance=(Float)data.getAttributeValue(Attribute.DETECTOR_CEN_DISTANCE);
    if(Fdistance!=null){
      distance=Fdistance.floatValue();
    }
    
    if(distance==0f){
      for( int i=0 ; i< ds.getNum_entries() ; i++ ){
        data=ds.getData_entry(i);
        detI=(DetInfoListAttribute)
          data.getAttribute(Attribute.DETECTOR_INFO_LIST);
        det=((DetectorInfo[])detI.getValue())[0];
        
        angle=det.getPosition().getScatteringAngle();
        angle=angle-2f*avg_angle/(float)Math.PI;
        
        angle=(float)Math.abs(Math.cos((double)angle));
        
        distance+=angle*det.getPosition().getDistance();
        total++;
      }
      distance=distance/((float)(total+1));
    }
    
    return distance*100f;
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
    
    FindPeaks op = new FindPeaks();
    
    //op = new FindPeaks( rds, 0 );
    //op.getResult();
    
    //System.out.println("Findpeaks("+datfile+",100,0)");
    op = new FindPeaks( rds, monct, 10, 20 );
    Vector peaked=(Vector)op.getResult();
    
    //System.out.println(((int[])rds.getAttributeValue(Attribute.RUN_NUM))[0]);
    
    Peak peak=new Peak();
    for( int i=0 ; i<peaked.size() ; i++ ){
      peak=(Peak)peaked.elementAt(i);
      System.out.println(peak);
    }
    System.exit(0);
  }
}
