// Beispiel 1: Freie Bahn
// Kein Hindernis in Spalte A – der Roboter läuft einfach geradeaus.

board {
    obstacle C3
    obstacle F5
    obstacle B7
}

while not atGoal {
    forward
}
stop
