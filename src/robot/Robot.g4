// Robot-DSL: Bewegung eines Roboters auf einem Schachbrett (Aufgabe 1d)
//
// Aufbau eines Programms:
//   board { obstacle C3  obstacle D5 ... }     (optional: Hindernisse)
//   <Anweisungen>                               (die Strategie)
//
// Der Roboter startet immer auf A1 mit Blick nach Norden (Richtung Reihe 8).
// Ziel: irgendein Feld der Reihe 8 erreichen.

grammar Robot;

// ---------- Parser-Regeln ----------

program    : board? statement* EOF ;

board      : 'board' '{' obstacle* '}' ;
obstacle   : 'obstacle' FIELD ;

statement  : command                                   # CommandStatement
           | 'while' condition block                   # WhileStatement
           | 'if' condition block ('else' block)?      # IfStatement
           ;

block      : '{' statement* '}' ;

command    : 'forward' | 'turnLeft' | 'turnRight' | 'stop' ;

condition  : 'not' condition                           # NotCondition
           | sensor                                    # SensorCondition
           ;

sensor     : 'frontFree' | 'leftFree' | 'rightFree' | 'atGoal' ;

// ---------- Lexer-Regeln ----------

FIELD        : [A-Ha-h] [1-8] ;
WORD         : [a-zA-Z_] [a-zA-Z_0-9]* ;   // unbekannte Wörter -> verständliche Fehlermeldung
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
WS           : [ \t\r\n]+ -> skip ;
