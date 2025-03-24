package fr.ynov.vpnClient.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class Utils {
    public static boolean isValidFQDNorIP(String input) {
        try {
            InetAddress.getByName(input);
            return true; // Valid IP Address
        } catch (UnknownHostException e) {
            Pattern fqdnPattern =Pattern.compile(
                    " \"^(?!-)([a-zA-Z0-9-]{1,63}(?<!-)(\\\\.[a-zA-Z0-9-]{1,63}(?<!-))*)\\\\.?$\\n\";"
            );

            return fqdnPattern.matcher(input).matches();
        }
    }

}
