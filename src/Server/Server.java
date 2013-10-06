package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import Packet.Message;

public class Server {

	private int portNum;
	private DatagramSocket listenSock;

	protected HashSet<Integer> portHash = new HashSet<Integer>();
	protected HashMap<DatagramSocket, Connection> connHash = new HashMap<DatagramSocket, Connection>();

	public Server(int portNum) {
		this.portNum = portNum;

		System.out.println("Server Is Listening On " + portNum);

		try {
			listenSock = new DatagramSocket(portNum);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		try {
			while (true) {
				Message rcvMsg = new Message();
				DatagramPacket rcvPkt = new DatagramPacket(rcvMsg.getMsgVec(),
						Message.MAX_PACKET_SIZE);
				listenSock.receive(rcvPkt);

				int readNum = rcvPkt.getLength();
				rcvMsg.setMsgSize(readNum);
				Random rand = new Random();

				int randomNum;
				while (true) {
					randomNum = rand
							.nextInt((Message.MAX_PORT_NUM - Message.MIN_PORT_NUM) + 1)
							+ Message.MIN_PORT_NUM;
					if (!portHash.contains(randomNum)) {
						portHash.add(randomNum);
						System.out
								.println("The Connection Was Assigned Port Number "
										+ randomNum);
						break;
					}
				}

				DatagramSocket sock = new DatagramSocket(randomNum);
				Connection conn = new Connection(this, sock, rcvPkt, rcvMsg);
				connHash.put(sock, conn);
				conn.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeConnection(DatagramSocket sock) {
		System.out.println("Remove Connection.");
		if (!connHash.containsKey(sock)
				|| !portHash.contains(sock.getLocalPort()))
			System.out.println("The Connection No Longer Exist.");
		else {
			Connection conn = connHash.get(sock);
			try {
				if (conn.inFile != null)
					conn.inFile.close();
			} catch (IOException e) {
				System.out.println("Input File Cannot Close.");
				e.printStackTrace();
			}
			try {
				if (conn.outFile != null)
					conn.outFile.close();
			} catch (IOException e) {
				System.out.println("Output File Cannot Close.");
				e.printStackTrace();
			}
			conn.sock.close();
			portHash.remove(sock.getLocalPort());
			connHash.remove(sock);
			System.out.println("Connection Was Removed.");
		}

	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Server server = new Server(Message.LISTENING_PORT_NUM);

	}

}
