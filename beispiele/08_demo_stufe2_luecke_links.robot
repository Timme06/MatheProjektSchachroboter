// Demo 4 – Stufe 2 scheitert: die Lücke in der Mauer liegt links

board {
    obstacle A3
    obstacle B5
    obstacle C5
    obstacle D5
    obstacle E5
    obstacle F5
    obstacle G5
    obstacle H5
}

while not atGoal {
    if frontFree {
        forward
    } else {
        turnRight
        forward
        turnLeft
    }
}
stop
