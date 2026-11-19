package com.expensetracker.util;

public final class CsvUtil {
    private CsvUtil(){}
    public static String row(Object... values){StringBuilder line=new StringBuilder();for(int i=0;i<values.length;i++){if(i>0)line.append(',');line.append(quote(values[i]==null?"":String.valueOf(values[i])));}return line.append("\r\n").toString();}
    private static String quote(String value){String checked=value;String trimmed=value.stripLeading();boolean numeric=trimmed.matches("-?\\d+(\\.\\d+)?");if(!trimmed.isEmpty()&&"=+-@\t\r".indexOf(trimmed.charAt(0))>=0&&!numeric)checked="'"+value;return "\""+checked.replace("\"","\"\"")+"\"";}
}
