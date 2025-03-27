package fr.ynov.vpnClient.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class Utils {
    // This method is used to check if a string is a valid fqdn/ip
    public static boolean isValidFQDNorIP(String input) {
        try {
            // Try to connect to the ip
            InetAddress.getByName(input);
            return true; // Valid IP Address
        } catch (UnknownHostException e) {
            // If it's fail, try a regex pattern to check if it is a valid fqdn
            Pattern fqdnPattern =Pattern.compile(
                    " \"^(?!-)([a-zA-Z0-9-]{1,63}(?<!-)(\\\\.[a-zA-Z0-9-]{1,63}(?<!-))*)\\\\.?$\\n\";"
            );

            return fqdnPattern.matcher(input).matches();
        }
    }

}
