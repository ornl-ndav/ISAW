/*
 * File:  ScriptUtil.java 
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
 * Contact : Peter Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/06/19 21:21:03  pfpeterson
 * Now does type checking when looking for an operator if could not
 * determine one from number of supplied parameters.
 *
 * Revision 1.1  2003/06/19 20:51:55  pfpeterson
 * Added to CVS.
 *
 */

package Command;

import DataSetTools.dataset.*;
import DataSetTools.operator.Operator;
import DataSetTools.operator.Generic.GenericOperator;
import DataSetTools.retriever.*;
import DataSetTools.util.SharedData;
import DataSetTools.util.SpecialString;
import DataSetTools.util.StringUtil;
import DataSetTools.viewer.IViewManager;
import DataSetTools.viewer.ViewManager;
import DataSetTools.writer.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.MissingResourceException;

/**
 * This is a utility class to locate some common tasks in scripts to
 * one central location. The common tasks are loading data, saving
 * data, sending data to listeners, and viewing data.
 */
public class ScriptUtil{
  public static boolean DEBUG=false;

  private static final String IMAGE          = "IMAGE";
  private static final String SCROLLED_GRAPH = "SCROLLED_GRAPH";
  private static final String SELECTED_GRAPH = "SELECTED_GRAPH";
  private static final String THREE_D        = "THREE_D";
  private static final String TABLE          = "TABLE";
  private static final String ISD_ERROR    = "ISD files only have one DataSet";

  private static Script_Class_List_Handler SCLH=null;

  // ============================== CONSTRUCTORS
  private ScriptUtil(){}

  /**
   * DataSets will be brought up in a viewer while all other objects
   * will have thier value converted to a string and printed in the
   * status pane.
   *
   * @return if the Object is a DataSet then ViewManager used is
   * returned, otherwise null.
   */
  public static ViewManager display(Object object){
    if(object instanceof DataSet){
      return display((DataSet)object,null);
    }else{
      SharedData.addmsg(StringUtil.toString(object));
      return null;
    }
  }

  /**
   * This allows a view of a DataSet to be brought up and specified
   * using the displayType. If displayType is null or empty then this
   * defaults to "IMAGE".
   *
   * @return returns a reference to the ViewManager that the DataSet
   * is in. This is intended for callers to use to destroy the view.
   */
  public static ViewManager display(DataSet ds, String displayType){
    // change to displayType to something easier to work with
    if(displayType==null || displayType.length()<=0)
      displayType=IMAGE;
    else
      displayType=displayType.toUpperCase().intern();

    if(DEBUG) System.out.println("IN DISPLAY("+ds+","+displayType+")");

    if( displayType==IMAGE )
      displayType = IViewManager.IMAGE;
    else if( displayType==SCROLLED_GRAPH) 
      displayType = IViewManager.SCROLLED_GRAPHS;
    else if( displayType==SELECTED_GRAPH) 
      displayType = IViewManager.SELECTED_GRAPHS;
    else if( displayType==THREE_D)
      displayType = IViewManager.THREE_D;
    else if( displayType==TABLE)
      displayType = IViewManager.TABLE;
    else
      displayType = displayType;

    return new ViewManager(ds , displayType);
  }

  /**
   * This will return the appropriate Writer for the given
   * filename. The Writer is selected by reviewing the extension.
   */
  public static Writer getWriter(String filename) throws IOException{
    if(filename==null || filename.length()<=0)
      throw new IOException("Empty filename");

    // determine the extension
    filename=StringUtil.setFileSeparator(filename);
    String extension=null;
    {
      int index=filename.lastIndexOf(".");
      if(index<=0)
        throw new IOException("Filename has no extension");
      extension=filename.substring(index+1).toUpperCase();
    }

    // determine which writer to use and return it
    if( extension.equals("NXS") || extension.equals("HDF")
                                                   || extension.equals("XMN") )
      return new NexWriter(filename);
    else if( extension.equals("XMI") || extension.equals("ZIP") )
      return new XmlDWriter(filename);
    else if( extension.equals("GSA") || extension.equals("GDA")
                       || extension.equals("GDA") || extension.equals("GSAS") )
      return new GsasWriter(filename);
    else
      throw new IOException("Unsupported extension "+extension);
  }

  /**
   * This saves a single DataSet to the specified filename. It is
   * actually a convenience method which calls {@link
   * #save(String,DataSet[]) save(filename,dss)}.
   */
  public static void save(String filename,DataSet ds) throws IOException{
    if(filename==null || filename.length()<=0)
      throw new IOException("Empty filename");
    if(ds==null)
      throw new NullPointerException("null DataSet");

    save(filename,new DataSet[]{ds});
  }

  /**
   * This saves an array of DataSets to the specified filename.
   */
  public static void save(String filename, DataSet[] dss)
       throws IOException, IllegalArgumentException, IndexOutOfBoundsException{
    if(filename==null || filename.length()<=0)
      throw new IOException("Empty filename");
    if(dss.length<=0)
      throw new IllegalArgumentException("Empty DataSet Array");

    // do something special for ISD files
    if( filename.toUpperCase().endsWith( ".ISD" ) ){
      if(dss.length!=1)
        throw new IndexOutOfBoundsException(ISD_ERROR);
      DataSet_IO.SaveDataSet( dss[0], filename );
      return;
    }

    // everything else uses a writer
    Writer writer=getWriter(filename);
    writer.writeDataSets(dss);
  }

  /**
   * This returns a retriever based on the the extension of the filename.
   */
  public static Retriever getRetriever(String filename) throws IOException{
    if(filename==null || filename.length()<=0)
      throw new IOException("Empty filename");

    // determine the extension
    filename=StringUtil.setFileSeparator(filename);
    String extension=null;
    {
      int index=filename.lastIndexOf(".");
      if(index<=0)
        throw new IOException("Filename has no extension");
      extension=filename.substring(index+1).toUpperCase();
    }

    // try multiple cases if the extension is run
    if(extension.equals("RUN")){
      String runfilename=filename;
      // try the file
      if(fileExists(filename))
        return new RunfileRetriever(filename);
      // find out where the directory ends
      int index=filename.lastIndexOf(SharedData.getProperty("file.separator"));
      String directory=null;
      String file=null;
      if(index<0){
        directory="";
        file=filename;
      }else{
        directory=filename.substring(0,index);
        file=filename.substring(index);
      }
      // try uppercase filename
      runfilename=directory+file.toUpperCase();
      if(fileExists(runfilename))
        return new RunfileRetriever(runfilename);
      // try lowercase filename
      runfilename=directory+file.toLowerCase();
      if(fileExists(runfilename))
        return new RunfileRetriever(runfilename);
      // if we got here then throw an exception
      throw new FileNotFoundException("Could not find "+filename);
    }else{
      if(! fileExists(filename) )
        throw new FileNotFoundException("Could not find "+filename);
    }

    // return the appropriate retriever
    if( extension.equals( "NXS" ) || extension.equals( "HDF" ) )
      return new NexusRetriever( filename );
    else if( extension.equals("ZIP") || extension.equals("XMI"))
      return new XmlDFileRetriever( filename);
    else if( extension.equals("GSA") || extension.equals("GDA"))
      return new GsasRetriever(filename);
    else if( extension.equals("SDDS") )
      return new SDDSRetriever( filename );
    else
      throw new IOException("Unsupported extension "+extension);
  }

  /**
   * This will load an array of DataSets from the filename as
   * specified in dsnums. The integers in dsnums are the DataSet
   * numbers, which are zero indexed.
   */
  public static DataSet[] load(String filename, int[] dsnums)
                                  throws IOException,IndexOutOfBoundsException{
    if(filename.toUpperCase().endsWith(".ISD")){
      for(int i=0 ; i<dsnums.length ; i++ )
        if(dsnums[i]>0) 
          throw new IndexOutOfBoundsException(ISD_ERROR);
      return load(filename);
    }

    // set up for reading the selected DataSet
    Retriever retriever=getRetriever(filename);
    int numberOfDataSets = retriever.numDataSets();

    // check that the dsnum is feasable
    for( int i=0 ; i<dsnums.length ; i++ ){
      if(dsnums[i]>=numberOfDataSets)
        throw new IndexOutOfBoundsException("Invalid dsnum: "+dsnums[i]+">="
                                            +numberOfDataSets);
      else if(dsnums[i]<0)
        throw new IndexOutOfBoundsException("Invalid dsnum: "+dsnums[i]+"<0");
    }

    // return the selected DataSet
    try{
      DataSet[] dss=new DataSet[dsnums.length];
      for( int i=0 ; i<dsnums.length ; i++ )
        dss[i]=retriever.getDataSet(dsnums[i]);
      return dss;
    }finally{
      // always cleanup
      retriever=null;
    }
  }

  /**
   * This loads one DataSet from the specified file.
   */
  public static DataSet load(String filename, int dsnum)
                                  throws IOException,IndexOutOfBoundsException{
    if(filename==null || filename.length()<=0)
      throw new IOException("Empty filename");

    if(filename.toUpperCase().endsWith(".ISD")){
      if(dsnum>0)
        throw new IndexOutOfBoundsException(ISD_ERROR);
      return load(filename)[0];
    }

    // set up for reading the selected DataSet
    Retriever retriever=getRetriever(filename);
    int numberOfDataSets = retriever.numDataSets();

    // check that the dsnum is feasable
    if(dsnum>=numberOfDataSets)
        throw new IndexOutOfBoundsException("Invalid dsnum: "+dsnum+">="
                                            +numberOfDataSets);

    // return the selected DataSet
    try{
      DataSet ds=retriever.getDataSet(dsnum);
      return ds;
    }finally{
      // always cleanup
      retriever=null;
    }
  }

  /**
   * This loads all of the DataSets from the specified file.
   */
  public static DataSet[] load(String filename) throws IOException{
    if(filename==null || filename.length()<=0)
      throw new IOException("Empty filename");

    // ISD files don't go throgh a retriever
    if(filename.toUpperCase().endsWith(".ISD")){
      if(! fileExists(filename) )
        throw new FileNotFoundException("Could not find "+filename);
      DataSet[] dss=new DataSet[1];
      dss[0] = DataSet_IO.LoadDataSet( filename );
      return dss;
    }

    // set up for reading the entire file into memory
    Retriever retriever=getRetriever(filename);
    int numberOfDataSets = retriever.numDataSets();

    // return the whole thing in memory
    try{
      if( numberOfDataSets>0 ){
        DataSet[] dss = new DataSet[numberOfDataSets];
        for( int i = 0; i < numberOfDataSets; i++ )
          dss[i] = retriever.getDataSet( i );
        return dss;
      }else{
        retriever=null;
        return null;
      }
    }finally{
      // always cleanup
      retriever = null;
      System.gc();
    }
  }

  /**
   * Convenience method which makes writing other code shorter.
   */
  private static boolean fileExists(String filename){
    return (new File(filename)).exists();
  }

  /**
   * Method stub intended to fill out the function of the 'Send'
   * method in scripts. Currently this just throws an
   * AbstractMethodError because there is no body to it.
   */
  public static void send(DataSet ds){
    throw new AbstractMethodError("This is a method stub");
  }

  /**
   * Method for finding the proper operator with the given command
   * name and list of parameter values. The operator will be
   * configured with the values it is given.
   */
  public static GenericOperator getOperator(String command,
                          Object[] param_vals) throws MissingResourceException{
    // initialize searching information
    int num_vals=0;
    if(param_vals!=null) num_vals=param_vals.length;

    // determine possible operators
    int[] candidates=findOperator(command,num_vals);

    GenericOperator operator=null;

    // if there is only one choice our work is done
    if(candidates.length==1){
      // throws a ClassCastException if Operator not a GenericOperator
      operator=(GenericOperator)SCLH.getOperator(candidates[0]);

      // copy over the values into the parameters
      return (GenericOperator)configOperator(operator,param_vals);
    }

    // now do the type checking
    if(operator==null){
      int opNum=findOperator(candidates,param_vals);
      if(opNum<0)
        throw new MissingResourceException("Could not find command \""+command
                            +"\"","Command.Script_Class_List_Handler",command);
      // throws a ClassCastException if Operator not a GenericOperator
      operator=(GenericOperator)SCLH.getOperator(opNum);
      // copy over the values into the parameters
      return (GenericOperator)configOperator(operator,param_vals);
    }

    // shouldn't get this far
    throw new MissingResourceException("Could not find command \""+command+"\""
                                 ,"Command.Script_Class_List_Handler",command);
  }

  /**
   * Finds an operator with the appropriate signature and configures
   * it. This will return a new instance of the operator.
   */
  public static GenericOperator getNewOperator(String command,
                                                          Object[] param_vals)
         throws MissingResourceException,InstantiationError,IllegalAccessError{
    // initialize searching information
    int num_vals=0;
    if(param_vals!=null) num_vals=param_vals.length;

    // determine possible operators
    int[] candidates=findOperator(command,num_vals);

    GenericOperator operator=null;

    // if there is only one choice our work is done
    if(candidates.length==1){
      // throws a ClassCastException if Operator not a GenericOperator
      operator=(GenericOperator)SCLH.getOperator(candidates[0]);

      // copy over the values into the parameters
      return (GenericOperator)configOperator((Operator)operator.clone(),
                                                                   param_vals);
    }

    // now do the type checking
    if(operator==null){
      int opNum=findOperator(candidates,param_vals);
      if(opNum<0)
        throw new MissingResourceException("Could not find command \""+command
                            +"\"","Command.Script_Class_List_Handler",command);
      // throws a ClassCastException if Operator not a GenericOperator
      operator=(GenericOperator)SCLH.getOperator(opNum);
      // copy over the values into the parameters
      return (GenericOperator)configOperator((Operator)operator.clone(),
                                                                   param_vals);
    }

    // shouldn't get this far
    throw new MissingResourceException("Could not find command \""+command+"\""
                                 ,"Command.Script_Class_List_Handler",command);
  }

  /**
   * Takes an operator and puts the values into the parameters.
   */
  private static Operator configOperator(Operator operator,
                                                          Object[] param_vals){
    // determine how much to copy into the operator
    int num_param=operator.getNum_parameters();
    int num_vals=0;
    if(param_vals!=null) num_vals=param_vals.length;
    if(num_vals>num_param)
      throw new IndexOutOfBoundsException("too many values for the number of "
                                          +"parameters");
    int max=Math.min(num_vals,num_param);

    // copy over the values into the parameters
    for( int i=0 ; i<max ; i++ )
      operator.getParameter(i).setValue(param_vals[i]);
    
    // return the configured operator
    return operator;
  }

  /**
   * This compares types in an operator with the parameter values to
   * determine which sould be used. This will return the first that
   * has a matching signature.
   *
   * @param candidates the list of candidates should all have the same
   * number of parameters.
   */
  private static int findOperator(int[] candidates, Object[] param_vals){
    // make sure there are candidates
    if(candidates==null || candidates.length<=0)
      return -1;

    // if there is only one element return its value
    if(candidates.length==1)
      return candidates[0];

    // set up for how many parameter types to check
    int num_param=SCLH.getNumParameters(candidates[0]);
    int num_vals=0;
    if(param_vals!=null) num_vals=param_vals.length;
    int max=Math.min(num_param,num_vals);

    // do the checking
    Object param=null;
    outer: for( int i=0 ; i<candidates.length ; i++ ){
      for( int j=0 ; j<max ; j++ ){
        param=SCLH.getOperatorParameter(candidates[i],j);
        if(param_vals[j]==null){
          // do nothing
        }else if( compareClass(param_vals[j],param) ){
          // do nothing
        }else{
          continue outer;
        }
      }
      // made it through all the parameters so this must be okay
      return i;
    }

    // didn't find what we want so return error
    return -1;
  }

  /**
   * This compares the classes of two objects to see if they can be
   * coerced into each other.
   */
  private static boolean compareClass(Object a, Object b){
    if( (a instanceof String) && (b instanceof SpecialString) )
      return true;
    if( (a instanceof SpecialString) && (b instanceof String) )
      return true;
    else if( (a instanceof Integer) && (b instanceof Float) )
      return true;
    else if( (a instanceof Float) && (b instanceof Integer) )
      return true;
    else if( a.getClass().isInstance(b) )
      return true;
    else
      return false;
  }

  /**
   * This creates an int[] of all indices with the correct command
   * name and an acceptable number of parameters. The aray will only
   * contain indices that refer to Operators with the same number of
   * parameters.
   */
  private static int[] findOperator(String command, int num_vals){
    int[] candidates=findOperator(command);
    if(candidates.length==1)
      return candidates;
    
    // initialize searching information
    int[] num_param=new int[candidates.length];
    boolean has_matching=false;

    System.out.println("Searching for "+command); // REMOVE

    // remove candidates with too few parameters
    System.out.println("00:"+StringUtil.toString(candidates)); // REMOVE
    for( int i=0 ; i<candidates.length ; i++ ){
      num_param[i]=SCLH.getNumParameters(candidates[i]);
      if(num_vals==num_param[i]){
        has_matching=true;
        break;
      }else if(num_vals>num_param[i]){
        candidates[i]=-1;
      }
    }
    System.out.println("01:"+StringUtil.toString(candidates)); // REMOVE

    // something has the right number so remove anything that doesn't
    if(has_matching){
      for( int i=0 ; i<candidates.length ; i++ ){
        if(num_vals!=num_param[i]){
          candidates[i]=-1;
        }
      }
      candidates=reduceArray(candidates);
      if(candidates==null)
        throw new MissingResourceException("Could not find command \""+command
                            +"\"","Command.Script_Class_List_Handler",command);
      else
        return candidates;
    }

    System.out.println("02:"+StringUtil.toString(candidates)); // REMOVE

    // find the minimum
    int min=100;
    for( int i=0 ; i<candidates.length ; i++ ){
      if(candidates[i]>=0){
        if(num_param[i]>min)
          candidates[i]=-1;
        else if(num_param[i]<min)
          min=num_param[i];
      }
    }
    // loop through and remove elements that don't match
    for( int i=0 ; i<candidates.length ; i++ ){
      if( (candidates[i]>=0) && (num_param[i]>min) )
        candidates[i]=-1;
    }

    System.out.println("03:"+StringUtil.toString(candidates)); // REMOVE

    // shrink the array and return
    return reduceArray(candidates);;
  }

  /**
   * This creates an int[] of all indices with the correct command name.
   */
  private static int[] findOperator(String command)
                                               throws MissingResourceException{
    // make sure we have a handle on the Script_Class_List_Handler
    if(SCLH==null) SCLH=new Script_Class_List_Handler();

    // find the first instance of the command
    int start=SCLH.getOperatorPosition(command);
    if(start==-1)
      throw new MissingResourceException("Could not find command \""+command
                            +"\"","Command.Script_Class_List_Handler",command);

    // determine what indices might be the right ones
    String opCommand=SCLH.getOperatorCommand(start);
    int count=1;
    while(command.equals(opCommand)){
      opCommand=SCLH.getOperatorCommand(start+count);
      count++;
    }

    // create the result array
    int[] result=new int[count-1];
    for( int i=0 ; i<result.length ; i++ ){
      result[i]=i+start;
    }

    return result;
  }

  /**
   * This takes an int[] and produces a smaller array with the values
   * less than one removed.
   */
  private static int[] reduceArray(int[] array){
    // determine the number of elements to keep
    int count=0;
    for( int i=0 ; i<array.length ; i++ )
      if(array[i]>=0) count++;

    // maybe return early
    if(count==array.length) // return if the whole thing is good
      return array;
    if(count==0)
      return null;

    // copy over the good values
    int[] new_array=new int[count];
    for( int i=0, j=0 ; i<array.length ; i++ ){
      if(array[i]>=0){
        new_array[j]=array[i];
        j++;
      }
    }

    // return the result
    return new_array;
  }

  /**
   * MAIN METHOD FOR TESTING ONLY
   */
  public static void main(String[] args){
    try{
      System.out.println("bob="+StringUtil.toString(getOperator("bob",null)));
    }catch(MissingResourceException e){
      System.out.println("[CAUGHT");
      e.printStackTrace();
      System.out.println("]");
    }
    System.out.println("WriteSCDExp="
                       +getOperator("WriteSCDExp",null).getClass().getName());
    System.out.println("Crunch="
                        +getOperator("Crunch",null).getClass().getName());
  }
}
