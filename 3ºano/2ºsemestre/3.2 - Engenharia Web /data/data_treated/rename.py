import os
import unidecode

def rename_to_utf8(directory):
    for filename in os.listdir(directory):
        # Define o caminho completo do arquivo
        file_path = os.path.join(directory, filename)
        
        # Verifica se é um arquivo
        if os.path.isfile(file_path):
            try:
                # Tenta decodificar o nome do arquivo para UTF-8
                new_filename = filename.encode('utf-8').decode('utf-8')
            except UnicodeDecodeError:
                # Caso ocorra um erro de decodificação, usa unidecode para converter o nome
                new_filename = unidecode.unidecode(filename)
            
            # Define o novo caminho completo do arquivo
            new_file_path = os.path.join(directory, new_filename)
            
            # Renomeia o arquivo
            os.rename(file_path, new_file_path)
            print(f'Renamed: {file_path} -> {new_file_path}')

# Diretório das imagens antigas e novas
old_images_dir = '/home/cid34senhas/Desktop/EW_tp/data/data_treated/atual'
new_images_dir = '/home/cid34senhas/Desktop/EW_tp/data/data_treated/atuais'

# Renomeia os arquivos em ambos os diretórios
rename_to_utf8(old_images_dir)
rename_to_utf8(new_images_dir)
