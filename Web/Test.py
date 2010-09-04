#!/usr/bin/python
import os, sys
import base64, hmac


import time
from hashlib import sha1 as sha


myhome = "C:\\Users\\ruth\\"
 
idfile = open(myhome + "/.orbiter/my.id")
ACCESS_KEY = idfile.read().strip()
idfile.close()


keyfile = open(myhome + "/.orbiter/ehx.key")
PRIVATE_KEY = keyfile.read()
keyfile.close()


#if len(sys.argv) < 2:
#    print 'Usage: testOrbiterREST.py [URI]'
#    sys.exit()


#URI = sys.argv[1]
URI="https://orbiter.sns.gov/internal/findnexusdev.php"

os.environ['TZ']='GMT'
#time.tzset()
EXPIRES = str(int(time.mktime(time.localtime(time.time()+60))))
#EXPIRES='2010-03-12 11:35:22'

str = URI + '/OrbiterAccessKeyId/' + ACCESS_KEY + '/Expires/' + EXPIRES
print ["str=",str,"private_key",PRIVATE_KEY]
Z=hmac.new(PRIVATE_KEY, str, sha).digest()
print ['Z=',Z]
ZZ =base64.b64encode(Z)
print ['ZZ=',ZZ]
SIGNATURE = ZZ.strip().replace('/','')
print ['Signature=',SIGNATURE]

print '\n\nTest generation of Orbiter VFS SOA URI\n'


print 'REQUEST:\n' + str + '/Signature/' + SIGNATURE + '\n'


print 'RESPONSE:\n'
from urllib import urlopen
#print urlopen(str + '/Signature/' + SIGNATURE).read()



