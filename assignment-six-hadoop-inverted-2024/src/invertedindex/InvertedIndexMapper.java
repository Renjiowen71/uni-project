package invertedindex;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvertedIndexMapper extends Mapper<LongWritable, Text, Text, LocationWritable> {
    private final LocationWritable loc = new LocationWritable();
    private final Text word = new Text();
    private Matcher matcher;
    static final Pattern nonWordPattern = Pattern.compile("[^a-zA-Z0-9]");
    static final Pattern documentIdPattern = Pattern.compile("id=\"(\\d+)\"");
    private Set<String> stopWords;

    @Override
    protected void setup(Context context) throws IOException {
        // Load stop words from the file
        Configuration conf = context.getConfiguration();
        String stopWordsFilePath = conf.get("stopwords.filename");
        stopWords = StopWordsLoader.loadStopWords(stopWordsFilePath);
    }
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        StringTokenizer tokenizer = new StringTokenizer(value.toString(), "\n");

        //Takes First Line <Document id ="">
        String documentId = getDocumentId(tokenizer, context);
        context.getCounter(InvertedIndex.StatsCounter.TOTAL_DOCUMENTS).increment(1);

        int sentence=0;
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            StringTokenizer tokenizerLine = new StringTokenizer(line);
            int index=0;
            while (tokenizerLine.hasMoreTokens()) {
                String token = tokenizerLine.nextToken();
                matcher = nonWordPattern.matcher(token);
                // Skip tokens containing characters other than [a-zA-Z0-9]
                if (matcher.find()) {
                    context.getCounter(InvertedIndex.StatsCounter.SKIPED_WORDS).increment(1);
                    continue;
                }
                if (!stopWords.contains(token)) {
                    //Setup key and value
                    word.set(token);
                    loc.newLocationWritable(Long.parseLong(documentId), sentence, index);
                    context.getCounter(InvertedIndex.StatsCounter.TOTAL_VALID_WORDS).increment(1);
                    context.getCounter(InvertedIndex.StatsCounter.TOTAL_VALID_WORD_LENGTH).increment(token.length());
                    context.write(word, loc);
                }
                //Add index for next token
                index+=token.length()+1;
            }
            sentence++;
        }
        long executionTime = System.currentTimeMillis() - startTime;
        context.getCounter(InvertedIndex.StatsCounter.EXECUTION_TIME_MAPPER).increment(executionTime);
    }

    private String getDocumentId(StringTokenizer tokenizer, Context context) {
        String firstLine = tokenizer.nextToken();
        matcher = documentIdPattern.matcher(firstLine);
        String documentId="";
        // Check if the pattern is found and extract the document id
        if (matcher.find()) {
            documentId = matcher.group(1);
        } else {
            context.getCounter(InvertedIndex.StatsCounter.DOCUMENT_ID_READ_FAILED).increment(1);
        }
        return documentId;
    }
}
