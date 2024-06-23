var Users = require("../models/users");
var mongoose = require('mongoose');

// GET /users
module.exports.users_list = function() {
    return Users.find().lean().exec()
    .then(docs => {
        docs.sort((a, b) => a.username.localeCompare(b.username));
        return docs;
    })
    .catch(err => {
        console.log(err);
        return;
    });
}

// GET /users/:id
module.exports.user_id = function(id) {
    return Users.findById(id).exec()
    .then(doc => {
        if (!doc) {
            console.log('User not found');
            return null;
        }
        console.log(doc);
        return doc;
    })
    .catch(err => {
        console.log(err);
        return null;
    });
}
// DELETE /users/:id
module.exports.delete_user = function(id) {
    return Users.deleteOne({ _id: id })
    .then(doc => {
        return doc;
    })
    .catch(err => {
        console.log(err);
        return;
    });
}

// POST /users
module.exports.create_user = function(data) {
    var newUser = new Users(data);
    return newUser.save()
    .then(doc => {
        return doc;
    })
    .catch(err => {
        console.log(err);
        return err;
    });
}


module.exports.findByUsername = function(username) {
    return Users.findOne({ username: username }).exec()
    .then(doc => {
        if (!doc) {
            console.log('User not found');
            return null;
        }
        console.log(doc);
        return doc;
    })
    .catch(err => {
        console.log(err);
        return null;
    });
}