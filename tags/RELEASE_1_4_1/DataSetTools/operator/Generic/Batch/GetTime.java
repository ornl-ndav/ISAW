/*
 * File:  GetTime.java 
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
 * Revision 1.2  2002/11/27 23:20:52  pfpeterson
 * standardized header
 *
 *
 */
package DataSetTools.operator.Generic.Batch;

import java.util.*;


/** 
 * This operator returns a string representing the current date and
 * time.
 */
public class GetTime extends GenericBatch{
    private static final String     TITLE                 = "Time";

    /**
     *  Creates operator with title "Time" and a default list of
     *  parameters (no parameters).
     */  
    public GetTime(){
	super( TITLE );
    }

    /** 
     * Get the name of this operator to use in scripts
     * 
     * @return "GetTime", the command used to invoke this
     * operator in Scripts
     */
    public String getCommand(){
        return "GetTime";
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
     *  parameters.
     *
     *  @return If successful, this operator gives back an integer of
     *  the time, in milliseconds, since ISAW was started.
     */
    public Object getResult(){
        long time=System.currentTimeMillis();
        return new Integer((int)(time-DataSetTools.util.SharedData.start_time));
    }
    
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){
        GetTime op = new GetTime();
        op.CopyParametersFrom( this );
        return op;
    }
}
