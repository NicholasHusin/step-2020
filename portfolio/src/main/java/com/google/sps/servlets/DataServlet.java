// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.gson.Gson;


@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private final int DEFAULT_COMMENTS_NUMBER = 5;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String content = request.getParameter("new-comment");
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("content", content);
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    int commentsNumber = parseCommentsNumber(request);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(commentsNumber));

    List<String> commentContents = new ArrayList<>();
    for (Entity entity : results) {
      String content = (String) entity.getProperty("content");
      commentContents.add(content);
    }

    String json = convertListToJson(commentContents);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  private String convertListToJson(List<String> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }

  /**
   * Returns the number of displayed comments selected by the user
   * DEFAULT_COMMENTS_NUMBER is  if the choice was invalid.
   **/
  private int parseCommentsNumber(HttpServletRequest request) {
    String commentsNumberString = request.getParameter("comments-number");

    int commentsNumber;
    try {
      commentsNumber = Integer.parseInt(commentsNumberString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + commentsNumberString);
      return DEFAULT_COMMENTS_NUMBER;
    }

    return commentsNumber;
  }
}


