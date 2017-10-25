package io.dts.parser;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import io.dts.common.common.exception.DtsException;

import java.util.Date;

public class TxcObjectWapper {
  public static String jsonObjectDeserialize(int type, Object value) {
    if (value == null) {
      return "null";
    }

    switch (type) {
      case java.sql.Types.ARRAY:
      case java.sql.Types.CHAR:
      case java.sql.Types.LONGNVARCHAR:
      case java.sql.Types.LONGVARCHAR:
      case java.sql.Types.NCHAR:
      case java.sql.Types.VARCHAR:
      case java.sql.Types.NVARCHAR:
        return "'" + value + "'";
      case java.sql.Types.DATE:
      case java.sql.Types.TIME:
      case java.sql.Types.TIMESTAMP:
        Date d = new Date(Long.parseLong(value.toString()));
        return JSON.toJSONString(d, SerializerFeature.WriteDateUseDateFormat,
            SerializerFeature.UseSingleQuotes);
      default:
        return value.toString();
    }
  }


  public static void appendParamMarkerObject(Object value, StringBuilder appender) {
    if (value == null) {
      appender.append("null");
      return;
    }

    if (String.class.isAssignableFrom(value.getClass())) {
      String text = (String) value;
      appender.append("'" + text.replaceAll("'", "''") + "'");
    } else if (Date.class.isAssignableFrom(value.getClass())) {
      appender.append("'" + value + "'");
    } else if (Number.class.isAssignableFrom(value.getClass())) {
      appender.append(value);
    } else {
      throw new DtsException("" + value.getClass());
    }
  }

  public static void appendParamMarkerObject(String name, Object value, StringBuilder appender) {

    appender.append(name);
    appender.append(" = ");
    if (String.class.isAssignableFrom(value.getClass())) {
      appender.append("'" + value + "'");
    } else if (Date.class.isAssignableFrom(value.getClass())) {
      appender.append("'" + value + "'");
    } else if (Number.class.isAssignableFrom(value.getClass())) {
      appender.append(value);
    } else {
      appender.append("NULL");
    }
  }
}
