/*
 * File: ScriptProcessorOperator.java
 *
 * Copyright (C) 2001 Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.4  2002/11/27 23:12:10  pfpeterson
 * standardized header
 *
 */
package Command;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.components.ParametersGUI.*;
import java.beans.PropertyChangeListener;

public abstract class ScriptProcessorOperator extends GenericOperator
                               implements IScriptProcessor, IDataSetListHandler{
    
    String Title,Command;
    
    public ScriptProcessorOperator( String Title){
        super(Title);
        this.Title = Title;
        Command ="UNKNOWN";
    }

    public ScriptProcessorOperator(){
        super("");
        this.Title ="";
        Command ="";
    }

    public abstract void execute1(javax.swing.text.Document Doc, int line);
    
    public abstract void setDocument( javax.swing.text.Document Doc);
    
    public abstract Object getResult();
    
    public abstract void  reset();
 
    public abstract void resetError();
    
    public abstract int getErrorCharPos();
 
    public abstract int getErrorLine() ;
    
    public abstract String getErrorMessage(); 

    public void setTitle( String Title){
        this.Title= Title;
    }

    public String getTitle(){
        return Title;
    }

    public void setCommand( String command){
        Command = command;
    }

    public String getCommand(){
        return Command;
    }     

    public  abstract String getVersion();
    
    public abstract void setLogDoc(javax.swing.text.Document doc); 
    
    public abstract void  addDataSet(DataSet dss) ;
 
    public abstract void  addIObserver(IObserver iobs);
    
    public  abstract void  deleteIObserver(IObserver iobs) ;
    
    public abstract void deleteIObservers(); 
    
    public abstract void addPropertyChangeListener(PropertyChangeListener P);
   
    public abstract DataSet[] getDataSets();

}
