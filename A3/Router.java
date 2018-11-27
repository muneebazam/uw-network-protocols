import java.net.*;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

class Router {

    static final int NBR_ROUTER = 5;
    static int router_id;    
    static int nse_port;
    static int router_port;
    static String nse_host;

    public static byte[] convertIntegersToBytes(int[] integers) {
        if (integers != null) {
            byte[] outputBytes = new byte[integers.length * 4];

            for(int i = 0, k = 0; i < integers.length; i++) {
                int integerTemp = integers[i];
                for(int j = 0; j < 4; j++, k++) {
                    outputBytes[k] = (byte)((integerTemp >> (8 * j)) & 0xFF);
                }
            }
            return outputBytes;
        } else {
            return null;
        }
    }

    public static void main(String args[]) throws Exception{

        // Handle command line arguments
        if (args.length != 4) {
            System.out.println("Incorrect number of arguements.");
            System.out.println("usage: router <router_id> <nse_host> <nse_port> <router_port>");
            System.exit(-1);
        } else {
            router_id = Integer.valueOf(args[0]);
            nse_host = args[1];
            nse_port = Integer.valueOf(args[2]);
            router_port = Integer.valueOf(args[3]);
        }

        // Create log file for this router
        String file_name = "router" + router_id + ".log";
        PrintWriter log = new PrintWriter(file_name);

        int[] int_data = {router_id};
        byte[] data = convertIntegersToBytes(int_data)
        DatagramSocket socket = new DatagramSocket(router_port);
        InetAddress clientIP = InetAddress.getByName(nse_host);
        DatagramPacket data_pkt = new DatagramPacket(data, data.length, clientIP, nse_port);
        socket.send(data_pkt);

        System.out.println("some problem maybe here");
        byte[] eot = new byte[4096];
        DatagramPacket eot_ack = new DatagramPacket(eot, eot.length);
        socket.receive(eot_ack);
        System.out.println("or here");
        socket.close();
        
    }

}
