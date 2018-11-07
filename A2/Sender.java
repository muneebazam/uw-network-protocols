/* Sender.java
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.io.File;
import java.nio.file.Files;

public class Sender
{
	private static final int MAX_PAYLOAD = 500; // bytes
	private static final int ACK_BUFFER = 1024; // bytes
	private static final int DELAY = 100; // ms
	private static final int TIMEOUT = 1000; // ms
	private static final int WINDOW_SIZE = 10;
	private static final int MAX_SEQ_NUM = 32;

	private static DatagramSocket receive_socket;
	private static String host_address;
	private static int total_num_packets;
	private static int send_port;
	private static int receive_port;
	private static String file_name;
	private static PrintWriter seq_num_log;
	private static Timer timer;
	private static packet[] packets;

	private static int num_packets_ACKd = 0;
	private static int next_packet = 0;

	// DEBUG MODE
	private static boolean DEBUG = true;

	// Semaphores to 'lock' the two variables above
	private static Semaphore num_packets_ACKd_sem = new Semaphore(1, true);
    private static Semaphore next_packet_sem = new Semaphore(1, true);

	private static packet[] create_packets_from_file() throws Exception {

		RandomAccessFile file = new RandomAccessFile(file_name, "r");
		byte[] file_bytes = new byte[(int) file.length()];
		file.readFully(file_bytes);
		file.close();
		total_num_packets = (int) Math.ceil((double) file_bytes.length / MAX_PAYLOAD);
		packet[] packets = new packet[total_num_packets];
		for (int i = 0; i < total_num_packets; i++) {
			int numBytes = Math.min(MAX_PAYLOAD, file_bytes.length - i * MAX_PAYLOAD);
            byte data[] = new byte[numBytes];
			System.arraycopy(file_bytes, i * MAX_PAYLOAD, data, 0, numBytes);
			try {
				packets[i] = packet.createPacket(i % MAX_SEQ_NUM, new String(data));
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}

		if (DEBUG) {
			System.out.println("number of packets are: " + total_num_packets);
		}
		return packets;
	}

	private static class ReceiveACKs extends Thread {
        private Thread t;
        private PrintWriter ack_log;

        ReceiveACKs() throws IOException {
            ack_log = new PrintWriter(new FileWriter("./ack.log"));
        }

        private void ACK_receiver() throws Exception{
            byte[] ack_data = new byte[ACK_BUFFER];
            while (true) {
                DatagramPacket ack_pkt = new DatagramPacket(ack_data, ack_data.length);
                receive_socket.receive(ack_pkt);
                int seq_num = packet.parseUDPdata(ack_pkt.getData()).getSeqNum();
				ack_log.println(seq_num);

				next_packet_sem.acquire();
				if ((next_packet - 1) % MAX_SEQ_NUM == seq_num) {
					num_packets_ACKd_sem.acquire();
					num_packets_ACKd = next_packet;
					num_packets_ACKd_sem.release();
				}
				next_packet_sem.release();

				if (DEBUG) {
					System.out.println("Received an ACK for packet " + seq_num);
				}
		
				// perform sequence number specific action 
				if (seq_num == (num_packets_ACKd % MAX_SEQ_NUM)){

					if (DEBUG) {
						System.out.println("Received an ACK for packet we were expecting: " + seq_num);
					}
					num_packets_ACKd_sem.acquire();
					System.out.println("Number of packets ACKd so far: " + num_packets_ACKd);
					num_packets_ACKd = num_packets_ACKd + 1;
					num_packets_ACKd_sem.release();
					if (num_packets_ACKd >= total_num_packets) {
						if (DEBUG) {
							System.out.println("Received ACK for all the packets we needed");
						}
						break;
					}
					restart_timer();
				}
            }
            timer.cancel();
			ack_log.close();
			num_packets_ACKd_sem.release();
			System.exit(0);
        }

        public void run() {
            try {
                ACK_receiver();
            } catch (Exception e) {
            	 e.printStackTrace(System.out);
			}
        }

        public void start() {
            if (t == null) {
                t = new Thread(this, "Acknowledgement Receiver");
                t.start();
            }
		}
	
	}
	
	private static void send_packet(int i) throws Exception {
		if (DEBUG) {
			System.out.println("Attempting to send packet: " + i);
		}
        byte[] data = packets[i].getUDPdata();
        DatagramSocket send_socket = new DatagramSocket();
        InetAddress clientIP = InetAddress.getByName(host_address);
        DatagramPacket data_pkt = new DatagramPacket(data, data.length, clientIP, send_port);
        send_socket.send(data_pkt);
		send_socket.close();
		
		// log the seq num
		seq_num_log.println(i);
	}
	
	private static void resend_packets() throws Exception {
        num_packets_ACKd_sem.acquire();
        next_packet_sem.acquire();
        for (int i = num_packets_ACKd; i < next_packet; i++) {
			if (DEBUG) {
				System.out.println("Attempting to resend packet: " + i);
			}
			send_packet(i);
        }
        next_packet_sem.release();
        num_packets_ACKd_sem.release();
	}
	
	private static void start_timer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
					if (DEBUG) {
						System.out.println("Timer expired, now resending all unACKd packets");
					}
                    resend_packets();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, DELAY, TIMEOUT);
	}
	
	private static void restart_timer() {
        timer.cancel();
		timer = new Timer();
        start_timer();
	}
	
	public static void main(String[] args) throws Exception {

		if (args.length != 4) {
			System.err.println("Incorrect number of arguments.");
			System.err.println("Usage: Sender <host_address> <send_port> <receive_port> <file_name>");
			System.exit(1);
		} else {
			host_address = args[0];
			send_port = Integer.valueOf(args[1]);
			receive_port = Integer.valueOf(args[2]);
			file_name = args[3];
		}

		seq_num_log = new PrintWriter(new FileWriter("./seqnum.log"));
		timer = new Timer();
		packets = create_packets_from_file();
		System.out.println(packets);
		receive_socket = new DatagramSocket(receive_port);

		ReceiveACKs ack_receiver = new ReceiveACKs();
		ack_receiver.start();

		num_packets_ACKd_sem.acquire();
		while (num_packets_ACKd < total_num_packets) {
			num_packets_ACKd_sem.release();
			next_packet_sem.acquire();
			while (next_packet < total_num_packets && 
				   (next_packet - num_packets_ACKd) < WINDOW_SIZE) {
				if (num_packets_ACKd == 0 && next_packet == 0) {
					if (DEBUG) {
						System.out.println("Attempting to send the first packet and starting timer");
					}
					start_timer();
				}
				send_packet(next_packet);
				next_packet += 1;
			}
			next_packet_sem.release();
			num_packets_ACKd_sem.acquire();
		}
		num_packets_ACKd_sem.release();
		seq_num_log.close();

		if (DEBUG) {
			System.out.println("made it to the EOT stage.");
		}

		// EOT Transmission
        DatagramSocket EOT_send_socket = new DatagramSocket();
        InetAddress clientIP = InetAddress.getByName(host_address);
        byte[] data = packet.createEOT(total_num_packets + 1).getUDPdata();
		DatagramPacket EOT_pkt = new DatagramPacket(data, data.length, clientIP, send_port);
		EOT_send_socket.send(EOT_pkt);
		EOT_send_socket.close();

        // Recieve EOT
        byte[] EOT = new byte[ACK_BUFFER];
		DatagramPacket EOT_ACK = new DatagramPacket(EOT, EOT.length);
		receive_socket.receive(EOT_ACK);
        packet EOT_ack_pkt = packet.parseUDPdata(EOT_ACK.getData());

		if (EOT_ack_pkt.getType() == 2) {
            receive_socket.close();
		}
		ack_receiver.interrupt();
		System.exit(0);
	}
}