package Client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import Packet.Message;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

public class ClientParser {

	public static void printHelp() {
		System.out.println("Usage: help -h put -p get -g aliasname -a");
	}

	public static void main(String[] args) {

		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option optPut = parser.addStringOption('p', "put");
		CmdLineParser.Option optGet = parser.addStringOption('g', "get");
		CmdLineParser.Option optAliasName = parser.addStringOption('a',
				"aliasname");
		CmdLineParser.Option optHelp = parser.addBooleanOption('h', "help");
		try {
			parser.parse(args);
		} catch (IllegalOptionValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownOptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String putFileName = (String) parser.getOptionValue(optPut, new String(
				""));
		String getFileName = (String) parser.getOptionValue(optGet, new String(
				""));
		if ((putFileName.isEmpty() && getFileName.isEmpty())
				|| (!putFileName.isEmpty() && !getFileName.isEmpty())) {
			System.out.println("The Command Can Only Be Either GET Or PUT.");
			System.exit(1);
		}

		String aliasFileName = (String) parser.getOptionValue(optAliasName,
				new String("Result.txt"));
		Client client;
		try {
			client = new Client(InetAddress.getLocalHost().getHostAddress(),
					Message.LISTENING_PORT_NUM);
			if (putFileName.isEmpty())
				client.download(getFileName, aliasFileName);
			if (getFileName.isEmpty())
				client.upload(putFileName, aliasFileName);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// client.upload("uploadTest.txt", "uploadResult.txt");
		// client.download("downloadTest.txt", "downloadResult.txt");
		Boolean help = (Boolean) parser.getOptionValue(optHelp, Boolean.FALSE);

		if (help) {
			printHelp();
		}

	}
}
