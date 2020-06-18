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
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public final class FindMeetingQuery {
  /**
   * Function to find the best meeting time, defined as:
   * - Times where all mandatory attendees are available.
   * - Times where there could be as much optional attendees as possible.
   *
   * Note that there might be cases where there could be the same amount of optional attendees available.
   * For example, optional attendee A and B can make it to 8:30 but C and D can make it to 9:30
   * In this case, priority is given to optional attendees that are inputted first.
   * Ex: If A B C D are inpputed in that order, the priority is A > B > C > D.
   *
   * Prepares HashMap to be used for memoization in addAsMuchAttendee
   * and does all needed type conversions.
   **/
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    HashMap<ArrayList<String>, ArrayList<TimeRange>> resultMemo = new HashMap<>();

    int minDuration                         = (int) request.getDuration();
    ArrayList<String> mandatoryAttendees    = new ArrayList<>(request.getAttendees());
    ArrayList<String> optionalAttendees     = new ArrayList<>(request.getOptionalAttendees());

    ArrayList<String> bestAttendees = addAsMuchAttendee(events, mandatoryAttendees, optionalAttendees, 
                                                        resultMemo, optionalAttendees.size() - 1, minDuration);

    return resultMemo.get(bestAttendees);
  }

  /**
   * Recursive function that returns the best list of attendee possible, defined as attendees
   * such that the meeting times are:
   * - Times where all mandatory attendees are available.
   * - Times where there could be as much optional attendees as possible.
   *
   * Gives priority to optional attendees that are inputted earlier.
   * 
   * How this function works:
   * 1. Start with the assumption that all optional attendees should be included 
   *    and index pointing to the last attendee on the list.
   * 2. If there's a time where all mandatory and all optional attendees could meet, return it.
   *    This means that we have found the best case possible in the current branch and can avoid
   *    wasted computation. We also memoize the result at this step.
   * 3. If not, branch the recursion into two cases:
   *    - One where we remove the attendee pointed by the index and move the index backwards by 1.
   *    - One where we don't remove the attendee and only move the index.
   * 4. The two branches will keep on recursing until they hit the base case and will return
   *    the best possible list in their respective branches.
   * 5. We compare the result from the two branches and return the best one upwards.
   *
   * In other words, this function generates the power set of attendees starting from the one
   * with the most elements. We start with the most elements so we can stop early if the best case
   * scenario is found early (For example, this function only runs once if there's a time slot
   * where all attendees can attend).
   **/
  private ArrayList<String> addAsMuchAttendee(Collection<Event> events, ArrayList<String> mandatoryAttendees,
                                                ArrayList<String> optionalAttendees, HashMap<ArrayList<String>, 
                                                ArrayList<TimeRange>> resultMemo, int index, int minDuration) {

    // Calculates and memoizes the possible times for a meeting given the current attendees.
    calculateMeetingTimeMemoize(events, mandatoryAttendees, optionalAttendees, resultMemo, minDuration);

    // Base case: No more attendees to modify.
    if (index < 0)
      return optionalAttendees;

    // Base case: Found the best case scenario in current recursion branch.
    if (!resultMemo.get(optionalAttendees).isEmpty())
      return optionalAttendees;


    // Set up variable for next recursion: 
    // One where the attendee in index is removed and one where it's not.
    ArrayList<String> withCurrentAttendee       = new ArrayList<>(optionalAttendees); 
    ArrayList<String> withoutCurrentAttendee    = new ArrayList<>(optionalAttendees); 
    withoutCurrentAttendee.remove(index);

    // Does the recursion and updates the two variables to point at the best possible lists in both branches.
    withCurrentAttendee     = addAsMuchAttendee(events, mandatoryAttendees, withCurrentAttendee, resultMemo, index - 1, minDuration);
    withoutCurrentAttendee  = addAsMuchAttendee(events, mandatoryAttendees, withoutCurrentAttendee, resultMemo, index - 1, minDuration);

    // Returns the best list of attendees upward.
    if (!resultMemo.get(withCurrentAttendee).isEmpty() && withCurrentAttendee.size() > withoutCurrentAttendee.size()) {
      return withCurrentAttendee;
    }

    return withoutCurrentAttendee;
  }

  /**
   * Helper function that calculates the possible meeting times given a list of attendees.
   * The result is stored into the HashMap to prevent repeated work in addAsMuchAttendee when recursing.
   *
   * Uses optional attendees as the key. 
   * Uses the possible time for the mandatory and optional attendees to meet as the result.
   **/
  private void calculateMeetingTimeMemoize(Collection<Event> events, ArrayList<String> mandatoryAttendees, 
                                            ArrayList<String> optionalAttendees, HashMap<ArrayList<String>, 
                                            ArrayList<TimeRange>> resultMemo, int minDuration) {

    if (resultMemo.containsKey(optionalAttendees)) {
      return;
    }

    ArrayList<String> attendees = new ArrayList<>();
    attendees.addAll(mandatoryAttendees);
    attendees.addAll(optionalAttendees);

    ArrayList<TimeRange> meetingTimes = calculateAvailableTimes(calculateBusyTimes(events, attendees), minDuration);
    resultMemo.put(optionalAttendees, meetingTimes);
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
  private void addIfPossible(ArrayList<TimeRange> times, int startTime, int endTime, 
                                int minDuration, boolean isInclusive) {

    if (startTime >= endTime || endTime - startTime < minDuration) {
      return;
    }

    times.add(TimeRange.fromStartEnd(startTime, endTime, isInclusive));
  }
}
