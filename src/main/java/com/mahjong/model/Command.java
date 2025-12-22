package com.mahjong.model;

/**
 * Commands for Client-Server communication.
 */
public enum Command {
    // Client -> Server
    LOGIN,          // Join server with nickname
    CREATE_ROOM,    // Create a new game room
    JOIN_ROOM,      // Join existing room
    PLAY_CARD,      // Discard a tile
    ACTION,         // Chow, Pong, Kong, Hu, Pass
    READY,          // Player is ready to start
    
    // Server -> Client
    LOGIN_SUCCESS,  // Login confirmed
    ROOM_UPDATE,    // Room state changed (players joined)
    GAME_START,     // Game loop starts
    GAME_UPDATE,    // New turn, tile drawn, etc.
    ACTION_REQUEST, // Asking player if they want to Chow/Pong/Kong
    GAME_OVER,      // Game end with scores
    ERROR           // Something went wrong
}
