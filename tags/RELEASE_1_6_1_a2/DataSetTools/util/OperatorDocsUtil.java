/*
 * File:  OperatorDocsUtil.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
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
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/07/07 14:22:13  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.util;

import DataSetTools.operator.DataSet.DataSetOperator;

import DataSetTools.operator.Operator;

import IsawHelp.HelpSystem.HTMLizer;

import java.util.Vector;


/**
 * Utility class to get the list of Operators and do things with it.  This
 * class uses HTMLizer to get ALL the Operators, both DataSet and Generic.
 */
public class OperatorDocsUtil {
  //~ Methods ******************************************************************

  /**
   * Formats the name of the Operator. Returns a system-specific slash
   * delimited name rather than a period delimited name.  In addition, the
   * names have a .java appended to the end.
   *
   * @return String following the style above.
   */
  public static String formatOperatorClassName( String opClass ) {
    opClass   = opClass.substring( opClass.indexOf( ' ' ), opClass.length(  ) );
    opClass   = opClass.replace( '.', '/' )
                       .trim(  );  //no periods
    opClass   = StringUtil.setFileSeparator( opClass );
    opClass   = opClass + ".java";

    return opClass;
  }

  /**
   * Method to run various static class methods.
   */
  public static void main( String[] args ) {
    String usage = "\nUSAGE: OperatorDocsUtil [-options]\n";

    if( args.length != 1 ) {
      System.out.println( usage );

      return;
    }

    if( args[0].equals( "--printOperators" ) ) {
      OperatorDocsUtil.printOperatorList(  );
    } else if( args[0].equals( "--printUnmatched" ) ) {
      OperatorDocsUtil.printUnmatchedOperatorList(  );
    } else if( args[0].equals( "--help" ) || args[0].equals( "-h" ) ) {
      StringBuffer help = new StringBuffer( "Options:\n\n" );

      help.append( "--printOperators\tPrints a list of all Operators" );
      help.append( "\n\n--printUnmatched\tPrints a list of all " );
      help.append( "Operators whose number of parameters\n\t\t\tdoes " );
      help.append( "not match the number of @param tags in their\n" );
      help.append( "\t\t\tgetDocumentation() method.\n" );
      System.out.println( help.toString(  ) );
    } else {
      System.out.println( usage );

      return;
    }
  }

  /**
   * Utility method to print the list of Operators.  Returns a system-specific
   * slash delimited name rather than a period delimited name.  In addition,
   * the names have a .java appended to the end, for easy command line
   * cut-paste-open.
   *
   * @return Vector of Operator names that follows the convention above.
   */
  public static Vector printOperatorList(  ) {
    Vector operators = HTMLizer.createOperatorVector(  );
    String opClass;
    Vector opNames   = new Vector( 200, 5 );

    for( int i = 0; i < operators.size(  ); i++ ) {
      opClass = formatOperatorClassName( 
          operators.elementAt( i ).getClass(  ).toString(  ) );

      opNames.add( opClass );
      System.out.println( opClass );
    }

    return opNames;
  }

  /**
   * Goes through the list of Operators and checks the number of parameter tags
   * in the getDocumentation() method against the actual number of parameters.
   * For DataSet operators, the number of parameter tags should be one more
   * than the number of parameters.
   *
   * @return Vector of Operators that has non-matching numbers of parameters
   *         and param tags.
   */
  public static Vector printUnmatchedOperatorList(  ) {
    Vector operators           = HTMLizer.createOperatorVector(  );
    int numParams;
    int numParamTags;
    Vector nonMatchedOperators = new Vector( 20, 4 );
    Operator op;
    String opClass;

    //for each Operator, parse its getDocumentation() for the number of param
    //tags.  Compare this to the number of parameters returned by
    //ScriptClassListHandler.  If they are not the same, add it to the Vector.
    for( int i = 0; i < operators.size(  ); i++ ) {
      op             = ( Operator )operators.elementAt( i );
      numParams      = op.getNum_parameters(  );
      numParamTags   = StringUtil.getNumOccurrences( 
          op.getDocumentation(  ), "@param" );

      if( numParams != numParamTags ) {  //they didn't match up
        opClass = formatOperatorClassName( 
            operators.elementAt( i ).getClass(  ).toString(  ) );

        //check if we have a DataSetOperator
        if( op instanceof DataSetOperator ) {
          if( ( numParams + 1 ) != numParamTags ) {  //no dice
            nonMatchedOperators.add( opClass );
            System.out.println( opClass );
          }
        } else {
          nonMatchedOperators.add( opClass );
          System.out.println( opClass );
        }
      }
    }

    return nonMatchedOperators;
  }
}
