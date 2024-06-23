var express = require('express');
var router = express.Router();
var Ruas = require("../controllers/ruas.js")



// GET /ruas 
router.get('/',function (req, res, next) {
    Ruas.ruas_list()
        .then(dados =>
            res.jsonp(dados)
        )
        .catch(erro => res.status(520).jsonp(erro))
});

// /ruas/:rua
router.get('/:rua', function (req, res, next) {
    Ruas.ruas_id(req.params.rua)
        .then(dados =>
            res.jsonp(dados)
        )
        .catch(erro => res.status(520).jsonp(erro))
});

/* POST /ruas */
router.post('/', function(req, res) {
    Ruas.insert(req.body)
        .then(data => res.jsonp(data))
        .catch(err => res.status(500).json({ error: err.message }))
});
module.exports = router;
