/*
 * File:  LoadASCII.java 
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
 * Revision 1.3  2002/12/20 17:53:08  dennis
 * Added getDocumentation() method. (Chris Bouzek)
 *
 * Revision 1.2  2002/11/27 23:21:16  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/09/27 19:10:14  pfpeterson
 * Added to CVS.
 *
 *
 */
package DataSetTools.operator.Generic.Load;

import DataSetTools.operator.*;
import DataSetTools.util.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import java.util.*;
import java.io.*;

/** 
 *  This operator provides a means to load an N-column ascii data file
 *  into ISAW.
 */

public class LoadASCII extends GenericLoad{
  private static final String TITLE = "Load N-column ASCII file";

    /** 
     * Creates operator with title "Load N-column ASCII file" and a
     * default list of parameters.
     */  
    public LoadASCII(){
        super( TITLE );
    }

    /** 
     * Creates operator with title "Load N-column ASCII file" and the
     * specified list of parameters. The getResult method must still
     * be used to execute the operator.
     *  
     * @param file_name The fully qualified ASCII file name.
     * @param num_head The number of header lines that should be
     * skipped while reading in the file
     * @param num_data The number of data lines to read in. If set to
     * zero all lines until the end of file are read.
     * @param xcol The column number (1-indexed) of the x-values
     * @param ycol The column number (1-indexed) of the y-values
     * @param dycol The column number (1-indexed) of the dy-values. If
     * this is set to zero then the data will have no errors
     */
    public LoadASCII( String file_name, int num_head, int num_data,
                      int xcol, int ycol, int dycol ){
        this();
        parameters = new Vector();
        addParameter(new Parameter("Filename", new LoadFileString(file_name)));
        addParameter( new Parameter("# header lines",new Integer(num_head)));
        addParameter( new Parameter("# data lines",new Integer(num_data)));
        addParameter( new Parameter("column of x-values",new Integer(xcol)));
        addParameter( new Parameter("column of y-values",new Integer(ycol)));
        addParameter( new Parameter("column of errors",new Integer(dycol)));
    }
    
    /** 
     * Get the name of this operator, used in scripts
     * 
     * @return "LoadASCII", the command used to invoke this operator
     * in Scripts
     */
    public String getCommand(){
        return "LoadASCII";
    }
    
    /** 
     * Sets default values for the parameters. The parameters set must
     * match the data types of the parameters used in the constructor.
     */
    public void setDefaultParameters(){
        parameters = new Vector();
        addParameter( new Parameter("Filename",new LoadFileString()));
        addParameter( new Parameter("# header lines",new Integer(0)));
        addParameter( new Parameter("# data lines",new Integer(0)));
        addParameter( new Parameter("column of x-values",new Integer(1)));
        addParameter( new Parameter("column of y-values",new Integer(2)));
        addParameter( new Parameter("column of errors",new Integer(0)));
    }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
   public String getDocumentation()
   {
     StringBuffer s = new StringBuffer("");
     s.append("@overview This operator provides a means to load an ");
     s.append("N-column ASCII data file into ISAW.");
     s.append("@assumptions The file exists and it contains valid ");
     s.append("histogram data.");
     s.append("@algorithm Uses the ASCII data in the file to create ");
     s.append("a histogram.  It then populates a new DataSet with that ");
     s.append("histogram.  The new DataSet has unknown values for the X-axis ");
     s.append("scale, the Y-axis scale, the X-axis label, and the Y-axis ");
     s.append("label.  Furthermore, a message is appended to the DataSet's ");
     s.append("log indicating that the ASCII file was loaded.");
     s.append("@param file_name The fully qualified ASCII file name.");
     s.append("@param num_head The number of header lines that should be ");
     s.append("skipped while reading in the file.");
     s.append("@param num_data The number of data lines to read in. If set ");
     s.append("to zero all lines until the end of file are read.");
     s.append("@param xcol The column number (1-indexed) of the x-values.");
     s.append("@param ycol The column number (1-indexed) of the y-values.");
     s.append("@param dycol The column number (1-indexed) of the ");
     s.append("dy-values.  If this is set to zero then the data will have ");
     s.append("no errors.");
     s.append("@return A new DataSet with the histogram that was read ");
     s.append("from the data file.");
     s.append("@error Returns an ErrorString if the file does not exist.");
     s.append("@error Returns an ErrorString if the file cannot be read.");
     s.append("@error Returns an ErrorString with the String value of the ");
     s.append("Exception if an input/output error occurs when reading the ");
     s.append("file.");
     return s.toString();
    }


    /** 
     *  Creates a new DataSet using the ASCII information in the file to
     *  form histogram data and populate the DataSet.
     *
     *  @return If successful, this returns a new DataSet with the
     *  histogram that was read from the data file.
     */
    public Object getResult(){
        DataSet ds = null;                      
        String  filename = getParameter(0).getValue().toString();
        int     num_head = ((Integer)getParameter(1).getValue()).intValue();
        int     num_data = ((Integer)getParameter(2).getValue()).intValue();
        int     xcol     = ((Integer)getParameter(3).getValue()).intValue();
        int     ycol     = ((Integer)getParameter(4).getValue()).intValue();
        int     dycol    = ((Integer)getParameter(5).getValue()).intValue();

        //System.out.println(filename+" "+num_head+" "+num_data+
        //                   " ("+xcol+","+ycol+","+dycol+")");
        
        File file=new File(filename);
        if(! file.exists() )
            return new ErrorString(filename+" does not exist");
        if(! file.canRead() )
            return new ErrorString(filename+" cannot be read");

        String title   = filename;
        String x_units = "unknown";
        String x_label = "unknown";
        String y_units = "unknown";
        String y_label = "unknown";

        StringAttribute fileattr=
            new StringAttribute(Attribute.FILE_NAME,filename);
        
        Vector x_vec  = new Vector();
        Vector y_vec  = new Vector();
        Vector dy_vec = new Vector();

        int maxcol=xcol;
        if(ycol>maxcol) maxcol=ycol;
        if(dycol>maxcol) maxcol=dycol;

        String line;
        StringBuffer sb;
        float tempf;
        TextFileReader tfr=null;
        try{
            tfr = new TextFileReader( filename );
            
            // skip the header lines
            for( int i=0 ; i<num_head ; i++ ){
                line=tfr.read_line();
                System.out.println("head"+i+": "+line);
            }

            
            if(num_data>0){  // read in the number of lines specified
                for( int i=0 ; i<num_data ; i++ ){
                    line=tfr.read_line();
                    sb=new StringBuffer(line.trim());
                    for( int j=0 ; j<maxcol ; j++ ){
                        tempf=StringUtil.getFloat(sb);
                        if(j+1==xcol){
                            x_vec.add(new Float(tempf));
                        }else if(j+1==ycol){
                            y_vec.add(new Float(tempf));
                        }else if(j+1==dycol){
                            dy_vec.add(new Float(tempf));
                        }
                    }
                }
                
            }else{  // read in until end of file
                while(! tfr.eof()){
                    line=tfr.read_line();
                    sb=new StringBuffer(line.trim());
                    for( int j=0 ; j<maxcol ; j++ ){
                        tempf=StringUtil.getFloat(sb);
                        if(j+1==xcol){
                            x_vec.add(new Float(tempf));
                        }else if(j+1==ycol){
                            y_vec.add(new Float(tempf));
                        }else if(j+1==dycol){
                            dy_vec.add(new Float(tempf));
                        }
                    }
                }
            }
        }catch ( IOException e ){
            // if there is an error than just give up
            if(tfr!=null) try{
                tfr.close();
            }catch(IOException e2){
                // let it drop on the floor
            }
            return new ErrorString( e.toString() );
        } 

        // don't forget to close the file
        if(tfr!=null) try{
            tfr.close();
        }catch(IOException e){
            // let it drop on the floor
        }

        // convert the vector of values to arrays
        float[] x  = new float[x_vec.size()];
        float[] y  = new float[y_vec.size()];
        float[] dy = new float[dy_vec.size()];
        for( int i=0 ; i<x.length ; i++ ){
            x[i]=((Float)x_vec.elementAt(i)).floatValue();
            y[i]=((Float)y_vec.elementAt(i)).floatValue();
            if(dycol>0){
                dy[i]=((Float)dy_vec.elementAt(i)).floatValue();
            }
        }

        // Using a DataSetFactory to build the DataSet will give the
        // DataSet a set of operators.
        DataSetFactory ds_factory = 
            new DataSetFactory("Sample",x_units,x_label,y_units,y_label);
        ds = ds_factory.getDataSet();
        ds.setAttribute(fileattr);
        
        // And create a spectrum from the data
        Data d=null;
        if(dycol>0){
            d = Data.getInstance( new VariableXScale(x), y, dy, 1 );
        }else{
            d = Data.getInstance( new VariableXScale(x), y, 1 );
        }
        d.setAttribute(fileattr);
        ds.addData_entry( d );

        // The file was loaded ok, so now add a log entry and return
        // the new DataSet.
        ds.addLog_entry("Loaded File " + filename );
        return ds;
    }

    /** 
     *  Creates a clone of this operator.  ( Operators need a clone
     *  method, so that Isaw can make copies of them when needed. )
     */
    public Object clone(){
        Operator op = new LoadASCII();
        op.CopyParametersFrom( this );
        return op;
    }

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    LoadASCII op = new LoadASCII();
    op.setDefaultParameters();
    System.out.println("\nCalling getResult():\n");
    System.out.println(op.getResult() + "\n");
    System.out.println("Calling getDocumentation():\n");
    System.out.println(op.getDocumentation());
  }

}
