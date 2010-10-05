all: Commands.jar ResInterface.jar ResImpl/ResourceManagerImpl.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.ResourceManagerImpl

many: cars flights rooms


cars:
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoSeed HavocadoSeedCar

flights:
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoSeed HavocadoSeedFlight

rooms:
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoSeed HavocadoSeedRoom

flesh: Commands.jar ResInterface.jar ResImpl/HavocadoFlesh.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoFlesh mimi

Commands.jar:
	javac Commands/*.java Commands/TCPCommands/*.java
	jar cvf Commands.jar Commands/*.class Commands/TCPCommands/*.class
	chmod 644 Commands.jar

ResInterface.jar:
	javac ResInterface/ResourceManager.java
	jar cvf ResInterface.jar ResInterface/*.class
	chmod 644 ResImpl/*.class
	chmod 644 ResInterface.jar
	chmod 644 ResInterface/*.class

ResImpl/ResourceManagerImpl.class:
	javac ResImpl/ResourceManagerImpl.java

ResImpl/HavocadoFlesh.class:
	javac ResImpl/HavocadoFlesh.java

clean:
	rm -rf ResImpl/*.class; rm -rf ResInterface/*.class; rm -f Commands.jar; rm -rf Commands/TCPCommands/*.class;
