from Lexnew import tokens, lexer,re
import ply.yacc as yacc


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
#     | funcao
#     | FUNCAO_RAW
#     | condicional 
#     | condicional expressao_condicional 
#     | str 
#     | PONTO
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
#
# condiconal : MAIOR 
#            | MENOR 
#
#
#expressao_condicional : IF exp THEN
#                      | IF exp THEN exp
#                      | IF exp ELSE exp THEN 
#                      | IF exp ELSE exp THEN exp 
#
#str : CHAR 
#    | SPACE
#    | CR 
#    | STRING
#    | EMIT

###########################################################################################################################################################################################

#SETERS 

dic={}
stack_size = 0 
jump_count = 0
existe_funcao_if = False 
n_variaveis  = 0 
storeg_counter = 0 


###########################################################################################################################################################################################
def p_1(p): 
    'Z : exp'
    global n_variaveis
    global existe_funcao_if 
    
    if n_variaveis>0   : 
        codigo = ""
        for i in range(n_variaveis):
            codigo = codigo + ("pushi 0\n")
            vmcode= codigo + f'start\n{p[1]}\nstop\n'
            existe_funcao_if = False

    elif existe_funcao_if:  
            vmcode = f'start\n{p[1]}\nstop\n'
    else: 
        vmcode = p[1]


    p[0] = f'{vmcode}'
    print("\n\n")
    print(p[0])

###########################################################################################################################################################################################

#exp : termo
#    | termo exp


def p_2(p):
    ''' exp : termo 
            | termo exp'''
    
    if len(p) > 2: 
        p[0] = f'{p[1]}{p[2]}'
    else: 
        p[0] = f'{p[1]}'
    print("exp : termno")

###########################################################################################################################################################################################


#termo :  
#     | expressao_artimetica
#     | funcao 
#     | condicional expressao_condicional
#     | condicional 
#     | str 
#     | FUNCAO 
#     | NUM 
#     | PONTO


def p_3(p): 
    '''termo : expressao_artimetica
            | funcao
            | condicional expressao_condicional 
            | condicional
            | str  
            '''
    if len(p)> 2 : 
        p[0] = f'{p[1]}{p[2]}'
    else: 
        p[0] = f'{p[1]}'
    print("termo : expresao arimetica e funca")

def p_3_1(p): 
    ''' termo : FUNCAO'''
    global existe_funcao_if
    existe_funcao_if = True
    decode = str(dic[p[1]])
    decode2 = decode.strip("{}'")
    code  = parser.parse(decode2)
    p[0] = f'{code}'
    print("termo : FUNCAO")


def p_3_2_2(p): 
        '''termo : NUM  '''
        global stack_size
        stack_size +=1
        p[0] = f'pushf {p[1]}\n'
        print("NUM")

def p_3_PONTO(p): 
    '''termo : PONTO'''
    global stack_size
    stack_size -=1
    if stack_size < 0: 
        p[0] = "Error: Stack Vazia"
    p[0] =  f'writei\nwriteln\n'


###########################################################################################################################################################################################


def p_3_1_2(p): 
    ''' funcao : FUNCAO_RAW'''
    parts = p[1].split(' ', 2)  
    if len(parts) > 1:
        id_func= parts[1]  
        func_rules = parts[2][:-1]
    dic[id_func]={func_rules}
    p[0] = f' '
    print("termo :   e funca raw ")

###########################################################################################################################################################################################

#expressao_artimetica : SOMA 
#                     | SUBTRACAO
#                     | MULT
#                     | DIV
#                     | MOD
#                     | SWAP
#                     | DROP
#                     | 2DROP
#                     | OVER
#                     | 2OVER
#                     | 2SWAP
#                     | ROT
#                     | DUP
#                     | 2DUP
#                     | DIVMOD


def p_4soma (p): 
    '''expressao_artimetica : SOMA '''
    global stack_size
    stack_size -=2
    if stack_size < 0: 
        p[0] = "Error: Stack Vazia"
    stack_size +=1
    p[0] = f'add\n'
    print("expressao_artimetica : ADD")


def p_4sub (p): 
    '''expressao_artimetica : SUBTRACAO '''
    global stack_size
    stack_size -=2
    if stack_size < 0: 
        p[0] = f"Error: Stack Vazia"
    else:
        stack_size +=1
        p[0] = f'sub\n'
    print("expressao_artimetica : SUB")


def p_4mul (p): 
    '''expressao_artimetica : MULT '''
    global stack_size
    stack_size -=2
    if stack_size < 0: 
        p[0] = "Error: Stack Vazia"
    stack_size +=1
    p[0] = f'mul\n'
    print("expressao_artimetica : MULÇT")


def p_4div (p): 
    '''expressao_artimetica : DIV '''
    global stack_size
    stack_size -=2
    if stack_size < 0: 
        p[0] = "Error: Stack Vazia"
    stack_size +=1
    p[0] = f'div\nftoi\n'
    print("expressao_artimetica : DIV")

def p_4mod (p): 
    '''expressao_artimetica : MOD '''
    p[0] = f'mod\n'
    print("expressao_artimetica : MOD")


def p_4swap (p):
    '''expressao_artimetica : SWAP '''
    p[0] = f'swap\n'
    print("expressao_artimetica : SWAP")

def p_4drop (p):
    '''expressao_artimetica : DROP '''
    p[0] = f'pop\n'
    print("expressao_artimetica : DROP")

def p_42drop (p):
    '''expressao_artimetica : 2DROP '''
    p[0] = f'pop 2\n'
    print("expressao_artimetica : 2DROP")

def p_4_over (p): 
    '''expressao_artimetica : OVER'''
    global n_variaveis 
    n_variaveis += 2
    global stack_size 
    if stack_size < 2: 
        p[0] = "Error: Stack Vazia"
    global storeg_counter
    res = f'storeg {storeg_counter}\nstoreg {storeg_counter +1}\npushg {storeg_counter +1}\npushg {storeg_counter}\npushg {storeg_counter +1}\n'
    stack_size += 1 
    p[0] = res 

def p_4_2over (p): 
    '''expressao_artimetica : 2OVER'''
    global n_variaveis 
    n_variaveis += 4
    global stack_size 
    if stack_size < 4: 
        p[0] = "Error: Stack Vazia"
    global storeg_counter
    res = f'storeg {storeg_counter}\nstoreg {storeg_counter +1}\nstoreg {storeg_counter +2}\npushg {storeg_counter+1}\npushg {storeg_counter + 0 }\npushg {storeg_counter +2}\n'
    stack_size += 2 
    p[0] = res 


def p_4_2swap (p): 
    '''expressao_artimetica : 2SWAP'''
    global n_variaveis 
    n_variaveis += 2
    global stack_size 
    if stack_size < 4: 
        p[0] = "Error: Stack Vazia"
    global storeg_counter
    res = f'storeg {storeg_counter}\nstoreg {storeg_counter +1}\nstoreg {storeg_counter +2}\nstoreg {storeg_counter +3}\npushg {storeg_counter +1}\npushg {storeg_counter}\npushg {storeg_counter +3}\npushg {storeg_counter +2}\n'
    p[0] = res 


def p_4_2rot (p): 
    '''expressao_artimetica : ROT'''
    global n_variaveis 
    n_variaveis += 3
    global stack_size 
    if stack_size < 3: 
        p[0] = "Error: Stack Vazia"
    global storeg_counter
    res = f'storeg {storeg_counter}\nstoreg {storeg_counter +1}\nstoreg {storeg_counter +2}\npushg {storeg_counter+1}\npushg {storeg_counter + 0 }\npushg {storeg_counter +2}\n'
    p[0] = res 

def p_4_2dup (p):
    '''expressao_artimetica : 2DUP'''
    global n_variaveis 
    n_variaveis += 2
    global stack_size 
    if stack_size < 2: 
        p[0] = "Error: Stack Vazia"
    global storeg_counter
    res = f'storeg {storeg_counter}\nstoreg {storeg_counter +1}\npushg {storeg_counter+1}\npushg {storeg_counter }\npushg {storeg_counter +1 }\npushg {storeg_counter}\n'
    stack_size += 2 
    p[0] = res

def p_4_dup (p):
    '''expressao_artimetica : DUP'''
    p[0] = {f'dup 1\n'}

def p_4_divmod (p):
    '''expressao_artimetica : DIVMOD'''
    global n_variaveis 
    n_variaveis += 2
    global stack_size 
    if stack_size < 1: 
        p[0] = "Error: Stack Vazia"
    global storeg_counter
    p[0] = f'storeg {storeg_counter}\nstoreg {storeg_counter +1}\npushg {storeg_counter +1}\npushg {storeg_counter}\nmod\npushg {storeg_counter +1}\npushg {storeg_counter}\ndiv\n'
    stack_size += 2
    print("expressao_artimetica : DIVMOD")
    

###########################################################################################################################################################################################

#expressao_condicional : IF exp THEN
#                      | IF exp THEN exp
#                      | IF exp ELSE exp THEN 
#                      | IF exp ELSE exp THEN exp 

def p_expressao_condicional(p):

    '''expressao_condicional    : IF exp THEN
                                | IF exp THEN exp
                                | IF exp ELSE exp THEN 
                                | IF exp ELSE exp THEN exp '''
    global jump_count
    jump_count +=1
    global existe_funcao_if
    existe_funcao_if = True

    if len(p) == 5:
        p[0] = f'jz endif{jump_count}\n{p[2]}endif{jump_count}:\n{p[4]}'
    elif len (p) == 4: 
        p[0] = f'jz endif{jump_count}\n{p[2]}endif{jump_count}:'
    elif len (p) == 6:
        p[0] = f'jz else{jump_count}\n{p[2]}\njump endif{jump_count}\nelse{jump_count}:\n{p[4]}endif{jump_count}:'
    elif len(p) == 7:
        p[0] = f'jz else{jump_count}\n{p[2]}\njump endif{jump_count}\nelse{jump_count}:\n{p[4]}endif{jump_count}:\n{p[6]}'

###########################################################################################################################################################################################

# condiconal : MAIOR 
#            | MENOR 

def p_6_maior (p):
    '''condicional  : MAIOR '''
    global stack_size
    stack_size -=2
    if stack_size < 0: 
            p[0] =  f"Error: Stack Vazia"
    p[0] =   f"sup\n"
    stack_size +=1
    print("condicional  : MAIOR ")

def p_6_menor (p):
    '''condicional  : MENOR '''
    global stack_size
    stack_size -=2
    if stack_size < 0: 
            p[0] =  f"Error: Stack Vazia"
    p[0] =   f"inf\n"
    stack_size +=1
    print("condicional  : MENOR ")

###########################################################################################################################################################################################

#str : CHAR 
#    | SPACE
#    | CR 
#    | STRING
#    | EMIT

def p_7_CHAR(p):
    '''str : CHAR'''
    parts = p[1].split(' ', 1)
    if len(parts) >= 2:
        # Pegar o primeiro item
        command = parts[0]
        value = parts[1] if len(parts) >= 2 else None
        parts2 = value.split(' ', 1)
        p[0] = f'pushs "{parts2[0]}"\nCHRCODE'

def p_7_SPACE(p): 
    '''str : SPACE'''
    p[0] = f'pushs " "\n'
    print("exp_char : SPACE")

def p_7_CR(p): 
    '''str : CR'''
    p[0] = f'writeln\n'

def p_7_STR(p): 
    '''str : STRING '''
    p[0] = f'pushs "{p[1]}"\n'

def p_7_EMIT(p): 
    '''str : EMIT'''
    global stack_size
    stack_size -=1
    if stack_size < 0: 
            p[0] =  f"Error: Stack Vazia"
    p[0] = f'WRITECHR\n'

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
    entrada = input("Pressa 's' para terminar:\n>")
    entrada =  re.sub(r'\s{2,}', ' ', entrada)

    if entrada.lower() == 's':
        break
    if entrada.lower() == 'c': 
        imprimir_questionario(dic)
    else:
        conversor = parser.parse(entrada)
    
    n_variaveis = 0


