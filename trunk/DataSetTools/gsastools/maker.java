/*
 * File:  maker.java
 *
 * Copyright (C) 1999, Dongfeng Chen
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
 * Contact : Alok Chatterjee <AChatterjee@anl.gov>
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
 * $Log$
 * Revision 1.3  2001/04/25 19:26:09  dennis
 * Added copyright and GPL info at the start of the file.
 *
 *
 */
package DataSetTools.gsastools;

import IPNS.Runfile.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.util.*; 
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.rmi.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import DataSetTools.components.image.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import java.text.DateFormat;
import java.text.*;


/**
 * Transfer the histogram to GSAS file format.
 * @version 1.0
 */

public class maker
{     
    static Runfile r=null;                      
    

/* ----------------------------- main ------------------------------------ */


public static void main(String[] args)
{
    gsas_filemaker gfm=new gsas_filemaker();
    String run= ".\\DataSetTools\\gsastools\\GPPD10628.RUN"; 
    //gfm.maker(run);
}
  


}
