package invertedindex;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public class LocationWritable implements Writable {

    private HashMap<Long, DocumentWritable> documentWritableHashMap;
    private int totalData;
    public LocationWritable() {
        this.documentWritableHashMap = new HashMap<>();
        this.totalData = 0;
    }
    public HashMap<Long, DocumentWritable> getDocumentWritableHashMap() {
        return documentWritableHashMap;
    }
    public void reset() {
        this.documentWritableHashMap.clear();
        this.totalData=0;
    }
    public int getTotalData(){
        return this.totalData;
    }
    public void newLocationWritable(long docId, long senId, long locId){
        this.documentWritableHashMap.clear();
        this.documentWritableHashMap.put(docId, new DocumentWritable(senId, locId));
        this.totalData=1;
    }
    public void merge(LocationWritable locationWritable){
        this.totalData+= locationWritable.getTotalData();
        for (HashMap.Entry<Long, DocumentWritable> entry : locationWritable.getDocumentWritableHashMap().entrySet()) {
            DocumentWritable documentWritable = this.documentWritableHashMap.get(entry.getKey());
            if(documentWritable==null){
                this.documentWritableHashMap.put(entry.getKey(), entry.getValue());
            } else {
                documentWritable.merge(entry.getValue());
            }
        }
    }
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(this.totalData);
        // Write the size of the HashMap
        out.writeInt(documentWritableHashMap.size());
        // Write each entry in the HashMap
        for (HashMap.Entry<Long, DocumentWritable> entry : documentWritableHashMap.entrySet()) {
            out.writeLong(entry.getKey()); // Write the key (Long)
            entry.getValue().write(out);  // Write the value (DocumentWritable)
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.totalData = in.readInt();
        // Read the size of the HashMap
        int size = in.readInt();
        // Initialize the HashMap
        documentWritableHashMap = new HashMap<>();
        // Read each entry in the HashMap
        for (int i = 0; i < size; i++) {
            Long key = in.readLong(); // Read the key (Long)
            DocumentWritable value = new DocumentWritable();
            value.readFields(in); // Read the value (DocumentWritable)
            documentWritableHashMap.put(key, value); // Add the entry to the HashMap
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //Print total data
        sb.append(" ");
        sb.append(this.totalData);
        //Sort the document id
        TreeMap<Long, DocumentWritable> sortedMap = new TreeMap<>(documentWritableHashMap);
        //For each document id print DocumentWritable
        for (Map.Entry<Long, DocumentWritable> entry : sortedMap.entrySet()) {
            Long docId = entry.getKey();
            DocumentWritable documentWritable = entry.getValue();
            //Enter and 1 tab for document id
            sb.append("\n\t");
            sb.append(docId);
            sb.append(" ");
            documentWritable.format(sb);
        }
        return sb.toString();
    }
}
