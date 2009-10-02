C***********************  statsSNS  **********************************

!!!   Linux version:  A.J. Schultz		June, 2004


C	Program to print a summary of intensity statistics.
C	A. Schultz   6/26/85
C       Modified by R. Goyette 9/21/89 to create an EasyPlot File
C       to display count above 3*Sigma versus Wavelength.

!!!   SNS version: September, 2009



	INTEGER H,HSTNUM
	INTEGER REFLAG,SEQNUM
	INTEGER DETNUM(100), DN				!DN added 4/21/08

	REAL	INTI,L1,L2

	CHARACTER EXPNAM*14

! Labelled common for 100 SNS detectors		3/21/08
	common /detect/ nrows(100), ncols(100), width(100),
     1	HEIGHT(100), DEPTH(100), DIST(100), CenterX(100),
     2	CenterY(100), CenterZ(100), BaseX(100), BaseY(100),
     3	BaseZ(100), UpX(100), UpY(100), UpZ(100), nod
     
! Labelled common for the incident spectrum	July, 2009
	common /spect_data/ xtime(1700), counts(1700)
     
	COMMON    /PEAKS/  col, row, chan, L2, twoth, az, dsp
	COMMON    /INSPAR/ L1, TZERO
	COMMON    /MONI/   MONCNT
	COMMON    /SET/    CHI, PHI, OMEGA, DETA
	COMMON    /DATA5/  MNSUM
	COMMON    /DATA8/  HSTNUM
	COMMON    /DATA11/ NRUN, DETNUM, dn, EXPNAM
	COMMON    /DATA12/ SEQNUM, H, K, L, X, Y, WL,
     1			   IPKOBS, INTI, SIGI, REFLAG

	DATA	HOM /0.39559974/

	CHARACTER  REFNAM*60,ANS*1
	INTEGER OLD
	INTEGER SUM,sumtot,SIG3,SIG3T,SIG5,SIG5T,SIG10,SIG10T
	DIMENSION IWL(1000),IRUN(100)


	DO 1,I=1,1000
		IWL(I)=0
 1	CONTINUE

	JEND = 0
	MRUN = 0
	OLD = 0
	SUMTOT = 0
	SIG3T = 0
     	SIG5T = 0
	SIG10T = 0

	open(unit=15, file='stats.out', status='REPLACE')
	
105	WRITE (*, 100)
100	FORMAT (/,'Enter "filename.filetype" : ',$)
	write (15, 100)
	READ (*, 110) REFNAM
110	FORMAT (A)
	OPEN(UNIT=11,file=REFNAM,status='OLD',ERR=115)
	write (15, 110) REFNAM
	
	write (*, 1100)
1100	format (/,'Enter dmin: ',$)
	write (15, 1100)
	read (*, *) dmin
	write (15, '(F5.2)') dmin
	
!  Initial read of integrate file to get instrument and detectors calibration.
	call readrefl_SNS_header (11, ieof)
	write (*, *) ' '
		
	GO TO 118
	
115	WRITE (*, *) 'Integrate file does not exist.'
	GO TO 105

118	WRITE (*, 120)
120	FORMAT (//,' ***  SUMMARY OF INTENSITY STATISTICS  ***'//,
     1  '      NRUN     TOTAL      3SIG      5SIG     10SIG'/)
	write (15, 120)

101	CALL READREFL_SNS (11, IEOF)

	IF (IEOF.EQ.1) GO TO 999

2001	IF ( OLD.EQ.NRUN ) GO TO 201 

	GO TO 190

999	JEND = 1

190	IF (OLD.GT.0) THEN
		WRITE (*, 199) OLD,SUM,SIG3,SIG5,SIG10
199		FORMAT(5I10)
		write (15, 199) OLD,SUM,SIG3,SIG5,SIG10

		SUMTOT = SUMTOT + SUM
		SIG3T = SIG3T + SIG3
		SIG5T = SIG5T + SIG5
		SIG10T = SIG10T + SIG10
	END IF

	IF (JEND.EQ.1) GO TO 300

	OLD = NRUN
	SUM = 0
	SIG3 = 0
	SIG5 = 0
	SIG10 = 0
	GO TO 101

201	if (dsp .lt. dmin) go to 101
	SUM = SUM + 1
	IF (INTI .LT. 3.0*SIGI) GO TO 101
	IUNIT=INT(WL*10)+1
	IWL(IUNIT)=IWL(IUNIT)+1
	SIG3 = SIG3 + 1
	IF (INTI .LT. 5.0*SIGI) GO TO 101
	SIG5 = SIG5 + 1
	IF (INTI .LT. 10.0*SIGI) GO TO 101
	SIG10 = SIG10 + 1
	GO TO 101


300	WRITE (*, 310) SUMTOT,SIG3T,SIG5T,SIG10T
310	FORMAT('    TOTALS',4I10)
	write (15, 310) SUMTOT,SIG3T,SIG5T,SIG10T

	OPEN(UNIT=20,file='PEAKSWL.DAT',status='OLD',ERR=320)
	GO TO 505
320	OPEN(UNIT=20,file='PEAKSWL.DAT',status='NEW')

505	WRITE (20,510)
510	FORMAT(11H'O' 0.0 0.0)

	DO 301 I=1,100
	  WRITE(20,302) FLOAT(I)/10.,IWL(I)
 302	  FORMAT( F5.2,10X,I6)
 301	CONTINUE

	WRITE (20,511)
 511	FORMAT(12H -999. -999.)
	CLOSE(UNIT=20)

	WRITE (*, *) ' '
	WRITE (*, *) 'The file PEAKSWL.DAT contains the number of peaks'
	WRITE (*, *) 'greater than 3sigma per 0.1 Angstrom'

	STOP
	END

C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------


	SUBROUTINE READREFL_SNS_HEADER (IUNIT,IEOF)

!!!	This subroutine will read the first 6 lines of a peaks or
!!!	integrate file with the SNS format.
!!!	A. J. Schultz	June, 2008

!	The first variable of each record is ISENT.

!	Linux version, January 2002, A.Schultz

!	ISENT = 0 --> variable name list for describing the histogram
!	      = 1 --> variable values of above list
!	      = 2 --> variable name list for a reflection
!	      = 3 --> variable values of above list
!	      = 4 --> variable name list for parameters for detectors
!	      = 5 --> variable values of parameters for detectors
!	      = 6 --> variable name list: L1    T0_SHIFT
!	      = 7 --> variable values: L1    T0_SHIFT

!	IEOF = 0 --> not end-of-file
!	     = 1 --> end-of-file


	INTEGER DETNUM(100), DN				!DN added 4/21/08

	REAL	L1

	CHARACTER EXPNAM*14

! Labelled common for 100 SNS detectors		3/21/08
	common /detect/ nrows(100), ncols(100), width(100),
     1	HEIGHT(100), DEPTH(100), DIST(100), CenterX(100),
     2	CenterY(100), CenterZ(100), BaseX(100), BaseY(100),
     3	BaseZ(100), UpX(100), UpY(100), UpZ(100), nod
	COMMON    /INSPAR/ L1,TZERO
	COMMON    /DATA11/ NRUN,DETNUM,dn,EXPNAM

	

	CHARACTER*138 TEXT
	
	IEOF=0
	
!	write (*, 50)
50	format(//,'Reflection file header:')

!  Read the first line
	READ (IUNIT, 100, END=999) TEXT
100	FORMAT(A)
!	write (*, 100) TEXT

!  Read the second line, ISENT = 6
	READ (IUNIT,100,END=999) TEXT
!	write (*, 100) TEXT
	
!  Read the third line, ISENT = 7
	READ (IUNIT,100,END=999) TEXT

	READ (TEXT,150) ISENT
150	FORMAT(I1)

! ISENT=7
	IF (ISENT.EQ.7) THEN
		READ (text, *) ISENT, L1, TZERO
	END IF

!	write (*, 100) TEXT

!  Read the fourth line, ISENT = 4
	READ (IUNIT,100,END=999) TEXT

!  Read lines with ISENT = 5
	nod = 0				!number of detectors
300	read (iunit, 100) text      	!ISENT=5

	READ (TEXT,150) ISENT
          IF (ISENT.NE.5) THEN	!End of reading detector parameters
                  BACKSPACE (IUNIT)
!                  write (*, *) ' '
!                  write (*, *) 'There are ',nod,' detectors.'
!                  write (*, *)
                  RETURN
          END IF

!	write (*, 100) TEXT
          
	  nod = nod + 1
	  i = nod
	  READ (TEXT, *) ISENT, detnum(i), NROWS(i), NCOLS(i),
     1	WIDTH(i), HEIGHT(i), DEPTH(i), DIST(i),
     2	CenterX(i), CenterY(i), CenterZ(i),
     3	BaseX(i), BaseY(i), BaseZ(i),
     4	UpX(i), UpY(i), UpZ(i) 
         GO TO 300
	
999	IEOF=1	!END OF FILE
	RETURN	
	END


C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------


	SUBROUTINE READREFL_SNS (IUNIT,IEOF)

!!!	Read a peaks or integrate file with the SNS format.
!!!	A. J. Schultz	April, 2008


!	Read a reflection from a reflection file.
!	The first variable of each record is ISENT.

!	Linux version, January 2002, A.Schultz

!	ISENT = 0 --> variable name list for describing the histogram
!	      = 1 --> variable values of above list
!	      = 2 --> variable name list for a reflection
!	      = 3 --> variable values of above list
!	      = 4 --> variable name list for parameters for detectors
!	      = 5 --> variable values of parameters for detectors
!	      = 6 --> variable name list: L1    T0_SHIFT
!	      = 7 --> variable values: L1    T0_SHIFT

!	IEOF = 0 --> not end-of-file
!	     = 1 --> end-of-file


	INTEGER H
	INTEGER REFLAG,SEQNUM
	INTEGER DETNUM(100), DN				!DN added 4/21/08
	REAL	INTI,L2
	CHARACTER EXPNAM*14

! Labelled common for 100 SNS detectors		3/21/08
	COMMON    /PEAKS/  col, row, chan, L2, twoth, az, dsp
	COMMON    /MONI/   MONCNT
	COMMON    /SET/    CHI, PHI, OMEGA, DETA
	COMMON    /DATA11/ NRUN,DETNUM,dn,EXPNAM
	COMMON    /DATA12/ SEQNUM, H, K, L, X, Y, WL,
     1			   IPKOBS, INTI, SIGI, REFLAG

	CHARACTER*160 TEXT	

	
	READ (IUNIT,100,END=999) TEXT
100	FORMAT(A)

	IEOF=0

	READ (TEXT,150) ISENT
150	FORMAT(I1)

! ISENT=3 ---> reflection
	IF (ISENT.EQ.3) THEN				!Reflection record
		READ (text, *) ISENT,SEQNUM,H,K,L,col,row,chan,L2, !ISENT=3
     1		twoth,az,wl,dsp,ipkobs,inti,sigi,reflag
		RETURN
	END IF

! ISENT=0 ---> new histogram
	IF (ISENT.EQ.0) THEN	!First record of a new histogram
		READ (IUNIT,*) ISENT,NRUN,DN,		!ISENT=1
     1		CHI,PHI,OMEGA,MONCNT
		READ (IUNIT,100) TEXT				!ISENT=2
		READ (IUNIT, *) ISENT,SEQNUM,H,K,L,col,row,chan,L2, !ISENT=3
     1		twoth,az,wl,dsp,ipkobs,inti,sigi,reflag
		RETURN
	END IF


	WRITE (*,*) '*** Error in SUBROUTINE READREFL ***'
	RETURN

999	IEOF=1	!END OF FILE
	RETURN	
	END


C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------

