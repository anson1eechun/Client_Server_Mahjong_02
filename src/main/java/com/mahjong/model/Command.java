package com.mahjong.model;

/**
 * Commands for Client-Server communication.
 */
public enum Command {
    // Client -> Server
    LOGIN,          // Join server with nickname
    PLAY_CARD,      // Discard a tile
    ACTION,         // Chow, Pong, Kong, Hu, Pass
    READY,          // Player is ready to start
    
    // Server -> Client
    LOGIN_SUCCESS,  // Login confirmed
    GAME_START,     // Game loop starts
    GAME_UPDATE,    // New turn, tile drawn, etc.
    ACTION_REQUEST, // Asking player if they want to Chow/Pong/Kong
    GAME_OVER,      // Game end with scores
    ERROR           // Something went wrong
}
