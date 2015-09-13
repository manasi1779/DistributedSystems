import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class HangMan{
	String fileName;
    FileReader words;
    FileReader players;
    ArrayList<String> wordlist  = new ArrayList<String>();
    ArrayList<Player> allPlayers = new ArrayList<Player>();
    ArrayList<Player> currentPlayers = new ArrayList<Player>();
    private String guessWord;    
    
    public HangMan(){
    	try{
			words = new FileReader("words.txt");
			players = new FileReader("players.txt");
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
    }
    public static void main(String[] args) throws IOException, InterruptedException{
    	HangMan game1 = new HangMan();
    	game1.fileName = args[0];
    	int length = args.length;
    	for(int i = 1; i < length; i++)
    	game1.currentPlayers.add(new Player(args[i]));
        game1.chooseWord();
        for(Player player:game1.currentPlayers){
        	System.out.println("Player "+player.name);        	
        	player.playHangman(game1.guessWord);
        }
        game1.sortPlayers();
        game1.displayTop2Scorers();
    }
    
    private void displayTop2Scorers() {
		for(int i =0; i<2;i++){
			System.out.println(currentPlayers.get(i).name + " "+currentPlayers.get(i).score );
		}
		
	}
	private void sortPlayers() {
		for(int i = 0; i<currentPlayers.size();i++){
			for(int j=i+1; j < currentPlayers.size(); j++)
				if(currentPlayers.get(i).compareTo(currentPlayers.get(j))<0){
					Player temp = new Player("temp");
					temp = currentPlayers.get(i);
					currentPlayers.remove(i);
					currentPlayers.add(i, currentPlayers.get(j-1));	
					currentPlayers.remove(j);
					currentPlayers.add(j, temp);
				}
		}		
	}
	public void chooseWord(){
    	Scanner br  = new Scanner(words);
        String line;
        while(br.hasNext()){
        	line = br.nextLine();
			wordlist.add(line);
		}
        br.close();
        Random rand = new Random();
        int randomWordIndex = rand.nextInt(wordlist.size());
        guessWord = wordlist.get(randomWordIndex).toLowerCase();
        System.out.println(guessWord);
    }
    
	private boolean chkIfPlayerExists(String name){
		boolean entry = false;
    	for(Player x: allPlayers)
     		if(x.name.equals(name)){
     			entry = true;
     			break;
     		}
    	return entry;
	}
	
	public void updatePlayersFile(){
		try {
			FileWriter fw = new FileWriter("players.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class Player{
	String name;
	int score;
	String displayString = "";
	
	public Player(String name){
		this.name = name;
	}
	
	public void playHangman(String guessWord) throws IOException, InterruptedException{
		int incorrectCount = 8;
		Scanner br  = new Scanner(System.in);
		for(int i = 0; i<guessWord.length(); i++)
			displayString = displayString + "_";
		char guessLetter;
		while(incorrectCount>0){
			guessLetter = br.nextLine().charAt(0);
			if(guessWord.contains(""+guessLetter)){
				updateDisplayString(guessWord,guessLetter);
				score += 10;
				System.out.println(displayString);
				if(displayString.equals(guessWord))
					break;
			}				
			else{
				System.out.println(displayString);
				incorrectCount--;
				score -= 5;
			}		
		}
		if(incorrectCount ==0)
			drawHangMan();
	}
	
	private void updateDisplayString(String guessWord, char guessLetter) {
		for(int i = 0; i<guessWord.length(); i++)
			if(guessWord.charAt(i) == guessLetter)
				displayString = displayString.substring(0, i) + guessLetter + displayString.substring(i+1,displayString.length());
	}
	
	public void drawHangMan() throws InterruptedException{
		System.out.println("");
		for(int i = 0; i<15; i++){
			if(i ==0)
				System.out.print("  -----------|");	
			if(i>0)
			System.out.print("  "+'|');
			if(i==1 | i==2)
				System.out.print("          |");	
			if(i==3)
				System.out.print("         /^\\");
			if(i==4)
				System.out.print("         | |");
			if(i == 5)
				System.out.print("         \\_/");
			if(i == 7)
				System.out.print("         \\|/");
			if(i ==6|i ==8|i ==9)
				System.out.print("          |");
			if(i == 10)
				System.out.print("         / \\");
			System.out.println("");
		}
	}
	
	public int compareTo(Player p){
		return score-p.score;
	}
}