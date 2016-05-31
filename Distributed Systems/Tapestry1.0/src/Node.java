import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Node {

	boolean bootstrap;
	String ID;
	final int max = 100;
	final int maxIndices = 10;
	String name;
	ArrayList<String>[] levelZero;
	ArrayList<String>[] levelOne;

	ArrayList<String>[] levelZeroNodeNames;
	ArrayList<String>[] levelOneNodeNames;
	String root;

	@SuppressWarnings("unchecked")
	public Node()
	{
		levelZero = (ArrayList<String>[])new ArrayList[maxIndices];
		levelOne = (ArrayList<String>[])new ArrayList[maxIndices];
		levelZeroNodeNames = (ArrayList<String>[])new ArrayList[maxIndices];
		levelOneNodeNames = (ArrayList<String>[])new ArrayList[maxIndices];

		ID = new String();
		ID = "";
		try {
			name = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		getID(name);

		System.out.println("My ID " + ID);

		if ( name.equalsIgnoreCase("glados"))
		{
			bootstrap = true;
		}

		else
		{
			bootstrap = false;
		}

		for ( int temp = 0; temp < maxIndices; temp++ )
		{
			levelZero[temp] = new ArrayList<>();
			levelOne[temp] = new ArrayList<>();
			levelZeroNodeNames[temp] = new ArrayList<>();
			levelOneNodeNames[temp] = new ArrayList<>();
		}

		root = "glados.cs.rit.edu";

		//adding self to table
		//addToTable(0, Character.getNumericValue(ID.charAt(0)), ID, name);
		//addToTable(1, Character.getNumericValue(ID.charAt(1)), ID, name);

		//level0 = new ArrayList();
		//level1 = new ArrayList();
	}

	public void getID(String name)
	{
		int val = name.hashCode() % max;

		if ( val < 0 )
		{
			val = val * (-1);
		}

		//System.out.println("Got val " + val);
		if ( val / 10 == 0 ) //only single digit
		{
			ID = String.valueOf(val);
			ID = "0" + ID;
		}
		else
		{
			ID = String.valueOf(val);
		}
	}

	public void addToTable ( int level, int index, String ID, String name )
	{
		if ( level == 0 )
		{
			if ( !levelZero[index].contains(ID))
			{
				//System.out.println("Adding to level 0 ");
				levelZero[index].add(ID);
				levelZeroNodeNames[index].add(name);
			}

		}
		else if ( level == 1 )
		{
			if ( !levelOne[index].contains(ID))
			{
				//System.out.println("Adding to level 1  ");
				levelOne[index].add(ID);
				levelOneNodeNames[index].add(name);
			}
		}
    /*
		System.out.println("My table");

		System.out.println("Level 0 ");
		for ( int temp = 0 ; temp < maxIndices; temp++ )
		{
			if ( levelZero[temp].size() > 0 )
			{
				System.out.print("Index " + temp);
				int sizeParsing = levelZero[temp].size();

				for ( int temp2 = 0; temp2 < sizeParsing; temp2++ )
				{
					System.out.print(levelZeroNodeNames[temp].get(temp2) 
							+ " " + levelZero[temp].get(temp2) + " ");
				}
				System.out.println();
			}
		}

		System.out.println("Level 1 ");
		for ( int temp = 0 ; temp < maxIndices; temp++ )
		{
			if ( levelOne[temp].size() > 0 )
			{
				System.out.print("Level index " + temp);
				int sizeParsing = levelOne[temp].size();

				for ( int temp2 = 0; temp2 < sizeParsing; temp2++ )
				{
					System.out.print(levelOneNodeNames[temp].get(temp2) 
							+ " " + levelOne[temp].get(temp2) + " ");
				}
				System.out.println();
			}
		}
   */
	}

	public String getNeighborName( int level, int index, int nodeCount )
	{
		if ( level == 0 )
		{
			return levelZeroNodeNames[index].get(nodeCount);
		}
		else
		{
			return levelOneNodeNames[index].get(nodeCount);
		}
	}
	
	public String getNeighborID( int level, int index, int nodeCount )
	{
		if ( level == 0 )
		{
			return levelZero[index].get(nodeCount);
		}
		else
		{
			return levelOne[index].get(nodeCount);
		}
	}
}
