/*
 * File:  NxNodeUtils.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.24  2005/02/03 08:18:07  kramer
 * Fixed an error in the getISO8601String(Date date) method, the current date
 * was used instead of 'date'.
 *
 * Commented out the section that writes fractions of a second because
 * parseISO8601(String dateString) can't read a ISO8601 string written to a
 * fraction of a second.
 *
 * Revision 1.23  2005/02/03 06:36:50  kramer
 *
 * Added the parseISO8601(String dateString) and getISO8601String(Date date)
 * methods which get a Date from an String written in ISO8601 format and a
 * String written in ISO8601 format from a Date.
 *
 * Revision 1.22  2004/12/23 18:47:05  rmikk
 * Fixed indentation and added lines between code.
 *
 * Revision 1.21  2004/05/14 15:03:27  rmikk
 * Removed unused variables
 *
 * Revision 1.20  2004/03/15 03:36:01  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.19  2004/02/16 02:15:55  bouzekc
 * Removed unused import statements.
 *
 * Revision 1.18  2003/10/19 20:01:57  rmikk
 * Added some documentation
 * Fixed new units for time to be us
 * Fixed error in calculating factor with a numeric prefix
 *
 * Revision 1.17  2003/10/15 03:05:44  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.16  2003/09/14 16:35:34  rmikk
 * -incorporated leading factors like 100us for times and
 * lengths
 * -Finished incorporating the time units up to hours.
 *
 * Revision 1.15  2003/06/18 20:32:54  pfpeterson
 * Removed deprecated method.
 *
 * Revision 1.14  2003/06/18 19:38:55  pfpeterson
 * ShowW() now calls method in StringUtil.
 *
 * Revision 1.13  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.12  2002/11/20 16:14:50  pfpeterson
 * reformating
 *
 * Revision 1.11  2002/06/19 16:27:04  rmikk
 * Eliminated commented out code.
 * Eliminated reference to deprecated Date.getYear()
 * Fixed code alignment and spacing
 *
 * Revision 1.10  2002/06/19 15:55:22  rmikk
 * Fixed the order of Date formats so that the seconds, when
 * there, is found
 *
 * Revision 1.9  2002/04/01 20:45:41  rmikk
 * Fixed Date Format exception report in jdk1.4
 * Added some support for the Nexus NXChar type
 *
 * Revision 1.8  2002/02/26 15:46:41  rmikk
 * Fixed the utility Showw routine
 * Added a utility routine to getConversion factors
 *
 */
package NexIO;


import gov.anl.ipns.Util.Sys.StringUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import neutron.nexus.NexusFile;


/**
 * Utilities for inputting data from various datasources
 */
public class NxNodeUtils {
    String errormessage = "";
    // ERROR CODES
    public static final String ER_BADFILE = "Bad File name";
    public static final String ER_OPEN = "Cannot open or Connect";
  
    private void showConst() {
        System.out.println("CHAR=" + NexusFile.NX_CHAR);
        System.out.println("FLOAT32=" + NexusFile.NX_FLOAT32);
        System.out.println("FLOAT64=" + NexusFile.NX_FLOAT64);
        System.out.println("INT16=" + NexusFile.NX_INT16);;
        System.out.println("INT32=" + NexusFile.NX_INT32);
        System.out.println("INT8=" + NexusFile.NX_INT8);
        System.out.println("UINT16=" + NexusFile.NX_UINT16);
        System.out.println("UINT32=" + NexusFile.NX_UINT32);
        System.out.println("UINT8=" + NexusFile.NX_UINT8);
    }
    
    /**
     * Attempts to parse a date string with various formats including
     * "- ,/, and ." separators for month,year,day specifiers.
     * @param  DateString  a String that represents a data
     * @return The Date object corresponding to the DateString
     */
    public static Date parse(String DateString)
    {
       return parse(DateString," ");
    }
    
    /**
     * Attempts to parse a date string with various formats including
     * "- ,/, and ." separators for month,year,day specifiers.
     * @param  DateString  a String that represents a data
     * @return The Date object corresponding to the DateString
     */
    private static Date parse(String DateString, String dateTimeSeparator) {
        Date Result;
        SimpleDateFormat fmt = new SimpleDateFormat();
    
        fmt.setLenient(true);
    
        String Date_formats[] = {"yyyy", "yyyy-MM", "yyyy-MM-dd", 
                "yyyy.MM.dd",//"yyyy-MMM-dd" , "yyyy.MMM.dd" , 
                "yyyy-M-d", "yyyy.M.d", "yy.MM.dd", "yy.N.d",
                "MM/dd/yyyy", "MM/dd/yy", "M/d/yyyy", "M/d/yy",
                "MMM/dd/yyyy", "MMM/d/yyyy", "MMM/dd/yy", "MMM/d/yy",
                "dd-MMM-yyyy", "dd/MMM/yyyy", "dd-MMM-yy", "d/MMM/yy",
                "MMM dd,yyyy"};
    
        String Time_formats[] = { "HH:mm:ss", "HH:mm", "hh:mm a", "hh:mma",
                "H:mm:ss", "H:mm", "h:mm a", "h:mma",
                "H:m:ss", "H:m", "h:m a", "h:ma", "" };
    
        for (int i = 0; i < Date_formats.length; i++) {
            for (int k = 0; k < Time_formats.length; k++) {
                String pattern = Date_formats[ i] + 
                                    dateTimeSeparator + 
                                       Time_formats[  k ];
        
                pattern = pattern.trim();
        
                try {
                    fmt.applyPattern(pattern);
                    Result = fmt.parse(DateString);
                    if (Result != null) {
                        return Result;
                    }
                } catch (Exception s) {// let it drop on the floor
                }

            }
        }//for i
        return null;
    
    }
    
    /**
     * Attempts to parse the date string and create a Date object describing 
     * the date listed in the string.  This method is specifically made to 
     * parse date strings written in ISO8601 format.
     * @param dateString The string that represents the date.
     * @return The Date object describing the date and time listed in the 
     *         string or null if it can't be determined.
     */
    public static Date parseISO8601(String dateString)
    {
       return parse(dateString,"T");
    }
    /*
       String strToUse = dateString;
       long deltaTime = 0;
       
       if (dateString.endsWith("Z"))
       {
          strToUse = dateString.substring(0,dateString.length()-1);
          deltaTime = 0;
       }
       else
       {
          int timeZoneIndex = strToUse.indexOf('+');
          if (timeZoneIndex != -1)
          {
             
          }
          else
          {
             timeZoneIndex = strToUse.indexOf('-');
             if (timeZoneIndex != -1)
             {
                
             }
          }
       }
       
       StringTokenizer tokenizer = new StringTokenizer(dateString,".");
       int numTokens = tokenizer.countTokens();
        if (numTokens >= 2)
       {
          
       }
    }*/
    
    public static String getISO8601String(Date date)
    {
       GregorianCalendar calendar = new GregorianCalendar();
          calendar.setTime(date);
       
       int year = calendar.get(Calendar.YEAR);
       //one is added because Calendar.getInstance().get(Calendar.MONTH) = 0
       //  to refer to January but the ISO8601 standard uses 01 to represent 
       //  January
       int month = calendar.get(Calendar.MONTH)+1;
       int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
       
       int hour = calendar.get(Calendar.HOUR_OF_DAY);
       int minute = calendar.get(Calendar.MINUTE);
       int second = calendar.get(Calendar.SECOND);
       int milliseconds = calendar.get(Calendar.MILLISECOND);
       
       int zoneOffsetMs = calendar.get(Calendar.ZONE_OFFSET);
       
       
       DecimalFormat twoDigitFormat = new DecimalFormat("00");
       DecimalFormat fourDigitFormat = new DecimalFormat("0000");
       
       StringBuffer dateBuffer = new StringBuffer();
          dateBuffer.append(fourDigitFormat.format(year));
          dateBuffer.append("-");
          dateBuffer.append(twoDigitFormat.format(month));
          dateBuffer.append("-");
          dateBuffer.append(twoDigitFormat.format(dayOfMonth));
          dateBuffer.append("T");
          dateBuffer.append(twoDigitFormat.format(hour));
          dateBuffer.append(":");
          dateBuffer.append(twoDigitFormat.format(minute));
          
          if (second != 0 || milliseconds != 0)
          {
             dateBuffer.append(":");
             dateBuffer.append(twoDigitFormat.format(second));
          }
          
          /*
           * TODO Uncomment this when parseISO8601(String dateString)  
           *      can read a String written to the fraction of a second
          if (milliseconds != 0)
          {
             DecimalFormat threeDigitFormat = new DecimalFormat("000");
             dateBuffer.append(".");
             dateBuffer.append(threeDigitFormat.format(milliseconds));
          }
          */
          
          if (zoneOffsetMs == 0)
          {
             dateBuffer.append("Z");
          }
          else
          {
             //TODO Test if zoneOffsetMs>0 then +hh:mm should be used (and not 
             //  -hh:mm
             if (zoneOffsetMs>0)
                dateBuffer.append("+");
             else //then its negative
                dateBuffer.append("-");
             
             int totalPosOffsetMs = Math.abs(zoneOffsetMs);
             int totalPosOffsetSec = totalPosOffsetMs/1000;
             int totalPosOffsetMin = totalPosOffsetSec/60;
             int hourToUse = totalPosOffsetMin/60;
             int extraMins = totalPosOffsetMin-hourToUse*60;
             
             dateBuffer.append(twoDigitFormat.format(hourToUse));
             dateBuffer.append(":");
             dateBuffer.append(twoDigitFormat.format(extraMins));
          }
          
          return dateBuffer.toString();
    }
 
    /**
     * Attempts to create an array of the given type and length
     *
     * @param type the NexusFile type
     * @param length the length of the array
     *
     * @return an array of the appropriate type and length or null
     */
    public Object CreateArray(int type, int length) {
        errormessage = "";
        Object X;
    
        if (type == NexusFile.NX_FLOAT32)
            X = new float[ length];
      
        else if (type == NexusFile.NX_CHAR)
            X = new byte[ length];
      
        else if (type == NexusFile.NX_FLOAT64)
            X = new double[ length];
      
        else if (type == NexusFile.NX_INT16)
            X = new short[ length];
      
        else if (type == NexusFile.NX_INT32)
            X = new int[ length];
      
        else if (type == NexusFile.NX_INT8)
            X = new byte[ length];
      
        else if (type == NexusFile.NX_UINT16)
            X = new short[ length];
      
        else if (type == NexusFile.NX_UINT32)
            X = new int[ length];
      
        else if (type == NexusFile.NX_UINT8)
            X = new byte[ length];
      
        else {
            errormessage = "CAtype not supported" + type;
      
            return null;
        }
    
        return X;
    }
  
    /**
     * Returns error message or "" if none
     */
    public String getErrorMessage() {
        return errormessage;
    }
  
    /**
     * Fixes arrays whose type is Unsigned or NX_CHAR to an appropriate type
     */
    public Object fixUnsignedArray(Object X, int type, int length) {
        errormessage = "";
        if (type == NexusFile.NX_FLOAT32)
            return X;
      
        else if (type == NexusFile.NX_FLOAT64)
            return X;
      
        else if (type == NexusFile.NX_INT16)
            return X;
      
        else if (type == NexusFile.NX_INT32)
            return X;
      
        else if (type == NexusFile.NX_INT8)
            return X;
      
        else if (type == NexusFile.NX_CHAR)
            return X;
      
        else if (type == NexusFile.NX_UINT16) {
      
            int u[] = new int[ length];
            int x;
      
            for (int i = 0; i < length; i++) {
                x = ((short[]) X)[ i];
                if (x >= 0)
                    u[ i] = x;
                else
                    u[ i] = 2 * ((int) java.lang.Short.MAX_VALUE + 1) + x;
        
            }
            return u;
      
        } else if (type == NexusFile.NX_UINT32) {
      
            long u[] = new long[ length];
            long x;
      
            for (int i = 0; i < length; i++) {
                x = ((int[]) X)[ i];
                if (x >= 0)
                    u[ i] = x;
                else {
                    long y = (long) (java.lang.Integer.MAX_VALUE);

                    u[ i] = 2 * (long) (y + 1) + (long) x;
                }
            }
            return u;
      
        } else if (type == NexusFile.NX_UINT8) {
            short u[] = new short[ length];
            short x;
      
            for (int i = 0; i < length; i++) {
                x = ((byte[]) X)[ i];
                if (x >= 0)
                    u[ i] = x;
                else {
                    short y = java.lang.Byte.MAX_VALUE;
          
                    y++;
                    y = (short) (y + y);
                    y = (short) (y + x);
                    u[ i] = y; 
                }
            }

            return u;
      
        } else {
      
            errormessage = "fUtype not supported" + type;
            return null;
        }
    
    }  //fixUnsignedArray

    /**
     * test array for parse dates
     */
    public static void main(String args[]) {
      
        String[] ss = {"abc", "cde", "efg"};
    
        System.out.println("String Arry=" + StringUtil.toString(ss));
        NxNodeUtils NU = new NxNodeUtils();
        Calendar C = new GregorianCalendar();
    
        while (true) {
            System.out.println("Enter String form for a data");
            char c = 0;
            String S = "";
      
            try {
                while (c < 32)
                    c = (char) System.in.read();
                while (c >= 32) {
                    S = S + c;
                    c = (char) System.in.read();
                }
            } catch (Exception s) {// let it drop on the floor
            }
            Date D = NU.parse(S);
      
            if (D == null)
                System.out.println("Result is null");
            else {
                System.out.println("Result is" +
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(D));
                System.out.println("Date.toString=" + D);
        
                C.setTime(D);
                int year = C.get(Calendar.YEAR);
        
                if (year < 500) {
                    C.set(Calendar.YEAR, year + 1900);
                }
        
                System.out.println("cal Year=" + C.get(Calendar.YEAR) + "," +
                    C.get(Calendar.MONTH));
            }
        }
    }
  
    private static int getNumericStart(String S) {
 
        boolean decimalDone = false, expOn = false, leadSign = true;

        for (int i = 0; i < S.length(); i++) {
            char c = S.charAt(i);

            if (Character.isDigit(c)) { 
                leadSign = false;
            } else if ((c == '.') && !decimalDone && !expOn) {
                leadSign = false;
                decimalDone = true;
          
            } else if (("+-".indexOf(c) >= 0) && leadSign)
                leadSign = false;
          
            else if (("Ee".indexOf(c) >= 0) && !expOn) {
                expOn = true;
          
                leadSign = true;
                decimalDone = true;
          
            } else
                return i;
        
        }
        return S.length();
    }

    /**
     * Gives Factor to mulitply OldUnits to get NewUnits(ISAW units)
     * @param  OldUnits  the units that are non ISAW units
     * @param NewUnits  the ISAW units. Must be radians, meters,Kelvin,us,grams,
     *                   Mev,or steradian
     * @return the factor to multiply a quantity in old units to get to the new units
     
     */
    public static float getConversionFactor(String OldUnits, String NewUnits) {
        if (NewUnits.equals("radians"))
            return AngleConversionFactor(OldUnits.trim());
    
        if (NewUnits.equals("meters"))
            return LengthConversionFactor(OldUnits.trim());
    
        if (NewUnits.equals("Kelvin"))
            return TempConversionFactor(OldUnits.trim());

            //Solid angle
        if (NewUnits.equals("us"))
            return TimeConversionFactor(OldUnits.trim());

        if (NewUnits.equals("grams"))
            return MassConversionFactor(OldUnits.trim());
    
        if (NewUnits.equals("Mev"))
            return EnergyConversionFactor(OldUnits.trim());

        if (NewUnits.equals("steradian"))  //"sr is another symbol but if not this
            return 1.0f;                    //??????????????????????????????????
    
        else return 1.0f;
    }

    public static float AngleConversionFactor(String OldUnits) { //base radians
    
        if (";rad;radian;r;".indexOf(";" + OldUnits + ";") >= 0)
            return 1.0f;
    
        if (";deg;degree;degrees;d;".indexOf(";" + OldUnits + ";") >= 0)
            return (float) (java.lang.Math.PI / 180.0);
    
        return 1.0f;
    }

    public static float LengthConversionFactor(String OldUnits) { //base m
        int n = getNumericStart(OldUnits);
        float factor = 1;

        if (n > 0)
            try {
                factor = (new Float(OldUnits.substring(0, n))).floatValue();
 
            } catch (Exception ss) {
                factor = 1;
            }
        OldUnits = OldUnits.substring(n);
        if ("m;meter;met;".indexOf(OldUnits + ";") >= 0)
            return factor * 1;
    
        if ("cm;centim;centimeter;cmeter;cmet;100mm;100millim;100millimeter;".indexOf(OldUnits + ";") >= 0)
            return factor * .01f;
    
        if ("mm;millim;millimeter;100um;100microm;".indexOf(OldUnits + ";") >= 0)
            return factor * .001f;
    
        if ("um;umet;umeter;umeters;".indexOf(OldUnits) >= 0)
            return factor * .000001f;
    
        if ("in;inch;".indexOf(OldUnits + ";") >= 0)
            return  factor * (float) (1.0 / 254.0);
    
        if ("ft;foot;feet;".indexOf(OldUnits + ";") >= 0)
            return factor * (float) (12.0 / 254.0);
    
        return factor * 1.0f;
    
    }

    public static float TempConversionFactor(String OldUnits) { //base Kelvin
        if ("kelvin;k;".indexOf(OldUnits.toLowerCase() + ";") >= 0)
            return 1.0f;

        if ("deg;degrees;d;".indexOf(OldUnits + ";") >= 0)
            return (float) (100.0 / 212.0);

        return 1.0f;
    }

    public static float TimeConversionFactor(String OldUnits) { //us 
        int n = getNumericStart(OldUnits);
        float factor = 1;

        if (n > 0)
            try {
                factor = (new Float(OldUnits.substring(0, n))).floatValue();
 
            } catch (Exception ss) {
                factor = 1;
            }
        OldUnits = OldUnits.substring(n);
        if (OldUnits == null)
            return factor;
       
        if (OldUnits.length() < 1)
            return factor;
       
        if (OldUnits.charAt(0) == '*')
            OldUnits = OldUnits.substring(1);
   
        if ("s;sec;second;seconds;".indexOf(OldUnits + ";") >= 0)
            return factor * 1000000.0f;
      
        if ("ms;msec;mseconds;msecond;millis;millisec;milliseconds".indexOf(OldUnits + ";") >= 0)
            return factor * 1000.0f;
      
        if ("us;usec;useconds;usecond;micros;microsec;microseconds;".indexOf(OldUnits + ";") >= 0)
            return factor;
      
        if ("ns;nsec;nseconds;nanos;nanosec;nanosecond;nanoseconds;".indexOf(OldUnits + ";") >= 0)
            return factor * .001f;
      
        if ("min;minute;minutes;".indexOf(OldUnits + ";") >= 0)
            return factor * 60 * 1000000.0f;
      
        if ("hr;hour;hrs;hours;".indexOf(OldUnits + ";") >= 0)
            return 60 * factor * 1000000.0f;
      
        if ("day;days;".indexOf(OldUnits + ";") >= 0)
            return 24 * 60 * factor * 1000000.0f;
      
        return 1.0f;
    
    }

    public static float MassConversionFactor(String OldUnits) { //grams
        return 1.0f;
    }

    public static float EnergyConversionFactor(String OldUnits) { //Mev
        return 1.0f;
    }

    /**
     * Test program for NxNodeUtils.java
     */
    public static void main1(String args[]) {
        Object X;
    
        NxNodeUtils NU = new NxNodeUtils();

        System.out.println("Byte=" + java.lang.Byte.MAX_VALUE);
        System.out.println("Short=" + java.lang.Short.MAX_VALUE);
        System.out.println("int=" + java.lang.Integer.MAX_VALUE);
        System.out.println("long=" + java.lang.Long.MAX_VALUE);
        char c = 0;

        X = null;
        NU.showConst();
        c = 'x';
        while (c != 'x') {
            System.out.println("Enter option desired");
            System.out.println("   1. Create int array");
            System.out.println("     2. Create float array");
            System.out.println("     3. Create byte array");
            System.out.println("     4. Create short array");
            System.out.println("     5. fix unsigned int array");
            System.out.println("      6. show fixed array");
            System.out.println("     7.show error");

            c = 0;
            try {
                while (c < 32)
                    c = (char) System.in.read();
            } catch (IOException s) {
                c = 0;
            }
            if (c == '1') {
                X = NU.CreateArray(NexusFile.NX_INT32, 10);
                for (int i = 0; i < 10; i++)
                    ((int[]) X)[ i] = (int) i;
            } else if (c == '2') {
                X = NU.CreateArray(NexusFile.NX_FLOAT32, 10);
                for (int i = 0; i < 10; i++)
                    ((float[ ]) X)[ i] = i;
            } else if (c == '3') {
                X = NU.CreateArray(NexusFile.NX_INT8, 10);
                for (int i = 0; i < 10; i++)
                    ((long[]) X)[ i] = i;
            } else if (c == '4') {
                X = NU.CreateArray(NexusFile.NX_INT16, 10);
                for (int i = 0; i < 10; i++)
                    ((short[]) X)[ i] = (short) i;
            } else if (c == '5') {
                X = NU.CreateArray(NexusFile.NX_UINT32, 10);
                for (int i = 0; i < 10; i++)
                    ((int[]) X)[ i] = i;
                ((int[]) X)[ 0] = -3000;
                X = NU.fixUnsignedArray(X, NexusFile.NX_UINT32, 10);
            } else if (c == '6') {
                System.out.println(StringUtil.toString(X));
            } else if (c == '7')
                System.out.println(NU.getErrorMessage());
        }
    }
}
