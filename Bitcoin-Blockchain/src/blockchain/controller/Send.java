package blockchain.controller;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class Send implements Runnable{
	//field
	private String ip = "224.1.1.6";
	private int port = 60006;
	private String data;
	//methods
	public Send(String data) {
		this.data = data;
	}
	@Override
	public void run() {
		try(MulticastSocket socket = new MulticastSocket()){
			DatagramPacket packet = new DatagramPacket(data.getBytes(), data.getBytes().length, new InetSocketAddress(ip, port));
			socket.send(packet);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
