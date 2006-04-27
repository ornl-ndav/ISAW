
deploy:
	mvn deploy:deploy-file -DgroupId=ISAW -DartifactId=jython \
		-Dversion=1.0-SNAPSHOT -Dpackaging=jar -Dfile=jython.jar \
		-DrepositoryId=sns-internal \
		-Durl=scpexe://snsdev.ornl.gov/var/www/maven/internal

local_deploy:
	mvn install:install-file -DgroupId=ISAW -DartifactId=jython \
		-Dversion=1.0-SNAPSHOT -Dpackaging=jar -Dfile=jython.jar

