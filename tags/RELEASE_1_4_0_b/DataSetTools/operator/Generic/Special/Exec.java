/*
 * File:  Exec.java   
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
 *  Revision 1.4  2002/07/29 18:22:56  pfpeterson
 *  Started working out the details of calling code interactively.
 *
 *  Revision 1.3  2002/07/29 16:35:09  pfpeterson
 *  Now redirects stderr from the process to the console as well.
 *
 *  Revision 1.2  2002/07/29 16:00:07  pfpeterson
 *  Fixed two bugs:
 *   - Confirms that process was started before returning.
 *   - Stops printing once encounters a null string.
 *
 *  Revision 1.1  2002/07/26 22:45:32  pfpeterson
 *  Added to CVS.
 *
 *   
 */

package DataSetTools.operator.Generic.Special;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.retriever.RunfileRetriever;

/**
 * This operator makes system calls. It does grab the process' STDOUT
 * stream and prints it to screen, otherwise it blindly calls the
 * method and returns the result (an integer). The method called is
 * assumed to be run without interaction. If it requires interaction
 * ISAW WILL HANG.
 */
public class Exec extends    GenericSpecial {
    /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
    /**
     * Construct an operator with a default parameter list.
     */
    public Exec( ){
        super( "Execute" );
    }

    /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
    /**
     *  Construct operator to execute a system command.
     *
     *  @param  command  The command to be executed.
     */
    
    public Exec( String command ){
        this();
        
        parameters=new Vector();
        addParameter(new Parameter("Command",command));
    }

    
    /* ------------------------- setDefaultParmeters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        parameters = new Vector();  // must do this to create empty list of 
                                    // parameters

        parameters=new Vector();
        addParameter( new Parameter("Command",""));
    }
    
    /* --------------------------- getCommand ------------------------------ */
    /**
     * @return	the command name to be used with script processor.
     */
    public String getCommand(){
        return "Exec";
    }
    
    /* --------------------------- getResult ------------------------------- */
    /*
     * This returns the group id of the detector with the largest
     * TOTAL_COUNT attribute. If no upstream monitor is found it will
     * return -1.
     */
    public Object getResult(){
        String command=(String)(getParameter(0).getValue());
        Process process=null;
        String output=null;
        String error=null;
        String input=null;
        boolean interactive=false; // start of interactive coding

        try{
            process=Runtime.getRuntime().exec(command);
            InputStream  in_stream  = process.getInputStream();
            OutputStream out_stream = process.getOutputStream();
            InputStream  err_stream = process.getErrorStream();
            InputStreamReader  in_reader  = new InputStreamReader(in_stream);
            OutputStreamWriter out_write  = new OutputStreamWriter(out_stream);
            InputStreamReader  err_reader = new InputStreamReader(err_stream);
            InputStreamReader  sys_reader = new InputStreamReader(System.in);
            BufferedReader in  = new BufferedReader(in_reader);
            BufferedWriter out = new BufferedWriter(out_write);
            BufferedReader err = new BufferedReader(err_reader);
            BufferedReader sys = new BufferedReader(sys_reader);
            while(true){
                output=in.readLine();
                error=err.readLine();
                if( output==null && error==null ){
                    if(!interactive)break;
                    input=sys.readLine();
                    if(input==null || input.length()<=0)
                        break;
                }else{
                    if(input!=null){
                        System.out.println("IN:"+input);
                        out.write(input,0,input.length());
                        out.newLine();
                        out.flush();
                    }
                    if(error!=null)
                        System.err.println(error);
                    if(output!=null)
                        System.out.println(output);
                }
            }
            process.waitFor();
        }catch(IOException e){
            SharedData.addmsg("IOException reported: "+e.getMessage());
        }catch(InterruptedException e){
            SharedData.addmsg("InterruptedException reported: "+e.getMessage());
        }finally{
            if(process!=null)
                return new Integer(process.exitValue());
            else
                return new Integer(-1);
        }
    }  


    /* ------------------------------ clone ------------------------------- */
    /**
     * Get a copy of the current SpectrometerEvaluator Operator.  The
     * list of parameters is also copied.
     */

    public Object clone(){
        Exec new_op = 
            new Exec( );
        
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }

    public static void main(String[] args){
        String command="echo hi there";
        if(args.length==1) command=args[0];
        
        Exec op;

        op=new Exec(command);
        System.out.println("RESULT: "+op.getResult());
        op=new Exec("ls");
        System.out.println("RESULT: "+op.getResult());
    }
}
