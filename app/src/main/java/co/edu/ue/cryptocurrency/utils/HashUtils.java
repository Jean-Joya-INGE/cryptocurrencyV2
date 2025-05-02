package co.edu.ue.cryptocurrency.utils;


import java.security.MessageDigest;

// Clase para hashear contraseñas
public class HashUtils {

    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString(); // Devuelve la contraseña en hash
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
