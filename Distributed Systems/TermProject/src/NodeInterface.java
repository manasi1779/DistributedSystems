import java.util.HashMap;

public interface NodeInterface {
	
	String entryNode = "kansas.cs.rit.edu";
	int entries = 10;
	//No. of nodes LOG base entries
	int levels = 3;
	static HashMap<Integer, String> addressTable = new HashMap<Integer, String>();

	class Init{
		static{
			addressTable.put(0, "buddy.cs.rit.edu");
			addressTable.put(1, "kansas.cs.rit.edu");
			addressTable.put(2, "glados.cs.rit.edu");
			addressTable.put(3, "newyork.cs.rit.edu");
			addressTable.put(4, "doors.cs.rit.edu");
			addressTable.put(5, "medusa.cs.rit.edu");
			addressTable.put(6, "kinks.cs.rit.edu");
			addressTable.put(7, "gorgon.cs.rit.edu");
		}
	}
	
	/**
	 * Publishes object when object is stored to root
	 * @param object
	 */
	public void publishObject(ObjectDataInterface object);
	
	/**
	 * Creates ObjectData for file and invokes
	 * @param name
	 */
	public void insertObject(String name);
	
	public void searchObject(String name);
	
	/**
	 * Node joins tapestry
	 */
	public void join();
	
	/**
	 * Node leaves tapestry
	 */
	public void leave();
	
	/**
	 * Un-publishes object when node is leaving tapestry
	 * @param object
	 */
	public void unpublishObject(ObjectDataInterface object);
	
	/**
	 * Routes search query to node storing object
	 * @param object
	 */
	public void routeToObject(ObjectDataInterface object);
	
	/**
	 * Stores object to node(?)
	 * @param node
	 * @param object
	 */
	public void routeToNode(NodeInterface node, ObjectDataInterface object);
	
	
	public String getGUID();
	
	/**
	 * To send publish messages
	 */
	public void multicast();
	
	/**
	 * After new Node is added or existing node is deleted,
	 * reroute(re-hash) some objects on surrogate node to new node 
	 */	
	public void reRouteObjects();
	
	/**
	 * Surrogate node notifies its neighbors so that they update their routing table for optimization
	 */
	public void notifyNeighbors();
	
	/**
	 * Update table for including
	 */
	public void updateTable();

}
