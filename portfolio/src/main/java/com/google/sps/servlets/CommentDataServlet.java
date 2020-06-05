package com.google.sps.servlets;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.utils.AuthCheck;


/**
 * Class that implements storing and retrieving of comment data.
 * This class overloads DataServlet methods.
 * Note that deletion is implement by the child of this class: DeleteCommentDataServlet.
 **/
@WebServlet("/comment-data")
public class CommentDataServlet extends DataServlet {
  private final int MIN_COMMENTS_NUMBER         = 5;
  private final String COMMENT_NUMBER_PARAMETER = "comments-number";
  private final String USER_LDAP_PARAMETER      = "ldap";
  protected final String ENTITY_KIND            = "Comment";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!hasEnoughPrivilege()) {
      return;
    }

    HashMap<String,String> extraParameters = new HashMap<String, String>();
    extraParameters.put(USER_LDAP_PARAMETER, AuthCheck.getLdap());

    doPost(request, response, ENTITY_KIND, extraParameters);
    response.sendRedirect(REDIRECT_URL_HOME);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!hasEnoughPrivilege()) {
      return;
    }

    int commentsNumber  = parseIntParameter(request, COMMENT_NUMBER_PARAMETER);
    commentsNumber      = Math.max(commentsNumber, MIN_COMMENTS_NUMBER);

    doGet(response, ENTITY_KIND, ENTITY_TIMESTAMP_PARAMETER, commentsNumber);
  }

  /**
   * Utility class to check if users who sent requests have enough privilege
   * to store / view / delete comments.
   * This should be re used by all functions that implements store / view / delete
   * to make sure that privilege level are synced should it be changed in the future.
   **/
  protected boolean hasEnoughPrivilege() {
    return AuthCheck.isGoogleEmail();
  }
}


