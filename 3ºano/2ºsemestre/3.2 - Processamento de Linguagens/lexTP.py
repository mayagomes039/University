import ply.lex as lex

tokens = [
    'NUM',
    'SOMA',
    'SUBTRACAO',
    'PONTO',
    'MULT',
    'DIV',
    'MOD',
    'DIVMOD'
    
]
t_DIVMOD = r'\/mod|\/MOD'
t_MOD = r'mod|MOD'
t_SUBTRACAO = r'-'
t_SOMA = r'\+'
t_PONTO = r'\.'
t_MULT = r'\*'
t_DIV = r'\/'
t_NUM = r'\d+'



t_ignore = ' \t\n\r'

def t_error(t):
    print('Caráter ilegal:', t.value[0])
    t.lexer.skip(1)

lexer = lex.lex()


