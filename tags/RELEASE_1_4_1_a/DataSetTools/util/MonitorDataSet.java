/*
 * File:  MonitorDataSet.java
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2002/04/03 19:52:36  pfpeterson
 *  Added to CVS.
 *
 */
package DataSetTools.util;

import java.io.*;
import DataSetTools.dataset.DataSet;

/**
 * The MonitorDataSet class is used to pass a filename string between
 * operators and the GUI so that appropriate GUI components can be
 * created to get the input values from the user. This is used
 * exclusively to select existing files.
 */
public class MonitorDataSet  extends DataSet{
    public MonitorDataSet(){
        super("MonitorDataSet", "");
    }

    /* public MonitorDataSet( ){
       super("");
       } */
    
    
    /* public MonitorDataSet( String message ){
       super( message );
       } */
    
}
