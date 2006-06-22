/*
 * Copyright 1997 Marty Hall
 * This appears in Core Web Programming from
 * Prentice Hall Publishers, and may be freely used
 * or adapted. 1997 Marty Hall, hall@apl.jhu.edu.
 *
 * Original log:
 * Header: /user/rsch/graham/java/packages/compiler/version1.2/RCS/Exec.java,v 1.2 1998/11/06 10:37:03 graham Exp
 *
 * Revision 1.2  1998/11/06 10:37:03  graham
 * Added support for multiple packages and inner classes.
 *
 * Revision 1.1  1998/01/07 10:00:00  graham
 * Initial revision
 *
 * $Log$
 * Revision 1.1  2004/01/28 22:09:57  bouzekc
 * Added to CVS.  Changed package to ExtTools.compiler, moved original log,
 * changed at symbol in email in author tag to "_at_".  Formatted code.
 * Added copyright info (for Marty Hall) at top of file.
 *
 *
 */
package ExtTools.compiler;

import java.io.*;


/**
 * A class that eases the pain of running external processes from applications.
 * Lets you run a program three ways:
 * 
 * <OL>
 * <li>
 * <B>exec</B>: Execute the command, returning immediately even if the command
 * is still running. This would be appropriate for printing a file.
 * </li>
 * <li>
 * <B>execWait</B>: Execute the command, but don't return until the command
 * finishes. This would be appropriate for sequential commands where the first
 * depends on the second having finished (e.g. <CODE>javac</CODE> followed by
 * <CODE>java</CODE>).
 * </li>
 * <li>
 * <B>execPrint</B>: Execute the command and print the output. This would be
 * appropriate for the UNIX command <CODE>ls</CODE>.
 * </li>
 * </ol>
 * 
 * Note that the PATH is not taken into account, so  you must specify the
 * <B>full</B> pathname to the command, and shell builtin commands will not
 * work. For instance, on Unix the above three examples might look like:
 * 
 * <OL>
 * <li>
 * <PRE>Exec.exec("/usr/ucb/lpr Some-File");</PRE>
 * </li>
 * <li>
 * <PRE>
 *        Exec.execWait("/usr/local/bin/javac Foo.java");
 *        Exec.execWait("/usr/local/bin/java Foo");
 *        </PRE>
 * </li>
 * <li>
 * <PRE>Exec.execPrint("/usr/bin/ls -al");</PRE>
 * </li>
 * </ol>
 * 
 * The <A HREF="../../compiler/Exec.java">source code</A> is available.
 *
 * @author Marty Hall (<A HREF="mailto:hall@apl.jhu.edu">
 *         hall_at_apl.jhu.edu</A>)
 * @version 1.0 1997
 */
public class Exec {
  //~ Static fields/initializers ***********************************************

  //----------------------------------------------------
  private static boolean verbose = true;

  //~ Methods ******************************************************************

  /**
   * Determines if the Exec class should print which commands are being
   * executed, and print error messages if a problem is found. Default is
   * true.
   *
   * @param verboseFlag true: print messages. false: don't.
   */
  public static void setVerbose( boolean verboseFlag ) {
    verbose = verboseFlag;
  }

  /**
   * Tests whether Exec will print status messages.
   *
   * @return true if Exec will print status messages
   */
  public static boolean getVerbose(  ) {
    return ( verbose );
  }

  /**
   * Starts a process to execute the command. Returns immediately, even if the
   * new process is still running.
   *
   * @param command The <B>full</B> pathname of the command to be executed. No
   *        shell builtins (e.g. "cd") or shell meta-chars (e.g. "&gt;")
   *        allowed.
   *
   * @return false if a problem is known to occur, but since this returns
   *         immediately, problems aren't usually found in time. Returns true
   *         otherwise.
   */
  public static boolean exec( String command ) {
    return ( exec( command, false, false ) );
  }

  /**
   * Starts a process to execute the command. Prints all output the command
   * gives.
   *
   * @param command The <B>full</B> pathname of the command to be executed. No
   *        shell builtins or shell meta-chars allowed.
   *
   * @return false if a problem is known to occur, either due to an exception
   *         or from the subprocess returning a non-zero value. Returns true
   *         otherwise.
   */
  public static boolean execPrint( String command ) {
    return ( exec( command, true, false ) );
  }

  /**
   * Starts a process to execute the command. Waits for the process to finish
   * before returning.
   *
   * @param command The <B>full</B> pathname of the command to be executed. No
   *        shell builtins or shell meta-chars allowed.
   *
   * @return false if a problem is known to occur, either due to an exception
   *         or from the subprocess returning a non-zero value. Returns true
   *         otherwise.
   */
  public static boolean execWait( String command ) {
    return ( exec( command, false, true ) );
  }

  //----------------------------------------------------
  // This creates a Process object via
  // Runtime.getRuntime.exec(). Depending on the
  // flags, it may call waitFor on the process
  // to avoid continuing until the process terminates,
  // or open an input stream from the process to read
  // the results.
  private static boolean exec( 
    String command, boolean printResults, boolean wait ) {
    if( verbose ) {
      printSeparator(  );
      System.out.println( "Executing '" + command + "'." );
    }

    try {
      // Start running command, returning immediately.
      Process p = Runtime.getRuntime(  ).exec( command );

      // Print the output. Since we read until
      // there is no more input, this causes us
      // to wait until the process is completed
      if( printResults ) {
        BufferedInputStream buffer   = new BufferedInputStream( 
            p.getInputStream(  ) );
        BufferedReader commandResult = new BufferedReader( 
            new InputStreamReader( buffer ) );
        String s                     = null;

        try {
          while( ( s = commandResult.readLine(  ) ) != null ) {
            System.out.println( "Output: " + s );
          }

          commandResult.close(  );

          if( p.exitValue(  ) != 0 ) {
            if( verbose ) {
              printError( command + " -- p.exitValue() != 0" );
            }

            return ( false );
          }

          // Ignore read errors; they mean process is done
        } catch( Exception e ) {}

        // If you don't print the results, then you
        // need to call waitFor to stop until the process
        // is completed
      } else if( wait ) {
        try {
          System.out.println( " " );

          int returnVal = p.waitFor(  );

          if( returnVal != 0 ) {
            if( verbose ) {
              printError( command );
            }

            return ( false );
          }
        } catch( Exception e ) {
          if( verbose ) {
            printError( command, e );
          }

          return ( false );
        }
      }
    } catch( Exception e ) {
      if( verbose ) {
        printError( command, e );
      }

      return ( false );
    }

    return ( true );
  }

  //----------------------------------------------------
  private static void printError( String command, Exception e ) {
    System.out.println( 
      "Error doing exec(" + command + "): " + e.getMessage(  ) );
    System.out.println( "Did you specify the full " + "pathname?" );
  }

  /**
   * DOCUMENT ME!
   *
   * @param command DOCUMENT ME!
   */
  private static void printError( String command ) {
    System.out.println( "Error executing '" + command + "'." );
  }

  //----------------------------------------------------
  private static void printSeparator(  ) {
    System.out.println( "==============================================" );
  }
  //----------------------------------------------------
}
