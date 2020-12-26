# WoodPacker
[![Java CI with Gradle](https://github.com/TrackerSB/WoodPacker/workflows/Java%20CI%20with%20Gradle/badge.svg)](https://github.com/TrackerSB/WoodPacker)
[![CodeQL](https://github.com/TrackerSB/WoodPacker/workflows/CodeQL/badge.svg)](https://github.com/TrackerSB/WoodPacker)
[![Codacy Security Scan](https://github.com/TrackerSB/WoodPacker/workflows/Codacy%20Security%20Scan/badge.svg)](https://github.com/TrackerSB/WoodPacker)
### Statistics (master branch)
[![Total](https://tokei.rs/b1/github/TrackerSB/WoodPacker?category=lines)](https://github.com/TrackerSB/WoodPacker)
[![LoC](https://tokei.rs/b1/github/TrackerSB/WoodPacker?category=code)](https://github.com/TrackerSB/WoodPacker)
[![comments](https://tokei.rs/b1/github/TrackerSB/WoodPacker?category=comments)](https://github.com/TrackerSB/WoodPacker)
[![blank lines](https://tokei.rs/b1/github/TrackerSB/WoodPacker?category=blanks)](https://github.com/TrackerSB/WoodPacker)
[![files](https://tokei.rs/b1/github/TrackerSB/WoodPacker?category=files)](https://github.com/TrackerSB/WoodPacker)
[![Github All Releases](https://img.shields.io/github/downloads/TrackerSB/WoodPacker/total.svg)](https://github.com/TrackerSB/WoodPacker)
## Motivation
Carpenters have to cut big wooden boards every day and all the time into potentially many pieces.
At this point there are a couple of targets carpenters want to archive when cutting the wooden board.
1. Utilize the wooden board as much as possible
1. Reduce the number required cuts
## Problems
In the general case such a problem boils down to a rectangle packing problem which optimizes for the criteria in the motivation section.
Unfortunately the rectangle packing problem is NP-hard.
However, since a carpenter works with wood there are several restrictions to the allowed solutions for a rectangle packing algorithm.
1. At any point there has to be at least one line on the wooden board to cut which is continues (otherwise a carpenter can not easily cut through)
1. The rectangles are often not allowed to be rotated (since the running direction of the wood grain is important)

Luckily, these restrictions should be enough to make an appropriate rectangle packing algorithm feasible
