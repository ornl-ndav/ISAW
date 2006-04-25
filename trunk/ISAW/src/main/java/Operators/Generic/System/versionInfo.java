package Operators.Generic.System;

import IsawGUI.Isaw;
import DataSetTools.util.SharedData;
import java.net.*;
import java.util.*;
/**
 * This method is provided to allow code to more easily capture information about the 
 * program as it is running.  The method that returns an array of strings which holds the 
 * version information for ISAW & java as well as who ran it on what computer.  This should
 * assist in problem diagnosis since problems will be able to be traced back to the version
 * of the code.
 * @author hammonds
 *
 */
public class versionInfo {
	public static Vector<String> getVersionInfo (){
		Vector<String> retVal = new Vector<String>();
		
		String isawVersion = Isaw.getVersion(false);
		String buildDate = SharedData.BUILD_DATE;
		String javaVersion = System.getProperty("java.version");
		String osName = System.getProperty("os.name");
		String userName = System.getProperty("user.name");
		String nodeName = new String();
		try {
			nodeName =  InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException ex){
			nodeName = "Unknown_Host";
		}
		retVal.add(isawVersion);
		retVal.add(buildDate);
		retVal.add(javaVersion);
		retVal.add(osName);
		retVal.add(userName);
		retVal.add(nodeName);
		
		return retVal;
	}
	
}
