import ply.lex as lex
import re

tokens = [
    'NUM',
    'SOMA',
    'SUBTRACAO',
    'PONTO',
    'MULT',
    'DIV',
    'MOD',
    'DIVMOD', 
    'DUP',
    '2DUP', 
    'SWAP',
    'OVER',
    'DROP',
    '2DROP',
    '2SWAP',
    '2OVER',
    'COMENTARIO',
    'FUNCAO_RAW',
    'ROT',
    'EMIT',
    'SPACE',
    'SPACES',
    'CHAR',
    'CR',
    'STR', 
    'IF',
    'ELSE',
    'THEN', 
    'IGUAL',
    'MAIOR',
    'MENOR',
    'FUNCAO'

    
]

t_MENOR = r'<'
t_MAIOR = r'>'
t_IGUAL = r'='
t_THEN = r'then|THEN'
t_ELSE = r'else|ELSE'
t_IF = r'if|IF'
t_CR  = r'cr|CR'
t_CHAR = r'(?i)(?:char|CHAR)\s+(\w+)'
t_SPACES = r'spaces|SPACES'
t_SPACE = r'space|SPACE'
t_EMIT = r'emit|EMIT'
t_ROT = r'rot|ROT'
t_2OVER = r'2over|2OVER'
t_2SWAP = r'2swap|2SWAP'
t_2DROP = r'2drop|2DROP'
t_DROP = r'drop|DROP'
t_2DUP = r'2dup|2DUP'
t_DUP = r'dup|DUP'
t_DIVMOD = r'\/mod|\/MOD'
t_MOD = r'mod|MOD'
t_SUBTRACAO = r'-'
t_SOMA = r'\+'
t_PONTO = r'\.'
t_MULT = r'\*'
t_DIV = r'\/'
t_NUM = r'\d+'
t_SWAP = r'swap|SWAP'
t_OVER = r'over|OVER'
t_FUNCAO = r'^(?!cr|CR|char|CHAR|ELSE|IF|ELSE|THEN)[A-Z]+'

def t_COMENTARIO(t): 
    r'\(.*?\)'
    return t 

def t_STR(t):
    r'\.\"(.*?)\"'
    match = re.match(r'\.\"(.*?)\"', t.value)
    if match:
        v = match.group(1)
    return v

def t_FUNCAO_RAW(t):
    r':\s+?(\w+)\s+?(\([^()]*\))?\s*(.*?)(?=;);'
    match = re.match(r':\s+?(\w+)\s+?(\([^()]*\))?\s*(.*?)(?=;);', t.value)
    if match:
        t.value = ": " + match.group(1) + " " + match.group(3) + ";"
        print(t)
        return t



t_ignore = ' \t\n\r' 

def t_error(t):
    print('Caráter ilegal:', t.value[0])
    t.lexer.skip(1)

lexer = lex.lex()