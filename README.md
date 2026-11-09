# ReCHor

A Swiss public-transport journey planner written in Java, built for EPFL's CS-108 course (*Pratique de la programmation orientée objet*, spring 2025) by Jad Chahed Ouazzani and Riad Elhariry.

Given a departure stop, an arrival stop, a date and a time, ReCHor finds the best ways to travel between them, like cff.ch or search.ch. It runs entirely offline on timetable data derived from the official Swiss public-transport timetable.

## Features

- **Multi-criteria search**: journeys are optimised for departure time, arrival time and number of changes at the same time. ReCHor shows every **Pareto-optimal** journey: each one is better than the others on at least one criterion (for example, leaving later but changing more often).
- **Connection Scan Algorithm (CSA)**: computes, in a single pass over the day's connections, the optimal journeys from every station to the destination, using precomputed walking times between nearby stops.
- **Compact data representation**: the timetable is read from binary files through memory-mapped buffers, and the optimisation criteria of a journey are packed into a single 64-bit `long` to keep the search fast and memory-light.
- **JavaFX interface**: an overview of all journeys found that day, a detailed view of the selected journey, and stop-name autocompletion.
- **Export**: open a journey on a map (uMap, OpenStreetMap Switzerland) via GeoJSON, or export it to a calendar in iCalendar (`.ics`) format.

## Project structure

```
src/ch/epfl/rechor/
├── timetable/        timetable model: stations, platforms, routes, trips, connections, transfers
│   └── mapped/       loading the binary timetable files through memory-mapped buffers
├── journey/          routing: Router (CSA), Profile, ParetoFront, PackedCriteria, JourneyExtractor,
│                     GeoJSON and iCalendar converters
└── gui/              JavaFX interface: QueryUI, SummaryUI, DetailUI, StopField
```

## Running

Requirements: a recent JDK and JavaFX.

1. Download a weekly timetable archive from the [course project page](https://cs108.epfl.ch/archive/25/p/) and unzip it into a folder named `timetable/` at the project root.
2. Run `ch.epfl.rechor.gui.Main`.

## About the course

The project was built in weekly steps over the semester, from a heavily guided first half to a largely independent second half. The full specification is on the [CS-108 project page](https://cs108.epfl.ch/archive/25/p/00.html).
