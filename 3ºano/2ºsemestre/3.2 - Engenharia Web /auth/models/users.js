const mongoose = require('mongoose');
const passportLocalMongoose = require('passport-local-mongoose');

const userSchema = new mongoose.Schema({
    _id: { type: String, required: true },
  username: { type: String, required: true },
  email: { type: String, required: true, unique: true },
  password: { type: String },
  filiacao: String,
  nivel: { type: String, enum: ['admin', 'consumidor'], required: true },
  dataRegisto: { type: Date},
  dataUltimoAcesso: { type: Date }
});

userSchema.plugin(passportLocalMongoose, { usernameField: 'username' });

module.exports = mongoose.model('User', userSchema);
