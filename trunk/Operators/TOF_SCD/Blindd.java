/*
 * File:  Blindd.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson<Mikkelsonr@UWstout.edu>
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.1  2003/02/10 13:32:45  rmikk
 * Initial Checkin for the Java Blind
 *
 * 
 *
 */
package Operators.TOF_SCD;

import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Information.XAxis.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import java.util.*;
import DataSetTools.parameter.*;
import java.text.*;
import java.io.*;
import IPNSSrc.*;
/** 
 * This operator takes a peaks file and a list of sequence numbers in this file.
 * It then calculates the orientation matrix and other parameters and stores them
 * in a matrix file
 */
public class Blindd extends  GenericTOF_SCD {
    private static final String  TITLE = "JBlind";
    private static final boolean DEBUG = false;

    /* ------------------------ Default constructor ------------------------- */ 
    /**
     *  Creates operator with title "Operator Template" and a default
     *  list of parameters.
     */  
    public Blindd()
    {
	super( TITLE );
    }
    
    /* ---------------------------- Constructor ----------------------------- */ 
    /** 
    *  
     */
    public Blindd( String PeaksFilename, String SeqNums, String MatFilename)
    {

	this(); 
    parameters = new Vector();
    addParameter( new LoadFilePG("Peak filename", PeaksFilename) );
    addParameter( new IntArrayPG("Seq nums", SeqNums) );
    addParameter( new SaveFilePG("Mat filename", MatFilename));
   

    }
    
    /* --------------------------- getCommand ------------------------------- */ 
    /** 
     * Returns Blindd, the name of this operator to use in scripts
     * 
     * @return  "Blindd", the command used to invoke this operator in Scripts
     */
    public String getCommand(){
	return "JBlind";
    }
    

    /*----------------------- getDocumentation -----------------------------*/
    public String getDocumentation(){
      StringBuffer S= new StringBuffer( 1200);
      S.append( "@overview This operator take a peaks file(produced by FindPeaks) and ");
      S.append( "a list of sequence numbers from this file to create a file with the ");
      S.append( "orientation matrix for the crystal along with several other ");
      S.append( "parameters. A blind.log file is also produced");
      S.append( "@algorithm The peaks are sent through the Blind program. This is a ");
      S.append( "program used at the IPNS division at Argonne National Laboratory for ");
      S.append( "this task.");
      S.append( "@assumptions The peak file must be of the standard format for peak ");
      S.append( "files supported by the IPNS division at Argonne National Laboratory");
      S.append( "@param   PeaksFilename- The name of the file with peak information");
      S.append( "@param  SeqNums- The list of sequence numbers to use. Eg 33:36,47,56");
      S.append( "@param  MatFilename- The filename to store the orientation matrix ");
      S.append( "and the other cell parameters ");
      S.append( "@return  Success or one of the errormessages below ");
      S.append( "@error Improper Peak filename ");
      S.append( "@error Improper sequence numbers");
      S.append( "@error Improper save matrix filename");
      S.append( "@error No sequence numbers selected");
      S.append( "@error  No peaks");
      S.append( "@error  Several I/O errors");
      S.append( "@error   ALL REFLECTIONS COPLANAR-PROGRAM TERMINATING \n");
      S.append( "  All the peaks were in one plane or on one line");
      S.append( "@error INITIAL NON-INTEGER INDICES \n");
      S.append( " Cannot get basis where all peaks have integer coefficients");
     
      return S.toString();

    }
    
    /* ----------------------- setDefaultParameters ------------------------- */ 
    /** 
     * Sets default values for the parameters.  This must match the
     * data types of the parameters.
     */
    public void setDefaultParameters(){
      parameters = new Vector();
      int[] intAr= new int[5];
      intAr[0]=30;intAr[1]=31;intAr[2]=32; intAr[3]=40;intAr[4]=42; 
      addParameter( new LoadFilePG("Peak filename","" ) );
      addParameter( new IntArrayPG("Seq nums",intAr ) );
      addParameter( new SaveFilePG("Mat filename","" ));
   
    
    }
    
    /* ----------------------------- getResult ------------------------------ */ 
    /** 
     *  Gets the desired peaks from the input peak file, runs the blind method, then
     *  stores the resultant orientation matrix and other parameters in the given
     *  matrix file.  A blind.log file is also created.
     *
     *  @return If successful, this operator adds the corresponding attributes and 
     *          operator to the data set 
     *  @see IPNSSrc.blind
     */
    public Object getResult(){
      String filename=((LoadFilePG)getParameter(0)).getStringValue();
      int[] seq= DataSetTools.util.IntList.ToArray(
                           ((IntArrayPG)getParameter(1)).getStringValue());
      String savFilename=((SaveFilePG)getParameter(2)).getStringValue();
     
      if( filename == null)
         return new ErrorString("Improper Peak filename");
       if( seq == null)
         return new ErrorString("Improper sequence numbers");
      if( savFilename== null)
          return new ErrorString("Improper save matrix filename");
        
      if( seq.length < 1)
        return new ErrorString(" No sequence numbers selected");
      Vector V=new Vector();
       
      float chi=0.0f,
            phi=0.0f,
            omega=0.0f,
            deta=0.0f,
            detd=0.0f;
     
      TextFileReader fin=null;
      FileOutputStream fout = null;
      try{ 
        fin= new TextFileReader( filename);
        fout = new FileOutputStream( savFilename );
        fin.read_line();
        int nseq= 0; 
        while( (!fin.eof())&&(nseq < seq.length))
          {int kk = fin.read_int();
           if(kk==1)
             {kk=fin.read_int();
              kk=fin.read_int();
              deta= (float)(fin.read_float() );
              detd= (float) (fin.read_float());
              detd= (float)(fin.read_float());
              chi= (float)(fin.read_float());
              phi=(float)( fin.read_float());
              omega=(float) (fin.read_float());

              fin.read_line();
            
             }
           else if( kk==3)
            {
             int seqnum=fin.read_int();
         
             boolean done = seqnum <= seq[nseq];
             while( !done) 
               {nseq ++;
                if(nseq >= seq.length)
                   done = true;
                else
                   done = seqnum <= seq[nseq];
                }
             if( nseq < seq.length)
                if( seqnum== seq[nseq])
                 {
                  float[] dat = new float[9];
                  dat[ 8]  = seqnum;
                  kk = fin.read_int();
                  kk = fin.read_int() ;
                  kk = fin.read_int();
                  float x = fin.read_float();
                  float y = fin.read_float();
                  float z = fin.read_float();
           
                  dat[ 5 ] = fin.read_float(); //xcm
                  dat[ 6 ] = fin.read_float(); //ycm
                  dat[ 7 ] = fin.read_float();//wl
                  dat[ 0 ] = chi; 
                  dat[ 1 ] = phi; 
                  dat[ 2 ] = omega;
                  dat[ 3 ] = deta;
                  dat[ 4]  = detd;
                  fin.read_line();
          
                  V.addElement(dat);
                 }
                else  
                   fin.read_line();  
             else 
                fin.read_line();

           
            }
           else
             fin.read_line();
          }
         } 
      catch( Exception s)
        {return new ErrorString("error="+s);
        
        }

     
      if( V== null) 
         return new ErrorString("No peaks");
      if(V.size()<1)
        return new ErrorString( "No peaks");
      double[] xx,
               yy,
               zz;
      xx = new double[V.size()+3];
      yy = new double[V.size()+3];
      zz = new double[V.size()+3];
      intW LMT = new intW(0);
     
      blind.blaue( V,xx,yy,zz,LMT,seq,1);
      double[] b= new double[9];
      doubleW dd= new doubleW(.08);
      intW mj= new intW(0);
    
      blind.bias(V.size()+3,xx,yy,zz,b,0,3,dd,4.0,mj,seq,1,123,0);

      //Write results to the matrix file
      try{
      DecimalFormat df = new DecimalFormat("##0.000000;#0.000000");
      StringBuffer sb= new StringBuffer(10*3+1);
    
      
      for( int i=0;i<3;i++)
        {for (int j=0;j<3;j++)
            sb.append(format(df.format( blind.u[3*j+i]),10));
         sb.append("\n");
            //fout.write((blind.u[3*j+i]+" ").getBytes());
         fout.write(sb.toString().getBytes());//"\n".getBytes());
         sb.setLength( 0 );
         }
      df = new DecimalFormat("#####0.000;####0.000");
     
      sb.append(format(df.format( blind.D1),10));
      sb.append(format(df.format( blind.D2),10));
      sb.append(format(df.format( blind.D3),10));
      sb.append(format(df.format( blind.D4),10));
      sb.append(format(df.format( blind.D5),10));
      sb.append(format(df.format( blind.D6),10));
      sb.append(format(df.format( blind.cellVol),10));
      //fout.write((blind.D1+" "+blind.D2+" "+blind.D3+" "+blind.D4+" "+
        //                blind.D5+" "+blind.D6+" "+blind.cellVol).getBytes());
      sb.append("\n");
      fout.write( sb.toString().getBytes());
      sb.setLength(0);
      for( int i=0; i < 7; i++)
        sb.append(format(df.format(0.0),10));
      sb.append("\n");
      fout.write( sb.toString().getBytes());
      //fout.write(("\n 0  0  0  0  0  0  0 \n").getBytes());
      fout.close();
         }
      catch( Exception sss)
        {
          return new ErrorString( sss.toString());
        }
      return "Success";
    	
    }
    
    /* ------------------------------- clone -------------------------------- */ 
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){ 
	GenericTOF_SCD op = new Blindd();
	op.CopyParametersFrom( this );
	return op;
    }
    
  static private String format(String rs, int length){
    while(rs.length()<length){
      rs=" "+rs;
    }
    return rs;
  }
    /* ------------------------------- main --------------------------------- */ 
    /** 
     * Standalong program to carry out the operations
     * @param   args[0] The name of the file with peak information
     * @param   args[1] The list of sequence numbers to use. Eg 33:36,47,56
     * @param   args[2] The name of the file to write the orientation matrix
     *
     */
    public static void main( String args[] )
    {
      if( args != null)
        if( args.length >2)
          {
           Blindd bl= new Blindd( args[0], args[1], args[2]);
           System.out.println("Result="+ bl.getResult() );
           System.exit( 0 );
          }
       System.out.println(" This program requires three arguments");
       System.out.println("   Arg 1:The name of the file with peak information");
       System.out.println("   Arg 2:The list of sequence numbers to use. Eg 33:36,47,56");
       System.out.println("   Arg 3:The name of the file to write the orientation matrix");
       System.exit( 0);

    }
}
