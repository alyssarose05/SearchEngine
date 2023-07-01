import java.util.*;
import java.io.*;
public class Stoplist {
	public static Hashtable<Integer,String> stopWords = new Hashtable<Integer,String>();
	public static File inputFile;
	
	// Take a document with stopwords, store them, and output the hash structure.
	public Stoplist(File i) throws FileNotFoundException {
		this.inputFile = i;
		this.storeStopWords();
	}
	
	// Fill the hash structure with stopwords and output it.
	public void storeStopWords() throws FileNotFoundException {
		// Fill the hash structure, the Hashtable, with the stopwords by key.
		Scanner scr = new Scanner(this.inputFile);
		while (scr.hasNextLine()) {
			String stopWord = scr.nextLine();
			stopWords.put(stopWord.hashCode(),stopWord);
		}
		
		// Output the stopwords in the hash structure by key.
		System.out.println(stopWords);
	}
	
	// Directly refer to the contents of the hashtable.
	public boolean contains(String str) {
		return stopWords.contains(str);
	}
}