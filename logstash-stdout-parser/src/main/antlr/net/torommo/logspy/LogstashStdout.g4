grammar LogstashStdout;

stdout
    : (json NEWLINE)+ EOF
    | (json NEWLINE)+ incomplete EOF
    | incomplete EOF
    | EOF
    ;

json
    : ~(NEWLINE)+
    ;

incomplete
    : ~(EOF)+
    ;

NEWLINE
    : '\n'
    ;

OTHER
    : .
    ;
