all: ResInterface.jar ResImpl/ResourceManagerImpl.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.ResourceManagerImpl

ResInterface.jar:
	javac ResInterface/ResourceManager.java
	jar cvf ResInterface.jar ResInterface/*.class
	chmod 644 ResImpl/*.class
	chmod 644 ResInterface.jar
	chmod 644 ResInterface/*.class

ResImpl/ResourceManagerImpl.class:
	javac ResImpl/ResourceManagerImpl.java

clean:
	rm -rf ResImpl/*.class; rm -rf ResInterface/*.class;