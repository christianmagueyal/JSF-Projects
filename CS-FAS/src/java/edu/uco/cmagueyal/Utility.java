package edu.uco.cmagueyal;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class Utility {
  private static final Random RANDOM = new SecureRandom();
  
  private static final int PASSWORD_LENGTH = 12;
  
  public static String generateRandomCode()
  {
      // Some letters and numbers are left out intentionally to avoid
      // confusion. For example, o O and 0, 1 l and L.
      String letters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789@";

      String code = "";
      for (int i=0; i<PASSWORD_LENGTH; i++)
      {
          int index = (int)(RANDOM.nextDouble()*letters.length());
          code += letters.substring(index, index+1);
      }
      return code;
  }
  public static String encrypt(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data.getBytes("UTF-8")); // Change this to "UTF-16" if needed
            byte[] digest = md.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return null;
        }
    }
  public static void errorMessage(String message, String label) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null);
        FacesContext.getCurrentInstance().addMessage(label, facesMsg);
    }
    public static void infoMessage(String message, String label) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, message, null);
        FacesContext.getCurrentInstance().addMessage(label, facesMsg);
    }
}
