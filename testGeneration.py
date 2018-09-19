import random

protocol = ["inbound","outbound"]
type = ["tcp","udp"]

f = open("test.csv", "w")
for i in range(0,10000):
	port = str(random.randint(0, 65535))
	ip1 = str(random.randint(0,255))+"."+str(random.randint(0,255))+"."+str(random.randint(0,255))+"."+str(random.randint(0,255))
	ip2 = str(random.randint(0,255))+"."+str(random.randint(0,255))+"."+str(random.randint(0,255))+"."+str(random.randint(0,255))+"-"+str(random.randint(0,255))+"."+str(random.randint(0,255))+"."+str(random.randint(0,255))+"."+str(random.randint(0,255))
	a = random.randint(0,1)
	if(a == 0):
		newip = ip1
	else:
		newip = ip2
	f.write(protocol[random.randint(0,1)]+","+type[random.randint(0,1)]+","+port+","+newip)
	f.write("\n")
f.close()
	