package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

/**
 * Authentication class intended to be used mostly as a utility class.
 * Class is used as a utility class rather than a parent to be inherited to prevent functionality bloating.
 * Provides common functions realted to authentication such login, logout, get email, etc.
 * Also provides a convenient GET request listener for client to check the status of user and get login / logout URL.
 *
 * Note that the GET request listener is purely there for the client to be able to display client page appropriately.
 * Backend functionalities should use the utility functions rather than the GET listener.
 * Do not rely on the login status sent by the client. Avoid client side validation and re-validate on the server.
 **/
@WebServlet(urlPatterns={"/auth-status", "/auth-login", "/auth-logout"})
public class AuthCheck extends HttpServlet {
  private static final String AUTH_STATUS_URL              = "/auth-status";
  private static final String AUTH_LOGIN_URL               = "/auth-login";
  private static final String AUTH_LOGOUT_URL              = "/auth-logout";
  private static final String REDIRECT_URL_PARAMETER       = "redirect";
  private static final String RESPONSE_CONTENT_TYPE_JSON   = "application/json;";
  private static final String GOOGLE_EMAIL_DOMAIN          = "@google.com";

  /**
   * Get request handler for client usage providing different usages based on where the request is sent.
   * Provides login and logout link in addition to checking whether a client is logged in or not.
   **/
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String requestUrl   = request.getRequestURI();
    String redirectUrl  = request.getParameter(REDIRECT_URL_PARAMETER);

    response.setContentType(RESPONSE_CONTENT_TYPE_JSON);
    String responseString = null;

    if (requestUrl.equals(AUTH_STATUS_URL)) {
      responseString = getEmail();
    }

    if (requestUrl.equals(AUTH_LOGIN_URL)) {
      responseString = generateLoginUrl(redirectUrl);
    } 

    if (requestUrl.equals(AUTH_LOGOUT_URL)) {
      responseString = generateLogoutUrl(redirectUrl);
    }

    response.getWriter().print(stringToJson(responseString));
  }

  static public boolean isLoggedIn() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.isUserLoggedIn();
  }

  static public String generateLoginUrl(String redirectUrl) {
    UserService userService = UserServiceFactory.getUserService();
    return userService.createLoginURL(redirectUrl); 
  }

  static public String generateLogoutUrl(String redirectUrl) {
    UserService userService = UserServiceFactory.getUserService();
    return userService.createLogoutURL(redirectUrl); 
  }

  static public String getEmail() {
    if (!isLoggedIn()) {
      return null;
    }

    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser().getEmail();
  }

  static public boolean isGoogleEmail() {
    if (!isLoggedIn()) {
      return false;
    }

    return getEmail().contains(GOOGLE_EMAIL_DOMAIN);
  }

  static public String getLdap() {
    if (!isGoogleEmail()) {
      return null;
    }

    return getEmail().replace(GOOGLE_EMAIL_DOMAIN, "");
  }

  private String stringToJson(String str) {
    Gson gson   = new Gson();
    return gson.toJson(str);
  }
}
