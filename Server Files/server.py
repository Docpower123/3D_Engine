import socket
import threading

# Define host and port
HOST = '192.168.1.110'  # Loopback address for localhost
PORT = 43434  # Arbitrary non-privileged port

# List to hold all client connections
clients = []

def handle_client(client_socket, client_address):
    try:
        while True:
            # Receive data from the client
            data = client_socket.recv(1024).decode('utf-8')
            if not data:
                break

            print(f"Received from {client_address}: {data}")

            # Broadcast the received data to all clients
            for c in clients:
                if c != client_socket:
                    c.sendall(data.encode('utf-8'))
    except Exception as e:
        print(f"An error occurred with client {client_address}: {e}")
    finally:
        # Remove the client from the list
        clients.remove(client_socket)
        client_socket.close()

def broadcast_message(message):
    # Send a message to all clients
    for client_socket in clients:
        try:
            client_socket.sendall(message.encode('utf-8'))
        except Exception as e:
            print(f"Error sending message to a client: {e}")

# Create a TCP socket
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind the socket to the address and port
server_socket.bind((HOST, PORT))

# Start listening for incoming connections
server_socket.listen()

print(f"Server listening on {HOST}:{PORT}")

while True:
    # Accept incoming connection
    client_socket, client_address = server_socket.accept()
    print(f"Connection established with {client_address}")

    # Add the client to the list
    clients.append(client_socket)

    # Create a new thread to handle the client
    client_thread = threading.Thread(target=handle_client, args=(client_socket, client_address))
    client_thread.start()

    # Send a welcome message to the client
    welcome_message = "Welcome to the chat server!"
    client_socket.sendall(welcome_message.encode('utf-8'))

    # Send a message to all clients about the new connection
    new_client_message = f"{client_address} has joined the chat."
    broadcast_message(new_client_message)
