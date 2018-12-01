import java.net.*;
import java.util.*;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// ADD CHECKS FOR BIND EXCEPTION OR BETTER ERROR MESSAGING 

class Node {
     
    public int id;
    public List<Node> shortestPath = new LinkedList<>();
    public Integer distance = Integer.MAX_VALUE;
    public Map<Node, Integer> adjacentNodes = new HashMap<>();
    public void addDestination(Node destination, int distance) {
        adjacentNodes.put(destination, distance);
    }
  
    Node(int id) {
        this.id = id;
    }
}

class Graph {
 
    public Set<Node> nodes = new HashSet<>();
    public void addNode(Node node) {
        nodes.add(node);
    }

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

    public static void calculateMinimumDistance(Node evaluationNode, Integer edgeWeigh, Node sourceNode) {
        Integer sourceDistance = sourceNode.distance;
        if (sourceDistance + edgeWeigh < evaluationNode.distance) {
            evaluationNode.distance = sourceDistance + edgeWeigh;
            LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.shortestPath);
            shortestPath.add(sourceNode);
            evaluationNode.shortestPath = shortestPath;
        }
    }
 
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

class Router {

    static final int NUM_ROUTERS = 5;
    static int router_id;    
    static int nse_port;
    static int router_port;
    static String nse_host;
    // change this to an array so we can loop through it in print
    static int num_links[] = new int[NUM_ROUTERS];
    static HashMap<Integer, Tuple> topology = new HashMap<Integer, Tuple>();
    static ArrayList matched = new ArrayList();
    static ArrayList hello_acks = new ArrayList();

    public static void printGraph(Graph graph) {
        for (Node n : graph.nodes) {
            System.out.println("Node: " + n.id);
            System.out.print("Adjacent Nodes:");

            for (Map.Entry<Node, Integer> neighbor: n.adjacentNodes.entrySet()) {
                Node node = neighbor.getKey();
                System.out.print(" " + node.id);
            }
            System.out.print("\n");
        }
    }

    public static void printTopology(HashMap<Integer, Tuple> topology) {
        for (int i = 0; i < NUM_ROUTERS; i++) {
            System.out.println("R" + router_id + " -> R" + (i + 1) + " nbr link " + num_links[i]);
        }
        Iterator iter = topology.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Tuple tuple = (Tuple) entry.getValue();
            System.out.println("R" + router_id + " -> R" + tuple.router_id + " link " + tuple.link_id + " cost " + tuple.link_cost);
        }
    }

    public static void findDestinations(Graph graph, HashMap<Integer, Tuple> topology) {
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

        // Create log file for this router
        PrintWriter log = new PrintWriter("router" + router_id + ".log");

        // Send INIT and receive CIRCUIT_DB from Emulator 
        int[] init_data = {router_id};
        byte[] init_data_bytes = convertIntegersToBytes(init_data);
        DatagramSocket socket = new DatagramSocket(router_port);
        InetAddress clientIP = InetAddress.getByName(nse_host);
        DatagramPacket init_pkt = new DatagramPacket(init_data_bytes, init_data_bytes.length, clientIP, nse_port);
        socket.send(init_pkt);
        byte[] circuit_db_bytes = new byte[4096];
        DatagramPacket circuit_db_in = new DatagramPacket(circuit_db_bytes, circuit_db_bytes.length);
        socket.receive(circuit_db_in);
        ByteBuffer circuit_db = ByteBuffer.wrap(circuit_db_in.getData()).order(ByteOrder.LITTLE_ENDIAN);

        // create arrays to represent direct links to this router and their respective costs 
        int offset = 0;
        int nbr_routers = (int) circuit_db.getInt(offset);
        int link_ids[] = new int[nbr_routers];
        int link_costs[] = new int[nbr_routers];
        offset += 4;

        // update above arrays and start building link state database (topology)
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
            int[] hello_pdu_data = {router_id, link_ids[i]};
            byte[] hello_pdu_bytes = convertIntegersToBytes(hello_pdu_data);
            DatagramPacket hello_pdu_pkt = new DatagramPacket(hello_pdu_bytes, hello_pdu_bytes.length, clientIP, nse_port);
            socket.send(hello_pdu_pkt);
        }

        Graph graph = new Graph();
        ArrayList nodeList = new ArrayList();

        Node source_node = new Node(router_id);
        graph.addNode(source_node);
        nodeList.add(router_id);

        for (int i = 0; i < nbr_routers; i++) {
            byte[] hello_pdu_buffer = new byte[4096];
            DatagramPacket hello_pdu_in = new DatagramPacket(hello_pdu_buffer, hello_pdu_buffer.length);
            socket.receive(hello_pdu_in);
            ByteBuffer ls_pdu = ByteBuffer.wrap(hello_pdu_in.getData()).order(ByteOrder.LITTLE_ENDIAN);

            int recv_router_id = (int) ls_pdu.getInt(0);
            int recv_link_id = (int) ls_pdu.getInt(4);
            hello_acks.add(recv_router_id);

            System.out.println("Recieved a HELLO_PDU from router " + recv_router_id + " through link " + recv_link_id + "\n");
            
            for (int j = 0; j < nbr_routers; j++) {
                // send LS_PDU each time
                System.out.println("Sending an LS_PDU to router " + recv_router_id + " from router " + router_id + " containing link id " + link_ids[j] + " with cost " + link_costs[j] + " through link " + recv_link_id + "\n");
                int[] ls_pdu_data = {router_id, router_id, link_ids[j], link_costs[j], recv_link_id};
                byte[] ls_pdu_send = convertIntegersToBytes(ls_pdu_data);
                DatagramPacket ls_pdu_pkt = new DatagramPacket(ls_pdu_send, ls_pdu_send.length, clientIP, nse_port);
                socket.send(ls_pdu_pkt);
            }
        }

        int ls_pdu_sender;
        int ls_pdu_router_id;
        int ls_pdu_link_id;
        int ls_pdu_link_cost;
        int ls_pdu_via;

        while (true) {
            byte[] ls_pdu_buffer = new byte[4096];
            DatagramPacket ls_pdu_in = new DatagramPacket(ls_pdu_buffer, ls_pdu_buffer.length);
            socket.receive(ls_pdu_in);
            ByteBuffer recv_ls_pdu = ByteBuffer.wrap(ls_pdu_in.getData()).order(ByteOrder.LITTLE_ENDIAN);

            ls_pdu_sender = (int) recv_ls_pdu.getInt(0);
            ls_pdu_router_id = (int) recv_ls_pdu.getInt(4);
            ls_pdu_link_id = (int) recv_ls_pdu.getInt(8);
            ls_pdu_link_cost = (int) recv_ls_pdu.getInt(12);
            ls_pdu_via = (int) recv_ls_pdu.getInt(16);

            System.out.println("Received an LS_PDU from router " + ls_pdu_sender + " via link id " + ls_pdu_via + " that " + ls_pdu_router_id + " has a link with id " + ls_pdu_link_id + " with cost " + ls_pdu_link_cost + "\n");

            Tuple temp = new Tuple(ls_pdu_router_id, ls_pdu_link_id, ls_pdu_link_cost);
            String str_key = "" + ls_pdu_router_id + ls_pdu_link_id + ls_pdu_link_cost;
            int key = Integer.parseInt(str_key);
            if (topology.containsKey(key) || !hello_acks.contains(ls_pdu_sender)) {
                printTopology(topology);
                graph = Graph.dijkstra(graph, source_node);
                System.out.println("ABOUT TO PRINT THE RIB TABLE RIGHT HERE FAM");
                for (Node n : graph.nodes) {
                    System.out.print("NODE: " + n.id + ", PATH: ");
                    List<Node> shortestPathList = n.shortestPath;
                    for (int i = 0; i < shortestPathList.size(); i++) {
                        Node tmp = shortestPathList.get(i);
                        System.out.print(" " + tmp.id);
                    }
                    System.out.print("\n");
                }
                printGraph(graph);
            } else {
                if (!nodeList.contains(ls_pdu_router_id)) {
                    nodeList.add(ls_pdu_router_id);
                    Node node = new Node(ls_pdu_router_id);
                    graph.addNode(node);
                }
                topology.put(key, temp);
                num_links[ls_pdu_router_id - 1] += 1;
                findDestinations(graph, topology);
                for (int i = 0; i < link_ids.length; i++) {
                    if ((int) link_ids[i] == ls_pdu_via) {
                        continue;
                    } else {
                        System.out.println("Sending an LS_PDU from router " + router_id + " stating that router " + ls_pdu_router_id + " has link " + ls_pdu_link_id + " with cost " + ls_pdu_link_cost + " . Sending through link " + link_ids[i] + "\n");
                        int[] recv_ls_pdu_data = {router_id, ls_pdu_router_id, ls_pdu_link_id, ls_pdu_link_cost, link_ids[i]};
                        byte[] recv_ls_pdu_pkt = convertIntegersToBytes(recv_ls_pdu_data);
                        DatagramPacket ls_pdu_pkt_propogate = new DatagramPacket(recv_ls_pdu_pkt, recv_ls_pdu_pkt.length, clientIP, nse_port);
                        socket.send(ls_pdu_pkt_propogate);
                    }
                }  
            }
        }
    }
}
