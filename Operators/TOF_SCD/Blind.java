/*
 * File:  Blind.java   
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
 *           Argonne, IL 60439-4845
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.6  2002/10/14 16:49:46  pfpeterson
 *  Added some more comments, made ErrorStrings slightly more useful,
 *  and hardwired that the output file will be 'blind.mat'.
 *
 *  Revision 1.5  2002/10/07 18:41:59  pfpeterson
 *  Made getResult() more windows friendly.
 *
 *  Revision 1.4  2002/10/02 21:59:30  pfpeterson
 *  Fixed bug where it wouldn't try to execute on windows machines.
 *
 *  Revision 1.3  2002/09/30 20:17:41  pfpeterson
 *  Added support for windows executable.
 *
 *  Revision 1.2  2002/09/30 14:45:50  pfpeterson
 *  Changed the unsuccesful returns to be ErrorString and the
 *  successful return to be the name of the saved matrix file.
 *
 *  Revision 1.1  2002/09/17 22:31:55  pfpeterson
 *  Added to CVS.
 *
 *
 *   
 */

package Operators.TOF_SCD;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.operator.Generic.TOF_SCD.*;

/**
 * This operator is intended to run A.J. Schultz's "blind"
 * program. This is not heavily tested but works fairly well.
 */
public class Blind extends    GenericTOF_SCD {
    /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
    /**
     * Construct an operator with a default parameter list.
     */
    public Blind( ){
        super( "Blind" );
    }

    /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
    /**
     *  Construct operator to execute blind
     *
     *  @param file     The peaks file to use with blind
     *  @param seq_nums The sequence numbers of peaks to use
     */
    
    public Blind( LoadFileString file, IntListString seq_nums ){
        this();
        
        parameters=new Vector();
        addParameter(new Parameter("Peaks File",file));
        addParameter(new Parameter("Sequence Numbers",seq_nums));
    }

    
    /* ------------------------- setDefaultParmeters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        parameters = new Vector();  // must do this to create empty list of 
                                    // parameters

        parameters=new Vector();
        addParameter( new Parameter("Peaks File",new LoadFileString("")) );
        addParameter( new Parameter("Sequence Numbers",new IntListString("")) );
    }
    
    /* --------------------------- getCommand ------------------------------ */
    /**
     * @return the command name to be used with script processor, in
     * this case Blind.
     */
    public String getCommand(){
        return "Blind";
    }
    
    /* --------------------------- getResult ------------------------------- */
    /*
     * Runs blind.
     */
    public Object getResult(){
        String peaksfile = (getParameter(0).getValue()).toString();
        String seq_nums  = (getParameter(1).getValue()).toString();
        int index;
        String direc;
        String matfile   = "blind.mat";
        int seqs[]       = IntList.ToArray(seq_nums);
        String fail      = "FAILED";

        // first check if the OS is acceptable
        if(! SysUtil.isOSokay(SysUtil.LINUX_WINDOWS) )
            return new ErrorString(fail+": must be using linux or windows "+
                                   "system");

        // confirm that the name of the peaksfile is a non-null string
        if( peaksfile==null || peaksfile.length()==0 )
            return new ErrorString(fail+": must specify a peaks file");

        // standardize the peaks filename
        peaksfile=FilenameUtil.fixSeparator(peaksfile);

        // then confirm the peaks file exists
        if(! SysUtil.fileExists(peaksfile) )
            return new ErrorString(fail+": peaks file does not exist");

        // find out the file directory
        index=peaksfile.lastIndexOf("/");
        if(index>0){
            direc=peaksfile.substring(0,index);
        }else{
            return new ErrorString(fail+": directory not found");
        }
        peaksfile=peaksfile.substring(index+1);

        // confirm that the directory is writable
        File dirF=new File(direc);
        if(! dirF.canWrite() )
            return new ErrorString(fail+": cannot write to specified directory "
                                   +dirF);

        // strip the end off of the peaks filename
        index=peaksfile.lastIndexOf(".peaks");
        if(index>0){
            peaksfile=peaksfile.substring(0,index);
        }

        // declare some things
        Process process=null;
        String output=null;
        File dir=new File(direc);
        String command=this.getFullBlindName();

        // exit out early if no blind executable found
        if(command==null)
            return new ErrorString(fail+": could not find blind executable");

        try{
            process=SysUtil.startProcess(command,direc);
            BufferedReader in=SysUtil.getSTDINreader(process);
            BufferedWriter out=SysUtil.getSTDOUTwriter(process);

            // skip over the first couple of lines
            SysUtil.jumpline(in,"LAUE INDEXER");

            // We are going to use a peaks file
            output=SysUtil.readline(in);
            while( output==null || output.indexOf("Input reflection from")<0 ){
                if( output!=null && output.length()>0){
                    System.out.println(output);
                }
                output=SysUtil.readline(in);
            }
            SysUtil.writeline(out,"y");
            System.out.println(output+"y");
              
            // enter the name of the peaks file
            output=SysUtil.readline(in);
            while( output==null || output.indexOf("Experiment name")<0 ){
                if(output!=null) System.out.print(output);
                output=SysUtil.readline(in);
            }
            SysUtil.writeline(out,peaksfile);
            System.out.println(output+peaksfile);

            // enter the reflections
            for(int i=0 ; i<seqs.length ; i++ ){
                output=SysUtil.readline(in);
                SysUtil.writeline(out,Integer.toString(seqs[i]));
                System.out.println(output+seqs[i]);
            }
            output=SysUtil.readline(in);
            SysUtil.writeline(out,"");
            System.out.println(output);
            
            // print out all the other information give from program
            output=SysUtil.readline(in);
            while( output==null || (output.indexOf("STORE THE MATRIX")<0
                                 && output.indexOf("PROGRAM TERMINATING")<0)){
                if(output!=null) System.out.println(output);
                output=SysUtil.readline(in);
            }
            if(output.indexOf("TERMINATING")>0){
                while( output==null || output.indexOf("D=")<0 ){
                    if(output!=null) System.out.println(output);
                    output=SysUtil.readline(in) ;
                }
                System.out.println(output);
                
                return new ErrorString(fail);
            }

            // save to a matrix file
            SysUtil.writeline(out,"y");
            System.out.println(output+"y");
            output=SysUtil.readline(in);
            SysUtil.writeline(out,"1");      // must choose (1) since experiment
            System.out.println(output+"1");  // file does not exist
            output=SysUtil.readline(in);
            SysUtil.writeline(out,matfile);
            System.out.println(output+matfile);
            
            // keep writing out information until the last line
            output=SysUtil.readline(in);
            SysUtil.jumpline(in,"To analyze the cell");

            // wait for the process to terminate
            process.waitFor();
        }catch(IOException e){
            SharedData.addmsg("IOException reported: "+e.getMessage());
        }catch(InterruptedException e){
            SharedData.addmsg("InterruptedException reported: "+e.getMessage());
        }finally{
            if(process!=null){
                if(process.exitValue()!=0){
                    return new ErrorString(fail+"("+process.exitValue()+")");
                }else{
                    return direc+'/'+matfile;
                }
            }else{
                return new ErrorString(fail);
            }
        }
    }  

    /* ------------------------------ clone ------------------------------- */
    /**
     * Get a copy of the current SpectrometerEvaluator Operator.  The
     * list of parameters is also copied.
     */

    public Object clone(){
        Blind new_op = 
            new Blind( );
        
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }

    /* ------------------------------ PRIVATE METHODS -------------------- */
    /**
     * Method to get the location of the blind executable. Assumed to
     * be right next to the class file.
     */
    private String getFullBlindName(){
        if(SysUtil.isOSokay(SysUtil.LINUX_ONLY)){
            return SysUtil.getBinLocation(this.getClass(),"blind");
        }else if(SysUtil.isOSokay(SysUtil.WINDOWS_ONLY)){
            return SysUtil.getBinLocation(this.getClass(),"blind.exe");
        }else{
            return null;
        }
    }

    /* --------------------------- MAIN METHOD --------------------------- */
    public static void main(String[] args){
        LoadFileString file=new LoadFileString("/IPNShome/pfpeterson/ISAW/Operators/TOF_SCD/quartz_isaw.peaks");
        IntListString seq_nums=new IntListString("1:5");
        
        Blind op;

        op=new Blind(file,seq_nums);
        System.out.println("RESULT: "+op.getResult());
        System.exit(0);
    }
}
