var express = require('express');
var router = express.Router();



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

/* GET users listing. */
router.get('/', verificaAcesso,  function(req, res, next) {
  res.send('respond with a resource');
});

/*

router.get('/', async function(req, res, next) {
  await axios.get('http://localhost:3434/users')
      .then(response => {
          console.log('users');
          const message = req.query.message;
          res.render('users', { users: response.data, aviso: message });
      })
      .catch(error => {
        console.log('error');

          console.error('Error fetching data:', error);
          res.status(520).jsonp(error); 
      });
});*/
module.exports = router;
