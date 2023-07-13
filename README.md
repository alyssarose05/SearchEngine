### HOW TO COMPILE AND RUN:

First, extract the Corpus folder (done due to taking up a lot of space).

COMPILE: Type this command: javac SearchEngine.java

RUN: Type this command: java SearchEngine -stopwords [stopwords-file] -corpus [folder-path] -input [query-text-file] -query [query] -snippet [number] -noTextFile -gui -stemmingMode

- Stopwords fiile: stopListInput.txt
- Corpus folder path: Copy and paste your computer path to the Corpus folder
- Sample query text file: input.txt by default
---
- Text file output is the default. The output will be in the output.txt file that is automatically generated. To turn this off, use the -noTextFile flag.
- GUI output is turned off by default. To turn this on, use the -gui flag.
- The -input flag is for a text file that contains queries.
- The -query flag is for a single query. Must be in "double quotes"
- The -snippet flag means this: The first -snippet- words of a document will be shown in the search results.
- To trigger stemming mode, use the -stemmingMode flag.

Use --h or --help for more help on flags.
