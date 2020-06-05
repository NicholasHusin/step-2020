package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.utils.AuthCheck;
import com.google.gson.Gson;

/**
 * Servlet class intended to be used by the client to be able to get their authentication status and login / logout link.
 * All of these functionalities can be called by the client by sending a request to the GET handler.
 * AuthCheck utility class is used to implement these functionalities.
 *
 * Note that the GET request listener is purely there for the client to be able to display client page appropriately.
 * Backend functionalities should use the utility class AuthCheck rather than the GET listener.
 * Do not rely on the login status sent by the client. Avoid client side validation and re-validate on the server.
 **/
@WebServlet(urlPatterns={"/auth-status", "/auth-login", "/auth-logout"})
public class AuthCheckServlet extends HttpServlet {
  private static final String AUTH_STATUS_URL              = "/auth-status";
  private static final String AUTH_LOGIN_URL               = "/auth-login";
  private static final String AUTH_LOGOUT_URL              = "/auth-logout";
  private static final String REDIRECT_URL_PARAMETER       = "redirect";
  private static final String RESPONSE_CONTENT_TYPE_JSON   = "application/json;";

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
      responseString = AuthCheck.getEmail();
    }

    if (requestUrl.equals(AUTH_LOGIN_URL)) {
      responseString = AuthCheck.generateLoginUrl(redirectUrl);
    } 

    if (requestUrl.equals(AUTH_LOGOUT_URL)) {
      responseString = AuthCheck.generateLogoutUrl(redirectUrl);
    }

    response.getWriter().print(stringToJson(responseString));
  }

  private String stringToJson(String str) {
    Gson gson   = new Gson();
    return gson.toJson(str);
  }
}
