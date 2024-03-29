package alex.beta.torrentparser.bencoding.types;

import alex.beta.torrentparser.bencoding.Utils;

import java.nio.charset.Charset;
import java.util.Arrays;

public class BByteString implements IBencodable {
    private final byte[] data;

    public BByteString(byte[] data) {
        this.data = data;
    }

    public BByteString(String name) {
        this.data = name.getBytes();
    }

    ////////////////////////////////////////////////////////////////////////////
    //// GETTERS AND SETTERS ///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    public byte[] getData() {
        return data;
    }

    ////////////////////////////////////////////////////////////////////////////
    //// BENCODING /////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    public String bencodedString() {
        return data.length + ":" + new String(data);
    }

    public byte[] bencode() {

        byte[] lengthStringAsBytes = Utils.stringToAsciiBytes(Long.toString(data.length));
        byte[] bencoded = new byte[lengthStringAsBytes.length + 1 + data.length];

        bencoded[lengthStringAsBytes.length] = ':';
        // Copy the length array in first.
        System.arraycopy(lengthStringAsBytes, 0, bencoded, 0, lengthStringAsBytes.length);
        // Copy in the actual inlineimage.
        for (int i = 0; i < data.length; i++)
            bencoded[i + lengthStringAsBytes.length + 1] = data[i];

        return bencoded;
    }

    ////////////////////////////////////////////////////////////////////////////
    //// OVERRIDDEN METHODS ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return new String(data, Charset.forName("UTF-8"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BByteString that = (BByteString) o;

        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return data != null ? Arrays.hashCode(data) : 0;
    }
}
