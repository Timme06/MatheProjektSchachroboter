// Beispiel 3: Vorlage für eure eigene Strategie
// Ein Brett mit Mauern und Sackgassen. Ein Weg nach oben existiert.
//
// Verfügbare Befehle:   forward  turnLeft  turnRight  stop
// Verfügbare Sensoren:  frontFree  leftFree  rightFree  atGoal
// Kontrollstrukturen:   while <Bedingung> { ... }
//                       if <Bedingung> { ... } else { ... }
//                       not <Bedingung>

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

// TODO: Hier eure Strategie eintragen (Stichwort: Rechte-Hand-Regel)
stop
