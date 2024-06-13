package invertedindex;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
public class InvertedIndexComparator extends WritableComparator {

    protected InvertedIndexComparator() {
        super(Text.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        Text word1 = (Text) a;
        Text word2 = (Text) b;

        // Compare by word length in descending order
        int length1 = word1.getLength();
        int length2 = word2.getLength();

        // Sort by length in descending order
        if (length1 > length2) {
            return -1;
        } else if (length1 < length2) {
            return 1;
        } else {
            // If lengths are equal, use natural ordering of words
            return word1.compareTo(word2);
        }
    }
}
