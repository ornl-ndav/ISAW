/*
 * @(#)gsas_filemaker.java  1.0 99/06/11 Dennis Mikkelson
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
 * Show image views diffractometer Data Set and transfer the 
 * histogram to GSAS file format.
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
