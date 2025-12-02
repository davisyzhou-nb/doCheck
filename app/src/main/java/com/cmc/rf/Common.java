package com.cmc.rf;

public class Common
{
    public static void memcpy(
            byte[] desBuf, byte[] srcBuf, int desOffset,int srcOffset, int count)
    {
        if(srcOffset + count > srcBuf.length)
        {
            return;
        }
        if(desOffset + count > desBuf.length)
        {
            return;
        }
        for(int i = 0; i < count; i++){
            desBuf[desOffset + i] = srcBuf[srcOffset + i];
        }
    }

    public static void memset(
            byte[] desBuf, int desOffset,byte value, int count)
    {
        for(int i = 0; i < count; i++){
            desBuf[desOffset + i] = value;
        }
    }

    public static boolean arrByteIndexof(
            byte[] desBuf, int desOffset,int descLength,
            byte[] srcBuf, int srcOffset, int size)
    {
        for(int i = desOffset; i < descLength-size; i++)
        {
            boolean b = true;
            for(int j=0;j<size;j++)
            {
                if(srcBuf[srcOffset+j] != desBuf[i+j])
                {
                    b = false;
                    break;
                }
            }
            if(b)return b;
        }
        return false;
    }

    private static synchronized String hex2Word(byte b)
    {
        return ("" + "0123456789ABCDEF".charAt(0x0f & b >> 4) + "0123456789ABCDEF".charAt(b & 0x0f));
    }

    public static String arrByte2String(byte[] buf,int offset,int size)
    {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < size; i++)
        {
            sb.append(hex2Word(buf[offset+i]));
            if(i < (buf.length - 1))
                sb.append(' ');
        }
        return sb.toString();
    }

    public static String arrByte2StringNoSpace(byte[] buf,int offset,int size)
    {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < size; i++)
        {
            sb.append(hex2Word(buf[offset+i]));
        }
        return sb.toString();
    }

    public static String arrByte2String(byte[] buf,int size)
    {
        return arrByte2String(buf, 0, size);
    }

    public static void hexStr2Bytes(String src,byte[] desc,int max)
    {
        int m=0,n=0;
        int l;
        src = src.replaceAll(" ", "");
        l = src.length()/2;
        //System.out.println(l);
        //byte[] ret = new byte[l];
        if(l>max)l=max;
        String str1;
        for (int i = 0; i < l; i++)
        {
            m=i*2+1;
            n=m+1;
            str1 = "0x" + src.substring(i*2, m) + src.substring(m,n);
            //System.out.println(str1);
            try
            {
                desc[i] = Integer.decode(str1).byteValue() ;// .parseInt(str1);
            }catch(Exception ex){}
        }
    }
    public static void intToBytes(int num,byte[] desc,int offset)
    {
        for (int i = 0; i < 4; i++)
        {
            desc[i+offset] = (byte)(num >>> (24 - i * 8));
        }
    }
    public static void intH2L(byte[] desc,int offset)
    {
        byte b = desc[offset+0];
        desc[offset+0] = desc[offset+3];
        desc[offset+3] = b;
        b = desc[offset+2];
        desc[offset+2] = desc[offset+1];
        desc[offset+1] = b;
    }
    public static void shortH2L(byte[] desc,int offset)
    {
        byte b = desc[offset+0];
        desc[offset+0] = desc[offset+1];
        desc[offset+1] = b;
    }
    public static void shortToBytes(short num,byte[] desc,int offset)
    {
        for (int i = 0; i < 2; i++)
        {
            desc[i+offset] = (byte) (num >>> (i * 8));
        }
    }
    public static int bytesToInt(byte[] data, int offset)
    {
        int num = 0;
        for (int i = offset; i < offset + 4; i++)
        {
            num <<= 8;
            num |= (data[i] & 0xff);
        }
        return num;
    }

    public static short bytesToShort(byte[] data, int offset)
    {
        short num = 0;
        for (int i = offset; i < offset + 2; i++)
        {
            num <<= 8;
            num |= (data[i] & 0xff);
        }
        return num;
    }

    public static byte bcc(byte[] data,int offset,int size)
    {
        byte b = 0;
        for(int i = 0; i < size; i++)
        {
            b ^= data[offset + i];
        }
        return b;
    }
}
