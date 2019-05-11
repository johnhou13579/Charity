var express = require('express');
var socket = require('socket.io');

var app = express();
var server = app.listen(2000,function(){
	console.log('listening to request on port 2000');
});

app.use(express.static('public'));

var io = socket(server);


io.sockets.on('connection', function(socket){

    socket.on('subscribe', function(data) { 
        console.log('joining room', data.room);
        socket.join(data.room); 
    })

    socket.on('unsubscribe', function(data) {  
        console.log('leaving room', data.room);
        socket.leave(data.room); 
    })

    socket.on('send', function(data) {
        console.log('sending message' +JSON.stringify(data));
        io.sockets.in(data.room).emit('message', data);
    });

    
});