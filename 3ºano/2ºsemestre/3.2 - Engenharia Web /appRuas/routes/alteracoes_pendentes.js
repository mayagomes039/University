var express = require('express');
var router = express.Router();
var AlteracoesPendentes = require("../controllers/alteracoes_pendentes.js")

// GET /alteracoespendentes
router.get('/', function(req, res, next) {
    AlteracoesPendentes.alteracoes_pendentes_list(req, res, next)
    .then(dados => {
        res.jsonp(dados)
    }).catch(err => {
        console.log(err);
        res.status(520).jsonp(err)
    });
});

// GET /alteracoespendentes/:rua
router.get('/:rua', function(req, res, next) {
    AlteracoesPendentes.alteracoes_pendentes_id(req.params.rua)
    .then(dados => {
        res.jsonp(dados)
    }).catch(err => {
        console.log(err);
        res.status(520).jsonp(err)
    });
});

// POST /alteracoespendentes/validar/:id
router.post('/validar/:id', function(req, res, next) {
    AlteracoesPendentes.alteracoes_validar(req.params.id)
    .then(dados => {
        res.jsonp(true)
    }).catch(err => {
        console.log(err);
        res.status(520).jsonp(false)
    });
});

// POST /alteracoespendentes/adicionar
router.post('/adicionar', function(req, res, next) {
    AlteracoesPendentes.create(req.body)
    .then(dados => {
        res.jsonp(dados)
    }).catch(err => {
        console.log(err);
        res.status(520).jsonp(err)
    });
});

// POST /alteracoespendentes/eliminar
router.post('/eliminar/:id', function(req, res, next) {
    AlteracoesPendentes.delete(req.params.id)
    .then(dados => {
        res.jsonp(dados)
    }).catch(err => {
        console.log(err);
        res.status(520).jsonp(err)
    });
});

module.exports = router;