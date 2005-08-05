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
 * Revision 1.1  2005/08/05 16:15:27  kramer
 * Initial checkin.  This is a utility class that contains the static
 * methods that do the actual work of dealing with ObjectStates.  The
 * operators defined in this package wrap particular methods in this class.
 *
 */
package Operators.Special.ObjectState;

import gov.anl.ipns.ViewTools.Components.IPreserveState;
import gov.anl.ipns.ViewTools.Components.ObjectState;

import java.io.IOException;
import java.util.Enumeration;

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
            }
            
            buffer.append("\n");
         }
      }
      
      return buffer.toString();
   }
}
