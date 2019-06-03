package com.truetech.xltrackingbeacon.Utils;

/**
 * Created by Ajwar on 09.06.2017.
 */
public class Constant {

    public static final String NAME_DB="xl_tracker";
    public static final int VERSION_DB=1;
    public static final String NAME_TABLE_LOC="track_data";
    public static final String COL_ID="_id";
    public static final String COL_DATA ="data";
    public static final String COL_DATE_INSERT ="date_insert";
    public static final String IMEI="imei";
    public static final String LIMIT_TRY_CONNECT="try_connect";
    public static final byte HEX_ONE=0x01;
    public static final byte HEX_NULL=0x00;
    public static final byte CODEC_ID=0x08;
    public static final byte PRIORITY=0x00;
    public static final byte MINUS_ONE=-1;
    public static final byte[] PREAMBLE=new byte[]{0,0,0,0};
    public static final int GLOBAL_MASK= 3;// in binary 0000 0011
    public static final int GLOBAL_MASK_WITHOUT_IO= 1;// in binary 0000 0001(without IO)
    public static final int THOUSAND=1000;
    public static final int HOUR=3600000;
    public static final int DAY=86400000;
    public static final int PERIOD_RESTART_TASK=5;
    public static final int MAX_LENGTH_RECORDS =10;
    public static final int SOCKET_TIMEOUT =15000;
    public static final int LIMIT_ROWS_IN_BD =15000;
    public static final int LIMIT_NUMBER_TRY_CONNECT =5;
    public static final char CHAR_TRUE='1';
    public static final char CHAR_FALSE='0';
    public static final String DEF_VALUE_STRING =null;
    public static final byte DEF_VALUE_NULL =0;
    public static final long DIFF_UNIX_TIME=1167609600;
    public static final boolean DEF_VALUE_BOOL = true;
    public static final String TAG="XLTracker";
    //public static final String HOST="192.168.1.40";
    //public static final String HOST="77.244.216.154";
    private Constant() {
    }
}
