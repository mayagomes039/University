const express = require('express');
const axios = require('axios');
const fs = require('fs');
const path = require('path');
const multer = require('multer');
const router = express.Router();

const upload = multer({ dest: 'uploads/' });

function verificaAcesso(req, res, next) {
    // verificar se o token é válido
    if (req.cookies.token == undefined) {
        res.redirect('/login')
    }

    else {
        axios.post("http://localhost:3434/users/verificar", { token: req.cookies.token }).
            then(dados => {
                req.mytoken = dados.data
                next()
            }).catch(e => {
                if (req.cookies.token != undefined) {
                    token = jwt.decode(req.cookies.token)
                    res.clearCookie('token')
                    res.redirect('/login')
                }
                else {
                    res.redirect('/login')
                }
            })
    }
}

// GET /ruas 
router.get('/', verificaAcesso, async function (req, res, next) {
    await axios.get('http://localhost:2034/ruas')
        .then(response => {
            const message = req.query.message;
            res.render('ruas', { ruas: response.data, aviso: message, nivel: req.mytoken.nivel});
        })
        .catch(error => {
            console.error('Error fetching data:', error);
            res.status(520).jsonp(error);
        });
});
// Get /ruas/registo
router.get('/registo', verificaAcesso, function (req, res, next) {
    axios.get('http://localhost:2034/ruas')
        .then(dados => {
            console.log("dados hi:" );
            res.render('form', { lista: dados.data });
        })
        .catch(erro => {
            res.render('error', { error: erro });
        });
});

// POST /ruas/registo
router.post('/registo', verificaAcesso, upload.single('myFile'), (req, res) => {
    if (req.file) {


        let oldPath = path.join(__dirname, '/../', req.file.path);
        let newPath = path.join(__dirname, '/../public/images/', req.file.originalname);

        fs.rename(oldPath, newPath, function (error) {
            if (error) {
                console.error("Error moving file:", error);
                return res.render("error", { "error": error });
            }

            var result = req.body;
            result.figuras = [{ id: req.body['figuras[0][id]'], imagem: '/images/' + req.file.originalname, legenda: req.body['figuras[0][legenda]'] }];
            console.log("carregou imagem");
            // verificar se o nivel do utilizaDOR ŕ consumidor ou admin 
            if (req.mytoken.nivel == "admin") {
                result.estado = "admin"
                console.log(result)
                
                axios.post("http://localhost:2034/ruas", result)
                .then(resp => {
                    res.redirect('/ruas');
                })
                .catch(erro => {
                    res.render("error", { "error": erro });
                });
            }
            if (req.mytoken.nivel == "consumidor") {
                console.log("consumidor")
                axios.post("http://localhost:2034/alteracoespendentes/adicionar", result)
                .then(dados => {
                    res.redirect('/ruas?message=Sucesso! Deve esperar pela confirmação do admin antes das alterações serem realizadas')
                })
                .catch(erro => {
                    res.render("error", { "error": erro });
                });
            } 
        });
    } else {
        console.log("No file uploaded");
        // Handle the case where no file was uploaded
        res.render("error", { "error": "No file uploaded" });
    }
});


// GET /ruas/:rua
router.get('/:rua', verificaAcesso, async function (req, res, next) {
    await axios.get('http://localhost:2034/ruas/' + req.params.rua, nivel = req.mytoken.nivel)
        .then(response => {
            console.log("dados recebidos:" + response.data);
            res.render('rua', { rua: response.data });
        })
        .catch(error => {
            console.error('Error fetching data:', error);
            res.status(520).jsonp(error);
        });
});

module.exports = router;
