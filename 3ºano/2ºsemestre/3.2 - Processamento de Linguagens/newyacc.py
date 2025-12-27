from Lexnew import tokens, lexer,re
import ply.yacc as yacc
from funcao_helper import parse_funcao

#Z : exp '$'
#
#
#
#
#
#exp : NUM
#    | NUM exp
#    | exp_res exp
#    | exp2
#    | FUNCAO_RAW exp
#    | FUNCAO_RAW
#    | exp3
#    | exp_char
#
#
#exp_res : op_res
#
#
#op1 : SOMA
#    | SUBTRACAO
#    | MULT
#    | DIV
#    | MOD
#    | SWAP
#    | 2DROP
#
#
#op1_1 : DIVMOD
#op1_2 : 2DUP 
#op1_3 : OVER
#
#
#op2 : DUP 
#    | PONTO 
#    | DROP 
#
#
#op3 : 2OVER
#op3_1 : 2SWAP
#
#
#op4 : ROT
#
# exp2  : exp_op1 op1
#       | exp_op1 op1_1
#       | exp_op1 op1_2
#       | exp_op1 op1_3
#       | exp_op2 op2
#       | exp_op3 op3 
#       | exp_op4 op4
#       | exp_op3 op3_1
#       | exp2 op2    
#
#exp3 : exp FUNCAO
#
#exp_char : SPACE
#         | SPACES
#         | CHAR
#         | CR 
#         | STR 
#
#op_res : exp_op1 op1
#       | exp_op1 op1_1
#       | exp_op1 op1_2
#       | exp_op1 op1_3
#       | exp_op2 op2
#       | exp_op3 op3 
#       | exp_op3 op3_1
#       | exp_op4 op4
#       | exp2 op2    
#
#exp_op1 : NUM NUM
#        | NUM op_res
#        | exp_res NUM
#        | exp_res exp_res
#
#exp_op2 : NUM
#        | exp_res
#        | CHAR
#
#exp_op3 : NUM NUM NUM NUM
#        | exp_res exp_res exp_res exp_res
#        | NUM exp_res exp_res exp_res
#        | NUM NUM exp_res exp_res
#        | NUM exp_res NUM exp_res
#        | NUM exp_res exp_res NUM
#        | exp_res NUM NUM exp_res
#        | exp_res exp_res NUM NUM
#        | exp_res NUM exp_res NUM
#        | NUM NUM NUM exp_res
#        | exp_res NUM NUM NUM
#        | NUM exp_res NUM NUM
#        | NUM NUM exp_res NUM
#        | exp_res NUM exp_res exp_res
#        | exp_res exp_res NUM exp_res
#        | exp_res exp_res exp_res NUM
#
#    
#exp_op4 : NUM NUM NUM
#        | exp_res exp_res NUM
#        | exp_res NUM exp_res
#        | NUM exp_res exp_res
#        | NUM exp_res NUM
#        | exp_res NUM NUM
#        | NUM NUM exp_res
#        | exp_res exp_res exp_res
# 
#
#
#
#
###########################################################################################################################################################################################

#SETERS 

dic={}
n_variaveis  = 0 
storeg_counter = 0 
existe_funcao = False 





###########################################################################################################################################################################################

def p_1(p): 
    'Z : exp'
    print("0")
    print("\n\n")
    
    global n_variaveis
    global existe_funcao
    global salvar
    global dic
    print( "numero de variaveis", n_variaveis)
    if n_variaveis>0  : 
        codigo = ""
        for i in range(n_variaveis):
            codigo = codigo + ("pushi 0\n")
            vmcode= codigo + f'start\n{p[1]}\nstop\n'
    else: 
            vmcode = f'start\n{p[1]}\nstop\n'
    funcao_code = " "
    for value_set in dic.values():
        for value in value_set:
            funcao_code += value.strip("{}'") + "\n"
    p[0] = vmcode + "\n\n"+ funcao_code
    print(p[0])
        

##########################################################################################################################################################################################

#exp : NUM
#    | NUM exp
#    | exp_res exp
#    | exp2
#    | FUNCAO_RAW exp
#    | FUNCAO_RAW
#    | FUNCAO 
#    | exp_char
#    | CHAR 


def p_exp_num (p): 
    ' exp : NUM '
    p[0] = f'pushf {p[1]}\n'
    print("exp : num")


def p_exp_num_exp(p): 
    '''exp : NUM exp'''
    p[0] = f'pushf {p[1]} \n{p[2]}'
    
    print("exp : NUM exp")

def p_exp_exp_res_exp(p): 
    '''exp : exp_res exp '''
    p[0] = f'{p[1]}\n{p[2]} '
    print("exp: exp_res exp")


def p_exp_exp2(p): 
    '''exp : exp2''' 
    p[0] = f'{p[1]}'
    print("exp : exp2")

def p_exp_FUNCAO_RAW(p): 
    'exp : FUNCAO_RAW'
    parts = p[1].split(' ', 2)  
    if len(parts) > 1:
        id_func= parts[1]  
        func_rules = parts[2][:-1]
    vmcode,numero = parse_funcao(func_rules)
    global n_variaveis 
    n_variaveis = numero
    a = f"{id_func}:\n {vmcode}\nreturn" 
    global dic 
    dic[id_func]={a}
    print("exp : funcao raw")


def p_exp_FUNCAO_RAW_exp(p): 
    'exp : FUNCAO_RAW exp'
    parts = p[1].split(' ', 2)  
    if len(parts) > 1:
        id_func= parts[1]  
        func_rules = parts[2][:-1]
    vmcode,numero = parse_funcao(func_rules)
    global n_variaveis 
    n_variaveis = numero
    a = f"{id_func}:\n  {vmcode}\nreturn" 
    global dic 
    dic[id_func]=a
    global salvar 
    salvar = a 
    print("exp : funcao raw exp " )



def p_exp_char (p): 
    'exp : CHAR '
    parts = p[1].split(' ', 1)
    if len(parts) >= 2:
        # Pegar o primeiro item
        command = parts[0]
        value = parts[1] if len(parts) >= 2 else None
        parts2 = value.split(' ', 1)
        p[0] = f'pushs "{parts2[0]}"\nCHRCODE'
        
def p_exp3 (p):
    'exp : exp3'
    p[0] = p[1]
    print("exp exp3")

def p_exp1 (p):
    'exp3 :  exp FUNCAO '
    p[0] = f'{p[1]} pusha {p[2]}\ncall'
    print("exp exp FUNCAO")

##########################################################################################################################################################################################

#exp_char : SPACE
#         | CR 
#         | STR 

def p_exp_char_cr (p): 
    'exp_char : CR '
    p[0] = f'writeln'

def p_exp_char_space (p): 
    'exp_char : SPACE '
    p[0] = f'pushs " "'
    print("exp_char : SPACE")

def p_exp_char_char (p): 
    'exp_char : CHAR '
    parts = p[1].split(' ', 1)
    if len(parts) == 2:
        p[0] = f'pushs "{parts[1]}"\nCHRCODE'




##########################################################################################################################################################################################
#exp_res : op_res

def p_exp_res_op_res(p): 
    ''' exp_res : op_res '''
    p[0] = f'{p[1]} '
    print("exp_res : op_res")


##########################################################################################################################################################################################

#op1 : SOMA
#    | SUBTRACAO
#    | MULT
#    | DIV
#    | MOD 
#    | SWAP
#    | 2DROP

def p_op1_soma(p): 
    'op1 : SOMA'
    p[0] = f'add'
    print("op soma")

def p_op1_sub(p): 
    'op1 : SUBTRACAO'
    p[0] = f'sub'

    print("op sub")

def p_op1_mult(p): 
    'op1 : MULT'
    p[0] = f'mul'
    print("op mul")

def p_op1_div(p): 
    'op1 : DIV'
    p[0] = f'div\nftoi'
    print("op div")

def p_op1_mod(p): 
    'op1 : MOD'
    p[0] = f'mod '
    print("op mod")

def p_op1_swap(p): 
    'op1 : SWAP'
    p[0] = f'swap '
    print("op swap")

def p_op1_2drop(p): 
    'op1 : 2DROP'
    p[0] = f'pop 2 '
    print("op1_4 drop")



##########################################################################################################################################################################################

#op1_1 : DIVMOD
#op1_2 : 2DUP 
#op1_3 : OVER

def p_op1_1divmod(p): 
    'op1_1 : DIVMOD'
    p[0] = f'mod'
    print("op divmod")

def p_op1_2dup(p): 
    'op1_2 : 2DUP'
    p[0] = f'dup 1'
    print("op dup")

def p_op1_3over(p): 
    'op1_3 : OVER'
    p[0] = f'over?'
    print("op over")



##########################################################################################################################################################################################

#op2 : DUP 
#    | PONTO 
#    | DROP
#    | SPACES    
#    

def p_op2_dup (p):
    'op2 : DUP'
    p[0] = f'dup 1'
    print("op2 dup")

def p_op2_PONTO (p):
    'op2 : PONTO'
    p[0] = f'writei\nwriteln'
    print("op2 ponto")

def p_op2_drop (p): 
    'op2 : DROP'
    p[0] = f'pop 1'
    print("op2 drop")

def p_op2_1spaces (p): 
    'op2_1 : SPACES'
    p[0] = p[1]
    print("op2_1 SPACES")

def p_op2_2emit (p): 
    'op2_2 : EMIT'
    p[0] = p[1]
    print("op2_2 EMIT")

##########################################################################################################################################################################################

#op3 : 2OVER
#op3_1 : 2SWAP

def p_op3_2over (p): 
    'op3 : 2OVER'
    p[0] = f'2over'
    print("op3 2over")


def p_op3_1_2swap (p): 
    'op3_1 : 2SWAP'
    p[0] = f'2swap'
    print("op3 2swap")

##########################################################################################################################################################################################

#op4 : ROT

def p_op4_rot (p): 
    'op4 : ROT'
    p[0] = f'rot'
    print("op4 rot")


##########################################################################################################################################################################################

#  exp2 : exp_op1 op1
#       | exp_op1 op1_1
#       | exp_op1 op1_2
#       | exp_op1 op1_3
#       | exp_op1 exp_op1 op3
#       | exp_op3 op3 
#       | exp_op3 op3_1
#       | exp_op4 op4
#       | exp2 op2   

def p_exp2_op1(p): 
    '''exp2 : exp_op1 op1 '''
    p[0] = f'{p[1]}\n{p[2]} '
    print("exp2 : exp_op1 op1")


def p_exp2_op1_1(p): 
    '''exp2 : exp_op1 op1_1 '''

    p[0] = f'{p[1]}\n{p[2]}\n{p[1]}\ndiv\nftoi'

    print("exp2 : exp_op1 op1_1")

def p_exp_2_op1_2(p): 
    '''exp2 : exp_op1 op1_2 '''

    if p[1]:  
        parts = p[1].split('\n', 1)  
        if len(parts) == 2:  
            p[0] = f'{parts[0]}\n{parts[1]}\n{parts[0]}\n{parts[1]}'

    print("exp2 : exp_op1 op1_2 ")

def p_exp_2_op1_3(p): 
    '''exp2 : exp_op1 op1_3 '''    
    global storeg_counter
    res = f'\nstoreg {storeg_counter}\nstoreg {storeg_counter +1}\npushg {storeg_counter +1}\npushg {storeg_counter}\npushg {storeg_counter +1}'
    p[0] = p[1] + res 
    print("exp2: exp1 op1_3 over")

def p_exp_2_exp_op1_exp_op1_op3(p): 
    '''exp2 : exp_op3 op3 '''    
    global storeg_counter
    res = f'\nstoreg {storeg_counter}\nstoreg {storeg_counter +1}\nstoreg {storeg_counter +2}\nstoreg {storeg_counter +3}\npushg {storeg_counter +3}\npushg {storeg_counter +2}\npushg {storeg_counter +1}\npushg {storeg_counter}\npushg {storeg_counter +3}\npushg {storeg_counter +2}'
    p[0] = p[1] + res 
    print("exp2: exp_op3 2over")


def p_exp_op1_exp_op1_op3_1(p): 
    '''exp2 : exp_op3 op3_1 '''    
    global storeg_counter
    res = f'\nstoreg {storeg_counter}\nstoreg {storeg_counter +1}\nstoreg {storeg_counter +2}\nstoreg {storeg_counter +3}\npushg {storeg_counter +1}\npushg {storeg_counter}\npushg {storeg_counter +3}\npushg {storeg_counter +2}'
    p[0] = p[1] + res 
    print("exp2 : exp_op3_1 2swap")

def p_exp_op4_op4(p): 
    '''exp2 : exp_op4 op4 '''    
    global storeg_counter
    res = f'\nstoreg {storeg_counter}\nstoreg {storeg_counter +1}\nstoreg {storeg_counter +2}\npushg {storeg_counter+1}\npushg {storeg_counter + 0 }\npushg {storeg_counter +2}'
    p[0] = p[1] + res 
    print("exp2 : exp_op4 op4 ")


def p_exp2_op2(p): 
    '''exp2 : exp_op2 op2 '''

    p[0] = f'{p[1]}\n{p[2]} '
    print("exp2 : exp_op2 op2")    

def p_exp2_op2_1(p): 
    '''exp2 : exp_op2 op2_1 '''
    a = p[1]

    a = int(p[1].split()[-1])

    espacos = ""
    for _ in range(a):
        espacos += " "


    p[0] = f'pushs "{espacos}"\nwrites'
    print("exp2 : exp_op2 op2")    

def p_exp2_op2_2(p): 
    '''exp2 : exp_op2 op2_2 '''
    a = p[1]
    a1 = str(a)
    if a1.isdigit():
        p[0] = f'{p[1]}\nwritef'
    else: 
        p[0] = f'{p[1]}\nwrites'
    print("exp2 : exp_op2 op2_2")  




##########################################################################################################################################################################################
#op_res : exp_op1 op1
#       | exp_op1 op1_1
#       | exp_op1 op1_2
#       | exp_op1 op1_3
#       | exp_op1 exp_op1 op3
#       | exp_op3 op3 
#       | exp_op3 op3_1
#       | exp_op4 op4
#       | exp2 op2

def p_exp_op1_op1(p): 
    '''op_res : exp_op1 op1 '''    
    p[0] = f'{p[1]}\n{p[2]} '
    print("op_res: exp1 op1")

def p_exp_op1_op1_1(p): 
    '''op_res : exp_op1 op1_1 '''    
    p[0] = f'{p[1]}\n{p[2]}\n{p[1]}\ndiv\nftoi'
    print("op_res: exp1 op1_1 divmod")

def p_exp_op1_op1_2(p): 
    '''op_res : exp_op1 op1_2 '''    
    if p[1]:  
        parts = p[1].split('\n', 1)  
        if len(parts) == 2:  
            p[0] = f'{parts[0]}\n{parts[1]}\n{parts[0]}\n{parts[1]}'
    print("op_res: exp1 op1_2 2dup")

def p_exp_op1_op1_3(p): 
    '''op_res : exp_op1 op1_3 '''    
    global storeg_counter
    res = f'\nstoreg {storeg_counter}\nstoreg {storeg_counter +1}\npushg {storeg_counter +1}\npushg {storeg_counter}\npushg {storeg_counter +1}'
    p[0] = p[1] + res 
    print("op_res: exp1 op1_3 over")

def p_exp_op1_exp_op1_op3(p): 
    '''op_res : exp_op3 op3 '''    
    global storeg_counter
    res = f'\nstoreg {storeg_counter}\nstoreg {storeg_counter +1}\nstoreg {storeg_counter +2}\nstoreg {storeg_counter +3}\npushg {storeg_counter +3}\npushg {storeg_counter +2}\npushg {storeg_counter +1}\npushg {storeg_counter}\npushg {storeg_counter +3}\npushg {storeg_counter +2}'
    p[0] = p[1] + res 
    print("op_res : exp_op3 2over")

def p_op_res_exp_op3_op3_1(p): 
    '''op_res : exp_op3 op3_1 '''    
    global storeg_counter
    res = f'\nstoreg {storeg_counter}\nstoreg {storeg_counter +1}\nstoreg {storeg_counter +2}\nstoreg {storeg_counter +3}\npushg {storeg_counter +1}\npushg {storeg_counter}\npushg {storeg_counter +3}\npushg {storeg_counter +2}'
    p[0] = p[1] + res 
    print("op_res : exp_op3_1 2swap")

def p_op_res_exp_op4_op4(p): 
    '''op_res : exp_op4 op4 '''    
    global storeg_counter
    res = f'\nstoreg {storeg_counter}\nstoreg {storeg_counter +1}\nstoreg {storeg_counter +2}\npushg {storeg_counter+1}\npushg {storeg_counter + 0 }\npushg {storeg_counter +2}'
    p[0] = p[1] + res 
    print("op_res : exp_op4 op4 ")

def p_exp_op2_op2(p):
    '''op_res : exp_op2 op2 ''' 
    p[0] = f'{p[1]}\n{p[2]} '
    print("op_res exp_op2 op2")


def p_op_res_op2_1(p): 
    '''op_res : exp_op2 op2_1 '''
    a = ""
    c = int(p[1])
    for i in range(c + 1):
        a = a + " "
    p[0] = a
    print("op_res : exp_op2 op2")   

def p_op_res_op2_2(p): 
    '''op_res : exp_op2 op2_2 '''
    a = p[1]
    a1 = str(a)
    if a1.isdigit():
        p[0] = f'{p[1]}\nwritef'
    else: 
        p[0] = f'{p[1]}\nwrites'
    print("op_res : exp_op2 op2_2") 

 

##########################################################################################################################################################################################

#exp_op1 : NUM NUM
#        | NUM op_res
#        | exp_res NUM
#        | exp_res exp_res

def p_4_exp_op1(p): 
    '''exp_op1 : NUM NUM              '''
    p[0] = f' pushf {p[1]}\n pushf {p[2]} '

    print("exp_op1 NUM NUM ")


def p_4_exp_op1_2(p): 
    '''exp_op1 : NUM op_res'''
    p[0] = f' pushf {p[1]} \n{p[2]}'
    print("exp_op1 NUM op_res")

def p_4_exp_op1_23(p): 
    '''exp_op1 : exp_res NUM'''
    p[0] = f'{p[1]} \n pushf {p[2]}'
    print("exp_op1 exp_res NUM ")

def p_4_exp_op1_24(p): 
    '''exp_op1 : exp_res exp_res'''
    p[0] = f'{p[1]}\n{p[2]} '
    print("exp_op1 exp_res exp_res ")

##########################################################################################################################################################################################

#exp_op2 : NUM
#        | exp_res
#        | CHAR


def p_exp_op2 (p): 
    '''exp_op2 : NUM '''
    p[0] = f'pushf {p[1]} '
    print(p[0])
    print("exp_op2 : NUM")

def p_exp_op2_exp (p): 
    '''exp_op2 : exp_res '''
    p[0] = f'{p[1]} '
    print("exp_op2 : exp_res")

def p_exp_op2_char (p):
    '''exp_op2 : CHAR '''
    parts = p[1].split(' ', 1)
    if len(parts) == 2:
        p[0] = f'pushs "{parts[1]}"\nCHRCODE'
    print("exp_op2_2 : CHAR")

    
##########################################################################################################################################################################################

#exp_op3 : NUM NUM NUM NUM
#        | exp_res exp_res exp_res exp_res
#        | NUM exp_res exp_res exp_res
#        | NUM NUM exp_res exp_res
#        | NUM exp_res NUM exp_res
#        | NUM exp_res exp_res NUM
#        | exp_res NUM NUM exp_res
#        | exp_res exp_res NUM NUM
#        | exp_res NUM exp_res NUM
#        | NUM NUM NUM exp_res
#        | exp_res NUM NUM NUM
#        | NUM exp_res NUM NUM
#        | NUM NUM exp_res NUM
#        | exp_res NUM exp_res exp_res
#        | exp_res exp_res NUM exp_res
#        | exp_res exp_res exp_res NUM


def p_exp_op3(p): 
    '''exp_op3 : NUM NUM NUM NUM'''
    p[0] = f' pushf {p[1]}\n pushf {p[2]}\n pushf {p[3]}\n pushf {p[4]}'
    print("exp_op3 NUM NUM NUM NUM")

def p_exp_op3_1(p): 
    '''exp_op3 : exp_res exp_res exp_res exp_res'''
    p[0] = f'{p[1]}\n{p[2]}\n{p[3]}\n{p[4]}'
    print("exp_op3 exp_res exp_res exp_res exp_res")

def p_exp_op3_2(p): 
    '''exp_op3 : NUM exp_res exp_res exp_res'''
    p[0] = f' pushf {p[1]}\n{p[2]}\n{p[3]}\n{p[4]}'
    print("exp_op3 NUM exp_res exp_res exp_res")

def p_exp_op3_3(p): 
    '''exp_op3 : NUM exp_res NUM exp_res'''
    p[0] = f' pushf {p[1]}\n{p[2]}\n pushf {p[3]}\n{p[4]}'
    print("exp_op3 NUM exp_res NUM exp_res")

def p_exp_op3_4(p): 
    '''exp_op3 : NUM NUM exp_res exp_res'''
    p[0] = f' pushf {p[1]}\n pushf {p[2]}\n{p[3]}\n{p[4]}'
    print("exp_op3 NUM NUM exp_res exp_res")

def p_exp_op3_5(p): 
    '''exp_op3 : NUM NUM NUM exp_res'''
    p[0] = f' pushf {p[1]}\n pushf {p[2]}\n pushf {p[3]}\n{p[4]}'
    print("exp_op3 NUM NUM NUM exp_res")

def p_exp_op3_6(p): 
    '''exp_op3 : exp_res NUM NUM NUM'''
    p[0] = f'{p[1]}\n pushf {p[2]}\n pushf {p[3]}\n pushf {p[4]}'
    print("exp_op3 exp_res NUM NUM NUM")

def p_exp_op3_7(p): 
    '''exp_op3 : exp_res exp_res NUM NUM'''
    p[0] = f'{p[1]}\n{p[2]}\n pushf {p[3]}\n pushf {p[4]}'
    print("exp_op3 exp_res exp_res NUM NUM")

def p_exp_op3_8(p): 
    '''exp_op3 : exp_res NUM exp_res NUM'''
    p[0] = f'{p[1]}\n pushf {p[2]}\n{p[3]}\n pushf {p[4]}'
    print("exp_op3 exp_res NUM exp_res NUM")

def p_exp_op3_9(p): 
    '''exp_op3 : NUM exp_res NUM NUM'''
    p[0] = f' pushf {p[1]}\n{p[2]}\n pushf {p[3]}\n pushf {p[4]}'
    print("exp_op3 NUM exp_res NUM NUM")

def p_exp_op3_10(p): 
    '''exp_op3 : NUM NUM exp_res NUM'''
    p[0] = f' pushf {p[1]}\n pushf {p[2]}\n{p[3]}\n pushf {p[4]}'
    print("exp_op3 NUM NUM exp_res NUM")

def p_exp_op3_11(p): 
    '''exp_op3 : exp_res NUM exp_res exp_res'''
    p[0] = f'{p[1]}\n pushf {p[2]}\n{p[3]}\n{p[4]}'
    print("exp_op3 exp_res NUM exp_res exp_res")

def p_exp_op3_12(p): 
    '''exp_op3 : NUM exp_res exp_res NUM'''
    p[0] = f' pushf {p[1]}\n {p[2]}\n{p[3]}\n pushf {p[4]}'
    print("exp_op3 NUM exp_res exp_res NUM")

def p_exp_op3_13(p): 
    '''exp_op3 : exp_res NUM NUM exp_res'''
    p[0] = f' {p[1]}\n pushf {p[2]}\n pushf{p[3]}\ {p[4]}'
    print("exp_op3 exp_res NUM NUM exp_res")

def p_exp_op3_14(p): 
    '''exp_op3 : exp_res  exp_res NUM exp_res'''
    p[0] = f'{p[1]}\n {p[2]}\n pushf{p[3]}\n{p[4]}'
    print("exp_op3 exp_res  exp_res NUM exp_res")

def p_exp_op3_15(p): 
    '''exp_op3 : exp_res  exp_res  exp_res NUM '''
    p[0] = f'{p[1]}\n {p[2]}\n{p[3]}\n pushf{p[4]}'
    print("exp_op3 exp_res  exp_res  exp_res NUM")

##########################################################################################################################################################################################

#exp_op4 : NUM NUM NUM
#        | exp_res NUM NUM
#        | exp_res exp_res NUM
#        | exp_res NUM exp_res
#        | NUM exp_res exp_res
#        | NUM exp_res NUM
#        | NUM NUM exp_res
#        | exp_res exp_res exp_res


def p_exp_op4_1(p): 
    '''exp_op4 : NUM NUM NUM '''
    p[0] = f'\n pushf {p[1]}\n pushf {p[2]}\n pushf {p[3]}'
    print("exp_op4 NUM NUM NUM")

def p_exp_op4_2(p): 
    '''exp_op4 : exp_res NUM NUM '''
    p[0] = f'{p[1]}\n pushf {p[2]}\n pushf {p[3]}'
    print("exp_op4 exp_res NUM NUM")

def p_exp_op4_3(p): 
    '''exp_op4 : exp_res exp_res NUM '''
    p[0] = f'{p[1]} {p[2]}\n pushf {p[3]}'
    print("exp_op4 exp_res exp_res NUM")

def p_exp_op4_4(p): 
    '''exp_op4 : exp_res NUM exp_res '''
    p[0] = f'{p[1]}\n pushf {p[2]}\n{p[3]}'
    print("exp_op4 exp_res NUM exp_res")

def p_exp_op4_5(p): 
    '''exp_op4 : exp_res exp_res exp_res '''
    p[0] = f'{p[1]} {p[2]} {p[3]}'
    print("exp_op4 exp_res exp_res exp_res")

def p_exp_op4_6(p): 
    '''exp_op4 : NUM exp_res NUM '''
    p[0] = f'\n pushf {p[1]}\n{p[2]}\n pushf {p[3]}'
    print("exp_op4 NUM exp_res NUM")

def p_exp_op4_7(p): 
    '''exp_op4 : NUM NUM exp_res '''
    p[0] = f'\n pushf {p[1]}\n pushf {p[2]}\n{p[3]}'
    print("exp_op4 NUM NUM exp_res")

def p_exp_op4_8(p): 
    '''exp_op4 : NUM exp_res exp_res '''
    p[0] = f'\n pushf {p[1]}\n{p[2]} {p[3]}'
    print("exp_op4 NUM exp_res exp_res")


##########################################################################################################################################################################################


def p_error(p):
    print("Erro sintático no input!")
    print(p)

parser = yacc.yacc()

#s = '''
#AVERAGE '''
#result = parser.parse(s)


def imprimir_questionario(dicionario):
    if not dic: 
        print("não ha dic ")
    for chave, valor in dicionario.items():
        print(f"Pergunta: {chave}")
        print(f"Resposta: {valor}")
        print()


def verificar_tokens(entrada):
    lexer.input(entrada)
    tokens_funcao = []
    print(entrada)
    while True:
        token = lexer.token()
        print(token)
        if not token:
            break  
        if token.type == 'FUNCAO_RAW':
            tokens_funcao.append(token.value)
    return tokens_funcao


def verificar_variaveis(entrada):
    lexer.input(entrada)
    global n_variaveis
    
    while True:
        token = lexer.token()
        if not token:
            break  
        if token.type == '2SWAP' or token.type == '2DUP' or  token.type == 'OVER':
            n_variaveis += 2
        elif token.type == 'ROT':
            n_variaveis += 3
        elif token.type == '2OVER':
            n_variaveis += 4 
    return n_variaveis


def verificar_existe_funcao(entrada):
    lexer.input(entrada)
    global existe_funcao
    while True:
        token = lexer.token()
        if not token:
            break  
        if token.type == 'FUNCAO':
            existe_funcao = True
    return 


while True:
    entrada = input("Digite uma expressão (ou 's' para terminar): ")
    entrada =  re.sub(r'\s{2,}', ' ', entrada)
    if entrada.lower() == 's':
        break
    tokens_na_entrada = verificar_tokens(entrada)
    tokens_variaveis = verificar_variaveis(entrada)
    verificar_existe_funcao(entrada)

    print("V" , tokens_variaveis)

    print("Tokens na entrada:", tokens_na_entrada)
    if tokens_na_entrada: 
        for func in tokens_na_entrada:
            pre = parser.parse(func)
    else:
        conversor = parser.parse(entrada)
        n_variaveis = 0 

    existe_funcao = False
    print()
    imprimir_questionario(dic)
    print("#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#")

print("Fim do programa.")


# TODO  mensagens de erro quando se quer dar print f e nao existem elementos na stack,