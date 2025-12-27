# conectar ao neo4j
from neo4j import GraphDatabase
import oracledb


# Conectar ao oracle
connection = oracledb.connect(user="bookstore", password="bookstore", dsn="localhost/orclcdb")

driver = GraphDatabase.driver("bolt://localhost:7687")

cursor = connection.cursor()

driver.execute_query("""
CALL apoc.trigger.install(
  'bookstore', 
  'validate_email_update', 
  '
   UNWIND keys($assignedNodeProperties) AS nodeId
   UNWIND $assignedNodeProperties[nodeId] AS propChange
   WITH propChange.node AS node, propChange.key AS propKey, propChange.new AS newValue
   WHERE propKey = "email" AND NOT newValue =~ "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"
   CALL apoc.util.validate(true, "Invalid email format.", [0])
  ', 
  {phase: "before"}
);
""")

driver.execute_query("""
CALL apoc.trigger.install(
  'bookstore', 
  'validate_email_create', 
  '
   UNWIND $createdNodes AS n
   WITH n
   WHERE n:Customer AND NOT n.email =~ "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"
   CALL apoc.util.validate(true, "Invalid email format.", [0])
   RETURN n
  ', 
  {phase: "before"}
);

""")


driver.execute_query("""
CALL apoc.trigger.install(
    'bookstore',
    'insert_order_history',
    'UNWIND $createdNodes AS node
     WITH node
     WHERE "Order" IN labels(node)
     MATCH (existing:OrderHistory)
     WITH node, max(existing.history_id) AS maxId
     CREATE (history:OrderHistory {
         history_id: CASE WHEN maxId IS NULL THEN 1 ELSE maxId + 1 END,
         status_date: datetime(),
         status_value: "Order Received"
     })
     CREATE (node)<-[:FOR_ORDER]-(history)',
    {phase: 'afterAsync'}
);
""")

