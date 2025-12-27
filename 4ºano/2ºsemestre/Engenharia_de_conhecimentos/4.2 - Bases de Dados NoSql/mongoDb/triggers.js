// validate_email

// marcar para insert e update no trigger
exports = async function(changeEvent) {
  const customers = context.services.get("BDNSQL").db("bookstore").collection("customers");
  
  const customerId = changeEvent.documentKey._id;

  // exp validação
  const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

  // como é mongo convem verificar se email existe?
  if(changeEvent.fullDocument.email){
    if(!emailRegex.test(changeEvent.fullDocument.email)){
      // abortar operação chega?
      console.log("Invalid email format");
      throw new Error('Invalid email format.');

    }
  }
}
// nao se pode prevenir a inserção em mongo, mas podemos usar schema validation
db.runCommand({
  collMod: "customers",
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name", "email"],
      properties: {
        email: {
          bsonType: "string",
          pattern: "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
          description: "Must be a valid email address"
        }
      }
    }
  },
  validationAction: "error", // previne a insercão se email não for válido
  validationLevel: "strict"
});

// insert_order_history

// tipo de trigger: insert
exports = async function(changeEvent) {
  const orders = context.services.get("BDNSQL").db("bookstore").collection("orders");

  const newOrder = changeEvent.fullDocument;
  const orderId = newOrder._id;

  try {
    const initOrderStatus = {
      status_id: 1,
      status_value: "Order Received", // nao tenho a certeza que seja este o valor, confirmar
      status_date: new Date()
    };

    await orders.updateOne(
      {"_id": orderId},
      {
        $push: {
          order_history: initOrderStatus
        }
      }
    )
    console.log(`inserted initial order status for ID: ${orderId}`)
  }
  catch(error){
    console.log("Error adding order hist", error)
  }

};

// prevent_book_deletion

// tipo de trigger: delete
exports = async function(changeEvent) {
  const orders =  context.services.get("BDNSQL").db("bookstore").collection("orders");
  const books = context.services.get("BDNSQL").db("bookstore").collection("books");

  const deletedBookId = changeEvent.documentKey._id;
  // not let a book get deleted if its in any orders

  // e se ja tiver completado?
  try {
    const existsOrderWithBook = await orders.findOne({
      "order_lines.book_id": deletedBookId

      // "order_history.status_value": { $ne: "Completed" }  se for preciso
    })

    if (existsOrderWithBook){
      console.log(` Book ${deletedBookId} was found in ongoing orders. Order ID: ${existsOrderWithBook._id}`);

      // como em mongodb triggers nao podemos prevenir deletes, voltamos a inserir
      try{
        const deletedBook = changeEvent.fullDocumentBeforeChange;
        await books.insertOne(deletedBook);
        console.log("reinsert sucessfull")
        
      }
      catch(error){
        console.error("Error re-inserting book:", error); 
      }

    }
  }
  catch(error){
    console.log("Error", error)
  }
};