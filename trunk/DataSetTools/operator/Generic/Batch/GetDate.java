/*
 * File:  GetDate.java 
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
 * Contact : Peter Peterson <pfpeterson@anl.gov>
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
 */
package DataSetTools.operator.Generic.Batch;

import java.util.*;


/** 
 * This operator returns a string representing the current date and
 * time.
 */
public class GetDate extends GenericBatch{
    private static final String     TITLE                 = "Date";

    /**
     *  Creates operator with title "Date" and a default list of
     *  parameters (no parameters).
     */  
    public GetDate(){
	super( TITLE );
    }

    /** 
     * Get the name of this operator to use in scripts
     * 
     * @return "GetDate", the command used to invoke this
     * operator in Scripts
     */
    public String getCommand(){
        return "GetDate";
    }

    /** 
     * Sets default values for the parameters. This must match the
     * data types of the parameters.
     */
    public void setDefaultParameters(){
        parameters = new Vector();
    }
    
    /** 
     *  Executes this operator using the values of the current
     *  parameters. This will print the current date and time in the
     *  StatusPane as well as return the string to the caller.
     *
     *  @return If successful, this operator gives back a string of
     *  the current date and time.
     */
    public Object getResult(){
        Date date=new Date(System.currentTimeMillis());
        DataSetTools.util.SharedData.addmsg(date.toString());
        return date.toString();
    }
    
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){
        GetDate op = new GetDate();
        op.CopyParametersFrom( this );
        return op;
    }
}
