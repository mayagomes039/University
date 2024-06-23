var Ruas = require("../models/ruas")
var mongoose = require('mongoose')
var objectId = mongoose.Types.ObjectId;

// GET /ruas
module.exports.ruas_list = function() {
    return Ruas.find().lean().exec()
    .then(docs => {
        docs.forEach(doc => {
            doc.rua = parseInt(doc.rua);
        });
        docs.sort((a, b) => a.rua - b.rua);
        return docs;
    })
    .catch(err => {
        console.log(err);
        return;
    });
}

// GET /ruas/:rua
module.exports.ruas_id = function(rua) {
    return Ruas.findOne({ rua: rua }).exec()
    .then(doc => {
        console.log(doc);
        return doc;
    })
    .catch(err => {
        console.log(err);
        return;
    });
}

module.exports.delete = function(id_rua){
    return Ruas.deleteOne({rua: id_rua})
    .then(doc => {
        return doc;
    })
    .catch(err => {
        console.log(err);
        return;
    });
}

module.exports.insert = rua => {
    return Ruas
        .create(rua);
}


module.exports.create = function(data){
    data_json = data.toJSON();
    delete data_json._id;
    data_json["descrições"] = data_json.descricao;
    delete data_json.descricao;

    var newRua = new Ruas(data_json);
    return newRua.save()
    .then(doc => {
        return doc;
    })
    .catch(err => {
        console.log(err);
        return err;
    });
}