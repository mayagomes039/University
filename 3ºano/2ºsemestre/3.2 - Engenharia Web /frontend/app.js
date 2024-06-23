var createError = require('http-errors');
var express = require('express');
//cockies 
var cookieParser = require('cookie-parser');
var path = require('path');
var logger = require('morgan');

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');
var ruasRouter = require('./routes/ruas');
var alteracoesPendentesRouter = require('./routes/alteracoes_pendentes');

var app = express();

// view engine setup
app.use(express.static(path.join(__dirname, 'public')));
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');


app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(express.static(path.join(__dirname, 'public')));
//cookies
app.use(cookieParser());

app.use('/', indexRouter);
app.use('/users', usersRouter);
app.use('/ruas', ruasRouter);
app.use('/alteracoespendentes', alteracoesPendentesRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});






// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

app.get('/', (req, res) => {
  res.render('formlayout');
});


module.exports = app;
