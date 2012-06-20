"""Converts an anvred .hkl file to a format used by GSAS.

Translation into jython of the anv2gsas.f program by Art Schulz (and others).
By Janik Zikovsky

$Id
"""
import os.path
import string
import math
from math import sin, cos, sqrt, acos, asin, atan, tan
import warnings
import shutil

#==================================================================
def split_noempty(input_string, sep=" "):
    """Helper function. Split a string but take out any empty entries."""
    s = string.split(input_string.strip(), sep)
    #Jython seems to return empty fields. Remove them
    s = [x for x in s if x != ""]
    return s


#==================================================================
class Reflection:
    """Data about one beam reflection."""

    #------------------------------------------------------------------
    def calculate_d_spacing(self, a_values):
        """Calculate the d-spacing corresponding to this reflection's hkl.

        Parameters:
            a_values: a tuple of the 6 reciprocal lattice parameters.
        """
        #Break out for convenience
        (a1, a2, a3, a4, a5, a6) = a_values
        h = self.h
        k = self.k
        l = self.l
        #Routine taken from Art Schulz FORTRAN code.
        dsp = h*h*a1 + k*k*a2 + l*l*a3 + \
                2.0*h*k*a4 + 2.0*h*l*a5 + \
                2.0*k*l*a6
        dsp = sqrt(dsp)
        dsp = 1.0/dsp
        #Save to the object
        self.d_spacing = dsp

    #------------------------------------------------------------------
    def correct_sigfosq(self, P, K):
        """Correct the sigfosq value (whatever that is)
        from the P and K parameters according to this equation:
        SIGFOSQ = SQRT(SIGFOSQ**2 + (XP*FOSQ)**2 + XK)

        Parameters:
            P, K: Floats.
        """

        #Near line 1003 of fortran program
        self.sigfosq = sqrt(self.sigfosq ** 2 + (P*self.fosq) ** 2 + K)


    #------------------------------------------------------------------
    def read_from_string(self, input_string, used_integrate=True):
        """Read one string from an .hkl file and interpret the result.

        Parameters:
            input_string: line from the .hkl file.
            used_integrate: set to True if it is the product of peak integration
            (from ISAW).

        """
        s = split_noempty(input_string)
        #Convert to floats
        val = [float(x) for x in s]



        if used_integrate:
            #Values came from INTSCD or INTEGRATE (which I think is in ISAW)
            #Read the values in, converting to int as needed
            self.h = float(val[0])
            self.k = float(val[1])
            self.l = float(val[2])
            if self.h == 0 and self.k == 0 and self.l == 0:
                #Marker for the end of the file. There is no more valid data
                return False
            self.fosq = val[3]
            self.sigfosq = val[4]
            self.x4 = int(val[5])
            self.lam = val[6]
            self.tbar = val[7]
            self.run_number = int(val[8])
            self.seqn = int(val[9]) #ISEQ
        else:
            #Data came from PEAKINT, whatever that is.
            self.isent = int(val[0])
            self.h = float(val[1])
            self.k = float(val[2])
            self.l = float(val[3])
            if self.h == 0 and self.k == 0 and self.l == 0:
                #Marker for the end of the file. There is no more valid data
                return False
            self.y01 = val[4]
            self.sigy01 = val[5]
            self.fosq = val[6]
            self.sigfosq = val[7]
            self.tbar = val[8]
            self.lam = val[9]
            self.run_number = int(val[10])
            self.seqn = int(val[11]) #ISEQ
            self.ireflag = int(val[12]) 
            #Bug in the FORTRAN code. X4 is not declared in this code path. I force it to 1.
            self.x4 = 1
        #Not sure why the histogram number was called X4, but here we go:
        self.histogram_number = self.x4

        #Reading was successful
        return True

    #------------------------------------------------------------------
    def write_to_binary(self, fileobj,pad2EOF):
        """Write out the binary data to the .S01, etc. file(s).

        Parameters:
            fileobj: file object to write to.
        """

        import struct
        #All these variables were defined at the start of the FORTRAN program, and are never changed.
        #They seem to be default/unused values in the binary record.
	ndiff = 0
	mul = 0
	icode = 11111
	incdnt = 8782348
	fotsq = 0.0
	fcsq = 0.0
	fctsq = 0.0
	phas = 0.0
	trans = 0.0
	extcor = 1.0
	wtfo = 0.0
	tof = 0.0
	xdet = 0.0
	ydet = 0.0
	peak = 0.0
	pkfrac = 1.0
	sigmai = 0.0
        scr1 = [0.0, 0.0, 0.0]
        scr2 = [0.0, 0.0, 0.0]

        #List of variable names to output
        output = "h,k,l,mul,icode,incdnt,d_spacing,lam,fosq," \
            "sigfosq,fotsq,fcsq,fctsq,phas,trans,extcor,wtfo,tof,xdet," \
            "ydet,peak,tbar,pkfrac,sigmai"

        format = ""
        values = []
        for s in output.split(","):
            #Get the value, either from the object or from that bunch of locals
            val = getattr(self, s, None)
            if val is None:
                val = locals()[s]
            values.append(val)
            #What format is good for it?
            if isinstance(val, int):
                format += 'i'
            elif isinstance(val, float):
                format += 'f'
            else:
                raise ValueError("Error! Wrong type of value found! Variable name '%s', value was %s" % (s, val))
        
        #Add 6 0.0 float values (the scr1 and scr2 array)
        format += 'ffffff'
        values += [0.0, 0.0, 0.0]
        values += [0.0, 0.0, 0.0]
        #And pad to 36 * 4 bytes by padding 6 more empty ints in there
        if pad2EOF:
           format += 'iiiiii'
           values += [0,0,0, 0,0,0]

        #Use struct to make a binary string
        #'>' means big endian order
        # < means little endian
        #print values
        s = struct.pack('<' + format, *values)
        #Write out that string (which is actually binary data)
        fileobj.write(s)

    


#==================================================================
def read_exp(exp_in, key, getfloats=False):
    """Look for an entry in a GSAS EXP file.

    Parameters:
        exp_in: EXP file object, already open.
        key: a 13-character string that is the key at the start of the line.
        getfloats: we expect 3 floats to be returned. Will return a single int otherwise

    Returns:
        either a tuple of 3 floats, or a single int. Depends on getfloats parameter
    """

    exp_in.seek(0)
    for line in exp_in.readlines():
        #Go through each line looking for the key
        if len(line) > 13:
            line_key = line[0:12]
            line_rest = line[13:]

            if line_key.strip() == key.strip():
                #Keys matchs!
                s = split_noempty(line_rest)
                #What format did we expect?
                if getfloats:
                    return (float(s[0]), float(s[1]), float(s[2]))
                else:
                    return int(s[0])


#==================================================================
def write_exp(exp_out, ihst, hkl_filename, nref, dmin):
    """Write out text to the GSAS EXP file.
    This subroutine writes histogram records to be included in the EXP file.
    
    Parameters:
        exp_out: EXP output file, already open
        ihst: histogram number
        hkl_filename: the .hkl file that was read.
        nref: number of reflections in this batch
        dmin: the smallest d-spacing in this batch of reflections.
    """
    #Common key start
    hist = "HST%3d" % ihst

    exp_out.write( hist + "  HFIL  " + hkl_filename + "\n" )
    exp_out.write( hist + '  HNAM  Histogram generated from ANVRED data\n')
    exp_out.write( hist + '  INST    1\n')
    exp_out.write( hist + ' IRAD     0\n')
    exp_out.write( hist + ' NREF %5d%10.5f\n' % (nref, dmin))
    exp_out.write( hist + 'HSCALE     1.0000        Y    0\n')


#==================================================================
def reciprocal(exp_in):
    """Calculate the reciprocal cell parameters from the real parameters.
    Reads values from the EXP file, already open.
    Parameters:
        exp_in: EXP file object, open.
    Returns:
        a1 to a6, the reciprocal lattice params
    """
    RAD = 57.29577951

    #These lattice parameters are read in from the EXP file.
    (a, b, c) = read_exp(exp_in, 'CRS1  ABC   ', getfloats=True)
    angles = read_exp(exp_in, 'CRS1  ANGLES', getfloats=True)
    #Convert to radians
    (alpha, beta, gamma) = [x/RAD for x in angles]

    #calculations are taken from Art Schulz's fortran program
    ca = cos(alpha)
    sa = sin(alpha)
    cb = cos(beta)
    sb = sin(beta)
    cg = cos(gamma)
    sg = sin(gamma)

    v = a*b*c*sqrt(1.0 - ca**2 - cb**2 - cg**2 + 2*ca*cb*cg)

    rec = [0,0,0,0,0,0,0]
    
    rec[1] = b*c*sa/v
    rec[2] = a*c*sb/v
    rec[3] = a*b*sg/v

    rec[4] = (cb*cg - ca)/(sb*sg)
    rec[4] = acos(rec[4])

    rec[5] = (ca*cg - cb)/(sa*sg)
    rec[5] = acos(rec[5])

    rec[6] = (ca*cb - cg)/(sa*sb)
    rec[6] = acos(rec[6])

    vs = 1.0/v

    a1 = rec[1]*rec[1]		#a* squared
    a2 = rec[2]*rec[2]		#b* squared
    a3 = rec[3]*rec[3]		#c* squared
    a4 = rec[1]*rec[2]*cos(rec[6])	#a*b*cos(gamma)
    a5 = rec[1]*rec[3]*cos(rec[5])	#a*c*cos(beta)
    a6 = rec[2]*rec[3]*cos(rec[4])	#b*c*cos(alpha)

    return (a1, a2, a3, a4, a5, a6)


#==================================================================
def convert_anvred_to_gsas(exp_filename, hkl_filename, P=0, K=0, used_integrate=True):
    """Do the conversion from anvred to GSAS.
    
    Parameters:
        exp_filename: .EXP GSAS file.
        hkl_filename: .hkl ANVRED output file
        P and K:  Used in the formula to correct sigfosq,
            SIG(Fo**2) = sqrt( SIGFOSQ**2 + ( P * FOSQ)**2 + K )
        used_integrate: set to True if it is the product of peak integration
            (from ISAW).
    """

    if not os.path.exists(exp_filename):
        raise IOError("The experiment file '%s', was not found." % (exp_filename))
    if not os.path.exists(hkl_filename):
        raise IOError("The .hkl file '%s', was not found." % (hkl_filename))

    #This is the GSAS file to read
    exp_in = open(exp_filename)

    #Get the name without extension
    (expname, extension) = os.path.splitext(exp_filename)

    #These data will be written into the output EXP file.
    nref = 0
    dmin = 1000.0
    htyp = 'SNT ' 
    
    #Check if there is already a histogram entry.
    nhist = read_exp(exp_in, ' EXPR  NHST ', getfloats=False)
    if not nhist is None:
        #Yes! So we don't want to alter it. Print out a warning."
        msg = """
            The EXP file will not be automatically updated with new
            histogram information. If you wish to update, open the EXP file
            with a text editor (Notepad) and include EXP-FILE.INC at the
            end of the EXP file. Then enter EXPEDT and type ''Y X''. This
            will replace the records at the beginning of the file with
            those at the end with the same keys, and will restore the file
            to keyed access."""
        print msg
        warnings.warn(msg)
        #We open a .INC file instead.
        exp_out = open(expname + ".INC", "w")
        if nhist == 1:
            htyp = 'SXC ' #Assume an existing x-ray data set.
    else:
        #This will be the modified EXP file.
        outname = expname + "_out.EXP"
        shutil.copyfile(expname + ".EXP", outname)
        #We append to the copy
        exp_out = open(outname, "a")


    #This is the output from ANVRED.
    hkl_file = open(hkl_filename)

    #Here the FORTRAN code gets a NRUN1 variable but never uses it.
    
    #Get the reciprocal lattice params. Read from the .EXP file
    a_values = reciprocal(exp_in)

    #Dictionary with all the open files
    myfiles = dict()


    #Read each reflection and write it out
    last_histogram = 0
    ref = Reflection()
    for line in hkl_file.readlines():
        if ref.read_from_string(line, used_integrate=used_integrate):
            #Some calculations need to be done
            ref.correct_sigfosq(P, K)
            ref.calculate_d_spacing(a_values)

            #What's the smallest d-spacing?
            if ref.d_spacing < dmin:
                dmin = ref.d_spacing

            #Make a binary file to write to
            if ref.histogram_number != last_histogram:
                if myfiles.has_key(ref.histogram_number):
                    #Just refer to the already open file
                    binary_out = myfiles[ref.histogram_number]
                    #This should probably not happen! Histograms should go in sequence.
                    msg = "Appending to a file that was previously started. This may mean the reflection numbers will be incorrect!."
                    print msg
                    warnings.warn(msg)
                else:
                    #If this is the 2nd record or later, make sure we write out
                    # the previous entry to the EXP file.
                    if last_histogram > 0:
                        write_exp(exp_out, last_histogram, hkl_filename, nref, dmin)
                    
                    #Create the file
                    binary_out = open(expname + ".s%02d" % ref.histogram_number, "wb")
                    myfiles[ref.histogram_number]  = binary_out
                    #Reset nref
                    nref = 0
                    dmin = 1000.0
                #Save the last histogram number
                last_histogram = ref.histogram_number
                
            #Write out the binary record
            ref.write_to_binary(binary_out, 1)
            #Count the reflections
            nref += 1

    #Write out the last EXP entry
    write_exp(exp_out, ref.histogram_number, hkl_filename, nref, dmin)

    #Some more goes to the EXP out file.
    exp_out.write(' EXPR  NHST %5d\n' % ref.histogram_number)
    #This seems to write out the type of histogram. Not sure about the details, simply copied from FORTRAN
    I = ref.histogram_number/12 + 1
    for j in xrange(1, I+1 ):
        if j < I: N = 12
        if j == I: N = ref.histogram_number - 12 * (I-1)
   #     exp_out.write(' EXPR  HTYP%1d  %4s\n' % (j, htyp))
        exp_out.write(' EXPR  HTYP%1d  ' % (j))
        for kk in xrange(1,N + 1):
            exp_out.write('%4s ' % ( htyp))
        exp_out.write('\n')

            
    #Close all files
    for (key, value) in myfiles.items():
        print "Output file written: %s" % value.name
        value.close()
    print "EXP file written: %s" % exp_out.name
    print "Rename(eliminate _out) to use with gsas"
    exp_out.close()


import unittest

#==================================================================
class Test_anv2gsas(unittest.TestCase):
    """Unit test for this module."""

    def test_normal(self):
        convert_anvred_to_gsas("ox80.EXP", "ox80.hkl")
        self.assertTrue(os.path.exists("ox80_out.EXP"), "EXP output file is created.")
        self.assertTrue(os.path.exists("ox80.s01"), "Binary output file is created.")

    def test_filenotfound1(self):
        self.assertRaises(IOError, convert_anvred_to_gsas, "ox80.EXP", "nonexistent.hkl")
        
    def test_filenotfound2(self):
        self.assertRaises(IOError, convert_anvred_to_gsas, "this_exp_file_does_not_exist", "ox80.hkl")

    def test_warnings(self):
        #Jython does not support the warnings.catch_warnings contect manager.
        # so can't test for warning.
        convert_anvred_to_gsas("OX80_2_after_anv2gsas.EXP", "ox80.hkl")
        self.assertTrue(os.path.exists("OX80_2_after_anv2gsas.INC"), "INC output file is created.")



#==================================================================
if __name__ == "__main__":
    unittest.main()
    
#    # --- Tests ---
#    print "Running tests of anv2gsas."
#    #Should run normally
#    convert_anvred_to_gsas("ox80.EXP", "ox80.hkl")
#    #Should give a warning and output a .INC file instead of a .EXP file
#    convert_anvred_to_gsas("OX80_2_after_anv2gsas.EXP", "ox80.hkl")
#    #Not found filenames:
#    convert_anvred_to_gsas("ox80", "nonexistent.hkl")
#    convert_anvred_to_gsas("this_exp_file_does_not_exist", "ox80.hkl")
