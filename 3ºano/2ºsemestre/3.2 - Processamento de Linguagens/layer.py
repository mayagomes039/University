vmCode  = ""

stack_size = 0 

def layer(yacc_list,dic):

    global vmCode
    global stack_size
    for elemento in yacc_list:
            if elemento.isdigit():
                stack_size +=1
                vmCode += f"pushf {elemento}\n"
            elif elemento == '+':
                stack_size -=2
                if stack_size < 0: 
                    vmCode = "Error: Stack Vazia"
                vmCode += f"add\n"
                stack_size +=1
            elif elemento == '-':
                stack_size -=2
                if stack_size < 0: 
                    vmCode = "Error: Stack Vazia"
                vmCode += f"sub\n"
                stack_size +=1
            elif elemento == '*':
                stack_size -=2
                if stack_size < 0: 
                    vmCode = "Error: Stack Vazia"
                vmCode += f"mul\n"
                stack_size +=1
            elif elemento == '/':
                stack_size -=2
                if stack_size < 0: 
                    vmCode = "Error: Stack Vazia"
# TODO confirmar a divisao por 0 
                vmCode += f"div\nftoi\n"
                stack_size +=1
            elif elemento == 'mod':
                vmCode += f"mod\n"
            elif elemento == 'swap':
                vmCode += f"swap\n"
            elif elemento == 'drop':
                vmCode += f"pop\n"
            elif elemento == '2drop':
                vmCode += f"pop 2\n"
            elif elemento == "" or elemento == "funcdef"  : 
                pass
            elif elemento.startswith('FUNCAO:'):
                elem = elemento.split(":")
                nome_funcao = elem[1]
                if nome_funcao in dic: 
                    func_list = []
                    funcao_raw = dic[nome_funcao]
                    funcao_raw_str = str(funcao_raw)
                    funcao_str = funcao_raw_str.strip("{} '")
                    funcao = funcao_str.split(" ")
                    func_list.extend(funcao)
                    layer(func_list, dic)
                else: 
                    vmCode = "Error: Func not defined"
            elif elemento == ">": 
                stack_size -=2
                if stack_size < 0: 
                    vmCode = "Error: Stack Vazia"
                vmCode += f"sup\n"
                stack_size +=1
            elif elemento == "<": 
                stack_size -=2
                if stack_size < 0: 
                    vmCode = "Error: Stack Vazia"
                vmCode += f"inf\n"
                stack_size +=1
            elif elemento.startswith('if->'):
                elem = elemento.split("->")
                else_statement = elem[1]
                vmCode += "jz if1\n"
                layer (else_statement, dic)
                vmCode+= "jump endif1\n"
            elif elemento.startswith('else->'):
                elem = elemento.split("->")
                else_statement = elem[1]
                vmCode += "if1:\n"
                layer (else_statement, dic)
            elif elemento.startswith('then->'):
                elem = elemento.split("->")
                else_statement = elem[1]
                vmCode += "endif1:\n"
                layer (else_statement, dic)
                vmCode+= "stop\n"
            elif elemento.startswith('then'):
                vmCode += "endif1:\nstop\n"


# TODO descubrir como se faz o igual
            else: 
                vmCode = "ERROR "

    return vmCode
    