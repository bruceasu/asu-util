
package me.asu.util.seq;

import java.util.concurrent.atomic.AtomicLong;
import me.asu.text.Hex;
import me.asu.util.Bytes;

public class Sequence {

    final AtomicLong seq;

    public Sequence() {
        // 10 位
        this((System.currentTimeMillis() / 1000) * 1000000000);
    }

    public Sequence(final long initial) {
        seq = new AtomicLong(initial);
    }

    public long next() {
        return seq.incrementAndGet();
    }

    public String nextToString() {
        return Hex.encodeHexString(Bytes.toBytes(seq.incrementAndGet()));
    }

}
