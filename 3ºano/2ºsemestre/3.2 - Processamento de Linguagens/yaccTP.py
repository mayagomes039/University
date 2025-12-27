from Lexnew import tokens, lexer,re
import ply.yacc as yacc
from layer import layer



#
#
#
#
#
#Z : exp '$'
#
#
#exp : termo
#      termo exp 
#
#
#termo : NUM 
#     | expressao_artimetica
# *    | funcao
#
#
#expressao_artimetica : SOMA 
#                     | SUBTRACAO
#                     | MULT
#                     | DIV
#                     | MOD
#                     | SWAP
#                     | 2DROP
# *                    | DIVMOD
#  *                   | 2DUP
#   *                  | OVER
#    *                 | DUP
#                     | DROP
#      *               | 2SWAP
#       *              | ROT
dic={}
yacc_list = []


def p_1(p): 
    'Z : exp'
    p[0] = p[1]
    global yacc_list
    exps = p[0].split(" ")
    yacc_list.extend(exps)


    

def p_2(p):
    ''' exp : termo 
            | termo exp'''
    
    if len(p) > 2: 
        p[0] = f'{p[1]} {p[2]}'
    else: 
        p[0] = p[1]

def p_3(p): 
    '''termo : NUM 
            | expressao_artimetica
            | condicional expressao_condicional
            | condicional
            | funcao  '''
    if len(p)> 2 : 
        p[0] = f'{p[1]} {p[2]}'
    else: 
        p[0] = p[1]

def p_3_1(p): 
    ''' funcao : FUNCAO_RAW'''
    parts = p[1].split(' ', 2)  
    if len(parts) > 1:
        id_func= parts[1]  
        func_rules = parts[2][:-1]
    dic[id_func]={func_rules}
    p[0] = f'funcdef'


def p_3_2(p): 
    ''' termo : FUNCAO'''
    p[0] = f'FUNCAO:{p[1]}'


def p_4(p): 
    '''expressao_artimetica : SOMA 
                            | SUBTRACAO
                            | MULT
                            | DIV
                            | MOD
                            | SWAP
                            | DROP
                            | 2DROP
                            '''
    p[0] = p[1]

def p_5 (p): 
    ''' expressao_condicional   :  IF statement ELSE statement THEN statement
                                |  IF statement THEN statement 
                                |  IF statement THEN '''
    if len (p) > 6: 
        p[0] = f'{p[1]}->{p[2]}  {p[3]}->{p[4]}  {p[5]}->{p[6]}'
    elif len (p) > 4 and len(p)<=5: 
        p[0] = f'{p[1]}->{p[2]} {p[3]}->{p[4]}  '
    elif len (p) <=4: 
        p[0] = f'{p[1]}->{p[2]} {p[3]}'

def p_5_1 (p): 
    ''' statement   : NUM
                    | expressao_artimetica ''' 
    p[0] = p[1]

def p_6 (p):
    '''condicional  : MAIOR
                    | MENOR '''
# TODO DESCOBRIR COMO SE FAZ O IGUAL ( Usar dup)
    p[0] = p[1]
def p_error(p):
    print("Erro sintático no input!")
    print(p)

parser = yacc.yacc()

def imprimir_questionario(dicionario):
    if not dic: 
        print("não ha dic ")
    for chave, valor in dicionario.items():
        print(f"Pergunta: {chave}")
        print(f"Resposta: {valor}")
        print()


while True:
    entrada = input("Pressa 's' para terminar e imprimir codigo para a VM:\n>")
    entrada =  re.sub(r'\s{2,}', ' ', entrada)
    if entrada.lower() == 's':
        break
    print(yacc_list)
    conversor = parser.parse(entrada)
    print(yacc_list)
    print("\nYACC Completed\n")

    
vmCode = layer(yacc_list, dic)
print("\nVM CODE:\n")
print(vmCode)


