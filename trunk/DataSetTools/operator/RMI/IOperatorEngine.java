/*
 * File:  IOperatorEngine.java
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

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * A client will use this class from a server to execute a method remotely.
 */
public interface IOperatorEngine extends Remote {
  //~ Methods ******************************************************************

  /**
   * Executes the given IROperator's getResult() method.
   *
   * @param iro The IROperator to use for the execution.
   *
   * @return The result of the calculation.
   *
   * @throws RemoteException
   */
  Object executeOperator( IROperator iro ) throws RemoteException;
}
