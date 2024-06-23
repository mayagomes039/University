
const express = require('express');
const router = express.Router();
const axios = require('axios');
const cookieParser = require('cookie-parser');
const jwt = require('jsonwebtoken');

function verificaAcesso(req, res, next) {
    console.log("verificaAcesso em alteracoes_pendentes.js");

    const token = req.cookies.token;

    if (!token) {
        console.log("Token not found, redirecting to login");
        return res.redirect('/login');
    }

    // Verify the token
    jwt.verify(token, "EngWeb2024", (err, decoded) => {
        if (err) {
            console.log("Invalid token, rendering login with error message");
            return res.render('login', { message: "Credenciais inválidas" });
        }

        console.log("Decoded token:", decoded);

        // Check if the user has the 'admin' role
        if (decoded.nivel !== 'admin') {
            console.log("User does not have admin role, redirecting to login");
            return res.render('login', { message: "Acesso negado. Apenas administradores podem acessar." });
        }

        // Token is valid and user is an admin, proceed to next middleware/route handler
        req.user = decoded;
        next();
    });
}

// GET /alteracoes_pendentes
router.get('/', verificaAcesso, function (req, res, next) {
    axios.get('http://localhost:2034/alteracoespendentes')
        .then(dados => {
            res.render('alteracoespendentes', { alteracoes: dados.data, aviso: req.query.message })
        })
        .catch(erro => res.status(520).jsonp(erro))
});

/* /alteracoes_pendentes/:rua
router.get('/:rua', function(req, res, next) {
    AlteracoesPendentes.alteracoes_pendentes_id(req.params.rua)
    .then(dados => 
        res.render('alteracoes', {alteracoes: dados})
    )
    .catch(erro => res.status(520).jsonp(erro))
});*/

// GET /alteracoes_pendentes/adicionar/:id
router.get('/adicionar/:id', verificaAcesso, function (req, res, next) {
    axios.get('http://localhost:2034/ruas/' + req.params.id)
        .then(dados => {
            res.render('sugerir_alteracao', { rua: dados.data })
        })
});

// POST /alteracoes_pendentes/adicionar
router.post('/adicionar', verificaAcesso, function (req, res, next) {
    const { nome: nomeBody, rua: ruaBody, descricao: descricaoBody, novafiguraid, novafiguraimagem, novafiguralegenda, novoenfiteutas, novoforo, novodesc, novavista } = req.body;

    //req.body
    const novaFigura = {
        id: novafiguraid,
        imagem: novafiguraimagem,
        legenda: novafiguralegenda
    };

    const novaCasa = {
        enfiteutas: novoenfiteutas,
        foro: novoforo,
        desc: novodesc,
        vista: novavista
    };

    let index = 0;
    const casas = [];
    while (true) {
        let casa = "casas[" + index + "]";
        let enfiteutas = casa + "[enfiteutas]";
        let foro = casa + "[foro]";
        let desc = casa + "[desc]";
        let vista = casa + "[vista]";
        if (req.body[enfiteutas] === undefined) break;
        casas.push({
            enfiteutas: req.body[enfiteutas],
            foro: req.body[foro],
            desc: req.body[desc],
            vista: req.body[vista]
        });
        index += 1;
    }
    casas.push(novaCasa);

    let index2 = 0;
    const figuras = [];
    while (true) {
        let figura = "figuras[" + index2 + "]";
        let id = figura + "[id]";
        let imagem = figura + "[imagem]";
        let legenda = figura + "[legenda]";
        if (req.body[id] === undefined) break;
        figuras.push({
            id: req.body[id],
            imagem: req.body[imagem],
            legenda: req.body[legenda]
        });
        index2 += 1;
    }
    figuras.push(novaFigura);

    doc = {
        rua: ruaBody,
        nome: nomeBody,
        figuras: figuras,
        descricao: descricaoBody,
        casas: casas
    };

    axios.post('http://localhost:2034/alteracoespendentes/adicionar', doc)
        .then(dados => {
            res.redirect('/ruas?message=Sucesso! Deve esperar pela confirmação do admin antes das alterações serem realizadas')
        })
        .catch(erro => res.status(520).jsonp(erro))
});


// /alteracoes_pendentes/validar/:id
router.get('/validar/:id', verificaAcesso, function (req, res, next) {
    axios.post('http://localhost:2034/alteracoespendentes/validar/' + req.params.id)
        .then(bool => {
            if (bool.data == true) res.status(200).redirect('/ruas?message=Alteracoes validadas com sucesso')
            else res.status(200).redirect('/alteracoespendentes?message=Erro ao validar alteracoes')
        })
        .catch(erro => res.status(520).jsonp(erro))
});

// /alteracoes_pendentes/eliminar/:id
router.get('/eliminar/:id', verificaAcesso, function (req, res, next) {
    axios.post('http://localhost:2034/alteracoespendentes/eliminar/' + req.params.id)
        .then(dados => {
            res.status(200).redirect('/alteracoespendentes?message=Alteracoes eliminadas com sucesso')
        })
        .catch(erro => res.status(520).jsonp(erro))
});

module.exports = router;