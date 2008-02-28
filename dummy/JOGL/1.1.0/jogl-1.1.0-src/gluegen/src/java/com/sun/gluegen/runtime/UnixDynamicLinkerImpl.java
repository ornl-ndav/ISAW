/* !---- DO NOT EDIT: This file autogenerated by com\sun\gluegen\JavaEmitter.java on Mon Jul 31 16:26:59 PDT 2006 ----! */

package com.sun.gluegen.runtime;

import com.sun.gluegen.runtime.*;

public class UnixDynamicLinkerImpl implements DynamicLinker
{

  public static final int RTLD_LAZY = 0x00001;
  public static final int RTLD_NOW = 0x00002;
  public static final int RTLD_NOLOAD = 0x00004;
  public static final int RTLD_GLOBAL = 0x00100;
  public static final int RTLD_LOCAL = 0x00000;
  public static final int RTLD_PARENT = 0x00200;
  public static final int RTLD_GROUP = 0x00400;
  public static final int RTLD_WORLD = 0x00800;
  public static final int RTLD_NODELETE = 0x01000;
  public static final int RTLD_FIRST = 0x02000;

  /** Interface to C language function: <br> <code> int dlclose(void * ); </code>    */
  private static native int dlclose(long arg0);

  /** Interface to C language function: <br> <code> char *  dlerror(void); </code>    */
  private static native java.lang.String dlerror();

  /** Interface to C language function: <br> <code> void *  dlopen(const char * , int); </code>    */
  private static native long dlopen(java.lang.String arg0, int arg1);

  /** Interface to C language function: <br> <code> void *  dlsym(void * , const char * ); </code>    */
  private static native long dlsym(long arg0, java.lang.String arg1);


  // --- Begin CustomJavaCode .cfg declarations
  public long openLibrary(String pathname) {
    // Note we use RTLD_GLOBAL visibility to allow this functionality to
    // be used to pre-resolve dependent libraries of JNI code without
    // requiring that all references to symbols in those libraries be
    // looked up dynamically via the ProcAddressTable mechanism; in
    // other words, one can actually link against the library instead of
    // having to dlsym all entry points. System.loadLibrary() uses
    // RTLD_LOCAL visibility so can't be used for this purpose.
    return dlopen(pathname, RTLD_LAZY | RTLD_GLOBAL);
  }
  
  public long lookupSymbol(long libraryHandle, String symbolName) {
    return dlsym(libraryHandle, symbolName);
  }
  
  public void closeLibrary(long libraryHandle) {
    dlclose(libraryHandle);
  }
  // ---- End CustomJavaCode .cfg declarations

} // end of class UnixDynamicLinkerImpl
