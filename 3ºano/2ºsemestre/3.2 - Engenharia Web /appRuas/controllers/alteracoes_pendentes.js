var AlteracoesPendentes = require('../models/alteracoes_pendentes');
var Ruas = require('../controllers/ruas');
var mongoose = require('mongoose')
var objectId = mongoose.Types.ObjectId;


// create alteracoes_pendentes
module.exports.create = function(data){
    var newAlteracoesPendentes = new AlteracoesPendentes(data);
    return newAlteracoesPendentes.save()
    .then(doc => {
        return doc;
    })
    .catch(err => {
        console.log(err);
        return err;
    });
}

// delete alteracoes_pendentes
module.exports.delete = function(data){
    const objectId = new mongoose.Types.ObjectId(data);
    return AlteracoesPendentes.deleteOne(objectId)
    .then(doc => {
        return doc;
    })
    .catch(err => {
        console.log(err);
        return;
    });
}


// GET /alteracoes_pendentes async
module.exports.alteracoes_pendentes_list = function(){
    return AlteracoesPendentes.find().lean().exec()
    .then(docs => {
        docs.forEach(doc => {
            doc.alteracoespendentes = parseInt(doc.alteracoespendentes);
        });
        return docs;
    })
    .catch(err => {
        console.log(err);
        return;
    });
}


// GET /alteracoes_pendentes/:id
module.exports.alteracoes_pendentes_id = function(id){
    return AlteracoesPendentes.findById(id).exec()
    .then(doc => {
        return doc;
    })
    .catch(err => {
        console.log(err);
        return;
    });
}

// GET /alteracoes_pendentes/validar/:id
module.exports.alteracoes_validar = (id) =>{
    return this.alteracoes_pendentes_id(id)
    .then(doc => {
        Ruas.delete(doc.rua)
        .then(() => {
            console.log("testesin")
            Ruas.create(doc)
            .then(() => {
                return this.delete(id);
            })
            .catch(err => {
                console.log(err);
                return err;
            });
        })
        .catch(err => {
            console.log(err);
            return err;
        });
    })
    .catch(err => {
        console.log(err);
        return err;
    }); 
}


