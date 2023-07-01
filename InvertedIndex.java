import java.util.*;
import java.io.*;
import java.util.regex.*;
import javax.swing.*;
public class InvertedIndex {
	public static Hashtable<String,ArrayList<String>> invertedIndex = new Hashtable<String,ArrayList<String>>();
	public static Hashtable<String,ArrayList<String>> porterAlgorithm = new Hashtable<String,ArrayList<String>>();
	public static Hashtable<String,ArrayList<String>> usedTable;
	public static Stoplist stopList;
	public static File[] corpus;
	public static File index, porter;
	public static int snippet;
	public static String[][] queries;
	public static JFrame frame = new JFrame();
	public static JScrollPane scrollPane = new JScrollPane();
	public static JLabel label;
	public static boolean guiFlag, textFlag, stemmingMode;


	// Take a stoplist with stopwords and corpus with documents to store the inverted index.
	public InvertedIndex(Stoplist s, File[] c, File i, File p, boolean st, int sn, String[][] q, boolean g, boolean t) throws FileNotFoundException {
		this.stopList = s;
		this.corpus = c;
		this.index = i;
		this.porter = p;
		this.stemmingMode = st;
		this.snippet = sn;
		this.queries = q;
		this.guiFlag = g;
		this.textFlag = t;
		
		frame.setTitle("Search Engine Results");
		frame.setSize(600,600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Only execute method below if inverted index is empty. Otherwise, recall contents.
		if (index.length() == 0) 
			this.storeInvertedIndex();
		else
			this.recallInvertedIndex();
		
		// Only execute method below if porter algorithm is empty. Otherwise, recall contents.
		if (porter.length() == 0) 
			this.storeStemming();
		else
			this.recallStemming();
		
		// Does the user want stemming mode or not?
		if (stemmingMode) 
			usedTable = porterAlgorithm;
		else
			usedTable = invertedIndex;
		
	}
	
	// Fills the inverted index hash table if it doesn't already exist
	public void storeInvertedIndex() throws FileNotFoundException {
		for (int i = 0; i < corpus.length; i++) {
			int totalWordCount = 1;
			 Pattern p = Pattern.compile("[a-z0-9]>(.*?)</[a-z0-9]", Pattern.CASE_INSENSITIVE);
			Scanner scr = new Scanner(corpus[i]);
			while (scr.hasNextLine()) {
				String currLine = scr.nextLine().toLowerCase();
				Matcher m = p.matcher(currLine);
				String split[] = new String[1];
				while(m.find()) {
				String found = m.group(1);
				split = found.split("\\s+|\\+|\\'|\\~|-|!|@|#|\\$|%|\\^|&|\\*|\"|\\[|\\]|\\.|:|;|<|>|,|\\/|\\\\|_|\\{|\\}|-|=|\\?|\\(|\\)|\\|");
					for (int j = 0; j < split.length && split[j] != null; j++) {
						
						// Not a stopword, not empty, and not a number.
						if (!stopList.contains(split[j].toLowerCase()) && !split[j].toLowerCase().equals("")) {
							// The non-stopword isn't in the inverted index yet.
							if (!invertedIndex.containsKey(split[j])) 
								invertedIndex.put(split[j].toLowerCase(),new ArrayList<String>());

							// The non-stopword doesn't have the document yet, and we are at the snippet point where the word can be found.
								if (!invertedIndex.get(split[j].toLowerCase()).contains(corpus[i].getName())) {
									invertedIndex.get(split[j].toLowerCase()).add(corpus[i].getName()); 
									invertedIndex.get(split[j].toLowerCase()).add(String.valueOf(1)); 
									invertedIndex.get(split[j].toLowerCase()).add(String.valueOf(totalWordCount)); 
								}
						
								// The non-stopword already has the document; update total occurrence count.
								else {
									int myIndex = invertedIndex.get(split[j].toLowerCase()).indexOf(corpus[i].getName()) + 1;
									int wordCount = Integer.parseInt(invertedIndex.get(split[j].toLowerCase()).get(myIndex)) + 1;
									invertedIndex.get(split[j].toLowerCase()).set(myIndex,String.valueOf(wordCount));
								}
						}
					
					// Increase total wordcount whether it's a stopword or not.
					totalWordCount++;
					}
				}
			}
		} 
		
		// Write to inverted index text file for future reference
		System.setOut(new PrintStream(index));
		Enumeration<String> arrayLists = invertedIndex.keys();
		while (arrayLists.hasMoreElements()) {
			String word = arrayLists.nextElement();
			
			System.out.print(word + " ");
			for (int i = 0; i < invertedIndex.get(word).size()-1; i++) {
				System.out.print(invertedIndex.get(word).get(i) + " ");
			}
			System.out.print(invertedIndex.get(word).get(invertedIndex.get(word).size()-1) + "\n");
		}
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}
	
	// Which documents contain the word, and how many times in each document?
	public void searchWord(String word) throws FileNotFoundException {
		System.setOut(new PrintStream(new File("wordSearchOutput.txt")));
		System.out.println("Search results for the word '" + word + ":'\n");
		int numDocs = invertedIndex.get(word).size() / 3;
		
		// Which document, and how many times in that document?
		for (int i = 0; i < numDocs; i++) {
			System.out.println("Document: '" + invertedIndex.get(word).get(i*3) + "'");
			System.out.print("Number of Times: " + Integer.parseInt(invertedIndex.get(word).get(i*3 + 1)) + "\n\n");
		}
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}
	
	// Which words does the document have, and how many times for each word?
	public void searchDoc(File doc) throws FileNotFoundException {
		System.setOut(new PrintStream(new File("docSearchOutput.txt")));
		System.out.println("Search results for the document '" + doc.getName() + ":'\n");
		Enumeration<String> arrayLists = usedTable.keys();
		
		while (arrayLists.hasMoreElements()) {
			String word = arrayLists.nextElement();
			
			// Takes effect if a non-stopword contains the document
			if (usedTable.get(word).contains(doc.getName())) {
				System.out.println("Word: '" + word + "'");
				System.out.print("Number of Times: " + Integer.parseInt(usedTable.get(word).get(usedTable.get(word).indexOf(doc.getName()) + 1)) + "\n\n");
			}
		}
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}
	
	// Prints all inverted index information about a word
	public void printIndexWord(String word) throws FileNotFoundException {
		System.setOut(new PrintStream(new File("wordPrintIndexOutput.txt")));
		System.out.println("Inverted index information on the word '" + word + ":'\n");
		int numDocs = usedTable.get(word).size() / 3;
		for (int i = 0; i < numDocs; i++) {
			System.out.println("Document: '" + usedTable.get(word).get(i*3) + "'");
			System.out.print("Number of Times: " + Integer.parseInt(usedTable.get(word).get(i*3 + 1)) + "\n");
			System.out.print("Number of Words Before Being Found: " + Integer.parseInt(usedTable.get(word).get(i*3 + 2)) + "\n\n");
		}
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}
	
	// Prints all inverted index information about a document
	public void printIndexDoc(File doc) throws FileNotFoundException {
		System.setOut(new PrintStream(new File("docPrintIndexOutput.txt")));
		System.out.println("Inverted index information on the document '" + doc + ":'\n");
		Enumeration<String> arrayLists = usedTable.keys();
		
		while (arrayLists.hasMoreElements()) {
			String word = arrayLists.nextElement();
			
			// Takes effect if a non-stopword contains the document
			if (usedTable.get(word).contains(doc.getName())) {
				System.out.println("Word: '" + word + "'");
				System.out.print("Number of Times: " + Integer.parseInt(usedTable.get(word).get(usedTable.get(word).indexOf(doc.getName()) + 1)) + "\n");
				System.out.print("Number of Words Before Being Found: " + Integer.parseInt(usedTable.get(word).get(usedTable.get(word).indexOf(doc.getName()) + 2)) + "\n\n");
			}
		}
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}
	
	// Recalls the inverted index if it already exists
	public void recallInvertedIndex() throws FileNotFoundException {
		Scanner scr = new Scanner(index);
		while (scr.hasNextLine()) {
			String split[] = scr.nextLine().split(" ");
			invertedIndex.put(split[0],new ArrayList<String>());
			for (int i = 1; i < split.length; i++) 
				invertedIndex.get(split[0]).add(split[i]);
		}
	}
	
	// Fills the porter algorithm hash table if it doesn't already exist
	public void storeStemming() throws FileNotFoundException {
		System.setOut(new PrintStream(porter));
		Enumeration<String> arrayLists = invertedIndex.keys();
		while (arrayLists.hasMoreElements()) {
			String word = arrayLists.nextElement();
			String stemmed = PorterStemmer.stemWord(word);
			
			if (!porterAlgorithm.containsKey(stemmed)) { // Since multiple words can have the same stem, it could already be in the hash table.
				porterAlgorithm.put(stemmed,new ArrayList<String>());
				System.out.print(stemmed + " ");
			}
			
			for (int i = 0; i < invertedIndex.get(word).size()-1; i++) {
				if (!porterAlgorithm.get(stemmed).contains(invertedIndex.get(word).get(i)) || isNumber(invertedIndex.get(word).get(i))) { // If the document isn't already in the hash table, or if it's a number (since numbers can be the same for one stemmed word)
					porterAlgorithm.get(stemmed).add(invertedIndex.get(word).get(i));
					System.out.print(invertedIndex.get(word).get(i) + " ");
				}
			}
			System.out.print(porterAlgorithm.get(stemmed).get(porterAlgorithm.get(stemmed).size()-1) + "\n");
		}
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out))); // Just resets the output location back to the way it was
	}
	
	// Used by the storeStemming() method to see if a string is a number or not
	public boolean isNumber(String str) {
		if (str == null)
			return false;
		try {
			int i = Integer.parseInt(str);
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	// Recalls the stemmed words if they already exist
	public void recallStemming() throws FileNotFoundException {
		Scanner scr = new Scanner(porter);
		while (scr.hasNextLine()) {
			String split[] = scr.nextLine().split(" ");
			porterAlgorithm.put(split[0],new ArrayList<String>());
			for (int i = 1; i < split.length; i++)
				porterAlgorithm.get(split[0]).add(split[i]);
		}
	}
	
	// Returns the documents that contain ALL words of a query
	public void query() throws FileNotFoundException {
		String resultGUI = "";
		String resultText = "";
	
		// State snippet
		resultGUI += "SNIPPET: " + snippet + "<br>";
		resultText += "SNIPPET: " + snippet + "\n";
		
		// State stemming mode status
		resultGUI += "STEMMING: " + stemmingMode + "<br>";
		resultText += "STEMMING: " + stemmingMode + "\n";
	
		// State if other type of output was generated
		resultGUI += "TEXT FILE GENERATED: " + textFlag + "<br><br>";
		resultText += "GUI GENERATED: " + guiFlag + "\n\n";
		
		// For each query, print its individual result.
		for (int h = 0; h < queries.length && queries[h] != null; h++) {
			double relevanceCount = 0;
			double docCount = 0;
			
			// Initialize titles
			resultGUI += "Query #" + (h+1) + ": \"";
			resultText += "Query #" + (h+1) + ": \"";
			Pattern p = Pattern.compile("[a-z0-9]>(.*?)</[a-z0-9]", Pattern.CASE_INSENSITIVE);
			int i = 0;
			for (i = 0; i < queries[h].length && queries[h][i+1] != null; i++) {
				resultGUI += queries[h][i] + " ";
				resultText += queries[h][i] + " ";
			}
			resultGUI += queries[h][i] + "\"" + "<br><br>";
			resultText += queries[h][i] + "\"" + "\n\n";
			
			// Look through corpus for eligible files
			for (int j = 0; j < corpus.length; j++) {
				String split[] = new String[1];
				// Generate output for eligible file
				if (containsAllWords(corpus[j],queries[h])) {
					docCount++;
					
					resultGUI += corpus[j].getName() + "<br>";
					resultText += corpus[j].getName() + "\n";
					Scanner scr = new Scanner(corpus[j]);
					int count = 0;
					
					resultText += "TEXT SNIPPET: ";
					// Generate snippet for output
					while (scr.hasNextLine() && count < snippet) {
						String currLine = scr.nextLine().toLowerCase();
						Matcher m = p.matcher(currLine);
						while(m.find() && count < snippet) {
							String found = m.group(1);
							split = found.split("\\s+|\\+|\\'|\\~|-|!|@|#|\\$|%|\\^|&|\\*|\"|\\[|\\]|\\.|:|;|<|>|,|\\/|\\\\|_|\\{|\\}|-|=|\\?|\\(|\\)|\\|");
							for (int k = 0; k < split.length && split[k] != null && count < snippet; k++) {
								resultGUI += split[k] + " ";
								resultText += split[k] + " ";
								count++;
							}
							resultGUI += "<br>";
							resultText += "\n";
						}
					}
					resultGUI += "<br>";
					resultText += "\n";
				
					// Important information about word in document: number of occurrences and first occurence
					for (int k = 0; k < queries[h].length && queries[h][k] != null; k++) {
						if (!stopList.contains(queries[h][k])) {
							resultGUI += "WORD: " + queries[h][k] + "<br>";
							resultGUI += "Appears " + usedTable.get(queries[h][k]).get(usedTable.get(queries[h][k]).indexOf(corpus[j].getName()) + 1) + " time(s)<br>";
							resultGUI += "First found at position " + usedTable.get(queries[h][k]).get(usedTable.get(queries[h][k]).indexOf(corpus[j].getName()) + 2) + "<br><br>";
							
							resultText += "WORD: " + queries[h][k] + "\n";
							resultText += "Appears " + usedTable.get(queries[h][k]).get(usedTable.get(queries[h][k]).indexOf(corpus[j].getName()) + 1) + " time(s)\n";
							resultText += "First found at position " + usedTable.get(queries[h][k]).get(usedTable.get(queries[h][k]).indexOf(corpus[j].getName()) + 2) + "\n\n";
						}
					}
					resultGUI += "---------------------------------------------<br><br>";
					resultText += "---------------------------------------------\n\n";
				}
				
				// Check if returned document is relevant to query or not
				if (isRelevant(split,queries[h]))
					relevanceCount++;
				
			}
			
			/* RECALL: (# calculated relative docs) / (# expected # of relative docs returned)
			PRECISION: (# calculated relative docs) / (# of docs returned by search engine) */
			
			resultGUI += "RECALL: " + (relevanceCount/(corpus.length/queries.length)) + "<br>";
			resultGUI += "PRECISION: " + (relevanceCount/docCount) + "<br>";
			
			resultText += "RECALL: " + (relevanceCount/(corpus.length/queries.length)) + "\n";
			resultText += "PRECISION: " + (relevanceCount/docCount) + "\n";
			
			resultGUI += "========= END OF QUERY " + (h+1) + " ============<br><br>";
			resultText += "========= END OF QUERY " + (h+1) + " ============\n\n";
	
		}
		
		// Does the user want a GUI output?
		if (guiFlag) {
			label = new JLabel("<html>" + resultGUI + "</html>", SwingConstants.CENTER);
			scrollPane.setViewportView(label);
			frame.add(scrollPane);
			frame.setVisible(true);
		}
		
		// Does the user want a text output?
		if (textFlag) {
			System.setOut(new PrintStream(new File("output.txt")));
			System.out.print(resultText);
			System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		}
	}
	
	// Determines whether a document contains ALL words of a query or not
	public boolean containsAllWords(File doc, String[] query) {
		for (int i = 0; i < query.length && query[i] != null; i++) 
			if (!(stopList.contains(query[i])) && !(usedTable.containsKey(query[i]) && usedTable.get(query[i]).contains(doc.getName()))) // If not a stopword and document doesn't have the word
				return false;
		return true;
	}
	
	// Determines if a returned document is relevant to the query
	public boolean isRelevant(String[] split, String[] query) {
		for (int i = 0; i < query.length && query[i] != null; i++)
			for (int j = 0; j < split.length && split[j] != null; j++)
				if (query[i].equalsIgnoreCase(split[j]))
					return true;
		return false;
	}
}