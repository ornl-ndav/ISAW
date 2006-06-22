/*
 * File: ObjectStateUtilities.java
 *
 * Copyright (C) 2005, Dominic Kramer
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
 * Primary   Dominic Kramer <kramerd@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *           
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.3  2005/08/17 21:22:19  kramer
 * -Added javadocs.
 * -Modified the ModifyObjectState() method so that it has the ability to
 *  create a path in an ObjectState if it does not already exist.
 * -Broke the ModifyObjectStateImpl() method into several smaller pieces,
 *  added comments, and made the method more effient by having it use
 *  the containsKey() method from the ObjectState class (instead of having
 *  it look at every key in an ObjectState until it found a match).
 * -Fixed several little bugs that would cause the Get/SetObjectStateValue()
 *  methods to modify the ObjectState in unexpected ways.
 *
 * Revision 1.2  2005/08/05 22:42:46  kramer
 *
 * -Added the methods SetObjectSateValue(), GetObjectStateValue(), and
 *  GetObjectStateDataType() which are used to get and set values for
 *  specific items in the ObjectState (as specified by a path).
 * -Added the methods ModifyObjectState() and ModifyObjectStateImpl() which
 *  do the actual work of the methods described above.
 * -Modified the PrintObjectState() method so that the data types of the
 *  values in the ObjectState are included in the printout.
 * -Added a main() method.
 *
 * Revision 1.1  2005/08/05 16:15:27  kramer
 *
 * Initial checkin.  This is a utility class that contains the static
 * methods that do the actual work of dealing with ObjectStates.  The
 * operators defined in this package wrap particular methods in this class.
 *
 */
package Operators.Special.ObjectState;

import gov.anl.ipns.ViewTools.Components.IPreserveState;
import gov.anl.ipns.ViewTools.Components.ObjectState;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This is a utility class that contains the static methods that work 
 * with <code>ObjectStates</code>.  This work includes loading and saving 
 * them to a file, getting them from and setting them on 
 * <code>IPreserveState</code> objects, and getting an ASCII printout of the 
 * <code>ObjectState</code>.  These methods have been made so that they can 
 * be wrapped inside operators, in which case they would do the main work of 
 * the operator.
 */
public class ObjectStateUtilities
{
   private static String INSERT = "insert";
   private static String REPLACE = "replace";
   private static String READ_VALUE = "read value";
   private static String READ_TYPE = "read type";
   
   /** Private to emphasize that this class should not be instantiated. */
   private ObjectStateUtilities() {}
   
   /**
    * Used to create a new empty <code>ObjectState</code>.
    * 
    * @return <code>new ObjectState()</code>.
    */
   public static ObjectState CreateEmptyState()
   {
      return new ObjectState();
   }
   
   /**
    * Used to load an <code>ObjectState</code> from a file.
    * 
    * @param filename The full path to the file that contains the 
    *                 <code>ObjectState</code>.
    * 
    * @return The <code>ObjectState</code> contained in the specified 
    *         file.  If the <code>ObjectState</code> could not be read an 
    *         Exception is thrown.
    * 
    * @throws IOException               Thrown if the given filename does 
    *                                   not correspond to a file that 
    *                                   contains an <code>ObjectState</code>.
    * @throws IllegalArgumentException  If and only if the filename given is 
    *                                   <code>null</code>.
    */
   public static ObjectState LoadObjectState(String filename) 
                                throws IOException
   {
      if (filename==null)
         throw new IllegalArgumentException("The ObjectState could not be " +
                                            "loaded because the filename " +
                                            "given could not be resolved " +
                                            "because it was 'null'");
      
      //make an ObjectState and load it from the file
      ObjectState state = new ObjectState();
      if (!state.silentFileChooser(filename, false))
         throw new IOException("The filename \""+filename+
                               "\" does not correspond to an ObjectState");
      
      return state;
   }
   
   /**
    * Used to save an <code>ObjectState</code> to a file.
    * 
    * @param state    The <code>ObjectState</code> that is to be saved.
    * @param filename The full path to the file where the 
    *                 <code>ObjectState</code> should be saved.
    * 
    * @return The String "Success" when the method completes successfully.  
    *         If the <code>ObjectState</code> could not be saved an 
    *         Exception is thrown.
    * 
    * @throws IOException               Thrown if the the 
    *                                   <code>ObjectState</code> could not 
    *                                   be saved to the specified file.
    * @throws IllegalArgumentException  If the filename or 
    *                                   <code>ObjectState</code> given is 
    *                                   <code>null</code>.
    */
   public static String SaveObjectState(ObjectState state, String filename) 
                           throws IOException
   {
      if (filename == null)
         throw new IllegalArgumentException("The ObjectState could not be " +
                                            "saved because the filename " +
                                            "given could not be resolved " +
                                            "because it was 'null'");
      
      if (state == null)
         throw new IllegalArgumentException("The state cannot be 'null'");
      
      //save the state to the given file
      if (!state.silentFileChooser(filename, true))
         throw new IOException("Could not save to the file \""+filename+"\"");
      
      return "Success";
   }
   
   /**
    * Used to get an <code>ObjectState</code> encapsulating a state of the 
    * <code>IPreserveState</code> object given.
    * 
    * @param preserve  The object whose state is to be encapsulated in an 
    *                  <code>ObjectState</code> and returned.
    * @param isDefault If <code>true</code>, the default state of the given 
    *                  <code>IPreserveState</code> object is returned.  
    *                  Otherwise, if <code>false</code>, the current state 
    *                  is returned.
    * 
    * @return The <code>ObjectState</code> either encapsulating the current 
    *         or default state of the given <code>IPreserveState</code> 
    *         object.
    * 
    * @throws IllegalArgumentException If the given 
    *                                  <code>IPreserveState</code> object 
    *                                  is <code>null</code>.
    */
   public static ObjectState GetObjectState(IPreserveState preserve, 
                                            boolean isDefault)
   {
      if (preserve == null)
         throw new IllegalArgumentException("The state could not be " +
                                            "retrieved because the object " +
                                            "given was 'null'");
      
      return preserve.getObjectState(isDefault);
   }
   
   /**
    * Used to set the given <code>ObjectState</code> on the the given 
    * <code>IPreserveState</code> object.
    * 
    * @param preserve The object whose state is to be set.
    * @param state    An encapsulation of the state to set.
    * 
    * @return The String "Success" is returned when the state was 
    *         successfully set.  If the state can't be set an 
    *         Exception is thrown.
    * 
    * @throws IllegalArgumentException If either of the given 
    *                                  <code>IPreserveState</code> or 
    *                                  <code>ObjectState</code> objects 
    *                                  are <code>null</code>.
    */
   public static String SetObjectState(IPreserveState preserve, 
                                       ObjectState state)
   {
      if (preserve == null)
         throw new IllegalArgumentException("The state could not be set " +
                                            "because the item to set the " +
                                            "state on is 'null'");
      
      if (state == null)
         throw new IllegalArgumentException("The state cannot be 'null'");
      
      preserve.setObjectState(state);
      return "Success";
   }
   
   public static String SetObjectStateValue(String path, 
                                            ObjectState state, 
                                            Object value, 
                                            boolean allowOverwrites, 
                                            boolean checkTypes, 
                                            boolean createPath) 
                                               throws Exception
   {
      String code = INSERT;
      if (allowOverwrites)
         code = REPLACE;
      
      ModifyObjectState(path, state, code, value, checkTypes, createPath);
      
      return "Success";
   }
   
   public static Object GetObjectStateValue(String path, ObjectState state) 
                                                              throws Exception
   {
      return ModifyObjectState(path, state, READ_VALUE, null, false, false);
   }
   
   public static Object GetObjectStateDataType(String path, ObjectState state)
                                                              throws Exception
   {
      return ModifyObjectState(path, state, READ_TYPE, null, false, false);
   }
   
   /**
    * Convience method that is used to access or modify an item in an 
    * <code>ObjectState</code>.  This method simply does some preliminary 
    * tests on the parameters given and uses them to invoke the 
    * {@link #ModifyObjectStateImpl(Vector, int, ObjectState, 
    * String, Object[], boolean, boolean) ModifyObjectStateImpl()} method.  
    * Thus, except for the parameters described here, the parameters for 
    * this method are the same as those for the 
    * <code>ModifyObjectStateImpl</code> method.
    * 
    * @param fullPath  The full path to the item in the 
    *                  <code>ObjectState</code> that is to be 
    *                  accessed or modified.
    * @param value     This value is only used if the value of the 
    *                  parameter <code>code</code> is either 
    *                  {@link #INSERT INSERT} or {@link #REPLACE REPLACE}.  
    *                  In this case, this parameter specifies the value to 
    *                  store in given <code>ObjectState</code>.
    * 
    * @return          If the value of the parameter <code>code</code> is 
    *                  either {@link #INSERT INSERT} or 
    *                  {@link #REPLACE REPLACE} this method returns the 
    *                  value that was inserted in the 
    *                  <code>ObjectState</code>.  Otherwise, if the value of 
    *                  the parameter <code>code</code> is either 
    *                  {@link #READ_TYPE READ_TYPE} or 
    *                  {@link #READ_VALUE READ_VALUE}, the value read from the 
    *                  <code>ObjectState</code> is returned.  A return value 
    *                  of <code>null</code> does not specify that an error 
    *                  has occured in this method.  This is because if an 
    *                  error has occured, an Exception is thrown instead.
    * 
    * @see #ModifyObjectStateImpl(Vector, int, ObjectState, String, 
    *                             Object[], boolean, boolean)
    */
   private static Object ModifyObjectState(String fullPath, 
                                           ObjectState state, 
                                           String code, Object value, 
                                           boolean typeSafe, 
                                           boolean createPath)
                                              throws Exception
   {
      if (fullPath == null)
         throw new IllegalArgumentException("The path cannot be 'null'");
         
      if (state == null)
         throw new IllegalArgumentException("The state cannot be 'null'");
      
      if (code == null)
         throw new IllegalArgumentException("The modification code " +
                                            "cannot be 'null'");
      
      StringTokenizer tokenizer = new StringTokenizer(fullPath, "/");
      int numTokens = tokenizer.countTokens();
      if (numTokens == 0)
         throw new Exception("The path cannot be empty");
         
      Vector pathVec = new Vector(numTokens);
      while (tokenizer.hasMoreTokens())
         pathVec.add(tokenizer.nextToken());
      
//      if (createPath)
//         createPath(pathVec, state, value);
      
      Object[] valueRef = new Object[] { value };
      ModifyObjectStateImpl(pathVec, 0, state, code, 
                            valueRef, typeSafe, createPath);
      
      return valueRef[0];
   }
   
   /**
    * The method that does the actual work of modifying an item in the 
    * <code>ObjectState</code> given.
    * 
    * @param fullPath  The Vector of Strings where each string in the 
    *                  vector is one of elements in a path in the 
    *                  <code>ObjectState</code>.
    * @param index     Specifies the index of the last element in the 
    *                  vector <code>fullPath</code> to use.  All of the 
    *                  elements in the range 0 to <code>index</code> inclusive 
    *                  are used to specify the path in the 
    *                  <code>ObjectState</code> that is currently being used.  
    *                  For example, if <code>fullPath</code> is the vector 
    *                  {"a", "b", "c", "d", "e"} and <code>index = 2</code>, 
    *                  then, this method will use the path "a/b/c" as the 
    *                  path to an element in the <code>ObjectState</code>.
    * @param state     The <code>ObjectState</code> that is to be used. 
    * @param code      Code used to specify the action that is to be done 
    *                  to the item in the <code>ObjectState</code>.
    *                  <p>
    *                  The following codes are supported:
    *                  <ul>
    *                    <li>
    *                        {@link #INSERT INSERT}:  
    *                        Specifies that the value given by the 
    *                        parameter <code>value</code> should be 
    *                        inserted in the <code>ObjectState</code> 
    *                        at the specified path if and only if the 
    *                        <code>ObjectState</code> doesn't already 
    *                        contain a value at that location.
    *                    </li>
    *                    <li>
    *                        {@link #REPLACE REPLACE}:  
    *                        Specifies that the value given by the 
    *                        parameter <code>value</code> should be 
    *                        inserted in the <code>ObjectState</code> 
    *                        at the specified path overwriting any 
    *                        value previously stored at the location.
    *                    </li>
    *                    <li>
    *                        {@link #READ_TYPE READ_TYPE}:  
    *                        Specifies that this method should obtain 
    *                        the data type of the value at the specified 
    *                        location in the <code>ObjectState</code>.  
    *                        This data type is returned by this method.
    *                    </li>
    *                    <li>
    *                        {@link #READ_TYPE READ_VALUE}:  
    *                        Specifies that this method should obtain 
    *                        the value at the specified location in 
    *                        the <code>ObjectState</code>.  This value
    *                        is returned by this method.
    *                    </li>
    *                  </ul>
    * @param value     If the value of the parameter <code>code</code> is 
    *                  either {@link #INSERT INSERT} or 
    *                  {@link #REPLACE REPLACE}, this parameter specifies 
    *                  the value to store in given <code>ObjectState</code>.  
    *                  If the value of the parameter <code>code</code> is 
    *                  {@link #READ_VALUE READ_VALUE} or 
    *                  (@link #READ_TYPE READ_TYPE}, this parameter is used 
    *                  to store the data that is read.
    * @param typeSafe  Used to specify if type safety should be applied to 
    *                  this method.  This parameter is only used if the 
    *                  value of the parameter <code>code</code> is either 
    *                  {@link #INSERT INSERT} or {@link #REPLACE REPLACE} 
    *                  (which specifies that the <code>ObjectState</code> 
    *                  should be modified).  Then, if this parameter is 
    *                  <code>true</code>, the <code>ObjectState</code> 
    *                  will only be modified if the data type of the 
    *                  value already in the <code>ObjectState</code> is 
    *                  the same as the data type of the value that is 
    *                  going to be placed in the <code>ObjectState</code>.  
    *                  If this parameter is <code>false</code>, no type 
    *                  checking is done.
    * @param createPath Used to specify if the path given should be created 
    *                   if it does not exist.  If <code>true</code>, the 
    *                   path will be created if it does not exist.  If 
    *                   <code>false</code>, the path will not be created 
    *                   if it does not exist.  Instead an exception will 
    *                   be thrown.  Notice that the effectiveness of creating 
    *                   the path depends on the value of the parameters 
    *                   <code>typeSafe</code> and <code>code</code>.  That 
    *                   is, in some cases type safety has to be disabled, and 
    *                   overwriting has to be enabled to allow part of the 
    *                   path to be overwritten so that the new path can be 
    *                   created.  If the path cannot be created for any 
    *                   reason, an exception is thrown.
    * @throws Exception  If any errors occur in this method an Exception is 
    *                    immediately thrown.  Examples of some problems 
    *                    that could occur are, the path given might be 
    *                    empty, too long, or not correspond to an item in 
    *                    the <code>ObjectState</code>.  Also, the value of 
    *                    the parameter <code>code</code> could be invalid.  
    *                    The Exceptions thrown are designed so that their 
    *                    error messages can be understood by the end user.
    */
   private static void ModifyObjectStateImpl(Vector fullPath, int index, 
                                             ObjectState state,  
                                             String code, Object[] value, 
                                             boolean typeSafe, 
                                             boolean createPath) 
                                                throws Exception
   {
      if ( !(index < fullPath.size()) )
         return;
      
      String targetKey = fullPath.elementAt(index).toString();
      if (state.containsKey(targetKey))
      {
         Object curKeyVal = state.get(targetKey);
         if ( (curKeyVal instanceof ObjectState) )
         {
            //if there is another element in the path, recurse to that level
            if ((index+1) < fullPath.size())
               ModifyObjectStateImpl(fullPath, index+1, 
                                     (ObjectState)curKeyVal, 
                                     code, value, typeSafe, createPath);
            else //just modify the ObjectState
               doModify(state, targetKey, value, code, typeSafe, createPath);
            
            return;
         }
         else
         {
            //if there are still more elements in the path
            if ( (index < (fullPath.size() -1)))
            {
               //if the elements at the ends of the paths in the ObjectState 
               //can be changed to ObjectState objects, change the end of 
               //the path to an ObjectState and keep recursing
               if ( (code == REPLACE) && !typeSafe)
               {
                   ObjectState newState = new ObjectState();
                   state.reset(targetKey, newState);
                   ModifyObjectStateImpl(fullPath, index+1, newState, 
                                         code, value, typeSafe, createPath);
               }
               else
               {
                  String curPath = getCurrentPath(fullPath, index);
                  StringBuffer errorBuf = new StringBuffer();
                  if (createPath)
                  {
                     errorBuf.append("The path could not be created because ");
                     errorBuf.append("the subpath ");
                     errorBuf.append(curPath);
                     errorBuf.append(" ends in a non-ObjectState object.  ");
                     errorBuf.append("Disabling type safety, and enabling ");
                     errorBuf.append("data overwriting would allow the path ");
                     errorBuf.append("to be created.  ");
                     errorBuf.append("However, the data at the path ");
                     errorBuf.append(curPath);
                     errorBuf.append(" would be lost.");
                  }
                  else
                  {
                     errorBuf.append("The specified path is invalid ");
                     errorBuf.append("because it contains extra invalid ");
                     errorBuf.append("keys.  Its longest valid subpath is \"");
                     errorBuf.append(curPath);
                     errorBuf.append("\"");
                  }
               
                  throw new Exception(errorBuf.toString());
               }
            }
            else //'targetKey' is a valid key in the ObjectState 'state
               doModify(state, targetKey, value, code, typeSafe, createPath);
         }
      }
      else //if the ObjectState 'state' doesn't contain the key 'targetKey'
      {
         //check if a new path should be created for it
         if (createPath)
         {
            if ( index < (fullPath.size()-1) )
            {
               ObjectState newState = new ObjectState();
               state.insert(targetKey, newState);
               ModifyObjectStateImpl(fullPath, index+1, newState, code, 
                                     value, typeSafe, createPath);
            }
            else
               doModify(state, targetKey, value, code, typeSafe, createPath);
            
            return;
         }
         else //complain that the path contains extra elements
            throw new Exception("The specified path is invalid.  The " +
                                "following subpath is the first point " +
                                "where the path became invalid:  \""+
                                getCurrentPath(fullPath, index)+"\"");
      }
   }
   
   /**
    * Used to convert the given vector of strings, which represents a sequence 
    * of path elements, into one string representing the path.
    * <p>
    * This method was designed to be used in the 
    * <code>ModifyObjectStateImpl</code> method.  That is, if an error 
    * occurs in that method, this method is invoked to get a string 
    * representation of the path at which the error occured.
    * 
    * @param fullPath A Vector of Strings where each string is a path element.
    * @param index Used to specify the subset of the path elements to use 
    *              when constructing the string.  That is, the strings in 
    *              the vector <code>fullPath</code> with indices between 
    *              0 and <code>index</code> (inclusive) will be used.  
    *              For example, if <code>fullPath</code> is 
    *              {"a", "b", "c", "d"} and <code>index = 2</code>, then the 
    *              path returned would be "a/b/c".
    * @return A string representation of the given path.
    */
   private static String getCurrentPath(Vector fullPath, int index)
   {
      StringBuffer curPath = new StringBuffer();
      for (int i=0; i<=index; i++)
      {
         curPath.append(fullPath.elementAt(i));
         curPath.append("/");
      }
      
      return curPath.toString();
   }
   
   /**
    * This does the actual work of modifying the given 
    * <code>ObjectState</code> for the <code>ModifyObjectStateImpl</code> 
    * method.
    * 
    * @param state       The <code>ObjectState</code> that is going to 
    *                    be directly modified.
    * @param targetKey   The key in the given <code>ObjectState</code> 
    *                    whose value is to be modified.
    * @param value       This should be a one-element array.  If the parameter 
    *                    <code>code</code> specifies that data should be 
    *                    read from the <code>ObjectState</code>, the value 
    *                    read will be stored in <code>value[0]</code>.  
    *                    If the parameter <code>code</code> specifies that 
    *                    data should be written to the 
    *                    <code>ObjectState</code>, the data at 
    *                    <code>value[0]</code> is the data that is written 
    *                    to the <code>ObjectState</code>. 
    * @param code        One of 
                         {@link #INSERT INSERT}, 
    *                    {@link #REPLACE REPLACE}, 
    *                    {@link #READ_TYPE READ_TYPE}, or 
    *                    {@link #READ_VALUE READ_VALUE}.
    * @param typeSafe    If <code>true</code> type safety will be enabled.  
    *                    That is, an item in the <code>ObjectState</code> 
    *                    will not be modified if its new value's data type 
    *                    is different from its only value's data type.
    * @param createPath  If <code>true</code>, this specifies that an item 
    *                    with the key <code>targetKey</code> should 
    *                    be created if it does not exist in the 
    *                    <code>ObjectState</code> given.
    * @throws Exception  If any problems occur while trying to modify the 
    *                    <code>ObjectState</code>, an Exception is thrown 
    *                    with a message that is designed to be understood 
    *                    by the user.
    */
   private static void doModify(ObjectState state, Object targetKey, 
                                Object[] value, String code, 
                                boolean typeSafe, boolean createPath)
                                   throws Exception
   {
      Object curKeyVal = state.get(targetKey);
      if (curKeyVal == null)
      {
         if (createPath)
         {
            state.insert(targetKey, value[0]);
            curKeyVal = value[0];
         }
         else
            return;
      }
      
      String storedTypeName = curKeyVal.getClass().getName();
      if ( (code.equals(INSERT)) || (code.equals(REPLACE)) )
      {
         String newTypeName = "";
         boolean typeCheckFailed = false;
         if (value[0] != null)
         {
            newTypeName = value[0].getClass().getName();
            typeCheckFailed = 
               (typeSafe && 
               !newTypeName.equals(storedTypeName));
         }
         
         if (typeCheckFailed) 
            throw new Exception("The value could not be " +
                                "modified because its data " +
                                "type (" + newTypeName + 
                                ") does not match the data " +
                                "type of the value already " +
                                "stored in the state ("+
                                storedTypeName+").");
                     
         if (code.equals(INSERT))
            state.insert(targetKey, value[0]);
         else if (code.equals(REPLACE))
            state.reset(targetKey, value[0]);
      }
      else if (code.equals(READ_TYPE))
         value[0] = storedTypeName;
      else if (code.equals(READ_VALUE))
         value[0] = curKeyVal;
      else
         throw new Exception("The ObjectState modification " +
                             "code '"+code+"' is not supported");
   }
   
   /**
    * This method is used to get a String printout of the given 
    * <code>ObjectState</code>.
    * <p>
    * The String returned is formatted so that all of the keys are 
    * appended and indented, one on each line.  If specified, the values that 
    * correspond to the keys are also appended next to the keys.  The 
    * <code>prefix</code> and <code>suffix</code> parameters can be adjusted 
    * to change the way the values are printed.  Next, if a given value is 
    * an <code>ObjectState</code> object, the <code>ObjectState</code> is 
    * printed in the same way, with each line indented again.
    * 
    * @param state        The <code>ObjectState</code> whose state is to 
    *                     be represented in a String.
    * @param prefix       If <code>printValues</code> is set to 
    *                     <code>true</code> (to specify that key values 
    *                     should be placed in the output), this is the 
    *                     String that is appended to the output before a 
    *                     key's value is appended to the output.
    *                     <p>
    *                     For example, suppose that <code>prefix = "("</code>. 
    *                     Also, suppose that <code>suffix = ")"</code>.  
    *                     Then, if it is specified that key values should be 
    *                     placed in the output, the following is a sample 
    *                     output:
    *                     <p>
    *                     key1 (value1)<br>
    *                     key2 (value2)<br>
    *                     key3
    *                     <ul>
    *                       key3a (value3a)<br>
    *                       key3b (value3b)
    *                     </ul>
    *                     key4 (value4)
    *                     <p>
    *                     Here <code>key3's</code> value is an 
    *                     <code>ObjectState</code> so its contents are 
    *                     placed in a tree-like structure.
    *                     <p>
    *                     For the next example, suppose that 
    *                     <code>prefix = "(\n"</code>.  Also, suppose that 
    *                     <code>suffix = ")"</code>.  Then the following is 
    *                     a sample output.
    *                     <p>
    *                     key1
    *                     <ul>
    *                       (value1)
    *                     </ul>
    *                     key2
    *                     <ul>
    *                       (value2)
    *                     </ul>
    *                     key3
    *                     <ul>
    *                       key3a
    *                       <ul>
    *                         (value3a)
    *                       </ul>
    *                       key3b
    *                       <ul>
    *                         (value3b)
    *                       </ul>
    *                     </ul>
    *                     key4
    *                     <ul>
    *                       (value4)
    *                     </ul>
    *                     <p>
    *                     Notice that the "\n" instructs the value to be 
    *                     placed on a new line.  Then, the value is 
    *                     automatically indented.  Lastly, the "(" and ")" 
    *                     strings are placed around the value.
    * @param suffix       See <code>prefix</code>.  If it is specified that 
    *                     key values should be placed in the output, this is 
    *                     the String that is appended to the output before 
    *                     each value is appended to the output.
    * @param printValues  True if the the each key's value should be 
    *                     appended to the output.  False if only the keys 
    *                     should be appended to the output.  This is 
    *                     useful if only the layout of a particular 
    *                     <code>ObjectState</code> should be acquired.
    * 
    * @return A String representation of the given <code>ObjectState</code>.
    */
   public static String PrintObjectState(ObjectState state, 
                                         String prefix, String suffix, 
                                         boolean printValues)
   {
      String tab = "  ";
      return PrintObjectState(state, tab, tab, prefix, suffix, printValues);
   }
   
   /**
    * This method does the actual work of making a String representation 
    * of an <code>ObjectState</code> object.  This method is designed so 
    * that it can be used in a recursive manner which reflects the  
    * <code>ObjectState's</code> hierarchial layout.  
    * 
    * @param state        The <code>ObjectState</code> whose state is to 
    *                     be represented in a String.
    * @param indent       Each key is appended to the String, one on each 
    *                     line.  This <code>indent</code> string is appended 
    *                     before each key.  Typically a string like "  " is 
    *                     specified for this parameter.  As a result, the 
    *                     keys will be indented. 
    * @param addIndent    This is the String that is to be be appended to 
    *                     <code>indent</code> at each new level in the 
    *                     printout.  Specifying this to parameter to be a 
    *                     String like "  " would cause each level to be 
    *                     indented two spaces.
    * @param prefix       {@link #PrintObjectState(ObjectState, 
    *                     String, String, boolean) See} the public 
    *                     PrintObjectState() method. 
    * @param suffix       {@link #PrintObjectState(ObjectState, 
    *                     String, String, boolean) See} the public 
    *                     PrintObjectState() method.
    * @param printValues  {@link #PrintObjectState(ObjectState, 
    *                     String, String, boolean) See} the public 
    *                     PrintObjectState() method.
    * 
    * @return A String representation of the given <code>ObjectState</code>.
    * 
    * @see #PrintObjectState(ObjectState, String, String, boolean)
    */
   private static String PrintObjectState(ObjectState state, 
                                          String indent, String addIndent, 
                                          String prefix, String suffix, 
                                          boolean printValues)
   {
      //check the arguments
      if (state==null)
         throw new IllegalArgumentException("The state cannot be 'null'");
      
      if (indent == null)
         indent = "  ";
      
      if (prefix == null)
         prefix = "(";
      
      if (suffix == null)
         suffix = ")";
      
      //used to build the string representing the ObjectState
      StringBuffer buffer = new StringBuffer();
      buffer.append("/\n");
      
      //variables used with the printing
      String newIndent = indent + addIndent;
      Enumeration keys = state.getKeys();
      Object element;
      Object key;
      
      while (keys.hasMoreElements())
      {
         key = keys.nextElement();
         
         buffer.append(indent);
         buffer.append(key);
         
         element = state.get(key);
         if (element instanceof ObjectState)
         {
            //if the element is an ObjectState, print the ObjectState 
            //in the same way this method's ObjectState was printed
            buffer.append(PrintObjectState((ObjectState)element, 
                                           newIndent, addIndent, 
                                           prefix, suffix, 
                                           printValues));
         }
         else
         {
            //if the key's value should be printed
            if (printValues)
            {
               //if the prefix starts with '\n', append '\n', indent the 
               //right amount and append the prefix (minus the '\n' character)
               if ( (prefix.length() > 0) && 
                    (prefix.charAt(0) == '\n') )
               {
                  buffer.append("\n");
                  buffer.append(newIndent);
                  buffer.append(prefix.substring(1, prefix.length()));
               }
               else
               {
                  //otherwise just append a space and the prefix
                  buffer.append(" ");
                  buffer.append(prefix);
               }
            
               //then append the element and the suffix
               buffer.append(element);
               buffer.append(suffix);
               
               //then append the type
               buffer.append(" <");
               buffer.append(element.getClass().getName());
               buffer.append(">");
            }
            
            buffer.append("\n");
         }
      }
      
      return buffer.toString();
   }
   
   /**
    * Convience method that adds key/value pairs to the given ObjectState 
    * so that it mimics the hierarchy of the given file from the current 
    * filesystem.  This method is only intended to be used by this class's 
    * main method to create test ObjectState objects.
    * 
    * @param state The state whose values are to be set.
    * @param file The file to start with.  The ObjectState given will have 
    *             its values set so that the ObjectState's hierarchy mimics 
    *             the hierarchy at and "below" this file.
    */
   private static void buildStateFromFS(ObjectState state, File file)
   {
      if (file.isDirectory())
      {
         ObjectState newState = new ObjectState();
         state.insert(file.getName(), newState);
         
         File[] subFiles = file.listFiles();
         for (int i=0; i<subFiles.length; i++)
            buildStateFromFS(newState, subFiles[i]);
      }
      else
         state.insert(file.getName(), "");
   }
   
   /**
    * Testbed.
    * 
    * @param args Unused.
    * @throws Exception Throws the same exception that would be thrown 
    *                   inside the getResult() method of an Operator if 
    *                   the sequence of static methods used in this method 
    *                   were used in an Operator.
    */
   public static void main(String[] args) throws Exception
   {
      ObjectState state = new ObjectState();
      File file = new File("/home/kramer/temp/ObjectState/");
      buildStateFromFS(state, file);
      
      System.out.println(PrintObjectState(state, "(", ")", true));
      
      SetObjectStateValue("ObjectState/a/ac/acc/a/a/a/a", 
                          state, new Integer(5), true, false, true);
      SetObjectStateValue("ObjectState/a/ac/acc", 
                          state, "NEW_VALUE", true, true, true);
      
      System.out.println(PrintObjectState(state, "(", ")", true));
      
      Object val = GetObjectStateDataType("ObjectState/a/ac/acc", state);
      System.out.println("type = "+val);
      
      val = GetObjectStateValue("ObjectState/a/ac/acc", state);
      System.out.println("value = "+val);
      
      /*
      ModifyObjectState("ObjectState/a/ac/acc", state, 
                        REPLACE, "NEW_VALUE", true);
      
      System.out.println(PrintObjectState(state, "(", ")", true));
      
      Object val = ModifyObjectState("ObjectState/a/ac/acc", state, 
                                     READ_TYPE, null, true);
      System.out.println("type = "+val);
      
      val = ModifyObjectState("ObjectState/a/ac/acc", state, 
                              READ_VALUE, null, true);
      System.out.println("value = "+val);
      */
   }
}
