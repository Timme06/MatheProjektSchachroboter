// Demo 5 – Stufe 3: Rechte-Hand-Regel auf demselben Brett

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
    if rightFree {
        turnRight
        forward
    } else {
        if frontFree {
            forward
        } else {
            turnLeft
        }
    }
}
stop
