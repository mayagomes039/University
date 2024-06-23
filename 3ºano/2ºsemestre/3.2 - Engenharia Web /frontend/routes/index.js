
const express = require('express');
const router = express.Router();
const axios = require('axios');
const cookieParser = require('cookie-parser');
const jwt = require('jsonwebtoken');


function verificaAcesso(req, res, next){
  // verificar se o token é válido
  if(req.cookies.token == undefined){
    res.redirect('/login')
  }

  else {
    axios.post("http://localhost:3434/users/verificar", { token: req.cookies.token }).
    then(dados => {
      req.mytoken = dados.data
      next()
    }).catch(e => {
      token = jwt.decode(req.cookies.token)
      res.clearCookie('token')
      res.redirect('/login')
    })
  }
}




/*

function verificaAcesso(req, res, next) {
  console.log("verificaAcesso");

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

   // if (decoded.role !== 'admin') {
   //   console.log("User does not have admin role, redirecting to login");
 //     return res.render('login', { message: "Acesso negado. Apenas administradores podem acessar." });
   // }
    // Token is valid, proceed to next middleware/route handler
    req.user = decoded;
    next();
  });
}*/

/* GET home page. */
router.get('/', verificaAcesso, function(req, res, next) {
  console.log("aqui. getHome");
  res.redirect('/login');
});


router.get('/login', function(req, res) {
  res.clearCookie('token')
  res.render('login');
}); 

router.get('/registar', function(req, res) {
  res.render('registar');
});

router.post('/registar', function(req, res) {
  axios.post('http://localhost:3434/users/register', req.body)
    .then(dados => {
      res.redirect('/login')
    })
    .catch(e => {
      res.render('login', { message: "Erro ao registar" });
    })
})

router.post('/login', function(req, res) {
  console.log(req.body);

  axios.post('http://localhost:3434/users/login', req.body)
    .then(response => {
      console.log("aqui. postLogin");

      // Corrected line to set cookie
      res.cookie('token', response.data.token);

      // Send a response back to the client
      //res.status(200).json({ message: 'Login successful', token: response.data.token });

      // Redirect to home page
      res.redirect('/ruas');


    })
    .catch(error => {
      console.error(error);
      res.status(500)
      res.render('errorlogin');
    });
});

module.exports = router;


