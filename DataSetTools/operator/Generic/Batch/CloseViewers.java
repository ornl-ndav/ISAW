/*
 * File:  CloseViewers.java 
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
import DataSetTools.operator.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;

/** 
 * This operator returns a string representing the current date and
 * time.
 */
public class CloseViewers extends GenericBatch{
  private static final String     TITLE                 = "CloseViewers";

  /**
   *  Creates operator with title "CloseViewers" and a default list of
   *  parameters.
   */  
  public CloseViewers(){
    super( TITLE );
  }
  
  /**
   *  Creates operator with title "CloseViewers" and the specified
   *  parameters.
   */  
  public CloseViewers(DataSet ds){
    this();
    getParameter(0).setValue(ds);
  }

  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return "CloseViewers", the command used to invoke this
   * operator in Scripts
   */
  public String getCommand(){
    return "CloseViewers";
  }
  
  /** 
   * Sets default values for the parameters. This must match the
   * data types of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter(new Parameter("DataSet",DataSet.EMPTY_DATA_SET));
  }
  
  /** 
   *  Executes this operator using the values of the current
   *  parameters. This will have the specified DataSet send a message
   *  to all of its viewers to close.
   *
   *  @return If successful, this operator gives back a string stating
   *  that things went well.
   */
  public Object getResult(){
    DataSet ds=(DataSet)(getParameter(0).getValue());
    if(ds==null){
      return new ErrorString("Encountered null DataSet in getResult");
    }else{
      ds.notifyIObservers(IObserver.CLOSE_VIEWERS);
      return "sent close message to all viewers";
    }
  }
  
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    CloseViewers op = new CloseViewers();
    op.CopyParametersFrom( this );
    return op;
  }
}
