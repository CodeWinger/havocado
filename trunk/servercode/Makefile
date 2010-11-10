all: Commands.jar ResInterface.jar
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.ResourceManagerImpl

many: cars flights rooms


cars: ResImpl/HavocadoSeed.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoSeed HavocadoSeedCar
flights: ResImpl/HavocadoSeed.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoSeed HavocadoSeedFlight

rooms: ResImpl/HavocadoSeed.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoSeed HavocadoSeedRoom

flesh: Commands.jar ResInterface.jar ResImpl/HavocadoFlesh.class
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoFlesh mimi willy mimi

Commands.jar:
	javac Commands/*.java
	jar cvf Commands.jar Commands/*.class
	chmod 644 Commands.jar

ResInterface.jar:
	javac ResInterface/ResourceManager.java
	jar cvf ResInterface.jar ResInterface/*.class
	chmod 644 ResImpl/*.class
	chmod 644 ResInterface.jar
	chmod 644 ResInterface/*.class

ResImpl/ResourceManagerImpl.class:
	javac ResImpl/ResourceManagerImpl.java

ResImpl/HavocadoFlesh.class: Commands.jar
	javac ResImpl/HavocadoFlesh.java

ResImpl/HavocadoSeed.class: Commands.jar
	javac ResImpl/HavocadoSeed.java

clean:
	rm -rf ResImpl/*.class; rm -rf ResInterface/*.class; rm -f Commands.jar; rm -rf Commands/TCPCommands/*.class;
