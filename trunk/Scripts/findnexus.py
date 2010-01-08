import os
import java.io.File 
from java.lang import System

#dir is an additional( first) directory searched along with several standard directories
#all arguments must be string, except verbose(0 or 1) and recurselevel( an integer or -1)
def findnexus( runNum, instrument='', Facility='', proposal='', collection='', verbose=0 ,dir='', ext='.nxs', recurselevel=6):
# search for instrxxxxext in dataDir union dir  and subdirectories up to recurselevel
  
   if( len(dir) > 0):
      res = findnex( dir, instrument , runNum, Facility,proposal, collection, verbose,ext, recurselevel)
      if( res != None):
         return res;

   D =java.lang.System.getProperty("Data_Directory","")
   if( len(D) >0):
      res = findnex( D, instrument , runNum, Facility,proposal, collection, verbose,ext, recurselevel)
      if( res != None):
         return res;
  
   D = System.getProperty("user.home","")
 
   if( len(D) > 0):
      res = findnex( D, instrument , runNum, Facility,proposal, collection, verbose,ext, recurselevel)
      if( res != None):
         return res;
   return ""
def listdir( dir): 
    return  (java.io.File(dir)).list()
def isfile(filename):
    X = java.lang.String(filename)
    if (java.io.File( X )).isFile():
       return 1
    return 0
def isdir( filename):
    if (java.io.File( java.lang.String(filename))).isDirectory():
      return 1
    return 0
#dir is the starting directory.  
def findnex( dir, instrument , runNum, Facility,proposal, collection, verbose,ext, recurselevel) :
   files =listdir( dir)

   n= len(files)
   for  i  in range(0,n):
      X =files[i] 
      if(len(X) >=0):
         if( isfile( dir+X)):
            if  (len(instrument) <=0) or  X.startswith(instrument) :
               if len(runNum) <=0 or X.rfind( runNum)>=0: 
                  if len(ext)<=0 or X.endswith(ext):
                     return dir+ X; 
         elif  recurselevel  <=0:
            print recurselevel
            return  None
         elif isdir( dir+X):
           
            res = findnex( dir+X+"/",instrument , runNum, Facility,proposal, collection, verbose,ext, recurselevel-1) 
            if  res != None:
               return res

   return None

#For testing
#print "Result="+findnexus("65","ARCS",dir="C:/ISAW/Sampleruns/")

    


