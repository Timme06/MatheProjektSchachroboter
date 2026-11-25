# Roboter-DSL – Schachbrett-Simulator (Aufgabe 1d)

## Starten
`src/Main.java` öffnen und auf den grünen Run-Pfeil klicken. Die Bibliotheken liegen in `lib/` und sind in `MatheProjekt.iml` eingetragen.

## Projektaufbau
| Pfad | Inhalt |
|---|---|
| `src/robot/Robot.g4` | Die Grammatik der DSL |
| `src/robot/parser/` | Von ANTLR generierter Lexer, Parser und Visitor (nicht von Hand ändern) |
| `src/robot/model/` | Brett, Blickrichtung, Laufzeitfehler |
| `src/robot/interpreter/` | Einlesen (`ProgramParser`), Ausführen (`Interpreter` für Anweisungen, `ConditionEvaluator` für Bedingungen – beides Visitoren) und `BoardBlockWriter` (schreibt Kisten in den Code) |
| `test/` | JUnit-Tests: Brett, Grammatik, Interpreter (inkl. Vergleich Rechte-Hand-Regel ↔ Breitensuche), BoardBlockWriter |
| `src/robot/gui/` | Swing-Oberfläche |
| `beispiele/*.robot` | Beispielprogramme; `05`–`09` sind die Demo-Dateien für die Präsentation (Stufe 1 bis 3) |
| `lib/` | `antlr4-runtime` 4.13.2 sowie JUnit 4.13.2 und Hamcrest für die Tests |

## Die Sprache
```
board {                 // optional: Kisten auf Feldern
    obstacle C3
}
while not atGoal {      // Strategie
    if frontFree { forward } else { turnRight }
}
stop
```
- Befehle: `forward`, `turnLeft`, `turnRight`, `stop`
- Sensoren: `frontFree`, `leftFree`, `rightFree`, `atGoal`
- Kontrollstrukturen: `while`, `if … else`, `not`
- Kommentare: `// …`
- Der Roboter startet auf A1 mit Blick nach Norden. Der Brettrand zählt als Hindernis.
- `forward` gegen ein Hindernis ist ein Laufzeitfehler.

## Grammatik ändern
Nach jeder Änderung an `Robot.g4` müssen Parser und Visitor neu erzeugt werden:
1. Rechtsklick auf `Robot.g4` → **Configure ANTLR…**
   - *Output directory*: `src` (des Projekts)
   - *Package*: `robot.parser`
   - Haken bei **generate parse tree visitor**, Haken bei *listener* entfernen
2. Rechtsklick auf `Robot.g4` → **Generate ANTLR Recognizer**
3. Prüfen, dass die Dateien in `src/robot/parser/` aktualisiert wurden (und kein zusätzlicher `gen`-Ordner entstanden ist).

Zum Testen einzelner Regeln: Rechtsklick auf `program` → **Test Rule program**.

## Tests ausführen
Rechtsklick auf den Ordner `test` → **Run 'All Tests'**.
