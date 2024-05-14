import socket
import threading
import time
import random

# List to keep track of connected clients and their positions
clients = []
client_positions = {}  # Dictionary to store client positions


# Function to create the world data
def create_world():
    world = []
    terrain_size = 20000
    world.append(terrain_size)
    world.append(2000)

    # create lights entities
    for i in range(2, 200, 2):
        world.append(random.uniform(-terrain_size / 2, terrain_size / 2))
        world.append(random.uniform(-terrain_size / 2, terrain_size / 2))

    # create entities
    for i in range(201, 50000, 6):
        world.append(random.choice(
            ["treeModel", "lowPolyTreeModel", "pineModel", "grassModel", "flowerModel", "fernModel", "toonRocksModel"]))
        world.append(random.uniform(-terrain_size / 2, terrain_size / 2))
        world.append(random.uniform(-terrain_size / 2, terrain_size / 2))
        world.append(abs(random.random()))
        world.append(abs(random.random()))
        world.append(random.uniform(0.7, 3))
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

        # Receive and update client positions
        while True:
            data = client_socket.recv(1024)
            if not data:
                break
            position = data.decode('utf-8')
            #print("Received position from", addr, ":", position)
            client_positions[addr] = position  # Update client position
            update_positions()
            #print(client_positions)
    except Exception as e:
        print("Error handling client:", e)
    finally:
        # Remove client from the list and their position
        clients.remove((client_socket, addr))
        client_positions.pop(addr, None)
        client_socket.close()
        print("Connection closed with", addr)


# Function to broadcast updated positions to all connected clients
def update_positions():
    # Create a formatted string of all positions
    positions_data = '/p'.join(f"{addr}; {pos}" for addr, pos in client_positions.items())
    encoded_data = bytes(positions_data + '\n', 'utf-8')
    for client, _ in clients:
        try:
            print(encoded_data.decode())
            client.sendall(encoded_data)

        except Exception as e:
            print("Error broadcasting positions:", e)


# Function to start the server
def server():
    host = '192.168.1.164'
    port = 5005
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((host, port))
    server_socket.listen(5)
    print("Server listening on port", port)

    while True:
        client_socket, addr = server_socket.accept()
        client_thread = threading.Thread(target=handle_client, args=(client_socket, addr))
        client_thread.start()


if __name__ == "__main__":
    server()
