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

package com.google.sps;

import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    int minDuration                         = (int) request.getDuration();

    ArrayList<TimeRange> busyMandatoryTimes = calculateBusyTimes(events, request.getAttendees());
    ArrayList<TimeRange> busyOptionalTimes  = calculateBusyTimes(events, request.getOptionalAttendees());

    ArrayList<TimeRange> busyAllTimes       = new ArrayList<TimeRange>();
    busyAllTimes.addAll(busyMandatoryTimes);
    busyAllTimes.addAll(busyOptionalTimes);

    ArrayList<TimeRange> availableAllTimes  = calculateAvailableTimes(busyAllTimes, minDuration);

    if (availableAllTimes.size() > 0) {
      return availableAllTimes; 
    }

    if (request.getAttendees().size() > 0) {
      return calculateAvailableTimes(busyMandatoryTimes, minDuration);
    }

    return new ArrayList<TimeRange>();
  }


  /**
   * Calculates timeranges where at least one of the newAttendees will be busy due to another event in events.
   **/
  private ArrayList<TimeRange> calculateBusyTimes(Collection<Event> events, Collection<String> newAttendees) {
    return events
      .stream()
      .filter(event -> !Collections.disjoint(event.getAttendees(), newAttendees))
      .map(event -> event.getWhen())
      .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Calculates a list of available timeranges given a list of unavailable timeranges
   * and a minimum duration of each available timerange.
   **/
  private ArrayList<TimeRange> calculateAvailableTimes(ArrayList<TimeRange> busyTimes, int minDuration) {
    ArrayList<TimeRange> availableTimes = new ArrayList<TimeRange>();

    Collections.sort(busyTimes, TimeRange.ORDER_BY_START);
    removeNestedTimeranges(busyTimes);

    if (busyTimes.size() == 0) {
      addIfPossible(availableTimes, TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, minDuration, true);
      return availableTimes;
    }

    addIfPossible(availableTimes, TimeRange.START_OF_DAY, busyTimes.get(0).start(), minDuration, false);

    for (int i = 0; i < busyTimes.size() - 1; ++i) {
      int availableStart    = busyTimes.get(i).end();
      int availableEnd      = busyTimes.get(i + 1).start();

      addIfPossible(availableTimes, availableStart, availableEnd, minDuration, false);
    }

    addIfPossible(availableTimes, busyTimes.get(busyTimes.size() - 1).end(), TimeRange.END_OF_DAY, minDuration, true);

    return availableTimes;
  }

  /**
   * Remove nested timeranges from list of sorted timeranges.
   **/
  private void removeNestedTimeranges(ArrayList<TimeRange> times) {
    Iterator<TimeRange> timesItr    = times.iterator();
    TimeRange prevTimeRange         = null;

    while (timesItr.hasNext()) {
      TimeRange currTimeRange = timesItr.next();

      if (prevTimeRange != null && prevTimeRange.contains(currTimeRange)) {
        timesItr.remove(); 
      } else {
        prevTimeRange = currTimeRange;
      }
    }
  }

  /**
   * Add a timerange to a list only when it fulfills the minimum criteria:
   * - The new timerange's start time is before its end time.
   * - The new timerange is longer than the minimum duration required.
   **/
  private void addIfPossible(ArrayList<TimeRange> times, int startTime, int endTime, int minDuration, boolean isInclusive) {
    if (startTime >= endTime || endTime - startTime < minDuration) {
      return;
    }

    times.add(TimeRange.fromStartEnd(startTime, endTime, isInclusive));
  }
}
