import json
import xml.etree.ElementTree as ET
import os

# Função para processar os detalhes de lugares, entidades e datas
def process_lugares_entidades_datas(text_files_path):
    lugares = []
    entidades = []
    datas = set()

    # Processar os ficheiros XML na pasta /texto
    for file_name in os.listdir(text_files_path):
        if file_name.endswith('.xml'):
            file_path = os.path.join(text_files_path, file_name)
            tree = ET.parse(file_path)
            root = tree.getroot()

            # Processar elementos <lugar>
            for lugar in root.findall('.//lugar'):
                lugar_dict = {
                    "nome": lugar.text,
                    "posição": f"l{len(lugares) + 1}"
                }
                if lugar_dict not in lugares:  # Verificar se o lugar já existe na lista
                    lugares.append(lugar_dict)

            # Processar elementos <entidade>
            for entidade in root.findall('.//entidade'):
                entidade_dict = {
                    "tipo": entidade.attrib.get('tipo', 'N/A'),
                    "nome": entidade.text,
                    "posição": f"e{len(entidades) + 1}"
                }
                if entidade_dict not in entidades:  # Verificar se a entidade já existe na lista
                    entidades.append(entidade_dict)

            # Processar elementos <data>
            for data_element in root.findall('.//data'):
                data_nome = data_element.text
                if data_nome not in datas:  # Verificar se a data já existe no conjunto
                    datas.add(data_nome)

    # Converter o conjunto de datas de volta para uma lista
    datas_list = [{"nome": data_nome, "posição": f"d{i+1}"} for i, data_nome in enumerate(datas)]

    return lugares, entidades, datas_list


# Função para processar o índice de ruas e os detalhes das ruas
def process_ruas(indice_ruas_path, text_files_path):
    ruas = []

    # Processar os ficheiros XML na pasta /texto
    for file_name in os.listdir(text_files_path):
        if file_name.endswith('.xml'):
            file_path = os.path.join(text_files_path, file_name)
            tree = ET.parse(file_path)
            root = tree.getroot()

            rua_numero = root.find('./meta/número').text

            rua_data = {
                "nome": root.find('./meta/nome').text,
                "rua": rua_numero,
                "figuras": [],
                "descrições": [],
                "casas": []
            }

            # Processar figuras
            for figura in root.findall('.//figura'):
                figura_data = {
                    "id": figura.attrib.get('id', 'N/A'),
                    "imagem": figura.find('imagem').attrib.get('path', 'N/A'),
                    "legenda": figura.find('legenda').text.strip() if figura.find('legenda') is not None else 'N/A'
                }
                rua_data["figuras"].append(figura_data)

            # Processar parágrafos
            descriptions = []
            for para in root.findall('.//corpo/para'):
                para_text = ''
                for elem in para.itertext():
                    para_text += elem
                descriptions.append(para_text.strip() + '\n')
            rua_data["descrições"] = ''.join(descriptions)

            # Processar casas
            for casa in root.findall('.//casa'):
                casa_data = {
                    "número": casa.find('número').text,
                    "enfiteutas": [enfiteuta.text for enfiteuta in casa.findall('enfiteuta')],
                    "foro": casa.find('foro').text if casa.find('foro') is not None else 'N/A',
                    "desc": [],
                    "vista": casa.find('vista').text if casa.find('vista') is not None else 'N/A'
                }

                # Processar descrições das casas
                desc_text = ''
                for desc in casa.findall('.//desc/para'):
                    for elem in desc.itertext():
                        desc_text += elem
                casa_data["desc"].append(desc_text.strip())

                rua_data["casas"].append(casa_data)

            ruas.append(rua_data)

    #Ordenar as ruas pelo numero da rua
    ruas = sorted(ruas, key=lambda x: int(x['rua']))

    return ruas

# Caminho dos ficheiros
indice_ruas_path = 'data/indiceruas.xml'
text_files_path = 'data/MapaRuas-materialBase/texto'

# Processar os dados de lugares, entidades e datas
lugares, entidades, datas = process_lugares_entidades_datas(text_files_path)

# Salvar os datasets em formato JSON
with open('lugares.json', 'w', encoding='utf-8') as lugares_file:
    json.dump(lugares, lugares_file, ensure_ascii=False, indent=4)

with open('entidades.json', 'w', encoding='utf-8') as entidades_file:
    json.dump(entidades, entidades_file, ensure_ascii=False, indent=4)

with open('datas.json', 'w', encoding='utf-8') as datas_file:
    json.dump(datas, datas_file, ensure_ascii=False, indent=4)

# Processar os dados das ruas
ruas_dataset = process_ruas(indice_ruas_path, text_files_path)

# Salvar o dataset de ruas em formato JSON
with open('ruas.json', 'w', encoding='utf-8') as ruas_file:
    json.dump(ruas_dataset, ruas_file, ensure_ascii=False, indent=4)

print("Datasets JSON criados com sucesso!")
