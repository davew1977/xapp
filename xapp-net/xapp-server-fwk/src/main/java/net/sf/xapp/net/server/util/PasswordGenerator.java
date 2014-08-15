package net.sf.xapp.net.server.util;

import net.sf.xapp.net.common.util.random.EntropySource;
import net.sf.xapp.net.common.util.random.RandomEntropySource;

public class PasswordGenerator
{
    private static final String chars = "023456789abcdefghijkmnopqrstuvwxyz";
    private static EntropySource entropySource = new RandomEntropySource();

    public static String generatePassword()
    {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<4;i++)
        {
            sb.append(chars.charAt(entropySource.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static void main(String[] args)
    {
        for(int i=0;i<100;i++)
        {
            System.out.println(generatePassword());
        }
    }
}
