package blockchain.controller;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Callable;

public class Receive implements Callable<String>{
	//field
	private String ip = "224.1.1.6";
	private int port = 60006;
	private String data;
	//methods
	@Override
	public String call() {
		try(MulticastSocket socket = new MulticastSocket(port)){
			byte[] newData = new byte[2048*100];
			socket.joinGroup(InetAddress.getByName(ip));
			DatagramPacket packet = new DatagramPacket(newData, newData.length);
			socket.receive(packet);
			return new String(newData);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
