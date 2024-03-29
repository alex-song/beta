package alex.beta.torrentparser.bencoding.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by christophe on 15.01.15.
 */
public class BList implements IBencodable {
    private final List<IBencodable> list;
    public byte[] blob;

    public BList() {
        this.list = new LinkedList<IBencodable>();
    }

    ////////////////////////////////////////////////////////////////////////////
    //// LOGIC METHODS /////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    public Iterator<IBencodable> getIterator() {
        return list.iterator();
    }

    public void add(IBencodable o) {
        this.list.add(o);
    }

    ////////////////////////////////////////////////////////////////////////////
    //// BENCODING /////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public String bencodedString() {
        StringBuilder sb = new StringBuilder();

        sb.append("l");

        for (IBencodable entry : this.list)
            sb.append(entry.bencodedString());

        sb.append("e");

        return sb.toString();
    }

    public byte[] bencode() {
        // Get the total size of the keys and values.
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        bytes.add((byte) 'l');
        for (IBencodable entry : this.list)
            for (byte b : entry.bencode())
                bytes.add(b);
        bytes.add((byte) 'e');

        byte[] bencoded = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++)
            bencoded[i] = bytes.get(i);

        return bencoded;
    }
    ////////////////////////////////////////////////////////////////////////////
    //// OVERRIDDEN METHODS ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Object entry : this.list) {
            sb.append(entry.toString());
        }
        sb.append(") ");

        return sb.toString();
    }

}
