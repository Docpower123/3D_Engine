import socket
import threading
import time
import random

# List to keep track of connected clients
clients = []


# Function to create the world data
def create_world():
    world = []
    world.append(20000)  # Add elements to the list using append method
    world.append(2000)
    # create lights entities
    for i in range(2, 100, 2):
        world.append(abs(random.random()))
        world.append(abs(random.random()))
    # create entities
    for i in range(101, 2106, 6):
        world.append(random.choice(
            ["treeModel", "lowPolyTreeModel", "pineModel", "grassModel", "flowerModel", "fernModel", "toonRocksModel"]))
        world.append(abs(random.random()))  # ex
        world.append(abs(random.random()))  # ez
        world.append(abs(random.random()))  # rx
        world.append(abs(random.random()))  # rz
        world.append(random.uniform(0, 5))  # scale
    return world


# Function to handle each client connection
def handle_client(client_socket, addr):
    print("Connection from", addr)
    try:
        # Send world data to the client
        world_data = create_world()
        client_socket.sendall(bytes(str(world_data) + '\n', 'utf-8'))
        time.sleep(1)  # Introduce a delay for demonstration

        # Add client to the list of connected clients
        clients.append((client_socket, addr))

        # Receive and broadcast messages from the client
        while True:
            data = client_socket.recv(1024)
            if not data:
                break
            print("Received data from", addr, ":", data.decode('utf-8'))
            broadcast(data)
    except Exception as e:
        print("Error handling client:", e)
    finally:
        # Remove client from the list of connected clients
        clients.remove((client_socket, addr))
        # Close the connection
        client_socket.close()
        print("Connection closed with", addr)


# Function to broadcast messages to all connected clients
def broadcast(message):
    for client, _ in clients:
        try:
            client.sendall(message)
        except Exception as e:
            print("Error broadcasting message:", e)


# Function to start the server
def server():
    # Define host and port
    host = 'localhost'
    port = 12345

    # Create a socket object
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Bind the socket to the host and port
    server_socket.bind((host, port))

    # Listen for incoming connections
    server_socket.listen(5)

    print("Server listening on port", port)

    # Accept incoming connections and handle them in separate threads
    while True:
        # Accept incoming connection
        client_socket, addr = server_socket.accept()

        # Create a new thread to handle the client
        client_thread = threading.Thread(target=handle_client, args=(client_socket, addr))
        client_thread.start()


# Run the server
if __name__ == "__main__":
    server()
