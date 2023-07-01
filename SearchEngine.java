import java.io.*;
import java.util.*;
public class SearchEngine {
	public static Stoplist stopList;
	public static InvertedIndex invertedIndex;
	public static File stopListInput, document, corpusFolder, index, porter;						
	public static String word;
	public static String[] query = new String[20];
	public static String[][] queries = new String[20][];
	public static File[] corpus;
	public static int snippet = 0;
	
	public static void main(String[] args) throws FileNotFoundException {
		
		// Flag system
		boolean stopListInputFlag = false;
		boolean searchWordFlag = false;
		boolean searchDocFlag = false;
		boolean printWordFlag = false;
		boolean printDocFlag = false;
		boolean corpusFlag = false;
		boolean invertedFlag = false;
		boolean porterFlag = false;
		boolean stemmingMode = false;
		boolean snippetFlag = false;
		boolean queryFlag = false;
		boolean guiFlag = false;
		
		// Text file output by default
		boolean textFlag = true;
		
		for (int i = 0; i < args.length; i++) {
			
			// Stoplist input file flag
			if (args[i].equalsIgnoreCase("-stopwords")) {
				stopListInput = new File(args[i+1]);
				stopListInputFlag = true;
			}

			// Search word flag
			if (args[i].equalsIgnoreCase("-SEARCH=WORD")) {
				word = args[i+1];
				searchWordFlag = true;
			}
			
			// Search document flag
			if (args[i].equalsIgnoreCase("-SEARCH=DOC")) {
				document = new File(args[i+1]);
				searchDocFlag = true;
			}
			
			// Print word flag
			if (args[i].equalsIgnoreCase("-PRINT_INDEX=WORD")) {
				word = args[i+1];
				printWordFlag = true;
			}
			
			// Print document flag
			if (args[i].equalsIgnoreCase("-PRINT_INDEX=DOC")) {
				document = new File(args[i+1]);
				printDocFlag = true;
			}
			
			// Corpus flag
			if (args[i].equalsIgnoreCase("-corpus" )) {
				corpusFolder= new File(args[i+1]);
				corpusFlag = true;
			}
			
			// Inverted index file flag
			if (args[i].equalsIgnoreCase("-invertedIndex")) {
				index = new File(args[i+1]);
				invertedFlag = true;
			}
			
			// Porter algorithm file flag
			if (args[i].equalsIgnoreCase("-porter")) {
				porter = new File(args[i+1]);
				porterFlag = true;
			}
			
			// Stemming mode flag
			if (args[i].equalsIgnoreCase("-stemmingMode")) 
				stemmingMode = true;
			
			// Snippet flag
			if (args[i].equalsIgnoreCase("-snippet"))
				snippet = Integer.parseInt(args[i+1]);
			
			// Query flag
			if (args[i].equalsIgnoreCase("-query")) {
				int queryIndex = 0;
				String first[] = args[i+1].split("\"");
				for (int j = 0; j < first.length; j++) {
					String second[] = first[j].split(" ");
					for (int k = 0; k < second.length; k++) {
						// Ignore special characters in query
						query[queryIndex++] = second[k].toLowerCase().replaceAll("\\s+|\\+|\\'|\\~|-|!|@|#|\\$|%|\\^|&|\\*|\"|\\[|\\]|\\.|:|;|<|>|,|\\/|\\\\|_|\\{|\\}|-|=|\\?|\\(|\\)|\\|",""); 
					}
				}
				queries[0] = query;
				queryFlag = true;
			}
			
			// Queries text file flag
			if (args[i].equalsIgnoreCase("-input")) {
				Scanner scr = new Scanner(new File(args[i+1]));
				int queryCount = 0;
				while (scr.hasNextLine()) {
					query = new String[25];
					int queryIndex = 0;
					String initial[] = scr.nextLine().split(" ");
					for (int j = 0; j < initial.length; j++) {
						// Ignore special characters in query
						query[queryIndex++] = initial[j].toLowerCase().replaceAll("\\s+|\\+|\\'|\\~|-|!|@|#|\\$|%|\\^|&|\\*|\"|\\[|\\]|\\.|:|;|<|>|,|\\/|\\\\|_|\\{|\\}|-|=|\\?|\\(|\\)|\\|",""); 
					}
					queries[queryCount++] = query;
				}
				queryFlag = true;
			}
			
			// No text file flag
			if (args[i].equalsIgnoreCase("-noTextFile")) 
				textFlag = false;
			
			// GUI output flag
			if (args[i].equalsIgnoreCase("-gui"))
				guiFlag = true;
			
			// Help flag
			if (args[i].equals("--h") || args[i].equals("--help")) {
				System.setOut(System.out);
				System.out.println("Flags:\n");
				System.out.println("INITIALIZATION FLAGS:");
				System.out.println("\t-stopwords <stopwords-file>: Fills the stoplist with stopwords");
				System.out.println("\t-corpus <folder-path>: Fills the corpus with documents in a folder");
				System.out.println("\t-invertedIndex <inverted-index-file>: Stores the inverted index");
				System.out.println("\t-porter <porter-file>: Stores the results of the Porter algorithm");
				System.out.println("\nSEARCH/PRINT FLAGS:");
				System.out.println("\t-SEARCH=WORD <word>: For searching for a word");
				System.out.println("\t-SEARCH=DOC <document>: For searching through a document");
				System.out.println("\t-PRINT_INDEX=WORD <word>: Prints inverted index information about a word");
				System.out.println("\t-PRINT_INDEX=DOC <document>: Prints inverted index information about a document");
				System.out.println("\nQUERY FLAGS:");
				System.out.println("\t-query <query>: Your query; must be in \"double quotes\"");
				System.out.println("\t-input <input-file>: A text file with queries in it");
				System.out.println("\t-snippet <number>: The first -snippet- words of a document are displayed in the search results.");
				System.out.println("\nTRIGGER FLAGS:");
				System.out.println("\t-noTextFile: Turns off the generation of an output text file"); 
				System.out.println("\t-gui: Turns on the generation of a GUI output file");
				System.out.println("\t-stemmingMode: Triggers stemming mode");
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
			}
		}
		
		// Default flag names, if needed
		if (!stopListInputFlag)
			stopListInput = new File("stopListInput.txt");
		if (!corpusFlag)
			corpusFolder = new File("corpus");
		if (!invertedFlag)
			index = new File("invertedIndex.txt");
		if (!porterFlag)
			porter = new File("porter.txt");
		
		// Fill corpus
		corpus = corpusFolder.listFiles();

		// Fill and output hash structure of stoplist
		System.setOut(new PrintStream(new File("stopListOutput.txt")));
		stopList = new Stoplist(stopListInput);
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		
		// Fill and output desired data from inverted index
		invertedIndex = new InvertedIndex(stopList,corpus,index,porter,stemmingMode,snippet,queries,guiFlag,textFlag);
		//System.setOut(new PrintStream(outputFile));
		
		// Search and/or print words/documents
		if (searchWordFlag)
			invertedIndex.searchWord(word);
		if (searchDocFlag)
			invertedIndex.searchDoc(document);
		if (printWordFlag)
			invertedIndex.printIndexWord(word);
		if (printDocFlag)
			invertedIndex.printIndexDoc(document); 
		if (queryFlag)
			invertedIndex.query();
	}
}