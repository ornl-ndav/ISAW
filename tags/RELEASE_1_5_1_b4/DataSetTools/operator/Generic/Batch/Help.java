/*
 * File:  Help.java 
 *
 * Copyright (C) 2003, Peter Peterson
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
 * Revision 1.1  2003/02/12 20:15:06  pfpeterson
 * Added to CVS.
 *
 */
package DataSetTools.operator.Generic.Batch;

import Command.Script_Class_List_Handler;
import DataSetTools.operator.*;
import DataSetTools.util.ErrorString;
import DataSetTools.util.SharedData;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.util.*;
import javax.swing.*;
import javax.swing.text.html.*;

/** 
 * This operator displays the documentation for a given
 * operator.
 */
public class Help extends GenericBatch{
  private static Script_Class_List_Handler sclh=null;
  private static int height=0;
  private static int width=0;

  /**
   * Creates operator with title "Help" and a default list of
   * parameters
   */  
  public Help(){
    super("Help");
  }

  /**
   * Creates an operator with the given operator specification.
   */
  public Help(String operator){
    this();
    getParameter(0).setValue(operator);
  }

  /** 
   * Returns the documentation for this operator to be used in the
   * help system.
   */
  public String getDocumentation(){
    StringBuffer res = new StringBuffer();
    
    // overview
    res.append("@overview This displays the help for a chosen operator in a new window.");
    // parameters
    res.append("@param operator is either the fully qualified class name or the command name.");
    // errors
    res.append("@error empty string is used to specify the operator");
    res.append("@error operator is not found");
    res.append("@error documentation is not returned by the operator");
    
    return res.toString();
  }
  
  /**
   * Returns the name of this operator to be used in scripts, in this
   * case 'help'.
   */    
  public String getCommand(){
    return "help";
  }
  
  /** 
   * Sets default values for the parameters. This must match the data
   * types of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter(new Parameter("Operator",""));
  }
    
  /** 
   * Executes this operator using the values of the current
   * parameters. This will bring up the documentation for the given
   * operator in a new window.
   */
  public Object getResult(){
    String name=getParameter(0).getValue().toString();
    Operator op=null;

    // can't do anything without a name
    if( name==null || name.length()<=0 )
      return new ErrorString("Cannot display help for empty operator command");

    // try the name as a class specification
    try{
      Class klass=Class.forName(name);
      if(klass!=null){
        Object obj=klass.newInstance();
        if( obj instanceof Operator)
          op=(Operator)obj;
        else
          return new ErrorString("Something wrong");
      }
    }catch(ClassNotFoundException e){
      op=null;
    }catch(InstantiationException e){
      op=null;
    }catch(IllegalAccessException e){
      op=null;
    }

    // didn't get the operator so it must be a classname
    if(op==null){
      // initialize the Script_Class_List_Handler if necessary
      if(sclh==null) sclh=new Script_Class_List_Handler();
      // get the operator
      op=sclh.getOperator(name);
    }

    // still didn't get the operator so error
    if(op==null)
      return new ErrorString("Could not find operator "+name);

    // get the documentation
    String doc=SharedData.HTMLPageMaker.createHTML(op);
    if(doc==null || doc.length()<=0)
      return new ErrorString("Empty documentation for operator "+name);

    // determine the window size
    if(width==0 || height==0){
      Dimension screensize=Toolkit.getDefaultToolkit().getScreenSize();
      height=screensize.height;
      width=(int)(height*4./3.);
      height=(int)(3.*height/4.);
      width=(int)(width/2.);
    }

    // display the documentation
    JFrame jf = new JFrame( "operator "+op.getCommand());
    JEditorPane jedPane = new JEditorPane();
    jedPane.setEditable(false);
    jedPane.setEditorKit( new HTMLEditorKit() );
    jedPane.setText(SharedData.HTMLPageMaker.createHTML(op));
    jf.getContentPane().add( new JScrollPane(jedPane) );
    jf.setSize(width,height);
    jf.show();
    
    return "Displaying help for "+op.getCommand();
  }
    
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone(){
    Help op = new Help();
    op.CopyParametersFrom( this );
    return op;
  }
    
  /**
   *  Main program to display help as specified on the command line
   */
  public static void main( String[] args ){
    Help op = null;
    String help_msg="specify operator command name or package qualified class";

    if(args.length==0){
      System.out.println(help_msg);
      op=new Help("DataSetTools.operator.Generic.Batch.Help");
    }else if(args.length==1){
      op=new Help(args[0]);
    }else{
      System.out.println(help_msg);
      System.exit(-1);
    }
      
    op.getResult();

  }
}
