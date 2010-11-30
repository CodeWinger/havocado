DIR = "/home/2007/sbriss7/School/Comp512"
CP = $(DIR)"/servercode:"$(DIR)"/servercode/ResImpl:"$(DIR)"/servercode/ResImpl/jgroups-2.9.0.GA.jar"
JC = javac -cp $(CP)
JAC = java -Djava.rmi.server.codebase=file:"${HOME}/School/Comp512/servercode/" -classpath $(CP)

ROLE = slave
CARSERVER = ${HOSTNAME}
FLIGHTSERVER = ${HOSTNAME}
ROOMSERVER = ${HOSTNAME}
CARRMINAME = HavocadoCar
FLIGHTRMINAME = HavocadoFlight
ROOMRMINAME = HavocadoRoom
MIDDLERMINAME = HavocadoMiddleware
JCONFIG = jconfig.xml
GROUPNAME =

all: ResInterface.jar
	$(JAC) ResImpl.HavocadoFlesh mimi willy mimi

many: cars flights rooms

cars: GROUPNAME = HavocadoCarGroup
cars: ResInterface.jar
	$(JAC) \
ResImpl.HavocadoSeed $(ROLE) $(GROUPNAME) $(CARRMINAME) $(JCONFIG)

flights: GROUPNAME = HavocadoFlightGroup
flights: ResInterface.jar
	$(JAC) \
ResImpl.HavocadoSeed $(ROLE) $(GROUPNAME) $(FLIGHTRMINAME) $(JCONFIG)

rooms: GROUPNAME = HavocadoRoomGroup
rooms: ResInterface.jar
	$(JAC) \
ResImpl.HavocadoSeed $(ROLE) $(GROUPNAME) $(ROOMRMINAME) $(JCONFIG)

flesh: GROUPNAME = HavocadoMiddlewareGroup
flesh: ResInterface.jar
	$(JAC) \
ResImpl.HavocadoFlesh $(ROLE) $(MIDDLERMINAME) $(GROUPNAME) $(CARSERVER) $(CARRMINAME) \
$(FLIGHTSERVER) $(FLIGHTRMINAME) $(ROOMSERVER) $(ROOMRMINAME) $(JCONFIG)

ResInterface.jar: lock resimpl
	$(JC) ResInterface/*.java
	jar cvf ResInterface.jar ResInterface/*.class
	chmod 644 LockManager/*.class
	chmod 644 exceptions/*.class
	chmod 644 ResImpl/*.class
	chmod 644 ResImpl/*.jar
	chmod 644 ResInterface.jar
	chmod 644 ResInterface/*.class

resimpl: lock commands
	$(JC) ResImpl/*.java

commands:
	$(JC) Commands/*.java Commands/RMICommands/*.java Commands/RMGroupCommands/*.java

lock:
	$(JC) LockManager/*.java

clean:
	rm -rf ResImpl/*.class; rm -rf ResInterface/*.class; rm -f Commands.jar; rm -f ResInterface.jar; rm -rf Commands/TCPCommands/*.class; rm -rf Commands/RMGroupCommands/*.class
