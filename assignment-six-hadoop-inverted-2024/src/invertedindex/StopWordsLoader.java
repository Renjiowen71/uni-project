package invertedindex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StopWordsLoader {

    public static Set<String> loadStopWords(String filePath) throws IOException {
        Set<String> stopWords = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    stopWords.add(line.toLowerCase()); // Convert to lowercase for case-insensitive comparison
                }
            }
        }

        return stopWords;
    }
}