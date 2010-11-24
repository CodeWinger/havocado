ROLE = slave
CARSERVER = ${HOSTNAME}
FLIGHTSERVER = ${HOSTNAME}
ROOMSERVER = ${HOSTNAME}
CARRMINAME =
FLIGHTRMINAME =
ROOMRMINAME =
MIDDLERMINAME = HavocadoMiddleware
GROUPNAME =

all: ResInterface.jar
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" ResImpl.HavocadoFlesh mimi willy mimi

many: cars flights rooms

cars: GROUPNAME = HavocadoCarGroup
cars: ResInterface.jar
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" \
ResImpl.HavocadoSeed HavocadoSeedCar $(ROLE) $(GROUPNAME) $(CARSERVER)

flights: GROUPNAME = HavocadoFlightGroup
flights: ResInterface.jar
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" \
ResImpl.HavocadoSeed HavocadoSeedFlight $(ROLE) $(GROUPNAME) $(FLIGHTSERVER)

rooms: GROUPNAME = HavocadoRoomGroup
rooms: ResInterface.jar
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" \
ResImpl.HavocadoSeed HavocadoSeedRoom $(ROLE) $(GROUPNAME) $(ROOMSERVER)

flesh: GROPUNAME = HavocadoMiddlewareGroup
flesh: ResInterface.jar
	java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" \
ResImpl.HavocadoFlesh $(ROLE) $(MIDDLERMINAME) $(GROUPNAME) $(CARSERVER) $(CARRMINAME) \
$(FLIGHTSERVER) $(FLIGHTRMINAME) $(ROOMSERVER) $(ROOMRMINAME)

ResInterface.jar: lock resimpl
	javac ResInterface/*.java
	jar cvf ResInterface.jar ResInterface/*.class
	chmod 644 LockManager/*.class
	chmod 644 exceptions/*.class
	chmod 644 ResImpl/*.class
	chmod 644 ResInterface.jar
	chmod 644 ResInterface/*.class

resimpl: lock commands
	javac ResImpl/*.java

commands:
	javac Commands/*.java Commands/RMICommands/*.java

lock:
	javac LockManager/*.java

clean:
	rm -rf ResImpl/*.class; rm -rf ResInterface/*.class; rm -f Commands.jar; rm -f ResInterface.jar; rm -rf Commands/TCPCommands/*.class;
