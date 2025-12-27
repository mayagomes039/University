from pymongo import MongoClient
from bson.son import SON

def query_0(db):
    # Consulta simples para testar conexão
    return list(db.books.find())


def query_1(db):
    pipeline = [
        {
            "$project": {
                "_id": 0,
                "book_id": "$_id",
                "Título do Livro": "$title",
                "Autores": "$authors.author_name",
                "Editora": "$publisher.publisher_name",
                "Idioma": "$language.language_name",
                "Páginas": "$num_pages",
                "Data de Publicação": "$publication_date"
            }
        },
        {
            "$sort": SON([("Título do Livro", 1)])
        }
    ]
    return list(db.books.aggregate(pipeline))

def query_2(db):
    pipeline = [
        {"$unwind": "$order_lines"},
        {"$group": {
            "_id": "$customer_id",
            "Número de Encomendas": {"$sum": 1},
            "Valor Total Gasto": {"$sum": "$order_lines.price_at_order"}
        }},
        {"$lookup": {
            "from": "customers",
            "localField": "_id",
            "foreignField": "_id",
            "as": "cliente"
        }},
        {"$unwind": "$cliente"},
        {"$project": {
            "_id": 0,
            "Nome do Cliente": {
                "$concat": ["$cliente.first_name", " ", "$cliente.last_name"]
            },
            "Email": "$cliente.email",
            "Número de Encomendas": 1,
            "Valor Total Gasto": 1
        }},
        {"$sort": SON([("Número de Encomendas", -1), ("Valor Total Gasto", -1)])},
        {"$limit": 10}
    ]
    return list(db.orders.aggregate(pipeline))

def query_3(db):
    pipeline = [
        # Stage 1: Desenrolar order_lines
        {"$unwind": "$order_lines"},
        
        # Stage 2: Lookup para buscar dados do livro
        {
            "$lookup": {
                "from": "books",
                "localField": "order_lines.book_id",
                "foreignField": "_id",
                "as": "book"
            }
        },
        {"$unwind": "$book"},
        
        # Stage 3: Filtrar apenas livros que têm autores (para corresponder ao JOIN do Oracle)
        {
            "$match": {
                "$and": [
                    {"book.authors": {"$exists": True}},
                    {"book.authors": {"$ne": []}},
                    {"book.authors": {"$not": {"$size": 0}}}
                ]
            }
        },
        
        # Stage 4: Agrupar por livro
        {
            "$group": {
                "_id": "$book._id",
                "titulo": {"$first": "$book.title"},
                "authors_raw": {"$first": "$book.authors"},
                "quantidade_vendida": {"$sum": 1},
                "preco_medio": {"$avg": "$order_lines.price_at_order"},
                "clientes_unicos": {"$addToSet": "$customer_id"}
            }
        },
        
        # Stage 5: Processar autores
        {
            "$addFields": {
                "autores": {
                    "$reduce": {
                        "input": {
                            "$sortArray": {
                                "input": "$authors_raw",
                                "sortBy": {"author_name": 1}
                            }
                        },
                        "initialValue": "",
                        "in": {
                            "$cond": {
                                "if": {"$eq": ["$$value", ""]},
                                "then": {"$ifNull": ["$$this.author_name", ""]},
                                "else": {"$concat": ["$$value", ", ", {"$ifNull": ["$$this.author_name", ""]}]}
                            }
                        }
                    }
                },
                "clientes_unicos_count": {"$size": "$clientes_unicos"}
            }
        },
        
        # Stage 6: Projeção final
        {
            "$project": {
                "_id": 0,
                "Título do Livro": "$titulo",
                "Autores": "$autores",
                "Quantidade Total Vendida": "$quantidade_vendida",
                "Preço Médio": {"$round": ["$preco_medio", 2]},
                "Clientes Únicos": "$clientes_unicos_count"
            }
        },
        
        # Stage 7: Ordenar e limitar
        {"$sort": {"Quantidade Total Vendida": -1}},
        {"$limit": 15}
    ]
    
    return list(db.orders.aggregate(pipeline))


def query_4(db):
    pipeline = [
        # Filtrar pedidos que tenham endereço destino com país
        {"$match": {"destination_address.country.country_id": {"$exists": True}}},

        # Desenrolar as linhas do pedido
        {"$unwind": "$order_lines"},

        # Agrupar por país, para calcular total encomendas, receita e clientes únicos
        {"$group": {
            "_id": {
                "country_id": "$destination_address.country.country_id",
                "country_name": "$destination_address.country.country_name"
            },
            "clientes_unicos": {"$addToSet": "$customer_id"},
            "total_encomendas": {"$sum": 1},  # Cada linha do pedido conta como uma encomenda?
            "receita_total": {"$sum": "$order_lines.price_at_order"}
        }},

        # Montar resultado com projeção de campos e cálculo do valor médio por encomenda
        {"$project": {
            "País": "$_id.country_name",
            "Clientes Únicos": {"$size": "$clientes_unicos"},
            "Total de Encomendas": "$total_encomendas",
            "Receita Total": "$receita_total",
            "Valor Médio por Encomenda": {
                "$cond": [
                    {"$gt": ["$total_encomendas", 0]},
                    {"$divide": ["$receita_total", "$total_encomendas"]},
                    0
                ]
            }
        }},

        # Ordenar por receita total decrescente
        {"$sort": {"Receita Total": -1}}
    ]

    return list(db.orders.aggregate(pipeline))


def query_5(db):
    pipeline = [
        # "Desenrola" o histórico de status para cada pedido
        {"$unwind": "$order_history"},

        # Ordena por pedido e status_date decrescente para pegar o status mais recente
        {"$sort": {"_id": 1, "order_history.status_date": -1}},

        # Agrupa por pedido para pegar o status mais recente (primeiro após ordenação)
        {"$group": {
            "_id": "$_id",  # pedido_id
            "latest_status_id": {"$first": "$order_history.status_id"},
            "latest_status_value": {"$first": "$order_history.status_value"}
        }},

        # Agora agrupa por status para contar número de pedidos em cada status
        {"$group": {
            "_id": "$latest_status_value",
            "num_encomendas": {"$sum": 1}
        }},

        # Agrupa para cálculo do total e montar array de estados
        {"$group": {
            "_id": None,
            "total_encomendas": {"$sum": "$num_encomendas"},
            "estados": {
                "$push": {
                    "Estado da Encomenda": "$_id",
                    "Número de Encomendas": "$num_encomendas"
                }
            }
        }},

        # "Desenrola" o array estados para projetar os dados
        {"$unwind": "$estados"},

        # Calcula a percentagem por estado
        {"$project": {
            "Estado da Encomenda": "$estados.Estado da Encomenda",
            "Número de Encomendas": "$estados.Número de Encomendas",
            "Percentagem": {
                "$round": [
                    {
                        "$multiply": [
                            {"$divide": ["$estados.Número de Encomendas", "$total_encomendas"]},
                            100
                        ]
                    },
                    2
                ]
            }
        }},

        # Ordena decrescente pela quantidade de encomendas
        {"$sort": {"Número de Encomendas": -1}}
    ]

    return list(db.orders.aggregate(pipeline))

def query_6(db):
    pipeline = [
        # Desenrolar o array de autores
        {"$unwind": "$authors"},

        # Agrupar por autor, coletar dados básicos e contar livros
        {
            "$group": {
                "_id": "$authors.author_id",
                "author_name": {"$first": "$authors.author_name"},
                "number_of_books": {"$sum": 1},
                "publishers": {"$addToSet": "$publisher.publisher_id"},
                "first_publication": {"$min": "$publication_date"},
                "last_publication": {"$max": "$publication_date"},
                "books_ids": {"$addToSet": "$_id"}
            }
        },

        # Lookup para orders, juntando livros vendidos
        {
            "$lookup": {
                "from": "orders",
                "let": {"books_ids": "$books_ids"},
                "pipeline": [
                    {"$unwind": "$order_lines"},
                    {
                        "$match": {
                            "$expr": {"$in": ["$order_lines.book_id", "$$books_ids"]}
                        }
                    },
                    {
                        "$group": {
                            "_id": None,
                            "total_sold": {"$sum": 1}
                        }
                    }
                ],
                "as": "order_matches"
            }
        },

        # Adicionar campo com o total vendido, 0 se não houver correspondência
        {
            "$addFields": {
                "total_books_sold": {
                    "$ifNull": [{"$arrayElemAt": ["$order_matches.total_sold", 0]}, 0]
                }
            }
        },

        # Contar editoras diferentes
        {
            "$addFields": {
                "different_publishers": {"$size": "$publishers"}
            }
        },

        # Projetar campos finais
        {
            "$project": {
                "_id": 0,
                "Nome do Autor": "$author_name",
                "Número de Livros": "$number_of_books",
                "Editoras Diferentes": "$different_publishers",
                "Primeira Publicação": "$first_publication",
                "Última Publicação": "$last_publication",
                "Total de Livros Vendidos": "$total_books_sold"
            }
        },

        # Ordenar por número de livros decrescente
        {
            "$sort": {"Número de Livros": -1}
        }
    ]

    return list(db["books"].aggregate(pipeline))




def query_7(db):
    pipeline = [
        # "Desmonta" o array order_lines para processar cada livro vendido
        {"$unwind": "$order_lines"},

        # Agrupa por ano, mês e nome do mês do pedido
        {
            "$group": {
                "_id": {
                    "year": {"$year": "$order_date"},
                    "month": {"$month": "$order_date"},
                    "monthName": {"$dateToString": {"format": "%B", "date": "$order_date"}}
                },
                "encomendas": {"$addToSet": "$_id"},         # pedidos únicos no mês
                "livros_vendidos": {"$sum": 1},              # cada linha = 1 livro vendido
                "receita_total": {"$sum": "$order_lines.price_at_order"}  # soma dos preços
            }
        },

        # Formata o resultado para saída clara
        {
            "$project": {
                "_id": 0,
                "Ano": "$_id.year",
                "Mês": "$_id.month",
                "Nome do Mês": "$_id.monthName",
                "Encomendas": {"$size": "$encomendas"},
                "Livros Vendidos": "$livros_vendidos",
                "Receita Total": "$receita_total"
            }
        },

        # Ordena por ano e mês descrescente (mais recente primeiro)
        {
            "$sort": {"Ano": -1, "Mês": -1}
        }
    ]

    return list(db.orders.aggregate(pipeline))


def query_8(db):
    pipeline = [
        # Desmonta o array de endereços para manipular individualmente
        {"$unwind": "$addresses"},

        # Monta a string formatada do endereço
        {
            "$addFields": {
                "formatted_address": {
                    "$concat": [
                        {"$toString": "$addresses.street_number"}, " ",
                        "$addresses.street_name", ", ",
                        "$addresses.city", ", ",
                        "$addresses.country.country_name", " (",
                        "$addresses.status.status_value", ")"
                    ]
                }
            }
        },

        # Agrupa novamente por cliente, juntando os endereços formatados
        {
            "$group": {
                "_id": {
                    "customer_id": "$_id",
                    "first_name": "$first_name",
                    "last_name": "$last_name",
                    "email": "$email"
                },
                "addresses": {
                    "$push": {
                        "address_id": "$addresses.address_id",
                        "formatted_address": "$formatted_address"
                    }
                },
                "address_count": {"$sum": 1}
            }
        },

        # Filtra clientes com mais de 1 endereço
        {"$match": {"address_count": {"$gt": 1}}},

        # Projeta o resultado final
        {
            "$project": {
                "_id": 0,
                "Nome do Cliente": {
                    "$concat": ["$_id.first_name", " ", "$_id.last_name"]
                },
                "Email": "$_id.email",
                "Número de Endereços": "$address_count",
                "Todos os Endereços": {
                    "$reduce": {
                        "input": {
                            "$map": {
                                "input": {
                                    "$sortArray": {
                                        "input": "$addresses",
                                        "sortBy": {"address_id": 1}
                                    }
                                },
                                "as": "addr",
                                "in": "$$addr.formatted_address"
                            }
                        },
                        "initialValue": "",
                        "in": {
                            "$cond": [
                                {"$eq": ["$$value", ""]},
                                "$$this",
                                {"$concat": ["$$value", "; ", "$$this"]}
                            ]
                        }
                    }
                }
            }
        },

        # Ordena por número de endereços (decrescente)
        {"$sort": {"Número de Endereços": -1}}
    ]

    return list(db.customers.aggregate(pipeline))


def query_9(db):
    pipeline = [
        # Agrupar por idioma e editora
        {
            "$group": {
                "_id": {
                    "language_id": "$language.language_id",
                    "language_name": "$language.language_name",
                    "publisher_id": "$publisher.publisher_id",
                    "publisher_name": "$publisher.publisher_name"
                },
                "numero_de_livros": {"$sum": 1},
                "media_paginas": {"$avg": "$num_pages"},
                "livro_mais_antigo": {"$min": "$publication_date"},
                "livro_mais_recente": {"$max": "$publication_date"}
            }
        },

        # Projeta os campos desejados para saída limpa
        {
            "$project": {
                "_id": 0,
                "Idioma": "$_id.language_name",
                "Editora": "$_id.publisher_name",
                "Número de Livros": "$numero_de_livros",
                "Média de Páginas": {"$round": ["$media_paginas", 2]},  # arredonda para 2 casas decimais
                "Livro Mais Antigo": "$livro_mais_antigo",
                "Livro Mais Recente": "$livro_mais_recente"
            }
        },

        # Ordena por nome do idioma e número de livros descendente
        {
            "$sort": {
                "Idioma": 1,
                "Número de Livros": -1
            }
        }
    ]

    return list(db.books.aggregate(pipeline))

def query_10(db):
    pipeline = [
        # Desenrola os pedidos para linhas individuais
        {"$unwind": "$order_lines"},

        # Agrupa por método de envio (id e nome)
        {
            "$group": {
                "_id": {
                    "method_id": "$shipping_method.method_id",
                    "method_name": "$shipping_method.method_name"
                },
                "numero_de_utilizacoes": {"$addToSet": "$_id"},  # conjunto de order_ids únicos
                "total_linhas": {"$sum": 1},                     # total de linhas de encomenda (livros vendidos)
                "receita_total": {"$sum": "$order_lines.price_at_order"}
            }
        },

        # Calcular média do valor das encomendas (por pedido)
        # Note que para média correta precisamos do total por pedido, 
        # mas aqui estamos somando por linhas; uma aproximação: receita_total / numero de encomendas
        {
            "$addFields": {
                "numero_de_encomendas": {"$size": "$numero_de_utilizacoes"},
                "valor_medio_encomenda": {
                    "$divide": ["$receita_total", {"$size": "$numero_de_utilizacoes"}]
                }
            }
        },

        # Total de utilizações para calcular percentagem
        {
            "$group": {
                "_id": None,
                "metodos": {
                    "$push": {
                        "method_name": "$_id.method_name",
                        "numero_de_utilizacoes": {"$size": "$numero_de_utilizacoes"},
                        "valor_medio_encomenda": "$valor_medio_encomenda",
                        "receita_total": "$receita_total"
                    }
                },
                "total_utilizacoes": {"$sum": {"$size": "$numero_de_utilizacoes"}}
            }
        },

        # Calcula a percentagem de uso por método
        {
            "$unwind": "$metodos"
        },
        {
            "$project": {
                "_id": 0,
                "Método de Envio": "$metodos.method_name",
                "Número de Utilizações": "$metodos.numero_de_utilizacoes",
                "Percentagem de Uso": {
                    "$round": [
                        {
                            "$multiply": [
                                {"$divide": ["$metodos.numero_de_utilizacoes", "$total_utilizacoes"]},
                                100
                            ]
                        },
                        2
                    ]
                },
                "Valor Médio das Encomendas": {
                    "$round": ["$metodos.valor_medio_encomenda", 2]
                },
                "Receita Total": {
                    "$round": ["$metodos.receita_total", 2]
                }
            }
        },

        # Ordena pelo número de utilizações (descendente)
        {
            "$sort": {"Número de Utilizações": -1}
        }
    ]

    return list(db.orders.aggregate(pipeline))


def query_11(db):
    pipeline = [
        # Lookup para encontrar orders que contêm este book_id nas order_lines
        {
            "$lookup": {
                "from": "orders",
                "let": {"book_id": "$_id"},
                "pipeline": [
                    {"$unwind": "$order_lines"},
                    {"$match": {"$expr": {"$eq": ["$order_lines.book_id", "$$book_id"]}}}
                ],
                "as": "orders_with_book"
            }
        },
        # Filtrar apenas livros que não aparecem em nenhuma order_line (nunca vendidos)
        {
            "$match": {
                "orders_with_book": {"$size": 0}
            }
        },
        # Calcular dias desde publicação
        {
            "$addFields": {
                "dias_desde_publicacao": {
                    "$divide": [
                        {"$subtract": [{"$toDate": "$$NOW"}, "$publication_date"]},
                        86400000  # milliseconds in a day
                    ]
                }
            }
        },
        # Converter para inteiro (equivalente ao TRUNC)
        {
            "$addFields": {
                "dias_desde_publicacao": {"$toInt": "$dias_desde_publicacao"}
            }
        },
        # Juntar nomes dos autores (equivalente ao LISTAGG)
        {
            "$addFields": {
                "autores": {
                    "$reduce": {
                        "input": "$authors",
                        "initialValue": "",
                        "in": {
                            "$cond": {
                                "if": {"$eq": ["$$value", ""]},
                                "then": "$$this.author_name",
                                "else": {"$concat": ["$$value", ", ", "$$this.author_name"]}
                            }
                        }
                    }
                }
            }
        },
        # Projeção final para formato desejado
        {
            "$project": {
                "_id": 0,
                "titulo_do_livro": "$title",
                "autores": 1,
                "editora": "$publisher.publisher_name",
                "idioma": "$language.language_name",
                "data_de_publicacao": "$publication_date",
                "dias_desde_publicacao": 1
            }
        },
        # Ordenar por data de publicação (mais recente primeiro)
        {
            "$sort": {"data_de_publicacao": -1}
        }
    ]
    
    return list(db.books.aggregate(pipeline))

def query_12(db):
    pipeline = [
        # Juntar orders com customers
        {
            "$lookup": {
                "from": "orders",
                "localField": "_id",
                "foreignField": "customer_id",
                "as": "orders"
            }
        },
        {
            "$unwind": {
                "path": "$orders",
                "preserveNullAndEmptyArrays": False
            }
        },
        {
            "$unwind": {
                "path": "$orders.order_lines",
                "preserveNullAndEmptyArrays": False
            }
        },
        # Agrupar por cliente
        {
            "$group": {
                "_id": "$_id",
                "customer_name": {
                    "$first": {
                        "$concat": ["$first_name", " ", "$last_name"]
                    }
                },
                "email": {"$first": "$email"},
                "total_orders": {"$addToSet": "$orders._id"},  # para contar ordens únicas
                "total_spent": {"$sum": "$orders.order_lines.price_at_order"},
                "first_order": {"$min": "$orders.order_date"},
                "last_order": {"$max": "$orders.order_date"}
            }
        },
        # Após agrupar, contar ordens únicas
        {
            "$addFields": {
                "total_orders": {"$size": "$total_orders"},
                "customer_lifespan_days": {
                    "$trunc": {
                        "$divide": [
                            {"$subtract": ["$last_order", "$first_order"]},
                            1000 * 60 * 60 * 24  # converter ms para dias
                        ]
                    }
                }
            }
        },
        # Adicionar categoria do cliente
        {
            "$addFields": {
                "categoria": {
                    "$switch": {
                        "branches": [
                            {
                                "case": {
                                    "$and": [
                                        {"$gte": ["$total_orders", 10]},
                                        {"$gte": ["$total_spent", 500]}
                                    ]
                                },
                                "then": "VIP"
                            },
                            {
                                "case": {
                                    "$or": [
                                        {"$gte": ["$total_orders", 5]},
                                        {"$gte": ["$total_spent", 200]}
                                    ]
                                },
                                "then": "Fiel"
                            },
                            {
                                "case": {"$gte": ["$total_orders", 2]},
                                "then": "Regular"
                            }
                        ],
                        "default": "Novo"
                    }
                }
            }
        },
        # Projetar os campos finais
        {
            "$project": {
                "_id": 0,
                "Nome do Cliente": "$customer_name",
                "Email": "$email",
                "Total de Encomendas": "$total_orders",
                "Total Gasto": "$total_spent",
                "Categoria de Cliente": "$categoria",
                "Primeira Encomenda": "$first_order",
                "Última Encomenda": "$last_order",
                "Dias Como Cliente": "$customer_lifespan_days"
            }
        },
        {
            "$sort": {
                "Total Gasto": -1
            }
        }
    ]

    return list(db.customers.aggregate(pipeline))

def query_13(db):
    pipeline = [
        # unwind ao o array 'authors' para processar cada autor individualmente.
        # mesmo que um autor tenha escrito vários livros para a mesma editora.
        {
            "$unwind": {
                "path": "$authors",
                "preserveNullAndEmptyArrays": True # Mantém os documentos de livros mesmo que não tenham autores
            }
        },
        #agrupar os docs pelo ID da editora para calcular métricas por editora.
        {
            "$group": {
                "_id": "$publisher.publisher_id", 
                "publisher_name": { "$first": "$publisher.publisher_name" }, 
                "books_in_catalog": { "$addToSet": "$_id" }, # collect aos IDs únicos dos livros publicados por esta editora
                "unique_authors": { "$addToSet": "$authors.author_id" }, 
                "published_languages": { "$addToSet": "$language.language_id" }
            }
        },
        # faz um join com a coleção 'orders' para obter os dados de vendas.
        # Usa 'let' para passar os IDs dos livros da editora para o sub-pipeline.
        {
            "$lookup": {
                "from": "orders", 
                "let": { "publisherBookIds": "$books_in_catalog" }, 
                "pipeline": [
                    { "$unwind": "$order_lines" }, #
                    {
                        "$match": {
                            "$expr": {
                                "$in": ["$order_lines.book_id", "$$publisherBookIds"] # Filtra linhas de pedido para livros desta editora
                            }
                        }
                    },
                    {
                        "$group": {
                            "_id": None, # Agrupa todas as linhas de pedido correspondentes em um único documento
                            "total_sold_lines": { "$sum": 1 },
                            "total_revenue": { "$sum": "$order_lines.price_at_order" } 
                        }
                    }
                ],
                "as": "sales_data" # resultado do lookup
            }
        },
        # unwind ao o array 'sales_data'.
        {
            "$unwind": {
                "path": "$sales_data",
                "preserveNullAndEmptyArrays": True # garante que editoras sem vendas ainda sejam incluída
            }
        },
        # projeta os campos finais e calcula métricas derivadas.
        {
            "$project": {
                "_id": 0, # 
                "Editora": "$publisher_name", 
                "Livros no Catálogo": { "$size": "$books_in_catalog" }, 
                "Autores Únicos": { "$size": { "$ifNull": ["$unique_authors", []] } }, # 
                "Total de Linhas Vendidas": { "$ifNull": ["$sales_data.total_sold_lines", 0] }, 
                "Receita Total": { "$ifNull": ["$sales_data.total_revenue", 0] }, 
                "Média de Vendas por Livro": {
                    "$cond": [
                        { "$gt": [{ "$size": "$books_in_catalog" }, 0] }, # Condição para evitar divisão por zero
                        { "$round": [{ "$divide": [{ "$ifNull": ["$sales_data.total_sold_lines", 0] }, { "$size": "$books_in_catalog" }] }, 2] }, # Calcula a média e arredonda
                        0 # Se não houver livros, a média é 0
                    ]
                },
                "Idiomas Publicados": { "$size": "$published_languages" } # Conta os idiomas únicos publicados
            }
        },
        # Ordena os resultados pela receita total em ordem decrescente.
        {
            "$sort": {
                "Receita Total": -1
            }
        }
    ]
    return list(db.books.aggregate(pipeline))


