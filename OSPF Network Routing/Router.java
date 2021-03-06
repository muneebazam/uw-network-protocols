import java.net.*;
import java.util.*;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* 
 * CS456 Networks Shortest Path Routing (OSPF)
 * Muneeb Azam (20630292)
 * December 3 2018
 */

// Each router is a struct of type Node
class Node {
     
    public int id;
    public List<Node> shortestPath = new LinkedList<>(); // will store shortest path from s to this node
    public Integer distance = Integer.MAX_VALUE; 
    public Map<Node, Integer> adjacentNodes = new HashMap<>(); // stores its direct neighbours
    public void addDestination(Node destination, int distance) {
        adjacentNodes.put(destination, distance);
    }
  
    Node(int id) {
        this.id = id;
    }
}

// network represented as a Graph 
class Graph {
 
    public Set<Node> nodes = new HashSet<>();
    public void addNode(Node node) {
        nodes.add(node);
    }

    // Takes a set of nodes and returns the closest
    public static Node getLowestDistanceNode(Set<Node> unsettledNodes) {
        Node lowestDistanceNode = null;
        int lowestDistance = Integer.MAX_VALUE;
        for (Node node: unsettledNodes) {
            int nodeDistance = node.distance;
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    // calculates the minimum distance between two Nodes
    public static void calculateMinimumDistance(Node evaluationNode, Integer edgeWeigh, Node sourceNode) {
        Integer sourceDistance = sourceNode.distance;
        if (sourceDistance + edgeWeigh < evaluationNode.distance) {
            evaluationNode.distance = sourceDistance + edgeWeigh;
            LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.shortestPath);
            shortestPath.add(sourceNode);
            evaluationNode.shortestPath = shortestPath;
        }
    }
 
    // Dijkstra algorthim to update routing information base
    public static Graph dijkstra(Graph graph, Node source) {
        source.distance = 0;
        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();
        unsettledNodes.add(source);
 
        while (unsettledNodes.size() != 0) {
            Node currentNode = Graph.getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);
            for (Map.Entry<Node, Integer> adjacencyPair: 
            currentNode.adjacentNodes.entrySet()) {
                Node adjacentNode = adjacencyPair.getKey();
                Integer edgeWeight = adjacencyPair.getValue();
                if (!settledNodes.contains(adjacentNode)) {
                    Graph.calculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
        return graph;
    }
}

// Topology is comprised of tuples (router_id, link_id, link_cost)
// We test packets against our topology (HashMap<Integer, Tuple>) to determine
// if we have already received this packet
class Tuple {

    public int router_id;
    public int link_id;
    public int link_cost;

    Tuple(int a, int b, int c) {
        router_id = a;
        link_id = b;
        link_cost = c;
    }
}

// Router class
class Router {

    static final int NUM_ROUTERS = 5;
    static int router_id;    
    static int nse_port;
    static int router_port;
    static String nse_host;
    static int num_links[] = new int[NUM_ROUTERS];
    static HashMap<Integer, Tuple> topology = new HashMap<Integer, Tuple>(); // Link-State Database struct
    static ArrayList matched = new ArrayList(); // links for which we received ack from both ends
    static ArrayList hello_acks = new ArrayList(); // routers we've receieved hello's from
    static ArrayList nodeList = new ArrayList(); // router_id's
    static Graph graph = new Graph();
    static PrintWriter log;

    // Prints a graphs corresponding Routing Info Base to log
    public static void printRIB(Graph graph) {
        log.println("Printing Routing Information Base (RIB):");
        log.println("R" + router_id + " -> LOCAL, 0");
        for (Node n : graph.nodes) {
            if (n.id == router_id) {
                continue;
            } else {
                log.print("R" + router_id);
                List<Node> shortestPathList = n.shortestPath;
                for (int i = 1; i < shortestPathList.size(); i++) {
                    Node tmp = shortestPathList.get(i);
                    log.print(" -> R" + tmp.id);
                }
                log.print(" -> R" + n.id + ", " + n.distance);
            }
            log.print("\n");
        }
        log.print("\n");
    }

    // Prints the networks topology from the given HashMap
    public static void printTopology(HashMap<Integer, Tuple> topology) {
        log.println("Printing Link State Database:");
        for (int i = 0; i < NUM_ROUTERS; i++) {
            log.println("R" + router_id + " -> R" + (i + 1) + " nbr link " + num_links[i]);
        }
        Iterator iter = topology.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Tuple tuple = (Tuple) entry.getValue();
            log.println("R" + router_id + " -> R" + tuple.router_id + " link " + tuple.link_id + " cost " + tuple.link_cost);
        }
        log.print("\n");
    }

    // Finds which routers we can route to based on the current Topology and Graph status 
    public static void findDestinations(Graph graph, HashMap<Integer, Tuple> topology) {
        // loops through the entire topology and updates matched list
        // with routers we can route to based on current topology
        for (int i = 0; i < topology.size(); i++) {
            ArrayList checked = new ArrayList();
            Iterator iterator = topology.entrySet().iterator();
            int routerId = 0;
            int routerLink = 0;
            boolean inTesting = false;
            while(iterator.hasNext()) {
                Map.Entry entry = (Map.Entry)iterator.next();
                Tuple tuple = (Tuple) entry.getValue();
                if (!matched.contains(tuple.link_id) && !checked.contains(tuple.link_id) && !inTesting) {
                    checked.add(tuple.link_id);
                    routerId = tuple.router_id;
                    routerLink = tuple.link_id;
                    inTesting = true;
                } else if (!matched.contains(tuple.link_id) && tuple.link_id == routerLink) {
                    Node n1 = null;
                    Node n2 = null;
                    for (Node n: graph.nodes) {
                        if (n.id == routerId) {
                            n1 = n;
                        } else if (n.id == tuple.router_id) {
                            n2 = n;
                        }
                    }
                    matched.add(routerLink);
                    n1.addDestination(n2, tuple.link_cost);
                    n2.addDestination(n1, tuple.link_cost);
                }
            }
        }
    }

    // converts an int[] to byte[]
    public static byte[] convertIntegersToBytes(int[] nums) {
        if (nums != null) {
            byte[] outputBytes = new byte[nums.length * 4];
            for(int i = 0, k = 0; i < nums.length; i++) {
                int integerTemp = nums[i];
                for(int j = 0; j < 4; j++, k++) {
                    outputBytes[k] = (byte)((integerTemp >> (8 * j)) & 0xFF);
                }
            }
            return outputBytes;
        } else {
            return null;
        }
    }

    // main
    public static void main(String args[]) throws Exception{

        // Handle command line arguments
        if (args.length != 4) {
            System.out.println("Incorrect number of arguments.");
            System.out.println("usage: router <router_id> <nse_host> <nse_port> <router_port>");
            System.exit(-1);
        } else {
            router_id = Integer.valueOf(args[0]);
            nse_host = args[1];
            nse_port = Integer.valueOf(args[2]);
            router_port = Integer.valueOf(args[3]);
        }

        // setup log
        log = new PrintWriter("router" + router_id + ".log");

        // Send INIT to Emulator
        int[] init_data = {router_id};
        byte[] init_data_bytes = convertIntegersToBytes(init_data);
        DatagramSocket socket = new DatagramSocket(router_port);
        InetAddress clientIP = InetAddress.getByName(nse_host);
        DatagramPacket init_pkt = new DatagramPacket(init_data_bytes, init_data_bytes.length, clientIP, nse_port);
        log.println("R" + router_id + " sends an INIT PDU: router_id " + router_id + "\n");
        socket.send(init_pkt);

        // Receive CIRCUIT_DB from Emulator
        byte[] circuit_db_bytes = new byte[4096];
        DatagramPacket circuit_db_in = new DatagramPacket(circuit_db_bytes, circuit_db_bytes.length);
        socket.receive(circuit_db_in);
        ByteBuffer circuit_db = ByteBuffer.wrap(circuit_db_in.getData()).order(ByteOrder.LITTLE_ENDIAN);

        // Create arrays to represent direct links to this router and their respective costs 
        int nbr_routers = (int) circuit_db.getInt(0);
        int link_ids[] = new int[nbr_routers];
        int link_costs[] = new int[nbr_routers];
        log.println("R" + router_id + " receives a CIRCUIT DB: nbr_links " + nbr_routers + "\n");
        log.flush();

        // Populate above arrays and start building topology
        int offset = 4;
        for (int i = 0; i < nbr_routers; i++) {
            link_ids[i] = (int) circuit_db.getInt(offset);
            offset += 4;
            link_costs[i] = (int) circuit_db.getInt(offset);
            offset += 4;
            Tuple temp = new Tuple(router_id, link_ids[i], link_costs[i]);
            String key = "" + router_id + link_ids[i] + link_costs[i];
            topology.put(Integer.parseInt(key), temp);
            num_links[router_id - 1] += 1;
        }

        // send HELLO_PDU on all direct links
        for (int i = 0; i < nbr_routers; i++) {
        	log.println("R" + router_id + " sends a HELLO PDU: router_id " + router_id + ", link_id " + link_ids[i] + "\n");
            int[] hello_pdu_data = {router_id, link_ids[i]};
            byte[] hello_pdu_bytes = convertIntegersToBytes(hello_pdu_data);
            DatagramPacket hello_pdu_pkt = new DatagramPacket(hello_pdu_bytes, hello_pdu_bytes.length, clientIP, nse_port);
            socket.send(hello_pdu_pkt);
            log.flush();
        }

        // Add this router (Node) to the graph struct
        Node source_node = new Node(router_id);
        graph.addNode(source_node);
        nodeList.add(router_id);

        // Send an LS_PDU on all links we've received a HELLO_PDU from
        for (int i = 0; i < nbr_routers; i++) {

            // Receives a HELLO_PDU
            byte[] hello_pdu_buffer = new byte[4096];
            DatagramPacket hello_pdu_in = new DatagramPacket(hello_pdu_buffer, hello_pdu_buffer.length);
            socket.receive(hello_pdu_in);
            ByteBuffer ls_pdu = ByteBuffer.wrap(hello_pdu_in.getData()).order(ByteOrder.LITTLE_ENDIAN);

            // Extract data and add router to our list
            int recv_router_id = (int) ls_pdu.getInt(0);
            int recv_link_id = (int) ls_pdu.getInt(4);
            hello_acks.add(recv_router_id);
            log.println("R" + router_id + " receives a HELLO PDU: router_id " + recv_router_id + ", link_id " + recv_link_id + "\n");

            // send set of LS_PDU's describing our current topology 
            for (int j = 0; j < nbr_routers; j++) {
                log.println("R" + router_id + " sends an LS PDU: sender " + router_id + ", router_id " + router_id + ", link_id " + link_ids[j] + ", link_cost " + link_costs[j] + ", via " + recv_link_id + "\n");
                int[] ls_pdu_data = {router_id, router_id, link_ids[j], link_costs[j], recv_link_id};
                byte[] ls_pdu_send = convertIntegersToBytes(ls_pdu_data);
                DatagramPacket ls_pdu_pkt = new DatagramPacket(ls_pdu_send, ls_pdu_send.length, clientIP, nse_port);
                socket.send(ls_pdu_pkt);
            }
            log.flush();
        }

        // infinite
        while (true) {

            // Receive an LS_PDU
            byte[] ls_pdu_bytes = new byte[4096];
            DatagramPacket ls_pdu_in = new DatagramPacket(ls_pdu_bytes, ls_pdu_bytes.length);
            socket.receive(ls_pdu_in);

            // Extract data 
            ByteBuffer recv_ls_pdu = ByteBuffer.wrap(ls_pdu_in.getData()).order(ByteOrder.LITTLE_ENDIAN);
            int ls_pdu_sender = (int) recv_ls_pdu.getInt(0);
            int ls_pdu_router_id = (int) recv_ls_pdu.getInt(4);
            int ls_pdu_link_id = (int) recv_ls_pdu.getInt(8);
            int ls_pdu_link_cost = (int) recv_ls_pdu.getInt(12);
            int ls_pdu_via = (int) recv_ls_pdu.getInt(16);
            log.println("R" + router_id + " receives an LS PDU: sender " + ls_pdu_sender + ", router_id " + ls_pdu_router_id + ", link_id " + ls_pdu_link_id + ", link_cost " + ls_pdu_link_cost + ", via " + ls_pdu_via + "\n");

            // create a tuple to represent LS_PDU 
            // We concatenate these 3 fields together to form an integer key which
            // acts as the lookup in our topology allowing us an O(1) lookup
            Tuple temp = new Tuple(ls_pdu_router_id, ls_pdu_link_id, ls_pdu_link_cost);
            String str_key = "" + ls_pdu_router_id + ls_pdu_link_id + ls_pdu_link_cost;
            int key = Integer.parseInt(str_key);

            // if key is in our topology we have recieved this packet so we will discard
            if (topology.containsKey(key) || !hello_acks.contains(ls_pdu_sender)) {
                continue;

            } else {
                // if this is the first packet from this router we have discovered
                // a new router and so we add it to our graph and topology
                topology.put(key, temp);
                num_links[ls_pdu_router_id - 1] += 1;
                if (!nodeList.contains(ls_pdu_router_id)) {
                    nodeList.add(ls_pdu_router_id);
                    Node node = new Node(ls_pdu_router_id);
                    graph.addNode(node);
                }

                // update our reachable destinations and run shortest path
                findDestinations(graph, topology);
                graph = Graph.dijkstra(graph, source_node);

                // Propogate LS_PDU through network (Except for link we recieved on)
                for (int i = 0; i < link_ids.length; i++) {
                    if ((int) link_ids[i] == ls_pdu_via) {
                        continue;
                    } else {
                    	log.println("R" + router_id + " sends an LS PDU: sender " + router_id + ", router_id " + ls_pdu_router_id + ", link_id " + ls_pdu_link_id + ", link_cost " + ls_pdu_link_cost + ", via " + link_ids[i] + "\n");
                        int[] recv_ls_pdu_data = {router_id, ls_pdu_router_id, ls_pdu_link_id, ls_pdu_link_cost, link_ids[i]};
                        byte[] recv_ls_pdu_pkt = convertIntegersToBytes(recv_ls_pdu_data);
                        DatagramPacket ls_pdu_pkt_propogate = new DatagramPacket(recv_ls_pdu_pkt, recv_ls_pdu_pkt.length, clientIP, nse_port);
                        socket.send(ls_pdu_pkt_propogate);
                    }
                }

                // If we have reached this point our topology
                // and graph were updated, log a new version
                printTopology(topology);
                printRIB(graph); 
                log.print("\n");
            }
            log.flush();
        }
    }
}
