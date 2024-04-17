import random
import socket
import threading

# Define host and port
HOST = '127.0.0.1'  # Loopback address for localhost
PORT = 43434  # Arbitrary non-privileged port

# List to hold all client connections
clients = []

world_packet = []


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


def setup_world():
    Entities = []
    lights = []
    World = (20000, 2000)
    # Generate random entities for the world
    for i in range(2000):
        type = random.choice(
            ['fernModel', 'lowPolyTreeModel', 'treeModel', 'pineModel', 'toonRocksModel', 'grassModel', 'flowerModel'])
        ex = 4 * random.random()
        ez = 4 * random.random()
        scale = random.random() * 1 + 4
        entity = (type, ex, ez, scale)
        Entities.append(entity)
    # Generate random lights for the world
    for i in range(200):
        type = random.choice(['rocksModel', 'lampModel'])
        ex = random.random()
        ez = random.random()
        light = (type, ex, ez)
        lights.append(light)
    world_pack = (World, Entities, lights)
    return world_pack


# Create a TCP socket
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind the socket to the address and port
server_socket.bind((HOST, PORT))

# Start listening for incoming connections
server_socket.listen()

print(f"Server listening on {HOST}:{PORT}")
world_packet = setup_world()

while True:
    # Accept incoming connection
    client_socket, client_address = server_socket.accept()
    print(f"Connection established with {client_address}")

    # Check if the client is not already in the list, then greet them
    if client_socket not in clients:
        clients.append(client_socket)
        client_socket.sendall(world_packet.encode('utf-8'))

    # Create a new thread to handle the client
    client_thread = threading.Thread(target=handle_client, args=(client_socket, client_address))
    client_thread.start()
