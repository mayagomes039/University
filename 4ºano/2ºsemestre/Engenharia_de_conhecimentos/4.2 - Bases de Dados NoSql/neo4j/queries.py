import time
from neo4j import GraphDatabase

NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"
NEO4J_PASSWORD = "your_password_here"

queries = [
    # Query 1
    """
    MATCH (b:Book)-[:WRITTEN_BY]->(a:Author),
          (b)-[:PUBLISHED_BY]->(p:Publisher),
          (b)-[:IN_LANGUAGE]->(l:Language)
    RETURN b.title AS `Título do Livro`,
           collect(DISTINCT a.author_name) AS Autores,
           p.publisher_name AS Editora,
           l.language_name AS Idioma,
           b.num_pages AS Páginas,
           b.publication_date AS `Data de Publicação`
    ORDER BY b.title
    """,
    # Query 2
    """
    MATCH (c:Customer)<-[:PLACED_BY]-(o:Order)-[co:CONTAINS]->(b:Book)
    WITH c, count(o) AS num_orders, sum(co.price) AS total_spent
    RETURN c.first_name + ' ' + c.last_name AS `Nome do Cliente`,
           c.email AS Email,
           num_orders AS `Número de Encomendas`,
           total_spent AS `Valor Total Gasto`
    ORDER BY num_orders DESC, total_spent DESC
    LIMIT 10
    """,
    # Query 3
    """
    MATCH (b:Book)<-[co:CONTAINS]-(o:Order)-[:PLACED_BY]->(c:Customer)
    MATCH (b)-[:WRITTEN_BY]->(a:Author)
    WITH b, 
         collect(DISTINCT a.author_name) as authors,
         collect(DISTINCT c.customer_id) as unique_customers,
         count(DISTINCT co) as total_quantity,
         avg(co.price) as avg_price
    RETURN b.title AS `Título do Livro`,
           apoc.text.join(authors, ', ') AS Autores,
           total_quantity AS `Quantidade Total Vendida`,
           avg_price AS `Preço Médio`,
           size(unique_customers) AS `Clientes Únicos`
    ORDER BY avg_price DESC
    LIMIT 15;
    """,
    # Query 4
    """
    MATCH (co:Country)<-[:LOCATED_IN]-(a:Address)<-[:LIVES_AT]-(c:Customer)<-[:PLACED_BY]-(o:Order)-[con:CONTAINS]->(b:Book)
    WITH co,
         count(DISTINCT c) AS `Clientes Únicos`,
         count(o) AS `Total de Encomendas`,
         sum(con.price) AS `Receita Total`, // This alias has a space
         avg(con.price) AS `Valor Médio por Encomenda`
    RETURN co.country_name AS País,
           `Clientes Únicos`,
           `Total de Encomendas`,
           `Receita Total`,
           `Valor Médio por Encomenda`
    ORDER BY `Receita Total` DESC
    """,
    # Query 5
    """
    MATCH (o:Order)<-[:FOR_ORDER]-(oh:OrderHistory)
    WITH o, oh ORDER BY oh.status_date DESC
    WITH o, head(collect(oh)) AS latest_oh 
    WITH latest_oh.status_value AS current_status_value, count(o) AS number_of_orders
    ORDER BY number_of_orders DESC


    WITH collect({status: current_status_value, count: number_of_orders}) AS status_counts_list,
         sum(number_of_orders) AS total_orders_overall

    UNWIND status_counts_list AS status_data
    WITH status_data.status AS `Estado da Encomenda`,        
         status_data.count AS `Número de Encomendas`,      
         round((status_data.count * 100.0 / total_orders_overall), 2) AS `Percentagem` 

    RETURN `Estado da Encomenda`,
           `Número de Encomendas`,
           `Percentagem`
    """,
    # Query 6
    """
    MATCH (a:Author)<-[:WRITTEN_BY]-(b:Book)
    OPTIONAL MATCH (b)-[:PUBLISHED_BY]->(p:Publisher)
    OPTIONAL MATCH (b)<-[co:CONTAINS]-(:Order)
    WITH a, count(DISTINCT b) AS `Número de Livros`, count(DISTINCT p) AS `Editoras Diferentes`,
         min(b.publication_date) AS `Primeira Publicação`, max(b.publication_date) AS `Última Publicação`,
         count(DISTINCT co) AS `Total de Livros Vendidos`
    RETURN a.author_name AS `Nome do Autor`, `Número de Livros`, `Editoras Diferentes`, 
           `Primeira Publicação`, `Última Publicação`, `Total de Livros Vendidos`
    ORDER BY `Número de Livros` DESC
    """,
    # Query 7
    """
    MATCH (o:Order)-[r:CONTAINS]->(:Book) 
    WHERE o.order_date IS NOT NULL
    WITH o.order_date.year AS ano,                      
         o.order_date.month AS mes_numero,              
         CASE o.order_date.month
           WHEN 1 THEN 'Janeiro'
           WHEN 2 THEN 'Fevereiro'
           WHEN 3 THEN 'Março'
           WHEN 4 THEN 'Abril'
           WHEN 5 THEN 'Maio'
           WHEN 6 THEN 'Junho'
           WHEN 7 THEN 'Julho'
           WHEN 8 THEN 'Agosto'
           WHEN 9 THEN 'Setembro'
           WHEN 10 THEN 'Outubro'
           WHEN 11 THEN 'Novembro'
           WHEN 12 THEN 'Dezembro'
           ELSE toString(o.order_date.month) 
         END AS nome_do_mes,
         count(DISTINCT o) AS numero_encomendas,     
         count(r) AS livros_vendidos,              
         sum(r.price) AS receita_total             

    ORDER BY ano DESC, mes_numero DESC

    RETURN ano AS `Ano`,
           mes_numero AS `Mês`,
           nome_do_mes AS `Nome do Mês`,
           numero_encomendas AS `Encomendas`,
           livros_vendidos AS `Livros Vendidos`,
           receita_total AS `Receita Total`
    """,
    # Query 8
    """
    MATCH (cust:Customer)-[r_lives_at:LIVES_AT]->(addr:Address)-[:LOCATED_IN]->(country:Country)
    WITH cust, addr, country, r_lives_at
    ORDER BY cust.customer_id, addr.address_id
    WITH cust,
         collect(
             toString(addr.street_number) + ' ' + addr.street_name + ', ' + country.country_name + ' (' + r_lives_at.status + ')'
         ) AS address_strings_list,
         count(addr) AS numero_de_enderecos
    
    WHERE numero_de_enderecos > 1
    RETURN cust.first_name + ' ' + cust.last_name AS `Nome do Cliente`,
           cust.email AS `Email`,
           numero_de_enderecos AS `Número de Endereços`,
           apoc.text.join(address_strings_list, '; ') AS `Todos os Endereços`
    ORDER BY numero_de_enderecos DESC
    """,
    # Query 9
    """
    MATCH (b:Book)-[:WRITTEN_IN]->(l:Language), (b)-[:PUBLISHED_BY]->(p:Publisher)
    WITH l, p, count(b) AS `Número de Livros`, avg(b.num_pages) AS `Média de Páginas`, min(b.publication_date) AS `Livro Mais Antigo`, max(b.publication_date) AS `Livro Mais Recente`
    RETURN l.language_name AS Idioma, p.publisher_name AS Editora, `Número de Livros`, `Média de Páginas`, `Livro Mais Antigo`, `Livro Mais Recente`
    ORDER BY Idioma, `Número de Livros` DESC
    """,
    # Query 10
    """
    MATCH (sm:ShippingMethod)<-[:SHIPPED_VIA]-(:Order)-[r_ol:CONTAINS]->(:Book)
    WITH sm,
         count(r_ol) AS numeroDeLinhasPorMetodo,    
         avg(r_ol.price) AS valorMedioLinhaPorMetodo, 
         sum(r_ol.price) AS receitaTotalPorMetodo  
    
    WITH collect({
            method_name: sm.method_name,
            linhas: numeroDeLinhasPorMetodo,
            avg_linha_price: valorMedioLinhaPorMetodo,
            total_revenue: receitaTotalPorMetodo
         }) AS method_stats_list,
         sum(numeroDeLinhasPorMetodo) AS grandTotalLinhas 
    UNWIND method_stats_list AS stats
    
    WITH stats.method_name AS metodoEnvio,
         stats.linhas AS numeroDeUtilizacoes, 
         round(toFloat(stats.linhas) * 100.0 / grandTotalLinhas, 2) AS percentagemDeUso,
         stats.avg_linha_price AS valorMedioEncomendas, 
         stats.total_revenue AS receitaTotal
    
    RETURN metodoEnvio AS `Método de Envio`,
           numeroDeUtilizacoes AS `Número de Utilizações`,
           percentagemDeUso AS `Percentagem de Uso`,
           valorMedioEncomendas AS `Valor Médio das Encomendas`,
           receitaTotal AS `Receita Total`
    ORDER BY numeroDeUtilizacoes DESC
    """,
    # Query 11
    """
    MATCH (b:Book)-[:PUBLISHED_BY]->(p:Publisher)
    MATCH (b)-[:IN_LANGUAGE]->(l:Language)
    WHERE NOT EXISTS ((:Order)-[:CONTAINS]->(b))
    WITH b, p, l 
    MATCH (b)-[:WRITTEN_BY]->(a:Author)
    WITH b, p, l, a ORDER BY a.author_name
    WITH b, p, l, COLLECT(a.author_name) AS authorNames
    WITH b, p, l, authorNames,
         duration.inDays(date(b.publication_date), date()).days AS daysSincePublication
    RETURN
        b.title AS `Título do Livro`,
        authorNames AS `Autores`, // This will be a list of author names, e.g., ["Author A", "Author B"]
        p.publisher_name AS `Editora`,
        l.language_name AS `Idioma`,
        b.publication_date AS `Data de Publicação`, // Or use toString(b.publication_date) if needed for specific formatting
        daysSincePublication AS `Dias Desde Publicação`
    
    ORDER BY b.publication_date DESC
    """,
    # Query 12
    """
    MATCH (c:Customer)<-[:PLACED_BY]-(o:Order)-[co:CONTAINS]->(b:Book)
    WITH c, count(o) AS total_orders, sum(co.price) AS total_spent, min(o.order_date) AS first_order, max(o.order_date) AS last_order, duration.inDays(min(o.order_date), max(o.order_date)).days AS customer_lifespan_days
    RETURN c.first_name + ' ' + c.last_name AS `Nome do Cliente`, c.email AS Email, total_orders AS `Total de Encomendas`, total_spent AS `Total Gasto`,
           CASE WHEN total_orders >= 10 AND total_spent >= 500 THEN 'VIP'
                WHEN total_orders >= 5 OR total_spent >= 200 THEN 'Fiel'
                WHEN total_orders >= 2 THEN 'Regular'
                ELSE 'Novo' END AS `Categoria de Cliente`,
           first_order AS `Primeira Encomenda`, last_order AS `Última Encomenda`, customer_lifespan_days AS `Dias Como Cliente`
    ORDER BY total_spent DESC
    """,
    # Query 13
    """
    MATCH (p:Publisher)
    WHERE EXISTS((p)<-[:PUBLISHED_BY]-(:Book))
    CALL {
        WITH p
        MATCH (p)<-[:PUBLISHED_BY]-(b_sub:Book)
        RETURN count(b_sub) AS livrosNoCatalogoCalc
    }
    CALL {
        WITH p
        MATCH (p)<-[:PUBLISHED_BY]-(b_sub:Book)-[:WRITTEN_BY]->(a_sub:Author)
        RETURN count(DISTINCT a_sub) AS autoresUnicosCalc
    }
    CALL {
        WITH p
        MATCH (p)<-[:PUBLISHED_BY]-(b_sub:Book)-[:IN_LANGUAGE]->(l_sub:Language)
        RETURN count(DISTINCT l_sub) AS idiomasPublicadosCalc
    }
    WITH p, livrosNoCatalogoCalc, autoresUnicosCalc, idiomasPublicadosCalc
    MATCH (p)<-[:PUBLISHED_BY]-(b_for_sales:Book) 
    OPTIONAL MATCH (b_for_sales)<-[ol:CONTAINS]-(:Order) 
    
    WITH p,
         livrosNoCatalogoCalc,
         autoresUnicosCalc,
         idiomasPublicadosCalc,
         count(ol) AS totalLinhasVendidasCalc, 
         sum(ol.price) AS receitaTotalCalc   
    
    WITH p.publisher_name AS editoraName,
         livrosNoCatalogoCalc,
         autoresUnicosCalc,
         idiomasPublicadosCalc,
         totalLinhasVendidasCalc,
         coalesce(receitaTotalCalc, 0) AS receitaTotalFinal, 
         round(toFloat(totalLinhasVendidasCalc) / livrosNoCatalogoCalc, 2) AS mediaVendasPorLivroCalc
    
    RETURN
        editoraName AS `Editora`,
        livrosNoCatalogoCalc AS `Livros no Catálogo`,
        autoresUnicosCalc AS `Autores Únicos`,
        totalLinhasVendidasCalc AS `Total de Linhas Vendidas`,
        receitaTotalFinal AS `Receita Total`,
        mediaVendasPorLivroCalc AS `Média de Vendas por Livro`,
        idiomasPublicadosCalc AS `Idiomas Publicados`
    ORDER BY receitaTotalFinal DESC
    """
]

def run_query(driver, query, index, output_file):
    with driver.session() as session:
        start_time = time.time()
        result = session.run(query)
        duration = time.time() - start_time

        output_file.write(f"\n=== QUERY {index + 1} RESULTS (Time: {duration:.4f} seconds) ===\n")
        for record in result:
            output_file.write(str(record.data()) + "\n")
        output_file.write("\n---\n")

        print(f"Query {index + 1} completed in {duration:.4f} seconds")  

if __name__ == "__main__":
    driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))
    try:
        with open("query_results.txt", "w", encoding="utf-8") as output_file:
            output_file.write("Neo4j Query Results\n=====================\n")
            total_start = time.time()
            for i, query in enumerate(queries):
                run_query(driver, query, i, output_file)
            total_duration = time.time() - total_start
            output_file.write(f"\nALL QUERIES COMPLETED IN {total_duration:.4f} seconds\n")
        print(f"\nResults saved to 'query_results.txt' ")
    finally:
        driver.close()