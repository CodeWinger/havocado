#!/usr/bin/python

filenames = ["benchmark1", "benchmark2", "lockScript"]

machineCounter = 1
machineMap = {}
machineMap2 = {}
timestamps = {}

def newMachine(name):
	global machineCounter
	if not (name in machineMap):
		machineMap[name] = machineCounter
		machineCounter = machineCounter + 1

def newMachine2(name):
	if not (name in machineMap2):
		machineMap2[name] = []

def processBuffer(buff):
	size = len(buff)
	if(size <= 1):
		return
	
	mimiName = buff[0].split(' ')[0]
	mimiFirstTime = long(buff[0].split(' ')[1])
	mimiLastTime = long(buff[size-1].split(' ')[1])
	newMachine2(mimiName)
	mimiTime = machineMap2[mimiName]
	mimiTime.append(mimiLastTime - mimiFirstTime);
	
	willyName = buff[1].split(' ')[0]
	willyFirstTime = long(buff[1].split(' ')[1])
	willyLastTime = long(buff[size-2].split(' ')[1])
	newMachine2(willyName)
	willyTime = machineMap2[willyName]
	willyTime.append(willyLastTime - willyFirstTime);
	
	foundLab = 0
	for i in range(size):
		line = buff[i]
		tokens = buff[i].split(' ')
		if "lab" in tokens[0]:
			if foundLab == 0:
				labName = tokens[0]
				labFirstTime = long(tokens[1])
				labLastTime = long(buff[i+1].split(' ')[1])
				newMachine2(labName)
				labTime = machineMap2[labName]
				labTime.append(labLastTime - labFirstTime)
				foundLab = 1
			else:
				foundLab = 0

def processFile2(fname):
	f = open(fname, "r")
	
	buff = []
	for line in f:
		line = str.rstrip(line)
		if len(line) > 1:
			buff.append(line)
		else:
			processBuffer(buff)
			buff = []
	
	f.close()

def processFile(fname):
	f = open(fname, "r")

	for line in f:
		line = str.rstrip(line)
		tokens = line.split(' ')
		if len(line) > 1:
			machine = tokens[0]
			newMachine(machine)
			time = str(tokens[1])
			timestamps[time] = machineMap[machine]
		
	f.close()

def writeOutput(fname):
	f = open(fname, 'w')
	for key in machineMap:
		f.write("#" + key + " : " + str(machineMap[key]));
		f.write("\n");
	f.write("#time machine\n")
	
	sortedTime = timestamps.keys();
	sortedTime.sort();
	
	for time in sortedTime:
		f.write(time + " " + str(timestamps[time]) + "\n")
	
	f.close()

def writeOutput2(fname):

	f = open(fname, 'w')
	for key in machineMap2:
		print key
		timestampList = machineMap2[key]
		tSize = len(timestampList)
		acc = 0
		for i in range(tSize):
			print timestampList[i]
			acc = acc + timestampList[i]
		avg = float(acc) / float(tSize)
		f.write("average time spent in " + key + ": " + str(avg) + "ms\n");
	
	f.close()

def main():
	for filename in filenames:
		processFile(filename + ".log")
		writeOutput(filename + ".out")		
		processFile2(filename + ".log")
		writeOutput2(filename + ".nls")

main()
