call mvn install:install-file -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc -Dpackaging=jar -Dversion=4.0 -Dfile=sqljdbc4.jar -DgeneratePom=true
call mvn install:install-file -DgroupId=com.sun -DartifactId=mail -Dpackaging=jar -Dversion=1.4.5 -Dfile=mail.jar -DgeneratePom=true
call mvn install:install-file -DgroupId=oracle.core -DartifactId=ojdbc -Dpackaging=jar -Dversion=1.4 -Dfile=ojdbc14.jar -DgeneratePom=true
call mvn install:install-file -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc -Dpackaging=jar -Dversion=4.1 -Dfile=sqljdbc41.jar -DgeneratePom=true
call mvn install:install-file -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc -Dpackaging=jar -Dversion=4.2 -Dfile=sqljdbc42.jar -DgeneratePom=true
