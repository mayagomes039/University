var express = require('express');
var router = express.Router();
var Users = require("../controllers/users.js");
const jwt = require('jsonwebtoken');
const passport = require('passport');
const User = require('../models/users.js');
var LocalStrategy = require('passport-local').Strategy
const cookieParser = require('cookie-parser');

var axios = require('axios')

var session = require('express-session');
const { randomInt } = require('crypto');



// GET /users 
router.get('/', function (req, res) {


  Users.users_list()
    .then(dados =>
      res.jsonp(dados)
    )
    .catch(erro => res.status(520).jsonp(erro))
});




// GET /users/:id
router.get('/:id', function (req, res, next) {
  Users.user_id(req.params.id)
    .then(dados =>
      res.jsonp(dados)
    )
    .catch(erro => res.status(520).jsonp(erro))
});

// POST /users
router.post('/', function (req, res, next) {
  console.log("/users POST")
  Users.create_user(req.body)
    .then(dados =>
      res.jsonp(dados)
    )
    .catch(erro => res.status(520).jsonp(erro))
});

// POST /users/register

router.post('/register', function (req, res) {
  console.log("/users/register POST")
  var d = new Date().toISOString().substring(0, 19);
  if(req.body.nivel == undefined) req.body.nivel = "consumidor"
  if(req.body._id == undefined) req.body._id = randomInt(1000000, 9999999)
  var newUser = new User({
    _id: req.body._id,
    username: req.body.username,
    email: req.body.email,
    filiacao: req.body.filiacao,
    nivel: req.body.nivel,
    dataRegisto: d,
    dataUltimoAcesso: ""
  });

  User.register(newUser, req.body.password, function (err, user) {


    if (err)
      res.status(520).jsonp({ error: err, message: "Register error: " + err })
    else
      res.status(201).jsonp('OK')
  });
  /*
    if (err) {
      res.status(205).jsonp({error: "User already exists"})
    } else {
      passport.authenticate("local")(req,res,function(){
        jwt.sign({ username: req.user.username, level: req.user.level, 
          sub: 'aula de EngWeb2024'}, 
          "EngWeb2024",
          {expiresIn: 3600},
          function(e, token) {
            if(e) res.status(500).jsonp({error: "Erro na geração do token: " + e}) 
            else res.status(201).jsonp({token: token})
          });
      })
      res.status(201).jsonp({message: "User registered"})
    }
  });*/
});

// POST /users/login
router.post('/login', passport.authenticate('local'), function (req, res) {
  jwt.sign({
    username: req.user.username,
    nivel: req.user.nivel,
    sub: 'projeto de EngWeb2024'
  },
    "EngWeb2024",
    { expiresIn: 3600 },
    function (e, token) {
      if (e) res.status(500).jsonp({ error: "Erro na geração do token: " + e })
      else res.status(201).jsonp({ token: token })
    });
})




/*
// POST /users/login
router.post('/login', async function(req, res, next) {
  try {
    // Verificar o username no corpo da requisição
    const user = await Users.findByUsername(req.body.username);
    
    if (!user) {
      return res.status(400).send('The user not found');
    }
    
    // Comparar a senha
    if (req.body.password === user.password) {
      return res.send('Success');
    } else {
      return res.status(400).send('Invalid Password');
    }
  } catch (error) {
    console.error(error);
    return res.status(500).send('Internal Server Error');
  }
});*/



router.post('/verificar', function (req, res) {
  jwt.verify(req.body.token, "EngWeb2024", function (e, payload) {
    if (e) {
      res.status(401).jsonp({ error: e })
    }
    else {
      res.status(200).jsonp(payload)
    }
  })
})

// PUT /users/:id
router.put('/:id', function (req, res, next) {

  Users.update_user(req.params.id, req.body)
    .then(dados =>
      res.jsonp(dados)
    )
    .catch(erro => res.status(520).jsonp(erro))
});

// DELETE /users/:id
router.delete('/:id', function (req, res, next) {
  Users.delete_user(req.params.id)
    .then(dados =>
      res.jsonp(dados)
    )
    .catch(erro => res.status(520).jsonp(erro))
});

module.exports = router;
