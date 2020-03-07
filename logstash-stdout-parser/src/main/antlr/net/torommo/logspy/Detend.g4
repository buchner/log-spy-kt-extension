grammar Detend;

start
    : entry+ NEWLINE EOF
    | EOF
    ;

entry
    : (INDENT | RIGHTWRAPPEDBY | RIGHTCAUSEDBY)? text
    ;

text
    : ~(INDENT | RIGHTCAUSEDBY | RIGHTWRAPPEDBY)+
    ;

RIGHTCAUSEDBY
    : '\nCaused by'
    ;

RIGHTWRAPPEDBY
    : '\nWrapped by'
    ;

INDENT
    : '\n\t'
    ;

NEWLINE
    : '\n'
    ;

TAB
    : '\t'
    ;

OTHER
    : .
    ;