from neo4j import GraphDatabase
import oracledb


connection = oracledb.connect(user="bookstore", password="bookstore", dsn="localhost/orclcdb")

driver = GraphDatabase.driver("bolt://localhost:7687")

cursor = connection.cursor()

driver.execute_query("""
CALL apoc.custom.installProcedure(
    'updateOrderStatus (p_order_id::INT, p_status_value::STRING) :: (message::STRING)',
    'MATCH (order:Order {order_id: $p_order_id})
     
     OPTIONAL MATCH (order)-[:HAS_HISTORY]->(history:OrderHistory)
     WITH order, history
     ORDER BY history.status_date DESC
     WITH order, collect(history)[0] AS latestHistory
     
     WITH order, 
          CASE WHEN latestHistory IS NULL THEN "" ELSE latestHistory.status_value END AS previousStatus,
          $p_status_value AS newStatus
     
     WHERE previousStatus <> newStatus
     
     OPTIONAL MATCH (existing:OrderHistory)
     WITH order, previousStatus, newStatus, max(existing.history_id) AS maxId
     
     CREATE (newHistory:OrderHistory {
         history_id: CASE WHEN maxId IS NULL THEN 1 ELSE maxId + 1 END,
         status_value: newStatus,
         status_date: datetime()
     })
     CREATE (order)<-[:FOR_ORDER]-(newHistory)
     
     RETURN "Status atualizado com sucesso para o pedido " + toString(order.order_id) AS message',
     'bookstore',
     'write'
);                  
""")






















