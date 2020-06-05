package com.google.sps.utils;

import com.google.gson.Gson;

/**
 * Utility class that implements functionalities related to parsing.
 * Parsing related functionalities should all be implemented and called from this class.
 **/
public class Parse {
  /**
    * Method to parsed any Java Object into its JSON equivalent.
    * Can be used with any Object as the GSON library utilizes reflection.
    **/
  public static String toJson(Object object) {
    Gson gson   = new Gson();
    String json = gson.toJson(object);
    return json;
  }
}
