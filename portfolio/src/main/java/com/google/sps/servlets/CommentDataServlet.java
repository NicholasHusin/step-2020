package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Class that implements storing and retrieving of comment data.
 * This class overloads DataServlet methods.
 * Note that deletion is implement by the child of this class: DeleteCommentDataServlet.
 **/
@WebServlet("/comment-data")
public class CommentDataServlet extends DataServlet {
  private final int MIN_COMMENTS_NUMBER         = 5;
  private final String COMMENT_NUMBER_PARAMETER = "comments-number";
  protected final String ENTITY_KIND            = "Comment";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    doPost(request, response, ENTITY_KIND);
    response.sendRedirect(REDIRECT_URL_HOME);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int commentsNumber  = parseIntParameter(request, COMMENT_NUMBER_PARAMETER);
    commentsNumber      = Math.max(commentsNumber, MIN_COMMENTS_NUMBER);

    doGet(response, ENTITY_KIND, ENTITY_TIMESTAMP_PARAMETER, commentsNumber);
  }
}


