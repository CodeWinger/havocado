all: Commands.jar ResInterface.jar ResImpl/ResourceManagerImpl.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.ResourceManagerImpl

many: cars flights rooms


cars: ResImpl/HavocadoSeed.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoSeed HavocadoSeedCar 11111

flights: ResImpl/HavocadoSeed.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoSeed HavocadoSeedFlight 22222

rooms: ResImpl/HavocadoSeed.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoSeed HavocadoSeedRoom 33333

flesh: Commands.jar ResInterface.jar ResImpl/HavocadoFlesh.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoFlesh mimi 11112

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

ResImpl/HavocadoSeed.class:
	javac ResImpl/HavocadoSeed.java

clean:
	rm -rf ResImpl/*.class; rm -rf ResInterface/*.class; rm -f Commands.jar; rm -rf Commands/TCPCommands/*.class;
