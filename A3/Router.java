import java.net.*;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.lang.IndexOutOfBoundsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;

/*
    Perform Dijkstra on every iteration
    - convert topolgy hashMap to adjacency lists
    - Copy implementation of Dijkstra algorithm
    - Modify algorithm to support INF's
    - Appropriate data structure to hold RIB
    - Print RIB helper function

*/

// ADD CHECKS FOR BIND EXCEPTION OR BETTER ERROR MESSAGING 

class Node {
     
    public int id;
     
    public List<Node> shortestPath = new LinkedList<>();
     
    public Integer distance = Integer.MAX_VALUE;
     
    public Map<Node, Integer> adjacentNodes = new HashMap<>();
 
    public void addDestination(Node destination, int distance) {
        adjacentNodes.put(destination, distance);
    }
  
    public Node(int id) {
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
 
    public static Graph calculateShortestPathFromSource(Graph graph, Node source) {
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

    static final int NBR_ROUTER = 5;
    static int router_id;    
    static int nse_port;
    static int router_port;
    static String nse_host;
    // change this to an array so we can loop through it in print
    static int router1_num_links = 0;
    static int router2_num_links = 0;
    static int router3_num_links = 0;
    static int router4_num_links = 0;
    static int router5_num_links = 0;
    static int total_links = 0;
    static HashMap<Integer, Tuple> topology = new HashMap<Integer, Tuple>();
    static ArrayList matched = new ArrayList();

    public static void printGraph(Graph graph) {
        for (Node n : graph.nodes) {
            System.out.println("Node: " + n.id);
            Map<Node, Integer> neighbors = n.adjacentNodes;
            Set set = neighbors.entrySet();
            Iterator iterator = set.iterator();
            System.out.print("Adjacent Nodes:");
            while(iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Node node = (Node) entry.getValue();
                System.out.print(" " + node.id);
            }
            System.out.print("\n");
        }
    }

    public static void printTopology(HashMap<Integer, Tuple> topology) {
        System.out.println("R" + router_id + " -> R1 nbr link " + router1_num_links);
        System.out.println("R" + router_id + " -> R2 nbr link " + router2_num_links);
        System.out.println("R" + router_id + " -> R3 nbr link " + router3_num_links);
        System.out.println("R" + router_id + " -> R4 nbr link " + router4_num_links);
        System.out.println("R" + router_id + " -> R5 nbr link " + router5_num_links);

        Set set = topology.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            System.out.print("R" + router_id + " -> ");
            Tuple temp = (Tuple) entry.getValue();
            System.out.println("R" + temp.router_id + " link " + temp.link_id + " cost " + temp.link_cost);
        }
    }

    public static void updateNumLinks(int router_id) {
        total_links += 1;
        switch(router_id) {
            case 1: 
                router1_num_links += 1;
                break;
            case 2:
                router2_num_links += 1;
                break;
            case 3:
                router3_num_links += 1;
                break;
            case 4:
                router4_num_links += 1;
                break;
            case 5:
                router5_num_links += 1;
                break;
            default:
                break;
        }
        return;
    }

    // loop through all links 

    public static void findDestinations(Graph graph, HashMap<Integer, Tuple> topology) {
        ArrayList checked = new ArrayList();
        for (int i = 0; i < topology.size()/2; i++) {
            Set set = topology.entrySet();
            Iterator iterator = set.iterator();

            int routerA_id = 0;
            int routerA_link = 0;
            int routerB_id = 0;
            int routerB_link = 0;
            while(iterator.hasNext()) {
                Map.Entry entry = (Map.Entry)iterator.next();
                Tuple tuple = (Tuple) entry.getValue();

                if (!matched.contains(tuple.link_id) && !checked.contains(tuple.link_id)) {
                    checked.add(tuple.link_id);
                    routerA_id = tuple.router_id;
                    routerA_link = tuple.link_id;
                    continue;
                }
                if (!matched.contains(tuple.link_id) && tuple.link_id == routerA_link) {
                    // found a pair
                    matched.add(routerA_link);
                    routerB_id = tuple.router_id;
                    routerB_link = tuple.link_id; 
                    System.out.println("The two routers are:" + routerA_id + " and " + routerB_id + " with link cost " + tuple.link_cost);
                    updateDestinations(graph, routerA_id, routerB_id, tuple.link_cost);
                }
            }
        }
        return;
    }

    public static void updateDestinations(Graph graph, int routerA_id, int routerB_id, int link_cost) {
        Node temp1 = null;
        Node temp2 = null;

        for (Node n: graph.nodes) {
            if (n.id == routerA_id) {
                System.out.println("found node: " + routerA_id);
                temp1 = n;
            }
            if (n.id == routerB_id) {
                System.out.println("found node: " + routerB_id);
                temp2 = n;
            }
        }
        temp1.addDestination(temp2, link_cost);
        temp2.addDestination(temp1, link_cost);
    }

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
        String file_name = "router" + router_id + ".log";
        PrintWriter log = new PrintWriter(file_name);

        int[] int_data = {router_id};
        byte[] data = convertIntegersToBytes(int_data);

        // add a check here or try catch 
        DatagramSocket socket = new DatagramSocket(router_port);
        InetAddress clientIP = InetAddress.getByName(nse_host);
        DatagramPacket data_pkt = new DatagramPacket(data, data.length, clientIP, nse_port);
        socket.send(data_pkt);

        byte[] eot = new byte[4096];
        DatagramPacket data_in = new DatagramPacket(eot, eot.length);
        socket.receive(data_in);

        ByteBuffer circuit_db = ByteBuffer.wrap(data_in.getData()).order(ByteOrder.LITTLE_ENDIAN);


        int nbr_routers = (int) circuit_db.getInt(0);
        System.out.println("the number of links attached to this router are: " + nbr_routers + "\n");

        int offset = 4;
        int link_ids[] = new int[nbr_routers];
        int link_costs[] = new int[nbr_routers];

        for (int i = 0; i < nbr_routers; i++) {
            link_ids[i] = (int) circuit_db.getInt(offset);
            offset += 4;
            link_costs[i] = (int) circuit_db.getInt(offset);
            offset += 4;
            Tuple temp = new Tuple(router_id, link_ids[i], link_costs[i]);
            String str_key = "" + router_id + link_ids[i] + link_costs[i];
            int key = Integer.parseInt(str_key);
            topology.put(key, temp);
            updateNumLinks(router_id);
            System.out.println("The link id is " + link_ids[i] + " and its cost is " + link_costs[i] + "\n");
        }

        ArrayList recv_hellos = new ArrayList();

        for (int i = 0; i < nbr_routers; i++) {
            System.out.println("Sending HELLO PDU to router number " + link_ids[i] + "\n");
            int[] hello_pdu_data = {router_id, link_ids[i]};
            byte[] hello_pdu = convertIntegersToBytes(hello_pdu_data);
            DatagramPacket hello_pdu_pkt = new DatagramPacket(hello_pdu, hello_pdu.length, clientIP, nse_port);
            socket.send(hello_pdu_pkt);
        }

        int recv_router_id;
        int recv_link_id;

        Graph graph = new Graph();

        for (int i = 0; i < nbr_routers; i++) {
            byte[] hello_pdu_buffer = new byte[4096];
            DatagramPacket hello_pdu_in = new DatagramPacket(hello_pdu_buffer, hello_pdu_buffer.length);
            socket.receive(hello_pdu_in);
            ByteBuffer ls_pdu = ByteBuffer.wrap(hello_pdu_in.getData()).order(ByteOrder.LITTLE_ENDIAN);

            recv_router_id = (int) ls_pdu.getInt(0);
            recv_link_id = (int) ls_pdu.getInt(4);
            recv_hellos.add(recv_router_id);

            System.out.println("Recieved a HELLO_PDU from router " + recv_router_id + " through link " + recv_link_id + "\n");

            Node node = new Node(recv_router_id);
            graph.addNode(node);
            
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
            if (topology.containsKey(key) || !recv_hellos.contains(ls_pdu_sender)) {
                printTopology(topology);
                printGraph(graph);
            } else {
                topology.put(key, temp);
                updateNumLinks(ls_pdu_router_id);
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
