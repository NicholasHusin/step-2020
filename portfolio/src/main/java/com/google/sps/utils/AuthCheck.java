package com.google.sps.utils;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;

/**
 * Authentication class intended to be used as a utility class.
 * Class is used as a utility class rather than a parent to be inherited to prevent functionality bloating.
 * Provides common functions related to authentication such login, logout, get email, etc.
 *
 * Note that any class that needs to do authentication should use this utility class.
 * Do not make assumptions on client's authentication status based on what informations was sent by the client.
 **/
public class AuthCheck {
  private static final String GOOGLE_EMAIL_DOMAIN = "@google.com";

  public static boolean isLoggedIn() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.isUserLoggedIn();
  }

  public static String generateLoginUrl(String redirectUrl) {
    UserService userService = UserServiceFactory.getUserService();
    return userService.createLoginURL(redirectUrl); 
  }

  public static String generateLogoutUrl(String redirectUrl) {
    UserService userService = UserServiceFactory.getUserService();
    return userService.createLogoutURL(redirectUrl); 
  }

  public static String getEmail() {
    if (!isLoggedIn()) {
      return null;
    }

    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser().getEmail();
  }

  public static boolean isGoogleEmail() {
    if (!isLoggedIn()) {
      return false;
    }

    return getEmail().contains(GOOGLE_EMAIL_DOMAIN);
  }

  public static String getLdap() {
    if (!isGoogleEmail()) {
      return null;
    }

    return getEmail().replace(GOOGLE_EMAIL_DOMAIN, "");
  }
}
