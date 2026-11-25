// Beispiel 4: Rechte-Hand-Regel (Wall Follower)
// Der Roboter hält die "rechte Hand" immer an einer Wand (Kiste oder Brettrand).
// Reihenfolge pro Durchlauf: rechts frei? -> rechts abbiegen und gehen,
// sonst vorne frei? -> geradeaus, sonst -> links drehen.
// Damit läuft er den Rand des erreichbaren Bereichs ab und kommt sicher
// auf Reihe 8 an, sofern ein Weg existiert. Gibt es keinen Weg, greift
// das Schrittlimit.

board {
    obstacle B2
    obstacle C2
    obstacle D2
    obstacle E2
    obstacle F2
    obstacle G2
    obstacle A4
    obstacle B4
    obstacle C4
    obstacle E4
    obstacle F4
    obstacle G4
    obstacle H4
    obstacle B6
    obstacle C6
    obstacle D6
    obstacle E6
    obstacle F6
    obstacle G6
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
