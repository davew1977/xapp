/*
 *
 * Date: 2010-okt-01
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;


import java.util.List;

public abstract class AbstractObject implements TransportObject
{
    public void expandToString(StringBuilder sb, String indent)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String expandToString()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeString(StringBuilder sb)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void populateFrom(List<Object> data)
    {
        throw new UnsupportedOperationException();

    }

    /**
     * default to binary serialization to string - used client side, overridden server side
     * @param str the object as string
     * @return
     */
    @Override
    public Object deserialize(String str)
    {
        String[] bits = str.trim().split(",");
        byte[] b = new byte[bits.length];
        for (int i = 0; i < b.length; i++)
        {
            b[i] = Byte.parseByte(bits[i]);
        }
        TransportHelper.readObj(this, b);
        return this;
    }

    @Override
    public String serialize()
    {
        byte[] b = TransportHelper.toByteArray(this);
        StringBuilder sb = new StringBuilder();
        for (byte b1 : b)
        {
            sb.append(b1).append(',');
        }
        return sb.toString();
    }

    public String getKey() {
        return null;
    }

    public void init() {

    }

    @Override
    public abstract ObjectType type();

    /*    public static void main(String[] args)
    {
        Round r = new Round();
        r.deserialize("0,7,48,95,99,95,51,95,53,0,5,48,95,99,95,51,0,0,0,0,0,0,0,0,0,9,97,108,102,114,105,115,116,111,110,0,0,0,0,0,0,0,50,0,0,0,10,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,-1,-1,-1,0,0,0,0,0,0,0,0,1,-1,-1,-1,-1,1,-1,-1,-1,-1,1,-1,-1,-1,-1,0,0,0,0,0,0,0,25,0,0,0,0,0,0,0,50,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,50,0,0,0,0,0,0,0,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,0,0,1,53,-95,113,-75,63,0,0,1,39,0,1,0,0,0,2,0,1,0,6,48,95,112,95,56,54,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,2,0,0,0,0,0,0,3,0,0,0,0,0,1,53,-95,113,-75,69,0,0,1,40,0,1,0,0,0,2,0,1,0,9,97,108,102,114,105,115,116,111,110,0,0,1,53,-95,113,-70,55,0,0,1,41,0,1,0,0,0,2,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,3,-24,0,0,1,53,-95,113,-70,57,0,0,1,52,0,1,0,0,0,2,1,0,0,0,3,1,0,0,0,1,0,0,1,53,-95,113,-14,-58,0,0,1,39,0,1,0,0,0,3,0,1,0,7,48,95,112,95,49,48,49,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,3,0,0,0,0,0,0,3,0,0,0,0,0,1,53,-95,113,-14,-57,0,0,1,40,0,1,0,0,0,3,0,1,0,5,86,105,120,101,110,0,0,1,53,-95,113,-6,-20,0,0,1,41,0,1,0,0,0,3,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,3,-24,0,0,1,53,-95,113,-6,-20,0,0,1,52,0,1,0,0,0,3,1,0,0,0,3,1,0,0,0,1,0,0,1,53,-95,114,14,69,0,0,1,77,0,1,0,6,48,95,112,95,56,54,0,9,97,108,102,114,105,115,116,111,110,0,5,104,101,108,108,111,0,0,1,53,-95,114,14,119,0,0,1,52,0,1,0,0,0,2,1,0,0,0,1,1,0,0,0,0,0,0,1,53,-95,114,14,120,0,0,1,52,0,1,0,0,0,3,1,0,0,0,1,1,0,0,0,0,0,0,1,53,-95,114,14,120,0,0,1,51,0,1,0,0,0,2,0,1,0,0,0,40,0,0,1,53,-95,114,14,120,0,0,1,51,0,1,0,0,0,3,0,1,0,0,0,19,0,0,1,53,-95,114,14,120,0,0,1,60,0,1,-1,-1,-1,-1,1,0,0,0,2,0,0,1,53,-95,114,14,121,0,0,1,61,0,1,0,0,0,0,1,0,0,0,6,0,0,1,53,-95,114,30,27,0,0,1,50,0,1,0,0,0,2,0,1,0,0,0,2,0,0,1,53,-95,114,30,27,0,0,1,50,0,1,0,0,0,3,0,1,0,0,0,2,0,0,1,53,-95,114,30,27,0,0,1,72,0,1,-1,-1,-1,-1,1,0,0,0,3,0,0,1,53,-95,114,30,27,0,0,1,47,0,1,0,0,0,2,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,25,0,0,1,53,-95,114,30,27,0,0,1,41,0,1,0,0,0,2,1,0,0,0,0,0,0,3,-24,1,0,0,0,0,0,0,3,-49,0,0,1,53,-95,114,30,28,0,0,1,50,0,1,0,0,0,2,1,0,0,0,2,1,0,0,0,4,0,0,1,53,-95,114,30,28,0,0,1,50,0,1,0,0,0,3,1,0,0,0,2,1,0,0,0,3,0,0,1,53,-95,114,30,28,0,0,1,47,0,1,0,0,0,3,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,50,0,0,1,53,-95,114,30,28,0,0,1,41,0,1,0,0,0,3,1,0,0,0,0,0,0,3,-24,1,0,0,0,0,0,0,3,-74,0,0,1,53,-95,114,30,28,0,0,1,50,0,1,0,0,0,3,1,0,0,0,3,1,0,0,0,4,0,0,1,53,-95,114,30,29,0,0,1,50,0,1,0,0,0,2,1,0,0,0,4,1,0,0,0,3,0,0,1,53,-95,114,30,29,0,0,1,51,0,1,0,0,0,2,1,0,0,0,40,0,0,0,1,53,-95,114,30,29,0,0,1,51,0,1,0,0,0,3,1,0,0,0,19,0,0,0,1,53,-95,114,30,29,0,0,1,61,0,1,0,0,0,6,1,0,0,0,5,0,0,1,53,-95,114,32,19,0,0,1,43,0,1,0,0,0,2,1,0,0,0,2,0,0,0,34,0,0,0,51,0,0,1,53,-95,114,32,20,0,0,1,43,0,1,0,0,0,3,1,0,0,0,2,0,0,0,6,0,0,0,25,0,0,1,53,-95,114,32,20,0,0,1,61,0,1,0,0,0,5,1,0,0,0,3,0,0,1,53,-95,114,35,54,0,0,1,75,0,1,0,0,0,0,1,0,0,0,1,0,0,1,53,-95,114,35,54,0,0,1,70,0,1,-1,-1,-1,-1,1,0,0,0,2,0,0,1,53,-95,114,35,54,0,0,1,61,0,1,0,0,0,3,1,0,0,0,2,0,0,1,53,-95,114,45,-41,0,0,1,77,0,1,0,7,48,95,112,95,49,48,49,0,5,86,105,120,101,110,0,23,112,111,111,111,111,111,111,111,111,111,111,111,111,111,111,111,111,111,111,111,111,111,111,0,0,1,53,-95,114,84,-7,0,0,1,81,0,1,0,0,0,0,0,0,0,50,1,0,0,0,0,0,0,3,-74,0,0,1,53,-95,114,84,-6,0,0,1,47,0,1,0,0,0,2,1,0,0,0,0,0,0,0,25,1,0,0,0,0,0,0,3,-24,0,0,1,53,-95,114,84,-6,0,0,1,41,0,1,0,0,0,2,1,0,0,0,0,0,0,3,-49,1,0,0,0,0,0,0,0,0,0,0,1,53,-95,114,84,-6,0,0,1,50,0,1,0,0,0,2,1,0,0,0,3,1,0,0,0,0,0,0,1,53,-95,114,84,-6,0,0,1,50,0,1,0,0,0,3,1,0,0,0,4,1,0,0,0,3,0,0,1,53,-95,114,84,-5,0,0,1,49,0,1,0,0,0,2,0,1,0,0,0,0,0,0,1,53,-95,114,84,-5,0,0,1,61,0,1,0,0,0,2,1,0,0,0,13,0,0,1,53,-95,114,86,-15,0,0,1,70,0,1,0,0,0,2,1,0,0,0,3,0,0,1,53,-95,114,86,-15,0,0,1,61,0,1,0,0,0,13,1,0,0,0,2,0,0,1,53,-95,114,105,76,0,0,1,47,0,1,0,0,0,3,1,0,0,0,0,0,0,0,50,1,0,0,0,0,0,0,3,-24,0,0,1,53,-95,114,105,76,0,0,1,41,0,1,0,0,0,3,1,0,0,0,0,0,0,3,-74,1,0,0,0,0,0,0,0,0,0,0,1,53,-95,114,105,76,0,0,1,50,0,1,0,0,0,3,1,0,0,0,3,1,0,0,0,0,0,0,1,53,-95,114,105,77,0,0,1,49,0,1,0,0,0,3,0,1,0,0,0,2,0,0,1,53,-95,114,105,77,0,0,1,61,0,1,0,0,0,2,1,0,0,0,13,0,0,1,53,-95,114,107,66,0,0,1,62,0,1,0,0,0,0,0,0,1,53,-95,114,107,67,0,0,1,66,0,1,0,0,0,0,1,0,0,0,2,0,0,0,0,0,0,3,-24,0,0,1,53,-95,114,107,67,0,0,1,66,0,1,0,0,0,0,1,0,0,0,3,0,0,0,0,0,0,3,-24,0,0,1,53,-95,114,107,67,0,0,1,62,0,1,0,0,0,0,0,0,1,53,-95,114,107,67,0,0,1,47,0,1,0,0,0,2,1,0,0,0,0,0,0,3,-24,1,0,0,0,0,0,0,0,0,0,0,1,53,-95,114,107,67,0,0,1,47,0,1,0,0,0,3,1,0,0,0,0,0,0,3,-24,1,0,0,0,0,0,0,0,0,0,0,1,53,-95,114,107,67,0,0,1,70,0,1,0,0,0,3,1,-1,-1,-1,-1,0,0,1,53,-95,114,107,68,0,0,1,81,0,1,0,0,0,0,0,0,3,-74,1,0,0,0,0,0,0,0,50,0,0,1,53,-95,114,107,68,0,0,1,61,0,1,0,0,0,13,1,0,0,0,14,0,0,1,53,-95,114,115,22,0,0,1,56,0,1,0,0,0,37,0,0,1,53,-95,114,115,22,0,0,1,56,0,1,0,0,0,20,0,0,1,53,-95,114,115,22,0,0,1,56,0,1,0,0,0,42,0,0,1,53,-95,114,115,23,0,0,1,56,0,1,0,0,0,15,0,0,1,53,-95,114,115,23,0,0,1,56,0,1,0,0,0,1,0,0,1,53,-95,114,115,24,0,0,1,61,0,1,0,0,0,14,1,0,0,0,11,0,0,1,53,-95,114,-104,54,0,0,1,76,0,0,1,0,0,0,1,0,0,0,1,0,0,0,2,0,0,0,0,0,0,7,-48,0,0,0,2,0,0,0,3,1,0,0,0,6,0,0,0,0,0,0,0,5,0,0,0,1,0,0,0,42,0,0,0,37,0,0,0,25,0,0,0,20,0,0,0,2,0,0,0,6,0,0,0,25,0,0,0,2,1,0,0,0,6,0,0,0,0,0,0,0,5,0,0,0,1,0,0,0,51,0,0,0,42,0,0,0,37,0,0,0,34,0,0,0,2,0,0,0,34,0,0,0,51,0,0,1,53,-95,114,-104,54,0,0,1,61,0,1,0,0,0,11,1,0,0,0,12,0,0,1,53,-95,114,-104,55,0,0,1,49,0,1,0,0,0,2,1,0,0,0,0,0,0,0,1,53,-95,114,-104,55,0,0,1,49,0,1,0,0,0,3,1,0,0,0,2,0,0,0,1,53,-95,114,-73,121,0,0,1,41,0,1,0,0,0,2,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,7,-48,0,0,1,53,-95,114,-73,121,0,0,1,76,0,1,0,0,0,1,0,0,0,1,0,0,0,2,0,0,0,0,0,0,7,-48,0,0,0,2,0,0,0,3,1,0,0,0,6,0,0,0,0,0,0,0,5,0,0,0,1,0,0,0,42,0,0,0,37,0,0,0,25,0,0,0,20,0,0,0,2,0,0,0,6,0,0,0,25,0,0,0,2,1,0,0,0,6,0,0,0,0,0,0,0,5,0,0,0,1,0,0,0,51,0,0,0,42,0,0,0,37,0,0,0,34,0,0,0,2,0,0,0,34,0,0,0,51,0,0,0,1,53,-95,114,-73,121,0,0,1,59,0,0,0,1,53,-95,114,-73,121,0,0,1,65,0,0,0,1,53,-95,114,-73,121,0,0,1,50,0,1,0,0,0,2,1,0,0,0,0,0,0,0,1,53,-95,114,-73,122,0,0,1,45,0,1,0,0,0,2,0,0,1,53,-95,114,-73,122,0,0,1,50,0,1,0,0,0,3,1,0,0,0,0,0,0,0,1,53,-95,114,-73,122,0,0,1,45,0,1,0,0,0,3,0,0,1,53,-95,114,-73,122,0,0,1,82,0,1,0,0,0,0,0,0,0,5,1,0,0,0,0,0,0,0,6,0,0,1,53,-95,114,-73,123,0,0,1,52,0,1,0,0,0,3,1,0,0,0,0,1,0,0,0,4,0,0,1,53,-95,114,-73,124,0,0,1,61,0,1,0,0,0,12,1,0,0,0,0,\n" +
                "");
        System.out.println(r.expandToString());
    }*/
}