var createError = require('http-errors');
var express = require('express');
var path = require('path');
//var cookieParser = require('cookie-parser');
var logger = require('morgan');


var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');



var mongoose = require('mongoose')
var passport = require('passport')
var session = require('express-session');

var LocalStrategy = require('passport-local').Strategy



var mongoDB = 'mongodb://127.0.0.1/EWtp2024';

mongoose.connect(mongoDB, {useNewUrlParser: true , useUnifiedTopology: true})
var db = mongoose.connection;
db.on('error', console.error.bind(console, 'MongoDB connection error....'))

db.once('open', () => {
    console.log('Conexão ao MongoDB')
})


var app = express();
var User = require('./models/users.js');

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

app.use(logger('dev'));
//app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(express.json());
app.use(session({
  secret: 'EngWeb2024', // This should be a long random string
  resave: true,
  saveUninitialized: false
}));
//app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));
passport.use(new LocalStrategy(User.authenticate()));
passport.serializeUser(User.serializeUser());
passport.deserializeUser(User.deserializeUser());

app.use('/', indexRouter);
app.use('/users', usersRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});


app.use(passport.initialize());
app.use(passport.session());

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
