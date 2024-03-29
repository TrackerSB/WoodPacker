# WoodPacker
[![Java CI with Gradle](https://github.com/TrackerSB/WoodPacker/workflows/Java%20CI%20with%20Gradle/badge.svg)](https://github.com/TrackerSB/WoodPacker)
[![CodeQL](https://github.com/TrackerSB/WoodPacker/workflows/CodeQL/badge.svg)](https://github.com/TrackerSB/WoodPacker)
[![Codacy Security Scan](https://github.com/TrackerSB/WoodPacker/workflows/Codacy%20Security%20Scan/badge.svg)](https://github.com/TrackerSB/WoodPacker)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/7f2ad56569ee4358bcead7c281ae7058)](https://www.codacy.com/gh/TrackerSB/WoodPacker/dashboard)
### Statistics (master branch)
[![Total](https://tokei.rs/b1/github/TrackerSB/WoodPacker?category=lines)](https://github.com/TrackerSB/WoodPacker)
[![LoC](https://tokei.rs/b1/github/TrackerSB/WoodPacker?category=code)](https://github.com/TrackerSB/WoodPacker)
[![comments](https://tokei.rs/b1/github/TrackerSB/WoodPacker?category=comments)](https://github.com/TrackerSB/WoodPacker)
[![blank lines](https://tokei.rs/b1/github/TrackerSB/WoodPacker?category=blanks)](https://github.com/TrackerSB/WoodPacker)
[![files](https://tokei.rs/b1/github/TrackerSB/WoodPacker?category=files)](https://github.com/TrackerSB/WoodPacker)
[![Github All Releases](https://img.shields.io/github/downloads/TrackerSB/WoodPacker/total.svg)](https://github.com/TrackerSB/WoodPacker)

## Overview
For a general overview visit [https://steinbrecher-bayern.de/projects/programs.html#woodPacker](https://steinbrecher-bayern.de/projects/programs.html#woodPacker).
## Elaboration of technical problems
### Rectangular packing problem
In the general case such a problem boils down to a rectangle packing problem which optimizes for the criteria in the motivation section.
Unfortunately the rectangle packing problem is NP-hard.
However, since a carpenter works with wood there are several restrictions to the allowed solutions for a rectangle packing algorithm.
1. At any point there has to be at least one line on the wooden board to cut which is continues (otherwise a carpenter can not easily cut through)
1. The rectangles are often not allowed to be rotated (since the running direction of the wood grain is important)

Luckily, these restrictions should be enough to make an appropriate rectangle packing algorithm feasible

### Optimization
Finding an optimal cutting plan depends on multiple criteria including how expensive the initial plank is and how long it should take to cut all the pieces.
Hence there is no clear "perfect" cutting plan and the optimization criteria have to be exposed to the user in a way such that users understand the criteria and can utilize them.
