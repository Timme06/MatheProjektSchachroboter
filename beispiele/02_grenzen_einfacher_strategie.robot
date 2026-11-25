// Beispiel 2: Grenzen einer einfachen Strategie
// "Wenn vorne frei, geh vor, sonst dreh rechts" klingt gut,
// läuft hier aber im Kreis am Brettrand entlang und kommt nie nach oben.
// Die Ausführung endet deshalb am Schrittlimit.

board {
    obstacle A4
}

while not atGoal {
    if frontFree {
        forward
    } else {
        turnRight
    }
}
stop
