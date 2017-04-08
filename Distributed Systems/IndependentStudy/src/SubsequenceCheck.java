
public class SubsequenceCheck {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(getLongestMatchingSubSequence("dabc", "abcb"));
		String[] pattern1 = getOnlyIoTIDs("22:68 11:55 33:56");
		String[] pattern2 = getOnlyIoTIDs("11:79 33:51 22:17");
		getLongestMatchingSubSequence(pattern1, pattern2);
	}
	
	public static String[] getOnlyIoTIDs(String data){
		String blocks[] = data.split(" ");
		String output[] = new String[blocks.length];
		int i =0;
		for(String block:blocks){
			output[i] = block.substring(0, block.indexOf(":"));
			i++;
		}
		return output;
	}

	public static int getLongestMatchingSubSequence(String pattern1, String pattern2){
		String subsequence = "";
		int matchSize[][]  = new int[pattern1.length() + 1][pattern2.length() + 1];
		for(int i = 0; i < pattern1.length()+1; i++){
			for(int j = 0; j < pattern2.length()+1; j++){
				if (i == 0 || j == 0)
					matchSize[i][j] = 0;
				else if(pattern1.charAt(i-1) == pattern2.charAt(j-1)){
					matchSize[i][j] = matchSize[i-1][j-1] + 1;
				}
				else{
					int max = Math.max(matchSize[i][j-1], matchSize[i-1][j]);
					matchSize[i][j] = max;
				}
			}
		}
		for(int j = 0; j < pattern2.length(); j++){
			if(matchSize[pattern1.length()][j] +1 == matchSize[pattern1.length()][j+1]){
				subsequence += pattern2.charAt(j);
			}
		}
		System.out.println(subsequence);
		return matchSize[pattern1.length()][pattern2.length()];
	}
	
	public static int getLongestMatchingSubSequence(String[] pattern1, String[] pattern2){
		String subsequence = "";
		int matchSize[][]  = new int[pattern1.length + 1][pattern2.length + 1];
		for(int i = 0; i < pattern1.length+1; i++){
			for(int j = 0; j < pattern2.length+1; j++){
				if (i == 0 || j == 0)
					matchSize[i][j] = 0;
				else if(pattern1[i-1].equals(pattern2[j-1])){
					matchSize[i][j] = matchSize[i-1][j-1] + 1;
				}
				else{
					int max = Math.max(matchSize[i][j-1], matchSize[i-1][j]);
					matchSize[i][j] = max;
				}
			}
		}
		for(int j = 0; j < pattern2.length; j++){
			if(matchSize[pattern1.length][j] +1 == matchSize[pattern1.length][j+1]){
				subsequence += ":"+pattern2[j];
			}
		}
		System.out.println("Matching pattern: "+subsequence);
		return matchSize[pattern1.length][pattern2.length];
	}
}
