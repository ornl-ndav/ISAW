/*
 * File:  LoadOffsets.java
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
 *           Intense Pulsed Neutron Source
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
 *  Revision 1.1  2002/04/02 23:02:00  pfpeterson
 *  Added to CVS.
 *
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.retriever.RunfileRetriever;

/**
 * This operator loads a set of offsets from a simple two-column ascii
 * file. The first column is group id (integer) and the second column
 * is a time offset in microseconds (float).
 */

public class LoadOffsets extends    DS_Attribute {
    /* ---------------------- DEFAULT CONSTRUCTOR ------------------------ */
    /**
     * Construct an operator with a default parameter list.  If this
     * constructor is used, the operator must be subsequently added to
     * the list of operators of a particular DataSet.  Also,
     * meaningful values for the parameters should be set ( using a
     * GUI ) before calling getResult() to apply the operator to the
     * DataSet this operator was added to.
     */

    public LoadOffsets( ){
        super( "Load Time Offsets" );
    }
    
  
    /* -------------------- FULL CONSTRUCTOR -------------------------- */
    /**
     *  Construct an operator for a specified DataSet and with the
     *  specified parameter values so that the operation can be
     *  invoked immediately by calling getResult().
     *
     *  @param  ds          The DataSet to which the operation is applied
     *  @param  offsets     The name of the offsets file.
     */

    public LoadOffsets( DataSet ds, LoadFileString  offsets ){
        this();                       // do the default constructor, then set
                                      // the parameter value(s) by altering a
                                      // reference to each of the parameters

        parameters=new Vector();
        addParameter( new Parameter("Offsets File", offsets) );
        
        //Parameter parameter = getParameter( 0 );
        //parameter.setValue( offsets);
        
        setDataSet( ds );         // record reference to the DataSet that
                                  // this operator should operate on
    }


    /* -------------------------- getCommand ----------------------------- */
    /**
     * @return     the command name to be used with script processor: in
     *             this case, LoadOffsets
     */
    public String getCommand(){
        return "LoadOffsets";
    }


    /* ------------------------ setDefaultParmeters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        parameters = new Vector();  // must do this to clear any old parameters
        
        addParameter(new Parameter( "Offsets", new LoadFileString("")));
    }
    
    
    /* -------------------------- getResult ----------------------------- */
    
    public Object getResult(){  
        DataSet ds = getDataSet();
        String offsets = (String)getParameter(0).getValue();

        int group_id=0;
        float time_offset=0f;

        //System.out.println("d:"+ds);
        //System.out.println("o:"+offsets);
        TextFileReader fr=null;
        Data data=null;
        FloatAttribute attr=new FloatAttribute(Attribute.TIME_OFFSET,0f);

        try{
            fr = new TextFileReader(offsets);

            while(!fr.eof()){
                data=null;
                fr.unread();
                fr.skip_blanks();
                group_id    = fr.read_int();
                fr.skip_blanks();
                time_offset = fr.read_float();
                //System.out.print("("+group_id+","+time_offset+")");
                
                data=ds.getData_entry_with_id(group_id);
                if(data!=null){
                    attr.setFloatValue(time_offset);
                    data.setAttribute((Attribute)attr.clone());
                    //System.out.print(" "+attr);
                }
                //System.out.println("");

            }
        }catch(IOException e){
            System.err.println("IOException: "+e.getMessage());
        }finally{
            if(fr!=null) try{
                fr.close();
            }catch(IOException e){
                System.err.println("Could not close "+offsets+":"
                                   +e.getMessage());
            }
        }
        return "Offsets Added";     
    }  

    /* ---------------------------- clone ----------------------------- */
    /**
     * Get a copy of the current LoadOffsets Operator.  The list of
     * parameters and the reference to the DataSet to which it applies
     * is copied.
     */
    public Object clone(){
        LoadOffsets new_op    = new LoadOffsets( );
                                              // copy the data set associated
                                              // with this operator
        new_op.setDataSet( this.getDataSet() );
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }

    public static void main(String args[]){
        String runfile="/IPNShome/pfpeterson/data/II_VI/SEPD/"
            +"dec2001/runfiles/sepd18124.run";
        LoadFileString offfile
            =new LoadFileString("/IPNShome/pfpeterson/trial.offsets");
        //System.out.println(runfile);
        DataSet rds=(new RunfileRetriever(runfile)).getDataSet(1);
        
        LoadOffsets op=new LoadOffsets(rds,offfile);
        op.getResult();
    }
}
