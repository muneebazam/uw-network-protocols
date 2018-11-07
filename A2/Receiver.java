// Receiver program for Go-Back-N protocol

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.PrintWriter;

public class Receiver {

	private static final int MAX_SEQ_NUM = 32;
	private static final int MAX_BUFFER_SIZE = 512; // bytes
	private static int expected_seq_num = 0;
	private static PrintWriter arrival_log;
	private static PrintWriter output_file;
	private static String host_address;
	private static int send_port;
	private static int receive_port;
	private static String file_name;

	// DEBUG MODE
	private static boolean DEBUG = true;

	private static void send_packet(packet pkt) throws Exception {
		byte[] data = pkt.getUDPdata();
		InetAddress client_address = InetAddress.getByName(host_address);
        DatagramSocket send_socket = new DatagramSocket();
        DatagramPacket data_packet = new DatagramPacket(data, data.length, client_address, send_port);
        send_socket.send(data_packet);
        send_socket.close();
    }

	public static void main(String[] args) throws Exception {
		
		// handle command line arguments
		if (args.length != 4) {
			System.err.println("Incorrect number of arguments");
			System.err.println("Usage: Receiver <host_name> <send_port> <receive_port> <file_name>");
			System.exit(1);
		} else {
			host_address = args[0];
			send_port = Integer.valueOf(args[1]);
			receive_port = Integer.valueOf(args[2]);
			file_name = args[3];
		}

		// Receiver setup
		DatagramSocket receive_socket = new DatagramSocket(receive_port);
		byte[] data_buffer = new byte[MAX_BUFFER_SIZE];
		arrival_log = new PrintWriter("arrival.log");
		output_file = new PrintWriter(file_name);

		// Main receiver
		while (true) {

			// receive a packet, from the client extracting and logging the seq_num
			DatagramPacket receive_pkt = new DatagramPacket(data_buffer, data_buffer.length);
            receive_socket.receive(receive_pkt);
            packet pkt = packet.parseUDPdata(receive_pkt.getData());
            int seq_num = pkt.getSeqNum();
			arrival_log.println(seq_num);

			if (DEBUG) {
				System.out.println("RECEIVER: Received packet with seq_num " + seq_num);
			}

			// packet type specific behaviour 
			if (pkt.getType() == 1) {
				if (seq_num == expected_seq_num) {
					if (DEBUG) {
						System.out.println("RECEIVER: Received the expected_seq_num: " + expected_seq_num);
					}
					output_file.print(new String(pkt.getData()));
                	send_packet(packet.createACK(seq_num));
					expected_seq_num += 1;
					expected_seq_num %= MAX_SEQ_NUM;
				} else {
					if (DEBUG) {
						System.out.println("RECEIVER: Received a packet we were not expecting: " + seq_num);
						System.out.println("RECEIVER: Expecting packet with seq_num: " + expected_seq_num);
					}
					if (expected_seq_num > 0) {
						packet ack = packet.createACK((expected_seq_num - 1) % MAX_SEQ_NUM);
						send_packet(ack);
					}
				}
			} else if (pkt.getType() == 2) {
				System.out.println("RECEIVER: Receieved EOT packet");
				send_packet(packet.createEOT(seq_num));
				break;
			}			
		}

		// cleanup and exit
		arrival_log.close();
		output_file.close();
		receive_socket.close();
		System.exit(0);
	}
}