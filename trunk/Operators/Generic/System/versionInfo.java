package Operators.Generic.System;

import IsawGUI.Isaw;
import DataSetTools.util.SharedData;
import java.net.*;

public class versionInfo {
	public static String[] getVersionInfo (){
		String[] retVal = new String[6];
		
		String iawVersion = Isaw.getVersion(false);
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
		retVal[0] = iawVersion;
		retVal[1] = buildDate;
		retVal[2] = javaVersion;
		retVal[3] = osName;
		retVal[4] = userName;
		retVal[5] = nodeName;
		
		return retVal;
	}
	
}
