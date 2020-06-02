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
  private final int DEFAULT_COMMENTS_NUMBER         = 5;
  private final String REDIRECT_URL_HOME            = "/";
  private final String RESPONSE_CONTENT_TYPE_JSON   = "application/json;";
  private final String NEW_COMMENT_PARAMETER        = "new-comment";
  private final String COMMENT_NUMBER_PARAMETER     = "comments-number";
  private final String COMMENT_ENTITY_KIND          = "Comment";
  private final String COMMENT_ENTITY_TEXT          = "text";
  private final String COMMENT_ENTITY_TIMESTAMP     = "timestamp";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String text     = request.getParameter(NEW_COMMENT_PARAMETER);
    long timestamp  = System.currentTimeMillis();

    Entity commentEntity = new Entity(COMMENT_ENTITY_KIND);
    commentEntity.setProperty(COMMENT_ENTITY_TEXT, text);
    commentEntity.setProperty(COMMENT_ENTITY_TIMESTAMP, timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect(REDIRECT_URL_HOME);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query         = new Query(COMMENT_ENTITY_KIND).addSort(COMMENT_ENTITY_TIMESTAMP, SortDirection.DESCENDING);
    int commentsNumber  = parseCommentsNumber(request);

    DatastoreService datastore      = DatastoreServiceFactory.getDatastoreService();
    List<Entity> commentEntities    = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(commentsNumber));

    String json = entitiesToJson(commentEntities);

    response.setContentType(RESPONSE_CONTENT_TYPE_JSON);
    response.getWriter().println(json);
  }

  /**
   * Converts a list of entities into a JSON using the GSON library.
   * Can be used even when more properties are added to entities to support other features as GSON library uses reflection.
   **/
  private String entitiesToJson(List<Entity> entities) {
    Gson gson   = new Gson();
    String json = gson.toJson(entities);
    return json;
  }

  /**
   * Returns the number of displayed comments selected by the user.
   * DEFAULT_COMMENTS_NUMBER is  if the choice was invalid.
   **/
  private int parseCommentsNumber(HttpServletRequest request) {
    String commentsNumberString = request.getParameter(COMMENT_NUMBER_PARAMETER);

    int commentsNumber;
    try {
      commentsNumber = Integer.parseInt(commentsNumberString);
    } catch (NumberFormatException e) {
      return DEFAULT_COMMENTS_NUMBER;
    }

    return commentsNumber;
  }
}


