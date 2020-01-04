grammar LogstashStdout;

stdout
    : json EOF
    | (json NEWLINE)+ EOF
    | (json NEWLINE)+ incomplete EOF
    ;

json
    : ~(NEWLINE)+
    ;

incomplete
    : ~(EOF)+
    ;

NONEWLINE
    : ~[\n]
    ;

NEWLINE
    : '\n'
    ;