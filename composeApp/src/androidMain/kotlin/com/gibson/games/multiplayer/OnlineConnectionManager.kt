package com.gibson.games.multiplayer

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class OnlineConnectionManager {
    private val database = FirebaseDatabase.getInstance()
    private var roomCode: String = ""
    private var listener: ChildEventListener? = null

    fun createRoom(
        onRoomCreated: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val code = generateRoomCode()
            roomCode = code

            database.reference
                .child("multiplayer_rooms")
                .child(code)
                .child("createdAt")
                .setValue(System.currentTimeMillis())
                .addOnSuccessListener {
                    MultiplayerSession.setOnlineConnection(
                        role = MultiplayerRole.HOST,
                        roomCode = code,
                        remoteName = "Online Player"
                    )
                    onRoomCreated(code)
                }
                .addOnFailureListener { error ->
                    onError(error.message ?: "Failed to create online room")
                }
        } catch (e: Exception) {
            onError(e.message ?: "Failed to create online room")
        }
    }

    fun joinRoom(
        code: String,
        onJoined: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanCode = code.trim().uppercase()

        if (cleanCode.isBlank()) {
            onError("Enter room code")
            return
        }

        database.reference
            .child("multiplayer_rooms")
            .child(cleanCode)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    roomCode = cleanCode
                    MultiplayerSession.setOnlineConnection(
                        role = MultiplayerRole.JOINER,
                        roomCode = cleanCode,
                        remoteName = "Online Host"
                    )
                    onJoined()
                } else {
                    onError("Room not found")
                }
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Failed to join online room")
            }
    }

    fun send(message: String) {
        val code = MultiplayerSession.onlineRoomCode.ifBlank { roomCode }
        if (code.isBlank()) return

        val data = mapOf(
            "senderId" to MultiplayerSession.localPlayerId,
            "message" to message,
            "time" to System.currentTimeMillis()
        )

        database.reference
            .child("multiplayer_rooms")
            .child(code)
            .child("messages")
            .push()
            .setValue(data)
    }

    fun startListening(
        onMessage: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stopListening()

        val code = MultiplayerSession.onlineRoomCode.ifBlank { roomCode }
        if (code.isBlank()) {
            onError("No online room connected")
            return
        }

        val messagesRef = database.reference
            .child("multiplayer_rooms")
            .child(code)
            .child("messages")

        listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val senderId = snapshot.child("senderId").getValue(String::class.java)
                val message = snapshot.child("message").getValue(String::class.java)

                if (senderId != null &&
                    senderId != MultiplayerSession.localPlayerId &&
                    !message.isNullOrBlank()
                ) {
                    onMessage(message)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        messagesRef.addChildEventListener(listener!!)
    }

    fun stopListening() {
        val code = MultiplayerSession.onlineRoomCode.ifBlank { roomCode }

        val currentListener = listener
        if (code.isNotBlank() && currentListener != null) {
            database.reference
                .child("multiplayer_rooms")
                .child(code)
                .child("messages")
                .removeEventListener(currentListener)
        }

        listener = null
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
