package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import Packet.Message;

public class Client {

	private static final String FILE_PATH_CLIENT = System.getProperty("user.dir") + "\\client\\";

	public DataInputStream inFile;
	public DataOutputStream outFile;
	private String ipAddr;
	private int portNum;
	private DatagramSocket sock;

	public Client(String ipAddr, int portNum) {

		this.ipAddr = ipAddr;
		this.portNum = portNum;

		try {
			sock = new DatagramSocket();
		} catch (IOException e) {
			System.out.println("Socket Error.");
			e.printStackTrace();
		}
	}

	public void upload(String srcFileName, String destFileName) {

		byte data[] = new byte[Message.BLOCK_SIZE];
		for (int i = 0; i < Message.BLOCK_SIZE; i++)
			data[i] = 0;

		try {
			inFile = new DataInputStream(new FileInputStream(FILE_PATH_CLIENT
					+ srcFileName));
		} catch (FileNotFoundException e1) {
			System.out.println("Client File Not Found.");
			e1.printStackTrace();
		}

		try {

			Message wrqMsg = new Message();
			wrqMsg.buildMsgWRQ(destFileName);
			DatagramPacket wrqPkt = new DatagramPacket(wrqMsg.getMsgVec(),
					wrqMsg.getMsgSize(), InetAddress.getLocalHost(), portNum);
			sock.send(wrqPkt);
			System.out.println("Write Request Sent.");

			Message rcvMsg = new Message();
			int blockNum = 0;
			while (true) {

				DatagramPacket rcvPkt = new DatagramPacket(rcvMsg.getMsgVec(),
						Message.MAX_PACKET_SIZE);
				sock.receive(rcvPkt);
				int readNum = rcvPkt.getLength();
				rcvMsg.setMsgSize(readNum);
				if (rcvMsg.isMsgACK()) {
					System.out.println("ACK Reponse Received.");
					int msgBlockNum = rcvMsg.getCharField(2);
					if (msgBlockNum != blockNum)
						System.out.println("Wrong Order.");

					int fileReadNum = inFile.read(data, 0, Message.BLOCK_SIZE);
					if (fileReadNum == -1)
						System.out.println("End Of File Reached.");
					else {
						blockNum++;
						Message dataMsg = new Message();
						dataMsg.buildMsgDATA(blockNum, data, fileReadNum);
						DatagramPacket dataPkt = new DatagramPacket(
								dataMsg.getMsgVec(), dataMsg.getMsgSize(),
								rcvPkt.getAddress(), rcvPkt.getPort());
						sock.send(dataPkt);
						System.out.println("Data Message Sent.");
						if (fileReadNum < Message.BLOCK_SIZE) {
							System.out.println("Client Done Sending.");
							break;
						}
					}

				} else if (rcvMsg.isMsgERROR()) {
					String errMsg = rcvMsg.getStringField(4);
					System.out.println(errMsg);

				} else {

				}

			}
		} catch (IOException e) {
			System.out.println("IO Exception.");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Client Connection Error.");
			e.printStackTrace();
		}

	}

	public void download(String srcFileName, String destFileName) {
		File fileHandler = new File(FILE_PATH_CLIENT + destFileName);
		if (fileHandler != null && fileHandler.exists())
			System.out.println("File Already Exist On Client.");

		try {
			outFile = new DataOutputStream(new FileOutputStream(
					FILE_PATH_CLIENT + destFileName));
		} catch (FileNotFoundException e) {
			System.out.println("Client File Not Found.");
			e.printStackTrace();
		}

		try {

			Message rrqMsg = new Message();
			rrqMsg.buildMsgRRQ(srcFileName);
			DatagramPacket rrqPkt = new DatagramPacket(rrqMsg.getMsgVec(),
					rrqMsg.getMsgSize(), InetAddress.getLocalHost(), portNum);
			sock.send(rrqPkt);
			System.out.println("Read Request Sent.");

			Message rcvMsg = new Message();
			while (true) {

				DatagramPacket rcvPkt = new DatagramPacket(rcvMsg.getMsgVec(),
						Message.MAX_PACKET_SIZE);
				sock.receive(rcvPkt);
				int readNum = rcvPkt.getLength();
				rcvMsg.setMsgSize(readNum);

				if (rcvMsg.isMsgDATA()) {

					outFile.write(rcvMsg.getDataField(4));
					System.out.println("Write To File");

					int blockNum = rcvMsg.getCharField(2);
					Message ackMsg = new Message();
					ackMsg.buildMsgACK(blockNum);

					DatagramPacket ackPkt = new DatagramPacket(
							ackMsg.getMsgVec(), ackMsg.getMsgSize(),
							rcvPkt.getAddress(), rcvPkt.getPort());
					sock.send(ackPkt);

					System.out.println("ACK Response Sent.");
					if (rcvMsg.getMsgSize() - 4 < Message.BLOCK_SIZE) {
						System.out.println("Client Done Receiving.");
						break;
					}
				} else if (rcvMsg.isMsgERROR()) {
					String errMsg = rcvMsg.getStringField(4);
					System.out.println(errMsg);

				} else {

				}

			}
		} catch (IOException e) {
			System.out.println("IO Exception");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Client Connection Error");
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		Client client;
		try {
			client = new Client(InetAddress.getLocalHost().getHostAddress(),
					Message.LISTENING_PORT_NUM);
			client.upload("uploadTest.txt", "uploadResult.txt");
			// client.download("downloadTest.txt", "downloadResult.txt");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
