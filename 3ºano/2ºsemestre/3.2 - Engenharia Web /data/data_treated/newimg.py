import json
import os
import re

# Função para extrair o ID e o nome da rua do nome do arquivo
def extrair_detalhes_imagem(nome_arquivo):
    padrao = r"(\d+)-(.+?)-(.+)\.(jpg|jpeg|png|JPG|JPEG|PNG)"
    correspondencia = re.match(padrao, nome_arquivo)
    if correspondencia:
        id_rua, nome_rua, vista, extensao = correspondencia.groups()
        return id_rua, nome_rua.replace("-", " "), vista
    return None, None, None

# Caminho para a pasta de imagens e arquivo JSON
caminho_imagens = '/home/cid34senhas/Desktop/EW_tp/frontend/public/atual'  # Atualize este caminho conforme necessário
arquivo_json = '/home/cid34senhas/Desktop/EW_tp/data/data_treated/ruas.json'  # Atualize este caminho conforme necessário

# Carregar dados do arquivo JSON
print("Carregando dados do arquivo JSON...")
with open(arquivo_json, 'r', encoding='utf-8') as f:
    dados = json.load(f)

# Processar cada imagem na pasta
print("Processando imagens...")
for nome_arquivo in os.listdir(caminho_imagens):
    print(f"Processando arquivo: {nome_arquivo}")
    id_rua, nome_rua, vista = extrair_detalhes_imagem(nome_arquivo)
    if id_rua and nome_rua and vista:
        print(f"Detalhes extraídos - ID: {id_rua}, Nome da Rua: {nome_rua}, Vista: {vista}")
        for rua in dados:
            if rua['rua'] == id_rua:
                nova_figura = {
                    "id": f"{id_rua}-{vista}",
                    "imagem": os.path.join(caminho_imagens, nome_arquivo),
                    "legenda": f"{nome_rua} - vista {vista}."
                }
                if 'figuras' not in rua:
                    rua['figuras'] = []
                rua['figuras'].append(nova_figura)
                print(f"Adicionada figura {nova_figura} à rua {rua['nome']}")
            else:
                print(f"Rua não encontrada para o ID: {id_rua} e Nome: {nome_rua}")
    else:
        print(f"Detalhes não extraídos para o arquivo: {nome_arquivo}")

# Salvar dados atualizados no arquivo JSON
print("Salvando dados atualizados no arquivo JSON...")
with open(arquivo_json, 'w', encoding='utf-8') as f:
    json.dump(dados, f, ensure_ascii=False, indent=4)

print("Figuras adicionadas com sucesso ao arquivo JSON.")
