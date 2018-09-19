/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firewall;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


/**
 *
 * @author pratik
 */
public class Firewall {

    Map<String, Protocol> inboundProtocol= new HashMap<>();
    Map<String, Protocol> outboundProtocol= new HashMap<>();

    
    public Firewall(String inputPath) throws IOException {
        preprocessRules(inputPath);
    }
    
    public void preprocessRules(String inputPath) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new FileReader(inputPath));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] country = line.split(",");
            String direction = country[0];
            String protocol = country[1];
            String port = country[2];
            String ipAddress = country[3];
            createRules(direction, protocol, port, ipAddress);

        }
    }

    public void createRules(String direction, String protocol, String port, String ipAddress){
    	Protocol p =null;
    	if(direction.equals("inbound")){
    		if(!inboundProtocol.containsKey(protocol)){
    			inboundProtocol.put(protocol, new Protocol(protocol));
    		}
    		p = inboundProtocol.get(protocol);
    		
    	}else if(direction.equals("outbound")){
    		if(!outboundProtocol.containsKey(protocol)){
    			outboundProtocol.put(protocol, new Protocol(protocol));	
    		}
    		p = outboundProtocol.get(protocol);
    	}
    	p.insertPorts(port,ipAddress);
    }
    
    public boolean accept_packet(String direction, String protocol, int port, String ipAddress){
        Protocol pr = null;
        if(direction.equals("inbound")){
            if(inboundProtocol.containsKey(protocol)){
                 pr = inboundProtocol.get(protocol);
            }else{
                return false;
            }
    	}else if(direction.equals("outbound")){
            if(outboundProtocol.containsKey(protocol)){
                 pr = outboundProtocol.get(protocol);
            }else{
                return false;
            }
    	}
        if(pr.ports.containsKey(port)){
            Port po = pr.ports.get(port);
            if(po.ipAddress.contains(ipAddress)){
                return true;
            }else{
                for(String[] limits : po.ipRanges){
                    if(IPRangeChecker.isValidRange(limits[0],limits[1],ipAddress))
                        return true;
                }
                return false;
            }
        }else{
            return false;
        }    
    }
    
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Firewall  f = new Firewall("C:\\Users\\prati\\Documents\\NetBeansProjects\\Firewall\\src\\firewall\\test.csv");
        System.out.println(f.accept_packet("inbound","tcp",42995,"74.109.133.46"));
        System.out.println(f.accept_packet("inbound", "udp", 53, "192.168.2.1"));
        System.out.println(f.accept_packet("outbound", "tcp", 65000, "52.13.48.93"));
        System.out.println(f.accept_packet("inbound", "tcp", 81, "192.168.1.2"));
        System.out.println(f.accept_packet("inbound", "udp", 24, "52.12.48.92"));
    }
    
}


class Protocol{
	String protocol;
	HashMap<Integer, Port> ports;

	Protocol(String input){
		protocol = input;
		ports = new HashMap<>();
	}

	void insertPorts(String portRange, String ipAddress){
		for(Port p : Port.extractPorts(portRange,this)){
			ports.put(p.portNumber,p);
            p.insertIps(ipAddress);
		}
	}

}

class Port{
	 int portNumber;
	 HashSet<String> ipAddress;
	 ArrayList<String[]>  ipRanges;

	Port(int port){
		portNumber = port;
		ipAddress = new HashSet<>();
		ipRanges = new ArrayList<>();
	}


	public static ArrayList<Port> extractPorts(String portRange, Protocol protocol){
		ArrayList<Port> linkedPorts = new ArrayList<>();
		if(portRange.indexOf("-")>0){
			String[] startEnd = portRange.split("-");
			for(int i = Integer.parseInt(startEnd[0]);i<=Integer.parseInt(startEnd[1]);i++){
				linkedPorts.add(createPort(i,protocol));
			}

		}else{
			int port = Integer.parseInt(portRange);
			linkedPorts.add(createPort(port,protocol));
		}
		return linkedPorts;
	}


	public void insertIps(String ip){
		if(ip.indexOf("-")>0){
			String[] startEnd = ip.split("-");
			ipRanges.add(startEnd);

		}else{
			ipAddress.add(ip);
		}
	}

	private static Port createPort(int portNumber,Protocol protocol){
		if(!protocol.ports.containsKey(portNumber)){
			Port p = new Port(portNumber);
			return p;
		}else{
			return protocol.ports.get(portNumber);
		}
	}



}

//Referd to github repo : https://gist.github.com/madan712/6651967
class IPRangeChecker {
    public static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
                result <<= 8;
                result |= octet & 0xff;
        }
        return result;
    }

    public static boolean isValidRange(String ipStart, String ipEnd, String ipToCheck) {
        try {
            long ipLo = ipToLong(InetAddress.getByName(ipStart));
            long ipHi = ipToLong(InetAddress.getByName(ipEnd));
            long ipToTest = ipToLong(InetAddress.getByName(ipToCheck));
            return (ipToTest >= ipLo && ipToTest <= ipHi);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

}