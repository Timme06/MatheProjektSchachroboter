// Demo 3 – Stufe 2: Ausweichen nach rechts klappt bei einzelnen Kisten

board {
    obstacle A4
    obstacle B6
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
