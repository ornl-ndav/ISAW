�� t �This work was supported by the Intense Pulsed Neutron Source Division
of Argonne National Laboratory, Argonne, IL 60439-4845, USA.

For further information, see <http://www.pns.anl.gov/ISAW/>
t OIntense Pulsed Neutron Source
Bldg 360 L170
9700 S. Cass Ave.
Argonne, IL 60439t JPHammonds@anl.govt Argonne National Laboratoryt John Hammondst parseMacroStringt parseMacroStringt Q/IPNShome/hammonds/workspace2/ISAW/Operators/Generic/System/parseMacroString.javat V/IPNShome/hammonds/workspace2/ISAW/Operators/Generic/System/ParseStringMacroBase.classt parseMacroStringw   vr java.lang.String��8z;�B  xptThis operator will parse a string for macros that need to be substututed for values stored in the
system properties.  Each macro starts with the pair ${ and is terminated with a }.  The string 
contained in this macro must correspond to a key in the System Properties.  tFirst look for $.  Next character must be {.  Look for }.  Extract & trim string between these 
delimiters.  Use this string in System.getProperties(macroString).   Delete macro from the original
String and replace this with the value stored in the property.t=Once a macro is started with a $ it must be completed or a Parse Exeption is thrown.  
The macro must correspond with a key in the system properties.
No Space is allowed between $ & {.  i.e. should be ${ not $ {.
Excess space surrounding the macro string is trimmed.  i.e. ${user.home} is equivalent to
${ user.home }t �A ParseException is thrown if macro is started and not completed properly or if the macro does not
correspond to a key in the SystemProperties.t 6String with macros substituted for appropriate values.tFirst look for $.  Next character must be {.  Look for }.  Extract & trim string between these 
delimiters.  Use this string in System.getProperties(macroString).   Delete macro from the original
String and replace this with the value stored in the property.w       t inStrt String to Parset "hello ${user.name}"t LoadFilet String to be parsedt Macros,Utils,System