package invertedindex;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public class DocumentWritable implements Writable {

    private HashMap<Long, HashSet<Long>> sentenceHashMap;
    private int totalData;

    public DocumentWritable() {
        this.sentenceHashMap = new HashMap<>();
        totalData=0;
    }

    public DocumentWritable(long senId, long locId){
        this.sentenceHashMap = new HashMap<>();
        HashSet<Long> locSet = new HashSet<>();
        locSet.add(locId);
        this.sentenceHashMap.put(senId, locSet);
        totalData=1;
    }

    public HashMap<Long, HashSet<Long>> getSentenceWritableHashMap() {
        return sentenceHashMap;
    }
    public int getTotalData() {
        return totalData;
    }
    public void merge(DocumentWritable documentWritable){
        // Update the totalData field by adding the totalData of the provided DocumentWritable
        this.totalData+=documentWritable.getTotalData();
        for (HashMap.Entry<Long, HashSet<Long>> entry : documentWritable.getSentenceWritableHashMap().entrySet()) {
            // Get the HashSet of values corresponding to the key from sentenceWritableHashMap
            HashSet<Long> locSet = this.sentenceHashMap.get(entry.getKey());
            // If the key does not exist, add a new entry
            if(locSet==null){
                this.sentenceHashMap.put(entry.getKey(), entry.getValue());
            } else {
                locSet.addAll(entry.getValue());
            }
        }
    }
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(totalData);
        // Write the size of the sentenceWritableHashMap
        out.writeInt(sentenceHashMap.size());
        // Write each entry in the sentenceWritableHashMap
        for (Map.Entry<Long, HashSet<Long>> entry : sentenceHashMap.entrySet()) {
            Long key = entry.getKey();
            HashSet<Long> value = entry.getValue();
            // Write the key
            out.writeLong(key);
            // Write the size of the HashSet
            out.writeInt(value.size());
            // Write each element in the HashSet
            for (Long val : value) {
                out.writeLong(val);
            }
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        totalData = in.readInt();
        // Read the size of the sentenceWritableHashMap
        int size = in.readInt();
        // Initialize the sentenceWritableHashMap
        sentenceHashMap = new HashMap<>(size);
        // Read each entry in the sentenceWritableHashMap
        for (int i = 0; i < size; i++) {
            Long key = in.readLong();
            int hashSetSize = in.readInt();
            // Initialize the HashSet
            HashSet<Long> hashSet = new HashSet<>();
            // Read each element in the HashSet
            for (int j = 0; j < hashSetSize; j++) {
                hashSet.add(in.readLong());
            }
            sentenceHashMap.put(key, hashSet);
        }
    }

    public void format(StringBuilder sb){
        //Print the total data
        sb.append(totalData);
        //Sort based on the sentence id
        TreeMap<Long, HashSet<Long>> sortedMap = new TreeMap<>(sentenceHashMap);
        for (Map.Entry<Long, HashSet<Long>> entry : sortedMap.entrySet()) {
            Long senId = entry.getKey();
            HashSet<Long> locSet = entry.getValue();
            //Enter and 2 tabs for sentence id
            sb.append("\n\t\t");
            sb.append(senId);
            //Sort the location id
            List<Long> list = new ArrayList<>(locSet);
            Collections.sort(list);
            //For each location id, print the id
            for(Long locId:list){
                sb.append(" ");
                sb.append(locId);
            }
        }
    }
}
