/*
 * File:  WriteFullProf.java 
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Modified:
 *
 * $Log$
 * Revision 1.2  2002/11/27 23:21:28  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/07/25 16:53:03  pfpeterson
 * Added to CVS.
 *
 *
 */

package DataSetTools.operator.Generic.Save;

import DataSetTools.dataset.*;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.math.*;
import DataSetTools.gsastools.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;
import DataSetTools.writer.*;
import java.util.*;
import java.io.*;

/** This is an operator to export files in the FullProf format as
 * determined from conversations with Jason Hodges.
 */
public class WriteFullProf extends GenericSave{
    public  static String EXTENSION = "fp";
    
    private static String TITLE     = "Save as FullProf file";
    private static String EOL       = "\n";
    
    private OutputStreamWriter outstream;
    private String             run_title;
    private String             run_file;
    
    public WriteFullProf(){
        super( TITLE );
        setDefaultParameters();
    }
    
    /** 
     *@param DS  The data set that is to be saved in gsas format
     *@param filename the name of the file where the data will be saved
     */
    public WriteFullProf( DataSet ds, String filename, boolean sn ){
        this();
        parameters = new Vector();
        addParameter( new Parameter("Data Set" , ds ));
        addParameter( new Parameter("Output File", filename ));
        addParameter( new Parameter("Sequential Bank Numbering",
                                    new Boolean(sn)));
    }
    
    public void setDefaultParameters(){
        parameters = new Vector();
        addParameter( new Parameter("Data Set" , DataSet.EMPTY_DATA_SET ));
        addParameter( new Parameter("Output File", ""));
        addParameter( new Parameter("Sequential Bank Numbering",
                                    Boolean.FALSE));
    }  
    
    /** 
     * Returns <B>SaveFullProf</b>, the command used by scripts to
     * refer to this operation
     */ 
    public String getCommand(){
        return "SaveFullProf";
    }
    
    /** 
     * executes the gsas command, saving the data to the file in gsas form.
     *
     * @return  "Success" only
     */
    public Object getResult(){
        DataSet ds       = (DataSet)( getParameter(0).getValue());
        String  filename = (String)(  getParameter(1).getValue());
        boolean sn       = ((Boolean)(getParameter(2).getValue()))
            .booleanValue();
        
        //System.out.println("DS="+ds+" FILE="+filename+" SEQ="+sn);

        // initialize some of the generic information about the data
        this.run_title=(String)ds.getAttributeValue(Attribute.RUN_TITLE);
        this.run_file=(String)ds.getAttributeValue(Attribute.FILE_NAME);


        Data d;
        String bankfile;
        int bankNum;
        String x_units=ds.getX_units();

        for( int i=0 ; i<ds.getNum_entries() ; i++ ){
            d=ds.getData_entry(i);
            if(sn){
                bankNum=i+1;
            }else{
                bankNum=((Integer)
                         d.getAttributeValue(Attribute.GROUP_ID)).intValue();
            }
            int index=filename.lastIndexOf(".");
            if(index>0){
                bankfile=filename.substring(0,index)+"b"+bankNum+filename.substring(index);
            }else{
                bankfile=filename+"b"+bankNum;
            }
            if(this.openFile(bankfile)){
                if(this.writeHeader(d,bankNum,x_units)){
                    if(!this.writeData(d)){
                        return "Failed";
                    }
                }else{
                    return "Failed";
                }
                this.closeFile();
            }else{
                return "Failed";
            }
        }
        return "Success";
    }
    
    private boolean openFile(String filename){
        File f=new File(filename);
        try{
            FileOutputStream op=new FileOutputStream(f);
            this.outstream=new OutputStreamWriter(op);
        }catch( FileNotFoundException e ){
            SharedData.addmsg("File not Found: "+e.getMessage());
            return false;
        }
        
        return true;
    }
    
    private void closeFile(){
        try{
            this.outstream.flush();
            this.outstream.close();
        }catch( IOException e ){
            SharedData.addmsg("IOException: "+e.getMessage());
        }
    }
    
    private boolean writeData(Data d){
        
        float[] x  = d.getX_values();
        float[] y  = d.getCopyOfY_values();
        float[] dy = d.getCopyOfErrors();
        
        StringBuffer data=new StringBuffer(36*y.length);

        // compile the data array
        for( int i=0 ; i<y.length ; i++ ){
            data.append("  "+Format.real( x[i],10,4));
            data.append("  "+Format.real( y[i],10,4));
            data.append("  "+Format.real(dy[i],10,4));
            data.append(EOL);
        }

        // add last bin boundaray
        /*if(x.length>y.length){
          data.append("   "+x[x.length-1]);
          }*/

        // write out the data
        try{
            this.outstream.write(data.toString());
        }catch( IOException e ){
            SharedData.addmsg("IOException: "+e.getMessage());
            return false;
        }
        return true;
    }

    private boolean writeHeader(Data d, int bankLabel, String units){
        int startline=1, endline=6; // must have 6 lines of header

        // set up a string buffer to allow faster writting
        StringBuffer header=new StringBuffer(20*6);

        // add the run title if defined
        if( this.run_title!=null && this.run_title.length()>0 ){
            header.append(this.run_title+EOL);
            startline++;
        }

        // add the run file name if defined
        if( this.run_file!=null && this.run_file.length()>0 ){
            header.append("Original data file: "+this.run_file+EOL);
            startline++;
        }

        // add the GSAS bank header line
        XInfo info=new XInfo(d.getX_scale(),units,GsasUtil.getType(d));
        header.append("BANK "+bankLabel+" "+info+EOL);
        startline++;

        // put in information about where the bank is located
        DetectorPosition pos=
            (DetectorPosition)d.getAttributeValue(Attribute.DETECTOR_POS);
        float initial_path=
            ((Float)d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
        if( pos!=null ){
            float angle=180f*pos.getScatteringAngle()/(float)Math.PI;
            float flight_path=initial_path+(pos.getSphericalCoords())[0];
            header.append("scattering_angle= "+Format.real(angle,6,2)+" deg");
            header.append("    total_length= "+Format.real(flight_path,5,2)
                          +" m"+EOL);
            startline++;
        }

        // add spacing lines if necessary
        for( int i=startline ; i<=endline ; i++ ){
            header.append("LINE "+i+EOL);
        }

        // write out the information
        try{
            this.outstream.write(header.toString());
        }catch( IOException e ){
            SharedData.addmsg("IOException: "+e.getMessage());
            return false;
        }

        return true;
    }
    
    /** 
     * Creates a clone of this operator.
     */
    public Object clone(){
        WriteFullProf W = new WriteFullProf();
        W.CopyParametersFrom( this );
        return W;
    }
    
    public static void main( String args[]){
        if(args.length==1 || args.length==2){
            String infile=args[0];
            String outfile=null;
            if(args.length==2){
                outfile=args[1];
            }else{
                int index=infile.lastIndexOf(".");
                if(index>0){
                    outfile=infile.substring(0,index+1)+EXTENSION;
                }else{
                    outfile=infile+"."+EXTENSION;
                }
                if(outfile.startsWith("SEPD")){
                    outfile="sepd"+outfile.substring(4);
                }

            }
            System.out.println(infile+" -> "+outfile);
            RunfileRetriever rr=new RunfileRetriever(infile);
            DataSet ds=rr.getDataSet(1);
            Operator op=new WriteFullProf(ds,outfile,false);
            System.out.println("RETURNED: "+op.getResult());
        }else{
            System.out.println("USAGE: WriteFullProf <infile> [outfile]");
        }
        System.exit(0);
    }
  
}
