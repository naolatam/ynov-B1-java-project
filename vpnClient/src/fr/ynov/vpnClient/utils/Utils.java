package fr.ynov.vpnClient.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * Utility class providing helper methods for network-related validation.
 */
public class Utils {

    /**
     * Validates whether the given input is a fully qualified domain name (FQDN) or an IP address.
     *
     * @param input The string to be validated.
     * @return {@code true} if the input is a valid FQDN or IP address, {@code false} otherwise.
     */
    public static boolean isValidFQDNorIP(String input) {
        try {
            // Attempt to resolve the input as an IP address
            InetAddress.getByName(input);
            return true; // Valid IP Address
        } catch (UnknownHostException e) {
            // If resolution fails, check if it matches an FQDN pattern
            Pattern fqdnPattern = Pattern.compile(
                    " \"^(?!-)([a-zA-Z0-9-]{1,63}(?<!-)(\\\\.[a-zA-Z0-9-]{1,63}(?<!-))*)\\\\.?$\\n\";"
            );

            return fqdnPattern.matcher(input).matches();
        }
    }

}
