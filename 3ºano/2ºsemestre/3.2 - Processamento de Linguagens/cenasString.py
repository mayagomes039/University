elif val_token == 'CR':
                vmCode += f"writeln\n"

elif val_token == 'SPACE':
    vmCode += f"pushs " "\n"

elif val_token == 'SPACES':
    input_n_spaces = 10
    espacos = ""
    for _ in range(input_n_spaces):
        espacos += " "

    vmCode += f'pushs "{espacos}"\nwrites'
    

elif val_token == 'CHAR':
        vmCode += f'pushs #INPUT DO CHAR# \nCHRCODE'

elif val_token == 'STR':
    vmCode += f"pushs #VALOR APPANHADO PELO INPUT NA EXPRESSÃO REGULAR#\n"                
