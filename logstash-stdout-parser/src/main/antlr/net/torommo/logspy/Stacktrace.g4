grammar Stacktrace;

start
    : stackTrace EOF
    | EOF
    ;

stackTrace
    : declaringClass (COLON SINGLESPACE message?)? NEWLINE nestedBlock? (cause | wrap)*
    ;

nestedBlock
    : INDENT stackFrame* suppressedBlock* DETEND
    ;

stackFrame
    : filledFrame
    | ommitedFrame
    ;

filledFrame
    : AT SINGLESPACE type DOT methodName LPARENTHESIS location RPARENTHESIS NEWLINE
    ;

ommitedFrame
    : ELLIPSIS SINGLESPACE unsignedInt SINGLESPACE COMMONFRAMES NEWLINE
    ;

suppressedBlock
    : SUPPRESSED COLON SINGLESPACE type (COLON SINGLESPACE message?)? NEWLINE nestedBlock? (cause | wrap)*
    ;

cause
    : CAUSEDBY COLON SINGLESPACE type (COLON SINGLESPACE message?)? NEWLINE nestedBlock?
    ;

wrap
    : WRAPPEDBY COLON SINGLESPACE type (COLON SINGLESPACE message?)? NEWLINE nestedBlock?
    ;

type
    : encodedType | declaringClass
    ;

encodedType
    : LSQUAREBRACKET+ declaringClass
    ;

message
    : ~(NEWLINE)+
    ;

declaringClass
    : packageName? className innerClassName*
    ;

packageName
    : (javaName DOT) +
    ;

className
    : javaName
    ;

innerClassName
    : DOLLAR javaName
    ;

methodName
    : javaName
    ;

location
    : (NATIVEMETHOD | UNKNOWNSOURCE | filename COLON lineNumber)
    ;

filename
    : ~(COLON)+
    ;

lineNumber
    : unsignedInt
    ;

javaName
    : ~(NEWLINE | COLON | DOT)+
    ;

unsignedInt
    : POSITIVEDIGIT (ZERO | POSITIVEDIGIT)*
    ;

AT
    : 'at'
    ;

CAUSEDBY
    : 'Caused by'
    ;

WRAPPEDBY
    : 'Wrapped by'
    ;

SUPPRESSED
    : 'Suppressed'
    ;

NATIVEMETHOD
    : 'Native Method'
    ;

UNKNOWNSOURCE
    : 'Unknown Source'
    ;

COMMONFRAMES
    : 'common frames ommited'
    ;

UPPERCASEROMAN
    : [A-Z]
    ;

LOWERCASEROMAN
    : [a-z]
    ;

ZERO
    : '0'
    ;

POSITIVEDIGIT
    : [1-9]
    ;

ELLIPSIS
    : '...'
    ;

TAB
    : '\t\t'
    ;

NEWLINE
    : '\n\n'
    ;

INDENT
    : '\n\t'
    ;

DETEND
    : '\t\n'
    ;

UNDERSCORE
    : '_'
    ;

DOT
    : '.'
    ;

MINUS
    : '-'
    ;

DOLLAR
    : '$'
    ;

COLON
    : ':'
    ;

LSQUAREBRACKET
    : '['
    ;

LPARENTHESIS
    : '('
    ;

RPARENTHESIS
    : ')'
    ;

SINGLESPACE
    : ' '
    ;

NULL
    : '\u{0}'
    ;