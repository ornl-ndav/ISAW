/*
 * File:  Write3ColGSAS.java 
 *
 * Copyright (C) 2004, Alok Chatterjee
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
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
 * Revision 1.2  2005/03/09 23:14:12  dennis
 * Added carriage return and line feed to the original 80 column GSAS
 * output files.  (Alok Chatterjee)
 *
 * Revision 1.1  2005/02/17 22:06:04  dennis
 * Initial version of operator to export files in the GSAS 3Col FXYE format.
 * (Alok Chatterjee)
 * Removed unused imports and two unused variables. (dennis)
 *
 */

package DataSetTools.operator.Generic.Save;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.Util.Numeric.Format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.gsastools.XInfo;
import DataSetTools.operator.Parameter;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.util.SharedData;

import DataSetTools.dataset.*;

import java.util.*;


/** This is an operator to export files in the GSAS 3Col format.
 */
public class Write3ColGSAS extends GenericSave{
    public  static String EXTENSION = "gsa";
    
    private static String TITLE     = "Save as 3Col GSAS file";
    private static String EOL       = "\n";
    
    private OutputStreamWriter outstream;
    private String             run_title;
    private String             run_file;

    DataSet ds = null;
    String month, year;

    public Write3ColGSAS(){
        super( TITLE );
        setDefaultParameters();
    }
    
    /** ------------------------- FULL CONSTRUCTOR ----------------------- */
    /** 
     * @param ds       The data set that is to be saved in 3 Col GSAS format
     * @param filename the name of the file where the data will be saved
     * @param sn       Indicates if the bank numbers are sequentially numbered 	
     */
    public Write3ColGSAS( DataSet mon_ds, DataSet ds, String filename, boolean sn ){
        this();
        parameters = new Vector();
        addParameter( new Parameter("Monitor Data Set" , mon_ds ));
        addParameter( new Parameter("Data Set" , ds ));
        addParameter( new Parameter("Output File", filename ));
        addParameter( new Parameter("Sequential Bank Numbering",
                                    new Boolean(sn)));
    }
    
    public void setDefaultParameters(){
        parameters = new Vector();
        addParameter( new Parameter("Monitor Data Set" , DataSet.EMPTY_DATA_SET ));
        addParameter( new Parameter("Data Set" , DataSet.EMPTY_DATA_SET ));
        addParameter( new Parameter("Output File", ""));
        addParameter( new Parameter("Sequential Bank Numbering",
                                    Boolean.FALSE));
    }  
    
    /** 
     * Returns <B>Save3ColGSAS</b>, the command used by scripts to
     * refer to this operation
     */ 
    public String getCommand(){
        return "Save3ColGSAS";
    }

 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This is an operator to export files in the 3 Col GSAS ");
    s.append("format.");
    s.append("@assumptions The given data set is not empty.\n");
    s.append("The specified filename either does not exist, or it is ");      
    s.append("acceptable to overwrite it.\n");                                                                    
    s.append("@algorithm This operator goes through every data entry in ");
    s.append("the given data set.  For each one it determines a bank number ");
    s.append("and creates a file.  It then makes sure that the file can be ");
    s.append("opened, and that the data and header can be written.  If just ");
    s.append("one entry of data does not meet one of these three ");
    s.append("requirements, then 'Failed' is returned.  Otherwise the files ");
    s.append("are exported in the GSAS format, and 'Success' is ");
    s.append("returned. ");
    s.append("@param ds The data set that is to be saved in GSAS format.");
    s.append("@param filename The name of the file where the data will ");
    s.append("be saved.");
    s.append("@param sn Indicates if the data of the data set are ");
    s.append("sequentially numbered. ");
    s.append("@return A String indicating that the file has been converted ");
    s.append("to the GSAS format.");
    s.append("@error Returns an error any time that opening or closing a ");
    s.append("file does not work properly.");
    s.append("@error Returns an error if the specified DataSet is not found.");
    return s.toString();
  }
                                                                                  
    /** 
     * executes the Save3ColGSAS command, saving the data to the file in 
     * full prof form.
     *
     * @return  "Success" if the file was successfully written and retrun 
     *          "Failed" if the file could not be written.
     */
    public Object getResult()
    {
        DataSet mon_ds   = (DataSet)( getParameter(0).getValue());
        ds       = (DataSet)( getParameter(1).getValue());
        String  filename = (String)(  getParameter(2).getValue());
        boolean sn       = ((Boolean)(getParameter(3).getValue()))
            .booleanValue();
        
        //System.out.println("DS="+ds+" FILE="+filename+" SEQ="+sn);

        // initialize some of the generic information about the data
        this.run_title=(String)ds.getAttributeValue(Attribute.RUN_TITLE);
        this.run_file=(String)ds.getAttributeValue(Attribute.FILE_NAME);
        String x_units=ds.getX_units();

        Data d, mon_data;
        String bankfile;
        int bankNum;

        mon_data = mon_ds.getData_entry(1);
       if(this.openFile(filename))
       {   
            int banknum = 1;     
         for( int i=0 ; i<ds.getNum_entries() ; i++ )
         {

            d=ds.getData_entry(i);
            if(sn)
            {
                bankNum=i+1;
            }
             else
            {
                bankNum=((Integer)
                         d.getAttributeValue(Attribute.GROUP_ID)).intValue();
            }
            int index=filename.lastIndexOf(".");
            if(index>0)
            {
                bankfile=filename.substring(0,index)+
                         "b"+bankNum+filename.substring(index);
            }
             else
            {
                bankfile=filename+"b"+bankNum;
            }
               
               this.writeHeader(mon_data, d, bankNum, x_units, banknum );
                banknum++;

               this.writeData(d);
            }
          } 
         closeFile();
           return "Success";
     }

    
    private boolean openFile(String filename)
    {
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
        d.setSqrtErrors(true);
        float[] dy = d.getCopyOfErrors();

         StringBuffer data=new StringBuffer(80*y.length);


        // compile the data array
        for( int i=0 ; i<y.length ; i++ ){
            data.append("  "+Format.real( x[i],10,4));
            data.append("  "+Format.real( y[i],10,4));
            data.append("  "+Format.real(dy[i],10,4));
            data.append("                                            ");
            data.append("\r"+"\n");
        }

        // write out the data
        try{
            this.outstream.write(data.toString());
        }catch( IOException e ){
            SharedData.addmsg("IOException: "+e.getMessage());
            return false;
        }
        return true;
    }

      private boolean writeHeader(Data mon_data, Data d, int bankLabel, String units, int banknum){

         int startline=1, endline=5; // must have 6 lines


        // set up a string buffer to allow faster writting
        StringBuffer header=new StringBuffer(80*6);

        // add the run title if defined
        if( this.run_title!=null && this.run_title.length()>0 && banknum==1){
            header.append( Format.string(this.run_title,80,false) +"\r"+"\n");
            startline++;
        }

        // add the monitor count
        if( this.run_file!=null && this.run_file.length()>0 && banknum==1 ){

          	Float count = (Float)mon_data.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
            header.append( Format.string("# Monitor: "+count,80,false) +"\r"+"\n");
            startline++;
        }
 
        // add the run file name if defined
        if( this.run_file!=null && this.run_file.length()>0 && banknum==1){
            header.append( Format.string("# Original data file: "+this.run_file,80,false) +"\r"+"\n");
            startline++;
        }


        startline++;

        // put in information about where the bank is located
        DetectorPosition pos=
            (DetectorPosition)d.getAttributeValue(Attribute.DETECTOR_POS);
        float initial_path=
            ((Float)d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
        if( pos!=null ){
            float angle=180f*pos.getScatteringAngle()/(float)Math.PI;
            float flight_path=initial_path+(pos.getSphericalCoords())[0];
           if (banknum==1) 
           { 
            header.append( Format.string("# Scattering_angle= "+Format.real(angle,6,2)+" deg"+ " Total_length= "+Format.real(flight_path,5,2)+" m",80,false) +"\r"+"\n");

           }
        // add spacing lines if necessary
 /*  if(banknum==1){
        for( int i=startline ; i<=endline ; i++ ){
            header.append( Format.string("#LINE "+i,80,false) +"\n");
        }
       }
 */


        if(banknum==1){
          String end = "End Date";
          String inst = "Instrument Name";
          Object O = ds.getAttributeValue( end);
  //System.out.println("End date O" +O.toString());
          Object OO = ds.getAttributeValue( inst );
          int date = getEndDate(O.toString());
          int tag ;
          if (Integer.parseInt(month) <= 6) 
             tag = 1 ;
          else 
             tag = 2;
            header.append( Format.string("# Vanadium run: "+OO+year+'_'+tag+".van"+"    "+"Parameter run: "+OO+year+'_'+tag+".prm" ,80,false) +"\r"+"\n");
        
       }
        // add the GSAS bank header line
        XScale xscale = d.getX_scale();
        XInfo info=new XInfo(xscale,units,"FXYE");//GsasUtil.getType(d));
        header.append( Format.string("BANK "+bankLabel+" "+info.toString(),80,false) +"\r"+"\n");
        startline++;
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

    //**************************************
          private int getEndDate(String end_date)

  //      private int getEndDate(RunFile runFile)
     {
        //The date as retrieved by the runfile takes the format
        //day-month-year, where day and year are two digit numbers 
        //and month is a three letter string representing the year
        // for instance 'JAN' Examples 1-JAN-98 or 04-JUL-96 or 14-MAY-95
        StringTokenizer st = null;
        try{
       // st = new StringTokenizer(runFile.getEndDate(), "-");
        st = new StringTokenizer(end_date, "-");
        } catch(Exception e){}
        String day = st.nextToken(); //Debug: System.out.println("day = " + day);
         month = st.nextToken();
         year = st.nextToken();
        day = day.trim(); month = month.trim(); year = year.trim();
        //int dayInt = 1; int monthInt = 1 ;  int yearInt = 2000;
        //put day into proper format.
        //That is if day is "5" set day = "05". If day is "16" leave it alone.
   //     System.out.println("length of day = " + day.length());
        if(day.length() == 1) day = "0" + day; 
        else if(day.length() != 1) { /*do nothing*/ }
        // debug: System.out.println("day = " + day);
        //now put month into the proper format. 
        if(month.equalsIgnoreCase("JAN")) month = "01";
        else if(month.equalsIgnoreCase("FEB")) month = "02";
        else if(month.equalsIgnoreCase("MAR")) month = "03";
        else if(month.equalsIgnoreCase("APR")) month = "04";
        else if(month.equalsIgnoreCase("MAY")) month = "05";
        else if(month.equalsIgnoreCase("JUN")) month = "06";
        else if(month.equalsIgnoreCase("JUL")) month = "07";
        else if(month.equalsIgnoreCase("AUG")) month = "08";
        else if(month.equalsIgnoreCase("SEP")) month = "09";
        else if(month.equalsIgnoreCase("OCT")) month = "10";
        else if(month.equalsIgnoreCase("NOV")) month = "11";
        else if(month.equalsIgnoreCase("DEC")) month = "12";
        //Now put year into correct format. 
        int temp = Integer.parseInt(year);
        if(temp<70) year = "20" + year; //eg 2013
        else year = "19" + year; //eg 1999
        //Now aggregate the year, month, day(in that order) into one string
        String yearMonthDay = year + month + day;
            //    System.out.println("yearMonthDay"+yearMonthDay);
        return Integer.parseInt(yearMonthDay);

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
          //  Operator op=new Write3ColGSAS(mon_ds,ds,outfile,false);
          //  System.out.println("RETURNED: "+op.getResult());
        }else{
            System.out.println("USAGE: Write3ColGSAS <infile> [outfile]");
        }
        System.exit(0);
    }
  
}
