package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import Packet.Message;

public class Connection extends Thread {

	private static final String FILE_PATH_SERVER = System.getProperty("user.dir") + "\\server\\";

	public DataInputStream inFile;
	public DataOutputStream outFile;
	protected Server server;
	protected DatagramSocket sock;
	protected DatagramPacket rcvPkt;
	protected Message rcvMsg;

	public Connection(Server server, DatagramSocket sock,
			DatagramPacket rcvPkt, Message rcvMsg) {
		this.server = server;
		this.sock = sock;
		this.rcvPkt = rcvPkt;
		this.rcvMsg = rcvMsg;
	}

	public void run() {
		try {
			if (rcvMsg.isMsgWRQ()) {
				System.out.println("Write Request Received.");
				String destFileName = rcvMsg.getStringField(2);
				File fileHandler = new File(FILE_PATH_SERVER + destFileName);
				if (fileHandler != null && fileHandler.exists()) {
					System.out.println("File Already Exist On Server.");
					Message errMsg = new Message();
					errMsg.buildMsgERROR(6, Message.FIELD_ERRMSG_6);
					DatagramPacket errPkt = new DatagramPacket(
							errMsg.getMsgVec(), errMsg.getMsgSize(),
							rcvPkt.getAddress(), rcvPkt.getPort());
					sock.send(errPkt);
					System.out.println("Error Message Sent.");

				} else {

					outFile = new DataOutputStream(new FileOutputStream(
							FILE_PATH_SERVER + destFileName));
					Message ackMsg = new Message();
					ackMsg.buildMsgACK(0);
					DatagramPacket ackPkt = new DatagramPacket(
							ackMsg.getMsgVec(), ackMsg.getMsgSize(),
							rcvPkt.getAddress(), rcvPkt.getPort());
					sock.send(ackPkt);
					System.out.println("Initial ACK Response Sent.");
					Message dataMsg = new Message();
					while (true) {

						DatagramPacket dataPkt = new DatagramPacket(
								dataMsg.getMsgVec(), Message.MAX_PACKET_SIZE);
						sock.receive(dataPkt);
						int readNum = dataPkt.getLength();
						dataMsg.setMsgSize(readNum);

						if (!dataMsg.isMsgDATA()) {
							System.out.println("Wrong Message Received.");
						}
						System.out.println("Data Message Received.");
						outFile.write(dataMsg.getDataField(4));
						System.out.println("Write To File.");
						int blockNum = dataMsg.getCharField(2);
						ackMsg.buildMsgACK(blockNum);
						ackPkt = new DatagramPacket(ackMsg.getMsgVec(),
								ackMsg.getMsgSize(), dataPkt.getAddress(),
								dataPkt.getPort());
						sock.send(ackPkt);
						System.out.println("ACK Response Sent.");
						if (dataMsg.getMsgSize() - 4 < Message.BLOCK_SIZE) {
							System.out.println("Server Done Receiving.");
							break;
						}
					}
				}

			} else if (rcvMsg.isMsgRRQ()) {

				byte data[] = new byte[Message.BLOCK_SIZE];
				for (int i = 0; i < Message.BLOCK_SIZE; i++)
					data[i] = 0;

				try {
					System.out.println("Read Request Received.");
					String srcFileName = rcvMsg.getStringField(2);
					inFile = new DataInputStream(new FileInputStream(
							FILE_PATH_SERVER + srcFileName));
				} catch (FileNotFoundException e1) {
					System.out.println("Server File Not Found.");
					Message errMsg = new Message();
					errMsg.buildMsgERROR(1, Message.FIELD_ERRMSG_1);
					DatagramPacket errPkt = new DatagramPacket(
							errMsg.getMsgVec(), errMsg.getMsgSize(),
							rcvPkt.getAddress(), rcvPkt.getPort());
					sock.send(errPkt);
					System.out.println("Error Message Sent.");
					e1.printStackTrace();
				}
				int blockNum = 0;
				while (true) {
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

						Message ackMsg = new Message();
						DatagramPacket ackPkt = new DatagramPacket(
								ackMsg.getMsgVec(), Message.MAX_PACKET_SIZE);
						sock.receive(ackPkt);
						int readNum = ackPkt.getLength();
						ackMsg.setMsgSize(readNum);

						if (!ackMsg.isMsgACK()) {
							Message errMsg = new Message();
							errMsg.buildMsgERROR(5, Message.FIELD_ERRMSG_5);
							DatagramPacket errPkt = new DatagramPacket(
									errMsg.getMsgVec(), errMsg.getMsgSize(),
									ackPkt.getAddress(), ackPkt.getPort());
							sock.send(errPkt);
							System.out.println("Error Message Sent.");

						} else if (fileReadNum < Message.BLOCK_SIZE) {
							System.out.println("Server Done Sending.");
							break;
						}
						System.out.println("ACK Packet Received.");
						int msgBlockNum = ackMsg.getCharField(2);
						if (msgBlockNum != blockNum)
							System.out.println("Wrong Order.");

					}
				}

			}
		} catch (Exception e) {
			System.out.println("Server Connection Error.");
			e.printStackTrace();
		} finally {
			server.closeConnection(sock);
		}
	}

}
