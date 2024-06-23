var mongoose = require('mongoose');

var contractsSchema = new mongoose.Schema({
    nome: String,
    rua: String,
    figuras: [{
        id: String,
        imagem: String,
        legenda: String
    }],
    descrições: String,
    casas: [{
        número: String,
        enfiteutas: [String],
        foro: String,
        desc: [String],
        vista: String
    }]
});
module.exports = mongoose.model('AlteracoesPendentes', contractsSchema);
