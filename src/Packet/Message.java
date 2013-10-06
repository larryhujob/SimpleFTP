package Packet;

public class Message {

	public static final char FIELD_OPCODE_RRQ = 1;
	public static final char FIELD_OPCODE_WRQ = 2;
	public static final char FIELD_OPCODE_DATA = 3;
	public static final char FIELD_OPCODE_ACK = 4;
	public static final char FIELD_OPCODE_ERROR = 5;

	public static final String FIELD_MODE_NETASCII = "netascii";
	public static final String FIELD_MODE_OCTET = "octet";
	public static final String FIELD_MODE_MAIL = "mail";

	public static final String FIELD_ERRMSG_0 = "Not defined, see error message (if any).";
	public static final String FIELD_ERRMSG_1 = "File not found.";
	public static final String FIELD_ERRMSG_2 = "Access violation.";
	public static final String FIELD_ERRMSG_3 = "Disk full or allocation exceeded.";
	public static final String FIELD_ERRMSG_4 = "Illegal TFTP operation.";
	public static final String FIELD_ERRMSG_5 = "Unknown transfer ID.";
	public static final String FIELD_ERRMSG_6 = "File already exists.";
	public static final String FIELD_ERRMSG_7 = "No such user.";

	public static final int MIN_PORT_NUM = 100;
	public static final int MAX_PORT_NUM = 130;
	public static final int LISTENING_PORT_NUM = 65;
	public static final int MAX_PACKET_SIZE = 1024;
	public static final int BLOCK_SIZE = 512;
	private int msgSize;
	private byte[] msgVec;

	public Message() {

		msgVec = new byte[MAX_PACKET_SIZE];
		initMsg();

	}

	private void initMsg() {
		for (int i = 0; i < MAX_PACKET_SIZE; i++)
			msgVec[i] = 0;
		msgSize = 0;

	}

	public byte[] getMsgVec() {
		return msgVec;
	}

	public int getMsgSize() {
		return msgSize;
	}

	public boolean setMsgSize(int size) {
		if (size > MAX_PACKET_SIZE)
			return false;
		msgSize = size;
		return true;

	}

	public boolean setByteField(byte b) {
		if (msgSize >= MAX_PACKET_SIZE)
			return false;

		msgVec[msgSize++] = b;
		return true;

	}

	public byte getByteField(int pos) throws Exception {
		if (pos < 0 || pos >= msgSize)
			throw new Exception("Index out of bound.");
		return msgVec[pos];
	}

	public boolean setCharField(char ch) {

		byte firstByte = (byte) ((ch & 0xff00) >> 8);
		if (!setByteField(firstByte))
			return false;
		byte secondByte = (byte) (ch & 0x00ff);
		return setByteField(secondByte);
	}

	public char getCharField(int pos) throws Exception {
		byte firstByte = getByteField(pos);
		byte secondByte = getByteField(pos + 1);
		return (char) ((firstByte << 8) | secondByte);
	}

	public String getStringField(int pos) throws Exception {
		String str = new String();
		while (getByteField(pos) != (byte) 0)
			str += (char) getByteField(pos++);
		return str;
	}

	public boolean setStringField(String str) {

		byte byteVec[] = str.getBytes();
		int len = byteVec.length;
		for (int i = 0; i < len; i++)
			if (!setByteField(byteVec[i]))
				return false;

		return true;

	}

	public boolean setDataField(byte data[], int dataSize) {
		if (dataSize + msgSize >= MAX_PACKET_SIZE)
			return false;
		for (int i = 0; i < dataSize; i++)
			msgVec[msgSize + i] = data[i];
		msgSize += dataSize;
		return true;
	}

	public byte[] getDataField(int pos) {
		byte ret[] = new byte[msgSize - pos];
		for (int i = pos; i < msgSize; i++)
			ret[i - pos] = msgVec[i];
		return ret;
	}

	public boolean buildMsgRRQ(String fileName) {
		initMsg();
		return setCharField(FIELD_OPCODE_RRQ) && setStringField(fileName)
				&& setByteField((byte) 0) && setStringField(FIELD_MODE_OCTET)
				&& setByteField((byte) 0);

	}

	public boolean buildMsgWRQ(String fileName) {
		initMsg();
		return setCharField(FIELD_OPCODE_WRQ) && setStringField(fileName)
				&& setByteField((byte) 0) && setStringField(FIELD_MODE_OCTET)
				&& setByteField((byte) 0);
	}

	public boolean buildMsgDATA(int blockNum, byte data[], int dataSize) {
		initMsg();
		return setCharField(FIELD_OPCODE_DATA)
				&& setCharField((char) (blockNum))
				&& setDataField(data, dataSize);
	}

	public boolean buildMsgACK(int blockNum) {
		initMsg();
		return setCharField(FIELD_OPCODE_ACK)
				&& setCharField((char) (blockNum));
	}

	public boolean buildMsgERROR(int errCode, String errMsg) {
		initMsg();
		return setCharField(FIELD_OPCODE_ERROR)
				&& setCharField((char) (errCode)) && setStringField(errMsg)
				&& setByteField((byte) 0);
	}

	public boolean isMsgRRQ() throws Exception {
		return getCharField(0) == FIELD_OPCODE_RRQ;
	}

	public boolean isMsgWRQ() throws Exception {
		return getCharField(0) == FIELD_OPCODE_WRQ;
	}

	public boolean isMsgDATA() throws Exception {
		return getCharField(0) == FIELD_OPCODE_DATA;
	}

	public boolean isMsgACK() throws Exception {
		return getCharField(0) == FIELD_OPCODE_ACK;
	}

	public boolean isMsgERROR() throws Exception {
		return getCharField(0) == FIELD_OPCODE_ERROR;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
