grammar Stacktrace;

start
    : stackTrace EOF
    | EOF
    ;

stackTrace
    : (vanillaType | type) COLON SINGLESPACE vanillaMessage ENTRYEND nestedBlock? cause*
    | type COLON SINGLESPACE ENTRYEND nestedBlock? cause*
    | type COLON SINGLESPACE creepyMessage
    | (vanillaType | type) ENTRYEND nestedBlock? cause*
    ;

nestedBlock
    : INDENT stackFrame* suppressedBlock* DETEND
    ;

stackFrame
    : filledFrame
    | ommitedFrame
    ;

filledFrame
    : AT SINGLESPACE (vanillaType | type) DOT methodName LPARENTHESIS location? RPARENTHESIS ENTRYEND
    | AT SINGLESPACE DOT LPARENTHESIS location? RPARENTHESIS ENTRYEND
    ;

ommitedFrame
    : DOT DOT DOT SINGLESPACE unsignedInt SINGLESPACE COMMONFRAMES ENTRYEND
    ;

suppressedBlock
    : SUPPRESSED COLON SINGLESPACE (vanillaType | type) COLON SINGLESPACE vanillaMessage ENTRYEND nestedBlock? cause*
    | SUPPRESSED COLON SINGLESPACE (vanillaType | type) COLON SINGLESPACE ENTRYEND nestedBlock? cause*
    | SUPPRESSED COLON SINGLESPACE (vanillaType | type) COLON SINGLESPACE creepyMessage
    | SUPPRESSED COLON SINGLESPACE (vanillaType | type) ENTRYEND nestedBlock? cause*
    ;

cause
    : (CAUSEDBY | WRAPPEDBY) COLON SINGLESPACE (vanillaType | type) COLON SINGLESPACE vanillaMessage ENTRYEND nestedBlock?
    | (CAUSEDBY | WRAPPEDBY) COLON SINGLESPACE (vanillaType | type) COLON SINGLESPACE ENTRYEND nestedBlock?
    | (CAUSEDBY | WRAPPEDBY) COLON SINGLESPACE (vanillaType | type) COLON SINGLESPACE creepyMessage
    | (CAUSEDBY | WRAPPEDBY) COLON SINGLESPACE (vanillaType | type) ENTRYEND nestedBlock?
    ;

vanillaType
    : (vanillaUnqualifiedName+ SLASH SLASH?)+ vanillaDeclaringClass
    | vanillaDeclaringClass
    ;

type
    : (unqualifiedName+ SLASH SLASH?)+ declaringClass
    | declaringClass
    ;

vanillaMessage
    : ~(ENTRYEND | INDENT | DETEND )+
    ;

creepyMessage
    : ~(EOF)+
    ;

vanillaDeclaringClass
    : vanillaPackageName? vanillaClassName vanillaInnerClassName*
    ;

declaringClass
    : packageName? className innerClassName*
    ;

vanillaPackageName
    : (~(ENTRYEND | NEWLINE | INDENT | DETEND | BACKTICK | SINGLESPACE | DOT)+ DOT)+
    ;

packageName
    : (unqualifiedName DOT)+
    ;

vanillaClassName
    :  ~(ENTRYEND | NEWLINE | INDENT | DETEND | BACKTICK | SINGLESPACE | LPARENTHESIS | RPARENTHESIS | DOT)+
    ;

className
    : unqualifiedName
    ;

vanillaInnerClassName
    : DOLLAR vanillaClassName
    ;

innerClassName
    : DOLLAR className
    ;

methodName
    : ~(BACKTICK | ENTRYEND | NEWLINE | INDENT | DETEND )+
    ;

location
    : (NATIVEMETHOD | UNKNOWNSOURCE | ~(ENTRYEND | NEWLINE | INDENT | DETEND )+) COLON lineNumber
    | (NATIVEMETHOD | UNKNOWNSOURCE | ~(ENTRYEND | NEWLINE | INDENT | DETEND )+)
    ;

lineNumber
    : signedInt
    | unsignedInt
    ;

vanillaUnqualifiedName
    : ~(ENTRYEND | NEWLINE | INDENT | DETEND | BACKTICK | SINGLESPACE)+
    ;

unqualifiedName
    : ~(ENTRYEND | NEWLINE | INDENT | DETEND | BACKTICK)+
    ;

signedInt
    : MINUS unsignedInt
    ;

unsignedInt
    : POSITIVEDIGIT (ZERO | POSITIVEDIGIT)*
    | ZERO
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

INIT
    : '<init>'
    ;

CLINIT
    : '<clinit>'
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

INDENT
    : '\n\t\t'
    ;

DETEND
    : '\t\t\n'
    ;

ENTRYEND
    : '\n\t\n'
    ;

NEWLINE
    : '\n\n\n'
    ;

TAB
    : '\t\t\t'
    ;

BACKTICK
    : '`'
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

LANGLEBRACKET
    : '<'
    ;

RANGLEBRACKET
    : '>'
    ;

SLASH
    : '/'
    ;

AT_SIGN
    : '@'
    ;

SINGLESPACE
    : ' '
    ;

SEMICOLON
    : ';'
    ;

NULL
    : '\u{0}'
    ;

OTHER
    : .
    ;