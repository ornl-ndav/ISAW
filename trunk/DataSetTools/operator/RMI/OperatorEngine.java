/*
 * File:  OperatorEngine.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
 *
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
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2003/08/15 04:06:12  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.operator.RMI;

import java.rmi.*;
import java.rmi.server.*;


/**
 * The implementation of IOperatorEngine, used by a client to remotely execute
 * an Operator.
 */
public class OperatorEngine extends UnicastRemoteObject
  implements IOperatorEngine {
  //~ Constructors *************************************************************

  /**
   * Creates a new OperatorEngine object.
   *
   * @throws RemoteException
   */
  public OperatorEngine(  ) throws RemoteException {
    super(  );
  }

  //~ Methods ******************************************************************

  /**
   * Executes the IROperator's getResult().
   *
   * @param iro The IROperator to use.
   *
   * @return The result of getResult().
   */
  public Object executeOperator( IROperator iro ) {
    return iro.getResult(  );
  }

  /**
   * Main class needed for remote execution of this OperatorEngine.  Binds the
   * String OperatorEngine to IOperatorEngine so the RMI registry can use it.
   *
   * @param args Unused.
   */
  public static void main( String[] args ) {
    if( System.getSecurityManager(  ) == null ) {
      System.setSecurityManager( new RMISecurityManager(  ) );
    }

    String name = "//192.168.0.1/IOperatorEngine";

    try {
      IOperatorEngine engine = new OperatorEngine(  );

      Naming.rebind( name, engine );
      System.out.println( "OperatorEngine bound" );
    } catch( Exception e ) {
      System.err.println( "OperatorEngine exception: " + e.getMessage(  ) );
      e.printStackTrace(  );
    }
  }
}
