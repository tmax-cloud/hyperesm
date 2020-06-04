package k8s.example.client.metering.util;

import java.util.concurrent.atomic.AtomicInteger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UIDGenerator {
    /** standard logger object */
    private static final Logger logger = Logger.getLogger("com.tmax.sysmaster.common.util");

    /** lookup back local address string, that is 127.0.0.1 */
    private static final String LOOPBACK_ADDRESS = "7F000001";
    private final AtomicInteger sequence = new AtomicInteger();
    private char[] localAddress;
    private byte[] localByteAddress;

    private static final UIDGenerator uidGenerator = new UIDGenerator();

    private UIDGenerator() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("<init>");
        }
        try {
            localByteAddress = java.net.InetAddress.getLocalHost().getAddress();
            int address = getIntegerAddress(localByteAddress);
            localAddress = SimpleUtil.toHexString(address, 8, true);
            if (LOOPBACK_ADDRESS.equals(new String(localAddress))) {
                logger.severe("Cannot get local INET address. Please check the security permission of java.net.SocketPermission(host,\"resolve\")");
            }
        } catch (java.net.UnknownHostException e) {
            logger.log(Level.SEVERE, "cannot lookup local host address", e);
        }
    }

    public static UIDGenerator getInstance() {
        return uidGenerator;
    }

    /**
     * generate given number of digits unique id the number of digits should be
     * between 8 and 32
     * 
     * @param object
     *            some object from which to get some hint on uniqueness
     * @param digits
     *            number of digits to generate
     */
    public String generate(Object object, int digits) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "generate(object - " + object + ", digits - " + digits + ")");
        }
        if (digits > 24) {
            return generate32(object, digits);
        }
        int unit = digits / 3;
        int offset = 0;
        char[] buffer = new char[digits];
        offset = digits - 2 * unit;
        System.arraycopy(localAddress, 8 - offset, buffer, 0, offset);
        SimpleUtil.toHexString(System.identityHashCode(object), unit, buffer, offset, true);
        offset += unit;
        SimpleUtil.toHexString(getNextInt(), unit, buffer, offset, true);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("generated id - " + new String(buffer));
        }
        return new String(buffer);
    }

    public byte[][] generateTwo4Bytes(Object object) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("generateTwo4Bytes(object - " + object + ")");
        }
        byte[][] bb = new byte[2][4];
        System.arraycopy(localByteAddress, localByteAddress.length - 3, bb[0], 0, 3);
        bb[0][3] = (byte) System.identityHashCode(object);
        writeInt(bb[1], getNextInt());
        return bb;
    }

    private static void writeInt(byte[] target, int value) {
        target[0] = (byte) ((value & 0xFF000000) >> 24);
        target[1] = (byte) ((value & 0x00FF0000) >> 16);
        target[2] = (byte) ((value & 0x0000FF00) >> 8);
        target[3] = (byte) (value & 0x000000FF);
    }

    public String generate(Object object) {
        return generate32(object, 32);
    }

    public String generate(Object object, long timeTick) {
        return generate32(object, 32, timeTick);
    }

    public String generate32(Object object, int digits) {
        return generate32(object, digits, System.currentTimeMillis());
    }

    public String generate32(Object object, int digits, long timeTick) {
        int unit = digits / 4;
        int offset = 0;
        char[] buffer = new char[digits];
        offset = digits - 3 * unit;
        System.arraycopy(localAddress, 8 - offset, buffer, 0, offset);
        SimpleUtil.toHexString((int) (timeTick & 0xFFFFFFFF), unit, buffer, offset, true);
        offset += unit;
        SimpleUtil.toHexString(System.identityHashCode(object), unit, buffer, offset, true);
        offset += unit;
        SimpleUtil.toHexString(getNextInt(), unit, buffer, offset, true);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("generated id - " + new String(buffer));
        }
        return new String(buffer);
    }
    
    /**
     * get next int
     */
    int getNextInt() {
        return sequence.getAndIncrement();
    }

    /**
     * calculate integer from given byte array
     */
    private static int getIntegerAddress(byte[] bytes) {
        int value = 0;
        int radix = 1;
        for (int i = bytes.length - 1; i >= 0; i--) {
            value += (bytes[i] & 0xFF) * radix;
            radix *= 0x100;
        }
        return value;
    }
}
