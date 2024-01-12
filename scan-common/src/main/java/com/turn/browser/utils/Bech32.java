package com.turn.browser.utils;


import com.bubble.utils.Numeric;
import com.bubble.utils.Strings;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Locale;

public class Bech32 {

    private static final int ADDRESS_SIZE = 160;

    private static final int ADDRESS_LENGTH_IN_HEX = ADDRESS_SIZE >> 2;

    /**
     * The Bech32 character set for encoding.
     */
    private static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

    /**
     * The Bech32 character set for decoding.
     */
    private static final byte[] CHARSET_REV = {-1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               15,
                                               -1,
                                               10,
                                               17,
                                               21,
                                               20,
                                               26,
                                               30,
                                               7,
                                               5,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               29,
                                               -1,
                                               24,
                                               13,
                                               25,
                                               9,
                                               8,
                                               23,
                                               -1,
                                               18,
                                               22,
                                               31,
                                               27,
                                               19,
                                               -1,
                                               1,
                                               0,
                                               3,
                                               16,
                                               11,
                                               28,
                                               12,
                                               14,
                                               6,
                                               4,
                                               2,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               29,
                                               -1,
                                               24,
                                               13,
                                               25,
                                               9,
                                               8,
                                               23,
                                               -1,
                                               18,
                                               22,
                                               31,
                                               27,
                                               19,
                                               -1,
                                               1,
                                               0,
                                               3,
                                               16,
                                               11,
                                               28,
                                               12,
                                               14,
                                               6,
                                               4,
                                               2,
                                               -1,
                                               -1,
                                               -1,
                                               -1,
                                               -1};

    private static class Bech32Data {

        public final String hrp;

        public final byte[] data;

        private Bech32Data(final String hrp, final byte[] data) {
            this.hrp = hrp;
            this.data = data;
        }

    }

    /**
     * Find the polynomial with value coefficients mod the generator as 30-bit.
     */
    private static int polymod(final byte[] values) {
        int c = 1;
        for (byte v_i : values) {
            int c0 = (c >>> 25) & 0xff;
            c = ((c & 0x1ffffff) << 5) ^ (v_i & 0xff);
            if ((c0 & 1) != 0) {
                c ^= 0x3b6a57b2;
            }
            if ((c0 & 2) != 0) {
                c ^= 0x26508e6d;
            }
            if ((c0 & 4) != 0) {
                c ^= 0x1ea119fa;
            }
            if ((c0 & 8) != 0) {
                c ^= 0x3d4233dd;
            }
            if ((c0 & 16) != 0) {
                c ^= 0x2a1462b3;
            }
        }
        return c;
    }

    /**
     * Expand a HRP for use in checksum computation.
     */
    private static byte[] expandHrp(final String hrp) {
        int hrpLength = hrp.length();
        byte ret[] = new byte[hrpLength * 2 + 1];
        for (int i = 0; i < hrpLength; ++i) {
            int c = hrp.charAt(i) & 0x7f; // Limit to standard 7-bit ASCII
            ret[i] = (byte) ((c >>> 5) & 0x07);
            ret[i + hrpLength + 1] = (byte) (c & 0x1f);
        }
        ret[hrpLength] = 0;
        return ret;
    }

    /**
     * Verify a checksum.
     */
    private static boolean verifyChecksum(final String hrp, final byte[] values) {
        byte[] hrpExpanded = expandHrp(hrp);
        byte[] combined = new byte[hrpExpanded.length + values.length];
        System.arraycopy(hrpExpanded, 0, combined, 0, hrpExpanded.length);
        System.arraycopy(values, 0, combined, hrpExpanded.length, values.length);
        return polymod(combined) == 1;
    }

    /**
     * Create a checksum.
     */
    private static byte[] createChecksum(final String hrp, final byte[] values) {
        byte[] hrpExpanded = expandHrp(hrp);
        byte[] enc = new byte[hrpExpanded.length + values.length + 6];
        System.arraycopy(hrpExpanded, 0, enc, 0, hrpExpanded.length);
        System.arraycopy(values, 0, enc, hrpExpanded.length, values.length);
        int mod = polymod(enc) ^ 1;
        byte[] ret = new byte[6];
        for (int i = 0; i < 6; ++i) {
            ret[i] = (byte) ((mod >>> (5 * (5 - i))) & 31);
        }
        return ret;
    }


    /**
     * encode a hex address to a bech32 address with a specified hrp.
     *
     * @param hrp        specified hrp
     * @param hexAddress hex address
     * @return bech32 address
     */
    public static String addressEncode(String hrp, String hexAddress) {
        return encode(hrp, convertBits(Numeric.hexStringToByteArray(hexAddress), 8, 5, true));
    }

    /**
     * encode the bech32 values with a specified hrp.
     *
     * @param hrp
     * @param bech32Values the values is returned by convertBits() func
     * @return bech32 address
     */
    private static String encode(String hrp, final byte[] bech32Values) {
        checkArgument(hrp.length() >= 1, "Human-readable part is too short");
        checkArgument(hrp.length() <= 83, "Human-readable part is too long");
        hrp = hrp.toLowerCase(Locale.ROOT);
        byte[] checksum = createChecksum(hrp, bech32Values);
        byte[] combined = new byte[bech32Values.length + checksum.length];
        System.arraycopy(bech32Values, 0, combined, 0, bech32Values.length);
        System.arraycopy(checksum, 0, combined, bech32Values.length, checksum.length);
        StringBuilder sb = new StringBuilder(hrp.length() + 1 + combined.length);
        sb.append(hrp);
        sb.append('1');
        for (byte b : combined) {
            sb.append(CHARSET.charAt(b));
        }
        return sb.toString();
    }

    /**
     * @param str bech32 string
     * @return
     * @throws RuntimeException
     */
    private static Bech32Data decode(final String str) throws RuntimeException {
        boolean lower = false, upper = false;
        final int pos = str.lastIndexOf('1');
        final int dataPartLength = str.length() - 1 - pos;
        byte[] values = new byte[dataPartLength];
        for (int i = 0; i < dataPartLength; ++i) {
            char c = str.charAt(i + pos + 1);
            values[i] = CHARSET_REV[c];
        }
        String hrp = str.substring(0, pos).toLowerCase(Locale.ROOT);
        if (!verifyChecksum(hrp, values)) {
            throw new RuntimeException("decode Bech32 address error");
        }
        return new Bech32Data(hrp, Arrays.copyOfRange(values, 0, values.length - 6));
    }

    /**
     * @param bech32Address bech32 address
     * @return byte[], plain hex
     * @throws RuntimeException
     */
    public static byte[] addressDecode(final String bech32Address) throws RuntimeException {
        if (Strings.isBlank(bech32Address)) {
            return new byte[]{};
        }
        Bech32.Bech32Data bech32Data = decode(bech32Address);
        return convertBits(bech32Data.data, 5, 8, false);
    }

    /**
     * @param bech32Address bech32 address
     * @return plain hex address
     * @throws RuntimeException
     */
    public static String addressDecodeHex(final String bech32Address) throws RuntimeException {
        byte[] bytes = addressDecode(bech32Address);
        return Numeric.toHexStringWithPrefixZeroPadded(Numeric.toBigInt(bytes), ADDRESS_LENGTH_IN_HEX);
    }


    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression   a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *                     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentException if {@code expression} is false
     */
    private static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    /**
     * Helper for re-arranging bits into groups.
     */
    private static byte[] convertBits(final byte[] in, final int fromBits, final int toBits, final boolean pad) {
        int acc = 0;
        int bits = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream(64);
        final int maxv = (1 << toBits) - 1;
        final int max_acc = (1 << (fromBits + toBits - 1)) - 1;
        for (int i = 0; i < in.length; i++) {
            int value = in[i] & 0xff;
            if ((value >>> fromBits) != 0) {
                throw new RuntimeException(String.format("Input value '%X' exceeds '%d' bit size", value, fromBits));
            }
            acc = ((acc << fromBits) | value) & max_acc;
            bits += fromBits;
            while (bits >= toBits) {
                bits -= toBits;
                out.write((acc >>> bits) & maxv);
            }
        }
        if (pad) {
            if (bits > 0) {
                out.write((acc << (toBits - bits)) & maxv);
            }
        } else if (bits >= fromBits || ((acc << (toBits - bits)) & maxv) != 0) {
            throw new RuntimeException("Could not convert bits, invalid padding");
        }
        return out.toByteArray();
    }


    public static boolean verifyHrp(final String hrp) {
        if (hrp == null || hrp.length() == 0) {
            return false;
        }

        byte[] bytes = hrp.getBytes();
        //if(bytes.length<1 || bytes.length>83){
        if (bytes.length != 3) {
            return false;
        }

        for (byte b : bytes) {
            if (b < 33 || b > 126) {
                return false;
            }
        }
        return true;
    }

    /**
     * change the hrp address to another hrp address
     *
     * @param originBech32Address origin bech32 address
     * @param destHrp             dest hrp
     * @return dest hrp bech32 address;
     */
    public static String changeHrp(final String originBech32Address, final String destHrp) {
        Bech32Data bech32Data = decode(originBech32Address);
        if (bech32Data.hrp.equals(destHrp)) {
            return originBech32Address;
        } else {
            return addressEncode(destHrp, addressDecodeHex(originBech32Address));
        }
    }

    /**
     * check if the bech32 address is according to current network;
     *
     * @param bech32Address bech32 address
     * @return true: the bech32Address matches to current network;
     */
    public static boolean checkBech32Addr(final String bech32Address) {
        Bech32Data bech32Data = decode(bech32Address);
        return bech32Data.hrp.equals("AA");
    }


    /**
     * compatible with ETH address, Alaya previous bech 32 address.
     * convert these addresses to be unified for the new version.
     *
     * @param originAddress
     * @return
     */
    public static String convertToUnifiedAddress(String originAddress) {
        if (Numeric.containsHexPrefix(originAddress)) { //compatible with ETH address
            return Bech32.addressEncode("AA", originAddress);
        } else {
            // compatible with Bech32
            return Bech32.changeHrp(originAddress, "AA");
        }
    }

}

