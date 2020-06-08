/**
 * Functions to run when page first loads.
 **/
window.addEventListener("load", function() {
  google.charts.load("current", {packages:["corechart"]});
  google.charts.setOnLoadCallback(loadHistoryChart);
});

function loadHistoryChart() {
  var data = google.visualization.arrayToDataTable([
    ["Command"  , "Frequency"   , { role: "style" }],
    ["vim"      , 1180          , "#EF5350"],
    ["ls"       , 878           , "#EC407A"],
    ["cd"       , 636           , "#AB47BC"],
    ["python"   , 486           , "#7E57C2"],
    ["docker"   , 287           , "#5C6BC0"],
    ["git"      , 200           , "#42A5F5"],
    ["ssh"      , 146           , "#29B6F6"],
    ["rm"       , 144           , "#26C6DA"],
    ["go"       , 106           , "#26A69A"],
    ["cat"      , 91            , "#66BB6A"],
    ["ulimit"   , 78            , "#9CCC65"],
    ["mv"       , 62            , "#D4E157"],
    ["pacman"   , 60            , "#FFEE58"],
    ["evince"   , 51            , "#FFCA28"],
    ["curl"     , 50            , "#FFA726"],
    ["tmux"     , 45            , "#FF7043"],
    ["grep"     , 45            , "#8D6E63"],
    ["tar"      , 39            , "#BDBDBD"],
    ["gcc"      , 39            , "#78909C"],
    ["htop"     , 35            , "#455A64"],
  ]);

  var view = new google.visualization.DataView(data);
  view.setColumns([0, 1,
    { calc: "stringify",
      sourceColumn: 1,
      type: "string",
      role: "annotation" },
    2]);

  var options = {
    title: "Most Frequently Used Commands Over Last ~3 Months",
    bar: {groupWidth: "95%"},
    legend: { position: "none" },
    height: 550,
    fontName: 'Monospace',
  };

  var chart = new google.visualization.BarChart(document.getElementById("history-chart"));
  chart.draw(view, options);
}
