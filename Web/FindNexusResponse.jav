/**
* This file was hand created to "match" the xml output from the SNS findnexus service

* This file is run through the (java) schemagen program that creates a schema

   a) (rename this file FindNexusResponse.java

   b) "schemagen  FindNexusResponse.java"
   c) Rename this file FindNexusResponse.jav or else it will be overwritten
   d) Just above the Web directory
   e)  C:\ISAW> "xjc -p Web Web\generated\schema1.xsd"
   f) Compile everything

    
    

* This uses the JAXB( Java Arch for XML binding) system

*  The annotations are documented in javax.xml.annotation. 
*  
*  See the Util file for examples of using the JAXB system for xml inputs
*/

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement( name="findnexus")
public class FindNexusResponse
{
@XmlElement
 Vector<OneElement> file; 

class OneElement
{
@XmlElement
   String path;
@XmlElement
   String prop;
@XmlElement
   int run;
@XmlElement
   String coll;
@XmlElement
   String inst;
  
   
}
}
